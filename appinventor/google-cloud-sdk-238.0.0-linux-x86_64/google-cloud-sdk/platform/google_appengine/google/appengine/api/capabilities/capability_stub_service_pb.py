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

from google.appengine.base.capabilities_pb import *
import google.appengine.base.capabilities_pb
google_dot_apphosting_dot_base_dot_capabilities__pb = __import__('google.appengine.base.capabilities_pb', {}, {}, [''])
class SetCapabilityStatusRequest(ProtocolBuffer.ProtocolMessage):
  has_service_name_ = 0
  service_name_ = ""
  has_call_ = 0
  call_ = ""
  has_status_ = 0

  def __init__(self, contents=None):
    self.status_ = Status()
    if contents is not None: self.MergeFromString(contents)

  def service_name(self): return self.service_name_

  def set_service_name(self, x):
    self.has_service_name_ = 1
    self.service_name_ = x

  def clear_service_name(self):
    if self.has_service_name_:
      self.has_service_name_ = 0
      self.service_name_ = ""

  def has_service_name(self): return self.has_service_name_

  def call(self): return self.call_

  def set_call(self, x):
    self.has_call_ = 1
    self.call_ = x

  def clear_call(self):
    if self.has_call_:
      self.has_call_ = 0
      self.call_ = ""

  def has_call(self): return self.has_call_

  def status(self): return self.status_

  def mutable_status(self): self.has_status_ = 1; return self.status_

  def clear_status(self):self.has_status_ = 0; self.status_.Clear()

  def has_status(self): return self.has_status_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_service_name()): self.set_service_name(x.service_name())
    if (x.has_call()): self.set_call(x.call())
    if (x.has_status()): self.mutable_status().MergeFrom(x.status())

  def Equals(self, x):
    if x is self: return 1
    if self.has_service_name_ != x.has_service_name_: return 0
    if self.has_service_name_ and self.service_name_ != x.service_name_: return 0
    if self.has_call_ != x.has_call_: return 0
    if self.has_call_ and self.call_ != x.call_: return 0
    if self.has_status_ != x.has_status_: return 0
    if self.has_status_ and self.status_ != x.status_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_service_name_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: service_name not set.')
    if (not self.has_call_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: call not set.')
    if (not self.has_status_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: status not set.')
    elif not self.status_.IsInitialized(debug_strs): initialized = 0
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(len(self.service_name_))
    n += self.lengthString(len(self.call_))
    n += self.lengthString(self.status_.ByteSize())
    return n + 3

  def ByteSizePartial(self):
    n = 0
    if (self.has_service_name_):
      n += 1
      n += self.lengthString(len(self.service_name_))
    if (self.has_call_):
      n += 1
      n += self.lengthString(len(self.call_))
    if (self.has_status_):
      n += 1
      n += self.lengthString(self.status_.ByteSizePartial())
    return n

  def Clear(self):
    self.clear_service_name()
    self.clear_call()
    self.clear_status()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putPrefixedString(self.service_name_)
    out.putVarInt32(18)
    out.putPrefixedString(self.call_)
    out.putVarInt32(26)
    out.putVarInt32(self.status_.ByteSize())
    self.status_.OutputUnchecked(out)

  def OutputPartial(self, out):
    if (self.has_service_name_):
      out.putVarInt32(10)
      out.putPrefixedString(self.service_name_)
    if (self.has_call_):
      out.putVarInt32(18)
      out.putPrefixedString(self.call_)
    if (self.has_status_):
      out.putVarInt32(26)
      out.putVarInt32(self.status_.ByteSizePartial())
      self.status_.OutputPartial(out)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_service_name(d.getPrefixedString())
        continue
      if tt == 18:
        self.set_call(d.getPrefixedString())
        continue
      if tt == 26:
        length = d.getVarInt32()
        tmp = ProtocolBuffer.Decoder(d.buffer(), d.pos(), d.pos() + length)
        d.skip(length)
        self.mutable_status().TryMerge(tmp)
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_service_name_: res+=prefix+("service_name: %s\n" % self.DebugFormatString(self.service_name_))
    if self.has_call_: res+=prefix+("call: %s\n" % self.DebugFormatString(self.call_))
    if self.has_status_:
      res+=prefix+"status <\n"
      res+=self.status_.__str__(prefix + "  ", printElemNumber)
      res+=prefix+">\n"
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kservice_name = 1
  kcall = 2
  kstatus = 3

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "service_name",
    2: "call",
    3: "status",
  }, 3)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
    2: ProtocolBuffer.Encoder.STRING,
    3: ProtocolBuffer.Encoder.STRING,
  }, 3, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.SetCapabilityStatusRequest'
class SetCapabilityStatusResponse(ProtocolBuffer.ProtocolMessage):

  def __init__(self, contents=None):
    pass
    if contents is not None: self.MergeFromString(contents)


  def MergeFrom(self, x):
    assert x is not self

  def Equals(self, x):
    if x is self: return 1
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    return initialized

  def ByteSize(self):
    n = 0
    return n

  def ByteSizePartial(self):
    n = 0
    return n

  def Clear(self):
    pass

  def OutputUnchecked(self, out):
    pass

  def OutputPartial(self, out):
    pass

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])


  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
  }, 0)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
  }, 0, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.SetCapabilityStatusResponse'
if _extension_runtime:
  pass

__all__ = ['SetCapabilityStatusRequest','SetCapabilityStatusResponse']
