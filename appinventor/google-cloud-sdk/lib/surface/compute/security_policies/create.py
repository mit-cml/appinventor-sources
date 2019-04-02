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
"""Command for creating security policies."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import os

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute.security_policies import client
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute.security_policies import flags
from googlecloudsdk.command_lib.compute.security_policies import security_policies_utils
from googlecloudsdk.core.util import files


class Create(base.CreateCommand):
  """Create a Google Compute Engine security policy.

  *{command}* is used to create security policies. A security policy policy is a
  set of rules that controls access to various resources.
  """

  SECURITY_POLICY_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.SECURITY_POLICY_ARG = flags.SecurityPolicyArgument()
    cls.SECURITY_POLICY_ARG.AddArgument(parser, operation_type='create')

    group = parser.add_group(mutex=True, help='Creation options.')

    group.add_argument(
        '--description',
        help=('An optional, textual description for the security policy.'))

    group.add_argument(
        '--file-name',
        help=('The name of the JSON or YAML file to create a security policy '
              'config from.'))

    parser.add_argument(
        '--file-format',
        choices=['json', 'yaml'],
        help=(
            'The format of the file to create the security policy config from. '
            'Specify either yaml or json. Defaults to yaml if not specified. '
            'Will be ignored if --file-name is not specified.'))

    parser.display_info.AddCacheUpdater(flags.SecurityPoliciesCompleter)

  def Collection(self):
    return 'compute.securityPolicies'

  def _GetTemplateFromFile(self, args, messages):
    if not os.path.exists(args.file_name):
      raise exceptions.BadFileException('No such file [{0}]'.format(
          args.file_name))
    if os.path.isdir(args.file_name):
      raise exceptions.BadFileException('[{0}] is a directory'.format(
          args.file_name))
    try:
      with files.FileReader(args.file_name) as import_file:
        if args.file_format == 'json':
          return security_policies_utils.SecurityPolicyFromFile(
              import_file, messages, 'json')
        return security_policies_utils.SecurityPolicyFromFile(
            import_file, messages, 'yaml')
    except Exception as exp:
      exp_msg = getattr(exp, 'message', str(exp))
      msg = ('Unable to read security policy config from specified file '
             '[{0}] because [{1}]'.format(args.file_name, exp_msg))
      raise exceptions.BadFileException(msg)

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    ref = self.SECURITY_POLICY_ARG.ResolveAsResource(args, holder.resources)
    security_policy = client.SecurityPolicy(ref, compute_client=holder.client)

    if args.file_name:
      template = self._GetTemplateFromFile(args, holder.client.messages)
      template.name = ref.Name()
    else:
      template = holder.client.messages.SecurityPolicy(
          name=ref.Name(), description=args.description)

    return security_policy.Create(template)
