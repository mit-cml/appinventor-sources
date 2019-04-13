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
"""Utility methods for operating on appengine service local stubs."""

import logging
import os
from google.appengine.api import apiproxy_stub_map
from google.appengine.api import mail_stub
from google.appengine.api import urlfetch_stub
from google.appengine.api import user_service_stub
from google.appengine.api.app_identity import app_identity_stub
from google.appengine.api.blobstore import blobstore_stub
from google.appengine.api.blobstore import file_blob_storage
from google.appengine.api.capabilities import capability_stub
from google.appengine.api.channel import channel_service_stub
from google.appengine.api.logservice import logservice_stub
from google.appengine.api.memcache import memcache_stub
from google.appengine.api.modules import modules_stub
from google.appengine.api.remote_socket import _remote_socket_stub
from google.appengine.api.search import simple_search_stub
from google.appengine.api.system import system_stub
from google.appengine.api.taskqueue import taskqueue_stub
from google.appengine.api.xmpp import xmpp_service_stub
from google.appengine.datastore import datastore_sqlite_stub
from google.appengine.datastore import datastore_stub_util
from google.appengine.datastore import datastore_v4_pb
from google.appengine.datastore import datastore_v4_stub

# We don't want to support datastore_v4 everywhere, because users are supposed
# to use the Cloud Datastore API going forward, so we don't want to put these
# entries in remote_api_servers.SERVICE_PB_MAP. But for our own implementation
# of the Cloud Datastore API we need those methods to work when an instance
# issues them, specifically the DatstoreApiServlet running as a module inside
# the app we are running. The consequence is that other app code can also
# issue datastore_v4 API requests, but since we don't document these requests
# or export them through any language bindings this is unlikely in practice.
DATASTORE_V4_METHODS = {
    'AllocateIds': (datastore_v4_pb.AllocateIdsRequest,
                    datastore_v4_pb.AllocateIdsResponse),
    'BeginTransaction': (datastore_v4_pb.BeginTransactionRequest,
                         datastore_v4_pb.BeginTransactionResponse),
    'Commit': (datastore_v4_pb.CommitRequest,
               datastore_v4_pb.CommitResponse),
    'ContinueQuery': (datastore_v4_pb.ContinueQueryRequest,
                      datastore_v4_pb.ContinueQueryResponse),
    'Lookup': (datastore_v4_pb.LookupRequest,
               datastore_v4_pb.LookupResponse),
    'Rollback': (datastore_v4_pb.RollbackRequest,
                 datastore_v4_pb.RollbackResponse),
    'RunQuery': (datastore_v4_pb.RunQueryRequest,
                 datastore_v4_pb.RunQueryResponse),
}


