# -*- coding: utf-8 -*- #
# Copyright 2014 Google Inc. All Rights Reserved.
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

"""Credentials for use with the developer shell."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import datetime
import json
import os

from apitools.base.protorpclite import messages

from googlecloudsdk.core import config

from oauth2client import client
import six

DEVSHELL_ENV = 'CLOUD_SHELL'
DEVSHELL_CLIENT_PORT = 'DEVSHELL_CLIENT_PORT'
DEVSHELL_ENV_IPV6_ENABLED = 'DEVSHELL_CLIENT_PORT_IPV6_ENABLED'


class Error(Exception):
  """Errors for this module."""
  pass


class CommunicationError(Error):
  """Errors for communication with the access token server."""


class NoDevshellServer(Error):
  """Error when no devshell server can be contacted."""


def MessageToPBLiteList(msg):
  """Convert a protorpc Message into a list suitable for PBLite.

  Args:
    msg: messages.Message, The Message to be turned into a list.

  Returns:
    [obj], A list that has only primitives or other lists suitable for
        PBLite serialization.
  """
  index_keys = dict([(f.number, f.name) for f in msg.all_fields()])
  if not index_keys:
    return []

  max_index = max(index_keys.keys())
  json_list = [None] * max_index

  for index, key in six.iteritems(index_keys):
    value = getattr(msg, key, None)
    if isinstance(value, messages.Message):
      value = MessageToPBLiteList(value)
    json_list[index-1] = value

  return json_list


def PBLiteListToMessage(pbl, message_class):
  """Convert a PBLite list into a type of the provided class.

  Args:
    pbl: [obj], A list deserialized from a PBLite message.
    message_class: type, The messages.Message subclass to be used to create
        the message.

  Returns:
    messages.Message, The deserialized message object.

  Raises:
    ValueError: If the list is too small for the message type.
  """
  if not isinstance(pbl, list):
    raise ValueError(
        '{obj} of type {type} is not a list'.format(
            obj=pbl, type=type(pbl)))
  kwargs = {}
  for field in message_class.all_fields():
    if field.number > len(pbl):
      raise ValueError(
          'PBLite list {list} is too small for {type}'.format(
              list=repr(pbl),
              type=message_class))
    value = pbl[field.number-1]
    if issubclass(field.type, messages.Message):
      value = PBLiteListToMessage(value, field.type)
    kwargs[field.name] = value
  return message_class(**kwargs)


def JSONToMessage(data, message_class):
  pbl = json.loads(data)
  return PBLiteListToMessage(pbl, message_class)


def MessageToJSON(msg):
  pbl = MessageToPBLiteList(msg)
  return json.dumps(pbl)


class CredentialInfoRequest(messages.Message):
  pass


class CredentialInfoResponse(messages.Message):
  user_email = messages.StringField(1, required=True)
  project_id = messages.StringField(2)
  access_token = messages.StringField(3)
  expires_in = messages.FloatField(4)


def _SendRecv(request):
  """Communicate with the devshell access token service."""
  port = int(os.getenv(DEVSHELL_CLIENT_PORT, 0))
  if not port:
    raise NoDevshellServer()
  return _SendRecvPort(request, port)


def _SendRecvPort(request, port):
  """Communicate with the devshell access token service."""

  # pylint:disable=g-import-not-at-top, Delay for performance.
  import socket

  data = MessageToJSON(request)
  n = len(data)
  nstr = '%d' % n
  if len(nstr) > 5:
    raise ValueError('length too long')

  if socket.has_ipv6 and os.getenv(DEVSHELL_ENV_IPV6_ENABLED) is not None:
    s = socket.socket(socket.AF_INET6)
  else:
    s = socket.socket()

  s.connect(('localhost', port))
  msg = ('%s\n%s' % (nstr, data)).encode('utf8')
  s.sendall(msg)

  resp_1 = s.recv(6).decode('utf8')
  if '\n' not in resp_1:
    raise CommunicationError('saw no newline in the first 6 bytes')
  nstr, extra = resp_1.split('\n', 1)
  resp_buffer = extra
  n = int(nstr)
  to_read = n-len(extra)
  if to_read > 0:
    resp_buffer += s.recv(to_read, socket.MSG_WAITALL).decode('utf8')

  return JSONToMessage(resp_buffer, CredentialInfoResponse)


def Project():
  """Fetch the project from a devshell auth proxy.

  Returns:
    The project ID or None, if no devshell proxy was listening.
  """
  request = CredentialInfoRequest()
  try:
    response = _SendRecv(request)
  except Exception:  # pylint:disable=broad-except
    return None
  return response.project_id


def DefaultAccount():
  """Fetch the account from a devshell auth proxy.

  Returns:
    The project ID or None, if no devshell proxy was listening.
  """
  request = CredentialInfoRequest()
  try:
    response = _SendRecv(request)
  except Exception:  # pylint:disable=broad-except
    return None
  return response.user_email


class DevshellCredentials(client.OAuth2Credentials):

  def __init__(self, **kwargs):
    # Update __dict__ directly instead of calling super __init__ to avoid having
    # to pass positional arguments.
    self.__dict__.update(**kwargs)
    self.invalid = False
    self._refresh(None)

  def _refresh(self, http):
    request = CredentialInfoRequest()
    self.devshell_response = _SendRecv(request)
    self.access_token = self.devshell_response.access_token
    if self.devshell_response.expires_in is not None:
      # Use utcnow as Oauth2client uses utcnow to determine if token is expired.
      self.token_expiry = (datetime.datetime.utcnow() + datetime.timedelta(
          seconds=self.devshell_response.expires_in))


def LoadDevshellCredentials():
  """Load devshell credentials from the proxy.

  Also sets various attributes on the credential object expected by other
  parties.

  Returns:
    DevshellCredentials, if available. If the proxy can't be reached or returns
    garbage data, this function returns None.
  """
  try:
    return DevshellCredentials(
        user_agent=config.CLOUDSDK_USER_AGENT,)
  except Exception:  # pylint:disable=broad-except, any problem means None
    return None


def IsDevshellEnvironment():
  return bool(os.getenv(DEVSHELL_ENV, False)) or HasDevshellAuth()


def HasDevshellAuth():
  port = int(os.getenv(DEVSHELL_CLIENT_PORT, 0))
  return port != 0
