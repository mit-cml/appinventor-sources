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
"""API library for access context manager zones."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.accesscontextmanager import util
from googlecloudsdk.api_lib.util import waiter

from googlecloudsdk.core import resources as core_resources


class Client(object):
  """High-level API client for access context access zones."""

  def __init__(self, client=None, messages=None, version='v1'):
    self.client = client or util.GetClient(version=version)
    self.messages = messages or self.client.MESSAGES_MODULE
    self.include_unrestricted_services = {
        'v1': False,
        'v1alpha': True,
        'v1beta': True
    }[version]

  def Get(self, zone_ref):
    return self.client.accessPolicies_servicePerimeters.Get(
        self.messages
        .AccesscontextmanagerAccessPoliciesServicePerimetersGetRequest(
            name=zone_ref.RelativeName()))

  def Patch(self,
            perimeter_ref,
            description=None,
            title=None,
            perimeter_type=None,
            resources=None,
            restricted_services=None,
            unrestricted_services=None,
            levels=None):
    """Patch a service perimeter.

    Any non-None fields will be included in the update mask.

    Args:
      perimeter_ref: resources.Resource, reference to the perimeter to patch
      description: str, description of the zone or None if not updating
      title: str, title of the zone or None if not updating
      perimeter_type: PerimeterTypeValueValuesEnum type enum value for the level
        or None if not updating
      resources: list of str, the names of resources (for now, just
        'projects/...') in the zone or None if not updating.
      restricted_services: list of str, the names of services
        ('example.googleapis.com') that *are* restricted by the access zone or
        None if not updating.
      unrestricted_services: list of str, the names of services
        ('example.googleapis.com') that *are not* restricted by the access zone
        or None if not updating.
      levels: list of Resource, the access levels (in the same policy) that must
        be satisfied for calls into this zone or None if not updating.

    Returns:
      AccessZone, the updated access zone
    """
    m = self.messages
    perimeter = m.ServicePerimeter()

    update_mask = []

    if description is not None:
      update_mask.append('description')
      perimeter.description = description
    if title is not None:
      update_mask.append('title')
      perimeter.title = title
    if perimeter_type is not None:
      update_mask.append('perimeterType')
      perimeter.perimeterType = perimeter_type
    status = m.ServicePerimeterConfig()
    status_mutated = False
    if resources is not None:
      update_mask.append('status.resources')
      status.resources = resources
      status_mutated = True
    if self.include_unrestricted_services and unrestricted_services is not None:
      update_mask.append('status.unrestrictedServices')
      status.unrestrictedServices = unrestricted_services
      status_mutated = True
    if restricted_services is not None:
      update_mask.append('status.restrictedServices')
      status.restrictedServices = restricted_services
      status_mutated = True
    if levels is not None:
      update_mask.append('status.accessLevels')
      status.accessLevels = [l.RelativeName() for l in levels]
      status_mutated = True
    if status_mutated:
      perimeter.status = status

    update_mask.sort()  # For ease-of-testing

    request_type = (
        m.AccesscontextmanagerAccessPoliciesServicePerimetersPatchRequest)
    request = request_type(
        servicePerimeter=perimeter,
        name=perimeter_ref.RelativeName(),
        updateMask=','.join(update_mask),
    )

    operation = self.client.accessPolicies_servicePerimeters.Patch(request)
    poller = util.OperationPoller(self.client.accessPolicies_servicePerimeters,
                                  self.client.operations, perimeter_ref)
    operation_ref = core_resources.REGISTRY.Parse(
        operation.name, collection='accesscontextmanager.operations')
    return waiter.WaitFor(
        poller, operation_ref,
        'Waiting for PATCH operation [{}]'.format(operation_ref.Name()))
