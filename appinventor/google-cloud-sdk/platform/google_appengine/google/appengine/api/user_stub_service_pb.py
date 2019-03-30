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
class SetOAuthUserRequest(ProtocolBuffer.ProtocolMessage):
  has_email_ = 0
  email_ = ""
  has_auth_domain_ = 0
  auth_domain_ = ""
  has_user_id_ = 0
  user_id_ = ""
  has_is_admin_ = 0
  is_admin_ = 0
  has_client_id_ = 0
  client_id_ = ""

  def __init__(self, contents=None):
    self.scopes_ = []
    if contents is not None: self.MergeFromString(contents)

  def email(self): return self.email_

  def set_email(self, x):
    self.has_email_ = 1
    self.email_ = x

  def clear_email(self):
    if self.has_email_:
      self.has_email_ = 0
      self.email_ = ""

  def has_email(self): return self.has_email_

  def auth_domain(self): return self.auth_domain_

  def set_auth_domain(self, x):
    self.has_auth_domain_ = 1
    self.auth_domain_ = x

  def clear_auth_domain(self):
    if self.has_auth_domain_:
      self.has_auth_domain_ = 0
      self.auth_domain_ = ""

  def has_auth_domain(self): return self.has_auth_domain_

  def user_id(self): return self.user_id_

  def set_user_id(self, x):
    self.has_user_id_ = 1
    self.user_id_ = x

  def clear_user_id(self):
    if self.has_user_id_:
      self.has_user_id_ = 0
      self.user_id_ = ""

  def has_user_id(self): return self.has_user_id_

  def is_admin(self): return self.is_admin_

  def set_is_admin(self, x):
    self.has_is_admin_ = 1
    self.is_admin_ = x

  def clear_is_admin(self):
    if self.has_is_admin_:
      self.has_is_admin_ = 0
      self.is_admin_ = 0

  def has_is_admin(self): return self.has_is_admin_

  def scopes_size(self): return len(self.scopes_)
  def scopes_list(self): return self.scopes_

  def scopes(self, i):
    return self.scopes_[i]

  def set_scopes(self, i, x):
    self.scopes_[i] = x

  def add_scopes(self, x):
    self.scopes_.append(x)

  def clear_scopes(self):
    self.scopes_ = []

  def client_id(self): return self.client_id_

  def set_client_id(self, x):
    self.has_client_id_ = 1
    self.client_id_ = x

  def clear_client_id(self):
    if self.has_client_id_:
      self.has_client_id_ = 0
      self.client_id_ = ""

  def has_client_id(self): return self.has_client_id_


  def MergeFrom(self, x):
    assert x is not self
    if (x.has_email()): self.set_email(x.email())
    if (x.has_auth_domain()): self.set_auth_domain(x.auth_domain())
    if (x.has_user_id()): self.set_user_id(x.user_id())
    if (x.has_is_admin()): self.set_is_admin(x.is_admin())
    for i in range(x.scopes_size()): self.add_scopes(x.scopes(i))
    if (x.has_client_id()): self.set_client_id(x.client_id())

  def Equals(self, x):
    if x is self: return 1
    if self.has_email_ != x.has_email_: return 0
    if self.has_email_ and self.email_ != x.email_: return 0
    if self.has_auth_domain_ != x.has_auth_domain_: return 0
    if self.has_auth_domain_ and self.auth_domain_ != x.auth_domain_: return 0
    if self.has_user_id_ != x.has_user_id_: return 0
    if self.has_user_id_ and self.user_id_ != x.user_id_: return 0
    if self.has_is_admin_ != x.has_is_admin_: return 0
    if self.has_is_admin_ and self.is_admin_ != x.is_admin_: return 0
    if len(self.scopes_) != len(x.scopes_): return 0
    for e1, e2 in zip(self.scopes_, x.scopes_):
      if e1 != e2: return 0
    if self.has_client_id_ != x.has_client_id_: return 0
    if self.has_client_id_ and self.client_id_ != x.client_id_: return 0
    return 1

  def IsInitialized(self, debug_strs=None):
    initialized = 1
    return initialized

  def ByteSize(self):
    n = 0
    if (self.has_email_): n += 1 + self.lengthString(len(self.email_))
    if (self.has_auth_domain_): n += 1 + self.lengthString(len(self.auth_domain_))
    if (self.has_user_id_): n += 1 + self.lengthString(len(self.user_id_))
    if (self.has_is_admin_): n += 2
    n += 1 * len(self.scopes_)
    for i in range(len(self.scopes_)): n += self.lengthString(len(self.scopes_[i]))
    if (self.has_client_id_): n += 1 + self.lengthString(len(self.client_id_))
    return n

  def ByteSizePartial(self):
    n = 0
    if (self.has_email_): n += 1 + self.lengthString(len(self.email_))
    if (self.has_auth_domain_): n += 1 + self.lengthString(len(self.auth_domain_))
    if (self.has_user_id_): n += 1 + self.lengthString(len(self.user_id_))
    if (self.has_is_admin_): n += 2
    n += 1 * len(self.scopes_)
    for i in range(len(self.scopes_)): n += self.lengthString(len(self.scopes_[i]))
    if (self.has_client_id_): n += 1 + self.lengthString(len(self.client_id_))
    return n

  def Clear(self):
    self.clear_email()
    self.clear_auth_domain()
    self.clear_user_id()
    self.clear_is_admin()
    self.clear_scopes()
    self.clear_client_id()

  def OutputUnchecked(self, out):
    if (self.has_email_):
      out.putVarInt32(10)
      out.putPrefixedString(self.email_)
    if (self.has_auth_domain_):
      out.putVarInt32(18)
      out.putPrefixedString(self.auth_domain_)
    if (self.has_user_id_):
      out.putVarInt32(26)
      out.putPrefixedString(self.user_id_)
    if (self.has_is_admin_):
      out.putVarInt32(32)
      out.putBoolean(self.is_admin_)
    for i in range(len(self.scopes_)):
      out.putVarInt32(42)
      out.putPrefixedString(self.scopes_[i])
    if (self.has_client_id_):
      out.putVarInt32(50)
      out.putPrefixedString(self.client_id_)

  def OutputPartial(self, out):
    if (self.has_email_):
      out.putVarInt32(10)
      out.putPrefixedString(self.email_)
    if (self.has_auth_domain_):
      out.putVarInt32(18)
      out.putPrefixedString(self.auth_domain_)
    if (self.has_user_id_):
      out.putVarInt32(26)
      out.putPrefixedString(self.user_id_)
    if (self.has_is_admin_):
      out.putVarInt32(32)
      out.putBoolean(self.is_admin_)
    for i in range(len(self.scopes_)):
      out.putVarInt32(42)
      out.putPrefixedString(self.scopes_[i])
    if (self.has_client_id_):
      out.putVarInt32(50)
      out.putPrefixedString(self.client_id_)

  def TryMerge(self, d):
    while d.avail() > 0:
      tt = d.getVarInt32()
      if tt == 10:
        self.set_email(d.getPrefixedString())
        continue
      if tt == 18:
        self.set_auth_domain(d.getPrefixedString())
        continue
      if tt == 26:
        self.set_user_id(d.getPrefixedString())
        continue
      if tt == 32:
        self.set_is_admin(d.getBoolean())
        continue
      if tt == 42:
        self.add_scopes(d.getPrefixedString())
        continue
      if tt == 50:
        self.set_client_id(d.getPrefixedString())
        continue


      if (tt == 0): raise ProtocolBuffer.ProtocolBufferDecodeError()
      d.skipData(tt)


  def __str__(self, prefix="", printElemNumber=0):
    res=""
    if self.has_email_: res+=prefix+("email: %s\n" % self.DebugFormatString(self.email_))
    if self.has_auth_domain_: res+=prefix+("auth_domain: %s\n" % self.DebugFormatString(self.auth_domain_))
    if self.has_user_id_: res+=prefix+("user_id: %s\n" % self.DebugFormatString(self.user_id_))
    if self.has_is_admin_: res+=prefix+("is_admin: %s\n" % self.DebugFormatBool(self.is_admin_))
    cnt=0
    for e in self.scopes_:
      elm=""
      if printElemNumber: elm="(%d)" % cnt
      res+=prefix+("scopes%s: %s\n" % (elm, self.DebugFormatString(e)))
      cnt+=1
    if self.has_client_id_: res+=prefix+("client_id: %s\n" % self.DebugFormatString(self.client_id_))
    return res


  def _BuildTagLookupTable(sparse, maxtag, default=None):
    return tuple([sparse.get(i, default) for i in range(0, 1+maxtag)])

  kemail = 1
  kauth_domain = 2
  kuser_id = 3
  kis_admin = 4
  kscopes = 5
  kclient_id = 6

  _TEXT = _BuildTagLookupTable({
    0: "ErrorCode",
    1: "email",
    2: "auth_domain",
    3: "user_id",
    4: "is_admin",
    5: "scopes",
    6: "client_id",
  }, 6)

  _TYPES = _BuildTagLookupTable({
    0: ProtocolBuffer.Encoder.NUMERIC,
    1: ProtocolBuffer.Encoder.STRING,
    2: ProtocolBuffer.Encoder.STRING,
    3: ProtocolBuffer.Encoder.STRING,
    4: ProtocolBuffer.Encoder.NUMERIC,
    5: ProtocolBuffer.Encoder.STRING,
    6: ProtocolBuffer.Encoder.STRING,
  }, 6, ProtocolBuffer.Encoder.MAX_TYPE)


  _STYLE = """"""
  _STYLE_CONTENT_TYPE = """"""
  _PROTO_DESCRIPTOR_NAME = 'apphosting.SetOAuthUserRequest'
if _extension_runtime:
  pass

__all__ = ['SetOAuthUserRequest']
