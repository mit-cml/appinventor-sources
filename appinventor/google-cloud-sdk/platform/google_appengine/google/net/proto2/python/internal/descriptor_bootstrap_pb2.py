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


_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor.FileDescriptor(
  name='net/proto2/proto/descriptor.proto',
  package='proto2',
  syntax='proto2',
  serialized_options=None,
  serialized_pb=_b('\n!net/proto2/proto/descriptor.proto\x12\x06proto2\">\n\x11\x46ileDescriptorSet\x12)\n\x04\x66ile\x18\x01 \x03(\x0b\x32\x1b.proto2.FileDescriptorProto\"\xa5\x03\n\x13\x46ileDescriptorProto\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x0f\n\x07package\x18\x02 \x01(\t\x12\x12\n\ndependency\x18\x03 \x03(\t\x12\x19\n\x11public_dependency\x18\n \x03(\x05\x12\x17\n\x0fweak_dependency\x18\x0b \x03(\x05\x12-\n\x0cmessage_type\x18\x04 \x03(\x0b\x32\x17.proto2.DescriptorProto\x12.\n\tenum_type\x18\x05 \x03(\x0b\x32\x1b.proto2.EnumDescriptorProto\x12/\n\x07service\x18\x06 \x03(\x0b\x32\x1e.proto2.ServiceDescriptorProto\x12/\n\textension\x18\x07 \x03(\x0b\x32\x1c.proto2.FieldDescriptorProto\x12$\n\x07options\x18\x08 \x01(\x0b\x32\x13.proto2.FileOptions\x12\x30\n\x10source_code_info\x18\t \x01(\x0b\x32\x16.proto2.SourceCodeInfo\x12\x0e\n\x06syntax\x18\x0c \x01(\t\"\xd8\x04\n\x0f\x44\x65scriptorProto\x12\x0c\n\x04name\x18\x01 \x01(\t\x12+\n\x05\x66ield\x18\x02 \x03(\x0b\x32\x1c.proto2.FieldDescriptorProto\x12/\n\textension\x18\x06 \x03(\x0b\x32\x1c.proto2.FieldDescriptorProto\x12,\n\x0bnested_type\x18\x03 \x03(\x0b\x32\x17.proto2.DescriptorProto\x12.\n\tenum_type\x18\x04 \x03(\x0b\x32\x1b.proto2.EnumDescriptorProto\x12?\n\x0f\x65xtension_range\x18\x05 \x03(\x0b\x32&.proto2.DescriptorProto.ExtensionRange\x12\x30\n\noneof_decl\x18\x08 \x03(\x0b\x32\x1c.proto2.OneofDescriptorProto\x12\'\n\x07options\x18\x07 \x01(\x0b\x32\x16.proto2.MessageOptions\x12=\n\x0ereserved_range\x18\t \x03(\x0b\x32%.proto2.DescriptorProto.ReservedRange\x12\x15\n\rreserved_name\x18\n \x03(\t\x1a\\\n\x0e\x45xtensionRange\x12\r\n\x05start\x18\x01 \x01(\x05\x12\x0b\n\x03\x65nd\x18\x02 \x01(\x05\x12.\n\x07options\x18\x03 \x01(\x0b\x32\x1d.proto2.ExtensionRangeOptions\x1a+\n\rReservedRange\x12\r\n\x05start\x18\x01 \x01(\x05\x12\x0b\n\x03\x65nd\x18\x02 \x01(\x05\"^\n\x15\x45xtensionRangeOptions\x12:\n\x14uninterpreted_option\x18\xe7\x07 \x03(\x0b\x32\x1b.proto2.UninterpretedOption*\t\x08\xe8\x07\x10\x80\x80\x80\x80\x02\"\xa1\x05\n\x14\x46ieldDescriptorProto\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x0e\n\x06number\x18\x03 \x01(\x05\x12\x31\n\x05label\x18\x04 \x01(\x0e\x32\".proto2.FieldDescriptorProto.Label\x12/\n\x04type\x18\x05 \x01(\x0e\x32!.proto2.FieldDescriptorProto.Type\x12\x11\n\ttype_name\x18\x06 \x01(\t\x12\x10\n\x08\x65xtendee\x18\x02 \x01(\t\x12\x15\n\rdefault_value\x18\x07 \x01(\t\x12\x13\n\x0boneof_index\x18\t \x01(\x05\x12\x11\n\tjson_name\x18\n \x01(\t\x12%\n\x07options\x18\x08 \x01(\x0b\x32\x14.proto2.FieldOptions\"\xb6\x02\n\x04Type\x12\x0f\n\x0bTYPE_DOUBLE\x10\x01\x12\x0e\n\nTYPE_FLOAT\x10\x02\x12\x0e\n\nTYPE_INT64\x10\x03\x12\x0f\n\x0bTYPE_UINT64\x10\x04\x12\x0e\n\nTYPE_INT32\x10\x05\x12\x10\n\x0cTYPE_FIXED64\x10\x06\x12\x10\n\x0cTYPE_FIXED32\x10\x07\x12\r\n\tTYPE_BOOL\x10\x08\x12\x0f\n\x0bTYPE_STRING\x10\t\x12\x0e\n\nTYPE_GROUP\x10\n\x12\x10\n\x0cTYPE_MESSAGE\x10\x0b\x12\x0e\n\nTYPE_BYTES\x10\x0c\x12\x0f\n\x0bTYPE_UINT32\x10\r\x12\r\n\tTYPE_ENUM\x10\x0e\x12\x11\n\rTYPE_SFIXED32\x10\x0f\x12\x11\n\rTYPE_SFIXED64\x10\x10\x12\x0f\n\x0bTYPE_SINT32\x10\x11\x12\x0f\n\x0bTYPE_SINT64\x10\x12\"C\n\x05Label\x12\x12\n\x0eLABEL_OPTIONAL\x10\x01\x12\x12\n\x0eLABEL_REQUIRED\x10\x02\x12\x12\n\x0eLABEL_REPEATED\x10\x03\"K\n\x14OneofDescriptorProto\x12\x0c\n\x04name\x18\x01 \x01(\t\x12%\n\x07options\x18\x02 \x01(\x0b\x32\x14.proto2.OneofOptions\"\x89\x02\n\x13\x45numDescriptorProto\x12\x0c\n\x04name\x18\x01 \x01(\t\x12/\n\x05value\x18\x02 \x03(\x0b\x32 .proto2.EnumValueDescriptorProto\x12$\n\x07options\x18\x03 \x01(\x0b\x32\x13.proto2.EnumOptions\x12\x45\n\x0ereserved_range\x18\x04 \x03(\x0b\x32-.proto2.EnumDescriptorProto.EnumReservedRange\x12\x15\n\rreserved_name\x18\x05 \x03(\t\x1a/\n\x11\x45numReservedRange\x12\r\n\x05start\x18\x01 \x01(\x05\x12\x0b\n\x03\x65nd\x18\x02 \x01(\x05\"c\n\x18\x45numValueDescriptorProto\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x0e\n\x06number\x18\x02 \x01(\x05\x12)\n\x07options\x18\x03 \x01(\x0b\x32\x18.proto2.EnumValueOptions\"\xad\x01\n\x16ServiceDescriptorProto\x12\x0c\n\x04name\x18\x01 \x01(\t\x12-\n\x06method\x18\x02 \x03(\x0b\x32\x1d.proto2.MethodDescriptorProto\x12-\n\x06stream\x18\x04 \x03(\x0b\x32\x1d.proto2.StreamDescriptorProto\x12\'\n\x07options\x18\x03 \x01(\x0b\x32\x16.proto2.ServiceOptions\"\xb8\x01\n\x15MethodDescriptorProto\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x12\n\ninput_type\x18\x02 \x01(\t\x12\x13\n\x0boutput_type\x18\x03 \x01(\t\x12&\n\x07options\x18\x04 \x01(\x0b\x32\x15.proto2.MethodOptions\x12\x1f\n\x10\x63lient_streaming\x18\x05 \x01(\x08:\x05\x66\x61lse\x12\x1f\n\x10server_streaming\x18\x06 \x01(\x08:\x05\x66\x61lse\"\x87\x01\n\x15StreamDescriptorProto\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\x1b\n\x13\x63lient_message_type\x18\x02 \x01(\t\x12\x1b\n\x13server_message_type\x18\x03 \x01(\t\x12&\n\x07options\x18\x04 \x01(\x0b\x32\x15.proto2.StreamOptions\"\xd2\t\n\x0b\x46ileOptions\x12\x19\n\x0e\x63\x63_api_version\x18\x02 \x01(\x05:\x01\x32\x12\"\n\x14\x63\x63_utf8_verification\x18\x18 \x01(\x08:\x04true\x12\x14\n\x0cjava_package\x18\x01 \x01(\t\x12\x19\n\x0epy_api_version\x18\x04 \x01(\x05:\x01\x32\x12\x1b\n\x10java_api_version\x18\x05 \x01(\x05:\x01\x32\x12!\n\x13java_use_javaproto2\x18\x06 \x01(\x08:\x04true\x12\x1e\n\x10java_java5_enums\x18\x07 \x01(\x08:\x04true\x12\x1c\n\x14java_alt_api_package\x18\x13 \x01(\t\x12\x34\n%java_enable_dual_generate_mutable_api\x18\x1a \x01(\x08:\x05\x66\x61lse\x12\x1c\n\x14java_outer_classname\x18\x08 \x01(\t\x12\"\n\x13java_multiple_files\x18\n \x01(\x08:\x05\x66\x61lse\x12%\n\x16java_string_check_utf8\x18\x1b \x01(\x08:\x05\x66\x61lse\x12\x1f\n\x10java_mutable_api\x18\x1c \x01(\x08:\x05\x66\x61lse\x12+\n#java_multiple_files_mutable_package\x18\x1d \x01(\t\x12=\n\x0coptimize_for\x18\t \x01(\x0e\x32 .proto2.FileOptions.OptimizeMode:\x05SPEED\x12\x12\n\ngo_package\x18\x0b \x01(\t\x12\x1a\n\x12javascript_package\x18\x0c \x01(\t\x12\x1a\n\x0fszl_api_version\x18\x0e \x01(\x05:\x01\x31\x12\"\n\x13\x63\x63_generic_services\x18\x10 \x01(\x08:\x05\x66\x61lse\x12$\n\x15java_generic_services\x18\x11 \x01(\x08:\x05\x66\x61lse\x12\"\n\x13py_generic_services\x18\x12 \x01(\x08:\x05\x66\x61lse\x12#\n\x14php_generic_services\x18* \x01(\x08:\x05\x66\x61lse\x12\x19\n\ndeprecated\x18\x17 \x01(\x08:\x05\x66\x61lse\x12\x1f\n\x10\x63\x63_enable_arenas\x18\x1f \x01(\x08:\x05\x66\x61lse\x12\x19\n\x11objc_class_prefix\x18$ \x01(\t\x12\x18\n\x10\x63sharp_namespace\x18% \x01(\t\x12\x14\n\x0cswift_prefix\x18\' \x01(\t\x12\x18\n\x10php_class_prefix\x18( \x01(\t\x12\x15\n\rphp_namespace\x18) \x01(\t\x12\x1d\n\x15use_cc_stubby_library\x18+ \x01(\x08\x12\x1e\n\x16php_metadata_namespace\x18, \x01(\t\x12\x14\n\x0cruby_package\x18- \x01(\t\x12:\n\x14uninterpreted_option\x18\xe7\x07 \x03(\x0b\x32\x1b.proto2.UninterpretedOption\":\n\x0cOptimizeMode\x12\t\n\x05SPEED\x10\x01\x12\r\n\tCODE_SIZE\x10\x02\x12\x10\n\x0cLITE_RUNTIME\x10\x03*\t\x08\xe8\x07\x10\x80\x80\x80\x80\x02J\x04\x08\x0f\x10\x10J\x04\x08\x16\x10\x17J\x04\x08\x19\x10\x1aJ\x04\x08\r\x10\x0eJ\x04\x08\x15\x10\x16J\x04\x08 \x10!J\x04\x08!\x10\"J\x04\x08\"\x10#J\x04\x08#\x10$J\x04\x08&\x10\'\"\xfc\x02\n\x0eMessageOptions\x12/\n#experimental_java_message_interface\x18\x04 \x03(\tB\x02\x18\x01\x12/\n#experimental_java_builder_interface\x18\x05 \x03(\tB\x02\x18\x01\x12/\n#experimental_java_interface_extends\x18\x06 \x03(\tB\x02\x18\x01\x12&\n\x17message_set_wire_format\x18\x01 \x01(\x08:\x05\x66\x61lse\x12.\n\x1fno_standard_descriptor_accessor\x18\x02 \x01(\x08:\x05\x66\x61lse\x12\x19\n\ndeprecated\x18\x03 \x01(\x08:\x05\x66\x61lse\x12\x11\n\tmap_entry\x18\x07 \x01(\x08\x12:\n\x14uninterpreted_option\x18\xe7\x07 \x03(\x0b\x32\x1b.proto2.UninterpretedOption*\t\x08\xe8\x07\x10\x80\x80\x80\x80\x02J\x04\x08\x08\x10\tJ\x04\x08\t\x10\n\"\xb3\x04\n\x0c\x46ieldOptions\x12\x31\n\x05\x63type\x18\x01 \x01(\x0e\x32\x1a.proto2.FieldOptions.CType:\x06STRING\x12\x0e\n\x06packed\x18\x02 \x01(\x08\x12\x36\n\x06jstype\x18\x06 \x01(\x0e\x32\x1b.proto2.FieldOptions.JSType:\tJS_NORMAL\x12\x13\n\x04lazy\x18\x05 \x01(\x08:\x05\x66\x61lse\x12\x19\n\ndeprecated\x18\x03 \x01(\x08:\x05\x66\x61lse\x12\x13\n\x04weak\x18\n \x01(\x08:\x05\x66\x61lse\x12<\n\x0fupgraded_option\x18\x0b \x03(\x0b\x32#.proto2.FieldOptions.UpgradedOption\x12%\n\x16\x64\x65precated_raw_message\x18\x0c \x01(\x08:\x05\x66\x61lse\x12\x1a\n\x0c\x65nforce_utf8\x18\r \x01(\x08:\x04true\x12:\n\x14uninterpreted_option\x18\xe7\x07 \x03(\x0b\x32\x1b.proto2.UninterpretedOption\x1a-\n\x0eUpgradedOption\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\r\n\x05value\x18\x02 \x01(\t\"/\n\x05\x43Type\x12\n\n\x06STRING\x10\x00\x12\x08\n\x04\x43ORD\x10\x01\x12\x10\n\x0cSTRING_PIECE\x10\x02\"5\n\x06JSType\x12\r\n\tJS_NORMAL\x10\x00\x12\r\n\tJS_STRING\x10\x01\x12\r\n\tJS_NUMBER\x10\x02*\t\x08\xe8\x07\x10\x80\x80\x80\x80\x02J\x04\x08\x04\x10\x05\"U\n\x0cOneofOptions\x12:\n\x14uninterpreted_option\x18\xe7\x07 \x03(\x0b\x32\x1b.proto2.UninterpretedOption*\t\x08\xe8\x07\x10\x80\x80\x80\x80\x02\"\xa5\x01\n\x0b\x45numOptions\x12\x13\n\x0bproto1_name\x18\x01 \x01(\t\x12\x13\n\x0b\x61llow_alias\x18\x02 \x01(\x08\x12\x19\n\ndeprecated\x18\x03 \x01(\x08:\x05\x66\x61lse\x12:\n\x14uninterpreted_option\x18\xe7\x07 \x03(\x0b\x32\x1b.proto2.UninterpretedOption*\t\x08\xe8\x07\x10\x80\x80\x80\x80\x02J\x04\x08\x04\x10\x05J\x04\x08\x05\x10\x06\"t\n\x10\x45numValueOptions\x12\x19\n\ndeprecated\x18\x01 \x01(\x08:\x05\x66\x61lse\x12:\n\x14uninterpreted_option\x18\xe7\x07 \x03(\x0b\x32\x1b.proto2.UninterpretedOption*\t\x08\xe8\x07\x10\x80\x80\x80\x80\x02\"\xb6\x01\n\x0eServiceOptions\x12\x1d\n\x0emulticast_stub\x18\x14 \x01(\x08:\x05\x66\x61lse\x12#\n\x17\x66\x61ilure_detection_delay\x18\x10 \x01(\x01:\x02-1\x12\x19\n\ndeprecated\x18! \x01(\x08:\x05\x66\x61lse\x12:\n\x14uninterpreted_option\x18\xe7\x07 \x03(\x0b\x32\x1b.proto2.UninterpretedOption*\t\x08\xe8\x07\x10\x80\x80\x80\x80\x02\"\x82\x0c\n\rMethodOptions\x12\x35\n\x08protocol\x18\x07 \x01(\x0e\x32\x1e.proto2.MethodOptions.Protocol:\x03TCP\x12\x14\n\x08\x64\x65\x61\x64line\x18\x08 \x01(\x01:\x02-1\x12$\n\x15\x64uplicate_suppression\x18\t \x01(\x08:\x05\x66\x61lse\x12\x18\n\tfail_fast\x18\n \x01(\x08:\x05\x66\x61lse\x12\'\n\x18\x65nd_user_creds_requested\x18\x1a \x01(\x08:\x05\x66\x61lse\x12\x1b\n\x0e\x63lient_logging\x18\x0b \x01(\x11:\x03\x32\x35\x36\x12\x1b\n\x0eserver_logging\x18\x0c \x01(\x11:\x03\x32\x35\x36\x12\x41\n\x0esecurity_level\x18\r \x01(\x0e\x32#.proto2.MethodOptions.SecurityLevel:\x04NONE\x12\x43\n\x0fresponse_format\x18\x0f \x01(\x0e\x32\x1c.proto2.MethodOptions.Format:\x0cUNCOMPRESSED\x12\x42\n\x0erequest_format\x18\x11 \x01(\x0e\x32\x1c.proto2.MethodOptions.Format:\x0cUNCOMPRESSED\x12\x13\n\x0bstream_type\x18\x12 \x01(\t\x12\x16\n\x0esecurity_label\x18\x13 \x01(\t\x12\x18\n\x10\x63lient_streaming\x18\x14 \x01(\x08\x12\x18\n\x10server_streaming\x18\x15 \x01(\x08\x12\x1a\n\x12legacy_stream_type\x18\x16 \x01(\t\x12\x1a\n\x12legacy_result_type\x18\x17 \x01(\t\x12\x1d\n\x15go_legacy_channel_api\x18\x1d \x01(\x08\x12(\n\x1clegacy_client_initial_tokens\x18\x18 \x01(\x03:\x02-1\x12(\n\x1clegacy_server_initial_tokens\x18\x19 \x01(\x03:\x02-1\x12@\n\x11legacy_token_unit\x18\x1c \x01(\x0e\x32\x1f.proto2.MethodOptions.TokenUnit:\x04\x42YTE\x12^\n\tlog_level\x18\x1b \x01(\x0e\x32\x1e.proto2.MethodOptions.LogLevel:+LOG_HEADER_AND_NON_PRIVATE_PAYLOAD_INTERNAL\x12\x19\n\ndeprecated\x18! \x01(\x08:\x05\x66\x61lse\x12V\n\x11idempotency_level\x18\" \x01(\x0e\x32&.proto2.MethodOptions.IdempotencyLevel:\x13IDEMPOTENCY_UNKNOWN\x12:\n\x14uninterpreted_option\x18\xe7\x07 \x03(\x0b\x32\x1b.proto2.UninterpretedOption\"\x1c\n\x08Protocol\x12\x07\n\x03TCP\x10\x00\x12\x07\n\x03UDP\x10\x01\"e\n\rSecurityLevel\x12\x08\n\x04NONE\x10\x00\x12\r\n\tINTEGRITY\x10\x01\x12\x19\n\x15PRIVACY_AND_INTEGRITY\x10\x02\x12 \n\x1cSTRONG_PRIVACY_AND_INTEGRITY\x10\x03\"0\n\x06\x46ormat\x12\x10\n\x0cUNCOMPRESSED\x10\x00\x12\x14\n\x10ZIPPY_COMPRESSED\x10\x01\"\x9f\x01\n\x08LogLevel\x12\x0c\n\x08LOG_NONE\x10\x00\x12\x13\n\x0fLOG_HEADER_ONLY\x10\x01\x12/\n+LOG_HEADER_AND_NON_PRIVATE_PAYLOAD_INTERNAL\x10\x02\x12#\n\x1fLOG_HEADER_AND_FILTERED_PAYLOAD\x10\x03\x12\x1a\n\x16LOG_HEADER_AND_PAYLOAD\x10\x04\"\"\n\tTokenUnit\x12\x0b\n\x07MESSAGE\x10\x00\x12\x08\n\x04\x42YTE\x10\x01\"P\n\x10IdempotencyLevel\x12\x17\n\x13IDEMPOTENCY_UNKNOWN\x10\x00\x12\x13\n\x0fNO_SIDE_EFFECTS\x10\x01\x12\x0e\n\nIDEMPOTENT\x10\x02*\t\x08\xe8\x07\x10\x80\x80\x80\x80\x02\"\xe7\x04\n\rStreamOptions\x12!\n\x15\x63lient_initial_tokens\x18\x01 \x01(\x03:\x02-1\x12!\n\x15server_initial_tokens\x18\x02 \x01(\x03:\x02-1\x12<\n\ntoken_unit\x18\x03 \x01(\x0e\x32\x1f.proto2.StreamOptions.TokenUnit:\x07MESSAGE\x12\x41\n\x0esecurity_level\x18\x04 \x01(\x0e\x32#.proto2.MethodOptions.SecurityLevel:\x04NONE\x12\x16\n\x0esecurity_label\x18\x05 \x01(\t\x12\x1b\n\x0e\x63lient_logging\x18\x06 \x01(\x05:\x03\x32\x35\x36\x12\x1b\n\x0eserver_logging\x18\x07 \x01(\x05:\x03\x32\x35\x36\x12\x14\n\x08\x64\x65\x61\x64line\x18\x08 \x01(\x01:\x02-1\x12\x18\n\tfail_fast\x18\t \x01(\x08:\x05\x66\x61lse\x12\'\n\x18\x65nd_user_creds_requested\x18\n \x01(\x08:\x05\x66\x61lse\x12^\n\tlog_level\x18\x0b \x01(\x0e\x32\x1e.proto2.MethodOptions.LogLevel:+LOG_HEADER_AND_NON_PRIVATE_PAYLOAD_INTERNAL\x12\x19\n\ndeprecated\x18! \x01(\x08:\x05\x66\x61lse\x12:\n\x14uninterpreted_option\x18\xe7\x07 \x03(\x0b\x32\x1b.proto2.UninterpretedOption\"\"\n\tTokenUnit\x12\x0b\n\x07MESSAGE\x10\x00\x12\x08\n\x04\x42YTE\x10\x01*\t\x08\xe8\x07\x10\x80\x80\x80\x80\x02\"\x95\x02\n\x13UninterpretedOption\x12\x32\n\x04name\x18\x02 \x03(\x0b\x32$.proto2.UninterpretedOption.NamePart\x12\x18\n\x10identifier_value\x18\x03 \x01(\t\x12\x1a\n\x12positive_int_value\x18\x04 \x01(\x04\x12\x1a\n\x12negative_int_value\x18\x05 \x01(\x03\x12\x14\n\x0c\x64ouble_value\x18\x06 \x01(\x01\x12\x14\n\x0cstring_value\x18\x07 \x01(\x0c\x12\x17\n\x0f\x61ggregate_value\x18\x08 \x01(\t\x1a\x33\n\x08NamePart\x12\x11\n\tname_part\x18\x01 \x02(\t\x12\x14\n\x0cis_extension\x18\x02 \x02(\x08\"\xcc\x01\n\x0eSourceCodeInfo\x12\x31\n\x08location\x18\x01 \x03(\x0b\x32\x1f.proto2.SourceCodeInfo.Location\x1a\x86\x01\n\x08Location\x12\x10\n\x04path\x18\x01 \x03(\x05\x42\x02\x10\x01\x12\x10\n\x04span\x18\x02 \x03(\x05\x42\x02\x10\x01\x12\x18\n\x10leading_comments\x18\x03 \x01(\t\x12\x19\n\x11trailing_comments\x18\x04 \x01(\t\x12!\n\x19leading_detached_comments\x18\x06 \x03(\t\"\x9e\x01\n\x11GeneratedCodeInfo\x12\x38\n\nannotation\x18\x01 \x03(\x0b\x32$.proto2.GeneratedCodeInfo.Annotation\x1aO\n\nAnnotation\x12\x10\n\x04path\x18\x01 \x03(\x05\x42\x02\x10\x01\x12\x13\n\x0bsource_file\x18\x02 \x01(\t\x12\r\n\x05\x62\x65gin\x18\x03 \x01(\x05\x12\x0b\n\x03\x65nd\x18\x04 \x01(\x05\x42\x92\x01\n\x13\x63om.google.protobufB\x10\x44\x65scriptorProtosH\x01Z>github.com/golang/protobuf/protoc-gen-go/descriptor;descriptor\xe0\x01\x01\xf8\x01\x01\xa2\x02\x03GPB\xaa\x02\x1aGoogle.Protobuf.Reflection')
)



