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




"""A module to use service stubs for testing.

To test applications that use App Engine services, such as datastore,
developers can use the available stub implementations. Service stubs behave like
the original service without causing permanent side effects. The datastore stub,
for example, allows you to write entities into memory without storing them to
the actual datastore. The testbed module makes using those stubs for testing
easier.

Example::

    import unittest

    from google.appengine.ext import ndb
    from google.appengine.ext import testbed


    class TestModel(ndb.Model):
      number = ndb.IntegerProperty(default=42)


    class MyTestCase(unittest.TestCase):

      def setUp(self):
        # First, create an instance of the Testbed class.
        self.testbed = testbed.Testbed()
        # Then activate the testbed, which will allow you to use
        # service stubs.
        self.testbed.activate()
        # Next, declare which service stubs you want to use.
        self.testbed.init_datastore_v3_stub()
        self.testbed.init_memcache_stub()

      def tearDown(self):
        # Don't forget to deactivate the testbed after the tests are
        # completed. If the testbed is not deactivated, the original
        # stubs will not be restored.
        self.testbed.deactivate()

      def testInsertEntity(self):
        # Because we use the datastore stub, this put() does not have
        # permanent side effects.
        TestModel().put()
        fetched_entities = TestModel.all().fetch(2)
        self.assertEqual(1, len(fetched_entities))
        self.assertEqual(42, fetched_entities[0].number)



Enable stubs and disable services
---------------------------------

The testbed module allows you to use stubs for the following services:
    - capability_service
    - channel
    - datastore_v3 (aka datastore)
    - images (only for dev_appserver)
    - mail (only for dev_appserver)
    - memcache
    - taskqueue
    - urlfetch
    - user
    - xmpp

To use a particular service stub, call `self.init_SERVICENAME_stub()`. Using
this call will replace calls to the service with calls to the service stub. If
you want to disable any calls to a particular service, call
`self.init_SERVICENAME_stub(enable=False)`. This call can be useful if you want
to test code that must not use a certain service.


Environment variables
---------------------

App Engine service stubs often depend on environment variables. For example, the
datastore stub uses `os.environ['APPLICATION_ID']` to store entities linked to a
particular app. testbed will use default values if nothing else is provided, but
you can change those values with `self.setup_env()`.
"""








import os
import threading









from google.appengine.api import apiproxy_stub
from google.appengine.api import apiproxy_stub_map
from google.appengine.api import datastore_file_stub
try:
  from google.appengine.api import mail_stub
except AttributeError:



  mail_stub = None
from google.appengine.api import request_info
from google.appengine.api import urlfetch_stub
from google.appengine.api import user_service_stub
from google.appengine.api.app_identity import app_identity_stub
from google.appengine.api.blobstore import blobstore_stub
from google.appengine.api.blobstore import dict_blob_storage
from google.appengine.api.capabilities import capability_stub
from google.appengine.api.channel import channel_service_stub
from google.appengine.api.files import file_service_stub
try:
  from google.appengine.api.images import images_stub
except ImportError:
  images_stub = None
try:
  from google.appengine.api.logservice import logservice_stub
except ImportError:
  logservice_stub = None
from google.appengine.api.memcache import memcache_stub
from google.appengine.api.modules import modules_stub
try:
  from google.appengine.api.search import simple_search_stub
except ImportError:
  simple_search_stub = None
from google.appengine.api.taskqueue import taskqueue_stub
from google.appengine.api.xmpp import xmpp_service_stub
try:
  from google.appengine.datastore import datastore_sqlite_stub
except ImportError:
  datastore_sqlite_stub = None
from google.appengine.datastore import cloud_datastore_v1_stub
from google.appengine.datastore import datastore_pbs
from google.appengine.datastore import datastore_stub_util
from google.appengine.datastore import datastore_v4_stub
from google.appengine.ext.cloudstorage import common as gcs_common
from google.appengine.ext.cloudstorage import stub_dispatcher as gcs_dispatcher
try:


  from google.appengine.ext.remote_api import remote_api_stub
