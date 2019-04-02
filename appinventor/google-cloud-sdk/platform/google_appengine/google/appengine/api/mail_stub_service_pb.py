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
from google.appengine.api.mail_service_pb import *
import google.appengine.api.mail_service_pb
google_dot_apphosting_dot_api_dot_mail__service__pb = __import__('google.appengine.api.mail_service_pb', {}, {}, [''])
class GetSentMessagesResponse(ProtocolBuffer.ProtocolMessage):

  def __init__(self, contents=None):
    self.sent_message_ = []
    if contents is not None: self.MergeFromString(contents)

  def sent_message_size(self): return len(self.sent_message_)
  def sent_message_list(self): return self.sent_message_

  def sent_message(self, i):
    return self.sent_message_[i]

  def mutable_sent_message(self, i):
    return self.sent_message_[i]

  def add_sent_message(self):
    x = MailMessage()
    self.sent_message_.append(x)
    return x

  def clear_sent_message(self):
    self.sent_message_ = []

  def MergeFrom(self, x):
    assert x is not self
    for i in range(x.sent_message_size()): self.add_sent_message().CopyFrom(x.sent_message(i))

  def Equals(self, x):
    if x is self: return 1
    if len(self.sent_message_) != len(x.sent_message_): return 0
    for e1, e2 in zip(self.sent_message_, x.sent_message_):
      if e1 != e2: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    for p in self.sent_message_:
      if not p.IsInitialized(debug_strs): initialized=0
    return initialized

  def ByteSize(self):
    n = 0
    n += 1 * len(self.sent_message_)
    for i in range(len(self.sent_message_)): n += self.lengthString(self.sent_message_[i].ByteSize())
    return n

  def ByteSizePartial(self):
    n = 0
    n += 1 * len(self.sent_message_)
    for i in range(len(self.sent_message_)): n += self.lengthString(self.sent_message_[i].ByteSizePartial())
    return n

  def Clear(self):
    self.clear_sent_message()

  def OutputUnchecked(self, out):
    for i in range(len(self.sent_message_)):
      out.putVarInt32(10)
      out.putVarInt32(self.sent_message_[i].ByteSize())
      self.sent_message_[i].OutputUnchecked(out)

  def OutputPartial(self, out):
    for i in range(len(self.sent_message_)):
      out.putVarInt32(10)
      out.putVarInt32(self.sent_message_[i].ByteSizePartial())
      self.sent_message_[i].OutputPartial(out)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        length = d.getVarInt32()
        tmp = ProtocolBuffer.Decoder(d.buffer(), d.pos(), d.pos() + length)
        d.skip(length)
        self.add_sent_message().TryMerge(tmp)
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    cnt=0
    for e in self.sent_message_:
      elm=""
      if printElemNumber: elm="(%d)" % cnt
      res+=prefix+("sent_message%s <\n" % elm)
      res+=e.__str__(prefix + "  ", printElemNumber)
      res+=prefix+">\n"
      cnt+=1
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  ksent_message = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "sent_message",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.GetSentMessagesResponse'
class ClearSentMessagesResponse(ProtocolBuffer.ProtocolMessage):
  has_messages_cleared_ = 0
  messages_cleared_ = 0

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def messages_cleared(self): return self.messages_cleared_

  def set_messages_cleared(self, x):
    self.has_messages_cleared_ = 1
    self.messages_cleared_ = x

  def clear_messages_cleared(self):
    if self.has_messages_cleared_:
      self.has_messages_cleared_ = 0
      self.messages_cleared_ = 0

  def has_messages_cleared(self): return self.has_messages_cleared_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_messages_cleared()): self.set_messages_cleared(x.messages_cleared())

  def Equals(self, x):
    if x is self: return 1
    if self.has_messages_cleared_ != x.has_messages_cleared_: return 0
    if self.has_messages_cleared_ and self.messages_cleared_ != x.messages_cleared_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    return initialized

  def ByteSize(self):
    n = 0
    if (self.has_messages_cleared_): n += 1 + self.lengthVarInt64(self.messages_cleared_)
    return n

  def ByteSizePartial(self):
    n = 0
    if (self.has_messages_cleared_): n += 1 + self.lengthVarInt64(self.messages_cleared_)
    return n

  def Clear(self):
    self.clear_messages_cleared()

  def OutputUnchecked(self, out):
    if (self.has_messages_cleared_):
      out.putVarInt32(8)
      out.putVarInt32(self.messages_cleared_)

  def OutputPartial(self, out):
    if (self.has_messages_cleared_):
      out.putVarInt32(8)
      out.putVarInt32(self.messages_cleared_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 8:
        self.set_messages_cleared(d.getVarInt32())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_messages_cleared_: res+=prefix+("messages_cleared: %s\n" % self.DebugFormatInt32(self.messages_cleared_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kmessages_cleared = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "messages_cleared",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.NUMERIC,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.ClearSentMessagesResponse'
class GetLogMailBodyResponse(ProtocolBuffer.ProtocolMessage):
  has_log_mail_body_ = 0
  log_mail_body_ = 0

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def log_mail_body(self): return self.log_mail_body_

  def set_log_mail_body(self, x):
    self.has_log_mail_body_ = 1
    self.log_mail_body_ = x

  def clear_log_mail_body(self):
    if self.has_log_mail_body_:
      self.has_log_mail_body_ = 0
      self.log_mail_body_ = 0

  def has_log_mail_body(self): return self.has_log_mail_body_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_log_mail_body()): self.set_log_mail_body(x.log_mail_body())

  def Equals(self, x):
    if x is self: return 1
    if self.has_log_mail_body_ != x.has_log_mail_body_: return 0
    if self.has_log_mail_body_ and self.log_mail_body_ != x.log_mail_body_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_log_mail_body_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: log_mail_body not set.')
    return initialized

  def ByteSize(self):
    n = 0
    return n + 2

  def ByteSizePartial(self):
    n = 0
    if (self.has_log_mail_body_):
      n += 2
    return n

  def Clear(self):
    self.clear_log_mail_body()

  def OutputUnchecked(self, out):
    out.putVarInt32(8)
    out.putBoolean(self.log_mail_body_)

  def OutputPartial(self, out):
    if (self.has_log_mail_body_):
      out.putVarInt32(8)
      out.putBoolean(self.log_mail_body_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 8:
        self.set_log_mail_body(d.getBoolean())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_log_mail_body_: res+=prefix+("log_mail_body: %s\n" % self.DebugFormatBool(self.log_mail_body_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  klog_mail_body = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "log_mail_body",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.NUMERIC,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.GetLogMailBodyResponse'
class SetLogMailBodyRequest(ProtocolBuffer.ProtocolMessage):
  has_log_mail_body_ = 0
  log_mail_body_ = 0

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def log_mail_body(self): return self.log_mail_body_

  def set_log_mail_body(self, x):
    self.has_log_mail_body_ = 1
    self.log_mail_body_ = x

  def clear_log_mail_body(self):
    if self.has_log_mail_body_:
      self.has_log_mail_body_ = 0
      self.log_mail_body_ = 0

  def has_log_mail_body(self): return self.has_log_mail_body_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_log_mail_body()): self.set_log_mail_body(x.log_mail_body())

  def Equals(self, x):
    if x is self: return 1
    if self.has_log_mail_body_ != x.has_log_mail_body_: return 0
    if self.has_log_mail_body_ and self.log_mail_body_ != x.log_mail_body_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_log_mail_body_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: log_mail_body not set.')
    return initialized

  def ByteSize(self):
    n = 0
    return n + 2

  def ByteSizePartial(self):
    n = 0
    if (self.has_log_mail_body_):
      n += 2
    return n

  def Clear(self):
    self.clear_log_mail_body()

  def OutputUnchecked(self, out):
    out.putVarInt32(8)
    out.putBoolean(self.log_mail_body_)

  def OutputPartial(self, out):
    if (self.has_log_mail_body_):
      out.putVarInt32(8)
      out.putBoolean(self.log_mail_body_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 8:
        self.set_log_mail_body(d.getBoolean())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_log_mail_body_: res+=prefix+("log_mail_body: %s\n" % self.DebugFormatBool(self.log_mail_body_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  klog_mail_body = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "log_mail_body",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.NUMERIC,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.SetLogMailBodyRequest'
class GetLogMailLevelResponse(ProtocolBuffer.ProtocolMessage):
  has_log_mail_level_ = 0
  log_mail_level_ = ""

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def log_mail_level(self): return self.log_mail_level_

  def set_log_mail_level(self, x):
    self.has_log_mail_level_ = 1
    self.log_mail_level_ = x

  def clear_log_mail_level(self):
    if self.has_log_mail_level_:
      self.has_log_mail_level_ = 0
      self.log_mail_level_ = ""

  def has_log_mail_level(self): return self.has_log_mail_level_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_log_mail_level()): self.set_log_mail_level(x.log_mail_level())

  def Equals(self, x):
    if x is self: return 1
    if self.has_log_mail_level_ != x.has_log_mail_level_: return 0
    if self.has_log_mail_level_ and self.log_mail_level_ != x.log_mail_level_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_log_mail_level_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: log_mail_level not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(len(self.log_mail_level_))
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_log_mail_level_):
      n += 1
      n += self.lengthString(len(self.log_mail_level_))
    return n

  def Clear(self):
    self.clear_log_mail_level()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putPrefixedString(self.log_mail_level_)

  def OutputPartial(self, out):
    if (self.has_log_mail_level_):
      out.putVarInt32(10)
      out.putPrefixedString(self.log_mail_level_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_log_mail_level(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_log_mail_level_: res+=prefix+("log_mail_level: %s\n" % self.DebugFormatString(self.log_mail_level_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  klog_mail_level = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "log_mail_level",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.GetLogMailLevelResponse'
class SetLogMailLevelRequest(ProtocolBuffer.ProtocolMessage):
  has_log_mail_level_ = 0
  log_mail_level_ = ""

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def log_mail_level(self): return self.log_mail_level_

  def set_log_mail_level(self, x):
    self.has_log_mail_level_ = 1
    self.log_mail_level_ = x

  def clear_log_mail_level(self):
    if self.has_log_mail_level_:
      self.has_log_mail_level_ = 0
      self.log_mail_level_ = ""

  def has_log_mail_level(self): return self.has_log_mail_level_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_log_mail_level()): self.set_log_mail_level(x.log_mail_level())

  def Equals(self, x):
    if x is self: return 1
    if self.has_log_mail_level_ != x.has_log_mail_level_: return 0
    if self.has_log_mail_level_ and self.log_mail_level_ != x.log_mail_level_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_log_mail_level_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: log_mail_level not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(len(self.log_mail_level_))
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_log_mail_level_):
      n += 1
      n += self.lengthString(len(self.log_mail_level_))
    return n

  def Clear(self):
    self.clear_log_mail_level()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putPrefixedString(self.log_mail_level_)

  def OutputPartial(self, out):
    if (self.has_log_mail_level_):
      out.putVarInt32(10)
      out.putPrefixedString(self.log_mail_level_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_log_mail_level(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_log_mail_level_: res+=prefix+("log_mail_level: %s\n" % self.DebugFormatString(self.log_mail_level_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  klog_mail_level = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "log_mail_level",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.SetLogMailLevelRequest'
if _extension_runtime:
  pass

__all__ = ['GetSentMessagesResponse','ClearSentMessagesResponse','GetLogMailBodyResponse','SetLogMailBodyRequest','GetLogMailLevelResponse','SetLogMailLevelRequest']