def setup_stubs(
    request_data,
    app_id,
    application_root,
    trusted,
    appidentity_email_address,
    appidentity_private_key_path,
    blobstore_path,
    datastore_consistency,
    datastore_path,
    datastore_require_indexes,
    datastore_auto_id_policy,
    images_host_prefix,
    logs_path,
    mail_smtp_host,
    mail_smtp_port,
    mail_smtp_user,
    mail_smtp_password,
    mail_enable_sendmail,
    mail_show_mail_body,
    mail_allow_tls,
    search_index_path,
    taskqueue_auto_run_tasks,
    taskqueue_default_http_server,
    user_login_url,
    user_logout_url,
    default_gcs_bucket_name,
    appidentity_oauth_url=None,
    datastore_grpc_stub_class=None):
  """Configures the APIs hosted by this server.

  Args:
    request_data: An apiproxy_stub.RequestInformation instance used by the
        stubs to lookup information about the request associated with an API
        call.
    app_id: The str application id e.g. "guestbook".
    application_root: The path to the directory containing the user's
        application e.g. "/home/joe/myapp".
    trusted: A bool indicating if privileged APIs should be made available.
    appidentity_email_address: Email address associated with a service account
        that has a downloadable key. May be None for no local application
        identity.
    appidentity_private_key_path: Path to private key file associated with
        service account (.pem format). Must be set if appidentity_email_address
        is set.
    blobstore_path: The path to the file that should be used for blobstore
        storage.
    datastore_consistency: The datastore_stub_util.BaseConsistencyPolicy to
        use as the datastore consistency policy.
    datastore_path: The path to the file that should be used for datastore
        storage.
    datastore_require_indexes: A bool indicating if the same production
        datastore indexes requirements should be enforced i.e. if True then
        a google.appengine.ext.db.NeedIndexError will be be raised if a query
        is executed without the required indexes.
    datastore_auto_id_policy: The type of sequence from which the datastore
        stub assigns auto IDs, either datastore_stub_util.SEQUENTIAL or
        datastore_stub_util.SCATTERED.
    images_host_prefix: The URL prefix (protocol://host:port) to prepend to
        image urls on calls to images.GetUrlBase.
    logs_path: Path to the file to store the logs data in.
    mail_smtp_host: The SMTP hostname that should be used when sending e-mails.
        If None then the mail_enable_sendmail argument is considered.
    mail_smtp_port: The SMTP port number that should be used when sending
        e-mails. If this value is None then mail_smtp_host must also be None.
    mail_smtp_user: The username to use when authenticating with the
        SMTP server. This value may be None if mail_smtp_host is also None or if
        the SMTP server does not require authentication.
    mail_smtp_password: The password to use when authenticating with the
        SMTP server. This value may be None if mail_smtp_host or mail_smtp_user
        is also None.
    mail_enable_sendmail: A bool indicating if sendmail should be used when
        sending e-mails. This argument is ignored if mail_smtp_host is not None.
    mail_show_mail_body: A bool indicating whether the body of sent e-mails
        should be written to the logs.
    mail_allow_tls: A bool indicating whether TLS should be allowed when
        communicating with an SMTP server. This argument is ignored if
        mail_smtp_host is None.
    search_index_path: The path to the file that should be used for search index
        storage.
    taskqueue_auto_run_tasks: A bool indicating whether taskqueue tasks should
        be run automatically or it the must be manually triggered.
    taskqueue_default_http_server: A str containing the address of the http
        server that should be used to execute tasks.
    user_login_url: A str containing the url that should be used for user login.
    user_logout_url: A str containing the url that should be used for user
        logout.
    default_gcs_bucket_name: A str, overriding the default bucket behavior.
    appidentity_oauth_url: A str containing the url to the oauth2 server to use
        to authenticate the private key. If set to None, then the standard
        google oauth2 server is used.
    datastore_grpc_stub_class: A type object which could be the class
        datastore_grpc_stub.DatastoreGrpcStub.
  """
  identity_stub = app_identity_stub.AppIdentityServiceStub.Create(
      email_address=appidentity_email_address,
      private_key_path=appidentity_private_key_path,
      oauth_url=appidentity_oauth_url)
  if default_gcs_bucket_name is not None:
    identity_stub.SetDefaultGcsBucketName(default_gcs_bucket_name)
  apiproxy_stub_map.apiproxy.RegisterStub('app_identity_service', identity_stub)

  blob_storage = file_blob_storage.FileBlobStorage(blobstore_path, app_id)
  apiproxy_stub_map.apiproxy.RegisterStub(
      'blobstore',
      blobstore_stub.BlobstoreServiceStub(blob_storage,
                                          request_data=request_data,
                                          storage_dir=blobstore_path,
                                          app_id=app_id))

  apiproxy_stub_map.apiproxy.RegisterStub(
      'capability_service',
      capability_stub.CapabilityServiceStub())

  apiproxy_stub_map.apiproxy.RegisterStub(
      'channel',
      channel_service_stub.ChannelServiceStub(request_data=request_data))

  if datastore_grpc_stub_class:
    apiproxy_stub_map.apiproxy.ReplaceStub(
        'datastore_v3',
        datastore_grpc_stub_class(os.environ['DATASTORE_EMULATOR_HOST']))
  else:
    apiproxy_stub_map.apiproxy.ReplaceStub(
        'datastore_v3',
        datastore_sqlite_stub.DatastoreSqliteStub(
            app_id,
            datastore_path,
            datastore_require_indexes,
            trusted,
            root_path=application_root,
            auto_id_policy=datastore_auto_id_policy,
            consistency_policy=datastore_consistency))

  apiproxy_stub_map.apiproxy.RegisterStub(
      'datastore_v4',
      datastore_v4_stub.DatastoreV4Stub(app_id))

  # pylint: disable=import-not-at-top
  try:
    from google.appengine.api.images import images_stub
  except ImportError:




    # We register a stub which throws a NotImplementedError for most RPCs.
    from google.appengine.api.images import images_not_implemented_stub
    apiproxy_stub_map.apiproxy.RegisterStub(
        'images',
        images_not_implemented_stub.ImagesNotImplementedServiceStub(
            host_prefix=images_host_prefix))
  # pylint: enable=import-not-at-top
  else:
    apiproxy_stub_map.apiproxy.RegisterStub(
        'images',
        images_stub.ImagesServiceStub(host_prefix=images_host_prefix))

  apiproxy_stub_map.apiproxy.RegisterStub(
      'logservice',
      logservice_stub.LogServiceStub(logs_path=logs_path))

  apiproxy_stub_map.apiproxy.RegisterStub(
      'mail',
      mail_stub.MailServiceStub(mail_smtp_host,
                                mail_smtp_port,
                                mail_smtp_user,
                                mail_smtp_password,
                                enable_sendmail=mail_enable_sendmail,
                                show_mail_body=mail_show_mail_body,
                                allow_tls=mail_allow_tls))

  apiproxy_stub_map.apiproxy.RegisterStub(
      'memcache',
      memcache_stub.MemcacheServiceStub())

  apiproxy_stub_map.apiproxy.RegisterStub(
      'modules',
      modules_stub.ModulesServiceStub(request_data))

  apiproxy_stub_map.apiproxy.RegisterStub(
      'remote_socket',
      _remote_socket_stub.RemoteSocketServiceStub())

  apiproxy_stub_map.apiproxy.RegisterStub(
      'search',
      simple_search_stub.SearchServiceStub(index_file=search_index_path))

  apiproxy_stub_map.apiproxy.RegisterStub(
      'system',
      system_stub.SystemServiceStub(request_data=request_data))

  apiproxy_stub_map.apiproxy.RegisterStub(
      'taskqueue',
      taskqueue_stub.TaskQueueServiceStub(
          root_path=application_root,
          auto_task_running=taskqueue_auto_run_tasks,
          default_http_server=taskqueue_default_http_server,
          request_data=request_data))
  apiproxy_stub_map.apiproxy.GetStub('taskqueue').StartBackgroundExecution()

  apiproxy_stub_map.apiproxy.RegisterStub(
      'urlfetch',
      urlfetch_stub.URLFetchServiceStub())

  apiproxy_stub_map.apiproxy.RegisterStub(
      'user',
      user_service_stub.UserServiceStub(login_url=user_login_url,
                                        logout_url=user_logout_url,
                                        request_data=request_data))

  apiproxy_stub_map.apiproxy.RegisterStub(
      'xmpp',
      xmpp_service_stub.XmppServiceStub())