except ImportError:
  pass
try:




  from google.appengine.ext.testbed import apiserver_util
except ImportError:
  apiserver_util = None


DEFAULT_ENVIRONMENT = {
    'APPENGINE_RUNTIME': 'python27',
    'APPLICATION_ID': 'testbed-test',
    'AUTH_DOMAIN': 'gmail.com',
    'HTTP_HOST': 'testbed.example.com',
    'CURRENT_MODULE_ID': 'default',
    'CURRENT_VERSION_ID': 'testbed-version',
    'REQUEST_ID_HASH': 'testbed-request-id-hash',
    'REQUEST_LOG_ID': '7357B3D7091D',
    'SERVER_NAME': 'testbed.example.com',
    'SERVER_SOFTWARE': 'Development/1.0 (testbed)',
    'SERVER_PORT': '80',
    'USER_EMAIL': '',
    'USER_ID': '',
}

# Deprecated legacy aliases for default environment variables. New code

DEFAULT_APP_ID = DEFAULT_ENVIRONMENT['APPLICATION_ID']
DEFAULT_AUTH_DOMAIN = DEFAULT_ENVIRONMENT['AUTH_DOMAIN']
DEFAULT_SERVER_NAME = DEFAULT_ENVIRONMENT['SERVER_NAME']
DEFAULT_SERVER_SOFTWARE = DEFAULT_ENVIRONMENT['SERVER_SOFTWARE']
DEFAULT_SERVER_PORT = DEFAULT_ENVIRONMENT['SERVER_PORT']


APP_IDENTITY_SERVICE_NAME = 'app_identity_service'
BLOBSTORE_SERVICE_NAME = 'blobstore'
CAPABILITY_SERVICE_NAME = 'capability_service'
CHANNEL_SERVICE_NAME = 'channel'
DATASTORE_SERVICE_NAME = 'datastore_v3'
FILES_SERVICE_NAME = 'file'
IMAGES_SERVICE_NAME = 'images'
LOG_SERVICE_NAME = 'logservice'
MAIL_SERVICE_NAME = 'mail'
MEMCACHE_SERVICE_NAME = 'memcache'
TASKQUEUE_SERVICE_NAME = 'taskqueue'
URLFETCH_SERVICE_NAME = 'urlfetch'
USER_SERVICE_NAME = 'user'
XMPP_SERVICE_NAME = 'xmpp'
SEARCH_SERVICE_NAME = 'search'
MODULES_SERVICE_NAME = 'modules'


INIT_STUB_METHOD_NAMES = {
    APP_IDENTITY_SERVICE_NAME: 'init_app_identity_stub',
    BLOBSTORE_SERVICE_NAME: 'init_blobstore_stub',
    CAPABILITY_SERVICE_NAME: 'init_capability_stub',
    CHANNEL_SERVICE_NAME: 'init_channel_stub',
    DATASTORE_SERVICE_NAME: 'init_datastore_v3_stub',
    FILES_SERVICE_NAME: 'init_files_stub',
    IMAGES_SERVICE_NAME: 'init_images_stub',
    LOG_SERVICE_NAME: 'init_logservice_stub',
    MAIL_SERVICE_NAME: 'init_mail_stub',
    MEMCACHE_SERVICE_NAME: 'init_memcache_stub',
    TASKQUEUE_SERVICE_NAME: 'init_taskqueue_stub',
    URLFETCH_SERVICE_NAME: 'init_urlfetch_stub',
    USER_SERVICE_NAME: 'init_user_stub',
    XMPP_SERVICE_NAME: 'init_xmpp_stub',
    SEARCH_SERVICE_NAME: 'init_search_stub',
    MODULES_SERVICE_NAME: 'init_modules_stub'
}


SUPPORTED_SERVICES = sorted(INIT_STUB_METHOD_NAMES)


