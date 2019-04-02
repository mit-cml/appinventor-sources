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
"""Class for representing Cloud Run source objects."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import abc
from googlecloudsdk.api_lib.run import configuration
from googlecloudsdk.api_lib.run import k8s_object
from googlecloudsdk.command_lib.run import config_changes


class Deployable(config_changes.ConfigChanger):
  """An object representing a Cloud Run app, container, or function."""

  @abc.abstractmethod
  def UploadFiles(self):
    """Uploads the files associated with the deployable to the given bucket.

    Returns:
      A dict mapping source file names to GCS locations or None if source upload
      is irrelevant for this deployment type.
    """
    pass

  @abc.abstractmethod
  def AdjustConfiguration(self, conf, metadata):
    """Mutate the given Configuration to match the code this Deployable deploys.

    Args:
      conf: configuration.Configuration, the Configuration to mutate.
      metadata: the metadata of the top-level object the configuration spec
        belongs to
    """
    pass


class ServerlessContainer(Deployable):
  """A Cloud Run container deployment."""

  deployment_type = 'container'

  def __init__(self, source_ref):
    self.source_ref = source_ref

  def UploadFiles(self):
    return None  # Irrelevant for containers

  def AdjustConfiguration(self, conf, metadata):
    annotations = k8s_object.AnnotationsFromMetadata(
        conf.MessagesModule(), metadata)
    annotations[configuration.USER_IMAGE_ANNOTATION] = (
        self.source_ref.source_path)
    conf.image = self.source_ref.source_path
    # Unset the build if we're deploying an image.
    # TODO(b/112662240): Remove conditional once this field is public
    if hasattr(conf.spec, 'build'):
      conf.spec.build = None
