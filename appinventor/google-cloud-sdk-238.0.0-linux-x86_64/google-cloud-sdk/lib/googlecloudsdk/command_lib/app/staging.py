# -*- coding: utf-8 -*- #
# Copyright 2016 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""Code to provide a hook for staging.

Some App Engine runtimes require an additional staging step before deployment
(e.g. when deploying compiled artifacts, or vendoring code that normally lives
outside of the app directory). This module contains (1) a registry mapping
runtime/environment combinations to staging commands, and (2) code to run said
commands.

The interface is defined as follows:

- A staging command is an executable (binary or script) that takes two
  positional parameters: the path of the `<service>.yaml` in the directory
  containing the unstaged application code, and the path of an empty directory
  in which to stage the application code.
- On success, the STDOUT and STDERR of the staging command are logged at the
  INFO level. On failure, a StagingCommandFailedError is raised containing the
  STDOUT and STDERR of the staging command (which are surfaced to the user as an
  ERROR message).
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import abc
import io
import os
import re
import tempfile

from googlecloudsdk.api_lib.app import env
from googlecloudsdk.api_lib.app import runtime_registry
from googlecloudsdk.command_lib.util import java
from googlecloudsdk.core import config
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import execution_utils
from googlecloudsdk.core import log
from googlecloudsdk.core.updater import update_manager
from googlecloudsdk.core.util import files
from googlecloudsdk.core.util import platforms
import six


_JAVA_APPCFG_ENTRY_POINT = 'com.google.appengine.tools.admin.AppCfg'

_JAVA_APPCFG_STAGE_FLAGS = ['--enable_new_staging_defaults']

_STAGING_COMMAND_OUTPUT_TEMPLATE = """\
------------------------------------ STDOUT ------------------------------------
{out}\
------------------------------------ STDERR ------------------------------------
{err}\
--------------------------------------------------------------------------------
"""


class StagingCommandNotFoundError(exceptions.Error):
  """Base error indicating that a staging command could not be found."""


class NoSdkRootError(StagingCommandNotFoundError):

  def __init__(self):
    super(NoSdkRootError, self).__init__(
        'No SDK root could be found. Please check your installation.')


class StagingCommandFailedError(exceptions.Error):

  def __init__(self, args, return_code, output_message):
    super(StagingCommandFailedError, self).__init__(
        'Staging command [{0}] failed with return code [{1}].\n\n{2}'.format(
            ' '.join(args), return_code, output_message))


# TODO(b/65026284): eliminate "mappers" entirely by making a shim command
def _JavaStagingMapper(command_path, descriptor, app_dir, staging_dir):
  """Map a java staging request to the right args.

  Args:
    command_path: str, path to the jar tool file.
    descriptor: str, path to the `appengine-web.xml`
    app_dir: str, path to the unstaged app directory
    staging_dir: str, path to the empty staging dir

  Raises:
    java.JavaError, if Java is not installed.

  Returns:
    [str], args for executable invocation.
  """
  del descriptor  # Unused, app_dir is sufficient
  java_bin = java.RequireJavaInstalled('local staging for java')
  args = ([java_bin, '-classpath', command_path, _JAVA_APPCFG_ENTRY_POINT] +
          _JAVA_APPCFG_STAGE_FLAGS + ['stage', app_dir, staging_dir])
  return args


