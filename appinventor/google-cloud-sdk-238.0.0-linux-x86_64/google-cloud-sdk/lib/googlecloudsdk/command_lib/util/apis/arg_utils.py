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

"""Utilities for generating and parsing arguments from API fields."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from collections import OrderedDict
import re

from apitools.base.protorpclite import messages
import enum  # pylint: disable=unused-import, for pytype
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.core import properties
from googlecloudsdk.core.resource import resource_property
from googlecloudsdk.core.util import http_encoding

import six

# Used to determine if a value has been set for an argument
UNSPECIFIED = object()


class Error(Exception):
  """Base exception for this module."""
  pass


class UnknownFieldError(Error):
  """The referenced field could not be found in the message object."""

  def __init__(self, field_name, message):
    super(UnknownFieldError, self).__init__(
        'Field [{}] not found in message [{}]. Available fields: [{}]'
        .format(field_name, message.__name__,
                ', '.join(f.name for f in message.all_fields())))


class ArgumentGenerationError(Error):
  """Generic error when we can't auto generate an argument for an api field."""

  def __init__(self, field_name, reason):
    super(ArgumentGenerationError, self).__init__(
        'Failed to generate argument for field [{}]: {}'
        .format(field_name, reason))


def GetFieldFromMessage(message, field_path):
  """Digs into the given message to extract the dotted field.

  If the field does not exist, and error is logged.

  Args:
    message: The apitools message to dig into.
    field_path: str, The dotted path of attributes and sub-attributes.

  Returns:
    The Field type.
  """
  fields = field_path.split('.')
  for f in fields[:-1]:
    message = _GetField(message, f).type
  return _GetField(message, fields[-1])


def SetFieldInMessage(message, field_path, value):
  """Sets the given field field in the message object.

  Args:
    message: A constructed apitools message object to inject the value into.
    field_path: str, The dotted path of attributes and sub-attributes.
    value: The value to set.
  """
  fields = field_path.split('.')
  for f in fields[:-1]:
    sub_message = getattr(message, f)
    is_repeated = _GetField(message, f).repeated
    if not sub_message:
      sub_message = _GetField(message, f).type()
      if is_repeated:
        sub_message = [sub_message]
      setattr(message, f, sub_message)
    message = sub_message[0] if is_repeated else sub_message
  setattr(message, fields[-1], value)


def _GetField(message, field_name):
  try:
    return message.field_by_name(field_name)
  except KeyError:
    raise UnknownFieldError(field_name, message)


# TODO(b/64147277): Pass this down from the generator, don't hard code.
DEFAULT_PARAMS = {'project': properties.VALUES.core.project.Get,
                  'projectId': properties.VALUES.core.project.Get,
                  'projectsId': properties.VALUES.core.project.Get,
                 }


def GetFromNamespace(namespace, arg_name, fallback=None, use_defaults=False):
  """Gets the given argument from the namespace."""
  if arg_name.startswith('--'):
    arg_name = arg_name[2:]
  normalized_arg_name = arg_name.replace('-', '_')
  value = getattr(namespace, normalized_arg_name, None)
  if not value and fallback:
    value = fallback()
  if not value and use_defaults:
    value = DEFAULT_PARAMS.get(arg_name, lambda: None)()
  return value


def Limit(method, namespace):
  """Gets the value of the limit flag (if present)."""
  if method.IsPageableList() and method.ListItemField():
    return getattr(namespace, 'limit')


def PageSize(method, namespace):
  """Gets the value of the page size flag (if present)."""
  if (method.IsPageableList() and method.ListItemField() and
      method.BatchPageSizeField()):
    return getattr(namespace, 'page_size')


class RepeatedMessageBindableType(object):
  """An interface for custom type generators that bind directly to a message.

  An argparse type function converts the parsed string into an object. Some
  types (like ArgDicts) can only be generated once we know what message it will
  be bound to (because the spec of the ArgDict depends on the fields and types
  in the message. This interface allows encapsulating the logic to generate a
  type function at the point when the message it is being bound to is known.
  """

  def GenerateType(self, message):
    """Generates an argparse type function to use to parse the argument.

    Args:
      message: The apitools message class.
    """
    pass

  def Action(self):
    """The argparse action to use for this argument.

    'store' is the default action, but sometimes something like 'append' might
    be required to allow the argument to be repeated and all values collected.

    Returns:
      str, The argparse action to use.
    """
    return 'store'


