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

"""Run a web flow for oauth2.

"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.core import log
from googlecloudsdk.core.util import pkg_resources
from googlecloudsdk.core.util import platforms

from oauth2client import client
from oauth2client import tools
from six.moves import input  # pylint: disable=redefined-builtin
from six.moves.http_client import ResponseNotReady


try:
  # pylint:disable=g-import-not-at-top
  from urlparse import parse_qsl
except ImportError:
  # pylint:disable=g-import-not-at-top
  from cgi import parse_qsl


class Error(Exception):
  """Exceptions for the flow module."""


class AuthRequestRejectedException(Error):
  """Exception for when the authentication request was rejected."""


class AuthRequestFailedException(Error):
  """Exception for when the authentication request was rejected."""


class ClientRedirectHandler(tools.ClientRedirectHandler):
  """A handler for OAuth 2.0 redirects back to localhost.

  Waits for a single request and parses the query parameters
  into the servers query_params and then stops serving.
  """

  # pylint:disable=invalid-name, This method is overriden from the base class.
  def do_GET(self):
    """Handle a GET request.

    Parses the query parameters and prints a message
    if the flow has completed. Note that we can't detect
    if an error occurred.
    """
    self.send_response(200)
    self.send_header('Content-type', 'text/html')  # pytype: disable=wrong-arg-types
    self.end_headers()
    query = self.path.split('?', 1)[-1]
    query = dict(parse_qsl(query))
    self.server.query_params = query

    if 'code' in query:
      page = 'oauth2_landing.html'
    else:
      page = 'oauth2_landing_error.html'

    self.wfile.write(pkg_resources.GetResource(__name__, page))


def Run(flow, launch_browser=True, http=None,
        auth_host_name='localhost', auth_host_port_start=8085):
  """Run a web flow to get oauth2 credentials.

  Args:
    flow: oauth2client.OAuth2WebServerFlow, A flow that is ready to run.
    launch_browser: bool, If False, give the user a URL to copy into
        a browser. Requires that they paste the refresh token back into the
        terminal. If True, opens a web browser in a new window.
    http: httplib2.Http, The http transport to use for authentication.
    auth_host_name: str, Host name for the redirect server.
    auth_host_port_start: int, First port to try for serving the redirect. If
        this port is taken, it will keep trying incrementing ports until 100
        have been tried, then fail.

  Returns:
    oauth2client.Credential, A ready-to-go credential that has already been
    put in the storage.

  Raises:
    AuthRequestRejectedException: If the request was rejected.
    AuthRequestFailedException: If the request fails.
  """

  if launch_browser:
    # pylint:disable=g-import-not-at-top, Import when needed for performance.
    import socket
    import webbrowser

    success = False
    port_number = auth_host_port_start

    while True:
      try:
        httpd = tools.ClientRedirectServer((auth_host_name, port_number),
                                           ClientRedirectHandler)
      except socket.error as e:
        if port_number > auth_host_port_start + 100:
          success = False
          break
        port_number += 1
      else:
        success = True
        break

    if success:
      flow.redirect_uri = ('http://%s:%s/' % (auth_host_name, port_number))

      authorize_url = flow.step1_get_authorize_url()
      # Without this, Chrome on MacOS will not launch unless Chrome
      # is already open. This is due to an bug in webbbrowser.py that tries to
      # open web browsers by app name using i.e. 'Chrome' but the actual app
      # name is 'Google Chrome' on Mac.
      if platforms.OperatingSystem.MACOSX == platforms.OperatingSystem.Current(
      ):
        try:
          webbrowser.register('Google Chrome', None,
                              webbrowser.MacOSXOSAScript('Google Chrome'), -1)
        except AttributeError:  # If MacOSXOSAScript not defined on module,
          pass                  # proceed with default behavior

      webbrowser.open(authorize_url, new=1, autoraise=True)
      message = 'Your browser has been opened to visit:'
      log.err.Print('{message}\n\n    {url}\n\n'.format(
          message=message,
          url=authorize_url,))

      httpd.handle_request()
      if 'error' in httpd.query_params:
        raise AuthRequestRejectedException('Unable to authenticate.')
      if 'code' in httpd.query_params:
        code = httpd.query_params['code']
      else:
        raise AuthRequestFailedException(
            'Failed to find "code" in the query parameters of the redirect.')
    else:
      message = ('Failed to start a local webserver listening on any port '
                 'between {start_port} and {end_port}. Please check your '
                 'firewall settings or locally running programs that may be '
                 'blocking or using those ports.')
      log.warning(message.format(
          start_port=auth_host_port_start,
          end_port=port_number,
      ))

      launch_browser = False
      log.warning('Defaulting to URL copy/paste mode.')

  if not launch_browser:
    flow.redirect_uri = client.OOB_CALLBACK_URN
    authorize_url = flow.step1_get_authorize_url()
    message = 'Go to the following link in your browser:'
    log.err.Print('{message}\n\n    {url}\n\n'.format(
        message=message,
        url=authorize_url,
    ))
    try:
      code = input('Enter verification code: ').strip()
    except EOFError as e:
      raise AuthRequestRejectedException(e)

  try:
    credential = flow.step2_exchange(code, http=http)
  except client.FlowExchangeError as e:
    raise AuthRequestFailedException(e)
  except ResponseNotReady:
    raise AuthRequestFailedException(
        'Could not reach the login server. A potential cause of this could be '
        'because you are behind a proxy. Please set the environment variables '
        'HTTPS_PROXY and HTTP_PROXY to the address of the proxy in the format '
        '"protocol://address:port" (without quotes) and try again.\n'
        'Example: HTTPS_PROXY=https://192.168.0.1:8080')

  return credential
