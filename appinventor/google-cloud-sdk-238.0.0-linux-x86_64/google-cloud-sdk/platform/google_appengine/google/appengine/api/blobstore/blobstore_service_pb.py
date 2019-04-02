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
class BlobstoreServiceError(ProtocolBuffer.ProtocolMessage):


  OK           =    0
  INTERNAL_ERROR =    1
  URL_TOO_LONG =    2
  PERMISSION_DENIED =    3
  BLOB_NOT_FOUND =    4
  DATA_INDEX_OUT_OF_RANGE =    5
  BLOB_FETCH_SIZE_TOO_LARGE =    6
  ARGUMENT_OUT_OF_RANGE =    8
  INVALID_BLOB_KEY =    9

  _ErrorCode_NAMES = {
    0: "OK",
    1: "INTERNAL_ERROR",
    2: "URL_TOO_LONG",
    3: "PERMISSION_DENIED",
    4: "BLOB_NOT_FOUND",
    5: "DATA_INDEX_OUT_OF_RANGE",
    6: "BLOB_FETCH_SIZE_TOO_LARGE",
    8: "ARGUMENT_OUT_OF_RANGE",
    9: "INVALID_BLOB_KEY",
  }

  def ErrorCode_Name(cls, x): return cls._ErrorCode_NAMES.get(x, "")
  ErrorCode_Name = classmethod(ErrorCode_Name)


  def __init__(self, contents=None):
    pass
    if contents is not None: self.MergeFromString(contents)


  def MergeFrom(self, x):
    assert x is not self

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.BlobstoreServiceError', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.BlobstoreServiceError')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.BlobstoreServiceError')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.BlobstoreServiceError', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.BlobstoreServiceError', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.BlobstoreServiceError', s)


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
  _PROTO_DESCRIPTOR_NAME = 'apphosting.BlobstoreServiceError'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjBhcHBob3N0aW5nL2FwaS9ibG9ic3RvcmUvYmxvYnN0b3JlX3NlcnZpY2UucHJvdG8KIGFwcGhvc3RpbmcuQmxvYnN0b3JlU2VydmljZUVycm9yc3oJRXJyb3JDb2RliwGSAQJPS5gBAIwBiwGSAQ5JTlRFUk5BTF9FUlJPUpgBAYwBiwGSAQxVUkxfVE9PX0xPTkeYAQKMAYsBkgERUEVSTUlTU0lPTl9ERU5JRUSYAQOMAYsBkgEOQkxPQl9OT1RfRk9VTkSYAQSMAYsBkgEXREFUQV9JTkRFWF9PVVRfT0ZfUkFOR0WYAQWMAYsBkgEZQkxPQl9GRVRDSF9TSVpFX1RPT19MQVJHRZgBBowBiwGSARVBUkdVTUVOVF9PVVRfT0ZfUkFOR0WYAQiMAYsBkgEQSU5WQUxJRF9CTE9CX0tFWZgBCYwBdLoBhQ0KMGFwcGhvc3RpbmcvYXBpL2Jsb2JzdG9yZS9ibG9ic3RvcmVfc2VydmljZS5wcm90bxIKYXBwaG9zdGluZxodYXBwaG9zdGluZy9hcGkvYXBpX2Jhc2UucHJvdG8i6wEKFUJsb2JzdG9yZVNlcnZpY2VFcnJvciLRAQoJRXJyb3JDb2RlEgYKAk9LEAASEgoOSU5URVJOQUxfRVJST1IQARIQCgxVUkxfVE9PX0xPTkcQAhIVChFQRVJNSVNTSU9OX0RFTklFRBADEhIKDkJMT0JfTk9UX0ZPVU5EEAQSGwoXREFUQV9JTkRFWF9PVVRfT0ZfUkFOR0UQBRIdChlCTE9CX0ZFVENIX1NJWkVfVE9PX0xBUkdFEAYSGQoVQVJHVU1FTlRfT1VUX09GX1JBTkdFEAgSFAoQSU5WQUxJRF9CTE9CX0tFWRAJIq4BChZDcmVhdGVVcGxvYWRVUkxSZXF1ZXN0EhQKDHN1Y2Nlc3NfcGF0aBgBIAIoCRIdChVtYXhfdXBsb2FkX3NpemVfYnl0ZXMYAiABKAMSJgoebWF4X3VwbG9hZF9zaXplX3Blcl9ibG9iX2J5dGVzGAMgASgDEhYKDmdzX2J1Y2tldF9uYW1lGAQgASgJEh8KF3VybF9leHBpcnlfdGltZV9zZWNvbmRzGAUgASgFIiYKF0NyZWF0ZVVwbG9hZFVSTFJlc3BvbnNlEgsKA3VybBgBIAIoCSI0ChFEZWxldGVCbG9iUmVxdWVzdBIQCghibG9iX2tleRgBIAMoCRINCgV0b2tlbhgCIAEoCSJMChBGZXRjaERhdGFSZXF1ZXN0EhAKCGJsb2Jfa2V5GAEgAigJEhMKC3N0YXJ0X2luZGV4GAIgAigDEhEKCWVuZF9pbmRleBgDIAIoAyImChFGZXRjaERhdGFSZXNwb25zZRIRCgRkYXRhGOgHIAIoDEICCAEiTgoQQ2xvbmVCbG9iUmVxdWVzdBIQCghibG9iX2tleRgBIAIoDBIRCgltaW1lX3R5cGUYAiACKAwSFQoNdGFyZ2V0X2FwcF9pZBgDIAIoDCIlChFDbG9uZUJsb2JSZXNwb25zZRIQCghibG9iX2tleRgBIAIoDCIoChREZWNvZGVCbG9iS2V5UmVxdWVzdBIQCghibG9iX2tleRgBIAMoCSIoChVEZWNvZGVCbG9iS2V5UmVzcG9uc2USDwoHZGVjb2RlZBgBIAMoCSI4CiRDcmVhdGVFbmNvZGVkR29vZ2xlU3RvcmFnZUtleVJlcXVlc3QSEAoIZmlsZW5hbWUYASACKAkiOQolQ3JlYXRlRW5jb2RlZEdvb2dsZVN0b3JhZ2VLZXlSZXNwb25zZRIQCghibG9iX2tleRgBIAIoCTK0BAoQQmxvYnN0b3JlU2VydmljZRJcCg9DcmVhdGVVcGxvYWRVUkwSIi5hcHBob3N0aW5nLkNyZWF0ZVVwbG9hZFVSTFJlcXVlc3QaIy5hcHBob3N0aW5nLkNyZWF0ZVVwbG9hZFVSTFJlc3BvbnNlIgASSQoKRGVsZXRlQmxvYhIdLmFwcGhvc3RpbmcuRGVsZXRlQmxvYlJlcXVlc3QaGi5hcHBob3N0aW5nLmJhc2UuVm9pZFByb3RvIgASSgoJRmV0Y2hEYXRhEhwuYXBwaG9zdGluZy5GZXRjaERhdGFSZXF1ZXN0Gh0uYXBwaG9zdGluZy5GZXRjaERhdGFSZXNwb25zZSIAEkoKCUNsb25lQmxvYhIcLmFwcGhvc3RpbmcuQ2xvbmVCbG9iUmVxdWVzdBodLmFwcGhvc3RpbmcuQ2xvbmVCbG9iUmVzcG9uc2UiABJWCg1EZWNvZGVCbG9iS2V5EiAuYXBwaG9zdGluZy5EZWNvZGVCbG9iS2V5UmVxdWVzdBohLmFwcGhvc3RpbmcuRGVjb2RlQmxvYktleVJlc3BvbnNlIgAShgEKHUNyZWF0ZUVuY29kZWRHb29nbGVTdG9yYWdlS2V5EjAuYXBwaG9zdGluZy5DcmVhdGVFbmNvZGVkR29vZ2xlU3RvcmFnZUtleVJlcXVlc3QaMS5hcHBob3N0aW5nLkNyZWF0ZUVuY29kZWRHb29nbGVTdG9yYWdlS2V5UmVzcG9uc2UiAEI8CiJjb20uZ29vZ2xlLmFwcGVuZ2luZS5hcGkuYmxvYnN0b3JlEAIoAUISQmxvYnN0b3JlU2VydmljZVBi"))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())

