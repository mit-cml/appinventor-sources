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
"""Common command-agnostic utility functions for sql import commands."""

from __future__ import absolute_import
from __future__ import division


from __future__ import unicode_literals


def SqlImportContext(sql_messages, uri, database=None, user=None):
  """Generates the ImportContext for the given args, for importing from SQL.

  Args:
    sql_messages: module, The messages module that should be used.
    uri: The URI of the bucket to import from; the output of the 'uri' arg.
    database: The database to import to; the output of the '--database' flag.
    user: The Postgres user to import as; the output of the '--user' flag.

  Returns:
    ImportContext, for use in InstancesImportRequest.importContext.
  """
  return sql_messages.ImportContext(
      uri=uri, database=database, fileType='SQL', importUser=user)


def CsvImportContext(sql_messages,
                     uri,
                     database,
                     table,
                     columns=None,
                     user=None):
  """Generates the ImportContext for the given args, for importing from CSV.

  Args:
    sql_messages: module, The messages module that should be used.
    uri: The URI of the bucket to import from; the output of the 'uri' arg.
    database: The database to import into; the output of the '--database' flag.
    table: The table to import into; the output of the '--table' flag.
    columns: The CSV columns to import form; the output of the '--columns' flag.
    user: The Postgres user to import as; the output of the '--user' flag.

  Returns:
    ImportContext, for use in InstancesImportRequest.importContext.
  """
  return sql_messages.ImportContext(
      csvImportOptions=sql_messages.ImportContext.CsvImportOptionsValue(
          columns=columns or [], table=table),
      uri=uri,
      database=database,
      fileType='CSV',
      importUser=user)
