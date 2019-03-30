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
"""Command for creating Google Compute Engine commitments."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import re

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import request_helper
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute.commitments import flags
from googlecloudsdk.command_lib.compute.commitments import reservation_helper
from googlecloudsdk.core import properties


_MISSING_COMMITMENTS_QUOTA_REGEX = r'Quota .COMMITMENTS. exceeded.+'


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Create(base.Command):
  """Create Google Compute Engine commitments."""

  @classmethod
  def Args(cls, parser):
    flags.MakeCommitmentArg(False).AddArgument(parser, operation_type='create')
    flags.AddCreateFlags(parser)

  def _MakeCreateRequest(
      self, args, messages, project, region, commitment_ref, unused_holder):
    commitment = messages.Commitment(
        name=commitment_ref.Name(),
        plan=flags.TranslatePlanArg(messages, args.plan),
        resources=flags.TranslateResourcesArg(messages, args.resources),
    )
    return messages.ComputeRegionCommitmentsInsertRequest(
        commitment=commitment,
        project=project,
        region=commitment_ref.region,
    )

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    resources = holder.resources
    commitment_ref = flags.MakeCommitmentArg(False).ResolveAsResource(
        args,
        resources,
        scope_lister=compute_flags.GetDefaultScopeLister(holder.client))

    messages = holder.client.messages
    region = properties.VALUES.compute.region.Get()
    project = properties.VALUES.core.project.Get()
    create_request = self._MakeCreateRequest(
        args, messages, project, region, commitment_ref, holder)

    service = holder.client.apitools_client.regionCommitments
    batch_url = holder.client.batch_url
    http = holder.client.apitools_client.http
    errors = []
    result = list(request_helper.MakeRequests(
        requests=[(service, 'Insert', create_request)],
        http=http,
        batch_url=batch_url,
        errors=errors))
    for i, error in enumerate(errors):
      if re.match(_MISSING_COMMITMENTS_QUOTA_REGEX, error[1]):
        errors[i] = (
            error[0],
            error[1] + (' You can request commitments quota on '
                        'https://cloud.google.com/compute/docs/instances/'
                        'signing-up-committed-use-discounts#quota'))
    if errors:
      utils.RaiseToolException(errors)
    return result


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class CreateBeta(Create):
  """Create Google Compute Engine commitments."""

  @classmethod
  def Args(cls, parser):
    flags.MakeCommitmentArg(False).AddArgument(parser, operation_type='create')
    # TODO(b/118885734): verify that when reservations has been in beta.
    flags.AddCreateFlags(parser, enable_ssd_and_accelerator_support=True)

  def _MakeCreateRequest(self, args, messages, project, region, commitment_ref,
                         holder):
    commitment = messages.Commitment(
        name=commitment_ref.Name(),
        plan=flags.TranslatePlanArg(messages, args.plan),
        resources=flags.TranslateResourcesArgGroup(messages, args))
    return messages.ComputeRegionCommitmentsInsertRequest(
        commitment=commitment,
        project=project,
        region=commitment_ref.region,
    )


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class CreateAlpha(Create):
  """Create Google Compute Engine commitments."""

  @classmethod
  def Args(cls, parser):
    flags.MakeCommitmentArg(False).AddArgument(parser, operation_type='create')
    flags.AddCreateFlags(parser, enable_ssd_and_accelerator_support=True)
    flags.AddReservationArgGroup(parser)
    messages = apis.GetMessagesModule('compute', 'alpha')
    flags.GetTypeMapperFlag(messages).choice_arg.AddToParser(parser)

  def _MakeCreateRequest(
      self, args, messages, project, region, commitment_ref, holder):
    commitment_type_flag = flags.GetTypeMapperFlag(messages)
    commitment_type = commitment_type_flag.GetEnumForChoice(args.type)
    commitment = messages.Commitment(
        allocations=reservation_helper.MakeReservations(args, messages, holder),
        name=commitment_ref.Name(),
        plan=flags.TranslatePlanArg(messages, args.plan),
        resources=flags.TranslateResourcesArgGroup(messages, args),
        type=commitment_type)
    return messages.ComputeRegionCommitmentsInsertRequest(
        commitment=commitment,
        project=project,
        region=commitment_ref.region,
    )
