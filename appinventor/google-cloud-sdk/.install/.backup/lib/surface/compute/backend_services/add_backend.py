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

"""Command for adding a backend to a backend service."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import encoding

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute.backend_services import backend_flags
from googlecloudsdk.command_lib.compute.backend_services import backend_services_utils
from googlecloudsdk.command_lib.compute.backend_services import flags


@base.ReleaseTracks(base.ReleaseTrack.GA)
class AddBackend(base.UpdateCommand):
  """Add a backend to a backend service.

  *{command}* is used to add a backend to a backend service. A
  backend is a group of tasks that can handle requests sent to a
  backend service. Currently, the group of tasks can be one or
  more Google Compute Engine virtual machine instances grouped
  together using an instance group.

  Traffic is first spread evenly across all virtual machines in
  the group. When the group is full, traffic is sent to the next
  nearest group(s) that still have remaining capacity.

  To modify the parameters of a backend after it has been added
  to the backend service, use
  `gcloud compute backend-services update-backend` or
  `gcloud compute backend-services edit`.
  """

  @staticmethod
  def Args(parser):
    flags.GLOBAL_REGIONAL_BACKEND_SERVICE_ARG.AddArgument(parser)
    backend_flags.AddDescription(parser)
    flags.MULTISCOPE_INSTANCE_GROUP_ARG.AddArgument(
        parser, operation_type='add to the backend service')
    backend_flags.AddBalancingMode(parser)
    backend_flags.AddCapacityLimits(parser)
    backend_flags.AddCapacityScalar(parser)

  def _GetGetRequest(self, client, backend_service_ref):
    if backend_service_ref.Collection() == 'compute.regionBackendServices':
      return (client.apitools_client.regionBackendServices,
              'Get',
              client.messages.ComputeRegionBackendServicesGetRequest(
                  backendService=backend_service_ref.Name(),
                  region=backend_service_ref.region,
                  project=backend_service_ref.project))
    return (client.apitools_client.backendServices,
            'Get',
            client.messages.ComputeBackendServicesGetRequest(
                backendService=backend_service_ref.Name(),
                project=backend_service_ref.project))

  def _GetSetRequest(self, client, backend_service_ref, replacement):
    if backend_service_ref.Collection() == 'compute.regionBackendServices':
      return (client.apitools_client.regionBackendServices,
              'Update',
              client.messages.ComputeRegionBackendServicesUpdateRequest(
                  backendService=backend_service_ref.Name(),
                  backendServiceResource=replacement,
                  region=backend_service_ref.region,
                  project=backend_service_ref.project))
    return (client.apitools_client.backendServices,
            'Update',
            client.messages.ComputeBackendServicesUpdateRequest(
                backendService=backend_service_ref.Name(),
                backendServiceResource=replacement,
                project=backend_service_ref.project))

  def _GetGroupRef(self, args, resources, client):
    return flags.MULTISCOPE_INSTANCE_GROUP_ARG.ResolveAsResource(
        args,
        resources,
        scope_lister=compute_flags.GetDefaultScopeLister(client))

  def _CreateBackendMessage(self, messages, group_uri, balancing_mode, args):
    """Create a backend message.

    Args:
      messages: The avalible API proto messages.
      group_uri: String. The backend instance group uri.
      balancing_mode: Backend.BalancingModeValueValuesEnum. The backend load
        balancing mode.
      args: argparse Namespace. The arguments given to the add-backend command.

    Returns:
      A new Backend message with its fields set according to the given
      arguments.
    """

    backend_services_utils.ValidateBalancingModeArgs(messages, args)
    return messages.Backend(
        balancingMode=balancing_mode,
        capacityScaler=args.capacity_scaler,
        description=args.description,
        group=group_uri,
        maxRate=args.max_rate,
        maxRatePerInstance=args.max_rate_per_instance,
        maxUtilization=args.max_utilization,
        maxConnections=args.max_connections,
        maxConnectionsPerInstance=args.max_connections_per_instance)

  def _Modify(self, client, resources, backend_service_ref, args, existing):
    replacement = encoding.CopyProtoMessage(existing)

    group_ref = self._GetGroupRef(args, resources, client)
    group_uri = group_ref.SelfLink()

    for backend in existing.backends:
      if group_uri == backend.group:
        if (group_ref.Collection() == 'compute.instanceGroups'
            or group_ref.Collection() == 'compute.networkEndpointGroups'):
          scope = 'zone'
        elif group_ref.Collection() == 'compute.regionInstanceGroups':
          scope = 'region'
        raise exceptions.ToolException(
            'Backend [{}] in {} [{}] already exists in backend service '
            '[{}].'.format(group_ref.Name(),
                           scope,
                           getattr(group_ref, scope),
                           backend_service_ref.Name()))

    if args.balancing_mode:
      balancing_mode = client.messages.Backend.BalancingModeValueValuesEnum(
          args.balancing_mode)
    else:
      balancing_mode = None

    backend = self._CreateBackendMessage(client.messages, group_uri,
                                         balancing_mode, args)

    replacement.backends.append(backend)
    return replacement

  def Run(self, args):
    """Issues requests necessary to add backend to the Backend Service."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    backend_service_ref = (
        flags.GLOBAL_REGIONAL_BACKEND_SERVICE_ARG.ResolveAsResource(
            args,
            holder.resources,
            scope_lister=compute_flags.GetDefaultScopeLister(client)))
    get_request = self._GetGetRequest(client, backend_service_ref)

    objects = client.MakeRequests([get_request])

    new_object = self._Modify(client, holder.resources, backend_service_ref,
                              args, objects[0])

    return client.MakeRequests(
        [self._GetSetRequest(client, backend_service_ref, new_object)])


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class AddBackendBeta(AddBackend):
  """Add a backend to a backend service.

  *{command}* is used to add a backend to a backend service. A
  backend is a group of tasks that can handle requests sent to a
  backend service. Currently, the group of tasks can be one or
  more Google Compute Engine virtual machine instances grouped
  together using an instance group or network endpoint group.

  Traffic is first spread evenly across all virtual machines or
  network endpoints in the group. When the group is full, traffic
  is sent to the next nearest group(s) that still have remaining
  capacity.

  To modify the parameters of a backend after it has been added
  to the backend service, use
  `gcloud compute backend-services update-backend` or
  `gcloud compute backend-services edit`.
  """

  @staticmethod
  def Args(parser):
    flags.GLOBAL_REGIONAL_BACKEND_SERVICE_ARG.AddArgument(parser)
    flags.AddInstanceGroupAndNetworkEndpointGroupArgs(parser, 'add to')
    backend_flags.AddDescription(parser)
    backend_flags.AddBalancingMode(parser, supports_neg=True)
    backend_flags.AddCapacityLimits(parser, supports_neg=True)
    backend_flags.AddCapacityScalar(parser)

  def _GetGroupRef(self, args, resources, client):
    if args.instance_group:
      return flags.MULTISCOPE_INSTANCE_GROUP_ARG.ResolveAsResource(
          args,
          resources,
          scope_lister=compute_flags.GetDefaultScopeLister(client))
    if args.network_endpoint_group:
      return flags.NETWORK_ENDPOINT_GROUP_ARG.ResolveAsResource(
          args,
          resources,
          scope_lister=compute_flags.GetDefaultScopeLister(client))

  def _CreateBackendMessage(self, messages, group_uri, balancing_mode, args):
    """Overrides."""

    backend_services_utils.ValidateBalancingModeArgs(
        messages, args, supports_neg=True)
    return messages.Backend(
        balancingMode=balancing_mode,
        capacityScaler=args.capacity_scaler,
        description=args.description,
        group=group_uri,
        maxRate=args.max_rate,
        maxRatePerInstance=args.max_rate_per_instance,
        maxRatePerEndpoint=args.max_rate_per_endpoint,
        maxUtilization=args.max_utilization,
        maxConnections=args.max_connections,
        maxConnectionsPerInstance=args.max_connections_per_instance,
        maxConnectionsPerEndpoint=args.max_connections_per_endpoint)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class AddBackendAlpha(AddBackendBeta):
  """Add a backend to a backend service.

  *{command}* is used to add a backend to a backend service. A
  backend is a group of tasks that can handle requests sent to a
  backend service. Currently, the group of tasks can be one or
  more Google Compute Engine virtual machine instances grouped
  together using an instance group or network endpoint group.

  Traffic is first spread evenly across all virtual machines or
  network endpoints in the group. When the group is full, traffic
  is sent to the next nearest group(s) that still have remaining
  capacity.

  To modify the parameters of a backend after it has been added
  to the backend service, use
  `gcloud compute backend-services update-backend` or
  `gcloud compute backend-services edit`.
  """

  @staticmethod
  def Args(parser):
    flags.GLOBAL_REGIONAL_BACKEND_SERVICE_ARG.AddArgument(parser)
    flags.AddInstanceGroupAndNetworkEndpointGroupArgs(parser, 'add to')
    backend_flags.AddDescription(parser)
    backend_flags.AddBalancingMode(parser, supports_neg=True)
    backend_flags.AddCapacityLimits(parser, supports_neg=True)
    backend_flags.AddCapacityScalar(parser)
    backend_flags.AddFailover(parser, default=None)

  def _CreateBackendMessage(self, messages, group_uri, balancing_mode, args):
    """Overrides."""

    backend_services_utils.ValidateBalancingModeArgs(
        messages, args, supports_neg=True)
    return messages.Backend(
        balancingMode=balancing_mode,
        capacityScaler=args.capacity_scaler,
        description=args.description,
        group=group_uri,
        maxRate=args.max_rate,
        maxRatePerInstance=args.max_rate_per_instance,
        maxRatePerEndpoint=args.max_rate_per_endpoint,
        maxUtilization=args.max_utilization,
        maxConnections=args.max_connections,
        maxConnectionsPerInstance=args.max_connections_per_instance,
        maxConnectionsPerEndpoint=args.max_connections_per_endpoint,
        failover=args.failover)
