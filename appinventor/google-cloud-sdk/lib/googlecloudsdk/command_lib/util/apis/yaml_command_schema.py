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

from enum import Enum

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.util.apis import arg_utils
from googlecloudsdk.command_lib.util.apis import resource_arg_schema
from googlecloudsdk.command_lib.util.apis import yaml_command_schema_util as util


NAME_FORMAT_KEY = '__name__'
REL_NAME_FORMAT_KEY = '__relative_name__'
RESOURCE_TYPE_FORMAT_KEY = '__resource_type__'


class CommandData(object):
  """A general holder object for yaml command schema."""

  def __init__(self, name, data):
    self.is_hidden = data.get('is_hidden', False)
    self.release_tracks = [
        base.ReleaseTrack.FromId(i) for i in data.get('release_tracks', [])]
    self.command_type = CommandType.ForName(data.get('command_type', name))
    self.help_text = data['help_text']
    self.request = Request(self.command_type, data['request'])
    self.response = Response(data.get('response', {}))
    async_data = data.get('async')
    iam_data = data.get('iam')
    update_data = data.get('update')
    if self.command_type == CommandType.WAIT and not async_data:
      raise util.InvalidSchemaError(
          'Wait commands must include an async section.')
    self.async = Async(async_data) if async_data else None
    self.iam = IamData(iam_data) if iam_data else None
    self.arguments = Arguments(data['arguments'])
    self.input = Input(self.command_type, data.get('input', {}))
    self.output = Output(data.get('output', {}))
    self.update = UpdateData(update_data) if update_data else None


class CommandType(Enum):
  """An enum for the types of commands the generator supports.

  Attributes:
    default_method: str, The name of the API method to use by default for this
      type of command.
  """
  DESCRIBE = 'get'
  LIST = 'list'
  DELETE = 'delete'
  CREATE = 'create'
  WAIT = 'get'
  UPDATE = 'patch'
  # IAM support currently implemented as subcommands
  GET_IAM_POLICY = 'getIamPolicy'
  SET_IAM_POLICY = 'setIamPolicy'
  # For add/remove-iam-policy-binding commands, the actual API method to modify
  # the iam support is 'setIamPolicy'.
  ADD_IAM_POLICY_BINDING = 'setIamPolicy'
  REMOVE_IAM_POLICY_BINDING = 'setIamPolicy'
  # Generic commands are those that don't extend a specific calliope command
  # base class.
  GENERIC = None

  def __init__(self, default_method):
    # Set the value to a unique object so multiple enums can have the same
    # default method.
    self._value_ = object()
    self.default_method = default_method

  @classmethod
  def ForName(cls, name):
    try:
      return CommandType[name.upper()]  # pytype: disable=not-indexable
    except KeyError:
      return CommandType.GENERIC


class Request(object):
  """A holder object for api request information specified in yaml command."""

  def __init__(self, command_type, data):
    self.collection = data['collection']
    self.api_version = data.get('api_version')
    self.method = data.get('method', command_type.default_method)
    if not self.method:
      raise util.InvalidSchemaError(
          'request.method was not specified and there is no default for this '
          'command type.')
    self.resource_method_params = data.get('resource_method_params', {})
    self.parse_resource_into_request = data.get(
        'parse_resource_into_request', True)
    self.static_fields = data.get('static_fields', {})
    self.modify_request_hooks = [
        util.Hook.FromPath(p) for p in data.get('modify_request_hooks', [])]
    self.create_request_hook = util.Hook.FromData(data, 'create_request_hook')
    self.issue_request_hook = util.Hook.FromData(data, 'issue_request_hook')
    self.use_relative_name = data.get('use_relative_name', True)


class Response(object):
  """A holder object for api response information specified in yaml command."""

  def __init__(self, data):
    self.id_field = data.get('id_field')
    self.result_attribute = data.get('result_attribute')
    self.error = ResponseError(data['error']) if 'error' in data else None
    self.modify_response_hooks = [
        util.Hook.FromPath(p) for p in data.get('modify_response_hooks', [])]


class ResponseError(object):

  def __init__(self, data):
    self.field = data.get('field', 'error')
    self.code = data.get('code')
    self.message = data.get('message')


