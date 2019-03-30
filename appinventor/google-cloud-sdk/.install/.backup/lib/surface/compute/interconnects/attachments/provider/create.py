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
"""Command for creating partner provider interconnect attachments."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute.interconnects.attachments import client
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute.interconnects import flags as interconnect_flags
from googlecloudsdk.command_lib.compute.interconnects.attachments import flags as attachment_flags


class Create(base.CreateCommand):
  """Create a Google Compute Engine partner provider interconnect attachment.

  *{command}* is used to create partner provider interconnect attachments. An
  interconnect attachment binds the underlying connectivity of an Interconnect
  to a path into and out of the customer's cloud network. Partner provider
  attachments can only be created by approved network partners.
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

    cls.INTERCONNECT_ATTACHMENT_ARG = (
        attachment_flags.InterconnectAttachmentArgument())
    cls.INTERCONNECT_ATTACHMENT_ARG.AddArgument(parser, operation_type='create')

    attachment_flags.AddBandwidth(parser, required=True)
    attachment_flags.AddVlan(parser)
    attachment_flags.AddPartnerAsn(parser)
    attachment_flags.AddPartnerMetadata(parser, required=True)
    attachment_flags.AddPairingKey(parser)
    attachment_flags.AddDescription(parser)
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

    return interconnect_attachment.CreateAlpha(
        description=args.description,
        interconnect=interconnect_ref,
        vlan_tag_802_1q=args.vlan,
        attachment_type='PARTNER_PROVIDER',
        bandwidth=args.bandwidth,
        pairing_key=args.pairing_key,
        candidate_subnets=args.candidate_subnets,
        partner_asn=args.partner_asn,
        partner_name=args.partner_name,
        partner_interconnect=args.partner_interconnect_name,
        partner_portal_url=args.partner_portal_url)
