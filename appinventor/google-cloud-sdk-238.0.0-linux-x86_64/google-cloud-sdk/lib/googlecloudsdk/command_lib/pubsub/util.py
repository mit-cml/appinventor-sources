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

"""A library that is used to support Cloud Pub/Sub commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.pubsub import subscriptions
from googlecloudsdk.api_lib.pubsub import topics
from googlecloudsdk.command_lib.projects import util as projects_util
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources
from googlecloudsdk.core.resource import resource_projector
from googlecloudsdk.core.util import times

import six

# Format for the seek time argument.
SEEK_TIME_FORMAT = '%Y-%m-%dT%H:%M:%S.%fZ'


# Collection for various subcommands.
TOPICS_COLLECTION = 'pubsub.projects.topics'
TOPICS_PUBLISH_COLLECTION = 'pubsub.topics.publish'
SNAPSHOTS_COLLECTION = 'pubsub.projects.snapshots'
SNAPSHOTS_LIST_COLLECTION = 'pubsub.snapshots.list'
SUBSCRIPTIONS_COLLECTION = 'pubsub.projects.subscriptions'
SUBSCRIPTIONS_ACK_COLLECTION = 'pubsub.subscriptions.ack'
SUBSCRIPTIONS_LIST_COLLECTION = 'pubsub.subscriptions.list'
SUBSCRIPTIONS_MOD_ACK_COLLECTION = 'pubsub.subscriptions.mod_ack'
SUBSCRIPTIONS_MOD_CONFIG_COLLECTION = 'pubsub.subscriptions.mod_config'
SUBSCRIPTIONS_PULL_COLLECTION = 'pubsub.subscriptions.pull'
SUBSCRIPTIONS_SEEK_COLLECTION = 'pubsub.subscriptions.seek'


class RequestsFailedError(exceptions.Error):
  """Indicates that some requests to the API have failed."""

  def __init__(self, requests, action):
    super(RequestsFailedError, self).__init__(
        'Failed to {action} the following: [{requests}].'.format(
            action=action, requests=','.join(requests)))


def ParseSnapshot(snapshot_name, project_id=''):
  project_id = _GetProject(project_id)
  return resources.REGISTRY.Parse(snapshot_name,
                                  params={'projectsId': project_id},
                                  collection=SNAPSHOTS_COLLECTION)


def ParseSubscription(subscription_name, project_id=''):
  project_id = _GetProject(project_id)
  return resources.REGISTRY.Parse(subscription_name,
                                  params={'projectsId': project_id},
                                  collection=SUBSCRIPTIONS_COLLECTION)


def ParseTopic(topic_name, project_id=''):
  project_id = _GetProject(project_id)
  return resources.REGISTRY.Parse(topic_name,
                                  params={'projectsId': project_id},
                                  collection=TOPICS_COLLECTION)


def ParseProject(project_id=None):
  project_id = _GetProject(project_id)
  return projects_util.ParseProject(project_id)


def _GetProject(project_id):
  return project_id or properties.VALUES.core.project.Get(required=True)


def SnapshotUriFunc(snapshot):
  if isinstance(snapshot, dict):
    name = snapshot['name']
  else:
    name = snapshot
  return ParseSnapshot(name).SelfLink()


def SubscriptionUriFunc(subscription):
  project = None
  if isinstance(subscription, dict):
    name = subscription['subscriptionId']
    project = subscription['projectId']
  elif isinstance(subscription, str):
    name = subscription
  else:
    name = subscription.name
  return ParseSubscription(name, project).SelfLink()


def TopicUriFunc(topic):
  if isinstance(topic, dict):
    name = topic['topicId']
  else:
    name = topic.name
  return ParseTopic(name).SelfLink()


def ParsePushConfig(args, client=None):
  """Parses configs of push subscription from args."""
  push_endpoint = args.push_endpoint
  if push_endpoint is None:
    return None

  client = client or subscriptions.SubscriptionsClient()
  oidc_token = None
  service_account_email = getattr(args, 'SERVICE_ACCOUNT_EMAIL', None)

  # Only set oidc_token when service_account_email is set.
  if service_account_email is not None:
    audience = getattr(args, 'OPTIONAL_AUDIENCE_OVERRIDE', None)
    oidc_token = client.messages.OidcToken(
        serviceAccountEmail=service_account_email, audience=audience)

  return client.messages.PushConfig(
      pushEndpoint=push_endpoint, oidcToken=oidc_token)


def FormatSeekTime(time):
  return times.FormatDateTime(time, fmt=SEEK_TIME_FORMAT, tzinfo=times.UTC)


def FormatDuration(duration):
  """Formats a duration argument to be a string with units.

  Args:
    duration (int): The duration in seconds.
  Returns:
    str: The formatted duration.
  """
  return str(duration) + 's'


def ParseAttributes(attribute_dict, messages=None):
  """Parses attribute_dict into a list of AdditionalProperty messages.

  Args:
    attribute_dict (Optional[dict]): Dict containing key=value pairs
      to parse.
    messages (Optional[module]): Module containing pubsub proto messages.
  Returns:
    list: List of AdditionalProperty messages.
  """
  messages = messages or topics.GetMessagesModule()
  attributes = []
  if attribute_dict:
    for key, value in sorted(six.iteritems(attribute_dict)):
      attributes.append(
          messages.PubsubMessage.AttributesValue.AdditionalProperty(
              key=key,
              value=value))
  return attributes


# TODO(b/32276674): Remove the use of custom *DisplayDict's.
def TopicDisplayDict(topic):
  """Creates a serializable from a Cloud Pub/Sub Topic operation for display.

  Args:
    topic: (Cloud Pub/Sub Topic) Topic to be serialized.
  Returns:
    A serialized object representing a Cloud Pub/Sub Topic
    operation (create, delete).
  """
  topic_display_dict = resource_projector.MakeSerializable(topic)
  topic_display_dict['topicId'] = topic.name
  del topic_display_dict['name']

  return topic_display_dict


def SubscriptionDisplayDict(subscription):
  """Creates a serializable from a Cloud Pub/Sub Subscription op for display.

  Args:
    subscription: (Cloud Pub/Sub Subscription) Subscription to be serialized.
  Returns:
    A serialized object representing a Cloud Pub/Sub Subscription
    operation (create, delete, update).
  """
  push_endpoint = ''
  subscription_type = 'pull'
  if subscription.pushConfig:
    if subscription.pushConfig.pushEndpoint:
      push_endpoint = subscription.pushConfig.pushEndpoint
      subscription_type = 'push'

  return {
      'subscriptionId': subscription.name,
      'topic': subscription.topic,
      'type': subscription_type,
      'pushEndpoint': push_endpoint,
      'ackDeadlineSeconds': subscription.ackDeadlineSeconds,
      'retainAckedMessages': bool(subscription.retainAckedMessages),
      'messageRetentionDuration': subscription.messageRetentionDuration,
  }


def SnapshotDisplayDict(snapshot):
  """Creates a serializable from a Cloud Pub/Sub Snapshot operation for display.

  Args:
    snapshot: (Cloud Pub/Sub Snapshot) Snapshot to be serialized.

  Returns:
    A serialized object representing a Cloud Pub/Sub Snapshot operation (create,
    delete).
  """
  return {
      'snapshotId': snapshot.name,
      'topic': snapshot.topic,
      'expireTime': snapshot.expireTime,
  }


def ListSubscriptionDisplayDict(subscription):
  """Returns a subscription dict with additional fields."""
  result = resource_projector.MakeSerializable(subscription)
  result['type'] = 'PUSH' if subscription.pushConfig.pushEndpoint else 'PULL'
  subscription_ref = ParseSubscription(subscription.name)
  result['projectId'] = subscription_ref.projectsId
  result['subscriptionId'] = subscription_ref.subscriptionsId
  topic_info = ParseTopic(subscription.topic)
  result['topicId'] = topic_info.topicsId
  return result


def ListTopicDisplayDict(topic):
  topic_dict = resource_projector.MakeSerializable(topic)
  topic_ref = ParseTopic(topic.name)
  topic_dict['topic'] = topic.name
  topic_dict['topicId'] = topic_ref.topicsId
  del topic_dict['name']
  return topic_dict


def ListTopicSubscriptionDisplayDict(topic_subscription):
  """Returns a topic_subscription dict with additional fields."""
  result = resource_projector.MakeSerializable(
      {'subscription': topic_subscription})

  subscription_ref = ParseSubscription(topic_subscription)
  result['projectId'] = subscription_ref.projectsId
  result['subscriptionId'] = subscription_ref.subscriptionsId
  return result


def ListSnapshotDisplayDict(snapshot):
  """Returns a snapshot dict with additional fields."""
  result = resource_projector.MakeSerializable(snapshot)
  snapshot_ref = ParseSnapshot(snapshot.name)
  result['projectId'] = snapshot_ref.projectsId
  result['snapshotId'] = snapshot_ref.snapshotsId
  topic_ref = ParseTopic(snapshot.topic)
  result['topicId'] = topic_ref.topicsId
  result['expireTime'] = snapshot.expireTime
  return result