class _Command(six.with_metaclass(abc.ABCMeta, object)):
  """Interface for a staging command to be invoked on the user source.

  This abstract class facilitates running an executable command that conforms to
  the "staging command" interface outlined in the module docstring.

  It implements the parts that are common to any such command while allowing
  interface implementors to swap out how the command is created.
  """

  @abc.abstractmethod
  def EnsureInstalled(self):
    """Ensure that the command is installed and available.

    May result in a command restart if installation is required.
    """
    raise NotImplementedError()

  @abc.abstractmethod
  def GetPath(self):
    """Returns the path to the command.

    Returns:
      str, the path to the command

    Raises:
      StagingCommandNotFoundError: if the staging command could not be found.
    """
    raise NotImplementedError()

  def GetArgs(self, descriptor, app_dir, staging_dir):
    """Get the args for the command to execute.

    Args:
      descriptor: str, path to the unstaged <service>.yaml or appengine-web.xml
      app_dir: str, path to the unstaged app directory
      staging_dir: str, path to the directory to stage in.

    Returns:
      list of str, the args for the command to run
    """
    return [self.GetPath(), descriptor, app_dir, staging_dir]

  def Run(self, staging_area, descriptor, app_dir):
    """Invokes a staging command with a given <service>.yaml and temp dir.

    Args:
      staging_area: str, path to the staging area.
      descriptor: str, path to the unstaged <service>.yaml or appengine-web.xml
      app_dir: str, path to the unstaged app directory

    Returns:
      str, the path to the staged directory or None if staging was not required.

    Raises:
      StagingCommandFailedError: if the staging command process exited non-zero.
    """
    staging_dir = tempfile.mkdtemp(dir=staging_area)
    args = self.GetArgs(descriptor, app_dir, staging_dir)
    log.info('Executing staging command: [{0}]\n\n'.format(' '.join(args)))
    out = io.StringIO()
    err = io.StringIO()
    return_code = execution_utils.Exec(args, no_exit=True, out_func=out.write,
                                       err_func=err.write)
    message = _STAGING_COMMAND_OUTPUT_TEMPLATE.format(out=out.getvalue(),
                                                      err=err.getvalue())
    log.info(message)
    if return_code:
      raise StagingCommandFailedError(args, return_code, message)
    return staging_dir


class NoopCommand(_Command):
  """A command that does nothing.

  Many runtimes do not require a staging step; this isn't a problem.
  """

  def EnsureInstalled(self):
    pass

  def GetPath(self):
    return None

  def GetArgs(self, descriptor, app_dir, staging_dir):
    return None

  def Run(self, staging_area, descriptor, app_dir):
    """Does nothing."""
    pass

  def __eq__(self, other):
    return isinstance(other, NoopCommand)


class _BundledCommand(_Command):
  """Represents a cross-platform command.

  Paths are relative to the Cloud SDK Root directory.

  Attributes:
    _nix_path: str, the path to the executable on Linux and OS X
    _windows_path: str, the path to the executable on Windows
    _component: str or None, the name of the Cloud SDK component which contains
      the executable
    _mapper: fn or None, function that maps a staging invocation to a command.
  """

  def __init__(self, nix_path, windows_path, component=None, mapper=None):
    self._nix_path = nix_path
    self._windows_path = windows_path
    self._component = component
    self._mapper = mapper or None

  @property
  def name(self):
    if platforms.OperatingSystem.Current() is platforms.OperatingSystem.WINDOWS:
      return self._windows_path
    else:
      return self._nix_path

  def GetPath(self):
    """Returns the path to the command.

    Returns:
      str, the path to the command

    Raises:
       NoSdkRootError: if no Cloud SDK root could be found (and therefore the
       command is not installed).
    """
    sdk_root = config.Paths().sdk_root
    if not sdk_root:
      raise NoSdkRootError()
    return os.path.join(sdk_root, self.name)

  def GetArgs(self, descriptor, app_dir, staging_dir):
    if self._mapper:
      return self._mapper(self.GetPath(), descriptor, app_dir, staging_dir)
    else:
      return super(_BundledCommand, self).GetArgs(descriptor, app_dir,
                                                  staging_dir)

  def EnsureInstalled(self):
    if self._component is None:
      return
    msg = ('The component [{component}] is required for staging this '
           'application.').format(component=self._component)
    update_manager.UpdateManager.EnsureInstalledAndRestart([self._component],
                                                           msg=msg)


class ExecutableCommand(_Command):
  """Represents a command that the user supplies.

  Attributes:
    _path: str, full path to the executable.
  """

  def __init__(self, path):
    self._path = path

  @property
  def name(self):
    os.path.basename(self._path)

  def GetPath(self):
    return self._path

  def EnsureInstalled(self):
    pass

  def GetArgs(self, descriptor, app_dir, staging_dir):
    return [self.GetPath(), descriptor, app_dir, staging_dir]

  @classmethod
  def FromInput(cls, executable):
    """Returns the command corresponding to the user input.

    Could be either of:
    - command on the $PATH or %PATH%
    - full path to executable (absolute or relative)

    Args:
      executable: str, the user-specified staging exectuable to use

    Returns:
      _Command corresponding to the executable

    Raises:
      StagingCommandNotFoundError: if the executable couldn't be found
    """
    try:
      path = files.FindExecutableOnPath(executable)
    except ValueError:
      # If this is a path (e.g. with os.path.sep in the string),
      # FindExecutableOnPath throws an exception
      path = None
    if path:
      return cls(path)

    if os.path.exists(executable):
      return cls(executable)

    raise StagingCommandNotFoundError('The provided staging command [{}] could '
                                      'not be found.'.format(executable))


