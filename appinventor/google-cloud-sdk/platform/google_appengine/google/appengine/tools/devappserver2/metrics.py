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
"""Provides a logger for logging devappserver2 metrics to Google Analytics.

The MetricsLogger is a singleton class which can be used directly in
devappserver2 scripts or via the few module level helper functions provided
within.

Sample usage in devappserver2:

### In devappserver2.py:

from  google.appengine.tools.devappserver2 import metrics

# When dev_appserver starts, one request is logged to Google Analytics:
metrics_logger = metrics.GetMetricsLogger()
metrics_logger.Start('GoogleAnalyticsClientId', 'UserAgent', {'python27', 'go'})
...
# When dev_appserver stops, a batch request is logged with deferred events.
metrics_logger.Stop()


### In any other devappserver2 libraries:

from  google.appengine.tools.devappserver2 import metrics

# Logging an event immediately:
metrics.GetMetricsLogger().Log('event_category', 'event_action')

# Deferred logging of unique events. These will be logged in batch when
# MetricsLogger.Stop is called. Duplicate events will only be logged once.
metrics.GetMetricsLogger().LogOnceAtStop('event_category', 'event_action')
"""

import datetime
import functools
import httplib
import json
import logging
import os
import platform
import sys
import urllib
from google.pyglib import singleton
from google.appengine.tools import sdk_update_checker
from google.appengine.tools.devappserver2 import constants


# Google Analytics Config
_GOOGLE_ANALYTICS_HTTPS_HOST = 'www.google-analytics.com'
_GOOGLE_ANALYTICS_COLLECT_ENDPOINT = '/collect'
_GOOGLE_ANALYTICS_BATCH_ENDPOINT = '/batch'
_GOOGLE_ANALYTICS_VERSION = 1
_GOOGLE_ANALYTICS_TRACKING_ID = 'UA-84862943-2'
_GOOGLE_ANALYTICS_EVENT_TYPE = 'event'

# Devappserver Google Analytics Event Categories
API_STUB_USAGE_CATEGORY = 'api_stub_usage'
DEVAPPSERVER_CATEGORY = 'devappserver'
ADMIN_CONSOLE_CATEGORY = 'admin-console'
DEVAPPSERVER_SERVICE_CATEGORY = 'devappserver-service'
API_SERVER_CATEGORY = 'apiserver'

# Devappserver Google Analytics Event Actions
API_STUB_USAGE_ACTION_TEMPLATE = 'use-%s'
ERROR_ACTION = 'error'
STOP_ACTION = 'stop'
START_ACTION = 'start'
FILE_CHANGE_ACTION = 'file_change'

# Devappserver Google Analytics Event Labels
AVERAGE_TIME_LABEL = 'average_time'
WATCHER_TYPE_LABEL = 'watcher_type'

# Devappserver Google Analytics Custom Dimensions.
# This maps the custom dimension name in Devappserver to the enumerated cd#
# parameter to be sent with HTTP requests. More details in measurement protocol:
# https://developers.google.com/analytics/devguides/collection/protocol
GOOGLE_ANALYTICS_DIMENSIONS = {
    'IsInteractive': 'cd1',
    'Runtimes': 'cd2',
    'SdkVersion': 'cd3',
    'PythonVersion': 'cd4',
    'AppEngineEnvironment': 'cd5',
    'FileWatcherType': 'cd6',
    'IsDevShell': 'cd7',
    'Platform': 'cd8',
    'Is64Bits': 'cd9',
    'SupportDatastoreEmulator': 'cd10',
    'DatastoreDataType': 'cd11',
    'UseSsl': 'cd12',
    'CmdArgs': 'cd13',
    'MultiModule': 'cd14',
    'DispatchConfig': 'cd15',
    'GRPCImportReport': 'cd16',
    'JavaMajorVersion': 'cd17',
}

# Devappserver Google Analytics Custom Metrics.
GOOGLE_ANALYTICS_METRICS = {
    'FileChangeDetectionAverageTime': 'cm1',
    'FileChangeEventCount': 'cm2'
}


