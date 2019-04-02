# -*- coding: utf-8 -*- #
# Copyright 2018 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Helpers for converting between Python objects and proto message objects."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.protorpclite import messages as _messages
from apitools.base.py import encoding as _encoding
from googlecloudsdk.command_lib.container.binauthz import exceptions

import six


def _FormatProtoPath(edges, field_names):
  """Constructs and returns a string representation of the proto path."""
  # Format the edges.
  path = [six.text_type(edge) for edge in edges]

  if len(field_names) > 1:
    # Use braces to group the errors when there are multiple errors.
    # e.g. foo.bar.{x,y,z}
    path.append('{{{}}}'.format(','.join(sorted(field_names))))
  elif field_names:
    # For single items, omit the braces.
    # e.g. foo.bar.x
    path.append(field_names[0])

  return '.'.join(path)


class Error(exceptions.Error):
  """Indicates an error with an encoded protorpclite message."""


class DecodeError(Error):
  """Indicates an error in decoding a protorpclite message."""

  @classmethod
  def FromErrorPaths(cls, message, errors):
    """Returns a DecodeError from a list of locations of errors.

    Args:
      message: The protorpc Message in which a parsing error occurred.
      errors: List[(edges, field_names)], A list of locations of errors
          encountered while decoding the message.
    """
    type_ = type(message).__name__
    base_msg = 'Failed to parse value(s) in protobuf [{type_}]:'.format(
        type_=type_)
    error_paths = [
        '  {type_}.{path}'.format(
            type_=type_,
            path=_FormatProtoPath(edges, field_names))
        for edges, field_names in errors]
    return cls('\n'.join([base_msg] + error_paths))


class ScalarTypeMismatchError(DecodeError):
  """Incicates a scalar property was provided a value of an unexpected type."""


def DictToMessageWithErrorCheck(dict_, message_type):
  """Convert "dict_" to a message of type message_type and check for errors.

  Args:
    dict_: The dict to parse into a protorpc Message.
    message_type: The protorpc Message type.

  Returns:
    A message of type "message_type" parsed from "dict_".

  Raises:
    DecodeError: One or more unparsable values were found in the parsed message.
  """
  try:
    message = _encoding.DictToMessage(dict_, message_type)
  except _messages.ValidationError as e:
    # NOTE: The ValidationError message is passable but does not specify the
    # full path to the property where the error occurred.
    raise ScalarTypeMismatchError(
        'Failed to parse value in protobuf [{type_}]:\n'
        '  {type_}.??: "{msg}"'.format(
            type_=message_type.__name__, msg=str(e)))
  except AttributeError:
    # TODO(b/77547931): This is a bug in apitools and must be fixed upstream.
    # The decode logic attempts an unchecked access to 'iteritems' assuming the
    # Message field's associated value is a dict.
    raise
  else:
    errors = list(_encoding.UnrecognizedFieldIter(message))
    if errors:
      raise DecodeError.FromErrorPaths(message, errors)

    return message
