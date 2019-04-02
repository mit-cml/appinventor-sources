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


BASE_URL = 'https://logging.googleapis.com/v2/'
DOCS_URL = 'https://cloud.google.com/logging/docs/'


class Collections(enum.Enum):
  """Collections for all supported apis."""

  BILLINGACCOUNTS = (
      'billingAccounts',
      'billingAccounts/{billingAccountsId}',
      {},
      [u'billingAccountsId'],
      True
  )
  BILLINGACCOUNTS_BUCKETS = (
      'billingAccounts.buckets',
      '{+name}',
      {
          '':
              'billingAccounts/{billingAccountsId}/buckets/{bucketsId}',
      },
      [u'name'],
      True
  )
  BILLINGACCOUNTS_EXCLUSIONS = (
      'billingAccounts.exclusions',
      '{+name}',
      {
          '':
              'billingAccounts/{billingAccountsId}/exclusions/{exclusionsId}',
      },
      [u'name'],
      True
  )
  BILLINGACCOUNTS_SINKS = (
      'billingAccounts.sinks',
      '{+sinkName}',
      {
          '':
              'billingAccounts/{billingAccountsId}/sinks/{sinksId}',
      },
      [u'sinkName'],
      True
  )
  BUCKETS = (
      'buckets',
      '{+name}',
      {
          '':
              '{v2Id}/{v2Id1}/buckets/{bucketsId}',
      },
      [u'name'],
      True
  )
  EXCLUSIONS = (
      'exclusions',
      '{+name}',
      {
          '':
              '{v2Id}/{v2Id1}/exclusions/{exclusionsId}',
      },
      [u'name'],
      True
  )
  FOLDERS = (
      'folders',
      'folders/{foldersId}',
      {},
      [u'foldersId'],
      True
  )
  FOLDERS_EXCLUSIONS = (
      'folders.exclusions',
      '{+name}',
      {
          '':
              'folders/{foldersId}/exclusions/{exclusionsId}',
      },
      [u'name'],
      True
  )
  FOLDERS_LOCATIONS = (
      'folders.locations',
      'folders/{foldersId}/locations/{locationsId}',
      {},
      [u'foldersId', u'locationsId'],
      True
  )
  FOLDERS_LOCATIONS_BUCKETS = (
      'folders.locations.buckets',
      '{+name}',
      {
          '':
              'folders/{foldersId}/locations/{locationsId}/buckets/'
              '{bucketsId}',
      },
      [u'name'],
      True
  )
  FOLDERS_SINKS = (
      'folders.sinks',
      '{+sinkName}',
      {
          '':
              'folders/{foldersId}/sinks/{sinksId}',
      },
      [u'sinkName'],
      True
  )
  ORGANIZATIONS = (
      'organizations',
      'organizations/{organizationsId}',
      {},
      [u'organizationsId'],
      True
  )
  ORGANIZATIONS_EXCLUSIONS = (
      'organizations.exclusions',
      '{+name}',
      {
          '':
              'organizations/{organizationsId}/exclusions/{exclusionsId}',
      },
      [u'name'],
      True
  )
  ORGANIZATIONS_LOCATIONS = (
      'organizations.locations',
      'organizations/{organizationsId}/locations/{locationsId}',
      {},
      [u'organizationsId', u'locationsId'],
      True
  )
  ORGANIZATIONS_LOCATIONS_BUCKETS = (
      'organizations.locations.buckets',
      '{+name}',
      {
          '':
              'organizations/{organizationsId}/locations/{locationsId}/'
              'buckets/{bucketsId}',
      },
      [u'name'],
      True
  )
  ORGANIZATIONS_SINKS = (
      'organizations.sinks',
      '{+sinkName}',
      {
          '':
              'organizations/{organizationsId}/sinks/{sinksId}',
      },
      [u'sinkName'],
      True
  )
  PROJECTS = (
      'projects',
      'projects/{projectsId}',
      {},
      [u'projectsId'],
      True
  )
  PROJECTS_EXCLUSIONS = (
      'projects.exclusions',
      '{+name}',
      {
          '':
              'projects/{projectsId}/exclusions/{exclusionsId}',
      },
      [u'name'],
      True
  )
  PROJECTS_LOCATIONS = (
      'projects.locations',
      'projects/{projectsId}/locations/{locationsId}',
      {},
      [u'projectsId', u'locationsId'],
      True
  )
  PROJECTS_LOCATIONS_BUCKETS = (
      'projects.locations.buckets',
      '{+name}',
      {
          '':
              'projects/{projectsId}/locations/{locationsId}/buckets/'
              '{bucketsId}',
      },
      [u'name'],
      True
  )
  PROJECTS_METRICS = (
      'projects.metrics',
      '{+metricName}',
      {
          '':
              'projects/{projectsId}/metrics/{metricsId}',
      },
      [u'metricName'],
      True
  )
  PROJECTS_SINKS = (
      'projects.sinks',
      '{+sinkName}',
      {
          '':
              'projects/{projectsId}/sinks/{sinksId}',
      },
      [u'sinkName'],
      True
  )
  SINKS = (
      'sinks',
      '{+sinkName}',
      {
          '':
              '{v2Id}/{v2Id1}/sinks/{sinksId}',
      },
      [u'sinkName'],
      True
  )

  def __init__(self, collection_name, path, flat_paths, params,
               enable_uri_parsing):
    self.collection_name = collection_name
    self.path = path
    self.flat_paths = flat_paths
    self.params = params
    self.enable_uri_parsing = enable_uri_parsing
