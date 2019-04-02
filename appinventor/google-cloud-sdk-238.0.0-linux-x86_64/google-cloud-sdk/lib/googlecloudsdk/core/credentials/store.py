# -*- coding: utf-8 -*- #
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

"""One-line documentation for auth module.

A detailed description of auth.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import datetime
import json
import os
import textwrap

from googlecloudsdk.core import config
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import http
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.credentials import creds
from googlecloudsdk.core.credentials import devshell as c_devshell
from googlecloudsdk.core.credentials import gce as c_gce
from googlecloudsdk.core.util import files

import httplib2
from oauth2client import client
from oauth2client.contrib import gce as oauth2client_gce
from oauth2client.contrib import reauth_errors
import six


GOOGLE_OAUTH2_PROVIDER_AUTHORIZATION_URI = (
    'https://accounts.google.com/o/oauth2/auth')
GOOGLE_OAUTH2_PROVIDER_REVOKE_URI = (
    'https://accounts.google.com/o/oauth2/revoke')
GOOGLE_OAUTH2_PROVIDER_TOKEN_URI = (
    'https://accounts.google.com/o/oauth2/token')


class Error(exceptions.Error):
  """Exceptions for the credentials module."""


class AuthenticationException(Error):
  """Exceptions that tell the users to run auth login."""

  def __init__(self, message):
    super(AuthenticationException, self).__init__(textwrap.dedent("""\
        {message}
        Please run:

          $ gcloud auth login

        to obtain new credentials, or if you have already logged in with a
        different account:

          $ gcloud config set account ACCOUNT

        to select an already authenticated account to use.""".format(
            message=message)))


class NoCredentialsForAccountException(AuthenticationException):
  """Exception for when no credentials are found for an account."""

  def __init__(self, account):
    super(NoCredentialsForAccountException, self).__init__(
        'Your current active account [{account}] does not have any'
        ' valid credentials'.format(account=account))


class NoActiveAccountException(AuthenticationException):
  """Exception for when there are no valid active credentials."""

  def __init__(self):
    super(NoActiveAccountException, self).__init__(
        'You do not currently have an active account selected.')


class TokenRefreshError(AuthenticationException,
                        client.AccessTokenRefreshError):
  """An exception raised when the auth tokens fail to refresh."""

  def __init__(self, error):
    message = ('There was a problem refreshing your current auth tokens: {0}'
               .format(error))
    super(TokenRefreshError, self).__init__(message)


class ReauthenticationException(Error):
  """Exceptions that tells the user to retry his command or run auth login."""

  def __init__(self, message):
    super(ReauthenticationException, self).__init__(textwrap.dedent("""\
        {message}
        Please retry your command or run:

          $ gcloud auth login

        To obtain new credentials.""".format(message=message)))


class TokenRefreshReauthError(ReauthenticationException):
  """An exception raised when the auth tokens fail to refresh due to reauth."""

  def __init__(self, error):
    message = ('There was a problem reauthenticating while refreshing your '
               'current auth tokens: {0}').format(error)
    super(TokenRefreshReauthError, self).__init__(message)


class InvalidCredentialFileException(Error):
  """Exception for when an external credential file could not be loaded."""

  def __init__(self, f, e):
    super(InvalidCredentialFileException, self).__init__(
        'Failed to load credential file: [{f}].  {message}'
        .format(f=f, message=str(e)))


class AccountImpersonationError(Error):
  """Exception for when attempting to impersonate a service account fails."""
  pass


class CredentialFileSaveError(Error):
  """An error for when we fail to save a credential file."""
  pass


class FlowError(Error):
  """Exception for when something goes wrong with a web flow."""


class RevokeError(Error):
  """Exception for when there was a problem revoking."""


IMPERSONATION_TOKEN_PROVIDER = None


class StaticCredentialProviders(object):
  """Manages a list of credential providers."""

  def __init__(self):
    self._providers = []

  def AddProvider(self, provider):
    self._providers.append(provider)

  def RemoveProvider(self, provider):
    self._providers.remove(provider)

  def GetCredentials(self, account):
    for provider in self._providers:
      cred = provider.GetCredentials(account)
      if cred is not None:
        return cred
    return None

  def GetAccounts(self):
    accounts = set()
    for provider in self._providers:
      accounts |= provider.GetAccounts()
    return accounts


STATIC_CREDENTIAL_PROVIDERS = StaticCredentialProviders()


class DevShellCredentialProvider(object):
  """Provides account, project and credential data for devshell env."""

  def GetCredentials(self, account):
    devshell_creds = c_devshell.LoadDevshellCredentials()
    if devshell_creds and (devshell_creds.devshell_response.user_email ==
                           account):
      return devshell_creds
    return None

  def GetAccount(self):
    return c_devshell.DefaultAccount()

  def GetAccounts(self):
    devshell_creds = c_devshell.LoadDevshellCredentials()
    if devshell_creds:
      return set([devshell_creds.devshell_response.user_email])
    return set()

  def GetProject(self):
    return c_devshell.Project()

  def Register(self):
    properties.VALUES.core.account.AddCallback(self.GetAccount)
    properties.VALUES.core.project.AddCallback(self.GetProject)
    STATIC_CREDENTIAL_PROVIDERS.AddProvider(self)

  def UnRegister(self):
    properties.VALUES.core.account.RemoveCallback(self.GetAccount)
    properties.VALUES.core.project.RemoveCallback(self.GetProject)
    STATIC_CREDENTIAL_PROVIDERS.RemoveProvider(self)


class GceCredentialProvider(object):
  """Provides account, project and credential data for gce vm env."""

  def GetCredentials(self, account):
    if account in c_gce.Metadata().Accounts():
      return AcquireFromGCE(account)
    return None

  def GetAccount(self):
    if properties.VALUES.core.check_gce_metadata.GetBool():
      return c_gce.Metadata().DefaultAccount()
    return None

  def GetAccounts(self):
    return set(c_gce.Metadata().Accounts())

  def GetProject(self):
    if properties.VALUES.core.check_gce_metadata.GetBool():
      return c_gce.Metadata().Project()
    return None

  def Register(self):
    properties.VALUES.core.account.AddCallback(self.GetAccount)
    properties.VALUES.core.project.AddCallback(self.GetProject)
    STATIC_CREDENTIAL_PROVIDERS.AddProvider(self)

  def UnRegister(self):
    properties.VALUES.core.account.RemoveCallback(self.GetAccount)
    properties.VALUES.core.project.RemoveCallback(self.GetProject)
    STATIC_CREDENTIAL_PROVIDERS.RemoveProvider(self)


def AvailableAccounts():
  """Get all accounts that have credentials stored for the CloudSDK.

  This function will also ping the GCE metadata server to see if GCE credentials
  are available.

  Returns:
    [str], List of the accounts.

  """
  store = creds.GetCredentialStore()
  accounts = store.GetAccounts() | STATIC_CREDENTIAL_PROVIDERS.GetAccounts()

  return sorted(accounts)


def LoadIfEnabled(allow_account_impersonation=True):
  """Get the credentials associated with the current account.

  If credentials have been disabled via properties, this will return None.
  Otherwise it will load credentials like normal. If credential loading fails
  for any reason (including the user not being logged in), the usual exception
  is raised.

  Args:
    allow_account_impersonation: bool, True to allow use of impersonated service
      account credentials (if that is configured). If False, the active user
      credentials will always be loaded.

  Returns:
    The credentials or None. The only time None is returned is if credentials
    are disabled via properties. If no credentials are present but credentials
    are enabled via properties, it will be an error.

  Raises:
    NoActiveAccountException: If account is not provided and there is no
        active account.
    c_gce.CannotConnectToMetadataServerException: If the metadata server cannot
        be reached.
    TokenRefreshError: If the credentials fail to refresh.
    TokenRefreshReauthError: If the credentials fail to refresh due to reauth.
  """
  if properties.VALUES.auth.disable_credentials.GetBool():
    return None
  return Load(allow_account_impersonation=allow_account_impersonation)


def Load(account=None, scopes=None, prevent_refresh=False,
         allow_account_impersonation=True):
  """Get the credentials associated with the provided account.

  This loads credentials regardless of whether credentials have been disabled
  via properties. Only use this when the functionality of the caller absolutely
  requires credentials (like printing out a token) vs logically requiring
  credentials (like for an http request).

  Args:
    account: str, The account address for the credentials being fetched. If
        None, the account stored in the core.account property is used.
    scopes: tuple, Custom auth scopes to request. By default CLOUDSDK_SCOPES
        are requested.
    prevent_refresh: bool, If True, do not refresh the access token even if it
        is out of date. (For use with operations that do not require a current
        access token, such as credential revocation.)
    allow_account_impersonation: bool, True to allow use of impersonated service
      account credentials (if that is configured). If False, the active user
      credentials will always be loaded.

  Returns:
    oauth2client.client.Credentials, The specified credentials.

  Raises:
    NoActiveAccountException: If account is not provided and there is no
        active account.
    NoCredentialsForAccountException: If there are no valid credentials
        available for the provided or active account.
    c_gce.CannotConnectToMetadataServerException: If the metadata server cannot
        be reached.
    TokenRefreshError: If the credentials fail to refresh.
    TokenRefreshReauthError: If the credentials fail to refresh due to reauth.
    AccountImpersonationError: If impersonation is requested but an
      impersonation provider is not configured.
  """
  cred = _Load(account, scopes, prevent_refresh)
  if not allow_account_impersonation:
    return cred
  impersonate_service_account = (
      properties.VALUES.auth.impersonate_service_account.Get())
  if not impersonate_service_account:
    return cred
  if not IMPERSONATION_TOKEN_PROVIDER:
    raise AccountImpersonationError(
        'gcloud is configured to impersonate service account [{}] but '
        'impersonation support is not available.'
        .format(impersonate_service_account))
  log.warning(
      'This command is using service account impersonation. All API calls will '
      'be executed as [{}].'.format(impersonate_service_account))
  return IMPERSONATION_TOKEN_PROVIDER.GetElevationAccessToken(
      impersonate_service_account, scopes or config.CLOUDSDK_SCOPES)


def _Load(account, scopes, prevent_refresh):
  """Helper for Load()."""
  # If a credential file is set, just use that and ignore the active account
  # and whatever is in the credential store.
  cred_file_override = properties.VALUES.auth.credential_file_override.Get()
  if cred_file_override:
    log.info('Using alternate credentials from file: [%s]',
             cred_file_override)
    try:
      cred = client.GoogleCredentials.from_stream(cred_file_override)
    except client.Error as e:
      raise InvalidCredentialFileException(cred_file_override, e)

    if cred.create_scoped_required():
      if scopes is None:
        scopes = config.CLOUDSDK_SCOPES
      cred = cred.create_scoped(scopes)

    # Set token_uri after scopes since token_uri needs to be explicitly
    # preserved when scopes are applied.
    token_uri_override = properties.VALUES.auth.token_host.Get()
    if token_uri_override:
      cred_type = creds.CredentialType.FromCredentials(cred)
      if cred_type in (creds.CredentialType.SERVICE_ACCOUNT,
                       creds.CredentialType.P12_SERVICE_ACCOUNT):
        cred.token_uri = token_uri_override
    # The credential override is not stored in credential store, but we still
    # want to cache access tokens between invocations.
    return creds.MaybeAttachAccessTokenCacheStore(cred)

  if not account:
    account = properties.VALUES.core.account.Get()

  if not account:
    raise NoActiveAccountException()

  cred = STATIC_CREDENTIAL_PROVIDERS.GetCredentials(account)
  if cred is not None:
    return cred

  store = creds.GetCredentialStore()
  cred = store.Load(account)
  if not cred:
    raise NoCredentialsForAccountException(account)

  # cred.token_expiry is in UTC time.
  if (not prevent_refresh and
      (not cred.token_expiry or
       cred.token_expiry < cred.token_expiry.utcnow())):
    Refresh(cred)

  return cred


def Refresh(credentials, http_client=None):
  """Refresh credentials.

  Calls credentials.refresh(), unless they're SignedJwtAssertionCredentials.

  Args:
    credentials: oauth2client.client.Credentials, The credentials to refresh.
    http_client: httplib2.Http, The http transport to refresh with.

  Raises:
    TokenRefreshError: If the credentials fail to refresh.
    TokenRefreshReauthError: If the credentials fail to refresh due to reauth.
  """
  response_encoding = None if six.PY2 else 'utf-8'
  try:
    credentials.refresh(http_client or
                        http.Http(response_encoding=response_encoding))
  except (client.AccessTokenRefreshError, httplib2.ServerNotFoundError) as e:
    raise TokenRefreshError(six.text_type(e))
  except reauth_errors.ReauthError as e:
    raise TokenRefreshReauthError(e.message)


def Store(credentials, account=None, scopes=None):
  """Store credentials according for an account address.

  Args:
    credentials: oauth2client.client.Credentials, The credentials to be stored.
    account: str, The account address of the account they're being stored for.
        If None, the account stored in the core.account property is used.
    scopes: tuple, Custom auth scopes to request. By default CLOUDSDK_SCOPES
        are requested.

  Raises:
    NoActiveAccountException: If account is not provided and there is no
        active account.
  """

  cred_type = creds.CredentialType.FromCredentials(credentials)
  if not cred_type.is_serializable:
    return

  if not account:
    account = properties.VALUES.core.account.Get()
  if not account:
    raise NoActiveAccountException()

  store = creds.GetCredentialStore()
  store.Store(account, credentials)
  _LegacyGenerator(account, credentials, scopes).WriteTemplate()


def ActivateCredentials(account, credentials):
  """Validates, stores and activates credentials with given account."""
  Refresh(credentials)
  Store(credentials, account)

  properties.PersistProperty(properties.VALUES.core.account, account)


def RevokeCredentials(credentials):
  credentials.revoke(http.Http())


def Revoke(account=None):
  """Revoke credentials and clean up related files.

  Args:
    account: str, The account address for the credentials to be revoked. If
        None, the currently active account is used.

  Returns:
    'True' if this call revoked the account; 'False' if the account was already
    revoked.

  Raises:
    NoActiveAccountException: If account is not provided and there is no
        active account.
    NoCredentialsForAccountException: If the provided account is not tied to any
        known credentials.
    RevokeError: If there was a more general problem revoking the account.
  """
  if not account:
    account = properties.VALUES.core.account.Get()
  if not account:
    raise NoActiveAccountException()

  if account in c_gce.Metadata().Accounts():
    raise RevokeError('Cannot revoke GCE-provided credentials.')

  credentials = Load(account, prevent_refresh=True)
  if not credentials:
    raise NoCredentialsForAccountException(account)

  if isinstance(credentials, c_devshell.DevshellCredentials):
    raise RevokeError(
        'Cannot revoke the automatically provisioned Cloud Shell credential.'
        'This comes from your browser session and will not persist outside'
        'of your connected Cloud Shell session.')

  rv = False
  try:
    if not account.endswith('.gserviceaccount.com'):
      RevokeCredentials(credentials)
      rv = True
  except client.TokenRevokeError as e:
    if e.args[0] == 'invalid_token':
      # Malformed or already revoked
      pass
    elif e.args[0] == 'invalid_request':
      # Service account token
      pass
    else:
      raise

  store = creds.GetCredentialStore()
  store.Remove(account)

  _LegacyGenerator(account, credentials).Clean()
  files.RmTree(config.Paths().LegacyCredentialsDir(account))
  return rv


def AcquireFromWebFlow(launch_browser=True,
                       auth_uri=None,
                       token_uri=None,
                       scopes=None,
                       client_id=None,
                       client_secret=None):
  """Get credentials via a web flow.

  Args:
    launch_browser: bool, Open a new web browser window for authorization.
    auth_uri: str, URI to open for authorization.
    token_uri: str, URI to use for refreshing.
    scopes: string or iterable of strings, scope(s) of the credentials being
      requested.
    client_id: str, id of the client requesting authorization
    client_secret: str, client secret of the client requesting authorization

  Returns:
    client.Credentials, Newly acquired credentials from the web flow.

  Raises:
    FlowError: If there is a problem with the web flow.
  """
  if auth_uri is None:
    auth_uri = properties.VALUES.auth.auth_host.Get(required=True)
  if token_uri is None:
    token_uri = properties.VALUES.auth.token_host.Get(required=True)
  if scopes is None:
    scopes = config.CLOUDSDK_SCOPES
  if client_id is None:
    client_id = properties.VALUES.auth.client_id.Get(required=True)
  if client_secret is None:
    client_secret = properties.VALUES.auth.client_secret.Get(required=True)

  webflow = client.OAuth2WebServerFlow(
      client_id=client_id,
      client_secret=client_secret,
      scope=scopes,
      user_agent=config.CLOUDSDK_USER_AGENT,
      auth_uri=auth_uri,
      token_uri=token_uri,
      prompt='select_account')
  return RunWebFlow(webflow, launch_browser=launch_browser)


def RunWebFlow(webflow, launch_browser=True):
  """Runs a preconfigured webflow to get an auth token.

  Args:
    webflow: client.OAuth2WebServerFlow, The configured flow to run.
    launch_browser: bool, Open a new web browser window for authorization.

  Returns:
    client.Credentials, Newly acquired credentials from the web flow.

  Raises:
    FlowError: If there is a problem with the web flow.
  """
  # pylint:disable=g-import-not-at-top, This is imported on demand for
  # performance reasons.
  from googlecloudsdk.core.credentials import flow

  try:
    cred = flow.Run(webflow, launch_browser=launch_browser, http=http.Http())
  except flow.Error as e:
    raise FlowError(e)
  return cred


def AcquireFromToken(refresh_token,
                     token_uri=GOOGLE_OAUTH2_PROVIDER_TOKEN_URI,
                     revoke_uri=GOOGLE_OAUTH2_PROVIDER_REVOKE_URI):
  """Get credentials from an already-valid refresh token.

  Args:
    refresh_token: An oauth2 refresh token.
    token_uri: str, URI to use for refreshing.
    revoke_uri: str, URI to use for revoking.

  Returns:
    client.Credentials, Credentials made from the refresh token.
  """
  cred = client.OAuth2Credentials(
      access_token=None,
      client_id=properties.VALUES.auth.client_id.Get(required=True),
      client_secret=properties.VALUES.auth.client_secret.Get(required=True),
      refresh_token=refresh_token,
      # always start expired
      token_expiry=datetime.datetime.utcnow(),
      token_uri=token_uri,
      user_agent=config.CLOUDSDK_USER_AGENT,
      revoke_uri=revoke_uri)
  return cred


def AcquireFromGCE(account=None):
  """Get credentials from a GCE metadata server.

  Args:
    account: str, The account name to use. If none, the default is used.

  Returns:
    client.Credentials, Credentials taken from the metadata server.

  Raises:
    c_gce.CannotConnectToMetadataServerException: If the metadata server cannot
      be reached.
    TokenRefreshError: If the credentials fail to refresh.
    TokenRefreshReauthError: If the credentials fail to refresh due to reauth.
  """
  credentials = oauth2client_gce.AppAssertionCredentials(email=account)
  Refresh(credentials)
  return credentials


def SaveCredentialsAsADC(credentials, file_path):
  """Saves the credentials to the given file.

  This file can be read back via
    cred = client.GoogleCredentials.from_stream(file_path)

  Args:
    credentials: client.OAuth2Credentials, obtained from a web flow
        or service account.
    file_path: str, file path to store credentials to. The file will be created.

  Raises:
    CredentialFileSaveError: on file io errors.
  """
  creds_type = creds.CredentialType.FromCredentials(credentials)
  if creds_type == creds.CredentialType.P12_SERVICE_ACCOUNT:
    raise CredentialFileSaveError(
        'Error saving Application Default Credentials: p12 keys are not'
        'supported in this format')

  if creds_type == creds.CredentialType.USER_ACCOUNT:
    credentials = client.GoogleCredentials(
        credentials.access_token,
        credentials.client_id,
        credentials.client_secret,
        credentials.refresh_token,
        credentials.token_expiry,
        credentials.token_uri,
        credentials.user_agent,
        credentials.revoke_uri)
  try:
    contents = json.dumps(credentials.serialization_data, sort_keys=True,
                          indent=2, separators=(',', ': '))  # pytype: disable=wrong-arg-types
    files.WriteFileContents(file_path, contents, private=True)
  except files.Error as e:
    log.debug(e, exc_info=True)
    raise CredentialFileSaveError(
        'Error saving Application Default Credentials: ' + str(e))


class _LegacyGenerator(object):
  """A class to generate the credential file for legacy tools."""

  def __init__(self, account, credentials, scopes=None):
    self.credentials = credentials
    self.credentials_type = creds.CredentialType.FromCredentials(credentials)
    if self.credentials_type == creds.CredentialType.UNKNOWN:
      raise creds.UnknownCredentialsType('Unknown credentials type.')
    if scopes is None:
      self.scopes = config.CLOUDSDK_SCOPES
    else:
      self.scopes = scopes

    paths = config.Paths()
    # Bq file while not generated here is created for caching
    # credentials, register so it is cleaned up.
    self._bq_path = paths.LegacyCredentialsBqPath(account)
    self._gsutil_path = paths.LegacyCredentialsGSUtilPath(account)
    self._p12_key_path = paths.LegacyCredentialsP12KeyPath(account)
    self._adc_path = paths.LegacyCredentialsAdcPath(account)

  def Clean(self):
    """Remove the credential file."""

    paths = [
        self._bq_path,
        self._gsutil_path,
        self._p12_key_path,
        self._adc_path,
    ]
    for p in paths:
      try:
        os.remove(p)
      except OSError:
        # file did not exist, so we're already done.
        pass

  def WriteTemplate(self):
    """Write the credential file."""

    # General credentials used by bq and gsutil.
    if self.credentials_type != creds.CredentialType.P12_SERVICE_ACCOUNT:
      SaveCredentialsAsADC(self.credentials, self._adc_path)

      if self.credentials_type == creds.CredentialType.USER_ACCOUNT:
        # We create a small .boto file for gsutil, to be put in BOTO_PATH.
        # Our client_id and client_secret should accompany our refresh token;
        # if a user loaded any other .boto files that specified a different
        # id and secret, those would override our id and secret, causing any
        # attempts to obtain an access token with our refresh token to fail.
        self._WriteFileContents(
            self._gsutil_path, '\n'.join([
                '[OAuth2]',
                'client_id = {cid}',
                'client_secret = {secret}',
                '',
                '[Credentials]',
                'gs_oauth2_refresh_token = {token}',
            ]).format(cid=config.CLOUDSDK_CLIENT_ID,
                      secret=config.CLOUDSDK_CLIENT_NOTSOSECRET,
                      token=self.credentials.refresh_token))
      elif self.credentials_type == creds.CredentialType.SERVICE_ACCOUNT:
        self._WriteFileContents(
            self._gsutil_path, '\n'.join([
                '[Credentials]',
                'gs_service_key_file = {key_file}',
            ]).format(key_file=self._adc_path))
      else:
        raise CredentialFileSaveError(
            'Unsupported credentials type {0}'.format(type(self.credentials)))
    else:  # P12 service account
      cred = self.credentials
      key = cred._private_key_pkcs12  # pylint: disable=protected-access
      password = cred._private_key_password  # pylint: disable=protected-access
      files.WriteBinaryFileContents(self._p12_key_path, key, private=True)

      # the .boto file gets some different fields
      self._WriteFileContents(
          self._gsutil_path, '\n'.join([
              '[Credentials]',
              'gs_service_client_id = {account}',
              'gs_service_key_file = {key_file}',
              'gs_service_key_file_password = {key_password}',
          ]).format(account=self.credentials.service_account_email,
                    key_file=self._p12_key_path,
                    key_password=password))

  def _WriteFileContents(self, filepath, contents):
    """Writes contents to a path, ensuring mkdirs.

    Args:
      filepath: str, The path of the file to write.
      contents: str, The contents to write to the file.
    """

    full_path = os.path.realpath(files.ExpandHomeDir(filepath))
    files.WriteFileContents(full_path, contents, private=True)
