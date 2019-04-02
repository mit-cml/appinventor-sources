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
"""Serves the stub App Engine APIs (e.g. memcache, datastore) over HTTP.

The Remote API protocol is used for communication with the API stubs.

The APIServer can be started either as a stand alone binary or directly from
other scripts, eg dev_appserver.py. When using as a stand alone binary, the
APIServer can be launched with or without the context of a specific application.

To launch the API Server in the context of an application, launch the APIServer
in the same way as dev_appserver.py:

  api_server.py [flags] <module> [<module>...]

When launching without the context of an application, a default application id
is provided, which can be overidden with the --application flag. Either of the
following are acceptable:

  api_server.py [flags]
  api_server.py --application=my-app-id [flags]
"""



import errno
import getpass
import itertools
import json
import logging
import os
import pickle
import shutil
import sys
import tempfile
import threading
import time
import traceback
import urllib2
import urlparse

import google
import portpicker
from google.appengine._internal.ruamel import yaml

from google.appengine.api import apiproxy_stub
from google.appengine.api import apiproxy_stub_map
from google.appengine.api import request_info as request_info_lib
from google.appengine.datastore import datastore_stub_util
from google.appengine.ext.remote_api import remote_api_pb
from google.appengine.ext.remote_api import remote_api_services
from google.appengine.runtime import apiproxy_errors
from google.appengine.tools.devappserver2 import application_configuration
from google.appengine.tools.devappserver2 import cli_parser
from google.appengine.tools.devappserver2 import constants
from google.appengine.tools.devappserver2 import datastore_converter
from google.appengine.tools.devappserver2 import datastore_grpc_stub
from google.appengine.tools.devappserver2 import errors
from google.appengine.tools.devappserver2 import login
from google.appengine.tools.devappserver2 import metrics
from google.appengine.tools.devappserver2 import shutdown
from google.appengine.tools.devappserver2 import stub_util
from google.appengine.tools.devappserver2 import util
from google.appengine.tools.devappserver2 import wsgi_request_info
from google.appengine.tools.devappserver2 import wsgi_server
from google.appengine.tools.devappserver2.cloud_emulators import cloud_emulator_manager


# The API lock is applied when calling API stubs that are not threadsafe.
GLOBAL_API_LOCK = threading.RLock()

# The default app id used when launching the api_server.py as a binary, without
# providing the context of a specific application.
DEFAULT_API_SERVER_APP_ID = 'dev~app-id'


class DatastoreFileError(Exception):
  """A datastore data file is not supported."""


def _execute_request(request, use_proto3=False):
  """Executes an API method call and returns the response object.

  Args:
    request: A remote_api_pb.Request object representing the API call e.g. a
        call to memcache.Get.
    use_proto3: A boolean representing is request is in proto3.

  Returns:
    A ProtocolBuffer.ProtocolMessage representing the API response e.g. a
    memcache_service_pb.MemcacheGetResponse.

  Raises:
    apiproxy_errors.CallNotFoundError: if the requested method doesn't exist.
    apiproxy_errors.ApplicationError: if the API method calls fails.
  """
  logging.debug('API server executing remote_api_pb.Request: \n%s', request)

  if use_proto3:
    service = request.service_name
    method = request.method
    if request.request_id:
      request_id = request.request_id
    else:
      # This logging could be time consuming. Hence set as debug level.
      logging.debug('Received a request without request_id.')
      request_id = None
  else:
    service = request.service_name()
    method = request.method()
    if request.has_request_id():
      request_id = request.request_id()
    else:
      logging.debug('Received a request without request_id.')
      request_id = None

  service_methods = (
      stub_util.DATASTORE_V4_METHODS if service == 'datastore_v4'
      else remote_api_services.STUB_SERVICE_PB_MAP.get(service, {}))
  # We do this rather than making a new map that is a superset of
  # remote_api_services.SERVICE_PB_MAP because that map is not initialized
  # all in one place, so we would have to be careful about where we made
  # our new map.

  request_class, response_class = service_methods.get(method, (None, None))
  if not request_class:
    raise apiproxy_errors.CallNotFoundError('%s.%s does not exist' % (service,
                                                                      method))

  request_data = request_class()
  if use_proto3:
    request_data.ParseFromString(request.request)
  else:
    request_data.ParseFromString(request.request())
  response_data = response_class()
  service_stub = apiproxy_stub_map.apiproxy.GetStub(service)

  def make_request():
    service_stub.MakeSyncCall(service,
                              method,
                              request_data,
                              response_data,
                              request_id)

  # If the service has not declared itself as threadsafe acquire
  # GLOBAL_API_LOCK.
  if service_stub.THREADSAFE:
    make_request()
  else:
    with GLOBAL_API_LOCK:
      make_request()
  metrics.GetMetricsLogger().LogOnceOnStop(
      metrics.API_STUB_USAGE_CATEGORY,
      metrics.API_STUB_USAGE_ACTION_TEMPLATE % service)

  logging.debug('API server responding with remote_api_pb.Response: \n%s',
                response_data)
  return response_data


