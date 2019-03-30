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



"""Errors used in the urlfetch API."""












class Error(Exception):
  """Base URL fetcher error type."""


class DownloadError(Error):
  """The URL could not be retrieved.

  This exception is only raised when we cannot contact the server. HTTP errors
  (such as 404) are returned in the `status_code` field in the return value of
  `fetch`, and no exception is raised.
  """


class MalformedReplyError(DownloadError):
  """The target server returned an invalid HTTP response.

  Responses are invalid if they contain no headers, malformed or incomplete
  headers, or have content missing.
  """


class TooManyRedirectsError(DownloadError):
  """`follow_redirects` was set to True, and the redirect limit was hit."""


class InternalTransientError(Error):
  """An internal transient error occurred."""


class ConnectionClosedError(DownloadError):
  """The target server prematurely closed the connection."""


class InvalidURLError(Error):
  """The URL given was empty or invalid.

  Only HTTP and HTTPS URLs are allowed. The maximum URL length is 2048
  characters. The login and password portion is not allowed. In deployed
  applications, only ports 80 and 443 for HTTP and HTTPS respectively are
  allowed.
  """


class PayloadTooLargeError(InvalidURLError):
  """The request payload exceeds the limit."""


class DNSLookupFailedError(DownloadError):
  """The DNS lookup for a URL failed."""


class DeadlineExceededError(DownloadError):
  """The URL was not fetched because the deadline was exceeded.

  This can occur with either the client-supplied `deadline`, or the system
  default if the client does not supply a `deadline` parameter.
  """


class ResponseTooLargeError(Error):
  """The response was too large and was truncated."""
  def __init__(self, response):
    self.response = response


class InvalidMethodError(Error):
  """An invalid value was provided for `method`."""


class SSLCertificateError(Error):
  """An invalid server certificate was presented."""
