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

"""Command for deleting security policies rules."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.api_lib.compute.security_policies import client
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.security_policies import flags as security_policies_flags
from googlecloudsdk.command_lib.compute.security_policies.rules import flags
from googlecloudsdk.core import properties


class Delete(base.DeleteCommand):
  """Delete Google Compute Engine security policy rules.

  *{command}* is used to delete security policy rules.
  """

  SECURITY_POLICY_ARG = None

  @classmethod
  def Args(cls, parser):
    flags.AddPriority(parser, 'delete', is_plural=True)
    cls.SECURITY_POLICY_ARG = (
        security_policies_flags.SecurityPolicyArgumentForRules())
    cls.SECURITY_POLICY_ARG.AddArgument(parser)
    parser.display_info.AddCacheUpdater(
        security_policies_flags.SecurityPoliciesCompleter)

  def Collection(self):
    return 'compute.securityPolicyRules'

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    refs = []
    for name in args.names:
      refs.append(holder.resources.Parse(
          name,
          collection=self.Collection(),
          params={
              'project': properties.VALUES.core.project.GetOrFail,
              'securityPolicy': args.security_policy
          }))
    utils.PromptForDeletion(refs)

    requests = []
    for ref in refs:
      security_policy_rule = client.SecurityPolicyRule(
          ref, compute_client=holder.client)
      requests.extend(security_policy_rule.Delete(only_generate_request=True))

    return holder.client.MakeRequests(requests)
