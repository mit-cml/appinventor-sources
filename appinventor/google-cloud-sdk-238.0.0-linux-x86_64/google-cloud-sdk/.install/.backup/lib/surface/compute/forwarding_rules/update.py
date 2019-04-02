# -*- coding: utf-8 -*- #
# Copyright 2017 Google Inc. All Rights Reserved.
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
"""Command to update forwarding-rules."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import constants
from googlecloudsdk.api_lib.compute.operations import poller
from googlecloudsdk.api_lib.util import waiter
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions as calliope_exceptions
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute.forwarding_rules import flags
from googlecloudsdk.command_lib.util.args import labels_util


def _Args(cls, parser):
  cls.FORWARDING_RULE_ARG = flags.ForwardingRuleArgument()
  cls.FORWARDING_RULE_ARG.AddArgument(parser)
  labels_util.AddUpdateLabelsFlags(parser)


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class Update(base.UpdateCommand):
  r"""Update a Google Compute Engine forwarding rule.

  *{command}* updates labels for a Google Compute Engine
  forwarding rule.  For example:

    $ {command} example-fr --region us-central1 \
      --update-labels=k0=value1,k1=value2 --remove-labels=k3

  will add/update labels ``k0'' and ``k1'' and remove labels with key ``k3''.

  Labels can be used to identify the forwarding rule and to filter them as in

    $ {parent_command} list --filter='labels.k1:value2'

  To list existing labels

    $ {parent_command} describe example-fr --format='default(labels)'

  """

  FORWARDING_RULE_ARG = None

  @classmethod
  def Args(cls, parser):
    _Args(cls, parser)

  def _CreateGlobalSetLabelsRequest(self, messages, forwarding_rule_ref,
                                    forwarding_rule, replacement):
    return messages.ComputeGlobalForwardingRulesSetLabelsRequest(
        project=forwarding_rule_ref.project,
        resource=forwarding_rule_ref.Name(),
        globalSetLabelsRequest=messages.GlobalSetLabelsRequest(
            labelFingerprint=forwarding_rule.labelFingerprint,
            labels=replacement))

  def _CreateRegionalSetLabelsRequest(self, messages, forwarding_rule_ref,
                                      forwarding_rule, replacement):
    return messages.ComputeForwardingRulesSetLabelsRequest(
        project=forwarding_rule_ref.project,
        resource=forwarding_rule_ref.Name(),
        region=forwarding_rule_ref.region,
        regionSetLabelsRequest=messages.RegionSetLabelsRequest(
            labelFingerprint=forwarding_rule.labelFingerprint,
            labels=replacement))

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client.apitools_client
    messages = holder.client.messages

    forwarding_rule_ref = self.FORWARDING_RULE_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(holder.client))

    labels_diff = labels_util.Diff.FromUpdateArgs(args)
    if not labels_diff.MayHaveUpdates():
      raise calliope_exceptions.RequiredArgumentException(
          'LABELS', 'At least one of --update-labels or '
          '--remove-labels must be specified.')

    if forwarding_rule_ref.Collection() == 'compute.globalForwardingRules':
      forwarding_rule = client.globalForwardingRules.Get(
          messages.ComputeGlobalForwardingRulesGetRequest(
              **forwarding_rule_ref.AsDict()))
      labels_value = messages.GlobalSetLabelsRequest.LabelsValue
    else:
      forwarding_rule = client.forwardingRules.Get(
          messages.ComputeForwardingRulesGetRequest(
              **forwarding_rule_ref.AsDict()))
      labels_value = messages.RegionSetLabelsRequest.LabelsValue

    labels_update = labels_diff.Apply(labels_value, forwarding_rule.labels)

    if not labels_update.needs_update:
      return forwarding_rule

    if forwarding_rule_ref.Collection() == 'compute.globalForwardingRules':
      request = self._CreateGlobalSetLabelsRequest(
          messages, forwarding_rule_ref, forwarding_rule, labels_update.labels)

      operation = client.globalForwardingRules.SetLabels(request)
      operation_ref = holder.resources.Parse(
          operation.selfLink, collection='compute.globalOperations')

      operation_poller = poller.Poller(client.globalForwardingRules)
    else:
      request = self._CreateRegionalSetLabelsRequest(
          messages, forwarding_rule_ref, forwarding_rule, labels_update.labels)

      operation = client.forwardingRules.SetLabels(request)
      operation_ref = holder.resources.Parse(
          operation.selfLink, collection='compute.regionOperations')

      operation_poller = poller.Poller(client.forwardingRules)

    return waiter.WaitFor(operation_poller, operation_ref,
                          'Updating labels of forwarding rule [{0}]'.format(
                              forwarding_rule_ref.Name()))


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class UpdateAlpha(Update):
  r"""Update a Google Compute Engine forwarding rule.

  *{command}* updates labels and network tier for a Google Compute Engine
  forwarding rule.

  Example to update labels:

    $ {command} example-fr --region us-central1 \
      --update-labels=k0=value1,k1=value2 --remove-labels=k3

  will add/update labels ``k0'' and ``k1'' and remove labels with key ``k3''.

  Labels can be used to identify the forwarding rule and to filter them as in

    $ {parent_command} list --filter='labels.k1:value2'

  To list existing labels

    $ {parent_command} describe example-fr --format='default(labels)'

  """

  @classmethod
  def Args(cls, parser):
    _Args(cls, parser)
    flags.AddNetworkTier(
        parser, supports_network_tier_flag=True, for_update=True)

  def ConstructNetworkTier(self, messages, network_tier):
    if network_tier:
      network_tier = network_tier.upper()
      if network_tier in constants.NETWORK_TIER_CHOICES_FOR_INSTANCE:
        return messages.ForwardingRule.NetworkTierValueValuesEnum(network_tier)
      else:
        raise calliope_exceptions.InvalidArgumentException(
            '--network-tier',
            'Invalid network tier [{tier}]'.format(tier=network_tier))
    else:
      return

  def Modify(self, messages, args, existing):
    """Returns a modified forwarding rule message and included fields."""
    if args.network_tier is None:
      return None
    else:
      return messages.ForwardingRule(
          name=existing.name,
          networkTier=self.ConstructNetworkTier(messages, args.network_tier))

  def Run(self, args):
    """Returns a list of requests necessary for updating forwarding rules."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client.apitools_client
    messages = holder.client.messages

    forwarding_rule_ref = self.FORWARDING_RULE_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(holder.client))

    labels_diff = labels_util.Diff.FromUpdateArgs(args)
    if not labels_diff.MayHaveUpdates() and args.network_tier is None:
      raise calliope_exceptions.ToolException(
          'At least one property must be specified.')

    # Get replacement.
    if forwarding_rule_ref.Collection() == 'compute.globalForwardingRules':
      get_request = (client.globalForwardingRules, 'Get',
                     messages.ComputeGlobalForwardingRulesGetRequest(
                         forwardingRule=forwarding_rule_ref.Name(),
                         project=forwarding_rule_ref.project))
      labels_value = messages.GlobalSetLabelsRequest.LabelsValue
    else:
      get_request = (client.forwardingRules, 'Get',
                     messages.ComputeForwardingRulesGetRequest(
                         forwardingRule=forwarding_rule_ref.Name(),
                         project=forwarding_rule_ref.project,
                         region=forwarding_rule_ref.region))
      labels_value = messages.RegionSetLabelsRequest.LabelsValue

    objects = holder.client.MakeRequests([get_request])
    forwarding_rule = objects[0]

    forwarding_rule_replacement = self.Modify(messages, args, forwarding_rule)
    label_update = labels_diff.Apply(labels_value, forwarding_rule.labels)

    # Create requests.
    requests = []

    if forwarding_rule_ref.Collection() == 'compute.globalForwardingRules':
      if forwarding_rule_replacement:
        request = messages.ComputeGlobalForwardingRulesPatchRequest(
            forwardingRule=forwarding_rule_ref.Name(),
            forwardingRuleResource=forwarding_rule_replacement,
            project=forwarding_rule_ref.project)
        requests.append((client.globalForwardingRules, 'Patch', request))
      if label_update.needs_update:
        request = self._CreateGlobalSetLabelsRequest(
            messages, forwarding_rule_ref, forwarding_rule, label_update.labels)
        requests.append((client.globalForwardingRules, 'SetLabels', request))
    else:
      if forwarding_rule_replacement:
        request = messages.ComputeForwardingRulesPatchRequest(
            forwardingRule=forwarding_rule_ref.Name(),
            forwardingRuleResource=forwarding_rule_replacement,
            project=forwarding_rule_ref.project,
            region=forwarding_rule_ref.region)
        requests.append((client.forwardingRules, 'Patch', request))
      if label_update.needs_update:
        request = self._CreateRegionalSetLabelsRequest(
            messages, forwarding_rule_ref, forwarding_rule, label_update.labels)
        requests.append((client.forwardingRules, 'SetLabels', request))

    return holder.client.MakeRequests(requests)
