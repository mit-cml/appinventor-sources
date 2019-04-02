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

"""Utilities for the iamcredentials API."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.util import apis_internal
from googlecloudsdk.core import resources

from oauth2client import client


def GenerateAccessToken(service_account_id, scopes):
  """Generates an access token for the given service account."""
  service_account_ref = resources.REGISTRY.Parse(
      service_account_id, collection='iamcredentials.serviceAccounts',
      params={'projectsId': '-', 'serviceAccountsId': service_account_id})

  # pylint: disable=protected-access
  iam_client = apis_internal._GetClientInstance(
      'iamcredentials', 'v1', allow_account_impersonation=False,
      force_resource_quota=True)
  response = iam_client.projects_serviceAccounts.GenerateAccessToken(
      iam_client.MESSAGES_MODULE
      .IamcredentialsProjectsServiceAccountsGenerateAccessTokenRequest(
          name=service_account_ref.RelativeName(),
          generateAccessTokenRequest=iam_client.MESSAGES_MODULE
          .GenerateAccessTokenRequest(scope=scopes)
      )
  )
  return response


class ImpersonationAccessTokenProvider(object):
  """A token provider for service account elevation.

  This supports the interface required by the core/credentials module.
  """

  def GetElevationAccessToken(self, service_account_id, scopes):
    response = GenerateAccessToken(service_account_id, scopes)
    return ImpersonationCredentials(
        service_account_id, response.accessToken, response.expireTime, scopes)


class ImpersonationCredentials(client.OAuth2Credentials):
  """Implementation of a credential that refreshes using the iamcredentials API.
  """

  def __init__(self, service_account_id, access_token, token_expiry, scopes):
    self._service_account_id = service_account_id
    super(ImpersonationCredentials, self).__init__(
        access_token, None, None, None, token_expiry, None, None, scopes=scopes)

  def _refresh(self, http):
    response = GenerateAccessToken(self._service_account_id, self.scopes)
    self.access_token = response.accessToken
    self.token_expiry = response.expireTime