class _LocalJavaAppDispatcher(request_info_lib._LocalFakeDispatcher):  # pylint: disable=protected-access
  """Dispatcher that sends HTTP requests to a Java app engine app."""

  def __init__(self,
               module_names=None,
               module_name_to_versions=None,
               module_name_to_default_versions=None,
               module_name_to_version_to_hostname=None,
               java_app_base_url=None):
    super(_LocalJavaAppDispatcher, self).__init__(
        module_names, module_name_to_versions, module_name_to_default_versions,
        module_name_to_version_to_hostname)
    self.java_app_base_url = java_app_base_url

  def add_request(self,
                  method,
                  relative_url,
                  headers,
                  body,
                  source_ip,
                  module_name=None,
                  version=None,
                  instance_id=None):
    request = urllib2.Request(
        url=self.java_app_base_url + relative_url,
        data=body,
        headers=dict(headers))
    response = urllib2.urlopen(request)
    return request_info_lib.ResponseTuple(str(response.getcode()), [], '')


class APIServer(wsgi_server.WsgiServer):
  """Serves API calls over HTTP and GRPC(optional)."""

  def __init__(self, host, port, app_id, use_grpc=False, grpc_api_port=0,
               enable_host_checking=True, gcd_emulator_launching_thread=None):
    self._app_id = app_id
    self._host = host

    if enable_host_checking:
      api_server_module = wsgi_server.WsgiHostCheck([host], self)
    else:
      api_server_module = self
    super(APIServer, self).__init__((host, port), api_server_module)

    self.set_balanced_address('localhost:8080')

    self._use_grpc = use_grpc
    self._grpc_api_port = grpc_api_port
    self._gcd_emulator_launching_thread = gcd_emulator_launching_thread

  def _start_grpc_server(self):
    """Starts gRPC API server."""
    grpc_service_pb2 = __import__('google.appengine.tools.devappserver2.'
                                  'grpc_service_pb2', globals(), locals(),
                                  ['grpc_service_pb2'])

    class CallHandler(grpc_service_pb2.BetaCallHandlerServicer):
      """Handles gRPC method calls."""

      def HandleCall(self, request, context):
        response = grpc_service_pb2.Response()
        try:
          api_response = _execute_request(request, use_proto3=True)
          response.response = api_response.Encode()
        except apiproxy_errors.ApplicationError, e:
          response.application_error.code = e.application_error
          response.application_error.detail = e.error_detail
        return response

    self._grpc_server = grpc_service_pb2.beta_create_CallHandler_server(
        CallHandler())

    # add_insecure_port() returns positive port number when port allocation is
    # successful. Otherwise it returns 0.
    # 'localhost' works with both ipv4 and ipv6.
    self._grpc_api_port = self._grpc_server.add_insecure_port(
        'localhost:' + str(self._grpc_api_port))
    if not self._grpc_api_port:
      raise errors.GrpcPortError('Error assigning grpc api port!')

    datastore_v3_stub = apiproxy_stub_map.apiproxy.GetStub('datastore_v3')
    if isinstance(datastore_v3_stub, datastore_grpc_stub.DatastoreGrpcStub):
      datastore_v3_stub.SetTxnAddTaskCallbackHostPort(
          'localhost:%d' % self._grpc_api_port)

    # We set this GRPC_PORT in environment variable as it is only accessed by
    # the devappserver process.
    os.environ['GRPC_PORT'] = str(self._grpc_api_port)
    logging.info('%s: http://localhost:%d',
                 constants.GRPC_API_SERVER_STARTING_MSG, self._grpc_api_port)
    self._grpc_server.start()

  def start(self):
    """Start the API Server."""
    # If ApiServer uses gcd emulator, emulator must get ready here.
    if self._gcd_emulator_launching_thread:
      self._gcd_emulator_launching_thread.join()
    super(APIServer, self).start()
    logging.info('%s: http://%s:%d',
                 constants.API_SERVER_STARTING_MSG, self._host, self.port)
    if self._use_grpc:
      self._start_grpc_server()

  def quit(self):
    super(APIServer, self).quit()
    if self._use_grpc:
      self._grpc_server.stop(0)
    stub_util.cleanup_stubs()

  def set_balanced_address(self, balanced_address):
    """Sets the balanced address from the dispatcher (e.g. "localhost:8080").

    This is used to enable APIs to build valid URLs.

    Args:
      balanced_address: string address of the balanced HTTP server.
    """
    self._balanced_address = balanced_address

  def _handle_POST(self, environ, start_response):
    """Handles a POST request containing a serialized remote_api_pb.Request.

    Args:
      environ: An environ dict for the request as defined in PEP-333.
      start_response: A start_response function with semantics defined in
        PEP-333.

    Returns:
      A single element list containing the string body of the HTTP response.
    """
    start_response('200 OK', [('Content-Type', 'application/octet-stream')])

    start_time = time.time()
    response = remote_api_pb.Response()
    try:
      request = remote_api_pb.Request()
      # NOTE: Exceptions encountered when parsing the PB or handling the request
      # will be propagated back to the caller the same way as exceptions raised
      # by the actual API call.
      if environ.get('HTTP_TRANSFER_ENCODING') == 'chunked':
        # CherryPy concatenates all chunks  when 'wsgi.input' is read but v3.2.2
        # will not return even when all of the data in all chunks has been
        # read. See: https://bitbucket.org/cherrypy/cherrypy/issue/1131.
        wsgi_input = environ['wsgi.input'].read(2**32)
      else:
        wsgi_input = environ['wsgi.input'].read(int(environ['CONTENT_LENGTH']))
      request.ParseFromString(wsgi_input)

      service = request.service_name()
      service_stub = apiproxy_stub_map.apiproxy.GetStub(service)

      if isinstance(service_stub, datastore_grpc_stub.DatastoreGrpcStub):
        # len(request.request()) is equivalent to calling ByteSize() on
        # deserialized request.request.
        if len(request.request()) > apiproxy_stub.MAX_REQUEST_SIZE:
          raise apiproxy_errors.RequestTooLargeError(
              apiproxy_stub.REQ_SIZE_EXCEEDS_LIMIT_MSG_TEMPLATE % (
                  service, request.method()))
        response = service_stub.MakeSyncCallForRemoteApi(request)
        metrics.GetMetricsLogger().LogOnceOnStop(
            metrics.API_STUB_USAGE_CATEGORY,
            metrics.API_STUB_USAGE_ACTION_TEMPLATE
            % 'datastore_v3_with_cloud_datastore_emulator')
      else:
        if request.has_request_id():
          request_id = request.request_id()
          environ['HTTP_HOST'] = self._balanced_address
          op = getattr(service_stub.request_data, 'register_request_id', None)
          if callable(op):
            op(environ, request_id)
        api_response = _execute_request(request).Encode()
        response.set_response(api_response)
    except Exception, e:
      if isinstance(e, apiproxy_errors.ApplicationError):
        level = logging.DEBUG
        application_error = response.mutable_application_error()
        application_error.set_code(e.application_error)
        application_error.set_detail(e.error_detail)
      else:
        # If the runtime instance is not Python, it won't be able to unpickle
        # the exception so use level that won't be ignored by default.
        level = logging.ERROR
        # Even if the runtime is Python, the exception may be unpicklable if
        # it requires importing a class blocked by the sandbox so just send
        # back the exception representation.
        # But due to our use of the remote API, at least some apiproxy errors
        # are generated in the Dev App Server main instance and not in the
        # language runtime and wrapping them causes different behavior from
        # prod so don't wrap them.
        if not isinstance(e, apiproxy_errors.Error):
          e = RuntimeError(repr(e))
      # While not strictly necessary for ApplicationError, do this to limit
      # differences with remote_api:handler.py.
      response.set_exception(pickle.dumps(e))






      logging.log(level, 'Exception while handling %s.%s()\n%s',
                  request.service_name(),
                  request.method(), traceback.format_exc())
    encoded_response = response.Encode()
    logging.debug('Handled %s.%s in %0.4f',
                  request.service_name(),
                  request.method(),
                  time.time() - start_time)
    return [encoded_response]

  def _handle_GET(self, environ, start_response):
    params = urlparse.parse_qs(environ['QUERY_STRING'])
    rtok = params.get('rtok', ['0'])[0]

    start_response('200 OK', [('Content-Type', 'text/plain')])
    return [yaml.dump({'app_id': self._app_id,
                       'rtok': rtok})]

  def _handle_CLEAR(self, environ, start_response):
    """Clear the stateful content from the API server."""
    start_response('200 OK', [('Content-Type', 'text/plain')])

    # TODO: Add more services as needed.
    clearable_stubs = [
        'app_identity_service', 'capability_service', 'datastore_v3',
        'logservice', 'mail', 'memcache', 'taskqueue'
    ]
    stubs_to_clear = urlparse.parse_qs(environ.get('QUERY_STRING')).get('stub')

    if stubs_to_clear:
      for stub_name in stubs_to_clear:
        if stub_name in clearable_stubs:
          apiproxy_stub_map.apiproxy.GetStub(stub_name).Clear()
    else:
      for stub_name in clearable_stubs:
        apiproxy_stub_map.apiproxy.GetStub(stub_name).Clear()

    # No response is necessary, a 200 status code is enough.
    return []

  def _handle_STATUS(self, environ, start_response):
    """Report the status of api server.

    Args:
      environ: An environ dict for the request as defined in PEP-333.
      start_response: A start_response function with semantics defined in
        PEP-333.

    Returns:
      A JSON string which is a dumped dict reporting statuses of api_server.
    """
    start_response('200 OK', [('Content-Type', 'text/plain')])
    status = {
        'datastore_emulator_host': os.environ.get(
            'DATASTORE_EMULATOR_HOST', 'None')
    }

    return json.dumps(status)

  def __call__(self, environ, start_response):
    if environ.get('PATH_INFO') == '/clear':
      return self._handle_CLEAR(environ, start_response)
    elif environ.get('PATH_INFO') == '/_ah/status':
      return self._handle_STATUS(environ, start_response)
    if environ['REQUEST_METHOD'] == 'GET':
      return self._handle_GET(environ, start_response)
    elif environ['REQUEST_METHOD'] == 'POST':
      return self._handle_POST(environ, start_response)
    else:
      start_response('405 Method Not Allowed', [])
      return []


