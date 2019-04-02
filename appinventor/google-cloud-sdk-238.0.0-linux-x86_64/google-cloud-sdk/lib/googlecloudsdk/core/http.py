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

"""A module to get an unauthenticated http object."""
# TODO(b/71388306): Unskip pytype on this file.
# pytype: skip-file

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import platform
import re
import time
import uuid
import enum

from googlecloudsdk.core import config
from googlecloudsdk.core import http_proxy
from googlecloudsdk.core import log
from googlecloudsdk.core import metrics
from googlecloudsdk.core import properties
from googlecloudsdk.core.console import console_attr
from googlecloudsdk.core.console import console_io
from googlecloudsdk.core.util import encoding
from googlecloudsdk.core.util import platforms

import httplib2
import six
from six.moves import urllib
from six.moves import zip  # pylint: disable=redefined-builtin


# Alternative spellings of User-Agent header key that may appear in requests.
_NORMALIZED_USER_AGENT = b'user-agent'
_USER_AGENT_HEADER_KEYS = [_NORMALIZED_USER_AGENT, b'User-Agent', b'USER-AGENT']


def Http(timeout='unset', response_encoding=None, ca_certs=None):
  """Get an httplib2.Http client that is properly configured for use by gcloud.

  This method does not add credentials to the client.  For an Http client that
  has been authenticated, use core.credentials.http.Http().

  Args:
    timeout: double, The timeout in seconds to pass to httplib2.  This is the
        socket level timeout.  If timeout is None, timeout is infinite.  If
        default argument 'unset' is given, a sensible default is selected.
    response_encoding: str, the encoding to use to decode the response.
    ca_certs: str, absolute filename of a ca_certs file that overrides the
        default. The gcloud config property for ca_certs, in turn, overrides
        this argument.

  Returns:
    An httplib2.Http client object configured with all the required settings
    for gcloud.
  """
  # Wrap the request method to put in our own user-agent, and trace reporting.
  gcloud_ua = MakeUserAgentString(properties.VALUES.metrics.command_name.Get())
  http_client = _CreateRawHttpClient(timeout, ca_certs)
  http_client = _Wrap(
      http_client,
      properties.VALUES.core.trace_token.Get(),
      properties.VALUES.core.trace_email.Get(),
      properties.VALUES.core.trace_log.GetBool(),
      properties.VALUES.core.request_reason.Get(),
      gcloud_ua,
      properties.VALUES.core.log_http.GetBool(),
      properties.VALUES.core.log_http_redact_token.GetBool(),
      response_encoding
  )

  return http_client


# TODO(b/120951866) Can this be the same as Http?
def HttpClient(
    timeout=None,
    proxy_info=httplib2.proxy_info_from_environment,
    ca_certs=httplib2.CA_CERTS,
    disable_ssl_certificate_validation=False):
  """Returns a httplib2.Http subclass.

  Args:
    timeout: float, Request timeout, in seconds.
    proxy_info: httplib2.ProxyInfo object or callable
    ca_certs: str, absolute filename of a ca_certs file
    disable_ssl_certificate_validation: bool, If true, disable ssl certificate
        validation.

  Returns: A httplib2.Http subclass

  """
  if properties.VALUES.proxy.use_urllib3_via_shim.GetBool():
    import httplib2shim  # pylint:disable=g-import-not-at-top
    http_class = httplib2shim.Http
  else:
    http_class = httplib2.Http

  return http_class(
      timeout=timeout,
      proxy_info=proxy_info,
      ca_certs=ca_certs,
      disable_ssl_certificate_validation=disable_ssl_certificate_validation)


def _CreateRawHttpClient(timeout='unset', ca_certs=None):
  """Create an HTTP client matching the appropriate gcloud properties."""
  # Compared with setting the default timeout in the function signature (i.e.
  # timeout=300), this lets you test with short default timeouts by mocking
  # GetDefaultTimeout.
  effective_timeout = timeout if timeout != 'unset' else GetDefaultTimeout()
  no_validate = properties.VALUES.auth.disable_ssl_validation.GetBool() or False
  ca_certs_property = properties.VALUES.core.custom_ca_certs_file.Get()
  # Believe an explicitly-set ca_certs property over anything we added.
  if ca_certs_property:
    ca_certs = ca_certs_property
  if no_validate:
    ca_certs = None
  return HttpClient(timeout=effective_timeout,
                    proxy_info=http_proxy.GetHttpProxyInfo(),
                    ca_certs=ca_certs,
                    disable_ssl_certificate_validation=no_validate)


