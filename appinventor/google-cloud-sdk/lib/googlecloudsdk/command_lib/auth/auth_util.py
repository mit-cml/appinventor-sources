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

"""Support library for the auth command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.core import properties
from googlecloudsdk.core.credentials import store as c_store


ACCOUNT_TABLE_FORMAT = ("""\
    table[title='Credentialed Accounts'](
        status.yesno(yes='*', no=''):label=ACTIVE,
        account
    )""")


class _AcctInfo(object):
  """An auth command resource list item.

  Attributes:
    account: The account name.
    status: The account status, one of ['ACTIVE', ''].
  """

  def __init__(self, account, active):
    self.account = account
    self.status = 'ACTIVE' if active else ''


def AllAccounts():
  """The resource list return value for the auth command Run() method."""
  active_account = properties.VALUES.core.account.Get()
  return [_AcctInfo(account, account == active_account)
          for account in c_store.AvailableAccounts()]
