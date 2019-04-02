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
from google.appengine.api.taskqueue.taskqueue_service_pb import *
import google.appengine.api.taskqueue.taskqueue_service_pb
google_dot_apphosting_dot_api_dot_taskqueue_dot_taskqueue__service__pb = __import__('google.appengine.api.taskqueue.taskqueue_service_pb', {}, {}, [''])
class GetQueuesResponse(ProtocolBuffer.ProtocolMessage):
  has_fetch_queues_response_ = 0
  has_fetch_queue_stats_response_ = 0

  def __init__(self, contents=None):
    self.fetch_queues_response_ = TaskQueueFetchQueuesResponse()
    self.fetch_queue_stats_response_ = TaskQueueFetchQueueStatsResponse()
    if contents is not None: self.MergeFromString(contents)

  def fetch_queues_response(self): return self.fetch_queues_response_

  def mutable_fetch_queues_response(self): self.has_fetch_queues_response_ = 1; return self.fetch_queues_response_

  def clear_fetch_queues_response(self):self.has_fetch_queues_response_ = 0; self.fetch_queues_response_.Clear()

  def has_fetch_queues_response(self): return self.has_fetch_queues_response_

  def fetch_queue_stats_response(self): return self.fetch_queue_stats_response_

  def mutable_fetch_queue_stats_response(self): self.has_fetch_queue_stats_response_ = 1; return self.fetch_queue_stats_response_

  def clear_fetch_queue_stats_response(self):self.has_fetch_queue_stats_response_ = 0; self.fetch_queue_stats_response_.Clear()

  def has_fetch_queue_stats_response(self): return self.has_fetch_queue_stats_response_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_fetch_queues_response()): self.mutable_fetch_queues_response().MergeFrom(x.fetch_queues_response())
    if (x.has_fetch_queue_stats_response()): self.mutable_fetch_queue_stats_response().MergeFrom(x.fetch_queue_stats_response())

  def Equals(self, x):
    if x is self: return 1
    if self.has_fetch_queues_response_ != x.has_fetch_queues_response_: return 0
    if self.has_fetch_queues_response_ and self.fetch_queues_response_ != x.fetch_queues_response_: return 0
    if self.has_fetch_queue_stats_response_ != x.has_fetch_queue_stats_response_: return 0
    if self.has_fetch_queue_stats_response_ and self.fetch_queue_stats_response_ != x.fetch_queue_stats_response_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_fetch_queues_response_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: fetch_queues_response not set.')
    elif not self.fetch_queues_response_.IsInitialized(debug_strs): initialized = 0
    if (not self.has_fetch_queue_stats_response_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: fetch_queue_stats_response not set.')
    elif not self.fetch_queue_stats_response_.IsInitialized(debug_strs): initialized = 0
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(self.fetch_queues_response_.ByteSize())
    n += self.lengthString(self.fetch_queue_stats_response_.ByteSize())
    return n + 2

  def ByteSizePartial(self):
    n = 0
    if (self.has_fetch_queues_response_):
      n += 1
      n += self.lengthString(self.fetch_queues_response_.ByteSizePartial())
    if (self.has_fetch_queue_stats_response_):
      n += 1
      n += self.lengthString(self.fetch_queue_stats_response_.ByteSizePartial())
    return n

  def Clear(self):
    self.clear_fetch_queues_response()
    self.clear_fetch_queue_stats_response()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putVarInt32(self.fetch_queues_response_.ByteSize())
    self.fetch_queues_response_.OutputUnchecked(out)
    out.putVarInt32(18)
    out.putVarInt32(self.fetch_queue_stats_response_.ByteSize())
    self.fetch_queue_stats_response_.OutputUnchecked(out)

  def OutputPartial(self, out):
    if (self.has_fetch_queues_response_):
      out.putVarInt32(10)
      out.putVarInt32(self.fetch_queues_response_.ByteSizePartial())
      self.fetch_queues_response_.OutputPartial(out)
    if (self.has_fetch_queue_stats_response_):
      out.putVarInt32(18)
      out.putVarInt32(self.fetch_queue_stats_response_.ByteSizePartial())
      self.fetch_queue_stats_response_.OutputPartial(out)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        length = d.getVarInt32()
        tmp = ProtocolBuffer.Decoder(d.buffer(), d.pos(), d.pos() + length)
        d.skip(length)
        self.mutable_fetch_queues_response().TryMerge(tmp)
        continue
      if tt == 18:
        length = d.getVarInt32()
        tmp = ProtocolBuffer.Decoder(d.buffer(), d.pos(), d.pos() + length)
        d.skip(length)
        self.mutable_fetch_queue_stats_response().TryMerge(tmp)
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_fetch_queues_response_:
      res+=prefix+"fetch_queues_response <\n"
      res+=self.fetch_queues_response_.__str__(prefix + "  ", printElemNumber)
      res+=prefix+">\n"
    if self.has_fetch_queue_stats_response_:
      res+=prefix+"fetch_queue_stats_response <\n"
      res+=self.fetch_queue_stats_response_.__str__(prefix + "  ", printElemNumber)
      res+=prefix+">\n"
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kfetch_queues_response = 1
  kfetch_queue_stats_response = 2

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "fetch_queues_response",
    2: "fetch_queue_stats_response",
  }, 2)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
    2: ProtocolBuffer.Encoder.STRING,
  }, 2, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.GetQueuesResponse'
