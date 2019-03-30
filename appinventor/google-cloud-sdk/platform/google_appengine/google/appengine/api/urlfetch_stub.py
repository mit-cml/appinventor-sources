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




"""Stub version of the urlfetch API, based on httplib."""




_successfully_imported_fancy_urllib = False
_fancy_urllib_InvalidCertException = None
_fancy_urllib_SSLError = None
try:
  import fancy_urllib
  _successfully_imported_fancy_urllib = True
  _fancy_urllib_InvalidCertException = fancy_urllib.InvalidCertificateException
  _fancy_urllib_SSLError = fancy_urllib.SSLError
except ImportError:
  pass

import gzip
import httplib
import logging
import os
import socket
import StringIO
import sys
import urllib
import urlparse

from google.appengine.api import apiproxy_stub
from google.appengine.api import urlfetch
from google.appengine.api import urlfetch_errors
from google.appengine.api import urlfetch_service_pb
from google.appengine.api import urlfetch_stub_cert_path
from google.appengine.runtime import apiproxy_errors



MAX_REQUEST_SIZE = 10 << 20

MAX_RESPONSE_SIZE = 2 ** 25

MAX_REDIRECTS = urlfetch.MAX_REDIRECTS

REDIRECT_STATUSES = frozenset([
  httplib.MOVED_PERMANENTLY,
  httplib.FOUND,
  httplib.SEE_OTHER,
  httplib.TEMPORARY_REDIRECT,
])

PRESERVE_ON_REDIRECT = frozenset(['GET', 'HEAD'])





_API_CALL_DEADLINE = 5.0





_API_CALL_VALIDATE_CERTIFICATE_DEFAULT = False



_MAX_REQUEST_SIZE = 10485760







_UNTRUSTED_REQUEST_HEADERS = frozenset([
  'content-length',
  'host',
  'vary',
  'via',
  'x-forwarded-for',
])




_MAX_URL_LENGTH = 10240


def _CanValidateCerts():
  return (_successfully_imported_fancy_urllib and
          fancy_urllib.can_validate_certs())


def _SetupSSL(path):
  global CERT_PATH
  if os.path.exists(path):
    CERT_PATH = path
  else:
    CERT_PATH = None
    logging.warning('%s missing; without this urlfetch will not be able to '
                    'validate SSL certificates.', path)

  if not _CanValidateCerts():
    logging.warning('No ssl package found. urlfetch will not be able to '
                    'validate SSL certificates.')




_SetupSSL(os.path.normpath(urlfetch_stub_cert_path.CERT_PATH))

def _IsAllowedPort(port):

  if port is None:
    return True
  try:
    port = int(port)
  except ValueError, e:
    return False




  if (port == 0 or
      (port >= 80 and port <= 90) or
      (port >= 440 and port <= 450) or
      port >= 1024):
    return True
  return False

def _IsLocalhost(host):
  """Determines whether 'host' points to localhost."""
  return host.startswith('localhost') or host.startswith('127.0.0.1')


