# -*- coding: utf-8 -*- #
# Copyright 2014 Google Inc. All Rights Reserved.
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
"""Common functions and classes for dealing with managed instances groups."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import random
import re
import string
import sys
from apitools.base.py import list_pager

from googlecloudsdk.api_lib.compute import exceptions
from googlecloudsdk.api_lib.compute import lister
from googlecloudsdk.api_lib.compute import path_simplifier
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions as calliope_exceptions
from googlecloudsdk.command_lib.compute.managed_instance_groups import auto_healing_utils
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.console import console_io
import six
from six.moves import range  # pylint: disable=redefined-builtin


_ALLOWED_UTILIZATION_TARGET_TYPES = [
    'DELTA_PER_MINUTE', 'DELTA_PER_SECOND', 'GAUGE']

_ALLOWED_UTILIZATION_TARGET_TYPES_LOWER = [
    'delta-per-minute', 'delta-per-second', 'gauge']

ARGS_CONFLICTING_WITH_AUTOSCALING_FILE_BETA = [
    'max_num_replicas', 'cool_down_period', 'custom_metric_utilization',
    'description', 'min_num_replicas',
    'scale_based_on_cpu', 'scale_based_on_load_balancing',
    'target_cpu_utilization', 'target_load_balancing_utilization'
]

ARGS_CONFLICTING_WITH_AUTOSCALING_FILE_ALPHA = (
    ARGS_CONFLICTING_WITH_AUTOSCALING_FILE_BETA + [
        'queue_scaling_acceptable_backlog_per_instance',
        'queue_scaling_cloud_pub_sub', 'queue_scaling_single_worker_throughput',
    ]
)

_MAX_AUTOSCALER_NAME_LENGTH = 63
# 4 character chosen from between lowercase letters and numbers give >1.6M
# possibilities with no more than 100 Autoscalers in one Zone and Project
# so probability that adding an autoscaler will fail because of name conflict
# is about 6e-5.
_NUM_RANDOM_CHARACTERS_IN_AS_NAME = 4

CLOUD_PUB_SUB_VALID_RESOURCE_RE = r'^[A-Za-z][A-Za-z0-9-_.~+%]{2,}$'


# TODO(b/110191362): resign from passing whole args to functions in this file


class Error(exceptions.Error):
  """Base exception for managed instance group exceptions."""


class ResourceNotFoundException(Error):
  """The user tries to get/use/update resource which does not exist."""


class ResourceAlreadyExistsException(Error):
  """The user tries to create resource which already exists."""


class ResourceCannotBeResolvedException(Error):
  """The user uses invalid / partial name to resolve URI for the resource."""


def ArgsSupportQueueScaling(args):
  return 'queue_scaling_acceptable_backlog_per_instance' in args


def AddAutoscalerArgs(
    parser, queue_scaling_enabled=False, autoscaling_file_enabled=False,
    stackdriver_metrics_flags=False, mode_enabled=False):
  """Adds commandline arguments to parser."""
  parser.add_argument(
      '--cool-down-period',
      type=arg_parsers.Duration(),
      help=('The time period that the autoscaler should wait before it starts '
            'collecting information from a new instance. This prevents the '
            'autoscaler from collecting information when the instance is '
            'initializing, during which the collected usage would not be '
            'reliable. The default is 60s. See $ gcloud topic datetimes '
            'for information on duration formats.'))
  parser.add_argument('--description', help='Notes about Autoscaler.')
  parser.add_argument('--min-num-replicas',
                      type=arg_parsers.BoundedInt(0, sys.maxsize),
                      help='Minimum number of replicas Autoscaler will set.')
  parser.add_argument('--max-num-replicas',
                      type=arg_parsers.BoundedInt(0, sys.maxsize),
                      required=not autoscaling_file_enabled,
                      help='Maximum number of replicas Autoscaler will set.')
  parser.add_argument('--scale-based-on-cpu',
                      action='store_true',
                      help='Autoscaler will be based on CPU utilization.')
  parser.add_argument('--scale-based-on-load-balancing',
                      action='store_true',
                      help=('Use autoscaling based on load balancing '
                            'utilization.'))
  parser.add_argument('--target-cpu-utilization',
                      type=arg_parsers.BoundedFloat(0.0, 1.0),
                      help='Autoscaler will aim to maintain CPU utilization at '
                      'target level (0.0 to 1.0).')
  parser.add_argument('--target-load-balancing-utilization',
                      type=arg_parsers.BoundedFloat(0.0, None),
                      help='Autoscaler will aim to maintain the load balancing '
                      'utilization level (greater than 0.0).')
  custom_metric_utilization_help = """\
Adds a target metric value for the Autoscaler to use.

*metric*::: Protocol-free URL of a Google Cloud Monitoring metric.

*utilization-target*::: Value of the metric Autoscaler will aim to
  maintain (greater than 0.0).

*utilization-target-type*::: How target is expressed. Valid values: {0}.
""".format(', '.join(_ALLOWED_UTILIZATION_TARGET_TYPES))
  if stackdriver_metrics_flags:
    custom_metric_utilization_help += """
