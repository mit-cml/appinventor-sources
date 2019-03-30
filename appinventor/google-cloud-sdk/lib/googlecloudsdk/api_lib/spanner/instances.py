# -*- coding: utf-8 -*- #
# Copyright 2016 Google Inc. All Rights Reserved.
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
"""Spanner instance API helper."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import list_pager
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources


# The list of pre-defined IAM roles in Spanner.
KNOWN_ROLES = [
    'roles/spanner.admin', 'roles/spanner.databaseAdmin',
    'roles/spanner.databaseReader', 'roles/spanner.databaseUser',
    'roles/spanner.viewer'
]


def Create(instance, config, description, nodes):
  """Create a new instance."""
  client = apis.GetClientInstance('spanner', 'v1')
  # Module containing the definitions of messages for the specified API.
  msgs = apis.GetMessagesModule('spanner', 'v1')
  config_ref = resources.REGISTRY.Parse(
      config,
      params={'projectsId': properties.VALUES.core.project.GetOrFail},
      collection='spanner.projects.instanceConfigs')
  project_ref = resources.REGISTRY.Create(
      'spanner.projects', projectsId=properties.VALUES.core.project.GetOrFail)
  req = msgs.SpannerProjectsInstancesCreateRequest(
      parent=project_ref.RelativeName(),
      createInstanceRequest=msgs.CreateInstanceRequest(
          instanceId=instance,
          instance=msgs.Instance(
              config=config_ref.RelativeName(),
              displayName=description,
              nodeCount=nodes)))
  return client.projects_instances.Create(req)


def SetPolicy(instance_ref, policy, field_mask=None):
  """Saves the given policy on the instance, overwriting whatever exists."""
  client = apis.GetClientInstance('spanner', 'v1')
  msgs = apis.GetMessagesModule('spanner', 'v1')
  req = msgs.SpannerProjectsInstancesSetIamPolicyRequest(
      resource=instance_ref.RelativeName(),
      setIamPolicyRequest=msgs.SetIamPolicyRequest(policy=policy,
                                                   updateMask=field_mask))
  return client.projects_instances.SetIamPolicy(req)


def GetIamPolicy(instance_ref):
  """Gets the IAM policy on an instance."""
  client = apis.GetClientInstance('spanner', 'v1')
  msgs = apis.GetMessagesModule('spanner', 'v1')
  req = msgs.SpannerProjectsInstancesGetIamPolicyRequest(
      resource=instance_ref.RelativeName())
  return client.projects_instances.GetIamPolicy(req)


def Delete(instance):
  """Delete an instance."""
  client = apis.GetClientInstance('spanner', 'v1')
  msgs = apis.GetMessagesModule('spanner', 'v1')
  ref = resources.REGISTRY.Parse(
      instance,
      params={'projectsId': properties.VALUES.core.project.GetOrFail},
      collection='spanner.projects.instances')
  req = msgs.SpannerProjectsInstancesDeleteRequest(name=ref.RelativeName())
  return client.projects_instances.Delete(req)


def Get(instance):
  """Get an instance by name."""
  client = apis.GetClientInstance('spanner', 'v1')
  msgs = apis.GetMessagesModule('spanner', 'v1')
  ref = resources.REGISTRY.Parse(
      instance,
      params={'projectsId': properties.VALUES.core.project.GetOrFail},
      collection='spanner.projects.instances')
  req = msgs.SpannerProjectsInstancesGetRequest(name=ref.RelativeName())
  return client.projects_instances.Get(req)


def List():
  """List instances in the project."""
  client = apis.GetClientInstance('spanner', 'v1')
  msgs = apis.GetMessagesModule('spanner', 'v1')
  project_ref = resources.REGISTRY.Create(
      'spanner.projects', projectsId=properties.VALUES.core.project.GetOrFail)
  req = msgs.SpannerProjectsInstancesListRequest(
      parent=project_ref.RelativeName())
  return list_pager.YieldFromList(
      client.projects_instances,
      req,
      field='instances',
      batch_size_attribute='pageSize')


def Patch(instance, description=None, nodes=None):
  """Update an instance."""
  fields = []
  if description is not None:
    fields.append('displayName')
  if nodes is not None:
    fields.append('nodeCount')
  client = apis.GetClientInstance('spanner', 'v1')
  msgs = apis.GetMessagesModule('spanner', 'v1')
  ref = resources.REGISTRY.Parse(
      instance,
      params={'projectsId': properties.VALUES.core.project.GetOrFail},
      collection='spanner.projects.instances')
  req = msgs.SpannerProjectsInstancesPatchRequest(
      name=ref.RelativeName(),
      updateInstanceRequest=msgs.UpdateInstanceRequest(
          fieldMask=','.join(fields),
          instance=msgs.Instance(displayName=description, nodeCount=nodes)))
  return client.projects_instances.Patch(req)
