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





"""Provides access functions for the app identity service.

To learn more about the App Identity API, review the `Overview`_ document.

.. _Overview:
   https://cloud.google.com/appengine/docs/python/appidentity/
"""









import os
import time

from google.appengine.api import apiproxy_stub_map
from google.appengine.api import memcache
from google.appengine.api.app_identity import app_identity_service_pb
from google.appengine.runtime import apiproxy_errors

__all__ = ['BackendDeadlineExceeded',
           'BlobSizeTooLarge',
           'InternalError',
           'InvalidScope',
           'NotAllowed',
           'OperationNotImplemented',
           'Error',
           'create_rpc',
           'make_sign_blob_call',
           'make_get_public_certificates_call',
           'make_get_service_account_name_call',
           'sign_blob',
           'get_public_certificates',
           'PublicCertificate',
           'get_service_account_name',
           'get_application_id',
           'get_default_version_hostname',
           'get_access_token',
           'get_access_token_uncached',
           'make_get_access_token_call',
           'get_default_gcs_bucket_name',
           'make_get_default_gcs_bucket_name_call',
          ]


_APP_IDENTITY_SERVICE_NAME = 'app_identity_service'
_SIGN_FOR_APP_METHOD_NAME = 'SignForApp'
_GET_CERTS_METHOD_NAME = 'GetPublicCertificatesForApp'
_GET_SERVICE_ACCOUNT_NAME_METHOD_NAME = 'GetServiceAccountName'
_GET_DEFAULT_GCS_BUCKET_NAME_METHOD_NAME = 'GetDefaultGcsBucketName'
_GET_ACCESS_TOKEN_METHOD_NAME = 'GetAccessToken'
_PARTITION_SEPARATOR = '~'
_DOMAIN_SEPARATOR = ':'
_MEMCACHE_KEY_PREFIX = '_ah_app_identity_'
_MEMCACHE_NAMESPACE = '_ah_'



_TOKEN_EXPIRY_SAFETY_MARGIN = 300
_MAX_TOKEN_CACHE_SIZE = 100


_MAX_RANDOM_EXPIRY_DELTA = 60




_access_token_cache = {}





_random_cache_expiry_delta = (
    hash(time.time()) % (_MAX_RANDOM_EXPIRY_DELTA * 1000) / 1000.0)


class Error(Exception):
  """Base error type."""


class BackendDeadlineExceeded(Error):
  """The communication to the backend service timed out."""


class BlobSizeTooLarge(Error):
  """The size of the blob to sign is larger than the allowed limit."""


class InternalError(Error):
  """An unspecified internal failure occurred."""


class InvalidScope(Error):
  """The scope is invalid."""


class NotAllowed(Error):
  """The operation is not allowed."""


class OperationNotImplemented(Error):
  """The operation is not implemented for the service account."""


def _to_app_identity_error(error):
  """Translates an application error to an external error, if possible.

  Args:
    error: An `ApplicationError` to translate.

  Returns:
    error: The App Identity API-specific error message.
  """
  error_map = {
      app_identity_service_pb.AppIdentityServiceError.NOT_A_VALID_APP:
      InternalError,
      app_identity_service_pb.AppIdentityServiceError.DEADLINE_EXCEEDED:
      BackendDeadlineExceeded,
      app_identity_service_pb.AppIdentityServiceError.BLOB_TOO_LARGE:
      BlobSizeTooLarge,
      app_identity_service_pb.AppIdentityServiceError.UNKNOWN_ERROR:
      InternalError,
      app_identity_service_pb.AppIdentityServiceError.UNKNOWN_SCOPE:
      InvalidScope,
      app_identity_service_pb.AppIdentityServiceError.NOT_ALLOWED:
      NotAllowed,
      app_identity_service_pb.AppIdentityServiceError.NOT_IMPLEMENTED:
      OperationNotImplemented,
      }
  if error.application_error in error_map:
    return error_map[error.application_error](error.error_detail)
  else:
    return InternalError('%s: %s' %
                         (error.application_error, error.error_detail))


class PublicCertificate(object):
  """Class that specifies information about a public certificate.

  Attributes:
    key_name: Name of the certificate.
    x509_certificate_pem: `X.509 certificates`_ in PEM format.

  .. _X.509 certificates:
     https://www.ietf.org/rfc/rfc2459.txt
  """

  def __init__(self, key_name, x509_certificate_pem):
    self.key_name = key_name
    self.x509_certificate_pem = x509_certificate_pem


