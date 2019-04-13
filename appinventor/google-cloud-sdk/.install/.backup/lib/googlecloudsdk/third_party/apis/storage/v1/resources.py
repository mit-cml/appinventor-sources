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


BASE_URL = 'https://www.googleapis.com/storage/v1/'
DOCS_URL = 'https://developers.google.com/storage/docs/json_api/'


class Collections(enum.Enum):
  """Collections for all supported apis."""

  BUCKETACCESSCONTROLS = (
      'bucketAccessControls',
      'b/{bucket}/acl/{entity}',
      {},
      [u'bucket', u'entity'],
      True
  )
  BUCKETS = (
      'buckets',
      'b/{bucket}',
      {},
      [u'bucket'],
      True
  )
  DEFAULTOBJECTACCESSCONTROLS = (
      'defaultObjectAccessControls',
      'b/{bucket}/defaultObjectAcl/{entity}',
      {},
      [u'bucket', u'entity'],
      True
  )
  NOTIFICATIONS = (
      'notifications',
      'b/{bucket}/notificationConfigs/{notification}',
      {},
      [u'bucket', u'notification'],
      True
  )
  OBJECTACCESSCONTROLS = (
      'objectAccessControls',
      'b/{bucket}/o/{object}/acl/{entity}',
      {},
      [u'bucket', u'object', u'entity'],
      True
  )
  OBJECTS = (
      'objects',
      'b/{bucket}/o/{object}',
      {},
      [u'bucket', u'object'],
      True
  )
  PROJECTS = (
      'projects',
      'projects/{project}',
      {},
      [u'project'],
      True
  )
  PROJECTS_SERVICEACCOUNT = (
      'projects.serviceAccount',
      'projects/{projectId}/serviceAccount',
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
