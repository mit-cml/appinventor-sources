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

"""A hidden command that prints access tokens.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.auth import exceptions as auth_exceptions
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions as c_exc
from googlecloudsdk.core.credentials import store as c_store
from oauth2client import client


@base.Hidden
class AccessToken(base.Command):
  """Print an access token for the active account."""

  @staticmethod
  def Args(parser):
    parser.add_argument(
        'account', nargs='?',
        help=('The account to get the access token for. Leave empty for the '
              'active account.'))
    parser.display_info.AddFormat('value(access_token)')

  @c_exc.RaiseErrorInsteadOf(auth_exceptions.AuthenticationError, client.Error)
  def Run(self, args):
    """Run the helper command."""

    cred = c_store.Load(args.account)
    c_store.Refresh(cred)

    if not cred.access_token:
      raise auth_exceptions.InvalidCredentialsError(
          'No access token could be obtained from the current credentials.')
    return cred
