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


BASE_URL = 'https://www.googleapis.com/sql/v1beta4/'
DOCS_URL = 'https://cloud.google.com/sql/docs/reference/latest'


class Collections(enum.Enum):
  """Collections for all supported apis."""

  BACKUPRUNS = (
      'backupRuns',
      'projects/{project}/instances/{instance}/backupRuns/{id}',
      {},
      [u'project', u'instance', u'id'],
      True
  )
  DATABASES = (
      'databases',
      'projects/{project}/instances/{instance}/databases/{database}',
      {},
      [u'project', u'instance', u'database'],
      True
  )
  INSTANCES = (
      'instances',
      'projects/{project}/instances/{instance}',
      {},
      [u'project', u'instance'],
      True
  )
  OPERATIONS = (
      'operations',
      'projects/{project}/operations/{operation}',
      {},
      [u'project', u'operation'],
      True
  )
  SSLCERTS = (
      'sslCerts',
      'projects/{project}/instances/{instance}/sslCerts/{sha1Fingerprint}',
      {},
      [u'project', u'instance', u'sha1Fingerprint'],
      True
  )
  PROJECTS = (
      'projects',
      'projects/{project}',
      {},
      [u'project'],
      True
  )

  def __init__(self, collection_name, path, flat_paths, params,
               enable_uri_parsing):
    self.collection_name = collection_name
    self.path = path
    self.flat_paths = flat_paths
    self.params = params
    self.enable_uri_parsing = enable_uri_parsing
