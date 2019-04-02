# -*- coding: utf-8 -*- #
# Copyright 2014 Google Inc. All Rights Reserved.
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

"""One-line documentation for auth module.

A detailed description of auth.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import json

from googlecloudsdk.core import config

from oauth2client import service_account


# TODO(b/33333969): Remove this after majority of users are past version.
# This class is not used anywhere. It is defined here as a fall back for
# legacy ServiceAccount credentials which when serialized would store its class
# path. If such credentials are loaded they will get deserilized via this class
# into oauth2client ServiceAccount, and on subsequent serialization will not use
# this class. In a way this will auto-upgrade credential format.
class ServiceAccountCredentials(service_account.ServiceAccountCredentials):

  @classmethod
  def from_json(cls, s):
    data = json.loads(s)
    return service_account.ServiceAccountCredentials.from_json({
        'client_id': data['_service_account_id'],
        '_service_account_email': data['_service_account_email'],
        '_private_key_id': data['_private_key_id'],
        '_private_key_pkcs8_pem': data['_private_key_pkcs8_text'],
        '_scopes': config.CLOUDSDK_SCOPES,
        '_user_agent': config.CLOUDSDK_USER_AGENT,
        'invalid': data['invalid'],
        'access_token': data['access_token'],
        'token_uri': data['token_uri'],
        'revoke_uri': data['revoke_uri'],
        'token_expiry': data['token_expiry'],
        '_kwargs': data['_kwargs'],
    })
