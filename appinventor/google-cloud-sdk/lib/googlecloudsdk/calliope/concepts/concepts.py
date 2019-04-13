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

"""Classes to specify concept and resource specs.

Concept specs hold information about concepts. "Concepts" are any entity that
has multiple attributes, which can be specified via multiple flags on the
command line. A single concept spec should be created and re-used for the same
concept everywhere it appears.

Resource specs (currently the only type of concept spec used in gcloud) hold
information about a Cloud resource. "Resources" are types of concepts that
correspond to Cloud resources specified by a collection path, such as
'example.projects.shelves.books'. Their attributes correspond to the parameters
of their collection path. As with concept specs, a single resource spec
should be defined and re-used for each collection.

For resources, attributes can be configured by ResourceParameterAttributeConfigs
using kwargs. In many cases, users should also be able to reuse configs for the
same attribute across several resources (for example,
'example.projects.shelves.books.pages' could also use the shelf and project
attribute configs).
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import copy
import re

from googlecloudsdk.calliope.concepts import deps as deps_lib
from googlecloudsdk.command_lib.util.apis import registry
from googlecloudsdk.command_lib.util.apis import yaml_command_schema_util as util
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources
import six


IGNORED_FIELDS = {
    'project': 'project',
    'projectId': 'project',
    'projectsId': 'project',
}


class Error(exceptions.Error):
  """Base class for errors in this module."""


class InitializationError(Error):
  """Raised if a spec fails to initialize."""


class ResourceConfigurationError(Error):
  """Raised if a resource is improperly declared."""


class InvalidResourceArgumentLists(Error):
  """Exception for missing, extra, or out of order arguments."""

  def __init__(self, expected, actual):
    expected = ['[' + e + ']' if e in IGNORED_FIELDS else e for e in expected]
    super(InvalidResourceArgumentLists, self).__init__(
        'Invalid resource arguments: Expected [{}], Found [{}].'.format(
            ', '.join(expected), ', '.join(actual)))


class ConceptSpec(object):
  """Base class for concept args."""

  @property
  def attributes(self):
    """A list of Attribute objects representing the attributes of the concept.

    Must be defined in subclasses.
    """
    raise NotImplementedError

  @property
  def name(self):
    """The name of the overall concept.

    Must be defined in subclasses.
    """
    raise NotImplementedError

  def Initialize(self, deps):
    """Initializes the concept using information provided by a Deps object.

    Must be defined in subclasses.

    Args:
      deps: googlecloudsdk.calliope.concepts.deps.Deps object representing the
        fallthroughs for the concept's attributes.

    Returns:
      the initialized concept.

    Raises:
      InitializationError, if the concept cannot be initialized.
    """
    raise NotImplementedError

  def Parse(self, attribute_to_args_map, base_fallthroughs_map,
            parsed_args=None, plural=False, allow_empty=False):
    """Lazy parsing function for resource.

    Must be overridden in subclasses.

    Args:
      attribute_to_args_map: {str: str}, A map of attribute names to the names
        of their associated flags.
      base_fallthroughs_map: {str: [deps.Fallthrough]} A map of attribute
        names to non-argument fallthroughs, including command-level
        fallthroughs.
      parsed_args: the parsed Namespace.
      plural: bool, True if multiple resources can be parsed, False otherwise.
      allow_empty: bool, True if resource parsing is allowed to return no
        resource, otherwise False.

    Returns:
      the initialized resource or a list of initialized resources if the
        resource argument was pluralized.
    """
    raise NotImplementedError

  def __eq__(self, other):
    if not isinstance(other, type(self)):
      return False
    return self.name == other.name and self.attributes == other.attributes

  def __hash__(self):
    return hash(self.name) + hash(self.attributes)


class _Attribute(object):
  """A base class for concept attributes.

  Attributes:
    name: The name of the attribute. Used primarily to control the arg or flag
      name corresponding to the attribute. Must be in all lower case.
    help_text: String describing the attribute's relationship to the concept,
      used to generate help for an attribute flag.
    required: True if the attribute is required.
    fallthroughs: [googlecloudsdk.calliope.concepts.deps_lib.Fallthrough], the
      list of sources of data, in priority order, that can provide a value for
      the attribute if not given on the command line. These should only be
      sources inherent to the attribute, such as associated properties, not
      command-specific sources.
    completer: core.cache.completion_cache.Completer, the completer associated
      with the attribute.
    value_type: the type to be accepted by the attribute arg. Defaults to str.
  """

  def __init__(self, name, help_text=None, required=False, fallthroughs=None,
               completer=None, value_type=None):
    """Initializes."""
    # Check for attributes that mix lower- and uppercase. Camel case is not
    # handled consistently among libraries.
    if re.search(r'[A-Z]', name) and re.search('r[a-z]', name):
      raise ValueError(
          'Invalid attribute name [{}]: Attribute names should be in lower '
          'snake case (foo_bar) so they can be transformed to flag names.'
          .format(name))
    self.name = name
    self.help_text = help_text
    self.required = required
    self.fallthroughs = fallthroughs or []
    self.completer = completer
    self.value_type = value_type or six.text_type

  def __eq__(self, other):
    """Overrides."""
    if not isinstance(other, type(self)):
      return False
    return (self.name == other.name and self.help_text == other.help_text
            and self.required == other.required
            and self.completer == other.completer
            and self.fallthroughs == other.fallthroughs
            and self.value_type == other.value_type)

  def __hash__(self):
    return sum(map(hash, [
        self.name, self.help_text, self.required, self.completer,
        self.value_type])) + sum(map(hash, self.fallthroughs))


class Attribute(_Attribute):
  """An attribute of a resource.

  Has all attributes of the base class along with resource-specific attributes.

  Attributes:
    completion_request_params: {str: str}, a dict of field names to params to
      use as static field values in any request to complete this resource.
    completion_id_field: str, the ID field of the return value in the
        response for completion requests.
  """

  def __init__(self, name, completion_request_params=None,
               completion_id_field=None, **kwargs):
    """Initializes."""
    self.completion_request_params = completion_request_params or {}
    self.completion_id_field = completion_id_field
    super(Attribute, self).__init__(name, **kwargs)

  def __eq__(self, other):
    """Overrides."""
    return (super(Attribute, self).__eq__(other)
            and self.completion_request_params
            == other.completion_request_params
            and self.completion_id_field == other.completion_id_field)

  def __hash__(self):
    return super(Attribute, self).__hash__() + sum(
        map(hash, [str(self.completion_request_params),
                   self.completion_id_field]))


class ResourceSpec(ConceptSpec):
  """Defines a Cloud resource as a set of attributes for argument creation.
  """

  # TODO(b/78851830): update the documentation to use this method.
  @classmethod
  def FromYaml(cls, yaml_data, api_version=None):
    """Constructs an instance of ResourceSpec from yaml data.

    Args:
      yaml_data: dict, the parsed data from a resources.yaml file under
        command_lib/.
      api_version: string, overrides the default version in the resource
        registry if provided.

    Returns:
      A ResourceSpec object.
    """
    if not yaml_data:
      return None
    collection = registry.GetAPICollection(
        yaml_data['collection'], api_version=api_version)
    attributes = ParseAttributesFromData(
        yaml_data.get('attributes'), collection.detailed_params)
    return cls(
        resource_collection=collection.full_name,
        resource_name=yaml_data['name'],
        api_version=collection.api_version,
        disable_auto_completers=yaml_data['disable_auto_completers'],
        plural_name=yaml_data.get('plural_name'),
        **{attribute.parameter_name: attribute for attribute in attributes})

  # TODO(b/67707644): Enable completers by default when confident enough.
  def __init__(self, resource_collection, resource_name='resource',
               api_version=None, disable_auto_completers=True, plural_name=None,
               **kwargs):
    """Initializes a ResourceSpec.

    To use a ResourceSpec, give a collection path such as
    'cloudiot.projects.locations.registries', and optionally an
    API version.

    For each parameter in the collection path, an attribute is added to the
    resource spec. Names can be created by default or overridden in the
    attribute_configs dict, which maps from the parameter name to a
    ResourceParameterAttributeConfig object. ResourceParameterAttributeConfigs
    also contain information about the help text that describes the attribute.

    Attribute naming: By default, attributes are named after their collection
    path param names, or "name" if they are the "anchor" attribute (the final
    parameter in the path).

    Args:
      resource_collection: The collection path of the resource.
      resource_name: The name of the resource, which will be used in attribute
        help text. Defaults to 'resource'.
      api_version: Overrides the default version in the resource
        registry.
      disable_auto_completers: bool, whether to add completers automatically
        where possible.
      plural_name: str, the pluralized name. Will be pluralized by default rules
        if not given in cases where the resource is referred to in the plural.
      **kwargs: Parameter names (such as 'projectsId') from the
        collection path, mapped to ResourceParameterAttributeConfigs.

    Raises:
      ResourceConfigurationError: if the resource is given unknown params or the
        collection has no params.
    """
    self._name = resource_name
    self.plural_name = plural_name
    self.collection = resource_collection
    self._resources = resources.REGISTRY.Clone()
    self._collection_info = self._resources.GetCollectionInfo(
        resource_collection, api_version=api_version)
    self.disable_auto_completers = disable_auto_completers
    collection_params = self._collection_info.GetParams('')
    self._attributes = []
    self._param_names_map = {}

    # Add attributes.
    anchor = False
    for i, param_name in enumerate(collection_params):
      if i == len(collection_params) - 1:
        anchor = True
      attribute_config = kwargs.pop(param_name,
                                    ResourceParameterAttributeConfig())
      attribute_name = self._AttributeName(param_name, attribute_config,
                                           anchor=anchor)
      new_attribute = Attribute(
          name=attribute_name,
          help_text=attribute_config.help_text,
          required=True,
          fallthroughs=attribute_config.fallthroughs,
          completer=attribute_config.completer,
          value_type=attribute_config.value_type,
          completion_request_params=attribute_config.completion_request_params,
          completion_id_field=attribute_config.completion_id_field)
      self._attributes.append(new_attribute)
      # Keep a map from attribute names to param names. While attribute names
      # are used for error messaging and arg creation/parsing, resource parsing
      # during command runtime requires parameter names.
      self._param_names_map[new_attribute.name] = param_name
    if not self._attributes:
      raise ResourceConfigurationError('Resource [{}] has no parameters; no '
                                       'arguments will be generated'.format(
                                           self._name))
    if kwargs:
      raise ResourceConfigurationError('Resource [{}] was given an attribute '
                                       'config for unknown attribute(s): [{}]'
                                       .format(self._name,
                                               ', '.join(six.iterkeys(kwargs))))

  @property
  def attributes(self):
    return self._attributes

  @property
  def name(self):
    return self._name

  @property
  def anchor(self):
    """The "anchor" attribute of the resource."""
    # self.attributes cannot be empty; will cause an error on init.
    return self.attributes[-1]

  def IsAnchor(self, attribute):
    """Convenience method."""
    return attribute == self.anchor

  @property
  def attribute_to_params_map(self):
    """A map from all attribute names to param names."""
    return self._param_names_map

  @property
  def collection_info(self):
    return self._collection_info

  def _AttributeName(self, param_name, attribute_config, anchor=False):
    """Chooses attribute name for a param name.

    If attribute_config gives an attribute name, that is used. Otherwise, if the
    param is an anchor attribute, 'name' is used, or if not, param_name is used.

    Args:
      param_name: str, the parameter name from the collection.
      attribute_config: ResourceParameterAttributeConfig, the config for the
        param_name.
      anchor: bool, whether the parameter is the "anchor" or the last in the
        collection path.

    Returns:
      (str) the attribute name.
    """
    if attribute_config.attribute_name:
      return attribute_config.attribute_name
    if anchor:
      return 'name'
    return param_name.replace('Id', '_id').lower()

  def ParamName(self, attribute_name):
    """Given an attribute name, gets the param name for resource parsing."""
    if attribute_name not in self.attribute_to_params_map:
      raise ValueError(
          'No param name found for attribute [{}]. Existing attributes are '
          '[{}]'.format(attribute_name,
                        ', '.join(sorted(self.attribute_to_params_map.keys()))))
    return self.attribute_to_params_map[attribute_name]

  def AttributeName(self, param_name):
    """Given a param name, gets the attribute name."""
    for attribute_name, p in six.iteritems(self.attribute_to_params_map):
      if p == param_name:
        return attribute_name

  def Initialize(self, fallthroughs_map, parsed_args=None):
    """Initializes a resource given its fallthroughs.

    If the attributes have a property or arg fallthrough but the full
    resource name is provided to the anchor attribute flag, the information
    from the resource name is used over the properties and args. This
    preserves typical resource parsing behavior in existing surfaces.

    Args:
      fallthroughs_map: {str: [deps_lib._FallthroughBase]}, a dict of finalized
        fallthroughs for the resource.
      parsed_args: the argparse namespace.

    Returns:
      (googlecloudsdk.core.resources.Resource) the fully initialized resource.

    Raises:
      googlecloudsdk.calliope.concepts.concepts.InitializationError, if the
        concept can't be initialized.
    """
    params = {}

    # Returns a function that can be used to parse each attribute, which will be
    # used only if the resource parser does not receive a fully qualified
    # resource name.
    def LazyGet(name):
      f = lambda: deps_lib.Get(name, fallthroughs_map, parsed_args=parsed_args)
      return f

    for attribute in self.attributes:
      params[self.ParamName(attribute.name)] = LazyGet(attribute.name)
    self._resources.RegisterApiByName(self._collection_info.api_name,
                                      self._collection_info.api_version)
    try:
      return self._resources.Parse(
          deps_lib.Get(
              self.anchor.name, fallthroughs_map, parsed_args=parsed_args),
          collection=self.collection,
          params=params)
    except deps_lib.AttributeNotFoundError as e:
      raise InitializationError(
          'The [{}] resource is not properly specified.\n'
          '{}'.format(self.name, six.text_type(e)))

  def Parse(self, attribute_to_args_map, base_fallthroughs_map,
            parsed_args=None, plural=False, allow_empty=False):
    """Lazy parsing function for resource.

    Args:
      attribute_to_args_map: {str: str}, A map of attribute names to the names
        of their associated flags.
      base_fallthroughs_map: {str: [deps_lib.Fallthrough]} A map of attribute
        names to non-argument fallthroughs, including command-level
        fallthroughs.
      parsed_args: the parsed Namespace.
      plural: bool, True if multiple resources can be parsed, False otherwise.
      allow_empty: bool, True if resource parsing is allowed to return no
        resource, otherwise False.

    Returns:
      the initialized resource or a list of initialized resources if the
        resource argument was pluralized.
    """
    if not plural:
      fallthroughs_map = self.BuildFullFallthroughsMap(
          attribute_to_args_map, base_fallthroughs_map,
          with_anchor_fallthroughs=False)
      try:
        return self.Initialize(
            fallthroughs_map, parsed_args=parsed_args)
      except InitializationError:
        if allow_empty:
          return None
        raise

    results = self._ParseFromPluralValue(attribute_to_args_map,
                                         base_fallthroughs_map,
                                         self.anchor,
                                         parsed_args)
    if results:
      return results

    if allow_empty:
      return []
    fallthroughs_map = self.BuildFullFallthroughsMap(
        attribute_to_args_map, base_fallthroughs_map)
    return self.Initialize(
        base_fallthroughs_map, parsed_args=parsed_args)

  def _ParseFromPluralValue(self, attribute_to_args_map, base_fallthroughs_map,
                            plural_attribute, parsed_args):
    """Helper for parsing a list of results from a plural fallthrough."""
    attribute_name = plural_attribute.name
    fallthroughs_map = self.BuildFullFallthroughsMap(
        attribute_to_args_map, base_fallthroughs_map, plural=True,
        with_anchor_fallthroughs=False)
    current_fallthroughs = fallthroughs_map.get(attribute_name, [])
    # Iterate through the values provided to the argument, creating for
    # each a separate parsed resource.
    parsed_resources = []
    for fallthrough in current_fallthroughs:
      try:
        values = fallthrough.GetValue(parsed_args)
      except deps_lib.FallthroughNotFoundError:
        continue
      for value in values:
        def F(return_value=value):
          return return_value

        new_fallthrough = deps_lib.Fallthrough(
            F, fallthrough.hint, active=fallthrough.active)
        fallthroughs_map[attribute_name] = [new_fallthrough]
        # Add the anchor fallthroughs for this particular value, so that the
        # error messages will contain the appropriate hints.
        self._AddAnchorFallthroughs(plural_attribute, fallthroughs_map)
        parsed_resources.append(
            self.Initialize(
                fallthroughs_map, parsed_args=parsed_args))
      return parsed_resources

  def BuildFullFallthroughsMap(self, attribute_to_args_map,
                               base_fallthroughs_map, plural=False,
                               with_anchor_fallthroughs=True):
    """Builds map of all fallthroughs including arg names.

    Fallthroughs are a list of objects that, when called, try different ways of
    getting values for attributes (see googlecloudsdk.calliope.concepts.
    deps_lib._Fallthrough). This method builds a map from the name of each
    attribute to its fallthroughs, including the "primary" fallthrough
    representing its corresponding argument value in parsed_args if any, and any
    fallthroughs that were configured for the attribute beyond that.

    Args:
      attribute_to_args_map: {str: str}, A map of attribute names to the names
        of their associated flags.
      base_fallthroughs_map: {str: [deps_lib._FallthroughBase]}, A map of
        attribute names to non-argument fallthroughs, including command-level
        fallthroughs.
      plural: bool, True if multiple resources can be parsed, False otherwise.
      with_anchor_fallthroughs: bool, whether to add fully specified anchor
        fallthroughs. Used only for getting help text/error messages,
        and for determining which attributes are specified -- not for parsing.

    Returns:
      {str: [deps_lib._Fallthrough]}, a map from attribute name to its
      fallthroughs.
    """
    fallthroughs_map = {}
    for attribute in self.attributes:
      fallthroughs_map[attribute.name] = (
          self.GetArgAndBaseFallthroughsForAttribute(attribute_to_args_map,
                                                     base_fallthroughs_map,
                                                     attribute,
                                                     plural=plural))
    if not with_anchor_fallthroughs:
      return fallthroughs_map
    for attribute in self.attributes:
      if self.IsAnchor(attribute):
        self._AddAnchorFallthroughs(attribute, fallthroughs_map)
    return fallthroughs_map

  def GetArgAndBaseFallthroughsForAttribute(self,
                                            attribute_to_args_map,
                                            base_fallthroughs_map,
                                            attribute,
                                            plural=False):
    """Gets all fallthroughs for an attribute except anchor-dependent ones."""
    attribute_name = attribute.name
    attribute_fallthroughs = []
    # The only args that should be lists are anchor args for plural
    # resources.
    attribute_is_plural = self.IsAnchor(attribute) and plural

    # Start the fallthroughs list with the primary associated arg for the
    # attribute.
    arg_name = attribute_to_args_map.get(attribute_name)
    if arg_name:
      attribute_fallthroughs.append(
          deps_lib.ArgFallthrough(arg_name, plural=attribute_is_plural))

    given_fallthroughs = base_fallthroughs_map.get(attribute_name, [])
    for fallthrough in given_fallthroughs:
      if attribute_is_plural:
        fallthrough = copy.deepcopy(fallthrough)
        fallthrough.plural = attribute_is_plural
      attribute_fallthroughs.append(fallthrough)
    return attribute_fallthroughs

  def _GetAttributeAnchorFallthroughs(self, anchor_fallthroughs, attribute):
    """Helper to get anchor-depednent fallthroughs for a specific attribute."""
    parameter_name = self.ParamName(attribute.name)
    anchor_based_fallthroughs = [
        deps_lib.FullySpecifiedAnchorFallthrough(
            anchor_fallthrough, self.collection_info, parameter_name)
        for anchor_fallthrough in anchor_fallthroughs
    ]
    return anchor_based_fallthroughs

  def _AddAnchorFallthroughs(self, anchor, fallthroughs_map):
    """Helper for adding anchor fallthroughs to the fallthroughs map."""
    anchor_fallthroughs = fallthroughs_map.get(anchor.name, [])
    for attribute in self.attributes:
      if attribute == anchor:
        continue
      anchor_based_fallthroughs = self._GetAttributeAnchorFallthroughs(
          anchor_fallthroughs, attribute)
      fallthroughs_map[attribute.name] = (
          anchor_based_fallthroughs + fallthroughs_map[attribute.name])

  def __eq__(self, other):
    return (super(ResourceSpec, self).__eq__(other)
            and self.disable_auto_completers == other.disable_auto_completers
            and self.attribute_to_params_map == other.attribute_to_params_map)

  def __hash__(self):
    return super(ResourceSpec, self).__hash__() + sum(
        map(hash, [self.disable_auto_completers, self.attribute_to_params_map]))


class ResourceParameterAttributeConfig(object):
  """Configuration used to create attributes from resource parameters."""

  @classmethod
  def FromData(cls, data):
    """Constructs an attribute config from data defined in the yaml file.

    Args:
      data: {}, the dict of data from the YAML file for this single attribute.

    Returns:
      ResourceParameterAttributeConfig
    """
    if not data:
      return None

    attribute_name = data['attribute_name']
    parameter_name = data['parameter_name']
    help_text = data['help']
    completion_id_field = data.get('completion_id_field', None)
    completion_request_params_list = data.get('completion_request_params', [])
    completion_request_params = {
        param.get('fieldName'): param.get('value')
        for param in completion_request_params_list
    }

    # Add property fallthroughs.
    fallthroughs = []
    prop = properties.FromString(data.get('property', ''))
    if prop:
      fallthroughs.append(deps_lib.PropertyFallthrough(prop))
    default_config = DEFAULT_RESOURCE_ATTRIBUTE_CONFIGS.get(attribute_name)
    if default_config:
      fallthroughs += [
          f for f in default_config.fallthroughs if f not in fallthroughs]
    # Add fallthroughs from python hooks.
    fallthrough_data = data.get('fallthroughs', [])
    fallthroughs_from_hook = [
        deps_lib.Fallthrough(util.Hook.FromPath(f['hook']), hint=f['hint'])
        for f in fallthrough_data
    ]
    fallthroughs += fallthroughs_from_hook
    return cls(
        name=attribute_name,
        help_text=help_text,
        fallthroughs=fallthroughs,
        completion_id_field=completion_id_field,
        completion_request_params=completion_request_params,
        parameter_name=parameter_name)

  def __init__(self,
               name=None,
               help_text=None,
               fallthroughs=None,
               completer=None,
               completion_request_params=None,
               completion_id_field=None,
               value_type=None,
               parameter_name=None):
    """Create a resource attribute.

    Args:
      name: str, the name of the attribute. This controls the naming of flags
        based on the attribute.
      help_text: str, generic help text for any flag based on the attribute. One
        special expansion is available to convert "{resource}" to the name of
        the resource.
      fallthroughs: [deps_lib.Fallthrough], A list of fallthroughs to use to
        resolve the attribute if it is not provided on the command line.
      completer: core.cache.completion_cache.Completer, the completer
        associated with the attribute.
      completion_request_params: {str: value}, a dict of field names to static
        values to fill in for the completion request.
      completion_id_field: str, the ID field of the return value in the
        response for completion commands.
      value_type: the type to be accepted by the attribute arg. Defaults to str.
      parameter_name: the API parameter name that this attribute maps to.
    """
    self.attribute_name = name
    self.help_text = help_text
    self.fallthroughs = fallthroughs or []
    # The completer is always None because neither the surface nor the yaml
    # schema allow for specifying completers currently.
    self.completer = completer
    self.completion_request_params = completion_request_params
    self.completion_id_field = completion_id_field
    self.value_type = value_type or six.text_type
    self.parameter_name = parameter_name


def ParseAttributesFromData(attributes_data, expected_param_names):
  """Parses a list of ResourceParameterAttributeConfig from yaml data.

  Args:
    attributes_data: dict, the attributes data defined in
      command_lib/resources.yaml file.
    expected_param_names: [str], the names of the API parameters that the API
      method accepts. Example, ['projectsId', 'instancesId'].

  Returns:
    [ResourceParameterAttributeConfig].

  Raises:
    InvalidResourceArgumentLists: if the attributes defined in the yaml file
      don't match the expected fields in the API method.
  """
  raw_attributes = [
      ResourceParameterAttributeConfig.FromData(a) for a in attributes_data
  ]
  registered_param_names = [a.parameter_name for a in raw_attributes]
  final_attributes = []

  # TODO(b/78851830): improve the time complexity here.
  for expected_name in expected_param_names:
    if raw_attributes and expected_name == raw_attributes[0].parameter_name:
      # Attribute matches expected, add it and continue checking.
      final_attributes.append(raw_attributes.pop(0))
    elif expected_name in IGNORED_FIELDS:
      # Attribute doesn't match but is being ignored. Add an auto-generated
      # attribute as a substitute.
      # Currently, it would only be the project config.
      attribute_name = IGNORED_FIELDS[expected_name]
      ignored_attribute = DEFAULT_RESOURCE_ATTRIBUTE_CONFIGS.get(attribute_name)
      # Manually add the parameter name, e.g. project, projectId or projectsId.
      ignored_attribute.parameter_name = expected_name
      final_attributes.append(ignored_attribute)
    else:
      # It doesn't match (or there are no more registered params) and the
      # field is not being ignored, error.
      raise InvalidResourceArgumentLists(expected_param_names,
                                         registered_param_names)

  if raw_attributes:
    # All expected fields were processed but there are still registered
    # attribute params remaining, they must be extra.
    raise InvalidResourceArgumentLists(expected_param_names,
                                       registered_param_names)

  return final_attributes


DEFAULT_PROJECT_ATTRIBUTE_CONFIG = ResourceParameterAttributeConfig(
    name='project',
    help_text='The Cloud project for the {resource}.',
    fallthroughs=[
        # Typically argument fallthroughs should be configured at the command
        # level, but the --project flag is currently available in every command.
        deps_lib.ArgFallthrough('--project'),
        deps_lib.PropertyFallthrough(properties.VALUES.core.project)
    ])
DEFAULT_RESOURCE_ATTRIBUTE_CONFIGS = {
    'project': DEFAULT_PROJECT_ATTRIBUTE_CONFIG}
_DEFAULT_CONFIGS = {'project': DEFAULT_PROJECT_ATTRIBUTE_CONFIG}
