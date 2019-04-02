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

"""Revoke credentials being used by the CloudSDK."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions as c_exc
from googlecloudsdk.command_lib.auth import auth_util
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.credentials import store as c_store
from googlecloudsdk.core.resource import resource_printer


class Revoke(base.Command):
  """Revoke access credentials for an account.

  Revokes credentials for the specified user accounts or service accounts.

  When given a user account, this command revokes the user account token on the
  server. If the revocation is successful, or if the token has already been
  revoked, this command removes the credential from the local machine.

  When given a service account, this command does not revoke the service account
  token on the server because service account tokens are not revocable. Instead,
  it will print a warning and remove the credential from the local machine.

  If no account is specified, this command revokes credentials for the currently
  active account, effectively logging out of that account. If --all is given,
  the behaviors described above apply individually to each account in the list.

  You can revoke credentials when you want to prevent gcloud and other Cloud
  SDK tools from using the specified account. You do not need to revoke
  credentials to switch between accounts.
  """

  @staticmethod
  def Args(parser):
    parser.add_argument('accounts', nargs='*',
                        help='Accounts whose credentials are to be revoked.')
    parser.add_argument('--all', action='store_true',
                        help='Revoke credentials for all accounts.')
    parser.display_info.AddFormat('list[title="Revoked credentials:"]')

  def Run(self, args):
    """Revoke credentials and update active account."""
    accounts = args.accounts or []
    if isinstance(accounts, str):
      accounts = [accounts]
    available_accounts = c_store.AvailableAccounts()
    unknown_accounts = set(accounts) - set(available_accounts)
    if unknown_accounts:
      raise c_exc.UnknownArgumentException(
          'accounts', ' '.join(unknown_accounts))
    if args.all:
      accounts = available_accounts

    active_account = properties.VALUES.core.account.Get()

    if not accounts and active_account:
      accounts = [active_account]

    if not accounts:
      raise c_exc.InvalidArgumentException(
          'accounts', 'No credentials available to revoke.')

    for account in accounts:
      if active_account == account:
        properties.PersistProperty(properties.VALUES.core.account, None)
      if not c_store.Revoke(account):
        if account.endswith('.gserviceaccount.com'):
          log.warning(
              '[{}] appears to be a service account. Service account tokens '
              'cannot be revoked, but they will expire automatically. To '
              'prevent use of the service account token earlier than the '
              'expiration, revoke the parent service account or service '
              'account key.'.format(account))
        else:
          log.warning(
              '[{}] already inactive (previously revoked?)'.format(account))
    return accounts

  def Epilog(self, unused_results_were_displayed):
    accounts = auth_util.AllAccounts()
    printer = resource_printer.Printer(
        auth_util.ACCOUNT_TABLE_FORMAT,
        out=log.status)
    printer.Print(accounts)
