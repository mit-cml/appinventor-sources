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

"""Module for requesting logs from the admin console.

This code was mostly copied from appcfg, and not significantly refactored.
Ideally gcloud would use the logging API for this information but that is not
yet available.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import calendar
import datetime
import re
import tempfile
import time

from googlecloudsdk.core import exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core.util import files

from six.moves import range  # pylint: disable=redefined-builtin


class Error(exceptions.Error):
  pass


class CannotOpenFileError(Error):

  def __init__(self, f, e):
    super(CannotOpenFileError, self).__init__(
        'Failed to open file [{f}]: {error}'.format(f=f, error=e))


class LogsRequester(object):
  """Provide facilities to export request logs."""

  def __init__(self, rpcserver, project, service, version,
               severity=None, vhost=None, include_vhost=None, include_all=None):
    """Constructor.

    Args:
      rpcserver: The RPC server to use.  Should be an instance of HttpRpcServer
        or TestRpcServer.
      project: The project id to fetch logs from.
      service: The service of the app to fetch logs from, optional.
      version: The version of the app to fetch logs for.
      severity: App log severity to request (0-4); None for no app logs.
      vhost: The virtual host of log messages to get. None for all hosts.
      include_vhost: If true, the virtual host is included in log messages.
      include_all: If true, we add to the log message everything we know
        about the request.
    """
    self.rpcserver = rpcserver
    self._next_offset_regex = re.compile(r'^#\s*next_offset=(\S+)\s*$')
    self._log_line_regex = re.compile(r'[^[]+\[(\d+/[A-Za-z]+/\d+):[^\d]*')

    self._params = {'app_id': project,
                    'version': version,
                    'limit': 1000,
                    'no_header': 1,
                    'module': service}
    if severity is not None:
      self._params['severity'] = str(severity)
    if vhost is not None:
      self._params['vhost'] = str(vhost)
    if include_vhost is not None:
      self._params['include_vhost'] = str(include_vhost)
    if include_all is not None:
      self._params['include_all'] = str(include_all)

  def DownloadLogsAppend(self, end_date, output_file):
    """Download the requested logs and append to an existing file.

    Args:
      end_date: datetime.date, Date object representing last day of logs to
        return.  If None, today is used.
      output_file: Output file name or '-' for standard output.
    """
    now = PacificDate(time.time())
    end_date = end_date if (end_date and end_date < now) else now
    valid_dates = (None, end_date)
    sentinel = FindSentinel(output_file)
    self._DownloadLogs(valid_dates, sentinel, output_file, append=True)

  def DownloadLogs(self, num_days, end_date, output_file):
    """Download the requested logs.

    This will write the logs to the file designated by self.output_file, or to
    stdout if the filename is '-'. Multiple roundtrips to the server may be
    made.

    Args:
      num_days: Number of days worth of logs to export; 0 for all available.
      end_date: datetime.date, Date object representing last day of logs to
        return.  If None, today is used.
      output_file: Output file name or '-' for standard output.
    """
    now = PacificDate(time.time())
    end_date = end_date if (end_date and end_date < now) else now
    # Default to 1 day if not provided.
    if num_days is None:
      num_days = 1
    # Start date is end - num_days, or beginning of time if num_days is 0.
    valid_dates = (
        end_date - datetime.timedelta(num_days - 1) if num_days else None,
        end_date)
    sentinel = None
    self._DownloadLogs(valid_dates, sentinel, output_file, append=False)

  def _DownloadLogs(self, valid_dates, sentinel, output_file, append):
    """Common utility method for both normal and append modes."""
    # A temporary file is used because the API for requesting logs
    # gives us the newest logs first.  We write them in this order to
    # the temporary file and then read the temporary file backwards,
    # copying to the output file line by line (special-casing null
    # bytes).
    tf = tempfile.TemporaryFile()
    last_offset = None
    try:
      while True:
        new_offset = self.RequestLogLines(
            tf, last_offset, valid_dates, sentinel)
        if not new_offset or new_offset == last_offset:
          break
        last_offset = new_offset
      if output_file == '-':
        of = log.out
      else:
        try:
          of = files.FileWriter(output_file, append=append)
        except files.Error as e:
          raise CannotOpenFileError(output_file, e)
      try:
        line_count = CopyReversedLines(tf, of)
      finally:
        of.flush()
        if of is not log.out:
          of.close()  # pytype: disable=attribute-error
    finally:
      tf.close()
    log.info('Copied %d records.', line_count)

  def RequestLogLines(self, tf, offset, valid_dates, sentinel):
    """Make a single roundtrip to the server.

    Args:
      tf: Writable binary stream to which the log lines returned by
        the server are written, stripped of headers, and excluding
        lines skipped due to self.sentinel or self.valid_dates filtering.
      offset: Offset string for a continued request; None for the first.
      valid_dates: (datetime.date, datetime.date), A tuple of start and end
        dates to get the logs between.
      sentinel: str, The last line in the log file we are appending to, or None.

    Returns:
      The offset string to be used for the next request, if another
      request should be issued; or None, if not.
    """
    log.debug('Request with offset %r.', offset)
    params = dict(self._params)
    if offset:
      params['offset'] = offset

    response = self.rpcserver.Send('/api/request_logs', payload=None, **params)
    response = response.replace('\r', '\0')
    lines = response.splitlines()
    log.info('Received %d bytes, %d records.', len(response), len(lines))

    # Move all references to self.<anything> out of the loop.
    is_skipping = True
    (start, end) = valid_dates
    next_offset_regex = self._next_offset_regex
    len_sentinel = len(sentinel) if sentinel else None

    next_offset = None
    for line in lines:
      if line.startswith('#'):
        match = next_offset_regex.match(line)
        # We are now (May 2014) frequently seeing None instead of a blank or
        # not-present next_offset at all. This extra check handles that.
        if match and match.group(1) != 'None':
          next_offset = match.group(1)
        continue

      if (sentinel and
          line.startswith(sentinel) and
          line[len_sentinel: len_sentinel + 1] in ('', '\0')):
        return None

      linedate = self.DateOfLogLine(line)
      # We don't write unparseable log lines, ever.
      if not linedate:
        continue
      if is_skipping:
        if linedate > end:
          continue
        else:
          # We are in the good date range, stop doing date comparisons.
          is_skipping = False

      if start and linedate < start:
        return None
      tf.write(line + '\n')
    return next_offset

  def DateOfLogLine(self, line):
    """Returns a date object representing the log line's timestamp.

    Args:
      line: a log line string.
    Returns:
      A date object representing the timestamp or None if parsing fails.
    """
    m = self._log_line_regex.match(line)
    if not m:
      return None
    try:
      return datetime.date(*time.strptime(m.group(1), '%d/%b/%Y')[:3])
    except ValueError:
      return None


def CopyReversedLines(instream, outstream, blocksize=2 ** 16):
  r"""Copy lines from input stream to output stream in reverse order.

  As a special feature, null bytes in the input are turned into
  newlines followed by tabs in the output, but these 'sub-lines'
  separated by null bytes are not reversed.  E.g. If the input is
  'A\0B\nC\0D\n', the output is 'C\n\tD\nA\n\tB\n'.

  Args:
    instream: A seekable stream open for reading in binary mode.
    outstream: A stream open for writing; doesn't have to be seekable or
      binary.
    blocksize: Optional block size for buffering, for unit testing.

  Returns:
    The number of lines copied.

  """
  line_count = 0
  instream.seek(0, 2)  # To EOF
  last_block = instream.tell() // blocksize
  spillover = ''
  for iblock in range(last_block + 1, -1, -1):
    instream.seek(iblock * blocksize)
    data = instream.read(blocksize)
    lines = data.splitlines(True)
    lines[-1:] = ''.join(lines[-1:] + [spillover]).splitlines(True)
    if lines and not lines[-1].endswith('\n'):
      # If the last line in the input doesn't end in \n, add it.
      lines[-1] += '\n'
    lines.reverse()
    if lines and iblock > 0:
      spillover = lines.pop()
    if lines:
      line_count += len(lines)
      data = ''.join(lines).replace('\0', '\n\t')
      outstream.write(data)
  return line_count


def PacificDate(now):
  """For a UTC timestamp, return the date in the US/Pacific timezone.

  Args:
    now: A posix timestamp giving current UTC time.

  Returns:
    A date object representing what day it is in the US/Pacific timezone.
  """
  # We avoid date.fromtimestamp() so that we don't rely on user's timezone.
  return datetime.date(*time.gmtime(PacificTime(now))[:3])


def PacificTime(now):
  """Helper to return the number of seconds between UTC and Pacific time.

  This is needed to compute today's date in Pacific time (more
  specifically: Mountain View local time), which is how request logs
  are reported.  (Google servers always report times in Mountain View
  local time, regardless of where they are physically located.)

  This takes (post-2006) US DST into account.  Pacific time is either
  8 hours or 7 hours west of UTC, depending on whether DST is in
  effect.  Since 2007, US DST starts on the Second Sunday in March
  March, and ends on the first Sunday in November.  (Reference:
  http://aa.usno.navy.mil/faq/docs/daylight_time.php.)

  Note that the server doesn't report its local time (the HTTP Date
  header uses UTC), and the client's local time is irrelevant.

  Args:
    now: A posix timestamp giving current UTC time.

  Returns:
    A pseudo-posix timestamp giving current Pacific time.  Passing
    this through time.gmtime() will produce a tuple in Pacific local
    time.
  """
  now -= 8 * 3600  # Convert to Pacific Standard Time (PST)
  if IsPacificDST(now):
    now += 3600  # Move one hour east when DST is in effect (PDT)
  return now


# DAY and SUNDAY are constants used by IsPacificDST
DAY = 24 * 3600
SUNDAY = 6  # Monday == 0


def IsPacificDST(now):
  """Helper for PacificTime to decide whether now is Pacific DST (PDT).

  Args:
    now: A pseudo-posix timestamp giving current time in PST.

  Returns:
    True if now falls within the range of DST, False otherwise.
  """
  pst = time.gmtime(now)  # Convert to time tuple
  year = pst[0]
  assert year >= 2007  # We don't need to deal with the old DST rules
  # Find 2am on the second Sunday in March
  begin = calendar.timegm((year, 3, 8, 2, 0, 0, 0, 0, 0))
  while time.gmtime(begin).tm_wday != SUNDAY:
    begin += DAY
  # Find 2am on the first Sunday in November
  end = calendar.timegm((year, 11, 1, 2, 0, 0, 0, 0, 0))
  while time.gmtime(end).tm_wday != SUNDAY:
    end += DAY
  return begin <= now < end


def FindSentinel(filename, blocksize=2 ** 16):
  """Return the sentinel line from the output file.

  Args:
    filename: The filename of the output file.  (We'll read this file.)
    blocksize: Optional block size for buffering, for unit testing.

  Returns:
    The contents of the last line in the file that doesn't start with
    a tab, with its trailing newline stripped; or None if the file
    couldn't be opened or no such line could be found by inspecting
    the last 'blocksize' bytes of the file.
  """
  try:
    fp = files.BinaryFileReader(filename)
  except files.Error as err:
    log.warning('Append mode disabled: can\'t read [%r]: %s', filename, err)
    return None
  try:
    fp.seek(0, 2)  # EOF
    fp.seek(max(0, fp.tell() - blocksize))
    lines = fp.readlines()
    del lines[:1]  # First line may be partial, throw it away
    sentinel = None
    for line in lines:
      if not line.startswith('\t'):
        sentinel = line
    if not sentinel:
      return None
    return sentinel.rstrip('\n')
  finally:
    fp.close()