AUTO_ID_POLICY_SEQUENTIAL = datastore_stub_util.SEQUENTIAL
AUTO_ID_POLICY_SCATTERED = datastore_stub_util.SCATTERED



def urlfetch_to_gcs_stub(url, payload, method, headers, request, response,
                         follow_redirects=False, deadline=None,
                         validate_certificate=None, http_proxy=None):

  """Forwards Google Cloud Storage `urlfetch` requests to gcs_dispatcher."""
  headers_map = dict(
      (header.key().lower(), header.value()) for header in headers)
  result = gcs_dispatcher.dispatch(method, headers_map, url, payload)
  response.set_statuscode(result.status_code)
  response.set_content(result.content[:urlfetch_stub.MAX_RESPONSE_SIZE])
  for k, v in result.headers.iteritems():
    if k.lower() == 'content-length' and method != 'HEAD':
      v = len(response.content())
    header_proto = response.add_header()
    header_proto.set_key(k)
    header_proto.set_value(str(v))
  if len(result.content) > urlfetch_stub.MAX_RESPONSE_SIZE:
    response.set_contentwastruncated(True)


def urlmatcher_for_gcs_stub(url):
  """Determines whether a URL should be handled by the Cloud Storage stub."""
  return url.startswith(gcs_common.local_api_url())



GCS_URLMATCHERS_TO_FETCH_FUNCTIONS = [
    (urlmatcher_for_gcs_stub, urlfetch_to_gcs_stub)]


class Error(Exception):
  """Base testbed error type."""


class NotActivatedError(Error):
  """Raised if the used testbed instance is not activated."""


class StubNotSupportedError(Error):
  """Raised if an unsupported service stub is accessed."""


class EmulatorSupportChecker(object):
  """A static class. Checks whether datastore emulator is supported.














  """

  _check_lock = threading.Lock()
  _use_datastore_emulator = None
  _api_port = None

  @classmethod
  def get_api_port(cls):
    """Returns the integer port number that api_server listens on."""
    return cls._api_port

  @classmethod
  def get_emulator_port(cls):
    """Returns the integer port number that datastore emulator listens on."""
    return cls._emulator_port

  @classmethod
  def init(cls, api_port, emulator_port):
    cls._api_port = api_port
    cls._emulator_port = emulator_port
    cls._use_datastore_emulator = True

  @classmethod
  def check(cls):
    """Checks whether cloud datastore should be used.

    In a unittest test process, the first call to this method sets the value of
    _use_datastore_emulator and _api_port. Subsequent calls to this method can
    read _use_datastore_emulator.

    Returns:
      A boolean that indicates whether cloud datastore should be used.
    """

    cls._check_lock.acquire()
    if cls._use_datastore_emulator is None:
      if os.environ.get('APPENGINE_TESTBED_USES_DATASTORE_EMULATOR'):

        cls._use_datastore_emulator = True
        cls._api_port = int(os.environ['API_SERVER_PORT'])
        cls._emulator_port = int(os.environ['DATASTORE_EMULATOR_PORT'])








      else:
        cls._use_datastore_emulator = False
    cls._check_lock.release()
    return cls._use_datastore_emulator