_FIELDDESCRIPTORPROTO_TYPE = _descriptor.EnumDescriptor(
  name='Type',
  full_name='proto2.FieldDescriptorProto.Type',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='TYPE_DOUBLE', index=0, number=1,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='TYPE_FLOAT', index=1, number=2,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='TYPE_INT64', index=2, number=3,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='TYPE_UINT64', index=3, number=4,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='TYPE_INT32', index=4, number=5,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='TYPE_FIXED64', index=5, number=6,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='TYPE_FIXED32', index=6, number=7,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='TYPE_BOOL', index=7, number=8,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='TYPE_STRING', index=8, number=9,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='TYPE_GROUP', index=9, number=10,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='TYPE_MESSAGE', index=10, number=11,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='TYPE_BYTES', index=11, number=12,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='TYPE_UINT32', index=12, number=13,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='TYPE_ENUM', index=13, number=14,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='TYPE_SFIXED32', index=14, number=15,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='TYPE_SFIXED64', index=15, number=16,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='TYPE_SINT32', index=16, number=17,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='TYPE_SINT64', index=17, number=18,
      serialized_options=None,
      type=None),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=1527,
  serialized_end=1837,
)
_sym_db.RegisterEnumDescriptor(_FIELDDESCRIPTORPROTO_TYPE)

