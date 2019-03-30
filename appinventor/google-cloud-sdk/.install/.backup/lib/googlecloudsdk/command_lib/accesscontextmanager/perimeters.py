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
"""Command line processing utilities for service perimeters."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.accesscontextmanager import util
from googlecloudsdk.calliope.concepts import concepts
from googlecloudsdk.command_lib.accesscontextmanager import common
from googlecloudsdk.command_lib.accesscontextmanager import levels
from googlecloudsdk.command_lib.accesscontextmanager import policies
from googlecloudsdk.command_lib.util.apis import arg_utils
from googlecloudsdk.command_lib.util.args import repeated
from googlecloudsdk.command_lib.util.concepts import concept_parsers
from googlecloudsdk.core import resources


REGISTRY = resources.REGISTRY


def AddAccessLevels(ref, args, req):
  """Hook to add access levels to request."""
  if args.IsSpecified('access_levels'):
    access_levels = []
    for access_level in args.access_levels:
      level_ref = resources.REGISTRY.Create(
          'accesscontextmanager.accessPolicies.accessLevels',
          accessLevelsId=access_level, **ref.Parent().AsDict())
      access_levels.append(level_ref.RelativeName())
    service_perimeter_config = req.servicePerimeter.status
    if not service_perimeter_config:
      service_perimeter_config = (
          util.GetMessages(version='v1beta').ServicePerimeterConfig)
    service_perimeter_config.accessLevels = access_levels
    req.servicePerimeter.status = service_perimeter_config
  return req


def AddImplicitServiceWildcard(ref, args, req):
  """Add an implicit wildcard for services if they are modified.

  If either restricted services or unrestricted services is given, the other
  must also be provided as a wildcard (`*`).

  If neither is given, this is a no-op.

  Args:
    ref: resources.Resource, the (unused) resource
    args: argparse namespace, the parse arguments
    req: AccesscontextmanagerAccessPoliciesAccessZonesCreateRequest

  Returns:
    The modified request.
  """
  del ref  # Unused in AddImplicitServiceWildcard
  service_perimeter_config = req.servicePerimeter.status
  if not service_perimeter_config:
    service_perimeter_config = util.GetMessages(
        version='v1beta').ServicePerimeterConfig
  if args.IsSpecified('restricted_services'):
    service_perimeter_config.unrestrictedServices = ['*']
  elif args.IsSpecified('unrestricted_services'):
    service_perimeter_config.restrictedServices = ['*']
  req.servicePerimeter.status = service_perimeter_config
  return req


def _GetAttributeConfig():
  return concepts.ResourceParameterAttributeConfig(
      name='perimeter', help_text='The ID of the service perimeter.')


def _GetResourceSpec():
  return concepts.ResourceSpec(
      'accesscontextmanager.accessPolicies.servicePerimeters',
      resource_name='perimeter',
      accessPoliciesId=policies.GetAttributeConfig(),
      servicePerimetersId=_GetAttributeConfig())


def AddResourceArg(parser, verb):
  """Add a resource argument for a service perimeter.

  NOTE: Must be used only if it's the only resource arg in the command.

  Args:
    parser: the parser for the command.
    verb: str, the verb to describe the resource, such as 'to update'.
  """
  concept_parsers.ConceptParser.ForResource(
      'perimeter',
      _GetResourceSpec(),
      'The service perimemter {}.'.format(verb),
      required=True).AddToParser(parser)


def GetTypeEnumMapper():
  return arg_utils.ChoiceEnumMapper(
      '--type',
      util.GetMessages().ServicePerimeter.PerimeterTypeValueValuesEnum,
      custom_mappings={
          'PERIMETER_TYPE_REGULAR': 'regular',
          'PERIMETER_TYPE_BRIDGE': 'bridge'
      },
      required=False,
      help_str="""\
          Type of the perimeter.

          A *regular* perimeter allows resources within this service perimeter
          to import and export data amongst themselves. A project may belong to
          at most one regular service perimeter.

          A *bridge* perimeter allows resources in different regular service
          perimeters to import and export data between each other. A project may
          belong to multiple bridge service perimeters (only if it also belongs to a
          regular service perimeter). Both restricted and unrestricted service lists,
          as well as access level lists, must be empty.
          """,
  )


def AddPerimeterUpdateArgs(parser):
  """Add args for perimeters update command."""
  args = [
      common.GetDescriptionArg('service perimeter'),
      common.GetTitleArg('service perimeter'),
      GetTypeEnumMapper().choice_arg
  ]
  for arg in args:
    arg.AddToParser(parser)
  _AddResources(parser)
  _AddUnrestrictedServices(parser)
  _AddRestrictedServices(parser)
  _AddLevelsUpdate(parser)


def _AddResources(parser):
  repeated.AddPrimitiveArgs(
      parser,
      'perimeter',
      'resources',
      'resources',
      additional_help=('Resources must be projects, in the form '
                       '`projects/<projectnumber>`.'))


def ParseResources(args, perimeter_result):
  return repeated.ParsePrimitiveArgs(
      args, 'resources', lambda: perimeter_result.Get().status.resources)


def _AddUnrestrictedServices(parser):
  repeated.AddPrimitiveArgs(
      parser,
      'perimemter',
      'unrestricted-services',
      'unrestricted services',
      metavar='SERVICE',
      additional_help=(
          'The perimeter boundary DOES NOT apply to these services (for '
          'example, `storage.googleapis.com`). A wildcard (```*```) may be '
          'given to denote all services.\n\n'
          'If restricted services are set, unrestricted services must be a '
          'wildcard.'))


def ParseUnrestrictedServices(args, perimeter_result):
  return repeated.ParsePrimitiveArgs(
      args, 'unrestricted_services',
      lambda: perimeter_result.Get().status.restrictedServices)


def _AddRestrictedServices(parser):
  repeated.AddPrimitiveArgs(
      parser,
      'perimeter',
      'restricted-services',
      'restricted services',
      metavar='SERVICE',
      additional_help=(
          'The perimeter boundary DOES apply to these services (for example, '
          '`storage.googleapis.com`). A wildcard (```*```) may be given to '
          'denote all services.\n\n'
          'If unrestricted services are set, restricted services must be a '
          'wildcard.'))


def ParseRestrictedServices(args, perimeter_result):
  return repeated.ParsePrimitiveArgs(
      args, 'restricted_services',
      lambda: perimeter_result.Get().status.restrictedServices)


def _AddLevelsUpdate(parser):
  repeated.AddPrimitiveArgs(
      parser,
      'perimeter',
      'access-levels',
      'access levels',
      metavar='LEVEL',
      additional_help=(
          'An intra-perimeter request must satisfy these access levels (for '
          'example, `MY_LEVEL`; must be in the same access policy as this '
          'perimeter) to be allowed.'))


def _GetLevelIdFromLevelName(level_name):
  return REGISTRY.Parse(
      level_name, collection=levels.COLLECTION).accessLevelsId


def ParseLevels(args, perimeter_result, policy_id):
  """Process repeated level changes."""

  def GetLevelIds():
    return [
        _GetLevelIdFromLevelName(l)
        for l in perimeter_result.Get().status.accessLevels
    ]

  level_ids = repeated.ParsePrimitiveArgs(args, 'access_levels', GetLevelIds)

  if level_ids is None:
    return None
  return [REGISTRY.Create(levels.COLLECTION,
                          accessPoliciesId=policy_id,
                          accessLevelsId=l) for l in level_ids]
