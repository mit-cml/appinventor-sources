# -*- coding: utf-8 -*- #
# Copyright 2015 Google Inc. All Rights Reserved.
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

"""Command to set IAM policy for a resource."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.iam import iam_util
from googlecloudsdk.command_lib.organizations import flags
from googlecloudsdk.command_lib.organizations import orgs_base


@base.ReleaseTracks(
    base.ReleaseTrack.GA, base.ReleaseTrack.BETA, base.ReleaseTrack.ALPHA)
class SetIamPolicy(orgs_base.OrganizationCommand):
  """Set IAM policy for an organization.

  Given an organization ID and a file encoded in JSON or YAML that contains the
  IAM policy, this command will set the IAM policy for that organization.
  """

  detailed_help = {
      'EXAMPLES': (
          '\n'.join([
              'The following command reads an IAM policy defined in a JSON',
              'file `policy.json` and sets it for an organization with the ID',
              '`123456789`:',
              '',
              '  $ {command} 123456789 policy.json',
          ]))
      }

  @staticmethod
  def Args(parser):
    flags.IdArg('whose IAM policy you want to set.').AddToParser(parser)
    parser.add_argument(
        'policy_file', help='JSON or YAML file containing the IAM policy.')

  def Run(self, args):
    messages = self.OrganizationsMessages()
    policy = iam_util.ParsePolicyFile(args.policy_file, messages.Policy)
    update_mask = iam_util.ConstructUpdateMaskFromPolicy(args.policy_file)

    # To preserve the existing set-iam-policy behavior of always overwriting
    # bindings and etag, add bindings and etag to update_mask.
    if 'bindings' not in update_mask:
      update_mask += ',bindings'
    if 'etag' not in update_mask:
      update_mask += ',etag'

    set_iam_policy_request = messages.SetIamPolicyRequest(
        policy=policy,
        updateMask=update_mask)

    policy_request = (
        messages.CloudresourcemanagerOrganizationsSetIamPolicyRequest(
            organizationsId=args.id,
            setIamPolicyRequest=set_iam_policy_request))
    result = self.OrganizationsClient().SetIamPolicy(policy_request)
    iam_util.LogSetIamPolicy(args.id, 'organization')
    return result
