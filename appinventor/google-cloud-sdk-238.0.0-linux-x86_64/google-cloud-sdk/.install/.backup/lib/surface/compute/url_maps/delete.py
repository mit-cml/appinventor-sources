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
"""Command for deleting URL maps."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute.url_maps import flags
from googlecloudsdk.command_lib.compute.url_maps import url_maps_utils


@base.ReleaseTracks(base.ReleaseTrack.GA, base.ReleaseTrack.BETA)
class Delete(base.DeleteCommand):
  """Delete URL maps.

  *{command}* deletes one or more URL maps.
  """

  URL_MAP_ARG = None

  @staticmethod
  def Args(parser):
    Delete.URL_MAP_ARG = flags.UrlMapArgument(plural=True)
    Delete.URL_MAP_ARG.AddArgument(parser, operation_type='delete')
    parser.display_info.AddCacheUpdater(flags.UrlMapsCompleter)

  def Run(self, args):
    """Issues requests necessary to delete URL maps."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    url_map_refs = Delete.URL_MAP_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(client))

    utils.PromptForDeletion(url_map_refs)

    requests = []
    for url_map_ref in url_map_refs:
      requests.append((client.apitools_client.urlMaps, 'Delete',
                       client.messages.ComputeUrlMapsDeleteRequest(
                           **url_map_ref.AsDict())))

    return client.MakeRequests(requests)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class DeleteAlpha(base.DeleteCommand):
  """Delete URL maps.

  *{command}* deletes one or more URL maps.
  """

  URL_MAP_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.URL_MAP_ARG = flags.UrlMapArgument(plural=True, include_alpha=True)
    cls.URL_MAP_ARG.AddArgument(parser, operation_type='delete')
    parser.display_info.AddCacheUpdater(flags.UrlMapsCompleterAlpha)

  def Run(self, args):
    """Issues requests necessary to delete URL maps."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    url_map_refs = self.URL_MAP_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(client))

    utils.PromptForDeletion(url_map_refs)

    requests = []
    for url_map_ref in url_map_refs:
      if url_maps_utils.IsRegionalUrlMapRef(url_map_ref):
        requests.append((client.apitools_client.regionUrlMaps, 'Delete',
                         client.messages.ComputeRegionUrlMapsDeleteRequest(
                             **url_map_ref.AsDict())))
      else:
        requests.append(
            (client.apitools_client.urlMaps, 'Delete',
             client.messages.ComputeUrlMapsDeleteRequest(**url_map_ref.AsDict())
            ))

    return client.MakeRequests(requests)
