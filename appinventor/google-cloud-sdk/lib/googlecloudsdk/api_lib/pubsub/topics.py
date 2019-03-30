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

"""Utilities for Cloud Pub/Sub Topics API."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import list_pager
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.command_lib.iam import iam_util
from googlecloudsdk.core import exceptions


class PublishOperationException(exceptions.Error):
  """Error when something went wrong with publish."""


class EmptyMessageException(exceptions.Error):
  """Error when no message was specified for a Publish operation."""


class NoFieldsSpecifiedError(exceptions.Error):
  """Error when no fields were specified for a Patch operation."""


class _TopicUpdateSetting(object):
  """Data container class for updating a topic."""

  def __init__(self, field_name, value):
    self.field_name = field_name
    self.value = value


def GetClientInstance(no_http=False):
  return apis.GetClientInstance('pubsub', 'v1', no_http=no_http)


def GetMessagesModule(client=None):
  client = client or GetClientInstance()
  return client.MESSAGES_MODULE


class TopicsClient(object):
  """Client for topics service in the Cloud Pub/Sub API."""

  def __init__(self, client=None, messages=None):
    self.client = client or GetClientInstance()
    self.messages = messages or GetMessagesModule(client)
    self._service = self.client.projects_topics

  def Create(self, topic_ref, labels=None, kms_key=None):
    """Creates a Topic.

    Args:
      topic_ref (Resource): Resource reference to the Topic to create.
      labels (LabelsValue): Labels for the topic to create.
      kms_key (str): Full resource name of kms_key to set on Topic or None.
    Returns:
      Topic: The created topic.
    """
    topic = self.messages.Topic(name=topic_ref.RelativeName(), labels=labels)
    if kms_key:
      topic.kmsKeyName = kms_key
    return self._service.Create(topic)

  def Get(self, topic_ref):
    """Gets a Topic.

    Args:
      topic_ref (Resource): Resource reference to the Topic to get.
    Returns:
      Topic: The topic.
    """
    get_req = self.messages.PubsubProjectsTopicsGetRequest(
        topic=topic_ref.RelativeName())
    return self._service.Get(get_req)

  def Delete(self, topic_ref):
    """Deletes a Topic.

    Args:
      topic_ref (Resource): Resource reference to the Topic to delete.
    Returns:
      Empty: An empty response message.
    """
    delete_req = self.messages.PubsubProjectsTopicsDeleteRequest(
        topic=topic_ref.RelativeName())
    return self._service.Delete(delete_req)

  def List(self, project_ref, page_size=100):
    """Lists Topics for a given project.

    Args:
      project_ref (Resource): Resource reference to Project to list
        Topics from.
      page_size (int): the number of entries in each batch (affects requests
        made, but not the yielded results).
    Returns:
      A generator of Topics in the Project.
    """
    list_req = self.messages.PubsubProjectsTopicsListRequest(
        project=project_ref.RelativeName(),
        pageSize=page_size
    )
    return list_pager.YieldFromList(
        self._service, list_req, batch_size=page_size,
        field='topics', batch_size_attribute='pageSize')

  def ListSnapshots(self, topic_ref, page_size=100):
    """Lists Snapshots for a given topic.

    Args:
      topic_ref (Resource): Resource reference to Topic to list
        snapshots from.
      page_size (int): the number of entries in each batch (affects requests
        made, but not the yielded results).
    Returns:
      A generator of Snapshots for the Topic.
    """
    list_req = self.messages.PubsubProjectsTopicsSnapshotsListRequest(
        topic=topic_ref.RelativeName(),
        pageSize=page_size
    )
    list_snaps_service = self.client.projects_topics_snapshots
    return list_pager.YieldFromList(
        list_snaps_service, list_req, batch_size=page_size,
        field='snapshots', batch_size_attribute='pageSize')

  def ListSubscriptions(self, topic_ref, page_size=100):
    """Lists Subscriptions for a given topic.

    Args:
      topic_ref (Resource): Resource reference to Topic to list
        subscriptions from.
      page_size (int): the number of entries in each batch (affects requests
        made, but not the yielded results).
    Returns:
      A generator of Subscriptions for the Topic..
    """
    list_req = self.messages.PubsubProjectsTopicsSubscriptionsListRequest(
        topic=topic_ref.RelativeName(),
        pageSize=page_size
    )
    list_subs_service = self.client.projects_topics_subscriptions
    return list_pager.YieldFromList(
        list_subs_service, list_req, batch_size=page_size,
        field='subscriptions', batch_size_attribute='pageSize')

  def Publish(self, topic_ref, message_body=None, attributes=None):
    """Publishes a message to the given topic.

    Args:
      topic_ref (Resource): Resource reference to Topic to publish to.
      message_body (bytes): Message to send.
      attributes (list[AdditionalProperty]): List of attributes to attach to
        the message.
    Returns:
      PublishResponse: Response message with message ids from the API.
    Raises:
      EmptyMessageException: If neither message nor attributes is
        specified.
      PublishOperationException: When something went wrong with the publish
        operation.
    """
    if not message_body and not attributes:
      raise EmptyMessageException(
          'You cannot send an empty message. You must specify either a '
          'MESSAGE, one or more ATTRIBUTE, or both.')
    message = self.messages.PubsubMessage(
        data=message_body,
        attributes=self.messages.PubsubMessage.AttributesValue(
            additionalProperties=attributes))
    publish_req = self.messages.PubsubProjectsTopicsPublishRequest(
        publishRequest=self.messages.PublishRequest(messages=[message]),
        topic=topic_ref.RelativeName())
    result = self._service.Publish(publish_req)
    if not result.messageIds:
      # If we got a result with empty messageIds, then we've got a problem.
      raise PublishOperationException(
          'Publish operation failed with Unknown error.')
    return result

  def SetIamPolicy(self, topic_ref, policy):
    """Sets an IAM policy on a Topic.

    Args:
      topic_ref (Resource): Resource reference for topic to set
        IAM policy on.
      policy (Policy): The policy to be added to the Topic.

    Returns:
      Policy: the policy which was set.
    """
    request = self.messages.PubsubProjectsTopicsSetIamPolicyRequest(
        resource=topic_ref.RelativeName(),
        setIamPolicyRequest=self.messages.SetIamPolicyRequest(policy=policy))
    return self._service.SetIamPolicy(request)

  def GetIamPolicy(self, topic_ref):
    """Gets the IAM policy for a Topic.

    Args:
      topic_ref (Resource): Resource reference for topic to get
        the IAM policy of.

    Returns:
      Policy: the policy for the Topic.
    """
    request = self.messages.PubsubProjectsTopicsGetIamPolicyRequest(
        resource=topic_ref.RelativeName())
    return self._service.GetIamPolicy(request)

  def AddIamPolicyBinding(self, topic_ref, member, role):
    """Adds an IAM Policy binding to a Topic.

    Args:
      topic_ref (Resource): Resource reference for subscription to add
        IAM policy binding to.
      member (str): The member to add.
      role (str): The role to assign to the member.
    Returns:
      Policy: the updated policy.
    Raises:
      api_exception.HttpException: If either of the requests failed.
    """
    policy = self.GetIamPolicy(topic_ref)
    iam_util.AddBindingToIamPolicy(self.messages.Binding, policy, member, role)
    return self.SetIamPolicy(topic_ref, policy)

  def RemoveIamPolicyBinding(self, topic_ref, member, role):
    """Removes an IAM Policy binding from a Topic.

    Args:
      topic_ref (Resource): Resource reference for subscription to remove
        IAM policy binding from.
      member (str): The member to remove.
      role (str): The role to remove the member from.
    Returns:
      Policy: the updated policy.
    Raises:
      api_exception.HttpException: If either of the requests failed.
    """
    policy = self.GetIamPolicy(topic_ref)
    iam_util.RemoveBindingFromIamPolicy(policy, member, role)
    return self.SetIamPolicy(topic_ref, policy)

  def Patch(self, topic_ref, labels=None,
            recompute_message_storage_policy=False):
    """Updates a Topic.

    Args:
      topic_ref (Resource): Resource reference for the topic to be updated.
      labels (LabelsValue): The Cloud labels for the topic.
      recompute_message_storage_policy (bool): True to have the API recalculate
        the policy.
    Returns:
      Topic: The updated topic.
    Raises:
      NoFieldsSpecifiedError: if no fields were specified.
    """
    update_settings = []
    if labels is not None:
      update_settings.append(_TopicUpdateSetting('labels', labels))
    if recompute_message_storage_policy:
      update_settings.append(_TopicUpdateSetting('messageStoragePolicy', None))
    topic = self.messages.Topic(name=topic_ref.RelativeName())

    update_mask = []
    for update_setting in update_settings:
      setattr(topic, update_setting.field_name, update_setting.value)
      update_mask.append(update_setting.field_name)
    if not update_mask:
      raise NoFieldsSpecifiedError('Must specify at least one field to update.')

    patch_req = self.messages.PubsubProjectsTopicsPatchRequest(
        updateTopicRequest=self.messages.UpdateTopicRequest(
            topic=topic,
            updateMask=','.join(update_mask)),
        name=topic_ref.RelativeName())

    return self._service.Patch(patch_req)
