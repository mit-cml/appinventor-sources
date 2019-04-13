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

"""A library containing flags used by Cloud Pub/Sub commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.pubsub import subscriptions
from googlecloudsdk.calliope import actions
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.pubsub import util
from googlecloudsdk.core import log


# Maximum number of attributes you can specify for a message.
MAX_ATTRIBUTES = 100


# Format string for deprecation message for renaming positional to flag.
DEPRECATION_FORMAT_STR = (
    'Positional argument `{0}` is deprecated. Please use `{1}` instead.')


def AddAckIdFlag(parser, action, add_deprecated=False):
  help_text = 'One or more ACK_ID to {}'.format(action)
  if add_deprecated:
    parser.add_argument(
        'ack_id', nargs='*', help=help_text,
        action=actions.DeprecationAction(
            'ACK_ID',
            show_message=lambda _: False,  # See ParseAckIdsArgs for reason.
            warn=DEPRECATION_FORMAT_STR.format('ACK_ID', '--ack-ids')))
  parser.add_argument(
      '--ack-ids',
      metavar='ACK_ID',
      required=not add_deprecated,
      type=arg_parsers.ArgList(),
      help=help_text)


def ParseAckIdsArgs(args):
  """Gets the list of ack_ids from args.

  This is only necessary because we are deprecating the positional `ack_id`.
  Putting the positional and a flag in an argument group, will group flag
  under positional args. This would be confusing.

  DeprecationAction can't be used here because in order to make the positional
  argument optional, we have to use `nargs='*'`. Since this means zero or more,
  the DeprecationAction warn message is always triggered.

  This function does not exist in util.py in order to group the explanation for
  why this exists with the deprecated flags.

  Once the positional is removed, this function can be removed.

  Args:
    args (argparse.Namespace): Parsed arguments

  Returns:
    list[str]: List of ack ids.
  """
  if args.ack_id and args.ack_ids:
    raise exceptions.ConflictingArgumentsException('ACK_ID', '--ack-ids')
  elif not (args.ack_id or args.ack_ids):
    raise exceptions.MinimumArgumentException(['ACK_ID', '--ack-ids'])
  else:
    if args.ack_id:
      log.warning(DEPRECATION_FORMAT_STR.format('ACK_ID', '--ack-ids'))
    ack_ids = args.ack_id or args.ack_ids
    if not isinstance(ack_ids, list):
      ack_ids = [ack_ids]
    return ack_ids


def AddIamPolicyFileFlag(parser):
  parser.add_argument('policy_file',
                      help='JSON or YAML file with the IAM policy')


def AddSeekFlags(parser):
  """Adds flags for the seek command to the parser."""
  seek_to_group = parser.add_mutually_exclusive_group(required=True)
  seek_to_group.add_argument(
      '--time', type=arg_parsers.Datetime.Parse,
      help="""\
          The time to seek to. Messages in the subscription that
          were published before this time are marked as acknowledged, and
          messages retained in the subscription that were published after
          this time are marked as unacknowledged.
          See $ gcloud topic datetimes for information on time formats.""")
  seek_to_group.add_argument(
      '--snapshot',
      help='The name of the snapshot. The snapshot\'s topic must be the same '
           'as that of the subscription.')
  parser.add_argument(
      '--snapshot-project',
      help="""\
          The name of the project the snapshot belongs to (if seeking to
          a snapshot). If not set, it defaults to the currently selected
          cloud project.""")


def AddPullFlags(parser, add_deprecated=False, add_wait=False):
  """Adds the main set of message pulling flags to a parser."""
  if add_deprecated:
    parser.add_argument(
        '--max-messages', type=int, default=1,
        help='The maximum number of messages that Cloud Pub/Sub can return '
             'in this response.',
        action=actions.DeprecationAction(
            '--max-messages',
            warn='`{flag_name}` is deprecated. Please use --limit instead.'))
  parser.add_argument(
      '--auto-ack', action='store_true', default=False,
      help='Automatically ACK every message pulled from this subscription.')
  if add_wait:
    parser.add_argument(
        '--wait', action='store_true', default=False,
        help='Wait (for a bounded amount of time) for new messages from the '
             'subscription, if there are none.')


def AddPushEndpointFlag(parser, required=False):
  parser.add_argument(
      '--push-endpoint', required=required,
      help='A URL to use as the endpoint for this subscription. This will '
           'also automatically set the subscription type to PUSH.')


def AddAckDeadlineFlag(parser, required=False):
  parser.add_argument(
      '--ack-deadline', type=int, required=required,
      help='The number of seconds the system will wait for a subscriber to '
           'acknowledge receiving a message before re-attempting delivery.')


def AddMessageRetentionFlags(parser, is_update):
  """Adds flags subscription's messsage retention properties to the parser."""
  if is_update:
    retention_parser = ParseRetentionDurationWithDefault
    retention_default_help = 'Specify "default" to use the default value.'
  else:
    retention_parser = arg_parsers.Duration()
    retention_default_help = ('The default value is 7 days, the minimum is '
                              '10 minutes, and the maximum is 7 days.')

  retention_parser = retention_parser or arg_parsers.Duration()
  parser.add_argument(
      '--retain-acked-messages',
      action='store_true',
      default=None,
      help="""\
          Whether or not to retain acknowledged messages.  If true,
          messages are not expunged from the subscription's backlog
          until they fall out of the --message-retention-duration
          window.""")
  parser.add_argument(
      '--message-retention-duration',
      type=retention_parser,
      help="""\
          How long to retain unacknowledged messages in the
          subscription's backlog, from the moment a message is
          published.  If --retain-acked-messages is true, this also
          configures the retention of acknowledged messages.  {}
          Valid values are strings of the form INTEGER[UNIT],
          where UNIT is one of "s", "m", "h", and "d" for seconds,
          minutes, hours, and days, respectively.  If the unit
          is omitted, seconds is assumed.""".format(retention_default_help))