class _MetricsLogger(object):
  """Logs metrics for the devappserver to Google Analytics."""

  def __init__(self):
    """Initializes a _MetricsLogger."""
    # Attributes that will be set later and sent with logging HTTP requests.
    self._client_id = None
    self._user_agent = None
    self._runtimes = None
    self._start_time = None
    self._environment = None

    # self._python_version is in the form: major.minor.macro.
    self._python_version = '.'.join(map(str, sys.version_info[:3]))
    self._sdk_version = (
        sdk_update_checker.GetVersionObject() or {}).get('release')
    self._is_dev_shell = constants.DEVSHELL_ENV in os.environ
    self._is_64_bits = sys.maxsize > 2**32
    self._platform = platform.platform()
    self._support_datastore_emulator = None
    self._datastore_data_type = None
    self._use_ssl = False
    self._cmd_args = None
    self._multi_module = None
    self._dispatch_config = None
    self._category = None
    self._grpc_import_report = None
    self._java_major_version = None

    # Stores events for batch logging once Stop has been called.
    self._log_once_on_stop_events = {}

  def Start(self, client_id, user_agent=None, runtimes=None, environment=None,
            support_datastore_emulator=None, datastore_data_type=None,
            use_ssl=False, cmd_args=None, multi_module=None,
            dispatch_config=None, category=DEVAPPSERVER_CATEGORY,
            grpc_import_report=None, java_major_version=None):
    """Starts a Google Analytics session for the current client.

    Args:
      client_id: A string Client ID representing a unique anonyized user.
      user_agent: A string user agent to send with each log.
      runtimes: A set of strings containing the runtimes used.
      environment: A set of strings containing the environments used.
      support_datastore_emulator: A boolean indicating whether dev_appserver
        supports Cloud Datastore emulator.
      datastore_data_type: A string representing the type of data for local
        datastore file.
      use_ssl: A boolean indicating whether SSL was enabled.
      cmd_args: An argparse.Namespace object representing commandline arguments
        passed to dev_appserver.
      multi_module: True if we have more than one module
      dispatch_config: True if we're using dispatch.yaml
      category: A string representing Google Analytics Event Categories.
      grpc_import_report: A dict reporting result of grpc import attempt.
      java_major_version: An integer representing java major version.
    """
    self._client_id = client_id
    self._user_agent = user_agent
    self._runtimes = ','.join(sorted(list(runtimes))) if runtimes else None
    self._environment = ','.join(
        sorted(list(environment))) if environment else None
    self._support_datastore_emulator = support_datastore_emulator
    self._datastore_data_type = datastore_data_type
    self._use_ssl = use_ssl
    self._cmd_args = json.dumps(vars(cmd_args)) if cmd_args else None
    self._multi_module = multi_module
    self._dispatch_config = dispatch_config
    self._category = category
    self._grpc_import_report = repr(grpc_import_report)
    self._java_major_version = java_major_version
    self.Log(DEVAPPSERVER_CATEGORY, START_ACTION)
    self._start_time = Now()

  def Stop(self, **kwargs):
    """Ends a Google Analytics session for the current client.

    A request to Stop the session is only made if the Start function has
    executed to set self._start_time.

    Args:
      **kwargs: Additional Google Analytics event parameters to include in the
        request body.
    """
    if self._start_time:
      total_run_time = int((Now() - self._start_time).total_seconds())
      self.LogOnceOnStop(
          self._category, STOP_ACTION, value=total_run_time, **kwargs)
      self.LogBatch(self._log_once_on_stop_events.itervalues())

  def Log(self, category, action, label=None, value=None, **kwargs):
    """Logs a single event to Google Analytics via HTTPS.

    Args:
      category: A string to use as the Google Analytics event category.
      action: A string to use as the Google Analytics event action.
      label: A string to use as the Google Analytics event label.
      value: A number to use as the Google Analytics event value.
      **kwargs: Additional Google Analytics event parameters to include in the
        request body.
    """
    self._SendRequestToGoogleAnalytics(
        _GOOGLE_ANALYTICS_COLLECT_ENDPOINT,
        self._EncodeEvent(category, action, label, value, **kwargs))

  def LogBatch(self, events):
    """Logs a batch of events to Google Analytics via HTTPS in a single call.

    Args:
      events: An iterable of event dicts whose keys match the args of the
        _EncodeEvent method.
    """
    events = '\n'.join([self._EncodeEvent(**event) for event in events])
    self._SendRequestToGoogleAnalytics(_GOOGLE_ANALYTICS_BATCH_ENDPOINT, events)

  def LogOnceOnStop(self, category, action, label=None, value=None, **kwargs):
    """Stores unique events for deferred batch logging when Stop is called.

    To prevent duplicate events, the raw request parameters are stored in a hash
    table to be batch logged when the Stop method is called.

    Args:
      category: A string to use as the Google Analytics event category.
      action: A string to use as the Google Analytics event category.
      label: A string to use as the Google Analytics event label.
      value: A number to use as the Google Analytics event value.
      **kwargs: Additional Google Analytics event parameters to include in the
        request body.
    """
    request = {
        'category': category,
        'action': action,
        'label': label,
        'value': value,
    }
    request.update(kwargs)
    self._log_once_on_stop_events[json.dumps(request, sort_keys=True)] = request

  def _SendRequestToGoogleAnalytics(self, endpoint, body):
    """Sends an HTTPS POST request to Google Analytics.

    Args:
      endpoint: The string endpoint path for the request, eg "/collect".
      body: The string body to send with the request.
    """
    if not self._client_id:
      logging.debug('Google Analytics is not configured. '
                    'If it were, we would send %r:', body)
      return

    headers = {'User-Agent': self._user_agent} if self._user_agent else {}

    # If anything goes wrong, we do not want to block the main devappserver
    # execution.
    try:
      httplib.HTTPSConnection(_GOOGLE_ANALYTICS_HTTPS_HOST).request(
          'POST', endpoint, body, headers)
    except:  # pylint: disable=bare-except
      logging.debug(
          'Google Analytics request failed: \n %s', str(sys.exc_info()))

  def _EncodeEvent(self, category, action, label=None, value=None, **kwargs):
    """Encodes a single event for sending to Google Analytics.

    Args:
      category: A string to use as the Google Analytics event category.
      action: A string to use as the Google Analytics event category.
      label: A string to use as the Google Analytics event label.
      value: A number to use as the Google Analytics event value.
      **kwargs: Additional Google Analytics event parameters to include in the
        request body.

    Returns:
      A string of the form "key1=value1&key2=value2&key3=value4" containing
      event data and metadata for use in the body of Google Analytics logging
      requests.
    """
    event = {
        # Event metadata
        'v': _GOOGLE_ANALYTICS_VERSION,
        'tid': _GOOGLE_ANALYTICS_TRACKING_ID,
        't': _GOOGLE_ANALYTICS_EVENT_TYPE,
        'cid': self._client_id,
        GOOGLE_ANALYTICS_DIMENSIONS['IsInteractive']: IsInteractive(),
        GOOGLE_ANALYTICS_DIMENSIONS['Runtimes']: self._runtimes,
        GOOGLE_ANALYTICS_DIMENSIONS['SdkVersion']: self._sdk_version,
        GOOGLE_ANALYTICS_DIMENSIONS['PythonVersion']: self._python_version,
        GOOGLE_ANALYTICS_DIMENSIONS['AppEngineEnvironment']:
            self._environment,
        GOOGLE_ANALYTICS_DIMENSIONS['IsDevShell']: self._is_dev_shell,
        GOOGLE_ANALYTICS_DIMENSIONS['Platform']: self._platform,
        GOOGLE_ANALYTICS_DIMENSIONS['Is64Bits']: self._is_64_bits,
        GOOGLE_ANALYTICS_DIMENSIONS[
            'SupportDatastoreEmulator']: self._support_datastore_emulator,
        GOOGLE_ANALYTICS_DIMENSIONS[
            'DatastoreDataType']: self._datastore_data_type,
        GOOGLE_ANALYTICS_DIMENSIONS['UseSsl']: self._use_ssl,
        GOOGLE_ANALYTICS_DIMENSIONS['CmdArgs']: self._cmd_args,
        GOOGLE_ANALYTICS_DIMENSIONS['MultiModule']: self._multi_module,
        GOOGLE_ANALYTICS_DIMENSIONS['DispatchConfig']: self._dispatch_config,
        GOOGLE_ANALYTICS_DIMENSIONS['GRPCImportReport']: self._grpc_import_report,  # pylint: disable=line-too-long
        GOOGLE_ANALYTICS_DIMENSIONS['JavaMajorVersion']: self._java_major_version,  # pylint: disable=line-too-long
        # Required event data
        'ec': category,
        'ea': action
    }

    # Optional event data
    if label:
      event['el'] = label
    if value:
      event['ev'] = value
    event.update(kwargs)

    return urllib.urlencode(event)


