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
"""Command for creating SSL certificates."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.ssl_certificates import flags
from googlecloudsdk.command_lib.compute.ssl_certificates import ssl_certificates_utils
from googlecloudsdk.core.util import files


def _Args(parser, include_alpha=False, support_managed_certs=False):
  """Add the SSL certificates command line flags to the parser."""
  parser.add_argument(
      '--description',
      help='An optional, textual description for the SSL certificate.')

  parser.display_info.AddCacheUpdater(flags.SslCertificatesCompleterAlpha
                                      if include_alpha else
                                      flags.SslCertificatesCompleter)

  if support_managed_certs:
    managed_or_not = parser.add_group(
        mutex=True,
        required=True,
        help='Flags for managed or self-managed certificate. ')

    managed_or_not.add_argument(
        '--domains',
        metavar='DOMAIN',
        type=arg_parsers.ArgList(min_length=1),
        default=[],
        help="""\
        List of domains to create a managed certificate for.
        """)

    not_managed = managed_or_not.add_group('Flags for self-managed certificate')
    not_managed.add_argument(
        '--certificate',
        metavar='LOCAL_FILE_PATH',
        required=True,
        help="""\
        Path to a local certificate file to create a self-managed
        certificate. The certificate must be in PEM format. The certificate
        chain must be no greater than 5 certs long. The chain must include at
        least one intermediate cert.
        """)
    not_managed.add_argument(
        '--private-key',
        metavar='LOCAL_FILE_PATH',
        required=True,
        help="""\
        Path to a local private key file. The private key must be in PEM
        format and must use RSA or ECDSA encryption.
        """)
  else:
    parser.add_argument(
        '--certificate',
        required=True,
        metavar='LOCAL_FILE_PATH',
        help="""\
        Path to a local certificate file. The certificate must be in PEM
        format. The certificate chain must be no greater than 5 certs long. The
        chain must include at least one intermediate cert.
        """)

    parser.add_argument(
        '--private-key',
        required=True,
        metavar='LOCAL_FILE_PATH',
        help="""\
        Path to a local private key file. The private key must be in PEM
        format and must use RSA or ECDSA encryption.
        """)


def _ParseCertificateArguments(client, args):
  """Parse commands arguments connected with certificate type.

  Args:
    client: Client object.
    args: Command line arguments.

  Returns:
    Tuple of certificate type, SslCertificateManagedSslCertificate and
    SslCertificateSelfManagedSslCertificate. Only one of SslCertificate
    messages can be not None.
  """
  self_managed = None
  managed = None
  certificate_type = None
  if args.certificate:
    certificate_type = \
        client.messages.SslCertificate.TypeValueValuesEnum.SELF_MANAGED
    certificate = files.ReadFileContents(args.certificate)
    private_key = files.ReadFileContents(args.private_key)
    self_managed = client.messages.SslCertificateSelfManagedSslCertificate(
        certificate=certificate, privateKey=private_key)
  if args.domains:
    certificate_type = \
        client.messages.SslCertificate.TypeValueValuesEnum.MANAGED
    managed = client.messages.SslCertificateManagedSslCertificate(
        domains=args.domains)
  return certificate_type, self_managed, managed


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Create(base.CreateCommand):
  """Create a Google Compute Engine SSL certificate.

  *{command}* is used to create SSL certificates which can be used to
  configure a target HTTPS proxy. An SSL certificate consists of a
  certificate and private key. The private key is encrypted before it is
  stored. For more information, see:
  [](https://cloud.google.com/load-balancing/docs/ssl-certificates)
  """

  SSL_CERTIFICATE_ARG = None

  @classmethod
  def Args(cls, parser):
    parser.display_info.AddFormat(flags.DEFAULT_LIST_FORMAT)
    cls.SSL_CERTIFICATE_ARG = flags.SslCertificateArgument()
    cls.SSL_CERTIFICATE_ARG.AddArgument(parser, operation_type='create')
    _Args(parser, include_alpha=False, support_managed_certs=False)

  def Run(self, args):
    """Issues the request necessary for adding the SSL certificate."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    ssl_certificate_ref = self.SSL_CERTIFICATE_ARG.ResolveAsResource(
        args, holder.resources)
    certificate = files.ReadFileContents(args.certificate)
    private_key = files.ReadFileContents(args.private_key)

    request = client.messages.ComputeSslCertificatesInsertRequest(
        sslCertificate=client.messages.SslCertificate(
            name=ssl_certificate_ref.Name(),
            certificate=certificate,
            privateKey=private_key,
            description=args.description),
        project=ssl_certificate_ref.project)

    return client.MakeRequests([(client.apitools_client.sslCertificates,
                                 'Insert', request)])


