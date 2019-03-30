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
"""Command for updating health checks."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import health_checks_utils
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute.health_checks import flags
from googlecloudsdk.core import exceptions as core_exceptions
from googlecloudsdk.core import log


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class Update(base.UpdateCommand):
  """Update a HTTP2 health check.

  *{command}* is used to update an existing HTTP2 health check. Only
  arguments passed in will be updated on the health check. Other
  attributes will remain unaffected.
  """

  HEALTH_CHECK_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.HEALTH_CHECK_ARG = flags.HealthCheckArgument('HTTP2')
    cls.HEALTH_CHECK_ARG.AddArgument(parser, operation_type='update')
    health_checks_utils.AddHttpRelatedUpdateArgs(parser, use_serving_port=True)
    health_checks_utils.AddHttpRelatedResponseArg(parser)
    health_checks_utils.AddProtocolAgnosticUpdateArgs(parser, 'HTTP2')

  def _GetGetRequest(self, client, health_check_ref):
    """Returns a request for fetching the existing health check."""
    return (client.apitools_client.healthChecks,
            'Get',
            client.messages.ComputeHealthChecksGetRequest(
                healthCheck=health_check_ref.Name(),
                project=health_check_ref.project))

  def _GetSetRequest(self, client, health_check_ref, replacement):
    """Returns a request for updating the health check."""
    return (client.apitools_client.healthChecks,
            'Update',
            client.messages.ComputeHealthChecksUpdateRequest(
                healthCheck=health_check_ref.Name(),
                healthCheckResource=replacement,
                project=health_check_ref.project))

  def _GetRegionalGetRequest(self, client, health_check_ref):
    """Returns a request for fetching the existing health check."""
    return (client.apitools_client.regionHealthChecks, 'Get',
            client.messages.ComputeRegionHealthChecksGetRequest(
                healthCheck=health_check_ref.Name(),
                project=health_check_ref.project,
                region=health_check_ref.region))

  def _GetRegionalSetRequest(self, client, health_check_ref, replacement):
    """Returns a request for updating the health check."""
    return (client.apitools_client.regionHealthChecks, 'Update',
            client.messages.ComputeRegionHealthChecksUpdateRequest(
                healthCheck=health_check_ref.Name(),
                healthCheckResource=replacement,
                project=health_check_ref.project,
                region=health_check_ref.region))

  def Modify(self, client, args, existing_check):
    """Returns a modified HealthCheck message."""
    # We do not support using 'update http2' with a health check of a
    # different protocol.
    if (existing_check.type !=
        client.messages.HealthCheck.TypeValueValuesEnum.HTTP2):
      raise core_exceptions.Error(
          'update http2 subcommand applied to health check with protocol ' +
          existing_check.type.name)

    # Description, PortName, Response and Host are the only attributes that can
    # be cleared by passing in an empty string (but we don't want to set it to
    # an empty string).
    if args.description:
      description = args.description
    elif args.description is None:
      description = existing_check.description
    else:
      description = None

    if args.host:
      host = args.host
    elif args.host is None:
      host = existing_check.http2HealthCheck.host
    else:
      host = None

    if args.response:
      response = args.response
    elif args.response is None:
      response = existing_check.http2HealthCheck.response
    else:
      response = None

    port, port_name, port_specification = health_checks_utils.\
        HandlePortRelatedFlagsForUpdate(
            args, existing_check.http2HealthCheck,
            supports_port_specification=True)

    proxy_header = existing_check.http2HealthCheck.proxyHeader
    if args.proxy_header is not None:
      proxy_header = (
          client.messages.HTTP2HealthCheck.ProxyHeaderValueValuesEnum(
              args.proxy_header))
    new_health_check = client.messages.HealthCheck(
        name=existing_check.name,
        description=description,
        type=client.messages.HealthCheck.TypeValueValuesEnum.HTTP2,
        http2HealthCheck=client.messages.HTTP2HealthCheck(
            host=host,
            port=port,
            portName=port_name,
            portSpecification=port_specification,
            requestPath=(args.request_path or
                         existing_check.http2HealthCheck.requestPath),
            proxyHeader=proxy_header,
            response=response),
        checkIntervalSec=(args.check_interval or
                          existing_check.checkIntervalSec),
        timeoutSec=args.timeout or existing_check.timeoutSec,
        healthyThreshold=(args.healthy_threshold or
                          existing_check.healthyThreshold),
        unhealthyThreshold=(args.unhealthy_threshold or
                            existing_check.unhealthyThreshold),
    )
    return new_health_check

  def Run(self, args):
    """Issues requests necessary to update the HTTP2 Health Checks."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    health_checks_utils.CheckProtocolAgnosticArgs(args)

    args_unset = not (args.port or args.request_path or args.check_interval or
                      args.timeout or args.healthy_threshold or
                      args.unhealthy_threshold or args.proxy_header or
                      args.use_serving_port)
    if (args.description is None and args.host is None and
        args.response is None and args.port_name is None and args_unset):
      raise exceptions.ToolException('At least one property must be modified.')

    health_check_ref = self.HEALTH_CHECK_ARG.ResolveAsResource(
        args, holder.resources)
    if health_checks_utils.IsRegionalHealthCheckRef(health_check_ref):
      get_request = self._GetRegionalGetRequest(client, health_check_ref)
    else:
      get_request = self._GetGetRequest(client, health_check_ref)

    objects = client.MakeRequests([get_request])

    new_object = self.Modify(client, args, objects[0])

    # If existing object is equal to the proposed object or if
    # Modify() returns None, then there is no work to be done, so we
    # print the resource and return.
    if objects[0] == new_object:
      log.status.Print(
          'No change requested; skipping update for [{0}].'.format(
              objects[0].name))
      return objects

    if health_checks_utils.IsRegionalHealthCheckRef(health_check_ref):
      set_request = self._GetRegionalSetRequest(client, health_check_ref,
                                                new_object)
    else:
      set_request = self._GetSetRequest(client, health_check_ref, new_object)

    return client.MakeRequests([set_request])


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class UpdateAlpha(Update):
  """Update a HTTP2 health check.

  *{command}* is used to update an existing HTTP2 health check. Only
  arguments passed in will be updated on the health check. Other
  attributes will remain unaffected.
  """

  HEALTH_CHECK_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.HEALTH_CHECK_ARG = flags.HealthCheckArgument(
        'HTTP2', include_alpha=True)
    cls.HEALTH_CHECK_ARG.AddArgument(parser, operation_type='update')
    health_checks_utils.AddHttpRelatedUpdateArgs(parser, use_serving_port=True)
    health_checks_utils.AddHttpRelatedResponseArg(parser)
    health_checks_utils.AddProtocolAgnosticUpdateArgs(parser, 'HTTP2')
