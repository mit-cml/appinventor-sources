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
"""`gcloud tasks queues create-app-engine-queue` command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.tasks import GetApiAdapter
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.tasks import constants
from googlecloudsdk.command_lib.tasks import flags
from googlecloudsdk.command_lib.tasks import parsers
from googlecloudsdk.core import log


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class CreateAppEngine(base.CreateCommand):
  """Create an App Engine queue.

  An App Engine queue is a push queue sent to an App Engine endpoint. The flags
  available to this command represent the fields of an App Engine queue that are
  mutable.

  If you have early access to Cloud Tasks, refer to the following guide for
  more information about the different queue target types:
  https://cloud.google.com/cloud-tasks/docs/queue-types.
  For access, sign up here: https://goo.gl/Ya0AZd
  """

  @staticmethod
  def Args(parser):
    flags.AddQueueResourceArg(parser, 'to create')
    flags.AddLocationFlag(parser)
    flags.AddCreateAppEngineQueueFlags(parser)

  def Run(self, args):
    api = GetApiAdapter(self.ReleaseTrack())
    queues_client = api.queues
    queue_ref = parsers.ParseQueue(args.queue, args.location)
    location_ref = parsers.ExtractLocationRefFromQueueRef(queue_ref)
    queue_config = parsers.ParseCreateOrUpdateQueueArgs(
        args, constants.APP_ENGINE_QUEUE, api.messages)
    log.warning(constants.QUEUE_MANAGEMENT_WARNING)
    create_response = queues_client.Create(
        location_ref,
        queue_ref,
        retry_config=queue_config.retryConfig,
        rate_limits=queue_config.rateLimits,
        app_engine_http_queue=queue_config.appEngineHttpQueue)
    log.CreatedResource(queue_ref.Name(), 'queue')
    return create_response


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class AlphaCreateAppEngine(base.CreateCommand):
  """Create an App Engine queue.

  An App Engine queue is a push queue sent to an App Engine endpoint. The flags
  available to this command represent the fields of an App Engine queue that are
  mutable.

  If you have early access to Cloud Tasks, refer to the following guide for
  more information about the different queue target types:
  https://cloud.google.com/cloud-tasks/docs/queue-types.
  For access, sign up here: https://goo.gl/Ya0AZd
  """

  @staticmethod
  def Args(parser):
    flags.AddQueueResourceArg(parser, 'to create')
    flags.AddLocationFlag(parser)
    flags.AddCreateAppEngineQueueFlags(parser, is_alpha=True)

  def Run(self, args):
    api = GetApiAdapter(self.ReleaseTrack())
    queues_client = api.queues
    queue_ref = parsers.ParseQueue(args.queue, args.location)
    location_ref = parsers.ExtractLocationRefFromQueueRef(queue_ref)
    queue_config = parsers.ParseCreateOrUpdateQueueArgs(
        args, constants.APP_ENGINE_QUEUE, api.messages, is_alpha=True)
    log.warning(constants.QUEUE_MANAGEMENT_WARNING)
    create_response = queues_client.Create(
        location_ref,
        queue_ref,
        retry_config=queue_config.retryConfig,
        rate_limits=queue_config.rateLimits,
        app_engine_http_target=queue_config.appEngineHttpTarget)
    log.CreatedResource(queue_ref.Name(), 'queue')
    return create_response