_FIELDDESCRIPTORPROTO_LABEL = _descriptor.EnumDescriptor(
  name='Label',
  full_name='proto2.FieldDescriptorProto.Label',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='LABEL_OPTIONAL', index=0, number=1,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='LABEL_REQUIRED', index=1, number=2,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='LABEL_REPEATED', index=2, number=3,
      serialized_options=None,
      type=None),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=1839,
  serialized_end=1906,
)
_sym_db.RegisterEnumDescriptor(_FIELDDESCRIPTORPROTO_LABEL)

_FILEOPTIONS_OPTIMIZEMODE = _descriptor.EnumDescriptor(
  name='OptimizeMode',
  full_name='proto2.FileOptions.OptimizeMode',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='SPEED', index=0, number=1,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='CODE_SIZE', index=1, number=2,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='LITE_RUNTIME', index=2, number=3,
      serialized_options=None,
      type=None),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=3961,
  serialized_end=4019,
)
_sym_db.RegisterEnumDescriptor(_FILEOPTIONS_OPTIMIZEMODE)

_FIELDOPTIONS_CTYPE = _descriptor.EnumDescriptor(
  name='CType',
  full_name='proto2.FieldOptions.CType',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='STRING', index=0, number=0,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='CORD', index=1, number=1,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='STRING_PIECE', index=2, number=2,
      serialized_options=None,
      type=None),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=4920,
  serialized_end=4967,
)
_sym_db.RegisterEnumDescriptor(_FIELDOPTIONS_CTYPE)

_FIELDOPTIONS_JSTYPE = _descriptor.EnumDescriptor(
  name='JSType',
  full_name='proto2.FieldOptions.JSType',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='JS_NORMAL', index=0, number=0,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='JS_STRING', index=1, number=1,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='JS_NUMBER', index=2, number=2,
      serialized_options=None,
      type=None),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=4969,
  serialized_end=5022,
)
_sym_db.RegisterEnumDescriptor(_FIELDOPTIONS_JSTYPE)

_METHODOPTIONS_PROTOCOL = _descriptor.EnumDescriptor(
  name='Protocol',
  full_name='proto2.MethodOptions.Protocol',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='TCP', index=0, number=0,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='UDP', index=1, number=1,
      serialized_options=None,
      type=None),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=6666,
  serialized_end=6694,
)
_sym_db.RegisterEnumDescriptor(_METHODOPTIONS_PROTOCOL)

_METHODOPTIONS_SECURITYLEVEL = _descriptor.EnumDescriptor(
  name='SecurityLevel',
  full_name='proto2.MethodOptions.SecurityLevel',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='NONE', index=0, number=0,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='INTEGRITY', index=1, number=1,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='PRIVACY_AND_INTEGRITY', index=2, number=2,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='STRONG_PRIVACY_AND_INTEGRITY', index=3, number=3,
      serialized_options=None,
      type=None),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=6696,
  serialized_end=6797,
)
_sym_db.RegisterEnumDescriptor(_METHODOPTIONS_SECURITYLEVEL)

_METHODOPTIONS_FORMAT = _descriptor.EnumDescriptor(
  name='Format',
  full_name='proto2.MethodOptions.Format',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='UNCOMPRESSED', index=0, number=0,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='ZIPPY_COMPRESSED', index=1, number=1,
      serialized_options=None,
      type=None),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=6799,
  serialized_end=6847,
)
_sym_db.RegisterEnumDescriptor(_METHODOPTIONS_FORMAT)

_METHODOPTIONS_LOGLEVEL = _descriptor.EnumDescriptor(
  name='LogLevel',
  full_name='proto2.MethodOptions.LogLevel',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='LOG_NONE', index=0, number=0,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='LOG_HEADER_ONLY', index=1, number=1,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='LOG_HEADER_AND_NON_PRIVATE_PAYLOAD_INTERNAL', index=2, number=2,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='LOG_HEADER_AND_FILTERED_PAYLOAD', index=3, number=3,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='LOG_HEADER_AND_PAYLOAD', index=4, number=4,
      serialized_options=None,
      type=None),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=6850,
  serialized_end=7009,
)
_sym_db.RegisterEnumDescriptor(_METHODOPTIONS_LOGLEVEL)