def _launch_gcd_emulator(
    app_id=None, emulator_port=0, silent=True, index_file='',
    require_indexes=False, datastore_path='', stub_type=None, cmd=None,
    is_test=False, auto_id_policy=datastore_stub_util.SEQUENTIAL):
  """Launch Cloud Datastore emulator asynchronously.

  If datastore_path is sqlite stub data, rename it and convert into emulator
  data.

  Args:
    app_id: A string representing application ID.
    emulator_port: An integer representing the port number for emulator to
      launch on.
    silent: A bool indicating if emulator runs in silent mode.
    index_file: A string indicating the fullpath to index.yaml.
    require_indexes: A bool. If True, queries that require index generation will
      fail.
    datastore_path: A string indicating the path to local datastore data.
    stub_type: An integer, which is one of
      datastore_converter.StubTypes.PYTHON_SQLITE_STUB or
      datastore_converter.StubTypes.JAVA_EMULATOR.
    cmd: A string representing the path to a executable shell script that
      invokes the emulator.
    is_test: A boolean. If True, run emulator in --testing mode for unittests.
      Otherwise override some emulator flags for dev_appserver use cases.
    auto_id_policy: A string specifying how the emualtor assigns auto id,
      default to sequential.

  Returns:
    A threading.Thread object that asynchronously launch the emulator.
  """
  emulator_host = 'localhost:%d' % emulator_port
  launching_thread = None
  emulator_manager = cloud_emulator_manager.DatastoreEmulatorManager(
      cmd=cmd, is_test=is_test)
  emulator_manager.CheckOptions(index_file=index_file,
                                storage_file=datastore_path)
  def _launch(need_conversion):
    """Launch the emulator and convert SQLite data if needed.

    Args:
      need_conversion: A bool. If True convert sqlite data to emulator data.
    """
    emulator_manager.Launch(
        emulator_port, silent, index_file, require_indexes, datastore_path,
        ('SEQUENTIAL' if auto_id_policy == datastore_stub_util.SEQUENTIAL
         else 'SCATTERED'))
    if need_conversion:
      logging.info(
          'Converting datastore_sqlite_stub data in %s to Cloud Datastore '
          'emulator data in %s.', sqlite_data_renamed, datastore_path)
      datastore_converter.convert_sqlite_data_to_emulator(
          app_id, sqlite_data_renamed, emulator_host)
    # Shutdown datastore emulator before dev_appserver quits.
    shutdown.extra_shutdown_callback.append(emulator_manager.Shutdown)

  if stub_type == datastore_converter.StubTypes.PYTHON_SQLITE_STUB:
    sqlite_data_renamed = datastore_path + '.sqlitestub'
    shutil.move(datastore_path, sqlite_data_renamed)
    logging.info('SQLite stub data has been renamed to %s', sqlite_data_renamed)
    launching_thread = threading.Thread(target=_launch, args=[True])
  else:
    launching_thread = threading.Thread(target=_launch, args=[False])
  launching_thread.start()
  os.environ['DATASTORE_EMULATOR_HOST'] = emulator_host
  return launching_thread


