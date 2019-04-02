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




import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
import google
from google.net.proto2.python.public import descriptor as _descriptor
from google.net.proto2.python.public import message as _message
from google.net.proto2.python.public import reflection as _reflection
from google.net.proto2.python.public import symbol_database as _symbol_database
import abc
import sys
try:
  __import__('google.net.rpc.python.proto_python_api_2_stub')
  __import__('google.net.rpc.python.pywraprpc')
  proto_python_api_2_stub = sys.modules.get('google.net.rpc.python.proto_python_api_2_stub')
  pywraprpc = sys.modules.get('google.net.rpc.python.pywraprpc')
  _client_stub_base_class = proto_python_api_2_stub.Stub
except ImportError:
  _client_stub_base_class = object
try:
  __import__('google.net.rpc.python.rpcserver')
  rpcserver = sys.modules.get('google.net.rpc.python.rpcserver')
  _server_stub_base_class = rpcserver.BaseRpcServer
except ImportError:
  _server_stub_base_class = object


_sym_db = _symbol_database.Default()


from google.storage.speckle.proto import client_pb2 as google_dot_storage_dot_speckle_dot_proto_dot_client__pb2


DESCRIPTOR = _descriptor.FileDescriptor(
  name='storage/speckle/proto/sql.proto',
  package='speckle.sql',
  syntax='proto2',
  serialized_options=_b('\n\033com.google.protos.cloud.sql\020\002(\002P\001'),
  serialized_pb=_b('\n\x1fstorage/speckle/proto/sql.proto\x12\x0bspeckle.sql\x1a\"storage/speckle/proto/client.proto\"\x8c\x03\n\x0b\x45xecRequest\x12\x10\n\x08instance\x18\x01 \x02(\t\x12\x14\n\x0cstatement_id\x18\x02 \x01(\x04\x12\x11\n\tstatement\x18\x03 \x01(\t\x12\x31\n\rbind_variable\x18\x04 \x03(\x0b\x32\x1a.speckle.BindVariableProto\x12\x15\n\rconnection_id\x18\x05 \x02(\x0c\x12%\n\x07options\x18\x06 \x01(\x0b\x32\x14.speckle.ExecOptions\x12I\n\x0estatement_type\x18\t \x01(\x0e\x32&.speckle.sql.ExecRequest.StatementType:\tSTATEMENT\x12\"\n\x05\x62\x61tch\x18\n \x01(\x0b\x32\x13.speckle.BatchProto\x12\x12\n\nrequest_id\x18\x0b \x01(\x04\"N\n\rStatementType\x12\r\n\tSTATEMENT\x10\x01\x12\x16\n\x12PREPARED_STATEMENT\x10\x02\x12\x16\n\x12\x43\x41LLABLE_STATEMENT\x10\x03\"b\n\x0c\x45xecResponse\x12$\n\x06result\x18\x01 \x01(\x0b\x32\x14.speckle.ResultProto\x12,\n\rsql_exception\x18\x02 \x01(\x0b\x32\x15.speckle.SqlException\"j\n\rExecOpRequest\x12\x10\n\x08instance\x18\x01 \x02(\t\x12\x15\n\rconnection_id\x18\x02 \x02(\x0c\x12\x1c\n\x02op\x18\x03 \x02(\x0b\x32\x10.speckle.OpProto\x12\x12\n\nrequest_id\x18\x08 \x01(\x04\"\xed\x01\n\x0e\x45xecOpResponse\x12\x12\n\nnative_sql\x18\x01 \x01(\t\x12%\n\tsavepoint\x18\x02 \x01(\x0b\x32\x12.speckle.SavePoint\x12,\n\rsql_exception\x18\x03 \x01(\x0b\x32\x15.speckle.SqlException\x12$\n\x06result\x18\x04 \x01(\x0b\x32\x14.speckle.ResultProto\x12\x30\n\x10\x63\x61\x63hed_rpc_error\x18\x05 \x01(\x0b\x32\x16.speckle.RpcErrorProto\x12\x1a\n\x0e\x63\x61\x63hed_payload\x18\x06 \x01(\x0c\x42\x02\x08\x01\"\xaa\x01\n\x0fMetadataRequest\x12\x10\n\x08instance\x18\x01 \x02(\t\x12\'\n\x08metadata\x18\x03 \x02(\x0e\x32\x15.speckle.MetadataType\x12\x31\n\rbind_variable\x18\x04 \x03(\x0b\x32\x1a.speckle.BindVariableProto\x12\x15\n\rconnection_id\x18\x05 \x02(\x0c\x12\x12\n\nrequest_id\x18\x08 \x01(\x04\"\xaa\x01\n\x10MetadataResponse\x12$\n\x06result\x18\x01 \x01(\x0b\x32\x14.speckle.ResultProto\x12\x42\n\x16jdbc_database_metadata\x18\x02 \x01(\x0b\x32\".speckle.JdbcDatabaseMetaDataProto\x12,\n\rsql_exception\x18\x03 \x01(\x0b\x32\x15.speckle.SqlException\"\xac\x01\n\x15OpenConnectionRequest\x12\x10\n\x08instance\x18\x01 \x02(\t\x12#\n\x08property\x18\x02 \x03(\x0b\x32\x11.speckle.Property\x12\x1b\n\x10protocol_version\x18\x05 \x01(\x04:\x01\x31\x12?\n\x0b\x63lient_type\x18\x06 \x01(\x0e\x32\x13.speckle.ClientType:\x15\x43LIENT_TYPE_JAVA_JDBC\"\x86\x01\n\x16OpenConnectionResponse\x12\x15\n\rconnection_id\x18\x01 \x01(\x0c\x12,\n\rsql_exception\x18\x02 \x01(\x0b\x32\x15.speckle.SqlException\x12\'\n\x08warnings\x18\x06 \x03(\x0b\x32\x15.speckle.SqlException\"A\n\x16\x43loseConnectionRequest\x12\x10\n\x08instance\x18\x01 \x02(\t\x12\x15\n\rconnection_id\x18\x02 \x02(\x0c\"G\n\x17\x43loseConnectionResponse\x12,\n\rsql_exception\x18\x01 \x01(\x0b\x32\x15.speckle.SqlException2\xac\x03\n\nSqlService\x12?\n\x04\x45xec\x12\x18.speckle.sql.ExecRequest\x1a\x19.speckle.sql.ExecResponse\"\x02P\x01\x12\x45\n\x06\x45xecOp\x12\x1a.speckle.sql.ExecOpRequest\x1a\x1b.speckle.sql.ExecOpResponse\"\x02P\x01\x12N\n\x0bGetMetadata\x12\x1c.speckle.sql.MetadataRequest\x1a\x1d.speckle.sql.MetadataResponse\"\x02P\x01\x12\x64\n\x0eOpenConnection\x12\".speckle.sql.OpenConnectionRequest\x1a#.speckle.sql.OpenConnectionResponse\"\tP\x01\x9a\x01\x04read\x12`\n\x0f\x43loseConnection\x12#.speckle.sql.CloseConnectionRequest\x1a$.speckle.sql.CloseConnectionResponse\"\x02P\x01\x42#\n\x1b\x63om.google.protos.cloud.sql\x10\x02(\x02P\x01')
  ,
  dependencies=[google_dot_storage_dot_speckle_dot_proto_dot_client__pb2.DESCRIPTOR,])



