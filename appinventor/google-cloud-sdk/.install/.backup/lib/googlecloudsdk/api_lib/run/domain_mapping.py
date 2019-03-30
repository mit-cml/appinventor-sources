# -*- coding: utf-8 -*- #
# Copyright 2019 Google Inc. All Rights Reserved.
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
"""Wraps a Cloud Run DomainMapping message for field access convenience."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.run import k8s_object


class DomainMapping(k8s_object.KubernetesObject):
  """Wraps a Cloud Run DomainMapping message.

  Setting properties on a DomainMapping (where possible) writes through to the
  nested Kubernetes-style fields.
  """

  API_CATEGORY = 'domains.cloudrun.com'
  KIND = 'DomainMapping'

  @property
  def route_name(self):
    return self.spec.routeName

  @route_name.setter
  def route_name(self, value):
    self._m.spec.routeName = value
