#!/usr/bin/env python
# -*- coding: utf-8 -*- #
#
# Copyright 2013 Google Inc. All Rights Reserved.
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

"""gcloud command line tool."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import time
START_TIME = time.time()

# pylint:disable=g-bad-import-order
# pylint:disable=g-import-not-at-top, We want to get the start time first.
import errno
import os
import sys

from googlecloudsdk.api_lib.iamcredentials import util as iamcred_util
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import cli
from googlecloudsdk.command_lib import crash_handling
from googlecloudsdk.command_lib.util.apis import yaml_command_translator
from googlecloudsdk.core import config
from googlecloudsdk.core import log
from googlecloudsdk.core import metrics
from googlecloudsdk.core import properties
from googlecloudsdk.core.credentials import store as creds_store
from googlecloudsdk.core.updater import local_state
from googlecloudsdk.core.updater import update_manager
from googlecloudsdk.core.util import keyboard_interrupt
from googlecloudsdk.core.util import platforms
import surface


# Disable stack traces when the command is interrupted.
keyboard_interrupt.InstallHandler()


if not config.Paths().sdk_root:
  # Don't do update checks if there is no install root.
  properties.VALUES.component_manager.disable_update_check.Set(True)


def UpdateCheck(command_path, **unused_kwargs):
  try:
    update_manager.UpdateManager.PerformUpdateCheck(command_path=command_path)
  # pylint:disable=broad-except, We never want this to escape, ever. Only
  # messages printed should reach the user.
  except Exception:
    log.debug('Failed to perform update check.', exc_info=True)


def CreateCLI(surfaces, translator=None):
  """Generates the gcloud CLI from 'surface' folder with extra surfaces.

  Args:
    surfaces: list(tuple(dot_path, dir_path)), extra commands or subsurfaces
              to add, where dot_path is calliope command path and dir_path
              path to command group or command.
    translator: yaml_command_translator.Translator, an alternative translator.
  Returns:
    calliope cli object.
  """
  def VersionFunc():
    generated_cli.Execute(['version'])

  def HandleKnownErrorFunc():
    crash_handling.ReportError(is_crash=False)

  pkg_root = os.path.dirname(os.path.dirname(surface.__file__))
  loader = cli.CLILoader(
      name='gcloud',
      command_root_directory=os.path.join(pkg_root, 'surface'),
      allow_non_existing_modules=True,
      version_func=VersionFunc,
      known_error_handler=HandleKnownErrorFunc,
      yaml_command_translator=(translator or
                               yaml_command_translator.Translator()),
  )
  loader.AddReleaseTrack(base.ReleaseTrack.ALPHA,
                         os.path.join(pkg_root, 'surface', 'alpha'),
                         component='alpha')
  loader.AddReleaseTrack(base.ReleaseTrack.BETA,
                         os.path.join(pkg_root, 'surface', 'beta'),
                         component='beta')

  for dot_path, dir_path in surfaces:
    loader.AddModule(dot_path, dir_path, component=None)

  # Check for updates on shutdown but not for any of the updater commands.
  # Skip update checks for 'gcloud version' command as it does that manually.
  exclude_commands = r'gcloud\.components\..*|gcloud\.version'
  loader.RegisterPostRunHook(UpdateCheck, exclude_commands=exclude_commands)
  generated_cli = loader.Generate()
  return generated_cli


def main(gcloud_cli=None, credential_providers=None):
  if not platforms.PythonVersion().IsCompatible(
      allow_py3=properties.VALUES.core.allow_py3.GetBool()):
    sys.exit(1)
  metrics.Started(START_TIME)
  # TODO(b/36049857): Put a real version number here
  metrics.Executions(
      'gcloud',
      local_state.InstallationState.VersionForInstalledComponent('core'))
  if gcloud_cli is None:
    gcloud_cli = CreateCLI([])

  # Register some other sources for credentials and project.
  credential_providers = credential_providers or [
      creds_store.DevShellCredentialProvider(),
      creds_store.GceCredentialProvider(),
  ]
  for provider in credential_providers:
    provider.Register()
  # Register support for service account impersonation.
  creds_store.IMPERSONATION_TOKEN_PROVIDER = (
      iamcred_util.ImpersonationAccessTokenProvider())

  try:
    try:
      gcloud_cli.Execute()
    except IOError as err:
      # We want to ignore EPIPE IOErrors.
      # By default, Python ignores SIGPIPE (see
      # http://utcc.utoronto.ca/~cks/space/blog/python/SignalExceptionSurprise).
      # This means that attempting to write any output to a closed pipe (e.g. in
      # the case of output piped to `head` or `grep -q`) will result in an
      # IOError, which gets reported as a gcloud crash. We don't want this
      # behavior, so we ignore EPIPE (it's not a real error; it's a normal thing
      # to occur).
      # Before, we restore the SIGPIPE signal handler, but that caused issues
      # with scripts/programs that wrapped gcloud.
      if err.errno != errno.EPIPE:
        raise
  except Exception as err:  # pylint:disable=broad-except
    crash_handling.HandleGcloudCrash(err)
    if properties.VALUES.core.print_unhandled_tracebacks.GetBool():
      # We want to see the traceback as normally handled by Python
      raise
    else:
      # This is the case for most non-Cloud SDK developers. They shouldn't see
      # the full stack trace, but just the nice "gcloud crashed" message.
      sys.exit(1)
  finally:
    for provider in credential_providers:
      provider.UnRegister()


if __name__ == '__main__':
  try:
    main()
  except KeyboardInterrupt:
    keyboard_interrupt.HandleInterrupt()
