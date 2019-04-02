#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
"""Methods for converting GAE local datastore data into GCD Emulator data."""

import httplib
import logging
import os
import shutil

from google.appengine.api import apiproxy_stub_map
from google.appengine.api import datastore
from google.appengine.api import datastore_file_stub
from google.appengine.datastore import datastore_sqlite_stub
from google.appengine.tools.devappserver2 import datastore_grpc_stub


SQLITE_HEADER = 'SQLite format 3\x00'

# Java object stream always start with magic bytes AC ED. See:
# docs.oracle.com/javase/7/docs/platform/serialization/spec/protocol.html
JAVA_STREAM_MAGIC = '\xac\xed'


class PersistException(Exception):
  """Raised when calls to cloud datastore emulator's /persist endpoint fail."""


class StubTypes(object):
  """Possible types of stub/emulator local datastore data."""
  # Data of datastore_file_stub
  PYTHON_FILE_STUB = 0
  # Data of datastore_sqlite_stub
  PYTHON_SQLITE_STUB = 1
  # Data of either legacy java emulator or GCD Emulator format. Both are
  # supported the emulator.
  JAVA_EMULATOR = 2


def get_stub_type(filename):
  """Determine which type of datastore fake a local data file belongs to.

  Args:
    filename: String indicating the local datastore data.

  Returns:
    The stub type of filename.

  Raises:
    IOError: if filename is not readable.
  """
  if not os.access(filename, os.R_OK):
    raise IOError('Does not have read access to %s' % filename)
  with open(filename, 'rb') as f:
    leading_characters = f.read(16)
    # Based on CL/41749056, python file stub and sqlite stub can have same
    # file extension. Hence we do not rely on extension to distinguish python
    # data.
    if leading_characters == SQLITE_HEADER:
      return StubTypes.PYTHON_SQLITE_STUB
    # NOTE, JAVA_STREAM_MAGIC would not appear in datastore_file_stub data.
    # Because datastore_file_stub store with pickled data in Ascii Code, while
    # JAVA_STREAM_MAGIC is in Extended Ascii Code.
    elif leading_characters[:2] == JAVA_STREAM_MAGIC:
      return StubTypes.JAVA_EMULATOR
    else:
      return StubTypes.PYTHON_FILE_STUB


def convert_sqlite_data_to_emulator(app_id, filename, gcd_emulator_host):
  """Convert datastore sqlite stub data to cloud emulator data.

  Args:
    app_id: A String representing application ID.
    filename: A String representing the absolute path to SQLite data.
    gcd_emulator_host: A String in the format of host:port indicate the hostname
      and port number of gcd emulator.

  Raises:
    PersistException: if the call to emulator's /persist endpoint fails.
  """
  previous_stub = apiproxy_stub_map.apiproxy.GetStub('datastore_v3')
  sqlite_stub = datastore_sqlite_stub.DatastoreSqliteStub(
      app_id, filename, trusted=True, use_atexit=False)
  apiproxy_stub_map.apiproxy.ReplaceStub('datastore_v3', sqlite_stub)
  entities = _fetch_all_datastore_entities()
  if entities:
    logging.info('Fetched %d entities from %s', len(entities), filename)
    grpc_stub = datastore_grpc_stub.DatastoreGrpcStub(gcd_emulator_host)
    grpc_stub.get_or_set_call_handler_stub()
    apiproxy_stub_map.apiproxy.ReplaceStub('datastore_v3', grpc_stub)
    datastore.Put(entities)

    # persist entities to disk in emulator's data format.
    conn = httplib.HTTPConnection(gcd_emulator_host)
    conn.request('POST', '/persist')
    response = conn.getresponse()
    msg = response.read()
    if httplib.OK != response.status:
      raise PersistException(msg)
    logging.info('Datastore conversion complete')
  else:
    logging.warning('Fetched 0 entity from %s, will not create cloud '
                    'datastore emulator file', filename)
  sqlite_stub.Close()
  apiproxy_stub_map.apiproxy.ReplaceStub('datastore_v3', previous_stub)


def convert_datastore_file_stub_data_to_sqlite(app_id, datastore_file):
  """Convert datastore_file_stub data into sqlite data.

  Args:
    app_id: String indicating application id.
    datastore_file: String indicating the file name of datastore_file_stub data.

  Raises:
    IOError: if datastore_file is not writeable.
  """
  if not os.access(datastore_file, os.W_OK):
    raise IOError('Does not have write access to %s' % datastore_file)
  logging.info('Converting datastore file stub data to sqlite.')
  previous_stub = apiproxy_stub_map.apiproxy.GetStub('datastore_v3')
  sqlite_file_name = datastore_file + '.sqlite'
  try:
    apiproxy_stub_map.apiproxy = apiproxy_stub_map.APIProxyStubMap()
    datastore_stub = datastore_file_stub.DatastoreFileStub(
        app_id, datastore_file, trusted=True, save_changes=False)
    apiproxy_stub_map.apiproxy.RegisterStub('datastore_v3', datastore_stub)

    entities = _fetch_all_datastore_entities()
    sqlite_datastore_stub = datastore_sqlite_stub.DatastoreSqliteStub(
        app_id, sqlite_file_name, trusted=True)
    apiproxy_stub_map.apiproxy.ReplaceStub('datastore_v3',
                                           sqlite_datastore_stub)
    datastore.Put(entities)
    sqlite_datastore_stub.Close()
  finally:



    apiproxy_stub_map.apiproxy.ReplaceStub('datastore_v3', previous_stub)

  file_stub_data_renamed = datastore_file + '.filestub'
  shutil.move(datastore_file, file_stub_data_renamed)
  shutil.move(sqlite_file_name, datastore_file)
  logging.info('Datastore conversion complete. File stub data has been renamed '
               'to %s', file_stub_data_renamed)


def _fetch_all_datastore_entities():
  """Returns all datastore entities from all namespaces as a list."""
  all_entities = []
  for namespace in datastore.Query('__namespace__').Run():
    namespace_name = namespace.key().name()
    for kind in datastore.Query('__kind__', namespace=namespace_name).Run():
      all_entities.extend(
          datastore.Query(kind.key().name(), namespace=namespace_name).Run())
  return all_entities