_EXECREQUEST_STATEMENTTYPE = _descriptor.EnumDescriptor(
  name='StatementType',
  full_name='speckle.sql.ExecRequest.StatementType',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='STATEMENT', index=0, number=1,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='PREPARED_STATEMENT', index=1, number=2,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='CALLABLE_STATEMENT', index=2, number=3,
      serialized_options=None,
      type=None),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=403,
  serialized_end=481,
)
_sym_db.RegisterEnumDescriptor(_EXECREQUEST_STATEMENTTYPE)


_EXECREQUEST = _descriptor.Descriptor(
  name='ExecRequest',
  full_name='speckle.sql.ExecRequest',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='instance', full_name='speckle.sql.ExecRequest.instance', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='statement_id', full_name='speckle.sql.ExecRequest.statement_id', index=1,
      number=2, type=4, cpp_type=4, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='statement', full_name='speckle.sql.ExecRequest.statement', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='bind_variable', full_name='speckle.sql.ExecRequest.bind_variable', index=3,
      number=4, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='connection_id', full_name='speckle.sql.ExecRequest.connection_id', index=4,
      number=5, type=12, cpp_type=9, label=2,
      has_default_value=False, default_value=_b(""),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='options', full_name='speckle.sql.ExecRequest.options', index=5,
      number=6, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='statement_type', full_name='speckle.sql.ExecRequest.statement_type', index=6,
      number=9, type=14, cpp_type=8, label=1,
      has_default_value=True, default_value=1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='batch', full_name='speckle.sql.ExecRequest.batch', index=7,
      number=10, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='request_id', full_name='speckle.sql.ExecRequest.request_id', index=8,
      number=11, type=4, cpp_type=4, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
    _EXECREQUEST_STATEMENTTYPE,
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=85,
  serialized_end=481,
)


