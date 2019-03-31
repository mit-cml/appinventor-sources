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
"""Monitors a directory tree for changes using mtime polling."""

import os
import threading
import warnings

from google.appengine.tools.devappserver2 import watcher_common

_MAX_MONITORED_FILES = 10000


class ShutdownError(Exception):
  pass


class MtimeFileWatcher(object):
  """Monitors a directory tree for changes using mtime polling."""

  # TODO: evaluate whether we can directly support multiple directories.
  SUPPORTS_MULTIPLE_DIRECTORIES = False

  def __init__(self, directory):
    self._directory = directory
    self._quit_event = threading.Event()
    self._watcher_ignore_re = None
    self._skip_files_re = None
    self._filename_to_mtime = None
    self._timeout = threading.Event()
    self._startup_thread = None

  def _refresh(self):
    self._filename_to_mtime = self._generate_filename_to_mtime()

  def set_watcher_ignore_re(self, watcher_ignore_re):
    """Allows the file watcher to ignore a custom pattern set by the user.

    Args:
      watcher_ignore_re: A RegexObject that optionally defines a pattern for the
          file watcher to ignore.
    """
    self._watcher_ignore_re = watcher_ignore_re

  def set_skip_files_re(self, skip_files_re):
    """Allows the file watcher to respect skip_files in app.yaml.

    Args:
      skip_files_re: The skip_files field of current ModuleConfiguration,
          defined in app.yaml.
    """
    self._skip_files_re = skip_files_re

  def start(self):
    """Start watching a directory for changes."""
    self._startup_thread = threading.Thread(
        target=self._refresh, name='Mtime File Watcher')
    self._startup_thread.start()

  def quit(self):
    """Stop watching a directory for changes."""
    self._quit_event.set()
    self._startup_thread.join()

  def changes(self, timeout_ms=0):
    """Returns a set of changed files if the watched directory has changed.

    The changes set is reset at every call.
    start() must be called before this method.

    Args:
      timeout_ms: Interface compatibility with the other watchers.
          It will just wait at most this time if no change is found.

    Returns:
      Returns the set of file paths changes if the watched directory has changed
      since the last call to changes or, if changes has never been called,
      since start was called.
    """
    self._startup_thread.join()
    timeout_s = timeout_ms / 1000.0

    old_filename_to_mtime = self._filename_to_mtime
    try:
      self._refresh()
      diff_items = set(self._filename_to_mtime.items()).symmetric_difference(
          old_filename_to_mtime.items())
      # os.path.getmtime() updates parent directory timestamps. E.g: There's a
      # directory structure 'root/foo/bar.txt', if you only touched 'bar.txt',
      # getmtime() would return a new timestamp for both 'bar.txt' and 'foo/'.
      #
      # Due to enabling 'skip_files_re', we have the following corner case:
      # Suppose in the above example 'foo/' is not-skippable, while 'foo/bar' is
      # skippable. Then diff_items would be like:
      # set([('foo/', timestamp1), ('foo/', timestamp2)])
      # 'where timestamp1 is the nearest mtime before the last time
      # _generate_filename_to_mtime() was run; while timestamp2 is the mtime
      # after the most recent change.'
      #
      # But we actually want to return an empty set.
      # 'effective_diff_items' excludes such unwanted items from 'diff_items'.
      dir_items = {}
      for k, _ in diff_items:
        if os.path.isdir(k):
          dir_items[k] = dir_items.setdefault(k, 0) + 1

      # If k is not in dir_items, then it is a file that has been changed.
      # If it appears in dir_items[k] once, it is a directory that has been
      # created or deleted. If it appears more than once, it is a directory
      # whose files or directories have changed underneath it. In the latter
      # case, the directory is not reloaded in entirety, rather the individual
      # files are reloaded.
      effective_diff_items = {k for k, _ in diff_items if (k not in dir_items
                                                           or dir_items[k] == 1)
                             }
      # returns immediately if we found a difference.
      if effective_diff_items or timeout_ms == 0:
        return effective_diff_items

      self._timeout.wait(timeout_s)
    except ShutdownError:
      pass
    return set()

  def _path_ignored(self, file_path):
    """Determines if a path is ignored or not.

    Args:
      file_path: The full (string) filepath.

    Returns:
      Boolean, True if ignored else False.
    """

    return watcher_common.ignore_file(
        file_path, self._skip_files_re, self._watcher_ignore_re)

  def _generate_filename_to_mtime(self):
    """Records the state of a directory.

    Returns:
      A dictionary of subdirectories and files under
      directory associated with their timestamps.
      the keys are absolute paths and values are epoch timestamps.

    Raises:
      ShutdownError: if the quit event has been fired during processing.
    """
    filename_to_mtime = {}
    num_files = 0
    for dirpath, dirnames, filenames in os.walk(
        self._directory, followlinks=True):
      if self._quit_event.is_set():
        raise ShutdownError()

      watcher_common.skip_ignored_dirs(dirpath, dirnames, self._skip_files_re)
      watcher_common.skip_ignored_dirs(dirpath, dirnames,
                                       self._watcher_ignore_re)
      filenames = [
          f for f in filenames
          if not self._path_ignored(os.path.join(dirpath, f))
      ]
      for filename in filenames + dirnames:
        if self._quit_event.is_set():
          raise ShutdownError()
        if num_files == _MAX_MONITORED_FILES:
          warnings.warn(
              'There are too many files in your application for '
              'changes in all of them to be monitored. You may have to '
              'restart the development server to see some changes to your '
              'files.')
          return filename_to_mtime
        num_files += 1
        path = os.path.join(dirpath, filename)
        try:
          filename_to_mtime[path] = os.path.getmtime(path)
        except (IOError, OSError):
          pass
    return filename_to_mtime
