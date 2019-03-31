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
"""Command to update the project."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import base
from googlecloudsdk.core import properties


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Update(base.UpdateCommand):
  r"""Update a Google Compute Engine project resource.

  *{command}* is used to update a Google Compute Engine project resource.
  """

  @classmethod
  def Args(cls, parser):
    parser.add_argument(
        '--default-network-tier',
        choices=['PREMIUM', 'STANDARD'],
        type=lambda x: x.upper(),
        help='The default network tier to assign to the project.')

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client.apitools_client
    messages = holder.client.messages

    requests = []

    if args.default_network_tier:
      request = messages.ComputeProjectsSetDefaultNetworkTierRequest(
          project=properties.VALUES.core.project.GetOrFail(),
          projectsSetDefaultNetworkTierRequest=messages.
          ProjectsSetDefaultNetworkTierRequest(
              networkTier=messages.ProjectsSetDefaultNetworkTierRequest.
              NetworkTierValueValuesEnum(args.default_network_tier)))
      requests.append((client.projects, 'SetDefaultNetworkTier', request))

    return holder.client.MakeRequests(requests)


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class UpdateBeta(Update):
  r"""Update a Google Compute Engine project resource.

  *{command}* is used to update a Google Compute Engine project resource.
  """


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class UpdateAlpha(UpdateBeta):
  r"""Update a Google Compute Engine project resource.

  *{command}* is used to update a Google Compute Engine project resource.
  """
