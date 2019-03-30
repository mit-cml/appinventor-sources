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
"""`gcloud tasks queues pause` command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.tasks import GetApiAdapter
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.tasks import constants
from googlecloudsdk.command_lib.tasks import flags
from googlecloudsdk.command_lib.tasks import parsers
from googlecloudsdk.core import log


class Pause(base.Command):
  """Pause a queue.

  If a queue is paused then the system will stop executing the tasks in the
  queue until it is resumed. Tasks can still be added when the queue is paused.
  """

  @staticmethod
  def Args(parser):
    flags.AddQueueResourceArg(parser, 'to pause')
    flags.AddLocationFlag(parser)

  def Run(self, args):
    queues_client = GetApiAdapter(self.ReleaseTrack()).queues
    queue_ref = parsers.ParseQueue(args.queue, args.location)
    log.warning(constants.QUEUE_MANAGEMENT_WARNING)
    queues_client.Pause(queue_ref)
    log.status.Print('Paused queue [{}].'.format(queue_ref.Name()))
