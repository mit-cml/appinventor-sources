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
"""Command for listing available reivions."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.run import connection_context
from googlecloudsdk.command_lib.run import flags
from googlecloudsdk.command_lib.run import pretty_print
from googlecloudsdk.command_lib.run import resource_args
from googlecloudsdk.command_lib.run import serverless_operations
from googlecloudsdk.command_lib.util.concepts import concept_parsers
from googlecloudsdk.command_lib.util.concepts import presentation_specs


class List(base.ListCommand):
  """List available revisions."""

  detailed_help = {
      'DESCRIPTION': """\
          {description}
          """,
      'EXAMPLES': """\
          To list all revisions for the provided service:

              $ {command} --service foo
         """,
  }

  @staticmethod
  def Args(parser):
    flags.AddServiceFlag(parser)
    namespace_presentation = presentation_specs.ResourcePresentationSpec(
        '--namespace',
        resource_args.GetNamespaceResourceSpec(),
        'Namespace to list services in.',
        required=True,
        prefixes=False)
    flags.AddRegionArgWithDefault(parser)
    concept_parsers.ConceptParser([
        resource_args.CLUSTER_PRESENTATION,
        namespace_presentation]).AddToParser(parser)
    parser.display_info.AddFormat(
        'table('
        '{ready_column},'
        'name:label=REVISION,service_name:label=SERVICE,author,'
        'creation_timestamp.date("%Y-%m-%d %H:%M:%S %Z"):label=CREATED)'.format(
            ready_column=pretty_print.READY_COLUMN))

  def Run(self, args):
    """List available revisions."""
    service_name = args.service
    conn_context = connection_context.GetConnectionContext(args)
    namespace_ref = args.CONCEPTS.namespace.Parse()
    with serverless_operations.Connect(conn_context) as client:
      return client.ListRevisions(namespace_ref, service_name)
