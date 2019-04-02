# -*- coding: utf-8 -*- #
# Copyright 2016 Google Inc. All Rights Reserved.
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
"""Flags and helpers for the compute ssl-certificates commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.command_lib.compute import completers as compute_completers
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.util import completers

DEFAULT_LIST_FORMAT = """\
    table(
      name,
      creationTimestamp
    )"""

BETA_LIST_FORMAT = """\
    table(
      name,
      type,
      creationTimestamp,
      expireTime,
      managed.status:label=MANAGED_STATUS,
      managed.domainStatus:format="yaml"
    )"""

ALPHA_LIST_FORMAT = BETA_LIST_FORMAT


class SslCertificatesCompleter(compute_completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(SslCertificatesCompleter, self).__init__(
        collection='compute.sslCertificates',
        list_command='compute ssl-certificates list --uri',
        **kwargs)


class GlobalSslCertificatesCompleter(compute_completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(GlobalSslCertificatesCompleter, self).__init__(
        collection='compute.sslCertificates',
        api_version='alpha',
        list_command='alpha compute ssl-certificates list --global --uri',
        **kwargs)


class RegionSslCertificatesCompleter(compute_completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(RegionSslCertificatesCompleter, self).__init__(
        collection='compute.regionSslCertificates',
        api_version='alpha',
        list_command=
        'alpha compute ssl-certificates list --filter=region:* --uri',
        **kwargs)


class SslCertificatesCompleterAlpha(completers.MultiResourceCompleter):

  def __init__(self, **kwargs):
    super(SslCertificatesCompleterAlpha, self).__init__(
        completers=[
            GlobalSslCertificatesCompleter, RegionSslCertificatesCompleter
        ],
        **kwargs)


def SslCertificateArgument(required=True, plural=False, include_alpha=False):
  return compute_flags.ResourceArgument(
      resource_name='SSL certificate',
      completer=SslCertificatesCompleterAlpha
      if include_alpha else SslCertificatesCompleter,
      plural=plural,
      required=required,
      global_collection='compute.sslCertificates',
      regional_collection='compute.regionSslCertificates'
      if include_alpha else None,
      region_explanation=compute_flags.REGION_PROPERTY_EXPLANATION
      if include_alpha else None)


def SslCertificatesArgumentForOtherResource(resource,
                                            required=True,
                                            include_alpha=False):
  return compute_flags.ResourceArgument(
      name='--ssl-certificates',
      resource_name='ssl certificate',
      completer=SslCertificatesCompleterAlpha
      if include_alpha else SslCertificatesCompleter,
      plural=True,
      required=required,
      global_collection='compute.sslCertificates',
      regional_collection='compute.regionSslCertificates'
      if include_alpha else None,
      region_explanation=compute_flags.REGION_PROPERTY_EXPLANATION
      if include_alpha else None,
      short_help=('A reference to SSL certificate resources that are used for '
                  'server-side authentication.'),
      detailed_help="""\
        References to at most 10 SSL certificate resources that are used for
        server-side authentication. The first SSL certificate in this list is
        considered the primary SSL certificate associated with the load
        balancer. The SSL certificate must exist and cannot be deleted while
        referenced by a {0}.
        """.format(resource))