def create_rpc(deadline=None, callback=None):
  """Creates an RPC object for use with the App Identity API.

  Args:
    deadline: Optional deadline in seconds for the operation; the default value
        is a system-specific deadline, typically 5 seconds.
    callback: Optional callable to invoke on completion.

  Returns:
    An `apiproxy_stub_map.UserRPC` object specialized for this service.
  """
  return apiproxy_stub_map.UserRPC(_APP_IDENTITY_SERVICE_NAME,
                                   deadline, callback)


def make_sign_blob_call(rpc, bytes_to_sign):
  """Executes the RPC call to sign a blob.

  Args:
    rpc: A UserRPC instance.
    bytes_to_sign: Blob that must be signed.

  Returns:
    A tuple that contains the signing key name and the signature.

  Raises:
    TypeError: If `bytes_to_sign` is not a string.
  """
  if not isinstance(bytes_to_sign, str):
    raise TypeError('bytes_to_sign must be str: %s'
                    % bytes_to_sign)
  request = app_identity_service_pb.SignForAppRequest()
  request.set_bytes_to_sign(bytes_to_sign)
  response = app_identity_service_pb.SignForAppResponse()

  def signing_for_app_result(rpc):
    """Checks success, handles exceptions, and returns the converted RPC result.

    This method waits for the RPC if it has not yet finished and calls the
    post-call hooks on the first invocation.

    Args:
      rpc: A UserRPC object.

    Returns:
      A tuple that contains signing key name and signature.
    """
    assert rpc.service == _APP_IDENTITY_SERVICE_NAME, repr(rpc.service)
    assert rpc.method == _SIGN_FOR_APP_METHOD_NAME, repr(rpc.method)
    try:
      rpc.check_success()
    except apiproxy_errors.ApplicationError, err:
      raise _to_app_identity_error(err)

    return (response.key_name(), response.signature_bytes())


  rpc.make_call(_SIGN_FOR_APP_METHOD_NAME, request,
                response, signing_for_app_result)


def make_get_public_certificates_call(rpc):
  """Executes the RPC call to get a list of public certificates.

  Args:
    rpc: A UserRPC instance.

  Returns:
    A list of `PublicCertificate` objects.
  """
  request = app_identity_service_pb.GetPublicCertificateForAppRequest()
  response = app_identity_service_pb.GetPublicCertificateForAppResponse()

  def get_certs_result(rpc):
    """Checks success, handles exceptions, and returns the converted RPC result.

    This method waits for the RPC if it has not yet finished and calls the
    post-call hooks on the first invocation.

    Args:
      rpc: A UserRPC object.

    Returns:
      A list of `PublicCertificate` objects.
    """
    assert rpc.service == _APP_IDENTITY_SERVICE_NAME, repr(rpc.service)
    assert rpc.method == _GET_CERTS_METHOD_NAME, repr(rpc.method)
    try:
      rpc.check_success()
    except apiproxy_errors.ApplicationError, err:
      raise _to_app_identity_error(err)
    result = []
    for cert in response.public_certificate_list_list():
      result.append(PublicCertificate(
          cert.key_name(), cert.x509_certificate_pem()))
    return result


  rpc.make_call(_GET_CERTS_METHOD_NAME, request, response, get_certs_result)


def make_get_service_account_name_call(rpc):
  """Gets the service account name of the app.

  Args:
    rpc: A UserRPC object.

  Returns:
    Service account name of the app.
  """
  request = app_identity_service_pb.GetServiceAccountNameRequest()
  response = app_identity_service_pb.GetServiceAccountNameResponse()

  def get_service_account_name_result(rpc):
    """Checks success, handles exceptions, and returns the converted RPC result.

    This method waits for the RPC if it has not yet finished and calls the
    post-call hooks on the first invocation.

    Args:
      rpc: A UserRPC object.

    Returns:
      A string of the service account name of the app.
    """
    assert rpc.service == _APP_IDENTITY_SERVICE_NAME, repr(rpc.service)
    assert rpc.method == _GET_SERVICE_ACCOUNT_NAME_METHOD_NAME, repr(rpc.method)
    try:
      rpc.check_success()
    except apiproxy_errors.ApplicationError, err:
      raise _to_app_identity_error(err)

    return response.service_account_name()


  rpc.make_call(_GET_SERVICE_ACCOUNT_NAME_METHOD_NAME, request,
                response, get_service_account_name_result)