class GetFilteredTasksResponse(ProtocolBuffer.ProtocolMessage):
  has_query_tasks_response_ = 0
  query_tasks_response_ = None

  def __init__(self, contents=None):
    self.eta_delta_ = []
    self.lazy_init_lock_ = _Lock()
    if contents is not None: self.MergeFromString(contents)

  def query_tasks_response(self):
    if self.query_tasks_response_ is None:
      self.lazy_init_lock_.acquire()
      try:
        if self.query_tasks_response_ is None: self.query_tasks_response_ = TaskQueueQueryTasksResponse()
      finally:
        self.lazy_init_lock_.release()
    return self.query_tasks_response_

  def mutable_query_tasks_response(self): self.has_query_tasks_response_ = 1; return self.query_tasks_response()

  def clear_query_tasks_response(self):

    if self.has_query_tasks_response_:
      self.has_query_tasks_response_ = 0;
      if self.query_tasks_response_ is not None: self.query_tasks_response_.Clear()

  def has_query_tasks_response(self): return self.has_query_tasks_response_

  def eta_delta_size(self): return len(self.eta_delta_)
  def eta_delta_list(self): return self.eta_delta_

  def eta_delta(self, i):
    return self.eta_delta_[i]

  def set_eta_delta(self, i, x):
    self.eta_delta_[i] = x

  def add_eta_delta(self, x):
    self.eta_delta_.append(x)

  def clear_eta_delta(self):
    self.eta_delta_ = []


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_query_tasks_response()): self.mutable_query_tasks_response().MergeFrom(x.query_tasks_response())
    for i in range(x.eta_delta_size()): self.add_eta_delta(x.eta_delta(i))

  def Equals(self, x):
    if x is self: return 1
    if self.has_query_tasks_response_ != x.has_query_tasks_response_: return 0
    if self.has_query_tasks_response_ and self.query_tasks_response_ != x.query_tasks_response_: return 0
    if len(self.eta_delta_) != len(x.eta_delta_): return 0
    for e1, e2 in zip(self.eta_delta_, x.eta_delta_):
      if e1 != e2: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (self.has_query_tasks_response_ and not self.query_tasks_response_.IsInitialized(debug_strs)): initialized = 0
    return initialized

  def ByteSize(self):
    n = 0
    if (self.has_query_tasks_response_): n += 1 + self.lengthString(self.query_tasks_response_.ByteSize())
    n += 1 * len(self.eta_delta_)
    for i in range(len(self.eta_delta_)): n += self.lengthString(len(self.eta_delta_[i]))
    return n

  def ByteSizePartial(self):
    n = 0
    if (self.has_query_tasks_response_): n += 1 + self.lengthString(self.query_tasks_response_.ByteSizePartial())
    n += 1 * len(self.eta_delta_)
    for i in range(len(self.eta_delta_)): n += self.lengthString(len(self.eta_delta_[i]))
    return n

  def Clear(self):
    self.clear_query_tasks_response()
    self.clear_eta_delta()

  def OutputUnchecked(self, out):
    if (self.has_query_tasks_response_):
      out.putVarInt32(10)
      out.putVarInt32(self.query_tasks_response_.ByteSize())
      self.query_tasks_response_.OutputUnchecked(out)
    for i in range(len(self.eta_delta_)):
      out.putVarInt32(18)
      out.putPrefixedString(self.eta_delta_[i])

  def OutputPartial(self, out):
    if (self.has_query_tasks_response_):
      out.putVarInt32(10)
      out.putVarInt32(self.query_tasks_response_.ByteSizePartial())
      self.query_tasks_response_.OutputPartial(out)
    for i in range(len(self.eta_delta_)):
      out.putVarInt32(18)
      out.putPrefixedString(self.eta_delta_[i])

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        length = d.getVarInt32()
        tmp = ProtocolBuffer.Decoder(d.buffer(), d.pos(), d.pos() + length)
        d.skip(length)
        self.mutable_query_tasks_response().TryMerge(tmp)
        continue
      if tt == 18:
        self.add_eta_delta(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_query_tasks_response_:
      res+=prefix+"query_tasks_response <\n"
      res+=self.query_tasks_response_.__str__(prefix + "  ", printElemNumber)
      res+=prefix+">\n"
    cnt=0
    for e in self.eta_delta_:
      elm=""
      if printElemNumber: elm="(%d)" % cnt
      res+=prefix+("eta_delta%s: %s\n" % (elm, self.DebugFormatString(e)))
      cnt+=1
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kquery_tasks_response = 1
  keta_delta = 2

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "query_tasks_response",
    2: "eta_delta",
  }, 2)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
    2: ProtocolBuffer.Encoder.STRING,
  }, 2, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.GetFilteredTasksResponse'
