# -*- coding: utf-8 -*- #
# Copyright 2013 Google Inc. All Rights Reserved.
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
"""List all instance operations.

Lists all instance operations that have been performed on the given
Cloud SQL instance.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import list_pager

from googlecloudsdk.api_lib.sql import api_util
from googlecloudsdk.api_lib.sql import validate
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.sql import flags
from googlecloudsdk.core import properties


@base.ReleaseTracks(base.ReleaseTrack.GA, base.ReleaseTrack.BETA,
                    base.ReleaseTrack.ALPHA)
class List(base.ListCommand):
  """Lists all instance operations for the given Cloud SQL instance."""

  @staticmethod
  def Args(parser):
    flags.AddInstance(parser)
    parser.display_info.AddFormat(flags.OPERATION_FORMAT_BETA)
    parser.display_info.AddCacheUpdater(None)

  def Run(self, args):
    """Lists all instance operations that have been performed on an instance.

    Args:
      args: argparse.Namespace, The arguments that this command was invoked
          with.

    Returns:
      A dict object that has the list of operation resources if the command ran
      successfully.
    """
    client = api_util.SqlClient(api_util.API_VERSION_DEFAULT)
    sql_client = client.sql_client
    sql_messages = client.sql_messages

    validate.ValidateInstanceName(args.instance)
    instance_ref = client.resource_parser.Parse(
        args.instance,
        params={'project': properties.VALUES.core.project.GetOrFail},
        collection='sql.instances')

    return list_pager.YieldFromList(
        sql_client.operations,
        sql_messages.SqlOperationsListRequest(
            project=instance_ref.project, instance=instance_ref.instance),
        limit=args.limit)