class Async(object):
  """A holder object for api async information specified in yaml command."""

  def __init__(self, data):
    self.collection = data['collection']
    self.api_version = data.get('api_version')
    self.method = data.get('method', 'get')
    self.response_name_field = data.get('response_name_field', 'name')
    self.extract_resource_result = data.get('extract_resource_result', True)
    resource_get_method = data.get('resource_get_method')
    if not self.extract_resource_result and resource_get_method:
      raise util.InvalidSchemaError(
          'async.resource_get_method was specified but extract_resource_result '
          'is False')
    self.resource_get_method = resource_get_method or 'get'
    self.operation_get_method_params = data.get(
        'operation_get_method_params', {})
    self.result_attribute = data.get('result_attribute')
    self.state = AsyncStateField(data.get('state', {}))
    self.error = AsyncErrorField(data.get('error', {}))


class IamData(object):
  """A holder object for IAM related information specified in yaml command."""

  def __init__(self, data):
    self.message_type_overrides = data.get('message_type_overrides', {})
    self.set_iam_policy_request_path = data.get('set_iam_policy_request_path')
    self.enable_condition = data.get('enable_condition', False)


class AsyncStateField(object):

  def __init__(self, data):
    self.field = data.get('field', 'done')
    self.success_values = data.get('success_values', [True])
    self.error_values = data.get('error_values', [])


class AsyncErrorField(object):

  def __init__(self, data):
    self.field = data.get('field', 'error')


class Arguments(object):
  """Everything about cli arguments are registered in this section."""

  def __init__(self, data):
    self.resource = resource_arg_schema.YAMLConceptArgument.FromData(
        data.get('resource'))
    self.additional_arguments_hook = util.Hook.FromData(
        data, 'additional_arguments_hook')
    self.params = [
        Argument.FromData(param_data) for param_data in data.get('params', [])]


class Argument(object):
  """Encapsulates data used to generate arguments.

  Most of the attributes of this object correspond directly to the schema and
  have more complete docs there.

  Attributes:
    api_field: The name of the field in the request that this argument values
      goes.
    arg_name: The name of the argument that will be generated. Defaults to the
      api_field if not set.
    help_text: The help text for the generated argument.
    metavar: The metavar for the generated argument. This will be generated
      automatically if not provided.
    completer: A completer for this argument.
    is_positional: Whether to make the argument positional or a flag.
    type: The type to use on the argparse argument.
    choices: A static map of choice to value the user types.
    default: The default for the argument.
    fallback: A function to call and use as the default for the argument.
    processor: A function to call to process the value of the argument before
      inserting it into the request.
    required: True to make this a required flag.
    hidden: True to make the argument hidden.
    action: An override for the argparse action to use for this argument.
    repeated: False to accept only one value when the request field is actually
      repeated.
    generate: False to not generate this argument. This can be used to create
      placeholder arg specs for defaults that don't actually need to be
      generated.
  """

  @classmethod
  def FromData(cls, data):
    """Gets the arg definition from the spec data.

    Args:
      data: The spec data.

    Returns:
      Argument, the parsed argument.

    Raises:
      InvalidSchemaError: if the YAML command is malformed.
    """
    group = data.get('group')
    if group:
      return ArgumentGroup.FromData(group)

    api_field = data.get('api_field')
    arg_name = data.get('arg_name', api_field)
    if not arg_name:
      raise util.InvalidSchemaError(
          'An argument must have at least one of [api_field, arg_name].')
    is_positional = data.get('is_positional')
    flag_name = arg_name if is_positional else '--' + arg_name

    if data.get('default') and data.get('fallback'):
      raise util.InvalidSchemaError(
          'An argument may have at most one of [default, fallback].')

    try:
      help_text = data['help_text']
    except KeyError:
      raise util.InvalidSchemaError('An argument must have help_text.')

    choices = data.get('choices')

    return cls(
        api_field,
        arg_name,
        help_text,
        metavar=data.get('metavar'),
        completer=util.Hook.FromData(data, 'completer'),
        is_positional=is_positional,
        type=util.ParseType(data.get('type')),
        choices=[util.Choice(d) for d in choices] if choices else None,
        default=data.get('default', arg_utils.UNSPECIFIED),
        fallback=util.Hook.FromData(data, 'fallback'),
        processor=util.Hook.FromData(data, 'processor'),
        required=data.get('required', False),
        hidden=data.get('hidden', False),
        action=util.ParseAction(data.get('action'), flag_name),
        repeated=data.get('repeated'),
    )

  # pylint:disable=redefined-builtin, type param needs to match the schema.
  def __init__(self, api_field=None, arg_name=None, help_text=None,
               metavar=None, completer=None, is_positional=None, type=None,
               choices=None, default=arg_utils.UNSPECIFIED, fallback=None,
               processor=None, required=False, hidden=False, action=None,
               repeated=None, generate=True):
    self.api_field = api_field
    self.arg_name = arg_name
    self.help_text = help_text
    self.metavar = metavar
    self.completer = completer
    self.is_positional = is_positional
    self.type = type
    self.choices = choices
    self.default = default
    self.fallback = fallback
    self.processor = processor
    self.required = required
    self.hidden = hidden
    self.action = action
    self.repeated = repeated
    self.generate = generate

  def Generate(self, message):
    """Generates and returns the base argument.

    Args:
      message: The API message, None for non-resource args.

    Returns:
      The base argument.
    """
    if self.api_field:
      field = arg_utils.GetFieldFromMessage(message, self.api_field)
    else:
      field = None
    return arg_utils.GenerateFlag(field, self)

  def Parse(self, message, namespace):
    """Sets the argument message value, if any, from the parsed args.

    Args:
      message: The API message, None for non-resource args.
      namespace: The parsed command line argument namespace.
    """
    if self.api_field is None:
      return
    value = arg_utils.GetFromNamespace(
        namespace, self.arg_name, fallback=self.fallback)
    if value is None:
      return
    field = arg_utils.GetFieldFromMessage(message, self.api_field)
    value = arg_utils.ConvertValue(
        field, value, repeated=self.repeated, processor=self.processor,
        choices=util.Choice.ToChoiceMap(self.choices))
    arg_utils.SetFieldInMessage(message, self.api_field, value)