_EXECRESPONSE = _descriptor.Descriptor(
  name='ExecResponse',
  full_name='speckle.sql.ExecResponse',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='result', full_name='speckle.sql.ExecResponse.result', index=0,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='sql_exception', full_name='speckle.sql.ExecResponse.sql_exception', index=1,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=483,
  serialized_end=581,
)


_EXECOPREQUEST = _descriptor.Descriptor(
  name='ExecOpRequest',
  full_name='speckle.sql.ExecOpRequest',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='instance', full_name='speckle.sql.ExecOpRequest.instance', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='connection_id', full_name='speckle.sql.ExecOpRequest.connection_id', index=1,
      number=2, type=12, cpp_type=9, label=2,
      has_default_value=False, default_value=_b(""),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='op', full_name='speckle.sql.ExecOpRequest.op', index=2,
      number=3, type=11, cpp_type=10, label=2,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='request_id', full_name='speckle.sql.ExecOpRequest.request_id', index=3,
      number=8, type=4, cpp_type=4, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=583,
  serialized_end=689,
)


_EXECOPRESPONSE = _descriptor.Descriptor(
  name='ExecOpResponse',
  full_name='speckle.sql.ExecOpResponse',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='native_sql', full_name='speckle.sql.ExecOpResponse.native_sql', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='savepoint', full_name='speckle.sql.ExecOpResponse.savepoint', index=1,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='sql_exception', full_name='speckle.sql.ExecOpResponse.sql_exception', index=2,
      number=3, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='result', full_name='speckle.sql.ExecOpResponse.result', index=3,
      number=4, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='cached_rpc_error', full_name='speckle.sql.ExecOpResponse.cached_rpc_error', index=4,
      number=5, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='cached_payload', full_name='speckle.sql.ExecOpResponse.cached_payload', index=5,
      number=6, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=_b(""),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=_b('\010\001'), file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=692,
  serialized_end=929,
)


_METADATAREQUEST = _descriptor.Descriptor(
  name='MetadataRequest',
  full_name='speckle.sql.MetadataRequest',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='instance', full_name='speckle.sql.MetadataRequest.instance', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='metadata', full_name='speckle.sql.MetadataRequest.metadata', index=1,
      number=3, type=14, cpp_type=8, label=2,
      has_default_value=False, default_value=1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='bind_variable', full_name='speckle.sql.MetadataRequest.bind_variable', index=2,
      number=4, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='connection_id', full_name='speckle.sql.MetadataRequest.connection_id', index=3,
      number=5, type=12, cpp_type=9, label=2,
      has_default_value=False, default_value=_b(""),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='request_id', full_name='speckle.sql.MetadataRequest.request_id', index=4,
      number=8, type=4, cpp_type=4, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=932,
  serialized_end=1102,
)


_METADATARESPONSE = _descriptor.Descriptor(
  name='MetadataResponse',
  full_name='speckle.sql.MetadataResponse',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='result', full_name='speckle.sql.MetadataResponse.result', index=0,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='jdbc_database_metadata', full_name='speckle.sql.MetadataResponse.jdbc_database_metadata', index=1,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='sql_exception', full_name='speckle.sql.MetadataResponse.sql_exception', index=2,
      number=3, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=1105,
  serialized_end=1275,
)


_OPENCONNECTIONREQUEST = _descriptor.Descriptor(
  name='OpenConnectionRequest',
  full_name='speckle.sql.OpenConnectionRequest',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='instance', full_name='speckle.sql.OpenConnectionRequest.instance', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='property', full_name='speckle.sql.OpenConnectionRequest.property', index=1,
      number=2, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='protocol_version', full_name='speckle.sql.OpenConnectionRequest.protocol_version', index=2,
      number=5, type=4, cpp_type=4, label=1,
      has_default_value=True, default_value=1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='client_type', full_name='speckle.sql.OpenConnectionRequest.client_type', index=3,
      number=6, type=14, cpp_type=8, label=1,
      has_default_value=True, default_value=1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=1278,
  serialized_end=1450,
)