@singleton.Singleton
class MetricsLogger(_MetricsLogger):
  """Singleton MetricsLogger class for logging to Google Analytics."""


# In accordance with Pyglib's Singleton, the first instance is created as
# below, and secondary clients can access the instance via
# MetricsLogger.Singleton(), as in GetMetricsLogger below. We instantiate the
# logger here, so all other uses in devappserver can call GetMetricsLogger.
MetricsLogger()


def GetMetricsLogger():
  """Returns the singleton instance of the MetricsLogger."""
  return MetricsLogger.Singleton()


def GetErrorDetails():
  """Returns a string representation of type and message of an exception."""
  return repr(sys.exc_info()[1])


def IsInteractive():
  """Returns true if the user's session has an interactive stdin."""
  return sys.stdin.isatty()


def Now():
  """Returns a datetime.datetime instance representing the current time.

  This is just a wrapper to ease testing against the datetime module.

  Returns:
    An instance of datetime.datetime.
  """
  return datetime.datetime.now()


class LogHandlerRequest(object):
  """A decorator for logging usage of a webapp2 request handler."""

  def __init__(self, category):
    """Initializes the decorator.

    Args:
      category: The string Google Analytics category for logging requests.
    """
    self._category = category

  def __call__(self, handler_method):
    """Provides a wrapped method for execution.

    Args:
      handler_method: The method that is wrapped by LogHandlerRequest.

    Returns:
      A wrapped handler method.
    """
    @functools.wraps(handler_method)
    def DecoratedHandler(handler_self, *args, **kwargs):
      """Logs the handler_method call and executes the handler_method."""
      GetMetricsLogger().LogOnceOnStop(
          self._category,
          '{class_name}.{method_name}'.format(
              class_name=handler_self.__class__.__name__,
              method_name=handler_method.__name__))
      handler_method(handler_self, *args, **kwargs)

    return DecoratedHandler
