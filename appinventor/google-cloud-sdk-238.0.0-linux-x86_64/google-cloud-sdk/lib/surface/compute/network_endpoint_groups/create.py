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
"""Create network endpoint group command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import network_endpoint_groups
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute.network_endpoint_groups import flags
from googlecloudsdk.core import log


def _Run(args, holder):
  """Issues the request necessary for adding the network endpoint group."""
  client = holder.client
  messages = holder.client.messages
  resources = holder.resources
  neg_client = network_endpoint_groups.NetworkEndpointGroupsClient(client,
                                                                   messages,
                                                                   resources)
  neg_ref = flags.MakeNetworkEndpointGroupsArg().ResolveAsResource(
      args, holder.resources,
      scope_lister=compute_flags.GetDefaultScopeLister(holder.client))

  result = neg_client.Create(
      neg_ref, args.network_endpoint_type,
      default_port=args.default_port, network=args.network,
      subnet=args.subnet)
  log.CreatedResource(neg_ref.Name(), 'network endpoint group')
  return result


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class Create(base.CreateCommand):
  """Creates a Google Compute Engine network endpoint group."""

  @staticmethod
  def Args(parser, support_neg_type=False):
    flags.MakeNetworkEndpointGroupsArg().AddArgument(parser)
    flags.AddCreateNegArgsToParser(parser, support_neg_type=support_neg_type)

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    return _Run(args, holder)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class CreateAlpha(Create):
  """Creates a Google Compute Engine network endpoint group."""

  @staticmethod
  def Args(parser):
    flags.MakeNetworkEndpointGroupsArg().AddArgument(parser)
    flags.AddCreateNegArgsToParser(parser, support_neg_type=True)
