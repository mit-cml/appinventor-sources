# -*- coding: utf-8 -*- #
# Copyright 2017 Google Inc. All Rights Reserved.
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

"""Data objects to support the yaml command schema."""


from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import actions
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.command_lib.util.apis import arg_utils
from googlecloudsdk.core import module_util

import six


class Error(Exception):
  """Base class for module errors."""
  pass


class InvalidSchemaError(Error):
  """Error for when a yaml command is malformed."""
  pass


class Hook(object):
  """Represents a Python code hook declared in the yaml spec.

  A code hook points to some python element with a module path, and attribute
  path like: package.module:class.attribute.

  If arguments are provided, first the function is called with the arguments
  and the return value of that is the hook that is used. For example:

  googlecloudsdk.calliope.arg_parsers:Duration:lower_bound=1s,upper_bound=1m
  """

  @classmethod
  def FromData(cls, data, key):
    """Gets the hook from the spec data.

    Args:
      data: The yaml spec
      key: The key to extract the hook path from.

    Returns:
      The Python element to call.
    """
    path = data.get(key)
    if path:
      return cls.FromPath(path)
    return None

  @classmethod
  def FromPath(cls, path):
    """Gets the hook from the function path.

    Args:
      path: str, The module path to the hook function.

    Returns:
      The Python element to call.
    """
    return ImportPythonHook(path).GetHook()

  def __init__(self, attribute, kwargs=None):
    self.attribute = attribute
    self.kwargs = kwargs

  def GetHook(self):
    """Gets the Python element that corresponds to this hook.

    Returns:
      A Python element.
    """
    if self.kwargs is not None:
      return  self.attribute(**self.kwargs)
    return self.attribute


def ImportPythonHook(path):
  """Imports the given python hook.

  Depending on what it is used for, a hook is a reference to a class, function,
  or attribute in Python code.

  Args:
    path: str, The path of the hook to import. It must be in the form of:
      package.module:attribute.attribute where the module path is separated from
      the class name and sub attributes by a ':'. Additionally, ":arg=value,..."
      can be appended to call the function with the given args and use the
      return value as the hook.

  Raises:
    InvalidSchemaError: If the given module or attribute cannot be loaded.

  Returns:
    Hook, the hook configuration.
  """
  parts = path.split(':')
  if len(parts) != 2 and len(parts) != 3:
    raise InvalidSchemaError(
        'Invalid Python hook: [{}]. Hooks must be in the format: '
        'package(.module)+:attribute(.attribute)*(:arg=value(,arg=value)*)?'
        .format(path))
  try:
    attr = module_util.ImportModule(parts[0] + ':' + parts[1])
  except module_util.ImportModuleError as e:
    raise InvalidSchemaError(
        'Could not import Python hook: [{}]. {}'.format(path, e))

  kwargs = None
  if len(parts) == 3:
    kwargs = {}
    for arg in parts[2].split(','):
      if not arg:
        continue
      arg_parts = arg.split('=')
      if len(arg_parts) != 2:
        raise InvalidSchemaError(
            'Invalid Python hook: [{}]. Args must be in the form arg=value,'
            'arg=value,...'.format(path))
      kwargs[arg_parts[0].strip()] = arg_parts[1].strip()

  return Hook(attr, kwargs)


STATIC_ACTIONS = {'store', 'store_true'}


def ParseAction(action, flag_name):
  """Parse the action out of the argument spec.

  Args:
    action: The argument action spec data.
    flag_name: str, The effective flag name.

  Raises:
    ValueError: If the spec is invalid.

  Returns:
    The action to use as argparse accepts it. It will either be a class that
    implements action, or it will be a str of a builtin argparse type.
  """
  if not action:
    return None

  if isinstance(action, six.string_types):
    if action in STATIC_ACTIONS:
      return action
    return Hook.FromPath(action)

  deprecation = action.get('deprecated')
  if deprecation:
    return actions.DeprecationAction(flag_name, **deprecation)

  raise ValueError('Unknown value for action: ' + str(action))


BUILTIN_TYPES = {
    'str': str,
    'int': int,
    'long': long if six.PY2 else int,
    'float': float,
    'bool': bool,
}


def ParseType(t):
  """Parse the action out of the argument spec.

  Args:
    t: The argument type spec data.

  Raises:
    ValueError: If the spec is invalid.

  Returns:
    The type to use as argparse accepts it.
  """
  if not t:
    return None

  if isinstance(t, six.string_types):
    builtin_type = BUILTIN_TYPES.get(t)
    if builtin_type:
      return builtin_type
    return Hook.FromPath(t)

  if 'arg_dict' in t:
    return ArgDict.FromData(t.get('arg_dict'))

  raise ValueError('Unknown value for type: ' + str(t))


class Choice(object):
  """Holds information about a single enum choice value."""

  def __init__(self, data):
    self.arg_value = data['arg_value']
    if isinstance(self.arg_value, six.string_types):
      # We always do a case insensitive comparison.
      self.arg_value = self.arg_value.lower()
    self.enum_value = data['enum_value']
    self.help_text = data.get('help_text')

  @classmethod
  def ToChoiceMap(cls, choices):
    """Converts a list of choices into a map for easy value lookup.

    Args:
      choices: [Choice], The choices.

    Returns:
      {arg_value: enum_value}, A mapping of user input to the value that should
      be used. All arg_values have already been converted to lowercase for
      comparison.
    """
    if not choices:
      return {}
    return {c.arg_value: c.enum_value for c in choices}