@base.UnicodeIsSupported
@base.ReleaseTracks(base.ReleaseTrack.BETA)
class CreateBeta(base.CreateCommand):
  """Create a Google Compute Engine SSL certificate.

  *{command}* is used to create SSL certificates which can be used to configure
  a target HTTPS proxy. An SSL certificate consists of a certificate and
  private key. The private key is encrypted before it is stored.

  You can create either a managed or a self-managed SslCertificate. A managed
  SslCertificate will be provisioned and renewed for you, when you specify
  the `--domains` flag. A self-managed certificate is created by passing the
  certificate obtained from Certificate Authority through `--certificate` and
  `--private-key` flags.
  """

  SSL_CERTIFICATE_ARG = None

  @classmethod
  def Args(cls, parser):
    parser.display_info.AddFormat(flags.BETA_LIST_FORMAT)
    cls.SSL_CERTIFICATE_ARG = flags.SslCertificateArgument()
    cls.SSL_CERTIFICATE_ARG.AddArgument(parser, operation_type='create')
    _Args(parser, include_alpha=False, support_managed_certs=True)

  def Run(self, args):
    """Issues the request necessary for adding the SSL certificate."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    ssl_certificate_ref = self.SSL_CERTIFICATE_ARG.ResolveAsResource(
        args, holder.resources)

    certificate_type, self_managed, managed = _ParseCertificateArguments(
        client, args)
    request = client.messages.ComputeSslCertificatesInsertRequest(
        sslCertificate=client.messages.SslCertificate(
            type=certificate_type,
            name=ssl_certificate_ref.Name(),
            selfManaged=self_managed,
            managed=managed,
            description=args.description),
        project=ssl_certificate_ref.project)

    return client.MakeRequests([(client.apitools_client.sslCertificates,
                                 'Insert', request)])


@base.UnicodeIsSupported
@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class CreateAlpha(base.CreateCommand):
  """Create a Google Compute Engine SSL certificate.

  *{command}* is used to create SSL certificates which can be used to configure
  a target HTTPS proxy. An SSL certificate consists of a certificate and
  private key. The private key is encrypted before it is stored.

  You can create either a managed or a self-managed SslCertificate. A managed
  SslCertificate will be provisioned and renewed for you, when you specify
  the `--domains` flag. A self-managed certificate is created by passing the
  certificate obtained from Certificate Authority through `--certificate` and
  `--private-key` flags.
  """

  SSL_CERTIFICATE_ARG = None

  @classmethod
  def Args(cls, parser):
    parser.display_info.AddFormat(flags.ALPHA_LIST_FORMAT)
    cls.SSL_CERTIFICATE_ARG = flags.SslCertificateArgument(include_alpha=True)
    cls.SSL_CERTIFICATE_ARG.AddArgument(parser, operation_type='create')
    _Args(parser, include_alpha=True, support_managed_certs=True)

  def Run(self, args):
    """Issues the request necessary for adding the SSL certificate."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    ssl_certificate_ref = self.SSL_CERTIFICATE_ARG.ResolveAsResource(
        args, holder.resources)

    certificate_type, self_managed, managed = _ParseCertificateArguments(
        client, args)

    if ssl_certificates_utils.IsRegionalSslCertificatesRef(ssl_certificate_ref):
      request = client.messages.ComputeRegionSslCertificatesInsertRequest(
          sslCertificate=client.messages.SslCertificate(
              type=certificate_type,
              name=ssl_certificate_ref.Name(),
              selfManaged=self_managed,
              managed=managed,
              description=args.description),
          region=ssl_certificate_ref.region,
          project=ssl_certificate_ref.project)
    else:
      request = client.messages.ComputeSslCertificatesInsertRequest(
          sslCertificate=client.messages.SslCertificate(
              type=certificate_type,
              name=ssl_certificate_ref.Name(),
              selfManaged=self_managed,
              managed=managed,
              description=args.description),
          project=ssl_certificate_ref.project)

    if ssl_certificates_utils.IsRegionalSslCertificatesRef(ssl_certificate_ref):
      collection = client.apitools_client.regionSslCertificates
    else:
      collection = client.apitools_client.sslCertificates

    return client.MakeRequests([(collection, 'Insert', request)])
