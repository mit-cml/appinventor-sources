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
"""Common classes and functions for forwarding rules."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import lister
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.calliope import exceptions as calliope_exceptions
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute import scope as compute_scope
from googlecloudsdk.command_lib.compute.forwarding_rules import flags
from googlecloudsdk.core import properties


def _ValidateGlobalArgs(args, include_alpha=False):
  """Validate the global forwarding rules args."""
  if args.target_instance:
    raise calliope_exceptions.ToolException(
        'You cannot specify [--target-instance] for a global '
        'forwarding rule.')
  if args.target_pool:
    raise calliope_exceptions.ToolException(
        'You cannot specify [--target-pool] for a global '
        'forwarding rule.')

  if getattr(args, 'backend_service', None):
    raise calliope_exceptions.ToolException(
        'You cannot specify [--backend-service] for a global '
        'forwarding rule.')

  if getattr(args, 'load_balancing_scheme', None) == 'INTERNAL':
    raise calliope_exceptions.ToolException(
        'You cannot specify internal [--load-balancing-scheme] for a global '
        'forwarding rule.')

  if getattr(args, 'target_vpn_gateway', None):
    raise calliope_exceptions.ToolException(
        'You cannot specify [--target-vpn-gateway] for a global '
        'forwarding rule.')

  if (include_alpha and
      getattr(args, 'load_balancing_scheme', None) == 'INTERNAL_SELF_MANAGED'):
    if not (getattr(args, 'target_http_proxy', None) or
            getattr(args, 'target_https_proxy', None)):
      raise calliope_exceptions.ToolException(
          'You must specify either [--target-http-proxy] or '
          '[--target-https-proxy] for an INTERNAL_SELF_MANAGED '
          '[--load-balancing-scheme].')

    if getattr(args, 'subnet', None):
      raise calliope_exceptions.ToolException(
          'You cannot specify [--subnet] for an INTERNAL_SELF_MANAGED '
          '[--load-balancing-scheme].')

    if not getattr(args, 'address', None):
      raise calliope_exceptions.ToolException(
          'You must specify [--address] for an INTERNAL_SELF_MANAGED '
          '[--load-balancing-scheme]')


def GetGlobalTarget(resources, args, include_alpha=False):
  """Return the forwarding target for a globally scoped request."""
  _ValidateGlobalArgs(args, include_alpha)
  if args.target_http_proxy:
    return flags.TargetHttpProxyArg().ResolveAsResource(args, resources)

  if args.target_https_proxy:
    return flags.TargetHttpsProxyArg().ResolveAsResource(args, resources)
  if args.target_ssl_proxy:
    return flags.TARGET_SSL_PROXY_ARG.ResolveAsResource(args, resources)
  if getattr(args, 'target_tcp_proxy', None):
    return flags.TARGET_TCP_PROXY_ARG.ResolveAsResource(args, resources)


def _ValidateRegionalArgs(args, include_alpha=False):
  """Validate the regional forwarding rules args.

  Args:
      args: The arguments given to the create/set-target command.
      include_alpha: Should alpha functionality be included?
  """

  if getattr(args, 'global', None):
    raise calliope_exceptions.ToolException(
        'You cannot specify [--global] for a regional '
        'forwarding rule.')

  # For flexible networking, with STANDARD network tier the regional forwarding
  # rule can have global target. The request may not specify network tier
  # because it can be set as default project setting, so here let backend do
  # validation.
  if args.target_instance_zone and not args.target_instance:
    raise calliope_exceptions.ToolException(
        'You cannot specify [--target-instance-zone] unless you are '
        'specifying [--target-instance].')

  if getattr(args, 'load_balancing_scheme', None) == 'INTERNAL':
    if getattr(args, 'port_range', None):
      raise calliope_exceptions.ToolException(
          'You cannot specify [--port-range] for a forwarding rule '
          'whose [--load-balancing-scheme] is internal, '
          'please use [--ports] flag instead.')

  schemes_allowing_network_fields = ['INTERNAL', 'INTERNAL_MANAGED']

  if (getattr(args, 'subnet', None) or
      getattr(args, 'network', None)) and getattr(
          args, 'load_balancing_scheme',
          None) not in schemes_allowing_network_fields:
    raise calliope_exceptions.ToolException(
        'You cannot specify [--subnet] or [--network] for non-internal '
        '[--load-balancing-scheme] forwarding rule.')

  if (include_alpha and
      getattr(args, 'load_balancing_scheme', None) == 'INTERNAL_SELF_MANAGED'):
    raise calliope_exceptions.ToolException(
        'You cannot specify an INTERNAL_SELF_MANAGED [--load-balancing-scheme] '
        'for a regional forwarding rule.')


def GetRegionalTarget(client,
                      resources,
                      args,
                      forwarding_rule_ref=None,
                      include_alpha=False):
  """Return the forwarding target for a regionally scoped request."""
  _ValidateRegionalArgs(args, include_alpha)
  if forwarding_rule_ref:
    region_arg = forwarding_rule_ref.region
    project_arg = forwarding_rule_ref.project
  else:
    region_arg = args.region
    project_arg = None

  if args.target_pool:
    if not args.target_pool_region and region_arg:
      args.target_pool_region = region_arg
    target_ref = flags.TARGET_POOL_ARG.ResolveAsResource(
        args,
        resources,
        scope_lister=compute_flags.GetDefaultScopeLister(client))
    target_region = target_ref.region
  elif args.target_instance:
    target_ref = flags.TARGET_INSTANCE_ARG.ResolveAsResource(
        args,
        resources,
        scope_lister=_GetZonesInRegionLister(
            ['--target-instance-zone'], region_arg, client, project_arg or
            properties.VALUES.core.project.GetOrFail()))
    target_region = utils.ZoneNameToRegionName(target_ref.zone)
  elif getattr(args, 'target_vpn_gateway', None):
    if not args.target_vpn_gateway_region and region_arg:
      args.target_vpn_gateway_region = region_arg
    target_ref = flags.TARGET_VPN_GATEWAY_ARG.ResolveAsResource(
        args, resources)
    target_region = target_ref.region
  elif getattr(args, 'backend_service', None):
    if not args.backend_service_region and region_arg:
      args.backend_service_region = region_arg
    target_ref = flags.BACKEND_SERVICE_ARG.ResolveAsResource(args, resources)
    target_region = target_ref.region
  elif args.target_http_proxy:
    target_ref = flags.TargetHttpProxyArg(
        include_alpha=include_alpha).ResolveAsResource(args, resources)
    target_region = region_arg
  elif args.target_https_proxy:
    target_ref = flags.TargetHttpsProxyArg(
        include_alpha=include_alpha).ResolveAsResource(args, resources)
    target_region = region_arg
  elif args.target_ssl_proxy:
    target_ref = flags.TARGET_SSL_PROXY_ARG.ResolveAsResource(args, resources)
    target_region = region_arg
  elif args.target_tcp_proxy:
    target_ref = flags.TARGET_TCP_PROXY_ARG.ResolveAsResource(args, resources)
    target_region = region_arg

  return target_ref, target_region


def _GetZonesInRegionLister(flag_names, region, compute_client, project):
  """Lists all the zones in a given region."""
  def Lister(*unused_args):
    """Returns a list of the zones for a given region."""
    if region:
      filter_expr = 'name eq {0}.*'.format(region)
    else:
      filter_expr = None

    errors = []
    global_resources = lister.GetGlobalResources(
        service=compute_client.apitools_client.zones,
        project=project,
        filter_expr=filter_expr,
        http=compute_client.apitools_client.http,
        batch_url=compute_client.batch_url,
        errors=errors)

    choices = [resource for resource in global_resources]
    if errors or not choices:
      punctuation = ':' if errors else '.'
      utils.RaiseToolException(
          errors,
          'Unable to fetch a list of zones. Specifying [{0}] may fix this '
          'issue{1}'.format(', or '.join(flag_names), punctuation))

    return {compute_scope.ScopeEnum.ZONE: choices}

  return Lister
