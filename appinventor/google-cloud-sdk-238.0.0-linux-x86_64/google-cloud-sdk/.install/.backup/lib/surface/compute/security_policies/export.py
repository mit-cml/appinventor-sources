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

"""Command for exporting security policy configs to a file."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute.security_policies import client
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute.security_policies import flags
from googlecloudsdk.command_lib.compute.security_policies import (
    security_policies_utils)
from googlecloudsdk.core import log
from googlecloudsdk.core.util import files


class Export(base.Command):
  """Export security policy configs into yaml files."""

  SECURITY_POLICY_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.SECURITY_POLICY_ARG = flags.SecurityPolicyArgument()
    cls.SECURITY_POLICY_ARG.AddArgument(parser, operation_type='export')

    parser.add_argument(
        '--file-name',
        required=True,
        help='The name of the file to export the security policy config to.')

    parser.add_argument(
        '--file-format',
        choices=['json', 'yaml'],
        help=(
            'The format of the file to export the security policy config to. '
            'Specify either yaml or json. Defaults to yaml if not specified.'))

  def Run(self, args):
    # Get the security policy.
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    ref = self.SECURITY_POLICY_ARG.ResolveAsResource(args, holder.resources)

    requests = []
    security_policy = client.SecurityPolicy(ref, compute_client=holder.client)
    requests.extend(security_policy.Describe(only_generate_request=True))
    resources = holder.client.MakeRequests(requests)

    # Export the security policy.
    try:
      with files.FileWriter(args.file_name) as export_file:
        if args.file_format == 'json':
          security_policies_utils.WriteToFile(export_file, resources[0], 'json')
        else:
          security_policies_utils.WriteToFile(export_file, resources[0], 'yaml')
    except EnvironmentError as exp:
      msg = 'Unable to export security policy to file [{0}]: {1}'.format(
          args.file_name, exp)
      raise exceptions.BadFileException(msg)

    log.status.Print(
        'Exported security policy to [{0}].'.format(args.file_name))
