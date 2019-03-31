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
class StoreBlobRequest(ProtocolBuffer.ProtocolMessage):
  has_blob_key_ = 0
  blob_key_ = ""
  has_content_ = 0
  content_ = ""

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

  def content(self): return self.content_

  def set_content(self, x):
    self.has_content_ = 1
    self.content_ = x

  def clear_content(self):
    if self.has_content_:
      self.has_content_ = 0
      self.content_ = ""

  def has_content(self): return self.has_content_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_blob_key()): self.set_blob_key(x.blob_key())
    if (x.has_content()): self.set_content(x.content())

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.StoreBlobRequest', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.StoreBlobRequest')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.StoreBlobRequest')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.StoreBlobRequest', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.StoreBlobRequest', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.StoreBlobRequest', s)


  def Equals(self, x):
    if x is self: return 1
    if self.has_blob_key_ != x.has_blob_key_: return 0
    if self.has_blob_key_ and self.blob_key_ != x.blob_key_: return 0
    if self.has_content_ != x.has_content_: return 0
    if self.has_content_ and self.content_ != x.content_: return 0
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
    if (self.has_content_): n += 1 + self.lengthString(len(self.content_))
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_blob_key_):
      n += 1
      n += self.lengthString(len(self.blob_key_))
    if (self.has_content_): n += 1 + self.lengthString(len(self.content_))
    return n

  def Clear(self):
    self.clear_blob_key()
    self.clear_content()

  def OutputUnchecked(self, out):
    out.putVarInt32(10)
    out.putPrefixedString(self.blob_key_)
    if (self.has_content_):
      out.putVarInt32(18)
      out.putPrefixedString(self.content_)

  def OutputPartial(self, out):
    if (self.has_blob_key_):
      out.putVarInt32(10)
      out.putPrefixedString(self.blob_key_)
    if (self.has_content_):
      out.putVarInt32(18)
      out.putPrefixedString(self.content_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_blob_key(d.getPrefixedString())
        continue
      if tt == 18:
        self.set_content(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_blob_key_: res+=prefix+("blob_key: %s\n" % self.DebugFormatString(self.blob_key_))
    if self.has_content_: res+=prefix+("content: %s\n" % self.DebugFormatString(self.content_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kblob_key = 1
  kcontent = 2

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "blob_key",
    2: "content",
  }, 2)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
    2: ProtocolBuffer.Encoder.STRING,
  }, 2, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.StoreBlobRequest'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjVhcHBob3N0aW5nL2FwaS9ibG9ic3RvcmUvYmxvYnN0b3JlX3N0dWJfc2VydmljZS5wcm90bwobYXBwaG9zdGluZy5TdG9yZUJsb2JSZXF1ZXN0ExoIYmxvYl9rZXkgASgCMAk4AhQTGgdjb250ZW50IAIoAjAJOAEUugGgBAo1YXBwaG9zdGluZy9hcGkvYmxvYnN0b3JlL2Jsb2JzdG9yZV9zdHViX3NlcnZpY2UucHJvdG8SCmFwcGhvc3RpbmcaHWFwcGhvc3RpbmcvYXBpL2FwaV9iYXNlLnByb3RvIjUKEFN0b3JlQmxvYlJlcXVlc3QSEAoIYmxvYl9rZXkYASACKAkSDwoHY29udGVudBgCIAEoDCKJAQoZU2V0QmxvYlN0b3JhZ2VUeXBlUmVxdWVzdBJHCgxzdG9yYWdlX3R5cGUYASACKA4yMS5hcHBob3N0aW5nLlNldEJsb2JTdG9yYWdlVHlwZVJlcXVlc3QuU3RvcmFnZVR5cGUiIwoLU3RvcmFnZVR5cGUSCgoGTUVNT1JZEAASCAoERklMRRABMroBChRCbG9ic3RvcmVTdHViU2VydmljZRJHCglTdG9yZUJsb2ISHC5hcHBob3N0aW5nLlN0b3JlQmxvYlJlcXVlc3QaGi5hcHBob3N0aW5nLmJhc2UuVm9pZFByb3RvIgASWQoSU2V0QmxvYlN0b3JhZ2VUeXBlEiUuYXBwaG9zdGluZy5TZXRCbG9iU3RvcmFnZVR5cGVSZXF1ZXN0GhouYXBwaG9zdGluZy5iYXNlLlZvaWRQcm90byIAQjwKImNvbS5nb29nbGUuYXBwZW5naW5lLmFwaS5ibG9ic3RvcmVCFkJsb2JzdG9yZVN0dWJTZXJ2aWNlUGI="))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())

class SetBlobStorageTypeRequest(ProtocolBuffer.ProtocolMessage):


  MEMORY       =    0
  FILE         =    1

  _StorageType_NAMES = {
    0: "MEMORY",
    1: "FILE",
  }

  def StorageType_Name(cls, x): return cls._StorageType_NAMES.get(x, "")
  StorageType_Name = classmethod(StorageType_Name)

  has_storage_type_ = 0
  storage_type_ = 0

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def storage_type(self): return self.storage_type_

  def set_storage_type(self, x):
    self.has_storage_type_ = 1
    self.storage_type_ = x

  def clear_storage_type(self):
    if self.has_storage_type_:
      self.has_storage_type_ = 0
      self.storage_type_ = 0

  def has_storage_type(self): return self.has_storage_type_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_storage_type()): self.set_storage_type(x.storage_type())

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.SetBlobStorageTypeRequest', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.SetBlobStorageTypeRequest')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.SetBlobStorageTypeRequest')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.SetBlobStorageTypeRequest', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.SetBlobStorageTypeRequest', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.SetBlobStorageTypeRequest', s)


  def Equals(self, x):
    if x is self: return 1
    if self.has_storage_type_ != x.has_storage_type_: return 0
    if self.has_storage_type_ and self.storage_type_ != x.storage_type_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    if (not self.has_storage_type_):
      initialized = 0
      if debug_strs is not None:
        debug_strs.append('Required field: storage_type not set.')
    return initialized

  def ByteSize(self):
    n = 0
    n += self.lengthVarInt64(self.storage_type_)
    return n + 1

  def ByteSizePartial(self):
    n = 0
    if (self.has_storage_type_):
      n += 1
      n += self.lengthVarInt64(self.storage_type_)
    return n

  def Clear(self):
    self.clear_storage_type()

  def OutputUnchecked(self, out):
    out.putVarInt32(8)
    out.putVarInt32(self.storage_type_)

  def OutputPartial(self, out):
    if (self.has_storage_type_):
      out.putVarInt32(8)
      out.putVarInt32(self.storage_type_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 8:
        self.set_storage_type(d.getVarInt32())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_storage_type_: res+=prefix+("storage_type: %s\n" % self.DebugFormatInt32(self.storage_type_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kstorage_type = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "storage_type",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.NUMERIC,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.SetBlobStorageTypeRequest'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjVhcHBob3N0aW5nL2FwaS9ibG9ic3RvcmUvYmxvYnN0b3JlX3N0dWJfc2VydmljZS5wcm90bwokYXBwaG9zdGluZy5TZXRCbG9iU3RvcmFnZVR5cGVSZXF1ZXN0ExoMc3RvcmFnZV90eXBlIAEoADAFOAJoABRzegtTdG9yYWdlVHlwZYsBkgEGTUVNT1JZmAEAjAGLAZIBBEZJTEWYAQGMAXTCARthcHBob3N0aW5nLlN0b3JlQmxvYlJlcXVlc3Q="))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())



class BlobstoreStubServiceStub(object):
  """Makes Stubby RPC calls to a BlobstoreStubService server."""

  __metaclass__ = abc.ABCMeta

  __slots__ = ()

  @abc.abstractmethod
  def StoreBlob(self, request, rpc=None, callback=None, response=None):
    """Make a StoreBlob RPC call.

    Args:
      request: a StoreBlobRequest instance.
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
  def SetBlobStorageType(self, request, rpc=None, callback=None, response=None):
    """Make a SetBlobStorageType RPC call.

    Args:
      request: a SetBlobStorageTypeRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto if callback is None. Otherwise, returns None.
    """
    raise NotImplementedError()


class _BlobstoreStubService_ClientBaseStub(
    BlobstoreStubServiceStub, _client_stub_base_class):
  """Makes Stubby RPC calls to a BlobstoreStubService server."""

  __slots__ = (
      '_protorpc_StoreBlob', '_full_name_StoreBlob',
      '_protorpc_SetBlobStorageType', '_full_name_SetBlobStorageType',
  )

  def __init__(self, rpc_stub, rpc_factory=None):
    super(_BlobstoreStubService_ClientBaseStub, self).__init__(
        None, inject_stub=rpc_stub, rpc_factory=rpc_factory)

    self._protorpc_StoreBlob = pywraprpc.RPC()
    self._full_name_StoreBlob = self._stub.GetFullMethodName(
        'StoreBlob')

    self._protorpc_SetBlobStorageType = pywraprpc.RPC()
    self._full_name_SetBlobStorageType = self._stub.GetFullMethodName(
        'SetBlobStorageType')

  def StoreBlob(self, request, rpc=None, callback=None, response=None):
    """Make a StoreBlob RPC call.

    Args:
      request: a StoreBlobRequest instance.
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
                          self._full_name_StoreBlob,
                          'StoreBlob',
                          request,
                          response,
                          callback,
                          self._protorpc_StoreBlob,
                          package_name='apphosting')

  def SetBlobStorageType(self, request, rpc=None, callback=None, response=None):
    """Make a SetBlobStorageType RPC call.

    Args:
      request: a SetBlobStorageTypeRequest instance.
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
                          self._full_name_SetBlobStorageType,
                          'SetBlobStorageType',
                          request,
                          response,
                          callback,
                          self._protorpc_SetBlobStorageType,
                          package_name='apphosting')


class _BlobstoreStubService_ClientStub(_BlobstoreStubService_ClientBaseStub):
  __slots__ = ('_params',)
  def __init__(self, rpc_stub_parameters, service_name, rpc_factory=None):
    if service_name is None:
      service_name = 'BlobstoreStubService'
    stub = pywraprpc.RPC_GenericStub(service_name, rpc_stub_parameters)
    super(_BlobstoreStubService_ClientStub, self).__init__(stub, rpc_factory=rpc_factory)
    self._params = rpc_stub_parameters


class _BlobstoreStubService_RPC2ClientStub(_BlobstoreStubService_ClientBaseStub):
  __slots__ = ()
  def __init__(self, server, channel, service_name, rpc_factory=None):
    if service_name is None:
      service_name = 'BlobstoreStubService'
    if channel is None:
      if server is None:
        raise RuntimeError('Invalid argument combination to create a stub')
      channel = pywraprpc.NewClientChannel(server)
    elif channel.version() == 1:
      raise RuntimeError('Expecting an RPC2 channel to create the stub')
    stub = pywraprpc.RPC_GenericStub(service_name, channel)
    super(_BlobstoreStubService_RPC2ClientStub, self).__init__(stub, rpc_factory=rpc_factory)


class BlobstoreStubService(_server_stub_base_class):
  """Base class for BlobstoreStubService Stubby servers."""

  @classmethod
  def _MethodSignatures(cls):
    """Returns a dict of {<method-name>: (<request-type>, <response-type>)}."""
    return {
      'StoreBlob': (StoreBlobRequest, google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto),
      'SetBlobStorageType': (SetBlobStorageTypeRequest, google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto),
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
    _server_stub_base_class.__init__(self, 'apphosting.BlobstoreStubService', *args, **kwargs)

  @staticmethod
  def NewStub(rpc_stub_parameters, service_name=None, rpc_factory=None):
    """USE NewRPC2Stub INSTEAD."""
    if _client_stub_base_class is object:
      raise RuntimeError('Add //net/rpc/python as a dependency to use Stubby')
    return _BlobstoreStubService_ClientStub(
        rpc_stub_parameters, service_name, rpc_factory=rpc_factory)

  @staticmethod
  def NewRPC2Stub(
      server=None, channel=None, service_name=None, rpc_factory=None):
    """Creates a new BlobstoreStubService Stubby2 client stub.

    Args:
      server: host:port or bns address (favor passing a channel instead).
      channel: directly use a channel to create a stub. Will ignore server
          argument if this is specified.
      service_name: the service name used by the Stubby server.
      rpc_factory: the rpc factory to use if no rpc argument is specified.

    Returns:
     A BlobstoreStubServiceStub to be used to invoke RPCs.
    """

    if _client_stub_base_class is object:
      raise RuntimeError('Add //net/rpc/python:proto_python_api_2_stub (or maybe //net/rpc/python:proto_python_api_1_stub, but eww and b/67959631) as a dependency to create Stubby stubs')
    return _BlobstoreStubService_RPC2ClientStub(
        server, channel, service_name, rpc_factory=rpc_factory)

  def StoreBlob(self, rpc, request, response):
    """Handles a StoreBlob RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a StoreBlobRequest that contains the client request
      response: a google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto that should be modified to send the response
    """
    raise NotImplementedError()


  def SetBlobStorageType(self, rpc, request, response):
    """Handles a SetBlobStorageType RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a SetBlobStorageTypeRequest that contains the client request
      response: a google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto that should be modified to send the response
    """
    raise NotImplementedError()

  def _AddMethodAttributes(self):
    """Sets attributes on Python RPC handlers.

    See BaseRpcServer in rpcserver.py for details.
    """
    rpcserver._GetHandlerDecorator(
        getattr(self.StoreBlob, '__func__'),
        StoreBlobRequest,
        google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto,
        None,
        'INTEGRITY')
    rpcserver._GetHandlerDecorator(
        getattr(self.SetBlobStorageType, '__func__'),
        SetBlobStorageTypeRequest,
        google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto,
        None,
        'INTEGRITY')

if _extension_runtime:
  pass

__all__ = ['StoreBlobRequest','SetBlobStorageTypeRequest','BlobstoreStubService']
