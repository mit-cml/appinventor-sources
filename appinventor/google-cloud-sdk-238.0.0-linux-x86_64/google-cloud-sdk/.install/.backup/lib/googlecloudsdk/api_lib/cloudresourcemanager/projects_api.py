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
"""Useful commands for interacting with the Cloud Resource Management API."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import list_pager

from googlecloudsdk.api_lib.cloudresourcemanager import projects_util
from googlecloudsdk.api_lib.resource_manager import folders
from googlecloudsdk.command_lib.iam import iam_util

DEFAULT_API_VERSION = projects_util.DEFAULT_API_VERSION


def List(limit=None,
         filter=None,  # pylint: disable=redefined-builtin
         batch_size=500,
         api_version=DEFAULT_API_VERSION):
  """Make API calls to List active projects.

  Args:
    limit: The number of projects to limit the results to. This limit is passed
      to the server and the server does the limiting.
    filter: The client side filter expression.
    batch_size: the number of projects to get with each request.
    api_version: the version of the api

  Returns:
    Generator that yields projects
  """
  client = projects_util.GetClient(api_version)
  messages = projects_util.GetMessages(api_version)
  return list_pager.YieldFromList(
      client.projects,
      messages.CloudresourcemanagerProjectsListRequest(
          filter=_AddActiveProjectFilter(filter)),
      batch_size=batch_size,
      limit=limit,
      field='projects',
      batch_size_attribute='pageSize')


def _AddActiveProjectFilter(filter_expr):
  if not filter_expr:
    return 'lifecycleState:ACTIVE'
  return 'lifecycleState:ACTIVE AND ({})'.format(filter_expr)


def Get(project_ref, api_version=DEFAULT_API_VERSION):
  """Get project information."""
  client = projects_util.GetClient(api_version)
  return client.projects.Get(
      client.MESSAGES_MODULE.CloudresourcemanagerProjectsGetRequest(
          projectId=project_ref.projectId))


def Create(project_ref,
           display_name=None,
           parent=None,
           labels=None,
           api_version=DEFAULT_API_VERSION):
  """Create a new project.

  Args:
    project_ref: The identifier for the project
    display_name: Optional display name for the project
    parent: Optional for the project (ex. folders/123 or organizations/5231)
    labels: Optional labels to apply to the project
    api_version: the version of the api

  Returns:
    An Operation object which can be used to check on the progress of the
    project creation.
  """
  client = projects_util.GetClient(api_version)
  messages = projects_util.GetMessages(api_version)
  return client.projects.Create(
      messages.Project(
          projectId=project_ref.Name(),
          name=display_name if display_name else project_ref.Name(),
          parent=parent,
          labels=labels))


def Delete(project_ref, api_version=DEFAULT_API_VERSION):
  """Delete an existing project."""
  client = projects_util.GetClient(api_version)
  messages = projects_util.GetMessages(api_version)

  client.projects.Delete(
      messages.CloudresourcemanagerProjectsDeleteRequest(
          projectId=project_ref.Name()))
  return projects_util.DeletedResource(project_ref.Name())


def Undelete(project_ref, api_version=DEFAULT_API_VERSION):
  """Undelete a project that has been deleted."""
  client = projects_util.GetClient(api_version)
  messages = projects_util.GetMessages(api_version)

  client.projects.Undelete(
      messages.CloudresourcemanagerProjectsUndeleteRequest(
          projectId=project_ref.Name()))
  return projects_util.DeletedResource(project_ref.Name())


def Update(project_ref,
           name=None,
           parent=None,
           labels_diff=None,
           api_version=DEFAULT_API_VERSION):
  """Update project information."""
  client = projects_util.GetClient(api_version)
  messages = projects_util.GetMessages(api_version)

  project = client.projects.Get(
      client.MESSAGES_MODULE.CloudresourcemanagerProjectsGetRequest(
          projectId=project_ref.projectId))

  if name:
    project.name = name

  if parent:
    project.parent = parent

  if labels_diff:
    labels_update = labels_diff.Apply(messages.Project.LabelsValue,
                                      project.labels)
    if labels_update.needs_update:
      project.labels = labels_update.labels

  return client.projects.Update(project)


def GetIamPolicy(project_ref, api_version=DEFAULT_API_VERSION):
  """Get IAM policy for a given project."""
  client = projects_util.GetClient(api_version)
  messages = projects_util.GetMessages(api_version)

  policy_request = messages.CloudresourcemanagerProjectsGetIamPolicyRequest(
      resource=project_ref.Name(),
  )
  return client.projects.GetIamPolicy(policy_request)


def SetIamPolicy(project_ref,
                 policy,
                 update_mask=None,
                 api_version=DEFAULT_API_VERSION):
  """Set IAM policy, for a given project."""
  client = projects_util.GetClient(api_version)
  messages = projects_util.GetMessages(api_version)

  set_iam_policy_request = messages.SetIamPolicyRequest(policy=policy)
  # Only include update_mask if provided, otherwise, leave the field unset.
  if update_mask is not None:
    set_iam_policy_request.updateMask = update_mask

  policy_request = messages.CloudresourcemanagerProjectsSetIamPolicyRequest(
      resource=project_ref.Name(),
      setIamPolicyRequest=set_iam_policy_request)
  return client.projects.SetIamPolicy(policy_request)


def SetIamPolicyFromFile(project_ref,
                         policy_file,
                         api_version=DEFAULT_API_VERSION):
  """Read projects IAM policy from a file, and set it."""
  messages = projects_util.GetMessages(api_version)
  policy = iam_util.ParsePolicyFile(policy_file, messages.Policy)
  update_mask = iam_util.ConstructUpdateMaskFromPolicy(policy_file)

  # To preserve the existing set-iam-policy behavior of always overwriting
  # bindings and etag, add bindings and etag to update_mask.
  if 'bindings' not in update_mask:
    update_mask += ',bindings'
  if 'etag' not in update_mask:
    update_mask += ',etag'

  return SetIamPolicy(project_ref, policy, update_mask, api_version)


def AddIamPolicyBinding(project_ref,
                        member,
                        role,
                        api_version=DEFAULT_API_VERSION):
  messages = projects_util.GetMessages(api_version)

  policy = GetIamPolicy(project_ref, api_version)
  iam_util.AddBindingToIamPolicy(messages.Binding, policy, member, role)
  return SetIamPolicy(project_ref, policy, api_version=api_version)


def AddIamPolicyBindingWithCondition(project_ref,
                                     member,
                                     role,
                                     condition,
                                     api_version=DEFAULT_API_VERSION):
  """Add iam binding with condition to project_ref's iam policy."""
  messages = projects_util.GetMessages(api_version)

  policy = GetIamPolicy(project_ref, api_version=api_version)
  iam_util.AddBindingToIamPolicyWithCondition(messages.Binding, messages.Expr,
                                              policy, member, role, condition)
  return SetIamPolicy(project_ref, policy, api_version=api_version)


