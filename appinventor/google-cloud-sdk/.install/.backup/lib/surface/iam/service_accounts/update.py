# -*- coding: utf-8 -*- #
# Copyright 2018 Google Inc. All Rights Reserved.
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

"""Command for updating service accounts."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.iam import util
from googlecloudsdk.api_lib.util import http_retry
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.iam import iam_util
from googlecloudsdk.core import log

import six.moves.http_client


@base.ReleaseTracks(base.ReleaseTrack.GA, base.ReleaseTrack.BETA)
class Update(base.Command):
  """Update an IAM service account."""

  @staticmethod
  def Args(parser):
    parser.add_argument('--display-name',
                        help='The new textual name to display for the account.')

    iam_util.AddServiceAccountNameArg(
        parser, action='to update')

  @http_retry.RetryOnHttpStatus(six.moves.http_client.CONFLICT)
  def Run(self, args):
    resource_name = iam_util.EmailToAccountResourceName(args.service_account)
    client, messages = util.GetClientAndMessages()
    current = client.projects_serviceAccounts.Get(
        messages.IamProjectsServiceAccountsGetRequest(name=resource_name))

    result = client.projects_serviceAccounts.Update(
        messages.ServiceAccount(
            name=resource_name,
            etag=current.etag,
            displayName=args.display_name))
    log.UpdatedResource(args.service_account, kind='serviceAccount')
    return result
