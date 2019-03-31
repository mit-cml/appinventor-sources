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
"""Command for waiting until managed instance group becomes stable."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags
from googlecloudsdk.command_lib.compute import scope as compute_scope
from googlecloudsdk.command_lib.compute.instance_groups import flags as instance_groups_flags
from googlecloudsdk.command_lib.compute.instance_groups.managed import wait_info
from googlecloudsdk.command_lib.util import time_util
from googlecloudsdk.core import log


def _AddArgs(parser):
  """Adds args."""
  parser.add_argument('--timeout',
                      type=int,
                      help='Timeout in seconds for waiting '
                      'for group becoming stable.')
  instance_groups_flags.MULTISCOPE_INSTANCE_GROUP_MANAGER_ARG.AddArgument(
      parser)


@base.ReleaseTracks(base.ReleaseTrack.GA, base.ReleaseTrack.BETA,
                    base.ReleaseTrack.ALPHA)
class WaitUntilStable(base.Command):
  """Waits until state of managed instance group is stable."""

  _TIME_BETWEEN_POLLS_SEC = 10

  @staticmethod
  def Args(parser):
    _AddArgs(parser=parser)

  def CreateGroupReference(self, client, resources, args):
    return (instance_groups_flags.MULTISCOPE_INSTANCE_GROUP_MANAGER_ARG.
            ResolveAsResource)(
                args,
                resources,
                default_scope=compute_scope.ScopeEnum.ZONE,
                scope_lister=flags.GetDefaultScopeLister(client))

  def Run(self, args):
    """Issues requests necessary to wait until stable on a MIG."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client
    start = time_util.CurrentTimeSec()
    group_ref = self.CreateGroupReference(client, holder.resources, args)

    while True:
      responses, errors = self._GetResources(client, group_ref)
      if errors:
        utils.RaiseToolException(errors)
      if responses[0].status.isStable:
        break
      log.out.Print(wait_info.CreateWaitText(responses[0]))
      time_util.Sleep(WaitUntilStable._TIME_BETWEEN_POLLS_SEC)

      if args.timeout and time_util.CurrentTimeSec() - start > args.timeout:
        raise utils.TimeoutError('Timeout while waiting for group to become '
                                 'stable.')
    log.out.Print('Group is stable')

  def GetRequestForGroup(self, client, group_ref):
    if group_ref.Collection() == 'compute.instanceGroupManagers':
      service = client.apitools_client.instanceGroupManagers
      request = service.GetRequestType('Get')(
          instanceGroupManager=group_ref.Name(),
          zone=group_ref.zone,
          project=group_ref.project)
    elif group_ref.Collection() == 'compute.regionInstanceGroupManagers':
      service = client.apitools_client.regionInstanceGroupManagers
      request = service.GetRequestType('Get')(
          instanceGroupManager=group_ref.Name(),
          region=group_ref.region,
          project=group_ref.project)
    else:
      raise ValueError('Unknown reference type {0}'.format(
          group_ref.Collection()))
    return (service, request)

  def _GetResources(self, client, group_ref):
    """Retrieves group with pending actions."""
    service, request = self.GetRequestForGroup(client, group_ref)
    errors = []
    results = client.MakeRequests(
        requests=[(service, 'Get', request)],
        errors_to_collect=errors)

    return results, errors