_METHODOPTIONS_TOKENUNIT = _descriptor.EnumDescriptor(
  name='TokenUnit',
  full_name='proto2.MethodOptions.TokenUnit',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='MESSAGE', index=0, number=0,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='BYTE', index=1, number=1,
      serialized_options=None,
      type=None),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=7011,
  serialized_end=7045,
)
_sym_db.RegisterEnumDescriptor(_METHODOPTIONS_TOKENUNIT)

_METHODOPTIONS_IDEMPOTENCYLEVEL = _descriptor.EnumDescriptor(
  name='IdempotencyLevel',
  full_name='proto2.MethodOptions.IdempotencyLevel',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='IDEMPOTENCY_UNKNOWN', index=0, number=0,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='NO_SIDE_EFFECTS', index=1, number=1,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='IDEMPOTENT', index=2, number=2,
      serialized_options=None,
      type=None),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=7047,
  serialized_end=7127,
)
_sym_db.RegisterEnumDescriptor(_METHODOPTIONS_IDEMPOTENCYLEVEL)

_STREAMOPTIONS_TOKENUNIT = _descriptor.EnumDescriptor(
  name='TokenUnit',
  full_name='proto2.StreamOptions.TokenUnit',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='MESSAGE', index=0, number=0,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='BYTE', index=1, number=1,
      serialized_options=None,
      type=None),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=7011,
  serialized_end=7045,
)
_sym_db.RegisterEnumDescriptor(_STREAMOPTIONS_TOKENUNIT)


_FILEDESCRIPTORSET = _descriptor.Descriptor(
  name='FileDescriptorSet',
  full_name='proto2.FileDescriptorSet',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='file', full_name='proto2.FileDescriptorSet.file', index=0,
      number=1, type=11, cpp_type=10, label=3,
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
  serialized_start=45,
  serialized_end=107,
)


_FILEDESCRIPTORPROTO = _descriptor.Descriptor(
  name='FileDescriptorProto',
  full_name='proto2.FileDescriptorProto',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='proto2.FileDescriptorProto.name', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='package', full_name='proto2.FileDescriptorProto.package', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='dependency', full_name='proto2.FileDescriptorProto.dependency', index=2,
      number=3, type=9, cpp_type=9, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='public_dependency', full_name='proto2.FileDescriptorProto.public_dependency', index=3,
      number=10, type=5, cpp_type=1, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='weak_dependency', full_name='proto2.FileDescriptorProto.weak_dependency', index=4,
      number=11, type=5, cpp_type=1, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='message_type', full_name='proto2.FileDescriptorProto.message_type', index=5,
      number=4, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='enum_type', full_name='proto2.FileDescriptorProto.enum_type', index=6,
      number=5, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='service', full_name='proto2.FileDescriptorProto.service', index=7,
      number=6, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='extension', full_name='proto2.FileDescriptorProto.extension', index=8,
      number=7, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='options', full_name='proto2.FileDescriptorProto.options', index=9,
      number=8, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='source_code_info', full_name='proto2.FileDescriptorProto.source_code_info', index=10,
      number=9, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='syntax', full_name='proto2.FileDescriptorProto.syntax', index=11,
      number=12, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
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
  serialized_start=110,
  serialized_end=531,
)


_DESCRIPTORPROTO_EXTENSIONRANGE = _descriptor.Descriptor(
  name='ExtensionRange',
  full_name='proto2.DescriptorProto.ExtensionRange',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='start', full_name='proto2.DescriptorProto.ExtensionRange.start', index=0,
      number=1, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='end', full_name='proto2.DescriptorProto.ExtensionRange.end', index=1,
      number=2, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='options', full_name='proto2.DescriptorProto.ExtensionRange.options', index=2,
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
  serialized_start=997,
  serialized_end=1089,
)

_DESCRIPTORPROTO_RESERVEDRANGE = _descriptor.Descriptor(
  name='ReservedRange',
  full_name='proto2.DescriptorProto.ReservedRange',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='start', full_name='proto2.DescriptorProto.ReservedRange.start', index=0,
      number=1, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='end', full_name='proto2.DescriptorProto.ReservedRange.end', index=1,
      number=2, type=5, cpp_type=1, label=1,
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
  serialized_start=1091,
  serialized_end=1134,
)

_DESCRIPTORPROTO = _descriptor.Descriptor(
  name='DescriptorProto',
  full_name='proto2.DescriptorProto',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='proto2.DescriptorProto.name', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='field', full_name='proto2.DescriptorProto.field', index=1,
      number=2, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='extension', full_name='proto2.DescriptorProto.extension', index=2,
      number=6, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='nested_type', full_name='proto2.DescriptorProto.nested_type', index=3,
      number=3, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='enum_type', full_name='proto2.DescriptorProto.enum_type', index=4,
      number=4, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='extension_range', full_name='proto2.DescriptorProto.extension_range', index=5,
      number=5, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='oneof_decl', full_name='proto2.DescriptorProto.oneof_decl', index=6,
      number=8, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='options', full_name='proto2.DescriptorProto.options', index=7,
      number=7, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='reserved_range', full_name='proto2.DescriptorProto.reserved_range', index=8,
      number=9, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='reserved_name', full_name='proto2.DescriptorProto.reserved_name', index=9,
      number=10, type=9, cpp_type=9, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[_DESCRIPTORPROTO_EXTENSIONRANGE, _DESCRIPTORPROTO_RESERVEDRANGE, ],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=534,
  serialized_end=1134,
)


_EXTENSIONRANGEOPTIONS = _descriptor.Descriptor(
  name='ExtensionRangeOptions',
  full_name='proto2.ExtensionRangeOptions',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='uninterpreted_option', full_name='proto2.ExtensionRangeOptions.uninterpreted_option', index=0,
      number=999, type=11, cpp_type=10, label=3,
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
  is_extendable=True,
  syntax='proto2',
  extension_ranges=[(1000, 536870912), ],
  oneofs=[
  ],
  serialized_start=1136,
  serialized_end=1230,
)


_FIELDDESCRIPTORPROTO = _descriptor.Descriptor(
  name='FieldDescriptorProto',
  full_name='proto2.FieldDescriptorProto',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='proto2.FieldDescriptorProto.name', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='number', full_name='proto2.FieldDescriptorProto.number', index=1,
      number=3, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='label', full_name='proto2.FieldDescriptorProto.label', index=2,
      number=4, type=14, cpp_type=8, label=1,
      has_default_value=False, default_value=1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='type', full_name='proto2.FieldDescriptorProto.type', index=3,
      number=5, type=14, cpp_type=8, label=1,
      has_default_value=False, default_value=1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='type_name', full_name='proto2.FieldDescriptorProto.type_name', index=4,
      number=6, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='extendee', full_name='proto2.FieldDescriptorProto.extendee', index=5,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='default_value', full_name='proto2.FieldDescriptorProto.default_value', index=6,
      number=7, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='oneof_index', full_name='proto2.FieldDescriptorProto.oneof_index', index=7,
      number=9, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='json_name', full_name='proto2.FieldDescriptorProto.json_name', index=8,
      number=10, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='options', full_name='proto2.FieldDescriptorProto.options', index=9,
      number=8, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
    _FIELDDESCRIPTORPROTO_TYPE,
    _FIELDDESCRIPTORPROTO_LABEL,
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=1233,
  serialized_end=1906,
)


_ONEOFDESCRIPTORPROTO = _descriptor.Descriptor(
  name='OneofDescriptorProto',
  full_name='proto2.OneofDescriptorProto',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='proto2.OneofDescriptorProto.name', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='options', full_name='proto2.OneofDescriptorProto.options', index=1,
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
  serialized_start=1908,
  serialized_end=1983,
)


_ENUMDESCRIPTORPROTO_ENUMRESERVEDRANGE = _descriptor.Descriptor(
  name='EnumReservedRange',
  full_name='proto2.EnumDescriptorProto.EnumReservedRange',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='start', full_name='proto2.EnumDescriptorProto.EnumReservedRange.start', index=0,
      number=1, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='end', full_name='proto2.EnumDescriptorProto.EnumReservedRange.end', index=1,
      number=2, type=5, cpp_type=1, label=1,
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
  serialized_start=2204,
  serialized_end=2251,
)

_ENUMDESCRIPTORPROTO = _descriptor.Descriptor(
  name='EnumDescriptorProto',
  full_name='proto2.EnumDescriptorProto',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='proto2.EnumDescriptorProto.name', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='value', full_name='proto2.EnumDescriptorProto.value', index=1,
      number=2, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='options', full_name='proto2.EnumDescriptorProto.options', index=2,
      number=3, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='reserved_range', full_name='proto2.EnumDescriptorProto.reserved_range', index=3,
      number=4, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='reserved_name', full_name='proto2.EnumDescriptorProto.reserved_name', index=4,
      number=5, type=9, cpp_type=9, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[_ENUMDESCRIPTORPROTO_ENUMRESERVEDRANGE, ],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=1986,
  serialized_end=2251,
)


_ENUMVALUEDESCRIPTORPROTO = _descriptor.Descriptor(
  name='EnumValueDescriptorProto',
  full_name='proto2.EnumValueDescriptorProto',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='proto2.EnumValueDescriptorProto.name', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='number', full_name='proto2.EnumValueDescriptorProto.number', index=1,
      number=2, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='options', full_name='proto2.EnumValueDescriptorProto.options', index=2,
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
  serialized_start=2253,
  serialized_end=2352,
)


_SERVICEDESCRIPTORPROTO = _descriptor.Descriptor(
  name='ServiceDescriptorProto',
  full_name='proto2.ServiceDescriptorProto',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='proto2.ServiceDescriptorProto.name', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='method', full_name='proto2.ServiceDescriptorProto.method', index=1,
      number=2, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='stream', full_name='proto2.ServiceDescriptorProto.stream', index=2,
      number=4, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='options', full_name='proto2.ServiceDescriptorProto.options', index=3,
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
  serialized_start=2355,
  serialized_end=2528,
)


