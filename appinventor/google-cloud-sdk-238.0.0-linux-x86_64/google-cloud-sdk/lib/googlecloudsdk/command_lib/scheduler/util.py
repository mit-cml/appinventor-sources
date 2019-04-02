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
"""Utilities for "gcloud scheduler" commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import exceptions as apitools_exceptions
from apitools.base.py import list_pager
from googlecloudsdk.api_lib.app import appengine_api_client as app_engine_api
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base as calliope_base
from googlecloudsdk.command_lib.app import create_util
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import properties
from googlecloudsdk.core.console import console_io
from googlecloudsdk.core.util import http_encoding


_PUBSUB_MESSAGE_URL = 'type.googleapis.com/google.pubsub.v1.PubsubMessage'


def _GetPubsubMessages():
  return apis.GetMessagesModule('pubsub', apis.ResolveVersion('pubsub'))


def _GetSchedulerClient():
  return apis.GetClientInstance('cloudscheduler', 'v1beta1')


def _GetSchedulerMessages():
  return apis.GetMessagesModule('cloudscheduler', 'v1beta1')


def ModifyCreateJobRequest(job_ref, args, create_job_req):
  """Change the job.name field to a relative name."""
  del args  # Unused in ModifyCreateJobRequest
  create_job_req.job.name = job_ref.RelativeName()
  return create_job_req


def ModifyCreatePubsubJobRequest(job_ref, args, create_job_req):
  """Add the pubsubMessage field to the given request.

  Because the Cloud Scheduler API has a reference to a PubSub message, but
  represents it as a bag of properties, we need to construct the object here and
  insert it into the request.

  Args:
    job_ref: Resource reference to the job to be created (unused)
    args: argparse namespace with the parsed arguments from the command line. In
        particular, we expect args.message_body and args.attributes (optional)
        to be AdditionalProperty types.
    create_job_req: CloudschedulerProjectsLocationsJobsCreateRequest, the
        request constructed from the remaining arguments.

  Returns:
    CloudschedulerProjectsLocationsJobsCreateRequest: the given request but with
        the job.pubsubTarget.pubsubMessage field populated.
  """
  ModifyCreateJobRequest(job_ref, args, create_job_req)
  create_job_req.job.pubsubTarget.data = http_encoding.Encode(
      args.message_body or args.message_body_from_file)
  if args.attributes:
    create_job_req.job.pubsubTarget.attributes = args.attributes
  return create_job_req


def ParseAttributes(attributes):
  """Parse "--attributes" flag as an argparse type.

  The flag is given as a Calliope ArgDict:

      --attributes key1=value1,key2=value2

  Args:
    attributes: str, the value of the --attributes flag.

  Returns:
    dict, a dict with 'additionalProperties' as a key, and a list of dicts
        containing key-value pairs as the value.
  """
  attributes = arg_parsers.ArgDict()(attributes)
  return {
      'additionalProperties':
          [{'key': key, 'value': value}
           for key, value in sorted(attributes.items())]
  }


class RegionResolvingError(exceptions.Error):
  """Error for when the app's region cannot be ultimately determined."""


class AppLocationResolver(object):
  """Callable that resolves and caches the app location for the project.

  The "fallback" for arg marshalling gets used multiple times in the course of
  YAML command translation. This prevents multiple API roundtrips without making
  that class stateful.
  """

  def __init__(self):
    self.location = None

  def __call__(self):
    if self.location is None:
      self.location = self._ResolveAppLocation()
    return self.location

  def _ResolveAppLocation(self):
    """Determines Cloud Scheduler location for the project or creates an app."""
    project = properties.VALUES.core.project.GetOrFail()
    location = self._GetLocation(project) or self._CreateApp(project)
    if location is not None:
      return location
    raise RegionResolvingError(
        'Could not determine the location for the project. Please try again.')

  def _GetLocation(self, project):
    """Gets the location from the Cloud Scheduler API."""
    try:
      client = _GetSchedulerClient()
      messages = _GetSchedulerMessages()
      request = messages.CloudschedulerProjectsLocationsListRequest(
          name='projects/{}'.format(project))
      locations = list(list_pager.YieldFromList(
          client.projects_locations, request, batch_size=2, field='locations',
          batch_size_attribute='pageSize'))

      if len(locations) > 1:
        # Projects currently can only use Cloud Scheduler in single region, so
        # this should never happen for now, but that will change in the future.
        raise RegionResolvingError('Multiple locations found for this project. '
                                   'Please specify an exact location.')
      if len(locations) == 1:
        return locations[0].labels.additionalProperties[0].value
      return None
    except apitools_exceptions.HttpNotFoundError:
      return None

  def _CreateApp(self, project):
    """Walks the user through creating an AppEngine app."""
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
        return self._GetLocation(project)
    return None
