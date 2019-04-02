# -*- coding: utf-8 -*- #
# Copyright 2017 Google Inc. All Rights Reserved.
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

"""A module to get an unauthenticated gRPC stub."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.core import http
from googlecloudsdk.core import properties
from googlecloudsdk.core.credentials import store as cred_store

import six

if six.PY2:
  # TODO(b/78118402): gRPC support on Python 3.
  # This doesn't work on py3 and nothing that calls it will work. We skip the
  # import here just so tests can load and be skipped without crashing.
  import grpc  # pylint: disable=g-import-not-at-top


class _MetadataPlugin(object):
  """Callable class to transform metadata for gRPC requests.

  credentials: oauth2client.client.OAuth2Credentials, The OAuth2 Credentials to
    use for creating access tokens.
  """

  def __init__(self, credentials):
    self._credentials = credentials

  def __call__(self, unused_context, callback):
    access_token = self._credentials.get_access_token().access_token
    headers = [
        ('authorization', 'Bearer ' + access_token),
    ]
    callback(headers, None)


def MakeSecureChannel(target):
  """Creates grpc secure channel.

  Args:
    target: str, The server address, for example:
      bigtableadmin.googleapis.com:443.

  Returns:
    grpc.secure channel.
  """

  credentials = cred_store.Load()
  # ssl_channel_credentials() loads root certificates from
  # `grpc/_adapter/credentials/roots.pem`.
  transport_creds = grpc.ssl_channel_credentials()
  custom_metadata_plugin = _MetadataPlugin(credentials)
  auth_creds = grpc.metadata_call_credentials(
      custom_metadata_plugin, name='google_creds')
  channel_creds = grpc.composite_channel_credentials(
      transport_creds, auth_creds)
  channel_args = (
      ('grpc.primary_user_agent',
       http.MakeUserAgentString(properties.VALUES.metrics.command_name.Get())),
  )
  return grpc.secure_channel(target, channel_creds,
                             options=channel_args)


def YieldFromList(list_method, request, items_field):
  """Yields items for a list request.

  Args:
    list_method: func(request), function takes in request, and returs a
      response with items_fiels repeated field.
    request: List method request proto payload.
    items_field: str, name of the field in the list_method response proto.

  Yields:
    proto messages from the items_field in the response.
  """
  while True:
    response = list_method(request)
    for r in getattr(response, items_field):
      yield r
    request.page_token = response.next_page_token
    if not request.page_token:
      break

