# -*- coding: utf-8 -*- #
# Copyright 2016 Google Inc. All Rights Reserved.
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
"""Command to show metadata for a specified organization."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import textwrap

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.organizations import flags
from googlecloudsdk.command_lib.organizations import orgs_base


@base.ReleaseTracks(
    base.ReleaseTrack.GA, base.ReleaseTrack.BETA, base.ReleaseTrack.ALPHA)
class Describe(orgs_base.OrganizationCommand, base.DescribeCommand):
  """Show metadata for an organization.

  Shows metadata for an organization, given a valid organization ID.

  This command can fail for the following reasons:
      * The organization specified does not exist.
      * The active account does not have permission to access the given
        organization.
  """
  detailed_help = {
      'EXAMPLES': textwrap.dedent("""\
          The following command prints metadata for an organization with the
          ID `3589215982`:

            $ {command} 3589215982
    """),
  }

  @staticmethod
  def Args(parser):
    flags.IdArg('you want to describe.').AddToParser(parser)

  def Run(self, args):
    service = self.OrganizationsClient()
    ref = self.GetOrganizationRef(args.id)
    request = (service.client.MESSAGES_MODULE
               .CloudresourcemanagerOrganizationsGetRequest(
                   organizationsId=ref.organizationsId))
    return service.Get(request)
