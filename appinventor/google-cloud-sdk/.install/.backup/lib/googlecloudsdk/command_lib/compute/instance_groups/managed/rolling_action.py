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
"""Create requests for rolling-action restart/recreate commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import managed_instance_groups_utils
from googlecloudsdk.command_lib.compute import flags
from googlecloudsdk.command_lib.compute import scope as compute_scope
from googlecloudsdk.command_lib.compute.instance_groups import flags as instance_groups_flags
from googlecloudsdk.command_lib.compute.managed_instance_groups import update_instances_utils
from googlecloudsdk.core.util import times


def CreateRequest(args,
                  cleared_fields,
                  client,
                  resources,
                  minimal_action,
                  max_surge=None):
  """Create request helper for compute instance-groups managed rolling-action.

  Args:
    args: argparse namespace
    cleared_fields: Fields which are left cleared, but should be send in request
    client: The compute client
    resources: The compute resources
    minimal_action: MinimalActionValueValuesEnum value
    max_surge: InstanceGroupManagerUpdatePolicy.maxSurge value

  Returns:
    ComputeInstanceGroupManagersPatchRequest or
    ComputeRegionInstanceGroupManagersPatchRequest instance

  Raises:
    ValueError: if instance group manager collection path is unknown
  """
  resource_arg = instance_groups_flags.MULTISCOPE_INSTANCE_GROUP_MANAGER_ARG
  default_scope = compute_scope.ScopeEnum.ZONE
  scope_lister = flags.GetDefaultScopeLister(client)
  igm_ref = resource_arg.ResolveAsResource(
      args, resources, default_scope=default_scope, scope_lister=scope_lister)

  if igm_ref.Collection() not in [
      'compute.instanceGroupManagers', 'compute.regionInstanceGroupManagers'
  ]:
    raise ValueError('Unknown reference type {0}'.format(igm_ref.Collection()))

  update_policy_type = (client.messages.InstanceGroupManagerUpdatePolicy.
                        TypeValueValuesEnum.PROACTIVE)
  max_unavailable = update_instances_utils.ParseFixedOrPercent(
      '--max-unavailable', 'max-unavailable', args.max_unavailable,
      client.messages)

  igm_info = managed_instance_groups_utils.GetInstanceGroupManagerOrThrow(
      igm_ref, client)

  versions = (igm_info.versions or [
      client.messages.InstanceGroupManagerVersion(
          instanceTemplate=igm_info.instanceTemplate)
  ])
  current_time_str = str(times.Now(times.UTC))
  for i, version in enumerate(versions):
    version.name = '%d/%s' % (i, current_time_str)

  # min_ready is available in alpha and beta APIs only
  if hasattr(args, 'min_ready'):
    update_policy = client.messages.InstanceGroupManagerUpdatePolicy(
        maxSurge=max_surge,
        maxUnavailable=max_unavailable,
        minReadySec=args.min_ready,
        minimalAction=minimal_action,
        type=update_policy_type)
  else:
    update_policy = client.messages.InstanceGroupManagerUpdatePolicy(
        maxSurge=max_surge,
        maxUnavailable=max_unavailable,
        minimalAction=minimal_action,
        type=update_policy_type)

  igm_resource = client.messages.InstanceGroupManager(
      instanceTemplate=None, updatePolicy=update_policy, versions=versions)
  if igm_ref.Collection() == 'compute.instanceGroupManagers':
    service = client.apitools_client.instanceGroupManagers
    request = client.messages.ComputeInstanceGroupManagersPatchRequest(
        instanceGroupManager=igm_ref.Name(),
        instanceGroupManagerResource=igm_resource,
        project=igm_ref.project,
        zone=igm_ref.zone)
  else:
    service = client.apitools_client.regionInstanceGroupManagers
    request = client.messages.ComputeRegionInstanceGroupManagersPatchRequest(
        instanceGroupManager=igm_ref.Name(),
        instanceGroupManagerResource=igm_resource,
        project=igm_ref.project,
        region=igm_ref.region)
  # Due to 'Patch' semantics, we have to clear either 'fixed' or 'percent'.
  # Otherwise, we'll get an error that both 'fixed' and 'percent' are set.
  if max_surge is not None:
    cleared_fields.append('updatePolicy.maxSurge.fixed' if max_surge.fixed is
                          None else 'updatePolicy.maxSurge.percent')
  if max_unavailable is not None:
    cleared_fields.append('updatePolicy.maxUnavailable.fixed'
                          if max_unavailable.fixed is None else
                          'updatePolicy.maxUnavailable.percent')
  return (service, 'Patch', request)
