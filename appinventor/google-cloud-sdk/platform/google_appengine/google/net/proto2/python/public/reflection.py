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






"""Contains a metaclass and helper functions used to create
protocol message classes from Descriptor objects at runtime.

Recall that a metaclass is the "type" of a class.
(A class is to a metaclass what an instance is to a class.)

In this case, we use the GeneratedProtocolMessageType metaclass
to inject all the useful functionality into the classes
output by the protocol compiler at compile-time.

The upshot of all this is that the real implementation
details for ALL pure-Python protocol buffers are *here in
this file*.
"""




from google.net.proto2.python.public import message_factory
from google.net.proto2.python.public import symbol_database



GeneratedProtocolMessageType = message_factory._GENERATED_PROTOCOL_MESSAGE_TYPE

MESSAGE_CLASS_CACHE = {}



def ParseMessage(descriptor, byte_str):
  """Generate a new Message instance from this Descriptor and a byte string.

  DEPRECATED: ParseMessage is deprecated because it is using MakeClass().
  Please use MessageFactory.GetPrototype() instead.

  Args:
    descriptor: Protobuf Descriptor object
    byte_str: Serialized protocol buffer byte string

  Returns:
    Newly created protobuf Message object.
  """
  result_class = MakeClass(descriptor)
  new_msg = result_class()
  new_msg.ParseFromString(byte_str)
  return new_msg



def MakeClass(descriptor):
  """Construct a class object for a protobuf described by descriptor.

  DEPRECATED: use MessageFactory.GetPrototype() instead.

  Args:
    descriptor: A descriptor.Descriptor object describing the protobuf.
  Returns:
    The Message class object described by the descriptor.
  """



  return symbol_database.Default().GetPrototype(descriptor)
