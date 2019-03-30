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
"""Command for modifying the target of forwarding rules."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import forwarding_rules_utils as utils
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute.forwarding_rules import flags


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Set(base.UpdateCommand):
  """Modify a forwarding rule to direct network traffic to a new target."""

  FORWARDING_RULE_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.FORWARDING_RULE_ARG = flags.ForwardingRuleArgument()
    flags.AddUpdateArgs(parser, include_beta=False)
    cls.FORWARDING_RULE_ARG.AddArgument(parser)

  def Run(self, args):
    """Issues requests necessary to set target on Forwarding Rule."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    forwarding_rule_ref = self.FORWARDING_RULE_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(client))

    if forwarding_rule_ref.Collection() == 'compute.globalForwardingRules':
      requests = self.CreateGlobalRequests(client, holder.resources,
                                           forwarding_rule_ref, args)
    elif forwarding_rule_ref.Collection() == 'compute.forwardingRules':
      requests = self.CreateRegionalRequests(client, holder.resources,
                                             forwarding_rule_ref, args)

    return client.MakeRequests(requests)

  def CreateGlobalRequests(self, client, resources, forwarding_rule_ref, args):
    """Create a globally scoped request."""
    target_ref = utils.GetGlobalTarget(resources, args)

    request = client.messages.ComputeGlobalForwardingRulesSetTargetRequest(
        forwardingRule=forwarding_rule_ref.Name(),
        project=forwarding_rule_ref.project,
        targetReference=client.messages.TargetReference(
            target=target_ref.SelfLink(),
        ),
    )

    return [(client.apitools_client.globalForwardingRules, 'SetTarget',
             request)]

  def CreateRegionalRequests(self, client, resources, forwarding_rule_ref,
                             args):
    """Create a regionally scoped request."""
    target_ref, _ = utils.GetRegionalTarget(
        client, resources, args, forwarding_rule_ref=forwarding_rule_ref)

    request = client.messages.ComputeForwardingRulesSetTargetRequest(
        forwardingRule=forwarding_rule_ref.Name(),
        project=forwarding_rule_ref.project,
        region=forwarding_rule_ref.region,
        targetReference=client.messages.TargetReference(
            target=target_ref.SelfLink(),
        ),
    )

    return [(client.apitools_client.forwardingRules, 'SetTarget', request)]


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class SetBeta(Set):
  """Modify a forwarding rule to direct network traffic to a new target."""

  @classmethod
  def Args(cls, parser):
    cls.FORWARDING_RULE_ARG = flags.ForwardingRuleArgument()
    flags.AddUpdateArgs(parser, include_beta=True)
    cls.FORWARDING_RULE_ARG.AddArgument(parser)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class SetAlpha(Set):
  """Modify a forwarding rule to direct network traffic to a new target."""

  @classmethod
  def Args(cls, parser):
    cls.FORWARDING_RULE_ARG = flags.ForwardingRuleArgument()
    flags.AddUpdateArgs(parser, include_beta=True, include_alpha=True)
    cls.FORWARDING_RULE_ARG.AddArgument(parser)


Set.detailed_help = {
    'DESCRIPTION': ("""\
        *{{command}}* is used to set a new target for a forwarding
        rule. {overview}

        When creating a forwarding rule, exactly one of  ``--target-instance'',
        ``--target-pool'', ``--target-http-proxy'', ``--target-https-proxy'',
        ``--target-ssl-proxy'', ``--target-tcp-proxy'' or
        ``--target-vpn-gateway'' must be specified.""".format(
            overview=flags.FORWARDING_RULES_OVERVIEW)),
}


SetBeta.detailed_help = Set.detailed_help
SetAlpha.detailed_help = Set.detailed_help
