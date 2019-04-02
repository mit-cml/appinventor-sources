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

"""A docker credential helper that provides credentials for GCR registries."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import datetime
import sys

from googlecloudsdk.calliope import base
from googlecloudsdk.core import exceptions
from googlecloudsdk.core.credentials import store as c_store
from googlecloudsdk.core.docker import credential_utils


@base.Hidden
@base.ReleaseTracks(base.ReleaseTrack.GA)
class DockerHelper(base.Command):
  """A Docker credential helper to provide access to GCR repositories."""

  GET = 'get'
  LIST = 'list'

  @staticmethod
  def Args(parser):
    parser.add_argument('method', help='The docker credential helper method.')
    # Docker expects the result in json format.
    parser.display_info.AddFormat('json')

  def Run(self, args):
    """Run the helper command."""

    if args.method == DockerHelper.LIST:
      return {
          # This tells Docker that the secret will be an access token, not a
          # username/password.
          # Docker normally expects a prefixed 'https://' for auth configs.
          ('https://' + url): '_dcgcloud_token'
          for url in credential_utils.DefaultAuthenticatedRegistries()
      }

    elif args.method == DockerHelper.GET:
      cred = c_store.Load()
      if (not cred.token_expiry or cred.token_expiry.utcnow() >
          cred.token_expiry - datetime.timedelta(minutes=55)):
        c_store.Refresh(cred)
      url = sys.stdin.read().strip()
      if (url.replace('https://', '',
                      1) not in credential_utils.SupportedRegistries()):
        raise exceptions.Error(
            'Repository url [{url}] is not supported'.format(url=url))
      # Putting an actual username in the response doesn't work. Docker will
      # then prompt for a password instead of using the access token.
      return {
          'Secret': cred.access_token,
          'Username': '_dcgcloud_token',
      }

    # Don't print anything if we are not supporting the given action.
    # The credential helper protocol also support 'store' and 'erase' actions
    # that don't apply here. The full spec can be found here:
    # https://github.com/docker/docker-credential-helpers#development
    args.GetDisplayInfo().AddFormat('none')
    return None