class CreateUploadURLRequest(ProtocolBuffer.ProtocolMessage):
  has_success_path_ = 0
  success_path_ = ""
  has_max_upload_size_bytes_ = 0
  max_upload_size_bytes_ = 0
  has_max_upload_size_per_blob_bytes_ = 0
  max_upload_size_per_blob_bytes_ = 0
  has_gs_bucket_name_ = 0
  gs_bucket_name_ = ""
  has_url_expiry_time_seconds_ = 0
  url_expiry_time_seconds_ = 0

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def success_path(self): return self.success_path_

  def set_success_path(self, x):
    self.has_success_path_ = 1
    self.success_path_ = x

  def clear_success_path(self):
    if self.has_success_path_:
      self.has_success_path_ = 0
      self.success_path_ = ""

  def has_success_path(self): return self.has_success_path_

  def max_upload_size_bytes(self): return self.max_upload_size_bytes_

  def set_max_upload_size_bytes(self, x):
    self.has_max_upload_size_bytes_ = 1
    self.max_upload_size_bytes_ = x

  def clear_max_upload_size_bytes(self):
    if self.has_max_upload_size_bytes_:
      self.has_max_upload_size_bytes_ = 0
      self.max_upload_size_bytes_ = 0

  def has_max_upload_size_bytes(self): return self.has_max_upload_size_bytes_

  def max_upload_size_per_blob_bytes(self): return self.max_upload_size_per_blob_bytes_

  def set_max_upload_size_per_blob_bytes(self, x):
    self.has_max_upload_size_per_blob_bytes_ = 1
    self.max_upload_size_per_blob_bytes_ = x

  def clear_max_upload_size_per_blob_bytes(self):
    if self.has_max_upload_size_per_blob_bytes_:
      self.has_max_upload_size_per_blob_bytes_ = 0
      self.max_upload_size_per_blob_bytes_ = 0

  def has_max_upload_size_per_blob_bytes(self): return self.has_max_upload_size_per_blob_bytes_

  def gs_bucket_name(self): return self.gs_bucket_name_

  def set_gs_bucket_name(self, x):
    self.has_gs_bucket_name_ = 1
    self.gs_bucket_name_ = x

  def clear_gs_bucket_name(self):
    if self.has_gs_bucket_name_:
      self.has_gs_bucket_name_ = 0
      self.gs_bucket_name_ = ""

  def has_gs_bucket_name(self): return self.has_gs_bucket_name_

  def url_expiry_time_seconds(self): return self.url_expiry_time_seconds_

  def set_url_expiry_time_seconds(self, x):
    self.has_url_expiry_time_seconds_ = 1
    self.url_expiry_time_seconds_ = x

  def clear_url_expiry_time_seconds(self):
    if self.has_url_expiry_time_seconds_:
      self.has_url_expiry_time_seconds_ = 0
      self.url_expiry_time_seconds_ = 0

  def has_url_expiry_time_seconds(self): return self.has_url_expiry_time_seconds_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_success_path()): self.set_success_path(x.success_path())
    if (x.has_max_upload_size_bytes()): self.set_max_upload_size_bytes(x.max_upload_size_bytes())
    if (x.has_max_upload_size_per_blob_bytes()): self.set_max_upload_size_per_blob_bytes(x.max_upload_size_per_blob_bytes())
    if (x.has_gs_bucket_name()): self.set_gs_bucket_name(x.gs_bucket_name())
    if (x.has_url_expiry_time_seconds()): self.set_url_expiry_time_seconds(x.url_expiry_time_seconds())

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.CreateUploadURLRequest', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.CreateUploadURLRequest')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.CreateUploadURLRequest')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.CreateUploadURLRequest', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.CreateUploadURLRequest', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.CreateUploadURLRequest', s)


  def Equals(self, x):
    if x is self: return 1
    if self.has_success_path_ != x.has_success_path_: return 0
    if self.has_success_path_ and self.success_path_ != x.success_path_: return 0
    if self.has_max_upload_size_bytes_ != x.has_max_upload_size_bytes_: return 0
    if self.has_max_upload_size_bytes_ and self.max_upload_size_bytes_ != x.max_upload_size_bytes_: return 0
    if self.has_max_upload_size_per_blob_bytes_ != x.has_max_upload_size_per_blob_bytes_: return 0
    if self.has_max_upload_size_per_blob_bytes_ and self.max_upload_size_per_blob_bytes_ != x.max_upload_size_per_blob_bytes_: return 0
    if self.has_gs_bucket_name_ != x.has_gs_bucket_name_: return 0
    if self.has_gs_bucket_name_ and self.gs_bucket_name_ != x.gs_bucket_name_: return 0
    if self.has_url_expiry_time_seconds_ != x.has_url_expiry_time_seconds_: return 0
    if self.has_url_expiry_time_seconds_ and self.url_expiry_time_seconds_ != x.url_expiry_time_seconds_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_success_path_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: success_path not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(len(self.success_path_))
    if (self.has_max_upload_size_bytes_): n += 1 + self.lengthVarInt64(self.max_upload_size_bytes_)
    if (self.has_max_upload_size_per_blob_bytes_): n += 1 + self.lengthVarInt64(self.max_upload_size_per_blob_bytes_)
    if (self.has_gs_bucket_name_): n += 1 + self.lengthString(len(self.gs_bucket_name_))
    if (self.has_url_expiry_time_seconds_): n += 1 + self.lengthVarInt64(self.url_expiry_time_seconds_)
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_success_path_):
      n += 1
      n += self.lengthString(len(self.success_path_))
    if (self.has_max_upload_size_bytes_): n += 1 + self.lengthVarInt64(self.max_upload_size_bytes_)
    if (self.has_max_upload_size_per_blob_bytes_): n += 1 + self.lengthVarInt64(self.max_upload_size_per_blob_bytes_)
    if (self.has_gs_bucket_name_): n += 1 + self.lengthString(len(self.gs_bucket_name_))
    if (self.has_url_expiry_time_seconds_): n += 1 + self.lengthVarInt64(self.url_expiry_time_seconds_)
    return n

  def Clear(self):
    self.clear_success_path()
    self.clear_max_upload_size_bytes()
    self.clear_max_upload_size_per_blob_bytes()
    self.clear_gs_bucket_name()
    self.clear_url_expiry_time_seconds()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putPrefixedString(self.success_path_)
    if (self.has_max_upload_size_bytes_):
      out.putVarInt32(16)
      out.putVarInt64(self.max_upload_size_bytes_)
    if (self.has_max_upload_size_per_blob_bytes_):
      out.putVarInt32(24)
      out.putVarInt64(self.max_upload_size_per_blob_bytes_)
    if (self.has_gs_bucket_name_):
      out.putVarInt32(34)
      out.putPrefixedString(self.gs_bucket_name_)
    if (self.has_url_expiry_time_seconds_):
      out.putVarInt32(40)
      out.putVarInt32(self.url_expiry_time_seconds_)

  def OutputPartial(self, out):
    if (self.has_success_path_):
      out.putVarInt32(10)
      out.putPrefixedString(self.success_path_)
    if (self.has_max_upload_size_bytes_):
      out.putVarInt32(16)
      out.putVarInt64(self.max_upload_size_bytes_)
    if (self.has_max_upload_size_per_blob_bytes_):
      out.putVarInt32(24)
      out.putVarInt64(self.max_upload_size_per_blob_bytes_)
    if (self.has_gs_bucket_name_):
      out.putVarInt32(34)
      out.putPrefixedString(self.gs_bucket_name_)
    if (self.has_url_expiry_time_seconds_):
      out.putVarInt32(40)
      out.putVarInt32(self.url_expiry_time_seconds_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_success_path(d.getPrefixedString())
        continue
      if tt == 16:
        self.set_max_upload_size_bytes(d.getVarInt64())
        continue
      if tt == 24:
        self.set_max_upload_size_per_blob_bytes(d.getVarInt64())
        continue
      if tt == 34:
        self.set_gs_bucket_name(d.getPrefixedString())
        continue
      if tt == 40:
        self.set_url_expiry_time_seconds(d.getVarInt32())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_success_path_: res+=prefix+("success_path: %s\n" % self.DebugFormatString(self.success_path_))
    if self.has_max_upload_size_bytes_: res+=prefix+("max_upload_size_bytes: %s\n" % self.DebugFormatInt64(self.max_upload_size_bytes_))
    if self.has_max_upload_size_per_blob_bytes_: res+=prefix+("max_upload_size_per_blob_bytes: %s\n" % self.DebugFormatInt64(self.max_upload_size_per_blob_bytes_))
    if self.has_gs_bucket_name_: res+=prefix+("gs_bucket_name: %s\n" % self.DebugFormatString(self.gs_bucket_name_))
    if self.has_url_expiry_time_seconds_: res+=prefix+("url_expiry_time_seconds: %s\n" % self.DebugFormatInt32(self.url_expiry_time_seconds_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  ksuccess_path = 1
  kmax_upload_size_bytes = 2
  kmax_upload_size_per_blob_bytes = 3
  kgs_bucket_name = 4
  kurl_expiry_time_seconds = 5

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "success_path",
    2: "max_upload_size_bytes",
    3: "max_upload_size_per_blob_bytes",
    4: "gs_bucket_name",
    5: "url_expiry_time_seconds",
  }, 5)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
    2: ProtocolBuffer.Encoder.NUMERIC,
    3: ProtocolBuffer.Encoder.NUMERIC,
    4: ProtocolBuffer.Encoder.STRING,
    5: ProtocolBuffer.Encoder.NUMERIC,
  }, 5, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.CreateUploadURLRequest'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjBhcHBob3N0aW5nL2FwaS9ibG9ic3RvcmUvYmxvYnN0b3JlX3NlcnZpY2UucHJvdG8KIWFwcGhvc3RpbmcuQ3JlYXRlVXBsb2FkVVJMUmVxdWVzdBMaDHN1Y2Nlc3NfcGF0aCABKAIwCTgCFBMaFW1heF91cGxvYWRfc2l6ZV9ieXRlcyACKAAwAzgBFBMaHm1heF91cGxvYWRfc2l6ZV9wZXJfYmxvYl9ieXRlcyADKAAwAzgBFBMaDmdzX2J1Y2tldF9uYW1lIAQoAjAJOAEUExoXdXJsX2V4cGlyeV90aW1lX3NlY29uZHMgBSgAMAU4ARTCASBhcHBob3N0aW5nLkJsb2JzdG9yZVNlcnZpY2VFcnJvcg=="))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())