_OPENCONNECTIONRESPONSE = _descriptor.Descriptor(
  name='OpenConnectionResponse',
  full_name='speckle.sql.OpenConnectionResponse',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='connection_id', full_name='speckle.sql.OpenConnectionResponse.connection_id', index=0,
      number=1, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=_b(""),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='sql_exception', full_name='speckle.sql.OpenConnectionResponse.sql_exception', index=1,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='warnings', full_name='speckle.sql.OpenConnectionResponse.warnings', index=2,
      number=6, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=1453,
  serialized_end=1587,
)


_CLOSECONNECTIONREQUEST = _descriptor.Descriptor(
  name='CloseConnectionRequest',
  full_name='speckle.sql.CloseConnectionRequest',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='instance', full_name='speckle.sql.CloseConnectionRequest.instance', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='connection_id', full_name='speckle.sql.CloseConnectionRequest.connection_id', index=1,
      number=2, type=12, cpp_type=9, label=2,
      has_default_value=False, default_value=_b(""),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=1589,
  serialized_end=1654,
)


_CLOSECONNECTIONRESPONSE = _descriptor.Descriptor(
  name='CloseConnectionResponse',
  full_name='speckle.sql.CloseConnectionResponse',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='sql_exception', full_name='speckle.sql.CloseConnectionResponse.sql_exception', index=0,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=1656,
  serialized_end=1727,
)