def MakeUserAgentString(cmd_path=None):
  """Return a user-agent string for this request.

  Contains 'gcloud' in addition to several other product IDs used for tracing in
  metrics reporting.

  Args:
    cmd_path: str representing the current command for tracing.

  Returns:
    str, User Agent string.
  """
  return ('gcloud/{version}'
          ' command/{cmd}'
          ' invocation-id/{inv_id}'
          ' environment/{environment}'
          ' environment-version/{env_version}'
          ' interactive/{is_interactive}'
          ' from-script/{from_script}'
          ' python/{py_version}'
          ' term/{term}'
          ' {ua_fragment}').format(
              version=config.CLOUD_SDK_VERSION.replace(' ', '_'),
              cmd=(cmd_path or properties.VALUES.metrics.command_name.Get()),
              inv_id=uuid.uuid4().hex,
              environment=properties.GetMetricsEnvironment(),
              env_version=properties.VALUES.metrics.environment_version.Get(),
              is_interactive=console_io.IsInteractive(error=True,
                                                      heuristic=True),
              py_version=platform.python_version(),
              ua_fragment=platforms.Platform.Current().UserAgentFragment(),
              from_script=console_io.IsRunFromShellScript(),
              term=console_attr.GetConsoleAttr().GetTermIdentifier())


def GetDefaultTimeout():
  return properties.VALUES.core.http_timeout.GetInt() or 300


def _Wrap(
    http_client, trace_token, trace_email, trace_log, request_reason, gcloud_ua,
    log_http, log_http_redact_token, response_encoding):
  """Wrap request with user-agent, and trace reporting.

  Args:
    http_client: The original http object.
    trace_token: str, Token to be used to route service request traces.
    trace_email: str, username to which service request traces should be sent.
    trace_log: bool, Enable/disable server side logging of service requests.
    request_reason: str, Justification for access.
    gcloud_ua: str, User agent string to be included in the request.
    log_http: bool, True to enable request/response logging.
    log_http_redact_token: bool, True to avoid logging access tokens if log_http
                           is set.
    response_encoding: str, the encoding to use to decode the response.

  Returns:
    http, The same http object but with the request method wrapped.
  """
  handlers = []

  handlers.append(Modifiers.Handler(
      Modifiers.RecordStartTime(),
      Modifiers.ReportDuration()))

  handlers.append(Modifiers.Handler(
      Modifiers.AppendToHeader(_NORMALIZED_USER_AGENT, gcloud_ua)))

  trace_value = None
  if trace_token:
    trace_value = 'token:{0}'.format(trace_token)
  elif trace_email:
    trace_value = 'email:{0}'.format(trace_email)
  elif trace_log:
    trace_value = 'log'

  if trace_value:
    handlers.append(Modifiers.Handler(
        Modifiers.AddQueryParam('trace', trace_value)))

  if request_reason:
    handlers.append(Modifiers.Handler(
        Modifiers.SetHeader('X-Goog-Request-Reason', request_reason)))

  # Do this one last so that it sees the affects of the other modifiers.
  if log_http:
    handlers.append(Modifiers.Handler(
        Modifiers.LogRequest(log_http_redact_token),
        Modifiers.LogResponse()))

  return Modifiers.WrapRequest(http_client, handlers,
                               response_encoding=response_encoding)


