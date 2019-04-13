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
class SetMaxSizeRequest(ProtocolBuffer.ProtocolMessage):
  has_max_size_bytes_ = 0
  max_size_bytes_ = 0

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def max_size_bytes(self): return self.max_size_bytes_

  def set_max_size_bytes(self, x):
    self.has_max_size_bytes_ = 1
    self.max_size_bytes_ = x

  def clear_max_size_bytes(self):
    if self.has_max_size_bytes_:
      self.has_max_size_bytes_ = 0
      self.max_size_bytes_ = 0

  def has_max_size_bytes(self): return self.has_max_size_bytes_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_max_size_bytes()): self.set_max_size_bytes(x.max_size_bytes())

  def Equals(self, x):
    if x is self: return 1
    if self.has_max_size_bytes_ != x.has_max_size_bytes_: return 0
    if self.has_max_size_bytes_ and self.max_size_bytes_ != x.max_size_bytes_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_max_size_bytes_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: max_size_bytes not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthVarInt64(self.max_size_bytes_)
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_max_size_bytes_):
      n += 1
      n += self.lengthVarInt64(self.max_size_bytes_)
    return n

  def Clear(self):
    self.clear_max_size_bytes()

  def OutputUnchecked(self, out):
    out.putVarInt32(8)
    out.putVarInt64(self.max_size_bytes_)

  def OutputPartial(self, out):
    if (self.has_max_size_bytes_):
      out.putVarInt32(8)
      out.putVarInt64(self.max_size_bytes_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 8:
        self.set_max_size_bytes(d.getVarInt64())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_max_size_bytes_: res+=prefix+("max_size_bytes: %s\n" % self.DebugFormatInt64(self.max_size_bytes_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kmax_size_bytes = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "max_size_bytes",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.NUMERIC,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.SetMaxSizeRequest'
class GetLruChainLengthResponse(ProtocolBuffer.ProtocolMessage):
  has_chain_length_ = 0
  chain_length_ = 0

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def chain_length(self): return self.chain_length_

  def set_chain_length(self, x):
    self.has_chain_length_ = 1
    self.chain_length_ = x

  def clear_chain_length(self):
    if self.has_chain_length_:
      self.has_chain_length_ = 0
      self.chain_length_ = 0

  def has_chain_length(self): return self.has_chain_length_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_chain_length()): self.set_chain_length(x.chain_length())

  def Equals(self, x):
    if x is self: return 1
    if self.has_chain_length_ != x.has_chain_length_: return 0
    if self.has_chain_length_ and self.chain_length_ != x.chain_length_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_chain_length_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: chain_length not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthVarInt64(self.chain_length_)
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_chain_length_):
      n += 1
      n += self.lengthVarInt64(self.chain_length_)
    return n

  def Clear(self):
    self.clear_chain_length()

  def OutputUnchecked(self, out):
    out.putVarInt32(8)
    out.putVarInt64(self.chain_length_)

  def OutputPartial(self, out):
    if (self.has_chain_length_):
      out.putVarInt32(8)
      out.putVarInt64(self.chain_length_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 8:
        self.set_chain_length(d.getVarInt64())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_chain_length_: res+=prefix+("chain_length: %s\n" % self.DebugFormatInt64(self.chain_length_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kchain_length = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "chain_length",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.NUMERIC,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.GetLruChainLengthResponse'
class SetClockRequest(ProtocolBuffer.ProtocolMessage):
  has_clock_time_milliseconds_ = 0
  clock_time_milliseconds_ = 0

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def clock_time_milliseconds(self): return self.clock_time_milliseconds_

  def set_clock_time_milliseconds(self, x):
    self.has_clock_time_milliseconds_ = 1
    self.clock_time_milliseconds_ = x

  def clear_clock_time_milliseconds(self):
    if self.has_clock_time_milliseconds_:
      self.has_clock_time_milliseconds_ = 0
      self.clock_time_milliseconds_ = 0

  def has_clock_time_milliseconds(self): return self.has_clock_time_milliseconds_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_clock_time_milliseconds()): self.set_clock_time_milliseconds(x.clock_time_milliseconds())

  def Equals(self, x):
    if x is self: return 1
    if self.has_clock_time_milliseconds_ != x.has_clock_time_milliseconds_: return 0
    if self.has_clock_time_milliseconds_ and self.clock_time_milliseconds_ != x.clock_time_milliseconds_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_clock_time_milliseconds_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: clock_time_milliseconds not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthVarInt64(self.clock_time_milliseconds_)
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_clock_time_milliseconds_):
      n += 1
      n += self.lengthVarInt64(self.clock_time_milliseconds_)
    return n

  def Clear(self):
    self.clear_clock_time_milliseconds()

  def OutputUnchecked(self, out):
    out.putVarInt32(8)
    out.putVarInt64(self.clock_time_milliseconds_)

  def OutputPartial(self, out):
    if (self.has_clock_time_milliseconds_):
      out.putVarInt32(8)
      out.putVarInt64(self.clock_time_milliseconds_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 8:
        self.set_clock_time_milliseconds(d.getVarInt64())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_clock_time_milliseconds_: res+=prefix+("clock_time_milliseconds: %s\n" % self.DebugFormatInt64(self.clock_time_milliseconds_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kclock_time_milliseconds = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "clock_time_milliseconds",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.NUMERIC,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.SetClockRequest'
class AdvanceClockRequest(ProtocolBuffer.ProtocolMessage):
  has_milliseconds_ = 0
  milliseconds_ = 0

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def milliseconds(self): return self.milliseconds_

  def set_milliseconds(self, x):
    self.has_milliseconds_ = 1
    self.milliseconds_ = x

  def clear_milliseconds(self):
    if self.has_milliseconds_:
      self.has_milliseconds_ = 0
      self.milliseconds_ = 0

  def has_milliseconds(self): return self.has_milliseconds_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_milliseconds()): self.set_milliseconds(x.milliseconds())

  def Equals(self, x):
    if x is self: return 1
    if self.has_milliseconds_ != x.has_milliseconds_: return 0
    if self.has_milliseconds_ and self.milliseconds_ != x.milliseconds_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_milliseconds_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: milliseconds not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthVarInt64(self.milliseconds_)
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_milliseconds_):
      n += 1
      n += self.lengthVarInt64(self.milliseconds_)
    return n

  def Clear(self):
    self.clear_milliseconds()

  def OutputUnchecked(self, out):
    out.putVarInt32(8)
    out.putVarInt64(self.milliseconds_)

  def OutputPartial(self, out):
    if (self.has_milliseconds_):
      out.putVarInt32(8)
      out.putVarInt64(self.milliseconds_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 8:
        self.set_milliseconds(d.getVarInt64())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_milliseconds_: res+=prefix+("milliseconds: %s\n" % self.DebugFormatInt64(self.milliseconds_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kmilliseconds = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "milliseconds",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.NUMERIC,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.AdvanceClockRequest'
class AdvanceClockResponse(ProtocolBuffer.ProtocolMessage):
  has_clock_time_milliseconds_ = 0
  clock_time_milliseconds_ = 0

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def clock_time_milliseconds(self): return self.clock_time_milliseconds_

  def set_clock_time_milliseconds(self, x):
    self.has_clock_time_milliseconds_ = 1
    self.clock_time_milliseconds_ = x

  def clear_clock_time_milliseconds(self):
    if self.has_clock_time_milliseconds_:
      self.has_clock_time_milliseconds_ = 0
      self.clock_time_milliseconds_ = 0

  def has_clock_time_milliseconds(self): return self.has_clock_time_milliseconds_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_clock_time_milliseconds()): self.set_clock_time_milliseconds(x.clock_time_milliseconds())

  def Equals(self, x):
    if x is self: return 1
    if self.has_clock_time_milliseconds_ != x.has_clock_time_milliseconds_: return 0
    if self.has_clock_time_milliseconds_ and self.clock_time_milliseconds_ != x.clock_time_milliseconds_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_clock_time_milliseconds_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: clock_time_milliseconds not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthVarInt64(self.clock_time_milliseconds_)
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_clock_time_milliseconds_):
      n += 1
      n += self.lengthVarInt64(self.clock_time_milliseconds_)
    return n

  def Clear(self):
    self.clear_clock_time_milliseconds()

  def OutputUnchecked(self, out):
    out.putVarInt32(8)
    out.putVarInt64(self.clock_time_milliseconds_)

  def OutputPartial(self, out):
    if (self.has_clock_time_milliseconds_):
      out.putVarInt32(8)
      out.putVarInt64(self.clock_time_milliseconds_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 8:
        self.set_clock_time_milliseconds(d.getVarInt64())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_clock_time_milliseconds_: res+=prefix+("clock_time_milliseconds: %s\n" % self.DebugFormatInt64(self.clock_time_milliseconds_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kclock_time_milliseconds = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "clock_time_milliseconds",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.NUMERIC,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.AdvanceClockResponse'
if _extension_runtime:
  pass

__all__ = ['SetMaxSizeRequest','GetLruChainLengthResponse','SetClockRequest','AdvanceClockRequest','AdvanceClockResponse']
