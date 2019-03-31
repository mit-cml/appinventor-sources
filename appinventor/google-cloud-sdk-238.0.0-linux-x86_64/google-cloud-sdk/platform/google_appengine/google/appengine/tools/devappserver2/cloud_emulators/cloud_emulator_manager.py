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
"""Methods for managing cloud emulators.

This module is the gate between App Engine local story and gogole cloud
emulators. It wires flags from app engine world into cloud emulators' flags,
perform misc checks and exposes process management logics for emulators.
"""
import os

from google.appengine.tools.devappserver2.cloud_emulators.datastore import datastore_emulator


class DatastoreEmulatorManager(object):
  """A wrapper class for common operations on Cloud Datastore emulator."""

  def __init__(self, cmd=None, is_test=False):
    """Constructor.

    Args:
      cmd: A string representing the path to a executable shell script that
        invokes the emulator.
      is_test: A boolean. If True, run the emulator in --testing mode. Otherwise
        override some emulator flags.

    Raises:
      IOError: Cloud Datastore emulator is not found.
    """
    self._cmd = cmd
    self._is_test = is_test

  @classmethod
  def CheckOptions(cls, index_file, storage_file):
    """Check options related to gcd emulator.

    This checks all user input options and is performed before launching the
    emulator to intercept erroneous options. The emulator defaultly runs in
    silent mode with stdout hidden.

    Args:
      index_file: A string indicating the name of index file.
      storage_file: A string indicating the name of storage file.
    """
    cls._CheckPotentialNewFile(index_file)
    cls._CheckPotentialNewFile(storage_file)

  @classmethod
  def _CheckPotentialNewFile(cls, filename):
    """Raises exception if filename is not a valid.

    A filename is valid when it exists or its parent directory parsed from its
    absolute path is an existing directory.

    Args:
      filename: A string indicating the name of a potentially new file.

    Raises:
      IOError: if filename is not a valid new filename.
    """
    if not filename or os.path.exists(filename):
      return
    dirname = os.path.dirname(os.path.abspath(filename))
    # isdir() returns False if dirname does not exist or is not a directory.
    if not os.path.isdir(dirname):
      raise IOError('Cannot create %s because %s is not an existing '
                    'directory.' % (filename, dirname))

  def Launch(self, port, silent, index_file, require_indexes=False,
             storage_file='', auto_id_policy=None):
    """Launch the emulator synchronously.

    Args:
      port: An integer representing the port number for emulator.
      silent: A bool indicating if emulator runs in silent mode.
      index_file: A string indicating the fullpath to index.yaml.
      require_indexes: A bool passing the flag 'require_indexes' to
        the emulator.
      storage_file: A string indicating emulator's storage file fullname.
      auto_id_policy: A string specifying how the emualtor assigns auto id.
    """
    options = [
        '--regenerate_indexes=false',
        '--auto_id_policy=%s' % auto_id_policy,
        '--port=%d' % port]
    if not self._is_test:
      # Override emulator options.
      # DatastoreEmulator Always launches the emulator with
      # '--testing' Flag, which is equivalent to setting:
      # --store_on_disk=false,
      # --store_index_configuration_on_disk=false
      # --consistency=1.0
      # All these flags are overridable. Dev_appserver by default wants:
      # --store_on_disk=true,
      # --store_index_configuration_on_disk=true
      # --consistency='time' (see defined in
      # datastore_stub_util.TimeBasedHRConsistencyPolicy). Currently, Time based
      # consistency is not supported by the emulator, and we temporarily use
      # strong consistency policy instead.
      # TODO: Support time based consistency policy in GCD Emulator.
      options.extend(
          ['--store_on_disk=true', '--store_index_configuration_on_disk=true'])
      options.append('--index_file=' + index_file)
      if storage_file:
        options.append('--storage_file=' + storage_file)
      if require_indexes:
        options.append('--require_indexes')

    self.emulator = datastore_emulator.DatastoreEmulator(
        emulator_cmd=self._cmd, start_options=options, silent=silent)

  def Shutdown(self):
    self.emulator.Stop()