class Modifiers(object):
  """Encapsulates a bunch of http request wrapping functionality.

  The general process is that you can define a series of handlers that get
  executed before and after the original http request you are mapping. All the
  request handlers are executed in the order provided. Request handlers must
  return a result that is used when invoking the corresponding response handler.
  Request handlers don't actually execute the request but rather just modify the
  request arguments. After all request handlers are executed, the original http
  request is executed. Finally, all response handlers are executed in order,
  getting passed both the http response as well as the result from their
  corresponding request handler.
  """

  class Handler(object):
    """A holder object for a pair of request and response handlers.

    Request handlers are invoked before the original http request, response
    handlers are invoked after.
    """

    def __init__(self, request, response=None):
      """Creates a new Handler.

      Args:
        request: f(args, kwargs) -> Result, A function that gets called before
          the original http request gets called. It has the same arguments as
          http.request(). It returns a Modifiers.Result object that contains
          data to be passed to later stages of execution.
        response: f(response, Modifiers.Result.data), A function that gets
          called after the original http request. It is passed the http response
          as well as whatever the request handler put in its Result object.
      """
      self.request = request
      self.response = response

  class Result(object):
    """A holder object for data a request modifier needs to return.

    Data from the Result object is later passed into the response handler after
    the original http request is executed.
    """

    def __init__(self, args=None, data=None):
      """Creates a new Result.

      Args:
        args: A modified version of the http request args passed into the
          request modifier (if they need to be changed). This is required
          because the args are a tuple and cannot be modified in place like the
          kwargs can.
        data: Anything the request modifier wants to save for later use in a
          response handler.
      """
      self.args = args
      self.data = data

  @classmethod
  def WrapRequest(cls, http_client, handlers, exc_handler=None,
                  exc_type=Exception, response_encoding=None):
    """Wraps an http client with request modifiers.

    Args:
      http_client: The original http client to be wrapped.
      handlers: [Modifiers.Handler], The handlers to execute before and after
        the original request.
      exc_handler: f(e), A function that takes an exception and handles it. It
        should also throw an exception if you don't want it to be swallowed.
      exc_type: The type of exception that should be caught and given to the
        handler.
      response_encoding: str, the encoding to use to decode the response.

    Returns:
      The wrapped http client.
    """
    orig_request = http_client.request

    def WrappedRequest(*args, **kwargs):
      """Replacement http.request() method."""
      modified_args = list(args)

      if not six.PY2:
        # httplib2 needs text under Python 3.
        RequestParam.URI.Set(
            modified_args, kwargs,
            encoding.Decode(RequestParam.URI.Get(modified_args, kwargs)))

      # We need to make a copy here because if we don't we will be modifying the
      # dictionary that people pass in.
      # TODO(b/37281703): Copy the entire dictionary. This is blocked on making
      # sure anything that comes through is actually copyable.
      headers = RequestParam.HEADERS.Get(modified_args, kwargs) or {}
      # httplib2 adds headers and body together to POST messages. If headers are
      # text, it prevents POSTing bytes because you will get a decode error when
      # it combines them. Normalize everything to bytes here so we call request
      # consistently.
      headers = Modifiers._EncodeHeaders(headers)
      RequestParam.HEADERS.Set(modified_args, kwargs, headers)

      modifier_data = []

      for handler in handlers:
        modifier_result = handler.request(modified_args, kwargs)
        if modifier_result.args:
          modified_args = modifier_result.args
        modifier_data.append(modifier_result.data)

      try:
        response = orig_request(*modified_args, **kwargs)
      except exc_type as e:  # pylint: disable=broad-except
        response = None
        if exc_handler:
          exc_handler(e)
        else:
          raise

      if response_encoding is not None:
        response = Modifiers._DecodeResponse(response, response_encoding)

      for handler, data in zip(handlers, modifier_data):
        if handler.response:
          handler.response(response, data)

      return response

    http_client.request = WrappedRequest

    # apitools needs this attribute to do credential refreshes during batch API
    # requests.
    if hasattr(orig_request, 'credentials'):
      setattr(http_client.request, 'credentials', orig_request.credentials)

    return http_client

  @classmethod
  def _EncodeHeaders(cls, headers):
    return dict(
        Modifiers._EncodeHeader(h, v) for h, v in six.iteritems(headers))

  @classmethod
  def _EncodeHeader(cls, header, value):
    if isinstance(header, six.text_type):
      header = header.encode('utf8')
    if isinstance(value, six.text_type):
      value = value.encode('utf8')
    return header, value

  @classmethod
  def _DecodeResponse(cls, response, response_encoding):
    """Decodes the response content if an encoding is given."""
    response, content = response
    content = content.decode(response_encoding)
    return response, content

  @classmethod
  def _GetUserAgentHeaderValue(cls, headers):
    """Retrieve the correct user-agent header key from the requests."""
    for hdr, value in six.iteritems(headers):
      if hdr in _USER_AGENT_HEADER_KEYS:
        return hdr, value
    return None, None

  @classmethod
  def AppendToHeader(cls, header, value):
    """Appends the given value to the existing value in the http request.

    Args:
      header: str, The name of the header to append to.
      value: str, The value to append to the existing header value.

    Returns:
      A function that can be used in a Handler.request.
    """
    header, value = Modifiers._EncodeHeader(header, value)
    def _AppendToHeader(args, kwargs):
      """Replacement http.request() method."""
      modified_args = list(args)
      headers = RequestParam.HEADERS.Get(args, kwargs) or {}
      if header == _NORMALIZED_USER_AGENT:
        found_header, current_value = cls._GetUserAgentHeaderValue(headers)
        if found_header:
          del headers[found_header]
      else:
        current_value = headers.get(header, b'')

      headers[header] = (
          (current_value + b' ' + value).strip() if current_value else value)
      RequestParam.HEADERS.Set(modified_args, kwargs, headers)
      return Modifiers.Result(args=modified_args)
    return _AppendToHeader

  @classmethod
  def SetHeader(cls, header, value):
    """Sets the given header value in the http request.

    Args:
      header: str, The name of the header to set to.
      value: str, The new value of the header.

    Returns:
      A function that can be used in a Handler.request.
    """
    header, value = Modifiers._EncodeHeader(header, value)
    def _SetHeader(args, kwargs):
      """Replacement http.request() method."""
      modified_args = list(args)
      headers = RequestParam.HEADERS.Get(args, kwargs) or {}
      headers[header] = value
      RequestParam.HEADERS.Set(modified_args, kwargs, headers)
      return Modifiers.Result(args=modified_args)
    return _SetHeader

  @classmethod
  def AddQueryParam(cls, param, value):
    """Adds the given query parameter to the http request.

    Args:
      param: str, The name of the parameter.
      value: str, The value of the parameter.

    Returns:
      A function that can be used in a Handler.request.
    """
    def _AddQueryParam(args, kwargs):
      """Replacement http.request() method."""
      url_parts = urllib.parse.urlsplit(
          RequestParam.URI.Get(args, kwargs))
      query_params = urllib.parse.parse_qs(url_parts.query)
      query_params[param] = value
      # Need to do this to convert a SplitResult into a list so it can be
      # modified.
      url_parts = list(url_parts)
      url_parts[3] = urllib.parse.urlencode(query_params, doseq=True)

      modified_args = list(args)
      # pylint:disable=too-many-function-args, This is just bogus.
      new_url = urllib.parse.urlunsplit(url_parts)
      RequestParam.URI.Set(modified_args, kwargs, new_url)
      return Modifiers.Result(args=modified_args)
    return _AddQueryParam

  @classmethod
  def LogRequest(cls, redact_token=True):
    """Logs the contents of the http request.

    Args:
      redact_token: bool, True to redact auth tokens.

    Returns:
      A function that can be used in a Handler.request.
    """
    def _LogRequest(args, kwargs):
      """Replacement http.request() method."""

      uri, method, body, headers = Modifiers._GetRequest(args, kwargs)

      # If set, these prevent the printing of the http body and replace it with
      # the reason the body is not being printed.
      redact_req_body_reason = None
      redact_resp_body_reason = None

      if redact_token and IsTokenUri(uri):
        redact_req_body_reason = (
            'Contains oauth token. Set log_http_redact_token property to false '
            'to print the body of this request.'
        )
        redact_resp_body_reason = (
            'Contains oauth token. Set log_http_redact_token property to false '
            'to print the body of this response.'
        )

      log.status.Print('=======================')
      log.status.Print('==== request start ====')
      log.status.Print('uri: {uri}'.format(uri=uri))
      log.status.Print('method: {method}'.format(method=method))
      log.status.Print('== headers start ==')
      for h, v in sorted(six.iteritems(headers)):
        if redact_token and (h == b'Authorization' or
                             h == b'x-goog-iam-authorization-token'):
          v = '--- Token Redacted ---'
        log.status.Print('{0}: {1}'.format(h, v))
      log.status.Print('== headers end ==')
      log.status.Print('== body start ==')
      if redact_req_body_reason is None:
        log.status.Print(body)
      else:
        log.status.Print('Body redacted: {}'.format(redact_req_body_reason))
      log.status.Print('== body end ==')
      log.status.Print('==== request end ====')

      return Modifiers.Result(data={
          'start_time': time.time(),
          'redact_resp_body_reason': redact_resp_body_reason,
      })
    return _LogRequest

  @classmethod
  def DumpRequest(cls, capturer):
    """Dumps the contents of the http request to capturer.

    Args:
      capturer: SessionCapturer, instance to pass request to

    Returns:
      A function that can be used in a Handler.request.
    """

    def _DumpRequest(args, kwargs):
      """Replacement http.request() method."""

      capturer.CaptureHttpRequest(*Modifiers._GetRequest(args, kwargs))

      return Modifiers.Result()

    return _DumpRequest

  @classmethod
  def LogResponse(cls):
    """Logs the contents of the http response.

    Returns:
      A function that can be used in a Handler.response.
    """
    def _LogResponse(response, data):
      """Response handler."""
      redact_resp_body_reason = data['redact_resp_body_reason']

      time_taken = time.time() - data['start_time']
      headers, content = response
      log.status.Print('---- response start ----')
      log.status.Print('-- headers start --')
      for h, v in sorted(six.iteritems(headers)):
        log.status.Print('{0}: {1}'.format(h, v))
      log.status.Print('-- headers end --')
      log.status.Print('-- body start --')
      if redact_resp_body_reason is None:
        log.status.Print(content)
      else:
        log.status.Print('Body redacted: {}'.format(redact_resp_body_reason))
      log.status.Print('-- body end --')
      log.status.Print('total round trip time (request+response): {0:.3f} secs'
                       .format(time_taken))
      log.status.Print('---- response end ----')
      log.status.Print('----------------------')
    return _LogResponse

  @classmethod
  def DumpResponse(cls, capturer):
    """Dumps the contents of the http response to capturer.

    Args:
      capturer: SessionCapturer, instance to pass response to

    Returns:
      A function that can be used in a Handler.request.
    """

    def _DumpResponse(response, unused_args):
      """Response handler."""
      capturer.CaptureHttpResponse(response[0], response[1])

    return _DumpResponse

  @classmethod
  def RecordStartTime(cls):
    """Records the time at which the request was started.

    Returns:
      A function that can be used in a Handler.request.
    """
    def _RecordStartTime(unused_args, unused_kwargs):
      """Replacement http.request() method."""
      return Modifiers.Result(data=time.time())
    return _RecordStartTime

  @classmethod
  def ReportDuration(cls):
    """Reports the duration of response to the metrics module.

    Returns:
      A function that can be used in a Handler.response.
    """
    def _ReportDuration(unused_response, start_time):
      """Response handler."""
      duration = time.time() - start_time
      metrics.RPCDuration(duration)

    return _ReportDuration

  @classmethod
  def _GetRequest(cls, args, kwargs):
    """Parse args and kwargs to get uri, method, body, headers."""
    # http.request has the following signature:
    # request(self, uri, method="GET", body=None, headers=None,
    #         redirections=DEFAULT_MAX_REDIRECTS, connection_type=None)

    uri = RequestParam.URI.Get(args, kwargs)
    method = RequestParam.METHOD.Get(args, kwargs) or 'GET'
    body = RequestParam.BODY.Get(args, kwargs) or ''
    headers = RequestParam.HEADERS.Get(args, kwargs) or {}
    return uri, method, body, headers


class RequestParam(enum.Enum):
  """Encapsulates the parameters to a request() call and how to extract them.
  """
  URI = ('uri', 0)
  METHOD = ('method', 1)
  BODY = ('body', 2)
  HEADERS = ('headers', 3)

  def __init__(self, arg_name, index):
    self.arg_name = arg_name
    self.index = index

  def Get(self, args, kwargs):
    if len(args) > self.index:
      return args[self.index]
    if self.arg_name in kwargs:
      return kwargs[self.arg_name]
    return None

  def Set(self, args, kwargs, value):
    if len(args) > self.index:
      args[self.index] = value
    else:
      kwargs[self.arg_name] = value


def IsTokenUri(uri):
  """Determine if the given URI is for requesting an access token."""
  if uri in ['https://accounts.google.com/o/oauth2/token',
             'https://www.googleapis.com/oauth2/v3/token',
             'https://www.googleapis.com/oauth2/v4/token',
             'https://oauth2.googleapis.com/token',
             'https://oauth2.googleapis.com/oauth2/v4/token']:
    return True

  metadata_regexp = ('metadata.google.internal/computeMetadata/.*?/instance/'
                     'service-accounts/.*?/token')

  return re.search(metadata_regexp, uri) is not None
