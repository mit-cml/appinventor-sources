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
"""Wraps a Cloud Run BuildTemplate message, making fields more convenient.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import json
from googlecloudsdk.api_lib.run import k8s_object


DEV_IMAGE_ANNOTATION = 'serverless.cloud.google.com/dev_image'
IGNORE_GLOB_ANNOTATION = 'serverless.cloud.google.com/ignore_glob'
LANGUAGE_LABEL = 'serverless.cloud.google.com/runtime_language'
VERSION_LABEL = 'serverless.cloud.google.com/runtime_version'

BUILD_TEMPLATE_NAMESPACE = 'build-templates'


class BuildTemplate(k8s_object.KubernetesObject):
  """Wraps a Cloud Run BuildTemplate object, making fields more convenient.

  Setting propertise on a BuildTemplate writes through to the nested
  Kubernetes-style fields. In this case, some of these fields are labels and
  annotations.
  """
  API_CATEGORY = 'build.dev'
  KIND = 'BuildTemplate'

  @classmethod
  def New(cls, client, namespace):
    ret = super(BuildTemplate, cls).New(client, namespace)
    ret.namespace = BUILD_TEMPLATE_NAMESPACE
    return ret

  @property
  def dev_image(self):
    return self.annotations.get(DEV_IMAGE_ANNOTATION)

  @property
  def language(self):
    return self.labels.get(LANGUAGE_LABEL)

  @property
  def version(self):
    return self.labels.get(VERSION_LABEL)

  @property
  def ignore_glob(self):
    """List of globs of files to ignore.

    To include only specific files, aka whitelisting, all files can be ignored,
    then certain files re-included. Order matters, per .gitignore/.gcloudignore
    spec.

    Returns:
      List[str].
    """
    ret = self.annotations.get(IGNORE_GLOB_ANNOTATION)
    if not ret:
      return []
    return json.loads(ret)
