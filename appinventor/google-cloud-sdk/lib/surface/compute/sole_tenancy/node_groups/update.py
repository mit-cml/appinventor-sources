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
"""Update node group command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute.sole_tenancy import node_groups
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute.sole_tenancy.node_groups import flags


class Update(base.UpdateCommand):
  """Updates a Google Compute Engine node group."""

  @staticmethod
  def Args(parser):
    flags.MakeNodeGroupArg().AddArgument(parser)
    flags.AddUpdateArgsToParser(parser)

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    messages = holder.client.messages
    groups_client = node_groups.NodeGroupsClient(
        holder.client.apitools_client, messages, holder.resources)

    node_group_ref = flags.MakeNodeGroupArg().ResolveAsResource(
        args, holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(holder.client))

    return groups_client.Update(
        node_group_ref,
        node_template=args.node_template,
        additional_node_count=args.add_nodes,
        delete_nodes=args.delete_nodes)
