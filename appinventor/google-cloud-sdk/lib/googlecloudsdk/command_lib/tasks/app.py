# -*- coding: utf-8 -*- #
# Copyright 2017 Google Inc. All Rights Reserved.
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
"""Utilities for App Engine apps for `gcloud tasks` commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import exceptions as apitools_exceptions
from googlecloudsdk.api_lib.app import appengine_api_client as app_engine_api
from googlecloudsdk.api_lib.tasks import GetApiAdapter
from googlecloudsdk.calliope import base as calliope_base
from googlecloudsdk.command_lib.app import create_util
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import properties
from googlecloudsdk.core.console import console_io


class RegionResolvingError(exceptions.Error):
  """Error for when the app's region cannot be ultimately determined."""


def ResolveAppLocation(project_ref):
  """Determines Cloud Tasks location for the project or creates an app.

  Args:
    project_ref: The project resource to look up the location for.

  Returns:
    The existing or created app's locationId.

  Raises:
    RegionResolvingError: If the region of the app could not be determined.
  """
  location = _GetLocation(project_ref) or _CreateApp(project_ref)
  if location is not None:
    return location
  raise RegionResolvingError(
      'Could not determine the location for the project. Please try again.')


def _GetLocation(project_ref):
  """Gets the location from the Cloud Tasks API."""
  try:
    locations_client = GetApiAdapter(calliope_base.ReleaseTrack.BETA).locations
    locations = list(locations_client.List(project_ref, page_size=2))
    if len(locations) > 1:
      # Projects currently can only use Cloud Tasks in single region, so this
      # should never happen for now, but that will change in the future.
      raise RegionResolvingError('Multiple locations found for this project. '
                                 'Please specify an exact location.')
    if len(locations) == 1:
      return locations[0].labels.additionalProperties[0].value
    return None
  except apitools_exceptions.HttpNotFoundError:
    return None


def _CreateApp(project_ref):
  """Walks the user through creating an AppEngine app."""
  project = properties.VALUES.core.project.GetOrFail()
  if console_io.PromptContinue(
      message=('There is no App Engine app in project [{}].'.format(project)),
      prompt_string=('Would you like to create one'),
      throw_if_unattended=True):
    try:
      app_engine_api_client = app_engine_api.GetApiClientForTrack(
          calliope_base.ReleaseTrack.GA)
      create_util.CreateAppInteractively(app_engine_api_client, project)
    except create_util.AppAlreadyExistsError:
      raise create_util.AppAlreadyExistsError(
          'App already exists in project [{}]. This may be due a race '
          'condition. Please try again.'.format(project))
    else:
      return _GetLocation(project_ref)
  return None
