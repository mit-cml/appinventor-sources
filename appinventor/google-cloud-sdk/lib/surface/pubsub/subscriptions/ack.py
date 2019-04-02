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

"""Cloud Pub/Sub subscriptions ack command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.pubsub import subscriptions
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.pubsub import flags
from googlecloudsdk.command_lib.pubsub import resource_args
from googlecloudsdk.core import log
from googlecloudsdk.core import properties


def _Run(args, ack_ids, legacy_output=False):
  client = subscriptions.SubscriptionsClient()

  subscription_ref = args.CONCEPTS.subscription.Parse()
  result = client.Ack(ack_ids, subscription_ref)

  log.status.Print('Acked the messages with the following ackIds: [{}]'
                   .format(','.join(ack_ids)))
  if legacy_output:
    return {'subscriptionId': subscription_ref.RelativeName(),
            'ackIds': ack_ids}
  else:
    return result


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Ack(base.Command):
  """Acknowledges one or more messages on the specified subscription."""

  detailed_help = {
      'DESCRIPTION': """\
          Acknowledges one or more messages as having been successfully received.
          If a delivered message is not acknowledged within the Subscription's
          ack deadline, Cloud Pub/Sub will attempt to deliver it again.

          To automatically acknowledge messages when pulling from a Subscription,
          you can use the `--auto-ack` flag on `gcloud pubsub subscriptions pull`.
      """
  }

  @staticmethod
  def Args(parser):
    resource_args.AddSubscriptionResourceArg(parser, 'to ACK messages on.')
    flags.AddAckIdFlag(parser, 'acknowledge.')

  def Run(self, args):
    return _Run(args, args.ack_ids)


@base.ReleaseTracks(base.ReleaseTrack.BETA, base.ReleaseTrack.ALPHA)
class AckBeta(Ack):
  """Acknowledges one or more messages on the specified subscription."""

  @staticmethod
  def Args(parser):
    resource_args.AddSubscriptionResourceArg(parser, 'to ACK messages on.')
    flags.AddAckIdFlag(parser, 'acknowledge.', add_deprecated=True)

  def Run(self, args):
    ack_ids = flags.ParseAckIdsArgs(args)
    legacy_output = properties.VALUES.pubsub.legacy_output.GetBool()
    return _Run(args, ack_ids, legacy_output=legacy_output)
