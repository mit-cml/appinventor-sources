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

"""Command for removing a path matcher from a URL map."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import encoding

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute.url_maps import flags
from googlecloudsdk.command_lib.compute.url_maps import url_maps_utils


@base.ReleaseTracks(base.ReleaseTrack.GA, base.ReleaseTrack.BETA)
class RemovePathMatcher(base.UpdateCommand):
  """Remove a path matcher from a URL map.

  *{command}* is used to remove a path matcher from a URL
  map. When a path matcher is removed, all host rules that
  refer to the path matcher are also removed.

  ## EXAMPLES
  To remove the path matcher named ``MY-MATCHER'' from the URL map named
  ``MY-URL-MAP'', you can use this command:

    $ {command} MY-URL-MAP --path-matcher MY-MATCHER
  """

  URL_MAP_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.URL_MAP_ARG = flags.UrlMapArgument()
    cls.URL_MAP_ARG.AddArgument(parser)

    parser.add_argument(
        '--path-matcher-name',
        required=True,
        help='The name of the path matcher to remove.')

  def _GetGetRequest(self, client, url_map_ref):
    """Returns the request for the existing URL map resource."""
    return (client.apitools_client.urlMaps,
            'Get',
            client.messages.ComputeUrlMapsGetRequest(
                urlMap=url_map_ref.Name(),
                project=url_map_ref.project))

  def _GetSetRequest(self, client, url_map_ref, replacement):
    return (client.apitools_client.urlMaps,
            'Update',
            client.messages.ComputeUrlMapsUpdateRequest(
                urlMap=url_map_ref.Name(),
                urlMapResource=replacement,
                project=url_map_ref.project))

  def _Modify(self, args, existing):
    """Returns a modified URL map message."""
    replacement = encoding.CopyProtoMessage(existing)

    # Removes the path matcher.
    new_path_matchers = []
    path_matcher_found = False
    for path_matcher in existing.pathMatchers:
      if path_matcher.name == args.path_matcher_name:
        path_matcher_found = True
      else:
        new_path_matchers.append(path_matcher)

    if not path_matcher_found:
      raise exceptions.ToolException(
          'No path matcher with the name [{0}] was found.'.format(
              args.path_matcher_name))

    replacement.pathMatchers = new_path_matchers

    # Removes all host rules that refer to the path matcher.
    new_host_rules = []
    for host_rule in existing.hostRules:
      if host_rule.pathMatcher != args.path_matcher_name:
        new_host_rules.append(host_rule)
    replacement.hostRules = new_host_rules

    return replacement

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    url_map_ref = self.URL_MAP_ARG.ResolveAsResource(args, holder.resources)
    get_request = self._GetGetRequest(client, url_map_ref)

    url_map = client.MakeRequests([get_request])[0]

    modified_url_map = self._Modify(args, url_map)

    return client.MakeRequests(
        [self._GetSetRequest(client, url_map_ref, modified_url_map)])


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class RemovePathMatcherAlpha(RemovePathMatcher):
  """Remove a path matcher from a URL map.

  *{command}* is used to remove a path matcher from a URL
  map. When a path matcher is removed, all host rules that
  refer to the path matcher are also removed.

  ## EXAMPLES
  To remove the path matcher named ``MY-MATCHER'' from the URL map named
  ``MY-URL-MAP'', you can use this command:

    $ {command} MY-URL-MAP --path-matcher MY-MATCHER
  """
  URL_MAP_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.URL_MAP_ARG = flags.UrlMapArgument(include_alpha=True)
    cls.URL_MAP_ARG.AddArgument(parser)

    parser.add_argument(
        '--path-matcher-name',
        required=True,
        help='The name of the path matcher to remove.')

  def _GetRegionalGetRequest(self, client, url_map_ref):
    """Returns the request to get an existing regional URL map resource."""
    return (client.apitools_client.regionUrlMaps, 'Get',
            client.messages.ComputeRegionUrlMapsGetRequest(
                urlMap=url_map_ref.Name(),
                project=url_map_ref.project,
                region=url_map_ref.region))

  def _GetRegionalSetRequest(self, client, url_map_ref, replacement):
    """Returns the request to update an existing regional URL map resource."""
    return (client.apitools_client.regionUrlMaps, 'Update',
            client.messages.ComputeRegionUrlMapsUpdateRequest(
                urlMap=url_map_ref.Name(),
                urlMapResource=replacement,
                project=url_map_ref.project,
                region=url_map_ref.region))

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    url_map_ref = self.URL_MAP_ARG.ResolveAsResource(args, holder.resources)
    if url_maps_utils.IsRegionalUrlMapRef(url_map_ref):
      get_request = self._GetRegionalGetRequest(client, url_map_ref)
    else:
      get_request = self._GetGetRequest(client, url_map_ref)

    url_map = client.MakeRequests([get_request])[0]
    modified_url_map = self._Modify(args, url_map)

    if url_maps_utils.IsRegionalUrlMapRef(url_map_ref):
      set_request = self._GetRegionalSetRequest(client, url_map_ref,
                                                modified_url_map)
    else:
      set_request = self._GetSetRequest(client, url_map_ref, modified_url_map)

    return client.MakeRequests([set_request])
