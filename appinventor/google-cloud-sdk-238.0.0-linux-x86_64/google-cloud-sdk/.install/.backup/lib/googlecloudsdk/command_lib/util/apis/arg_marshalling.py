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

"""Classes that generate and parse arguments for apitools messages."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.protorpclite import messages
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope.concepts import concepts
from googlecloudsdk.calliope.concepts import multitype
from googlecloudsdk.command_lib.util.apis import arg_utils
from googlecloudsdk.command_lib.util.apis import yaml_command_schema
from googlecloudsdk.command_lib.util.concepts import concept_parsers
from googlecloudsdk.command_lib.util.concepts import presentation_specs
from googlecloudsdk.core import resources
from googlecloudsdk.core.resource import resource_property
import six


class Error(Exception):
  """Base class for this module's exceptions."""
  pass


class DeclarativeArgumentGenerator(object):
  """An argument generator that operates off a declarative configuration.

  When using this generator, you must provide attributes for the arguments that
  should be generated. All resource arguments must be provided and arguments
  will only be generated for API fields for which attributes were provided.
  """

  def __init__(self, method, arg_info, resource_arg):
    """Creates a new Argument Generator.

    Args:
      method: APIMethod, The method to generate arguments for.
      arg_info: [yaml_command_schema.Argument], Information about
        request fields and how to map them into arguments.
      resource_arg: resource_arg_schema.YAMLResourceArgument, The spec for
        the primary resource arg.
    """
    self.method = method
    self.arg_info = arg_info
    self.resource_arg = resource_arg
    self.resource_spec = self.resource_arg.GenerateResourceSpec(
        self.method.resource_argument_collection) if self.resource_arg else None

  def GenerateArgs(self):
    """Generates all the CLI arguments required to call this method.

    Returns:
      {str, calliope.base.Action}, A map of field name to the argument.
    """
    args = []
    args.extend(self._GenerateArguments())
    args.extend(self._GenerateResourceArg())
    return args

  def CreateRequest(self,
                    namespace,
                    static_fields=None,
                    resource_method_params=None,
                    use_relative_name=True,
                    override_method=None,
                    parse_resource_into_request=True,
                    existing_message=None):
    """Generates the request object for the method call from the parsed args.

    Args:
      namespace: The argparse namespace.
      static_fields: {str, value}, A mapping of API field name to value to
        insert into the message. This is a convenient way to insert extra data
        while the request is being constructed for fields that don't have
        corresponding arguments.
      resource_method_params: {str: str}, A mapping of API method parameter name
        to resource ref attribute name when the API method uses non-standard
        names.
      use_relative_name: Use ref.RelativeName() if True otherwise ref.Name().
      override_method: APIMethod, The method other than self.method, this is
        used when the command has more than one API call.
      parse_resource_into_request: bool, True if the resource reference should
        be automatically parsed into the request.
      existing_message: the apitools message returned from server, which is used
        to construct the to-be-modified message when the command follows
        get-modify-update pattern.

    Returns:
      The apitools message to be send to the method.
    """
    message_type = (override_method or self.method).GetRequestType()
    message = message_type()

    # If an apitools message is provided, use the existing one by default
    # instead of creating an empty one.
    if existing_message:
      message = arg_utils.ParseExistingMessageIntoMessage(
          message, existing_message, self.method)

    # Insert static fields into message.
    arg_utils.ParseStaticFieldsIntoMessage(message, static_fields=static_fields)

    # Parse api Fields into message.
    self._ParseArguments(message, namespace)

    ref = self._ParseResourceArg(namespace)
    if not ref:
      return message

    # For each method path field, get the value from the resource reference.
    if parse_resource_into_request:
      arg_utils.ParseResourceIntoMessage(
          ref, self.method, message,
          resource_method_params=resource_method_params,
          request_id_field=self.resource_arg.request_id_field,
          use_relative_name=use_relative_name)

    return message

  def GetRequestResourceRef(self, namespace):
    """Gets a resource reference for the resource being operated on.

    Args:
      namespace: The argparse namespace.

    Returns:
      resources.Resource, The parsed resource reference.
    """
    return self._ParseResourceArg(namespace)

  def GetResponseResourceRef(self, id_value, namespace):
    """Gets a resource reference for a resource returned by a list call.

    It parses the namespace to find a reference to the parent collection and
    then creates a reference to the child resource with the given id_value.

    Args:
      id_value: str, The id of the child resource that was returned.
      namespace: The argparse namespace.

    Returns:
      resources.Resource, The parsed resource reference.
    """
    parent_ref = self.GetRequestResourceRef(namespace)
    return resources.REGISTRY.Parse(
        id_value,
        collection=self.method.collection.full_name,
        params=parent_ref.AsDict())

  def Limit(self, namespace):
    """Gets the value of the limit flag (if present)."""
    return arg_utils.Limit(self.method, namespace)

  def PageSize(self, namespace):
    """Gets the value of the page size flag (if present)."""
    return arg_utils.PageSize(self.method, namespace)

  def _GenerateArguments(self):
    """Generates the arguments for the API fields of this method."""
    message = self.method.GetRequestType()
    return [arg.Generate(message) for arg in self.arg_info]

  def _GetAnchorArgName(self):
    """Get the anchor argument name for the resource spec."""
    if self.resource_arg.name_override:
      flag_name = self.resource_arg.name_override
    elif hasattr(self.resource_spec, 'anchor'):
      flag_name = self.resource_spec.anchor.name
    else:
      flag_name = self.resource_arg.name or self.resource_spec.name

    anchor_arg_is_flag = (
        not self.resource_arg.is_positional or self.method.IsList())
    anchor_arg_name = (
        '--' + flag_name if anchor_arg_is_flag
        else flag_name)
    return anchor_arg_name

  def _GenerateResourceArg(self):
    """Generates the flags to add to the parser that appear in the method path.

    Returns:
      {str, calliope.base.Argument}, A map of field name to argument.
    """
    if not self.resource_arg:
      return []

    # The anchor arg is positional unless explicitly overridden by the
    # attributes or for list commands (where everything should be a flag since
    # the parent resource collection is being used).
    anchor_arg_name = self._GetAnchorArgName()
    no_gen = {
        n: ''
        for _, n in six.iteritems(concepts.IGNORED_FIELDS)
        if n in self.resource_arg.attribute_names
    }
    no_gen.update({n: '' for n in self.resource_arg.removed_flags})
    command_level_fallthroughs = {}
    concept_parsers.UpdateFallthroughsMap(
        command_level_fallthroughs,
        anchor_arg_name,
        self.resource_arg.command_level_fallthroughs)
    presentation_spec_class = presentation_specs.ResourcePresentationSpec
    if isinstance(self.resource_spec, multitype.MultitypeResourceSpec):
      presentation_spec_class = (
          presentation_specs.MultitypeResourcePresentationSpec)
    concept = concept_parsers.ConceptParser(
        [presentation_spec_class(
            anchor_arg_name,
            self.resource_spec,
            self.resource_arg.group_help,
            prefixes=False,
            required=True,
            flag_name_overrides=no_gen)],
        command_level_fallthroughs=command_level_fallthroughs)
    return [concept]

  def _ParseArguments(self, message, namespace):
    """Parse all the arguments from the namespace into the message object.

    Args:
      message: A constructed apitools message object to inject the value into.
      namespace: The argparse namespace.
    """
    for arg in self.arg_info:
      arg.Parse(message, namespace)

  def _ParseResourceArg(self, namespace):
    """Gets the resource ref for the resource specified as the positional arg.

    Args:
      namespace: The argparse namespace.

    Returns:
      The parsed resource ref or None if no resource arg was generated for this
      method.
    """
    if not self.resource_arg:
      return

    result = arg_utils.GetFromNamespace(
        namespace.CONCEPTS, self._GetAnchorArgName()).Parse()
    if isinstance(result, multitype.TypedConceptResult):
      result = result.result
    return result


