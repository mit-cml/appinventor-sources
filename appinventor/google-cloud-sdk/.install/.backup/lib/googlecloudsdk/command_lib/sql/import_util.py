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
"""Common utility functions for sql import commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.sql import import_util
from googlecloudsdk.api_lib.sql import operations
from googlecloudsdk.api_lib.sql import validate
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.sql import flags
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.console import console_io


def AddBaseImportFlags(parser, filetype):
  base.ASYNC_FLAG.AddToParser(parser)
  flags.AddInstanceArgument(parser)
  flags.AddUriArgument(
      parser,
      'Path to the {filetype} file in Google Cloud Storage from which'
      ' the import is made. The URI is in the form gs://bucketName/fileName.'
      ' Compressed gzip files (.gz) are also supported.'
      .format(filetype=filetype))
  flags.AddUser(parser, 'PostgreSQL user for this import operation.')


def RunImportCommand(args, client, import_context):
  """Imports data into a Cloud SQL instance.

  Args:
    args: argparse.Namespace, The arguments that this command was invoked with.
    client: SqlClient instance, with sql_client and sql_messages props, for use
        in generating messages and making API calls.
    import_context: ImportContext; format-specific import metadata.

  Returns:
    A dict representing the import operation resource, if '--async' is used,
    or else None.

  Raises:
    HttpException: An HTTP error response was received while executing API
        request.
    ToolException: An error other than HTTP error occurred while executing the
        command.
  """
  sql_client = client.sql_client
  sql_messages = client.sql_messages

  validate.ValidateInstanceName(args.instance)
  instance_ref = client.resource_parser.Parse(
      args.instance,
      params={'project': properties.VALUES.core.project.GetOrFail},
      collection='sql.instances')

  console_io.PromptContinue(
      message='Data from [{uri}] will be imported to [{instance}].'.format(
          uri=args.uri, instance=args.instance),
      default=True,
      cancel_on_no=True)

  import_request = sql_messages.SqlInstancesImportRequest(
      instance=instance_ref.instance,
      project=instance_ref.project,
      instancesImportRequest=sql_messages.InstancesImportRequest(
          importContext=import_context))

  result_operation = sql_client.instances.Import(import_request)

  operation_ref = client.resource_parser.Create(
      'sql.operations',
      operation=result_operation.name,
      project=instance_ref.project)

  if args.async:
    return sql_client.operations.Get(
        sql_messages.SqlOperationsGetRequest(
            project=operation_ref.project, operation=operation_ref.operation))

  operations.OperationsV1Beta4.WaitForOperation(
      sql_client, operation_ref, 'Importing data into Cloud SQL instance')

  log.status.write('Imported data from [{bucket}] into [{instance}].\n'.format(
      instance=instance_ref, bucket=args.uri))

  return None


def RunSqlImportCommand(args, client):
  """Imports data from a SQL dump file into Cloud SQL instance.

  Args:
    args: argparse.Namespace, The arguments that this command was invoked
        with.
    client: SqlClient instance, with sql_client and sql_messages props, for use
        in generating messages and making API calls.

  Returns:
    A dict representing the import operation resource, if '--async' is used,
    or else None.
  """
  sql_import_context = import_util.SqlImportContext(
      client.sql_messages, args.uri, args.database, args.user)
  return RunImportCommand(args, client, sql_import_context)


def RunCsvImportCommand(args, client):
  """Imports data from a CSV file into Cloud SQL instance.

  Args:
    args: argparse.Namespace, The arguments that this command was invoked
        with.
    client: SqlClient instance, with sql_client and sql_messages props, for use
        in generating messages and making API calls.

  Returns:
    A dict representing the import operation resource, if '--async' is used,
    or else None.
  """
  csv_import_context = import_util.CsvImportContext(
      client.sql_messages, args.uri, args.database, args.table, args.columns,
      args.user)
  return RunImportCommand(args, client, csv_import_context)
