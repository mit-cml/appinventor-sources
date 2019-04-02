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
"""Provides a GoRuntimeInstanceFactory for Go runtime instances.

The instances serve content for "script" handlers. In the case that an errant
application is provided, a _GoBuildFailureRuntimeProxy is provided which serves
500s with the application stack trace as the response body.
"""



import logging
import os
import sys
import threading

from google.appengine.api import appinfo
from google.appengine.tools.devappserver2 import application_configuration

from google.appengine.tools.devappserver2 import http_runtime
from google.appengine.tools.devappserver2 import instance
from google.appengine.tools.devappserver2 import metrics
from google.appengine.tools.devappserver2 import util
from google.appengine.tools.devappserver2.go import application as go_application
from google.appengine.tools.devappserver2.go import errors as go_errors
from google.appengine.tools.devappserver2.go import gaego
from google.appengine.tools.devappserver2.go import managedvm as go_managedvm

_REBUILD_CONFIG_CHANGES = frozenset(
    [application_configuration.SKIP_FILES_CHANGED,
     application_configuration.NOBUILD_FILES_CHANGED])


class _GoBuildFailureRuntimeProxy(instance.RuntimeProxy):
  """Serves an error page for a Go application build failure.

  When there is an error in the user's application code, a 500 is served with
  the error stack trace in the response body. This is similar to how webapp2
  behaves with the debug=True flag for Python applictions.
  """

  def __init__(self, failure_exception):
    self._failure_exception = failure_exception

  def start(self):
    pass

  def quit(self):
    pass

  def handle(self, environ, start_response, url_map, match, request_id,
             request_type):
    """Serves a request by displaying an error page.

    Args:
      environ: An environ dict for the request as defined in PEP-333.
      start_response: A function with semantics defined in PEP-333.
      url_map: An appinfo.URLMap instance containing the configuration for the
          handler matching this request.
      match: A re.MatchObject containing the result of the matched URL pattern.
      request_id: A unique string id associated with the request.
      request_type: The type of the request. See instance.*_REQUEST module
          constants.

    Yields:
      A sequence of strings containing the body of the HTTP response.
    """
    start_response('500 Internal Server Error',
                   [('Content-Type', 'text/plain; charset=utf-8')])
    yield 'The Go application could not be built.\n'
    yield '\n'
    yield str(self._failure_exception)


