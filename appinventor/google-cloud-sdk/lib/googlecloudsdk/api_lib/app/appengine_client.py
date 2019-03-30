# -*- coding: utf-8 -*- #
# Copyright 2015 Google Inc. All Rights Reserved.
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

"""Module used by gcloud to communicate with appengine services."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from __future__ import with_statement

from googlecloudsdk.api_lib.app import util
from googlecloudsdk.api_lib.app import yaml_parsing
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.console import console_io
from googlecloudsdk.core.credentials import devshell as c_devshell
from googlecloudsdk.core.credentials import http
from googlecloudsdk.core.credentials import store as c_store
from googlecloudsdk.third_party.appengine.datastore import datastore_index
from googlecloudsdk.third_party.appengine.tools import appengine_rpc_httplib2
from oauth2client import service_account
from oauth2client.contrib import gce as oauth2client_gce
import six.moves.urllib.error
import six.moves.urllib.parse
import six.moves.urllib.request


APPCFG_SCOPES = ['https://www.googleapis.com/auth/cloud-platform']

# Parameters for reading from the GCE metadata service.
METADATA_BASE = 'http://metadata.google.internal'
SERVICE_ACCOUNT_BASE = ('computeMetadata/v1/instance/service-accounts/default')

RpcServerClass = appengine_rpc_httplib2.HttpRpcServerOAuth2  # pylint: disable=invalid-name


class Error(exceptions.Error):
  """Base exception for the module."""
  pass


class UnknownConfigType(Error):
  """An exception for when trying to update a config type we don't know."""
  pass


