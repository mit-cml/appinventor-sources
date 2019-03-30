# -*- coding: utf-8 -*- #
# Copyright 2016 Google Inc. All Rights Reserved.
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
"""Command for updating network peerings."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute.networks.peerings import flags
from googlecloudsdk.core import properties


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class UpdateAlpha(base.Command):
  """Update a Google Compute Engine network peering."""

  @staticmethod
  def Args(parser):
    parser.add_argument('name', help='The name of the peering.')
    parser.add_argument(
        '--network',
        required=True,
        help='The name of the network in the current project to be peered '
        'with the peer network.')
    flags.AddImportCustomRoutesFlag(parser)
    flags.AddExportCustomRoutesFlag(parser)

  def Run(self, args):
    """Issues the request necessary for updating the peering."""
    self.ValidateArgs(args)
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client
    messages = holder.client.messages
    request = client.messages.ComputeNetworksUpdatePeeringRequest(
        network=args.network,
        networksUpdatePeeringRequest=client.messages
        .NetworksUpdatePeeringRequest(
            networkPeering=messages.NetworkPeering(
                name=args.name,
                exportCustomRoutes=args.export_custom_routes,
                importCustomRoutes=args.import_custom_routes)),
        project=properties.VALUES.core.project.GetOrFail())

    return client.MakeRequests([(client.apitools_client.networks,
                                 'UpdatePeering', request)])

  def ValidateArgs(self, args):
    """Validate arguments."""
    if not any([
        args.export_custom_routes is not None,
        args.import_custom_routes is not None,
    ]):
      raise exceptions.ToolException('At least one property must be modified.')
