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
"""Command for deleting SSL certificates."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute.ssl_certificates import flags
from googlecloudsdk.command_lib.compute.ssl_certificates import ssl_certificates_utils


@base.ReleaseTracks(base.ReleaseTrack.GA, base.ReleaseTrack.BETA)
class Delete(base.DeleteCommand):
  """Delete Google Compute Engine SSL certificates.

  *{command}* deletes one or more Google Compute Engine SSL certificates.
  SSL certificates can only be deleted when no other resources (e.g.,
  target HTTPS proxies) refer to them.
  """

  SSL_CERTIFICATE_ARG = None

  @staticmethod
  def Args(parser):
    Delete.SSL_CERTIFICATE_ARG = flags.SslCertificateArgument(plural=True)
    Delete.SSL_CERTIFICATE_ARG.AddArgument(parser, operation_type='delete')
    parser.display_info.AddCacheUpdater(flags.SslCertificatesCompleter)

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    ssl_certificate_refs = Delete.SSL_CERTIFICATE_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(client))

    utils.PromptForDeletion(ssl_certificate_refs)

    requests = []
    for ssl_certificate_ref in ssl_certificate_refs:
      requests.append((client.apitools_client.sslCertificates, 'Delete',
                       client.messages.ComputeSslCertificatesDeleteRequest(
                           **ssl_certificate_ref.AsDict())))

    return client.MakeRequests(requests)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class DeleteAlpha(Delete):
  """Delete Google Compute Engine SSL certificates.

  *{command}* deletes one or more Google Compute Engine SSL certificates.
  SSL certificates can only be deleted when no other resources (e.g.,
  target HTTPS proxies) refer to them.
  """
  SSL_CERTIFICATE_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.SSL_CERTIFICATE_ARG = flags.SslCertificateArgument(
        plural=True, include_alpha=True)
    cls.SSL_CERTIFICATE_ARG.AddArgument(parser, operation_type='delete')
    parser.display_info.AddCacheUpdater(flags.SslCertificatesCompleterAlpha)

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    ssl_certificate_refs = self.SSL_CERTIFICATE_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(client))

    utils.PromptForDeletion(ssl_certificate_refs)

    requests = []
    for ssl_certificate_ref in ssl_certificate_refs:
      if ssl_certificates_utils.IsRegionalSslCertificatesRef(
          ssl_certificate_ref):
        requests.append(
            (client.apitools_client.regionSslCertificates, 'Delete',
             client.messages.ComputeRegionSslCertificatesDeleteRequest(
                 **ssl_certificate_ref.AsDict())))
      else:
        requests.append((client.apitools_client.sslCertificates, 'Delete',
                         client.messages.ComputeSslCertificatesDeleteRequest(
                             **ssl_certificate_ref.AsDict())))

    return client.MakeRequests(requests)
