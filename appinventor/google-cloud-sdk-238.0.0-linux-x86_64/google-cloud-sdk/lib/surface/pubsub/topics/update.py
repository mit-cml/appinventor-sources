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

"""Cloud Pub/Sub topics update command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.pubsub import topics
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.pubsub import resource_args
from googlecloudsdk.command_lib.util.args import labels_util
from googlecloudsdk.core import log


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class Update(base.UpdateCommand):
  """This feature is part of an invite-only release of the Cloud Pub/Sub API.

  Updates an existing Cloud Pub/Sub topic.
  This feature is part of an invitation-only release of the underlying
  Cloud Pub/Sub API. The command will generate errors unless you have access to
  this API. This restriction should be relaxed in the near future. Please
  contact cloud-pubsub@google.com with any questions in the meantime.
  """

  @staticmethod
  def Args(parser):
    """Registers flags for this command."""
    resource_args.AddTopicResourceArg(parser, 'to update.')
    labels_util.AddUpdateLabelsFlags(parser)
    parser.add_argument(
        '--recompute-message-storage-policy',
        action='store_true',
        help='If given, Cloud Pub/Sub will recompute the regions where messages'
             " can be stored at rest based on your organization's policies.")

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Returns:
      A serialized object (dict) describing the results of the operation.

    Raises:
      An HttpException if there was a problem calling the
      API topics.Patch command.
    """
    client = topics.TopicsClient()
    topic_ref = args.CONCEPTS.topic.Parse()

    labels_update = labels_util.ProcessUpdateArgsLazy(
        args, client.messages.Topic.LabelsValue,
        orig_labels_thunk=lambda: client.Get(topic_ref).labels)

    result = None
    try:
      result = client.Patch(
          topic_ref,
          labels_update.GetOrNone(),
          args.recompute_message_storage_policy)
    except topics.NoFieldsSpecifiedError:
      if not any(args.IsSpecified(arg) for arg in (
          'clear_labels', 'update_labels', 'remove_labels',
          'recompute_message_storage_policy')):
        raise
      log.status.Print('No update to perform.')
    else:
      log.UpdatedResource(topic_ref.RelativeName(), kind='topic')
    return result
