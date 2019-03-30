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
"""Command for obtaining details about a given route."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.run import connection_context
from googlecloudsdk.command_lib.run import flags
from googlecloudsdk.command_lib.run import resource_args
from googlecloudsdk.command_lib.run import serverless_operations
from googlecloudsdk.command_lib.util.concepts import concept_parsers
from googlecloudsdk.command_lib.util.concepts import presentation_specs


class Describe(base.Command):
  """Obtain details about a given route."""

  detailed_help = {
      'DESCRIPTION': """\
          {description}
          """,
      'EXAMPLES': """\
          To obtain details about a given route:

              $ {command} <route-name>
          """,
  }

  @staticmethod
  def Args(parser):
    flags.AddRegionArg(parser)
    route_presentation = presentation_specs.ResourcePresentationSpec(
        'ROUTE',
        resource_args.GetRouteResourceSpec(),
        'Route to describe.',
        required=True,
        prefixes=False)
    concept_parsers.ConceptParser([
        resource_args.CLUSTER_PRESENTATION,
        route_presentation]).AddToParser(parser)
    parser.display_info.AddFormat('yaml')

  def Run(self, args):
    """Obtain details about a given route."""
    conn_context = connection_context.GetConnectionContext(args)
    route_ref = args.CONCEPTS.route.Parse()
    with serverless_operations.Connect(conn_context) as client:
      conf = client.GetRoute(route_ref)
    if not conf:
      raise flags.ArgumentError(
          'Cannot find route [{}]'.format(
              route_ref.routesId))
    return conf