class CreateUploadURLResponse(ProtocolBuffer.ProtocolMessage):
  has_url_ = 0
  url_ = ""

  def __init__(self, contents=None):
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


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_url()): self.set_url(x.url())

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.CreateUploadURLResponse', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.CreateUploadURLResponse')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.CreateUploadURLResponse')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.CreateUploadURLResponse', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.CreateUploadURLResponse', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.CreateUploadURLResponse', s)


  def Equals(self, x):
    if x is self: return 1
    if self.has_url_ != x.has_url_: return 0
    if self.has_url_ and self.url_ != x.url_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_url_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: url not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(len(self.url_))
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_url_):
      n += 1
      n += self.lengthString(len(self.url_))
    return n

  def Clear(self):
    self.clear_url()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putPrefixedString(self.url_)

  def OutputPartial(self, out):
    if (self.has_url_):
      out.putVarInt32(10)
      out.putPrefixedString(self.url_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_url(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_url_: res+=prefix+("url: %s\n" % self.DebugFormatString(self.url_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kurl = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "url",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.CreateUploadURLResponse'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjBhcHBob3N0aW5nL2FwaS9ibG9ic3RvcmUvYmxvYnN0b3JlX3NlcnZpY2UucHJvdG8KImFwcGhvc3RpbmcuQ3JlYXRlVXBsb2FkVVJMUmVzcG9uc2UTGgN1cmwgASgCMAk4AhTCASBhcHBob3N0aW5nLkJsb2JzdG9yZVNlcnZpY2VFcnJvcg=="))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())

class DeleteBlobRequest(ProtocolBuffer.ProtocolMessage):
  has_token_ = 0
  token_ = ""

  def __init__(self, contents=None):
    self.blob_key_ = []
    if contents is not None: self.MergeFromString(contents)

  def blob_key_size(self): return len(self.blob_key_)
  def blob_key_list(self): return self.blob_key_

  def blob_key(self, i):
    return self.blob_key_[i]

  def set_blob_key(self, i, x):
    self.blob_key_[i] = x

  def add_blob_key(self, x):
    self.blob_key_.append(x)

  def clear_blob_key(self):
    self.blob_key_ = []

  def token(self): return self.token_

  def set_token(self, x):
    self.has_token_ = 1
    self.token_ = x

  def clear_token(self):
    if self.has_token_:
      self.has_token_ = 0
      self.token_ = ""

  def has_token(self): return self.has_token_


  def MergeFrom(self, x):
    assert x is not self
    for i in range(x.blob_key_size()): self.add_blob_key(x.blob_key(i))
    if (x.has_token()): self.set_token(x.token())

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.DeleteBlobRequest', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.DeleteBlobRequest')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.DeleteBlobRequest')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.DeleteBlobRequest', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.DeleteBlobRequest', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.DeleteBlobRequest', s)


  def Equals(self, x):
    if x is self: return 1
    if len(self.blob_key_) != len(x.blob_key_): return 0
    for e1, e2 in zip(self.blob_key_, x.blob_key_):
      if e1 != e2: return 0
    if self.has_token_ != x.has_token_: return 0
    if self.has_token_ and self.token_ != x.token_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    return initialized

  def ByteSize(self):
    n = 0
    n += 1 * len(self.blob_key_)
    for i in range(len(self.blob_key_)): n += self.lengthString(len(self.blob_key_[i]))
    if (self.has_token_): n += 1 + self.lengthString(len(self.token_))
    return n

  def ByteSizePartial(self):
    n = 0
    n += 1 * len(self.blob_key_)
    for i in range(len(self.blob_key_)): n += self.lengthString(len(self.blob_key_[i]))
    if (self.has_token_): n += 1 + self.lengthString(len(self.token_))
    return n

  def Clear(self):
    self.clear_blob_key()
    self.clear_token()

  def OutputUnchecked(self, out):
    for i in range(len(self.blob_key_)):
      out.putVarInt32(10)
      out.putPrefixedString(self.blob_key_[i])
    if (self.has_token_):
      out.putVarInt32(18)
      out.putPrefixedString(self.token_)

  def OutputPartial(self, out):
    for i in range(len(self.blob_key_)):
      out.putVarInt32(10)
      out.putPrefixedString(self.blob_key_[i])
    if (self.has_token_):
      out.putVarInt32(18)
      out.putPrefixedString(self.token_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.add_blob_key(d.getPrefixedString())
        continue
      if tt == 18:
        self.set_token(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    cnt=0
    for e in self.blob_key_:
      elm=""
      if printElemNumber: elm="(%d)" % cnt
      res+=prefix+("blob_key%s: %s\n" % (elm, self.DebugFormatString(e)))
      cnt+=1
    if self.has_token_: res+=prefix+("token: %s\n" % self.DebugFormatString(self.token_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kblob_key = 1
  ktoken = 2

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "blob_key",
    2: "token",
  }, 2)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
    2: ProtocolBuffer.Encoder.STRING,
  }, 2, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.DeleteBlobRequest'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjBhcHBob3N0aW5nL2FwaS9ibG9ic3RvcmUvYmxvYnN0b3JlX3NlcnZpY2UucHJvdG8KHGFwcGhvc3RpbmcuRGVsZXRlQmxvYlJlcXVlc3QTGghibG9iX2tleSABKAIwCTgDFBMaBXRva2VuIAIoAjAJOAEUwgEgYXBwaG9zdGluZy5CbG9ic3RvcmVTZXJ2aWNlRXJyb3I="))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())

class FetchDataRequest(ProtocolBuffer.ProtocolMessage):
  has_blob_key_ = 0
  blob_key_ = ""
  has_start_index_ = 0
  start_index_ = 0
  has_end_index_ = 0
  end_index_ = 0

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def blob_key(self): return self.blob_key_

  def set_blob_key(self, x):
    self.has_blob_key_ = 1
    self.blob_key_ = x

  def clear_blob_key(self):
    if self.has_blob_key_:
      self.has_blob_key_ = 0
      self.blob_key_ = ""

  def has_blob_key(self): return self.has_blob_key_

  def start_index(self): return self.start_index_

  def set_start_index(self, x):
    self.has_start_index_ = 1
    self.start_index_ = x

  def clear_start_index(self):
    if self.has_start_index_:
      self.has_start_index_ = 0
      self.start_index_ = 0

  def has_start_index(self): return self.has_start_index_

  def end_index(self): return self.end_index_

  def set_end_index(self, x):
    self.has_end_index_ = 1
    self.end_index_ = x

  def clear_end_index(self):
    if self.has_end_index_:
      self.has_end_index_ = 0
      self.end_index_ = 0

  def has_end_index(self): return self.has_end_index_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_blob_key()): self.set_blob_key(x.blob_key())
    if (x.has_start_index()): self.set_start_index(x.start_index())
    if (x.has_end_index()): self.set_end_index(x.end_index())

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.FetchDataRequest', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.FetchDataRequest')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.FetchDataRequest')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.FetchDataRequest', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.FetchDataRequest', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.FetchDataRequest', s)


  def Equals(self, x):
    if x is self: return 1
    if self.has_blob_key_ != x.has_blob_key_: return 0
    if self.has_blob_key_ and self.blob_key_ != x.blob_key_: return 0
    if self.has_start_index_ != x.has_start_index_: return 0
    if self.has_start_index_ and self.start_index_ != x.start_index_: return 0
    if self.has_end_index_ != x.has_end_index_: return 0
    if self.has_end_index_ and self.end_index_ != x.end_index_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_blob_key_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: blob_key not set.')
    if (not self.has_start_index_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: start_index not set.')
    if (not self.has_end_index_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: end_index not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(len(self.blob_key_))
    n += self.lengthVarInt64(self.start_index_)
    n += self.lengthVarInt64(self.end_index_)
    return n + 3

  def ByteSizePartial(self):
    n = 0
    if (self.has_blob_key_):
      n += 1
      n += self.lengthString(len(self.blob_key_))
    if (self.has_start_index_):
      n += 1
      n += self.lengthVarInt64(self.start_index_)
    if (self.has_end_index_):
      n += 1
      n += self.lengthVarInt64(self.end_index_)
    return n

  def Clear(self):
    self.clear_blob_key()
    self.clear_start_index()
    self.clear_end_index()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putPrefixedString(self.blob_key_)
    out.putVarInt32(16)
    out.putVarInt64(self.start_index_)
    out.putVarInt32(24)
    out.putVarInt64(self.end_index_)

  def OutputPartial(self, out):
    if (self.has_blob_key_):
      out.putVarInt32(10)
      out.putPrefixedString(self.blob_key_)
    if (self.has_start_index_):
      out.putVarInt32(16)
      out.putVarInt64(self.start_index_)
    if (self.has_end_index_):
      out.putVarInt32(24)
      out.putVarInt64(self.end_index_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_blob_key(d.getPrefixedString())
        continue
      if tt == 16:
        self.set_start_index(d.getVarInt64())
        continue
      if tt == 24:
        self.set_end_index(d.getVarInt64())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_blob_key_: res+=prefix+("blob_key: %s\n" % self.DebugFormatString(self.blob_key_))
    if self.has_start_index_: res+=prefix+("start_index: %s\n" % self.DebugFormatInt64(self.start_index_))
    if self.has_end_index_: res+=prefix+("end_index: %s\n" % self.DebugFormatInt64(self.end_index_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kblob_key = 1
  kstart_index = 2
  kend_index = 3

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "blob_key",
    2: "start_index",
    3: "end_index",
  }, 3)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
    2: ProtocolBuffer.Encoder.NUMERIC,
    3: ProtocolBuffer.Encoder.NUMERIC,
  }, 3, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.FetchDataRequest'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjBhcHBob3N0aW5nL2FwaS9ibG9ic3RvcmUvYmxvYnN0b3JlX3NlcnZpY2UucHJvdG8KG2FwcGhvc3RpbmcuRmV0Y2hEYXRhUmVxdWVzdBMaCGJsb2Jfa2V5IAEoAjAJOAIUExoLc3RhcnRfaW5kZXggAigAMAM4AhQTGgllbmRfaW5kZXggAygAMAM4AhTCASBhcHBob3N0aW5nLkJsb2JzdG9yZVNlcnZpY2VFcnJvcg=="))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())

class FetchDataResponse(ProtocolBuffer.ProtocolMessage):
  has_data_ = 0
  data_ = ""

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def data(self): return self.data_

  def set_data(self, x):
    self.has_data_ = 1
    self.data_ = x

  def clear_data(self):
    if self.has_data_:
      self.has_data_ = 0
      self.data_ = ""

  def has_data(self): return self.has_data_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_data()): self.set_data(x.data())

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.FetchDataResponse', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.FetchDataResponse')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.FetchDataResponse')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.FetchDataResponse', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.FetchDataResponse', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.FetchDataResponse', s)


  def Equals(self, x):
    if x is self: return 1
    if self.has_data_ != x.has_data_: return 0
    if self.has_data_ and self.data_ != x.data_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_data_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: data not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(len(self.data_))
    return n + 2

  def ByteSizePartial(self):
    n = 0
    if (self.has_data_):
      n += 2
      n += self.lengthString(len(self.data_))
    return n

  def Clear(self):
    self.clear_data()

  def OutputUnchecked(self, out):
    out.putVarInt32(8002)
    out.putPrefixedString(self.data_)

  def OutputPartial(self, out):
    if (self.has_data_):
      out.putVarInt32(8002)
      out.putPrefixedString(self.data_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 8002:
        self.set_data(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_data_: res+=prefix+("data: %s\n" % self.DebugFormatString(self.data_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kdata = 1000

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1000: "data",
  }, 1000)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1000: ProtocolBuffer.Encoder.STRING,
  }, 1000, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.FetchDataResponse'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjBhcHBob3N0aW5nL2FwaS9ibG9ic3RvcmUvYmxvYnN0b3JlX3NlcnZpY2UucHJvdG8KHGFwcGhvc3RpbmcuRmV0Y2hEYXRhUmVzcG9uc2UTGgRkYXRhIOgHKAIwCTgCowGqAQVjdHlwZbIBBENvcmSkARTCASBhcHBob3N0aW5nLkJsb2JzdG9yZVNlcnZpY2VFcnJvcg=="))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())

class CloneBlobRequest(ProtocolBuffer.ProtocolMessage):
  has_blob_key_ = 0
  blob_key_ = ""
  has_mime_type_ = 0
  mime_type_ = ""
  has_target_app_id_ = 0
  target_app_id_ = ""

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def blob_key(self): return self.blob_key_

  def set_blob_key(self, x):
    self.has_blob_key_ = 1
    self.blob_key_ = x

  def clear_blob_key(self):
    if self.has_blob_key_:
      self.has_blob_key_ = 0
      self.blob_key_ = ""

  def has_blob_key(self): return self.has_blob_key_

  def mime_type(self): return self.mime_type_

  def set_mime_type(self, x):
    self.has_mime_type_ = 1
    self.mime_type_ = x

  def clear_mime_type(self):
    if self.has_mime_type_:
      self.has_mime_type_ = 0
      self.mime_type_ = ""

  def has_mime_type(self): return self.has_mime_type_

  def target_app_id(self): return self.target_app_id_

  def set_target_app_id(self, x):
    self.has_target_app_id_ = 1
    self.target_app_id_ = x

  def clear_target_app_id(self):
    if self.has_target_app_id_:
      self.has_target_app_id_ = 0
      self.target_app_id_ = ""

  def has_target_app_id(self): return self.has_target_app_id_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_blob_key()): self.set_blob_key(x.blob_key())
    if (x.has_mime_type()): self.set_mime_type(x.mime_type())
    if (x.has_target_app_id()): self.set_target_app_id(x.target_app_id())

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.CloneBlobRequest', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.CloneBlobRequest')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.CloneBlobRequest')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.CloneBlobRequest', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.CloneBlobRequest', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.CloneBlobRequest', s)


  def Equals(self, x):
    if x is self: return 1
    if self.has_blob_key_ != x.has_blob_key_: return 0
    if self.has_blob_key_ and self.blob_key_ != x.blob_key_: return 0
    if self.has_mime_type_ != x.has_mime_type_: return 0
    if self.has_mime_type_ and self.mime_type_ != x.mime_type_: return 0
    if self.has_target_app_id_ != x.has_target_app_id_: return 0
    if self.has_target_app_id_ and self.target_app_id_ != x.target_app_id_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_blob_key_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: blob_key not set.')
    if (not self.has_mime_type_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: mime_type not set.')
    if (not self.has_target_app_id_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: target_app_id not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(len(self.blob_key_))
    n += self.lengthString(len(self.mime_type_))
    n += self.lengthString(len(self.target_app_id_))
    return n + 3

  def ByteSizePartial(self):
    n = 0
    if (self.has_blob_key_):
      n += 1
      n += self.lengthString(len(self.blob_key_))
    if (self.has_mime_type_):
      n += 1
      n += self.lengthString(len(self.mime_type_))
    if (self.has_target_app_id_):
      n += 1
      n += self.lengthString(len(self.target_app_id_))
    return n

  def Clear(self):
    self.clear_blob_key()
    self.clear_mime_type()
    self.clear_target_app_id()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putPrefixedString(self.blob_key_)
    out.putVarInt32(18)
    out.putPrefixedString(self.mime_type_)
    out.putVarInt32(26)
    out.putPrefixedString(self.target_app_id_)

  def OutputPartial(self, out):
    if (self.has_blob_key_):
      out.putVarInt32(10)
      out.putPrefixedString(self.blob_key_)
    if (self.has_mime_type_):
      out.putVarInt32(18)
      out.putPrefixedString(self.mime_type_)
    if (self.has_target_app_id_):
      out.putVarInt32(26)
      out.putPrefixedString(self.target_app_id_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_blob_key(d.getPrefixedString())
        continue
      if tt == 18:
        self.set_mime_type(d.getPrefixedString())
        continue
      if tt == 26:
        self.set_target_app_id(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_blob_key_: res+=prefix+("blob_key: %s\n" % self.DebugFormatString(self.blob_key_))
    if self.has_mime_type_: res+=prefix+("mime_type: %s\n" % self.DebugFormatString(self.mime_type_))
    if self.has_target_app_id_: res+=prefix+("target_app_id: %s\n" % self.DebugFormatString(self.target_app_id_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kblob_key = 1
  kmime_type = 2
  ktarget_app_id = 3

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "blob_key",
    2: "mime_type",
    3: "target_app_id",
  }, 3)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
    2: ProtocolBuffer.Encoder.STRING,
    3: ProtocolBuffer.Encoder.STRING,
  }, 3, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.CloneBlobRequest'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjBhcHBob3N0aW5nL2FwaS9ibG9ic3RvcmUvYmxvYnN0b3JlX3NlcnZpY2UucHJvdG8KG2FwcGhvc3RpbmcuQ2xvbmVCbG9iUmVxdWVzdBMaCGJsb2Jfa2V5IAEoAjAJOAIUExoJbWltZV90eXBlIAIoAjAJOAIUExoNdGFyZ2V0X2FwcF9pZCADKAIwCTgCFMIBIGFwcGhvc3RpbmcuQmxvYnN0b3JlU2VydmljZUVycm9y"))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())

class CloneBlobResponse(ProtocolBuffer.ProtocolMessage):
  has_blob_key_ = 0
  blob_key_ = ""

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def blob_key(self): return self.blob_key_

  def set_blob_key(self, x):
    self.has_blob_key_ = 1
    self.blob_key_ = x

  def clear_blob_key(self):
    if self.has_blob_key_:
      self.has_blob_key_ = 0
      self.blob_key_ = ""

  def has_blob_key(self): return self.has_blob_key_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_blob_key()): self.set_blob_key(x.blob_key())

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.CloneBlobResponse', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.CloneBlobResponse')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.CloneBlobResponse')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.CloneBlobResponse', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.CloneBlobResponse', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.CloneBlobResponse', s)


  def Equals(self, x):
    if x is self: return 1
    if self.has_blob_key_ != x.has_blob_key_: return 0
    if self.has_blob_key_ and self.blob_key_ != x.blob_key_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_blob_key_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: blob_key not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(len(self.blob_key_))
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_blob_key_):
      n += 1
      n += self.lengthString(len(self.blob_key_))
    return n

  def Clear(self):
    self.clear_blob_key()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putPrefixedString(self.blob_key_)

  def OutputPartial(self, out):
    if (self.has_blob_key_):
      out.putVarInt32(10)
      out.putPrefixedString(self.blob_key_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_blob_key(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_blob_key_: res+=prefix+("blob_key: %s\n" % self.DebugFormatString(self.blob_key_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kblob_key = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "blob_key",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.CloneBlobResponse'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjBhcHBob3N0aW5nL2FwaS9ibG9ic3RvcmUvYmxvYnN0b3JlX3NlcnZpY2UucHJvdG8KHGFwcGhvc3RpbmcuQ2xvbmVCbG9iUmVzcG9uc2UTGghibG9iX2tleSABKAIwCTgCFMIBIGFwcGhvc3RpbmcuQmxvYnN0b3JlU2VydmljZUVycm9y"))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())

class DecodeBlobKeyRequest(ProtocolBuffer.ProtocolMessage):

  def __init__(self, contents=None):
    self.blob_key_ = []
    if contents is not None: self.MergeFromString(contents)

  def blob_key_size(self): return len(self.blob_key_)
  def blob_key_list(self): return self.blob_key_

  def blob_key(self, i):
    return self.blob_key_[i]

  def set_blob_key(self, i, x):
    self.blob_key_[i] = x

  def add_blob_key(self, x):
    self.blob_key_.append(x)

  def clear_blob_key(self):
    self.blob_key_ = []


  def MergeFrom(self, x):
    assert x is not self
    for i in range(x.blob_key_size()): self.add_blob_key(x.blob_key(i))

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.DecodeBlobKeyRequest', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.DecodeBlobKeyRequest')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.DecodeBlobKeyRequest')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.DecodeBlobKeyRequest', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.DecodeBlobKeyRequest', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.DecodeBlobKeyRequest', s)


  def Equals(self, x):
    if x is self: return 1
    if len(self.blob_key_) != len(x.blob_key_): return 0
    for e1, e2 in zip(self.blob_key_, x.blob_key_):
      if e1 != e2: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    return initialized

  def ByteSize(self):
    n = 0
    n += 1 * len(self.blob_key_)
    for i in range(len(self.blob_key_)): n += self.lengthString(len(self.blob_key_[i]))
    return n

  def ByteSizePartial(self):
    n = 0
    n += 1 * len(self.blob_key_)
    for i in range(len(self.blob_key_)): n += self.lengthString(len(self.blob_key_[i]))
    return n

  def Clear(self):
    self.clear_blob_key()

  def OutputUnchecked(self, out):
    for i in range(len(self.blob_key_)):
      out.putVarInt32(10)
      out.putPrefixedString(self.blob_key_[i])

  def OutputPartial(self, out):
    for i in range(len(self.blob_key_)):
      out.putVarInt32(10)
      out.putPrefixedString(self.blob_key_[i])

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.add_blob_key(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    cnt=0
    for e in self.blob_key_:
      elm=""
      if printElemNumber: elm="(%d)" % cnt
      res+=prefix+("blob_key%s: %s\n" % (elm, self.DebugFormatString(e)))
      cnt+=1
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kblob_key = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "blob_key",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.DecodeBlobKeyRequest'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjBhcHBob3N0aW5nL2FwaS9ibG9ic3RvcmUvYmxvYnN0b3JlX3NlcnZpY2UucHJvdG8KH2FwcGhvc3RpbmcuRGVjb2RlQmxvYktleVJlcXVlc3QTGghibG9iX2tleSABKAIwCTgDFMIBIGFwcGhvc3RpbmcuQmxvYnN0b3JlU2VydmljZUVycm9y"))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())

class DecodeBlobKeyResponse(ProtocolBuffer.ProtocolMessage):

  def __init__(self, contents=None):
    self.decoded_ = []
    if contents is not None: self.MergeFromString(contents)

  def decoded_size(self): return len(self.decoded_)
  def decoded_list(self): return self.decoded_

  def decoded(self, i):
    return self.decoded_[i]

  def set_decoded(self, i, x):
    self.decoded_[i] = x

  def add_decoded(self, x):
    self.decoded_.append(x)

  def clear_decoded(self):
    self.decoded_ = []


  def MergeFrom(self, x):
    assert x is not self
    for i in range(x.decoded_size()): self.add_decoded(x.decoded(i))

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.DecodeBlobKeyResponse', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.DecodeBlobKeyResponse')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.DecodeBlobKeyResponse')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.DecodeBlobKeyResponse', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.DecodeBlobKeyResponse', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.DecodeBlobKeyResponse', s)


  def Equals(self, x):
    if x is self: return 1
    if len(self.decoded_) != len(x.decoded_): return 0
    for e1, e2 in zip(self.decoded_, x.decoded_):
      if e1 != e2: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    return initialized

  def ByteSize(self):
    n = 0
    n += 1 * len(self.decoded_)
    for i in range(len(self.decoded_)): n += self.lengthString(len(self.decoded_[i]))
    return n

  def ByteSizePartial(self):
    n = 0
    n += 1 * len(self.decoded_)
    for i in range(len(self.decoded_)): n += self.lengthString(len(self.decoded_[i]))
    return n

  def Clear(self):
    self.clear_decoded()

  def OutputUnchecked(self, out):
    for i in range(len(self.decoded_)):
      out.putVarInt32(10)
      out.putPrefixedString(self.decoded_[i])

  def OutputPartial(self, out):
    for i in range(len(self.decoded_)):
      out.putVarInt32(10)
      out.putPrefixedString(self.decoded_[i])

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.add_decoded(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    cnt=0
    for e in self.decoded_:
      elm=""
      if printElemNumber: elm="(%d)" % cnt
      res+=prefix+("decoded%s: %s\n" % (elm, self.DebugFormatString(e)))
      cnt+=1
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kdecoded = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "decoded",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.DecodeBlobKeyResponse'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjBhcHBob3N0aW5nL2FwaS9ibG9ic3RvcmUvYmxvYnN0b3JlX3NlcnZpY2UucHJvdG8KIGFwcGhvc3RpbmcuRGVjb2RlQmxvYktleVJlc3BvbnNlExoHZGVjb2RlZCABKAIwCTgDFMIBIGFwcGhvc3RpbmcuQmxvYnN0b3JlU2VydmljZUVycm9y"))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())

class CreateEncodedGoogleStorageKeyRequest(ProtocolBuffer.ProtocolMessage):
  has_filename_ = 0
  filename_ = ""

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def filename(self): return self.filename_

  def set_filename(self, x):
    self.has_filename_ = 1
    self.filename_ = x

  def clear_filename(self):
    if self.has_filename_:
      self.has_filename_ = 0
      self.filename_ = ""

  def has_filename(self): return self.has_filename_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_filename()): self.set_filename(x.filename())

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.CreateEncodedGoogleStorageKeyRequest', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.CreateEncodedGoogleStorageKeyRequest')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.CreateEncodedGoogleStorageKeyRequest')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.CreateEncodedGoogleStorageKeyRequest', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.CreateEncodedGoogleStorageKeyRequest', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.CreateEncodedGoogleStorageKeyRequest', s)


  def Equals(self, x):
    if x is self: return 1
    if self.has_filename_ != x.has_filename_: return 0
    if self.has_filename_ and self.filename_ != x.filename_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_filename_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: filename not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(len(self.filename_))
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_filename_):
      n += 1
      n += self.lengthString(len(self.filename_))
    return n

  def Clear(self):
    self.clear_filename()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putPrefixedString(self.filename_)

  def OutputPartial(self, out):
    if (self.has_filename_):
      out.putVarInt32(10)
      out.putPrefixedString(self.filename_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_filename(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_filename_: res+=prefix+("filename: %s\n" % self.DebugFormatString(self.filename_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kfilename = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "filename",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.CreateEncodedGoogleStorageKeyRequest'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjBhcHBob3N0aW5nL2FwaS9ibG9ic3RvcmUvYmxvYnN0b3JlX3NlcnZpY2UucHJvdG8KL2FwcGhvc3RpbmcuQ3JlYXRlRW5jb2RlZEdvb2dsZVN0b3JhZ2VLZXlSZXF1ZXN0ExoIZmlsZW5hbWUgASgCMAk4AhTCASBhcHBob3N0aW5nLkJsb2JzdG9yZVNlcnZpY2VFcnJvcg=="))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())

class CreateEncodedGoogleStorageKeyResponse(ProtocolBuffer.ProtocolMessage):
  has_blob_key_ = 0
  blob_key_ = ""

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def blob_key(self): return self.blob_key_

  def set_blob_key(self, x):
    self.has_blob_key_ = 1
    self.blob_key_ = x

  def clear_blob_key(self):
    if self.has_blob_key_:
      self.has_blob_key_ = 0
      self.blob_key_ = ""

  def has_blob_key(self): return self.has_blob_key_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_blob_key()): self.set_blob_key(x.blob_key())

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.CreateEncodedGoogleStorageKeyResponse', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.CreateEncodedGoogleStorageKeyResponse')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.CreateEncodedGoogleStorageKeyResponse')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.CreateEncodedGoogleStorageKeyResponse', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.CreateEncodedGoogleStorageKeyResponse', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.CreateEncodedGoogleStorageKeyResponse', s)


  def Equals(self, x):
    if x is self: return 1
    if self.has_blob_key_ != x.has_blob_key_: return 0
    if self.has_blob_key_ and self.blob_key_ != x.blob_key_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_blob_key_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: blob_key not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthString(len(self.blob_key_))
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_blob_key_):
      n += 1
      n += self.lengthString(len(self.blob_key_))
    return n

  def Clear(self):
    self.clear_blob_key()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putPrefixedString(self.blob_key_)

  def OutputPartial(self, out):
    if (self.has_blob_key_):
      out.putVarInt32(10)
      out.putPrefixedString(self.blob_key_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_blob_key(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_blob_key_: res+=prefix+("blob_key: %s\n" % self.DebugFormatString(self.blob_key_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kblob_key = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "blob_key",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.CreateEncodedGoogleStorageKeyResponse'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjBhcHBob3N0aW5nL2FwaS9ibG9ic3RvcmUvYmxvYnN0b3JlX3NlcnZpY2UucHJvdG8KMGFwcGhvc3RpbmcuQ3JlYXRlRW5jb2RlZEdvb2dsZVN0b3JhZ2VLZXlSZXNwb25zZRMaCGJsb2Jfa2V5IAEoAjAJOAIUwgEgYXBwaG9zdGluZy5CbG9ic3RvcmVTZXJ2aWNlRXJyb3I="))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())



class BlobstoreServiceStub(object):
  """Makes Stubby RPC calls to a BlobstoreService server."""

  __metaclass__ = abc.ABCMeta

  __slots__ = ()

  @abc.abstractmethod
  def CreateUploadURL(self, request, rpc=None, callback=None, response=None):
    """Make a CreateUploadURL RPC call.

    Args:
      request: a CreateUploadURLRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The CreateUploadURLResponse if callback is None. Otherwise, returns None.
    """
    raise NotImplementedError()

  @abc.abstractmethod
  def DeleteBlob(self, request, rpc=None, callback=None, response=None):
    """Make a DeleteBlob RPC call.

    Args:
      request: a DeleteBlobRequest instance.
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
  def FetchData(self, request, rpc=None, callback=None, response=None):
    """Make a FetchData RPC call.

    Args:
      request: a FetchDataRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The FetchDataResponse if callback is None. Otherwise, returns None.
    """
    raise NotImplementedError()

  @abc.abstractmethod
  def CloneBlob(self, request, rpc=None, callback=None, response=None):
    """Make a CloneBlob RPC call.

    Args:
      request: a CloneBlobRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The CloneBlobResponse if callback is None. Otherwise, returns None.
    """
    raise NotImplementedError()

  @abc.abstractmethod
  def DecodeBlobKey(self, request, rpc=None, callback=None, response=None):
    """Make a DecodeBlobKey RPC call.

    Args:
      request: a DecodeBlobKeyRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The DecodeBlobKeyResponse if callback is None. Otherwise, returns None.
    """
    raise NotImplementedError()

  @abc.abstractmethod
  def CreateEncodedGoogleStorageKey(self, request, rpc=None, callback=None, response=None):
    """Make a CreateEncodedGoogleStorageKey RPC call.

    Args:
      request: a CreateEncodedGoogleStorageKeyRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The CreateEncodedGoogleStorageKeyResponse if callback is None. Otherwise, returns None.
    """
    raise NotImplementedError()


class _BlobstoreService_ClientBaseStub(
    BlobstoreServiceStub, _client_stub_base_class):
  """Makes Stubby RPC calls to a BlobstoreService server."""

  __slots__ = (
      '_protorpc_CreateUploadURL', '_full_name_CreateUploadURL',
      '_protorpc_DeleteBlob', '_full_name_DeleteBlob',
      '_protorpc_FetchData', '_full_name_FetchData',
      '_protorpc_CloneBlob', '_full_name_CloneBlob',
      '_protorpc_DecodeBlobKey', '_full_name_DecodeBlobKey',
      '_protorpc_CreateEncodedGoogleStorageKey', '_full_name_CreateEncodedGoogleStorageKey',
  )

  def __init__(self, rpc_stub, rpc_factory=None):
    super(_BlobstoreService_ClientBaseStub, self).__init__(
        None, inject_stub=rpc_stub, rpc_factory=rpc_factory)

    self._protorpc_CreateUploadURL = pywraprpc.RPC()
    self._full_name_CreateUploadURL = self._stub.GetFullMethodName(
        'CreateUploadURL')

    self._protorpc_DeleteBlob = pywraprpc.RPC()
    self._full_name_DeleteBlob = self._stub.GetFullMethodName(
        'DeleteBlob')

    self._protorpc_FetchData = pywraprpc.RPC()
    self._full_name_FetchData = self._stub.GetFullMethodName(
        'FetchData')

    self._protorpc_CloneBlob = pywraprpc.RPC()
    self._full_name_CloneBlob = self._stub.GetFullMethodName(
        'CloneBlob')

    self._protorpc_DecodeBlobKey = pywraprpc.RPC()
    self._full_name_DecodeBlobKey = self._stub.GetFullMethodName(
        'DecodeBlobKey')

    self._protorpc_CreateEncodedGoogleStorageKey = pywraprpc.RPC()
    self._full_name_CreateEncodedGoogleStorageKey = self._stub.GetFullMethodName(
        'CreateEncodedGoogleStorageKey')

  def CreateUploadURL(self, request, rpc=None, callback=None, response=None):
    """Make a CreateUploadURL RPC call.

    Args:
      request: a CreateUploadURLRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The CreateUploadURLResponse if callback is None. Otherwise, returns None.
    """

    if response is None:
      response = CreateUploadURLResponse
    return self._MakeCall(rpc,
                          self._full_name_CreateUploadURL,
                          'CreateUploadURL',
                          request,
                          response,
                          callback,
                          self._protorpc_CreateUploadURL,
                          package_name='apphosting')

  def DeleteBlob(self, request, rpc=None, callback=None, response=None):
    """Make a DeleteBlob RPC call.

    Args:
      request: a DeleteBlobRequest instance.
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
                          self._full_name_DeleteBlob,
                          'DeleteBlob',
                          request,
                          response,
                          callback,
                          self._protorpc_DeleteBlob,
                          package_name='apphosting')

  def FetchData(self, request, rpc=None, callback=None, response=None):
    """Make a FetchData RPC call.

    Args:
      request: a FetchDataRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The FetchDataResponse if callback is None. Otherwise, returns None.
    """

    if response is None:
      response = FetchDataResponse
    return self._MakeCall(rpc,
                          self._full_name_FetchData,
                          'FetchData',
                          request,
                          response,
                          callback,
                          self._protorpc_FetchData,
                          package_name='apphosting')

  def CloneBlob(self, request, rpc=None, callback=None, response=None):
    """Make a CloneBlob RPC call.

    Args:
      request: a CloneBlobRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The CloneBlobResponse if callback is None. Otherwise, returns None.
    """

    if response is None:
      response = CloneBlobResponse
    return self._MakeCall(rpc,
                          self._full_name_CloneBlob,
                          'CloneBlob',
                          request,
                          response,
                          callback,
                          self._protorpc_CloneBlob,
                          package_name='apphosting')

  def DecodeBlobKey(self, request, rpc=None, callback=None, response=None):
    """Make a DecodeBlobKey RPC call.

    Args:
      request: a DecodeBlobKeyRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The DecodeBlobKeyResponse if callback is None. Otherwise, returns None.
    """

    if response is None:
      response = DecodeBlobKeyResponse
    return self._MakeCall(rpc,
                          self._full_name_DecodeBlobKey,
                          'DecodeBlobKey',
                          request,
                          response,
                          callback,
                          self._protorpc_DecodeBlobKey,
                          package_name='apphosting')

  def CreateEncodedGoogleStorageKey(self, request, rpc=None, callback=None, response=None):
    """Make a CreateEncodedGoogleStorageKey RPC call.

    Args:
      request: a CreateEncodedGoogleStorageKeyRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The CreateEncodedGoogleStorageKeyResponse if callback is None. Otherwise, returns None.
    """

    if response is None:
      response = CreateEncodedGoogleStorageKeyResponse
    return self._MakeCall(rpc,
                          self._full_name_CreateEncodedGoogleStorageKey,
                          'CreateEncodedGoogleStorageKey',
                          request,
                          response,
                          callback,
                          self._protorpc_CreateEncodedGoogleStorageKey,
                          package_name='apphosting')


class _BlobstoreService_ClientStub(_BlobstoreService_ClientBaseStub):
  __slots__ = ('_params',)
  def __init__(self, rpc_stub_parameters, service_name, rpc_factory=None):
    if service_name is None:
      service_name = 'BlobstoreService'
    stub = pywraprpc.RPC_GenericStub(service_name, rpc_stub_parameters)
    super(_BlobstoreService_ClientStub, self).__init__(stub, rpc_factory=rpc_factory)
    self._params = rpc_stub_parameters


class _BlobstoreService_RPC2ClientStub(_BlobstoreService_ClientBaseStub):
  __slots__ = ()
  def __init__(self, server, channel, service_name, rpc_factory=None):
    if service_name is None:
      service_name = 'BlobstoreService'
    if channel is None:
      if server is None:
        raise RuntimeError('Invalid argument combination to create a stub')
      channel = pywraprpc.NewClientChannel(server)
    elif channel.version() == 1:
      raise RuntimeError('Expecting an RPC2 channel to create the stub')
    stub = pywraprpc.RPC_GenericStub(service_name, channel)
    super(_BlobstoreService_RPC2ClientStub, self).__init__(stub, rpc_factory=rpc_factory)


class BlobstoreService(_server_stub_base_class):
  """Base class for BlobstoreService Stubby servers."""

  @classmethod
  def _MethodSignatures(cls):
    """Returns a dict of {<method-name>: (<request-type>, <response-type>)}."""
    return {
      'CreateUploadURL': (CreateUploadURLRequest, CreateUploadURLResponse),
      'DeleteBlob': (DeleteBlobRequest, google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto),
      'FetchData': (FetchDataRequest, FetchDataResponse),
      'CloneBlob': (CloneBlobRequest, CloneBlobResponse),
      'DecodeBlobKey': (DecodeBlobKeyRequest, DecodeBlobKeyResponse),
      'CreateEncodedGoogleStorageKey': (CreateEncodedGoogleStorageKeyRequest, CreateEncodedGoogleStorageKeyResponse),
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
    _server_stub_base_class.__init__(self, 'apphosting.BlobstoreService', *args, **kwargs)

  @staticmethod
  def NewStub(rpc_stub_parameters, service_name=None, rpc_factory=None):
    """USE NewRPC2Stub INSTEAD."""
    if _client_stub_base_class is object:
      raise RuntimeError('Add //net/rpc/python as a dependency to use Stubby')
    return _BlobstoreService_ClientStub(
        rpc_stub_parameters, service_name, rpc_factory=rpc_factory)

  @staticmethod
  def NewRPC2Stub(
      server=None, channel=None, service_name=None, rpc_factory=None):
    """Creates a new BlobstoreService Stubby2 client stub.

    Args:
      server: host:port or bns address (favor passing a channel instead).
      channel: directly use a channel to create a stub. Will ignore server
          argument if this is specified.
      service_name: the service name used by the Stubby server.
      rpc_factory: the rpc factory to use if no rpc argument is specified.

    Returns:
     A BlobstoreServiceStub to be used to invoke RPCs.
    """

    if _client_stub_base_class is object:
      raise RuntimeError('Add //net/rpc/python:proto_python_api_2_stub (or maybe //net/rpc/python:proto_python_api_1_stub, but eww and b/67959631) as a dependency to create Stubby stubs')
    return _BlobstoreService_RPC2ClientStub(
        server, channel, service_name, rpc_factory=rpc_factory)

  def CreateUploadURL(self, rpc, request, response):
    """Handles a CreateUploadURL RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a CreateUploadURLRequest that contains the client request
      response: a CreateUploadURLResponse that should be modified to send the response
    """
    raise NotImplementedError()


  def DeleteBlob(self, rpc, request, response):
    """Handles a DeleteBlob RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a DeleteBlobRequest that contains the client request
      response: a google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto that should be modified to send the response
    """
    raise NotImplementedError()


  def FetchData(self, rpc, request, response):
    """Handles a FetchData RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a FetchDataRequest that contains the client request
      response: a FetchDataResponse that should be modified to send the response
    """
    raise NotImplementedError()


  def CloneBlob(self, rpc, request, response):
    """Handles a CloneBlob RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a CloneBlobRequest that contains the client request
      response: a CloneBlobResponse that should be modified to send the response
    """
    raise NotImplementedError()


  def DecodeBlobKey(self, rpc, request, response):
    """Handles a DecodeBlobKey RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a DecodeBlobKeyRequest that contains the client request
      response: a DecodeBlobKeyResponse that should be modified to send the response
    """
    raise NotImplementedError()


  def CreateEncodedGoogleStorageKey(self, rpc, request, response):
    """Handles a CreateEncodedGoogleStorageKey RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a CreateEncodedGoogleStorageKeyRequest that contains the client request
      response: a CreateEncodedGoogleStorageKeyResponse that should be modified to send the response
    """
    raise NotImplementedError()

  def _AddMethodAttributes(self):
    """Sets attributes on Python RPC handlers.

    See BaseRpcServer in rpcserver.py for details.
    """
    rpcserver._GetHandlerDecorator(
        getattr(self.CreateUploadURL, '__func__'),
        CreateUploadURLRequest,
        CreateUploadURLResponse,
        None,
        'INTEGRITY')
    rpcserver._GetHandlerDecorator(
        getattr(self.DeleteBlob, '__func__'),
        DeleteBlobRequest,
        google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto,
        None,
        'INTEGRITY')
    rpcserver._GetHandlerDecorator(
        getattr(self.FetchData, '__func__'),
        FetchDataRequest,
        FetchDataResponse,
        None,
        'INTEGRITY')
    rpcserver._GetHandlerDecorator(
        getattr(self.CloneBlob, '__func__'),
        CloneBlobRequest,
        CloneBlobResponse,
        None,
        'INTEGRITY')
    rpcserver._GetHandlerDecorator(
        getattr(self.DecodeBlobKey, '__func__'),
        DecodeBlobKeyRequest,
        DecodeBlobKeyResponse,
        None,
        'INTEGRITY')
    rpcserver._GetHandlerDecorator(
        getattr(self.CreateEncodedGoogleStorageKey, '__func__'),
        CreateEncodedGoogleStorageKeyRequest,
        CreateEncodedGoogleStorageKeyResponse,
        None,
        'INTEGRITY')

if _extension_runtime:
  pass

__all__ = ['BlobstoreServiceError','CreateUploadURLRequest','CreateUploadURLResponse','DeleteBlobRequest','FetchDataRequest','FetchDataResponse','CloneBlobRequest','CloneBlobResponse','DecodeBlobKeyRequest','DecodeBlobKeyResponse','CreateEncodedGoogleStorageKeyRequest','CreateEncodedGoogleStorageKeyResponse','BlobstoreService']
