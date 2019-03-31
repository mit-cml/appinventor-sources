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
"""Command for describing target TCP proxies."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.target_tcp_proxies import flags


class Describe(base.DescribeCommand):
  """Display detailed information about a target TCP proxy."""

  TARGET_TCP_PROXY_ARG = None

  @staticmethod
  def Args(parser):
    Describe.TARGET_TCP_PROXY_ARG = flags.TargetTcpProxyArgument()
    Describe.TARGET_TCP_PROXY_ARG.AddArgument(parser, operation_type='describe')

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    ref = self.TARGET_TCP_PROXY_ARG.ResolveAsResource(args, holder.resources)

    client = holder.client.apitools_client
    messages = holder.client.messages

    request = messages.ComputeTargetTcpProxiesGetRequest(
        project=ref.project, targetTcpProxy=ref.Name())

    errors = []
    resources = holder.client.MakeRequests(
        [(client.targetTcpProxies, 'Get', request)], errors)

    if errors:
      utils.RaiseToolException(errors)
    return resources[0]


Describe.detailed_help = {
    'brief': 'Display detailed information about a target TCP proxy',
    'DESCRIPTION': """\
        *{command}* displays all data associated with a target TCP proxy
        in a project.
        """,
}