_EXECREQUEST.fields_by_name['bind_variable'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._BINDVARIABLEPROTO
_EXECREQUEST.fields_by_name['options'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._EXECOPTIONS
_EXECREQUEST.fields_by_name['statement_type'].enum_type = _EXECREQUEST_STATEMENTTYPE
_EXECREQUEST.fields_by_name['batch'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._BATCHPROTO
_EXECREQUEST_STATEMENTTYPE.containing_type = _EXECREQUEST
_EXECRESPONSE.fields_by_name['result'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._RESULTPROTO
_EXECRESPONSE.fields_by_name['sql_exception'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._SQLEXCEPTION
_EXECOPREQUEST.fields_by_name['op'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._OPPROTO
_EXECOPRESPONSE.fields_by_name['savepoint'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._SAVEPOINT
_EXECOPRESPONSE.fields_by_name['sql_exception'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._SQLEXCEPTION
_EXECOPRESPONSE.fields_by_name['result'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._RESULTPROTO
_EXECOPRESPONSE.fields_by_name['cached_rpc_error'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._RPCERRORPROTO
_METADATAREQUEST.fields_by_name['metadata'].enum_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._METADATATYPE
_METADATAREQUEST.fields_by_name['bind_variable'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._BINDVARIABLEPROTO
_METADATARESPONSE.fields_by_name['result'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._RESULTPROTO
_METADATARESPONSE.fields_by_name['jdbc_database_metadata'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._JDBCDATABASEMETADATAPROTO
_METADATARESPONSE.fields_by_name['sql_exception'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._SQLEXCEPTION
_OPENCONNECTIONREQUEST.fields_by_name['property'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._PROPERTY
_OPENCONNECTIONREQUEST.fields_by_name['client_type'].enum_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._CLIENTTYPE
_OPENCONNECTIONRESPONSE.fields_by_name['sql_exception'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._SQLEXCEPTION
_OPENCONNECTIONRESPONSE.fields_by_name['warnings'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._SQLEXCEPTION
_CLOSECONNECTIONRESPONSE.fields_by_name['sql_exception'].message_type = google_dot_storage_dot_speckle_dot_proto_dot_client__pb2._SQLEXCEPTION
DESCRIPTOR.message_types_by_name['ExecRequest'] = _EXECREQUEST
DESCRIPTOR.message_types_by_name['ExecResponse'] = _EXECRESPONSE
DESCRIPTOR.message_types_by_name['ExecOpRequest'] = _EXECOPREQUEST
DESCRIPTOR.message_types_by_name['ExecOpResponse'] = _EXECOPRESPONSE
DESCRIPTOR.message_types_by_name['MetadataRequest'] = _METADATAREQUEST
DESCRIPTOR.message_types_by_name['MetadataResponse'] = _METADATARESPONSE
DESCRIPTOR.message_types_by_name['OpenConnectionRequest'] = _OPENCONNECTIONREQUEST
DESCRIPTOR.message_types_by_name['OpenConnectionResponse'] = _OPENCONNECTIONRESPONSE
DESCRIPTOR.message_types_by_name['CloseConnectionRequest'] = _CLOSECONNECTIONREQUEST
DESCRIPTOR.message_types_by_name['CloseConnectionResponse'] = _CLOSECONNECTIONRESPONSE
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

ExecRequest = _reflection.GeneratedProtocolMessageType('ExecRequest', (_message.Message,), dict(
  DESCRIPTOR = _EXECREQUEST,
  __module__ = 'google.storage.speckle.proto.sql_pb2'

  ))
_sym_db.RegisterMessage(ExecRequest)

ExecResponse = _reflection.GeneratedProtocolMessageType('ExecResponse', (_message.Message,), dict(
  DESCRIPTOR = _EXECRESPONSE,
  __module__ = 'google.storage.speckle.proto.sql_pb2'

  ))
_sym_db.RegisterMessage(ExecResponse)

ExecOpRequest = _reflection.GeneratedProtocolMessageType('ExecOpRequest', (_message.Message,), dict(
  DESCRIPTOR = _EXECOPREQUEST,
  __module__ = 'google.storage.speckle.proto.sql_pb2'

  ))
_sym_db.RegisterMessage(ExecOpRequest)

ExecOpResponse = _reflection.GeneratedProtocolMessageType('ExecOpResponse', (_message.Message,), dict(
  DESCRIPTOR = _EXECOPRESPONSE,
  __module__ = 'google.storage.speckle.proto.sql_pb2'

  ))
_sym_db.RegisterMessage(ExecOpResponse)

MetadataRequest = _reflection.GeneratedProtocolMessageType('MetadataRequest', (_message.Message,), dict(
  DESCRIPTOR = _METADATAREQUEST,
  __module__ = 'google.storage.speckle.proto.sql_pb2'

  ))
_sym_db.RegisterMessage(MetadataRequest)

MetadataResponse = _reflection.GeneratedProtocolMessageType('MetadataResponse', (_message.Message,), dict(
  DESCRIPTOR = _METADATARESPONSE,
  __module__ = 'google.storage.speckle.proto.sql_pb2'

  ))
_sym_db.RegisterMessage(MetadataResponse)

OpenConnectionRequest = _reflection.GeneratedProtocolMessageType('OpenConnectionRequest', (_message.Message,), dict(
  DESCRIPTOR = _OPENCONNECTIONREQUEST,
  __module__ = 'google.storage.speckle.proto.sql_pb2'

  ))
_sym_db.RegisterMessage(OpenConnectionRequest)

OpenConnectionResponse = _reflection.GeneratedProtocolMessageType('OpenConnectionResponse', (_message.Message,), dict(
  DESCRIPTOR = _OPENCONNECTIONRESPONSE,
  __module__ = 'google.storage.speckle.proto.sql_pb2'

  ))
_sym_db.RegisterMessage(OpenConnectionResponse)

CloseConnectionRequest = _reflection.GeneratedProtocolMessageType('CloseConnectionRequest', (_message.Message,), dict(
  DESCRIPTOR = _CLOSECONNECTIONREQUEST,
  __module__ = 'google.storage.speckle.proto.sql_pb2'

  ))
_sym_db.RegisterMessage(CloseConnectionRequest)

CloseConnectionResponse = _reflection.GeneratedProtocolMessageType('CloseConnectionResponse', (_message.Message,), dict(
  DESCRIPTOR = _CLOSECONNECTIONRESPONSE,
  __module__ = 'google.storage.speckle.proto.sql_pb2'

  ))
_sym_db.RegisterMessage(CloseConnectionResponse)


DESCRIPTOR._options = None
_EXECOPRESPONSE.fields_by_name['cached_payload']._options = None

_SQLSERVICE = _descriptor.ServiceDescriptor(
  name='SqlService',
  full_name='speckle.sql.SqlService',
  file=DESCRIPTOR,
  index=0,
  serialized_options=None,
  serialized_start=1730,
  serialized_end=2158,
  methods=[
  _descriptor.MethodDescriptor(
    name='Exec',
    full_name='speckle.sql.SqlService.Exec',
    index=0,
    containing_service=None,
    input_type=_EXECREQUEST,
    output_type=_EXECRESPONSE,
    serialized_options=_b('P\001'),
  ),
  _descriptor.MethodDescriptor(
    name='ExecOp',
    full_name='speckle.sql.SqlService.ExecOp',
    index=1,
    containing_service=None,
    input_type=_EXECOPREQUEST,
    output_type=_EXECOPRESPONSE,
    serialized_options=_b('P\001'),
  ),
  _descriptor.MethodDescriptor(
    name='GetMetadata',
    full_name='speckle.sql.SqlService.GetMetadata',
    index=2,
    containing_service=None,
    input_type=_METADATAREQUEST,
    output_type=_METADATARESPONSE,
    serialized_options=_b('P\001'),
  ),
  _descriptor.MethodDescriptor(
    name='OpenConnection',
    full_name='speckle.sql.SqlService.OpenConnection',
    index=3,
    containing_service=None,
    input_type=_OPENCONNECTIONREQUEST,
    output_type=_OPENCONNECTIONRESPONSE,
    serialized_options=_b('P\001\232\001\004read'),
  ),
  _descriptor.MethodDescriptor(
    name='CloseConnection',
    full_name='speckle.sql.SqlService.CloseConnection',
    index=4,
    containing_service=None,
    input_type=_CLOSECONNECTIONREQUEST,
    output_type=_CLOSECONNECTIONRESPONSE,
    serialized_options=_b('P\001'),
  ),
])
_sym_db.RegisterServiceDescriptor(_SQLSERVICE)

DESCRIPTOR.services_by_name['SqlService'] = _SQLSERVICE



class SqlServiceStub(object):
  """Makes Stubby RPC calls to a SqlService server."""

  __metaclass__ = abc.ABCMeta

  __slots__ = ()

  @abc.abstractmethod
  def Exec(self, request, rpc=None, callback=None, response=None):
    """Make a Exec RPC call.

    Args:
      request: a ExecRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The ExecResponse if callback is None. Otherwise, returns None.
    """
    raise NotImplementedError()

  @abc.abstractmethod
  def ExecOp(self, request, rpc=None, callback=None, response=None):
    """Make a ExecOp RPC call.

    Args:
      request: a ExecOpRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The ExecOpResponse if callback is None. Otherwise, returns None.
    """
    raise NotImplementedError()

  @abc.abstractmethod
  def GetMetadata(self, request, rpc=None, callback=None, response=None):
    """Make a GetMetadata RPC call.

    Args:
      request: a MetadataRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The MetadataResponse if callback is None. Otherwise, returns None.
    """
    raise NotImplementedError()

  @abc.abstractmethod
  def OpenConnection(self, request, rpc=None, callback=None, response=None):
    """Make a OpenConnection RPC call.

    Args:
      request: a OpenConnectionRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The OpenConnectionResponse if callback is None. Otherwise, returns None.
    """
    raise NotImplementedError()

  @abc.abstractmethod
  def CloseConnection(self, request, rpc=None, callback=None, response=None):
    """Make a CloseConnection RPC call.

    Args:
      request: a CloseConnectionRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The CloseConnectionResponse if callback is None. Otherwise, returns None.
    """
    raise NotImplementedError()


class _SqlService_ClientBaseStub(
    SqlServiceStub, _client_stub_base_class):
  """Makes Stubby RPC calls to a SqlService server."""

  __slots__ = (
      '_protorpc_Exec', '_full_name_Exec',
      '_protorpc_ExecOp', '_full_name_ExecOp',
      '_protorpc_GetMetadata', '_full_name_GetMetadata',
      '_protorpc_OpenConnection', '_full_name_OpenConnection',
      '_protorpc_CloseConnection', '_full_name_CloseConnection',
  )

  def __init__(self, rpc_stub, rpc_factory=None):
    super(_SqlService_ClientBaseStub, self).__init__(
        None, inject_stub=rpc_stub, rpc_factory=rpc_factory)

    self._protorpc_Exec = pywraprpc.RPC()
    self._protorpc_Exec.set_fail_fast(True)
    self._full_name_Exec = self._stub.GetFullMethodName(
        'Exec')

    self._protorpc_ExecOp = pywraprpc.RPC()
    self._protorpc_ExecOp.set_fail_fast(True)
    self._full_name_ExecOp = self._stub.GetFullMethodName(
        'ExecOp')

    self._protorpc_GetMetadata = pywraprpc.RPC()
    self._protorpc_GetMetadata.set_fail_fast(True)
    self._full_name_GetMetadata = self._stub.GetFullMethodName(
        'GetMetadata')

    self._protorpc_OpenConnection = pywraprpc.RPC()
    self._protorpc_OpenConnection.set_fail_fast(True)
    self._full_name_OpenConnection = self._stub.GetFullMethodName(
        'OpenConnection')

    self._protorpc_CloseConnection = pywraprpc.RPC()
    self._protorpc_CloseConnection.set_fail_fast(True)
    self._full_name_CloseConnection = self._stub.GetFullMethodName(
        'CloseConnection')

  def Exec(self, request, rpc=None, callback=None, response=None):
    """Make a Exec RPC call.

    Args:
      request: a ExecRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The ExecResponse if callback is None. Otherwise, returns None.
    """

    if response is None:
      response = ExecResponse
    return self._MakeCall(rpc,
                          self._full_name_Exec,
                          'Exec',
                          request,
                          response,
                          callback,
                          self._protorpc_Exec,
                          package_name='speckle.sql')

  def ExecOp(self, request, rpc=None, callback=None, response=None):
    """Make a ExecOp RPC call.

    Args:
      request: a ExecOpRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The ExecOpResponse if callback is None. Otherwise, returns None.
    """

    if response is None:
      response = ExecOpResponse
    return self._MakeCall(rpc,
                          self._full_name_ExecOp,
                          'ExecOp',
                          request,
                          response,
                          callback,
                          self._protorpc_ExecOp,
                          package_name='speckle.sql')

  def GetMetadata(self, request, rpc=None, callback=None, response=None):
    """Make a GetMetadata RPC call.

    Args:
      request: a MetadataRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The MetadataResponse if callback is None. Otherwise, returns None.
    """

    if response is None:
      response = MetadataResponse
    return self._MakeCall(rpc,
                          self._full_name_GetMetadata,
                          'GetMetadata',
                          request,
                          response,
                          callback,
                          self._protorpc_GetMetadata,
                          package_name='speckle.sql')

  def OpenConnection(self, request, rpc=None, callback=None, response=None):
    """Make a OpenConnection RPC call.

    Args:
      request: a OpenConnectionRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The OpenConnectionResponse if callback is None. Otherwise, returns None.
    """

    if response is None:
      response = OpenConnectionResponse
    return self._MakeCall(rpc,
                          self._full_name_OpenConnection,
                          'OpenConnection',
                          request,
                          response,
                          callback,
                          self._protorpc_OpenConnection,
                          package_name='speckle.sql')

  def CloseConnection(self, request, rpc=None, callback=None, response=None):
    """Make a CloseConnection RPC call.

    Args:
      request: a CloseConnectionRequest instance.
      rpc: Optional RPC instance to use for the call.
      callback: Optional final callback. Will be called as
          callback(rpc, result) when the rpc completes. If None, the
          call is synchronous.
      response: Optional ProtocolMessage to be filled in with response.

    Returns:
      The CloseConnectionResponse if callback is None. Otherwise, returns None.
    """

    if response is None:
      response = CloseConnectionResponse
    return self._MakeCall(rpc,
                          self._full_name_CloseConnection,
                          'CloseConnection',
                          request,
                          response,
                          callback,
                          self._protorpc_CloseConnection,
                          package_name='speckle.sql')


class _SqlService_ClientStub(_SqlService_ClientBaseStub):
  __slots__ = ('_params',)
  def __init__(self, rpc_stub_parameters, service_name, rpc_factory=None):
    if service_name is None:
      service_name = 'SqlService'
    stub = pywraprpc.RPC_GenericStub(service_name, rpc_stub_parameters)
    super(_SqlService_ClientStub, self).__init__(stub, rpc_factory=rpc_factory)
    self._params = rpc_stub_parameters


class _SqlService_RPC2ClientStub(_SqlService_ClientBaseStub):
  __slots__ = ()
  def __init__(self, server, channel, service_name, rpc_factory=None):
    if service_name is None:
      service_name = 'SqlService'
    if channel is None:
      if server is None:
        raise RuntimeError('Invalid argument combination to create a stub')
      channel = pywraprpc.NewClientChannel(server)
    elif channel.version() == 1:
      raise RuntimeError('Expecting an RPC2 channel to create the stub')
    stub = pywraprpc.RPC_GenericStub(service_name, channel)
    super(_SqlService_RPC2ClientStub, self).__init__(stub, rpc_factory=rpc_factory)


class SqlService(_server_stub_base_class):
  """Base class for SqlService Stubby servers."""

  @classmethod
  def _MethodSignatures(cls):
    """Returns a dict of {<method-name>: (<request-type>, <response-type>)}."""
    return {
      'Exec': (ExecRequest, ExecResponse),
      'ExecOp': (ExecOpRequest, ExecOpResponse),
      'GetMetadata': (MetadataRequest, MetadataResponse),
      'OpenConnection': (OpenConnectionRequest, OpenConnectionResponse),
      'CloseConnection': (CloseConnectionRequest, CloseConnectionResponse),
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
    _server_stub_base_class.__init__(self, 'speckle.sql.SqlService', *args, **kwargs)

  @staticmethod
  def NewStub(rpc_stub_parameters, service_name=None, rpc_factory=None):
    """USE NewRPC2Stub INSTEAD."""
    if _client_stub_base_class is object:
      raise RuntimeError('Add //net/rpc/python as a dependency to use Stubby')
    return _SqlService_ClientStub(
        rpc_stub_parameters, service_name, rpc_factory=rpc_factory)

  @staticmethod
  def NewRPC2Stub(
      server=None, channel=None, service_name=None, rpc_factory=None):
    """Creates a new SqlService Stubby2 client stub.

    Args:
      server: host:port or bns address (favor passing a channel instead).
      channel: directly use a channel to create a stub. Will ignore server
          argument if this is specified.
      service_name: the service name used by the Stubby server.
      rpc_factory: the rpc factory to use if no rpc argument is specified.

    Returns:
     A SqlServiceStub to be used to invoke RPCs.
    """

    if _client_stub_base_class is object:
      raise RuntimeError('Add //net/rpc/python:proto_python_api_2_stub (or maybe //net/rpc/python:proto_python_api_1_stub, but eww and b/67959631) as a dependency to create Stubby stubs')
    return _SqlService_RPC2ClientStub(
        server, channel, service_name, rpc_factory=rpc_factory)

  def Exec(self, rpc, request, response):
    """Handles a Exec RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a ExecRequest that contains the client request
      response: a ExecResponse that should be modified to send the response
    """
    raise NotImplementedError()


  def ExecOp(self, rpc, request, response):
    """Handles a ExecOp RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a ExecOpRequest that contains the client request
      response: a ExecOpResponse that should be modified to send the response
    """
    raise NotImplementedError()


  def GetMetadata(self, rpc, request, response):
    """Handles a GetMetadata RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a MetadataRequest that contains the client request
      response: a MetadataResponse that should be modified to send the response
    """
    raise NotImplementedError()


  def OpenConnection(self, rpc, request, response):
    """Handles a OpenConnection RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a OpenConnectionRequest that contains the client request
      response: a OpenConnectionResponse that should be modified to send the response
    """
    raise NotImplementedError()


  def CloseConnection(self, rpc, request, response):
    """Handles a CloseConnection RPC call. You should override this.

    Args:
      rpc: a Stubby RPC object
      request: a CloseConnectionRequest that contains the client request
      response: a CloseConnectionResponse that should be modified to send the response
    """
    raise NotImplementedError()

  def _AddMethodAttributes(self):
    """Sets attributes on Python RPC handlers.

    See BaseRpcServer in rpcserver.py for details.
    """
    rpcserver._GetHandlerDecorator(
        getattr(self.Exec, '__func__'),
        ExecRequest,
        ExecResponse,
        None,
        'INTEGRITY')
    rpcserver._GetHandlerDecorator(
        getattr(self.ExecOp, '__func__'),
        ExecOpRequest,
        ExecOpResponse,
        None,
        'INTEGRITY')
    rpcserver._GetHandlerDecorator(
        getattr(self.GetMetadata, '__func__'),
        MetadataRequest,
        MetadataResponse,
        None,
        'INTEGRITY')
    rpcserver._GetHandlerDecorator(
        getattr(self.OpenConnection, '__func__'),
        OpenConnectionRequest,
        OpenConnectionResponse,
        None,
        'INTEGRITY')
    rpcserver._GetHandlerDecorator(
        getattr(self.CloseConnection, '__func__'),
        CloseConnectionRequest,
        CloseConnectionResponse,
        None,
        'INTEGRITY')


