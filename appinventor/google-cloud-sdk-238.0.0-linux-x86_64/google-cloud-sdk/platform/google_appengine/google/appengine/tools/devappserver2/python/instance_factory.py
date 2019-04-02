#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
"""Serves content for "script" handlers using the Python runtime."""



import logging
import os
import shutil
import subprocess
import sys
import tempfile
import time

import google
from google.appengine.api import appinfo
from google.appengine.tools.devappserver2 import application_configuration
from google.appengine.tools.devappserver2 import errors
from google.appengine.tools.devappserver2 import http_runtime
from google.appengine.tools.devappserver2 import instance

_RUNTIME_PATH = os.path.abspath(
    os.path.join(os.path.dirname(sys.argv[0]), '_python_runtime.py'))
_RUNTIME_ARGS = [sys.executable, _RUNTIME_PATH]

_MODERN_DEFAULT_ENTRYPOINT = 'gunicorn -b :${PORT} main:app'

_DEFAULT_REQUIREMENT_FILE_NAME = 'requirements.txt'

_RECREATE_MODERN_INSTANCE_FACTORY_CONFIG_CHANGES = set([
    application_configuration.ENTRYPOINT_ADDED,
    application_configuration.ENTRYPOINT_REMOVED])


# TODO: Refactor this factory class for modern runtimes.
class PythonRuntimeInstanceFactory(instance.InstanceFactory,
                                   instance.ModernInstanceFactoryMixin):
  """A factory that creates new Python runtime Instances."""

  START_URL_MAP = appinfo.URLMap(
      url='/_ah/start',
      script='$PYTHON_LIB/default_start_handler.py',
      login='admin')
  WARMUP_URL_MAP = appinfo.URLMap(
      url='/_ah/warmup',
      script='$PYTHON_LIB/default_warmup_handler.py',
      login='admin')
  SUPPORTS_INTERACTIVE_REQUESTS = True
  FILE_CHANGE_INSTANCE_RESTART_POLICY = instance.AFTER_FIRST_REQUEST

  def _is_modern(self):
    return self._module_configuration.runtime.startswith('python3')

  def _CheckPythonExecutable(self):
    try:
      version_str = subprocess.check_output(['python3', '--version'])
      logging.info('Detected %s', version_str)
    except OSError:  # If python3 is not found, an OSError would be raised.
      raise errors.Python3NotFoundError(
          'Could not find python3 executable. Check to make sure that Python 3 '
          'is installed, and that python3 is on your PATH.')

  def __init__(self, request_data, runtime_config_getter, module_configuration):
    """Initializer for PythonRuntimeInstanceFactory.

    Args:
      request_data: A wsgi_request_info.WSGIRequestInfo that will be provided
          with request information for use by API stubs.
      runtime_config_getter: A function that can be called without arguments
          and returns the runtime_config_pb2.Config containing the configuration
          for the runtime.
      module_configuration: An application_configuration.ModuleConfiguration
          instance respresenting the configuration of the module that owns the
          runtime.
    """
    super(PythonRuntimeInstanceFactory, self).__init__(
        request_data,
        8 if runtime_config_getter().threadsafe else 1, 10)
    self._runtime_config_getter = runtime_config_getter
    self._module_configuration = module_configuration
    self._venv_dir = ''
    if self._is_modern():
      self._CheckPythonExecutable()
      self._SetupVirtualenvFromConfiguration()

  def __del__(self):
    self._CleanUpVenv(self._venv_dir)

  @classmethod
  def _CleanUpVenv(cls, venv_dir):
    if os.path.exists(venv_dir):
      shutil.rmtree(venv_dir)

  @property
  def _OrigRequirementsFile(self):
    return os.path.join(
        os.path.dirname(self._module_configuration.config_path),
        _DEFAULT_REQUIREMENT_FILE_NAME)

  @property
  def _entrypoint(self):
    """Returns the entrypoint as is in module configuration."""
    return self._module_configuration.entrypoint

  def _SetupVirtualenvFromConfiguration(self):
    self._CleanUpVenv(self._venv_dir)
    self._venv_dir = tempfile.mkdtemp()

    if self._entrypoint:
      self.venv_env_vars = self._SetupVirtualenv(
          self._venv_dir, self._OrigRequirementsFile)
    else:  # use default entrypoint
      # Copy requirements.txt into a temporary file. It will be destroyed once
      # the life of self._requirements_file ends. It is created in a directory
      # different from venv_dir so that venv_dir starts clean.
      with tempfile.NamedTemporaryFile() as requirements_file:
        # Make a copy of user requirements.txt, the copy is safe to modify.
        if os.path.exists(self._OrigRequirementsFile):
          with open(self._OrigRequirementsFile, 'r') as orig_f:
            requirements_file.write(orig_f.read())

        # Similar to production, append gunicorn to requirements.txt
        # as default entrypoint needs it.
        requirements_file.write('\ngunicorn')

        # flushing it because _SetupVirtualenv uses it in a separate process.
        requirements_file.flush()
        self.venv_env_vars = self._SetupVirtualenv(
            self._venv_dir, requirements_file.name)

  def configuration_changed(self, config_changes):
    """Called when the configuration of the module has changed.

    Args:
      config_changes: A set containing the changes that occoured. See the
          *_CHANGED constants in the application_configuration module.
    """
    if config_changes & _RECREATE_MODERN_INSTANCE_FACTORY_CONFIG_CHANGES:
      self._SetupVirtualenvFromConfiguration()

  def dependency_libraries_changed(self, file_changes):
    """Decide whether dependency libraries in requirements.txt changed.

    If these libraries changed, recreate virtualenv with updated
    requirements.txt. This should only be called for python3+ runtime.

    Args:
      file_changes: A set of strings, representing paths to file changes.

    Returns:
      A bool indicating whether dependency libraries changed.
    """
    dep_libs_changed = None
    if self._is_modern():
      dep_libs_changed = next(
          (x for x in file_changes if x.endswith(
              _DEFAULT_REQUIREMENT_FILE_NAME)), None)
      if dep_libs_changed:
        self._SetupVirtualenvFromConfiguration()
    return dep_libs_changed is not None

  def _GetRuntimeArgs(self):
    if self._is_modern():
      return (self._entrypoint or _MODERN_DEFAULT_ENTRYPOINT).split()
    else:
      return _RUNTIME_ARGS

  @classmethod
  def _WaitForProcWithLastLineStreamed(cls, proc, proc_stdout):
    # Stream the last line of a process output, so that users can see
    # progress instead of doubting dev_appserver hangs.
    while proc.poll() is None:  # in progress
      lastline = proc_stdout.readline().strip()
      if lastline:
        sys.stdout.write(lastline)
        sys.stdout.flush()
        # Erase previous lastline.
        sys.stdout.write(
            '\b'*len(lastline) + ' '*len(lastline) + '\b'*len(lastline))
        time.sleep(0.2)
    sys.stdout.write('\n')
    return proc.poll()

  @classmethod
  def _RunPipInstall(cls, venv_dir, requirements_file_name):
    """Run pip install inside a virtualenv, with decent stdout."""
    # Run pip install based on user supplied requirements.txt.
    pip_out = tempfile.NamedTemporaryFile(delete=False)
    logging.info(
        'Using pip to install dependency libraries; pip stdout is redirected '
        'to %s', pip_out.name)
    with open(pip_out.name, 'r') as pip_out_r:
      pip_path = os.path.join(venv_dir, 'bin', 'pip')
      for pip_cmd in [[pip_path, 'install', '--upgrade', 'pip'],
                      [pip_path, 'install', '-r', requirements_file_name]]:
        cmd_str = ' '.join(pip_cmd)
        logging.info('Running %s', cmd_str)
        pip_proc = subprocess.Popen(pip_cmd, stdout=pip_out)
        if cls._WaitForProcWithLastLineStreamed(pip_proc, pip_out_r) != 0:
          sys.exit('Failed to run "{}"'.format(cmd_str))

  @classmethod
  def _SetupVirtualenv(cls, venv_dir, requirements_file_name):
    """Create virtualenv for py3 instances and run pip install."""
    # Create a clean virtualenv
    call_res = subprocess.call(['python3', '-m', 'venv', venv_dir])
    if call_res:
      # `python3 -m venv` Failed.
      # Clean up venv_dir and try 'virtualenv' command instead.
      cls._CleanUpVenv(venv_dir)
      call_res = subprocess.call(['virtualenv', venv_dir])
      if call_res:
        raise IOError('Cannot create virtualenv {}'.format(venv_dir))
    cls._RunPipInstall(venv_dir, requirements_file_name)

    # These env vars are used in subprocess to have the same effect as running
    # `source ${venv_dir}/bin/activate`
    return {
        'VIRTUAL_ENV': venv_dir,
        'PATH': ':'.join(
            [os.path.join(venv_dir, 'bin'), os.environ['PATH']])
    }

  def _GetRuntimeEnvironmentVariables(self, instance_id=None):
    if self._is_modern():
      res = {'PYTHONHASHSEED': 'random'}
      res.update(self.get_modern_env_vars(instance_id))
      res.update(self.venv_env_vars)
    else:
      # TODO: Do not pass os.environ to local python27 runtime.
      res = dict(os.environ, PYTHONHASHSEED='random')
    for kv in self._runtime_config_getter().environ:
      res[kv.key] = kv.value
    return res

  def _get_process_flavor(self):
    return (http_runtime.START_PROCESS_WITH_ENTRYPOINT
            if self._is_modern() else http_runtime.START_PROCESS_REVERSE)

  def new_instance(self, instance_id, expect_ready_request=False):
    """Create and return a new Instance.

    Args:
      instance_id: A string or integer representing the unique (per module) id
          of the instance.
      expect_ready_request: If True then the instance will be sent a special
          request (i.e. /_ah/warmup or /_ah/start) before it can handle external
          requests.

    Returns:
      The newly created instance.Instance.
    """
    def instance_config_getter():
      runtime_config = self._runtime_config_getter()
      runtime_config.instance_id = str(instance_id)
      return runtime_config
    proxy = http_runtime.HttpRuntimeProxy(
        self._GetRuntimeArgs(),
        instance_config_getter,
        self._module_configuration,
        env=self._GetRuntimeEnvironmentVariables(instance_id),
        start_process_flavor=self._get_process_flavor())
    return instance.Instance(self.request_data,
                             instance_id,
                             proxy,
                             self.max_concurrent_requests,
                             self.max_background_threads,
                             expect_ready_request)
