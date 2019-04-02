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

"""Helpers for loading resource argument definitions from a yaml declaration."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope.concepts import concepts
from googlecloudsdk.calliope.concepts import multitype
from googlecloudsdk.calliope.concepts import util as resource_util
from googlecloudsdk.command_lib.util.apis import registry
from googlecloudsdk.command_lib.util.apis import yaml_command_schema_util as util
import six


class YAMLConceptArgument(object):

  @classmethod
  def FromData(cls, data):
    if not data:
      return None
    if 'resources' in data['spec']:
      return YAMLMultitypeResourceArgument.FromData(data)
    return YAMLResourceArgument.FromData(data)


class YAMLResourceArgument(YAMLConceptArgument):
  """Encapsulates the spec for the resource arg of a declarative command."""

  @classmethod
  def FromData(cls, data):
    if not data:
      return None

    return cls(
        data['spec'],
        data['help_text'],
        is_positional=data.get('is_positional', True),
        is_parent_resource=data.get('is_parent_resource', False),
        removed_flags=data.get('removed_flags'),
        disable_auto_completers=data['spec'].get(
            'disable_auto_completers', True),
        arg_name=data.get('arg_name'),
        command_level_fallthroughs=data.get('command_level_fallthroughs', {}),
        display_name_hook=data.get('display_name_hook'),
        override_resource_collection=data.get('override_resource_collection',
                                              False)
    )

  @classmethod
  def FromSpecData(cls, data):
    """Create a resource argument with no command-level information configured.

    Given just the reusable resource specification (such as attribute names
    and fallthroughs, it can be used to generate a ResourceSpec. Not suitable
    for adding directly to a command as a solo argument.

    Args:
      data: the yaml resource definition.

    Returns:
      YAMLResourceArgument with no group help or flag name information.
    """
    if not data:
      return None

    return cls(data, None)

  def __init__(self, data, group_help, is_positional=True, removed_flags=None,
               is_parent_resource=False, disable_auto_completers=True,
               arg_name=None, command_level_fallthroughs=None,
               display_name_hook=None, override_resource_collection=False):
    self.name = data['name'] if arg_name is None else arg_name
    self.name_override = arg_name
    self.request_id_field = data.get('request_id_field')

    self.group_help = group_help
    self.is_positional = is_positional
    self.is_parent_resource = is_parent_resource
    self.removed_flags = removed_flags or []
    self.command_level_fallthroughs = _GenerateFallthroughsMap(
        command_level_fallthroughs)

    self._full_collection_name = data['collection']
    self._api_version = data.get('api_version')
    self._attribute_data = data['attributes']
    self._disable_auto_completers = disable_auto_completers
    self._plural_name = data.get('plural_name')
    self.display_name_hook = (
        util.Hook.FromPath(display_name_hook) if display_name_hook else None)
    self.override_resource_collection = override_resource_collection

    for removed in self.removed_flags:
      if removed not in self.attribute_names:
        raise util.InvalidSchemaError(
            'Removed flag [{}] for resource arg [{}] references an attribute '
            'that does not exist. Valid attributes are [{}]'.format(
                removed, self.name, ', '.join(self.attribute_names)))

  @property
  def attribute_names(self):
    return [a['attribute_name'] for a in self._attribute_data]

  def GenerateResourceSpec(self, resource_collection=None):
    """Creates a concept spec for the resource argument.

    Args:
      resource_collection: registry.APICollection, The collection that the
        resource arg must be for. This simply does some extra validation to
        ensure that resource arg is for the correct collection and api_version.
        If not specified, the resource arg will just be loaded based on the
        collection it specifies.

    Returns:
      concepts.ResourceSpec, The generated specification that can be added to
      a parser.
    """
    if self.is_parent_resource and resource_collection:
      parent_collection, _, _ = resource_collection.full_name.rpartition('.')
      resource_collection = registry.GetAPICollection(
          parent_collection, api_version=self._api_version)

    if resource_collection and not self.override_resource_collection:
      # Validate that the expected collection matches what was registered for
      # the resource argument specification.
      if resource_collection.full_name != self._full_collection_name:
        raise util.InvalidSchemaError(
            'Collection names do not match for resource argument specification '
            '[{}]. Expected [{}], found [{}]'
            .format(self.name, resource_collection.full_name,
                    self._full_collection_name))
      if (self._api_version and
          self._api_version != resource_collection.api_version):
        raise util.InvalidSchemaError(
            'API versions do not match for resource argument specification '
            '[{}]. Expected [{}], found [{}]'
            .format(self.name, resource_collection.api_version,
                    self._api_version))
    else:
      # No required collection, just load whatever the resource arg declared
      # for itself.
      resource_collection = registry.GetAPICollection(
          self._full_collection_name, api_version=self._api_version)

    attributes = concepts.ParseAttributesFromData(
        self._attribute_data, resource_collection.detailed_params)
    return concepts.ResourceSpec(
        resource_collection.full_name,
        resource_name=self.name,
        api_version=resource_collection.api_version,
        disable_auto_completers=self._disable_auto_completers,
        plural_name=self._plural_name,
        **{attribute.parameter_name: attribute for attribute in attributes})


def _GenerateFallthroughsMap(command_level_fallthroughs_data):
  """Generate a map of command-level fallthroughs."""
  command_level_fallthroughs_data = command_level_fallthroughs_data or {}
  command_level_fallthroughs = {}

  def _FallthroughStringFromData(fallthrough_data):
    if fallthrough_data.get('is_positional', False):
      return resource_util.PositionalFormat(fallthrough_data['arg_name'])
    return resource_util.FlagNameFormat(fallthrough_data['arg_name'])

  for attribute_name, fallthroughs_data in six.iteritems(
      command_level_fallthroughs_data):
    fallthroughs_list = [_FallthroughStringFromData(fallthrough)
                         for fallthrough in fallthroughs_data]
    command_level_fallthroughs[attribute_name] = fallthroughs_list

  return command_level_fallthroughs


class YAMLMultitypeResourceArgument(YAMLConceptArgument):
  """Encapsulates the spec for the resource arg of a declarative command."""

  @classmethod
  def FromData(cls, data):
    if not data:
      return None

    return cls(
        data['spec'],
        data['help_text'],
        is_positional=data.get('is_positional', True),
        is_parent_resource=data.get('is_parent_resource', False),
        removed_flags=data.get('removed_flags'),
        arg_name=data.get('arg_name'),
        command_level_fallthroughs=data.get('command_level_fallthroughs', {}),
        display_name_hook=data.get('display_name_hook')
    )

  def __init__(self, data, group_help, is_positional=True, removed_flags=None,
               is_parent_resource=False, disable_auto_completers=True,
               arg_name=None, command_level_fallthroughs=None,
               display_name_hook=None):
    self.name = data['name'] if arg_name is None else arg_name
    self.name_override = arg_name
    self.request_id_field = data.get('request_id_field')

    self.group_help = group_help
    self.is_positional = is_positional
    self.is_parent_resource = is_parent_resource
    self.removed_flags = removed_flags or []
    self.command_level_fallthroughs = _GenerateFallthroughsMap(
        command_level_fallthroughs)
    self._plural_name = data.get('plural_name')
    self._resources = data.get('resources') or []
    if not disable_auto_completers:
      raise ValueError('disable_auto_completers must be True for '
                       'multitype resource argument [{}]'.format(self.name))
    self.display_name_hook = (
        util.Hook.FromPath(display_name_hook) if display_name_hook else None)

  @property
  def attribute_names(self):
    attribute_names = []
    for sub_resource in self._resources:
      sub_resource_arg = YAMLResourceArgument.FromSpecData(sub_resource)
      for attribute_name in sub_resource_arg.attribute_names:
        if attribute_name not in attribute_names:
          attribute_names.append(attribute_name)
    return attribute_names

  def GenerateResourceSpec(self, resource_collection=None):
    """Creates a concept spec for the resource argument.

    Args:
      resource_collection: registry.APICollection, The collection that the
        resource arg must be for. This simply does some extra validation to
        ensure that resource arg is for the correct collection and api_version.
        If not specified, the resource arg will just be loaded based on the
        collection it specifies.

    Returns:
      multitype.MultitypeResourceSpec, The generated specification that can be
      added to a parser.
    """
    name = self.name
    resource_specs = []
    collections = []
    # Need to find a matching collection for validation, if the collection
    # is specified.
    for sub_resource in self._resources:
      sub_resource_arg = YAMLResourceArgument.FromSpecData(sub_resource)
      sub_resource_spec = sub_resource_arg.GenerateResourceSpec()
      resource_specs.append(sub_resource_spec)
      # pylint: disable=protected-access
      collections.append((sub_resource_arg._full_collection_name,
                          sub_resource_arg._api_version))
      # pylint: enable=protected-access
    if resource_collection:
      resource_collection_tuple = (resource_collection.full_name,
                                   resource_collection.api_version)
      if (resource_collection_tuple not in collections and
          (resource_collection_tuple[0], None) not in collections):
        raise util.InvalidSchemaError(
            'Collection names do not match for resource argument specification '
            '[{}]. Expected [{} version {}], and no contained resources '
            'matched. Given collections: [{}]'
            .format(self.name, resource_collection.full_name,
                    resource_collection.api_version,
                    ', '.join(sorted(
                        ['{} {}'.format(coll, vers)
                         for (coll, vers) in collections]))))
    return multitype.MultitypeResourceSpec(name, *resource_specs)
