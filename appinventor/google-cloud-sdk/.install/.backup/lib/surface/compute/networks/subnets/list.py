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

"""Command for listing subnetworks."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import lister
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.networks.subnets import flags


class List(base.ListCommand):
  """List subnetworks."""

  @staticmethod
  def Args(parser):
    parser.display_info.AddFormat(flags.DEFAULT_LIST_FORMAT)
    lister.AddRegionsArg(parser)
    parser.display_info.AddCacheUpdater(flags.SubnetworksCompleter)

    parser.add_argument(
        '--network',
        help='Only show subnetworks of a specific network.')

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    request_data = lister.ParseRegionalFlags(args, holder.resources)

    list_implementation = lister.RegionalLister(
        client, client.apitools_client.subnetworks)

    for resource in lister.Invoke(request_data, list_implementation):
      if args.network is None:
        yield resource
      elif 'network' in resource:
        network_ref = holder.resources.Parse(resource['network'])
        if network_ref.Name() == args.network:
          yield resource


List.detailed_help = base_classes.GetRegionalListerHelp('subnetworks')