def create_api_server(
    request_info, storage_path, options, app_id, app_root):
  """Creates an API server.

  Args:
    request_info: An apiproxy_stub.RequestInfo instance used by the stubs to
      lookup information about the request associated with an API call.
    storage_path: A string directory for storing API stub data.
    options: An instance of argparse.Namespace containing command line flags.
    app_id: String representing an application ID, used for configuring paths
      and string constants in API stubs.
    app_root: The path to the directory containing the user's
      application e.g. "/home/joe/myapp", used for locating application yaml
      files, eg index.yaml for the datastore stub.

  Returns:
    An instance of APIServer.

  Raises:
    DatastoreFileError: Cloud Datastore emulator is used while stub_type is
      datastore_converter.StubTypes.PYTHON_FILE_STUB.
  """
  datastore_path = get_datastore_path(storage_path, options.datastore_path)

  logs_path = options.logs_path or os.path.join(storage_path, 'logs.db')
  search_index_path = options.search_indexes_path or os.path.join(
      storage_path, 'search_indexes')
  blobstore_path = options.blobstore_path or os.path.join(
      storage_path, 'blobs')

  if options.clear_datastore:
    _clear_datastore_storage(datastore_path)
  if options.clear_search_indexes:
    _clear_search_indexes_storage(search_index_path)
  if options.auto_id_policy == datastore_stub_util.SEQUENTIAL:
    logging.warn("--auto_id_policy='sequential' is deprecated. This option "
                 "will be removed in a future release.")

  application_address = '%s' % options.host
  if options.port and options.port != 80:
    application_address += ':' + str(options.port)

  user_login_url = '/%s?%s=%%s' % (
      login.LOGIN_URL_RELATIVE, login.CONTINUE_PARAM)
  user_logout_url = '/%s?%s=%%s' % (
      login.LOGOUT_URL_RELATIVE, login.CONTINUE_PARAM)

  if options.datastore_consistency_policy == 'time':
    consistency = datastore_stub_util.TimeBasedHRConsistencyPolicy()
  elif options.datastore_consistency_policy == 'random':
    consistency = datastore_stub_util.PseudoRandomHRConsistencyPolicy()
  elif options.datastore_consistency_policy == 'consistent':
    consistency = datastore_stub_util.PseudoRandomHRConsistencyPolicy(1.0)
  else:
    assert 0, ('unknown consistency policy: %r' %
               options.datastore_consistency_policy)

  stub_type = (datastore_converter.get_stub_type(datastore_path)
               if os.path.exists(datastore_path) else None)
  gcd_emulator_launching_thread = None
  if options.support_datastore_emulator:
    if stub_type == datastore_converter.StubTypes.PYTHON_FILE_STUB:
      raise DatastoreFileError(
          'The datastore file %s cannot be recognized by dev_appserver. Please '
          'restart dev_appserver with --clear_datastore=1' % datastore_path)
    # The flag should override environment variable regarding emulator host.
    if options.running_datastore_emulator_host:
      os.environ['DATASTORE_EMULATOR_HOST'] = (
          options.running_datastore_emulator_host)
    env_emulator_host = os.environ.get('DATASTORE_EMULATOR_HOST')
    if env_emulator_host:  # emulator already running, reuse it.
      logging.warning(
          'Detected environment variable DATASTORE_EMULATOR_HOST=%s, '
          'dev_appserver will speak to the Cloud Datastore emulator running on '
          'this address. The datastore_path %s will be neglected.\nIf you '
          'want datastore to store on %s, remove DATASTORE_EMULATOR_HOST '
          'from environment variables and restart dev_appserver',
          env_emulator_host, datastore_path, datastore_path)
    else:
      gcd_emulator_launching_thread = _launch_gcd_emulator(
          app_id=app_id,
          emulator_port=(options.datastore_emulator_port
                         if options.datastore_emulator_port else
                         portpicker.pick_unused_port()),
          silent=options.dev_appserver_log_level != 'debug',
          index_file=os.path.join(app_root, 'index.yaml'),
          require_indexes=options.require_indexes,
          datastore_path=datastore_path,
          stub_type=stub_type,
          cmd=options.datastore_emulator_cmd,
          is_test=options.datastore_emulator_is_test_mode,
          auto_id_policy=options.auto_id_policy)
  else:
    # Use SQLite stub.
    # For historic reason we are still supporting conversion from file stub to
    # SQLite stub data. But this conversion will go away.



    if stub_type == datastore_converter.StubTypes.PYTHON_FILE_STUB:
      datastore_converter.convert_datastore_file_stub_data_to_sqlite(
          app_id, datastore_path)

  stub_util.setup_stubs(
      request_data=request_info,
      app_id=app_id,
      application_root=app_root,
      # The "trusted" flag is only relevant for Google administrative
      # applications.
      trusted=getattr(options, 'trusted', False),
      appidentity_email_address=options.appidentity_email_address,
      appidentity_private_key_path=os.path.abspath(
          options.appidentity_private_key_path)
      if options.appidentity_private_key_path else None,
      blobstore_path=blobstore_path,
      datastore_path=datastore_path,
      datastore_consistency=consistency,
      datastore_require_indexes=options.require_indexes,
      datastore_auto_id_policy=options.auto_id_policy,
      images_host_prefix='http://%s' % application_address,
      logs_path=logs_path,
      mail_smtp_host=options.smtp_host,
      mail_smtp_port=options.smtp_port,
      mail_smtp_user=options.smtp_user,
      mail_smtp_password=options.smtp_password,
      mail_enable_sendmail=options.enable_sendmail,
      mail_show_mail_body=options.show_mail_body,
      mail_allow_tls=options.smtp_allow_tls,
      search_index_path=search_index_path,
      taskqueue_auto_run_tasks=options.enable_task_running,
      taskqueue_default_http_server=application_address,
      user_login_url=user_login_url,
      user_logout_url=user_logout_url,
      default_gcs_bucket_name=options.default_gcs_bucket_name,
      appidentity_oauth_url=options.appidentity_oauth_url,
      datastore_grpc_stub_class=(
          datastore_grpc_stub.DatastoreGrpcStub
          if options.support_datastore_emulator else None)
  )
  return APIServer(
      options.api_host, options.api_port, app_id,
      options.api_server_supports_grpc or options.support_datastore_emulator,
      options.grpc_api_port, options.enable_host_checking,
      gcd_emulator_launching_thread)


