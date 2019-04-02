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

"""WebSocket connection class for tunneling with Cloud IAP."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import collections
import threading
import time

from googlecloudsdk.api_lib.compute import iap_tunnel_websocket_helper as helper
from googlecloudsdk.api_lib.compute import iap_tunnel_websocket_utils as utils
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import http
from googlecloudsdk.core import log
from googlecloudsdk.core.util import retry

MAX_WEBSOCKET_OPEN_WAIT_TIME_SEC = 60  # seconds
MAX_RECONNECT_SLEEP_TIME_MS = 20 * 1000  # milliseconds
MAX_RECONNECT_WAIT_TIME_MS = 60 * 60 * 1000  # milliseconds
MAX_UNSENT_QUEUE_LENGTH = 20
RECONNECT_INITIAL_SLEEP_MS = 1500  # milliseconds


class ConnectionCreationError(exceptions.Error):
  pass


class ConnectionReconnectTimeout(exceptions.Error):
  pass


class SubprotocolEarlyAckError(exceptions.Error):
  pass


class SubprotocolEarlyDataError(exceptions.Error):
  pass


class SubprotocolExtraConnectSuccessSid(exceptions.Error):
  pass


class SubprotocolExtraReconnectSuccessAck(exceptions.Error):
  pass


class SubprotocolInvalidAckError(exceptions.Error):
  pass


class SubprotocolOutOfOrderAckError(exceptions.Error):
  pass


class IapTunnelWebSocket(object):
  """Cloud IAP WebSocket class for tunnelling connections."""

  def __init__(self, tunnel_target, get_access_token_callback,
               data_handler_callback, close_handler_callback,
               ignore_certs=False):
    self._tunnel_target = tunnel_target
    self._get_access_token_callback = get_access_token_callback
    self._data_handler_callback = data_handler_callback
    self._close_handler_callback = close_handler_callback
    self._ignore_certs = ignore_certs

    self._websocket_helper = None
    self._connect_msg_received = False
    self._connection_sid = None
    self._stopping = False
    self._close_message_sent = False
    self._send_and_reconnect_thread = None

    self._total_bytes_confirmed = 0
    self._total_bytes_received = 0
    self._total_bytes_received_and_acked = 0
    self._unsent_data = collections.deque()
    self._unconfirmed_data = collections.deque()

  def __del__(self):
    if self._websocket_helper:
      self._websocket_helper.Close()

  def Close(self):
    """Close down local connection and WebSocket connection."""
    self._stopping = True
    try:
      self._close_handler_callback()
    except:  # pylint: disable=bare-except
      pass
    if self._websocket_helper:
      if not self._close_message_sent:
        self._websocket_helper.SendClose()
        self._close_message_sent = True
      self._websocket_helper.Close()

  def InitiateConnection(self):
    """Initiate the WebSocket connection."""
    utils.CheckPythonVersion(self._ignore_certs)
    utils.ValidateParameters(self._tunnel_target)

    self._StartNewWebSocket()
    self._WaitForOpenOrRaiseError()
    self._send_and_reconnect_thread = threading.Thread(
        target=self._SendDataAndReconnectWebSocket)
    self._send_and_reconnect_thread.daemon = True
    self._send_and_reconnect_thread.start()

  def Send(self, bytes_to_send):
    """Send bytes over WebSocket connection."""
    while bytes_to_send:
      first_to_send = bytes_to_send[:utils.SUBPROTOCOL_MAX_DATA_FRAME_SIZE]
      bytes_to_send = bytes_to_send[utils.SUBPROTOCOL_MAX_DATA_FRAME_SIZE:]
      if first_to_send:
        self._EnqueueBytesWithWaitForReconnect(first_to_send)

  def _AttemptReconnect(self, reconnect_func):
    """Attempt to reconnect with a new WebSocket."""

    r = retry.Retryer(max_wait_ms=MAX_RECONNECT_WAIT_TIME_MS,
                      exponential_sleep_multiplier=1.1,
                      wait_ceiling_ms=MAX_RECONNECT_SLEEP_TIME_MS)
    try:
      r.RetryOnException(func=reconnect_func,
                         sleep_ms=RECONNECT_INITIAL_SLEEP_MS)
    except retry.RetryException:
      log.warning('Unable to reconnect within [%d] ms',
                  MAX_RECONNECT_WAIT_TIME_MS, exc_info=True)
      self._StopConnectionAsync()

  def _EnqueueBytesWithWaitForReconnect(self, bytes_to_send):
    """Add bytes to the queue; sleep waiting for reconnect if queue is full."""
    end_time = time.time() + MAX_RECONNECT_WAIT_TIME_MS / 1000.0
    while time.time() < end_time:
      if len(self._unsent_data) < MAX_UNSENT_QUEUE_LENGTH:
        self._unsent_data.append(bytes_to_send)
        log.debug('ENQUEUE data_len [%d] bytes_to_send[:20] [%r]',
                  len(bytes_to_send), bytes_to_send[:20])
        return
      time.sleep(0.01)
    raise ConnectionReconnectTimeout()

  def _HasConnected(self):
    """Returns true if we received a connect message."""
    return self._connect_msg_received

  def _IsClosed(self):
    return ((self._websocket_helper and self._websocket_helper.IsClosed()) or
            (self._send_and_reconnect_thread and
             not self._send_and_reconnect_thread.isAlive()))

  def _StartNewWebSocket(self):
    """Start a new WebSocket and thread to listen for incoming data."""
    headers = ['User-Agent: ' + http.MakeUserAgentString()]
    if self._get_access_token_callback:
      headers += ['Authorization: Bearer ' + self._get_access_token_callback()]

    if self._connection_sid:
      url = utils.CreateWebSocketReconnectUrl(
          self._tunnel_target, self._connection_sid, self._total_bytes_received)
      log.info('Reconnecting with URL [%r]', url)
    else:
      url = utils.CreateWebSocketConnectUrl(self._tunnel_target)
      log.info('Connecting with URL [%r]', url)

    self._connect_msg_received = False
    self._websocket_helper = helper.IapTunnelWebSocketHelper(
        url, headers, self._ignore_certs, self._tunnel_target.proxy_info,
        self._OnData, self._OnClose)
    self._websocket_helper.StartReceivingThread()

  def _SendAck(self):
    """Send an ACK back to server."""
    if self._total_bytes_received > self._total_bytes_received_and_acked:
      bytes_received = self._total_bytes_received
      try:
        ack_data = utils.CreateSubprotocolAckFrame(bytes_received)
        self._websocket_helper.Send(ack_data)
        self._total_bytes_received_and_acked = bytes_received
      except helper.WebSocketConnectionClosed:
        pass
      except EnvironmentError as e:
        log.info('Unable to send WebSocket ack [%s]', str(e))
      except:  # pylint: disable=bare-except
        if not self._IsClosed():
          log.info('Error while attempting to ack [%d] bytes', bytes_received,
                   exc_info=True)

  def _SendDataAndReconnectWebSocket(self):
    """Main function for send_and_reconnect_thread."""
    def Reconnect():
      if not self._stopping:
        self._StartNewWebSocket()
        self._WaitForOpenOrRaiseError()

    try:
      while not self._stopping:
        if self._IsClosed():
          self._AttemptReconnect(Reconnect)

        elif self._HasConnected():
          self._SendQueuedData()
          if not self._IsClosed():
            self._SendAck()

        if not self._stopping:
          time.sleep(0.01)
    except:  # pylint: disable=bare-except
      log.debug('Error from WebSocket while sending data.', exc_info=True)
    self.Close()

  def _SendQueuedData(self):
    """Send data that is sitting in the unsent data queue."""
    while self._unsent_data and not self._stopping:
      try:
        send_data = utils.CreateSubprotocolDataFrame(self._unsent_data[0])
        self._websocket_helper.Send(send_data)
        self._unconfirmed_data.append(self._unsent_data.popleft())
      except helper.WebSocketConnectionClosed:
        break
      except EnvironmentError as e:
        log.info('Unable to send WebSocket data [%s]', str(e))
        break
      except:  # pylint: disable=bare-except
        log.info('Error while attempting to send [%d] bytes', len(send_data),
                 exc_info=True)
        break

  def _StopConnectionAsync(self):
    self._stopping = True

  def _WaitForOpenOrRaiseError(self):
    """Wait for WebSocket open confirmation or any error condition."""
    for _ in range(MAX_WEBSOCKET_OPEN_WAIT_TIME_SEC * 100):
      if self._IsClosed():
        break
      if self._HasConnected():
        return
      time.sleep(0.01)

    if (self._websocket_helper and self._websocket_helper.IsClosed() and
        self._websocket_helper.ErrorMsg()):
      extra_msg = ''
      # Error messages like 'Handshake status 400' or 'Handshake status 404'
      # may often indicate missing permissions.
      if self._websocket_helper.ErrorMsg().startswith('Handshake status 40'):
        extra_msg = ' (May be due to missing permissions)'
      error_msg = ('Error while connecting [%s].%s' %
                   (self._websocket_helper.ErrorMsg(), extra_msg))
      raise ConnectionCreationError(error_msg)

    raise ConnectionCreationError('Unexpected error while connecting. Check '
                                  'logs for more details.')

  def _OnClose(self):
    self._StopConnectionAsync()

  def _OnData(self, binary_data):
    """Receive a single message from the server."""
    tag, bytes_left = utils.ExtractSubprotocolTag(binary_data)
    # In order of decreasing usage during connection:
    if tag == utils.SUBPROTOCOL_TAG_DATA:
      self._HandleSubprotocolData(bytes_left)
    elif tag == utils.SUBPROTOCOL_TAG_ACK:
      self._HandleSubprotocolAck(bytes_left)
    elif tag == utils.SUBPROTOCOL_TAG_CONNECT_SUCCESS_SID:
      self._HandleSubprotocolConnectSuccessSid(bytes_left)
    elif tag == utils.SUBPROTOCOL_TAG_RECONNECT_SUCCESS_ACK:
      self._HandleSubprotocolReconnectSuccessAck(bytes_left)
    else:
      log.debug('Unsupported subprotocol tag [%r], discarding the message', tag)

  def _HandleSubprotocolAck(self, binary_data):
    """Handle Subprotocol ACK Frame."""
    if not self._HasConnected():
      self._StopConnectionAsync()
      raise SubprotocolEarlyAckError('Received ACK before connected.')

    bytes_confirmed, bytes_left = utils.ExtractSubprotocolAck(binary_data)
    self._ConfirmData(bytes_confirmed)
    if bytes_left:
      log.debug('Discarding [%d] extra bytes after processing ACK',
                len(bytes_left))

  def _HandleSubprotocolConnectSuccessSid(self, binary_data):
    """Handle Subprotocol CONNECT_SUCCESS_SID Frame."""
    if self._HasConnected():
      self._StopConnectionAsync()
      raise SubprotocolExtraConnectSuccessSid(
          'Received CONNECT_SUCCESS_SID after already connected.')

    data, bytes_left = utils.ExtractSubprotocolConnectSuccessSid(binary_data)
    self._connection_sid = data
    self._connect_msg_received = True
    if bytes_left:
      log.debug(
          'Discarding [%d] extra bytes after processing CONNECT_SUCCESS_SID',
          len(bytes_left))

  def _HandleSubprotocolReconnectSuccessAck(self, binary_data):
    """Handle Subprotocol RECONNECT_SUCCESS_ACK Frame."""
    if self._HasConnected():
      self._StopConnectionAsync()
      raise SubprotocolExtraReconnectSuccessAck(
          'Received RECONNECT_SUCCESS_ACK after already connected.')

    bytes_confirmed, bytes_left = (
        utils.ExtractSubprotocolReconnectSuccessAck(binary_data))
    bytes_being_confirmed = bytes_confirmed - self._total_bytes_confirmed
    self._ConfirmData(bytes_confirmed)
    log.info(
        'Reconnecting: confirming [%d] bytes and resending [%d] messages.',
        bytes_being_confirmed, len(self._unconfirmed_data))
    self._unsent_data.extendleft(reversed(self._unconfirmed_data))
    self._unconfirmed_data = collections.deque()
    self._connect_msg_received = True
    if bytes_left:
      log.debug(
          'Discarding [%d] extra bytes after processing RECONNECT_SUCCESS_ACK',
          len(bytes_left))

  def _HandleSubprotocolData(self, binary_data):
    """Handle Subprotocol DATA Frame."""
    if not self._HasConnected():
      self._StopConnectionAsync()
      raise SubprotocolEarlyDataError('Received DATA before connected.')

    data, bytes_left = utils.ExtractSubprotocolData(binary_data)
    self._total_bytes_received += len(data)
    try:
      self._data_handler_callback(data)
    except:  # pylint: disable=bare-except
      self._StopConnectionAsync()
      raise
    if bytes_left:
      log.debug('Discarding [%d] extra bytes after processing DATA',
                len(bytes_left))

  def _ConfirmData(self, bytes_confirmed):
    """Discard data that has been confirmed via ACKs received from server."""
    if bytes_confirmed < self._total_bytes_confirmed:
      self._StopConnectionAsync()
      raise SubprotocolOutOfOrderAckError(
          'Received out-of-order Ack for [%d] bytes.' % bytes_confirmed)

    bytes_to_confirm = bytes_confirmed - self._total_bytes_confirmed
    while bytes_to_confirm and self._unconfirmed_data:
      data_chunk = self._unconfirmed_data.popleft()
      if len(data_chunk) > bytes_to_confirm:
        self._unconfirmed_data.appendleft(data_chunk[bytes_to_confirm:])
        self._total_bytes_confirmed += bytes_to_confirm
      else:
        self._total_bytes_confirmed += len(data_chunk)
      bytes_to_confirm = bytes_confirmed - self._total_bytes_confirmed

    if bytes_to_confirm:
      self._StopConnectionAsync()
      raise SubprotocolInvalidAckError(
          'Bytes confirmed [%r] were larger than bytes sent [%r].' %
          (bytes_confirmed, self._total_bytes_confirmed))
