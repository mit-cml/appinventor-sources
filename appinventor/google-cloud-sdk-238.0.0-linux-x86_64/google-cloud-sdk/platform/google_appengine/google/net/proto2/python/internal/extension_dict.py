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


"""Contains _ExtensionDict class to represent extensions.
"""

from google.net.proto2.python.internal import type_checkers
from google.net.proto2.python.public.descriptor import FieldDescriptor


def _VerifyExtensionHandle(message, extension_handle):
  """Verify that the given extension handle is valid."""

  if not isinstance(extension_handle, FieldDescriptor):
    raise KeyError('HasExtension() expects an extension handle, got: %s' %
                   extension_handle)

  if not extension_handle.is_extension:
    raise KeyError('"%s" is not an extension.' % extension_handle.full_name)

  if not extension_handle.containing_type:
    raise KeyError('"%s" is missing a containing_type.'
                   % extension_handle.full_name)

  if extension_handle.containing_type is not message.DESCRIPTOR:
    raise KeyError('Extension "%s" extends message type "%s", but this '
                   'message is of type "%s".' %
                   (extension_handle.full_name,
                    extension_handle.containing_type.full_name,
                    message.DESCRIPTOR.full_name))





class _ExtensionDict(object):

  """Dict-like container for Extension fields on proto instances.

  Note that in all cases we expect extension handles to be
  FieldDescriptors.
  """

  def __init__(self, extended_message):
    """
    Args:
      extended_message: Message instance for which we are the Extensions dict.
    """
    self._extended_message = extended_message

  def __getitem__(self, extension_handle):
    """Returns the current value of the given extension handle."""

    _VerifyExtensionHandle(self._extended_message, extension_handle)

    result = self._extended_message._fields.get(extension_handle)
    if result is not None:
      return result

    if extension_handle.label == FieldDescriptor.LABEL_REPEATED:
      result = extension_handle._default_constructor(self._extended_message)
    elif extension_handle.cpp_type == FieldDescriptor.CPPTYPE_MESSAGE:
      assert getattr(extension_handle.message_type, '_concrete_class', None), (
          'Uninitialized concrete class found for field %r (message type %r)'
          % (extension_handle.full_name,
             extension_handle.message_type.full_name))
      result = extension_handle.message_type._concrete_class()
      try:
        result._SetListener(self._extended_message._listener_for_children)
      except ReferenceError:
        pass
    else:


      return extension_handle.default_value







    result = self._extended_message._fields.setdefault(
        extension_handle, result)

    return result

  def __eq__(self, other):
    if not isinstance(other, self.__class__):
      return False

    my_fields = self._extended_message.ListFields()
    other_fields = other._extended_message.ListFields()


    my_fields = [field for field in my_fields if field.is_extension]
    other_fields = [field for field in other_fields if field.is_extension]

    return my_fields == other_fields

  def __ne__(self, other):
    return not self == other

  def __hash__(self):
    raise TypeError('unhashable object')





  def __setitem__(self, extension_handle, value):
    """If extension_handle specifies a non-repeated, scalar extension
    field, sets the value of that field.
    """

    _VerifyExtensionHandle(self._extended_message, extension_handle)

    if (extension_handle.label == FieldDescriptor.LABEL_REPEATED or
        extension_handle.cpp_type == FieldDescriptor.CPPTYPE_MESSAGE):
      raise TypeError(
          'Cannot assign to extension "%s" because it is a repeated or '
          'composite type.' % extension_handle.full_name)



    type_checker = type_checkers.GetTypeChecker(extension_handle)

    self._extended_message._fields[extension_handle] = (
        type_checker.CheckValue(value))
    self._extended_message._Modified()

  def _FindExtensionByName(self, name):
    """Tries to find a known extension with the specified name.

    Args:
      name: Extension full name.

    Returns:
      Extension field descriptor.
    """
    return self._extended_message._extensions_by_name.get(name, None)

  def _FindExtensionByNumber(self, number):
    """Tries to find a known extension with the field number.

    Args:
      number: Extension field number.

    Returns:
      Extension field descriptor.
    """
    return self._extended_message._extensions_by_number.get(number, None)