def make_get_default_gcs_bucket_name_call(rpc):
  """Gets the default Google Cloud Storage bucket name for the app.

  Args:
    rpc: A UserRPC object.

  Returns:
    The default Google Cloud Storage bucket name for the app.
  """
  request = app_identity_service_pb.GetDefaultGcsBucketNameRequest()
  response = app_identity_service_pb.GetDefaultGcsBucketNameResponse()

  if rpc.deadline is not None:
    request.set_deadline(rpc.deadline)

  def get_default_gcs_bucket_name_result(rpc):
    """Checks success, handles exceptions, and returns the converted RPC result.

    This method waits for the RPC if it has not yet finished and calls the
    post-call hooks on the first invocation.

    Args:
      rpc: A UserRPC object.

    Returns:
      A string of the name of the app's default Google Cloud Storage bucket.
    """
    assert rpc.service == _APP_IDENTITY_SERVICE_NAME, repr(rpc.service)
    assert rpc.method == _GET_DEFAULT_GCS_BUCKET_NAME_METHOD_NAME, (
        repr(rpc.method))
    try:
      rpc.check_success()
    except apiproxy_errors.ApplicationError, err:
      raise _to_app_identity_error(err)

    return response.default_gcs_bucket_name() or None


  rpc.make_call(_GET_DEFAULT_GCS_BUCKET_NAME_METHOD_NAME, request,
                response, get_default_gcs_bucket_name_result)


def sign_blob(bytes_to_sign, deadline=None):
  """Signs a blob.

  Args:
    bytes_to_sign: The blob that must be signed.
    deadline: Optional deadline in seconds for the operation; the default value
      is a system-specific deadline, typically 5 seconds.

  Returns:
    A tuple containing the signing key name and signature.
  """
  rpc = create_rpc(deadline)
  make_sign_blob_call(rpc, bytes_to_sign)
  rpc.wait()
  return rpc.get_result()


def get_public_certificates(deadline=None):
  """Gets public certificates.

  Args:
    deadline: Optional deadline in seconds for the operation; the default value
        is a system-specific deadline, typically 5 seconds.

  Returns:
    A list of `PublicCertificate` objects.
  """
  rpc = create_rpc(deadline)
  make_get_public_certificates_call(rpc)
  rpc.wait()
  return rpc.get_result()


def get_service_account_name(deadline=None):
  """Gets the service account name of the app.

  Args:
    deadline: Optional deadline in seconds for the operation; the default value
        is a system-specific deadline, typically 5 seconds.

  Returns:
    The service account name of the app.
  """
  rpc = create_rpc(deadline)
  make_get_service_account_name_call(rpc)
  rpc.wait()
  return rpc.get_result()


def get_default_gcs_bucket_name(deadline=None):
  """Gets the default Google Cloud Storage bucket name for the app.

  Args:
    deadline: Optional deadline in seconds for the operation; the default value
        is a system-specific deadline, typically 5 seconds.

  Returns:
    Default bucket name for the app.
  """
  rpc = create_rpc(deadline)
  make_get_default_gcs_bucket_name_call(rpc)
  rpc.wait()
  return rpc.get_result()


def _ParseFullAppId(app_id):
  """Parses a full app ID into `partition`, `domain_name`, and `display_app_id`.

  Args:
    app_id: The full partitioned app ID.

  Returns:
    A tuple `(partition, domain_name, display_app_id)`.  The partition and
    domain name might be empty.
  """
  partition = ''
  psep = app_id.find(_PARTITION_SEPARATOR)
  if psep > 0:
    partition = app_id[:psep]
    app_id = app_id[psep+1:]
  domain_name = ''
  dsep = app_id.find(_DOMAIN_SEPARATOR)
  if dsep > 0:
    domain_name = app_id[:dsep]
    app_id = app_id[dsep+1:]
  return partition, domain_name, app_id


def get_application_id():
  """Gets the application ID of an app.

  Returns:
    The application ID of the app.
  """
  full_app_id = os.getenv('APPLICATION_ID')
  _, domain_name, display_app_id = _ParseFullAppId(full_app_id)
  if domain_name:
    return '%s%s%s' % (domain_name, _DOMAIN_SEPARATOR, display_app_id)
  return display_app_id


def get_default_version_hostname():
  """Gets the standard host name of the default version of the app.

  For example, if your `application_id` is `my-app`, then the result might be
  `my-app.appspot.com`.

  Returns:
    The standard host name of the default version of the application.
  """





  return os.getenv('DEFAULT_VERSION_HOSTNAME')




