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

"""Lists Google Cloud Functions."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import exceptions as api_exceptions
from apitools.base.py import list_pager

from googlecloudsdk.api_lib.functions import util
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions as base_exceptions
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources


class List(base.ListCommand):
  """List Google Cloud Functions."""

  @staticmethod
  def Args(parser):
    parser.add_argument(
        '--regions',
        metavar='REGION',
        help=('Regions containing functions to list. By default, functions '
              'from the region configured in [functions/region] property are '
              'listed.'),
        type=arg_parsers.ArgList(min_length=1),
        default=[])
    parser.display_info.AddFormat(
        'table(name.basename(), status, trigger():label=TRIGGER, '
        'name.scope("locations").segment(0):label=REGION)')

  def Run(self, args):
    client = util.GetApiClientInstance()
    messages = util.GetApiMessagesModule()
    if args.regions:
      locations = args.regions
    else:
      locations = ['-']
    project = properties.VALUES.core.project.GetOrFail()
    limit = args.limit

    return self._YieldFromLocations(locations, project, limit, messages, client)

  def _YieldFromLocations(self, locations, project, limit, messages, client):
    for location in locations:
      location_ref = resources.REGISTRY.Parse(
          location,
          params={'projectsId': project},
          collection='cloudfunctions.projects.locations')
      for function in self._YieldFromLocation(
          location_ref, limit, messages, client):
        yield function

  def _YieldFromLocation(self, location_ref, limit, messages, client):
    list_generator = list_pager.YieldFromList(
        service=client.projects_locations_functions,
        request=self.BuildRequest(location_ref, messages),
        limit=limit, field='functions',
        batch_size_attribute='pageSize')
    # Decorators (e.g. util.CatchHTTPErrorRaiseHTTPException) don't work
    # for generators. We have to catch the exception above the iteration loop,
    # but inside the function.
    try:
      for item in list_generator:
        yield item
    except api_exceptions.HttpError as error:
      msg = util.GetHttpErrorMessage(error)
      exceptions.reraise(base_exceptions.HttpException(msg))

  def BuildRequest(self, location_ref, messages):
    return messages.CloudfunctionsProjectsLocationsFunctionsListRequest(
        parent=location_ref.RelativeName())
