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
"""Command for updating instances of managed instance group."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import managed_instance_groups_utils
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags
from googlecloudsdk.command_lib.compute import scope as compute_scope
from googlecloudsdk.command_lib.compute.instance_groups import flags as instance_groups_flags
from googlecloudsdk.command_lib.compute.instance_groups.managed import flags as instance_groups_managed_flags
from googlecloudsdk.command_lib.compute.managed_instance_groups import update_instances_utils


def _AddArgs(parser, supports_min_ready=False):
  """Adds args."""
  instance_groups_managed_flags.AddTypeArg(parser)
  instance_groups_managed_flags.AddMaxSurgeArg(parser)
  instance_groups_managed_flags.AddMaxUnavailableArg(parser)
  if supports_min_ready:
    instance_groups_managed_flags.AddMinReadyArg(parser)
  parser.add_argument(
      '--version',
      type=arg_parsers.ArgDict(spec={'template': str,
                                     'name': str}),
      metavar='template=TEMPLATE,[name=NAME]',
      help=('Original instance template resource to be used. '
            'Each version has the following format: '
            'template=TEMPLATE,[name=NAME]'),
      required=True)
  parser.add_argument(
      '--canary-version',
      type=arg_parsers.ArgDict(
          spec={'template': str,
                'target-size': str,
                'name': str}),
      category=base.COMMONLY_USED_FLAGS,
      metavar='template=TEMPLATE,target-size=FIXED_OR_PERCENT,[name=NAME]',
      help=('New instance template resource to be used. '
            'Each version has the following format: '
            'template=TEMPLATE,target-size=FIXED_OR_PERCENT,[name=NAME]'))
  instance_groups_managed_flags.AddForceArg(parser)


@base.ReleaseTracks(base.ReleaseTrack.GA)
class StartUpdate(base.Command):
  """Start update instances of managed instance group."""

  @staticmethod
  def Args(parser):
    _AddArgs(parser=parser)
    instance_groups_flags.MULTISCOPE_INSTANCE_GROUP_MANAGER_ARG.AddArgument(
        parser)

  def Run(self, args):
    update_instances_utils.ValidateCanaryVersionFlag('--canary-version',
                                                     args.canary_version)
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    cleared_fields = []
    request = self.CreateRequest(args, cleared_fields, client, holder.resources)

    with client.apitools_client.IncludeFields(cleared_fields):
      return client.MakeRequests([request])

  def CreateRequest(self, args, cleared_fields, client, resources):
    resource_arg = instance_groups_flags.MULTISCOPE_INSTANCE_GROUP_MANAGER_ARG
    default_scope = compute_scope.ScopeEnum.ZONE
    scope_lister = flags.GetDefaultScopeLister(client)
    igm_ref = resource_arg.ResolveAsResource(
        args, resources, default_scope=default_scope, scope_lister=scope_lister)

    if igm_ref.Collection() not in [
        'compute.instanceGroupManagers', 'compute.regionInstanceGroupManagers'
    ]:
      raise ValueError('Unknown reference type {0}'.format(
          igm_ref.Collection()))

    update_policy_type = update_instances_utils.ParseUpdatePolicyType(
        '--type', args.type, client.messages)
    max_surge = update_instances_utils.ParseFixedOrPercent(
        '--max-surge', 'max-surge', args.max_surge, client.messages)
    max_unavailable = update_instances_utils.ParseFixedOrPercent(
        '--max-unavailable', 'max-unavailable', args.max_unavailable,
        client.messages)

    igm_info = managed_instance_groups_utils.GetInstanceGroupManagerOrThrow(
        igm_ref, client)

    versions = []
    versions.append(
        update_instances_utils.ParseVersion(igm_ref.project, '--version',
                                            args.version, resources,
                                            client.messages))
    if args.canary_version:
      versions.append(
          update_instances_utils.ParseVersion(
              igm_ref.project, '--canary-version', args.canary_version,
              resources, client.messages))
    managed_instance_groups_utils.ValidateVersions(igm_info, versions,
                                                   args.force)

    # TODO(b/36049787): Decide what we should do when two versions have the same
    #              instance template (this can happen with canary restart
    #              performed using tags).
    igm_version_names = {
        version.instanceTemplate: version.name
        for version in igm_info.versions
    }
    for version in versions:
      if not version.name:
        version.name = igm_version_names.get(version.instanceTemplate)
    minimal_action = (client.messages.InstanceGroupManagerUpdatePolicy.
                      MinimalActionValueValuesEnum.REPLACE)

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
    if hasattr(igm_ref, 'zone'):
      service = client.apitools_client.instanceGroupManagers
      request = (client.messages.ComputeInstanceGroupManagersPatchRequest(
          instanceGroupManager=igm_ref.Name(),
          instanceGroupManagerResource=igm_resource,
          project=igm_ref.project,
          zone=igm_ref.zone))
    elif hasattr(igm_ref, 'region'):
      service = client.apitools_client.regionInstanceGroupManagers
      request = (client.messages.ComputeRegionInstanceGroupManagersPatchRequest(
          instanceGroupManager=igm_ref.Name(),
          instanceGroupManagerResource=igm_resource,
          project=igm_ref.project,
          region=igm_ref.region))
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


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class StartUpdateAlphaBeta(StartUpdate):
  """Start update instances of managed instance group."""

  @staticmethod
  def Args(parser):
    _AddArgs(parser=parser, supports_min_ready=True)
    instance_groups_flags.MULTISCOPE_INSTANCE_GROUP_MANAGER_ARG.AddArgument(
        parser)


StartUpdate.detailed_help = {
    'brief':
        'Updates instances in a managed instance group',
    'DESCRIPTION':
        """\
        *{command}* updates instances in a managed instance group,
        according to the given versions and the given update policy."""
}
