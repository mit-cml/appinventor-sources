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
"""Shared resource flags for Cloud Spanner commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope.concepts import concepts
from googlecloudsdk.calliope.concepts import deps
from googlecloudsdk.command_lib.util.concepts import concept_parsers
from googlecloudsdk.core import properties

_PROJECT = properties.VALUES.core.project
_INSTANCE = properties.VALUES.spanner.instance


def InstanceAttributeConfig():
  """Get instance resource attribute with default value."""
  return concepts.ResourceParameterAttributeConfig(
      name='instance',
      help_text='The Cloud Spanner instance for the {resource}.',
      fallthroughs=[deps.PropertyFallthrough(_INSTANCE)])


def DatabaseAttributeConfig():
  """Get database resource attribute."""
  return concepts.ResourceParameterAttributeConfig(
      name='database',
      help_text='The Cloud Spanner database for the {resource}.')


def SessionAttributeConfig():
  """Get session resource attribute."""
  return concepts.ResourceParameterAttributeConfig(
      name='session', help_text='The Cloud Spanner session for the {resource}.')


def GetInstanceResourceSpec():
  return concepts.ResourceSpec(
      'spanner.projects.instances',
      resource_name='instance',
      instancesId=InstanceAttributeConfig(),
      projectsId=concepts.DEFAULT_PROJECT_ATTRIBUTE_CONFIG)


def GetDatabaseResourceSpec():
  return concepts.ResourceSpec(
      'spanner.projects.instances.databases',
      resource_name='database',
      databasesId=DatabaseAttributeConfig(),
      instancesId=InstanceAttributeConfig(),
      projectsId=concepts.DEFAULT_PROJECT_ATTRIBUTE_CONFIG)


def GetSessionResourceSpec():
  return concepts.ResourceSpec(
      'spanner.projects.instances.databases.sessions',
      resource_name='session',
      sessionsId=SessionAttributeConfig(),
      databasesId=DatabaseAttributeConfig(),
      instancesId=InstanceAttributeConfig(),
      projectsId=concepts.DEFAULT_PROJECT_ATTRIBUTE_CONFIG)


def AddInstanceResourceArg(parser, verb, positional=True):
  """Add a resource argument for a Cloud Spanner instance.

  NOTE: Must be used only if it's the only resource arg in the command.

  Args:
    parser: the argparse parser for the command.
    verb: str, the verb to describe the resource, such as 'to update'.
    positional: bool, if True, means that the instance ID is a positional rather
      than a flag.
  """
  name = 'instance' if positional else '--instance'
  concept_parsers.ConceptParser.ForResource(
      name,
      GetInstanceResourceSpec(),
      'The Cloud Spanner instance {}.'.format(verb),
      required=True).AddToParser(parser)


def AddDatabaseResourceArg(parser, verb, positional=True):
  """Add a resource argument for a Cloud Spanner database.

  NOTE: Must be used only if it's the only resource arg in the command.

  Args:
    parser: the argparse parser for the command.
    verb: str, the verb to describe the resource, such as 'to update'.
    positional: bool, if True, means that the database ID is a positional rather
      than a flag.
  """
  name = 'database' if positional else '--database'
  concept_parsers.ConceptParser.ForResource(
      name,
      GetDatabaseResourceSpec(),
      'The Cloud Spanner database {}.'.format(verb),
      required=True).AddToParser(parser)


def AddSessionResourceArg(parser, verb, positional=True):
  """Add a resource argument for a Cloud Spanner session.

  NOTE: Must be used only if it's the only resource arg in the command.

  Args:
    parser: the parser for the command.
    verb: str, the verb to describe the resource, such as 'to update'.
    positional: bool, if True, means that the session ID is a positional rather
      than a flag.
  """
  name = 'session' if positional else '--session'
  concept_parsers.ConceptParser.ForResource(
      name,
      GetSessionResourceSpec(),
      'The Cloud Spanner session {}.'.format(verb),
      required=True).AddToParser(parser)
