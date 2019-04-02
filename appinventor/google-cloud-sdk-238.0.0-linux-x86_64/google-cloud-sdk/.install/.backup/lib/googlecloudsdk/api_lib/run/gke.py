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
"""Library for integrating Cloud Run with GKE."""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import base64
import contextlib
import os
import socket
import ssl
import tempfile
import threading
from googlecloudsdk.api_lib.container import api_adapter
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import properties
from googlecloudsdk.core.util import files

import six


class NoCaCertError(exceptions.Error):
  pass


class _AddressPatches(object):
  """Singleton class to hold patches on getaddrinfo."""

  _instance = None

  @classmethod
  def Initialize(cls):
    assert not cls._instance
    cls._instance = cls()

  @classmethod
  def Get(cls):
    assert cls._instance
    return cls._instance

  def __init__(self):
    self._host_to_ip = None
    self._ip_to_host = None
    self._old_getaddrinfo = None
    self._old_match_hostname = None
    self._lock = threading.Lock()

  @contextlib.contextmanager
  def MonkeypatchAddressChecking(self, hostname, ip):
    """Change ssl address checking so the given ip answers to the hostname."""
    with self._lock:
      if self._host_to_ip is None:
        self._host_to_ip = {}
        self._ip_to_host = {}
        self._old_match_hostname = ssl.match_hostname
        self._old_getaddrinfo = socket.getaddrinfo
        if six.PY3:
          ssl.match_hostname = self._MatchHostname
        else:
          socket.getaddrinfo = self._GetAddrInfo
      if hostname in self._host_to_ip:
        raise ValueError(
            'Cannot re-patch the same address: {}'.format(hostname))
      if ip in self._ip_to_host:
        raise ValueError(
            'Cannot re-patch the same address: {}'.format(ip))
      self._host_to_ip[hostname] = ip
      self._ip_to_host[ip] = hostname
    try:
      if six.PY3:
        yield ip
      else:
        yield hostname
    finally:
      with self._lock:
        del self._host_to_ip[hostname]
        del self._ip_to_host[ip]
        if not self._host_to_ip:
          self._host_to_ip = None
          self._ip_to_host = None
          if six.PY3:
            ssl.match_hostname = self._old_match_hostname
          else:
            socket.getaddrinfo = self._old_getaddrinfo

  def _GetAddrInfo(self, host, *args, **kwargs):
    """Like socket.getaddrinfo, only with translation."""
    with self._lock:
      assert self._host_to_ip is not None
      if host in self._host_to_ip:
        host = self._host_to_ip[host]
    return self._old_getaddrinfo(host, *args, **kwargs)

  def _MatchHostname(self, cert, hostname):
    # A replacement for ssl.match_hostname(cert, hostname)
    # Since we'll be connecting with hostname as bare IP address, the goal is
    # to treat that as if it were the hostname `kubernetes.default`, which
    # is what the GKE master asserts it is.
    with self._lock:
      assert self._ip_to_host is not None
      if hostname in self._ip_to_host:
        hostname = self._ip_to_host[hostname]
    return self._old_match_hostname(cert, hostname)

_AddressPatches.Initialize()


def MonkeypatchAddressChecking(hostname, ip):
  """Manipulate SSL address checking so we can talk to GKE.

  GKE provides an IP address for talking to the k8s master, and a
  ca_certs that signs the tls certificate the master provides. Unfortunately,
  that tls certificate is for `kubernetes`, `kubernetes.default`,
  `kubernetes.default.svc`, or `kubernetes.default.svc.cluster.local`.

  In Python 3, we do this by patching ssl.match_hostname to allow the
  `kubernetes.default` when we connect to the given IP address.

  In Python 2, httplib2 does its own hosname checking so this isn't available.
  Instead, we change getaddrinfo to allow a "fake /etc/hosts" effect.
  This allows us to use `kubernetes.default` as the hostname while still
  connecting to the ip address we know is the kubernetes server.

  This is all ok, because we got the ca_cert that it'll use directly from the
  gke api.  Calls to `getaddrinfo` that specifically ask for a given hostname
  can be redirected to the ip address we provide for the hostname, as if we had
  edited /etc/hosts, without editing /etc/hosts.

  Arguments:
    hostname: hostname to replace
    ip: ip address to replace the hostname with
  Returns:
    A context manager that patches an internal function for its duration, and
    yields the endpoint to actually connect to.
  """
  return _AddressPatches.Get().MonkeypatchAddressChecking(hostname, ip)


@contextlib.contextmanager
def _DisableUserProjectQuota():
  """Use legacy quota; required for the container surface.

  Causes the http libraries (action at a distance, but there's no better way)
  not to set the X-Goog-User-Project header.

  Yields:
    For a block to be executed while the user project quota is disabled.
  """
  reset_billing_project = False
  try:
    if not properties.VALUES.billing.quota_project.IsExplicitlySet():
      reset_billing_project = True
      properties.VALUES.billing.quota_project.Set(
          properties.VALUES.billing.LEGACY)
    yield
  finally:
    if reset_billing_project:
      properties.VALUES.billing.quota_project.Set(None)


@contextlib.contextmanager
def ClusterConnectionInfo(cluster_ref):
  """Get the info we need to use to connect to a GKE cluster.

  Arguments:
    cluster_ref: reference to the cluster to connect to.
  Yields:
    A tuple of (endpoint, ca_certs), where endpoint is the ip address
    of the GKE master, and ca_certs is the absolute path of a temporary file
    (lasting the life of the python process) holding the ca_certs to connect to
    the GKE cluster.
  Raises:
    NoCaCertError: if the cluster is missing certificate authority data.
  """
  with _DisableUserProjectQuota():
    adapter = api_adapter.NewAPIAdapter('v1')
    cluster = adapter.GetCluster(cluster_ref)
  auth = cluster.masterAuth
  if auth and auth.clusterCaCertificate:
    ca_data = auth.clusterCaCertificate
  else:
    # This should not happen unless the cluster is in an unusual error
    # state.
    raise NoCaCertError('Cluster is missing certificate authority data.')
  fd, filename = tempfile.mkstemp()
  os.close(fd)
  files.WriteBinaryFileContents(filename, base64.b64decode(ca_data),
                                private=True)
  try:
    yield cluster.endpoint, filename
  finally:
    os.remove(filename)


