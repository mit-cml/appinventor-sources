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
"""Code that's shared between multiple target-https-proxies subcommands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals


def ResolveTargetHttpsProxyUrlMap(args, url_map_arg, target_https_proxy_ref,
                                  resources):
  """Parses the URL map that is pointed to by a Target HTTPS Proxy from args.

  This function handles parsing a regional/global URL map that is
  pointed to by a regional/global Target HTTPS Proxy.

  Args:
    args: The arguments provided to the target_https_proxies command.
    url_map_arg: The ResourceArgument specification for the url map argument.
    target_https_proxy_ref: The resource reference to the Target HTTPS Proxy.
                           This is obtained by parsing the Target HTTPS Proxy
                           arguments provided.
    resources: ComputeApiHolder resources.

  Returns:
    Returns the URL map resource
  """

  if IsRegionalTargetHttpsProxiesRef(target_https_proxy_ref):
    if not getattr(args, 'url_map_region', None):
      args.url_map_region = target_https_proxy_ref.region
  else:
    if not getattr(args, 'global_url_map', None):
      args.global_url_map = bool(args.url_map)

  return url_map_arg.ResolveAsResource(args, resources)


def ResolveSslCertificates(args, ssl_certificate_arg, target_https_proxy_ref,
                           resources):
  """Parses the ssl certs that are pointed to by a Target HTTPS Proxy from args.

  This function handles parsing regional/global ssl certificates that are
  pointed to by a regional/global Target HTTPS Proxy.

  Args:
    args: The arguments provided to the target_https_proxies command.
    ssl_certificate_arg: The ResourceArgument specification for the
                         ssl_certificates argument.
    target_https_proxy_ref: The resource reference to the Target HTTPS Proxy.
                            This is obtained by parsing the Target HTTPS Proxy
                            arguments provided.
    resources: ComputeApiHolder resources.

  Returns:
    Returns the SSL Certificates resource
  """

  if IsRegionalTargetHttpsProxiesRef(target_https_proxy_ref):
    if not getattr(args, 'ssl_certificates_region', None):
      args.ssl_certificates_region = target_https_proxy_ref.region
  else:
    if not getattr(args, 'global_ssl_certificates', None):
      args.global_ssl_certificates = bool(args.ssl_certificates)

  return ssl_certificate_arg.ResolveAsResource(args, resources)


def IsRegionalTargetHttpsProxiesRef(target_https_proxy_ref):
  """Returns True if the Target HTTPS Proxy reference is regional."""

  return target_https_proxy_ref.Collection() == \
         'compute.regionTargetHttpsProxies'


def IsGlobalTargetHttpsProxiesRef(target_https_proxy_ref):
  """Returns True if the Target HTTPS Proxy reference is global."""

  return target_https_proxy_ref.Collection() == 'compute.targetHttpsProxies'