class AppengineClient(object):
  """Client used by gcloud to communicate with appengine services.

  Attributes:
    server: The appengine server to which requests are sent.
    project: The appengine application in use.
    oauth2_access_token: An existing OAuth2 access token to use.
    oauth2_refresh_token: An existing OAuth2 refresh token to use.
    authenticate_service_account: Authenticate using the default service account
      for the Google Compute Engine VM in which gcloud is being called.
    ignore_bad_certs: Whether to ignore certificate errors when talking to the
      server.
  """

  _PREPARE_TIMEOUT_RETIRES = 15

  def __init__(self, server=None, ignore_bad_certs=False):
    self.server = server or 'appengine.google.com'
    self.project = properties.VALUES.core.project.Get(required=True)
    self.ignore_bad_certs = ignore_bad_certs
    # Auth related options
    self.oauth2_access_token = None
    self.oauth2_refresh_token = None
    self.oauth_scopes = APPCFG_SCOPES
    self.authenticate_service_account = False
    self.client_id = None
    self.client_secret = None

    credentials = c_store.LoadIfEnabled()
    if credentials:
      if isinstance(credentials, service_account.ServiceAccountCredentials):
        self.oauth2_access_token = credentials.access_token
        self.client_id = credentials.client_id
        self.client_secret = credentials.client_secret
      elif isinstance(credentials, c_devshell.DevshellCredentials):
        # TODO(b/36057357): This passes the access token to use for API calls to
        # appcfg which means that commands that are longer than the lifetime
        # of the access token may fail - e.g. some long deployments.  The proper
        # solution is to integrate appcfg closer with the Cloud SDK libraries,
        # this code will go away then and the standard credentials flow will be
        # used.
        self.oauth2_access_token = credentials.access_token
        self.client_id = None
        self.client_secret = None
      elif isinstance(credentials, oauth2client_gce.AppAssertionCredentials):
        # If we are on GCE, use the service account
        self.authenticate_service_account = True
        self.client_id = None
        self.client_secret = None
      else:
        # Otherwise use a stored refresh token
        self.oauth2_refresh_token = credentials.refresh_token
        self.client_id = credentials.client_id
        self.client_secret = credentials.client_secret

  def CleanupIndexes(self, index_yaml):
    """Removes unused datastore indexes.

    Args:
      index_yaml: The parsed yaml file with index data.
    """
    rpcserver = self._GetRpcServer()
    response = rpcserver.Send('/api/datastore/index/diff',
                              app_id=self.project, payload=index_yaml.ToYAML())
    unused_new_indexes, notused_indexes = (
        datastore_index.ParseMultipleIndexDefinitions(response))

    # Get confirmation from user which indexes should be deleted.
    deletions = datastore_index.IndexDefinitions(indexes=[])
    if notused_indexes.indexes:
      for index in notused_indexes.indexes:
        msg = ('This index is no longer defined in your index.yaml file.\n{0}'
               .format(str(index.ToYAML())))
        prompt = 'Do you want to delete this index'
        if console_io.PromptContinue(msg, prompt, default=True):
          deletions.indexes.append(index)

    # Do deletions of confirmed indexes.
    if deletions.indexes:
      response = rpcserver.Send('/api/datastore/index/delete',
                                app_id=self.project, payload=deletions.ToYAML())
      not_deleted = datastore_index.ParseIndexDefinitions(response)

      # Notify the user when indexes are not deleted.
      if not_deleted.indexes:
        not_deleted_count = len(not_deleted.indexes)
        if not_deleted_count == 1:
          warning_message = ('An index was not deleted.  Most likely this is '
                             'because it no longer exists.\n\n')
        else:
          warning_message = ('%d indexes were not deleted.  Most likely this '
                             'is because they no longer exist.\n\n'
                             % not_deleted_count)
        for index in not_deleted.indexes:
          warning_message += index.ToYAML()
        log.warning(warning_message)

  def PrepareVmRuntime(self):
    """Prepare the application for vm runtimes and return state."""
    rpcserver = self._GetRpcServer(
        timeout_max_errors=self._PREPARE_TIMEOUT_RETIRES)
    rpcserver.Send('/api/vms/prepare', app_id=self.project)

  def UpdateConfig(self, config_name, parsed_yaml):
    """Updates any of the supported config file types.

    Args:
      config_name: str, The name of the config to deploy.
      parsed_yaml: The parsed object corresponding to that config type.

    Raises:
      UnknownConfigType: If config_name is not a value config type.

    Returns:
      Whatever the underlying update methods return.
    """
    if config_name == yaml_parsing.ConfigYamlInfo.CRON:
      return self.UpdateCron(parsed_yaml)
    if config_name == yaml_parsing.ConfigYamlInfo.DISPATCH:
      return self.UpdateDispatch(parsed_yaml)
    if config_name == yaml_parsing.ConfigYamlInfo.DOS:
      return self.UpdateDos(parsed_yaml)
    if config_name == yaml_parsing.ConfigYamlInfo.INDEX:
      return self.UpdateIndexes(parsed_yaml)
    if config_name == yaml_parsing.ConfigYamlInfo.QUEUE:
      return self.UpdateQueues(parsed_yaml)
    raise UnknownConfigType(
        'Config type [{0}] is not a known config type'.format(config_name))

  def UpdateCron(self, cron_yaml):
    """Updates any new or changed cron definitions.

    Args:
      cron_yaml: The parsed yaml file with cron data.
    """
    self._GetRpcServer().Send('/api/cron/update',
                              app_id=self.project, payload=cron_yaml.ToYAML())

  def UpdateDispatch(self, dispatch_yaml):
    """Updates new or changed dispatch definitions.

    Args:
      dispatch_yaml: The parsed yaml file with dispatch data.
    """
    self._GetRpcServer().Send('/api/dispatch/update',
                              app_id=self.project,
                              payload=dispatch_yaml.ToYAML())

  def UpdateDos(self, dos_yaml):
    """Updates any new or changed dos definitions.

    Args:
      dos_yaml: The parsed yaml file with dos data.
    """
    self._GetRpcServer().Send('/api/dos/update',
                              app_id=self.project, payload=dos_yaml.ToYAML())

  def UpdateIndexes(self, index_yaml):
    """Updates indexes.

    Args:
      index_yaml: The parsed yaml file with index data.
    """
    self._GetRpcServer().Send('/api/datastore/index/add',
                              app_id=self.project, payload=index_yaml.ToYAML())

  def UpdateQueues(self, queue_yaml):
    """Updates any new or changed task queue definitions.

    Args:
      queue_yaml: The parsed yaml file with queue data.
    """
    self._GetRpcServer().Send('/api/queue/update',
                              app_id=self.project, payload=queue_yaml.ToYAML())

  def _GetRpcServer(self, timeout_max_errors=2):
    """Returns an instance of an AbstractRpcServer.

    Args:
      timeout_max_errors: How many timeout errors to retry.
    Returns:
      A new AbstractRpcServer, on which RPC calls can be made.
    """
    log.debug('Host: {0}'.format(self.server))

    if self._IsGceEnvironment():
      credentials = oauth2client_gce.AppAssertionCredentials()
    else:
      credentials = None

    # In this case, the get_user_credentials parameters to the RPC server
    # constructor is actually an OAuth2Parameters.

    get_user_credentials = (
        appengine_rpc_httplib2.HttpRpcServerOAuth2.OAuth2Parameters(
            access_token=self.oauth2_access_token,
            client_id=self.client_id,
            client_secret=self.client_secret,
            scope=APPCFG_SCOPES,
            refresh_token=self.oauth2_refresh_token,
            credential_file=None,
            token_uri=None,
            credentials=credentials))
    # Also set gflags flag... this is a bit of a hack.
    if hasattr(appengine_rpc_httplib2.tools, 'FLAGS'):
      appengine_rpc_httplib2.tools.FLAGS.auth_local_webserver = True

    server = RpcServerClass(
        self.server,
        get_user_credentials,
        util.GetUserAgent(),
        util.GetSourceName(),
        host_override=None,
        save_cookies=True,
        auth_tries=3,
        timeout_max_errors=timeout_max_errors,
        account_type='HOSTED_OR_GOOGLE',
        secure=True,
        ignore_certs=self.ignore_bad_certs,
        http_object=http.Http())
    # TODO(b/36050949) Hack to avoid failure due to missing cacerts.txt
    # resource.
    server.certpath = None
    # Don't use a cert file if the user passed ignore-bad-certs.
    server.cert_file_available = not self.ignore_bad_certs
    return util.RPCServer(server)

  def _IsGceEnvironment(self):
    """Determine if we are running in a GCE environment.

    Returns:
      True if we are running in a GCE environment.

    Raises:
      Error: The user has requested authentication for a service account but the
      environment is not correct for that to work.
    """
    if self.authenticate_service_account:
      # Avoid hard-to-understand errors later by checking that we have a
      # metadata service (so we are in a GCE VM) and that the VM is configured
      # with access to the appengine.admin scope.
      url = '%s/%s/scopes' % (METADATA_BASE, SERVICE_ACCOUNT_BASE)
      try:
        req = six.moves.urllib.request.Request(
            url, headers={'Metadata-Flavor': 'Google'})
        vm_scopes_string = six.moves.urllib.request.urlopen(req).read()
      except six.moves.urllib.error.URLError as e:
        raise Error(
            'Could not obtain scope list from metadata service: %s: %s. This '
            'may be because we are not running in a Google Compute Engine VM.' %
            (url, e))
      vm_scopes = vm_scopes_string.split()
      missing = list(set(self.oauth_scopes).difference(vm_scopes))
      if missing:
        raise Error(
            'You are currently logged into gcloud using a service account '
            'which does not have the appropriate access to [{0}]. The account '
            'has the following scopes: [{1}].  It needs [{2}] in order to '
            'succeed.\nPlease recreate this VM instance with the missing '
            'scopes. You may also log into a standard account that has the '
            'appropriate access by using `gcloud auth login`.'
            .format(self.project, ', '.join(vm_scopes), ', '.join(missing)))
      return True
    else:
      return False
