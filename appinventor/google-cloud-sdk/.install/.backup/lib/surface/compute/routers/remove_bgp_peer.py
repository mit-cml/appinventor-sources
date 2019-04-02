# -*- coding: utf-8 -*- #
# Copyright 2015 Google Inc. All Rights Reserved.
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

"""Command for removing a BGP peer from a Google Compute Engine router."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import encoding

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.routers import flags
from googlecloudsdk.core import exceptions


class PeerNotFoundError(exceptions.Error):
  """Raised when a peer is not found."""

  def __init__(self, name):
    self.name = name
    msg = 'peer `{0}` not found'.format(name)
    super(PeerNotFoundError, self).__init__(msg)


class RemoveBgpPeer(base.UpdateCommand):
  """Remove a BGP peer from a Google Compute Engine router.

  *{command}* removes a BGP peer from a Google Compute Engine router.
  """

  ROUTER_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.ROUTER_ARG = flags.RouterArgument()
    cls.ROUTER_ARG.AddArgument(parser, operation_type='update')

    parser.add_argument(
        '--peer-name',
        required=True,
        help='The name of the peer being removed.')

  def GetGetRequest(self, client, router_ref):
    return (client.apitools_client.routers,
            'Get',
            client.messages.ComputeRoutersGetRequest(
                router=router_ref.Name(),
                region=router_ref.region,
                project=router_ref.project))

  def GetSetRequest(self, client, router_ref, replacement):
    return (client.apitools_client.routers,
            'Patch',
            client.messages.ComputeRoutersPatchRequest(
                router=router_ref.Name(),
                routerResource=replacement,
                region=router_ref.region,
                project=router_ref.project))

  def Modify(self, args, existing, cleared_fields):
    """Mutate the router and record any cleared_fields for Patch request."""
    replacement = encoding.CopyProtoMessage(existing)

    # remove peer if exists
    peer = None
    for p in replacement.bgpPeers:
      if p.name == args.peer_name:
        peer = p
        replacement.bgpPeers.remove(peer)
        if not replacement.bgpPeers:
          cleared_fields.append('bgpPeers')
        break

    if peer is None:
      raise PeerNotFoundError(args.peer_name)

    return replacement

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    router_ref = self.ROUTER_ARG.ResolveAsResource(args, holder.resources)
    get_request = self.GetGetRequest(client, router_ref)

    objects = client.MakeRequests([get_request])

    # Cleared list fields need to be explicitly identified for Patch API.
    cleared_fields = []
    new_object = self.Modify(args, objects[0], cleared_fields)

    with client.apitools_client.IncludeFields(cleared_fields):
      # There is only one response because one request is made above
      result = client.MakeRequests(
          [self.GetSetRequest(client, router_ref, new_object)])
    return result