def make_get_access_token_call(rpc, scopes, service_account_id=None):
  """Generates the OAuth 2.0 access token to act on behalf of the application.

  This method is asynchronous and uncached. Most developers should use
  `get_access_token` instead.

  Args:
    rpc: An RPC object.
    scopes: The requested API scope string, or a list of strings.

  Raises:
    InvalidScope: If the scopes are unspecified or invalid.
  """
  request = app_identity_service_pb.GetAccessTokenRequest()
  if not scopes:
    raise InvalidScope('No scopes specified.')
  if isinstance(scopes, basestring):
    request.add_scope(scopes)
  else:
    for scope in scopes:
      request.add_scope(scope)
  if service_account_id:
    if isinstance(service_account_id, (int, long)):
      request.set_service_account_id(service_account_id)
    elif isinstance(service_account_id, basestring):
      request.set_service_account_name(service_account_id)
    else:
      raise TypeError()

  response = app_identity_service_pb.GetAccessTokenResponse()

  def get_access_token_result(rpc):
    """Checks success, handles exceptions, and returns the converted RPC result.

    This method waits for the RPC if it has not yet finished, and calls the
    post-call hooks on the first invocation.

    Args:
      rpc: A UserRPC object.

    Returns:
      A `Pair`, `Access` token string and the expiration time in seconds since
      the epoch.
    """
    assert rpc.service == _APP_IDENTITY_SERVICE_NAME, repr(rpc.service)
    assert rpc.method == _GET_ACCESS_TOKEN_METHOD_NAME, repr(rpc.method)
    try:
      rpc.check_success()
    except apiproxy_errors.ApplicationError, err:
      raise _to_app_identity_error(err)

    return response.access_token(), response.expiration_time()


  rpc.make_call(_GET_ACCESS_TOKEN_METHOD_NAME, request,
                response, get_access_token_result)




def get_access_token_uncached(scopes, deadline=None, service_account_id=None):
  """Generates the OAuth 2.0 access token to act on behalf of the application.

  This method is asynchronous and uncached. Most developers should use
  `get_access_token` instead.

  Args:
    scopes: The requested API scope string, or a list of strings.
    deadline: Optional deadline in seconds for the operation; the default value
      is a system-specific deadline, typically 5 seconds.

  Returns:
    A `Pair`, `Access` token string and the expiration time in seconds since
    the epoch.
  """
  rpc = create_rpc(deadline)
  make_get_access_token_call(rpc, scopes, service_account_id=service_account_id)
  rpc.wait()
  return rpc.get_result()




def get_access_token(scopes, service_account_id=None):
  """The OAuth 2.0 access token to act on behalf of the application.

  This token will be cached.

  A token will be generated and cached for the service account for the
  App Engine application.

  Each application has an associated Google account. This function returns an
  OAuth 2.0 access token that corresponds to the running app. Access tokens are
  safe to cache and reuse until their expiry time as returned. This method
  caches access tokens using both an in-process cache and memcache.

  Args:
    scopes: The requested API scope string, or a list of strings.

  Returns:
    A `Pair`, `Access` token string and the expiration time in seconds since
    the epoch.
  """



  cache_key = _MEMCACHE_KEY_PREFIX + str(scopes)
  if service_account_id:
    cache_key += ',%s' % service_account_id


  cached = _access_token_cache.get(cache_key)
  if cached is not None:
    access_token, expires_at = cached
    safe_expiry = (expires_at - _TOKEN_EXPIRY_SAFETY_MARGIN -
                   _random_cache_expiry_delta)
    if time.time() < safe_expiry:
      return access_token, expires_at


  memcache_value = memcache.get(cache_key, namespace=_MEMCACHE_NAMESPACE)
  if memcache_value:
    access_token, expires_at = memcache_value
  else:
    access_token, expires_at = get_access_token_uncached(
        scopes, service_account_id=service_account_id)




    memcache_expiry = expires_at - _TOKEN_EXPIRY_SAFETY_MARGIN
    memcache_expiry -= _MAX_RANDOM_EXPIRY_DELTA
    memcache_expiry -= 10
    memcache.add(cache_key, (access_token, expires_at),
                 memcache_expiry,
                 namespace=_MEMCACHE_NAMESPACE)


  if len(_access_token_cache) >= _MAX_TOKEN_CACHE_SIZE:


    _access_token_cache.clear()
  _access_token_cache[cache_key] = (access_token, expires_at)

  return access_token, expires_at
