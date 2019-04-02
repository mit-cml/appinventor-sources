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
"""Command for list subnetworks which the current user has permission to use."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import list_pager
from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import base
from googlecloudsdk.core import properties


class ListUsableSubnets(base.ListCommand):
  """List subnetworks which the current user has permission to use."""

  @staticmethod
  def _EnableComputeApi():
    return properties.VALUES.compute.use_new_list_usable_subnets_api.GetBool()

  @staticmethod
  def Args(parser):
    parser.display_info.AddFormat("""\
        table(
          subnetwork.segment(-5):label=PROJECT,
          subnetwork.segment(-3):label=REGION,
          network.segment(-1):label=NETWORK,
          subnetwork.segment(-1):label=SUBNET,
          ipCidrRange:label=RANGE,
          secondaryIpRanges.map().format("{0} {1}", rangeName, ipCidrRange).list(separator="\n"):label=SECONDARY_RANGES
        )""")

  def Collection(self):
    return 'compute.subnetworks'

  def GetUriFunc(self):
    def _GetUri(search_result):
      return ''.join([
          p.value.string_value
          for p
          in search_result.resource.additionalProperties
          if p.key == 'selfLink'])
    return _GetUri

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client
    messages = holder.client.messages
    request = messages.ComputeSubnetworksListUsableRequest(
        project=properties.VALUES.core.project.Get(required=True))
    return list_pager.YieldFromList(
        client.apitools_client.subnetworks,
        request,
        method='ListUsable',
        batch_size_attribute='maxResults',
        batch_size=500,
        field='items')


ListUsableSubnets.detailed_help = {
    'brief':
        """\
        List Google Compute Engine subnetworks permitted for use.
        """,
    'DESCRIPTION':
        """\
        *{command}* is used to list Google Compute Engine subnetworks in a
        project that the user has permission to use.

        By default, usable subnetworks are listed for the default Google Cloud
        Platform project and user account. These values can be overridden by
        setting the global flags: `--project=PROJECT_ID` and/or
        `--account=ACCOUNT`.
        """,
    'EXAMPLES':
        """\
          To list all subnetworks in the default project that are usable by the
          default user:

            $ {command}

          To list all subnetworks in a specific project that are usable by a
          specific user:

            $ {command} \
                --project=PROJECT_ID --account=ACCOUNT
        """,
}
