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
"""bigtable instances create command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.bigtable import util as bigtable_util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.bigtable import arguments
from googlecloudsdk.core import log
from googlecloudsdk.core import resources


class CreateInstance(base.CreateCommand):
  """Create a new Bigtable instance."""

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    (arguments.ArgAdder(parser)
     .AddInstanceDisplayName(required=True)
     .AddCluster().AddClusterNodes(in_instance=True)
     .AddClusterStorage().AddClusterZone(in_instance=True)
     .AddAsync().AddInstanceType(
         default='PRODUCTION', help_text='The type of instance to create.'))
    arguments.AddInstanceResourceArg(parser, 'to create', positional=True)
    parser.display_info.AddCacheUpdater(arguments.InstanceCompleter)

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Returns:
      Some value that we want to have printed later.
    """
    cli = bigtable_util.GetAdminClient()
    ref = bigtable_util.GetInstanceRef(args.instance)
    parent_ref = resources.REGISTRY.Create(
        'bigtableadmin.projects', projectId=ref.projectsId)
    msgs = bigtable_util.GetAdminMessages()

    instance_type = msgs.Instance.TypeValueValuesEnum(args.instance_type)
    num_nodes = arguments.ProcessInstanceTypeAndNodes(args, instance_type)

    msg = msgs.CreateInstanceRequest(
        instanceId=ref.Name(),
        parent=parent_ref.RelativeName(),
        instance=msgs.Instance(
            displayName=args.display_name,
            type=msgs.Instance.TypeValueValuesEnum(args.instance_type)),
        clusters=msgs.CreateInstanceRequest.ClustersValue(additionalProperties=[
            msgs.CreateInstanceRequest.ClustersValue.AdditionalProperty(
                key=args.cluster,
                value=msgs.Cluster(
                    serveNodes=num_nodes,
                    defaultStorageType=(
                        msgs.Cluster.DefaultStorageTypeValueValuesEnum(
                            args.cluster_storage_type.upper())),
                    # TODO(b/36056455): switch location to resource
                    # when b/29566669 is fixed on API
                    location=bigtable_util.LocationUrl(args.cluster_zone)))
        ]))
    result = cli.projects_instances.Create(msg)
    operation_ref = bigtable_util.GetOperationRef(result)

    if args.async:
      log.CreatedResource(
          operation_ref,
          kind='bigtable instance {0}'.format(ref.Name()),
          is_async=True)
      return result

    return bigtable_util.AwaitInstance(
        operation_ref, 'Creating bigtable instance {0}'.format(ref.Name()))