def RemoveIamPolicyBinding(project_ref,
                           member,
                           role,
                           api_version=DEFAULT_API_VERSION):
  policy = GetIamPolicy(project_ref, api_version=api_version)
  iam_util.RemoveBindingFromIamPolicy(policy, member, role)
  return SetIamPolicy(project_ref, policy, api_version=api_version)


def RemoveIamPolicyBindingWithCondition(project_ref,
                                        member,
                                        role,
                                        condition,
                                        all_conditions,
                                        api_version=DEFAULT_API_VERSION):
  """Remove iam binding with condition from project_ref's iam policy."""
  policy = GetIamPolicy(project_ref, api_version=api_version)
  iam_util.RemoveBindingFromIamPolicyWithCondition(policy, member, role,
                                                   condition, all_conditions)
  return SetIamPolicy(project_ref, policy, api_version=api_version)


def ParentNameToResourceId(parent_name, api_version=DEFAULT_API_VERSION):
  messages = projects_util.GetMessages(api_version)
  if not parent_name:
    return None
  elif parent_name.startswith('folders/'):
    return messages.ResourceId(
        id=folders.FolderNameToId(parent_name), type='folder')
  elif parent_name.startswith('organizations/'):
    return messages.ResourceId(
        id=parent_name[len('organizations/'):], type='organization')