class GoRuntimeInstanceFactory(instance.InstanceFactory):
  """A factory that creates new Go runtime Instances."""

  START_URL_MAP = appinfo.URLMap(
      url='/_ah/start',
      script='_go_app',
      login='admin')
  WARMUP_URL_MAP = appinfo.URLMap(
      url='/_ah/warmup',
      script='_go_app',
      login='admin')
  FILE_CHANGE_INSTANCE_RESTART_POLICY = instance.ALWAYS

  def __init__(self, request_data, runtime_config_getter, module_configuration):
    """Initializer for GoRuntimeInstanceFactory.

    Args:
      request_data: A wsgi_request_info.WSGIRequestInfo that will be provided
          with request information for use by API stubs.
      runtime_config_getter: A function that can be called without arguments
          and returns the runtime_config_pb2.RuntimeConfig containing the
          configuration for the runtime.
      module_configuration: An application_configuration.ModuleConfiguration
          instance respresenting the configuration of the module that owns the
          runtime.
    """
    super(GoRuntimeInstanceFactory, self).__init__(request_data, 8, 10)
    self._runtime_config_getter = runtime_config_getter
    self._module_configuration = module_configuration
    self._application_lock = threading.Lock()
    if (module_configuration.runtime == 'vm' or
        util.is_env_flex(module_configuration.env)):
      self._start_process_flavor = http_runtime.START_PROCESS_REVERSE
      self._go_application = go_managedvm.GoManagedVMApp(
          self._module_configuration)
    elif module_configuration.runtime.startswith('go1'):
      self._start_process_flavor = http_runtime.START_PROCESS_REVERSE
      go_config = runtime_config_getter().go_config
      self._go_application = gaego.GaeGoApplication(
          self._module_configuration,
          go_config.work_dir,
          go_config.enable_debugging)
    else:
      self._start_process_flavor = http_runtime.START_PROCESS
      go_config = runtime_config_getter().go_config
      self._go_application = go_application.GoApplication(
          self._module_configuration,
          go_config.work_dir,
          go_config.enable_debugging)
    self._modified_since_last_build = False
    self._last_build_error = None

  def _find_go_mod_dir(self, base_path):
    """Returns the path to go.mod, if it exists, or None if not."""
    # Start at application_root and walk up until we reach root or find a go.mod
    pwd = base_path
    if os.path.isfile(os.path.join(pwd, 'go.mod')):
      return pwd
    while pwd:
      if os.path.isfile(os.path.join(pwd, 'go.mod')):
        logging.warning('go.mod found in parent directory. Move app.yaml to '
                        'that directory and add the line "main: %s".',
                        os.path.relpath(base_path, pwd))
        return pwd
      parent = os.path.dirname(pwd)
      if parent == pwd:
        return None
      pwd = parent
    return None

  def get_restart_directories(self):
    """Returns a list of directories changes in which should trigger a restart.

    Returns:
      A list of src directory paths in the GOPATH. Changes (i.e. files added,
      deleted or modified) in these directories will trigger a restart of all
      instances created with this factory.
    """
    # Go < 1.11 should always watch GOPATH
    # Go == 1.11 should only watch GOPATH if GO111MODULE != on
    # Go > 1.11 should only watch go.mod dir
    go_mod_dir = self._find_go_mod_dir(
        self._module_configuration.application_root)
    # pylint: disable=line-too-long
    if go_mod_dir and (
        os.getenv('GO111MODULE', '').lower() == 'on' or  # explicitly enabled
        (os.getenv('GO111MODULE', 'auto').lower() == 'auto' and  # automatic, so depends on GOPATH
         (not os.getenv('GOPATH') or  # either GOPATH unset, or...
          not go_mod_dir.startswith(os.getenv('GOPATH'))))):  # ...path is not on GOPATH
      logging.info('Building with dependencies from go.mod.')
      return [go_mod_dir]
    logging.info('Building with dependencies from GOPATH.')
    # pylint: enable=line-too-long

    # Go <= 1.10 assumes GOPATH, or will infer it
    if not self._runtime_config_getter().go_config.enable_watching_go_path:
      return []

    default_gopath = os.path.join(os.path.expanduser('~'), 'go')
    go_path = os.getenv('GOPATH', default_gopath)
    if not go_path:
      logging.error(
          'GOPATH and/or HOME are not set, not watching GOPATH for changes.')
      return []

    if sys.platform.startswith('win32'):
      roots = go_path.split(';')
    else:
      roots = go_path.split(':')
    dirs = [os.path.join(r, 'src') for r in roots]
    return [d for d in dirs if os.path.isdir(d)]

  def files_changed(self):
    """Called when a file relevant to the factory *might* have changed."""
    with self._application_lock:
      self._modified_since_last_build = True

  def configuration_changed(self, config_changes):
    """Called when the configuration of the module has changed.

    Args:
      config_changes: A set containing the changes that occured. See the
          *_CHANGED constants in the application_configuration module.
    """
    if config_changes & _REBUILD_CONFIG_CHANGES:
      with self._application_lock:
        self._modified_since_last_build = True

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

    with self._application_lock:
      try:
        if self._go_application.maybe_build(self._modified_since_last_build):
          if self._last_build_error:
            logging.info('Go application successfully built.')
          self._last_build_error = None
      except go_errors.BuildError as e:
        logging.error('Failed to build Go application: %s', e)
        # Deploy a failure proxy now and each time a new instance is requested.
        self._last_build_error = e
        metrics.GetMetricsLogger().LogOnceOnStop(
            metrics.DEVAPPSERVER_CATEGORY, metrics.ERROR_ACTION, label=repr(e))

      self._modified_since_last_build = False

      if self._last_build_error:
        logging.debug('Deploying new instance of failure proxy.')
        proxy = _GoBuildFailureRuntimeProxy(self._last_build_error)
      else:
        environ = self._go_application.get_environment()
        # Add in the environment settings from app_yaml "env_variables:"
        runtime_config = self._runtime_config_getter()
        for kv in runtime_config.environ:
          environ[kv.key] = kv.value
        proxy = http_runtime.HttpRuntimeProxy(
            [self._go_application.go_executable],
            instance_config_getter,
            self._module_configuration,
            environ,
            start_process_flavor=self._start_process_flavor)

    return instance.Instance(self.request_data,
                             instance_id,
                             proxy,
                             self.max_concurrent_requests,
                             self.max_background_threads,
                             expect_ready_request)
