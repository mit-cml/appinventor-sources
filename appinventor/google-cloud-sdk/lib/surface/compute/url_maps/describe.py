# -*- coding: utf-8 -*- #
# Copyright 2014 Google Inc. All Rights Reserved.
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
"""Command for describing url maps."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute.url_maps import flags
from googlecloudsdk.command_lib.compute.url_maps import url_maps_utils


@base.ReleaseTracks(base.ReleaseTrack.GA, base.ReleaseTrack.BETA)
class Describe(base.DescribeCommand):
  """Describe a URL map.

  *{command}* displays all data associated with a URL map in a
  project.
  """

  URL_MAP_ARG = None

  @staticmethod
  def Args(parser):
    Describe.URL_MAP_ARG = flags.UrlMapArgument()
    Describe.URL_MAP_ARG.AddArgument(parser, operation_type='describe')

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    url_map_ref = self.URL_MAP_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(client))

    request = client.messages.ComputeUrlMapsGetRequest(
        **url_map_ref.AsDict())

    return client.MakeRequests([(client.apitools_client.urlMaps,
                                 'Get', request)])[0]


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class DescribeAlpha(base.DescribeCommand):
  """Describe a URL map.

  *{command}* displays all data associated with a URL map in a
  project.
  """

  URL_MAP_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.URL_MAP_ARG = flags.UrlMapArgument(include_alpha=True)
    cls.URL_MAP_ARG.AddArgument(parser, operation_type='describe')

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    url_map_ref = self.URL_MAP_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(client))

    if url_maps_utils.IsRegionalUrlMapRef(url_map_ref):
      service = client.apitools_client.regionUrlMaps
      request = client.messages.ComputeRegionUrlMapsGetRequest(
          **url_map_ref.AsDict())
    else:
      service = client.apitools_client.urlMaps
      request = client.messages.ComputeUrlMapsGetRequest(**url_map_ref.AsDict())

    return client.MakeRequests([(service, 'Get', request)])[0]
