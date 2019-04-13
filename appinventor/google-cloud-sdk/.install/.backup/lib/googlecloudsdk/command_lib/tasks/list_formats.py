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
"""List command formats and transforms for `gcloud tasks`."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.command_lib.tasks import constants
from googlecloudsdk.command_lib.tasks import parsers


# pylint: disable=line-too-long
_ALPHA_QUEUE_LIST_FORMAT = '''table(
    name.basename():label="QUEUE_NAME",
    queuetype():label=TYPE,
    state,
    rateLimits.maxConcurrentTasks.yesno(no="unlimited").format("{0}").sub("-1", "unlimited"):label="MAX_NUM_OF_TASKS",
    rateLimits.maxTasksDispatchedPerSecond.yesno(no="unlimited"):label="MAX_RATE (/sec)",
    retryConfig.maxAttempts.yesno(no="unlimited"):label="MAX_ATTEMPTS")'''


_QUEUE_LIST_FORMAT = '''table(
    name.basename():label="QUEUE_NAME",
    queuetype():label=TYPE,
    state,
    rateLimits.maxConcurrentDispatches.yesno(no="unlimited").format("{0}").sub("-1", "unlimited"):label="MAX_NUM_OF_TASKS",
    rateLimits.maxDispatchesPerSecond.yesno(no="unlimited"):label="MAX_RATE (/sec)",
    retryConfig.maxAttempts.yesno(no="unlimited").format("{0}").sub("-1", "unlimited"):label="MAX_ATTEMPTS")'''


_ALPHA_TASK_LIST_FORMAT = '''table(
    name.basename():label="TASK_NAME",
    tasktype():label=TYPE,
    createTime,
    scheduleTime,
    status.attemptDispatchCount.yesno(no="0"):label="DISPATCH_ATTEMPTS",
    status.attemptResponseCount.yesno(no="0"):label="RESPONSE_ATTEMPTS",
    status.lastAttemptStatus.responseStatus.message.yesno(no="Unknown")
        :label="LAST_ATTEMPT_STATUS")'''


_TASK_LIST_FORMAT = '''table(
    name.basename():label="TASK_NAME",
    tasktype():label=TYPE,
    createTime,
    scheduleTime,
    dispatchCount.yesno(no="0"):label="DISPATCH_ATTEMPTS",
    responseCount.yesno(no="0"):label="RESPONSE_ATTEMPTS",
    lastAttempt.responseStatus.message.yesno(no="Unknown")
        :label="LAST_ATTEMPT_STATUS")'''


_LOCATION_LIST_FORMAT = '''table(
     locationId:label="NAME",
     name:label="FULL_NAME")'''
# pylint: enable=line-too-long


def AddListQueuesFormats(parser, is_alpha=False):
  parser.display_info.AddTransforms({'queuetype': _TranformQueueType})
  parser.display_info.AddFormat(
      _ALPHA_QUEUE_LIST_FORMAT if is_alpha else _QUEUE_LIST_FORMAT)
  parser.display_info.AddUriFunc(parsers.QueuesUriFunc)


def AddListTasksFormats(parser, is_alpha=False):
  parser.display_info.AddTransforms({'tasktype': _TranformTaskType})
  parser.display_info.AddFormat(
      _ALPHA_TASK_LIST_FORMAT if is_alpha else _TASK_LIST_FORMAT)
  parser.display_info.AddUriFunc(parsers.TasksUriFunc)


def AddListLocationsFormats(parser):
  parser.display_info.AddFormat(_LOCATION_LIST_FORMAT)
  parser.display_info.AddUriFunc(parsers.LocationsUriFunc)


def _IsPullQueue(r):
  return 'pullTarget' in r


def _IsAppEngineQueue(r):
  # appEngineHttpTarget is used in the v2beta2 version of the API but will be
  # deprecated soon.
  return 'appEngineHttpTarget' in r or 'appEngineHttpQueue' in r


def _IsPullTask(r):
  return 'pullMessage' in r


def _IsAppEngineTask(r):
  return 'appEngineHttpRequest' in r


def _TranformQueueType(r):
  if _IsPullQueue(r):
    return constants.PULL_QUEUE
  if _IsAppEngineQueue(r):
    return constants.APP_ENGINE_QUEUE


def _TranformTaskType(r):
  if _IsPullTask(r):
    return constants.PULL_QUEUE
  if _IsAppEngineTask(r):
    return constants.APP_ENGINE_QUEUE
