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



from google.net.proto import ProtocolBuffer
import abc
import array
try:
  from thread import allocate_lock as _Lock
except ImportError:
  from threading import Lock as _Lock

if hasattr(__builtins__, 'xrange'): range = xrange

if hasattr(ProtocolBuffer, 'ExtendableProtocolMessage'):
  _extension_runtime = True
  _ExtendableProtocolMessage = ProtocolBuffer.ExtendableProtocolMessage
else:
  _extension_runtime = False
  _ExtendableProtocolMessage = ProtocolBuffer.ProtocolMessage

from google.appengine.api.api_base_pb import *
import google.appengine.api.api_base_pb
google_dot_apphosting_dot_api_dot_api__base__pb = __import__('google.appengine.api.api_base_pb', {}, {}, [''])
class SetHttpProxyRequest(ProtocolBuffer.ProtocolMessage):
  has_http_proxy_host_ = 0
  http_proxy_host_ = ""
  has_http_proxy_port_ = 0
  http_proxy_port_ = 0

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def http_proxy_host(self): return self.http_proxy_host_

  def set_http_proxy_host(self, x):
    self.has_http_proxy_host_ = 1
    self.http_proxy_host_ = x

  def clear_http_proxy_host(self):
    if self.has_http_proxy_host_:
      self.has_http_proxy_host_ = 0
      self.http_proxy_host_ = ""

  def has_http_proxy_host(self): return self.has_http_proxy_host_

  def http_proxy_port(self): return self.http_proxy_port_

  def set_http_proxy_port(self, x):
    self.has_http_proxy_port_ = 1
    self.http_proxy_port_ = x

  def clear_http_proxy_port(self):
    if self.has_http_proxy_port_:
      self.has_http_proxy_port_ = 0
      self.http_proxy_port_ = 0

  def has_http_proxy_port(self): return self.has_http_proxy_port_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_http_proxy_host()): self.set_http_proxy_host(x.http_proxy_host())
    if (x.has_http_proxy_port()): self.set_http_proxy_port(x.http_proxy_port())

  def Equals(self, x):
    if x is self: return 1
    if self.has_http_proxy_host_ != x.has_http_proxy_host_: return 0
    if self.has_http_proxy_host_ and self.http_proxy_host_ != x.http_proxy_host_: return 0
    if self.has_http_proxy_port_ != x.has_http_proxy_port_: return 0
    if self.has_http_proxy_port_ and self.http_proxy_port_ != x.http_proxy_port_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_http_proxy_host_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: http_proxy_host not set.')
    if (not self.has_http_proxy_port_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: http_proxy_port not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(len(self.http_proxy_host_))
    n += self.lengthVarInt64(self.http_proxy_port_)
    return n + 2

  def ByteSizePartial(self):
    n = 0
    if (self.has_http_proxy_host_):
      n += 1
      n += self.lengthString(len(self.http_proxy_host_))
    if (self.has_http_proxy_port_):
      n += 1
      n += self.lengthVarInt64(self.http_proxy_port_)
    return n

  def Clear(self):
    self.clear_http_proxy_host()
    self.clear_http_proxy_port()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putPrefixedString(self.http_proxy_host_)
    out.putVarInt32(16)
    out.putVarInt32(self.http_proxy_port_)

  def OutputPartial(self, out):
    if (self.has_http_proxy_host_):
      out.putVarInt32(10)
      out.putPrefixedString(self.http_proxy_host_)
    if (self.has_http_proxy_port_):
      out.putVarInt32(16)
      out.putVarInt32(self.http_proxy_port_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_http_proxy_host(d.getPrefixedString())
        continue
      if tt == 16:
        self.set_http_proxy_port(d.getVarInt32())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_http_proxy_host_: res+=prefix+("http_proxy_host: %s\n" % self.DebugFormatString(self.http_proxy_host_))
    if self.has_http_proxy_port_: res+=prefix+("http_proxy_port: %s\n" % self.DebugFormatInt32(self.http_proxy_port_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  khttp_proxy_host = 1
  khttp_proxy_port = 2

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "http_proxy_host",
    2: "http_proxy_port",
  }, 2)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
    2: ProtocolBuffer.Encoder.NUMERIC,
  }, 2, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.SetHttpProxyRequest'
if _extension_runtime:
  pass

__all__ = ['SetHttpProxyRequest']