def GenerateFlag(field, attributes, fix_bools=True, category=None):
  """Generates a flag for a single field in a message.

  Args:
    field: The apitools field object.
    attributes: yaml_command_schema.Argument, The attributes to use to
      generate the arg.
    fix_bools: True to generate boolean flags as switches that take a value or
      False to just generate them as regular string flags.
    category: The help category to put the flag in.

  Raises:
    ArgumentGenerationError: When an argument could not be generated from the
      API field.

  Returns:
    calliope.base.Argument, The generated argument.
  """
  variant = field.variant if field else None
  t = attributes.type or TYPES.get(variant, None)

  choices = None
  if attributes.choices is not None:
    choice_map = {c.arg_value: c.help_text for c in attributes.choices}
    # If help text is provided, give a choice map. Otherwise, just use the
    # choice values.
    choices = (choice_map if any(choice_map.values())
               else sorted(choice_map.keys()))
  elif variant == messages.Variant.ENUM:
    choices = [EnumNameToChoice(name) for name in sorted(field.type.names())]

  action = attributes.action
  if t == bool and fix_bools and not action:
    # For boolean flags, we want to create a flag with action 'store_true'
    # rather than a flag that takes a value and converts it to a boolean. Only
    # do this if not using a custom action.
    action = 'store_true'
  # Default action is store if one was not provided.
  action = action or 'store'

  # pylint: disable=g-explicit-bool-comparison, only an explicit False should
  # override this, None just means to do the default.
  repeated = (field and field.repeated) and attributes.repeated != False

  if repeated:
    if action != 'store':
      raise ArgumentGenerationError(
          field.name,
          'The field is repeated but is using a custom action. You might'
          ' want to set repeated: False in your arg spec.')
    if t:
      # A special ArgDict wrapper type was given, bind it to the message so it
      # can generate the message from the key/value pairs.
      if isinstance(t, RepeatedMessageBindableType):
        action = t.Action()
        t = t.GenerateType(field.type)
      # If a simple type was provided, just use a list of that type (even if it
      # is a message). The type function will be responsible for converting to
      # the correct value. If type is an ArgList or ArgDict, don't try to wrap
      # it.
      elif not isinstance(t, arg_parsers.ArgList):
        t = arg_parsers.ArgList(element_type=t, choices=choices)
        # Don't register the choices on the argparse arg because it is validated
        # by the ArgList.
        choices = None
  elif isinstance(t, RepeatedMessageBindableType):
    raise ArgumentGenerationError(
        field.name, 'The given type can only be used on repeated fields.')

  if field and not t and action == 'store' and not attributes.processor:
    # The type is unknown and there is no custom action or processor, we don't
    # know what to do with this.
    raise ArgumentGenerationError(
        field.name, 'The field is of an unknown type. You can specify a type '
                    'function or a processor to manually handle this argument.')

  name = attributes.arg_name
  arg = base.Argument(
      name if attributes.is_positional else '--' + name,
      category=category if not attributes.is_positional else None,
      action=action,
      completer=attributes.completer,
      help=attributes.help_text,
      hidden=attributes.hidden,
  )
  if attributes.default != UNSPECIFIED:
    arg.kwargs['default'] = attributes.default
  if action != 'store_true':
    # For this special action type, it won't accept a bunch of the common
    # kwargs, so we can only add them if not generating a boolean flag.
    metavar = attributes.metavar or name
    arg.kwargs['metavar'] = resource_property.ConvertToAngrySnakeCase(
        metavar.replace('-', '_'))
    arg.kwargs['type'] = t
    arg.kwargs['choices'] = choices
  if not attributes.is_positional:
    arg.kwargs['required'] = attributes.required
  return arg