class AutoArgumentGenerator(object):
  """An argument generator to generate arguments for all fields in a message.

  When using this generator, you don't provide any manual configuration for
  arguments, it is all done automatically based on the request messages.

  There are two modes for this generator. In 'raw' mode, no modifications are
  done at all to the generated fields. In normal mode, certain list fields are
  not generated and instead our global list flags are used (and orchestrate
  the proper API fields automatically). In both cases, we generate additional
  resource arguments for path parameters.
  """
  FLAT_RESOURCE_ARG_NAME = 'resource'
  IGNORABLE_LIST_FIELDS = {'filter', 'pageToken', 'orderBy'}

  def __init__(self, method, raw=False):
    """Creates a new Argument Generator.

    Args:
      method: APIMethod, The method to generate arguments for.
      raw: bool, True to do no special processing of arguments for list
        commands. If False, typical List command flags will be added in and the
        equivalent API fields will be ignored.
    """
    self.method = method
    self.raw = raw
    self.is_atomic = self.method.detailed_params != self.method.params

    self.ignored_fields = set()
    if not raw and self.method.IsPageableList():
      self.ignored_fields |= AutoArgumentGenerator.IGNORABLE_LIST_FIELDS
      batch_page_size_field = self.method.BatchPageSizeField()
      if batch_page_size_field:
        self.ignored_fields.add(batch_page_size_field)

  def GenerateArgs(self):
    """Generates all the CLI arguments required to call this method.

    Returns:
      {str, calliope.base.Action}, A map of field name to the argument.
    """
    seen = set()
    args = []

    def _UpdateArgs(arguments):
      """Update args."""
      for arg in arguments:
        try:
          name = arg.name
        except IndexError:
          # An argument group does not have a name.
          pass
        else:
          if name in seen:
            continue
          seen.add(name)
        args.append(arg)

    # NOTICE: The call order is significant. Duplicate arg names are possible.
    # The first of the duplicate args entered wins.
    _UpdateArgs(self._GenerateResourceArg())
    _UpdateArgs(self._GenerateArguments('', self.method.GetRequestType()))
    _UpdateArgs(self._GenerateListMethodFlags())

    return args

  def CreateRequest(self, namespace):
    """Generates the request object for the method call from the parsed args.

    Args:
      namespace: The argparse namespace.

    Returns:
      The apitools message to be send to the method.
    """
    request_type = self.method.GetRequestType()
    # Recursively create the message and sub-messages.
    fields = self._ParseArguments(namespace, '', request_type)

    # For each actual method path field, add the attribute to the request.
    ref = self._ParseResourceArg(namespace)
    if ref:
      relative_name = ref.RelativeName()
      fields.update({f: getattr(ref, f, relative_name)
                     for f in self.method.params})
    return request_type(**fields)

  def Limit(self, namespace):
    """Gets the value of the limit flag (if present)."""
    if not self.raw:
      return arg_utils.Limit(self.method, namespace)

  def PageSize(self, namespace):
    """Gets the value of the page size flag (if present)."""
    if not self.raw:
      return arg_utils.PageSize(self.method, namespace)

  def _GenerateListMethodFlags(self):
    """Generates all the CLI flags for a List command.

    Returns:
      {str, calliope.base.Action}, A map of field name to the argument.
    """
    flags = []
    if not self.raw and self.method.IsList():
      flags.append(base.FILTER_FLAG)
      flags.append(base.SORT_BY_FLAG)
      if self.method.IsPageableList() and self.method.ListItemField():
        # We can use YieldFromList() with a limit.
        flags.append(base.LIMIT_FLAG)
        if self.method.BatchPageSizeField():
          # API supports page size.
          flags.append(base.PAGE_SIZE_FLAG)
    return flags

  def _GenerateArguments(self, prefix, message):
    """Gets the arguments to add to the parser that appear in the method body.

    Args:
      prefix: str, A string to prepend to the name of the flag. This is used
        for flags representing fields of a submessage.
      message: The apitools message to generate the flags for.

    Returns:
      {str, calliope.base.Argument}, A map of field name to argument.
    """
    args = []
    field_helps = arg_utils.FieldHelpDocs(message)
    for field in message.all_fields():
      field_help = field_helps.get(field.name, None)
      name = self._GetArgName(field.name, field_help)
      if not name:
        continue
      name = prefix + name
      if field.variant == messages.Variant.MESSAGE:
        sub_args = self._GenerateArguments(name + '.', field.type)
        if sub_args:
          help_text = (name + ': ' + field_help) if field_help else ''
          group = base.ArgumentGroup(help=help_text)
          args.append(group)
          for arg in sub_args:
            group.AddArgument(arg)
      else:
        attributes = yaml_command_schema.Argument(name, name, field_help)
        arg = arg_utils.GenerateFlag(field, attributes, fix_bools=False,
                                     category='MESSAGE')
        if not arg.kwargs.get('help'):
          arg.kwargs['help'] = 'API doc needs help for field [{}].'.format(name)
        args.append(arg)
    return args

  def _GenerateResourceArg(self):
    """Gets the flags to add to the parser that appear in the method path.

    Returns:
      {str, calliope.base.Argument}, A map of field name to argument.
    """
    args = []
    field_names = (self.method.request_collection.detailed_params
                   if self.method.request_collection else None)
    if not field_names:
      return args
    field_helps = arg_utils.FieldHelpDocs(self.method.GetRequestType())
    default_help = 'For substitution into: ' + self.method.detailed_path

    # Make a dedicated positional in addition to the flags for each part of
    # the URI path.
    arg = base.Argument(
        AutoArgumentGenerator.FLAT_RESOURCE_ARG_NAME,
        nargs='?',
        help='The GRI for the resource being operated on.')
    args.append(arg)

    for field in field_names:
      arg = base.Argument(
          '--' + field,
          metavar=resource_property.ConvertToAngrySnakeCase(field),
          category='RESOURCE',
          help=field_helps.get(field, default_help))
      args.append(arg)
    return args

  def _ParseArguments(self, namespace, prefix, message):
    """Recursively generates the request message and any sub-messages.

    Args:
      namespace: The argparse namespace containing the all the parsed arguments.
      prefix: str, The flag prefix for the sub-message being generated.
      message: The apitools class for the message.

    Returns:
      The instantiated apitools Message with all fields filled in from flags.
    """
    kwargs = {}
    for field in message.all_fields():
      arg_name = self._GetArgName(field.name)
      if not arg_name:
        continue
      arg_name = prefix + arg_name
      # Field is a sub-message, recursively generate it.
      if field.variant == messages.Variant.MESSAGE:
        sub_kwargs = self._ParseArguments(namespace, arg_name + '.', field.type)
        if sub_kwargs:
          # Only construct the sub-message if we have something to put in it.
          value = field.type(**sub_kwargs)
          # TODO(b/38000796): Handle repeated fields correctly.
          kwargs[field.name] = value if not field.repeated else [value]
      # Field is a scalar, just get the value.
      else:
        value = arg_utils.GetFromNamespace(namespace, arg_name)
        if value is not None:
          kwargs[field.name] = arg_utils.ConvertValue(field, value)
    return kwargs

  def _ParseResourceArg(self, namespace):
    """Gets the resource ref for the resource specified as the positional arg.

    Args:
      namespace: The argparse namespace.

    Returns:
      The parsed resource ref or None if no resource arg was generated for this
      method.
    """
    field_names = (self.method.request_collection.detailed_params
                   if self.method.request_collection else None)
    if not field_names:
      return
    r = getattr(namespace, AutoArgumentGenerator.FLAT_RESOURCE_ARG_NAME)

    params = {}
    defaults = {}
    for f in field_names:
      value = getattr(namespace, f)
      if value:
        params[f] = value
      else:
        default = arg_utils.DEFAULT_PARAMS.get(f, lambda: None)()
        if default:
          defaults[f] = default

    if not r and not params and len(defaults) < len(field_names):
      # No values were explicitly given and there are not enough defaults for
      # the parse to work.
      return None

    defaults.update(params)
    return resources.REGISTRY.Parse(
        r, collection=self.method.request_collection.full_name,
        params=defaults)

  def _GetArgName(self, field_name, field_help=None):
    """Gets the name of the argument to generate for the field.

    Args:
      field_name: str, The name of the field.
      field_help: str, The help for the field in the API docs.

    Returns:
      str, The name of the argument to generate, or None if this field is output
      only or should be ignored.
    """
    if field_help and arg_utils.IsOutputField(field_help):
      return None
    if field_name in self.ignored_fields:
      return None
    if (field_name == self.method.request_field and
        field_name.lower().endswith('request')):
      return 'request'
    return field_name