# Path to the go-app-stager binary
_GO_APP_STAGER_DIR = os.path.join('platform', 'google_appengine')

# Path to the jar which contains the staging command
_APPENGINE_TOOLS_JAR = os.path.join(
    'platform', 'google_appengine', 'google', 'appengine', 'tools', 'java',
    'lib', 'appengine-tools-api.jar')

_STAGING_REGISTRY = {
    runtime_registry.RegistryEntry(
        re.compile(r'(go|go1\..+)$'), {
            env.FLEX, env.MANAGED_VMS
        }):
        _BundledCommand(
            os.path.join(_GO_APP_STAGER_DIR, 'go-app-stager'),
            os.path.join(_GO_APP_STAGER_DIR, 'go-app-stager.exe'),
            component='app-engine-go'),
    runtime_registry.RegistryEntry(
        re.compile(r'(go|go1\..+|%s)$' % env.GO_TI_RUNTIME_EXPR.pattern), {
            env.STANDARD,
        }):
        _BundledCommand(
            os.path.join(_GO_APP_STAGER_DIR, 'go-app-stager'),
            os.path.join(_GO_APP_STAGER_DIR, 'go-app-stager.exe'),
            component='app-engine-go'),
    runtime_registry.RegistryEntry('java-xml', {env.STANDARD}):
        _BundledCommand(
            _APPENGINE_TOOLS_JAR,
            _APPENGINE_TOOLS_JAR,
            component='app-engine-java',
            mapper=_JavaStagingMapper),
}

# _STAGING_REGISTRY_BETA extends _STAGING_REGISTRY, overriding entries if the
# same key is used.
_STAGING_REGISTRY_BETA = {}


class Stager(object):

  def __init__(self, registry, staging_area):
    self.registry = registry
    self.staging_area = staging_area

  def Stage(self, descriptor, app_dir, runtime, environment):
    """Stage the given deployable or do nothing if N/A.

    Args:
      descriptor: str, path to the unstaged <service>.yaml or appengine-web.xml
      app_dir: str, path to the unstaged app directory
      runtime: str, the name of the runtime for the application to stage
      environment: api_lib.app.env.Environment, the environment for the
          application to stage

    Returns:
      str, the path to the staged directory or None if no corresponding staging
          command was found.

    Raises:
      NoSdkRootError: if no Cloud SDK installation root could be found.
      StagingCommandFailedError: if the staging command process exited non-zero.
    """
    command = self.registry.Get(runtime, environment)
    if not command:
      return None
    command.EnsureInstalled()
    return command.Run(self.staging_area, descriptor, app_dir)


def GetRegistry():
  return runtime_registry.Registry(_STAGING_REGISTRY, default=NoopCommand())


def GetBetaRegistry():
  mappings = _STAGING_REGISTRY.copy()
  mappings.update(_STAGING_REGISTRY_BETA)
  return runtime_registry.Registry(mappings, default=NoopCommand())


def GetStager(staging_area):
  """Get the default stager."""
  return Stager(GetRegistry(), staging_area)


def GetBetaStager(staging_area):
  """Get the beta stager, used for `gcloud beta *` commands."""
  return Stager(GetBetaRegistry(), staging_area)


def GetNoopStager(staging_area):
  """Get a stager with an empty registry."""
  return Stager(
      runtime_registry.Registry({}, default=NoopCommand()), staging_area)


def GetOverrideStager(command, staging_area):
  """Get a stager with a registry that always calls the given command."""
  return Stager(
      runtime_registry.Registry(None, override=command, default=NoopCommand()),
      staging_area)
