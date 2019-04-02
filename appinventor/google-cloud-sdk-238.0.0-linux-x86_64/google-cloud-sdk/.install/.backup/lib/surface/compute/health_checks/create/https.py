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
"""Command for creating HTTPS health checks."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import health_checks_utils
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import completers
from googlecloudsdk.command_lib.compute.health_checks import flags


def _Run(args,
         holder,
         supports_port_specification=False,
         include_alpha=False):
  """Issues the request necessary for adding the health check."""
  client = holder.client
  messages = client.messages

  health_check_ref = flags.HealthCheckArgument(
      'HTTPS', include_alpha=include_alpha).ResolveAsResource(
          args, holder.resources)
  proxy_header = messages.HTTPSHealthCheck.ProxyHeaderValueValuesEnum(
      args.proxy_header)

  https_health_check = messages.HTTPSHealthCheck(
      host=args.host,
      port=args.port,
      portName=args.port_name,
      requestPath=args.request_path,
      proxyHeader=proxy_header,
      response=args.response)

  health_checks_utils.ValidateAndAddPortSpecificationToHealthCheck(
      args, https_health_check, supports_port_specification)

  if health_checks_utils.IsRegionalHealthCheckRef(health_check_ref):
    request = messages.ComputeRegionHealthChecksInsertRequest(
        healthCheck=messages.HealthCheck(
            name=health_check_ref.Name(),
            description=args.description,
            type=messages.HealthCheck.TypeValueValuesEnum.HTTPS,
            httpsHealthCheck=https_health_check,
            checkIntervalSec=args.check_interval,
            timeoutSec=args.timeout,
            healthyThreshold=args.healthy_threshold,
            unhealthyThreshold=args.unhealthy_threshold),
        project=health_check_ref.project,
        region=health_check_ref.region)
    collection = client.apitools_client.regionHealthChecks
  else:
    request = messages.ComputeHealthChecksInsertRequest(
        healthCheck=messages.HealthCheck(
            name=health_check_ref.Name(),
            description=args.description,
            type=messages.HealthCheck.TypeValueValuesEnum.HTTPS,
            httpsHealthCheck=https_health_check,
            checkIntervalSec=args.check_interval,
            timeoutSec=args.timeout,
            healthyThreshold=args.healthy_threshold,
            unhealthyThreshold=args.unhealthy_threshold),
        project=health_check_ref.project)
    collection = client.apitools_client.healthChecks

  return client.MakeRequests([(collection, 'Insert', request)])


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Create(base.CreateCommand):
  """Create HTTPS non-legacy health check to monitor load balanced instances."""

  @classmethod
  def Args(cls,
           parser,
           supports_use_serving_port=False,
           regionalized=False):
    parser.display_info.AddFormat(flags.DEFAULT_LIST_FORMAT)
    flags.HealthCheckArgument(
        'HTTPS', include_alpha=regionalized).AddArgument(
            parser, operation_type='create')
    health_checks_utils.AddHttpRelatedCreationArgs(
        parser,
        use_serving_port=supports_use_serving_port)
    health_checks_utils.AddProtocolAgnosticCreationArgs(parser, 'HTTPS')
    health_checks_utils.AddHttpRelatedResponseArg(parser)
    parser.display_info.AddCacheUpdater(completers.HealthChecksCompleterAlpha
                                        if regionalized else
                                        completers.HttpsHealthChecksCompleter)

  def Run(self, args):
    """Issues the request necessary for adding the health check."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    return _Run(args, holder)


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class CreateBeta(Create):
  """Create HTTPS non-legacy health check to monitor load balanced instances."""

  @staticmethod
  def Args(parser,
           supports_use_serving_port=True,
           regionalized=False):
    Create.Args(
        parser,
        supports_use_serving_port=supports_use_serving_port,
        regionalized=regionalized)

  def Run(self, args):
    """Issues the request necessary for adding the health check."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    return _Run(args, holder, supports_port_specification=True)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class CreateAlpha(CreateBeta):
  """Create HTTPS non-legacy health check to monitor load balanced instances."""

  @staticmethod
  def Args(parser):
    CreateBeta.Args(parser, regionalized=True)

  def Run(self, args):
    """Issues the request necessary for adding the health check."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    return _Run(
        args,
        holder,
        supports_port_specification=True,
        include_alpha=True)


Create.detailed_help = {
    'brief': ('Create a HTTPS health check to monitor load balanced instances'),
    'DESCRIPTION': """\
        *{command}* is used to create an HTTPS non-legacy health check. HTTPS
        health checks monitor instances in a load balancer controlled by a
        target pool. All arguments to the command are optional except for the
        name of the health check. Note, by default, this health check monitors
        TCP port 80.
        For more information on load balancing, see
        [](https://cloud.google.com/compute/docs/load-balancing-and-autoscaling/)
        """,
}