class URLFetchServiceStub(apiproxy_stub.APIProxyStub):
  """Stub version of the urlfetch API to be used with apiproxy_stub_map."""

  THREADSAFE = True

  def __init__(self,
               service_name='urlfetch',
               urlmatchers_to_fetch_functions=None):
    """Initializer.

    Args:
      service_name: Service name expected for all calls.
      urlmatchers_to_fetch_functions: A list of two-element tuples.
        The first element is a urlmatcher predicate function that takes
        a url and determines a match. The second is a function that
        can retrieve result for that url. If no match is found, a url is
        handled by the default _RetrieveURL function.
        When more than one match is possible, the first match is used.
    """
    super(URLFetchServiceStub, self).__init__(service_name,
                                              max_request_size=MAX_REQUEST_SIZE)
    self._urlmatchers_to_fetch_functions = urlmatchers_to_fetch_functions or []
    self.http_proxy = None

  def _Dynamic_SetHttpProxy(self, request, response):
    self.http_proxy = (request.http_proxy_host(), request.http_proxy_port())

  def _Dynamic_Fetch(self, request, response):
    """Trivial implementation of URLFetchService::Fetch().

    Args:
      request: the fetch to perform, a URLFetchRequest
      response: the fetch response, a URLFetchResponse
    """
    if len(request.url()) > _MAX_URL_LENGTH:
      logging.error('URL is too long: %s...' % request.url()[:50])
      raise apiproxy_errors.ApplicationError(
          urlfetch_service_pb.URLFetchServiceError.INVALID_URL)

    (protocol, host, path, query, fragment) = urlparse.urlsplit(request.url())

    payload = None
    if request.method() == urlfetch_service_pb.URLFetchRequest.GET:
      method = 'GET'
    elif request.method() == urlfetch_service_pb.URLFetchRequest.POST:
      method = 'POST'
      payload = request.payload()
    elif request.method() == urlfetch_service_pb.URLFetchRequest.HEAD:
      method = 'HEAD'
    elif request.method() == urlfetch_service_pb.URLFetchRequest.PUT:
      method = 'PUT'
      payload = request.payload()
    elif request.method() == urlfetch_service_pb.URLFetchRequest.DELETE:
      method = 'DELETE'
    elif request.method() == urlfetch_service_pb.URLFetchRequest.PATCH:
      method = 'PATCH'
      payload = request.payload()
    else:
      logging.error('Invalid method: %s', request.method())
      raise apiproxy_errors.ApplicationError(
        urlfetch_service_pb.URLFetchServiceError.INVALID_URL)

    if payload is not None and len(payload) > _MAX_REQUEST_SIZE:
      raise apiproxy_errors.ApplicationError(
          urlfetch_service_pb.URLFetchServiceError.PAYLOAD_TOO_LARGE)

    if not (protocol == 'http' or protocol == 'https'):
      logging.error('Invalid protocol: %s', protocol)
      raise apiproxy_errors.ApplicationError(
        urlfetch_service_pb.URLFetchServiceError.INVALID_URL)

    if not host:
      logging.error('Missing host.')
      raise apiproxy_errors.ApplicationError(
          urlfetch_service_pb.URLFetchServiceError.INVALID_URL)

    self._SanitizeHttpHeaders(_UNTRUSTED_REQUEST_HEADERS,
                              request.header_list())
    deadline = _API_CALL_DEADLINE
    if request.has_deadline():
      deadline = request.deadline()
    validate_certificate = _API_CALL_VALIDATE_CERTIFICATE_DEFAULT
    if request.has_mustvalidateservercertificate():
      validate_certificate = request.mustvalidateservercertificate()

    custom_fetch_function = self._GetCustomFetchFunction(request.url())
    if custom_fetch_function:
      custom_fetch_function(request.url(), payload, method,
                            request.header_list(), request, response,
                            follow_redirects=request.followredirects(),
                            deadline=deadline,
                            validate_certificate=validate_certificate)
    else:
      self._RetrieveURL(request.url(), payload, method,
                        request.header_list(), request, response,
                        follow_redirects=request.followredirects(),
                        deadline=deadline,
                        validate_certificate=validate_certificate,
                        http_proxy=self.http_proxy)

  def _GetCustomFetchFunction(self, url):
    """Get the custom fetch function for a url.

    Args:
      url: A url to fetch from. str.

    Returns:
      A custom fetch function for this url, or None if no matching custom
      function is found.
    """
    for urlmatcher, fetch_function in self._urlmatchers_to_fetch_functions:
      if urlmatcher(url):
        return fetch_function
    return None

  @staticmethod
  def _RetrieveURL(url, payload, method, headers, request, response,
                   follow_redirects=True, deadline=_API_CALL_DEADLINE,
                   validate_certificate=_API_CALL_VALIDATE_CERTIFICATE_DEFAULT,
                   http_proxy=None):
    """Retrieves a URL over network.

    Args:
      url: String containing the URL to access.
      payload: Request payload to send, if any; None if no payload.
        If the payload is unicode, we assume it is utf-8.
      method: HTTP method to use (e.g., 'GET')
      headers: List of additional header objects to use for the request.
      request: A urlfetch_service_pb.URLFetchRequest proto object from
          original request.
      response: A urlfetch_service_pb.URLFetchResponse proto object to
          populate with the response data.
      follow_redirects: optional setting (defaulting to True) for whether or not
        we should transparently follow redirects (up to MAX_REDIRECTS)
      deadline: Number of seconds to wait for the urlfetch to finish.
      validate_certificate: If true, do not send request to server unless the
        certificate is valid, signed by a trusted CA and the hostname matches
        the certificate.
      http_proxy: Tuple of (hostname, port), where hostname is a string and port
        is an int, to use as the http proxy.

    Raises:
      Raises an apiproxy_errors.ApplicationError exception with
      INVALID_URL_ERROR in cases where:
        - The protocol of the redirected URL is bad or missing.
        - The port is not in the allowable range of ports.
      Raises an apiproxy_errors.ApplicationError exception with
      TOO_MANY_REDIRECTS in cases when MAX_REDIRECTS is exceeded
    """
    last_protocol = ''
    last_host = ''
    if isinstance(payload, unicode):
      payload = payload.encode('utf-8')

    for redirect_number in xrange(MAX_REDIRECTS + 1):
      parsed = urlparse.urlsplit(url)
      protocol, host, path, query, fragment = parsed







      port = urllib.splitport(urllib.splituser(host)[1])[1]

      if not _IsAllowedPort(port):
        logging.error(
          'urlfetch received %s ; port %s is not allowed in production!' %
          (url, port))





        raise apiproxy_errors.ApplicationError(
          urlfetch_service_pb.URLFetchServiceError.INVALID_URL)

      if protocol and not host:

        logging.error('Missing host on redirect; target url is %s' % url)
        raise apiproxy_errors.ApplicationError(
          urlfetch_service_pb.URLFetchServiceError.INVALID_URL)




      if not host and not protocol:
        host = last_host
        protocol = last_protocol


      if port == '0':
        host = host.replace(':0', '')









      adjusted_headers = {
          'User-Agent':
          [('AppEngine-Google; (+http://code.google.com/appengine; appid: %s)'
            % os.getenv('APPLICATION_ID'))],
          'Host': [host],
          'Accept-Encoding': ['gzip'],
      }
      if payload is not None:


        adjusted_headers['Content-Length'] = [str(len(payload))]


      if method == 'POST' and payload:
        adjusted_headers['Content-Type'] = [
            'application/x-www-form-urlencoded']

      passthrough_content_encoding = False
      for header in headers:



        header_key = header.key()
        if header_key.lower() == 'user-agent':
          adjusted_headers[header_key.title()] = [(
              '%s %s' % (header.value(), adjusted_headers['User-Agent'][0]))]
        elif header_key.lower() == 'accept-encoding':
          passthrough_content_encoding = True
          adjusted_headers[header_key.title()] = [header.value()]
        elif header_key.lower() == 'content-type':
          adjusted_headers[header_key.title()] = [header.value()]
        else:
          adjusted_headers.setdefault(header_key, []).append(header.value())

      if payload is not None:
        escaped_payload = payload.encode('string_escape')
      else:
        escaped_payload = ''
      logging.debug('Making HTTP request: host = %r, '
                    'url = %r, payload = %.1000r, headers = %r',
                    host, url, escaped_payload, adjusted_headers)
      try:
        proxy_host = None

        if protocol == 'http':
          connection_class = httplib.HTTPConnection
          default_port = 80

          if http_proxy and not _IsLocalhost(host):
            proxy_host = '%s:%d' % (http_proxy[0],
                                    http_proxy[1])
          elif os.environ.get('HTTP_PROXY') and not _IsLocalhost(host):
            _, proxy_host, _, _, _ = (
                urlparse.urlsplit(os.environ.get('HTTP_PROXY')))
        elif protocol == 'https':
          if (validate_certificate and _CanValidateCerts() and
              CERT_PATH):

            connection_class = fancy_urllib.create_fancy_connection(
                ca_certs=CERT_PATH)
          else:
            connection_class = httplib.HTTPSConnection

          default_port = 443

          if os.environ.get('HTTPS_PROXY') and not _IsLocalhost(host):
            _, proxy_host, _, _, _ = (
                urlparse.urlsplit(os.environ.get('HTTPS_PROXY')))
        else:

          error_msg = 'Redirect specified invalid protocol: "%s"' % protocol
          logging.error(error_msg)
          raise apiproxy_errors.ApplicationError(
              urlfetch_service_pb.URLFetchServiceError.INVALID_URL, error_msg)






        connection_kwargs = {'timeout': deadline}




        if (not validate_certificate and sys.version_info >= (2, 7, 9)
            and protocol == 'https'):



          import ssl
          connection_kwargs['context'] = ssl._create_unverified_context()

        if proxy_host:
          proxy_address, _, proxy_port = proxy_host.partition(':')
          connection = connection_class(
              proxy_address, proxy_port if proxy_port else default_port,
              **connection_kwargs)
          full_path = urlparse.urlunsplit((protocol, host, path, query, ''))

          if protocol == 'https':
            connection.set_tunnel(host)
        else:
          connection = connection_class(host, **connection_kwargs)
          full_path = urlparse.urlunsplit(('', '', path, query, ''))



        last_protocol = protocol
        last_host = host

        try:
          _SendRequest(connection, method, full_path, payload, adjusted_headers)
          http_response = connection.getresponse()
          if method == 'HEAD':
            http_response_data = ''
          else:
            http_response_data = http_response.read()
        finally:
          connection.close()
      except _fancy_urllib_InvalidCertException, e:
        raise apiproxy_errors.ApplicationError(
          urlfetch_service_pb.URLFetchServiceError.SSL_CERTIFICATE_ERROR,
          str(e))
      except _fancy_urllib_SSLError, e:





        app_error = (
            urlfetch_service_pb.URLFetchServiceError.DEADLINE_EXCEEDED
            if 'timed out' in e.message else
            urlfetch_service_pb.URLFetchServiceError.SSL_CERTIFICATE_ERROR)
        raise apiproxy_errors.ApplicationError(app_error, str(e))
      except socket.timeout, e:
        raise apiproxy_errors.ApplicationError(
          urlfetch_service_pb.URLFetchServiceError.DEADLINE_EXCEEDED, str(e))
      except (httplib.error, socket.error, IOError), e:
        raise apiproxy_errors.ApplicationError(
          urlfetch_service_pb.URLFetchServiceError.FETCH_ERROR, str(e))

      if http_response.status >= 600:
        raise apiproxy_errors.ApplicationError(
            urlfetch_service_pb.URLFetchServiceError.FETCH_ERROR,
            'Status %s unknown' % http_response.status)




      if http_response.status in REDIRECT_STATUSES and follow_redirects:

        url = http_response.getheader('Location', None)
        if url is None:
          error_msg = 'Missing "Location" header for redirect.'
          logging.error(error_msg)
          raise apiproxy_errors.ApplicationError(
              urlfetch_service_pb.URLFetchServiceError.MALFORMED_REPLY,
              error_msg)



        if (http_response.status != httplib.TEMPORARY_REDIRECT and
            method not in PRESERVE_ON_REDIRECT):
          logging.warn('Received a %s to a %s. Redirecting with a GET',
                       http_response.status, method)
          method = 'GET'
          payload = None
      else:
        response.set_statuscode(http_response.status)
        if (http_response.getheader('content-encoding') == 'gzip' and
            not passthrough_content_encoding):
          gzip_stream = StringIO.StringIO(http_response_data)
          gzip_file = gzip.GzipFile(fileobj=gzip_stream)
          http_response_data = gzip_file.read()
        response.set_content(http_response_data[:MAX_RESPONSE_SIZE])


        for header_key in http_response.msg.keys():
          for header_value in http_response.msg.getheaders(header_key):
            if (header_key.lower() == 'content-encoding' and
                header_value == 'gzip' and
                not passthrough_content_encoding):
              continue
            if header_key.lower() == 'content-length' and method != 'HEAD':
              header_value = str(len(response.content()))
            header_proto = response.add_header()
            header_proto.set_key(header_key)
            header_proto.set_value(header_value)

        if len(http_response_data) > MAX_RESPONSE_SIZE:
          response.set_contentwastruncated(True)



        if request.url() != url:
          response.set_finalurl(url)


        break
    else:
      error_msg = 'Too many repeated redirects'
      logging.error(error_msg)
      raise apiproxy_errors.ApplicationError(
          urlfetch_service_pb.URLFetchServiceError.TOO_MANY_REDIRECTS,
          error_msg)

  def _SanitizeHttpHeaders(self, untrusted_headers, headers):
    """Cleans "unsafe" headers from the HTTP request, in place.

    Args:
      untrusted_headers: Set of untrusted headers names (all lowercase).
      headers: List of Header objects. The list is modified in place.
    """
    prohibited_headers = [h.key() for h in headers
                          if h.key().lower() in untrusted_headers]
    if prohibited_headers:
      logging.warn('Stripped prohibited headers from URLFetch request: %s',
                   prohibited_headers)

      for index in reversed(xrange(len(headers))):
        if headers[index].key().lower() in untrusted_headers:
          del headers[index]


def _SendRequest(connection, method, full_path, payload, headers):
  """Sends an HTTP request on a connection to the URL described by full_path.

  Compared to httplib.HTTPConnection's request method, this preserves all values
  for repeated headers.

  Args:
    connection: An instance or subclass of httplib.HTTPConnection.
    method: The string HTTP method name, eg 'GET'.
    full_path: The string full URL path for the request.
    payload: The string request payload to send.
    headers: A dict of headers to send with the request. The dict maps string
      header names to lists of associated header values.
  """
  connection.connect()
  header_names = [name.lower() for name in headers]
  connection.putrequest(
      method, full_path, skip_host='host' in header_names,
      skip_accept_encoding='accept-encoding' in header_names)
  for header, values in headers.iteritems():
    for value in values:
      connection.putheader(header, value)
  if payload is not None and 'content-length' not in header_names:
    connection._set_content_length(payload)
  connection.endheaders(payload)