_METHODDESCRIPTORPROTO = _descriptor.Descriptor(
  name='MethodDescriptorProto',
  full_name='proto2.MethodDescriptorProto',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='proto2.MethodDescriptorProto.name', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='input_type', full_name='proto2.MethodDescriptorProto.input_type', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='output_type', full_name='proto2.MethodDescriptorProto.output_type', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='options', full_name='proto2.MethodDescriptorProto.options', index=3,
      number=4, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='client_streaming', full_name='proto2.MethodDescriptorProto.client_streaming', index=4,
      number=5, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='server_streaming', full_name='proto2.MethodDescriptorProto.server_streaming', index=5,
      number=6, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
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
  serialized_start=2531,
  serialized_end=2715,
)


_STREAMDESCRIPTORPROTO = _descriptor.Descriptor(
  name='StreamDescriptorProto',
  full_name='proto2.StreamDescriptorProto',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='proto2.StreamDescriptorProto.name', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='client_message_type', full_name='proto2.StreamDescriptorProto.client_message_type', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='server_message_type', full_name='proto2.StreamDescriptorProto.server_message_type', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='options', full_name='proto2.StreamDescriptorProto.options', index=3,
      number=4, type=11, cpp_type=10, label=1,
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
  serialized_start=2718,
  serialized_end=2853,
)


_FILEOPTIONS = _descriptor.Descriptor(
  name='FileOptions',
  full_name='proto2.FileOptions',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='cc_api_version', full_name='proto2.FileOptions.cc_api_version', index=0,
      number=2, type=5, cpp_type=1, label=1,
      has_default_value=True, default_value=2,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='cc_utf8_verification', full_name='proto2.FileOptions.cc_utf8_verification', index=1,
      number=24, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=True,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='java_package', full_name='proto2.FileOptions.java_package', index=2,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='py_api_version', full_name='proto2.FileOptions.py_api_version', index=3,
      number=4, type=5, cpp_type=1, label=1,
      has_default_value=True, default_value=2,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='java_api_version', full_name='proto2.FileOptions.java_api_version', index=4,
      number=5, type=5, cpp_type=1, label=1,
      has_default_value=True, default_value=2,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='java_use_javaproto2', full_name='proto2.FileOptions.java_use_javaproto2', index=5,
      number=6, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=True,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='java_java5_enums', full_name='proto2.FileOptions.java_java5_enums', index=6,
      number=7, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=True,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='java_alt_api_package', full_name='proto2.FileOptions.java_alt_api_package', index=7,
      number=19, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='java_enable_dual_generate_mutable_api', full_name='proto2.FileOptions.java_enable_dual_generate_mutable_api', index=8,
      number=26, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='java_outer_classname', full_name='proto2.FileOptions.java_outer_classname', index=9,
      number=8, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='java_multiple_files', full_name='proto2.FileOptions.java_multiple_files', index=10,
      number=10, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='java_string_check_utf8', full_name='proto2.FileOptions.java_string_check_utf8', index=11,
      number=27, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='java_mutable_api', full_name='proto2.FileOptions.java_mutable_api', index=12,
      number=28, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='java_multiple_files_mutable_package', full_name='proto2.FileOptions.java_multiple_files_mutable_package', index=13,
      number=29, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='optimize_for', full_name='proto2.FileOptions.optimize_for', index=14,
      number=9, type=14, cpp_type=8, label=1,
      has_default_value=True, default_value=1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='go_package', full_name='proto2.FileOptions.go_package', index=15,
      number=11, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='javascript_package', full_name='proto2.FileOptions.javascript_package', index=16,
      number=12, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='szl_api_version', full_name='proto2.FileOptions.szl_api_version', index=17,
      number=14, type=5, cpp_type=1, label=1,
      has_default_value=True, default_value=1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='cc_generic_services', full_name='proto2.FileOptions.cc_generic_services', index=18,
      number=16, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='java_generic_services', full_name='proto2.FileOptions.java_generic_services', index=19,
      number=17, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='py_generic_services', full_name='proto2.FileOptions.py_generic_services', index=20,
      number=18, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='php_generic_services', full_name='proto2.FileOptions.php_generic_services', index=21,
      number=42, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='deprecated', full_name='proto2.FileOptions.deprecated', index=22,
      number=23, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='cc_enable_arenas', full_name='proto2.FileOptions.cc_enable_arenas', index=23,
      number=31, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='objc_class_prefix', full_name='proto2.FileOptions.objc_class_prefix', index=24,
      number=36, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='csharp_namespace', full_name='proto2.FileOptions.csharp_namespace', index=25,
      number=37, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='swift_prefix', full_name='proto2.FileOptions.swift_prefix', index=26,
      number=39, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='php_class_prefix', full_name='proto2.FileOptions.php_class_prefix', index=27,
      number=40, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='php_namespace', full_name='proto2.FileOptions.php_namespace', index=28,
      number=41, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='use_cc_stubby_library', full_name='proto2.FileOptions.use_cc_stubby_library', index=29,
      number=43, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='php_metadata_namespace', full_name='proto2.FileOptions.php_metadata_namespace', index=30,
      number=44, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='ruby_package', full_name='proto2.FileOptions.ruby_package', index=31,
      number=45, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='uninterpreted_option', full_name='proto2.FileOptions.uninterpreted_option', index=32,
      number=999, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
    _FILEOPTIONS_OPTIMIZEMODE,
  ],
  serialized_options=None,
  is_extendable=True,
  syntax='proto2',
  extension_ranges=[(1000, 536870912), ],
  oneofs=[
  ],
  serialized_start=2856,
  serialized_end=4090,
)


_MESSAGEOPTIONS = _descriptor.Descriptor(
  name='MessageOptions',
  full_name='proto2.MessageOptions',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='experimental_java_message_interface', full_name='proto2.MessageOptions.experimental_java_message_interface', index=0,
      number=4, type=9, cpp_type=9, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='experimental_java_builder_interface', full_name='proto2.MessageOptions.experimental_java_builder_interface', index=1,
      number=5, type=9, cpp_type=9, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='experimental_java_interface_extends', full_name='proto2.MessageOptions.experimental_java_interface_extends', index=2,
      number=6, type=9, cpp_type=9, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='message_set_wire_format', full_name='proto2.MessageOptions.message_set_wire_format', index=3,
      number=1, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='no_standard_descriptor_accessor', full_name='proto2.MessageOptions.no_standard_descriptor_accessor', index=4,
      number=2, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='deprecated', full_name='proto2.MessageOptions.deprecated', index=5,
      number=3, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='map_entry', full_name='proto2.MessageOptions.map_entry', index=6,
      number=7, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='uninterpreted_option', full_name='proto2.MessageOptions.uninterpreted_option', index=7,
      number=999, type=11, cpp_type=10, label=3,
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
  is_extendable=True,
  syntax='proto2',
  extension_ranges=[(1000, 536870912), ],
  oneofs=[
  ],
  serialized_start=4093,
  serialized_end=4473,
)


_FIELDOPTIONS_UPGRADEDOPTION = _descriptor.Descriptor(
  name='UpgradedOption',
  full_name='proto2.FieldOptions.UpgradedOption',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='proto2.FieldOptions.UpgradedOption.name', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='value', full_name='proto2.FieldOptions.UpgradedOption.value', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
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
  serialized_start=4873,
  serialized_end=4918,
)

_FIELDOPTIONS = _descriptor.Descriptor(
  name='FieldOptions',
  full_name='proto2.FieldOptions',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='ctype', full_name='proto2.FieldOptions.ctype', index=0,
      number=1, type=14, cpp_type=8, label=1,
      has_default_value=True, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='packed', full_name='proto2.FieldOptions.packed', index=1,
      number=2, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='jstype', full_name='proto2.FieldOptions.jstype', index=2,
      number=6, type=14, cpp_type=8, label=1,
      has_default_value=True, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='lazy', full_name='proto2.FieldOptions.lazy', index=3,
      number=5, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='deprecated', full_name='proto2.FieldOptions.deprecated', index=4,
      number=3, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='weak', full_name='proto2.FieldOptions.weak', index=5,
      number=10, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='upgraded_option', full_name='proto2.FieldOptions.upgraded_option', index=6,
      number=11, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='deprecated_raw_message', full_name='proto2.FieldOptions.deprecated_raw_message', index=7,
      number=12, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='enforce_utf8', full_name='proto2.FieldOptions.enforce_utf8', index=8,
      number=13, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=True,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='uninterpreted_option', full_name='proto2.FieldOptions.uninterpreted_option', index=9,
      number=999, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[_FIELDOPTIONS_UPGRADEDOPTION, ],
  enum_types=[
    _FIELDOPTIONS_CTYPE,
    _FIELDOPTIONS_JSTYPE,
  ],
  serialized_options=None,
  is_extendable=True,
  syntax='proto2',
  extension_ranges=[(1000, 536870912), ],
  oneofs=[
  ],
  serialized_start=4476,
  serialized_end=5039,
)


_ONEOFOPTIONS = _descriptor.Descriptor(
  name='OneofOptions',
  full_name='proto2.OneofOptions',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='uninterpreted_option', full_name='proto2.OneofOptions.uninterpreted_option', index=0,
      number=999, type=11, cpp_type=10, label=3,
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
  is_extendable=True,
  syntax='proto2',
  extension_ranges=[(1000, 536870912), ],
  oneofs=[
  ],
  serialized_start=5041,
  serialized_end=5126,
)


_ENUMOPTIONS = _descriptor.Descriptor(
  name='EnumOptions',
  full_name='proto2.EnumOptions',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='proto1_name', full_name='proto2.EnumOptions.proto1_name', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='allow_alias', full_name='proto2.EnumOptions.allow_alias', index=1,
      number=2, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='deprecated', full_name='proto2.EnumOptions.deprecated', index=2,
      number=3, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='uninterpreted_option', full_name='proto2.EnumOptions.uninterpreted_option', index=3,
      number=999, type=11, cpp_type=10, label=3,
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
  is_extendable=True,
  syntax='proto2',
  extension_ranges=[(1000, 536870912), ],
  oneofs=[
  ],
  serialized_start=5129,
  serialized_end=5294,
)


