#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


"""Stub version of the capability service API."""








import copy
from google.appengine.api import apiproxy_stub
from google.appengine.api import capabilities as capabilities_lib



IsEnabledRequest = capabilities_lib.IsEnabledRequest
IsEnabledResponse = capabilities_lib.IsEnabledResponse
CapabilityConfig = capabilities_lib.CapabilityConfig




_SUPPORTED_CAPABILITIES = {
    'blobstore': {'*': CapabilityConfig.ENABLED},
    'datastore_v3': {'*': CapabilityConfig.ENABLED,
                     'write': CapabilityConfig.ENABLED},
    'images': {'*': CapabilityConfig.ENABLED},
    'mail': {'*': CapabilityConfig.ENABLED},
    'memcache': {'*': CapabilityConfig.ENABLED},
    'taskqueue': {'*': CapabilityConfig.ENABLED},
    'urlfetch': {'*': CapabilityConfig.ENABLED},
    'xmpp': {'*': CapabilityConfig.ENABLED}
}




_CONFIG_STATUS_TO_SUMMARY_STATUS = {
    CapabilityConfig.DISABLED: IsEnabledResponse.DISABLED,
    CapabilityConfig.ENABLED: IsEnabledResponse.ENABLED,
    CapabilityConfig.SCHEDULED: IsEnabledResponse.SCHEDULED_NOW,
    CapabilityConfig.UNKNOWN: IsEnabledResponse.UNKNOWN
}


class CapabilityServiceStub(apiproxy_stub.APIProxyStub):
  """Capability service stub."""

  THREADSAFE = True

  def __init__(self, service_name='capability_service'):
    """Constructor.

    Args:
      service_name: Service name expected for all calls.
    """
    super(CapabilityServiceStub, self).__init__(service_name)
    self._packages = copy.deepcopy(_SUPPORTED_CAPABILITIES)

  def SetPackageEnabled(self, package, enabled):
    """Set all features of a given package to enabled.

    This method is thread-unsafe, so should only be called during set-up, before
    multiple API server threads start.

    Args:
      package: Name of package.
      enabled: True to enable, False to disable.

    Raises:
      KeyError: When an unsupported package is requested.
    """
    if package not in _SUPPORTED_CAPABILITIES:


      raise KeyError(
          'Unsupported package. Received "%s". Must be one of %s.' %
          (package, self._packages.values()))

    status = CapabilityConfig.ENABLED if enabled else CapabilityConfig.DISABLED
    for capability in self._packages.get(package):
      self.SetCapabilityStatus(package, capability, status)

  def SetCapabilityStatus(self, package, capability, status):
    """Set the status of an individual capability.

    This method is thread-unsafe, so should only be called during set-up, before
    multiple API server threads start.

    Args:
      package: String name of package containing the capability.
      capability: String capability name.
      status: The CapabilityConfig status enum to set.

    Raises:
      KeyError: When an unsupported package or capability is requested.
    """


    if package not in _SUPPORTED_CAPABILITIES:
      raise KeyError(
          'Unsupported package. Received "%s". Must be one of %s.' %
          (package, self._packages.values()))

    if capability not in _SUPPORTED_CAPABILITIES[package]:
      raise KeyError(
          'Unsupported capability for package "%s". Received "%s". Must be one '
          'of %s.' % (package, capability, self._packages[package].values()))

    self._packages[package][capability] = status

  def _Dynamic_IsEnabled(self, request, response):
    """Implementation of CapabilityService::IsEnabled().

    Args:
      request: An IsEnabledRequest.
      response: An IsEnabledResponse.
    """
    capability_statuses = []
    for capability in request.capability_list():
      default_config = response.add_config()
      default_config.set_package(request.package())
      default_config.set_capability(capability)
      try:
        config_status = self._packages[request.package()][capability]
      except KeyError:
        config_status = CapabilityConfig.UNKNOWN
      default_config.set_status(config_status)
      capability_statuses.append(config_status)




    summary_status = _CONFIG_STATUS_TO_SUMMARY_STATUS[max(capability_statuses)]
    response.set_summary_status(summary_status)

  def _Dynamic_SetCapabilityStatus(self, request, response):
    """Implementation of CapabilityStubService::SetCapabilityStatus().

    Args:
      request: An IsEnabledRequest.
      response: An IsEnabledResponse.
    """
    self.SetCapabilityStatus(
        request.service_name(), request.call(), request.status())

  def Clear(self):
    self._packages = copy.deepcopy(_SUPPORTED_CAPABILITIES)
