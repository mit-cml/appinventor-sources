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

"""Command for removing a host rule from a URL map."""

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
class RemoveHostRule(base.UpdateCommand):
  """Remove a host rule from a URL map.

  *{command}* is used to remove a host rule from a URL map. When
  a host rule is removed, its path matcher is only removed if
  it is not referenced by any other host rules and
  `--delete-orphaned-path-matcher` is provided.

  ## EXAMPLES
  To remove a host rule that contains the host `example.com`
  from the URL map named `MY-URL-MAP`, you can use this
  command:

    $ {command} MY-URL-MAP --host example.com
  """

  URL_MAP_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.URL_MAP_ARG = flags.UrlMapArgument()
    cls.URL_MAP_ARG.AddArgument(parser)

    parser.add_argument(
        '--host',
        required=True,
        help='One of the hosts in the host rule to remove.')

    parser.add_argument(
        '--delete-orphaned-path-matcher',
        action='store_true',
        default=False,
        help=('If provided and a path matcher is orphaned as a result of this '
              'command, the command removes the orphaned path matcher instead '
              'of failing.'))

  def _GetGetRequest(self, client, url_map_ref):
    """Returns the request for the existing URL map resource."""
    if url_maps_utils.IsGlobalUrlMapRef(url_map_ref):
      return (client.apitools_client.urlMaps, 'Get',
              client.messages.ComputeUrlMapsGetRequest(
                  urlMap=url_map_ref.Name(), project=url_map_ref.project))
    else:
      return (client.apitools_client.regionUrlMaps, 'Get',
              client.messages.ComputeRegionUrlMapsGetRequest(
                  urlMap=url_map_ref.Name(),
                  project=url_map_ref.project,
                  region=url_map_ref.region))

  def _GetSetRequest(self, client, url_map_ref, replacement):
    if url_maps_utils.IsGlobalUrlMapRef(url_map_ref):
      return (client.apitools_client.urlMaps, 'Update',
              client.messages.ComputeUrlMapsUpdateRequest(
                  urlMap=url_map_ref.Name(),
                  urlMapResource=replacement,
                  project=url_map_ref.project))
    else:
      return (client.apitools_client.regionUrlMaps, 'Update',
              client.messages.ComputeRegionUrlMapsUpdateRequest(
                  urlMap=url_map_ref.Name(),
                  urlMapResource=replacement,
                  project=url_map_ref.project,
                  region=url_map_ref.region))

  def _Modify(self, args, existing):
    """Returns a modified URL map message."""
    replacement = encoding.CopyProtoMessage(existing)

    path_matcher_to_remove = None
    new_host_rules = []
    for host_rule in existing.hostRules:
      if args.host in host_rule.hosts:
        path_matcher_to_remove = host_rule.pathMatcher
      else:
        new_host_rules.append(host_rule)

    if not path_matcher_to_remove:
      raise exceptions.ToolException(
          'No host rule contains the host [{0}].'.format(args.host))

    replacement.hostRules = new_host_rules

    path_matcher_is_used_by_other_rules = False
    for host_rule in replacement.hostRules:
      if host_rule.pathMatcher == path_matcher_to_remove:
        path_matcher_is_used_by_other_rules = True
        break

    if not path_matcher_is_used_by_other_rules:
      if args.delete_orphaned_path_matcher:
        replacement.pathMatchers = [
            path_matcher for path_matcher in existing.pathMatchers
            if path_matcher.name != path_matcher_to_remove]
      else:
        raise exceptions.ToolException(
            'This operation will orphan the path matcher [{0}]. To '
            'delete the orphan path matcher, rerun this command with '
            '[--delete-orphaned-path-matcher] or use [gcloud compute '
            'url-maps edit] to modify the URL map by hand.'.format(
                host_rule.pathMatcher))

    return replacement

  def Run(self, args):
    """Issues requests necessary to remove host rule on URL maps."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    url_map_ref = self.URL_MAP_ARG.ResolveAsResource(args, holder.resources)
    get_request = self._GetGetRequest(client, url_map_ref)

    objects = client.MakeRequests([get_request])

    new_object = self._Modify(args, objects[0])

    return client.MakeRequests(
        [self._GetSetRequest(client, url_map_ref, new_object)])


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class RemoveHostRuleAlpha(RemoveHostRule):
  """Remove a host rule from a URL map.

  *{command}* is used to remove a host rule from a URL map. When
  a host rule is removed, its path matcher is only removed if
  it is not referenced by any other host rules and
  `--delete-orphaned-path-matcher` is provided.

  ## EXAMPLES
  To remove a host rule that contains the host `example.com`
  from the URL map named `MY-URL-MAP`, you can use this
  command:

    $ {command} MY-URL-MAP --host example.com --global
  """

  URL_MAP_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.URL_MAP_ARG = flags.UrlMapArgument(include_alpha=True)
    cls.URL_MAP_ARG.AddArgument(parser)

    parser.add_argument(
        '--host',
        required=True,
        help='One of the hosts in the host rule to remove.')

    parser.add_argument(
        '--delete-orphaned-path-matcher',
        action='store_true',
        default=False,
        help=('If provided and a path matcher is orphaned as a result of this '
              'command, the command removes the orphaned path matcher instead '
              'of failing.'))
