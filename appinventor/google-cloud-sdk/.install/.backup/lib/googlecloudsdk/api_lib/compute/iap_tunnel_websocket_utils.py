# -*- coding: utf-8 -*- #
# Copyright 2018 Google Inc. All Rights Reserved.
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

"""Utility functions for WebSocket tunnelling with Cloud IAP."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import collections
import os
import struct
import sys

from googlecloudsdk.core import exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.util import http_proxy_types
import httplib2
import six
from six.moves.urllib import parse
import socks

URL_SCHEME = 'wss'
URL_HOST = 'tunnel.cloudproxy.app'
URL_PATH_ROOT = '/v4'
CONNECT_ENDPOINT = 'connect'
RECONNECT_ENDPOINT = 'reconnect'

SUBPROTOCOL_NAME = 'relay.tunnel.cloudproxy.app'
SUBPROTOCOL_TAG_LEN = 2
SUBPROTOCOL_HEADER_LEN = SUBPROTOCOL_TAG_LEN + 4
SUBPROTOCOL_MAX_DATA_FRAME_SIZE = 16384
SUBPROTOCOL_TAG_CONNECT_SUCCESS_SID = 0x0001
SUBPROTOCOL_TAG_RECONNECT_SUCCESS_ACK = 0x0002
SUBPROTOCOL_TAG_DATA = 0x0004
SUBPROTOCOL_TAG_ACK = 0x0007

# The proxy_info field should be either None or type httplib2.ProxyInfo
IapTunnelTargetInfo = collections.namedtuple(
    'IapTunnelTarget',
    ['project', 'zone', 'instance', 'interface', 'port', 'url_override',
     'proxy_info'])


class CACertsFileUnavailable(exceptions.Error):
  pass


class IncompleteData(exceptions.Error):
  pass


class InvalidWebSocketSubprotocolData(exceptions.Error):
  pass


class MissingTunnelParameter(exceptions.Error):
  pass


class PythonVersionMissingSNI(exceptions.Error):
  pass


class UnsupportedProxyType(exceptions.Error):
  pass


def ValidateParameters(tunnel_target):
  for field_name, field_value in tunnel_target._asdict().items():
    if not field_value and field_name not in ('url_override', 'proxy_info'):
      raise MissingTunnelParameter('Missing required tunnel argument: ' +
                                   field_name)
  if tunnel_target.proxy_info:
    proxy_type = tunnel_target.proxy_info.proxy_type
    if (proxy_type and proxy_type != socks.PROXY_TYPE_HTTP):
      raise UnsupportedProxyType(
          'Unsupported proxy type: ' +
          http_proxy_types.REVERSE_PROXY_TYPE_MAP[proxy_type])


def CheckCACertsFile(ignore_certs):
  """Get and check that CA cert file exists."""
  ca_certs = httplib2.CA_CERTS
  custom_ca_certs = properties.VALUES.core.custom_ca_certs_file.Get()
  if custom_ca_certs:
    ca_certs = custom_ca_certs
  if not os.path.exists(ca_certs):
    error_msg = 'Unable to locate CA certificates file.'
    log.warning(error_msg)
    error_msg += ' [%s]' % ca_certs
    if ignore_certs:
      log.info(error_msg)
    else:
      raise CACertsFileUnavailable(error_msg)
  return ca_certs


def CheckPythonVersion(ignore_certs):
  if (not ignore_certs and
      (six.PY2 and sys.version_info < (2, 7, 9) or
       six.PY3 and sys.version_info < (3, 2, 0))):
    raise PythonVersionMissingSNI(
        'Python version %d.%d.%d does not support SSL/TLS SNI needed for '
        'certificate verification on WebSocket connection.' %
        (sys.version_info.major, sys.version_info.minor,
         sys.version_info.micro))


def CreateWebSocketConnectUrl(tunnel_target):
  """Create Connect URL for WebSocket connection."""
  return _CreateWebSocketUrl(CONNECT_ENDPOINT,
                             {'project': tunnel_target.project,
                              'zone': tunnel_target.zone,
                              'instance': tunnel_target.instance,
                              'interface': tunnel_target.interface,
                              'port': tunnel_target.port},
                             tunnel_target.url_override)


def CreateWebSocketReconnectUrl(tunnel_target, sid, ack_bytes):
  """Create Reconnect URL for WebSocket connection."""
  return _CreateWebSocketUrl(RECONNECT_ENDPOINT,
                             {'sid': sid, 'ack': ack_bytes},
                             tunnel_target.url_override)


def _CreateWebSocketUrl(endpoint, url_query_pieces, url_override):
  """Create URL for WebSocket connection."""
  scheme, hostname, path_root = (URL_SCHEME, URL_HOST, URL_PATH_ROOT)
  if url_override:
    url_override_parts = parse.urlparse(url_override)
    scheme, hostname, path_override = url_override_parts[:3]
    if path_override and path_override != '/':
      path_root = path_override

  qs = parse.urlencode(url_query_pieces)
  path = ('%s%s' % (path_root, endpoint) if path_root.endswith('/')
          else '%s/%s' % (path_root, endpoint))
  return parse.urlunparse((scheme, hostname, path, '', qs, ''))


def CreateSubprotocolAckFrame(ack_bytes):
  try:
    return struct.pack(str('>HQ'), SUBPROTOCOL_TAG_ACK, ack_bytes)
  except struct.error:
    raise InvalidWebSocketSubprotocolData('Invalid Ack [%r]' % ack_bytes)


def CreateSubprotocolDataFrame(bytes_to_send):
  return struct.pack(str('>HI%ds' % len(bytes_to_send)),
                     SUBPROTOCOL_TAG_DATA, len(bytes_to_send), bytes_to_send)


def ExtractSubprotocolAck(binary_data):
  return _ExtractUnsignedInt64(binary_data)


def ExtractSubprotocolConnectSuccessSid(binary_data):
  data_len, binary_data = _ExtractUnsignedInt32(binary_data)
  return _ExtractBinaryArray(binary_data, data_len)


def ExtractSubprotocolData(binary_data):
  data_len, binary_data = _ExtractUnsignedInt32(binary_data)
  return _ExtractBinaryArray(binary_data, data_len)


def ExtractSubprotocolReconnectSuccessAck(binary_data):
  return _ExtractUnsignedInt64(binary_data)


def ExtractSubprotocolTag(binary_data):
  return _ExtractUnsignedInt16(binary_data)


def _ExtractUnsignedInt16(binary_data):
  if len(binary_data) < 2:
    raise IncompleteData()
  return (struct.unpack(str('>H'), binary_data[:2])[0],
          binary_data[2:])


def _ExtractUnsignedInt32(binary_data):
  if len(binary_data) < 4:
    raise IncompleteData()
  return (struct.unpack(str('>I'), binary_data[:4])[0],
          binary_data[4:])


def _ExtractUnsignedInt64(binary_data):
  if len(binary_data) < 8:
    raise IncompleteData()
  return (struct.unpack(str('>Q'), binary_data[:8])[0],
          binary_data[8:])


def _ExtractBinaryArray(binary_data, data_len):
  if len(binary_data) < data_len:
    raise IncompleteData()
  return (struct.unpack(str('%ds' % data_len), binary_data[:data_len])[0],
          binary_data[data_len:])
