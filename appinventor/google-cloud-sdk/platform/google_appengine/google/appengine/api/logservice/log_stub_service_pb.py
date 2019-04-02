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
import base64
try:
  from thread import allocate_lock as _Lock
except ImportError:
  from threading import Lock as _Lock
try:
  from google3.net.proto import _net_proto___parse__python
except ImportError:
  _net_proto___parse__python = None
import sys
try:
  __import__('google.net.rpc.python.proto_python_api_1_stub')
  __import__('google.net.rpc.python.pywraprpc')
  proto_python_api_1_stub = sys.modules.get('google.net.rpc.python.proto_python_api_1_stub')
  pywraprpc = sys.modules.get('google.net.rpc.python.pywraprpc')
  _client_stub_base_class = proto_python_api_1_stub.Stub
except ImportError:
  _client_stub_base_class = object
try:
  __import__('google.net.rpc.python.rpcserver')
  rpcserver = sys.modules.get('google.net.rpc.python.rpcserver')
  _server_stub_base_class = rpcserver.BaseRpcServer
except ImportError:
  _server_stub_base_class = object

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
from google.appengine.api.logservice.log_service_pb import *
import google.appengine.api.logservice.log_service_pb
google_dot_apphosting_dot_api_dot_logservice_dot_log__service__pb = __import__('google.appengine.api.logservice.log_service_pb', {}, {}, [''])
class AddRequestInfoRequest(ProtocolBuffer.ProtocolMessage):
  has_request_log_ = 0
  request_log_ = None

  def __init__(self, contents=None):
    self.lazy_init_lock_ = _Lock()
    if contents is not None: self.MergeFromString(contents)

  def request_log(self):
    if self.request_log_ is None:
      self.lazy_init_lock_.acquire()
      try:
        if self.request_log_ is None: self.request_log_ = google.appengine.api.logservice.log_service_pb.RequestLog()
      finally:
        self.lazy_init_lock_.release()
    return self.request_log_

  def mutable_request_log(self): self.has_request_log_ = 1; return self.request_log()

  def clear_request_log(self):

    if self.has_request_log_:
      self.has_request_log_ = 0;
      if self.request_log_ is not None: self.request_log_.Clear()

  def has_request_log(self): return self.has_request_log_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_request_log()): self.mutable_request_log().MergeFrom(x.request_log())

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.AddRequestInfoRequest', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.AddRequestInfoRequest')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.AddRequestInfoRequest')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.AddRequestInfoRequest', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.AddRequestInfoRequest', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.AddRequestInfoRequest', s)


  def Equals(self, x):
    if x is self: return 1
    if self.has_request_log_ != x.has_request_log_: return 0
    if self.has_request_log_ and self.request_log_ != x.request_log_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (self.has_request_log_ and not self.request_log_.IsInitialized(debug_strs)): initialized = 0
    return initialized

  def ByteSize(self):
    n = 0
    if (self.has_request_log_): n += 1 + self.lengthString(self.request_log_.ByteSize())
    return n

  def ByteSizePartial(self):
    n = 0
    if (self.has_request_log_): n += 1 + self.lengthString(self.request_log_.ByteSizePartial())
    return n

  def Clear(self):
    self.clear_request_log()

  def OutputUnchecked(self, out):
    if (self.has_request_log_):
      out.putVarInt32(10)
      out.putVarInt32(self.request_log_.ByteSize())
      self.request_log_.OutputUnchecked(out)

  def OutputPartial(self, out):
    if (self.has_request_log_):
      out.putVarInt32(10)
      out.putVarInt32(self.request_log_.ByteSizePartial())
      self.request_log_.OutputPartial(out)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        length = d.getVarInt32()
        tmp = ProtocolBuffer.Decoder(d.buffer(), d.pos(), d.pos() + length)
        d.skip(length)
        self.mutable_request_log().TryMerge(tmp)
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_request_log_:
      res+=prefix+"request_log <\n"
      res+=self.request_log_.__str__(prefix + "  ", printElemNumber)
      res+=prefix+">\n"
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  krequest_log = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "request_log",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.AddRequestInfoRequest'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjBhcHBob3N0aW5nL2FwaS9sb2dzZXJ2aWNlL2xvZ19zdHViX3NlcnZpY2UucHJvdG8KIGFwcGhvc3RpbmcuQWRkUmVxdWVzdEluZm9SZXF1ZXN0ExoLcmVxdWVzdF9sb2cgASgCMAs4AUoVYXBwaG9zdGluZy5SZXF1ZXN0TG9nowGqAQVjdHlwZbIBBnByb3RvMqQBFLoBlwgKMGFwcGhvc3RpbmcvYXBpL2xvZ3NlcnZpY2UvbG9nX3N0dWJfc2VydmljZS5wcm90bxIKYXBwaG9zdGluZxodYXBwaG9zdGluZy9hcGkvYXBpX2Jhc2UucHJvdG8aK2FwcGhvc3RpbmcvYXBpL2xvZ3NlcnZpY2UvbG9nX3NlcnZpY2UucHJvdG8iRAoVQWRkUmVxdWVzdEluZm9SZXF1ZXN0EisKC3JlcXVlc3RfbG9nGAEgASgLMhYuYXBwaG9zdGluZy5SZXF1ZXN0TG9nIlEKFEFkZEFwcExvZ0xpbmVSZXF1ZXN0EiUKCGxvZ19saW5lGAEgASgLMhMuYXBwaG9zdGluZy5Mb2dMaW5lEhIKCnJlcXVlc3RfaWQYAiABKAkihQIKFlN0YXJ0UmVxdWVzdExvZ1JlcXVlc3QSEgoKcmVxdWVzdF9pZBgBIAIoCRIXCg91c2VyX3JlcXVlc3RfaWQYAiABKAkSCgoCaXAYAyABKAkSDgoGYXBwX2lkGAQgASgJEhIKCnZlcnNpb25faWQYBSABKAkSEAoIbmlja25hbWUYBiABKAkSEgoKdXNlcl9hZ2VudBgHIAEoCRIMCgRob3N0GAggASgJEg4KBm1ldGhvZBgJIAEoCRIQCghyZXNvdXJjZRgKIAEoCRIUCgxodHRwX3ZlcnNpb24YCyABKAkSEgoKc3RhcnRfdGltZRgMIAEoAxIOCgZtb2R1bGUYDSABKAkiUQoURW5kUmVxdWVzdExvZ1JlcXVlc3QSEgoKcmVxdWVzdF9pZBgBIAIoCRIOCgZzdGF0dXMYAiACKAUSFQoNcmVzcG9uc2Vfc2l6ZRgDIAEoBTLaAgoOTG9nU3R1YlNlcnZpY2USUQoOQWRkUmVxdWVzdEluZm8SIS5hcHBob3N0aW5nLkFkZFJlcXVlc3RJbmZvUmVxdWVzdBoaLmFwcGhvc3RpbmcuYmFzZS5Wb2lkUHJvdG8iABJPCg1BZGRBcHBMb2dMaW5lEiAuYXBwaG9zdGluZy5BZGRBcHBMb2dMaW5lUmVxdWVzdBoaLmFwcGhvc3RpbmcuYmFzZS5Wb2lkUHJvdG8iABJPCg1FbmRSZXF1ZXN0TG9nEiAuYXBwaG9zdGluZy5FbmRSZXF1ZXN0TG9nUmVxdWVzdBoaLmFwcGhvc3RpbmcuYmFzZS5Wb2lkUHJvdG8iABJTCg9TdGFydFJlcXVlc3RMb2cSIi5hcHBob3N0aW5nLlN0YXJ0UmVxdWVzdExvZ1JlcXVlc3QaGi5hcHBob3N0aW5nLmJhc2UuVm9pZFByb3RvIgBCOgokY29tLmdvb2dsZS5hcHBob3N0aW5nLmFwaS5sb2dzZXJ2aWNlKAFCEExvZ1N0dWJTZXJ2aWNlUGI="))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())

