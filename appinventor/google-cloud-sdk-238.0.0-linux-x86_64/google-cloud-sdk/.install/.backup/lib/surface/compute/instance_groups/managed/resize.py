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

"""Command for setting size of managed instance group."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import sys

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute import flags
from googlecloudsdk.command_lib.compute import scope as compute_scope
from googlecloudsdk.command_lib.compute.instance_groups import flags as instance_groups_flags


def _AddArgs(parser, creation_retries):
  """Adds args."""
  parser.add_argument(
      '--size',
      required=True,
      type=arg_parsers.BoundedInt(0, sys.maxsize, unlimited=True),
      help=('Target number of instances in managed instance group.'))

  if creation_retries:
    parser.add_argument('--creation-retries', action='store_true', default=True,
                        help='When instance creation fails retry it.')


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Resize(base.Command):
  """Set managed instance group size."""

  @staticmethod
  def Args(parser):
    _AddArgs(parser=parser, creation_retries=False)
    instance_groups_flags.MULTISCOPE_INSTANCE_GROUP_MANAGER_ARG.AddArgument(
        parser)

  def CreateGroupReference(self, client, resources, args):
    return (instance_groups_flags.MULTISCOPE_INSTANCE_GROUP_MANAGER_ARG.
            ResolveAsResource(
                args, resources,
                default_scope=compute_scope.ScopeEnum.ZONE,
                scope_lister=flags.GetDefaultScopeLister(client)))

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    igm_ref = self.CreateGroupReference(client, holder.resources, args)
    if igm_ref.Collection() == 'compute.instanceGroupManagers':
      service = client.apitools_client.instanceGroupManagers
      request = client.messages.ComputeInstanceGroupManagersResizeRequest(
          instanceGroupManager=igm_ref.Name(),
          size=args.size,
          project=igm_ref.project,
          zone=igm_ref.zone)
    elif igm_ref.Collection() == 'compute.regionInstanceGroupManagers':
      service = client.apitools_client.regionInstanceGroupManagers
      request = client.messages.ComputeRegionInstanceGroupManagersResizeRequest(
          instanceGroupManager=igm_ref.Name(),
          size=args.size,
          project=igm_ref.project,
          region=igm_ref.region)
    else:
      raise ValueError('Unknown reference type {0}'.format(
          igm_ref.Collection()))

    return client.MakeRequests([(service, 'Resize', request)])


@base.ReleaseTracks(base.ReleaseTrack.BETA, base.ReleaseTrack.ALPHA)
class ResizeBeta(Resize):
  """Set managed instance group size."""

  @staticmethod
  def Args(parser):
    _AddArgs(parser=parser, creation_retries=True)
    instance_groups_flags.MULTISCOPE_INSTANCE_GROUP_MANAGER_ARG.AddArgument(
        parser)

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    igm_ref = self.CreateGroupReference(client, holder.resources, args)
    if igm_ref.Collection() == 'compute.instanceGroupManagers':
      service = client.apitools_client.instanceGroupManagers
      method = 'ResizeAdvanced'
      request = (
          client.messages.ComputeInstanceGroupManagersResizeAdvancedRequest(
              instanceGroupManager=igm_ref.Name(),
              instanceGroupManagersResizeAdvancedRequest=(
                  client.messages.InstanceGroupManagersResizeAdvancedRequest(
                      targetSize=args.size,
                      noCreationRetries=not args.creation_retries,
                  )),
              project=igm_ref.project,
              zone=igm_ref.zone))
    else:
      if not args.creation_retries:
        raise exceptions.ConflictingArgumentsException(
            '--no-creation-retries', '--region')
      service = client.apitools_client.regionInstanceGroupManagers
      method = 'Resize'
      request = client.messages.ComputeRegionInstanceGroupManagersResizeRequest(
          instanceGroupManager=igm_ref.Name(),
          size=args.size,
          project=igm_ref.project,
          region=igm_ref.region)

    return client.MakeRequests([(service, method, request)])


Resize.detailed_help = {
    'brief': 'Set managed instance group size.',
    'DESCRIPTION': """
        *{command}* resize a managed instance group to a provided size.

If you resize down, the Instance Group Manager service deletes instances from
the group until the group reaches the desired size. Instances are deleted
in arbitrary order but the Instance Group Manager takes into account some
considerations before it chooses which instance to delete. For more information,
see https://cloud.google.com/compute/docs/reference/rest/v1/instanceGroupManagers/resize.

If you resize up, the service adds instances to the group using the current
instance template until the group reaches the desired size.
""",
}
ResizeBeta.detailed_help = Resize.detailed_help
