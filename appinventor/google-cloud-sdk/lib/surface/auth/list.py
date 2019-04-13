# -*- coding: utf-8 -*- #
# Copyright 2013 Google Inc. All Rights Reserved.
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

"""Command to list the available accounts."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.auth import auth_util
from googlecloudsdk.core import log


class List(base.ListCommand):
  # pylint: disable=g-docstring-has-escape
  """Lists credentialed accounts.

  Lists accounts whose credentials have been obtained using `gcloud init`,
  `gcloud auth login` and `gcloud auth activate-service-account`, and shows
  which account is active. The active account is used by gcloud and other Cloud
  SDK tools to access Google Cloud Platform. While there is no limit on the
  number of accounts with stored credentials, there is only one active account.

  ## EXAMPLES

  To set an existing account to be the current active account, run:

    $ gcloud config set core/account your-email-account@gmail.com

  If you don't have an existing account, create one using:

    $ gcloud init

  To list the active account name:

    $ gcloud auth list --filter=status:ACTIVE --format="value(account)"

  To list the inactive account names with prefix `test`:

    $ gcloud auth list --filter="-status:ACTIVE account:test*" \
--format="value(account)"
  """

  @staticmethod
  def Args(parser):
    base.URI_FLAG.RemoveFromParser(parser)
    parser.add_argument(
        '--filter-account',
        help="""\
        List only credentials for one account. Use
        --filter="account~_PATTERN_" to select accounts that match
        _PATTERN_.""")
    parser.display_info.AddFormat(auth_util.ACCOUNT_TABLE_FORMAT)

  def Run(self, args):
    accounts = auth_util.AllAccounts()
    if args.filter_account:
      accounts = [a for a in accounts if a.account == args.filter_account]
    return accounts

  def Epilog(self, resources_were_displayed):
    if resources_were_displayed:
      log.status.Print("""
To set the active account, run:
    $ gcloud config set account `ACCOUNT`
""")
    else:
      log.status.Print("""
No credentialed accounts.

To login, run:
  $ gcloud auth login `ACCOUNT`
""")
