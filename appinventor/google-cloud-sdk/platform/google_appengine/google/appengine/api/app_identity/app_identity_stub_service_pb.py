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
class SetDefaultGcsBucketNameRequest(ProtocolBuffer.ProtocolMessage):
  has_default_gcs_bucket_name_ = 0
  default_gcs_bucket_name_ = ""

  def __init__(self, contents=None):
    if contents is not None: self.MergeFromString(contents)

  def default_gcs_bucket_name(self): return self.default_gcs_bucket_name_

  def set_default_gcs_bucket_name(self, x):
    self.has_default_gcs_bucket_name_ = 1
    self.default_gcs_bucket_name_ = x

  def clear_default_gcs_bucket_name(self):
    if self.has_default_gcs_bucket_name_:
      self.has_default_gcs_bucket_name_ = 0
      self.default_gcs_bucket_name_ = ""

  def has_default_gcs_bucket_name(self): return self.has_default_gcs_bucket_name_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_default_gcs_bucket_name()): self.set_default_gcs_bucket_name(x.default_gcs_bucket_name())

  if _net_proto___parse__python is not None:
    def _CMergeFromString(self, s):
      _net_proto___parse__python.MergeFromString(self, 'apphosting.SetDefaultGcsBucketNameRequest', s)

  if _net_proto___parse__python is not None:
    def _CEncode(self):
      return _net_proto___parse__python.Encode(self, 'apphosting.SetDefaultGcsBucketNameRequest')

  if _net_proto___parse__python is not None:
    def _CEncodePartial(self):
      return _net_proto___parse__python.EncodePartial(self, 'apphosting.SetDefaultGcsBucketNameRequest')

  if _net_proto___parse__python is not None:
    def _CToASCII(self, output_format):
      return _net_proto___parse__python.ToASCII(self, 'apphosting.SetDefaultGcsBucketNameRequest', output_format)


  if _net_proto___parse__python is not None:
    def ParseASCII(self, s):
      _net_proto___parse__python.ParseASCII(self, 'apphosting.SetDefaultGcsBucketNameRequest', s)


  if _net_proto___parse__python is not None:
    def ParseASCIIIgnoreUnknown(self, s):
      _net_proto___parse__python.ParseASCIIIgnoreUnknown(self, 'apphosting.SetDefaultGcsBucketNameRequest', s)


  def Equals(self, x):
    if x is self: return 1
    if self.has_default_gcs_bucket_name_ != x.has_default_gcs_bucket_name_: return 0
    if self.has_default_gcs_bucket_name_ and self.default_gcs_bucket_name_ != x.default_gcs_bucket_name_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    return initialized

  def ByteSize(self):
    n = 0
    if (self.has_default_gcs_bucket_name_): n += 1 + self.lengthString(len(self.default_gcs_bucket_name_))
    return n

  def ByteSizePartial(self):
    n = 0
    if (self.has_default_gcs_bucket_name_): n += 1 + self.lengthString(len(self.default_gcs_bucket_name_))
    return n

  def Clear(self):
    self.clear_default_gcs_bucket_name()

  def OutputUnchecked(self, out):
    if (self.has_default_gcs_bucket_name_):
      out.putVarInt32(10)
      out.putPrefixedString(self.default_gcs_bucket_name_)

  def OutputPartial(self, out):
    if (self.has_default_gcs_bucket_name_):
      out.putVarInt32(10)
      out.putPrefixedString(self.default_gcs_bucket_name_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_default_gcs_bucket_name(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_default_gcs_bucket_name_: res+=prefix+("default_gcs_bucket_name: %s\n" % self.DebugFormatString(self.default_gcs_bucket_name_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kdefault_gcs_bucket_name = 1

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "default_gcs_bucket_name",
  }, 1)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
  }, 1, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.SetDefaultGcsBucketNameRequest'
  _SERIALIZED_DESCRIPTOR = array.array('B')
  _SERIALIZED_DESCRIPTOR.fromstring(base64.decodestring("WjthcHBob3N0aW5nL2FwaS9hcHBfaWRlbnRpdHkvYXBwX2lkZW50aXR5X3N0dWJfc2VydmljZS5wcm90bwopYXBwaG9zdGluZy5TZXREZWZhdWx0R2NzQnVja2V0TmFtZVJlcXVlc3QTGhdkZWZhdWx0X2djc19idWNrZXRfbmFtZSABKAIwCTgBFLoB7AIKO2FwcGhvc3RpbmcvYXBpL2FwcF9pZGVudGl0eS9hcHBfaWRlbnRpdHlfc3R1Yl9zZXJ2aWNlLnByb3RvEgphcHBob3N0aW5nGh1hcHBob3N0aW5nL2FwaS9hcGlfYmFzZS5wcm90byJBCh5TZXREZWZhdWx0R2NzQnVja2V0TmFtZVJlcXVlc3QSHwoXZGVmYXVsdF9nY3NfYnVja2V0X25hbWUYASABKAkyfQoWQXBwSWRlbnRpdHlTdHViU2VydmljZRJjChdTZXREZWZhdWx0R2NzQnVja2V0TmFtZRIqLmFwcGhvc3RpbmcuU2V0RGVmYXVsdEdjc0J1Y2tldE5hbWVSZXF1ZXN0GhouYXBwaG9zdGluZy5iYXNlLlZvaWRQcm90byIAQkAKJGNvbS5nb29nbGUuYXBwZW5naW5lLmFwaS5hcHBpZGVudGl0eUIYQXBwSWRlbnRpdHlTdHViU2VydmljZVBi"))
  if _net_proto___parse__python is not None:
    _net_proto___parse__python.RegisterType(
        _SERIALIZED_DESCRIPTOR.tostring())



class AppIdentityStubServiceStub(object):
  """Makes Stubby RPC calls to a AppIdentityStubService server."""

  __metaclass__ = abc.ABCMeta

  __slots__ = ()

  @abc.abstractmethod
  def SetDefaultGcsBucketName(self, request, rpc=None, callback=None, response=None):
    """Make a SetDefaultGcsBucketName RPC call.

    Args:
      request: a SetDefaultGcsBucketNameRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto if callback is None. Otherwise, returns None.
    """
    raise NotImplementedError()


class _AppIdentityStubService_ClientBaseStub(
    AppIdentityStubServiceStub, _client_stub_base_class):
  """Makes Stubby RPC calls to a AppIdentityStubService server."""

  __slots__ = (
      '_protorpc_SetDefaultGcsBucketName', '_full_name_SetDefaultGcsBucketName',
  )

  def __init__(self, rpc_stub, rpc_factory=None):
    super(_AppIdentityStubService_ClientBaseStub, self).__init__(
        None, inject_stub=rpc_stub, rpc_factory=rpc_factory)

    self._protorpc_SetDefaultGcsBucketName = pywraprpc.RPC()
    self._full_name_SetDefaultGcsBucketName = self._stub.GetFullMethodName(
        'SetDefaultGcsBucketName')

  def SetDefaultGcsBucketName(self, request, rpc=None, callback=None, response=None):
    """Make a SetDefaultGcsBucketName RPC call.

    Args:
      request: a SetDefaultGcsBucketNameRequest instance.
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
                          self._full_name_SetDefaultGcsBucketName,
                          'SetDefaultGcsBucketName',
                          request,
                          response,
                          callback,
                          self._protorpc_SetDefaultGcsBucketName,
                          package_name='apphosting')


class _AppIdentityStubService_ClientStub(_AppIdentityStubService_ClientBaseStub):
  __slots__ = ('_params',)
  def __init__(self, rpc_stub_parameters, service_name, rpc_factory=None):
    if service_name is None:
      service_name = 'AppIdentityStubService'
    stub = pywraprpc.RPC_GenericStub(service_name, rpc_stub_parameters)
    super(_AppIdentityStubService_ClientStub, self).__init__(stub, rpc_factory=rpc_factory)
    self._params = rpc_stub_parameters


class _AppIdentityStubService_RPC2ClientStub(_AppIdentityStubService_ClientBaseStub):
  __slots__ = ()
  def __init__(self, server, channel, service_name, rpc_factory=None):
    if service_name is None:
      service_name = 'AppIdentityStubService'
    if channel is None:
      if server is None:
        raise RuntimeError('Invalid argument combination to create a stub')
      channel = pywraprpc.NewClientChannel(server)
    elif channel.version() == 1:
      raise RuntimeError('Expecting an RPC2 channel to create the stub')
    stub = pywraprpc.RPC_GenericStub(service_name, channel)
    super(_AppIdentityStubService_RPC2ClientStub, self).__init__(stub, rpc_factory=rpc_factory)


class AppIdentityStubService(_server_stub_base_class):
  """Base class for AppIdentityStubService Stubby servers."""

  @classmethod
  def _MethodSignatures(cls):
    """Returns a dict of {<method-name>: (<request-type>, <response-type>)}."""
    return {
      'SetDefaultGcsBucketName': (SetDefaultGcsBucketNameRequest, google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto),
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
    _server_stub_base_class.__init__(self, 'apphosting.AppIdentityStubService', *args, **kwargs)

  @staticmethod
  def NewStub(rpc_stub_parameters, service_name=None, rpc_factory=None):
    """USE NewRPC2Stub INSTEAD."""
    if _client_stub_base_class is object:
      raise RuntimeError('Add //net/rpc/python as a dependency to use Stubby')
    return _AppIdentityStubService_ClientStub(
        rpc_stub_parameters, service_name, rpc_factory=rpc_factory)

  @staticmethod
  def NewRPC2Stub(
      server=None, channel=None, service_name=None, rpc_factory=None):
    """Creates a new AppIdentityStubService Stubby2 client stub.

    Args:
      server: host:port or bns address (favor passing a channel instead).
      channel: directly use a channel to create a stub. Will ignore server
          argument if this is specified.
      service_name: the service name used by the Stubby server.
      rpc_factory: the rpc factory to use if no rpc argument is specified.

    Returns:
     A AppIdentityStubServiceStub to be used to invoke RPCs.
    """

    if _client_stub_base_class is object:
      raise RuntimeError('Add //net/rpc/python:proto_python_api_2_stub (or maybe //net/rpc/python:proto_python_api_1_stub, but eww and b/67959631) as a dependency to create Stubby stubs')
    return _AppIdentityStubService_RPC2ClientStub(
        server, channel, service_name, rpc_factory=rpc_factory)

  def SetDefaultGcsBucketName(self, rpc, request, response):
    """Handles a SetDefaultGcsBucketName RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a SetDefaultGcsBucketNameRequest that contains the client request
      response: a google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto that should be modified to send the response
    """
    raise NotImplementedError()

  def _AddMethodAttributes(self):
    """Sets attributes on Python RPC handlers.

    See BaseRpcServer in rpcserver.py for details.
    """
    rpcserver._GetHandlerDecorator(
        getattr(self.SetDefaultGcsBucketName, '__func__'),
        SetDefaultGcsBucketNameRequest,
        google_dot_apphosting_dot_api_dot_api__base__pb.VoidProto,
        None,
        'INTEGRITY')

if _extension_runtime:
  pass

__all__ = ['SetDefaultGcsBucketNameRequest','AppIdentityStubService']
