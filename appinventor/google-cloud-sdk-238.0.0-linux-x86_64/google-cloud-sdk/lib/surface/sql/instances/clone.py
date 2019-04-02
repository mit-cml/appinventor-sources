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
"""Clones a Cloud SQL instance."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.sql import api_util
from googlecloudsdk.api_lib.sql import exceptions
from googlecloudsdk.api_lib.sql import operations
from googlecloudsdk.api_lib.sql import validate
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.sql import flags
from googlecloudsdk.core import log
from googlecloudsdk.core import properties

_DETAILED_HELP = """

  *{command}* creates a clone of the Cloud SQL instance. The source and the
  destination instances must be in the same project. The clone once created
  will be an independent Cloud SQL instance.

  The binary log coordinates, if specified, act as the point up to which the
  source instance is cloned. If not specified, the source instance is
  cloned up to the most recent binary log coordinates at the time the command is
  executed.

  ## EXAMPLES

  To clone a source instance to the most recent binary log coordinates:

    $ {command} instance-foo instance-bar

  or to clone at specific binary log coordinates:

    $ {command} instance-foo instance-bar --bin-log-file-name mysql-bin.000020 --bin-log-position 170
"""


@base.ReleaseTracks(base.ReleaseTrack.GA, base.ReleaseTrack.BETA,
                    base.ReleaseTrack.ALPHA)
class Clone(base.CreateCommand):
  """Clones a Cloud SQL instance."""

  @classmethod
  def Args(cls, parser):
    """Declare flag and positional arguments for the command parser."""
    base.ASYNC_FLAG.AddToParser(parser)
    parser.display_info.AddFormat(flags.GetInstanceListFormat())
    parser.add_argument(
        'source',
        completer=flags.InstanceCompleter,
        help='Cloud SQL instance ID of the source.')
    parser.add_argument(
        'destination', help='Cloud SQL instance ID of the clone.')
    parser.add_argument(
        '--bin-log-file-name',
        required=False,
        help="""\
        Represents the name of the binary log file created by the source
        instance if it has binary logs enabled.
        If specified, is the point up to which the source instance is cloned.
        It must be specified along with --bin-log-position to form a valid
        binary log coordinates.
        e.g., mysql-bin.000001
        """)
    parser.add_argument(
        '--bin-log-position',
        type=int,
        required=False,
        help="""\
        Represents the position (offset) inside the binary log file created by
        the source instance if it has binary logs enabled.
        If specified, is the point up to which the source instance is cloned.
        It must be specified along with --bin-log-file to form a valid binary
        log coordinates.
        e.g., 123 (a numeric value)
        """)
    parser.display_info.AddCacheUpdater(flags.InstanceCompleter)

  def _CheckSourceAndDestination(self, source_instance_ref,
                                 destination_instance_ref):
    if source_instance_ref.project != destination_instance_ref.project:
      raise exceptions.ArgumentError(
          'The source and the clone instance must belong to the same project:'
          ' "{src}" != "{dest}".'.format(
              src=source_instance_ref.project,
              dest=destination_instance_ref.project))

  def _GetInstanceRefsFromArgs(self, args, client):
    """Get validated refs to source and destination instances from args."""

    validate.ValidateInstanceName(args.source)
    validate.ValidateInstanceName(args.destination)
    source_instance_ref = client.resource_parser.Parse(
        args.source,
        params={'project': properties.VALUES.core.project.GetOrFail},
        collection='sql.instances')
    destination_instance_ref = client.resource_parser.Parse(
        args.destination,
        params={'project': properties.VALUES.core.project.GetOrFail},
        collection='sql.instances')

    self._CheckSourceAndDestination(source_instance_ref,
                                    destination_instance_ref)
    return source_instance_ref, destination_instance_ref

  def _UpdateRequestFromArgs(self, request, args, sql_messages):
    if args.bin_log_file_name and args.bin_log_position:
      clone_context = request.instancesCloneRequest.cloneContext
      clone_context.binLogCoordinates = sql_messages.BinLogCoordinates(
          binLogFileName=args.bin_log_file_name,
          binLogPosition=args.bin_log_position)
    elif args.bin_log_file_name or args.bin_log_position:
      raise exceptions.ArgumentError(
          'Both --bin-log-file-name and --bin-log-position must be specified to'
          ' represent a valid binary log coordinate up to which the source is'
          ' cloned.')

  def Run(self, args):
    """Clones a Cloud SQL instance.

    Args:
      args: argparse.Namespace, The arguments that this command was invoked
          with.

    Returns:
      A dict object representing the operations resource describing the
      clone operation if the clone was successful.
    Raises:
      ArgumentError: The arguments are invalid for some reason.
    """

    client = api_util.SqlClient(api_util.API_VERSION_DEFAULT)
    sql_client = client.sql_client
    sql_messages = client.sql_messages

    source_instance_ref, destination_instance_ref = (
        self._GetInstanceRefsFromArgs(args, client))

    request = sql_messages.SqlInstancesCloneRequest(
        project=source_instance_ref.project,
        instance=source_instance_ref.instance,
        instancesCloneRequest=sql_messages.InstancesCloneRequest(
            cloneContext=sql_messages.CloneContext(
                destinationInstanceName=destination_instance_ref.instance)))

    self._UpdateRequestFromArgs(request, args, sql_messages)

    result = sql_client.instances.Clone(request)

    operation_ref = client.resource_parser.Create(
        'sql.operations',
        operation=result.name,
        project=destination_instance_ref.project)

    if args.async:
      if not args.IsSpecified('format'):
        args.format = 'default'
      return sql_client.operations.Get(
          sql_messages.SqlOperationsGetRequest(
              project=operation_ref.project, operation=operation_ref.operation))
    operations.OperationsV1Beta4.WaitForOperation(sql_client, operation_ref,
                                                  'Cloning Cloud SQL instance')
    log.CreatedResource(destination_instance_ref)
    rsource = sql_client.instances.Get(
        sql_messages.SqlInstancesGetRequest(
            project=destination_instance_ref.project,
            instance=destination_instance_ref.instance))
    return rsource


Clone.__doc__ += _DETAILED_HELP
