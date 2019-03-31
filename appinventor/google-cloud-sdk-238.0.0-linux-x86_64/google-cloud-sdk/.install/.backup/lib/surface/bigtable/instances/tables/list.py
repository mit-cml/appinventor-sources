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
"""bigtable tables list command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.bigtable import arguments
from googlecloudsdk.core import exceptions as core_exceptions
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources
from google.bigtable.admin.v2 import bigtable_table_admin_pb2


try:
  # TODO(b/33587054): Make sure grpc is available on all platforms.
  # pylint: disable=g-import-not-at-top
  # pylint: disable=g-bad-import-order
  from googlecloudsdk.core import grpc_util
  from google.bigtable.admin.v2 import bigtable_table_admin_pb2_grpc
  grpc_available = True
except ImportError:
  grpc_available = False


def _GetUriFunction(resource):
  table_ref = resources.REGISTRY.ParseRelativeName(
      resource.name,
      collection='bigtableadmin.projects.instances.tables')
  return table_ref.SelfLink()


class ListInstances(base.ListCommand):
  """List existing Bigtable instance tables."""

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    parser.display_info.AddFormat("""
          table(
            name.basename():sort=1
          )
        """)
    parser.display_info.AddUriFunc(_GetUriFunction)
    arguments.ArgAdder(parser).AddInstance(
        positional=False, required=False, multiple=True)

  def Run(self, args):
    if not grpc_available:
      raise core_exceptions.InternalError('gRPC is not available')

    channel = grpc_util.MakeSecureChannel('bigtableadmin.googleapis.com:443')
    instances = args.instances or ['-']
    for instance in instances:
      instance_ref = resources.REGISTRY.Parse(
          instance,
          params={'projectsId': properties.VALUES.core.project.GetOrFail},
          collection='bigtableadmin.projects.instances')

      stub = bigtable_table_admin_pb2_grpc.BigtableTableAdminStub(channel)

      request = bigtable_table_admin_pb2.ListTablesRequest(
          parent=instance_ref.RelativeName(),
      )

      return grpc_util.YieldFromList(
          stub.ListTables, request, items_field='tables')
