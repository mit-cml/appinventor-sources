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

"""WebSocket helper class for tunneling with Cloud IAP."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import logging
import ssl
import sys
import threading
import traceback

from googlecloudsdk.api_lib.compute import iap_tunnel_websocket_utils as utils
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import log

import websocket

TUNNEL_CLOUDPROXY_ORIGIN = 'bot:iap-tunneler'


class WebSocketConnectionClosed(exceptions.Error):
  pass


class WebSocketInvalidOpcodeError(exceptions.Error):
  pass


class WebSocketSendError(exceptions.Error):
  pass


class IapTunnelWebSocketHelper(object):
  """Helper class for common operations on websocket and related metadata."""

  def __init__(self, url, headers, ignore_certs, proxy_info, on_data, on_close):
    self._on_data = on_data
    self._on_close = on_close
    self._proxy_info = proxy_info
    self._receiving_thread = None

    ca_certs = utils.CheckCACertsFile(ignore_certs)
    self._sslopt = {'cert_reqs': ssl.CERT_REQUIRED,
                    'ca_certs': ca_certs}
    if ignore_certs:
      self._sslopt['cert_reqs'] = ssl.CERT_NONE
      self._sslopt['check_hostname'] = False

    # Disable most of random logging in websocket library itself
    logging.getLogger('websocket').setLevel(logging.CRITICAL)

    self._is_closed = False
    self._error_msg = ''
    self._websocket = websocket.WebSocketApp(
        url, header=headers, on_close=self._OnClose, on_data=self._OnData,
        on_error=self._OnError, subprotocols=[utils.SUBPROTOCOL_NAME])

  def __del__(self):
    self.Close()

  def Close(self, msg=''):
    """Close the WebSocket."""
    if not self._is_closed:
      try:
        self._websocket.close()
      except:  # pylint: disable=bare-except
        pass
      if not self._error_msg:
        self._error_msg = msg
      self._is_closed = True

  def IsClosed(self):
    """Check to see if WebSocket has closed."""
    return (self._is_closed or
            (self._receiving_thread and not self._receiving_thread.isAlive()))

  def ErrorMsg(self):
    return self._error_msg

  def Send(self, send_data):
    """Send data on WebSocket connection."""
    try:
      log.debug('SEND data_len [%d] send_data[:20] %r', len(send_data),
                send_data[:20])
      self._websocket.send(send_data, opcode=websocket.ABNF.OPCODE_BINARY)
    except EnvironmentError:
      self.Close()
      raise
    except websocket.WebSocketConnectionClosedException:
      self.Close()
      raise WebSocketConnectionClosed()
    except Exception as e:  # pylint: disable=broad-except
      # Convert websocket library errors and any others into one based on
      # exceptions.Error
      tb = sys.exc_info()[2]
      self.Close()
      exceptions.reraise(
          WebSocketSendError(traceback.format_exception_only(type(e), e),
                             tb=tb))

  def SendClose(self):
    """Send WebSocket Close message if possible."""
    if self._websocket.sock:
      log.debug('CLOSE')
      try:
        self._websocket.sock.send_close()
      except (EnvironmentError,
              websocket.WebSocketConnectionClosedException) as e:
        log.info('Unable to send WebSocket Close message [%s].', str(e))
        self.Close()
      except:  # pylint: disable=bare-except
        log.info('Error during WebSocket send of Close message.', exc_info=True)
        self.Close()

  def StartReceivingThread(self):
    if not self._is_closed:
      self._receiving_thread = threading.Thread(
          target=self._ReceiveFromWebSocket)
      self._receiving_thread.daemon = True
      self._receiving_thread.start()

  def _OnClose(self, unused_websocket_app, *optional_close_data):
    """Callback for WebSocket Close messages."""
    # The function definition for _OnClose is a hack so the underlying library
    # will follow the right branches for this callback.  The websocket library
    # does not account for a callback possibly being a class method which would
    # add +1 for 'self' to the arg list.  We expect it to be called with two
    # elements in the optional_close_data when a Close message is received from
    # the server: optional_close_data == [close_code (int), close_reason (str)]
    if not optional_close_data:
      # Just a local close event and not an actual Close message.
      self.Close()
      return

    # Having optional_close_data present (even if empty) indicates an actual
    # Close message was received vs the WebSocket just closing for another
    # reason.
    if (len(optional_close_data) == 2 and optional_close_data[0] is not None and
        optional_close_data[1] is not None):
      close_msg = '%d: %s' % (optional_close_data[0], optional_close_data[1])
    else:
      close_msg = 'Server close message received with missing or invalid data.'

    log.info('Received WebSocket Close message [%s].', close_msg)
    self.Close(msg=close_msg)
    try:
      self._on_close()
    except (EnvironmentError, exceptions.Error):
      log.info('Error while processing Close message', exc_info=True)
      raise

  def _OnData(self, unused_websocket_app, binary_data, opcode, unused_finished):
    """Callback for WebSocket Data messages."""
    log.debug('RECV opcode [%r] data_len [%d] binary_data[:20] [%r]', opcode,
              len(binary_data), binary_data[:20])
    try:
      # Even though we will only be processing BINARY messages, a bug in the
      # underlying websocket library will report the last opcode in a
      # multi-frame message instead of the first opcode - so CONT instead of
      # BINARY.
      if opcode not in (websocket.ABNF.OPCODE_CONT,
                        websocket.ABNF.OPCODE_BINARY):
        raise WebSocketInvalidOpcodeError('Unexpected WebSocket opcode [%r].' %
                                          opcode)
      self._on_data(binary_data)
    except EnvironmentError as e:
      log.info('Error [%s] while sending to client.', str(e))
      self.Close()
      raise
    except:  # pylint: disable=bare-except
      log.info('Error while processing Data message.', exc_info=True)
      self.Close()
      raise

  def _OnError(self, unused_websocket_app, exception_obj):
    # Do not call Close() from here as it may generate callbacks in some error
    # conditions that can create a feedback loop with this function.
    if not self._is_closed:
      log.info('Error during WebSocket processing:\n' +
               ''.join(traceback.format_exception_only(type(exception_obj),
                                                       exception_obj)))
      self._error_msg = str(exception_obj)

  def _ReceiveFromWebSocket(self):
    """Receive data from WebSocket connection."""
    try:
      if self._proxy_info:
        http_proxy_auth = None
        if self._proxy_info.proxy_user or self._proxy_info.proxy_pass:
          http_proxy_auth = (self._proxy_info.proxy_user,
                             self._proxy_info.proxy_pass)
        self._websocket.run_forever(
            origin=TUNNEL_CLOUDPROXY_ORIGIN, sslopt=self._sslopt,
            http_proxy_host=self._proxy_info.proxy_host,
            http_proxy_port=self._proxy_info.proxy_port,
            http_proxy_auth=http_proxy_auth)
      else:
        self._websocket.run_forever(origin=TUNNEL_CLOUDPROXY_ORIGIN,
                                    sslopt=self._sslopt)
    except:  # pylint: disable=bare-except
      log.info('Error while receiving from WebSocket.', exc_info=True)
    self.Close()
