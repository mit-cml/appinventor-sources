# -*- coding: utf-8 -*- #
# Copyright 2016 Google Inc. All Rights Reserved.
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
"""Logic for streaming logs.

We implement streaming with two important implementation details.  First,
we use polling because Cloud Logging does not support streaming. Second, we
have no guarantee that we will receive logs in chronological order.
This is because clients can emit logs with chosen timestamps.  However,
we want to generate an ordered list of logs.  So, we choose to not fetch logs
in the most recent N seconds.  We also decided to skip logs that are returned
too late (their timestamp is more than N seconds old).
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import datetime
import time

import enum

from googlecloudsdk.api_lib.logging import common as logging_common
from googlecloudsdk.core import log
from googlecloudsdk.core.util import times


_UNIX_ZERO_TIMESTAMP = '1970-01-01T01:00:00.000000000Z'


class LogPosition(object):
  """Tracks a position in the log.

  Log messages are sorted by timestamp.  Within a given timestamp, logs will be
  returned in order of insert_id.
  """

  def __init__(self, timestamp=None):
    self.timestamp = timestamp or _UNIX_ZERO_TIMESTAMP
    self.insert_id = ''
    self.need_insert_id_in_lb_filter = False

  def Update(self, timestamp, insert_id):
    """Update the log position based on new log entry data.

    Args:
        timestamp: the timestamp of the message we just read, as an RFC3339
                   string.
        insert_id: the insert id of the message we just read.

    Returns:
        True if the position was updated; False if not.
    """
    if timestamp < self.timestamp:
      # The message is behind this LogPosition.  No update required.
      return False
    elif timestamp == self.timestamp:
      # When the timestamp is the same, we need to move forward the insert id.
      if insert_id > self.insert_id:
        self.insert_id = insert_id
        self.need_insert_id_in_lb_filter = True
        return True
      return False
    else:
      # Once we see a new timestamp, move forward the minimum time that we're
      # willing to accept.
      self.need_insert_id_in_lb_filter = False
      self.insert_id = insert_id
      self.timestamp = timestamp
      return True

  def GetFilterLowerBound(self):
    """The log message filter which keeps out messages which are too old.

    Returns:
        The lower bound filter text that we should use.
    """

    if self.need_insert_id_in_lb_filter:
      return '((timestamp="{0}" AND insertId>"{1}") OR timestamp>"{2}")'.format(
          self.timestamp, self.insert_id, self.timestamp)
    else:
      return 'timestamp>="{0}"'.format(self.timestamp)

  def GetFilterUpperBound(self, now):
    """The log message filter which keeps out messages which are too new.

    Args:
        now: The current time, as a datetime object.

    Returns:
        The upper bound filter text that we should use.
    """

    tzinfo = times.ParseDateTime(self.timestamp).tzinfo
    now = now.replace(tzinfo=tzinfo)
    upper_bound = now - datetime.timedelta(seconds=5)
    return 'timestamp<"{0}"'.format(
        times.FormatDateTime(upper_bound, '%Y-%m-%dT%H:%M:%S.%6f%Ez'))


class _TaskIntervalTimer(object):
  """Timer to facilitate performing multiple tasks at different intervals.

  Here's an overview of how the caller sees this class:

  >>> timer = _TaskIntervalTimer({'a': 5, 'b': 10, 'c': 3})
  >>> timer.Wait()  # sleeps 3 seconds, total time elapsed 3
  ['c']
  >>> timer.Wait()  # sleeps 2 seconds, total time elapsed 5
  ['a']
  >>> timer.Wait()  # sleeps 1 second,  total time elapsed 6
  ['c']
  >>> timer.Wait()  # sleeps 3 seconds, total time elapsed 9
  ['c']
  >>> timer.Wait()  # sleeps 1 second,  total time elapsed 10
  ['a', 'c']

  And here's how it might be used in practice:

  >>> timer = _TaskIntervalTimer({'foo': 1, 'bar': 10, 'baz': 3})
  >>> while True:
  ...   tasks = timer.Wait()
  ...   if 'foo' in tasks:
  ...     foo()
  ...   if 'bar' in tasks:
  ...     bar()
  ...   if 'baz' in tasks:
  ...     do_baz()


  Attributes:
    task_intervals: dict (hashable to int), mapping from some representation of
      a task to to the interval (in seconds) at which the task should be
      performed
  """

  def __init__(self, task_intervals):
    self.task_intervals = task_intervals
    self._time_remaining = self.task_intervals.copy()

  def Wait(self):
    """Wait for the next task(s) and return them.

    Returns:
      set, the tasks which should be performed
    """
    sleep_time = min(self._time_remaining.values())
    time.sleep(sleep_time)

    tasks = set()
    for task in self._time_remaining:
      self._time_remaining[task] -= sleep_time
      if self._time_remaining[task] == 0:
        self._time_remaining[task] = self.task_intervals[task]
        tasks.add(task)
    return tasks


class LogFetcher(object):
  """A class which fetches job logs."""

  class _Tasks(enum.Enum):
    POLL = 1
    CHECK_CONTINUE = 2

  LOG_BATCH_SIZE = 1000  # API max

  def __init__(self, filters=None, polling_interval=10,
               continue_func=lambda x: True, continue_interval=None,
               num_prev_entries=None):
    """Initializes the LogFetcher.

    Args:
      filters: list of string filters used in the API call.
      polling_interval: amount of time to sleep between each poll.
      continue_func: One-arg function that takes in the number of empty polls
        and outputs a boolean to decide if we should keep polling or not. If not
        given, keep polling indefinitely.
      continue_interval: int, how often to check whether the job is complete
        using continue_function. If not provided, defaults to the same as the
        polling interval.
      num_prev_entries: int, if provided, will first perform a decending
        query to set a lower bound timestamp equal to that of the n:th entry.
    """
    self.base_filters = filters or []
    self.polling_interval = polling_interval
    self.continue_interval = continue_interval or polling_interval
    self.should_continue = continue_func
    start_timestamp = _GetTailStartingTimestamp(filters, num_prev_entries)
    log.debug('start timestamp: {}'.format(start_timestamp))
    self.log_position = LogPosition(timestamp=start_timestamp)

  def GetLogs(self):
    """Retrieves a batch of logs.

    After we fetch the logs, we ensure that none of the logs have been seen
    before.  Along the way, we update the most recent timestamp.

    Returns:
      A list of valid log entries.
    """
    utcnow = datetime.datetime.utcnow()
    lower_filter = self.log_position.GetFilterLowerBound()
    upper_filter = self.log_position.GetFilterUpperBound(utcnow)
    new_filter = self.base_filters + [lower_filter, upper_filter]
    entries = logging_common.FetchLogs(
        log_filter=' AND '.join(new_filter),
        order_by='ASC',
        limit=self.LOG_BATCH_SIZE)
    return [entry for entry in entries if
            self.log_position.Update(entry.timestamp, entry.insertId)]

  def YieldLogs(self):
    """Polls Get API for more logs.

    We poll so long as our continue function, which considers the number of
    periods without new logs, returns True.

    Yields:
        A single log entry.
    """
    timer = _TaskIntervalTimer({
        self._Tasks.POLL: self.polling_interval,
        self._Tasks.CHECK_CONTINUE: self.continue_interval
    })
    empty_polls = 0
    # Do both tasks when we start
    tasks = [self._Tasks.POLL, self._Tasks.CHECK_CONTINUE]
    while True:
      if self._Tasks.POLL in tasks:
        logs = self.GetLogs()
        if logs:
          empty_polls = 0
          for log_entry in logs:
            yield log_entry
        else:
          empty_polls += 1
      if self._Tasks.CHECK_CONTINUE in tasks:
        should_continue = self.should_continue(empty_polls)
        if not should_continue:
          break
      tasks = timer.Wait()


def _GetTailStartingTimestamp(filters, offset=None):
  """Returns the starting timestamp to start streaming logs from.

  Args:
    filters: [str], existing filters, should not contain timestamp constraints.
    offset: int, how many entries ago we should pick the starting timestamp.
      If not provided, unix time zero will be returned.

  Returns:
    str, A timestamp that can be used as lower bound or None if no lower bound
      is necessary.
  """
  if not offset:
    return None
  entries = list(logging_common.FetchLogs(log_filter=' AND '.join(filters),
                                          order_by='DESC',
                                          limit=offset))
  if len(entries) < offset:
    return None
  return list(entries)[-1].timestamp