def ConvertValue(field, value, repeated=None, processor=None, choices=None):
  """Coverts the parsed value into something to insert into a request message.

  If a processor is registered, that is called on the value.
  If a choices mapping was provided, each value is mapped back into its original
  value.
  If the field is an enum, the value will be looked up by name and the Enum type
  constructed.

  Args:
    field: The apitools field object.
    value: The parsed value. This must be a scalar for scalar fields and a list
      for repeated fields.
    repeated: bool, Set to False if this arg was forced to be singular even
      though the API field it corresponds to is repeated.
    processor: A function to process the value before putting it into the
      message.
    choices: {str: str} A mapping of argument value, to enum API enum value.

  Returns:
    The value to insert into the message.
  """
  # pylint: disable=g-explicit-bool-comparison, only an explicit False should
  # override this, None just means to do the default.
  arg_repeated = field.repeated and repeated != False

  if processor:
    value = processor(value)
  else:
    valid_choices = None
    if choices:
      valid_choices = choices.keys()
      if arg_repeated:
        value = [_MapChoice(choices, v) for v in value]
      else:
        value = _MapChoice(choices, value)
    if field.variant == messages.Variant.ENUM:
      t = field.type
      if arg_repeated:
        value = [ChoiceToEnum(v, t, valid_choices=valid_choices) for v in value]
      else:
        value = ChoiceToEnum(value, t, valid_choices=valid_choices)

  if field.repeated and not arg_repeated and not isinstance(value, list):
    # If we manually made this arg singular, but it is actually a repeated field
    # wrap it in a list.
    value = [value]
  return value


def _MapChoice(choices, value):
  if isinstance(value, six.string_types):
    value = value.lower()
  return choices.get(value, value)


def ParseResourceIntoMessage(ref, method, message, resource_method_params=None,
                             request_id_field=None, use_relative_name=True):
  """Set fields in message corresponding to a resource.

  Args:
    ref: googlecloudsdk.core.resources.Resource, the resource reference.
    method: the API method.
    message: apitools Message object.
    resource_method_params: {str: str}, A mapping of API method parameter name
      to resource ref attribute name, if any
    request_id_field: str, the name that the ID of the resource arg takes if the
      API method params and the resource params don't match.
    use_relative_name: Used ref.RelativeName() if True, otherwise ref.Name().
  """
  resource_method_params = resource_method_params or {}
  resource_method_params = resource_method_params.copy()

  # This only happens for non-list methods where the API method params don't
  # match the resource parameters (basically only create methods). In this
  # case, we re-parse the resource as its parent collection (to fill in the
  # API parameters, and we insert the name of the resource itself into the
  # correct position in the body of the request method.
  if (request_id_field and method.resource_argument_collection.detailed_params
      != method.request_collection.detailed_params):
    # Sets the name of the resource in the message object body.
    SetFieldInMessage(message, request_id_field, ref.Name())
    # Create a reference for the parent resource to put in the API params.
    ref = ref.Parent(
        parent_collection=method.request_collection.full_name)

  ref_name = ref.RelativeName() if use_relative_name else ref.Name()
  for p in method.params:
    value = getattr(ref, resource_method_params.pop(p, p), ref_name)
    SetFieldInMessage(message, p, value)
  for message_field_name, ref_param_name in resource_method_params.items():
    value = getattr(ref, ref_param_name, ref_name)
    SetFieldInMessage(message, message_field_name, value)


def ParseStaticFieldsIntoMessage(message, static_fields=None):
  """Set fields in message corresponding to a dict of static field values.

  Args:
    message: the Apitools message.
    static_fields: dict of fields to values.
  """
  static_fields = static_fields or {}
  for field_path, value in six.iteritems(static_fields):
    field = GetFieldFromMessage(message, field_path)
    SetFieldInMessage(
        message, field_path, ConvertValue(field, value))


