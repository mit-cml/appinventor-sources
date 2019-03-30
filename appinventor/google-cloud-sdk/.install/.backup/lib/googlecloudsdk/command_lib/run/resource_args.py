# -*- coding: utf-8 -*- #
# Copyright 2018 Google Inc. All Rights Reserved.
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

"""Shared resource flags for Cloud Run commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import os
import re

from googlecloudsdk.calliope.concepts import concepts
from googlecloudsdk.calliope.concepts import deps
from googlecloudsdk.command_lib.run import flags
from googlecloudsdk.command_lib.util.concepts import presentation_specs
from googlecloudsdk.core import properties
from googlecloudsdk.core.console import console_io


class ServicePromptFallthrough(deps.Fallthrough):
  """Fall through to reading the service from an interactive prompt."""

  def __init__(self):
    super(ServicePromptFallthrough, self).__init__(
        function=None,
        hint='specify the service name at an interactive prompt')

  def _Call(self, parsed_args):
    if not console_io.CanPrompt():
      return None
    source_ref = None
    if hasattr(parsed_args, 'source') or hasattr(parsed_args, 'image'):
      source_ref = flags.GetSourceRef(parsed_args.source, parsed_args.image)
    message = 'Service name:'
    if source_ref:
      default_name = GenerateServiceName(source_ref)
      service_name = console_io.PromptWithDefault(
          message=message, default=default_name)
    else:
      service_name = console_io.PromptResponse(message=message)
    return service_name


def GenerateServiceName(source_ref):
  """Produce a valid default service name.

  Converts a file path or image path into a reasonable default service name by
  stripping file path delimeters, image tags, and image hashes.
  For example, the image name 'gcr.io/myproject/myimage:latest' would produce
  the service name 'myimage'.

  Args:
    source_ref: SourceRef, The app's source directory or container path.

  Returns:
    A valid Cloud Run service name.
  """
  base_name = os.path.basename(source_ref.source_path.rstrip(os.sep))
  base_name = base_name.split(':')[0]  # Discard image tag if present.
  base_name = base_name.split('@')[0]  # Disacard image hash if present.
  # Remove non-supported special characters.
  return re.sub(r'[^a-zA-Z0-9-]', '', base_name).strip('-').lower()


class ServiceFileFallthrough(deps.Fallthrough):
  """Fall through to reading the property from .gcloud-serverless-service."""

  def __init__(self, name):
    super(ServiceFileFallthrough, self).__init__(
        function=None,
        hint='put a {name} key-value pair in .gcloud-serverless-service'.format(
            name=name))
    self.name = name

  def _Call(self, parsed_args):
    config = flags.GetLocalConfig(parsed_args)
    if config:
      return getattr(config, self.name, None)
    return None


class DefaultFallthrough(deps.Fallthrough):
  """Use the namespace "default".

  For Knative only.

  For Cloud Run, raises an ArgumentError if project not set.
  """

  def __init__(self):
    super(DefaultFallthrough, self).__init__(
        function=None,
        hint='For Cloud Run on Kubernetes Engine, defaults to "default". '
        'Otherwise, defaults to project ID.')

  def _Call(self, parsed_args):
    if (getattr(parsed_args, 'cluster', None) or
        properties.VALUES.run.cluster.Get()):
      return 'default'
    elif not (getattr(parsed_args, 'project', None) or
              properties.VALUES.core.project.Get()):
      # HACK: Compensate for how "namespace" is actually "project" in Cloud Run
      # by providing an error message explicitly early here.
      raise flags.ArgumentError(
          'The [project] resource is not properly specified. '
          'Please specify the argument [--project] on the command line or '
          'set the property [core/project].')
    return None


def NamespaceAttributeConfig():
  return concepts.ResourceParameterAttributeConfig(
      name='namespace',
      help_text='Specific to Cloud Run on Kubernetes Engine: '
      'The Kubernetes namespace for the {resource}',
      fallthroughs=[
          deps.PropertyFallthrough(properties.VALUES.run.namespace),
          DefaultFallthrough(),
          deps.ArgFallthrough('project'),
          deps.PropertyFallthrough(properties.VALUES.core.project),
      ])


def ServiceAttributeConfig(prompt=False):
  if prompt:
    fallthroughs = [
        ServiceFileFallthrough('service'),
        ServicePromptFallthrough(),
    ]
  else:
    fallthroughs = []
  return concepts.ResourceParameterAttributeConfig(
      name='service',
      help_text='The Service for the {resource}.',
      fallthroughs=fallthroughs)


def ConfigurationAttributeConfig():
  return concepts.ResourceParameterAttributeConfig(
      name='configuration',
      help_text='The Configuration for the {resource}.')


def RouteAttributeConfig():
  return concepts.ResourceParameterAttributeConfig(
      name='route',
      help_text='The Route for the {resource}.')


def RevisionAttributeConfig():
  return concepts.ResourceParameterAttributeConfig(
      name='revision',
      help_text='The Revision for the {resource}.')


def DomainAttributeConfig():
  return concepts.ResourceParameterAttributeConfig(
      name='domain',
      help_text='The name of the domain to be mapped to.')


def ClusterAttributeConfig():
  return concepts.ResourceParameterAttributeConfig(
      name='cluster',
      help_text='Specific to Cloud Run on Kubernetes Engine: '
      'The name of the Kubernetes Engine cluster to use. Alternatively, set the'
      ' property [run/cluster].',
      fallthroughs=[
          deps.PropertyFallthrough(properties.VALUES.run.cluster)])


def ClusterLocationAttributeConfig():
  return concepts.ResourceParameterAttributeConfig(
      name='location',
      help_text='Specific to Cloud Run on Kubernetes Engine: '
      'The location of the {resource}. Alternatively, set the '
      'property [run/cluster_location].',
      fallthroughs=[
          deps.PropertyFallthrough(
              properties.VALUES.run.cluster_location)])


def GetClusterResourceSpec():
  return concepts.ResourceSpec(
      'container.projects.zones.clusters',
      projectId=concepts.DEFAULT_PROJECT_ATTRIBUTE_CONFIG,
      zone=ClusterLocationAttributeConfig(),
      clusterId=ClusterAttributeConfig(),
      resource_name='cluster')


def GetServiceResourceSpec(prompt=False):
  return concepts.ResourceSpec(
      'run.namespaces.services',
      namespacesId=NamespaceAttributeConfig(),
      servicesId=ServiceAttributeConfig(prompt),
      resource_name='service')


def GetConfigurationResourceSpec():
  return concepts.ResourceSpec(
      'run.namespaces.configurations',
      namespacesId=NamespaceAttributeConfig(),
      configurationsId=ConfigurationAttributeConfig(),
      resource_name='configuration')


def GetRouteResourceSpec():
  return concepts.ResourceSpec(
      'run.namespaces.routes',
      namespacesId=NamespaceAttributeConfig(),
      routesId=RouteAttributeConfig(),
      resource_name='route')


def GetRevisionResourceSpec():
  return concepts.ResourceSpec(
      'run.namespaces.revisions',
      namespacesId=NamespaceAttributeConfig(),
      revisionsId=RevisionAttributeConfig(),
      resource_name='revision')


def GetDomainMappingResourceSpec():
  return concepts.ResourceSpec(
      'run.namespaces.domainmappings',
      namespacesId=NamespaceAttributeConfig(),
      domainmappingsId=DomainAttributeConfig(),
      resource_name='DomainMapping')


def GetNamespaceResourceSpec():
  return concepts.ResourceSpec(
      'run.namespaces',
      namespacesId=NamespaceAttributeConfig(),
      resource_name='namespace')


CLUSTER_PRESENTATION = presentation_specs.ResourcePresentationSpec(
    '--cluster',
    GetClusterResourceSpec(),
    'Specific to Cloud Run on Kubernetes Engine: '
    'The Kubernetes Engine cluster to connect to.',
    required=False,
    prefixes=True)