_ENUMVALUEOPTIONS = _descriptor.Descriptor(
  name='EnumValueOptions',
  full_name='proto2.EnumValueOptions',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='deprecated', full_name='proto2.EnumValueOptions.deprecated', index=0,
      number=1, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='uninterpreted_option', full_name='proto2.EnumValueOptions.uninterpreted_option', index=1,
      number=999, type=11, cpp_type=10, label=3,
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
  is_extendable=True,
  syntax='proto2',
  extension_ranges=[(1000, 536870912), ],
  oneofs=[
  ],
  serialized_start=5296,
  serialized_end=5412,
)


_SERVICEOPTIONS = _descriptor.Descriptor(
  name='ServiceOptions',
  full_name='proto2.ServiceOptions',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='multicast_stub', full_name='proto2.ServiceOptions.multicast_stub', index=0,
      number=20, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='failure_detection_delay', full_name='proto2.ServiceOptions.failure_detection_delay', index=1,
      number=16, type=1, cpp_type=5, label=1,
      has_default_value=True, default_value=float(-1),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='deprecated', full_name='proto2.ServiceOptions.deprecated', index=2,
      number=33, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='uninterpreted_option', full_name='proto2.ServiceOptions.uninterpreted_option', index=3,
      number=999, type=11, cpp_type=10, label=3,
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
  is_extendable=True,
  syntax='proto2',
  extension_ranges=[(1000, 536870912), ],
  oneofs=[
  ],
  serialized_start=5415,
  serialized_end=5597,
)


_METHODOPTIONS = _descriptor.Descriptor(
  name='MethodOptions',
  full_name='proto2.MethodOptions',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='protocol', full_name='proto2.MethodOptions.protocol', index=0,
      number=7, type=14, cpp_type=8, label=1,
      has_default_value=True, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='deadline', full_name='proto2.MethodOptions.deadline', index=1,
      number=8, type=1, cpp_type=5, label=1,
      has_default_value=True, default_value=float(-1),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='duplicate_suppression', full_name='proto2.MethodOptions.duplicate_suppression', index=2,
      number=9, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='fail_fast', full_name='proto2.MethodOptions.fail_fast', index=3,
      number=10, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='end_user_creds_requested', full_name='proto2.MethodOptions.end_user_creds_requested', index=4,
      number=26, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='client_logging', full_name='proto2.MethodOptions.client_logging', index=5,
      number=11, type=17, cpp_type=1, label=1,
      has_default_value=True, default_value=256,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='server_logging', full_name='proto2.MethodOptions.server_logging', index=6,
      number=12, type=17, cpp_type=1, label=1,
      has_default_value=True, default_value=256,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='security_level', full_name='proto2.MethodOptions.security_level', index=7,
      number=13, type=14, cpp_type=8, label=1,
      has_default_value=True, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='response_format', full_name='proto2.MethodOptions.response_format', index=8,
      number=15, type=14, cpp_type=8, label=1,
      has_default_value=True, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='request_format', full_name='proto2.MethodOptions.request_format', index=9,
      number=17, type=14, cpp_type=8, label=1,
      has_default_value=True, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='stream_type', full_name='proto2.MethodOptions.stream_type', index=10,
      number=18, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='security_label', full_name='proto2.MethodOptions.security_label', index=11,
      number=19, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='client_streaming', full_name='proto2.MethodOptions.client_streaming', index=12,
      number=20, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='server_streaming', full_name='proto2.MethodOptions.server_streaming', index=13,
      number=21, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='legacy_stream_type', full_name='proto2.MethodOptions.legacy_stream_type', index=14,
      number=22, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='legacy_result_type', full_name='proto2.MethodOptions.legacy_result_type', index=15,
      number=23, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='go_legacy_channel_api', full_name='proto2.MethodOptions.go_legacy_channel_api', index=16,
      number=29, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='legacy_client_initial_tokens', full_name='proto2.MethodOptions.legacy_client_initial_tokens', index=17,
      number=24, type=3, cpp_type=2, label=1,
      has_default_value=True, default_value=-1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='legacy_server_initial_tokens', full_name='proto2.MethodOptions.legacy_server_initial_tokens', index=18,
      number=25, type=3, cpp_type=2, label=1,
      has_default_value=True, default_value=-1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='legacy_token_unit', full_name='proto2.MethodOptions.legacy_token_unit', index=19,
      number=28, type=14, cpp_type=8, label=1,
      has_default_value=True, default_value=1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='log_level', full_name='proto2.MethodOptions.log_level', index=20,
      number=27, type=14, cpp_type=8, label=1,
      has_default_value=True, default_value=2,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='deprecated', full_name='proto2.MethodOptions.deprecated', index=21,
      number=33, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='idempotency_level', full_name='proto2.MethodOptions.idempotency_level', index=22,
      number=34, type=14, cpp_type=8, label=1,
      has_default_value=True, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='uninterpreted_option', full_name='proto2.MethodOptions.uninterpreted_option', index=23,
      number=999, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
    _METHODOPTIONS_PROTOCOL,
    _METHODOPTIONS_SECURITYLEVEL,
    _METHODOPTIONS_FORMAT,
    _METHODOPTIONS_LOGLEVEL,
    _METHODOPTIONS_TOKENUNIT,
    _METHODOPTIONS_IDEMPOTENCYLEVEL,
  ],
  serialized_options=None,
  is_extendable=True,
  syntax='proto2',
  extension_ranges=[(1000, 536870912), ],
  oneofs=[
  ],
  serialized_start=5600,
  serialized_end=7138,
)


_STREAMOPTIONS = _descriptor.Descriptor(
  name='StreamOptions',
  full_name='proto2.StreamOptions',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='client_initial_tokens', full_name='proto2.StreamOptions.client_initial_tokens', index=0,
      number=1, type=3, cpp_type=2, label=1,
      has_default_value=True, default_value=-1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='server_initial_tokens', full_name='proto2.StreamOptions.server_initial_tokens', index=1,
      number=2, type=3, cpp_type=2, label=1,
      has_default_value=True, default_value=-1,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='token_unit', full_name='proto2.StreamOptions.token_unit', index=2,
      number=3, type=14, cpp_type=8, label=1,
      has_default_value=True, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='security_level', full_name='proto2.StreamOptions.security_level', index=3,
      number=4, type=14, cpp_type=8, label=1,
      has_default_value=True, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='security_label', full_name='proto2.StreamOptions.security_label', index=4,
      number=5, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='client_logging', full_name='proto2.StreamOptions.client_logging', index=5,
      number=6, type=5, cpp_type=1, label=1,
      has_default_value=True, default_value=256,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='server_logging', full_name='proto2.StreamOptions.server_logging', index=6,
      number=7, type=5, cpp_type=1, label=1,
      has_default_value=True, default_value=256,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='deadline', full_name='proto2.StreamOptions.deadline', index=7,
      number=8, type=1, cpp_type=5, label=1,
      has_default_value=True, default_value=float(-1),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='fail_fast', full_name='proto2.StreamOptions.fail_fast', index=8,
      number=9, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='end_user_creds_requested', full_name='proto2.StreamOptions.end_user_creds_requested', index=9,
      number=10, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='log_level', full_name='proto2.StreamOptions.log_level', index=10,
      number=11, type=14, cpp_type=8, label=1,
      has_default_value=True, default_value=2,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='deprecated', full_name='proto2.StreamOptions.deprecated', index=11,
      number=33, type=8, cpp_type=7, label=1,
      has_default_value=True, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='uninterpreted_option', full_name='proto2.StreamOptions.uninterpreted_option', index=12,
      number=999, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
    _STREAMOPTIONS_TOKENUNIT,
  ],
  serialized_options=None,
  is_extendable=True,
  syntax='proto2',
  extension_ranges=[(1000, 536870912), ],
  oneofs=[
  ],
  serialized_start=7141,
  serialized_end=7756,
)


_UNINTERPRETEDOPTION_NAMEPART = _descriptor.Descriptor(
  name='NamePart',
  full_name='proto2.UninterpretedOption.NamePart',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name_part', full_name='proto2.UninterpretedOption.NamePart.name_part', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='is_extension', full_name='proto2.UninterpretedOption.NamePart.is_extension', index=1,
      number=2, type=8, cpp_type=7, label=2,
      has_default_value=False, default_value=False,
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
  serialized_start=7985,
  serialized_end=8036,
)

_UNINTERPRETEDOPTION = _descriptor.Descriptor(
  name='UninterpretedOption',
  full_name='proto2.UninterpretedOption',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='proto2.UninterpretedOption.name', index=0,
      number=2, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='identifier_value', full_name='proto2.UninterpretedOption.identifier_value', index=1,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='positive_int_value', full_name='proto2.UninterpretedOption.positive_int_value', index=2,
      number=4, type=4, cpp_type=4, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='negative_int_value', full_name='proto2.UninterpretedOption.negative_int_value', index=3,
      number=5, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='double_value', full_name='proto2.UninterpretedOption.double_value', index=4,
      number=6, type=1, cpp_type=5, label=1,
      has_default_value=False, default_value=float(0),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='string_value', full_name='proto2.UninterpretedOption.string_value', index=5,
      number=7, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=_b(""),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='aggregate_value', full_name='proto2.UninterpretedOption.aggregate_value', index=6,
      number=8, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[_UNINTERPRETEDOPTION_NAMEPART, ],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=7759,
  serialized_end=8036,
)


_SOURCECODEINFO_LOCATION = _descriptor.Descriptor(
  name='Location',
  full_name='proto2.SourceCodeInfo.Location',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='path', full_name='proto2.SourceCodeInfo.Location.path', index=0,
      number=1, type=5, cpp_type=1, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='span', full_name='proto2.SourceCodeInfo.Location.span', index=1,
      number=2, type=5, cpp_type=1, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='leading_comments', full_name='proto2.SourceCodeInfo.Location.leading_comments', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='trailing_comments', full_name='proto2.SourceCodeInfo.Location.trailing_comments', index=3,
      number=4, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='leading_detached_comments', full_name='proto2.SourceCodeInfo.Location.leading_detached_comments', index=4,
      number=6, type=9, cpp_type=9, label=3,
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
  serialized_start=8109,
  serialized_end=8243,
)

