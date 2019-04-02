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
"""Utility functions for gcloud firestore emulator."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import os
from googlecloudsdk.command_lib.emulators import util
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import execution_utils
from googlecloudsdk.core import log
from googlecloudsdk.core.util import platforms


class NoFirestoreEmulatorError(exceptions.Error):

  def __init__(self):
    super(NoFirestoreEmulatorError,
          self).__init__('Unable to find the Google Cloud Firestore emulator')


def GetFirestoreEmulatorRoot():
  """Gets the directory of the Firestore emulator installation in the Cloud SDK.

  Raises:
    NoCloudSDKError: If there is no SDK root.
    NoFirestoreEmulatorError: If the installation dir does not exist.

  Returns:
    str, The path to the root of the Firestore emulator installation within
    Cloud SDK.
  """
  sdk_root = util.GetCloudSDKRoot()
  firestore_dir = os.path.join(sdk_root, 'platform', 'cloud-firestore-emulator')
  if not os.path.isdir(firestore_dir):
    raise NoFirestoreEmulatorError()
  return firestore_dir


def ArgsForFirestoreEmulator(emulator_args):
  """Constucts an argument list for calling the Firestore emulator.

  Args:
    emulator_args: args for the emulator.

  Returns:
    An argument list to execute the Firestore emulator.
  """
  current_os = platforms.OperatingSystem.Current()
  if current_os is platforms.OperatingSystem.WINDOWS:
    cmd = 'cloud_firestore_emulator.cmd'
    exe = os.path.join(GetFirestoreEmulatorRoot(), cmd)
    return execution_utils.ArgsForCMDTool(exe, *emulator_args)
  else:
    cmd = 'cloud_firestore_emulator'
    exe = os.path.join(GetFirestoreEmulatorRoot(), cmd)
    return execution_utils.ArgsForExecutableTool(exe, *emulator_args)


FIRESTORE = 'firestore'
FIRESTORE_TITLE = 'Google Cloud Firestore emulator'


def StartFirestoreEmulator(args, log_file=None):
  """Starts the firestore emulator with the given arguments.

  Args:
    args: Arguments passed to the start command.
    log_file: optional file argument to reroute process's output.

  Returns:
    process, The handle of the child process running the datastore emulator.
  """
  start_args = ['start']
  start_args.append('--host={0}'.format(args.host_port.host))
  start_args.append('--port={0}'.format(args.host_port.port))
  if args.rules:
    start_args.append('--rules={0}'.format(args.rules))
  exec_args = ArgsForFirestoreEmulator(start_args)

  log.status.Print('Executing: {0}'.format(' '.join(exec_args)))
  return util.Exec(exec_args, log_file=log_file)


def ValidateStartArgs(args):  # pylint: disable=unused-argument
  pass


def GetHostPort():
  return util.GetHostPort(FIRESTORE)


class FirestoreEmulator(util.Emulator):
  """Represents the ability to start and route firestore emulator."""

  def Start(self, port):
    args = util.AttrDict({
        'host_port': {
            'host': 'localhost',
            'port': port,
        }
    })
    return StartFirestoreEmulator(args, self._GetLogNo())

  @property
  def prefixes(self):
    return ['google.firestore']

  @property
  def service_name(self):
    return FIRESTORE

  @property
  def emulator_title(self):
    return FIRESTORE_TITLE

  @property
  def emulator_component(self):
    return 'cloud-firestore-emulator'
