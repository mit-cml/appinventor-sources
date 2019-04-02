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
"""Manage the lifecycle of modules and dispatch requests to them."""

import collections
import logging
import socket
import sys
import threading
import urlparse
import wsgiref.headers

from google.appengine.api import appinfo
from google.appengine.api import request_info
from google.appengine.tools.devappserver2 import instance
from google.appengine.tools.devappserver2 import module
from google.appengine.tools.devappserver2 import runtime_factories
from google.appengine.tools.devappserver2 import scheduled_executor
from google.appengine.tools.devappserver2 import start_response_utils
from google.appengine.tools.devappserver2 import thread_executor
from google.appengine.tools.devappserver2 import wsgi_server

# This file uses pep8 naming.
# pylint: disable=invalid-name

_THREAD_POOL = thread_executor.ThreadExecutor()

ResponseTuple = collections.namedtuple('ResponseTuple',
                                       ['status', 'headers', 'content'])

# This must be kept in sync with dispatch_ah_url_path_prefix_whitelist in
# google/production/borg/apphosting/templates/frontend.borg.
DISPATCH_AH_URL_PATH_PREFIX_WHITELIST = ('/_ah/queue/deferred',
                                         '/_ah/api',)


class PortRegistry(object):
  """Thread-safe registry of port->service mapping.

  Note a service can be either an application service defined in app.yaml or
  the optional dispatcher service defined in dispatcher.yaml.
  """

  def __init__(self):
    self._ports = {}
    self._ports_lock = threading.RLock()

  def add(self, port, _service, inst):
    with self._ports_lock:
      self._ports[port] = (_service, inst)

  def get(self, port):
    with self._ports_lock:
      return self._ports[port]

  def has(self, port):
    with self._ports_lock:
      return port in self._ports