class FlushQueueRequest(ProtocolBuffer.ProtocolMessage):
  has_queue_name_ = 0
  queue_name_ = ""

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def queue_name(self): return self.queue_name_

  def set_queue_name(self, x):
    self.has_queue_name_ = 1
    self.queue_name_ = x

  def clear_queue_name(self):
    if self.has_queue_name_:
      self.has_queue_name_ = 0
      self.queue_name_ = ""

  def has_queue_name(self): return self.has_queue_name_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_queue_name()): self.set_queue_name(x.queue_name())

  def Equals(self, x):
    if x is self: return 1
    if self.has_queue_name_ != x.has_queue_name_: return 0
    if self.has_queue_name_ and self.queue_name_ != x.queue_name_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_queue_name_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: queue_name not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(len(self.queue_name_))
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_queue_name_):
      n += 1
      n += self.lengthString(len(self.queue_name_))
    return n

  def Clear(self):
    self.clear_queue_name()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putPrefixedString(self.queue_name_)

  def OutputPartial(self, out):
    if (self.has_queue_name_):
      out.putVarInt32(10)
      out.putPrefixedString(self.queue_name_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_queue_name(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_queue_name_: res+=prefix+("queue_name: %s\n" % self.DebugFormatString(self.queue_name_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kqueue_name = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "queue_name",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.FlushQueueRequest'
class GetFilteredTasksRequest(ProtocolBuffer.ProtocolMessage):
  has_url_ = 0
  url_ = ""
  has_name_ = 0
  name_ = ""

  def __init__(self, contents=None):
    self.queue_names_ = []
    if contents is not None: self.MergeFromString(contents)

  def url(self): return self.url_

  def set_url(self, x):
    self.has_url_ = 1
    self.url_ = x

  def clear_url(self):
    if self.has_url_:
      self.has_url_ = 0
      self.url_ = ""

  def has_url(self): return self.has_url_

  def name(self): return self.name_

  def set_name(self, x):
    self.has_name_ = 1
    self.name_ = x

  def clear_name(self):
    if self.has_name_:
      self.has_name_ = 0
      self.name_ = ""

  def has_name(self): return self.has_name_

  def queue_names_size(self): return len(self.queue_names_)
  def queue_names_list(self): return self.queue_names_

  def queue_names(self, i):
    return self.queue_names_[i]

  def set_queue_names(self, i, x):
    self.queue_names_[i] = x

  def add_queue_names(self, x):
    self.queue_names_.append(x)

  def clear_queue_names(self):
    self.queue_names_ = []


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_url()): self.set_url(x.url())
    if (x.has_name()): self.set_name(x.name())
    for i in range(x.queue_names_size()): self.add_queue_names(x.queue_names(i))

  def Equals(self, x):
    if x is self: return 1
    if self.has_url_ != x.has_url_: return 0
    if self.has_url_ and self.url_ != x.url_: return 0
    if self.has_name_ != x.has_name_: return 0
    if self.has_name_ and self.name_ != x.name_: return 0
    if len(self.queue_names_) != len(x.queue_names_): return 0
    for e1, e2 in zip(self.queue_names_, x.queue_names_):
      if e1 != e2: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    return initialized

  def ByteSize(self):
    n = 0
    if (self.has_url_): n += 1 + self.lengthString(len(self.url_))
    if (self.has_name_): n += 1 + self.lengthString(len(self.name_))
    n += 1 * len(self.queue_names_)
    for i in range(len(self.queue_names_)): n += self.lengthString(len(self.queue_names_[i]))
    return n

  def ByteSizePartial(self):
    n = 0
    if (self.has_url_): n += 1 + self.lengthString(len(self.url_))
    if (self.has_name_): n += 1 + self.lengthString(len(self.name_))
    n += 1 * len(self.queue_names_)
    for i in range(len(self.queue_names_)): n += self.lengthString(len(self.queue_names_[i]))
    return n

  def Clear(self):
    self.clear_url()
    self.clear_name()
    self.clear_queue_names()

  def OutputUnchecked(self, out):
    if (self.has_url_):
      out.putVarInt32(10)
      out.putPrefixedString(self.url_)
    if (self.has_name_):
      out.putVarInt32(18)
      out.putPrefixedString(self.name_)
    for i in range(len(self.queue_names_)):
      out.putVarInt32(26)
      out.putPrefixedString(self.queue_names_[i])

  def OutputPartial(self, out):
    if (self.has_url_):
      out.putVarInt32(10)
      out.putPrefixedString(self.url_)
    if (self.has_name_):
      out.putVarInt32(18)
      out.putPrefixedString(self.name_)
    for i in range(len(self.queue_names_)):
      out.putVarInt32(26)
      out.putPrefixedString(self.queue_names_[i])

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_url(d.getPrefixedString())
        continue
      if tt == 18:
        self.set_name(d.getPrefixedString())
        continue
      if tt == 26:
        self.add_queue_names(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_url_: res+=prefix+("url: %s\n" % self.DebugFormatString(self.url_))
    if self.has_name_: res+=prefix+("name: %s\n" % self.DebugFormatString(self.name_))
    cnt=0
    for e in self.queue_names_:
      elm=""
      if printElemNumber: elm="(%d)" % cnt
      res+=prefix+("queue_names%s: %s\n" % (elm, self.DebugFormatString(e)))
      cnt+=1
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kurl = 1
  kname = 2
  kqueue_names = 3

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "url",
    2: "name",
    3: "queue_names",
  }, 3)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
    2: ProtocolBuffer.Encoder.STRING,
    3: ProtocolBuffer.Encoder.STRING,
  }, 3, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.GetFilteredTasksRequest'
class PatchQueueYamlParserRequest(ProtocolBuffer.ProtocolMessage):
  has_patched_return_value_ = 0
  patched_return_value_ = ""

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def patched_return_value(self): return self.patched_return_value_

  def set_patched_return_value(self, x):
    self.has_patched_return_value_ = 1
    self.patched_return_value_ = x

  def clear_patched_return_value(self):
    if self.has_patched_return_value_:
      self.has_patched_return_value_ = 0
      self.patched_return_value_ = ""

  def has_patched_return_value(self): return self.has_patched_return_value_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_patched_return_value()): self.set_patched_return_value(x.patched_return_value())

  def Equals(self, x):
    if x is self: return 1
    if self.has_patched_return_value_ != x.has_patched_return_value_: return 0
    if self.has_patched_return_value_ and self.patched_return_value_ != x.patched_return_value_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_patched_return_value_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: patched_return_value not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(len(self.patched_return_value_))
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_patched_return_value_):
      n += 1
      n += self.lengthString(len(self.patched_return_value_))
    return n

  def Clear(self):
    self.clear_patched_return_value()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putPrefixedString(self.patched_return_value_)

  def OutputPartial(self, out):
    if (self.has_patched_return_value_):
      out.putVarInt32(10)
      out.putPrefixedString(self.patched_return_value_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_patched_return_value(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_patched_return_value_: res+=prefix+("patched_return_value: %s\n" % self.DebugFormatString(self.patched_return_value_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kpatched_return_value = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "patched_return_value",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.PatchQueueYamlParserRequest'
class SetUpStubRequest(ProtocolBuffer.ProtocolMessage):
  has_service_name_ = 0
  service_name_ = ""
  has_root_path_ = 0
  root_path_ = ""
  has_auto_task_running_ = 0
  auto_task_running_ = 0
  has_task_retry_seconds_ = 0
  task_retry_seconds_ = 0
  has_all_queues_valid_ = 0
  all_queues_valid_ = 0
  has_default_http_server_ = 0
  default_http_server_ = ""
  has_testing_validate_state_ = 0
  testing_validate_state_ = 0
  has_request_data_ = 0
  request_data_ = ""
  has_gettime_ = 0
  gettime_ = ""

  def __init__(self, contents=None):
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

  def root_path(self): return self.root_path_

  def set_root_path(self, x):
    self.has_root_path_ = 1
    self.root_path_ = x

  def clear_root_path(self):
    if self.has_root_path_:
      self.has_root_path_ = 0
      self.root_path_ = ""

  def has_root_path(self): return self.has_root_path_

  def auto_task_running(self): return self.auto_task_running_

  def set_auto_task_running(self, x):
    self.has_auto_task_running_ = 1
    self.auto_task_running_ = x

  def clear_auto_task_running(self):
    if self.has_auto_task_running_:
      self.has_auto_task_running_ = 0
      self.auto_task_running_ = 0

  def has_auto_task_running(self): return self.has_auto_task_running_

  def task_retry_seconds(self): return self.task_retry_seconds_

  def set_task_retry_seconds(self, x):
    self.has_task_retry_seconds_ = 1
    self.task_retry_seconds_ = x

  def clear_task_retry_seconds(self):
    if self.has_task_retry_seconds_:
      self.has_task_retry_seconds_ = 0
      self.task_retry_seconds_ = 0

  def has_task_retry_seconds(self): return self.has_task_retry_seconds_

  def all_queues_valid(self): return self.all_queues_valid_

  def set_all_queues_valid(self, x):
    self.has_all_queues_valid_ = 1
    self.all_queues_valid_ = x

  def clear_all_queues_valid(self):
    if self.has_all_queues_valid_:
      self.has_all_queues_valid_ = 0
      self.all_queues_valid_ = 0

  def has_all_queues_valid(self): return self.has_all_queues_valid_

  def default_http_server(self): return self.default_http_server_

  def set_default_http_server(self, x):
    self.has_default_http_server_ = 1
    self.default_http_server_ = x

  def clear_default_http_server(self):
    if self.has_default_http_server_:
      self.has_default_http_server_ = 0
      self.default_http_server_ = ""

  def has_default_http_server(self): return self.has_default_http_server_

  def testing_validate_state(self): return self.testing_validate_state_

  def set_testing_validate_state(self, x):
    self.has_testing_validate_state_ = 1
    self.testing_validate_state_ = x

  def clear_testing_validate_state(self):
    if self.has_testing_validate_state_:
      self.has_testing_validate_state_ = 0
      self.testing_validate_state_ = 0

  def has_testing_validate_state(self): return self.has_testing_validate_state_

  def request_data(self): return self.request_data_

  def set_request_data(self, x):
    self.has_request_data_ = 1
    self.request_data_ = x

  def clear_request_data(self):
    if self.has_request_data_:
      self.has_request_data_ = 0
      self.request_data_ = ""

  def has_request_data(self): return self.has_request_data_

  def gettime(self): return self.gettime_

  def set_gettime(self, x):
    self.has_gettime_ = 1
    self.gettime_ = x

  def clear_gettime(self):
    if self.has_gettime_:
      self.has_gettime_ = 0
      self.gettime_ = ""

  def has_gettime(self): return self.has_gettime_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_service_name()): self.set_service_name(x.service_name())
    if (x.has_root_path()): self.set_root_path(x.root_path())
    if (x.has_auto_task_running()): self.set_auto_task_running(x.auto_task_running())
    if (x.has_task_retry_seconds()): self.set_task_retry_seconds(x.task_retry_seconds())
    if (x.has_all_queues_valid()): self.set_all_queues_valid(x.all_queues_valid())
    if (x.has_default_http_server()): self.set_default_http_server(x.default_http_server())
    if (x.has_testing_validate_state()): self.set_testing_validate_state(x.testing_validate_state())
    if (x.has_request_data()): self.set_request_data(x.request_data())
    if (x.has_gettime()): self.set_gettime(x.gettime())

  def Equals(self, x):
    if x is self: return 1
    if self.has_service_name_ != x.has_service_name_: return 0
    if self.has_service_name_ and self.service_name_ != x.service_name_: return 0
    if self.has_root_path_ != x.has_root_path_: return 0
    if self.has_root_path_ and self.root_path_ != x.root_path_: return 0
    if self.has_auto_task_running_ != x.has_auto_task_running_: return 0
    if self.has_auto_task_running_ and self.auto_task_running_ != x.auto_task_running_: return 0
    if self.has_task_retry_seconds_ != x.has_task_retry_seconds_: return 0
    if self.has_task_retry_seconds_ and self.task_retry_seconds_ != x.task_retry_seconds_: return 0
    if self.has_all_queues_valid_ != x.has_all_queues_valid_: return 0
    if self.has_all_queues_valid_ and self.all_queues_valid_ != x.all_queues_valid_: return 0
    if self.has_default_http_server_ != x.has_default_http_server_: return 0
    if self.has_default_http_server_ and self.default_http_server_ != x.default_http_server_: return 0
    if self.has_testing_validate_state_ != x.has_testing_validate_state_: return 0
    if self.has_testing_validate_state_ and self.testing_validate_state_ != x.testing_validate_state_: return 0
    if self.has_request_data_ != x.has_request_data_: return 0
    if self.has_request_data_ and self.request_data_ != x.request_data_: return 0
    if self.has_gettime_ != x.has_gettime_: return 0
    if self.has_gettime_ and self.gettime_ != x.gettime_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    return initialized

  def ByteSize(self):
    n = 0
    if (self.has_service_name_): n += 1 + self.lengthString(len(self.service_name_))
    if (self.has_root_path_): n += 1 + self.lengthString(len(self.root_path_))
    if (self.has_auto_task_running_): n += 2
    if (self.has_task_retry_seconds_): n += 1 + self.lengthVarInt64(self.task_retry_seconds_)
    if (self.has_all_queues_valid_): n += 2
    if (self.has_default_http_server_): n += 1 + self.lengthString(len(self.default_http_server_))
    if (self.has_testing_validate_state_): n += 2
    if (self.has_request_data_): n += 1 + self.lengthString(len(self.request_data_))
    if (self.has_gettime_): n += 1 + self.lengthString(len(self.gettime_))
    return n

  def ByteSizePartial(self):
    n = 0
    if (self.has_service_name_): n += 1 + self.lengthString(len(self.service_name_))
    if (self.has_root_path_): n += 1 + self.lengthString(len(self.root_path_))
    if (self.has_auto_task_running_): n += 2
    if (self.has_task_retry_seconds_): n += 1 + self.lengthVarInt64(self.task_retry_seconds_)
    if (self.has_all_queues_valid_): n += 2
    if (self.has_default_http_server_): n += 1 + self.lengthString(len(self.default_http_server_))
    if (self.has_testing_validate_state_): n += 2
    if (self.has_request_data_): n += 1 + self.lengthString(len(self.request_data_))
    if (self.has_gettime_): n += 1 + self.lengthString(len(self.gettime_))
    return n

  def Clear(self):
    self.clear_service_name()
    self.clear_root_path()
    self.clear_auto_task_running()
    self.clear_task_retry_seconds()
    self.clear_all_queues_valid()
    self.clear_default_http_server()
    self.clear_testing_validate_state()
    self.clear_request_data()
    self.clear_gettime()

  def OutputUnchecked(self, out):
    if (self.has_service_name_):
      out.putVarInt32(10)
      out.putPrefixedString(self.service_name_)
    if (self.has_root_path_):
      out.putVarInt32(18)
      out.putPrefixedString(self.root_path_)
    if (self.has_auto_task_running_):
      out.putVarInt32(24)
      out.putBoolean(self.auto_task_running_)
    if (self.has_task_retry_seconds_):
      out.putVarInt32(32)
      out.putVarInt32(self.task_retry_seconds_)
    if (self.has_all_queues_valid_):
      out.putVarInt32(40)
      out.putBoolean(self.all_queues_valid_)
    if (self.has_default_http_server_):
      out.putVarInt32(50)
      out.putPrefixedString(self.default_http_server_)
    if (self.has_testing_validate_state_):
      out.putVarInt32(56)
      out.putBoolean(self.testing_validate_state_)
    if (self.has_request_data_):
      out.putVarInt32(66)
      out.putPrefixedString(self.request_data_)
    if (self.has_gettime_):
      out.putVarInt32(74)
      out.putPrefixedString(self.gettime_)

  def OutputPartial(self, out):
    if (self.has_service_name_):
      out.putVarInt32(10)
      out.putPrefixedString(self.service_name_)
    if (self.has_root_path_):
      out.putVarInt32(18)
      out.putPrefixedString(self.root_path_)
    if (self.has_auto_task_running_):
      out.putVarInt32(24)
      out.putBoolean(self.auto_task_running_)
    if (self.has_task_retry_seconds_):
      out.putVarInt32(32)
      out.putVarInt32(self.task_retry_seconds_)
    if (self.has_all_queues_valid_):
      out.putVarInt32(40)
      out.putBoolean(self.all_queues_valid_)
    if (self.has_default_http_server_):
      out.putVarInt32(50)
      out.putPrefixedString(self.default_http_server_)
    if (self.has_testing_validate_state_):
      out.putVarInt32(56)
      out.putBoolean(self.testing_validate_state_)
    if (self.has_request_data_):
      out.putVarInt32(66)
      out.putPrefixedString(self.request_data_)
    if (self.has_gettime_):
      out.putVarInt32(74)
      out.putPrefixedString(self.gettime_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_service_name(d.getPrefixedString())
        continue
      if tt == 18:
        self.set_root_path(d.getPrefixedString())
        continue
      if tt == 24:
        self.set_auto_task_running(d.getBoolean())
        continue
      if tt == 32:
        self.set_task_retry_seconds(d.getVarInt32())
        continue
      if tt == 40:
        self.set_all_queues_valid(d.getBoolean())
        continue
      if tt == 50:
        self.set_default_http_server(d.getPrefixedString())
        continue
      if tt == 56:
        self.set_testing_validate_state(d.getBoolean())
        continue
      if tt == 66:
        self.set_request_data(d.getPrefixedString())
        continue
      if tt == 74:
        self.set_gettime(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_service_name_: res+=prefix+("service_name: %s\n" % self.DebugFormatString(self.service_name_))
    if self.has_root_path_: res+=prefix+("root_path: %s\n" % self.DebugFormatString(self.root_path_))
    if self.has_auto_task_running_: res+=prefix+("auto_task_running: %s\n" % self.DebugFormatBool(self.auto_task_running_))
    if self.has_task_retry_seconds_: res+=prefix+("task_retry_seconds: %s\n" % self.DebugFormatInt32(self.task_retry_seconds_))
    if self.has_all_queues_valid_: res+=prefix+("all_queues_valid: %s\n" % self.DebugFormatBool(self.all_queues_valid_))
    if self.has_default_http_server_: res+=prefix+("default_http_server: %s\n" % self.DebugFormatString(self.default_http_server_))
    if self.has_testing_validate_state_: res+=prefix+("testing_validate_state: %s\n" % self.DebugFormatBool(self.testing_validate_state_))
    if self.has_request_data_: res+=prefix+("request_data: %s\n" % self.DebugFormatString(self.request_data_))
    if self.has_gettime_: res+=prefix+("gettime: %s\n" % self.DebugFormatString(self.gettime_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kservice_name = 1
  kroot_path = 2
  kauto_task_running = 3
  ktask_retry_seconds = 4
  kall_queues_valid = 5
  kdefault_http_server = 6
  ktesting_validate_state = 7
  krequest_data = 8
  kgettime = 9

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "service_name",
    2: "root_path",
    3: "auto_task_running",
    4: "task_retry_seconds",
    5: "all_queues_valid",
    6: "default_http_server",
    7: "testing_validate_state",
    8: "request_data",
    9: "gettime",
  }, 9)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
    2: ProtocolBuffer.Encoder.STRING,
    3: ProtocolBuffer.Encoder.NUMERIC,
    4: ProtocolBuffer.Encoder.NUMERIC,
    5: ProtocolBuffer.Encoder.NUMERIC,
    6: ProtocolBuffer.Encoder.STRING,
    7: ProtocolBuffer.Encoder.NUMERIC,
    8: ProtocolBuffer.Encoder.STRING,
    9: ProtocolBuffer.Encoder.STRING,
  }, 9, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.SetUpStubRequest'
class GetQueueStateInfoResponse_QueueInfo_TaskInfo(ProtocolBuffer.ProtocolMessage):
  has_task_name_ = 0
  task_name_ = ""
  has_eta_millis_ = 0
  eta_millis_ = 0
  has_add_request_ = 0
  add_request_ = None

  def __init__(self, contents=None):
    self.lazy_init_lock_ = _Lock()
    if contents is not None: self.MergeFromString(contents)

  def task_name(self): return self.task_name_

  def set_task_name(self, x):
    self.has_task_name_ = 1
    self.task_name_ = x

  def clear_task_name(self):
    if self.has_task_name_:
      self.has_task_name_ = 0
      self.task_name_ = ""

  def has_task_name(self): return self.has_task_name_

  def eta_millis(self): return self.eta_millis_

  def set_eta_millis(self, x):
    self.has_eta_millis_ = 1
    self.eta_millis_ = x

  def clear_eta_millis(self):
    if self.has_eta_millis_:
      self.has_eta_millis_ = 0
      self.eta_millis_ = 0

  def has_eta_millis(self): return self.has_eta_millis_

  def add_request(self):
    if self.add_request_ is None:
      self.lazy_init_lock_.acquire()
      try:
        if self.add_request_ is None: self.add_request_ = TaskQueueAddRequest()
      finally:
        self.lazy_init_lock_.release()
    return self.add_request_

  def mutable_add_request(self): self.has_add_request_ = 1; return self.add_request()

  def clear_add_request(self):

    if self.has_add_request_:
      self.has_add_request_ = 0;
      if self.add_request_ is not None: self.add_request_.Clear()

  def has_add_request(self): return self.has_add_request_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_task_name()): self.set_task_name(x.task_name())
    if (x.has_eta_millis()): self.set_eta_millis(x.eta_millis())
    if (x.has_add_request()): self.mutable_add_request().MergeFrom(x.add_request())

  def Equals(self, x):
    if x is self: return 1
    if self.has_task_name_ != x.has_task_name_: return 0
    if self.has_task_name_ and self.task_name_ != x.task_name_: return 0
    if self.has_eta_millis_ != x.has_eta_millis_: return 0
    if self.has_eta_millis_ and self.eta_millis_ != x.eta_millis_: return 0
    if self.has_add_request_ != x.has_add_request_: return 0
    if self.has_add_request_ and self.add_request_ != x.add_request_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (self.has_add_request_ and not self.add_request_.IsInitialized(debug_strs)): initialized = 0
    return initialized

  def ByteSize(self):
    n = 0
    if (self.has_task_name_): n += 1 + self.lengthString(len(self.task_name_))
    if (self.has_eta_millis_): n += 1 + self.lengthVarInt64(self.eta_millis_)
    if (self.has_add_request_): n += 1 + self.lengthString(self.add_request_.ByteSize())
    return n

  def ByteSizePartial(self):
    n = 0
    if (self.has_task_name_): n += 1 + self.lengthString(len(self.task_name_))
    if (self.has_eta_millis_): n += 1 + self.lengthVarInt64(self.eta_millis_)
    if (self.has_add_request_): n += 1 + self.lengthString(self.add_request_.ByteSizePartial())
    return n

  def Clear(self):
    self.clear_task_name()
    self.clear_eta_millis()
    self.clear_add_request()

  def OutputUnchecked(self, out):
    if (self.has_task_name_):
      out.putVarInt32(10)
      out.putPrefixedString(self.task_name_)
    if (self.has_eta_millis_):
      out.putVarInt32(16)
      out.putVarInt64(self.eta_millis_)
    if (self.has_add_request_):
      out.putVarInt32(26)
      out.putVarInt32(self.add_request_.ByteSize())
      self.add_request_.OutputUnchecked(out)

  def OutputPartial(self, out):
    if (self.has_task_name_):
      out.putVarInt32(10)
      out.putPrefixedString(self.task_name_)
    if (self.has_eta_millis_):
      out.putVarInt32(16)
      out.putVarInt64(self.eta_millis_)
    if (self.has_add_request_):
      out.putVarInt32(26)
      out.putVarInt32(self.add_request_.ByteSizePartial())
      self.add_request_.OutputPartial(out)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_task_name(d.getPrefixedString())
        continue
      if tt == 16:
        self.set_eta_millis(d.getVarInt64())
        continue
      if tt == 26:
        length = d.getVarInt32()
        tmp = ProtocolBuffer.Decoder(d.buffer(), d.pos(), d.pos() + length)
        d.skip(length)
        self.mutable_add_request().TryMerge(tmp)
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_task_name_: res+=prefix+("task_name: %s\n" % self.DebugFormatString(self.task_name_))
    if self.has_eta_millis_: res+=prefix+("eta_millis: %s\n" % self.DebugFormatInt64(self.eta_millis_))
    if self.has_add_request_:
      res+=prefix+"add_request <\n"
      res+=self.add_request_.__str__(prefix + "  ", printElemNumber)
      res+=prefix+">\n"
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  ktask_name = 1
  keta_millis = 2
  kadd_request = 3

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "task_name",
    2: "eta_millis",
    3: "add_request",
  }, 3)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
    2: ProtocolBuffer.Encoder.NUMERIC,
    3: ProtocolBuffer.Encoder.STRING,
  }, 3, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.GetQueueStateInfoResponse_QueueInfo_TaskInfo'
