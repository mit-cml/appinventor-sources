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
"""Network endpoint group api client."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import utils as api_utils
from googlecloudsdk.api_lib.compute.operations import poller
from googlecloudsdk.api_lib.util import waiter
from googlecloudsdk.command_lib.util.apis import arg_utils


class NetworkEndpointGroupsClient(object):
  """Client for network endpoint groups service in the GCE API."""

  def __init__(self, client, messages, resources):
    self.client = client
    self.messages = messages
    self.resources = resources
    self._service = self.client.apitools_client.networkEndpointGroups

  def Create(self, neg_ref, network_endpoint_type, default_port=None,
             network=None, subnet=None):
    """Creates a network endpoint group."""
    network_uri = None
    if network:
      network_ref = self.resources.Parse(network, {'project': neg_ref.project},
                                         collection='compute.networks')
      network_uri = network_ref.SelfLink()
    subnet_uri = None
    if subnet:
      region = api_utils.ZoneNameToRegionName(neg_ref.zone)
      subnet_ref = self.resources.Parse(
          subnet,
          {'project': neg_ref.project, 'region': region},
          collection='compute.subnetworks')
      subnet_uri = subnet_ref.SelfLink()

    endpoint_type_enum = (self.messages.NetworkEndpointGroup
                          .NetworkEndpointTypeValueValuesEnum)
    network_endpoint_group = self.messages.NetworkEndpointGroup(
        name=neg_ref.Name(),
        networkEndpointType=arg_utils.ChoiceToEnum(
            network_endpoint_type, endpoint_type_enum),
        loadBalancer=self.messages.NetworkEndpointGroupLbNetworkEndpointGroup(
            defaultPort=default_port,
            network=network_uri,
            subnetwork=subnet_uri))
    request = self.messages.ComputeNetworkEndpointGroupsInsertRequest(
        networkEndpointGroup=network_endpoint_group,
        project=neg_ref.project,
        zone=neg_ref.zone)

    return self.client.MakeRequests([(self._service, 'Insert', request)])[0]

  def AttachEndpoints(self, neg_ref, endpoints):
    """Attaches network endpoints to a network endpoint group."""
    request_class = (
        self.messages.ComputeNetworkEndpointGroupsAttachNetworkEndpointsRequest)
    nested_request_class = (
        self.messages.NetworkEndpointGroupsAttachEndpointsRequest)
    request = request_class(
        networkEndpointGroup=neg_ref.Name(),
        project=neg_ref.project,
        zone=neg_ref.zone,
        networkEndpointGroupsAttachEndpointsRequest=nested_request_class(
            networkEndpoints=self._GetEndpointMessageList(endpoints)))
    return self._service.AttachNetworkEndpoints(request)

  def DetachEndpoints(self, neg_ref, endpoints):
    """Detaches network endpoints to a network endpoint group."""
    request_class = (
        self.messages.ComputeNetworkEndpointGroupsDetachNetworkEndpointsRequest)
    nested_request_class = (
        self.messages.NetworkEndpointGroupsDetachEndpointsRequest)
    request = request_class(
        networkEndpointGroup=neg_ref.Name(),
        project=neg_ref.project,
        zone=neg_ref.zone,
        networkEndpointGroupsDetachEndpointsRequest=nested_request_class(
            networkEndpoints=self._GetEndpointMessageList(endpoints)))
    return self._service.DetachNetworkEndpoints(request)

  def _GetEndpointMessageList(self, endpoints):
    return [
        self.messages.NetworkEndpoint(
            instance=endpoint['instance'],
            ipAddress=endpoint['ip'] if 'ip' in endpoint else None,
            port=endpoint['port'] if 'port' in endpoint else None)
        for endpoint in endpoints]

  def _GetOperationsRef(self, operation):
    return self.resources.Parse(operation.selfLink,
                                collection='compute.zoneOperations')

  def _WaitForResult(self, operation_poller, operation_ref, message):
    if operation_ref:
      return waiter.WaitFor(operation_poller, operation_ref, message)
    return None

  def Update(self, neg_ref, add_endpoints=None, remove_endpoints=None):
    """Updates a Compute Network Endpoint Group."""
    attach_endpoints_ref = None
    detach_endpoints_ref = None

    if add_endpoints:
      operation = self.AttachEndpoints(neg_ref, add_endpoints)
      attach_endpoints_ref = self._GetOperationsRef(operation)

    if remove_endpoints:
      operation = self.DetachEndpoints(neg_ref, remove_endpoints)
      detach_endpoints_ref = self._GetOperationsRef(operation)

    neg_name = neg_ref.Name()
    operation_poller = poller.Poller(self._service)
    result = None
    result = self._WaitForResult(
        operation_poller, attach_endpoints_ref,
        'Attaching {0} endpoints to [{1}].'.format(
            len(add_endpoints) if add_endpoints else 0, neg_name)) or result
    result = self._WaitForResult(
        operation_poller, detach_endpoints_ref,
        'Detaching {0} endpoints from [{1}].'.format(
            len(remove_endpoints) if remove_endpoints else 0, neg_name)
    ) or result

    return result
