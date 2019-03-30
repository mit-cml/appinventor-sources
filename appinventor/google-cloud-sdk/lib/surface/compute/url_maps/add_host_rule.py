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

"""Command for adding a host rule to a URL map."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import encoding

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.url_maps import flags
from googlecloudsdk.command_lib.compute.url_maps import url_maps_utils


def _Args(parser):
  """Add command line flags to the parser."""

  parser.add_argument(
      '--description',
      help='An optional, textual description for the host rule.')

  parser.add_argument(
      '--hosts',
      type=arg_parsers.ArgList(min_length=1),
      metavar='HOST',
      required=True,
      help="""\
      The set of hosts to match requests against. Each host must be
      a fully qualified domain name (FQDN) with the exception that
      the host can begin with a ``*'' or ``*-''. ``*'' acts as a
      glob and will match any string of atoms to the left where an
      atom is separated by dots (``.'') or dashes (``-'').
      """)

  parser.add_argument(
      '--path-matcher-name',
      required=True,
      help="""\
      The name of the patch matcher to use if a request matches this
      host rule. The patch matcher must already exist in the URL map
      (see `gcloud compute url-maps add-path-matcher`).
      """)


@base.ReleaseTracks(base.ReleaseTrack.GA, base.ReleaseTrack.BETA)
class AddHostRule(base.UpdateCommand):
  # pylint:disable=line-too-long
  """Add a rule to a URL map to map hosts to a path matcher.

  *{command}* is used to add a mapping of hosts to a patch
  matcher in a URL map. The mapping will match the host
  component of HTTP requests to path matchers which in turn map
  the request to a backend service. Before adding a host rule,
  at least one path matcher must exist in the URL map to take
  care of the path component of the requests.
  `gcloud compute url-maps add-path-matcher` or
  `gcloud compute url-maps edit` can be used to add path matchers.

  ## EXAMPLES
  To create a host rule mapping the ```*-foo.example.com``` and
  ```example.com``` hosts to the ```www``` path matcher, run:

    $ {command} MY-URL-MAP --hosts '*-foo.example.com,example.com' --path-matcher-name www
  """
  # pylint:enable=line-too-long

  URL_MAP_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.URL_MAP_ARG = flags.UrlMapArgument()
    cls.URL_MAP_ARG.AddArgument(parser)

    _Args(parser)

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

  def _Modify(self, client, args, existing):
    """Returns a modified URL map message."""
    replacement = encoding.CopyProtoMessage(existing)

    new_host_rule = client.messages.HostRule(
        description=args.description,
        hosts=sorted(args.hosts),
        pathMatcher=args.path_matcher_name)

    replacement.hostRules.append(new_host_rule)

    return replacement

  def Run(self, args):
    """Issues requests necessary to add host rule to the Url Map."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    url_map_ref = self.URL_MAP_ARG.ResolveAsResource(args, holder.resources)
    get_request = self._GetGetRequest(client, url_map_ref)

    objects = client.MakeRequests([get_request])

    new_object = self._Modify(client, args, objects[0])

    return client.MakeRequests(
        [self._GetSetRequest(client, url_map_ref, new_object)])


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class AddHostRuleAlpha(AddHostRule):
  # pylint:disable=line-too-long
  """Add a rule to a URL map to map hosts to a path matcher.

  *{command}* is used to add a mapping of hosts to a patch
  matcher in a URL map. The mapping will match the host
  component of HTTP requests to path matchers which in turn map
  the request to a backend service. Before adding a host rule,
  at least one path matcher must exist in the URL map to take
  care of the path component of the requests.
  `gcloud compute url-maps add-path-matcher` or
  `gcloud compute url-maps edit` can be used to add path matchers.

  ## EXAMPLES
  To create a host rule mapping the ```*-foo.example.com``` and
  ```example.com``` hosts to the ```www``` path matcher, run:

    $ {command} MY-URL-MAP --hosts '*-foo.example.com,example.com'
    --path-matcher-name www --global
  """
  # pylint:enable=line-too-long

  URL_MAP_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.URL_MAP_ARG = flags.UrlMapArgument(include_alpha=True)
    cls.URL_MAP_ARG.AddArgument(parser)

    _Args(parser)

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
    """Issues requests necessary to add host rule to the Url Map."""

    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    url_map_ref = self.URL_MAP_ARG.ResolveAsResource(args, holder.resources)
    if url_maps_utils.IsRegionalUrlMapRef(url_map_ref):
      get_request = self._GetRegionalGetRequest(client, url_map_ref)
    else:
      get_request = self._GetGetRequest(client, url_map_ref)

    old_url_map = client.MakeRequests([get_request])[0]
    modified_url_map = self._Modify(client, args, old_url_map)

    if url_maps_utils.IsRegionalUrlMapRef(url_map_ref):
      set_request = self._GetRegionalSetRequest(client, url_map_ref,
                                                modified_url_map)
    else:
      set_request = self._GetSetRequest(client, url_map_ref, modified_url_map)

    return client.MakeRequests([set_request])