Mutually exclusive with `--update-stackdriver-metric`.
"""
  parser.add_argument(
      '--custom-metric-utilization',
      type=arg_parsers.ArgDict(
          spec={
              'metric': str,
              'utilization-target': float,
              'utilization-target-type': str,
          },
      ),
      action='append',
      help=custom_metric_utilization_help,
  )

  if queue_scaling_enabled:
    parser.add_argument(
        '--queue-scaling-cloud-pub-sub',
        type=arg_parsers.ArgDict(
            spec={
                'topic': str,
                'subscription': str,
            },
        ),
        help="""\
        Specifies queue-based scaling based on a Cloud Pub/Sub queuing system.
        Both topic and subscription are required.

        *topic*::: Topic specification. Can be just a name or a partial URL
        (starting with "projects/..."). Topic must belong to the same project as
        Autoscaler.

        *subscription*::: Subscription specification. Can be just a name or a
        partial URL (starting with "projects/..."). Subscription must belong to
        the same project as Autoscaler and must be connected to the specified
        topic.
        """
    )
    parser.add_argument('--queue-scaling-acceptable-backlog-per-instance',
                        type=arg_parsers.BoundedFloat(0.0, None),
                        help='Queue-based scaling target: autoscaler will aim '
                        'to assure that average number of tasks in the queue '
                        'is no greater than this value.',)
    parser.add_argument('--queue-scaling-single-worker-throughput',
                        type=arg_parsers.BoundedFloat(0.0, None),
                        help='Hint the autoscaler for queue-based scaling on '
                        'how much throughput a single worker instance is able '
                        'to consume.')
  if autoscaling_file_enabled:
    parser.add_argument(
        '--autoscaling-file',
        metavar='PATH',
        help=('Path of the file from which autoscaling configuration will be '
              'loaded. This flag allows you to atomically setup complex '
              'autoscalers.'))
  if stackdriver_metrics_flags:
    parser.add_argument(
        '--remove-stackdriver-metric',
        metavar='METRIC',
        help=('Stackdriver metric to remove from autoscaling configuration. '
              'If the metric is the only input used for autoscaling the '
              'command will fail.'))
    parser.add_argument(
        '--update-stackdriver-metric',
        metavar='METRIC',
        help=('Stackdriver metric to use as an input for autoscaling. '
              'When using this flag you must also specify target value of the '
              'metric by specifying '
              '`--stackdriver-metric-single-instance-assignment` or '
              '`--stackdriver-metric-utilization-target` and '
              '`--stackdriver-metric-utilization-target-type`. '
              'Mutually exclusive with `--custom-metric-utilization`.'))
    parser.add_argument(
        '--stackdriver-metric-filter',
        metavar='FILTER',
        help=('Expression for filtering samples used to autoscale, see '
              'https://cloud.google.com/monitoring/api/v3/filters.'))
    parser.add_argument(
        '--stackdriver-metric-utilization-target',
        metavar='TARGET',
        type=float,
        help=('Value of the metric Autoscaler will aim to maintain. When '
              'specifying this flag you must also provide '
              '`--stackdriver-metric-utilization-target-type`. Mutually '
              'exclusive with '
              '`--stackdriver-metric-single-instance-assignment` and '
              '`--custom-metric-utilization`.'))

    parser.add_argument(
        '--stackdriver-metric-utilization-target-type',
        metavar='TARGET_TYPE',
        choices=_ALLOWED_UTILIZATION_TARGET_TYPES_LOWER,
        help=('Value of the metric Autoscaler will aim to maintain. When '
              'specifying this flag you must also provide '
              '`--stackdriver-metric-utilization-target`. Mutually '
              'exclusive with '
              '`--stackdriver-metric-single-instance-assignment` and '
              '`--custom-metric-utilization`.'))
    parser.add_argument(
        '--stackdriver-metric-single-instance-assignment',
        metavar='ASSIGNMENT',
        type=float,
        help=('Autoscaler will aim to maintain value of metric divided by '
              'number of instances at this level. Mutually '
              'exclusive with '
              '`-stackdriver-metric-utilization-target-type`, '
              '`-stackdriver-metric-utilization-target-type`, and '
              '`--custom-metric-utilization`.'))

  if mode_enabled:
    GetModeFlag().AddToParser(parser)


def GetModeFlag():
  # Can't use a ChoiceEnumMapper because we don't have access to the "messages"
  # module for the right API version.
  return base.ChoiceArgument(
      '--mode',
      {
          'on': ('to permit autoscaling to scale up and down (default for '
                 'new autoscalers).'),
          'only-up': 'to permit autoscaling to scale only up and not down.',
          'only-down': 'to permit autoscaling to scale only down and not up.',
          'off': ('to turn off autoscaling, while keeping the new '
                  'configuration.')
      },
      help_str="""\
          Set the mode of an autoscaler for a managed instance group.

          You can turn off or restrict MIG activities without changing MIG
          configuration and then having to restore it later. MIG configuration
          persists while the activities are turned off or restricted, and the
          activities pick it up when they are turned on again or when the
          restrictions are lifted.
      """)


def _ValidateCloudPubSubResource(pubsub_spec_dict, expected_resource_type):
  """Validate Cloud Pub/Sub resource spec format."""
  def RaiseInvalidArgument(message):
    raise calliope_exceptions.InvalidArgumentException(
        '--queue-scaling-cloud-pub-sub:{0}'.format(expected_resource_type),
        message)

  if expected_resource_type not in pubsub_spec_dict:
    raise calliope_exceptions.ToolException(
        'Both topic and subscription are required for Cloud Pub/Sub '
        'queue scaling specification.')
  split_resource = pubsub_spec_dict[expected_resource_type].split('/')

  if len(split_resource) == 1:
    resource_name = split_resource[0]
  elif len(split_resource) == 4:
    (project_prefix, unused_project_name,
     resource_prefix, resource_name) = split_resource
    if project_prefix != 'projects':
      RaiseInvalidArgument(
          'partial-URL format for Cloud PubSub resource does not start with '
          '"projects/"')
    if resource_prefix != '{0}s'.format(expected_resource_type):
      RaiseInvalidArgument('not in valid resource types: topic, subscription.')
  else:
    RaiseInvalidArgument(
        'Cloud PubSub resource must either be just a name or a partial '
        'URL (starting with "projects/").')
  if not re.match(CLOUD_PUB_SUB_VALID_RESOURCE_RE, resource_name):
    RaiseInvalidArgument('resource name not valid.')


def ValidateConflictsWithAutoscalingFile(args, conflicting_args):
  if (hasattr(args, 'autoscaling_file') and
      args.IsSpecified('autoscaling_file')):
    for arg in conflicting_args:
      if args.IsSpecified(arg):
        conflicting_flags = [
            '--' + a.replace('_', '-')
            for a in conflicting_args
        ]
        raise calliope_exceptions.ConflictingArgumentsException(
            *(['--autoscaling-file'] + conflicting_flags))


def _ValidateCustomMetricUtilizationVsUpdateStackdriverMetric(args):
  if (args.IsSpecified('custom_metric_utilization') and
      args.IsSpecified('update_stackdriver_metric')):
    raise calliope_exceptions.ConflictingArgumentsException(
        '--custom-metric-utilization', '--update-stackdriver-metric')


def _ValidateRemoveStackdriverMetricVsUpdateStackdriverMetric(args):
  if (args.IsSpecified('update_stackdriver_metric') and
      args.IsSpecified('remove_stackdriver_metric') and
      args.update_stackdriver_metric == args.remove_stackdriver_metric):
    raise calliope_exceptions.InvalidArgumentException(
        '--update-stackdriver-metric',
        'You can not remove Stackdriver metric you are updating with '
        '[--update-stackdriver-metric] flag.')


def _ValidateRequiringUpdateStackdriverMetric(args):
  if not args.IsSpecified('update_stackdriver_metric'):
    requiring_flags = [
        'stackdriver_metric_filter',
        'stackdriver_metric_single_instance_assignment',
        'stackdriver_metric_utilization_target',
        'stackdriver_metric_utilization_target_type',
    ]
    for f in requiring_flags:
      if args.IsSpecified(f):
        raise calliope_exceptions.RequiredArgumentException(
            '--' + f.replace('_', '-'),
            '[--update-stackdriver-metric] required to use this flag.')


def _ValidateRequiredByUpdateStackdriverMetric(args):
  if args.IsSpecified('update_stackdriver_metric'):
    one_of_required = [
        'stackdriver_metric_single_instance_assignment',
        'stackdriver_metric_utilization_target',]
    if not any([args.IsSpecified(f) for f in one_of_required]):
      flags = [
          '[--{}]'.format(f.replace('_', '--')) for f in one_of_required]
      msg = ('You must provide one of {} with '
             '[--update-stackdriver-metric].'.format(', '.join(flags)))
      raise calliope_exceptions.RequiredArgumentException(
          '--update-stackdriver-metric', msg)


def _ValidateSingleInstanceAssignmentVsUtilizationTarget(args):
  if args.IsSpecified('stackdriver_metric_single_instance_assignment'):
    potential_conflicting = [
        'stackdriver_metric_utilization_target',
        'stackdriver_metric_utilization_target_type',
    ]
    conflicting = [f for f in potential_conflicting if args.IsSpecified(f)]
    if any(conflicting):
      assignment_flag = '--stackdriver-metric-single-instance-assignment'
      conflicting_flags = [
          '[--{}]'.format(f.replace('_', '-')) for f in conflicting]
      raise calliope_exceptions.ConflictingArgumentsException(
          assignment_flag,
          'You cannot use any of {} with `{}`'.format(
              conflicting_flags, assignment_flag))


def _ValidateUtilizationTargetHasType(args):
  if (args.IsSpecified('stackdriver_metric_utilization_target') and
      not args.IsSpecified('stackdriver_metric_utilization_target_type')):
    raise calliope_exceptions.RequiredArgumentException(
        '--stackdriver-metric-utilization-target-type',
        'Required with [--stackdriver-metric-utilization-target].')


def ValidateStackdriverMetricsFlags(args):
  """Perform validations related to .*stackdriver-metric.* flags."""
  _ValidateCustomMetricUtilizationVsUpdateStackdriverMetric(args)
  _ValidateRemoveStackdriverMetricVsUpdateStackdriverMetric(args)
  _ValidateRequiringUpdateStackdriverMetric(args)
  _ValidateRequiredByUpdateStackdriverMetric(args)
  _ValidateSingleInstanceAssignmentVsUtilizationTarget(args)
  _ValidateUtilizationTargetHasType(args)


def ValidateGeneratedAutoscalerIsValid(args, autoscaler):
  if (args.IsSpecified('remove_stackdriver_metric') and
      not autoscaler.autoscalingPolicy.customMetricUtilizations and
      not autoscaler.autoscalingPolicy.cpuUtilization and
      not autoscaler.autoscalingPolicy.loadBalancingUtilization):
    raise calliope_exceptions.InvalidArgumentException(
        '--remove-stackdriver-metric',
        'This would remove the only signal used for autoscaling. If you want '
        'to stop autoscaling the Managed Instance Group use `stop-autoscaling` '
        'command instead.')


def ValidateAutoscalerArgs(args):
  """Validates args."""
  if args.min_num_replicas and args.max_num_replicas:
    if args.min_num_replicas > args.max_num_replicas:
      raise calliope_exceptions.InvalidArgumentException(
          '--max-num-replicas', 'can\'t be less than min num replicas.')

  if args.custom_metric_utilization:
    for custom_metric_utilization in args.custom_metric_utilization:
      for field in ('utilization-target', 'metric', 'utilization-target-type'):
        if field not in custom_metric_utilization:
          raise calliope_exceptions.InvalidArgumentException(
              '--custom-metric-utilization', field + ' not present.')
      if custom_metric_utilization['utilization-target'] < 0:
        raise calliope_exceptions.InvalidArgumentException(
            '--custom-metric-utilization utilization-target', 'less than 0.')

  if ArgsSupportQueueScaling(args):
    queue_spec_found = False
    queue_target_found = False
    if args.queue_scaling_cloud_pub_sub:
      _ValidateCloudPubSubResource(
          args.queue_scaling_cloud_pub_sub, 'topic')
      _ValidateCloudPubSubResource(
          args.queue_scaling_cloud_pub_sub, 'subscription')
      queue_spec_found = True

    if args.queue_scaling_acceptable_backlog_per_instance is not None:
      queue_target_found = True

    if queue_spec_found != queue_target_found:
      raise calliope_exceptions.ToolException(
          'Both queue specification and queue scaling target must be provided '
          'for queue-based autoscaling.')


def GetInstanceGroupManagerOrThrow(igm_ref, client):
  """Retrieves the given Instance Group Manager if possible.

  Args:
    igm_ref: reference to the Instance Group Manager.
    client: The compute client.
  Returns:
    Instance Group Manager object.
  """
  if hasattr(igm_ref, 'region'):
    service = client.apitools_client.regionInstanceGroupManagers
    request_type = service.GetRequestType('Get')
  if hasattr(igm_ref, 'zone'):
    service = client.apitools_client.instanceGroupManagers
    request_type = service.GetRequestType('Get')
  request = request_type(**igm_ref.AsDict())

  errors = []
  # Run through the generator to actually make the requests and get potential
  # errors.
  igm_details = client.MakeRequests([(service, 'Get', request)],
                                    errors_to_collect=errors)

  if errors or len(igm_details) != 1:
    utils.RaiseException(errors, ResourceNotFoundException,
                         error_message='Could not fetch resource:')
  return igm_details[0]


def CreateZoneRef(resources, data):
  """Create zone reference from object with project and zone fields."""
  return resources.Parse(
      None,
      params={'project': data.project,
              'zone': data.zone},
      collection='compute.zones')


def CreateRegionRef(resources, data):
  """Create region reference from object with project and region fields."""
  return resources.Parse(
      None,
      params={'project': data.project,
              'region': data.region},
      collection='compute.regions')


def GroupByProject(locations):
  """Group locations by project field."""
  result = {}
  for location in locations or []:
    if location.project not in result:
      result[location.project] = []
    result[location.project].append(location)
  return result


def AutoscalersForLocations(zones, regions, client,
                            fail_when_api_not_supported=True):
  """Finds all Autoscalers defined for a given project and locations.

  Args:
    zones: iterable of target zone references
    regions: iterable of target region references
    client: The compute client.
    fail_when_api_not_supported: If true, raise tool exception if API does not
        support autoscaling.
  Returns:
    A list of Autoscaler objects.
  """
  # Errors is passed through library calls and modified with
  # (ERROR_CODE, ERROR_MESSAGE) tuples.
  errors = []

  # Explicit list() is required to unwind the generator and make sure errors
  # are detected at this level.
  requests = []
  for project, zones in six.iteritems(GroupByProject(zones)):
    requests += lister.FormatListRequests(
        service=client.apitools_client.autoscalers,
        project=project,
        scopes=sorted(set([zone_ref.zone for zone_ref in zones])),
        scope_name='zone',
        filter_expr=None)

  if regions:
    if hasattr(client.apitools_client, 'regionAutoscalers'):
      for project, regions in six.iteritems(GroupByProject(regions)):
        requests += lister.FormatListRequests(
            service=client.apitools_client.regionAutoscalers,
            project=project,
            scopes=sorted(set([region_ref.region for region_ref in regions])),
            scope_name='region',
            filter_expr=None)
    else:
      if fail_when_api_not_supported:
        errors.append((None, 'API does not support regional autoscaling'))

  autoscalers = client.MakeRequests(
      requests=requests,
      errors_to_collect=errors)

  if errors:
    utils.RaiseToolException(
        errors,
        error_message='Could not check if the Managed Instance Group is '
        'Autoscaled.')

  return autoscalers


def AutoscalersForMigs(migs, autoscalers):
  """Finds Autoscalers with target amongst given IGMs.

  Args:
    migs: List of triples (IGM name, scope type, location reference).
    autoscalers: A list of Autoscalers to search among.
  Returns:
    A list of all Autoscalers with target on mig_names list.
  """
  igm_url_regexes = []
  for (name, scope_type, location) in migs:
    igm_url_regexes.append(
        '/projects/{project}/{scopeType}/{scopeName}/'
        'instanceGroupManagers/{name}$'
        .format(project=location.project,
                scopeType=(scope_type + 's'),
                scopeName=getattr(location, scope_type),
                name=name))
  igm_url_regex = re.compile('(' + ')|('.join(igm_url_regexes) + ')')
  result = [
      autoscaler for autoscaler in autoscalers
      if igm_url_regex.search(autoscaler.target)
  ]
  return result


def AutoscalerForMigByRef(client, resources, igm_ref):
  """Returns autoscaler targeting given instance group manager.

  Args:
    client: a GCE client
    resources: a GCE resource registry
    igm_ref: reference to instance group manager
  Returns:
    Autoscaler message with autoscaler targeting the IGM refferenced by
    igm_ref or None if there isn't one.
  Raises:
    ValueError: if instance group manager collection path is unknown
  """
  if igm_ref.Collection() == 'compute.instanceGroupManagers':
    scope_type = 'zone'
    location = CreateZoneRef(resources, igm_ref)
    zones, regions = [location], None
  elif igm_ref.Collection() == 'compute.regionInstanceGroupManagers':
    scope_type = 'region'
    location = CreateRegionRef(resources, igm_ref)
    zones, regions = None, [location]
  else:
    raise ValueError('Unknown reference type {0}'.format(igm_ref.Collection()))

  autoscalers = AutoscalersForLocations(
      regions=regions,
      zones=zones,
      client=client)

  return AutoscalerForMig(
      mig_name=igm_ref.Name(),
      autoscalers=autoscalers,
      location=location,
      scope_type=scope_type)


def AutoscalerForMig(mig_name, autoscalers, location, scope_type):
  """Finds Autoscaler targeting given IGM.

  Args:
    mig_name: Name of MIG targeted by Autoscaler.
    autoscalers: A list of Autoscalers to search among.
    location: Target location reference.
    scope_type: Target scope type.
  Returns:
    Autoscaler object for autoscaling the given Instance Group Manager or None
    when such Autoscaler does not exist.
  """
  autoscalers = AutoscalersForMigs(
      [(mig_name, scope_type, location)], autoscalers)
  if autoscalers:
    # For each Instance Group Manager there can be at most one Autoscaler having
    # the Manager as a target, so when one is found it can be returned as it is
    # the only one.
    if len(autoscalers) == 1:
      return autoscalers[0]
    else:
      raise calliope_exceptions.ToolException(
          'More than one Autoscaler with given target.')
  return None


def AddAutoscalersToMigs(migs_iterator,
                         client,
                         resources,
                         fail_when_api_not_supported=True):
  """Add Autoscaler to each IGM object if autoscaling is enabled for it."""
  def ParseZone(zone_link):
    return resources.Parse(
        zone_link,
        params={'project': properties.VALUES.core.project.GetOrFail},
        collection='compute.zones')

  def ParseRegion(region_link):
    return resources.Parse(
        region_link,
        params={'project': properties.VALUES.core.project.GetOrFail},
        collection='compute.regions')

  migs = list(migs_iterator)
  zones = set([ParseZone(mig['zone']) for mig in migs if 'zone' in mig])
  regions = set(
      [ParseRegion(mig['region']) for mig in migs if 'region' in mig])
  autoscalers = {}
  all_autoscalers = AutoscalersForLocations(
      zones=zones,
      regions=regions,
      client=client,
      fail_when_api_not_supported=fail_when_api_not_supported)

  for location in list(zones) + list(regions):
    autoscalers[location.Name()] = []

  for autoscaler in all_autoscalers:
    autoscaler_scope = None
    if autoscaler.zone is not None:
      autoscaler_scope = ParseZone(autoscaler.zone)
    if hasattr(autoscaler, 'region') and autoscaler.region is not None:
      autoscaler_scope = ParseRegion(autoscaler.region)
    if autoscaler_scope is not None:
      autoscalers.setdefault(autoscaler_scope.Name(), [])
      autoscalers[autoscaler_scope.Name()].append(autoscaler)

  for mig in migs:
    location = None
    scope_type = None
    if 'region' in mig:
      location = ParseRegion(mig['region'])
      scope_type = 'region'
    elif 'zone' in mig:
      location = ParseZone(mig['zone'])
      scope_type = 'zone'

    autoscaler = None
    if location and scope_type:
      autoscaler = AutoscalerForMig(
          mig_name=mig['name'],
          autoscalers=autoscalers[location.Name()],
          location=location,
          scope_type=scope_type)
    if autoscaler:
      mig['autoscaler'] = autoscaler
    yield mig


def _BuildCpuUtilization(args, messages):
  if args.target_cpu_utilization:
    return messages.AutoscalingPolicyCpuUtilization(
        utilizationTarget=args.target_cpu_utilization,
    )
  if args.scale_based_on_cpu:
    return messages.AutoscalingPolicyCpuUtilization()
  return None


def _BuildCustomMetricUtilizationsFromCustomMetricUtilizationFlag(
    flag, messages):
  """Translate --custom-metric-utilization flag to API message."""
  result = []
  for custom_metric_utilization in flag:
    result.append(
        messages.AutoscalingPolicyCustomMetricUtilization(
            utilizationTarget=custom_metric_utilization[
                'utilization-target'],
            metric=custom_metric_utilization['metric'],
            utilizationTargetType=(
                messages
                .AutoscalingPolicyCustomMetricUtilization
                .UtilizationTargetTypeValueValuesEnum(
                    custom_metric_utilization['utilization-target-type'],
                )
            ),
        )
    )
  return result


def _RemoveMetricFromList(metrics, to_remove):
  for i, metric in enumerate(metrics):
    if metric.metric == to_remove:
      del metrics[i]
      return


def _UpdateCustomMetricUtilizationsFromStackoverflowFlags(
    args, messages, original):
  """Take apply stackdriver flags to customMetricUtilizations."""
  if original:
    result = original.autoscalingPolicy.customMetricUtilizations
  else:
    result = []
  if args.remove_stackdriver_metric:
    _RemoveMetricFromList(result, args.remove_stackdriver_metric)
  if args.update_stackdriver_metric:
    _RemoveMetricFromList(result, args.update_stackdriver_metric)
    if args.stackdriver_metric_utilization_target_type:
      target_type = (
          messages.AutoscalingPolicyCustomMetricUtilization
          .UtilizationTargetTypeValueValuesEnum(
              args.stackdriver_metric_utilization_target_type.upper().
              replace('-', '_'),
          )
      )
    else:
      target_type = None

    if args.stackdriver_metric_filter and "'" in args.stackdriver_metric_filter:
      log.warning(
          "The provided filter contains a single quote character ('). While "
          "valid as a metric/resource label value, it's not a control "
          "character that is part of the filtering language; if you meant "
          "to use it to quote a string value, you need to use a double quote "
          "character (\") instead.")
    result.append(
        messages.AutoscalingPolicyCustomMetricUtilization(
            utilizationTarget=args.stackdriver_metric_utilization_target,
            metric=args.update_stackdriver_metric,
            utilizationTargetType=target_type,
            singleInstanceAssignment=(
                args.stackdriver_metric_single_instance_assignment
            ),
            filter=args.stackdriver_metric_filter,
        )
    )
  return result


def _BuildCustomMetricUtilizations(args, messages, original):
  """Builds custom metric utilization policy list from args.

  Args:
    args: command line arguments.
    messages: module containing message classes.
    original: original autoscaler message.
  Returns:
    AutoscalingPolicyCustomMetricUtilization list.
  """
  if args.custom_metric_utilization:
    return _BuildCustomMetricUtilizationsFromCustomMetricUtilizationFlag(
        args.custom_metric_utilization, messages)
  if hasattr(args, 'stackdriver_metric_filter'):
    return _UpdateCustomMetricUtilizationsFromStackoverflowFlags(
        args, messages, original)


def _BuildLoadBalancingUtilization(args, messages):
  if args.target_load_balancing_utilization:
    return messages.AutoscalingPolicyLoadBalancingUtilization(
        utilizationTarget=args.target_load_balancing_utilization,
    )
  if args.scale_based_on_load_balancing:
    return messages.AutoscalingPolicyLoadBalancingUtilization()
  return None


def _BuildQueueBasedScaling(args, messages):
  """Builds queue based scaling policy from args.

  Args:
    args: command line arguments.
    messages: module containing message classes.
  Returns:
    AutoscalingPolicyQueueBasedScaling message object or None.
  """
  if not ArgsSupportQueueScaling(args):
    return None

  queue_policy_dict = {}
  if args.queue_scaling_cloud_pub_sub:
    queue_policy_dict['cloudPubSub'] = (
        messages.AutoscalingPolicyQueueBasedScalingCloudPubSub(
            topic=args.queue_scaling_cloud_pub_sub['topic'],
            subscription=args.queue_scaling_cloud_pub_sub['subscription']))
  else:
    return None  # No queue spec.

  if args.queue_scaling_acceptable_backlog_per_instance is not None:
    queue_policy_dict['acceptableBacklogPerInstance'] = (
        args.queue_scaling_acceptable_backlog_per_instance)
  else:
    return None  # No queue target.

  if args.queue_scaling_single_worker_throughput is not None:
    queue_policy_dict['singleWorkerThroughputPerSec'] = (
        args.queue_scaling_single_worker_throughput)

  return messages.AutoscalingPolicyQueueBasedScaling(**queue_policy_dict)


def ParseModeString(mode, messages):
  return messages.AutoscalingPolicy.ModeValueValuesEnum(
      mode.upper().replace('-', '_'))


def _BuildMode(args, messages, original):
  if not args.mode:
    return original.autoscalingPolicy.mode if original else None
  return ParseModeString(args.mode, messages)


def _BuildAutoscalerPolicy(args, messages, original, mode_enabled=False):
  """Builds AutoscalingPolicy from args.

  Args:
    args: command line arguments.
    messages: module containing message classes.
    original: original autoscaler message.
    mode_enabled: bool, whether to include the 'autoscalingPolicy.mode' field in
      the message.
  Returns:
    AutoscalingPolicy message object.
  """
  policy_dict = {
      'coolDownPeriodSec': args.cool_down_period,
      'cpuUtilization': _BuildCpuUtilization(args, messages),
      'customMetricUtilizations': _BuildCustomMetricUtilizations(
          args, messages, original),
      'loadBalancingUtilization': _BuildLoadBalancingUtilization(
          args, messages),
      'queueBasedScaling': _BuildQueueBasedScaling(args, messages),
      'maxNumReplicas': args.max_num_replicas,
      'minNumReplicas': args.min_num_replicas,
  }
  if mode_enabled:
    policy_dict['mode'] = _BuildMode(args, messages, original)
  return messages.AutoscalingPolicy(
      **dict((key, value) for key, value in six.iteritems(policy_dict)
             if value is not None))  # Filter out None values.


def AdjustAutoscalerNameForCreation(autoscaler_resource, igm_ref):
  """Set name of autoscaler o be created.

  If autoscaler name is not None it wNone ill be used as a prefix of name of the
  autoscaler to be created. Prefix may be shortened so that the name fits below
  length limit. Name prefix is followed by '-' character and four
  random letters.

  Args:
    autoscaler_resource: Autoscaler resource to be created.
    igm_ref: reference to Instance Group Manager targeted by the Autoscaler.
  """
  if autoscaler_resource.name is None:
    autoscaler_resource.name = igm_ref.Name()
  trimmed_name = autoscaler_resource.name[
      0:(_MAX_AUTOSCALER_NAME_LENGTH - _NUM_RANDOM_CHARACTERS_IN_AS_NAME - 1)]
  random_characters = [
      random.choice(string.ascii_lowercase + string.digits)
      for _ in range(_NUM_RANDOM_CHARACTERS_IN_AS_NAME)
  ]
  random_suffix = ''.join(random_characters)
  new_name = '{0}-{1}'.format(trimmed_name, random_suffix)
  autoscaler_resource.name = new_name


def BuildAutoscaler(args, messages, igm_ref, name, original,
                    mode_enabled=False):
  """Builds autoscaler message protocol buffer."""
  autoscaler = messages.Autoscaler(
      autoscalingPolicy=_BuildAutoscalerPolicy(args, messages, original,
                                               mode_enabled=mode_enabled),
      description=args.description,
      name=name,
      target=igm_ref.SelfLink(),
  )
  return autoscaler


def CreateAutohealingPolicies(messages, health_check, initial_delay):
  """Creates autohealing policy list from args."""
  if health_check is None and initial_delay is None:
    return []
  policy = messages.InstanceGroupManagerAutoHealingPolicy()
  if health_check:
    policy.healthCheck = health_check
  if initial_delay:
    policy.initialDelaySec = initial_delay
  return [policy]


def ModifyAutohealingPolicies(current_policies, messages, args,
                              health_check_url):
  """Modifies existing autohealing policy from args."""
  if args.clear_autohealing:
    return [messages.InstanceGroupManagerAutoHealingPolicy()]
  if not health_check_url and not args.initial_delay:
    # No modifications of AutoHealingPolicies.
    return None
  current_policy = current_policies[0] if current_policies else None
  policy = current_policy or messages.InstanceGroupManagerAutoHealingPolicy()
  if health_check_url:
    policy.healthCheck = health_check_url
  if args.initial_delay:
    policy.initialDelaySec = args.initial_delay
  return [policy]


def ValidateAutohealingPolicies(auto_healing_policies):
  """Validates autohealing policies.

  Args:
    auto_healing_policies: list of AutoHealingPolicies to validate
  """
  if not auto_healing_policies:
    return
  # Only a single auto_healing_policy is allowed. Displaying warnings for any
  # additional entries is unnecessary as an error will be returned.
  policy = auto_healing_policies[0]
  if not policy.healthCheck and policy.initialDelaySec:
    message = ('WARNING: Health check should be provided when specifying '
               'initial delay.')
    console_io.PromptContinue(message=message, cancel_on_no=True)


def _GetInstanceTemplatesSet(*versions_lists):
  versions_set = set()
  for versions_list in versions_lists:
    versions_set.update(versions_list)
  return versions_set


def ValidateVersions(igm_info, new_versions, force=False):
  """Validates whether versions provided by user are consistent.

  Args:
    igm_info: instance group manager resource.
    new_versions: list of new versions.
    force: if true, we allow any combination of instance templates, as long as
    they are different. If false, only the following transitions are allowed:
    X -> Y, X -> (X, Y), (X, Y) -> X, (X, Y) -> Y, (X, Y) -> (X, Y)
  """
  if (len(new_versions) == 2
      and new_versions[0].instanceTemplate == new_versions[1].instanceTemplate):
    raise calliope_exceptions.ToolException(
        'Provided instance templates must be different.')
  if force:
    return

  # Only X -> Y, X -> (X, Y), (X, Y) -> X, (X, Y) -> Y, (X, Y) -> (X, Y)
  # are allowed in gcloud (unless --force)
  # Equivalently, at most two versions in old and new versions set union
  if igm_info.versions:
    igm_templates = [version.instanceTemplate for version in igm_info.versions]
  elif igm_info.instanceTemplate:
    igm_templates = [igm_info.instanceTemplate]
  else:
    raise calliope_exceptions.ToolException(
        'Either versions or instance template must be specified for '
        'managed instance group.')

  new_templates = [version.instanceTemplate for version in new_versions]
  version_count = len(_GetInstanceTemplatesSet(igm_templates, new_templates))
  if version_count > 2:
    raise calliope_exceptions.ToolException(
        'Update inconsistent with current state. '
        'The only allowed transitions between versions are: '
        'X -> Y, X -> (X, Y), (X, Y) -> X, (X, Y) -> Y, (X, Y) -> (X, Y). '
        'Please check versions templates or use --force.')


def AddAutoscaledPropertyToMigs(migs, client, resources):
  """Add Autoscaler information if Autoscaler is defined for the MIGs.

  Issue additional queries to detect if any given Instange Group Manager is
  a target of some autoscaler and add this information to in 'autoscaled'
  property.

  Args:
    migs: list of dicts, List of IGM resources converted to dictionaries
    client: a GCE client
    resources: a GCE resource registry

  Returns:
    Pair of:
    - boolean - True iff any autoscaler has an error
    - Copy of migs list with additional property 'autoscaled' set to 'No'/'Yes'/
    'Yes (*)' for each MIG depending on look-up result.
  """

  augmented_migs = []
  had_errors = False
  for mig in AddAutoscalersToMigs(
      migs_iterator=_ComputeInstanceGroupSize(migs, client, resources),
      client=client,
      resources=resources,
      fail_when_api_not_supported=False):
    if 'autoscaler' in mig and mig['autoscaler'] is not None:
      # status is present in autoscaler iff Autoscaler message has embedded
      # StatusValueValuesEnum defined.
      if (getattr(mig['autoscaler'], 'status', False) and mig['autoscaler']
          .status == client.messages.Autoscaler.StatusValueValuesEnum.ERROR):
        mig['autoscaled'] = 'yes (*)'
        had_errors = True
      else:
        mig['autoscaled'] = 'yes'
    else:
      mig['autoscaled'] = 'no'
    augmented_migs.append(mig)
  return (had_errors, augmented_migs)


def _ComputeInstanceGroupSize(items, client, resources):
  """Add information about Instance Group size."""
  errors = []
  zone_refs = [
      resources.Parse(
          mig['zone'],
          params={'project': properties.VALUES.core.project.GetOrFail},
          collection='compute.zones') for mig in items if 'zone' in mig
  ]
  region_refs = [
      resources.Parse(
          mig['region'],
          params={'project': properties.VALUES.core.project.GetOrFail},
          collection='compute.regions') for mig in items if 'region' in mig
  ]

  zonal_instance_groups = []
  for project, zone_refs in six.iteritems(GroupByProject(zone_refs)):
    zonal_instance_groups.extend(
        lister.GetZonalResources(
            service=client.apitools_client.instanceGroups,
            project=project,
            requested_zones=set([zone.zone for zone in zone_refs]),
            filter_expr=None,
            http=client.apitools_client.http,
            batch_url=client.batch_url,
            errors=errors))

  regional_instance_groups = []
  if getattr(client.apitools_client, 'regionInstanceGroups', None):
    for project, region_refs in six.iteritems(GroupByProject(region_refs)):
      regional_instance_groups.extend(
          lister.GetRegionalResources(
              service=client.apitools_client.regionInstanceGroups,
              project=project,
              requested_regions=set([region.region for region in region_refs]),
              filter_expr=None,
              http=client.apitools_client.http,
              batch_url=client.batch_url,
              errors=errors))

  instance_groups = zonal_instance_groups + regional_instance_groups
  instance_group_uri_to_size = {ig.selfLink: ig.size for ig in instance_groups}

  if errors:
    utils.RaiseToolException(errors)

  for item in items:
    self_link = item['selfLink']
    gm_self_link = self_link.replace('/instanceGroupManagers/',
                                     '/instanceGroups/')

    item['size'] = str(instance_group_uri_to_size.get(gm_self_link, ''))
    yield item


def GetHealthCheckUri(resources, args):
  """Creates health check reference from args."""
  if args.health_check:
    ref = auto_healing_utils.HEALTH_CHECK_ARG.ResolveAsResource(args, resources)
    return ref.SelfLink()
  if args.http_health_check:
    return resources.Parse(
        args.http_health_check,
        params={'project': properties.VALUES.core.project.GetOrFail},
        collection='compute.httpHealthChecks').SelfLink()
  if args.https_health_check:
    return resources.Parse(
        args.https_health_check,
        params={'project': properties.VALUES.core.project.GetOrFail},
        collection='compute.httpsHealthChecks').SelfLink()


# TODO(b/70203649): improve/fix method (no silent errors, add optimizations)
def CreateInstanceReferences(holder, igm_ref, instance_names):
  """Creates references to instances in instance group (zonal or regional)."""
  if igm_ref.Collection() == 'compute.instanceGroupManagers':
    instance_refs = []
    for instance in instance_names:
      instance_refs.append(
          holder.resources.Parse(
              instance,
              params={
                  'project': igm_ref.project,
                  'zone': igm_ref.zone,
              },
              collection='compute.instances'))
    return instance_refs
  elif igm_ref.Collection() == 'compute.regionInstanceGroupManagers':
    messages = holder.client.messages
    request = (
        messages.ComputeRegionInstanceGroupManagersListManagedInstancesRequest)(
            instanceGroupManager=igm_ref.Name(),
            region=igm_ref.region,
            project=igm_ref.project)
    managed_instances = list_pager.YieldFromList(
        service=holder.client.apitools_client.regionInstanceGroupManagers,
        batch_size=500,
        request=request,
        method='ListManagedInstances',
        field='managedInstances',
    )
    instances_to_return = []
    for instance_ref in managed_instances:
      if path_simplifier.Name(
          instance_ref.instance
      ) in instance_names or instance_ref.instance in instance_names:
        instances_to_return.append(instance_ref.instance)
    return instances_to_return
  else:
    raise ValueError('Unknown reference type {0}'.format(igm_ref.Collection()))


def GetDeviceNamesFromStatefulPolicy(stateful_policy):
  """Returns a list of device names from given StatefulPolicy message."""
  if not stateful_policy or not stateful_policy.preservedState \
      or not stateful_policy.preservedState.disks:
    return []
  return [disk.key
          for disk in stateful_policy.preservedState.disks.additionalProperties]


def IsAutoscalerNew(autoscaler):
  return getattr(autoscaler, 'name', None) is None


def ApplyInstanceRedistributionTypeToUpdatePolicy(
    client, instance_redistribution_type, update_policy):
  if instance_redistribution_type:
    if update_policy is None:
      update_policy = client.messages.InstanceGroupManagerUpdatePolicy()
    update_policy.instanceRedistributionType = (
        client.messages.InstanceGroupManagerUpdatePolicy.
        InstanceRedistributionTypeValueValuesEnum)(
            instance_redistribution_type)
  return update_policy
