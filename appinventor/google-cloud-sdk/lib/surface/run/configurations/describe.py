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
"""Command for obtaining details about a given configuration."""

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
  """Obtain details about a given configuration."""

  detailed_help = {
      'DESCRIPTION': """\
          {description}
          """,
      'EXAMPLES': """\
          To obtain details about a given configuration:

              $ {command} <configuration-name>
          """,
  }

  @staticmethod
  def Args(parser):
    flags.AddRegionArg(parser)
    configuration_presentation = presentation_specs.ResourcePresentationSpec(
        'CONFIGURATION',
        resource_args.GetConfigurationResourceSpec(),
        'Configuration to describe.',
        required=True,
        prefixes=False)
    concept_parsers.ConceptParser([
        resource_args.CLUSTER_PRESENTATION,
        configuration_presentation]).AddToParser(parser)
    parser.display_info.AddFormat('yaml')

  def Run(self, args):
    """Obtain details about a given configuration."""
    conn_context = connection_context.GetConnectionContext(args)
    configuration_ref = args.CONCEPTS.configuration.Parse()
    with serverless_operations.Connect(conn_context) as client:
      conf = client.GetConfiguration(configuration_ref)
    if not conf:
      raise flags.ArgumentError(
          'Cannot find configuration [{}]'.format(
              configuration_ref.configurationsId))
    return conf