_SOURCECODEINFO = _descriptor.Descriptor(
  name='SourceCodeInfo',
  full_name='proto2.SourceCodeInfo',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='location', full_name='proto2.SourceCodeInfo.location', index=0,
      number=1, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[_SOURCECODEINFO_LOCATION, ],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=8039,
  serialized_end=8243,
)


_GENERATEDCODEINFO_ANNOTATION = _descriptor.Descriptor(
  name='Annotation',
  full_name='proto2.GeneratedCodeInfo.Annotation',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='path', full_name='proto2.GeneratedCodeInfo.Annotation.path', index=0,
      number=1, type=5, cpp_type=1, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='source_file', full_name='proto2.GeneratedCodeInfo.Annotation.source_file', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='begin', full_name='proto2.GeneratedCodeInfo.Annotation.begin', index=2,
      number=3, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='end', full_name='proto2.GeneratedCodeInfo.Annotation.end', index=3,
      number=4, type=5, cpp_type=1, label=1,
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
  serialized_start=8325,
  serialized_end=8404,
)

_GENERATEDCODEINFO = _descriptor.Descriptor(
  name='GeneratedCodeInfo',
  full_name='proto2.GeneratedCodeInfo',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='annotation', full_name='proto2.GeneratedCodeInfo.annotation', index=0,
      number=1, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[_GENERATEDCODEINFO_ANNOTATION, ],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=8246,
  serialized_end=8404,
)

_FILEDESCRIPTORSET.fields_by_name['file'].message_type = _FILEDESCRIPTORPROTO
_FILEDESCRIPTORPROTO.fields_by_name['message_type'].message_type = _DESCRIPTORPROTO
_FILEDESCRIPTORPROTO.fields_by_name['enum_type'].message_type = _ENUMDESCRIPTORPROTO
_FILEDESCRIPTORPROTO.fields_by_name['service'].message_type = _SERVICEDESCRIPTORPROTO
_FILEDESCRIPTORPROTO.fields_by_name['extension'].message_type = _FIELDDESCRIPTORPROTO
_FILEDESCRIPTORPROTO.fields_by_name['options'].message_type = _FILEOPTIONS
_FILEDESCRIPTORPROTO.fields_by_name['source_code_info'].message_type = _SOURCECODEINFO
_DESCRIPTORPROTO_EXTENSIONRANGE.fields_by_name['options'].message_type = _EXTENSIONRANGEOPTIONS
_DESCRIPTORPROTO_EXTENSIONRANGE.containing_type = _DESCRIPTORPROTO
_DESCRIPTORPROTO_RESERVEDRANGE.containing_type = _DESCRIPTORPROTO
_DESCRIPTORPROTO.fields_by_name['field'].message_type = _FIELDDESCRIPTORPROTO
_DESCRIPTORPROTO.fields_by_name['extension'].message_type = _FIELDDESCRIPTORPROTO
_DESCRIPTORPROTO.fields_by_name['nested_type'].message_type = _DESCRIPTORPROTO
_DESCRIPTORPROTO.fields_by_name['enum_type'].message_type = _ENUMDESCRIPTORPROTO
_DESCRIPTORPROTO.fields_by_name['extension_range'].message_type = _DESCRIPTORPROTO_EXTENSIONRANGE
_DESCRIPTORPROTO.fields_by_name['oneof_decl'].message_type = _ONEOFDESCRIPTORPROTO
_DESCRIPTORPROTO.fields_by_name['options'].message_type = _MESSAGEOPTIONS
_DESCRIPTORPROTO.fields_by_name['reserved_range'].message_type = _DESCRIPTORPROTO_RESERVEDRANGE
_EXTENSIONRANGEOPTIONS.fields_by_name['uninterpreted_option'].message_type = _UNINTERPRETEDOPTION
_FIELDDESCRIPTORPROTO.fields_by_name['label'].enum_type = _FIELDDESCRIPTORPROTO_LABEL
_FIELDDESCRIPTORPROTO.fields_by_name['type'].enum_type = _FIELDDESCRIPTORPROTO_TYPE
_FIELDDESCRIPTORPROTO.fields_by_name['options'].message_type = _FIELDOPTIONS
_FIELDDESCRIPTORPROTO_TYPE.containing_type = _FIELDDESCRIPTORPROTO
_FIELDDESCRIPTORPROTO_LABEL.containing_type = _FIELDDESCRIPTORPROTO
_ONEOFDESCRIPTORPROTO.fields_by_name['options'].message_type = _ONEOFOPTIONS
_ENUMDESCRIPTORPROTO_ENUMRESERVEDRANGE.containing_type = _ENUMDESCRIPTORPROTO
_ENUMDESCRIPTORPROTO.fields_by_name['value'].message_type = _ENUMVALUEDESCRIPTORPROTO
_ENUMDESCRIPTORPROTO.fields_by_name['options'].message_type = _ENUMOPTIONS
_ENUMDESCRIPTORPROTO.fields_by_name['reserved_range'].message_type = _ENUMDESCRIPTORPROTO_ENUMRESERVEDRANGE
_ENUMVALUEDESCRIPTORPROTO.fields_by_name['options'].message_type = _ENUMVALUEOPTIONS
_SERVICEDESCRIPTORPROTO.fields_by_name['method'].message_type = _METHODDESCRIPTORPROTO
_SERVICEDESCRIPTORPROTO.fields_by_name['stream'].message_type = _STREAMDESCRIPTORPROTO
_SERVICEDESCRIPTORPROTO.fields_by_name['options'].message_type = _SERVICEOPTIONS
_METHODDESCRIPTORPROTO.fields_by_name['options'].message_type = _METHODOPTIONS
_STREAMDESCRIPTORPROTO.fields_by_name['options'].message_type = _STREAMOPTIONS
_FILEOPTIONS.fields_by_name['optimize_for'].enum_type = _FILEOPTIONS_OPTIMIZEMODE
_FILEOPTIONS.fields_by_name['uninterpreted_option'].message_type = _UNINTERPRETEDOPTION
_FILEOPTIONS_OPTIMIZEMODE.containing_type = _FILEOPTIONS
_MESSAGEOPTIONS.fields_by_name['uninterpreted_option'].message_type = _UNINTERPRETEDOPTION
_FIELDOPTIONS_UPGRADEDOPTION.containing_type = _FIELDOPTIONS
_FIELDOPTIONS.fields_by_name['ctype'].enum_type = _FIELDOPTIONS_CTYPE
_FIELDOPTIONS.fields_by_name['jstype'].enum_type = _FIELDOPTIONS_JSTYPE
_FIELDOPTIONS.fields_by_name['upgraded_option'].message_type = _FIELDOPTIONS_UPGRADEDOPTION
_FIELDOPTIONS.fields_by_name['uninterpreted_option'].message_type = _UNINTERPRETEDOPTION
_FIELDOPTIONS_CTYPE.containing_type = _FIELDOPTIONS
_FIELDOPTIONS_JSTYPE.containing_type = _FIELDOPTIONS
_ONEOFOPTIONS.fields_by_name['uninterpreted_option'].message_type = _UNINTERPRETEDOPTION
_ENUMOPTIONS.fields_by_name['uninterpreted_option'].message_type = _UNINTERPRETEDOPTION
_ENUMVALUEOPTIONS.fields_by_name['uninterpreted_option'].message_type = _UNINTERPRETEDOPTION
_SERVICEOPTIONS.fields_by_name['uninterpreted_option'].message_type = _UNINTERPRETEDOPTION
_METHODOPTIONS.fields_by_name['protocol'].enum_type = _METHODOPTIONS_PROTOCOL
_METHODOPTIONS.fields_by_name['security_level'].enum_type = _METHODOPTIONS_SECURITYLEVEL
_METHODOPTIONS.fields_by_name['response_format'].enum_type = _METHODOPTIONS_FORMAT
_METHODOPTIONS.fields_by_name['request_format'].enum_type = _METHODOPTIONS_FORMAT
_METHODOPTIONS.fields_by_name['legacy_token_unit'].enum_type = _METHODOPTIONS_TOKENUNIT
_METHODOPTIONS.fields_by_name['log_level'].enum_type = _METHODOPTIONS_LOGLEVEL
_METHODOPTIONS.fields_by_name['idempotency_level'].enum_type = _METHODOPTIONS_IDEMPOTENCYLEVEL
_METHODOPTIONS.fields_by_name['uninterpreted_option'].message_type = _UNINTERPRETEDOPTION
_METHODOPTIONS_PROTOCOL.containing_type = _METHODOPTIONS
_METHODOPTIONS_SECURITYLEVEL.containing_type = _METHODOPTIONS
_METHODOPTIONS_FORMAT.containing_type = _METHODOPTIONS
_METHODOPTIONS_LOGLEVEL.containing_type = _METHODOPTIONS
_METHODOPTIONS_TOKENUNIT.containing_type = _METHODOPTIONS
_METHODOPTIONS_IDEMPOTENCYLEVEL.containing_type = _METHODOPTIONS
_STREAMOPTIONS.fields_by_name['token_unit'].enum_type = _STREAMOPTIONS_TOKENUNIT
_STREAMOPTIONS.fields_by_name['security_level'].enum_type = _METHODOPTIONS_SECURITYLEVEL
_STREAMOPTIONS.fields_by_name['log_level'].enum_type = _METHODOPTIONS_LOGLEVEL
_STREAMOPTIONS.fields_by_name['uninterpreted_option'].message_type = _UNINTERPRETEDOPTION
_STREAMOPTIONS_TOKENUNIT.containing_type = _STREAMOPTIONS
_UNINTERPRETEDOPTION_NAMEPART.containing_type = _UNINTERPRETEDOPTION
_UNINTERPRETEDOPTION.fields_by_name['name'].message_type = _UNINTERPRETEDOPTION_NAMEPART
_SOURCECODEINFO_LOCATION.containing_type = _SOURCECODEINFO
_SOURCECODEINFO.fields_by_name['location'].message_type = _SOURCECODEINFO_LOCATION
_GENERATEDCODEINFO_ANNOTATION.containing_type = _GENERATEDCODEINFO
_GENERATEDCODEINFO.fields_by_name['annotation'].message_type = _GENERATEDCODEINFO_ANNOTATION
DESCRIPTOR.message_types_by_name['FileDescriptorSet'] = _FILEDESCRIPTORSET
DESCRIPTOR.message_types_by_name['FileDescriptorProto'] = _FILEDESCRIPTORPROTO
DESCRIPTOR.message_types_by_name['DescriptorProto'] = _DESCRIPTORPROTO
DESCRIPTOR.message_types_by_name['ExtensionRangeOptions'] = _EXTENSIONRANGEOPTIONS
DESCRIPTOR.message_types_by_name['FieldDescriptorProto'] = _FIELDDESCRIPTORPROTO
DESCRIPTOR.message_types_by_name['OneofDescriptorProto'] = _ONEOFDESCRIPTORPROTO
DESCRIPTOR.message_types_by_name['EnumDescriptorProto'] = _ENUMDESCRIPTORPROTO
DESCRIPTOR.message_types_by_name['EnumValueDescriptorProto'] = _ENUMVALUEDESCRIPTORPROTO
DESCRIPTOR.message_types_by_name['ServiceDescriptorProto'] = _SERVICEDESCRIPTORPROTO
DESCRIPTOR.message_types_by_name['MethodDescriptorProto'] = _METHODDESCRIPTORPROTO
DESCRIPTOR.message_types_by_name['StreamDescriptorProto'] = _STREAMDESCRIPTORPROTO
DESCRIPTOR.message_types_by_name['FileOptions'] = _FILEOPTIONS
DESCRIPTOR.message_types_by_name['MessageOptions'] = _MESSAGEOPTIONS
DESCRIPTOR.message_types_by_name['FieldOptions'] = _FIELDOPTIONS
DESCRIPTOR.message_types_by_name['OneofOptions'] = _ONEOFOPTIONS
DESCRIPTOR.message_types_by_name['EnumOptions'] = _ENUMOPTIONS
DESCRIPTOR.message_types_by_name['EnumValueOptions'] = _ENUMVALUEOPTIONS
DESCRIPTOR.message_types_by_name['ServiceOptions'] = _SERVICEOPTIONS
DESCRIPTOR.message_types_by_name['MethodOptions'] = _METHODOPTIONS
DESCRIPTOR.message_types_by_name['StreamOptions'] = _STREAMOPTIONS
DESCRIPTOR.message_types_by_name['UninterpretedOption'] = _UNINTERPRETEDOPTION
DESCRIPTOR.message_types_by_name['SourceCodeInfo'] = _SOURCECODEINFO
DESCRIPTOR.message_types_by_name['GeneratedCodeInfo'] = _GENERATEDCODEINFO
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

