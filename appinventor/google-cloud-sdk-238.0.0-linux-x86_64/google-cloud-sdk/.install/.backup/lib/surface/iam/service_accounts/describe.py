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

"""Command for getting service accounts."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import textwrap

from googlecloudsdk.api_lib.iam import util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.iam import iam_util


class Describe(base.DescribeCommand):
  """Show metadata for a service account from a project."""

  detailed_help = {
      'DESCRIPTION': textwrap.dedent("""\
          This command shows metadata for a service account.

          This call can fail for the following reasons:
              * The service account specified does not exist.
              * The active user does not have permission to access the given
                service account.
          """),
      'EXAMPLES': textwrap.dedent("""\
          To print metadata for a service account from your project, run:

            $ {command} my-iam-account@somedomain.com
          """),
  }

  @staticmethod
  def Args(parser):
    iam_util.AddServiceAccountNameArg(
        parser, action='to describe')

  def Run(self, args):
    # TODO(b/25212870): use resource parsing.
    client, messages = util.GetClientAndMessages()
    return client.projects_serviceAccounts.Get(
        messages.IamProjectsServiceAccountsGetRequest(
            name=iam_util.EmailToAccountResourceName(args.service_account)))
