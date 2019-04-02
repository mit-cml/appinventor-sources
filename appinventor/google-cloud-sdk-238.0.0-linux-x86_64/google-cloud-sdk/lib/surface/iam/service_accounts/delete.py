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

"""Command for deleting service accounts."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import textwrap

from googlecloudsdk.api_lib.iam import util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.iam import iam_util
from googlecloudsdk.core import log
from googlecloudsdk.core.console import console_io


class Delete(base.DeleteCommand):
  """Delete a service account from a project."""

  detailed_help = {
      'EXAMPLES': textwrap.dedent("""\
          To delete an service account from your project, run:

            $ {command} my-iam-account@somedomain.com
          """),
  }

  @staticmethod
  def Args(parser):
    iam_util.AddServiceAccountNameArg(
        parser, action='to delete')

  def Run(self, args):
    console_io.PromptContinue(
        message='You are about to delete service '
        'account [{0}].'.format(args.service_account),
        cancel_on_no=True)
    client, messages = util.GetClientAndMessages()
    client.projects_serviceAccounts.Delete(
        messages.IamProjectsServiceAccountsDeleteRequest(
            name=iam_util.EmailToAccountResourceName(args.service_account)))

    log.status.Print('deleted service account [{0}]'.format(
        args.service_account))