class ArgumentGroup(object):
  """Encapsulates data used to generate argument groups.

  Most of the attributes of this object correspond directly to the schema and
  have more complete docs there.

  Attributes:
    help_text: Optional help text for the group.
    required: True to make the group required.
    mutex: True to make the group mutually exclusive.
    hidden: True to make the group hidden.
    arguments: The list of arguments in the group.
  """

  @classmethod
  def FromData(cls, data):
    """Gets the arg group definition from the spec data.

    Args:
      data: The group spec data.

    Returns:
      ArgumentGroup, the parsed argument group.

    Raises:
      InvalidSchemaError: if the YAML command is malformed.
    """
    return cls(
        help_text=data.get('help_text'),
        required=data.get('required', False),
        mutex=data.get('mutex', False),
        hidden=data.get('hidden', False),
        arguments=[Argument.FromData(item) for item in data.get('params')],
    )

  def __init__(self, help_text=None, required=False, mutex=False, hidden=False,
               arguments=None):
    self.help_text = help_text
    self.required = required
    self.mutex = mutex
    self.hidden = hidden
    self.arguments = arguments

  def Generate(self, message):
    """Generates and returns the base argument group.

    Args:
      message: The API message, None for non-resource args.

    Returns:
      The base argument group.
    """
    group = base.ArgumentGroup(
        mutex=self.mutex, required=self.required, help=self.help_text)
    for arg in self.arguments:
      group.AddArgument(arg.Generate(message))
    return group

  def Parse(self, message, namespace):
    """Sets argument group message values, if any, from the parsed args.

    Args:
      message: The API message, None for non-resource args.
      namespace: The parsed command line argument namespace.
    """
    for arg in self.arguments:
      arg.Parse(message, namespace)


class Input(object):

  def __init__(self, command_type, data):
    self.confirmation_prompt = data.get('confirmation_prompt')
    if not self.confirmation_prompt and command_type is CommandType.DELETE:
      self.confirmation_prompt = (
          'You are about to delete {{{}}} [{{{}}}]'.format(
              RESOURCE_TYPE_FORMAT_KEY, NAME_FORMAT_KEY))


class Output(object):

  def __init__(self, data):
    self.format = data.get('format')


class UpdateData(object):
  """A holder object for yaml update command."""

  def __init__(self, data):
    self.mask_field = data.get('mask_field', None)
    self.read_modify_update = data.get('read_modify_update', False)
    self.disable_auto_field_mask = data.get('disable_auto_field_mask', False)
