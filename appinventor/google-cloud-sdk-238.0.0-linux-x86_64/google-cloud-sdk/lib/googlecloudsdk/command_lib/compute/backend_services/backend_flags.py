# pylint: disable=E1305
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

"""Flags and helpers for the compute backend-services backend commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.command_lib.compute import flags
from googlecloudsdk.core import log


def AddDescription(parser):
  parser.add_argument(
      '--description',
      help='An optional, textual description for the backend.')


def AddInstanceGroup(parser, operation_type, with_deprecated_zone=False):
  """Add arguments to define instance group."""
  parser.add_argument(
      '--instance-group',
      required=True,
      help='The name or URI of a Google Cloud Instance Group.')

  scope_parser = parser.add_mutually_exclusive_group()
  flags.AddRegionFlag(
      scope_parser,
      resource_type='instance group',
      operation_type='{0} the backend service'.format(operation_type),
      flag_prefix='instance-group',
      explanation=flags.REGION_PROPERTY_EXPLANATION_NO_DEFAULT)
  if with_deprecated_zone:
    flags.AddZoneFlag(
        scope_parser,
        resource_type='instance group',
        operation_type='{0} the backend service'.format(operation_type),
        explanation='DEPRECATED, use --instance-group-zone flag instead.')
  flags.AddZoneFlag(
      scope_parser,
      resource_type='instance group',
      operation_type='{0} the backend service'.format(operation_type),
      flag_prefix='instance-group',
      explanation=flags.ZONE_PROPERTY_EXPLANATION_NO_DEFAULT)


def WarnOnDeprecatedFlags(args):
  if getattr(args, 'zone', None):  # TODO(b/28518663).
    log.warning(
        'The --zone flag is deprecated, please use --instance-group-zone'
        ' instead. It will be removed in a future release.')


def _GetBalancingModes(supports_neg):
  """Returns the --balancing-modes flag value choices name:description dict."""
  per_rate_flags = '*--max-rate-per-instance*'
  per_connection_flags = '*--max-connections-per-instance*'
  utilization_extra_help = ''
  if supports_neg:
    per_rate_flags += '/*--max-rate-per-endpoint*'
    per_connection_flags += '*--max-max-per-endpoint*'
    utilization_extra_help = (
        'This is incompatible with --network-endpoint-group.')
  balancing_modes = {
      'RATE': """\
          Spreads load based on how many HTTP requests per second (RPS) the
          backend can handle. This balancing mode is only available to backend
          services that use HTTP, HTTPS, or HTTP/2 protocols.
          You must specify exactly one of these additional parameters:
          `--max-rate`, `--max-rate-per-instance`, or`--max-rate-per-endpoint`.
          """.format(per_rate_flags),
      'UTILIZATION': """\
          Spreads load based on the CPU utilization of instances in a backend
          instance group. This balancing mode is only available to backend
          services with --load-balancing-scheme EXTERNAL and
          instance group backends. There is no restriction on the
          backend service protocol.
          The following additional parameters may be specified:
          `--max-utilization`, `--max-rate`, `--max-rate-per-instance`,
          `--max-connections`, `--max-connections-per-instance`.
          For valid combinations, see `--max-utilization` below.
          """.format(utilization_extra_help),
      'CONNECTION': """\
          Spreads load based on how many concurrent connections the backend
          can handle. This balancing mode is only available to backend
          services that use SSL, TCP, or UDP protocols.
          For backend services with --load-balancing-scheme EXTERNAL, you
          must specify exactly one of these additional parameters:
          `--max-connections`, `--max-connections-per-instance`, or
          `--max-connections-per-endpoint`.
          For backend services with --load-balancing-scheme INTERNAL, you
          must omit all of these parameters.
          """.format(per_connection_flags),
  }
  return balancing_modes


def AddBalancingMode(parser, supports_neg=False):
  """Add balancing mode arguments."""
  parser.add_argument(
      '--balancing-mode',
      choices=_GetBalancingModes(supports_neg),
      type=lambda x: x.upper(),
      help="""\
      Defines the strategy for balancing load.""")


def AddCapacityLimits(parser, supports_neg=False):
  """Add capacity thresholds arguments."""
  AddMaxUtilization(parser)
  capacity_group = parser.add_group(mutex=True)
  rate_group, connections_group = capacity_group, capacity_group
  if supports_neg:
    rate_group = capacity_group.add_group(mutex=True)
    connections_group = capacity_group.add_group(mutex=True)
    rate_group.add_argument(
        '--max-rate-per-endpoint',
        type=float,
        help="""\
        Only valid for network endpoint group backends. Defines a maximum
        number of HTTP requests per second (RPS) per endpoint if all endpoints
        are healthy. When one or more endpoints are unhealthy, an effective
        maximum rate per healthy endpoint is calculated by multiplying
        MAX_RATE_PER_ENDPOINT by the number of endpoints in the network
        endpoint group, then dividing by the number of healthy endpoints.
        """)
    connections_group.add_argument(
        '--max-connections-per-endpoint',
        type=int,
        help="""\
        Only valid for network endpoint group backends. Defines a maximum
        number of connections per endpoint if all endpoints are healthy. When
        one or more endpoints are unhealthy, an effective maximum number of
        connections per healthy endpoint is calculated by multiplying
        MAX_CONNECTIONS_PER_ENDPOINT by the number of endpoints in the network
        endpoint group, then dividing by the number of healthy endpoints.
        """)

  rate_group.add_argument(
      '--max-rate',
      type=int,
      help="""\
      Maximum number of HTTP requests per second (RPS) that the backend can
      handle. Valid for instance group and network endpoint group backends.
      Must not be defined if the backend is a managed instance group using
      autoscaling based on load balancing.
      """)
  rate_group.add_argument(
      '--max-rate-per-instance',
      type=float,
      help="""\
      Only valid for instance group backends. Defines a maximum number of
      HTTP requests per second (RPS) per instance if all instances in the
      instance group are healthy. When one or more instances are unhealthy,
      an effective maximum RPS per healthy instance is calculated by
      multiplying MAX_RATE_PER_INSTANCE by the number of instances in the
      instance group, then dividing by the number of healthy instances. This
      parameter is compatible with managed instance group backends that use
      autoscaling based on load balancing.
      """)
  connections_group.add_argument(
      '--max-connections',
      type=int,
      help="""\
      Maximum concurrent connections that the backend can handle.
      Valid for instance group and network endpoint group backends.
      """)
  connections_group.add_argument(
      '--max-connections-per-instance',
      type=int,
      help="""\
      Only valid for instance group backends. Defines a maximum number
      of concurrent connections per instance if all instances in the
      instance group are healthy. When one or more instances are
      unhealthy, an effective maximum number of connections per healthy
      instance is calculated by multiplying MAX_CONNECTIONS_PER_INSTANCE
      by the number of instances in the instance group, then dividing by
      the number of healthy instances.
      """)


def AddMaxUtilization(parser):
  parser.add_argument(
      '--max-utilization',
      type=arg_parsers.BoundedFloat(lower_bound=0.0, upper_bound=1.0),
      help="""\
      Defines the maximum target for average CPU utilization of the backend
      instance in the backend instance group. Acceptable values are 0.0 (0%)
      through 1.0 (100%). Available for all backend service protocols,
      with --balancing-mode=UTILIZATION.

      For backend services that use SSL, TCP, or UDP protocols, you may specify
      either `--max-connections` or `--max-connections-per-instance`, either by
      themselves or one in conjunction with `--max-utilization`. In other words,
      the following configuration options are supported:
      * no additional parameter
      * just `--max-utilization`
      * just `--max-connections`
      * just `--max-connections-per-instance`
      * both `--max-utilization` and `--max-connections`
      * both `--max-utilization` and `--max-connections-per-instance`

      The meanings for `--max-connections` and `--max-connections-per-instance`
      are the same as for --balancing-mode=CONNECTION. If one is used in
      conjunction with `--max-utilization`, instances are considered at capacity
      when either maximum utilization or maximum connections is reached.

      For backend services that use HTTP, HTTPS, or HTTP/2 protocols, you may
      specify either `--max-rate` or `--max-rate-per-instance`, either by
      themselves or one in conjunction with `--max-utilization`. In other words,
      the following configuration options are supported:
      * no additional parameter
      * just `--max-utilization`
      * just `--max-rate`
      * just `--max-rate-per-instance`
      * both `--max-utilization` and `--max-rate`
      * both `--max-utilization` and `--max-rate-per-instance`

      The meanings for `--max-rate` and `--max-rate-per-instance` are the same
      as for --balancing-mode=RATE. If one is used in conjunction with
      `--max-utilization`, instances are considered at capacity when *either*
      maximum utilization or the maximum rate is reached.""")


def AddCapacityScalar(parser):
  parser.add_argument(
      '--capacity-scaler',
      type=arg_parsers.BoundedFloat(lower_bound=0.0, upper_bound=1.0),
      help="""\
      A setting that applies to all balancing modes. This value is multiplied
      by the balancing mode value to set the current max usage of the instance
      group. Acceptable values are `0.0` (0%) through `1.0` (100%). Setting this
      value to `0.0` (0%) drains the backend service. Note that draining a
      backend service only prevents new connections to instances in the group.
      All existing connections are allowed to continue until they close by
      normal means. This cannot be used for internal load balancing.""")


def AddFailover(parser, default):
  """Adds the failover argument to the argparse."""
  parser.add_argument(
      '--failover',
      action='store_true',
      default=default,
      help="""\
      Designates whether this is a failover backend. More than one
      failover backend can be configured for a given BackendService.
      Not compatible with the --global flag""")
