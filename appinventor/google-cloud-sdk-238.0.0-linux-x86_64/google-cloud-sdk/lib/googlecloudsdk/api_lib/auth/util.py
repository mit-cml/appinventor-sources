# -*- coding: utf-8 -*- #
# Copyright 2016 Google Inc. All Rights Reserved.
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

"""A library to support auth commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import json
import os

from googlecloudsdk.core import exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core.credentials import store as c_store
from googlecloudsdk.core.util import encoding
from googlecloudsdk.core.util import files

from oauth2client import client
from oauth2client import clientsecrets


# Client ID from project "usable-auth-library", configured for
# general purpose API testing
# pylint: disable=g-line-too-long
DEFAULT_CREDENTIALS_DEFAULT_CLIENT_ID = '764086051850-6qr4p6gpi6hn506pt8ejuq83di341hur.apps.googleusercontent.com'
DEFAULT_CREDENTIALS_DEFAULT_CLIENT_SECRET = 'd-FL95Q19q7MQmFpd7hHD0Ty'
CLOUD_PLATFORM_SCOPE = 'https://www.googleapis.com/auth/cloud-platform'
GOOGLE_DRIVE_SCOPE = 'https://www.googleapis.com/auth/drive'
USER_EMAIL_SCOPE = 'https://www.googleapis.com/auth/userinfo.email'

DEFAULT_SCOPES = [
    USER_EMAIL_SCOPE,
    CLOUD_PLATFORM_SCOPE
]


class Error(exceptions.Error):
  """A base exception for this class."""
  pass


class InvalidClientSecretsError(Error):
  """An error for when we fail to load the client secrets file."""
  pass


def DoInstalledAppBrowserFlow(launch_browser, scopes, client_id_file=None,
                              client_id=None, client_secret=None):
  """Launches a browser to get credentials.

  Args:
    launch_browser: bool, True to do a browser flow, false to allow the user to
      type in a token from a different browser.
    scopes: [str], The list of scopes to authorize.
    client_id_file: str, The path to a file containing the client id and secret
      to use for the flow.  If None, the default client id for the Cloud SDK is
      used.
    client_id: str, An alternate client id to use.  This is ignored if you give
      a client id file.  If None, the default client id for the Cloud SDK is
      used.
    client_secret: str, The secret to go along with client_id if specified.

  Returns:
    The clients obtained from the web flow.
  """
  try:
    if client_id_file:
      client_type = GetClientSecretsType(client_id_file)
      if client_type != clientsecrets.TYPE_INSTALLED:
        raise InvalidClientSecretsError(
            'Only client IDs of type \'%s\' are allowed, but encountered '
            'type \'%s\'' % (clientsecrets.TYPE_INSTALLED, client_type))
      webflow = client.flow_from_clientsecrets(
          filename=client_id_file,
          scope=scopes)
      return c_store.RunWebFlow(webflow, launch_browser=launch_browser)
    else:
      return c_store.AcquireFromWebFlow(
          launch_browser=launch_browser,
          scopes=scopes,
          client_id=client_id,
          client_secret=client_secret)
  except c_store.FlowError:
    msg = 'There was a problem with web authentication.'
    if launch_browser:
      msg += ' Try running again with --no-launch-browser.'
    log.error(msg)
    raise


# TODO(b/29157057) make this information accessible through oauth2client
# instead of duplicating internal code from clientsecrets
def GetClientSecretsType(client_id_file):
  """Get the type of the client secrets file (web or installed)."""
  invalid_file_format_msg = (
      'Invalid file format. See '
      'https://developers.google.com/api-client-library/'
      'python/guide/aaa_client_secrets')

  try:
    obj = json.loads(files.ReadFileContents(client_id_file))
  except files.Error:
    raise InvalidClientSecretsError(
        'Cannot read file: "%s"' % client_id_file)
  if obj is None:
    raise InvalidClientSecretsError(invalid_file_format_msg)
  if len(obj) != 1:
    raise InvalidClientSecretsError(
        invalid_file_format_msg + ' '
        'Expected a JSON object with a single property for a "web" or '
        '"installed" application')
  return tuple(obj)[0]


def ADCFilePath():
  """Gets the ADC default file path.

  Returns:
    str, The path to the default ADC file.
  """
  # pylint:disable=protected-access
  return client._get_well_known_file()


def AdcEnvVariable():
  """Gets the value of the ADC environment variable.

  Returns:
    str, The value of the env var or None if unset.
  """
  return encoding.GetEncodedValue(
      os.environ, client.GOOGLE_APPLICATION_CREDENTIALS, None)


def SaveCredentialsAsADC(creds):
  """Saves the credentials to the given file.

  Args:
    creds: The credentials obtained from a web flow.

  Returns:
    str, The full path to the ADC file that was written.
  """
  adc_file = ADCFilePath()
  c_store.SaveCredentialsAsADC(creds, adc_file)
  return os.path.abspath(adc_file)
