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
"""Command for creating dedicated interconnect attachments."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute.interconnects.attachments import client
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import parser_errors
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute.interconnects import flags as interconnect_flags
from googlecloudsdk.command_lib.compute.interconnects.attachments import flags as attachment_flags
from googlecloudsdk.command_lib.compute.routers import flags as router_flags
from googlecloudsdk.core import log

_DOCUMENTATION_LINK = 'https://cloud.google.com/interconnect/docs/how-to/dedicated/creating-vlan-attachments'


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Create(base.CreateCommand):
  """Create a Google Compute Engine dedicated interconnect attachment.

  *{command}* is used to create a dedicated interconnect attachments. An
  interconnect attachment is what binds the underlying connectivity of an
  interconnect to a path into and out of the customer's cloud network.
  """
  INTERCONNECT_ATTACHMENT_ARG = None
  INTERCONNECT_ARG = None
  ROUTER_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.INTERCONNECT_ARG = (
        interconnect_flags.InterconnectArgumentForOtherResource(
            'The interconnect for the interconnect attachment'))
    cls.INTERCONNECT_ARG.AddArgument(parser)

    cls.ROUTER_ARG = router_flags.RouterArgumentForOtherResources()
    cls.ROUTER_ARG.AddArgument(parser)

    cls.INTERCONNECT_ATTACHMENT_ARG = (
        attachment_flags.InterconnectAttachmentArgument())
    cls.INTERCONNECT_ATTACHMENT_ARG.AddArgument(parser, operation_type='create')
    attachment_flags.AddDescription(parser)
    attachment_flags.AddAdminEnabled(parser, default_behavior=True)
    attachment_flags.AddVlan(parser)
    attachment_flags.AddCandidateSubnets(parser)

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    attachment_ref = self.INTERCONNECT_ATTACHMENT_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(holder.client))

    interconnect_attachment = client.InterconnectAttachment(
        attachment_ref, compute_client=holder.client)

    interconnect_ref = None
    if args.interconnect is not None:
      interconnect_ref = self.INTERCONNECT_ARG.ResolveAsResource(
          args, holder.resources)

    if args.router_region is None:
      args.router_region = attachment_ref.region

    if args.router_region != attachment_ref.region:
      raise parser_errors.ArgumentException(
          'router-region must be same as the attachment region.')

    router_ref = None
    if args.router is not None:
      router_ref = self.ROUTER_ARG.ResolveAsResource(args, holder.resources)

    return interconnect_attachment.CreateAlpha(
        description=args.description,
        interconnect=interconnect_ref,
        attachment_type='DEDICATED',
        router=router_ref,
        vlan_tag_802_1q=args.vlan,
        admin_enabled=args.admin_enabled,
        candidate_subnets=args.candidate_subnets,
        bandwidth=getattr(args, 'bandwidth', None))

  def Epilog(self, resources_were_displayed):
    message = ('You must configure your Google Cloud Router with an interface '
               'and BGP peer for your created VLAN attachment. See also {} for '
               'more detailed help.'.format(_DOCUMENTATION_LINK))
    log.status.Print(message)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class CreateWithBandwidth(Create):
  """Create a Google Compute Engine dedicated interconnect attachment.

  *{command}* is used to create a dedicated interconnect attachments. An
  interconnect attachment is what binds the underlying connectivity of an
  interconnect to a path into and out of the customer's cloud network.
  """

  @classmethod
  def Args(cls, parser):
    super(CreateWithBandwidth, cls).Args(parser)
    attachment_flags.AddBandwidth(parser, required=False)
