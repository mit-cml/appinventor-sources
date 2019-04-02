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
"""Utilities for flags for `gcloud tasks` commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import sys

from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.tasks import constants


def AddQueueResourceArg(parser, verb):
  base.Argument('queue', help='The queue {}.\n\n'.format(verb)).AddToParser(
      parser)


def AddQueueResourceFlag(parser, required=True, plural_tasks=False):
  description = ('The queue the tasks belong to.' if plural_tasks else
                 'The queue the task belongs to.')
  argument = base.Argument('--queue', help=description, required=required)
  argument.AddToParser(parser)


def AddTaskResourceArgs(parser, verb):
  base.Argument('task', help='The task {}.\n\n'.format(verb)).AddToParser(
      parser)
  AddQueueResourceFlag(parser, required=False)


def AddLocationFlag(parser):
  argument = base.Argument(
      '--location', hidden=True,
      help='The location of the app associated with the active project.')
  argument.AddToParser(parser)


def AddCreatePullQueueFlags(parser):
  for flag in _PullQueueFlags():
    flag.AddToParser(parser)


def AddCreateAppEngineQueueFlags(parser, is_alpha=False):
  flags = _AlphaAppEngineQueueFlags() if is_alpha else _AppEngineQueueFlags()
  for flag in flags:
    flag.AddToParser(parser)


def AddUpdatePullQueueFlags(parser):
  for flag in _PullQueueFlags():
    _AddFlagAndItsClearEquivalent(flag, parser)


def AddUpdateAppEngineQueueFlags(parser, is_alpha=False):
  flags = _AlphaAppEngineQueueFlags() if is_alpha else _AppEngineQueueFlags()
  for flag in flags:
    _AddFlagAndItsClearEquivalent(flag, parser)


def _AddFlagAndItsClearEquivalent(flag, parser):
  update_group = base.ArgumentGroup(mutex=True)
  update_group.AddArgument(flag)
  update_group.AddArgument(_EquivalentClearFlag(flag))
  update_group.AddToParser(parser)


def _EquivalentClearFlag(flag):
  name = flag.name.replace('--', '--clear-')
  clear_flag = base.Argument(
      name, action='store_true', help="""\
      Clear the field corresponding to `{}`.""".format(flag.name))
  return clear_flag


def AddPolicyFileFlag(parser):
  base.Argument('policy_file', help="""\
      JSON or YAML file containing the IAM policy.""").AddToParser(parser)


def AddTaskLeaseScheduleTimeFlag(parser, verb):
  base.Argument(
      '--schedule-time', required=True,
      help="""\
      The task's current schedule time. This restriction is to check that the
      caller is {} the correct task.
      """.format(verb)).AddToParser(parser)


def AddTaskLeaseDurationFlag(parser, helptext=None):
  if helptext is None:
    helptext = ('The number of seconds for the desired new lease duration, '
                'starting from now. The maximum lease duration is 1 week.')
  base.Argument('--lease-duration', required=True, type=int,
                help=helptext).AddToParser(parser)


def AddMaxTasksToLeaseFlag(parser):
  # Default help for base.LIMIT_FLAG is inaccurate and confusing in this case
  base.Argument(
      '--limit', type=int, default=1000, category=base.LIST_COMMAND_FLAGS,
      help="""\
      The maximum number of tasks to lease. The maximum that can be requested is
      1000.
      """).AddToParser(parser)


def AddFilterLeasedTasksFlag(parser):
  tag_filter_group = parser.add_mutually_exclusive_group()
  tag_filter_group.add_argument('--tag', help="""\
      A tag to filter each task to be leased. If a task has the tag and the
      task is available to be leased, then it is listed and leased.
      """)
  tag_filter_group.add_argument('--oldest-tag', action='store_true', help="""\
      Only lease tasks which have the same tag as the task with the oldest
      schedule time.
      """)


def AddCreatePullTaskFlags(parser):
  """Add flags needed for creating a pull task to the parser."""
  AddQueueResourceFlag(parser, required=True)
  _GetTaskIdFlag().AddToParser(parser)
  for flag in _PullTaskFlags():
    flag.AddToParser(parser)
  _AddPayloadFlags(parser, True)


def AddCreateAppEngineTaskFlags(parser, is_alpha=False):
  """Add flags needed for creating a App Engine task to the parser."""
  AddQueueResourceFlag(parser, required=True)
  _GetTaskIdFlag().AddToParser(parser)
  flags = _AlphaAppEngineTaskFlags() if is_alpha else _AppEngineTaskFlags()
  for flag in flags:
    flag.AddToParser(parser)
  _AddPayloadFlags(parser, is_alpha)


def _PullQueueFlags():
  return [
      base.Argument(
          '--max-attempts',
          type=arg_parsers.BoundedInt(1, sys.maxsize, unlimited=True),
          help="""\
          The maximum number of attempts per task in the queue.
          """),
      base.Argument(
          '--max-retry-duration',
          help="""\
          The time limit for retrying a failed task, measured from when the task
          was first run. Once the `--max-retry-duration` time has passed and the
          task has been attempted --max-attempts times, no further attempts will
          be made and the task will be deleted.

          Must be a string that ends in 's', such as "5s".
          """),
  ]


def _BaseAppEngineQueueFlags():
  return _PullQueueFlags() + [
      base.Argument(
          '--max-doublings',
          type=int,
          help="""\
          The time between retries will double maxDoublings times.

          A tasks retry interval starts at minBackoff, then doubles maxDoublings
          times, then increases linearly, and finally retries retries at
          intervals of maxBackoff up to maxAttempts times.

          For example, if minBackoff is 10s, maxBackoff is 300s, and
          maxDoublings is 3, then the a task will first be retried in 10s. The
          retry interval will double three times, and then increase linearly by
          2^3 * 10s. Finally, the task will retry at intervals of maxBackoff
          until the task has been attempted maxAttempts times. Thus, the
          requests will retry at 10s, 20s, 40s, 80s, 160s, 240s, 300s, 300s.
          """),
      base.Argument(
          '--min-backoff',
          help="""\
          The minimum amount of time to wait before retrying a task after it
          fails. Must be a string that ends in 's', such as "5s".
          """),
      base.Argument(
          '--max-backoff',
          help="""\
          The maximum amount of time to wait before retrying a task after it
          fails. Must be a string that ends in 's', such as "5s".
          """),
      base.Argument(
          '--routing-override',
          type=arg_parsers.ArgDict(key_type=_GetAppEngineRoutingKeysValidator(),
                                   min_length=1, max_length=3,
                                   operators={':': None}),
          metavar='KEY:VALUE',
          help="""\
          If provided, the specified route is used for all tasks in the queue,
          no matter what is set is at the task-level.

          KEY must be at least one of: [{}]. Any missing keys will use the
          default.
          """.format(', '.join(constants.APP_ENGINE_ROUTING_KEYS))),
  ]


def _AlphaAppEngineQueueFlags():
  return _BaseAppEngineQueueFlags() + [
      base.Argument(
          '--max-tasks-dispatched-per-second',
          type=float,
          help="""\
          The maximum rate at which tasks are dispatched from this queue.
          """),
      base.Argument(
          '--max-concurrent-tasks',
          type=int,
          help="""\
          The maximum number of concurrent tasks that Cloud Tasks allows to
          be dispatched for this queue. After this threshold has been reached,
          Cloud Tasks stops dispatching tasks until the number of outstanding
          requests decreases.
          """),
  ]


def _AppEngineQueueFlags():
  return _BaseAppEngineQueueFlags() + [
      base.Argument(
          '--max-dispatches-per-second',
          type=float,
          help="""\
          The maximum rate at which tasks are dispatched from this queue.
          """),
      base.Argument(
          '--max-concurrent-dispatches',
          type=int,
          help="""\
          The maximum number of concurrent tasks that Cloud Tasks allows to
          be dispatched for this queue. After this threshold has been reached,
          Cloud Tasks stops dispatching tasks until the number of outstanding
          requests decreases.
          """),
  ]


def _PullTaskFlags():
  return _CommonTaskFlags() + [
      base.Argument('--tag', help="""\
          An optional label used to group similar tasks.
          """),
  ]


def _BaseAppEngineTaskFlags():
  return _CommonTaskFlags() + [
      base.Argument('--method', help="""\
          The HTTP method to use for the request. If not specified, "POST" will
          be used.
          """),
      base.Argument('--header', metavar='HEADER_FIELD: HEADER_VALUE',
                    action='append', type=_GetHeaderArgValidator(),
                    help="""\
          An HTTP request header. Header values can contain commas. This flag
          can be repeated. Repeated header fields will have their values
          overridden.
          """),
      base.Argument(
          '--routing',
          type=arg_parsers.ArgDict(key_type=_GetAppEngineRoutingKeysValidator(),
                                   min_length=1, max_length=3,
                                   operators={':': None}),
          metavar='KEY:VALUE',
          help="""\
          The route to be used for this task. KEY must be at least one of:
          [{}]. Any missing keys will use the default.

          Routing can be overridden by the queue-level `--routing-override`
          flag.
          """.format(', '.join(constants.APP_ENGINE_ROUTING_KEYS))),
  ]


def _AlphaAppEngineTaskFlags():
  return _BaseAppEngineTaskFlags() + [
      base.Argument('--url', help="""\
          The relative URL of the request. Must begin with "/" and must be a
          valid HTTP relative URL. It can contain a path and query string
          arguments. If not specified, then the root path "/" will be used.
          """),
  ]


def _AppEngineTaskFlags():
  return _BaseAppEngineTaskFlags() + [
      base.Argument('--relative-uri', help="""\
          The relative URI of the request. Must begin with "/" and must be a
          valid HTTP relative URI. It can contain a path and query string
          arguments. If not specified, then the root path "/" will be used.
          """),
  ]


def _GetTaskIdFlag():
  return base.Argument(
      'task',
      metavar='TASK_ID',
      nargs='?',
      help="""\
      The task to create.

      If not specified then the system will generate a random unique task
      ID. Explicitly specifying a task ID enables task de-duplication. If a
      task's ID is identical to that of an existing task or a task that was
      deleted or completed recently then the call will fail.

      Because there is an extra lookup cost to identify duplicate task
      names, tasks created with IDs have significantly increased latency.
      Using hashed strings for the task ID or for the prefix of the task ID
      is recommended.
      """)


def _CommonTaskFlags():
  return [
      base.Argument('--schedule-time', help="""\
          The time when the task is scheduled to be first attempted. Defaults to
          "now" if not specified.
          """)
  ]


def _AddPayloadFlags(parser, is_alpha=False):
  """Adds either payload or body flags."""
  payload_group = parser.add_mutually_exclusive_group()
  if is_alpha:
    payload_group.add_argument('--payload-content', help="""\
            Data payload used by the task worker to process the task.
            """)
    payload_group.add_argument('--payload-file', help="""\
            File containing data payload used by the task worker to process the
            task.
            """)
  else:
    payload_group.add_argument('--body-content', help="""\
            HTTP Body data sent to the task worker processing the task.
            """)
    payload_group.add_argument('--body-file', help="""\
            File containing HTTP body data sent to the task worker processing
            the task.
            """)


def _GetAppEngineRoutingKeysValidator():
  return arg_parsers.CustomFunctionValidator(
      lambda k: k in constants.APP_ENGINE_ROUTING_KEYS,
      'Only the following keys are valid for routing: [{}].'.format(
          ', '.join(constants.APP_ENGINE_ROUTING_KEYS)))


def _GetHeaderArgValidator():
  return arg_parsers.RegexpValidator(
      r'^(\S+):(.+)$', 'Must be of the form: "HEADER_FIELD: HEADER_VALUE".')