def _clear_datastore_storage(datastore_path):
  """Delete the datastore storage file at the given path."""
  # lexists() returns True for broken symlinks, where exists() returns False.
  if os.path.lexists(datastore_path):
    try:
      os.remove(datastore_path)
    except OSError, err:
      logging.warning(
          'Failed to remove datastore file %r: %s', datastore_path, err)


def _clear_search_indexes_storage(search_index_path):
  """Delete the search indexes storage file at the given path."""
  # lexists() returns True for broken symlinks, where exists() returns False.
  if os.path.lexists(search_index_path):
    try:
      os.remove(search_index_path)
    except OSError, err:
      logging.warning(
          'Failed to remove search indexes file %r: %s', search_index_path, err)


def get_storage_path(path, app_id):
  """Returns a path to the directory where stub data can be stored."""
  _, _, app_id = app_id.replace(':', '_').rpartition('~')
  if path is None:
    for path in _generate_storage_paths(app_id):
      try:
        os.mkdir(path, 0700)
      except OSError, err:
        if err.errno == errno.EEXIST:
          # Check that the directory is only accessable by the current user to
          # protect against an attacker creating the directory in advance in
          # order to access any created files. Windows has per-user temporary
          # directories and st_mode does not include per-user permission
          # information so assume that it is safe.
          if sys.platform == 'win32' or (
              (os.stat(path).st_mode & 0777) == 0700 and os.path.isdir(path)):
            return path
          else:
            continue
        raise
      else:
        return path
  elif not os.path.exists(path):
    os.mkdir(path)
    return path
  elif not os.path.isdir(path):
    raise IOError('the given storage path %r is a file, a directory was '
                  'expected' % path)
  else:
    return path