def ParseExistingMessageIntoMessage(message, existing_message, method):
  """Sets fields in message based on an existing message.

  This function is used for get-modify-update pattern. The request type of
  update requests would be either the same as the response type of get requests
  or one field inside the request would be the same as the get response.

  For example:
  1) update.request_type_name = ServiceAccount
     get.response_type_name = ServiceAccount
  2) update.request_type_name = updateInstanceRequest
     updateInstanceRequest.instance = Instance
     get.response_type_name = Instance

  If the existing message has the same type as the message to be sent for the
  request, then return the existing message instead. If they are different, find
  the field in the message which has the same type as existing_message, then
  assign exsiting message to that field.

  Args:
    message: the apitools message to construct a new request.
    existing_message: the exsting apitools message returned from server.
    method: APIMethod, the method to generate request for.

  Returns:
    A modified apitools message to be send to the method.
  """
  if type(existing_message) == type(message):  # pylint: disable=unidiomatic-typecheck
    return existing_message

  # For read-modify-update api calls, the field name would be the same level
  # or the next level of the request.
  # TODO(b/111069150): refactor this part, don't hard code.
  existing_message_name = type(existing_message).__name__
  field_name = existing_message_name[0].lower() + existing_message_name[1:]
  field_path = ''
  if method.request_field != field_name:
    field_path += method.request_field
    field_path += '.'
  field_path += field_name

  SetFieldInMessage(message, field_path, existing_message)
  return message


def ChoiceToEnum(choice, enum_type, item_type='choice', valid_choices=None):
  # type: (str, enum.Enum, list) -> enum.Enum
  """Converts the typed choice into an apitools Enum value."""
  if choice is None:
    return None
  name = choice.replace('-', '_').upper()
  valid_choices = (valid_choices or
                   [EnumNameToChoice(n) for n in enum_type.names()])
  try:
    return enum_type.lookup_by_name(name)
  except KeyError:
    raise arg_parsers.ArgumentTypeError(
        'Invalid {item}: {selection}. Valid choices are: [{values}].'.format(
            item=item_type,
            selection=EnumNameToChoice(name),
            values=', '.join(c for c in sorted(valid_choices))))


def EnumNameToChoice(name):
  """Converts the name of an Enum value into a typeable choice."""
  return name.replace('_', '-').lower()


_LONG_TYPE = long if six.PY2 else int


TYPES = {
    messages.Variant.DOUBLE: float,
    messages.Variant.FLOAT: float,

    messages.Variant.INT64: _LONG_TYPE,
    messages.Variant.UINT64: _LONG_TYPE,
    messages.Variant.SINT64: _LONG_TYPE,

    messages.Variant.INT32: int,
    messages.Variant.UINT32: int,
    messages.Variant.SINT32: int,

    messages.Variant.STRING: six.text_type,
    messages.Variant.BOOL: bool,

    # TODO(b/70980549): Do something better with bytes.
    messages.Variant.BYTES: http_encoding.Encode,
    # For enums, we want to accept upper and lower case from the user, but
    # always compare against lowercase enum choices.
    messages.Variant.ENUM: EnumNameToChoice,
    messages.Variant.MESSAGE: None,
}


def FieldHelpDocs(message, section='Fields'):
  """Gets the help text for the fields in the request message.

  Args:
    message: The apitools message.
    section: str, The section to extract help data from. Fields is the default,
      may also be Values to extract enum data, for example.

  Returns:
    {str: str}, A mapping of field name to help text.
  """
  field_helps = {}
  current_field = None

  match = re.search(r'^\s+{}:.*$'.format(section),
                    message.__doc__ or '', re.MULTILINE)
  if not match:
    # Couldn't find any fields at all.
    return field_helps

  for line in message.__doc__[match.end():].splitlines():
    match = re.match(r'^\s+(\w+): (.*)$', line)
    if match:
      # This line is the start of a new field.
      current_field = match.group(1)
      field_helps[current_field] = match.group(2).strip()
    elif current_field:
      # Append additional text to the in progress field.
      to_append = line.strip()
      if to_append:
        current_text = field_helps.get(current_field, '')
        field_helps[current_field] = current_text + ' ' + to_append

  return field_helps


