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
"""Command for describing target HTTP proxies."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute.target_http_proxies import flags
from googlecloudsdk.command_lib.compute.target_http_proxies import target_http_proxies_utils


@base.ReleaseTracks(base.ReleaseTrack.GA, base.ReleaseTrack.BETA)
class Describe(base.DescribeCommand):
  """Display detailed information about a target HTTP proxy.

  *{command}* displays all data associated with a target HTTP proxy
  in a project.
  """

  TARGET_HTTP_PROXY_ARG = None

  @staticmethod
  def Args(parser):
    Describe.TARGET_HTTP_PROXY_ARG = flags.TargetHttpProxyArgument()
    Describe.TARGET_HTTP_PROXY_ARG.AddArgument(
        parser, operation_type='describe')

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    target_http_proxy_ref = self.TARGET_HTTP_PROXY_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(client))

    request = client.messages.ComputeTargetHttpProxiesGetRequest(
        **target_http_proxy_ref.AsDict())

    return client.MakeRequests([(client.apitools_client.targetHttpProxies,
                                 'Get', request)])[0]


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class DescribeAlpha(Describe):
  """Display detailed information about a target HTTP proxy.

  *{command}* displays all data associated with a target HTTP proxy
  in a project.
  """

  TARGET_HTTP_PROXY_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.TARGET_HTTP_PROXY_ARG = flags.TargetHttpProxyArgument(
        include_alpha=True)
    cls.TARGET_HTTP_PROXY_ARG.AddArgument(parser, operation_type='describe')

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    target_http_proxy_ref = self.TARGET_HTTP_PROXY_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(client))

    if target_http_proxies_utils.IsRegionalTargetHttpProxiesRef(
        target_http_proxy_ref):
      request = client.messages.ComputeRegionTargetHttpProxiesGetRequest(
          **target_http_proxy_ref.AsDict())
      collection = client.apitools_client.regionTargetHttpProxies
    else:
      request = client.messages.ComputeTargetHttpProxiesGetRequest(
          **target_http_proxy_ref.AsDict())
      collection = client.apitools_client.targetHttpProxies

    return client.MakeRequests([(collection, 'Get', request)])[0]