def AddSubscriptionTopicResourceFlags(parser):
  """Adds --topic and --topic-project flags to a parser."""
  parser.add_argument(
      '--topic', required=True,
      help='The name of the topic from which this subscription is receiving '
           'messages. Each subscription is attached to a single topic.')
  parser.add_argument(
      '--topic-project',
      help='The name of the project the provided topic belongs to. '
           'If not set, it defaults to the currently selected cloud project.')


def ParseRetentionDurationWithDefault(value):
  if value == subscriptions.DEFAULT_MESSAGE_RETENTION_VALUE:
    return value
  return util.FormatDuration(arg_parsers.Duration()(value))


def ParseExpirationPeriodWithNeverSentinel(value):
  if value == subscriptions.NEVER_EXPIRATION_PERIOD_VALUE:
    return value
  return util.FormatDuration(arg_parsers.Duration()(value))


def AddSubscriptionSettingsFlags(parser, track, is_update=False):
  AddAckDeadlineFlag(parser)
  AddPushEndpointFlag(parser)
  AddMessageRetentionFlags(parser, is_update)
  if track in [base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA]:
    parser.add_argument(
        '--expiration-period',
        type=ParseExpirationPeriodWithNeverSentinel,
        help="""The subscription will expire if it is inactive for the given
            period. Valid values are strings of the form INTEGER[UNIT], where
            UNIT is one of "s", "m", "h", and "d" for seconds, minutes, hours,
            and days, respectively. If the unit is omitted, seconds is
            assumed. This flag additionally accepts the special value "never" to
            indicate that the subscription will never expire.""")


def AddPublishMessageFlags(parser, add_deprecated=False):
  """Adds the flags for building a PubSub message to the parser.

  Args:
    parser: The argparse parser.
    add_deprecated: Whether or not to add the deprecated flags.
  """
  message_help_text = """\
      The body of the message to publish to the given topic name.
      Information on message formatting and size limits can be found at:
      https://cloud.google.com/pubsub/docs/publisher#publish"""
  if add_deprecated:
    parser.add_argument(
        'message_body', nargs='?', default=None,
        help=message_help_text,
        action=actions.DeprecationAction(
            'MESSAGE_BODY',
            show_message=lambda _: False,
            warn=DEPRECATION_FORMAT_STR.format('MESSAGE_BODY', '--message')))
  parser.add_argument(
      '--message', help=message_help_text)

  parser.add_argument(
      '--attribute', type=arg_parsers.ArgDict(max_length=MAX_ATTRIBUTES),
      help='Comma-separated list of attributes. Each ATTRIBUTE has the form '
           'name=value". You can specify up to {0} attributes.'.format(
               MAX_ATTRIBUTES))


def ParseMessageBody(args):
  """Gets the message body from args.

  This is only necessary because we are deprecating the positional `ack_id`.
  Putting the positional and a flag in an argument group, will group flag
  under positional args. This would be confusing.

  DeprecationAction can't be used here because the positional argument is
  optional (nargs='?') Since this means zero or more, the DeprecationAction
  warn message is always triggered.

  This function does not exist in util.py in order to group the explanation for
  why this exists with the deprecated flags.

  Once the positional is removed, this function can be removed.

  Args:
    args (argparse.Namespace): Parsed arguments

  Returns:
    Optional[str]: message body.
  """
  if args.message_body and args.message:
    raise exceptions.ConflictingArgumentsException('MESSAGE_BODY', '--message')

  if args.message_body is not None:
    log.warning(DEPRECATION_FORMAT_STR.format('MESSAGE_BODY', '--message'))
  return args.message_body or args.message
