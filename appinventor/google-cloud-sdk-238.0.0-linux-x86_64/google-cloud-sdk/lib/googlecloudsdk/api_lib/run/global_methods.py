# -*- coding: utf-8 -*- #
# Copyright 2019 Google Inc. All Rights Reserved.
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
"""Methods and constants for global access."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.runtime_config import util
from googlecloudsdk.api_lib.util import apis

from googlecloudsdk.core import properties


SERVERLESS_API_NAME = 'run'
SERVERLESS_API_VERSION = 'v1alpha1'


def ListRegions():
  """Get the list of all available regions from control plane.

  Returns:
    A list of str, which are regions.
  """
  client = apis.GetClientInstance(SERVERLESS_API_NAME, SERVERLESS_API_VERSION)
  project_resource_relname = util.ProjectPath(
      properties.VALUES.core.project.Get(required=True))
  response = client.projects_locations.List(
      client.MESSAGES_MODULE.RunProjectsLocationsListRequest(
          name=project_resource_relname,
          pageSize=100))
  return [l.locationId for l in response.locations]
