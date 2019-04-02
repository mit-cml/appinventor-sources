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


BASE_URL = 'https://dataflow.googleapis.com/v1b3/'
DOCS_URL = 'https://cloud.google.com/dataflow'


class Collections(enum.Enum):
  """Collections for all supported apis."""

  PROJECTS = (
      'projects',
      'projects/{projectId}',
      {},
      [u'projectId'],
      True
  )
  PROJECTS_JOBS = (
      'projects.jobs',
      'projects/{projectId}/jobs/{jobId}',
      {},
      [u'projectId', u'jobId'],
      True
  )
  PROJECTS_LOCATIONS = (
      'projects.locations',
      'projects/{projectId}/locations/{location}',
      {},
      [u'projectId', u'location'],
      True
  )
  PROJECTS_LOCATIONS_JOBS = (
      'projects.locations.jobs',
      'projects/{projectId}/locations/{location}/jobs/{jobId}',
      {},
      [u'projectId', u'location', u'jobId'],
      True
  )
  PROJECTS_LOCATIONS_TEMPLATES = (
      'projects.locations.templates',
      'projects/{projectId}/locations/{location}/templates:get',
      {},
      [u'projectId', u'location'],
      True
  )
  PROJECTS_TEMPLATES = (
      'projects.templates',
      'projects/{projectId}/templates:get',
      {},
      [u'projectId'],
      True
  )

  def __init__(self, collection_name, path, flat_paths, params,
               enable_uri_parsing):
    self.collection_name = collection_name
    self.path = path
    self.flat_paths = flat_paths
    self.params = params
    self.enable_uri_parsing = enable_uri_parsing
