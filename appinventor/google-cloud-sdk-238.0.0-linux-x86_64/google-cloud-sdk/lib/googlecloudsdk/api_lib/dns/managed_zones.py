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
"""API client library for Cloud DNS managed zones."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.util import apis


class Client(object):
  """API client for Cloud DNS managed zones."""

  _API_NAME = 'dns'

  def __init__(self, version, client, messages=None):
    self.version = version
    self.client = client
    self._service = self.client.managedZones
    self.messages = messages or self.client.MESSAGES_MODULE

  @classmethod
  def FromApiVersion(cls, version):
    return cls(version, apis.GetClientInstance(cls._API_NAME, version))

  def Get(self, zone_ref):
    return self._service.Get(
        self.messages.DnsManagedZonesGetRequest(
            project=zone_ref.project,
            managedZone=zone_ref.managedZone))

  def Patch(self,
            zone_ref,
            dnssec_config=None,
            description=None,
            labels=None,
            private_visibility_config=None,
            forwarding_config=None):
    """Managed Zones Update Request."""
    zone = self.messages.ManagedZone(
        name=zone_ref.Name(),
        dnssecConfig=dnssec_config,
        description=description,
        labels=labels)
    if private_visibility_config:
      zone.privateVisibilityConfig = private_visibility_config
    if forwarding_config:
      zone.forwardingConfig = forwarding_config
    return self._service.Patch(
        self.messages.DnsManagedZonesPatchRequest(
            managedZoneResource=zone,
            project=zone_ref.project,
            managedZone=zone_ref.Name()))
