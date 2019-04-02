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
"""General utility functions for devappserver2."""




import BaseHTTPServer
import os
import platform
import re
import socket
import subprocess
import urllib
import wsgiref.headers
from google.appengine.tools import sdk_update_checker


# The SDK version returned when there is no available VERSION file.
_DEFAULT_SDK_VERSION = '(Internal)'

# This environment variable is intended for making ndb to work with the
# Cloud Datastore instead of the App Engine Datastore.
_DATASTORE_PROJECT_ID_ENV = 'DATASTORE_PROJECT_ID'


def get_headers_from_environ(environ):
  """Get a wsgiref.headers.Headers object with headers from the environment.

  Headers in environ are prefixed with 'HTTP_', are all uppercase, and have
  had dashes replaced with underscores.  This strips the HTTP_ prefix and
  changes underscores back to dashes before adding them to the returned set
  of headers.

  Args:
    environ: An environ dict for the request as defined in PEP-333.

  Returns:
    A wsgiref.headers.Headers object that's been filled in with any HTTP
    headers found in environ.
  """
  headers = wsgiref.headers.Headers([])
  for header, value in environ.iteritems():
    if header.startswith('HTTP_'):
      headers[header[5:].replace('_', '-')] = value
  # Content-Type is special; it does not start with 'HTTP_'.
  if 'CONTENT_TYPE' in environ:
    headers['CONTENT-TYPE'] = environ['CONTENT_TYPE']
  return headers


def construct_url_from_environ(environ, secure, include_query_params, port):
  """Construct a URL from the environ and other parameters.

  Implementation adapted from
  https://www.python.org/dev/peps/pep-0333/#url-reconstruction.

  Args:
    environ: An environ dict as defined in PEP-333.
    secure: boolean, if True the url will be https, otherwise http
    include_query_params: boolean, if True will include query params from
      environ
    port: int, the port for the new url

  Returns:
    str, the constructed URL.
  """
  url_result = 'https' if secure else 'http'
  url_result += '://'
  if environ.get('HTTP_HOST'):
    url_result += environ['HTTP_HOST'].split(':')[0]
  else:
    url_result += environ['SERVER_NAME']
  url_result += ':' + str(port)
  url_result += urllib.quote(environ.get('SCRIPT_NAME', ''))
  url_result += urllib.quote(environ.get('PATH_INFO', ''))
  if include_query_params and environ.get('QUERY_STRING'):
    url_result += '?' + environ['QUERY_STRING']
  return url_result


def put_headers_in_environ(headers, environ):
  """Given a list of headers, put them into environ based on PEP-333.

  This converts headers to uppercase, prefixes them with 'HTTP_', and
  converts dashes to underscores before adding them to the environ dict.

  Args:
    headers: A list of (header, value) tuples.  The HTTP headers to add to the
      environment.
    environ: An environ dict for the request as defined in PEP-333.
  """
  for key, value in headers:
    environ['HTTP_%s' % key.upper().replace('-', '_')] = value


def is_env_flex(env):
  return env in ['2', 'flex', 'flexible']


class HTTPServerIPv6(BaseHTTPServer.HTTPServer):
  """An HTTPServer that supports IPv6 connections.

  The standard HTTPServer has address_family hardcoded to socket.AF_INET.
  """
  address_family = socket.AF_INET6


def get_sdk_version():
  """Parses the SDK VERSION file for the SDK version.

  Returns:
    A semver string representing the SDK version, eg 1.9.55. If no VERSION file
    is available, eg for internal SDK builds, a non-semver default string is
    provided.
  """
  version_object = sdk_update_checker.GetVersionObject()
  if version_object:
    return version_object['release']
  else:
    return _DEFAULT_SDK_VERSION


def setup_environ(app_id):
  """Sets up the os.environ dictionary for the front-end server and API server.

  This function should only be called once.

  Args:
    app_id: The id of the application.
  """
  os.environ['APPLICATION_ID'] = app_id
  # Purge _DATASTORE_PROJECT_ID_ENV from dev_appserver process. Otherwise the
  # logics for datastore rpc would be tricked to use Cloud Datastore mode.
  # If necessary, users can still pass this environment variable to local
  # runtime processes via app.yaml or the --env_var flag.
  if _DATASTORE_PROJECT_ID_ENV in os.environ:
    del os.environ[_DATASTORE_PROJECT_ID_ENV]


def is_windows():
  """Returns a boolean indicating whether dev_appserver is on windows."""
  return platform.system().lower() == 'windows'


def _platform_executable_extensions():
  if is_windows():
    return ('.exe', '.cmd', '.bat', '.com', '.ps1')
  else:
    return ('', '.sh')


def _find_java_on_system_path():
  """Find the java executable on $PATH."""
  path = os.environ.get('PATH', None)
  if not path:
    return None
  for ext in _platform_executable_extensions():
    for directory in path.split(os.pathsep):
      # Windows can have paths quoted.
      directory = directory.strip('"')
      full = os.path.normpath(os.path.join(directory, 'java') + ext)
      # On Windows os.access(full, os.X_OK) is always True.
      if os.path.isfile(full) and os.access(full, os.X_OK):
        return full
  return None


def get_java_major_version():
  """Get the major version of java.

  This method checks the output of 'java -version'.

  Returns:
    An integer version number if java version can be parsed. Otherwise None.
  """
  java_path = _find_java_on_system_path()
  try:
    output = subprocess.check_output(
        [java_path, '-version'], stderr=subprocess.STDOUT)
  except Exception:  # pylint: disable=broad-except
    # Cannot find executable java on $PATH.
    return None
  # Find java major version.
  match = re.search(r'version "1\.', output)
  if match:
    # We are in a pre http://openjdk.java.net/jeps/223 world,
    # this is the 1.6.xx, 1.7.xx, 1.8.xxx world.
    match = re.search(r'version "(\d+)\.(\d+)\.', output)
    if not match:
      # illegal version string. The java executable is unusable.
      return None
    return int(match.group(2))
  else:
    # We are in a post http://openjdk.java.net/jeps/223 world
    match = re.search(r'version "([1-9][0-9]*)', output)
    if not match:
      # illegal version string. The java executable is unusable.
      return None
    return int(match.group(1))
