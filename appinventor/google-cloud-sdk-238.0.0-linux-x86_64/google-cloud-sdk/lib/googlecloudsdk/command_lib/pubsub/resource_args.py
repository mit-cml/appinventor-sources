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

"""Shared resource flags for Cloud Pub/Sub commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope.concepts import concepts
from googlecloudsdk.command_lib.util.concepts import concept_parsers
from googlecloudsdk.command_lib.util.concepts import presentation_specs


def SubscriptionAttributeConfig():
  return concepts.ResourceParameterAttributeConfig(
      name='subscription',
      help_text='Name of the subscription.')


def TopicAttributeConfig():
  return concepts.ResourceParameterAttributeConfig(
      name='topic',
      help_text='Name of the topic.')


def GetSubscriptionResourceSpec():
  return concepts.ResourceSpec(
      'pubsub.projects.subscriptions',
      resource_name='subscription',
      subscriptionsId=SubscriptionAttributeConfig(),
      projectsId=concepts.DEFAULT_PROJECT_ATTRIBUTE_CONFIG)


def GetTopicResourceSpec():
  return concepts.ResourceSpec(
      'pubsub.projects.topics',
      resource_name='topic',
      topicsId=TopicAttributeConfig(),
      projectsId=concepts.DEFAULT_PROJECT_ATTRIBUTE_CONFIG)


def CreateSubscriptionResourceArg(verb, plural=False):
  """Create a resource argument for a Cloud Pub/Sub Subscription.

  Args:
    verb: str, the verb to describe the resource, such as 'to update'.
    plural: bool, if True, use a resource argument that returns a list.

  Returns:
    the PresentationSpec for the resource argument.
  """
  if plural:
    help_stem = 'One or more subscriptions'
  else:
    help_stem = 'Name of the subscription'
  return presentation_specs.ResourcePresentationSpec(
      'subscription',
      GetSubscriptionResourceSpec(),
      '{} {}'.format(help_stem, verb),
      required=True,
      plural=plural,
      prefixes=True)


def AddSubscriptionResourceArg(parser, verb, plural=False):
  """Add a resource argument for a Cloud Pub/Sub Subscription.

  Args:
    parser: the parser for the command.
    verb: str, the verb to describe the resource, such as 'to update'.
    plural: bool, if True, use a resource argument that returns a list.
  """
  concept_parsers.ConceptParser(
      [CreateSubscriptionResourceArg(verb, plural=plural)]
  ).AddToParser(parser)


def CreateTopicResourceArg(verb, positional=True, plural=False):
  """Create a resource argument for a Cloud Pub/Sub Topic.

  Args:
    verb: str, the verb to describe the resource, such as 'to update'.
    positional: bool, if True, means that the topic ID is a positional rather
      than a flag. If not positional, this also creates a '--topic-project' flag
      as subscriptions and topics do not need to be in the same project.
    plural: bool, if True, use a resource argument that returns a list.

  Returns:
    the PresentationSpec for the resource argument.
  """
  if positional:
    name = 'topic'
    flag_name_overrides = {}
  else:
    name = '--topic' if not plural else '--topics'
    flag_name_overrides = {'project': '--topic-project'}
  help_stem = 'Name of the topic'
  if plural:
    help_stem = 'One or more topics'
  return presentation_specs.ResourcePresentationSpec(
      name,
      GetTopicResourceSpec(),
      '{} {}'.format(help_stem, verb),
      required=True,
      flag_name_overrides=flag_name_overrides,
      plural=plural,
      prefixes=True)


def AddTopicResourceArg(parser, verb, positional=True, plural=False):
  """Add a resource argument for a Cloud Pub/Sub Topic.

  Args:
    parser: the parser for the command.
    verb: str, the verb to describe the resource, such as 'to update'.
    positional: bool, if True, means that the topic ID is a positional rather
      than a flag. If not positional, this also creates a '--topic-project' flag
      as subscriptions and topics do not need to be in the same project.
    plural: bool, if True, use a resource argument that returns a list.
  """
  concept_parsers.ConceptParser(
      [CreateTopicResourceArg(verb, positional=positional, plural=plural)]
  ).AddToParser(parser)


def AddResourceArgs(parser, resources):
  """Add resource arguments for commands that have topic and subscriptions.

  Args:
    parser: the parser for the command.
    resources: a list of resource args to add.
  """
  concept_parsers.ConceptParser(resources).AddToParser(parser)
