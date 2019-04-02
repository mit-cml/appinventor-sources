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
"""Wraps a Serverless Service message, making fields more convenient."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.run import configuration
from googlecloudsdk.api_lib.run import k8s_object


ENDPOINT_VISIBILITY = 'serving.knative.dev/visibility'
CLUSTER_LOCAL = 'cluster-local'


class Service(k8s_object.KubernetesObject):
  """Wraps a Serverless Service message, making fields more convenient.

  Setting properties on a Service (where possible) writes through to the
  nested Kubernetes-style fields.
  """
  API_CATEGORY = 'serving.knative.dev'
  KIND = 'Service'

  @classmethod
  def New(cls, client, namespace, private_endpoint=None):
    """Produces a new Service object.

    Args:
      client: The Cloud Run API client.
      namespace: str, The serving namespace.
      private_endpoint: bool, True if the new Service should only be accessible
          from within the cluster.

    Returns:
      A new Service object to be deployed.
    """
    ret = super(Service, cls).New(client, namespace)
    # We're in oneOf territory, set the other to None for now.
    ret.spec.pinned = None
    # The build is also a oneOf
    # TODO(b/112662240): Remove conditional once this field is public
    if hasattr(ret.configuration.spec, 'build'):
      ret.configuration.spec.build = None

    if private_endpoint:
      ret.labels[ENDPOINT_VISIBILITY] = CLUSTER_LOCAL

    # Unset a pile of unused things on the container.
    ret.configuration.container.lifecycle = None
    ret.configuration.container.livenessProbe = None
    ret.configuration.container.readinessProbe = None
    ret.configuration.container.resources = None
    ret.configuration.container.securityContext = None
    return ret

  @property
  def configuration(self):
    """Configuration (configuration.Configuration) of the service, if any."""
    options = (self._m.spec.pinned, self._m.spec.runLatest)
    ret = next((o.configuration for o in options if o is not None), None)
    return configuration.Configuration.SpecOnly(ret, self._messages)

  @property
  def latest_created_revision(self):
    return self.status.latestCreatedRevisionName

  @property
  def latest_ready_revision(self):
    return self.status.latestReadyRevisionName

  @property
  def serving_revisions(self):
    return [t.revisionName for t in self.status.traffic if t.percent]

  @property
  def domain(self):
    return self._m.status.domain

  @property
  def ready_symbol(self):
    if (self.ready is False and
        self.latest_ready_revision and
        self.latest_created_revision != self.latest_ready_revision):
      return '!'
    return super(Service, self).ready_symbol