FileDescriptorSet = _reflection.GeneratedProtocolMessageType('FileDescriptorSet', (_message.Message,), dict(
  DESCRIPTOR = _FILEDESCRIPTORSET,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(FileDescriptorSet)

FileDescriptorProto = _reflection.GeneratedProtocolMessageType('FileDescriptorProto', (_message.Message,), dict(
  DESCRIPTOR = _FILEDESCRIPTORPROTO,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(FileDescriptorProto)

DescriptorProto = _reflection.GeneratedProtocolMessageType('DescriptorProto', (_message.Message,), dict(

  ExtensionRange = _reflection.GeneratedProtocolMessageType('ExtensionRange', (_message.Message,), dict(
    DESCRIPTOR = _DESCRIPTORPROTO_EXTENSIONRANGE,
    __module__ = 'google.net.proto2.proto.descriptor_pb2'

    ))
  ,

  ReservedRange = _reflection.GeneratedProtocolMessageType('ReservedRange', (_message.Message,), dict(
    DESCRIPTOR = _DESCRIPTORPROTO_RESERVEDRANGE,
    __module__ = 'google.net.proto2.proto.descriptor_pb2'

    ))
  ,
  DESCRIPTOR = _DESCRIPTORPROTO,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(DescriptorProto)
_sym_db.RegisterMessage(DescriptorProto.ExtensionRange)
_sym_db.RegisterMessage(DescriptorProto.ReservedRange)

ExtensionRangeOptions = _reflection.GeneratedProtocolMessageType('ExtensionRangeOptions', (_message.Message,), dict(
  DESCRIPTOR = _EXTENSIONRANGEOPTIONS,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(ExtensionRangeOptions)

FieldDescriptorProto = _reflection.GeneratedProtocolMessageType('FieldDescriptorProto', (_message.Message,), dict(
  DESCRIPTOR = _FIELDDESCRIPTORPROTO,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(FieldDescriptorProto)

OneofDescriptorProto = _reflection.GeneratedProtocolMessageType('OneofDescriptorProto', (_message.Message,), dict(
  DESCRIPTOR = _ONEOFDESCRIPTORPROTO,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(OneofDescriptorProto)

EnumDescriptorProto = _reflection.GeneratedProtocolMessageType('EnumDescriptorProto', (_message.Message,), dict(

  EnumReservedRange = _reflection.GeneratedProtocolMessageType('EnumReservedRange', (_message.Message,), dict(
    DESCRIPTOR = _ENUMDESCRIPTORPROTO_ENUMRESERVEDRANGE,
    __module__ = 'google.net.proto2.proto.descriptor_pb2'

    ))
  ,
  DESCRIPTOR = _ENUMDESCRIPTORPROTO,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(EnumDescriptorProto)
_sym_db.RegisterMessage(EnumDescriptorProto.EnumReservedRange)

EnumValueDescriptorProto = _reflection.GeneratedProtocolMessageType('EnumValueDescriptorProto', (_message.Message,), dict(
  DESCRIPTOR = _ENUMVALUEDESCRIPTORPROTO,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(EnumValueDescriptorProto)

ServiceDescriptorProto = _reflection.GeneratedProtocolMessageType('ServiceDescriptorProto', (_message.Message,), dict(
  DESCRIPTOR = _SERVICEDESCRIPTORPROTO,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(ServiceDescriptorProto)

MethodDescriptorProto = _reflection.GeneratedProtocolMessageType('MethodDescriptorProto', (_message.Message,), dict(
  DESCRIPTOR = _METHODDESCRIPTORPROTO,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(MethodDescriptorProto)

StreamDescriptorProto = _reflection.GeneratedProtocolMessageType('StreamDescriptorProto', (_message.Message,), dict(
  DESCRIPTOR = _STREAMDESCRIPTORPROTO,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(StreamDescriptorProto)

FileOptions = _reflection.GeneratedProtocolMessageType('FileOptions', (_message.Message,), dict(
  DESCRIPTOR = _FILEOPTIONS,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(FileOptions)

MessageOptions = _reflection.GeneratedProtocolMessageType('MessageOptions', (_message.Message,), dict(
  DESCRIPTOR = _MESSAGEOPTIONS,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(MessageOptions)

FieldOptions = _reflection.GeneratedProtocolMessageType('FieldOptions', (_message.Message,), dict(

  UpgradedOption = _reflection.GeneratedProtocolMessageType('UpgradedOption', (_message.Message,), dict(
    DESCRIPTOR = _FIELDOPTIONS_UPGRADEDOPTION,
    __module__ = 'google.net.proto2.proto.descriptor_pb2'

    ))
  ,
  DESCRIPTOR = _FIELDOPTIONS,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(FieldOptions)
_sym_db.RegisterMessage(FieldOptions.UpgradedOption)

OneofOptions = _reflection.GeneratedProtocolMessageType('OneofOptions', (_message.Message,), dict(
  DESCRIPTOR = _ONEOFOPTIONS,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(OneofOptions)

EnumOptions = _reflection.GeneratedProtocolMessageType('EnumOptions', (_message.Message,), dict(
  DESCRIPTOR = _ENUMOPTIONS,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(EnumOptions)

EnumValueOptions = _reflection.GeneratedProtocolMessageType('EnumValueOptions', (_message.Message,), dict(
  DESCRIPTOR = _ENUMVALUEOPTIONS,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(EnumValueOptions)

ServiceOptions = _reflection.GeneratedProtocolMessageType('ServiceOptions', (_message.Message,), dict(
  DESCRIPTOR = _SERVICEOPTIONS,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(ServiceOptions)

MethodOptions = _reflection.GeneratedProtocolMessageType('MethodOptions', (_message.Message,), dict(
  DESCRIPTOR = _METHODOPTIONS,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(MethodOptions)

StreamOptions = _reflection.GeneratedProtocolMessageType('StreamOptions', (_message.Message,), dict(
  DESCRIPTOR = _STREAMOPTIONS,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(StreamOptions)

UninterpretedOption = _reflection.GeneratedProtocolMessageType('UninterpretedOption', (_message.Message,), dict(

  NamePart = _reflection.GeneratedProtocolMessageType('NamePart', (_message.Message,), dict(
    DESCRIPTOR = _UNINTERPRETEDOPTION_NAMEPART,
    __module__ = 'google.net.proto2.proto.descriptor_pb2'

    ))
  ,
  DESCRIPTOR = _UNINTERPRETEDOPTION,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(UninterpretedOption)
_sym_db.RegisterMessage(UninterpretedOption.NamePart)

SourceCodeInfo = _reflection.GeneratedProtocolMessageType('SourceCodeInfo', (_message.Message,), dict(

  Location = _reflection.GeneratedProtocolMessageType('Location', (_message.Message,), dict(
    DESCRIPTOR = _SOURCECODEINFO_LOCATION,
    __module__ = 'google.net.proto2.proto.descriptor_pb2'

    ))
  ,
  DESCRIPTOR = _SOURCECODEINFO,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(SourceCodeInfo)
_sym_db.RegisterMessage(SourceCodeInfo.Location)

GeneratedCodeInfo = _reflection.GeneratedProtocolMessageType('GeneratedCodeInfo', (_message.Message,), dict(

  Annotation = _reflection.GeneratedProtocolMessageType('Annotation', (_message.Message,), dict(
    DESCRIPTOR = _GENERATEDCODEINFO_ANNOTATION,
    __module__ = 'google.net.proto2.proto.descriptor_pb2'

    ))
  ,
  DESCRIPTOR = _GENERATEDCODEINFO,
  __module__ = 'google.net.proto2.proto.descriptor_pb2'

  ))
_sym_db.RegisterMessage(GeneratedCodeInfo)
_sym_db.RegisterMessage(GeneratedCodeInfo.Annotation)



