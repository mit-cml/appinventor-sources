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


BASE_URL = 'https://dataproc.googleapis.com/v1/'
DOCS_URL = 'https://cloud.google.com/dataproc/'


class Collections(enum.Enum):
  """Collections for all supported apis."""

  PROJECTS = (
      'projects',
      'projects/{projectsId}',
      {},
      [u'projectsId'],
      True
  )
  PROJECTS_LOCATIONS = (
      'projects.locations',
      'projects/{projectsId}/locations/{locationsId}',
      {},
      [u'projectsId', u'locationsId'],
      True
  )
  PROJECTS_LOCATIONS_WORKFLOWTEMPLATES = (
      'projects.locations.workflowTemplates',
      '{+name}',
      {
          '':
              'projects/{projectsId}/locations/{locationsId}/'
              'workflowTemplates/{workflowTemplatesId}',
      },
      [u'name'],
      True
  )
  PROJECTS_REGIONS = (
      'projects.regions',
      'projects/{projectId}/regions/{regionId}',
      {},
      [u'projectId', u'regionId'],
      True
  )
  PROJECTS_REGIONS_CLUSTERS = (
      'projects.regions.clusters',
      'projects/{projectId}/regions/{region}/clusters/{clusterName}',
      {},
      [u'projectId', u'region', u'clusterName'],
      True
  )
  PROJECTS_REGIONS_JOBS = (
      'projects.regions.jobs',
      'projects/{projectId}/regions/{region}/jobs/{jobId}',
      {},
      [u'projectId', u'region', u'jobId'],
      True
  )
  PROJECTS_REGIONS_OPERATIONS = (
      'projects.regions.operations',
      '{+name}',
      {
          '':
              'projects/{projectsId}/regions/{regionsId}/operations/'
              '{operationsId}',
      },
      [u'name'],
      True
  )
  PROJECTS_REGIONS_OPERATIONS_LIST = (
      'projects.regions.operations_list',
      'projects/{projectId}/regions/{regionId}/operations',
      {},
      [u'projectId', u'regionId'],
      True
  )
  PROJECTS_REGIONS_WORKFLOWTEMPLATES = (
      'projects.regions.workflowTemplates',
      '{+name}',
      {
          '':
              'projects/{projectsId}/regions/{regionsId}/workflowTemplates/'
              '{workflowTemplatesId}',
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