def setup_test_stubs(
    request_data=None,
    app_id='myapp',
    application_root='/tmp/root',
    trusted=False,
    appidentity_email_address=None,
    appidentity_private_key_path=None,
    # TODO: is this correct? If I'm following the flow correctly, this
    # should not be a file but a directory.
    blobstore_path='/dev/null',
    datastore_consistency=None,
    datastore_path=':memory:',
    datastore_require_indexes=False,
    datastore_auto_id_policy=datastore_stub_util.SCATTERED,
    images_host_prefix='http://localhost:8080',
    logs_path=':memory:',
    mail_smtp_host='',
    mail_smtp_port=25,
    mail_smtp_user='',
    mail_smtp_password='',
    mail_enable_sendmail=False,
    mail_show_mail_body=False,
    mail_allow_tls=True,
    search_index_path=None,
    taskqueue_auto_run_tasks=False,
    taskqueue_default_http_server='http://localhost:8080',
    user_login_url='/_ah/login?continue=%s',
    user_logout_url='/_ah/logout?continue=%s',
    default_gcs_bucket_name=None,
    appidentity_oauth_url=None):
  """Similar to setup_stubs with reasonable test defaults and recallable."""

  # Reset the stub map between requests because a stub map only allows a
  # stub to be added once.
  apiproxy_stub_map.apiproxy = apiproxy_stub_map.APIProxyStubMap()

  if datastore_consistency is None:
    datastore_consistency = (
        datastore_stub_util.PseudoRandomHRConsistencyPolicy())

  setup_stubs(request_data,
              app_id,
              application_root,
              trusted,
              appidentity_email_address,
              appidentity_private_key_path,
              blobstore_path,
              datastore_consistency,
              datastore_path,
              datastore_require_indexes,
              datastore_auto_id_policy,
              images_host_prefix,
              logs_path,
              mail_smtp_host,
              mail_smtp_port,
              mail_smtp_user,
              mail_smtp_password,
              mail_enable_sendmail,
              mail_show_mail_body,
              mail_allow_tls,
              search_index_path,
              taskqueue_auto_run_tasks,
              taskqueue_default_http_server,
              user_login_url,
              user_logout_url,
              default_gcs_bucket_name,
              appidentity_oauth_url)


def cleanup_stubs():
  """Do any necessary stub cleanup e.g. saving data."""
  # Saving datastore
  logging.info('Applying all pending transactions and saving the datastore')
  datastore_stub = apiproxy_stub_map.apiproxy.GetStub('datastore_v3')
  datastore_stub.Write()
  logging.info('Saving search indexes')
  apiproxy_stub_map.apiproxy.GetStub('search').Write()
  apiproxy_stub_map.apiproxy.GetStub('taskqueue').Shutdown()

  # We early delete this global variable to avoid exceptions of calling NoneType
  # object, which would happen during DatastoreGrpcStub instance finalization.
  # Safe object finalization was implemented with PEP 442 in python3.4
  del apiproxy_stub_map.apiproxy
