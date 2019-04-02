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
"""Command for creating network peerings."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import batch_helper
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.calliope import actions
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.networks.peerings import flags
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources


def _MakeRequests(client, requests, is_async):
  """Helper for making asynchronous or synchronous peering creation requests."""
  if is_async:
    responses, errors = batch_helper.MakeRequests(
        requests=requests,
        http=client.apitools_client.http,
        batch_url=client.batch_url)
    if not errors:
      for operation in responses:
        log.status.write('Creating network peering for [{0}]\n'.format(
            operation.targetLink))
        log.status.write('Monitor its progress at [{0}]\n'.format(
            operation.selfLink))
    else:
      utils.RaiseToolException(errors)
  else:
    # We want to run through the generator that MakeRequests returns in order
    # to actually make the requests.
    responses = client.MakeRequests(requests)

  return responses


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Create(base.Command):
  """Create a Google Compute Engine network peering."""

  @staticmethod
  def ArgsCommon(parser):

    parser.add_argument('name', help='The name of the peering.')

    parser.add_argument(
        '--network',
        required=True,
        help='The name of the network in the current project to be peered '
        'with the peer network.')

    parser.add_argument(
        '--peer-network',
        required=True,
        help='The name of the network to be peered with the current network.')

    parser.add_argument(
        '--peer-project',
        required=False,
        help='The name of the project for the peer network.  If not specified, '
        'defaults to current project.')

    base.ASYNC_FLAG.AddToParser(parser)

  @staticmethod
  def Args(parser):
    Create.ArgsCommon(parser)
    parser.add_argument(
        '--auto-create-routes',
        action='store_true',
        default=False,
        required=False,
        help='If set, will automatically create routes for the network '
        'peering.  Note that a backend error will be returned if this is '
        'not set.')

  def Run(self, args):
    """Issues the request necessary for adding the peering."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    peer_network_ref = resources.REGISTRY.Parse(
        args.peer_network,
        params={
            'project':
                args.peer_project or properties.VALUES.core.project.GetOrFail
        },
        collection='compute.networks')

    request = client.messages.ComputeNetworksAddPeeringRequest(
        network=args.network,
        networksAddPeeringRequest=client.messages.NetworksAddPeeringRequest(
            autoCreateRoutes=args.auto_create_routes,
            name=args.name,
            peerNetwork=peer_network_ref.RelativeName()),
        project=properties.VALUES.core.project.GetOrFail())

    requests = [(client.apitools_client.networks, 'AddPeering', request)]
    return _MakeRequests(client, requests, args.async)


@base.ReleaseTracks(base.ReleaseTrack.BETA, base.ReleaseTrack.ALPHA)
class CreateAlphaBeta(Create):
  """Create a Google Compute Engine network peering."""

  @staticmethod
  def Args(parser):
    super(CreateAlphaBeta, CreateAlphaBeta).ArgsCommon(parser)
    flags.AddImportCustomRoutesFlag(parser)
    flags.AddExportCustomRoutesFlag(parser)

    action = actions.DeprecationAction(
        'auto-create-routes',
        warn='Flag --auto-create-routes is deprecated and will '
        'be removed in a future release.',
        action='store_true')
    parser.add_argument(
        '--auto-create-routes',
        action=action,
        default=False,
        required=False,
        help='If set, will automatically create routes for the '
        'network peering. Flag auto-create-routes is deprecated. Peer network '
        'subnet routes are always created in a network when peered.')

  def Run(self, args):
    """Issues the request necessary for adding the peering."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    peer_network_ref = resources.REGISTRY.Parse(
        args.peer_network,
        params={
            'project':
                args.peer_project or properties.VALUES.core.project.GetOrFail
        },
        collection='compute.networks')

    request = client.messages.ComputeNetworksAddPeeringRequest(
        network=args.network,
        networksAddPeeringRequest=client.messages.NetworksAddPeeringRequest(
            networkPeering=client.messages.NetworkPeering(
                name=args.name,
                network=peer_network_ref.RelativeName(),
                exportCustomRoutes=args.export_custom_routes,
                importCustomRoutes=args.import_custom_routes,
                exchangeSubnetRoutes=True)),
        project=properties.VALUES.core.project.GetOrFail())

    requests = [(client.apitools_client.networks, 'AddPeering', request)]
    return _MakeRequests(client, requests, args.async)