class GetQueueStateInfoResponse_QueueInfo(ProtocolBuffer.ProtocolMessage):
  has_queue_name_ = 0
  queue_name_ = ""
  has_formatted_rate_string_ = 0
  formatted_rate_string_ = ""
  has_bucket_size_ = 0
  bucket_size_ = 0
  has_max_concurrent_requests_ = 0
  max_concurrent_requests_ = 0
  has_retry_parameters_ = 0
  retry_parameters_ = None
  has_target_ = 0
  target_ = ""
  has_mode_ = 0
  mode_ = ""
  has_acl_ = 0
  acl_ = None

  def __init__(self, contents=None):
    self.task_infos_ = []
    self.lazy_init_lock_ = _Lock()
    if contents is not None: self.MergeFromString(contents)

  def queue_name(self): return self.queue_name_

  def set_queue_name(self, x):
    self.has_queue_name_ = 1
    self.queue_name_ = x

  def clear_queue_name(self):
    if self.has_queue_name_:
      self.has_queue_name_ = 0
      self.queue_name_ = ""

  def has_queue_name(self): return self.has_queue_name_

  def formatted_rate_string(self): return self.formatted_rate_string_

  def set_formatted_rate_string(self, x):
    self.has_formatted_rate_string_ = 1
    self.formatted_rate_string_ = x

  def clear_formatted_rate_string(self):
    if self.has_formatted_rate_string_:
      self.has_formatted_rate_string_ = 0
      self.formatted_rate_string_ = ""

  def has_formatted_rate_string(self): return self.has_formatted_rate_string_

  def bucket_size(self): return self.bucket_size_

  def set_bucket_size(self, x):
    self.has_bucket_size_ = 1
    self.bucket_size_ = x

  def clear_bucket_size(self):
    if self.has_bucket_size_:
      self.has_bucket_size_ = 0
      self.bucket_size_ = 0

  def has_bucket_size(self): return self.has_bucket_size_

  def max_concurrent_requests(self): return self.max_concurrent_requests_

  def set_max_concurrent_requests(self, x):
    self.has_max_concurrent_requests_ = 1
    self.max_concurrent_requests_ = x

  def clear_max_concurrent_requests(self):
    if self.has_max_concurrent_requests_:
      self.has_max_concurrent_requests_ = 0
      self.max_concurrent_requests_ = 0

  def has_max_concurrent_requests(self): return self.has_max_concurrent_requests_

  def retry_parameters(self):
    if self.retry_parameters_ is None:
      self.lazy_init_lock_.acquire()
      try:
        if self.retry_parameters_ is None: self.retry_parameters_ = TaskQueueRetryParameters()
      finally:
        self.lazy_init_lock_.release()
    return self.retry_parameters_

  def mutable_retry_parameters(self): self.has_retry_parameters_ = 1; return self.retry_parameters()

  def clear_retry_parameters(self):

    if self.has_retry_parameters_:
      self.has_retry_parameters_ = 0;
      if self.retry_parameters_ is not None: self.retry_parameters_.Clear()

  def has_retry_parameters(self): return self.has_retry_parameters_

  def target(self): return self.target_

  def set_target(self, x):
    self.has_target_ = 1
    self.target_ = x

  def clear_target(self):
    if self.has_target_:
      self.has_target_ = 0
      self.target_ = ""

  def has_target(self): return self.has_target_

  def mode(self): return self.mode_

  def set_mode(self, x):
    self.has_mode_ = 1
    self.mode_ = x

  def clear_mode(self):
    if self.has_mode_:
      self.has_mode_ = 0
      self.mode_ = ""

  def has_mode(self): return self.has_mode_

  def acl(self):
    if self.acl_ is None:
      self.lazy_init_lock_.acquire()
      try:
        if self.acl_ is None: self.acl_ = TaskQueueAcl()
      finally:
        self.lazy_init_lock_.release()
    return self.acl_

  def mutable_acl(self): self.has_acl_ = 1; return self.acl()

  def clear_acl(self):

    if self.has_acl_:
      self.has_acl_ = 0;
      if self.acl_ is not None: self.acl_.Clear()

  def has_acl(self): return self.has_acl_

  def task_infos_size(self): return len(self.task_infos_)
  def task_infos_list(self): return self.task_infos_

  def task_infos(self, i):
    return self.task_infos_[i]

  def mutable_task_infos(self, i):
    return self.task_infos_[i]

  def add_task_infos(self):
    x = GetQueueStateInfoResponse_QueueInfo_TaskInfo()
    self.task_infos_.append(x)
    return x

  def clear_task_infos(self):
    self.task_infos_ = []

  def MergeFrom(self, x):
    assert x is not self
    if (x.has_queue_name()): self.set_queue_name(x.queue_name())
    if (x.has_formatted_rate_string()): self.set_formatted_rate_string(x.formatted_rate_string())
    if (x.has_bucket_size()): self.set_bucket_size(x.bucket_size())
    if (x.has_max_concurrent_requests()): self.set_max_concurrent_requests(x.max_concurrent_requests())
    if (x.has_retry_parameters()): self.mutable_retry_parameters().MergeFrom(x.retry_parameters())
    if (x.has_target()): self.set_target(x.target())
    if (x.has_mode()): self.set_mode(x.mode())
    if (x.has_acl()): self.mutable_acl().MergeFrom(x.acl())
    for i in range(x.task_infos_size()): self.add_task_infos().CopyFrom(x.task_infos(i))

  def Equals(self, x):
    if x is self: return 1
    if self.has_queue_name_ != x.has_queue_name_: return 0
    if self.has_queue_name_ and self.queue_name_ != x.queue_name_: return 0
    if self.has_formatted_rate_string_ != x.has_formatted_rate_string_: return 0
    if self.has_formatted_rate_string_ and self.formatted_rate_string_ != x.formatted_rate_string_: return 0
    if self.has_bucket_size_ != x.has_bucket_size_: return 0
    if self.has_bucket_size_ and self.bucket_size_ != x.bucket_size_: return 0
    if self.has_max_concurrent_requests_ != x.has_max_concurrent_requests_: return 0
    if self.has_max_concurrent_requests_ and self.max_concurrent_requests_ != x.max_concurrent_requests_: return 0
    if self.has_retry_parameters_ != x.has_retry_parameters_: return 0
    if self.has_retry_parameters_ and self.retry_parameters_ != x.retry_parameters_: return 0
    if self.has_target_ != x.has_target_: return 0
    if self.has_target_ and self.target_ != x.target_: return 0
    if self.has_mode_ != x.has_mode_: return 0
    if self.has_mode_ and self.mode_ != x.mode_: return 0
    if self.has_acl_ != x.has_acl_: return 0
    if self.has_acl_ and self.acl_ != x.acl_: return 0
    if len(self.task_infos_) != len(x.task_infos_): return 0
    for e1, e2 in zip(self.task_infos_, x.task_infos_):
      if e1 != e2: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (self.has_retry_parameters_ and not self.retry_parameters_.IsInitialized(debug_strs)): initialized = 0
    if (self.has_acl_ and not self.acl_.IsInitialized(debug_strs)): initialized = 0
    for p in self.task_infos_:
      if not p.IsInitialized(debug_strs): initialized=0
    return initialized

  def ByteSize(self):
    n = 0
    if (self.has_queue_name_): n += 1 + self.lengthString(len(self.queue_name_))
    if (self.has_formatted_rate_string_): n += 1 + self.lengthString(len(self.formatted_rate_string_))
    if (self.has_bucket_size_): n += 1 + self.lengthVarInt64(self.bucket_size_)
    if (self.has_max_concurrent_requests_): n += 1 + self.lengthVarInt64(self.max_concurrent_requests_)
    if (self.has_retry_parameters_): n += 1 + self.lengthString(self.retry_parameters_.ByteSize())
    if (self.has_target_): n += 1 + self.lengthString(len(self.target_))
    if (self.has_mode_): n += 1 + self.lengthString(len(self.mode_))
    if (self.has_acl_): n += 1 + self.lengthString(self.acl_.ByteSize())
    n += 1 * len(self.task_infos_)
    for i in range(len(self.task_infos_)): n += self.lengthString(self.task_infos_[i].ByteSize())
    return n

  def ByteSizePartial(self):
    n = 0
    if (self.has_queue_name_): n += 1 + self.lengthString(len(self.queue_name_))
    if (self.has_formatted_rate_string_): n += 1 + self.lengthString(len(self.formatted_rate_string_))
    if (self.has_bucket_size_): n += 1 + self.lengthVarInt64(self.bucket_size_)
    if (self.has_max_concurrent_requests_): n += 1 + self.lengthVarInt64(self.max_concurrent_requests_)
    if (self.has_retry_parameters_): n += 1 + self.lengthString(self.retry_parameters_.ByteSizePartial())
    if (self.has_target_): n += 1 + self.lengthString(len(self.target_))
    if (self.has_mode_): n += 1 + self.lengthString(len(self.mode_))
    if (self.has_acl_): n += 1 + self.lengthString(self.acl_.ByteSizePartial())
    n += 1 * len(self.task_infos_)
    for i in range(len(self.task_infos_)): n += self.lengthString(self.task_infos_[i].ByteSizePartial())
    return n

  def Clear(self):
    self.clear_queue_name()
    self.clear_formatted_rate_string()
    self.clear_bucket_size()
    self.clear_max_concurrent_requests()
    self.clear_retry_parameters()
    self.clear_target()
    self.clear_mode()
    self.clear_acl()
    self.clear_task_infos()

  def OutputUnchecked(self, out):
    if (self.has_queue_name_):
      out.putVarInt32(10)
      out.putPrefixedString(self.queue_name_)
    if (self.has_formatted_rate_string_):
      out.putVarInt32(18)
      out.putPrefixedString(self.formatted_rate_string_)
    if (self.has_bucket_size_):
      out.putVarInt32(24)
      out.putVarInt32(self.bucket_size_)
    if (self.has_max_concurrent_requests_):
      out.putVarInt32(32)
      out.putVarInt32(self.max_concurrent_requests_)
    if (self.has_retry_parameters_):
      out.putVarInt32(42)
      out.putVarInt32(self.retry_parameters_.ByteSize())
      self.retry_parameters_.OutputUnchecked(out)
    if (self.has_target_):
      out.putVarInt32(50)
      out.putPrefixedString(self.target_)
    if (self.has_mode_):
      out.putVarInt32(58)
      out.putPrefixedString(self.mode_)
    if (self.has_acl_):
      out.putVarInt32(66)
      out.putVarInt32(self.acl_.ByteSize())
      self.acl_.OutputUnchecked(out)
    for i in range(len(self.task_infos_)):
      out.putVarInt32(74)
      out.putVarInt32(self.task_infos_[i].ByteSize())
      self.task_infos_[i].OutputUnchecked(out)

  def OutputPartial(self, out):
    if (self.has_queue_name_):
      out.putVarInt32(10)
      out.putPrefixedString(self.queue_name_)
    if (self.has_formatted_rate_string_):
      out.putVarInt32(18)
      out.putPrefixedString(self.formatted_rate_string_)
    if (self.has_bucket_size_):
      out.putVarInt32(24)
      out.putVarInt32(self.bucket_size_)
    if (self.has_max_concurrent_requests_):
      out.putVarInt32(32)
      out.putVarInt32(self.max_concurrent_requests_)
    if (self.has_retry_parameters_):
      out.putVarInt32(42)
      out.putVarInt32(self.retry_parameters_.ByteSizePartial())
      self.retry_parameters_.OutputPartial(out)
    if (self.has_target_):
      out.putVarInt32(50)
      out.putPrefixedString(self.target_)
    if (self.has_mode_):
      out.putVarInt32(58)
      out.putPrefixedString(self.mode_)
    if (self.has_acl_):
      out.putVarInt32(66)
      out.putVarInt32(self.acl_.ByteSizePartial())
      self.acl_.OutputPartial(out)
    for i in range(len(self.task_infos_)):
      out.putVarInt32(74)
      out.putVarInt32(self.task_infos_[i].ByteSizePartial())
      self.task_infos_[i].OutputPartial(out)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_queue_name(d.getPrefixedString())
        continue
      if tt == 18:
        self.set_formatted_rate_string(d.getPrefixedString())
        continue
      if tt == 24:
        self.set_bucket_size(d.getVarInt32())
        continue
      if tt == 32:
        self.set_max_concurrent_requests(d.getVarInt32())
        continue
      if tt == 42:
        length = d.getVarInt32()
        tmp = ProtocolBuffer.Decoder(d.buffer(), d.pos(), d.pos() + length)
        d.skip(length)
        self.mutable_retry_parameters().TryMerge(tmp)
        continue
      if tt == 50:
        self.set_target(d.getPrefixedString())
        continue
      if tt == 58:
        self.set_mode(d.getPrefixedString())
        continue
      if tt == 66:
        length = d.getVarInt32()
        tmp = ProtocolBuffer.Decoder(d.buffer(), d.pos(), d.pos() + length)
        d.skip(length)
        self.mutable_acl().TryMerge(tmp)
        continue
      if tt == 74:
        length = d.getVarInt32()
        tmp = ProtocolBuffer.Decoder(d.buffer(), d.pos(), d.pos() + length)
        d.skip(length)
        self.add_task_infos().TryMerge(tmp)
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_queue_name_: res+=prefix+("queue_name: %s\n" % self.DebugFormatString(self.queue_name_))
    if self.has_formatted_rate_string_: res+=prefix+("formatted_rate_string: %s\n" % self.DebugFormatString(self.formatted_rate_string_))
    if self.has_bucket_size_: res+=prefix+("bucket_size: %s\n" % self.DebugFormatInt32(self.bucket_size_))
    if self.has_max_concurrent_requests_: res+=prefix+("max_concurrent_requests: %s\n" % self.DebugFormatInt32(self.max_concurrent_requests_))
    if self.has_retry_parameters_:
      res+=prefix+"retry_parameters <\n"
      res+=self.retry_parameters_.__str__(prefix + "  ", printElemNumber)
      res+=prefix+">\n"
    if self.has_target_: res+=prefix+("target: %s\n" % self.DebugFormatString(self.target_))
    if self.has_mode_: res+=prefix+("mode: %s\n" % self.DebugFormatString(self.mode_))
    if self.has_acl_:
      res+=prefix+"acl <\n"
      res+=self.acl_.__str__(prefix + "  ", printElemNumber)
      res+=prefix+">\n"
    cnt=0
    for e in self.task_infos_:
      elm=""
      if printElemNumber: elm="(%d)" % cnt
      res+=prefix+("task_infos%s <\n" % elm)
      res+=e.__str__(prefix + "  ", printElemNumber)
      res+=prefix+">\n"
      cnt+=1
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kqueue_name = 1
  kformatted_rate_string = 2
  kbucket_size = 3
  kmax_concurrent_requests = 4
  kretry_parameters = 5
  ktarget = 6
  kmode = 7
  kacl = 8
  ktask_infos = 9

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "queue_name",
    2: "formatted_rate_string",
    3: "bucket_size",
    4: "max_concurrent_requests",
    5: "retry_parameters",
    6: "target",
    7: "mode",
    8: "acl",
    9: "task_infos",
  }, 9)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
    2: ProtocolBuffer.Encoder.STRING,
    3: ProtocolBuffer.Encoder.NUMERIC,
    4: ProtocolBuffer.Encoder.NUMERIC,
    5: ProtocolBuffer.Encoder.STRING,
    6: ProtocolBuffer.Encoder.STRING,
    7: ProtocolBuffer.Encoder.STRING,
    8: ProtocolBuffer.Encoder.STRING,
    9: ProtocolBuffer.Encoder.STRING,
  }, 9, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.GetQueueStateInfoResponse_QueueInfo'