class Dispatcher(request_info.Dispatcher):
  """A devappserver2 implementation of request_info.Dispatcher.

  In addition to the request_info.Dispatcher interface, it owns modules and
  manages their lifetimes.
  """

  # TODO: Make the *_config arguments optional, and clean up associated
  # tests in module_test, dispatcher_test, and java_config_files_test that
  # explicitely pass in *_config=None.
  def __init__(self,
               configuration,
               host,
               port,
               auth_domain,
               runtime_stderr_loglevel,
               php_config,
               python_config,
               java_config,
               go_config,
               custom_config,
               cloud_sql_config,
               vm_config,
               module_to_max_instances,
               use_mtime_file_watcher,
               watcher_ignore_re,
               automatic_restart,
               allow_skipped_files,
               module_to_threadsafe_override,
               external_port,
               specified_service_ports=None,
               enable_host_checking=True,
               ssl_certificate_paths=None):
    """Initializer for Dispatcher.

    Args:
      configuration: An application_configuration.ApplicationConfiguration
          instance storing the configuration data for the app.
      host: A string containing the host that any HTTP servers should bind to
          e.g. "localhost".
      port: An int specifying the first port where servers should listen.
      auth_domain: A string containing the auth domain to set in the environment
          variables.
      runtime_stderr_loglevel: An int reprenting the minimum logging level at
          which runtime log messages should be written to stderr. See
          devappserver2.py for possible values.
      php_config: A runtime_config_pb2.PhpConfig instances containing PHP
          runtime-specific configuration. If None then defaults are used.
      python_config: A runtime_config_pb2.PythonConfig instance containing
          Python runtime-specific configuration. If None then defaults are
          used.
      java_config: A runtime_config_pb2.JavaConfig instance containing Java
          runtime-specific configuration. If None then defaults are used.
      go_config: A runtime_config_pb2.GoConfig instance containing Go
          runtime-specific configuration. If None then defaults are used.
      custom_config: A runtime_config_pb2.CustomConfig instance. If None, or
          'custom_entrypoint' is not set, then attempting to instantiate a
          custom runtime module will result in an error.
      cloud_sql_config: A runtime_config_pb2.CloudSQL instance containing the
          required configuration for local Google Cloud SQL development. If None
          then Cloud SQL will not be available.
      vm_config: A runtime_config_pb2.VMConfig instance containing
          VM runtime-specific configuration.
      module_to_max_instances: A mapping between a module name and the maximum
          number of instances that can be created (this overrides the settings
          found in the configuration argument) e.g.
          {'default': 10, 'backend': 15}.
      use_mtime_file_watcher: A bool containing whether to use mtime polling to
          monitor file changes even if other options are available on the
          current platform.
      watcher_ignore_re: A RegexObject that optionally defines a pattern for the
          file watcher to ignore.
      automatic_restart: If True then instances will be restarted when a
          file or configuration change that affects them is detected.
      allow_skipped_files: If True then all files in the application's directory
          are readable, even if they appear in a static handler or "skip_files"
          directive.
      module_to_threadsafe_override: A mapping between the module name and what
          to override the module's YAML threadsafe configuration (so modules
          not named continue to use their YAML configuration).
      external_port: The port on which the single external module is expected
          to listen, or None if there are no external modules. This will later
          be changed so that the association between external modules and their
          ports is more flexible.
      specified_service_ports: A dict of string(service_name)->int(port number).
          This allows services of given names to run on specified ports.
      enable_host_checking: A bool indicating that HTTP Host checking should
          be enforced for incoming requests.
      ssl_certificate_paths: A ssl_utils.SSLCertificatePaths instance. If
          specified, modules will be launched with SSL.
    """
    self._configuration = configuration
    self._php_config = php_config
    self._python_config = python_config
    self._java_config = java_config
    self._go_config = go_config
    self._custom_config = custom_config
    self._cloud_sql_config = cloud_sql_config
    self._vm_config = vm_config
    self._request_data = None
    self._api_host = None
    self._api_port = None
    self._running_modules = []
    self._module_configurations = {}
    self._host = host
    self._default_port = port
    self._auth_domain = auth_domain
    self._runtime_stderr_loglevel = runtime_stderr_loglevel
    self._module_name_to_module = {}
    self._dispatch_server = None
    self._quit_event = threading.Event()  # Set when quit() has been called.
    self._update_checking_thread = threading.Thread(
        target=self._loop_checking_for_updates,
        name='Dispatcher Update Checking')
    self._module_to_max_instances = module_to_max_instances or {}
    self._use_mtime_file_watcher = use_mtime_file_watcher
    self._watcher_ignore_re = watcher_ignore_re
    self._automatic_restart = automatic_restart
    self._allow_skipped_files = allow_skipped_files
    self._module_to_threadsafe_override = module_to_threadsafe_override
    self._executor = scheduled_executor.ScheduledExecutor(_THREAD_POOL)
    self._port_registry = PortRegistry()
    self._external_port = external_port
    self._specified_service_ports = specified_service_ports or {}
    self._enable_host_checking = enable_host_checking
    self._ssl_certificate_paths = ssl_certificate_paths

  def start(self, api_host, api_port, request_data):
    """Starts the configured modules.

    Args:
      api_host: The hostname that APIServer listens for RPC requests on.
      api_port: The port that APIServer listens for RPC requests on.
      request_data: A wsgi_request_info.WSGIRequestInfo that will be provided
          with request information for use by API stubs.

    Raises:
      RuntimeError: In case of cannot find port for a service.
    """
    self._api_host = api_host
    self._api_port = api_port
    self._request_data = request_data
    self._executor.start()
    if self._configuration.dispatch:
      self._dispatch_server = wsgi_server.WsgiServer((self._host,
                                                      self._default_port), self)
      self._dispatch_server.start()
      logging.info('Starting dispatcher running at: http://%s:%s', self._host,
                   self._dispatch_server.port)
      self._update_checking_thread.start()
      self._port_registry.add(self._dispatch_server.port, None, None)

    next_available_port = self._default_port
    for module_configuration in self._configuration.modules:
      service_name = module_configuration.module_name
      self._module_configurations[service_name] = module_configuration

      service_port = 0
      if service_name in self._specified_service_ports:
        service_port = self._specified_service_ports[service_name]
      elif next_available_port:
        next_available_port = self._find_next_available_port(
            next_available_port, service_name)
        service_port = next_available_port

      # If necessary, find an additional port to bind to for accepting https
      # connections
      ssl_port = None
      if self._ssl_certificate_paths:
        ssl_port = self._find_next_available_port(service_port + 1,
                                                  service_name)

      _service = self._create_module(module_configuration, service_port,
                                     ssl_port)
      _service.start()
      self._module_name_to_module[module_configuration.module_name] = _service

      log_message = 'Starting module "%s" running at: http://%s' % (
          module_configuration.module_name, _service.balanced_address)
      if ssl_port:
        log_message += ' and https://%s:%s' % (self._host, ssl_port)
      logging.info(log_message)

  def _find_next_available_port(self, starting_port, service_name):
    """Finds an available port in the port registry starting at starting_port.

    Args:
      starting_port: int, the port to start searching from.
      service_name: str, the name of the service used in exception if no port
        could be found.

    Raises:
      RuntimeError: If no port can be found.

    Returns:
      A port that is available for binding to.
    """
    next_available_port = starting_port
    while self._port_registry.has(next_available_port):
      next_available_port += 1
    if next_available_port >= (1 << 16):
      raise RuntimeError('Cannot find port for service %s' % service_name)
    return next_available_port

  @property
  def dispatch_port(self):
    """The port that the dispatch HTTP server for the Module is listening on."""
    assert self._dispatch_server, 'dispatch server not running'
    assert self._dispatch_server.ready, 'dispatch server not ready'
    return self._dispatch_server.port

  @property
  def host(self):
    """The host that the HTTP server for this Dispatcher is listening on."""
    return self._host

  @property
  def dispatch_address(self):
    """The address of the dispatch HTTP server e.g. "localhost:8080"."""
    if self.dispatch_port != 80:
      return '%s:%s' % (self.host, self.dispatch_port)
    else:
      return self.host

  def _check_for_updates(self):
    self._configuration.dispatch.check_for_updates()

  def _loop_checking_for_updates(self):
    """Loops until the Dispatcher exits, reloading dispatch.yaml config."""
    while not self._quit_event.is_set():
      self._check_for_updates()
      self._quit_event.wait(timeout=1)

  def get_watcher_results(self):
    """Returns a list of tuples of file watcher results for google analytics."""
    results = []
    for _module in self._module_name_to_module.values():
      result = _module.get_watcher_result()
      # Make sure the module has file watcher, and file change hisotry
      # was not empty.
      if result and result[1]:
        results.append(result)
    return results

  def quit(self):
    """Quits all modules."""
    self._executor.quit()
    self._quit_event.set()
    if self._dispatch_server:
      self._dispatch_server.quit()
    for _module in self._module_name_to_module.values():
      _module.quit()

  def check_python_version(self, runtime):
    """Check the python version and give proper warnings if necessary."""
    if runtime == 'python27':
      if sys.version_info[1] < 7:
        logging.warning('You are creating a python27 module, but your python '
                        'minor version is below 2.7.')
      elif sys.version_info[2] < runtime_factories.PYTHON27_PROD_VERSION[2]:
        logging.warning('Your python27 micro version is below %s, our '
                        'current production version.',
                        '.'.join(map(str,
                                     runtime_factories.PYTHON27_PROD_VERSION)))

  def _create_module(self, module_configuration, port, ssl_port=None):
    self.check_python_version(module_configuration.runtime)
    max_instances = self._module_to_max_instances.get(
        module_configuration.module_name)
    threadsafe_override = self._module_to_threadsafe_override.get(
        module_configuration.module_name)

    if self._external_port:
      module_configuration.external_port = self._external_port
      module_class = module.ExternalModule
    elif module_configuration.is_manual_scaling:
      module_class = module.ManualScalingModule
    elif module_configuration.is_basic_scaling:
      module_class = module.BasicScalingModule
    else:
      module_class = module.AutoScalingModule

    module_instance = module_class(
        module_configuration=module_configuration,
        host=self._host,
        balanced_port=port,
        api_host=self._api_host,
        api_port=self._api_port,
        auth_domain=self._auth_domain,
        runtime_stderr_loglevel=self._runtime_stderr_loglevel,
        php_config=self._php_config,
        python_config=self._python_config,
        custom_config=self._custom_config,
        java_config=self._java_config,
        go_config=self._go_config,
        cloud_sql_config=self._cloud_sql_config,
        vm_config=self._vm_config,
        default_version_port=self._default_port,
        port_registry=self._port_registry,
        request_data=self._request_data,
        dispatcher=self,
        max_instances=max_instances,
        use_mtime_file_watcher=self._use_mtime_file_watcher,
        watcher_ignore_re=self._watcher_ignore_re,
        automatic_restarts=self._automatic_restart,
        allow_skipped_files=self._allow_skipped_files,
        threadsafe_override=threadsafe_override,
        enable_host_checking=self._enable_host_checking,
        ssl_certificate_paths=self._ssl_certificate_paths,
        ssl_port=ssl_port)

    return module_instance

  @property
  def modules(self):
    return self._module_name_to_module.values()

  def get_hostname(self, module_name, version, instance_id=None):
    """Returns the hostname for a (module, version, instance_id) tuple.

    If instance_id is set, this will return a hostname for that particular
    instances. Otherwise, it will return the hostname for load-balancing.
    Returning 0.0.0.0 is modified to be a more useful address to the user.

    Args:
      module_name: A str containing the name of the module.
      version: A str containing the version.
      instance_id: An optional str containing the instance ID.

    Returns:
      A str containing the hostname.

    Raises:
      request_info.ModuleDoesNotExistError: The module does not exist.
      request_info.VersionDoesNotExistError: The version does not exist.
      request_info.InvalidInstanceIdError: The instance ID is not valid for the
          module/version or the module/version uses automatic scaling.
    """
    _module = self._get_module(module_name, version)
    if instance_id is None:
      hostname = _module.balanced_address
    else:
      hostname = _module.get_instance_address(instance_id)

    parts = hostname.split(':')
    # 0.0.0.0 or 0 binds to all interfaces but only connects to localhost.
    # Convert to an address that can connect from local and remote machines.
    # TODO: handle IPv6 bind-all address (::).
    try:
      if socket.inet_aton(parts[0]) == '\0\0\0\0':
        hostname = ':'.join([socket.gethostname()] + parts[1:])
    except socket.error:
      # socket.inet_aton raised an exception so parts[0] is not an IP address.
      pass
    return hostname

  def get_module_names(self):
    """Returns a list of module names."""
    return list(self._module_name_to_module)

  def get_module_by_name(self, _module):
    """Returns the module with the given name.

    Args:
      _module: A str containing the name of the module.

    Returns:
      The module.Module with the provided name.

    Raises:
      request_info.ModuleDoesNotExistError: The module does not exist.
    """
    try:
      return self._module_name_to_module[_module]
    except KeyError:
      raise request_info.ModuleDoesNotExistError(_module)

  def get_versions(self, _module):
    """Returns a list of versions for a module.

    Args:
      _module: A str containing the name of the module.

    Returns:
      A list of str containing the versions for the specified module.

    Raises:
      request_info.ModuleDoesNotExistError: The module does not exist.
    """
    if _module in self._module_configurations:
      return [self._module_configurations[_module].major_version]
    else:
      raise request_info.ModuleDoesNotExistError(_module)

  def get_default_version(self, _module):
    """Returns the default version for a module.

    Args:
      _module: A str containing the name of the module.

    Returns:
      A str containing the default version for the specified module.

    Raises:
      request_info.ModuleDoesNotExistError: The module does not exist.
    """
    if _module in self._module_configurations:
      return self._module_configurations[_module].major_version
    else:
      raise request_info.ModuleDoesNotExistError(_module)

  def add_event(self, runnable, eta, service=None, event_id=None):
    """Add a callable to be run at the specified time.

    Args:
      runnable: A callable object to call at the specified time.
      eta: An int containing the time to run the event, in seconds since the
          epoch.
      service: A str containing the name of the service that owns this event.
          This should be set if event_id is set.
      event_id: A str containing the id of the event. If set, this can be passed
          to update_event to change the time at which the event should run.
    """
    if service is not None and event_id is not None:
      key = (service, event_id)
    else:
      key = None
    self._executor.add_event(runnable, eta, key)

  def update_event(self, eta, service, event_id):
    """Update the eta of a scheduled event.

    Args:
      eta: An int containing the time to run the event, in seconds since the
          epoch.
      service: A str containing the name of the service that owns this event.
      event_id: A str containing the id of the event to update.
    """
    self._executor.update_event(eta, (service, event_id))

  def _get_module(self, module_name, version):
    """Attempts to find the specified module.

    Args:
      module_name: The name of the module.
      version: The version id.
    Returns:
      Module object.
    Raises:
      request_info.ModuleDoesNotExistError: The module doesn't exist.
      request_info.VersionDoesNotExistError: The version doesn't exist.
    """
    if not module_name:
      module_name = appinfo.DEFAULT_MODULE
    if module_name not in self._module_name_to_module:
      raise request_info.ModuleDoesNotExistError()
    if (version is not None and
        version != self._module_configurations[module_name].major_version):
      raise request_info.VersionDoesNotExistError()
    return self._module_name_to_module[module_name]

  def _get_module_with_soft_routing(self, module_name, version):
    """Uses soft-routing to find the specified module.

    Soft-routing is an attempt to match the production resolution order, which
    is slightly more permissive than the Modules API behavior. Here are the
    rules allowed:
    1. If a module is requested that doesn't exist, use the default module.
    2. If a module is requested that doesn't exist, and there is no default
    module, use any module.

    Args:
      module_name: The name of the module.
      version: The version id.
    Returns:
      Module object.
    Raises:
      request_info.ModuleDoesNotExistError: The module doesn't exist.
      request_info.VersionDoesNotExistError: The version doesn't exist.
    """
    if not module_name or module_name not in self._module_name_to_module:
      if appinfo.DEFAULT_MODULE in self._module_name_to_module:
        module_name = appinfo.DEFAULT_MODULE
      elif self._module_name_to_module:
        # If there is no default module, but there are other modules, take any.
        # This is somewhat of a hack, and can be removed if we ever enforce the
        # existence of a default module.
        module_name = self._module_name_to_module.keys()[0]
      else:
        raise request_info.ModuleDoesNotExistError(module_name)
    if (version is not None and
        version != self._module_configurations[module_name].major_version):
      raise request_info.VersionDoesNotExistError()
    return self._module_name_to_module[module_name]

  def set_num_instances(self, module_name, version, num_instances):
    """Sets the number of instances to run for a version of a module.

    Args:
      module_name: A str containing the name of the module.
      version: A str containing the version.
      num_instances: An int containing the number of instances to run.

    Raises:
      ModuleDoesNotExistError: The module does not exist.
      VersionDoesNotExistError: The version does not exist.
      NotSupportedWithAutoScalingError: The provided module/version uses
          automatic scaling.
    """
    self._get_module(module_name, version).set_num_instances(num_instances)

  def get_num_instances(self, module_name, version):
    """Returns the number of instances running for a version of a module.

    Returns:
      An int containing the number of instances running for a module version.

    Args:
      module_name: A str containing the name of the module.
      version: A str containing the version.

    Raises:
      ModuleDoesNotExistError: The module does not exist.
      VersionDoesNotExistError: The version does not exist.
      NotSupportedWithAutoScalingError: The provided module/version uses
          automatic scaling.
    """
    return self._get_module(module_name, version).get_num_instances()

  def start_version(self, module_name, version):
    """Starts a version of a module.

    Args:
      module_name: A str containing the name of the module.
      version: A str containing the version.

    Raises:
      ModuleDoesNotExistError: The module does not exist.
      VersionDoesNotExistError: The version does not exist.
      NotSupportedWithAutoScalingError: The provided module/version uses
          automatic scaling.
    """
    self._get_module(module_name, version).resume()

  def stop_version(self, module_name, version):
    """Stops a version of a module.

    Args:
      module_name: A str containing the name of the module.
      version: A str containing the version.

    Raises:
      ModuleDoesNotExistError: The module does not exist.
      VersionDoesNotExistError: The version does not exist.
      NotSupportedWithAutoScalingError: The provided module/version uses
          automatic scaling.
    """
    self._get_module(module_name, version).suspend()

  def send_background_request(self, module_name, version, inst,
                              background_request_id):
    """Dispatch a background thread request.

    Args:
      module_name: A str containing the module name to service this
          request.
      version: A str containing the version to service this request.
      inst: The instance to service this request.
      background_request_id: A str containing the unique background thread
          request identifier.

    Raises:
      NotSupportedWithAutoScalingError: The provided module/version uses
          automatic scaling.
      BackgroundThreadLimitReachedError: The instance is at its background
          thread capacity.
    """
    _module = self._get_module_with_soft_routing(module_name, version)
    try:
      inst.reserve_background_thread()
    except instance.CannotAcceptRequests:
      raise request_info.BackgroundThreadLimitReachedError()
    port = _module.get_instance_port(inst.instance_id)
    environ = _module.build_request_environ(
        'GET', '/_ah/background',
        [('X-AppEngine-BackgroundRequest', background_request_id)],
        '', '0.1.0.3', port)
    _THREAD_POOL.submit(self._handle_request,
                        environ,
                        start_response_utils.null_start_response,
                        _module,
                        inst,
                        request_type=instance.BACKGROUND_REQUEST,
                        catch_and_log_exceptions=True)

  # TODO: Think of better names for add_async_request and
  # add_request.
  def add_async_request(self, method, relative_url, headers, body, source_ip,
                        module_name=None, version=None, instance_id=None):
    """Dispatch an HTTP request asynchronously.

    Args:
      method: A str containing the HTTP method of the request.
      relative_url: A str containing path and query string of the request.
      headers: A list of (key, value) tuples where key and value are both str.
      body: A str containing the request body.
      source_ip: The source ip address for the request.
      module_name: An optional str containing the module name to service this
          request. If unset, the request will be dispatched to the default
          module.
      version: An optional str containing the version to service this request.
          If unset, the request will be dispatched to the default version.
      instance_id: An optional str containing the instance_id of the instance to
          service this request. If unset, the request will be dispatched to
          according to the load-balancing for the module and version.
    """
    if module_name:
      _module = self._get_module_with_soft_routing(module_name, version)
    else:
      _module = self._module_for_request(urlparse.urlsplit(relative_url).path)
    inst = _module.get_instance(instance_id) if instance_id else None
    port = _module.get_instance_port(instance_id) if instance_id else (
        _module.balanced_port)
    environ = _module.build_request_environ(method, relative_url, headers, body,
                                            source_ip, port)

    _THREAD_POOL.submit(self._handle_request,
                        environ,
                        start_response_utils.null_start_response,
                        _module,
                        inst,
                        catch_and_log_exceptions=True)

  def add_request(self, method, relative_url, headers, body, source_ip,
                  module_name=None, version=None, instance_id=None,
                  fake_login=False):
    """Process an HTTP request.

    Args:
      method: A str containing the HTTP method of the request.
      relative_url: A str containing path and query string of the request.
      headers: A list of (key, value) tuples where key and value are both str.
      body: A str containing the request body.
      source_ip: The source ip address for the request.
      module_name: An optional str containing the module name to service this
          request. If unset, the request will be dispatched according to the
          host header and relative_url.
      version: An optional str containing the version to service this request.
          If unset, the request will be dispatched according to the host header
          and relative_url.
      instance_id: An optional str containing the instance_id of the instance to
          service this request. If unset, the request will be dispatched
          according to the host header and relative_url and, if applicable, the
          load-balancing for the module and version.
      fake_login: A bool indicating whether login checks should be bypassed,
          i.e. "login: required" should be ignored for this request.

    Returns:
      A request_info.ResponseTuple containing the response information for the
      HTTP request.
    """
    if module_name:
      _module = self._get_module_with_soft_routing(module_name, version)
      inst = _module.get_instance(instance_id) if instance_id else None
    else:
      headers_dict = wsgiref.headers.Headers(headers)
      _module, inst = self._resolve_target(
          headers_dict['Host'], urlparse.urlsplit(relative_url).path)
    if inst:
      try:
        port = _module.get_instance_port(inst.instance_id)
      except request_info.NotSupportedWithAutoScalingError:
        port = _module.balanced_port
    else:
      port = _module.balanced_port
    environ = _module.build_request_environ(method, relative_url, headers, body,
                                            source_ip, port,
                                            fake_login=fake_login)
    start_response = start_response_utils.CapturingStartResponse()
    response = self._handle_request(environ,
                                    start_response,
                                    _module,
                                    inst)

    # merged_response can have side effects which modify start_response.*, so
    # we cannot safely inline it into the ResponseTuple initialization below.
    merged = start_response.merged_response(response)
    return request_info.ResponseTuple(start_response.status,
                                      start_response.response_headers,
                                      merged)

  def _resolve_target(self, hostname, path):
    """Returns the module and instance that should handle this request.

    Args:
      hostname: A string containing the value of the host header in the request
          or None if one was not present.
      path: A string containing the path of the request.

    Returns:
      A tuple (_module, inst) where:
        _module: The module.Module that should handle this request.
        inst: The instance.Instance that should handle this request or None if
            the module's load balancing should decide on the instance.

    Raises:
      request_info.ModuleDoesNotExistError: if hostname is not known.
    """
    if self._default_port == 80:
      default_address = self.host
    else:
      default_address = '%s:%s' % (self.host, self._default_port)
    if not hostname or hostname == default_address:
      return self._module_for_request(path), None





    default_address_offset = hostname.find(default_address)
    if default_address_offset > 0:
      prefix = hostname[:default_address_offset - 1]
      # The prefix should be 'module', but might be 'instance.version.module',
      # 'version.module', or 'instance.module'. These alternatives work in
      # production, but devappserver2 doesn't support running multiple versions
      # of the same module. All we can really do is route to the default
      # version of the specified module.
      if '.' in prefix:
        logging.warning('Ignoring instance/version in %s; multiple versions '
                        'are not supported in devappserver.', prefix)
      module_name = prefix.split('.')[-1]
      return self._get_module_with_soft_routing(module_name, None), None

    else:
      if ':' in hostname:
        port = int(hostname.split(':', 1)[1])
      else:
        port = 80
      try:
        _module, inst = self._port_registry.get(port)
      except KeyError:
        raise request_info.ModuleDoesNotExistError(hostname)
    if not _module:
      _module = self._module_for_request(path)
    return _module, inst

  def _handle_request(self, environ, start_response, _module,
                      inst=None, request_type=instance.NORMAL_REQUEST,
                      catch_and_log_exceptions=False):
    """Dispatch a WSGI request.

    Args:
      environ: An environ dict for the request as defined in PEP-333.
      start_response: A function with semantics defined in PEP-333.
      _module: The module to dispatch this request to.
      inst: The instance to service this request. If None, the module will
          be left to choose the instance to serve this request.
      request_type: The request_type of this request. See instance.*_REQUEST
          module constants.
      catch_and_log_exceptions: A bool containing whether to catch and log
          exceptions in handling the request instead of leaving it for the
          caller to handle.

    Returns:
      An iterable over the response to the request as defined in PEP-333.
    """
    try:
      return _module._handle_request(environ, start_response, inst=inst,
                                     request_type=request_type)
    except:
      if catch_and_log_exceptions:
        logging.exception('Internal error while handling request.')
      else:
        raise

  def __call__(self, environ, start_response):
    return self._handle_request(
        environ, start_response, self._module_for_request(environ['PATH_INFO']))

  def _should_use_dispatch_config(self, path):
    """Determines whether or not to use the dispatch config.

    Args:
      path: The request path.
    Returns:
      A Boolean indicating whether or not to use the rules in dispatch config.
    """
    if (not path.startswith('/_ah/') or
        any(path.startswith(wl) for wl
            in DISPATCH_AH_URL_PATH_PREFIX_WHITELIST)):
      return True
    else:
      logging.warning('Skipping dispatch.yaml rules because %s is not a '
                      'dispatchable path.', path)
      return False

  def _module_for_request(self, path):
    dispatch = self._configuration.dispatch
    if dispatch and self._should_use_dispatch_config(path):
      for url, module_name in dispatch.dispatch:
        if (url.path_exact and path == url.path or
            not url.path_exact and path.startswith(url.path)):
          return self._get_module_with_soft_routing(module_name, None)
    return self._get_module_with_soft_routing(None, None)
