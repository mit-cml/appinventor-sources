# -*- coding: utf-8 -*- #
# Copyright 2015 Google Inc. All Rights Reserved.
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
"""Resource definitions for cloud platform apis."""

import enum


BASE_URL = 'https://iap.googleapis.com/'
DOCS_URL = 'https://cloud.google.com/iap'


class Collections(enum.Enum):
  """Collections for all supported apis."""

  PROJECTS = (
      'projects',
      'projects/{project}/iap_web',
      {},
      [u'project'],
      True
  )
  PROJECTS_IAP_WEB = (
      'projects.iap_web',
      'projects/{project}/iap_web/{iapWebId}',
      {},
      [u'project', u'iapWebId'],
      True
  )
  PROJECTS_IAP_WEB_SERVICES = (
      'projects.iap_web.services',
      'projects/{project}/iap_web/{iapWebId}/services/{serviceId}',
      {},
      [u'project', u'iapWebId', u'serviceId'],
      True
  )
  PROJECTS_IAP_WEB_SERVICES_VERSIONS = (
      'projects.iap_web.services.versions',
      'projects/{project}/iap_web/{iapWebId}/services/{serviceId}/versions/'
      '{versionId}',
      {},
      [u'project', u'iapWebId', u'serviceId', u'versionId'],
      True
  )

  def __init__(self, collection_name, path, flat_paths, params,
               enable_uri_parsing):
    self.collection_name = collection_name
    self.path = path
    self.flat_paths = flat_paths
    self.params = params
    self.enable_uri_parsing = enable_uri_parsing
