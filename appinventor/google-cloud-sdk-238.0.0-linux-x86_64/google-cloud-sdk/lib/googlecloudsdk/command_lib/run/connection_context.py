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
"""Dynamic context for connection to Cloud Run."""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import abc
import contextlib
import re
import ssl
import sys
from googlecloudsdk.api_lib.run import gke
from googlecloudsdk.api_lib.run import global_methods
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.command_lib.run import exceptions as serverless_exceptions
from googlecloudsdk.command_lib.run import flags

from googlecloudsdk.core import properties

import six
from six.moves.urllib import parse as urlparse


@contextlib.contextmanager
def _OverrideEndpointOverrides(override):
  """Context manager to override the Cloud Run endpoint overrides for a while.

  Args:
    override: str, New value for Cloud Run endpoint.
  Yields:
    None.
  """
  old_endpoint = properties.VALUES.api_endpoint_overrides.run.Get()
  try:
    properties.VALUES.api_endpoint_overrides.run.Set(override)
    yield
  finally:
    properties.VALUES.api_endpoint_overrides.run.Set(old_endpoint)


class ConnectionInfo(six.with_metaclass(abc.ABCMeta)):
  """Information useful in constructing an API client."""

  def __init__(self):
    self.endpoint = None
    self.ca_certs = None
    self._cm = None

  @property
  def api_name(self):
    return global_methods.SERVERLESS_API_NAME

  @property
  def api_version(self):
    return global_methods.SERVERLESS_API_VERSION

  @abc.abstractmethod
  def Connect(self):
    pass

  @abc.abstractproperty
  def operator(self):
    pass

  @abc.abstractproperty
  def ns_label(self):
    pass

  @abc.abstractproperty
  def supports_one_platform(self):
    pass

  @abc.abstractproperty
  def location_label(self):
    pass

  def __enter__(self):
    self._cm = self.Connect()
    return self._cm.__enter__()

  def __exit__(self, typ, value, traceback):
    return self._cm.__exit__(typ, value, traceback)


def _CheckTLSSupport():
  """Provide a useful error message if the user's doesn't have TLS 1.2."""
  if re.match('OpenSSL 0\\.', ssl.OPENSSL_VERSION):
    # User's OpenSSL is too old.
    min_required_version = ('2.7.15' if sys.version_info.major == 2 else '3.4')
    raise serverless_exceptions.NoTLSError(
        'Your Python installation is using the SSL library {}, '
        'which does not support TLS 1.2. '
        'TLS 1.2 is required to connect to Cloud Run on Kubernetes Engine. '
        'Please upgrade to '
        'Python {} or greater, which comes bundled with OpenSSL >1.0.'.format(
            ssl.OPENSSL_VERSION,
            min_required_version))
  # PROTOCOL_TLSv1_2 applies to [2.7.9, 2.7.13) or [3.4, 3.6).
  # PROTOCOL_TLS applies to 2.7.13 and above, or 3.6 and above.
  if not (hasattr(ssl, 'PROTOCOL_TLS') or hasattr(ssl, 'PROTOCOL_TLSv1_2')):
    # User's Python is too old.
    min_required_version = ('2.7.9' if sys.version_info.major == 2 else '3.4')
    raise serverless_exceptions.NoTLSError(
        'Your Python {}.{}.{} installation does not support TLS 1.2, which is'
        ' required to connect to Cloud Run on Kubernetes Engine. '
        'Please upgrade to Python {} or greater.'.format(
            sys.version_info.major,
            sys.version_info.minor,
            sys.version_info.micro,
            min_required_version))


class _GKEConnectionContext(ConnectionInfo):
  """Context manager to connect to the GKE Cloud Run add-in."""

  def __init__(self, cluster_ref):
    super(_GKEConnectionContext, self).__init__()
    self.cluster_ref = cluster_ref
    self.region = None

  @property
  def ns_label(self):
    return 'namespace'

  @property
  def operator(self):
    return 'Cloud Run on GKE'

  @property
  def location_label(self):
    return ' of cluster [{{{{bold}}}}{}{{{{reset}}}}]'.format(
        self.cluster_ref.Name())

  @contextlib.contextmanager
  def Connect(self):
    _CheckTLSSupport()
    with gke.ClusterConnectionInfo(self.cluster_ref) as (ip, ca_certs):
      self.ca_certs = ca_certs
      with gke.MonkeypatchAddressChecking('kubernetes.default', ip) as endpoint:
        self.endpoint = 'https://{}/'.format(endpoint)
        with _OverrideEndpointOverrides(self.endpoint):
          yield self

  @property
  def supports_one_platform(self):
    return False


class _RegionalConnectionContext(ConnectionInfo):
  """Context manager to connect a particular Cloud Run region."""

  def __init__(self, region):
    super(_RegionalConnectionContext, self).__init__()
    self.region = region

  @property
  def ns_label(self):
    return 'project'

  @property
  def operator(self):
    return 'Cloud Run'

  @property
  def location_label(self):
    return ' region [{{{{bold}}}}{}{{{{reset}}}}]'.format(
        self.region)

  @contextlib.contextmanager
  def Connect(self):
    global_endpoint = apis.GetEffectiveApiEndpoint(
        global_methods.SERVERLESS_API_NAME,
        global_methods.SERVERLESS_API_VERSION)
    scheme, netloc, path, params, query, fragment = urlparse.urlparse(
        global_endpoint)
    netloc = '{}-{}'.format(self.region, netloc)
    self.endpoint = urlparse.urlunparse(
        (scheme, netloc, path, params, query, fragment))
    with _OverrideEndpointOverrides(self.endpoint):
      yield self

  @property
  def supports_one_platform(self):
    return True


def GetConnectionContext(args):
  """Gets the regional or GKE connection context.

  Args:
    args: Namespace, the args namespace.

  Raises:
    ConfigurationError if cluster is specified without a location.

  Returns:
    A GKE or regional ConnectionInfo object.
  """

  cluster_ref = args.CONCEPTS.cluster.Parse()
  if cluster_ref:
    return _GKEConnectionContext(cluster_ref)

  flags.ValidateClusterArgs(args)
  region = flags.GetRegion(args, prompt=True)
  if not region:
    raise flags.ArgumentError('You must specify either a cluster or a region.')
  return _RegionalConnectionContext(region)
