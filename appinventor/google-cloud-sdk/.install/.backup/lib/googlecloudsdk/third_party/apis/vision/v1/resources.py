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


BASE_URL = 'https://vision.googleapis.com/v1/'
DOCS_URL = 'https://cloud.google.com/vision/'


class Collections(enum.Enum):
  """Collections for all supported apis."""

  IMAGES = (
      'images',
      'images',
      {},
      [],
      True
  )
  LOCATIONS = (
      'locations',
      'locations/{locationsId}',
      {},
      [u'locationsId'],
      True
  )
  LOCATIONS_OPERATIONS = (
      'locations.operations',
      '{+name}',
      {
          '':
              'locations/{locationsId}/operations/{operationsId}',
      },
      [u'name'],
      True
  )
  OPERATIONS = (
      'operations',
      '{+name}',
      {
          '':
              'operations/{operationsId}',
      },
      [u'name'],
      True
  )
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
  PROJECTS_LOCATIONS_PRODUCTSETS = (
      'projects.locations.productSets',
      '{+name}',
      {
          '':
              'projects/{projectsId}/locations/{locationsId}/productSets/'
              '{productSetsId}',
      },
      [u'name'],
      True
  )
  PROJECTS_LOCATIONS_PRODUCTS = (
      'projects.locations.products',
      '{+name}',
      {
          '':
              'projects/{projectsId}/locations/{locationsId}/products/'
              '{productsId}',
      },
      [u'name'],
      True
  )
  PROJECTS_LOCATIONS_PRODUCTS_REFERENCEIMAGES = (
      'projects.locations.products.referenceImages',
      '{+name}',
      {
          '':
              'projects/{projectsId}/locations/{locationsId}/products/'
              '{productsId}/referenceImages/{referenceImagesId}',
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
