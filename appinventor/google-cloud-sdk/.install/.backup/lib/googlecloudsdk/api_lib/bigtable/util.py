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

"""A library that is used to support our commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import json
import re

from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.api_lib.util import waiter
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources


def GetAdminClient():
  """Shortcut to get the latest Bigtable Admin client."""
  return apis.GetClientInstance('bigtableadmin', 'v2')


def GetAdminMessages():
  """Shortcut to get the latest Bigtable Admin messages."""
  return apis.GetMessagesModule('bigtableadmin', 'v2')


def AddClusterIdArgs(parser):
  """Adds --zone and --cluster args to the parser."""
  parser.add_argument(
      '--zone',
      help='ID of the zone where the cluster is located.',
      # TODO(b/36049937): specify list of zones or not? eg...
      # choices=['europe-west1-c', 'us-central1-b'],
      required=True)
  parser.add_argument(
      'cluster',
      help='Unique ID of the cluster.')


def AddClusterInfoArgs(parser):
  """Adds --name and --nodes args to the parser."""
  parser.add_argument(
      '--description',
      help='Friendly name of the cluster.',
      required=True)
  parser.add_argument(
      '--nodes',
      help='Number of Cloud Bigtable nodes to serve.',
      required=True,
      type=int)
  parser.add_argument(
      '--async',
      help='Return immediately, without waiting for operation to finish.',
      action='store_true')


def ProjectUrl():
  return '/'.join(['projects', properties.VALUES.core.project.Get()])


def ZoneUrl(args):
  return '/'.join([ProjectUrl(), 'zones', args.zone])


def LocationUrl(location):
  # TODO(b/36049938): deprecate when a location resource is available in the API
  return '/'.join([ProjectUrl(), 'locations', location])


def ClusterUrl(args):
  """Creates the canonical URL for a cluster resource."""
  return '/'.join([ZoneUrl(args), 'clusters', args.cluster])


def MakeCluster(args):
  """Creates a dict representing a Cluster proto from user-specified args."""
  cluster = {}
  if args.description:
    cluster['display_name'] = args.description
  if args.nodes:
    cluster['serve_nodes'] = args.nodes
  return cluster


def ExtractZoneAndCluster(cluster_id):
  m = re.match('projects/[^/]+/zones/([^/]+)/clusters/(.*)', cluster_id)
  return m.group(1), m.group(2)


def _Await(result_service, operation_ref, message):
  client = GetAdminClient()
  poller = waiter.CloudOperationPoller(result_service, client.operations)
  return waiter.WaitFor(poller, operation_ref, message)


def AwaitCluster(operation_ref, message):
  """Waits for cluster long running operation to complete."""
  client = GetAdminClient()
  return _Await(client.projects_instances_clusters, operation_ref, message)


def AwaitInstance(operation_ref, message):
  """Waits for instance long running operation to complete."""
  client = GetAdminClient()
  return _Await(client.projects_instances, operation_ref, message)


def AwaitAppProfile(operation_ref, message):
  """Waits for app profile long running operation to complete."""
  client = GetAdminClient()
  return _Await(client.projects_instances_appProfiles, operation_ref, message)


def GetAppProfileRef(instance, app_profile):
  """Get a resource reference to an app profile."""
  return resources.REGISTRY.Parse(
      app_profile,
      params={
          'projectsId': properties.VALUES.core.project.GetOrFail,
          'instancesId': instance,
      },
      collection='bigtableadmin.projects.instances.appProfiles')


def GetClusterRef(instance, cluster):
  """Get a resource reference to a cluster."""
  return resources.REGISTRY.Parse(
      cluster,
      params={
          'projectsId': properties.VALUES.core.project.GetOrFail,
          'instancesId': instance,
      },
      collection='bigtableadmin.projects.instances.clusters')


def GetOperationRef(operation):
  """Get a resource reference to a long running operation."""
  return resources.REGISTRY.ParseRelativeName(operation.name,
                                              'bigtableadmin.operations')


def GetInstanceRef(instance):
  """Get a resource reference to an instance."""
  return resources.REGISTRY.Parse(
      instance,
      params={
          'projectsId': properties.VALUES.core.project.GetOrFail,
      },
      collection='bigtableadmin.projects.instances')


WARNING_TYPE_PREFIX = 'CLOUD_BIGTABLE_APP_PROFILE_WARNING'


def FormatErrorMessages(exception):
  """Format app profile error message from API and raise new exception.

  The error messages returned from the backend API are not formatted well when
  using the default format. This raises a new generic exception with a well
  formatted error message built from the original response.

  Args:
    exception: HttpError raised by API.

  Raises:
    exceptions.HttpException: Reformatted error raised by API.
  """
  response = json.loads(exception.content)
  if not (response['error'] and response['error']['details']):
    raise exception
  errors = ['Errors:']
  warnings = ['Warnings (use --force to ignore):']
  for detail in response['error']['details']:
    violations = detail.get('violations', [])
    for violation in violations:
      if violation.get('type').startswith(WARNING_TYPE_PREFIX):
        warnings.append(violation.get('description'))
      else:
        errors.append(violation.get('description'))

  error_msg = ''
  if len(warnings) > 1:
    error_msg += '\n\t'.join(warnings)
  if len(errors) > 1:
    error_msg += '\n\t'.join(errors)

  if not error_msg:
    raise exception
  raise exceptions.HttpException(
      exception, '{}\n{}'.format(response['error']['message'], error_msg))
