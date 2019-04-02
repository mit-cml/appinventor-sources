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
"""Utilities for parsing arguments to `gcloud tasks` commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import encoding
from googlecloudsdk.command_lib.tasks import app
from googlecloudsdk.command_lib.tasks import constants
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources
from googlecloudsdk.core.console import console_io
from googlecloudsdk.core.util import http_encoding

import six  # pylint: disable=unused-import
from six.moves import filter  # pylint:disable=redefined-builtin
from six.moves import map  # pylint:disable=redefined-builtin


_PROJECT = properties.VALUES.core.project.GetOrFail


class NoFieldsSpecifiedError(exceptions.Error):
  """Error for when calling an update method with no fields specified."""


class FullTaskUnspecifiedError(exceptions.Error):
  """Error parsing task without specifing the queue or full path."""


def ParseProject():
  return resources.REGISTRY.Parse(
      _PROJECT(),
      collection=constants.PROJECTS_COLLECTION)


def ParseLocation(location):
  return resources.REGISTRY.Parse(
      location,
      params={'projectsId': _PROJECT},
      collection=constants.LOCATIONS_COLLECTION)


def ParseQueue(queue, location=None):
  """Parses an id or uri for a queue.

  Args:
    queue: An id, self-link, or relative path of a queue resource.
    location: The location of the app associated with the active project.

  Returns:
    A queue resource reference, or None if passed-in queue is Falsy.
  """
  if not queue:
    return None

  queue_ref = None
  try:
    queue_ref = resources.REGISTRY.Parse(queue,
                                         collection=constants.QUEUES_COLLECTION)
  except resources.RequiredFieldOmittedException:
    app_location = location or app.ResolveAppLocation(ParseProject())
    location_ref = ParseLocation(app_location)
    queue_ref = resources.REGISTRY.Parse(
        queue, params={'projectsId': location_ref.projectsId,
                       'locationsId': location_ref.locationsId},
        collection=constants.QUEUES_COLLECTION)
  return queue_ref


def ParseTask(task, queue_ref=None):
  """Parses an id or uri for a task."""
  params = queue_ref.AsDict() if queue_ref else None
  try:
    return resources.REGISTRY.Parse(task,
                                    collection=constants.TASKS_COLLECTION,
                                    params=params)
  except resources.RequiredFieldOmittedException:
    raise FullTaskUnspecifiedError(
        'Must specify either the fully qualified task path or the queue flag.')


def ExtractLocationRefFromQueueRef(queue_ref):
  params = queue_ref.AsDict()
  del params['queuesId']
  location_ref = resources.REGISTRY.Parse(
      None, params=params, collection=constants.LOCATIONS_COLLECTION)
  return location_ref


def ParseCreateOrUpdateQueueArgs(args,
                                 queue_type,
                                 messages,
                                 is_update=False,
                                 is_alpha=False):
  """Parses queue level args."""
  if is_alpha:
    return messages.Queue(
        retryConfig=_ParseRetryConfigArgs(args, queue_type, messages,
                                          is_update, is_alpha),
        rateLimits=_ParseAlphaRateLimitsArgs(args, queue_type, messages,
                                             is_update),
        pullTarget=_ParsePullTargetArgs(args, queue_type, messages, is_update),
        appEngineHttpTarget=_ParseAppEngineHttpTargetArgs(
            args, queue_type, messages, is_update))
  else:
    return messages.Queue(
        retryConfig=_ParseRetryConfigArgs(args, queue_type, messages,
                                          is_update, is_alpha),
        rateLimits=_ParseRateLimitsArgs(args, queue_type, messages, is_update),
        appEngineHttpQueue=_ParseAppEngineHttpQueueArgs(args, queue_type,
                                                        messages, is_update))


def ParseCreateTaskArgs(args, task_type, messages, is_alpha=False):
  """Parses task level args."""
  if is_alpha:
    return messages.Task(
        scheduleTime=args.schedule_time,
        pullMessage=_ParsePullMessageArgs(args, task_type, messages),
        appEngineHttpRequest=_ParseAlphaAppEngineHttpRequestArgs(
            args, task_type, messages))
  else:
    return messages.Task(
        scheduleTime=args.schedule_time,
        appEngineHttpRequest=_ParseAppEngineHttpRequestArgs(args, task_type,
                                                            messages))


def CheckUpdateArgsSpecified(args, queue_type, is_alpha=False):
  """Verifies that args are valid for updating a queue."""
  if queue_type == constants.PULL_QUEUE:
    if not _AnyArgsSpecified(args, ['max_attempts', 'max_retry_duration'],
                             clear_args=True):
      raise NoFieldsSpecifiedError('Must specify at least one field to update.')
  if queue_type == constants.APP_ENGINE_QUEUE:
    if is_alpha:
      if not _AnyArgsSpecified(
          args, [
              'max_attempts', 'max_retry_duration', 'max_doublings',
              'min_backoff', 'max_backoff', 'max_tasks_dispatched_per_second',
              'max_concurrent_tasks', 'routing_override'
          ],
          clear_args=True):
        raise NoFieldsSpecifiedError(
            'Must specify at least one field to update.')
    else:
      if not _AnyArgsSpecified(
          args, [
              'max_attempts', 'max_retry_duration', 'max_doublings',
              'min_backoff', 'max_backoff', 'max_dispatches_per_second',
              'max_concurrent_dispatches', 'routing_override'
          ],
          clear_args=True):
        raise NoFieldsSpecifiedError(
            'Must specify at least one field to update.')


def _AnyArgsSpecified(specified_args_object, args_list, clear_args=False):
  clear_args_list = []
  if clear_args:
    clear_args_list = [_EquivalentClearArg(a) for a in args_list]
  return any(
      filter(specified_args_object.IsSpecified, args_list + clear_args_list))


def _EquivalentClearArg(arg):
  return 'clear_{}'.format(arg)


def _ParseRetryConfigArgs(args, queue_type, messages, is_update,
                          is_alpha=False):
  """Parses the attributes of 'args' for Queue.retryConfig."""
  if (queue_type == constants.PULL_QUEUE and
      _AnyArgsSpecified(args, ['max_attempts', 'max_retry_duration'],
                        clear_args=is_update)):
    retry_config = messages.RetryConfig(
        maxRetryDuration=args.max_retry_duration)
    _AddMaxAttemptsFieldsFromArgs(args, retry_config, is_alpha)
    return retry_config

  if (queue_type == constants.APP_ENGINE_QUEUE and
      _AnyArgsSpecified(args, ['max_attempts', 'max_retry_duration',
                               'max_doublings', 'min_backoff', 'max_backoff'],
                        clear_args=is_update)):
    retry_config = messages.RetryConfig(
        maxRetryDuration=args.max_retry_duration,
        maxDoublings=args.max_doublings, minBackoff=args.min_backoff,
        maxBackoff=args.max_backoff)
    _AddMaxAttemptsFieldsFromArgs(args, retry_config, is_alpha)
    return retry_config


def _AddMaxAttemptsFieldsFromArgs(args, config_object, is_alpha=False):
  if args.IsSpecified('max_attempts'):
    # args.max_attempts is a BoundedInt and so None means unlimited
    if args.max_attempts is None:
      if is_alpha:
        config_object.unlimitedAttempts = True
      else:
        config_object.maxAttempts = -1
    else:
      config_object.maxAttempts = args.max_attempts


def _ParseAlphaRateLimitsArgs(args, queue_type, messages, is_update):
  """Parses the attributes of 'args' for Queue.rateLimits."""
  if (queue_type == constants.APP_ENGINE_QUEUE and
      _AnyArgsSpecified(args, ['max_tasks_dispatched_per_second',
                               'max_concurrent_tasks'],
                        clear_args=is_update)):
    return messages.RateLimits(
        maxTasksDispatchedPerSecond=args.max_tasks_dispatched_per_second,
        maxConcurrentTasks=args.max_concurrent_tasks)


def _ParseRateLimitsArgs(args, queue_type, messages, is_update):
  """Parses the attributes of 'args' for Queue.rateLimits."""
  if (queue_type == constants.APP_ENGINE_QUEUE and _AnyArgsSpecified(
      args, ['max_dispatches_per_second', 'max_concurrent_dispatches'],
      clear_args=is_update)):
    return messages.RateLimits(
        maxDispatchesPerSecond=args.max_dispatches_per_second,
        maxConcurrentDispatches=args.max_concurrent_dispatches)


def _ParsePullTargetArgs(unused_args, queue_type, messages, is_update):
  """Parses the attributes of 'args' for Queue.pullTarget."""
  if queue_type == constants.PULL_QUEUE and not is_update:
    return messages.PullTarget()


def _ParseAppEngineHttpTargetArgs(args, queue_type, messages, is_update):
  """Parses the attributes of 'args' for Queue.appEngineHttpTarget."""
  if queue_type == constants.APP_ENGINE_QUEUE:
    routing_override = None
    if args.IsSpecified('routing_override'):
      routing_override = messages.AppEngineRouting(**args.routing_override)
    elif is_update and args.IsSpecified('clear_routing_override'):
      routing_override = messages.AppEngineRouting()
    return messages.AppEngineHttpTarget(
        appEngineRoutingOverride=routing_override)


def _ParseAppEngineHttpQueueArgs(args, queue_type, messages, is_update):
  """Parses the attributes of 'args' for Queue.appEngineHttpQueue."""
  if queue_type == constants.APP_ENGINE_QUEUE:
    routing_override = None
    if args.IsSpecified('routing_override'):
      routing_override = messages.AppEngineRouting(**args.routing_override)
    elif is_update and args.IsSpecified('clear_routing_override'):
      routing_override = messages.AppEngineRouting()
    return messages.AppEngineHttpQueue(
        appEngineRoutingOverride=routing_override)


def _ParsePullMessageArgs(args, task_type, messages):
  if task_type == constants.PULL_QUEUE:
    return messages.PullMessage(payload=_ParsePayloadArgs(args), tag=args.tag)


def _ParseAlphaAppEngineHttpRequestArgs(args, task_type, messages):
  """Parses the attributes of 'args' for Task.appEngineHttpRequest."""
  if task_type == constants.APP_ENGINE_QUEUE:
    routing = (
        messages.AppEngineRouting(**args.routing) if args.routing else None)
    http_method = (messages.AppEngineHttpRequest.HttpMethodValueValuesEnum(
        args.method.upper()) if args.IsSpecified('method') else None)
    return messages.AppEngineHttpRequest(
        appEngineRouting=routing, headers=_ParseHeaderArg(args, messages),
        httpMethod=http_method, payload=_ParsePayloadArgs(args),
        relativeUrl=args.url)


def _ParsePayloadArgs(args):
  if args.IsSpecified('payload_file'):
    payload = console_io.ReadFromFileOrStdin(args.payload_file, binary=False)
  elif args.IsSpecified('payload_content'):
    payload = args.payload_content
  else:
    return None
  return http_encoding.Encode(payload)


def _ParseAppEngineHttpRequestArgs(args, task_type, messages):
  """Parses the attributes of 'args' for Task.appEngineHttpRequest."""
  if task_type == constants.APP_ENGINE_QUEUE:
    routing = (
        messages.AppEngineRouting(**args.routing) if args.routing else None)
    http_method = (messages.AppEngineHttpRequest.HttpMethodValueValuesEnum(
        args.method.upper()) if args.IsSpecified('method') else None)
    return messages.AppEngineHttpRequest(
        appEngineRouting=routing, headers=_ParseHeaderArg(args, messages),
        httpMethod=http_method, body=_ParseBodyArgs(args),
        relativeUri=args.relative_uri)


def _ParseBodyArgs(args):
  if args.IsSpecified('body_file'):
    body = console_io.ReadFromFileOrStdin(args.body_file, binary=False)
  elif args.IsSpecified('body_content'):
    body = args.body_content
  else:
    return None
  return http_encoding.Encode(body)


def _ParseHeaderArg(args, messages):
  if args.header:
    headers_dict = {k: v for k, v in map(_SplitHeaderArgValue, args.header)}
    return encoding.DictToAdditionalPropertyMessage(
        headers_dict, messages.AppEngineHttpRequest.HeadersValue)


def _SplitHeaderArgValue(header_arg_value):
  key, value = header_arg_value.split(':', 1)
  return key, value.lstrip()


def FormatLeaseDuration(lease_duration):
  return '{}s'.format(lease_duration)


def ParseTasksLeaseFilterFlags(args):
  if args.oldest_tag:
    return 'tag_function=oldest_tag()'
  if args.IsSpecified('tag'):
    return 'tag="{}"'.format(args.tag)


def QueuesUriFunc(queue):
  return resources.REGISTRY.Parse(
      queue.name,
      params={'projectsId': _PROJECT},
      collection=constants.QUEUES_COLLECTION).SelfLink()


def TasksUriFunc(task):
  return resources.REGISTRY.Parse(
      task.name,
      params={'projectsId': _PROJECT},
      collection=constants.TASKS_COLLECTION).SelfLink()


def LocationsUriFunc(task):
  return resources.REGISTRY.Parse(
      task.name,
      params={'projectsId': _PROJECT},
      collection=constants.LOCATIONS_COLLECTION).SelfLink()