class Testbed(object):
  """Class providing APIs to manipulate stubs for testing.

  This class allows you to replace App Engine services with fake stub
  implementations. These stubs act like the actual APIs but do not invoke the
  replaced services.

  In order to use a fake service stub or disable a real service, invoke the
  corresponding `init_*_stub` methods of this class.
  """

  def __init__(self):
    self._activated = False

    self._enabled_stubs = {}

    self._blob_storage = None

  def activate(self, use_datastore_emulator=False):
    """Activates the testbed.

    Invoking this method will also assign default values to environment
    variables that are required by App Engine services, such as
    `os.environ['APPLICATION_ID']`. You can set custom values with
    `setup_env()`.

    Args:
      use_datastore_emulator: True if user specifies testbed to use the Cloud
        Datastore Emulator.
    """
    self._orig_env = dict(os.environ)
    self.setup_env()






    self._original_stub_map = apiproxy_stub_map.apiproxy
    self._test_stub_map = apiproxy_stub_map.APIProxyStubMap()
    internal_map = self._original_stub_map._APIProxyStubMap__stub_map
    self._test_stub_map._APIProxyStubMap__stub_map = dict(internal_map)
    apiproxy_stub_map.apiproxy = self._test_stub_map
    self._activated = True

    if use_datastore_emulator:
      if not EmulatorSupportChecker.check():
        api_port, emulator_port = apiserver_util.setup_api_server()
        EmulatorSupportChecker.init(api_port, emulator_port)
      self._use_datastore_emulator = True
    else:
      self._use_datastore_emulator = EmulatorSupportChecker.check()
    if self._use_datastore_emulator:
      self.api_port = EmulatorSupportChecker.get_api_port()
      self.rpc_server = remote_api_stub.ConfigureRemoteApi(
          os.environ['APPLICATION_ID'],
          '/',
          lambda: ('', ''),
          'localhost:%d' % self.api_port,
          services=[],
          apiproxy=self._test_stub_map,
          use_remote_datastore=False)
      self._emulator_port = EmulatorSupportChecker.get_emulator_port()

  def deactivate(self):
    """Deactivates the testbed.

    This method will restore the API proxy and environment variables to the
    state they were in before `activate()` was called.

    Raises:
      NotActivatedError: If called before `activate()` was called.
    """
    if not self._activated:
      raise NotActivatedError('The testbed is not activated.')

    for service_name, deactivate_callback in self._enabled_stubs.iteritems():
      if deactivate_callback:
        deactivate_callback(self._test_stub_map.GetStub(service_name))

    apiproxy_stub_map.apiproxy = self._original_stub_map
    self._enabled_stubs = {}


    os.environ.clear()
    os.environ.update(self._orig_env)
    self._blob_storage = None
    self._activated = False

  def setup_env(self, overwrite=False, **kwargs):
    """Sets default and custom environment variables.

    By default, all of the items in `DEFAULT_ENVIRONMENT` will be created
    without being specified.  To set a value other than the default, or to pass
    a custom environment variable, pass a corresponding keyword argument.

    Example::

        # All defaults
        testbed_instance.setup_env()
        # All defaults, overriding AUTH_DOMAIN
        testbed_instance.setup_env(auth_domain='custom')
        # All defaults; adds a custom os.environ['CUSTOM'] = 'foo'
        testbed_instance.setup_env(custom='foo')


    To overwrite the values set by a previous invocation, pass `overwrite=True`.
    Passing this value will not result in an `OVERWRITE` entry in `os.environ`.

    Args:
      overwrite: Boolean; specifies whether to overwrite items with
          corresponding entries in `os.environ`.
      **kwargs: Environment variables to set. The name of the argument will be
          uppercased and used as a key in `os.environ`.
    """
    merged_kwargs = {}
    for key, value in kwargs.iteritems():

      if key == 'app_id':
        key = 'APPLICATION_ID'
      merged_kwargs[key.upper()] = value
    if not overwrite:
      for key, value in DEFAULT_ENVIRONMENT.iteritems():
        if key not in merged_kwargs:
          merged_kwargs[key] = value
    for key, value in merged_kwargs.iteritems():
      if overwrite or key not in os.environ:
        os.environ[key] = value

  def _register_stub(self, service_name, stub, deactivate_callback=None):
    """Registers a service stub.

    Args:
      service_name: The name of the service the stub represents.
      stub: The stub.
      deactivate_callback: An optional function to call to deactivate the
          stub. Must accept the stub as the only argument.

    Raises:
      NotActivatedError: The testbed is not activated.
    """
    self._disable_stub(service_name)
    self._test_stub_map.RegisterStub(service_name, stub)
    self._enabled_stubs[service_name] = deactivate_callback

  def _disable_stub(self, service_name):
    """Disables a service stub.

    Args:
      service_name: The name of the service to disable.

    Raises:
      NotActivatedError: The testbed is not activated.
    """
    if not self._activated:
      raise NotActivatedError('The testbed is not activated.')
    deactivate_callback = self._enabled_stubs.pop(service_name, None)
    if deactivate_callback:
      deactivate_callback(self._test_stub_map.GetStub(service_name))
    if service_name in self._test_stub_map._APIProxyStubMap__stub_map:
      del self._test_stub_map._APIProxyStubMap__stub_map[service_name]

  def get_stub(self, service_name):
    """Gets the stub for a service.

    Args:
      service_name: The name of the service.

    Returns:
      The stub for `service_name`.

    Raises:
      NotActivatedError: The testbed is not activated.
      StubNotSupportedError: The service is not supported by testbed.
      StubNotEnabledError: The service stub has not been enabled.
    """
    if not self._activated:
      raise NotActivatedError('The testbed is not activated.')
    if service_name not in SUPPORTED_SERVICES:
      msg = 'The "%s" service is not supported by testbed' % service_name
      raise StubNotSupportedError(msg)
    if service_name not in self._enabled_stubs:
      return None
    return self._test_stub_map.GetStub(service_name)

  def init_app_identity_stub(self, enable=True):
    """Enables the app identity stub.

    Args:
      enable: `True` if the fake service should be enabled, or `False` if the
          real service should be disabled.
    """
    if not enable:
      self._disable_stub(APP_IDENTITY_SERVICE_NAME)
      return

    stub = app_identity_stub.AppIdentityServiceStub()
    self._register_stub(APP_IDENTITY_SERVICE_NAME, stub)

  def _get_blob_storage(self):
    """Creates a blob storage for stubs if needed."""
    if self._blob_storage is None:
      self._blob_storage = dict_blob_storage.DictBlobStorage()
    return self._blob_storage

  def init_blobstore_stub(self, enable=True):
    """Enables the blobstore stub.

    Args:
      enable: `True` if the fake service should be enabled, or `False` if the
          real service should be disabled.
    """
    if not enable:
      self._disable_stub(BLOBSTORE_SERVICE_NAME)
      return

    stub = blobstore_stub.BlobstoreServiceStub(self._get_blob_storage())
    self._register_stub(BLOBSTORE_SERVICE_NAME, stub)

  def init_capability_stub(self, enable=True):
    """Enables the capability stub.

    Args:
      enable: `True` if the fake service should be enabled, or `False` if the
          real service should be disabled.
    """
    if not enable:
      self._disable_stub(CAPABILITY_SERVICE_NAME)
      return
    stub = capability_stub.CapabilityServiceStub()
    self._register_stub(CAPABILITY_SERVICE_NAME, stub)

  def init_channel_stub(self, enable=True):
    """Enables the channel stub.

    Args:
      enable: `True` if the fake service should be enabled, or `False` if the
          real service should be disabled.
    """
    if not enable:
      self._disable_stub(CHANNEL_SERVICE_NAME)
      return
    stub = channel_service_stub.ChannelServiceStub()
    self._register_stub(CHANNEL_SERVICE_NAME, stub)

  def init_datastore_v3_stub(self, enable=True, datastore_file=None,
                             use_sqlite=False,
                             auto_id_policy=AUTO_ID_POLICY_SEQUENTIAL,
                             **stub_kw_args):
    """Enables the datastore stub.

    The `datastore_file` argument can be set to the path of an existing
    datastore file, or `None` (default) to use an in-memory datastore that is
    initially empty. If you use the sqlite stub and have defined
    `datastore_file`, the changes that you apply in a test will be written to
    the file. If you use the default datastore stub, changes are *not* saved to
    disk unless you set `save_changes=True`.

    Note:
        You can only access those entities of the datastore file that use the
        same application ID as the test run. You can change the application ID
        for a test with `setup_env()`.

    Args:
      enable: `True` if the fake service should be enabled, or `False` if the
          real service should be disabled.
      datastore_file: File name of a dev_appserver datastore file.
      use_sqlite: `True` to use the Sqlite stub, or `False` (default) to use
          the file stub.
      auto_id_policy: How datastore stub assigns auto IDs. This value can be
          either `AUTO_ID_POLICY_SEQUENTIAL` or `AUTO_ID_POLICY_SCATTERED`.
      **stub_kw_args: Keyword arguments passed on to the service stub.

    Raises:
      StubNotSupportedError: If datastore_sqlite_stub is None.
    """
    if self._use_datastore_emulator:





      self._disable_stub(DATASTORE_SERVICE_NAME)

      delegate_stub = (
          remote_api_stub.DatastoreStubTestbedDelegate(
              self.rpc_server, '/', stub_kw_args.get(
                  'max_request_size', apiproxy_stub.MAX_REQUEST_SIZE),
              emulator_port=self._emulator_port))
      delegate_stub.Clear()
      self._test_stub_map.RegisterStub(
          DATASTORE_SERVICE_NAME, delegate_stub)
      consistency_policy = stub_kw_args.get(
          'consistency_policy',
          datastore_stub_util.PseudoRandomHRConsistencyPolicy(probability=1.0))
      datastore_stub_util.UpdateEmulatorConfig(
          self._emulator_port, auto_id_policy, consistency_policy)
      if isinstance(consistency_policy,
                    datastore_stub_util.PseudoRandomHRConsistencyPolicy):
        consistency_policy.is_using_cloud_datastore_emulator = True
        consistency_policy.emulator_port = self._emulator_port


      self._enabled_stubs[DATASTORE_SERVICE_NAME] = None
      return
    if not enable:
      self._disable_stub(DATASTORE_SERVICE_NAME)
      self._disable_stub(datastore_v4_stub.SERVICE_NAME)
      self._disable_stub(cloud_datastore_v1_stub.SERVICE_NAME)
      return
    if use_sqlite:
      if datastore_sqlite_stub is None:
        raise StubNotSupportedError(
            'The sqlite stub is not supported in production.')
      stub = datastore_sqlite_stub.DatastoreSqliteStub(
          os.environ['APPLICATION_ID'],
          datastore_file,
          use_atexit=False,
          auto_id_policy=auto_id_policy,
          **stub_kw_args)
    else:
      stub_kw_args.setdefault('save_changes', False)
      stub = datastore_file_stub.DatastoreFileStub(
          os.environ['APPLICATION_ID'],
          datastore_file,
          use_atexit=False,
          auto_id_policy=auto_id_policy,
          **stub_kw_args)
    self._register_stub(DATASTORE_SERVICE_NAME, stub,
                        self._deactivate_datastore_v3_stub)
    v4_stub = datastore_v4_stub.DatastoreV4Stub(os.environ['APPLICATION_ID'])
    self._register_stub(datastore_v4_stub.SERVICE_NAME, v4_stub)
    if datastore_pbs._CLOUD_DATASTORE_ENABLED:
      helper = datastore_pbs.googledatastore.helper
      credential_env = helper._DATASTORE_USE_STUB_CREDENTIAL_FOR_TEST_ENV
      os.environ[credential_env] = 'True'
      cloud_stub = cloud_datastore_v1_stub.CloudDatastoreV1Stub(
          os.environ['APPLICATION_ID'])
      self._register_stub(cloud_datastore_v1_stub.SERVICE_NAME, cloud_stub)

  def _deactivate_datastore_v3_stub(self, stub):
    stub.Write()

  def init_files_stub(self, enable=True):
    """Enables the Files API stub.

    Args:
      enable: `True` if the fake service should be enabled, or `False` if the
          real service should be disabled.
    """
    if not enable:
      self._disable_stub(FILES_SERVICE_NAME)
      return

    stub = file_service_stub.FileServiceStub(self._get_blob_storage())
    self._register_stub(FILES_SERVICE_NAME, stub)

  def init_images_stub(self, enable=True, **stub_kwargs):
    """Enables the images stub.

    The images service stub is only available in dev_appserver because it uses
    the PIL library.

    Args:
      enable: `True` if the fake service should be enabled, or `False` if the
          real service should be disabled.
      **stub_kwargs: Keyword arguments passed on to the service stub.
    """
    if not enable:
      self._disable_stub(IMAGES_SERVICE_NAME)
      return
    if images_stub is None:
      msg = ('Could not initialize images API; you are likely '
             'missing the Python "PIL" module.')
      raise StubNotSupportedError(msg)
    stub = images_stub.ImagesServiceStub(**stub_kwargs)
    self._register_stub(IMAGES_SERVICE_NAME, stub)

  def init_logservice_stub(self, enable=True):
    """Enables the log service stub.

    Args:
      enable: `True` if the fake service should be enabled, or `False` if the
          real service should be disabled.

    Raises:
      StubNotSupportedError: The logservice stub is unvailable.
    """
    if not enable:
      self._disable_stub(LOG_SERVICE_NAME)
      return
    if logservice_stub is None:
      raise StubNotSupportedError(
          'The logservice stub is not supported in production.')
    stub = logservice_stub.LogServiceStub()
    self._register_stub(LOG_SERVICE_NAME, stub)

  def init_mail_stub(self, enable=True, **stub_kw_args):
    """Enables the mail stub.

    The email service stub is only available in dev_appserver because it uses
    the `subprocess` module.

    Args:
      enable: `True` if the fake service should be enabled, or `False` if the
          real service should be disabled.
      **stub_kw_args: Keyword arguments that are passed on to the service
          stub.
    """
    if not enable:
      self._disable_stub(MAIL_SERVICE_NAME)
      return
    stub = mail_stub.MailServiceStub(**stub_kw_args)
    self._register_stub(MAIL_SERVICE_NAME, stub)

  def init_memcache_stub(self, enable=True):
    """Enables the memcache stub.

    Args:
      enable: `True` if the fake service should be enabled, or `False` if the
          real service should be disabled.
    """
    if not enable:
      self._disable_stub(MEMCACHE_SERVICE_NAME)
      return
    stub = memcache_stub.MemcacheServiceStub()
    self._register_stub(MEMCACHE_SERVICE_NAME, stub)

  def init_taskqueue_stub(self, enable=True, **stub_kw_args):
    """Enables the taskqueue stub.

    Args:
      enable: `True` if the fake service should be enabled, or `False` if the
          real service should be disabled.
      **stub_kw_args: Keyword arguments passed on to the service stub.
    """
    if self._use_datastore_emulator:
      self._disable_stub(TASKQUEUE_SERVICE_NAME)
      delegate_stub = (
          remote_api_stub.TaskqueueStubTestbedDelegate(self.rpc_server, '/'))
      delegate_stub.SetUpStub(**stub_kw_args)
      self._test_stub_map.RegisterStub(
          TASKQUEUE_SERVICE_NAME, delegate_stub)
      self._enabled_stubs[TASKQUEUE_SERVICE_NAME] = None
      return
    if not enable:
      self._disable_stub(TASKQUEUE_SERVICE_NAME)
      return
    stub = taskqueue_stub.TaskQueueServiceStub(**stub_kw_args)
    self._register_stub(TASKQUEUE_SERVICE_NAME, stub)

  def init_urlfetch_stub(self, enable=True, urlmatchers=None):
    """Enables the urlfetch stub.

    The urlfetch service stub uses the urllib module to make requests. On
    appserver, urllib also relies the urlfetch infrastructure, so using this
    stub will have no effect.

    Args:
      enable: `True` if the fake service should be enabled, or `False` if the
          real service should be disabled.
      urlmatchers: optional initial sequence of (matcher, fetcher) pairs to
          populate urlmatchers_to_fetch_functions; matchers passed here, if
          any, take precedence over default matchers dispatching GCS access.
    """
    if not enable:
      self._disable_stub(URLFETCH_SERVICE_NAME)
      return
    urlmatchers_to_fetch_functions = []
    if urlmatchers:
      urlmatchers_to_fetch_functions.extend(urlmatchers)
    urlmatchers_to_fetch_functions.extend(
        GCS_URLMATCHERS_TO_FETCH_FUNCTIONS)
    stub = urlfetch_stub.URLFetchServiceStub(
        urlmatchers_to_fetch_functions=urlmatchers_to_fetch_functions)
    self._register_stub(URLFETCH_SERVICE_NAME, stub)

  def init_user_stub(self, enable=True, **stub_kw_args):
    """Enables the users stub.

    Args:
      enable: `True` if the fake service should be enabled, or `False` if the
          real service should be disabled.
      **stub_kw_args: Keyword arguments that are passed on to the service
          stub.
    """
    if not enable:
      self._disable_stub(USER_SERVICE_NAME)
      return
    stub = user_service_stub.UserServiceStub(**stub_kw_args)
    self._register_stub(USER_SERVICE_NAME, stub)

  def init_xmpp_stub(self, enable=True):
    """Enables the xmpp stub.

    Args:
      enable: `True` if the fake service should be enabled, or `False` if the
          real service should be disabled.
    """
    if not enable:
      self._disable_stub(XMPP_SERVICE_NAME)
      return
    stub = xmpp_service_stub.XmppServiceStub()
    self._register_stub(XMPP_SERVICE_NAME, stub)

  def init_search_stub(self, enable=True):
    """Enables the search stub.

    Args:
      enable: `True` if the fake service should be enabled, or `False` if the
          real service should be disabled.
    """
    if not enable:
      self._disable_stub(SEARCH_SERVICE_NAME)
      return
    if simple_search_stub is None:
      raise StubNotSupportedError('Could not initialize search API')
    stub = simple_search_stub.SearchServiceStub()
    self._register_stub(SEARCH_SERVICE_NAME, stub)

  def init_modules_stub(self, enable=True):
    """Enables the modules stub.

    Args:
      enable: `True` if the fake service should be enabled, or `False` if the
          real service should be disabled.
    """
    if not enable:
      self._disable_stub(MODULES_SERVICE_NAME)
      return

    stub = modules_stub.ModulesServiceStub(request_info._LocalRequestInfo())
    self._register_stub(MODULES_SERVICE_NAME, stub)

  def _init_stub(self, service_name, *args, **kwargs):
    """Enables a stub by service name.

    Args:
      service_name: Name of the service to initialize.  This name should be
          the name used by the service stub.
      *args: Arguments to be passed to the service stub.
      **kwargs: Keyword arguments to be passed to the service stub.

      Additional arguments are passed along to the specific stub initializer.

    Raises:
      NotActivatedError: When this function is called before testbed is
          activated or after it is deactivated.
      StubNotSupportedError: When an unsupported `service_name` is provided.
    """
    if not self._activated:
      raise NotActivatedError('The testbed is not activated.')
    method_name = INIT_STUB_METHOD_NAMES.get(service_name, None)
    if method_name is None:
      msg = 'The "%s" service is not supported by testbed' % service_name
      raise StubNotSupportedError(msg)

    method = getattr(self, method_name)
    method(*args, **kwargs)

  def init_all_stubs(self, enable=True):
    """Enables all known testbed stubs.

    Args:
      enable: `True` if the fake service should be enabled, or `False` if the
          real service should be disabled.
    """
    for service_name in SUPPORTED_SERVICES:
      self._init_stub(service_name, enable)
