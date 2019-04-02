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


BASE_URL = 'https://edge.googleapis.com/v1alpha1/'
DOCS_URL = 'https://cloud.google.com/iot-edge/'


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
  PROJECTS_LOCATIONS_REGISTRIES = (
      'projects.locations.registries',
      'projects/{projectsId}/locations/{locationsId}/registries/'
      '{registriesId}',
      {},
      [u'projectsId', u'locationsId', u'registriesId'],
      True
  )
  PROJECTS_LOCATIONS_REGISTRIES_DEVICES = (
      'projects.locations.registries.devices',
      'projects/{projectsId}/locations/{locationsId}/registries/'
      '{registriesId}/devices/{devicesId}',
      {},
      [u'projectsId', u'locationsId', u'registriesId', u'devicesId'],
      True
  )
  PROJECTS_LOCATIONS_REGISTRIES_DEVICES_CONTAINERS = (
      'projects.locations.registries.devices.containers',
      '{+name}',
      {
          '':
              'projects/{projectsId}/locations/{locationsId}/registries/'
              '{registriesId}/devices/{devicesId}/containers/{containersId}',
      },
      [u'name'],
      True
  )
  PROJECTS_LOCATIONS_REGISTRIES_DEVICES_FUNCTIONS = (
      'projects.locations.registries.devices.functions',
      '{+name}',
      {
          '':
              'projects/{projectsId}/locations/{locationsId}/registries/'
              '{registriesId}/devices/{devicesId}/functions/{functionsId}',
      },
      [u'name'],
      True
  )
  PROJECTS_LOCATIONS_REGISTRIES_DEVICES_MLMODELS = (
      'projects.locations.registries.devices.mlModels',
      '{+name}',
      {
          '':
              'projects/{projectsId}/locations/{locationsId}/registries/'
              '{registriesId}/devices/{devicesId}/mlModels/{mlModelsId}',
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
