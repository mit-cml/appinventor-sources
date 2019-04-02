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


BASE_URL = 'https://websecurityscanner.googleapis.com/v1beta/'
DOCS_URL = 'https://cloud.google.com/security-scanner/'


class Collections(enum.Enum):
  """Collections for all supported apis."""

  PROJECTS = (
      'projects',
      'projects/{projectsId}',
      {},
      [u'projectsId'],
      True
  )
  PROJECTS_SCANCONFIGS = (
      'projects.scanConfigs',
      '{+name}',
      {
          '':
              'projects/{projectsId}/scanConfigs/{scanConfigsId}',
      },
      [u'name'],
      True
  )
  PROJECTS_SCANCONFIGS_SCANRUNS = (
      'projects.scanConfigs.scanRuns',
      '{+name}',
      {
          '':
              'projects/{projectsId}/scanConfigs/{scanConfigsId}/scanRuns/'
              '{scanRunsId}',
      },
      [u'name'],
      True
  )
  PROJECTS_SCANCONFIGS_SCANRUNS_CRAWLEDURLS = (
      'projects.scanConfigs.scanRuns.crawledUrls',
      'projects/{projectsId}/scanConfigs/{scanConfigsId}/scanRuns/'
      '{scanRunsId}',
      {},
      [u'projectsId', u'scanConfigsId', u'scanRunsId'],
      False
  )
  PROJECTS_SCANCONFIGS_SCANRUNS_FINDINGS = (
      'projects.scanConfigs.scanRuns.findings',
      '{+name}',
      {
          '':
              'projects/{projectsId}/scanConfigs/{scanConfigsId}/scanRuns/'
              '{scanRunsId}/findings/{findingsId}',
      },
      [u'name'],
      True
  )

  def __init__(self, collection_name, path, flat_paths, params,
               enable_uri_parsing):
    self.collection_name = collection_name
    self.path = path
    self.flat_paths = flat_paths
    self.params = params
    self.enable_uri_parsing = enable_uri_parsing
