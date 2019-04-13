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

"""Used to collect anonymous SDK metrics."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import atexit
import contextlib
import json
import os
import pickle
import platform
import socket
import subprocess
import sys
import tempfile
import time
import uuid

from googlecloudsdk.core import config
from googlecloudsdk.core import execution_utils
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.console import console_attr
from googlecloudsdk.core.console import console_io
from googlecloudsdk.core.util import encoding
from googlecloudsdk.core.util import files
from googlecloudsdk.core.util import platforms

import six
import six.moves.urllib.error
import six.moves.urllib.parse
import six.moves.urllib.request


_GA_ENDPOINT = 'https://ssl.google-analytics.com/batch'
_GA_TID = 'UA-36037335-2'
_GA_TID_TESTING = 'UA-36037335-13'
_GA_INSTALLS_CATEGORY = 'Installs'
_GA_COMMANDS_CATEGORY = 'Commands'
_GA_HELP_CATEGORY = 'Help'
_GA_ERROR_CATEGORY = 'Error'
_GA_EXECUTIONS_CATEGORY = 'Executions'
_GA_TEST_EXECUTIONS_CATEGORY = 'TestExecutions'

_CSI_ENDPOINT = 'https://csi.gstatic.com/csi'
_CSI_ID = 'cloud_sdk'
_CSI_LOAD_EVENT = 'load'
_CSI_RUN_EVENT = 'run'
_CSI_TOTAL_EVENT = 'total'
_CSI_REMOTE_EVENT = 'remote'
_CSI_LOCAL_EVENT = 'local'
_START_EVENT = 'start'

_CLEARCUT_ENDPOINT = 'https://play.googleapis.com/log'
_CLEARCUT_EVENT_METADATA_KEY = 'event_metadata'
_CLEARCUT_ERROR_TYPE_KEY = 'error_type'


class _GAEvent(object):

  def __init__(self, category, action, label, value, **kwargs):
    self.category = category
    self.action = action
    self.label = label
    self.value = value
    self.custom_dimensions = kwargs


def _GetTimeMillis(time_secs=None):
  return int(round((time_secs or time.time()) * 1000))


class _TimedEvent(object):

  def __init__(self, name, time_millis):
    self.name = name
    self.time_millis = time_millis


class _CommandTimer(object):
  """A class for timing the execution of a command."""

  def __init__(self):
    self.__start = 0
    self.__events = []
    self.__total_rpc_duration = 0
    self.__total_local_duration = 0
    self.__category = 'unknown'
    self.__action = 'unknown'
    self.__label = None
    self.__flag_names = None

  def SetContext(self, category, action, label, flag_names):
    self.__category = category
    self.__action = action
    self.__label = label
    self.__flag_names = flag_names

  def GetAction(self):
    return self.__action

  def Event(self, name, event_time=None):
    time_millis = _GetTimeMillis(event_time)

    if name is _START_EVENT:
      self.__start = time_millis
      return

    self.__events.append(_TimedEvent(name, time_millis))

    if name is _CSI_TOTAL_EVENT:
      self.__total_local_duration = time_millis - self.__start
      self.__total_local_duration -= self.__total_rpc_duration

  def AddRPCDuration(self, duration_in_ms):
    self.__total_rpc_duration += duration_in_ms

  def _GetCSIAction(self):
    csi_action = '{0},{1}'.format(self.__category, self.__action)
    if self.__label:
      csi_action = '{0},{1}'.format(csi_action, self.__label)
    csi_action = csi_action.replace('.', ',').replace('-', '_')
    return csi_action

  def GetCSIParams(self):
    """Gets the fields to send in the CSI beacon."""
    params = [('action', self._GetCSIAction())]

    if self.__flag_names is not None:
      params.append(('flag_names', self.__flag_names))

    response_times = [
        '{0}.{1}'.format(event.name, event.time_millis - self.__start)
        for event in self.__events]
    params.append(('rt', ','.join(response_times)))

    interval_times = [
        '{0}.{1}'.format(_CSI_REMOTE_EVENT, self.__total_rpc_duration),
        '{0}.{1}'.format(_CSI_LOCAL_EVENT, self.__total_local_duration),
    ]
    params.append(('it', ','.join(interval_times)))

    return params

  def GetGATimingsParams(self):
    """Gets the GA timings params corresponding to all the timed events."""
    ga_timings_params = []

    event_params = [('utc', self.__category), ('utl', self.__action)]
    if self.__flag_names is not None:
      event_params.append(('cd6', self.__flag_names))

    for event in self.__events:
      timing_params = [('utv', event.name),
                       ('utt', event.time_millis - self.__start)]
      timing_params.extend(event_params)
      ga_timings_params.append(timing_params)

    ga_timings_params.append(
        [('utv', _CSI_REMOTE_EVENT),
         ('utt', self.__total_rpc_duration)] + event_params)

    ga_timings_params.append(
        [('utv', _CSI_LOCAL_EVENT),
         ('utt', self.__total_local_duration)] + event_params)

    return ga_timings_params

  def GetClearcutParams(self):
    """Gets the clearcut params corresponding to all the timed events."""
    event_latency_ms = self.__total_local_duration + self.__total_rpc_duration

    sub_event_latency_ms = [
        {'key': event.name, 'latency_ms': event.time_millis - self.__start}
        for event in self.__events
    ]
    sub_event_latency_ms.append({
        'key': _CSI_LOCAL_EVENT, 'latency_ms': self.__total_local_duration
    })
    sub_event_latency_ms.append({
        'key': _CSI_REMOTE_EVENT, 'latency_ms': self.__total_rpc_duration
    })

    return event_latency_ms, sub_event_latency_ms


class _MetricsCollector(object):
  """A singleton class to handle metrics reporting."""

  _disabled_cache = None
  _instance = None
  test_group = None

  @staticmethod
  def GetCollectorIfExists():
    return _MetricsCollector._instance

  @staticmethod
  def GetCollector():
    """Returns the singleton _MetricsCollector instance or None if disabled."""
    if _MetricsCollector._IsDisabled():
      return None

    if not _MetricsCollector._instance:
      _MetricsCollector._instance = _MetricsCollector()
    return _MetricsCollector._instance

  @staticmethod
  def ResetCollectorInstance(disable_cache=None, ga_tid=_GA_TID):
    """Reset the singleton _MetricsCollector and reinitialize it.

    This should only be used for tests, where we want to collect some metrics
    but not others, and we have to reinitialize the collector with a different
    Google Analytics tracking id.

    Args:
      disable_cache: Metrics collector keeps an internal cache of the disabled
          state of metrics. This controls the value to reinitialize the cache.
          None means we will refresh the cache with the default values.
          True/False forces a specific value.
      ga_tid: The Google Analytics tracking ID to use for metrics collection.
          Defaults to _GA_TID.
    """
    _MetricsCollector._disabled_cache = disable_cache
    if _MetricsCollector._IsDisabled():
      _MetricsCollector._instance = None
    else:
      _MetricsCollector._instance = _MetricsCollector(ga_tid)

  @staticmethod
  def _IsDisabled():
    """Returns whether metrics collection should be disabled."""
    if _MetricsCollector._disabled_cache is None:
      # Don't collect metrics for completions.
      if '_ARGCOMPLETE' in os.environ:
        _MetricsCollector._disabled_cache = True
      else:
        # Don't collect metrics if the user has opted out.
        disabled = properties.VALUES.core.disable_usage_reporting.GetBool()
        if disabled is None:
          # There is no preference set, fall back to the installation default.
          disabled = config.INSTALLATION_CONFIG.disable_usage_reporting
        _MetricsCollector._disabled_cache = disabled
    return _MetricsCollector._disabled_cache

  def __init__(self, ga_tid=_GA_TID):
    """Initialize a new MetricsCollector.

    This should only be invoked through the static GetCollector() function or
    the static ResetCollectorInstance() function.

    Args:
      ga_tid: The Google Analytics tracking ID to use for metrics collection.
              Defaults to _GA_TID.
    """
    current_platform = platforms.Platform.Current()
    self._user_agent = _MetricsCollector._GetUserAgent(current_platform)
    self._async_popen_args = current_platform.AsyncPopenArgs()
    self._project_ids = {}

    hostname = socket.gethostname()
    install_type = 'Google' if hostname.endswith('.google.com') else 'External'
    cid = _MetricsCollector._GetCID()

    # Table of common params to send to both GA and CSI.
    # First column is GA name, second column is CSI name, third is the value.
    common_params = [
        ('cd1', 'release_channel', config.INSTALLATION_CONFIG.release_channel),
        ('cd2', 'install_type', install_type),
        ('cd3', 'environment', properties.GetMetricsEnvironment()),
        ('cd4', 'interactive', console_io.IsInteractive(error=True,
                                                        heuristic=True)),
        ('cd5', 'python_version', platform.python_version()),
        # cd6 passed as argument to _GAEvent - cd6 = Flag Names
        ('cd7', 'environment_version',
         properties.VALUES.metrics.environment_version.Get()),
        # cd8 passed as argument to _GAEvent - cd8 = Error
        # cd9 passed as argument to _GAEvent - cd9 = Error Extra Info
        ('cd12', 'from_script', console_io.IsRunFromShellScript()),
        ('cd13', 'term',
         console_attr.GetConsoleAttr().GetTermIdentifier()),
    ]

    self._ga_event_params = [
        ('v', '1'),
        ('tid', ga_tid),
        ('cid', cid),
        ('t', 'event')]
    self._ga_event_params.extend(
        [(param[0], param[2]) for param in common_params])
    self._ga_events = []

    self._ga_timing_params = [
        ('v', '1'),
        ('tid', ga_tid),
        ('cid', cid),
        ('t', 'timing')]
    self._ga_timing_params.extend(
        [(param[0], param[2]) for param in common_params])

    cloud_sdk_version = config.CLOUD_SDK_VERSION
    self._csi_params = [('s', _CSI_ID),
                        ('v', '2'),
                        ('rls', cloud_sdk_version),
                        ('c', cid)]
    self._csi_params.extend([(param[1], param[2]) for param in common_params])
    self._timer = _CommandTimer()

    self._clearcut_request_params = {
        'client_info': {
            'client_type': 'DESKTOP',
            'desktop_client_info': {
                'os': current_platform.operating_system.id
            }
        },
        'log_source_name': 'CONCORD',
        'zwieback_cookie': cid,
    }
    self._clearcut_concord_event_params = {
        'release_version': cloud_sdk_version,
        'console_type': 'CloudSDK',
    }
    self._clearcut_concord_event_metadata = [
        {'key': param[1], 'value': str(param[2])} for param in common_params]
    self._clearcut_concord_timed_events = []

    self._metrics = []

    # Tracking the level so we can only report metrics for the top level action
    # (and not other actions executed within an action). Zero is the top level.
    self._action_level = 0

    log.debug('Metrics collector initialized...')

  @staticmethod
  def _GetCID():
    """Gets the client id from the config file, or generates a new one.

    Returns:
      str, The hex string of the client id.
    """
    uuid_path = config.Paths().analytics_cid_path
    cid = None
    if os.path.exists(uuid_path):
      cid = files.ReadFileContents(uuid_path)
      if cid:
        return cid

    cid = uuid.uuid4().hex  # A random UUID
    files.MakeDir(os.path.dirname(uuid_path))
    files.WriteFileContents(uuid_path, cid)
    return cid

  @staticmethod
  def _GetUserAgent(current_platform=None):
    """Constructs a user agent string from config and platform fragments.

    Args:
      current_platform: Optional platforms.Platform for pulling
        platform-specific user agent details.

    Returns:
      str, The user agent for the current client.
    """
    current_platform = current_platform or platforms.Platform.Current()

    return 'CloudSDK/{version} {fragment}'.format(
        version=config.CLOUD_SDK_VERSION,
        fragment=current_platform.UserAgentFragment())

  def IncrementActionLevel(self):
    self._action_level += 1

  def DecrementActionLevel(self):
    self._action_level -= 1

  def RecordTimedEvent(self, name, record_only_on_top_level=False,
                       event_time=None):
    """Records the time when a particular event happened.

    Args:
      name: str, Name of the event.
      record_only_on_top_level: bool, Whether to record only on top level.
      event_time: float, Time when the event happened in secs since epoch.
    """
    if self._action_level == 0 or not record_only_on_top_level:
      self._timer.Event(name, event_time=event_time)

  def RecordRPCDuration(self, duration_in_ms):
    """Records the time when a particular event happened.

    Args:
      duration_in_ms: int, Duration of the RPC in milli seconds.
    """
    self._timer.AddRPCDuration(duration_in_ms)

  def SetTimerContext(self, category, action, label=None, flag_names=None):
    """Sets the context for which the timer is collecting timed events.

    Args:
      category: str, Category of the action being timed.
      action: str, Name of the action being timed.
      label: str, Additional information about the action being timed.
      flag_names: str, Comma separated list of flag names used with the action.
    """
    # We only want to time top level commands
    if category is _GA_COMMANDS_CATEGORY and self._action_level != 0:
      return

    # We want to report error times against the top level action
    if category is _GA_ERROR_CATEGORY and self._action_level != 0:
      action = self._timer.GetAction()

    self._timer.SetContext(category, action, label, flag_names)

  def CollectCSIMetric(self):
    """Adds metric with latencies for the given command to the metrics queue."""
    params = self._timer.GetCSIParams()
    params.extend(self._csi_params)
    data = six.moves.urllib.parse.urlencode(params)

    headers = {'user-agent': self._user_agent}
    self.CollectHTTPBeacon('{0}?{1}'.format(_CSI_ENDPOINT, data),
                           'GET', None, headers)

  def RecordGAEvent(self, event):
    """Adds the given GA event to the metrics queue.

    Args:
      event: _Event, The event to process.
    """
    params = [
        ('ec', event.category),
        ('ea', event.action),
        ('el', event.label),
        ('ev', event.value),
    ]
    custom_dimensions = [
        (k, v) for k, v in six.iteritems(event.custom_dimensions)
        if v is not None]
    params.extend(sorted(custom_dimensions))
    params.extend(self._ga_event_params)
    data = six.moves.urllib.parse.urlencode(params)
    self._ga_events.append(data)

  def CollectGAMetric(self):
    ga_timings = []
    for timing_params in self._timer.GetGATimingsParams():
      timing_params.extend(self._ga_timing_params)
      timing_data = six.moves.urllib.parse.urlencode(timing_params)
      ga_timings.append(timing_data)

    data = '\n'.join(self._ga_events + ga_timings)
    headers = {'user-agent': self._user_agent}
    self.CollectHTTPBeacon(_GA_ENDPOINT, 'POST', data, headers)

  def RecordClearcutEvent(self, event_type, event_name, event_metadata):
    concord_event = dict(self._clearcut_concord_event_params)
    concord_event['event_type'] = event_type
    concord_event['event_name'] = event_name
    concord_event[_CLEARCUT_EVENT_METADATA_KEY] = list(
        self._clearcut_concord_event_metadata)
    concord_event[_CLEARCUT_EVENT_METADATA_KEY].extend(event_metadata)  # pytype: disable=attribute-error
    self._clearcut_concord_timed_events.append((concord_event,
                                                _GetTimeMillis()))

  def CollectClearcutMetric(self):
    """Collect the required clearcut HTTP beacon."""
    clearcut_request = dict(self._clearcut_request_params)
    clearcut_request['request_time_ms'] = _GetTimeMillis()

    event_latency, sub_event_latencies = self._timer.GetClearcutParams()
    command_latency_set = False
    for concord_event, _ in self._clearcut_concord_timed_events:
      if (concord_event['event_type'] is _GA_COMMANDS_CATEGORY and
          command_latency_set):
        continue
      concord_event['latency_ms'] = event_latency
      concord_event['sub_event_latency_ms'] = sub_event_latencies
      command_latency_set = concord_event['event_type'] is _GA_COMMANDS_CATEGORY

    clearcut_request['log_event'] = []
    for concord_event, event_time_ms in self._clearcut_concord_timed_events:
      clearcut_request['log_event'].append({
          'source_extension_json': json.dumps(concord_event, sort_keys=True),
          'event_time_ms': event_time_ms
      })

    data = json.dumps(clearcut_request, sort_keys=True)
    headers = {'user-agent': self._user_agent}
    self.CollectHTTPBeacon(_CLEARCUT_ENDPOINT, 'POST', data, headers)

  def CollectHTTPBeacon(self, url, method, body, headers):
    """Record a custom event to an arbitrary endpoint.

    Args:
      url: str, The full url of the endpoint to hit.
      method: str, The HTTP method to issue.
      body: str, The body to send with the request.
      headers: {str: str}, A map of headers to values to include in the request.
    """
    self._metrics.append((url, method, body, headers))

  def ReportMetrics(self, wait_for_report=False):
    """Reports the collected metrics using a separate async process."""
    if not self._metrics:
      return

    temp_metrics_file = tempfile.NamedTemporaryFile(delete=False)
    with temp_metrics_file:
      pickle.dump(self._metrics, temp_metrics_file)
      self._metrics = []

    this_file = encoding.Decode(__file__)
    reporting_script_path = os.path.realpath(
        os.path.join(os.path.dirname(this_file), 'metrics_reporter.py'))
    execution_args = execution_utils.ArgsForPythonTool(
        reporting_script_path, temp_metrics_file.name)
    # On Python 2.x on Windows, the first arg can't be unicode. We encode
    # encode it anyway because there is really nothing else we can do if
    # that happens.
    # https://bugs.python.org/issue19264
    execution_args = [encoding.Encode(a) for a in execution_args]

    exec_env = os.environ.copy()
    encoding.SetEncodedValue(exec_env, 'PYTHONPATH', os.pathsep.join(sys.path))

    try:
      p = subprocess.Popen(execution_args, env=exec_env,
                           **self._async_popen_args)
      log.debug('Metrics reporting process started...')
    except OSError:
      # This can happen specifically if the Python executable moves between the
      # start of this process and now.
      log.debug('Metrics reporting process failed to start.')
    if wait_for_report:
      # NOTE: p.wait() can cause a deadlock. p.communicate() is recommended.
      # See python docs for more information.
      p.communicate()
      log.debug('Metrics reporting process finished.')


def _RecordEventAndSetTimerContext(
    category, action, label, value=0, flag_names=None,
    error=None, error_extra_info_json=None):
  """Common code for processing a GA event."""
  collector = _MetricsCollector.GetCollector()

  if collector:
    # Override label for tests. This way we can filter by test group.
    if _MetricsCollector.test_group and category is not _GA_ERROR_CATEGORY:
      label = _MetricsCollector.test_group

    cds = {}
    event_metadata = []
    if flag_names is not None:
      cds['cd6'] = flag_names
      event_metadata.append({'key': 'flag_names', 'value': str(flag_names)})
    if error is not None:
      cds['cd8'] = error
      event_metadata.append({'key': _CLEARCUT_ERROR_TYPE_KEY, 'value': error})
    if error_extra_info_json is not None:
      cds['cd9'] = error_extra_info_json
      event_metadata.append({'key': 'extra_error_info',
                             'value': error_extra_info_json})

    collector.RecordGAEvent(
        _GAEvent(category=category, action=action, label=label, value=value,
                 **cds))

    if category is _GA_EXECUTIONS_CATEGORY:
      event_metadata.append({'key': 'binary_version', 'value': label})
    elif category is _GA_HELP_CATEGORY:
      event_metadata.append({'key': 'help_mode', 'value': label})
    elif category is _GA_ERROR_CATEGORY:
      event_metadata.append({'key': _CLEARCUT_ERROR_TYPE_KEY, 'value': label})
    elif category is _GA_INSTALLS_CATEGORY:
      event_metadata.append({'key': 'component_version', 'value': label})
    collector.RecordClearcutEvent(
        event_type=category, event_name=action, event_metadata=event_metadata)

    # Don't include version. We already send it as the rls CSI parameter.
    if category in [_GA_COMMANDS_CATEGORY, _GA_EXECUTIONS_CATEGORY]:
      collector.SetTimerContext(category, action, flag_names=flag_names)
    elif category in [_GA_ERROR_CATEGORY, _GA_HELP_CATEGORY,
                      _GA_TEST_EXECUTIONS_CATEGORY]:
      collector.SetTimerContext(category, action, label, flag_names=flag_names)
    # Ignoring installs for now since there could be multiple per cmd execution.


def _GetFlagNameString(flag_names):
  if flag_names is None:
    # We have no information on the flags that were used.
    return ''
  if not flag_names:
    # We explicitly know that no flags were used.
    return '==NONE=='
  # One or more flags were used.
  return ','.join(sorted(flag_names))


def CaptureAndLogException(func):
  """Function decorator to capture and log any exceptions."""
  def Wrapper(*args, **kwds):
    try:
      return func(*args, **kwds)
    # pylint:disable=bare-except
    except:
      log.debug('Exception captured in %s', func.__name__, exc_info=True)
  return Wrapper


def StartTestMetrics(test_group_id, test_method):
  _MetricsCollector.ResetCollectorInstance(False, _GA_TID_TESTING)
  _MetricsCollector.test_group = test_group_id
  _RecordEventAndSetTimerContext(
      _GA_TEST_EXECUTIONS_CATEGORY,
      test_method,
      test_group_id,
      value=0)


def StopTestMetrics():
  collector = _MetricsCollector.GetCollectorIfExists()
  if collector:
    collector.ReportMetrics(wait_for_report=True)
  _MetricsCollector.test_group = None
  _MetricsCollector.ResetCollectorInstance(True)


def GetCIDIfMetricsEnabled():
  """Gets the client id if metrics collection is enabled.

  Returns:
    str, The hex string of the client id if metrics is enabled, else an empty
    string.
  """
  # pylint: disable=protected-access
  if _MetricsCollector._IsDisabled():
    # We directly set an environment variable with the return value of this
    # function, and so return the empty string rather than None.
    return ''
  return _MetricsCollector._GetCID()
  # pylint: enable=protected-access


def GetUserAgentIfMetricsEnabled():
  """Gets the user agent if metrics collection is enabled.

  Returns:
    The complete user agent string if metrics is enabled, else None.
  """
  # pylint: disable=protected-access
  if not _MetricsCollector._IsDisabled():
    return _MetricsCollector._GetUserAgent()
  return None
  # pylint: enable=protected-access


@CaptureAndLogException
@atexit.register
def Shutdown():
  """Reports the metrics that were collected."""
  collector = _MetricsCollector.GetCollectorIfExists()
  if collector:
    collector.RecordTimedEvent(_CSI_TOTAL_EVENT)
    collector.CollectCSIMetric()
    collector.CollectGAMetric()
    collector.CollectClearcutMetric()
    collector.ReportMetrics()


def _GetExceptionName(error):
  """Gets a friendly exception name for the given error.

  Args:
    error: An exception class.

  Returns:
    str, The name of the exception to log.
  """
  if error:
    try:
      return '{0}.{1}'.format(error.__module__, error.__name__)
    # pylint:disable=bare-except, Never want to fail on metrics reporting.
    except:
      return 'unknown'
  return None


def _GetErrorExtraInfo(error_extra_info):
  """Serializes the extra info into a json string for logging.

  Args:
    error_extra_info: {str: json-serializable}, A json serializable dict of
      extra info that we want to log with the error. This enables us to write
      queries that can understand the keys and values in this dict.

  Returns:
    str, The value to pass to GA or None.
  """
  if error_extra_info:
    return json.dumps(error_extra_info, sort_keys=True)
  return None


@CaptureAndLogException
def Installs(component_id, version_string):
  """Logs that an SDK component was installed.

  Args:
    component_id: str, The component id that was installed.
    version_string: str, The version of the component.
  """
  _RecordEventAndSetTimerContext(
      _GA_INSTALLS_CATEGORY, component_id, version_string)


@CaptureAndLogException
def Commands(command_path, version_string='unknown', flag_names=None,
             error=None, error_extra_info=None):
  """Logs that a gcloud command was run.

  Args:
    command_path: [str], The '.' separated name of the calliope command.
    version_string: [str], The version of the command.
    flag_names: [str], The names of the flags that were used during this
      execution.
    error: class, The class (not the instance) of the Exception if a user
      tried to run a command that produced an error.
    error_extra_info: {str: json-serializable}, A json serializable dict of
      extra info that we want to log with the error. This enables us to write
      queries that can understand the keys and values in this dict.
  """
  _RecordEventAndSetTimerContext(
      _GA_COMMANDS_CATEGORY, command_path, version_string,
      flag_names=_GetFlagNameString(flag_names),
      error=_GetExceptionName(error),
      error_extra_info_json=_GetErrorExtraInfo(error_extra_info))


@CaptureAndLogException
def Help(command_path, mode):
  """Logs that help for a gcloud command was run.

  Args:
    command_path: str, The '.' separated name of the calliope command.
    mode: str, The way help was invoked (-h, --help, help).
  """
  _RecordEventAndSetTimerContext(_GA_HELP_CATEGORY, command_path, mode)


@CaptureAndLogException
def Error(command_path, error, flag_names=None, error_extra_info=None):
  """Logs that a top level Exception was caught for a gcloud command.

  Args:
    command_path: str, The '.' separated name of the calliope command.
    error: class, The class (not the instance) of the exception that was
      caught.
    flag_names: [str], The names of the flags that were used during this
      execution.
    error_extra_info: {str: json-serializable}, A json serializable dict of
      extra info that we want to log with the error. This enables us to write
      queries that can understand the keys and values in this dict.
  """
  _RecordEventAndSetTimerContext(
      _GA_ERROR_CATEGORY, command_path, _GetExceptionName(error),
      flag_names=_GetFlagNameString(flag_names),
      error_extra_info_json=_GetErrorExtraInfo(error_extra_info))


@CaptureAndLogException
def Executions(command_name, version_string='unknown'):
  """Logs that a top level SDK script was run.

  Args:
    command_name: str, The script name.
    version_string: str, The version of the command.
  """
  _RecordEventAndSetTimerContext(
      _GA_EXECUTIONS_CATEGORY, command_name, version_string)


@CaptureAndLogException
def Started(start_time):
  """Record the time when the command was started.

  Args:
    start_time: float, The start time in seconds since epoch.
  """
  collector = _MetricsCollector.GetCollector()
  if collector:
    collector.RecordTimedEvent(name=_START_EVENT,
                               record_only_on_top_level=True,
                               event_time=start_time)


@CaptureAndLogException
def Loaded():
  """Record the time when command loading was completed."""
  collector = _MetricsCollector.GetCollector()
  if collector:
    collector.RecordTimedEvent(name=_CSI_LOAD_EVENT,
                               record_only_on_top_level=True)
    collector.IncrementActionLevel()


@CaptureAndLogException
def Ran():
  """Record the time when command running was completed."""
  collector = _MetricsCollector.GetCollector()
  if collector:
    collector.DecrementActionLevel()
    collector.RecordTimedEvent(name=_CSI_RUN_EVENT,
                               record_only_on_top_level=True)


@CaptureAndLogException
def CustomTimedEvent(event_name):
  """Record the time when a custom event was completed.

  Args:
    event_name: The name of the event. This must match the pattern
      "[a-zA-Z0-9_]+".
  """
  collector = _MetricsCollector.GetCollector()
  if collector:
    collector.RecordTimedEvent(event_name)


@contextlib.contextmanager
def RecordDuration(span_name):
  """Record duration of a span of time.

  Two timestamps will be sent, and the duration in between will be considered as
  the client side latency of this span.

  Args:
    span_name: str, The name of the span to time.

  Yields:
    None
  """
  CustomTimedEvent(span_name + '_start')
  yield
  CustomTimedEvent(span_name)


@CaptureAndLogException
def RPCDuration(duration_in_secs):
  """Record the time taken to perform an RPC.

  Args:
    duration_in_secs: float, The duration of the RPC in seconds.
  """
  collector = _MetricsCollector.GetCollector()
  if collector:
    collector.RecordRPCDuration(_GetTimeMillis(duration_in_secs))


@CaptureAndLogException
def CustomBeacon(url, method, body, headers):
  """Record a custom event to an arbitrary endpoint.

  Args:
    url: str, The full url of the endpoint to hit.
    method: str, The HTTP method to issue.
    body: str, The body to send with the request.
    headers: {str: str}, A map of headers to values to include in the request.
  """
  collector = _MetricsCollector.GetCollector()
  if collector:
    collector.CollectHTTPBeacon(url, method, body, headers)
