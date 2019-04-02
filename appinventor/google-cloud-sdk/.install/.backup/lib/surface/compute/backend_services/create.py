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
"""Command for creating backend services.

   There are separate alpha, beta, and GA command classes in this file.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute import signed_url_flags
from googlecloudsdk.command_lib.compute.backend_services import backend_services_utils
from googlecloudsdk.command_lib.compute.backend_services import flags
from googlecloudsdk.core import log


# TODO(b/73642225): Determine whether 'https' should be default
def _ResolvePortName(args):
  """Determine port name if one was not specified."""
  if args.port_name:
    return args.port_name

  if args.protocol == 'HTTPS':
    return 'https'
  if args.protocol == 'HTTP2':
    return 'http2'
  if args.protocol == 'SSL':
    return 'ssl'
  if args.protocol == 'TCP':
    return 'tcp'

  return 'http'


# TODO(b/73642225): Determine whether 'HTTPS' should be default
def _ResolveProtocol(messages, args, default='HTTP'):
  return messages.BackendService.ProtocolValueValuesEnum(
      args.protocol or default)


def AddIapFlag(parser):
  # TODO(b/34479878): It would be nice if the auto-generated help text were
  # a bit better so we didn't need to be quite so verbose here.
  flags.AddIap(
      parser,
      help="""\
      Configure Identity Aware Proxy (IAP) service. You can configure IAP to be
      'enabled' or 'disabled' (default). If it is enabled you can provide values
      for 'oauth2-client-id' and 'oauth2-client-secret'. For example,
      '--iap=enabled,oauth2-client-id=foo,oauth2-client-secret=bar' will
      turn IAP on, and '--iap=disabled' will turn it off. See
      https://cloud.google.com/iap/ for more information about this feature.
      """)


@base.ReleaseTracks(base.ReleaseTrack.GA)
class CreateGA(base.CreateCommand):
  """Create a backend service.

  *{command}* is used to create backend services. Backend
  services define groups of backends that can receive
  traffic. Each backend group has parameters that define the
  group's capacity (e.g. max CPU utilization, max queries per
  second, ...). URL maps define which requests are sent to which
  backend services.

  Backend services created through this command will start out
  without any backend groups. To add backend groups, use 'gcloud
  compute backend-services add-backend' or 'gcloud compute
  backend-services edit'.
  """

  HEALTH_CHECK_ARG = None
  HTTP_HEALTH_CHECK_ARG = None
  HTTPS_HEALTH_CHECK_ARG = None

  @classmethod
  def Args(cls, parser):
    parser.display_info.AddFormat(flags.DEFAULT_LIST_FORMAT)
    flags.GLOBAL_REGIONAL_BACKEND_SERVICE_ARG.AddArgument(
        parser, operation_type='create')
    flags.AddDescription(parser)
    cls.HEALTH_CHECK_ARG = flags.HealthCheckArgument()
    cls.HEALTH_CHECK_ARG.AddArgument(parser, cust_metavar='HEALTH_CHECK')
    cls.HTTP_HEALTH_CHECK_ARG = flags.HttpHealthCheckArgument()
    cls.HTTP_HEALTH_CHECK_ARG.AddArgument(
        parser, cust_metavar='HTTP_HEALTH_CHECK')
    cls.HTTPS_HEALTH_CHECK_ARG = flags.HttpsHealthCheckArgument()
    cls.HTTPS_HEALTH_CHECK_ARG.AddArgument(
        parser, cust_metavar='HTTPS_HEALTH_CHECK')
    flags.AddTimeout(parser)
    flags.AddPortName(parser)
    flags.AddProtocol(parser, default=None)
    flags.AddEnableCdn(parser, default=False)
    flags.AddSessionAffinity(parser)
    flags.AddAffinityCookieTtl(parser)
    flags.AddConnectionDrainingTimeout(parser)
    flags.AddLoadBalancingScheme(parser)
    flags.AddCacheKeyIncludeProtocol(parser, default=True)
    flags.AddCacheKeyIncludeHost(parser, default=True)
    flags.AddCacheKeyIncludeQueryString(parser, default=True)
    flags.AddCacheKeyQueryStringList(parser)
    AddIapFlag(parser)
    parser.display_info.AddCacheUpdater(flags.BackendServicesCompleter)
    signed_url_flags.AddSignedUrlCacheMaxAge(parser, required=False)

  def _CreateBackendService(self, holder, args, backend_services_ref):
    health_checks = flags.GetHealthCheckUris(args, self, holder.resources)
    if not health_checks:
      raise exceptions.ToolException('At least one health check required.')

    enable_cdn = True if args.enable_cdn else None

    return holder.client.messages.BackendService(
        description=args.description,
        name=backend_services_ref.Name(),
        healthChecks=health_checks,
        portName=_ResolvePortName(args),
        protocol=_ResolveProtocol(holder.client.messages, args),
        timeoutSec=args.timeout,
        enableCDN=enable_cdn)

  def CreateGlobalRequests(self, holder, args, backend_services_ref):
    if args.load_balancing_scheme == 'INTERNAL':
      raise exceptions.ToolException(
          'Must specify --region for internal load balancer.')
    backend_service = self._CreateBackendService(holder, args,
                                                 backend_services_ref)

    client = holder.client

    if args.connection_draining_timeout is not None:
      backend_service.connectionDraining = client.messages.ConnectionDraining(
          drainingTimeoutSec=args.connection_draining_timeout)

    if args.session_affinity is not None:
      backend_service.sessionAffinity = (
          client.messages.BackendService.SessionAffinityValueValuesEnum(
              args.session_affinity))
    if args.session_affinity is not None:
      backend_service.affinityCookieTtlSec = args.affinity_cookie_ttl

    backend_services_utils.ApplyCdnPolicyArgs(
        client,
        args,
        backend_service,
        is_update=False,
        apply_signed_url_cache_max_age=True)

    self._ApplyIapArgs(client.messages, args.iap, backend_service)

    request = client.messages.ComputeBackendServicesInsertRequest(
        backendService=backend_service,
        project=backend_services_ref.project)

    return [(client.apitools_client.backendServices, 'Insert', request)]

  def CreateRegionalRequests(self, holder, args, backend_services_ref):
    backend_service = self._CreateRegionBackendService(holder, args,
                                                       backend_services_ref)
    client = holder.client
    if args.connection_draining_timeout is not None:
      backend_service.connectionDraining = client.messages.ConnectionDraining(
          drainingTimeoutSec=args.connection_draining_timeout)

    if args.session_affinity is not None:
      backend_service.sessionAffinity = (
          client.messages.BackendService.SessionAffinityValueValuesEnum(
              args.session_affinity))

    request = client.messages.ComputeRegionBackendServicesInsertRequest(
        backendService=backend_service,
        region=backend_services_ref.region,
        project=backend_services_ref.project)

    return [(client.apitools_client.regionBackendServices, 'Insert', request)]

  def _CreateRegionBackendService(self, holder, args, backend_services_ref):
    health_checks = flags.GetHealthCheckUris(args, self, holder.resources)
    if not health_checks:
      raise exceptions.ToolException('At least one health check required.')

    messages = holder.client.messages

    return messages.BackendService(
        description=args.description,
        name=backend_services_ref.Name(),
        healthChecks=health_checks,
        loadBalancingScheme=(
            messages.BackendService.LoadBalancingSchemeValueValuesEnum(
                args.load_balancing_scheme)),
        protocol=_ResolveProtocol(messages, args, default='TCP'),
        timeoutSec=args.timeout)

  def _ApplyIapArgs(self, messages, iap_arg, backend_service):
    if iap_arg is not None:
      backend_service.iap = backend_services_utils.GetIAP(iap_arg,
                                                          messages)
      if backend_service.iap.enabled:
        log.warning(backend_services_utils.IapBestPracticesNotice())
      if (backend_service.iap.enabled and backend_service.protocol is not
          messages.BackendService.ProtocolValueValuesEnum.HTTPS):
        log.warning(backend_services_utils.IapHttpWarning())

  def Run(self, args):
    """Issues request necessary to create Backend Service."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client
    ref = flags.GLOBAL_REGIONAL_BACKEND_SERVICE_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(client))
    if ref.Collection() == 'compute.backendServices':
      requests = self.CreateGlobalRequests(holder, args, ref)
    elif ref.Collection() == 'compute.regionBackendServices':
      requests = self.CreateRegionalRequests(holder, args, ref)

    return client.MakeRequests(requests)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class CreateAlpha(CreateGA):
  """Create a backend service.

  *{command}* is used to create backend services. Backend
  services define groups of backends that can receive
  traffic. Each backend group has parameters that define the
  group's capacity (e.g. max CPU utilization, max queries per
  second, ...). URL maps define which requests are sent to which
  backend services.

  Backend services created through this command will start out
  without any backend groups. To add backend groups, use 'gcloud
  compute backend-services add-backend' or 'gcloud compute
  backend-services edit'.
  """

  HEALTH_CHECK_ARG = None
  HTTP_HEALTH_CHECK_ARG = None
  HTTPS_HEALTH_CHECK_ARG = None

  @classmethod
  def Args(cls, parser):
    parser.display_info.AddFormat(flags.DEFAULT_LIST_FORMAT)
    flags.GLOBAL_REGIONAL_BACKEND_SERVICE_ARG.AddArgument(
        parser, operation_type='create')
    flags.AddDescription(parser)
    cls.HEALTH_CHECK_ARG = flags.HealthCheckArgument(include_alpha=True)
    cls.HEALTH_CHECK_ARG.AddArgument(parser, cust_metavar='HEALTH_CHECK')
    cls.HTTP_HEALTH_CHECK_ARG = flags.HttpHealthCheckArgument()
    cls.HTTP_HEALTH_CHECK_ARG.AddArgument(
        parser, cust_metavar='HTTP_HEALTH_CHECK')
    cls.HTTPS_HEALTH_CHECK_ARG = flags.HttpsHealthCheckArgument()
    cls.HTTPS_HEALTH_CHECK_ARG.AddArgument(
        parser, cust_metavar='HTTPS_HEALTH_CHECK')
    flags.AddTimeout(parser)
    flags.AddPortName(parser)
    flags.AddProtocol(
        parser,
        default=None)
    flags.AddEnableCdn(parser, default=False)
    flags.AddCacheKeyIncludeProtocol(parser, default=True)
    flags.AddCacheKeyIncludeHost(parser, default=True)
    flags.AddCacheKeyIncludeQueryString(parser, default=True)
    flags.AddCacheKeyQueryStringList(parser)
    flags.AddSessionAffinity(parser)
    flags.AddAffinityCookieTtl(parser)
    flags.AddConnectionDrainingTimeout(parser)
    flags.AddLoadBalancingScheme(parser, include_alpha=True)
    flags.AddCustomRequestHeaders(parser, remove_all_flag=False, default=False)
    signed_url_flags.AddSignedUrlCacheMaxAge(parser, required=False)
    flags.AddConnectionDrainOnFailover(parser, default=None)
    flags.AddDropTrafficIfUnhealthy(parser, default=None)
    flags.AddFailoverRatio(parser)
    flags.AddEnableLogging(parser, default=None)
    flags.AddLoggingSampleRate(parser)
    AddIapFlag(parser)
    parser.display_info.AddCacheUpdater(flags.BackendServicesCompleter)

  def CreateGlobalRequests(self, holder, args, backend_services_ref):
    if args.load_balancing_scheme == 'INTERNAL':
      raise exceptions.ToolException(
          'Must specify --region for internal load balancer.')
    if (args.connection_drain_on_failover is not None or
        args.drop_traffic_if_unhealthy is not None or
        args.failover_ratio is not None):
      raise exceptions.InvalidArgumentException(
          '--global',
          'cannot specify failover policies for global backend services.')
    backend_service = self._CreateBackendService(holder, args,
                                                 backend_services_ref)

    client = holder.client
    if args.connection_draining_timeout is not None:
      backend_service.connectionDraining = (client.messages.ConnectionDraining(
          drainingTimeoutSec=args.connection_draining_timeout))

    if args.enable_cdn:
      backend_service.enableCDN = args.enable_cdn

    backend_services_utils.ApplyCdnPolicyArgs(
        client,
        args,
        backend_service,
        is_update=False,
        apply_signed_url_cache_max_age=True)

    if args.session_affinity is not None:
      backend_service.sessionAffinity = (
          client.messages.BackendService.SessionAffinityValueValuesEnum(
              args.session_affinity))
    if args.affinity_cookie_ttl is not None:
      backend_service.affinityCookieTtlSec = args.affinity_cookie_ttl
    if args.custom_request_header is not None:
      backend_service.customRequestHeaders = args.custom_request_header

    self._ApplyIapArgs(client.messages, args.iap, backend_service)

    if args.load_balancing_scheme != 'EXTERNAL':
      backend_service.loadBalancingScheme = (
          client.messages.BackendService.LoadBalancingSchemeValueValuesEnum(
              args.load_balancing_scheme))

    backend_services_utils.ApplyLogConfigArgs(client.messages, args,
                                              backend_service)

    request = client.messages.ComputeBackendServicesInsertRequest(
        backendService=backend_service,
        project=backend_services_ref.project)

    return [(client.apitools_client.backendServices, 'Insert', request)]

  def CreateRegionalRequests(self, holder, args, backend_services_ref):
    if (not args.cache_key_include_host or
        not args.cache_key_include_protocol or
        not args.cache_key_include_query_string or
        args.cache_key_query_string_blacklist is not None or
        args.cache_key_query_string_whitelist is not None):
      raise exceptions.ToolException(
          'Custom cache key flags cannot be used for regional requests.')
    if (args.enable_logging is not None or
        args.logging_sample_rate is not None):
      raise exceptions.InvalidArgumentException(
          '--region',
          'cannot specify logging options for regional backend services.')

    backend_service = self._CreateRegionBackendService(holder, args,
                                                       backend_services_ref)
    client = holder.client

    if args.connection_draining_timeout is not None:
      backend_service.connectionDraining = client.messages.ConnectionDraining(
          drainingTimeoutSec=args.connection_draining_timeout)
    if args.custom_request_header is not None:
      backend_service.customRequestHeaders = args.custom_request_header
    backend_services_utils.ApplyFailoverPolicyArgs(client.messages, args,
                                                   backend_service)

    if args.session_affinity is not None:
      backend_service.sessionAffinity = (
          client.messages.BackendService.SessionAffinityValueValuesEnum(
              args.session_affinity))

    request = client.messages.ComputeRegionBackendServicesInsertRequest(
        backendService=backend_service,
        region=backend_services_ref.region,
        project=backend_services_ref.project)

    return [(client.apitools_client.regionBackendServices, 'Insert', request)]

  def _CreateRegionBackendService(self, holder, args, backend_services_ref):
    health_checks = flags.GetHealthCheckUris(args, self, holder.resources)
    if not health_checks:
      raise exceptions.ToolException('At least one health check required.')

    messages = holder.client.messages

    return messages.BackendService(
        description=args.description,
        name=backend_services_ref.Name(),
        healthChecks=health_checks,
        loadBalancingScheme=(
            messages.BackendService.LoadBalancingSchemeValueValuesEnum(
                args.load_balancing_scheme)),
        protocol=_ResolveProtocol(messages, args, default='TCP'),
        timeoutSec=args.timeout)


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class CreateBeta(CreateGA):
  """Create a backend service.

  *{command}* is used to create backend services. Backend
  services define groups of backends that can receive
  traffic. Each backend group has parameters that define the
  group's capacity (e.g. max CPU utilization, max queries per
  second, ...). URL maps define which requests are sent to which
  backend services.

  Backend services created through this command will start out
  without any backend groups. To add backend groups, use 'gcloud
  compute backend-services add-backend' or 'gcloud compute
  backend-services edit'.
  """

  HEALTH_CHECK_ARG = None
  HTTP_HEALTH_CHECK_ARG = None
  HTTPS_HEALTH_CHECK_ARG = None

  @classmethod
  def Args(cls, parser):
    parser.display_info.AddFormat(flags.DEFAULT_LIST_FORMAT)
    flags.GLOBAL_REGIONAL_BACKEND_SERVICE_ARG.AddArgument(
        parser, operation_type='create')
    flags.AddDescription(parser)
    cls.HEALTH_CHECK_ARG = flags.HealthCheckArgument()
    cls.HEALTH_CHECK_ARG.AddArgument(parser, cust_metavar='HEALTH_CHECK')
    cls.HTTP_HEALTH_CHECK_ARG = flags.HttpHealthCheckArgument()
    cls.HTTP_HEALTH_CHECK_ARG.AddArgument(
        parser, cust_metavar='HTTP_HEALTH_CHECK')
    cls.HTTPS_HEALTH_CHECK_ARG = flags.HttpsHealthCheckArgument()
    cls.HTTPS_HEALTH_CHECK_ARG.AddArgument(
        parser, cust_metavar='HTTPS_HEALTH_CHECK')
    flags.AddTimeout(parser)
    flags.AddPortName(parser)
    flags.AddProtocol(
        parser,
        default=None)
    flags.AddEnableCdn(parser, default=False)
    flags.AddSessionAffinity(parser)
    flags.AddAffinityCookieTtl(parser)
    flags.AddConnectionDrainingTimeout(parser)
    flags.AddLoadBalancingScheme(parser)
    flags.AddCustomRequestHeaders(parser, remove_all_flag=False)
    flags.AddCacheKeyIncludeProtocol(parser, default=True)
    flags.AddCacheKeyIncludeHost(parser, default=True)
    flags.AddCacheKeyIncludeQueryString(parser, default=True)
    flags.AddCacheKeyQueryStringList(parser)
    flags.AddEnableLogging(parser, default=None)
    flags.AddLoggingSampleRate(parser)
    signed_url_flags.AddSignedUrlCacheMaxAge(parser, required=False)
    AddIapFlag(parser)

  def CreateGlobalRequests(self, holder, args, backend_services_ref):
    if args.load_balancing_scheme == 'INTERNAL':
      raise exceptions.ToolException(
          'Must specify --region for internal load balancer.')
    backend_service = self._CreateBackendService(holder, args,
                                                 backend_services_ref)

    client = holder.client

    if args.connection_draining_timeout is not None:
      backend_service.connectionDraining = client.messages.ConnectionDraining(
          drainingTimeoutSec=args.connection_draining_timeout)
    if args.session_affinity is not None:
      backend_service.sessionAffinity = (
          client.messages.BackendService.SessionAffinityValueValuesEnum(
              args.session_affinity))
    if args.session_affinity is not None:
      backend_service.affinityCookieTtlSec = args.affinity_cookie_ttl
    if args.IsSpecified('custom_request_header'):
      backend_service.customRequestHeaders = args.custom_request_header

    backend_services_utils.ApplyCdnPolicyArgs(
        client,
        args,
        backend_service,
        is_update=False,
        apply_signed_url_cache_max_age=True)

    self._ApplyIapArgs(client.messages, args.iap, backend_service)

    backend_services_utils.ApplyLogConfigArgs(client.messages, args,
                                              backend_service)

    request = client.messages.ComputeBackendServicesInsertRequest(
        backendService=backend_service,
        project=backend_services_ref.project)

    return [(client.apitools_client.backendServices, 'Insert', request)]

  def CreateRegionalRequests(self, holder, args, backend_services_ref):
    if (args.enable_logging is not None or
        args.logging_sample_rate is not None):
      raise exceptions.InvalidArgumentException(
          '--region',
          'cannot specify logging options for regional backend services.')

    backend_service = self._CreateRegionBackendService(holder, args,
                                                       backend_services_ref)
    client = holder.client

    if args.connection_draining_timeout is not None:
      backend_service.connectionDraining = client.messages.ConnectionDraining(
          drainingTimeoutSec=args.connection_draining_timeout)
    if args.IsSpecified('custom_request_header'):
      backend_service.customRequestHeaders = args.custom_request_header

    if args.session_affinity is not None:
      backend_service.sessionAffinity = (
          client.messages.BackendService.SessionAffinityValueValuesEnum(
              args.session_affinity))

    request = client.messages.ComputeRegionBackendServicesInsertRequest(
        backendService=backend_service,
        region=backend_services_ref.region,
        project=backend_services_ref.project)

    return [(client.apitools_client.regionBackendServices, 'Insert', request)]

  def _CreateRegionBackendService(self, holder, args, backend_services_ref):
    health_checks = flags.GetHealthCheckUris(args, self, holder.resources)
    if not health_checks:
      raise exceptions.ToolException('At least one health check required.')

    messages = holder.client.messages

    return messages.BackendService(
        description=args.description,
        name=backend_services_ref.Name(),
        healthChecks=health_checks,
        loadBalancingScheme=(
            messages.BackendService.LoadBalancingSchemeValueValuesEnum(
                args.load_balancing_scheme)),
        protocol=_ResolveProtocol(messages, args, default='TCP'),
        timeoutSec=args.timeout)
