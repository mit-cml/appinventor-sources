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
"""Wrapper of the Cloud Datastore emulator for Google Appengine local dev."""




import httplib
import logging
import os
import socket
import subprocess
import sys
import tempfile
import time

import httplib2
import portpicker
from google.appengine.tools.devappserver2 import constants


_DEFAULT_EMULATOR_OPTIONS = ['--testing']


class InvalidEmulatorOptionError(ValueError):
  """Raised when options to start the emulator are invalid."""


def ParsePortFromOption(options):
  """Parse the port number from emulator options.

  Port can be specified in either '-p 123' or '--port=123'.

  Args:
    options: A list of Strings representing options passed to the emulator.

  Returns:
    An integer representing the port number or None if no port number is found.

  Raises:
    InvalidEmulatorOptionError: If '-p' is not followed with a port number, or
      port number is not a valid integer.
  """
  port = None
  for idx, option in enumerate(options):
    if option == '-p':
      if idx + 1 >= len(options):
        raise InvalidEmulatorOptionError(
            'Found -p, but no port number is specified.')
      port = options[idx+1]
      break
    if option.startswith('--port='):
      _, port = option.split('=', 1)
      break
  if port:
    if not port.isdigit():
      raise InvalidEmulatorOptionError(
          '%s is not a valid port number!' % port)
    else:
      port = int(port)
  return port


class DatastoreEmulator(object):
  """A Datastore emulator."""

  def __init__(self,
               emulator_cmd=None,
               deadline=10,
               start_options=(),
               silent=False):
    """Constructs a DatastoreEmulator.

    Clients should use DatastoreEmulatorFactory to construct DatastoreEmulator
    instances.

    Args:
      emulator_cmd: A string representing the path to an executable script that
        invokes emulator binary.
      deadline: A integer representing number of seconds to wait for the
        datastore to start.
      start_options: A list of additional command-line options to pass to the
          emulator 'start' command.
      silent: A bool indicates if emulator runs in silent mode.

    Raises:
      IOError: if the emulator failed to start within the deadline
    """
    self._emulator_cmd = emulator_cmd
    self._http = httplib2.Http()
    self.__running = False

    self._silent = silent

    self._redirected_output = open(os.devnull, 'wb') if self._silent else None

    # Start the emulator and wait for it to start responding to requests.
    cmd = [self._emulator_cmd, 'start'] + _DEFAULT_EMULATOR_OPTIONS
    if start_options:
      cmd.extend(start_options)
    port = ParsePortFromOption(start_options or [])
    if not port:
      port = portpicker.pick_unused_port()
      cmd.append('--port=%d' % port)
    self._host = 'http://localhost:%d' % port
    cmd.append(tempfile.mkdtemp())

    # On windows, cloud_datastore_emulator.bat always prompts up
    # 'Terminate batch job (Y/N)'. Passing nul to this .bat avoids self.Stop()
    # hang at this prompt up.
    if sys.platform.startswith('win'):
      cmd.append('<nul')

    popen_kwargs = {}
    if self._silent:
      popen_kwargs.update(
          stdout=self._redirected_output, stderr=self._redirected_output)

    self.emulator_proc = subprocess.Popen(cmd, **popen_kwargs)
    if not self._WaitForStartup(deadline):
      raise IOError('emulator did not respond within %ds' % deadline)
    self.__datastore = None
    self.__running = True

  def _WaitForStartup(self, deadline):
    """Waits for the emulator to start.

    Args:
      deadline: deadline in seconds

    Returns:
      True if the emulator responds within the deadline, False otherwise.
    """
    start = time.time()
    interval = 0.2

    def Elapsed():
      return time.time() - start

    logging.info('%s: %s',
                 constants.DATASTORE_EMULATOR_STARTING_MSG, self._host)
    while True:
      try:
        response, _ = self._http.request(self._host)
        if response.status == 200:
          logging.info(
              'Cloud Datastore emulator responded after %f seconds', Elapsed())
          return True
      except (socket.error, httplib.ResponseNotReady):
        pass
      if Elapsed() >= deadline:
        # Out of time; give up.
        return False
      else:
        time.sleep(interval)

  def Clear(self):
    """Clears all data from the emulator instance.

    Returns:
      True if the data was successfully cleared, False otherwise.
    """
    response, _ = self._http.request('%s/reset' % self._host, method='POST')
    if response.status == 200:
      return True
    else:
      logging.warning('failed to clear emulator; response was: %s', response)

  def Stop(self):
    """Stops the emulator instance."""
    if not self.__running:
      return
    logging.info('shutting down the emulator running at %s', self._host)
    try:
      response, _ = self._http.request('%s/shutdown' % self._host,
                                       method='POST')
      if response.status != 200:
        logging.warning(
            'failed to shut down Cloud Datastore emulator; response: %s',
            response)
    except httplib.BadStatusLine as e:
      logging.warning(
          'failed to shut down Cloud Datastore emulator; received error: %s', e)
    finally:
      self.__running = False
      # wait for emulator to shutdown:
      self.emulator_proc.communicate()
      if self._silent:
        self._redirected_output.close()

  def __del__(self):
    if not self.__running:
      return
    # The user forgets to call Stop()
    logging.warning('emulator shutting down due to '
                    'DatastoreEmulator object deletion')
    self.Stop()

  def GetHostPort(self):
    """Returns the hostname:port of this emulator."""
    return self._host
