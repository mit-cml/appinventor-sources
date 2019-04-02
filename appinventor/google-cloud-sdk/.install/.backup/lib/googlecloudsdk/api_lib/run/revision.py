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
"""Wraps a Cloud Run revision message with convenience methods."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.run import build_template
from googlecloudsdk.api_lib.run import k8s_object


# Label names as to be stored in k8s object metadata
AUTHOR_ANNOTATION = 'run.googleapis.com/lastModifierEmail'
SERVICE_LABEL = 'serving.knative.dev/service'


class Revision(k8s_object.KubernetesObject):
  """Wraps a Cloud Run Revision message, making fields more convenient."""

  API_CATEGORY = 'serving.knative.dev'
  KIND = 'Revision'
  READY_CONDITION = 'Ready'
  TERMINAL_CONDITIONS = {
      READY_CONDITION,
  }

  @property
  def env_vars(self):
    """Returns a mutable, dict-like object to manage env vars.

    The returned object can be used like a dictionary, and any modifications to
    the returned object (i.e. setting and deleting keys) modify the underlying
    nested env vars fields.
    """
    if self._m.spec and self._m.spec.container:
      return k8s_object.ListAsDictionaryWrapper(
          self._m.spec.container.env, self._messages.EnvVar)

  @property
  def author(self):
    return self.annotations.get(AUTHOR_ANNOTATION)

  @property
  def creation_timestamp(self):
    return self._m.metadata.creationTimestamp

  @property
  def gcs_location(self):
    return self._m.status.gcs.location

  @property
  def language(self):
    return self.labels.get(build_template.LANGUAGE_LABEL)

  @property
  def version(self):
    return self.labels[build_template.VERSION_LABEL]

  @property
  def service_name(self):
    return self.labels[SERVICE_LABEL]

  @property
  def serving_state(self):
    return self.spec.servingState

  @property
  def image(self):
    return self._m.spec.container.image

  @property
  def image_digest(self):
    """The URL of the image, by digest. Stable when tags are not."""
    return self.status.imageDigest
