# -*- coding: utf-8 -*- #
# Copyright 2015 Google Inc. All Rights Reserved.
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
"""Code that's shared between multiple subnets subcommands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import exceptions as calliope_exceptions
import six


def MakeSubnetworkUpdateRequest(client,
                                subnet_ref,
                                enable_private_ip_google_access=None,
                                add_secondary_ranges=None,
                                remove_secondary_ranges=None,
                                enable_flow_logs=None,
                                aggregation_interval=None,
                                flow_sampling=None,
                                metadata=None,
                                set_role_active=None,
                                drain_timeout_seconds=None,
                                enable_private_ipv6_access=None,
                                private_ipv6_google_access_type=None):
  """Make the appropriate update request for the args.

  Args:
    client: GCE API client
    subnet_ref: Reference to a subnetwork
    enable_private_ip_google_access: Enable/disable access to Google Cloud APIs
      from this subnet for instances without a public ip address.
    add_secondary_ranges: List of secondary IP ranges to add to the subnetwork
      for use in IP aliasing.
    remove_secondary_ranges: List of secondary ranges to remove from the
      subnetwork.
    enable_flow_logs: Enable/disable flow logging for this subnet.
    aggregation_interval: The internal at which to aggregate flow logs.
    flow_sampling: The sampling rate for flow logging in this subnet.
    metadata: Whether metadata fields should be added reported flow logs.
    set_role_active: Updates the role of a BACKUP subnet to ACTIVE.
    drain_timeout_seconds: The maximum amount of time to drain connections from
      the active subnet to the backup subnet with set_role_active=True.
    enable_private_ipv6_access: Enable/disable private IPv6 access for the
      subnet.
    private_ipv6_google_access_type: The private IPv6 google access type for the
      VMs in this subnet.

  Returns:
    response, result of sending the update request for the subnetwork
  """
  convert_to_enum = lambda x: x.replace('-', '_').upper()
  if enable_private_ip_google_access is not None:
    google_access = (
        client.messages.SubnetworksSetPrivateIpGoogleAccessRequest())
    google_access.privateIpGoogleAccess = enable_private_ip_google_access

    google_access_request = (
        client.messages.ComputeSubnetworksSetPrivateIpGoogleAccessRequest(
            project=subnet_ref.project,
            region=subnet_ref.region,
            subnetwork=subnet_ref.Name(),
            subnetworksSetPrivateIpGoogleAccessRequest=google_access))
    return client.MakeRequests([(client.apitools_client.subnetworks,
                                 'SetPrivateIpGoogleAccess',
                                 google_access_request)])
  elif add_secondary_ranges is not None:
    subnetwork = client.MakeRequests(
        [(client.apitools_client.subnetworks,
          'Get', client.messages.ComputeSubnetworksGetRequest(
              **subnet_ref.AsDict()))])[0]

    for secondary_range in add_secondary_ranges:
      for range_name, ip_cidr_range in sorted(six.iteritems(secondary_range)):
        subnetwork.secondaryIpRanges.append(
            client.messages.SubnetworkSecondaryRange(
                rangeName=range_name, ipCidrRange=ip_cidr_range))

    return client.MakeRequests(
        [CreateSubnetworkPatchRequest(client, subnet_ref, subnetwork)])
  elif remove_secondary_ranges is not None:
    subnetwork = client.MakeRequests(
        [(client.apitools_client.subnetworks,
          'Get', client.messages.ComputeSubnetworksGetRequest(
              **subnet_ref.AsDict()))])[0]

    for name in remove_secondary_ranges[0]:
      if name not in [r.rangeName for r in subnetwork.secondaryIpRanges]:
        raise calliope_exceptions.UnknownArgumentException(
            'remove-secondary-ranges', 'Subnetwork does not have a range {}, '
            'present ranges are {}.'.format(
                name, [r.rangeName for r in subnetwork.secondaryIpRanges]))
    subnetwork.secondaryIpRanges = [
        r for r in subnetwork.secondaryIpRanges
        if r.rangeName not in remove_secondary_ranges[0]
    ]

    cleared_fields = []
    if not subnetwork.secondaryIpRanges:
      cleared_fields.append('secondaryIpRanges')
    with client.apitools_client.IncludeFields(cleared_fields):
      return client.MakeRequests(
          [CreateSubnetworkPatchRequest(client, subnet_ref, subnetwork)])
  elif enable_flow_logs is not None:
    subnetwork = client.messages.Subnetwork()
    subnetwork.fingerprint = client.MakeRequests([
        (client.apitools_client.subnetworks, 'Get',
         client.messages.ComputeSubnetworksGetRequest(**subnet_ref.AsDict()))
    ])[0].fingerprint

    subnetwork.enableFlowLogs = enable_flow_logs
    return client.MakeRequests(
        [CreateSubnetworkPatchRequest(client, subnet_ref, subnetwork)])
  elif aggregation_interval is not None:
    subnetwork = client.messages.Subnetwork()
    subnetwork.fingerprint = client.MakeRequests([
        (client.apitools_client.subnetworks, 'Get',
         client.messages.ComputeSubnetworksGetRequest(**subnet_ref.AsDict()))
    ])[0].fingerprint

    subnetwork.aggregationInterval = (
        client.messages.Subnetwork.AggregationIntervalValueValuesEnum(
            convert_to_enum(aggregation_interval)))
    return client.MakeRequests(
        [CreateSubnetworkPatchRequest(client, subnet_ref, subnetwork)])
  elif flow_sampling is not None:
    subnetwork = client.messages.Subnetwork()
    subnetwork.fingerprint = client.MakeRequests([
        (client.apitools_client.subnetworks, 'Get',
         client.messages.ComputeSubnetworksGetRequest(**subnet_ref.AsDict()))
    ])[0].fingerprint

    subnetwork.flowSampling = flow_sampling
    return client.MakeRequests(
        [CreateSubnetworkPatchRequest(client, subnet_ref, subnetwork)])
  elif metadata is not None:
    subnetwork = client.messages.Subnetwork()
    subnetwork.fingerprint = client.MakeRequests([
        (client.apitools_client.subnetworks, 'Get',
         client.messages.ComputeSubnetworksGetRequest(**subnet_ref.AsDict()))
    ])[0].fingerprint

    subnetwork.metadata = client.messages.Subnetwork.MetadataValueValuesEnum(
        convert_to_enum(metadata))
    return client.MakeRequests(
        [CreateSubnetworkPatchRequest(client, subnet_ref, subnetwork)])
  elif private_ipv6_google_access_type is not None:
    subnetwork = client.MakeRequests([
        (client.apitools_client.subnetworks, 'Get',
         client.messages.ComputeSubnetworksGetRequest(**subnet_ref.AsDict()))
    ])[0]

    subnetwork.privateIpv6GoogleAccess = (
        client.messages.Subnetwork.PrivateIpv6GoogleAccessValueValuesEnum(
            ConvertPrivateIpv6GoogleAccess(
                convert_to_enum(private_ipv6_google_access_type))))
    return client.MakeRequests(
        [CreateSubnetworkPatchRequest(client, subnet_ref, subnetwork)])
  elif enable_private_ipv6_access is not None:
    subnetwork = client.MakeRequests([
        (client.apitools_client.subnetworks, 'Get',
         client.messages.ComputeSubnetworksGetRequest(**subnet_ref.AsDict()))
    ])[0]

    subnetwork.enablePrivateV6Access = enable_private_ipv6_access
    subnetwork.privateIpv6GoogleAccess = None
    return client.MakeRequests(
        [CreateSubnetworkPatchRequest(client, subnet_ref, subnetwork)])
  elif set_role_active is not None:
    subnetwork = client.MakeRequests([
        (client.apitools_client.subnetworks, 'Get',
         client.messages.ComputeSubnetworksGetRequest(**subnet_ref.AsDict()))
    ])[0]

    subnetwork.role = client.messages.Subnetwork.RoleValueValuesEnum.ACTIVE
    patch_request = client.messages.ComputeSubnetworksPatchRequest(
        project=subnet_ref.project,
        subnetwork=subnet_ref.subnetwork,
        region=subnet_ref.region,
        subnetworkResource=subnetwork,
        drainTimeoutSeconds=drain_timeout_seconds)
    return client.MakeRequests([(client.apitools_client.subnetworks, 'Patch',
                                 patch_request)])

  return client.MakeRequests([])


def CreateSubnetworkPatchRequest(client, subnet_ref, subnetwork_resource):
  patch_request = client.messages.ComputeSubnetworksPatchRequest(
      project=subnet_ref.project,
      subnetwork=subnet_ref.subnetwork,
      region=subnet_ref.region,
      subnetworkResource=subnetwork_resource)
  return (client.apitools_client.subnetworks, 'Patch', patch_request)


def ConvertPrivateIpv6GoogleAccess(choice):
  choices_to_enum = {
      'DISABLE': 'DISABLE_GOOGLE_ACCESS',
      'ENABLE_BIDIRECTIONAL_ACCESS': 'ENABLE_BIDIRECTIONAL_ACCESS_TO_GOOGLE',
      'ENABLE_OUTBOUND_VM_ACCESS': 'ENABLE_OUTBOUND_VM_ACCESS_TO_GOOGLE'
  }
  return choices_to_enum.get(choice)