def GetRecursiveMessageSpec(message):
  """Gets the recursive representation of a message as a dictionary.

  Args:
    message: The apitools message.

  Returns:
    {str: object}, A recursive mapping of field name to its data.
  """
  field_helps = FieldHelpDocs(message)
  data = {}
  for field in message.all_fields():
    field_data = {'description': field_helps.get(field.name)}
    field_data['repeated'] = field.repeated
    if field.variant == messages.Variant.MESSAGE:
      field_data['type'] = field.type.__name__
      field_data['fields'] = GetRecursiveMessageSpec(field.type)
    else:
      field_data['type'] = field.variant
      if field.variant == messages.Variant.ENUM:
        enum_help = FieldHelpDocs(field.type, 'Values')
        field_data['choices'] = {n: enum_help.get(n)
                                 for n in field.type.names()}

    data[field.name] = field_data
  return data


def IsOutputField(help_text):
  """Determines if the given field is output only based on help text."""
  return help_text and (
      help_text.startswith('[Output Only]') or
      help_text.endswith('@OutputOnly'))


class ChoiceEnumMapper(object):
  """Utility class for mapping apitools Enum messages to argparse choice args.

  Dynamically builds a base.Argument from an enum message.
  Derives choice values from supplied enum or an optional custom_mapping dict
  (see below).

  Class Attributes:
   choices: Either a list of strings [str] specifying the commandline choice
       values or an ordered dict of choice value to choice help string mappings
       {str -> str}
   enum: underlying enum whos values map to supplied choices.
   choice_arg: base.Argument object
   choice_mappings: Mapping of argparse choice value strings to enum values.
   custom_mappings: Optional dict mapping enum values to a custom
     argparse choice value. To maintain compatiblity with base.ChoiceAgrument(),
     dict can be either:
     {str-> str} - Enum String value to choice argument value i.e.
     {'MY_MUCH_LONGER_ENUM_VALUE':'short-arg'}
     OR
     {str -> (str, str)} -  Enum string value to  tuple of
     (choice argument value, choice help string) i.e.
     {'MY_MUCH_LONGER_ENUM_VALUE':('short-arg','My short arg help text.')}
  """
  _CUSTOM_MAPPING_ERROR = ('custom_mappings must be a dict of enum string '
                           'values to argparse argument choices. Choices must '
                           'be either a string or a string tuple of (choice, '
                           'choice_help_text): [{}]')

  def __init__(self,
               arg_name,
               message_enum,
               custom_mappings=None,
               help_str=None,
               required=False,
               action=None,
               metavar=None,
               dest=None,
               default=None,
               hidden=False,
               include_filter=None):
    """Initialize ChoiceEnumMapper.

    Args:
      arg_name: str, The name of the argparse argument to create
      message_enum: apitools.Enum, the enum to map
      custom_mappings: See Above.
      help_str: string, pass through for base.Argument,
        see base.ChoiceArgument().
      required: boolean,string, pass through for base.Argument,
          see base.ChoiceArgument().
      action: string or argparse.Action, string, pass through for base.Argument,
          see base.ChoiceArgument().
      metavar: string,  string, pass through for base.Argument,
          see base.ChoiceArgument()..
      dest: string, string, pass through for base.Argument,
          see base.ChoiceArgument().
      default: string, string, pass through for base.Argument,
          see base.ChoiceArgument().
      hidden: boolean, pass through for base.Argument,
          see base.ChoiceArgument().
      include_filter: callable, function of type string->bool used to filter
          enum values from message_enum that should be included in choices.
          If include_filter returns True for a particular enum value, it will be
          included otherwise it will be excluded. This is ignored if
          custom_mappings is specified.

    Raises:
      ValueError: If no enum is given, mappings are incomplete
      TypeError: If invalid values are passed for base.Argument or
       custom_mapping
    """
    # pylint:disable=protected-access
    if not isinstance(message_enum, messages._EnumClass):
      raise ValueError('Invalid Message Enum: [{}]'.format(message_enum))
    self._arg_name = arg_name
    self._enum = message_enum
    self._custom_mappings = custom_mappings
    if include_filter is not None and not callable(include_filter):
      raise TypeError('include_filter must be callable received [{}]'.format(
          include_filter))

    self._filter = include_filter
    self._filtered_enum = self._enum
    self._ValidateAndParseMappings()
    self._choice_arg = base.ChoiceArgument(
        arg_name,
        self.choices,
        help_str=help_str,
        required=required,
        action=action,
        metavar=metavar,
        dest=dest,
        default=default,
        hidden=hidden)

  def _ValidateAndParseMappings(self):
    """Validates and parses choice to enum mappings.

    Validates and parses choice to enum mappings including any custom mappings.

    Raises:
      ValueError: custom_mappings does not contain correct number of mapped
        values.
      TypeError: custom_mappings is incorrect type or contains incorrect types
        for mapped values.
    """
    if self._custom_mappings:  # Process Custom Mappings
      if not isinstance(self._custom_mappings, dict):
        raise TypeError(
            self._CUSTOM_MAPPING_ERROR.format(self._custom_mappings))
      enum_strings = set([x.name for x in self._enum])
      diff = set(self._custom_mappings.keys()) - enum_strings
      if diff:
        raise ValueError('custom_mappings [{}] may only contain mappings'
                         ' for enum values. invalid values:[{}]'.format(
                             ', '.join(self._custom_mappings.keys()),
                             ', '.join(diff)))
      try:
        self._ParseCustomMappingsFromTuples()
      except (TypeError, ValueError):
        self._ParseCustomMappingsFromStrings()

    else:  # No Custom Mappings so do automagic mapping
      if callable(self._filter):
        self._filtered_enum = [
            e for e in self._enum if self._filter(e.name)
        ]

      self._choice_to_enum = {
          EnumNameToChoice(x.name): x
          for x in self._filtered_enum
      }
      self._enum_to_choice = {
          y.name: x
          for x, y in six.iteritems(self._choice_to_enum)
      }
      self._choices = sorted(self._choice_to_enum.keys())

  def _ParseCustomMappingsFromTuples(self):
    """Parses choice to enum mappings from custom_mapping with tuples.

     Parses choice mappings from dict mapping Enum strings to a tuple of
     choice values and choice help {str -> (str, str)} mapping.

    Raises:
      TypeError - Custom choices are not not valid (str,str) tuples.
    """
    self._choice_to_enum = {}
    self._enum_to_choice = {}
    self._choices = OrderedDict()
    for enum_string, (choice, help_str) in sorted(
        six.iteritems(self._custom_mappings)):
      self._choice_to_enum[choice] = self._enum(enum_string)
      self._enum_to_choice[enum_string] = choice
      self._choices[choice] = help_str

  def _ParseCustomMappingsFromStrings(self):
    """Parses choice to enum mappings from custom_mapping with strings.

     Parses choice mappings from dict mapping Enum strings to choice
     values {str -> str} mapping.

    Raises:
      TypeError - Custom choices are not strings
    """
    self._choice_to_enum = {}
    self._choices = []

    for enum_string, choice_string in sorted(
        six.iteritems(self._custom_mappings)):
      if not isinstance(choice_string, six.string_types):
        raise TypeError(
            self._CUSTOM_MAPPING_ERROR.format(self._custom_mappings))
      self._choice_to_enum[choice_string] = self._enum(enum_string)
      self._choices.append(choice_string)
    self._enum_to_choice = self._custom_mappings

  def GetChoiceForEnum(self, enum_value):
    """Converts an enum value to a choice argument value."""
    return self._enum_to_choice.get(str(enum_value))

  def GetEnumForChoice(self, choice_value):
    """Converts a mapped string choice value to an enum."""
    return self._choice_to_enum.get(choice_value)

  @property
  def choices(self):
    return self._choices

  @property
  def enum(self):
    return self._enum

  @property
  def filtered_enum(self):
    return self._filtered_enum

  @property
  def choice_arg(self):
    return self._choice_arg

  @property
  def choice_mappings(self):
    return self._choice_to_enum

  @property
  def custom_mappings(self):
    return self._custom_mappings

  @property
  def include_filter(self):
    return self._filter