def get_datastore_path(storage_path, datastore_path):
  """Gets the path to datastore data file.

  Args:
    storage_path: A string directory for storing API stub data.
    datastore_path: A string representing the path to local datastore file.

  Returns:
    A string representing the path to datastore data file.
  """
  return datastore_path or os.path.join(storage_path, 'datastore.db')


def _generate_storage_paths(app_id):
  """Yield an infinite sequence of possible storage paths."""
  if sys.platform == 'win32':
    # The temp directory is per-user on Windows so there is no reason to add
    # the username to the generated directory name.
    user_format = ''
  else:
    try:
      user_name = getpass.getuser()
    except Exception:  # pylint: disable=broad-except
      # The possible set of exceptions is not documented.
      user_format = ''
    else:
      user_format = '.%s' % user_name

  tempdir = tempfile.gettempdir()
  yield os.path.join(tempdir, 'appengine.%s%s' % (app_id, user_format))
  for i in itertools.count(1):
    yield os.path.join(tempdir, 'appengine.%s%s.%d' % (app_id, user_format, i))


PARSER = cli_parser.create_command_line_parser(
    cli_parser.API_SERVER_CONFIGURATION)


def main():
  """Parses command line options and launches the API server."""
  shutdown.install_signal_handlers()

  options = PARSER.parse_args()
  logging.getLogger().setLevel(
      constants.LOG_LEVEL_TO_PYTHON_CONSTANT[options.dev_appserver_log_level])

  # Parse the application configuration if config_paths are provided, else
  # provide sensible defaults.
  if options.config_paths:
    app_config = application_configuration.ApplicationConfiguration(
        options.config_paths, options.app_id)
    app_id = app_config.app_id
    app_root = app_config.modules[0].application_root
  else:
    app_id = (options.app_id_prefix + options.app_id if
              options.app_id else DEFAULT_API_SERVER_APP_ID)
    app_root = tempfile.mkdtemp()
  util.setup_environ(app_id)

  # pylint: disable=protected-access
  if options.java_app_base_url:
    # If the user specified a java_app_base_url, then the actual app is running
    # via the classic Java SDK, so we use the appropriate dispatcher that will
    # send requests to the Java app rather than forward them internally to a
    # devappserver2 module.
    dispatcher = _LocalJavaAppDispatcher(
        java_app_base_url=options.java_app_base_url)
  else:
    # TODO: Rename LocalFakeDispatcher or re-implement for
    # api_server.py.
    dispatcher = request_info_lib._LocalFakeDispatcher()
  request_info = wsgi_request_info.WSGIRequestInfo(dispatcher)
  # pylint: enable=protected-access

  metrics_logger = metrics.GetMetricsLogger()
  metrics_logger.Start(
      options.google_analytics_client_id,
      user_agent=options.google_analytics_user_agent,
      support_datastore_emulator=options.support_datastore_emulator,
      category=metrics.API_SERVER_CATEGORY)

  # When Cloud Datastore Emulator is invoked from api_server, it should be in
  # test mode, which stores in memory.
  options.datastore_emulator_is_test_mode = True
  server = create_api_server(
      request_info=request_info,
      storage_path=get_storage_path(options.storage_path, app_id),
      options=options, app_id=app_id, app_root=app_root)

  try:
    server.start()
    shutdown.wait_until_shutdown()
  finally:
    metrics.GetMetricsLogger().Stop()
    server.quit()


if __name__ == '__main__':
  main()
