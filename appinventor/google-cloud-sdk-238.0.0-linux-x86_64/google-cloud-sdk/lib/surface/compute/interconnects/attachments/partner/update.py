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
"""Command for updating interconnects."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute.interconnects.attachments import client
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute.interconnects.attachments import flags as attachment_flags
from googlecloudsdk.command_lib.util.args import labels_util


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class Update(base.UpdateCommand):
  """Update a Google Compute Engine partner interconnect attachment.

  *{command}* is used to update partner interconnect attachments. A partner
  interconnect attachment binds the underlying connectivity of a provider's
  Interconnect to a path into and out of the customer's cloud network.
  """

  INTERCONNECT_ATTACHMENT_ARG = None
  INTERCONNECT_ARG = None
  ROUTER_ARG = None

  @classmethod
  def Args(cls, parser):

    cls.INTERCONNECT_ATTACHMENT_ARG = (
        attachment_flags.InterconnectAttachmentArgument())
    cls.INTERCONNECT_ATTACHMENT_ARG.AddArgument(parser, operation_type='patch')
    attachment_flags.AddDescription(parser)
    attachment_flags.AddAdminEnabled(parser, update=True)
    labels_util.AddUpdateLabelsFlags(parser)

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    attachment_ref = self.INTERCONNECT_ATTACHMENT_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(holder.client))

    interconnect_attachment = client.InterconnectAttachment(
        attachment_ref, compute_client=holder.client)

    labels = None
    label_fingerprint = None
    labels_diff = labels_util.Diff.FromUpdateArgs(args)
    if labels_diff.MayHaveUpdates():
      old_attachment = interconnect_attachment.Describe()
      labels_cls = holder.client.messages.InterconnectAttachment.LabelsValue
      labels = labels_diff.Apply(
          labels_cls, labels=old_attachment.labels).GetOrNone()
      if labels is not None:
        label_fingerprint = old_attachment.labelFingerprint

    return interconnect_attachment.PatchAlphaAndBeta(
        description=args.description,
        admin_enabled=args.admin_enabled,
        labels=labels,
        label_fingerprint=label_fingerprint)


@base.ReleaseTracks(base.ReleaseTrack.GA)
class GaUpdate(Update):
  """Update a Google Compute Engine partner interconnect attachment.

  *{command}* is used to update partner interconnect attachments. A partner
  interconnect attachment binds the underlying connectivity of a provider's
  Interconnect to a path into and out of the customer's cloud network.
  """

  @classmethod
  def Args(cls, parser):
    cls.INTERCONNECT_ATTACHMENT_ARG = (
        attachment_flags.InterconnectAttachmentArgument())
    cls.INTERCONNECT_ATTACHMENT_ARG.AddArgument(parser, operation_type='patch')
    attachment_flags.AddDescription(parser)
    attachment_flags.AddAdminEnabled(parser, update=True)

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    attachment_ref = self.INTERCONNECT_ATTACHMENT_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(holder.client))

    interconnect_attachment = client.InterconnectAttachment(
        attachment_ref, compute_client=holder.client)

    return interconnect_attachment.PatchGa(
        description=args.description, admin_enabled=args.admin_enabled)