class AddAppLogLineRequest(ProtocolBuffer.ProtocolMessage):
  has_log_line_ = 0
  log_line_ = None
  has_request_id_ = 0
  request_id_ = ""

  def __init__(self, contents=None):
    self.lazy_init_lock_ = _Lock()
    if contents is not None: self.MergeFromString(contents)

  def log_line(self):
    if self.log_line_ is None:
      self.lazy_init_lock_.acquire()
      try:
        if self.log_line_ is None: self.log_line_ = google.appengine.api.logservice.log_service_pb.LogLine()
      finally:
        self.lazy_init_lock_.release()
    return self.log_line_

  def mutable_log_line(self): self.has_log_line_ = 1; return self.log_line()

  def clear_log_line(self):

    if self.has_log_line_:
      self.has_log_line_ = 0;
      if self.log_line_ is not None: self.log_line_.Clear()

  def has_log_line(self): return self.has_log_line_

  def request_id(self): return self.request_id_

  def set_request_id(self, x):
    self.has_request_id_ = 1
    self.request_id_ = x

  def clear_request_id(self):
    if self.has_request_id_:
      self.has_request_id_ = 0
      self.request_id_ = ""

  def has_request_id(self): return self.has_request_id_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_log_line()): self.mutable_log_line().MergeFrom(x.log_line())
    if (x.has_request_id()): self.set_request_id(x.request_id())

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.AddAppLogLineRequest', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.AddAppLogLineRequest')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.AddAppLogLineRequest')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.AddAppLogLineRequest', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.AddAppLogLineRequest', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.AddAppLogLineRequest', s)


  def Equals(self, x):
    if x is self: return 1
    if self.has_log_line_ != x.has_log_line_: return 0
    if self.has_log_line_ and self.log_line_ != x.log_line_: return 0
    if self.has_request_id_ != x.has_request_id_: return 0
    if self.has_request_id_ and self.request_id_ != x.request_id_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (self.has_log_line_ and not self.log_line_.IsInitialized(debug_strs)): initialized = 0
    return initialized

  def ByteSize(self):
    n = 0
    if (self.has_log_line_): n += 1 + self.lengthString(self.log_line_.ByteSize())
    if (self.has_request_id_): n += 1 + self.lengthString(len(self.request_id_))
    return n

  def ByteSizePartial(self):
    n = 0
    if (self.has_log_line_): n += 1 + self.lengthString(self.log_line_.ByteSizePartial())
    if (self.has_request_id_): n += 1 + self.lengthString(len(self.request_id_))
    return n

  def Clear(self):
    self.clear_log_line()
    self.clear_request_id()

  def OutputUnchecked(self, out):
    if (self.has_log_line_):
      out.putVarInt32(10)
      out.putVarInt32(self.log_line_.ByteSize())
      self.log_line_.OutputUnchecked(out)
    if (self.has_request_id_):
      out.putVarInt32(18)
      out.putPrefixedString(self.request_id_)

  def OutputPartial(self, out):
    if (self.has_log_line_):
      out.putVarInt32(10)
      out.putVarInt32(self.log_line_.ByteSizePartial())
      self.log_line_.OutputPartial(out)
    if (self.has_request_id_):
      out.putVarInt32(18)
      out.putPrefixedString(self.request_id_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        length = d.getVarInt32()
        tmp = ProtocolBuffer.Decoder(d.buffer(), d.pos(), d.pos() + length)
        d.skip(length)
        self.mutable_log_line().TryMerge(tmp)
        continue
      if tt == 18:
        self.set_request_id(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_log_line_:
      res+=prefix+"log_line <\n"
      res+=self.log_line_.__str__(prefix + "  ", printElemNumber)
      res+=prefix+">\n"
    if self.has_request_id_: res+=prefix+("request_id: %s\n" % self.DebugFormatString(self.request_id_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  klog_line = 1
  krequest_id = 2

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "log_line",
    2: "request_id",
  }, 2)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
    2: ProtocolBuffer.Encoder.STRING,
  }, 2, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.AddAppLogLineRequest'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjBhcHBob3N0aW5nL2FwaS9sb2dzZXJ2aWNlL2xvZ19zdHViX3NlcnZpY2UucHJvdG8KH2FwcGhvc3RpbmcuQWRkQXBwTG9nTGluZVJlcXVlc3QTGghsb2dfbGluZSABKAIwCzgBShJhcHBob3N0aW5nLkxvZ0xpbmWjAaoBBWN0eXBlsgEGcHJvdG8ypAEUExoKcmVxdWVzdF9pZCACKAIwCTgBFMIBIGFwcGhvc3RpbmcuQWRkUmVxdWVzdEluZm9SZXF1ZXN0"))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())

class StartRequestLogRequest(ProtocolBuffer.ProtocolMessage):
  has_request_id_ = 0
  request_id_ = ""
  has_user_request_id_ = 0
  user_request_id_ = ""
  has_ip_ = 0
  ip_ = ""
  has_app_id_ = 0
  app_id_ = ""
  has_version_id_ = 0
  version_id_ = ""
  has_nickname_ = 0
  nickname_ = ""
  has_user_agent_ = 0
  user_agent_ = ""
  has_host_ = 0
  host_ = ""
  has_method_ = 0
  method_ = ""
  has_resource_ = 0
  resource_ = ""
  has_http_version_ = 0
  http_version_ = ""
  has_start_time_ = 0
  start_time_ = 0
  has_module_ = 0
  module_ = ""

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def request_id(self): return self.request_id_

  def set_request_id(self, x):
    self.has_request_id_ = 1
    self.request_id_ = x

  def clear_request_id(self):
    if self.has_request_id_:
      self.has_request_id_ = 0
      self.request_id_ = ""

  def has_request_id(self): return self.has_request_id_

  def user_request_id(self): return self.user_request_id_

  def set_user_request_id(self, x):
    self.has_user_request_id_ = 1
    self.user_request_id_ = x

  def clear_user_request_id(self):
    if self.has_user_request_id_:
      self.has_user_request_id_ = 0
      self.user_request_id_ = ""

  def has_user_request_id(self): return self.has_user_request_id_

  def ip(self): return self.ip_

  def set_ip(self, x):
    self.has_ip_ = 1
    self.ip_ = x

  def clear_ip(self):
    if self.has_ip_:
      self.has_ip_ = 0
      self.ip_ = ""

  def has_ip(self): return self.has_ip_

  def app_id(self): return self.app_id_

  def set_app_id(self, x):
    self.has_app_id_ = 1
    self.app_id_ = x

  def clear_app_id(self):
    if self.has_app_id_:
      self.has_app_id_ = 0
      self.app_id_ = ""

  def has_app_id(self): return self.has_app_id_

  def version_id(self): return self.version_id_

  def set_version_id(self, x):
    self.has_version_id_ = 1
    self.version_id_ = x

  def clear_version_id(self):
    if self.has_version_id_:
      self.has_version_id_ = 0
      self.version_id_ = ""

  def has_version_id(self): return self.has_version_id_

  def nickname(self): return self.nickname_

  def set_nickname(self, x):
    self.has_nickname_ = 1
    self.nickname_ = x

  def clear_nickname(self):
    if self.has_nickname_:
      self.has_nickname_ = 0
      self.nickname_ = ""

  def has_nickname(self): return self.has_nickname_

  def user_agent(self): return self.user_agent_

  def set_user_agent(self, x):
    self.has_user_agent_ = 1
    self.user_agent_ = x

  def clear_user_agent(self):
    if self.has_user_agent_:
      self.has_user_agent_ = 0
      self.user_agent_ = ""

  def has_user_agent(self): return self.has_user_agent_

  def host(self): return self.host_

  def set_host(self, x):
    self.has_host_ = 1
    self.host_ = x

  def clear_host(self):
    if self.has_host_:
      self.has_host_ = 0
      self.host_ = ""

  def has_host(self): return self.has_host_

  def method(self): return self.method_

  def set_method(self, x):
    self.has_method_ = 1
    self.method_ = x

  def clear_method(self):
    if self.has_method_:
      self.has_method_ = 0
      self.method_ = ""

  def has_method(self): return self.has_method_

  def resource(self): return self.resource_

  def set_resource(self, x):
    self.has_resource_ = 1
    self.resource_ = x

  def clear_resource(self):
    if self.has_resource_:
      self.has_resource_ = 0
      self.resource_ = ""

  def has_resource(self): return self.has_resource_

  def http_version(self): return self.http_version_

  def set_http_version(self, x):
    self.has_http_version_ = 1
    self.http_version_ = x

  def clear_http_version(self):
    if self.has_http_version_:
      self.has_http_version_ = 0
      self.http_version_ = ""

  def has_http_version(self): return self.has_http_version_

  def start_time(self): return self.start_time_

  def set_start_time(self, x):
    self.has_start_time_ = 1
    self.start_time_ = x

  def clear_start_time(self):
    if self.has_start_time_:
      self.has_start_time_ = 0
      self.start_time_ = 0

  def has_start_time(self): return self.has_start_time_

  def module(self): return self.module_

  def set_module(self, x):
    self.has_module_ = 1
    self.module_ = x

  def clear_module(self):
    if self.has_module_:
      self.has_module_ = 0
      self.module_ = ""

  def has_module(self): return self.has_module_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_request_id()): self.set_request_id(x.request_id())
    if (x.has_user_request_id()): self.set_user_request_id(x.user_request_id())
    if (x.has_ip()): self.set_ip(x.ip())
    if (x.has_app_id()): self.set_app_id(x.app_id())
    if (x.has_version_id()): self.set_version_id(x.version_id())
    if (x.has_nickname()): self.set_nickname(x.nickname())
    if (x.has_user_agent()): self.set_user_agent(x.user_agent())
    if (x.has_host()): self.set_host(x.host())
    if (x.has_method()): self.set_method(x.method())
    if (x.has_resource()): self.set_resource(x.resource())
    if (x.has_http_version()): self.set_http_version(x.http_version())
    if (x.has_start_time()): self.set_start_time(x.start_time())
    if (x.has_module()): self.set_module(x.module())

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.StartRequestLogRequest', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.StartRequestLogRequest')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.StartRequestLogRequest')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.StartRequestLogRequest', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.StartRequestLogRequest', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.StartRequestLogRequest', s)


  def Equals(self, x):
    if x is self: return 1
    if self.has_request_id_ != x.has_request_id_: return 0
    if self.has_request_id_ and self.request_id_ != x.request_id_: return 0
    if self.has_user_request_id_ != x.has_user_request_id_: return 0
    if self.has_user_request_id_ and self.user_request_id_ != x.user_request_id_: return 0
    if self.has_ip_ != x.has_ip_: return 0
    if self.has_ip_ and self.ip_ != x.ip_: return 0
    if self.has_app_id_ != x.has_app_id_: return 0
    if self.has_app_id_ and self.app_id_ != x.app_id_: return 0
    if self.has_version_id_ != x.has_version_id_: return 0
    if self.has_version_id_ and self.version_id_ != x.version_id_: return 0
    if self.has_nickname_ != x.has_nickname_: return 0
    if self.has_nickname_ and self.nickname_ != x.nickname_: return 0
    if self.has_user_agent_ != x.has_user_agent_: return 0
    if self.has_user_agent_ and self.user_agent_ != x.user_agent_: return 0
    if self.has_host_ != x.has_host_: return 0
    if self.has_host_ and self.host_ != x.host_: return 0
    if self.has_method_ != x.has_method_: return 0
    if self.has_method_ and self.method_ != x.method_: return 0
    if self.has_resource_ != x.has_resource_: return 0
    if self.has_resource_ and self.resource_ != x.resource_: return 0
    if self.has_http_version_ != x.has_http_version_: return 0
    if self.has_http_version_ and self.http_version_ != x.http_version_: return 0
    if self.has_start_time_ != x.has_start_time_: return 0
    if self.has_start_time_ and self.start_time_ != x.start_time_: return 0
    if self.has_module_ != x.has_module_: return 0
    if self.has_module_ and self.module_ != x.module_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_request_id_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: request_id not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(len(self.request_id_))
    if (self.has_user_request_id_): n += 1 + self.lengthString(len(self.user_request_id_))
    if (self.has_ip_): n += 1 + self.lengthString(len(self.ip_))
    if (self.has_app_id_): n += 1 + self.lengthString(len(self.app_id_))
    if (self.has_version_id_): n += 1 + self.lengthString(len(self.version_id_))
    if (self.has_nickname_): n += 1 + self.lengthString(len(self.nickname_))
    if (self.has_user_agent_): n += 1 + self.lengthString(len(self.user_agent_))
    if (self.has_host_): n += 1 + self.lengthString(len(self.host_))
    if (self.has_method_): n += 1 + self.lengthString(len(self.method_))
    if (self.has_resource_): n += 1 + self.lengthString(len(self.resource_))
    if (self.has_http_version_): n += 1 + self.lengthString(len(self.http_version_))
    if (self.has_start_time_): n += 1 + self.lengthVarInt64(self.start_time_)
    if (self.has_module_): n += 1 + self.lengthString(len(self.module_))
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_request_id_):
      n += 1
      n += self.lengthString(len(self.request_id_))
    if (self.has_user_request_id_): n += 1 + self.lengthString(len(self.user_request_id_))
    if (self.has_ip_): n += 1 + self.lengthString(len(self.ip_))
    if (self.has_app_id_): n += 1 + self.lengthString(len(self.app_id_))
    if (self.has_version_id_): n += 1 + self.lengthString(len(self.version_id_))
    if (self.has_nickname_): n += 1 + self.lengthString(len(self.nickname_))
    if (self.has_user_agent_): n += 1 + self.lengthString(len(self.user_agent_))
    if (self.has_host_): n += 1 + self.lengthString(len(self.host_))
    if (self.has_method_): n += 1 + self.lengthString(len(self.method_))
    if (self.has_resource_): n += 1 + self.lengthString(len(self.resource_))
    if (self.has_http_version_): n += 1 + self.lengthString(len(self.http_version_))
    if (self.has_start_time_): n += 1 + self.lengthVarInt64(self.start_time_)
    if (self.has_module_): n += 1 + self.lengthString(len(self.module_))
    return n

  def Clear(self):
    self.clear_request_id()
    self.clear_user_request_id()
    self.clear_ip()
    self.clear_app_id()
    self.clear_version_id()
    self.clear_nickname()
    self.clear_user_agent()
    self.clear_host()
    self.clear_method()
    self.clear_resource()
    self.clear_http_version()
    self.clear_start_time()
    self.clear_module()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putPrefixedString(self.request_id_)
    if (self.has_user_request_id_):
      out.putVarInt32(18)
      out.putPrefixedString(self.user_request_id_)
    if (self.has_ip_):
      out.putVarInt32(26)
      out.putPrefixedString(self.ip_)
    if (self.has_app_id_):
      out.putVarInt32(34)
      out.putPrefixedString(self.app_id_)
    if (self.has_version_id_):
      out.putVarInt32(42)
      out.putPrefixedString(self.version_id_)
    if (self.has_nickname_):
      out.putVarInt32(50)
      out.putPrefixedString(self.nickname_)
    if (self.has_user_agent_):
      out.putVarInt32(58)
      out.putPrefixedString(self.user_agent_)
    if (self.has_host_):
      out.putVarInt32(66)
      out.putPrefixedString(self.host_)
    if (self.has_method_):
      out.putVarInt32(74)
      out.putPrefixedString(self.method_)
    if (self.has_resource_):
      out.putVarInt32(82)
      out.putPrefixedString(self.resource_)
    if (self.has_http_version_):
      out.putVarInt32(90)
      out.putPrefixedString(self.http_version_)
    if (self.has_start_time_):
      out.putVarInt32(96)
      out.putVarInt64(self.start_time_)
    if (self.has_module_):
      out.putVarInt32(106)
      out.putPrefixedString(self.module_)

  def OutputPartial(self, out):
    if (self.has_request_id_):
      out.putVarInt32(10)
      out.putPrefixedString(self.request_id_)
    if (self.has_user_request_id_):
      out.putVarInt32(18)
      out.putPrefixedString(self.user_request_id_)
    if (self.has_ip_):
      out.putVarInt32(26)
      out.putPrefixedString(self.ip_)
    if (self.has_app_id_):
      out.putVarInt32(34)
      out.putPrefixedString(self.app_id_)
    if (self.has_version_id_):
      out.putVarInt32(42)
      out.putPrefixedString(self.version_id_)
    if (self.has_nickname_):
      out.putVarInt32(50)
      out.putPrefixedString(self.nickname_)
    if (self.has_user_agent_):
      out.putVarInt32(58)
      out.putPrefixedString(self.user_agent_)
    if (self.has_host_):
      out.putVarInt32(66)
      out.putPrefixedString(self.host_)
    if (self.has_method_):
      out.putVarInt32(74)
      out.putPrefixedString(self.method_)
    if (self.has_resource_):
      out.putVarInt32(82)
      out.putPrefixedString(self.resource_)
    if (self.has_http_version_):
      out.putVarInt32(90)
      out.putPrefixedString(self.http_version_)
    if (self.has_start_time_):
      out.putVarInt32(96)
      out.putVarInt64(self.start_time_)
    if (self.has_module_):
      out.putVarInt32(106)
      out.putPrefixedString(self.module_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_request_id(d.getPrefixedString())
        continue
      if tt == 18:
        self.set_user_request_id(d.getPrefixedString())
        continue
      if tt == 26:
        self.set_ip(d.getPrefixedString())
        continue
      if tt == 34:
        self.set_app_id(d.getPrefixedString())
        continue
      if tt == 42:
        self.set_version_id(d.getPrefixedString())
        continue
      if tt == 50:
        self.set_nickname(d.getPrefixedString())
        continue
      if tt == 58:
        self.set_user_agent(d.getPrefixedString())
        continue
      if tt == 66:
        self.set_host(d.getPrefixedString())
        continue
      if tt == 74:
        self.set_method(d.getPrefixedString())
        continue
      if tt == 82:
        self.set_resource(d.getPrefixedString())
        continue
      if tt == 90:
        self.set_http_version(d.getPrefixedString())
        continue
      if tt == 96:
        self.set_start_time(d.getVarInt64())
        continue
      if tt == 106:
        self.set_module(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_request_id_: res+=prefix+("request_id: %s\n" % self.DebugFormatString(self.request_id_))
    if self.has_user_request_id_: res+=prefix+("user_request_id: %s\n" % self.DebugFormatString(self.user_request_id_))
    if self.has_ip_: res+=prefix+("ip: %s\n" % self.DebugFormatString(self.ip_))
    if self.has_app_id_: res+=prefix+("app_id: %s\n" % self.DebugFormatString(self.app_id_))
    if self.has_version_id_: res+=prefix+("version_id: %s\n" % self.DebugFormatString(self.version_id_))
    if self.has_nickname_: res+=prefix+("nickname: %s\n" % self.DebugFormatString(self.nickname_))
    if self.has_user_agent_: res+=prefix+("user_agent: %s\n" % self.DebugFormatString(self.user_agent_))
    if self.has_host_: res+=prefix+("host: %s\n" % self.DebugFormatString(self.host_))
    if self.has_method_: res+=prefix+("method: %s\n" % self.DebugFormatString(self.method_))
    if self.has_resource_: res+=prefix+("resource: %s\n" % self.DebugFormatString(self.resource_))
    if self.has_http_version_: res+=prefix+("http_version: %s\n" % self.DebugFormatString(self.http_version_))
    if self.has_start_time_: res+=prefix+("start_time: %s\n" % self.DebugFormatInt64(self.start_time_))
    if self.has_module_: res+=prefix+("module: %s\n" % self.DebugFormatString(self.module_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  krequest_id = 1
  kuser_request_id = 2
  kip = 3
  kapp_id = 4
  kversion_id = 5
  knickname = 6
  kuser_agent = 7
  khost = 8
  kmethod = 9
  kresource = 10
  khttp_version = 11
  kstart_time = 12
  kmodule = 13

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "request_id",
    2: "user_request_id",
    3: "ip",
    4: "app_id",
    5: "version_id",
    6: "nickname",
    7: "user_agent",
    8: "host",
    9: "method",
    10: "resource",
    11: "http_version",
    12: "start_time",
    13: "module",
  }, 13)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
    2: ProtocolBuffer.Encoder.STRING,
    3: ProtocolBuffer.Encoder.STRING,
    4: ProtocolBuffer.Encoder.STRING,
    5: ProtocolBuffer.Encoder.STRING,
    6: ProtocolBuffer.Encoder.STRING,
    7: ProtocolBuffer.Encoder.STRING,
    8: ProtocolBuffer.Encoder.STRING,
    9: ProtocolBuffer.Encoder.STRING,
    10: ProtocolBuffer.Encoder.STRING,
    11: ProtocolBuffer.Encoder.STRING,
    12: ProtocolBuffer.Encoder.NUMERIC,
    13: ProtocolBuffer.Encoder.STRING,
  }, 13, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.StartRequestLogRequest'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjBhcHBob3N0aW5nL2FwaS9sb2dzZXJ2aWNlL2xvZ19zdHViX3NlcnZpY2UucHJvdG8KIWFwcGhvc3RpbmcuU3RhcnRSZXF1ZXN0TG9nUmVxdWVzdBMaCnJlcXVlc3RfaWQgASgCMAk4AhQTGg91c2VyX3JlcXVlc3RfaWQgAigCMAk4ARQTGgJpcCADKAIwCTgBFBMaBmFwcF9pZCAEKAIwCTgBFBMaCnZlcnNpb25faWQgBSgCMAk4ARQTGghuaWNrbmFtZSAGKAIwCTgBFBMaCnVzZXJfYWdlbnQgBygCMAk4ARQTGgRob3N0IAgoAjAJOAEUExoGbWV0aG9kIAkoAjAJOAEUExoIcmVzb3VyY2UgCigCMAk4ARQTGgxodHRwX3ZlcnNpb24gCygCMAk4ARQTGgpzdGFydF90aW1lIAwoADADOAEUExoGbW9kdWxlIA0oAjAJOAEUwgEgYXBwaG9zdGluZy5BZGRSZXF1ZXN0SW5mb1JlcXVlc3Q="))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())

class EndRequestLogRequest(ProtocolBuffer.ProtocolMessage):
  has_request_id_ = 0
  request_id_ = ""
  has_status_ = 0
  status_ = 0
  has_response_size_ = 0
  response_size_ = 0

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def request_id(self): return self.request_id_

  def set_request_id(self, x):
    self.has_request_id_ = 1
    self.request_id_ = x

  def clear_request_id(self):
    if self.has_request_id_:
      self.has_request_id_ = 0
      self.request_id_ = ""

  def has_request_id(self): return self.has_request_id_

  def status(self): return self.status_

  def set_status(self, x):
    self.has_status_ = 1
    self.status_ = x

  def clear_status(self):
    if self.has_status_:
      self.has_status_ = 0
      self.status_ = 0

  def has_status(self): return self.has_status_

  def response_size(self): return self.response_size_

  def set_response_size(self, x):
    self.has_response_size_ = 1
    self.response_size_ = x

  def clear_response_size(self):
    if self.has_response_size_:
      self.has_response_size_ = 0
      self.response_size_ = 0

  def has_response_size(self): return self.has_response_size_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_request_id()): self.set_request_id(x.request_id())
    if (x.has_status()): self.set_status(x.status())
    if (x.has_response_size()): self.set_response_size(x.response_size())

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.EndRequestLogRequest', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.EndRequestLogRequest')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.EndRequestLogRequest')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.EndRequestLogRequest', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.EndRequestLogRequest', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.EndRequestLogRequest', s)


  def Equals(self, x):
    if x is self: return 1
    if self.has_request_id_ != x.has_request_id_: return 0
    if self.has_request_id_ and self.request_id_ != x.request_id_: return 0
    if self.has_status_ != x.has_status_: return 0
    if self.has_status_ and self.status_ != x.status_: return 0
    if self.has_response_size_ != x.has_response_size_: return 0
    if self.has_response_size_ and self.response_size_ != x.response_size_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_request_id_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: request_id not set.')
    if (not self.has_status_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: status not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(len(self.request_id_))
    n += self.lengthVarInt64(self.status_)
    if (self.has_response_size_): n += 1 + self.lengthVarInt64(self.response_size_)
    return n + 2

  def ByteSizePartial(self):
    n = 0
    if (self.has_request_id_):
      n += 1
      n += self.lengthString(len(self.request_id_))
    if (self.has_status_):
      n += 1
      n += self.lengthVarInt64(self.status_)
    if (self.has_response_size_): n += 1 + self.lengthVarInt64(self.response_size_)
    return n

  def Clear(self):
    self.clear_request_id()
    self.clear_status()
    self.clear_response_size()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putPrefixedString(self.request_id_)
    out.putVarInt32(16)
    out.putVarInt32(self.status_)
    if (self.has_response_size_):
      out.putVarInt32(24)
      out.putVarInt32(self.response_size_)

  def OutputPartial(self, out):
    if (self.has_request_id_):
      out.putVarInt32(10)
      out.putPrefixedString(self.request_id_)
    if (self.has_status_):
      out.putVarInt32(16)
      out.putVarInt32(self.status_)
    if (self.has_response_size_):
      out.putVarInt32(24)
      out.putVarInt32(self.response_size_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_request_id(d.getPrefixedString())
        continue
      if tt == 16:
        self.set_status(d.getVarInt32())
        continue
      if tt == 24:
        self.set_response_size(d.getVarInt32())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_request_id_: res+=prefix+("request_id: %s\n" % self.DebugFormatString(self.request_id_))
    if self.has_status_: res+=prefix+("status: %s\n" % self.DebugFormatInt32(self.status_))
    if self.has_response_size_: res+=prefix+("response_size: %s\n" % self.DebugFormatInt32(self.response_size_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  krequest_id = 1
  kstatus = 2
  kresponse_size = 3

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "request_id",
    2: "status",
    3: "response_size",
  }, 3)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
    2: ProtocolBuffer.Encoder.NUMERIC,
    3: ProtocolBuffer.Encoder.NUMERIC,
  }, 3, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.EndRequestLogRequest'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjBhcHBob3N0aW5nL2FwaS9sb2dzZXJ2aWNlL2xvZ19zdHViX3NlcnZpY2UucHJvdG8KH2FwcGhvc3RpbmcuRW5kUmVxdWVzdExvZ1JlcXVlc3QTGgpyZXF1ZXN0X2lkIAEoAjAJOAIUExoGc3RhdHVzIAIoADAFOAIUExoNcmVzcG9uc2Vfc2l6ZSADKAAwBTgBFMIBIGFwcGhvc3RpbmcuQWRkUmVxdWVzdEluZm9SZXF1ZXN0"))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())



class LogStubServiceStub(object):
  """Makes Stubby RPC calls to a LogStubService server."""

  __metaclass__ = abc.ABCMeta

  __slots__ = ()

  @abc.abstractmethod
  def AddRequestInfo(self, request, rpc=None, callback=None, response=None):
    """Make a AddRequestInfo RPC call.

    Args:
      request: a AddRequestInfoRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto if callback is None. Otherwise, returns None.
    """
    raise NotImplementedError()

  @abc.abstractmethod
  def AddAppLogLine(self, request, rpc=None, callback=None, response=None):
    """Make a AddAppLogLine RPC call.

    Args:
      request: a AddAppLogLineRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto if callback is None. Otherwise, returns None.
    """
    raise NotImplementedError()

  @abc.abstractmethod
  def EndRequestLog(self, request, rpc=None, callback=None, response=None):
    """Make a EndRequestLog RPC call.

    Args:
      request: a EndRequestLogRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto if callback is None. Otherwise, returns None.
    """
    raise NotImplementedError()

  @abc.abstractmethod
  def StartRequestLog(self, request, rpc=None, callback=None, response=None):
    """Make a StartRequestLog RPC call.

    Args:
      request: a StartRequestLogRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto if callback is None. Otherwise, returns None.
    """
    raise NotImplementedError()


class _LogStubService_ClientBaseStub(
    LogStubServiceStub, _client_stub_base_class):
  """Makes Stubby RPC calls to a LogStubService server."""

  __slots__ = (
      '_protorpc_AddRequestInfo', '_full_name_AddRequestInfo',
      '_protorpc_AddAppLogLine', '_full_name_AddAppLogLine',
      '_protorpc_EndRequestLog', '_full_name_EndRequestLog',
      '_protorpc_StartRequestLog', '_full_name_StartRequestLog',
  )

  def __init__(self, rpc_stub, rpc_factory=None):
    super(_LogStubService_ClientBaseStub, self).__init__(
        None, inject_stub=rpc_stub, rpc_factory=rpc_factory)

    self._protorpc_AddRequestInfo = pywraprpc.RPC()
    self._full_name_AddRequestInfo = self._stub.GetFullMethodName(
        'AddRequestInfo')

    self._protorpc_AddAppLogLine = pywraprpc.RPC()
    self._full_name_AddAppLogLine = self._stub.GetFullMethodName(
        'AddAppLogLine')

    self._protorpc_EndRequestLog = pywraprpc.RPC()
    self._full_name_EndRequestLog = self._stub.GetFullMethodName(
        'EndRequestLog')

    self._protorpc_StartRequestLog = pywraprpc.RPC()
    self._full_name_StartRequestLog = self._stub.GetFullMethodName(
        'StartRequestLog')

  def AddRequestInfo(self, request, rpc=None, callback=None, response=None):
    """Make a AddRequestInfo RPC call.

    Args:
      request: a AddRequestInfoRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto if callback is None. Otherwise, returns None.
    """

    if response is None:
      response = google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto
    return self._MakeCall(rpc,
                          self._full_name_AddRequestInfo,
                          'AddRequestInfo',
                          request,
                          response,
                          callback,
                          self._protorpc_AddRequestInfo,
                          package_name='apphosting')

  def AddAppLogLine(self, request, rpc=None, callback=None, response=None):
    """Make a AddAppLogLine RPC call.

    Args:
      request: a AddAppLogLineRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto if callback is None. Otherwise, returns None.
    """

    if response is None:
      response = google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto
    return self._MakeCall(rpc,
                          self._full_name_AddAppLogLine,
                          'AddAppLogLine',
                          request,
                          response,
                          callback,
                          self._protorpc_AddAppLogLine,
                          package_name='apphosting')

  def EndRequestLog(self, request, rpc=None, callback=None, response=None):
    """Make a EndRequestLog RPC call.

    Args:
      request: a EndRequestLogRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto if callback is None. Otherwise, returns None.
    """

    if response is None:
      response = google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto
    return self._MakeCall(rpc,
                          self._full_name_EndRequestLog,
                          'EndRequestLog',
                          request,
                          response,
                          callback,
                          self._protorpc_EndRequestLog,
                          package_name='apphosting')

  def StartRequestLog(self, request, rpc=None, callback=None, response=None):
    """Make a StartRequestLog RPC call.

    Args:
      request: a StartRequestLogRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto if callback is None. Otherwise, returns None.
    """

    if response is None:
      response = google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto
    return self._MakeCall(rpc,
                          self._full_name_StartRequestLog,
                          'StartRequestLog',
                          request,
                          response,
                          callback,
                          self._protorpc_StartRequestLog,
                          package_name='apphosting')


class _LogStubService_ClientStub(_LogStubService_ClientBaseStub):
  __slots__ = ('_params',)
  def __init__(self, rpc_stub_parameters, service_name, rpc_factory=None):
    if service_name is None:
      service_name = 'LogStubService'
    stub = pywraprpc.RPC_GenericStub(service_name, rpc_stub_parameters)
    super(_LogStubService_ClientStub, self).__init__(stub, rpc_factory=rpc_factory)
    self._params = rpc_stub_parameters


class _LogStubService_RPC2ClientStub(_LogStubService_ClientBaseStub):
  __slots__ = ()
  def __init__(self, server, channel, service_name, rpc_factory=None):
    if service_name is None:
      service_name = 'LogStubService'
    if channel is None:
      if server is None:
        raise RuntimeError('Invalid argument combination to create a stub')
      channel = pywraprpc.NewClientChannel(server)
    elif channel.version() == 1:
      raise RuntimeError('Expecting an RPC2 channel to create the stub')
    stub = pywraprpc.RPC_GenericStub(service_name, channel)
    super(_LogStubService_RPC2ClientStub, self).__init__(stub, rpc_factory=rpc_factory)


class LogStubService(_server_stub_base_class):
  """Base class for LogStubService Stubby servers."""

  @classmethod
  def _MethodSignatures(cls):
    """Returns a dict of {<method-name>: (<request-type>, <response-type>)}."""
    return {
      'AddRequestInfo': (AddRequestInfoRequest, google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto),
      'AddAppLogLine': (AddAppLogLineRequest, google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto),
      'EndRequestLog': (EndRequestLogRequest, google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto),
      'StartRequestLog': (StartRequestLogRequest, google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto),
      }

  @classmethod
  def _StreamMethodSignatures(cls):
    """Returns a dict of {<method-name>: (<request-type>, <stream-type>, <response-type>)}."""
    return {
      }

  def __init__(self, *args, **kwargs):
    """Creates a Stubby RPC server.

    The arguments to this constructor are the same as the arguments to
    BaseRpcServer.__init__ in rpcserver.py *MINUS* export_name. This
    constructor passes its own value for export_name to
    BaseRpcServer.__init__, so callers of this constructor should only
    pass to this constructor values corresponding to
    BaseRpcServer.__init__'s remaining arguments.
    """
    if _server_stub_base_class is object:
      raise NotImplementedError('Add //net/rpc/python:rpcserver as a '
                                'dependency for Stubby server support.')
    _server_stub_base_class.__init__(self, 'apphosting.LogStubService', *args, **kwargs)

  @staticmethod
  def NewStub(rpc_stub_parameters, service_name=None, rpc_factory=None):
    """USE NewRPC2Stub INSTEAD."""
    if _client_stub_base_class is object:
      raise RuntimeError('Add //net/rpc/python as a dependency to use Stubby')
    return _LogStubService_ClientStub(
        rpc_stub_parameters, service_name, rpc_factory=rpc_factory)

  @staticmethod
  def NewRPC2Stub(
      server=None, channel=None, service_name=None, rpc_factory=None):
    """Creates a new LogStubService Stubby2 client stub.

    Args:
      server: host:port or bns address (favor passing a channel instead).
      channel: directly use a channel to create a stub. Will ignore server
          argument if this is specified.
      service_name: the service name used by the Stubby server.
      rpc_factory: the rpc factory to use if no rpc argument is specified.

    Returns:
     A LogStubServiceStub to be used to invoke RPCs.
    """

    if _client_stub_base_class is object:
      raise RuntimeError('Add //net/rpc/python:proto_python_api_2_stub (or maybe //net/rpc/python:proto_python_api_1_stub, but eww and b/67959631) as a dependency to create Stubby stubs')
    return _LogStubService_RPC2ClientStub(
        server, channel, service_name, rpc_factory=rpc_factory)

  def AddRequestInfo(self, rpc, request, response):
    """Handles a AddRequestInfo RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a AddRequestInfoRequest that contains the client request
      response: a google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto that should be modified to send the response
    """
    raise NotImplementedError()


  def AddAppLogLine(self, rpc, request, response):
    """Handles a AddAppLogLine RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a AddAppLogLineRequest that contains the client request
      response: a google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto that should be modified to send the response
    """
    raise NotImplementedError()


  def EndRequestLog(self, rpc, request, response):
    """Handles a EndRequestLog RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a EndRequestLogRequest that contains the client request
      response: a google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto that should be modified to send the response
    """
    raise NotImplementedError()


  def StartRequestLog(self, rpc, request, response):
    """Handles a StartRequestLog RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a StartRequestLogRequest that contains the client request
      response: a google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto that should be modified to send the response
    """
    raise NotImplementedError()

  def _AddMethodAttributes(self):
    """Sets attributes on Python RPC handlers.

    See BaseRpcServer in rpcserver.py for details.
    """
    rpcserver._GetHandlerDecorator(
        getattr(self.AddRequestInfo, '__func__'),
        AddRequestInfoRequest,
        google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto,
        None,
        'INTEGRITY')
    rpcserver._GetHandlerDecorator(
        getattr(self.AddAppLogLine, '__func__'),
        AddAppLogLineRequest,
        google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto,
        None,
        'INTEGRITY')
    rpcserver._GetHandlerDecorator(
        getattr(self.EndRequestLog, '__func__'),
        EndRequestLogRequest,
        google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto,
        None,
        'INTEGRITY')
    rpcserver._GetHandlerDecorator(
        getattr(self.StartRequestLog, '__func__'),
        StartRequestLogRequest,
        google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto,
        None,
        'INTEGRITY')

if _extension_runtime:
  pass

__all__ = ['AddRequestInfoRequest','AddAppLogLineRequest','StartRequestLogRequest','EndRequestLogRequest','LogStubService']