class ArgDict(arg_utils.RepeatedMessageBindableType):
  """A wrapper to bind an ArgDict argument to a message.

  The non-flat mode has one dict per message. When the field is repeated, you
  can repeat the message by repeating the flag. For example, given a message
  with fields foo and bar, it looks like:

  --arg foo=1,bar=2 --arg foo=3,bar=4

  The Action method below is used later during argument generation to tell
  argparse to allow repeats of the dictionary and to append them.
  """

  @classmethod
  def FromData(cls, data):
    fields = [ArgDictField.FromData(d) for d in data['spec']]
    if data.get('flatten'):
      if len(fields) != 2:
        raise InvalidSchemaError(
            'Flattened ArgDicts must have exactly two items in the spec.')
      return FlattenedArgDict(fields[0], fields[1])
    return cls(fields)

  def __init__(self, fields):
    self.fields = fields

  def Action(self):
    return 'append'

  def GenerateType(self, message):
    """Generates an argparse type function to use to parse the argument.

    The return of the type function will be an instance of the given message
    with the fields filled in.

    Args:
      message: The apitools message class.

    Raises:
      InvalidSchemaError: If a type for a field could not be determined.

    Returns:
      f(str) -> message, The type function that parses the ArgDict and returns
      a message instance.
    """
    spec = {}
    for f in self.fields:
      api_field = arg_utils.GetFieldFromMessage(message, f.api_field)
      t = f.type or arg_utils.TYPES.get(api_field.variant)
      if not t:
        raise InvalidSchemaError('Unknown type for field: ' + f.api_field)
      spec[f.arg_name] = t

    required = [f.arg_name for f in self.fields if f.required]
    arg_dict = arg_parsers.ArgDict(spec=spec, required_keys=required)

    def Parse(arg_value):
      """Inner method that argparse actually calls."""
      result = arg_dict(arg_value)
      message_instance = message()
      for f in self.fields:
        value = result.get(f.arg_name)
        api_field = arg_utils.GetFieldFromMessage(message, f.api_field)
        value = arg_utils.ConvertValue(
            api_field, value, choices=Choice.ToChoiceMap(f.choices))
        arg_utils.SetFieldInMessage(message_instance, f.api_field, value)
      return message_instance
    return Parse


class FlattenedArgDict(arg_utils.RepeatedMessageBindableType):
  """A wrapper to bind an ArgDict argument to a message with a key/value pair.

  The flat mode has one dict corresponding to a repeated field. For example,
  given a message with fields key and value, it looks like:

  --arg a=b,c=d

  Which would generate 2 instances of the message:
  [{key=a, value=b}, {key=c, value=d}]
  """

  def __init__(self, key_field, value_field):
    self.key_spec = key_field
    self.value_spec = value_field

  def _GetType(self, message, field):
    f = arg_utils.GetFieldFromMessage(
        message, field.api_field)
    t = field.type or arg_utils.TYPES.get(f.variant)
    if not t:
      raise InvalidSchemaError('Unknown type for field: ' + field.api_field)
    return f, t

  def GenerateType(self, message):
    """Generates an argparse type function to use to parse the argument.

    The return of the type function will be a list of instances of the given
    message with the fields filled in.

    Args:
      message: The apitools message class.

    Raises:
      InvalidSchemaError: If a type for a field could not be determined.

    Returns:
      f(str) -> [message], The type function that parses the ArgDict and returns
      a list of message instances.
    """
    key_field, key_type = self._GetType(message, self.key_spec)
    value_field, value_type = self._GetType(message, self.value_spec)
    arg_dict = arg_parsers.ArgDict(key_type=key_type, value_type=value_type)

    def Parse(arg_value):
      """Inner method that argparse actually calls."""
      result = arg_dict(arg_value)
      messages = []
      for k, v in sorted(six.iteritems(result)):
        message_instance = message()
        arg_utils.SetFieldInMessage(
            message_instance, self.key_spec.api_field,
            arg_utils.ConvertValue(
                key_field, k, choices=self.key_spec.ChoiceMap()))
        arg_utils.SetFieldInMessage(
            message_instance, self.value_spec.api_field,
            arg_utils.ConvertValue(
                value_field, v, choices=self.value_spec.ChoiceMap()))
        messages.append(message_instance)
      return messages
    return Parse


class ArgDictField(object):
  """Attributes about the fields that make up an ArgDict spec.

  Attributes:
    api_field: The name of the field under the repeated message that the value
      should be put.
    arg_name: The name of the key in the dict.
    type: The argparse type of the value of this field.
    required: True if the key is required.
    choices: A static map of choice to value the user types.
  """

  @classmethod
  def FromData(cls, data):
    api_field = data['api_field']
    arg_name = data.get('arg_name', api_field)
    t = ParseType(data.get('type'))
    required = data.get('required', True)
    choices = data.get('choices')
    choices = [Choice(d) for d in choices] if choices else None
    return cls(api_field, arg_name, t, required, choices)

  def __init__(self, api_field, arg_name, t, required, choices):
    self.api_field = api_field
    self.arg_name = arg_name
    self.type = t
    self.required = required
    self.choices = choices

  def ChoiceMap(self):
    return Choice.ToChoiceMap(self.choices)
