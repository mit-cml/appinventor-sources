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
"""gcloud dns managed-zone update command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.dns import flags
from googlecloudsdk.command_lib.dns import resource_args
from googlecloudsdk.command_lib.dns import util as command_util
from googlecloudsdk.core import log


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class Update(base.UpdateCommand):
  """Update an existing Cloud DNS policy.

  Update an existing Cloud DNS policy.

  ## EXAMPLES

  To change the description of a policy, run:

    $ {command} mypolicy --description="Hello, world!"

  """

  def _FetchPolicy(self, policy_ref):
    """Get policy to be Updated."""
    client = apis.GetClientInstance('dns', 'v1beta2')
    m = apis.GetMessagesModule('dns', 'v1beta2')
    get_request = m.DnsPoliciesGetRequest(
        policy=policy_ref.Name(), project=policy_ref.project)
    return client.policies.Get(get_request)

  @staticmethod
  def Args(parser):
    resource_args.AddPolicyResourceArg(parser, verb=' to update.')
    flags.GetPolicyDescriptionArg().AddToParser(parser)
    flags.GetPolicyNetworksArg().AddToParser(parser)
    flags.GetPolicyInbboundForwardingArg().AddToParser(parser)
    flags.GetPolicyAltNameServersnArg().AddToParser(parser)
    parser.display_info.AddFormat('json')

  def Run(self, args):
    client = apis.GetClientInstance('dns', 'v1beta2')
    messages = apis.GetMessagesModule('dns', 'v1beta2')

    # Get Policy
    policy_ref = args.CONCEPTS.policy.Parse()
    to_update = self._FetchPolicy(policy_ref)

    if not (args.IsSpecified('networks') or args.IsSpecified('description') or
            args.IsSpecified('enable_inbound_forwarding') or
            args.IsSpecified('alternative_name_servers')):
      log.status.Print('Nothing to update.')
      return to_update

    if args.IsSpecified('networks'):
      if args.networks == ['']:
        args.networks = []
      to_update.networks = command_util.ParseNetworks(args.networks,
                                                      policy_ref.project)

    if args.IsSpecified('alternative_name_servers'):
      if args.alternative_name_servers == ['']:
        args.alternative_name_servers = []
      to_update.alternativeNameServerConfig = command_util.ParseAltNameServers(
          args.alternative_name_servers)

    if args.IsSpecified('enable_inbound_forwarding'):
      to_update.enableInboundForwarding = args.enable_inbound_forwarding
    if args.IsSpecified('description'):
      to_update.description = args.description

    update_req = messages.DnsPoliciesUpdateRequest(
        policy=to_update.name,
        policyResource=to_update,
        project=policy_ref.project)

    updated_policy = client.policies.Update(update_req).policy

    log.UpdatedResource(updated_policy.name, kind='Policy')

    return updated_policy