class GetQueueStateInfoResponse(ProtocolBuffer.ProtocolMessage):

  def __init__(self, contents=None):
    self.queues_ = []
    if contents is not None: self.MergeFromString(contents)

  def queues_size(self): return len(self.queues_)
  def queues_list(self): return self.queues_

  def queues(self, i):
    return self.queues_[i]

  def mutable_queues(self, i):
    return self.queues_[i]

  def add_queues(self):
    x = GetQueueStateInfoResponse_QueueInfo()
    self.queues_.append(x)
    return x

  def clear_queues(self):
    self.queues_ = []

  def MergeFrom(self, x):
    assert x is not self
    for i in range(x.queues_size()): self.add_queues().CopyFrom(x.queues(i))

  def Equals(self, x):
    if x is self: return 1
    if len(self.queues_) != len(x.queues_): return 0
    for e1, e2 in zip(self.queues_, x.queues_):
      if e1 != e2: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    for p in self.queues_:
      if not p.IsInitialized(debug_strs): initialized=0
    return initialized

  def ByteSize(self):
    n = 0
    n += 1 * len(self.queues_)
    for i in range(len(self.queues_)): n += self.lengthString(self.queues_[i].ByteSize())
    return n

  def ByteSizePartial(self):
    n = 0
    n += 1 * len(self.queues_)
    for i in range(len(self.queues_)): n += self.lengthString(self.queues_[i].ByteSizePartial())
    return n

  def Clear(self):
    self.clear_queues()

  def OutputUnchecked(self, out):
    for i in range(len(self.queues_)):
      out.putVarInt32(10)
      out.putVarInt32(self.queues_[i].ByteSize())
      self.queues_[i].OutputUnchecked(out)

  def OutputPartial(self, out):
    for i in range(len(self.queues_)):
      out.putVarInt32(10)
      out.putVarInt32(self.queues_[i].ByteSizePartial())
      self.queues_[i].OutputPartial(out)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        length = d.getVarInt32()
        tmp = ProtocolBuffer.Decoder(d.buffer(), d.pos(), d.pos() + length)
        d.skip(length)
        self.add_queues().TryMerge(tmp)
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    cnt=0
    for e in self.queues_:
      elm=""
      if printElemNumber: elm="(%d)" % cnt
      res+=prefix+("queues%s <\n" % elm)
      res+=e.__str__(prefix + "  ", printElemNumber)
      res+=prefix+">\n"
      cnt+=1
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kqueues = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "queues",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.GetQueueStateInfoResponse'
class LoadQueueXmlRequest(ProtocolBuffer.ProtocolMessage):
  has_queue_xml_path_ = 0
  queue_xml_path_ = ""

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def queue_xml_path(self): return self.queue_xml_path_

  def set_queue_xml_path(self, x):
    self.has_queue_xml_path_ = 1
    self.queue_xml_path_ = x

  def clear_queue_xml_path(self):
    if self.has_queue_xml_path_:
      self.has_queue_xml_path_ = 0
      self.queue_xml_path_ = ""

  def has_queue_xml_path(self): return self.has_queue_xml_path_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_queue_xml_path()): self.set_queue_xml_path(x.queue_xml_path())

  def Equals(self, x):
    if x is self: return 1
    if self.has_queue_xml_path_ != x.has_queue_xml_path_: return 0
    if self.has_queue_xml_path_ and self.queue_xml_path_ != x.queue_xml_path_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_queue_xml_path_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: queue_xml_path not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(len(self.queue_xml_path_))
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_queue_xml_path_):
      n += 1
      n += self.lengthString(len(self.queue_xml_path_))
    return n

  def Clear(self):
    self.clear_queue_xml_path()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putPrefixedString(self.queue_xml_path_)

  def OutputPartial(self, out):
    if (self.has_queue_xml_path_):
      out.putVarInt32(10)
      out.putPrefixedString(self.queue_xml_path_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_queue_xml_path(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_queue_xml_path_: res+=prefix+("queue_xml_path: %s\n" % self.DebugFormatString(self.queue_xml_path_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kqueue_xml_path = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "queue_xml_path",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.LoadQueueXmlRequest'
class SetTaskQueueClockRequest(ProtocolBuffer.ProtocolMessage):
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
  _PROTO_DESCRIPTOR_NAME = 'apphosting.SetTaskQueueClockRequest'
if _extension_runtime:
  pass

__all__ = ['GetQueuesResponse','GetFilteredTasksResponse','FlushQueueRequest','GetFilteredTasksRequest','PatchQueueYamlParserRequest','SetUpStubRequest','GetQueueStateInfoResponse_QueueInfo_TaskInfo','GetQueueStateInfoResponse_QueueInfo','GetQueueStateInfoResponse','LoadQueueXmlRequest','SetTaskQueueClockRequest']
