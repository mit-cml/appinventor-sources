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




"""Task Queue API.

Enables an application to queue background work for itself. Work is done through
webhooks that process tasks pushed from a queue, or workers that manually pull
tasks from a queue. In push queues, tasks will execute in best-effort order of
ETA. Webhooks that fail will cause tasks to be retried at a later time. In pull
queues, workers are responsible of leasing tasks for processing and deleting the
tasks when completed. Multiple queues cab exist with independent throttling
controls.

Webhook URLs can be specified directly for push tasks, or the default URL scheme
can be used, which will translate task names into URLs relative to a queue's
base path. A default queue is also provided for simple usage.
"""













__all__ = [

    'BadTaskStateError', 'BadTransactionState', 'BadTransactionStateError',
    'DatastoreError', 'DuplicateTaskNameError', 'Error', 'InternalError',
    'InvalidQueueError', 'InvalidQueueNameError', 'InvalidTaskError',
    'InvalidTaskNameError', 'InvalidUrlError', 'PermissionDeniedError',
    'TaskAlreadyExistsError', 'TaskTooLargeError', 'TombstonedTaskError',
    'TooManyTasksError', 'TransientError', 'UnknownQueueError',
    'InvalidLeaseTimeError', 'InvalidMaxTasksError', 'InvalidDeadlineError',
    'InvalidQueueModeError', 'TransactionalRequestTooLargeError',
    'TaskLeaseExpiredError', 'QueuePausedError', 'InvalidEtaError',
    'InvalidTagError',

    'MAX_QUEUE_NAME_LENGTH', 'MAX_TASK_NAME_LENGTH', 'MAX_TASK_SIZE_BYTES',
    'MAX_PULL_TASK_SIZE_BYTES', 'MAX_PUSH_TASK_SIZE_BYTES',
    'MAX_LEASE_SECONDS', 'MAX_TASKS_PER_ADD',
    'MAX_TASKS_PER_LEASE',
    'MAX_URL_LENGTH',

    'DEFAULT_APP_VERSION',

    'Queue', 'QueueStatistics', 'Task', 'TaskRetryOptions', 'add', 'create_rpc']


import calendar
import cgi
import datetime
import logging
import math
import os
import re
import time
import urllib
import urlparse

from google.appengine.api import apiproxy_stub_map
from google.appengine.api import app_identity
from google.appengine.api import modules
from google.appengine.api import namespace_manager
from google.appengine.api import urlfetch
from google.appengine.api.taskqueue import taskqueue_service_pb
from google.appengine.runtime import apiproxy_errors


class Error(Exception):
  """Base class for exceptions in this module."""


class UnknownQueueError(Error):
  """The queue specified is unknown."""


class TransientError(Error):
  """A transient error occurred while accessing the queue. Try again later."""


class InternalError(Error):
  """An internal error occurred while accessing this queue.

  If the problem continues, contact the App Engine team through the support
  forum. Be sure to include a description of your problem.
  """


class InvalidTaskError(Error):
  """The parameters, headers, or method of the task is invalid."""


class InvalidTaskNameError(InvalidTaskError):
  """The name of the task is invalid."""


class TaskTooLargeError(InvalidTaskError):
  """The task is too large with its headers and payload."""


class TaskAlreadyExistsError(InvalidTaskError):
  """The task already exists. It has not yet run."""


class TombstonedTaskError(InvalidTaskError):
  """The task has been tombstoned.

  A task with the same name was previously executed in the queue; names should
  be unique within a queue.
  """


class InvalidUrlError(InvalidTaskError):
  """The relative URL used for the task is invalid."""


class InvalidEtaError(InvalidTaskError):
  """The task's ETA is invalid."""


class BadTaskStateError(Error):
  """The task is in the wrong state for the requested operation."""


class InvalidQueueError(Error):
  """The queue's configuration is invalid."""


class InvalidQueueNameError(InvalidQueueError):
  """The name of the queue is invalid."""


class _RelativeUrlError(Error):
  """The relative URL supplied is invalid."""


class PermissionDeniedError(Error):
  """The requested operation is not allowed for this app."""


class DuplicateTaskNameError(Error):
  """Two tasks have the same name.

  When adding multiple tasks to a queue in a batch, more than one task cannot
  have the same name.
  """


class TooManyTasksError(Error):
  """Too many tasks were present in a single function call."""


class DatastoreError(Error):
  """There was a datastore error while accessing the queue."""


class BadTransactionStateError(Error):
  """The state of the current transaction does not permit this operation."""


class InvalidTaskRetryOptionsError(Error):
  """The task retry configuration is invalid."""


class InvalidLeaseTimeError(Error):
  """The lease time period is invalid."""


class InvalidMaxTasksError(Error):
  """The requested maximum number of tasks in `lease_tasks` is invalid."""


class InvalidDeadlineError(Error):
  """The requested deadline in `lease_tasks` is invalid."""


class InvalidQueueModeError(Error):
  """Invokes a pull queue operation on a push queue or vice versa."""


class TransactionalRequestTooLargeError(TaskTooLargeError):
  """The total size of this transaction (including tasks) was too large."""


class TaskLeaseExpiredError(Error):
  """The task lease could not be renewed because it had already expired."""


class QueuePausedError(Error):
  """The queue is paused and cannot process modify task lease requests."""


class InvalidTagError(Error):
  """The specified tag is invalid."""


class _DefaultAppVersionSingleton(object):
  def __repr__(self):
    return '<DefaultApplicationVersion>'


class _UnknownAppVersionSingleton(object):
  def __repr__(self):
    return '<UnknownApplicationVersion>'




BadTransactionState = BadTransactionStateError

MAX_QUEUE_NAME_LENGTH = 100

MAX_PULL_TASK_SIZE_BYTES = 2 ** 20

MAX_PUSH_TASK_SIZE_BYTES = 100 * (2 ** 10)

MAX_TASK_NAME_LENGTH = 500

MAX_TASK_SIZE_BYTES = MAX_PUSH_TASK_SIZE_BYTES

MAX_TASKS_PER_ADD = 100

MAX_TRANSACTIONAL_REQUEST_SIZE_BYTES = 2 ** 20


MAX_URL_LENGTH = 2083

MAX_TASKS_PER_LEASE = 1000

MAX_TAG_LENGTH = 500

MAX_LEASE_SECONDS = 3600 * 24 * 7


DEFAULT_APP_VERSION = _DefaultAppVersionSingleton()
_UNKNOWN_APP_VERSION = _UnknownAppVersionSingleton()

_DEFAULT_QUEUE = 'default'

_DEFAULT_QUEUE_PATH = '/_ah/queue'

_MAX_COUNTDOWN_SECONDS = 3600 * 24 * 30

_METHOD_MAP = {
    'GET': taskqueue_service_pb.TaskQueueAddRequest.GET,
    'POST': taskqueue_service_pb.TaskQueueAddRequest.POST,
    'HEAD': taskqueue_service_pb.TaskQueueAddRequest.HEAD,
    'PUT': taskqueue_service_pb.TaskQueueAddRequest.PUT,
    'DELETE': taskqueue_service_pb.TaskQueueAddRequest.DELETE,
}

_NON_POST_HTTP_METHODS = frozenset(['GET', 'HEAD', 'PUT', 'DELETE'])

_BODY_METHODS = frozenset(['POST', 'PUT', 'PULL'])

_TASK_NAME_PATTERN = r'^[a-zA-Z0-9_-]{1,%s}$' % MAX_TASK_NAME_LENGTH

_TASK_NAME_RE = re.compile(_TASK_NAME_PATTERN)

_QUEUE_NAME_PATTERN = r'^[a-zA-Z0-9-]{1,%s}$' % MAX_QUEUE_NAME_LENGTH

_QUEUE_NAME_RE = re.compile(_QUEUE_NAME_PATTERN)

_ERROR_MAPPING = {
    taskqueue_service_pb.TaskQueueServiceError.UNKNOWN_QUEUE: UnknownQueueError,
    taskqueue_service_pb.TaskQueueServiceError.TRANSIENT_ERROR:
        TransientError,
    taskqueue_service_pb.TaskQueueServiceError.INTERNAL_ERROR: InternalError,
    taskqueue_service_pb.TaskQueueServiceError.TASK_TOO_LARGE:
        TaskTooLargeError,
    taskqueue_service_pb.TaskQueueServiceError.INVALID_TASK_NAME:
    InvalidTaskNameError,
        taskqueue_service_pb.TaskQueueServiceError.INVALID_QUEUE_NAME:
    InvalidQueueNameError,
    taskqueue_service_pb.TaskQueueServiceError.INVALID_URL: InvalidUrlError,
    taskqueue_service_pb.TaskQueueServiceError.INVALID_QUEUE_RATE:
        InvalidQueueError,
    taskqueue_service_pb.TaskQueueServiceError.PERMISSION_DENIED:
        PermissionDeniedError,
    taskqueue_service_pb.TaskQueueServiceError.TASK_ALREADY_EXISTS:
        TaskAlreadyExistsError,
    taskqueue_service_pb.TaskQueueServiceError.TOMBSTONED_TASK:
        TombstonedTaskError,
    taskqueue_service_pb.TaskQueueServiceError.INVALID_ETA: InvalidEtaError,
    taskqueue_service_pb.TaskQueueServiceError.INVALID_REQUEST: Error,
    taskqueue_service_pb.TaskQueueServiceError.UNKNOWN_TASK: Error,
    taskqueue_service_pb.TaskQueueServiceError.TOMBSTONED_QUEUE: Error,
    taskqueue_service_pb.TaskQueueServiceError.DUPLICATE_TASK_NAME:
        DuplicateTaskNameError,
    taskqueue_service_pb.TaskQueueServiceError.INVALID_QUEUE_MODE:
        InvalidQueueModeError,

    taskqueue_service_pb.TaskQueueServiceError.TOO_MANY_TASKS:
        TooManyTasksError,
    taskqueue_service_pb.TaskQueueServiceError.TRANSACTIONAL_REQUEST_TOO_LARGE:
        TransactionalRequestTooLargeError,
    taskqueue_service_pb.TaskQueueServiceError.TASK_LEASE_EXPIRED:
        TaskLeaseExpiredError,
    taskqueue_service_pb.TaskQueueServiceError.QUEUE_PAUSED:
        QueuePausedError,
    taskqueue_service_pb.TaskQueueServiceError.INVALID_TAG:
        InvalidTagError,

}







_PRESERVE_ENVIRONMENT_HEADERS = (
    ('X-AppEngine-Default-Namespace', 'HTTP_X_APPENGINE_DEFAULT_NAMESPACE'),)



class _UTCTimeZone(datetime.tzinfo):
  """UTC time zone."""

  ZERO = datetime.timedelta(0)

  def utcoffset(self, dt):
    return self.ZERO

  def dst(self, dt):
    return self.ZERO

  def tzname(self, dt):
    return 'UTC'

  def __repr__(self):
    return '_UTCTimeZone()'


_UTC = _UTCTimeZone()


def _parse_relative_url(relative_url):
  """Parses a relative URL and splits it into its path and query string.

  Args:
      relative_url: The relative URL, starting with a '/'.

  Returns:
      Tuple (path, query) where:
        path: The path in the relative URL.
        query: The query string in the URL without the '?'' character.

  Raises:
     _RelativeUrlError: If the `relative_url` is invalid for any reason.
  """
  if not relative_url:
    raise _RelativeUrlError('The relative URL is empty')
  (scheme, netloc, path, query, fragment) = urlparse.urlsplit(relative_url)
  if scheme or netloc:
    raise _RelativeUrlError('Relative URL cannot have a scheme or location')
  if fragment:
    raise _RelativeUrlError('Relative URL cannot specify a fragment')
  if not path or path[0] != '/':
    raise _RelativeUrlError('The relative URL path must start with "/"')
  return path, query


def _flatten_params(params):
  """Converts a dictionary of parameters to a list of parameters.

  Any unicode strings in keys or values will be encoded as UTF-8.

  Args:
      params: Dictionary mapping parameter keys to values. Values will be
          converted to a string and added to the list as tuple (key, value). If
          a values is iterable and not a string, each contained value will be
          added as a separate (key, value) tuple.

  Returns:
      List of (key, value) tuples.
  """

  def get_string(value):
    if isinstance(value, unicode):
      return unicode(value).encode('utf-8')
    else:




      return str(value)

  param_list = []
  for key, value in params.iteritems():
    key = get_string(key)
    if isinstance(value, basestring):
      param_list.append((key, get_string(value)))
    else:
      try:
        iterator = iter(value)
      except TypeError:
        param_list.append((key, str(value)))
      else:
        param_list.extend((key, get_string(v)) for v in iterator)

  return param_list


def _MakeAsyncCall(method, request, response, get_result_hook=None, rpc=None):
  """Internal helper to schedule an asynchronous RPC.

  Args:
      method: The name of the taskqueue_service method that should be called,
          for example: `BulkAdd`.
      request: The protocol buffer that contains the request argument.
      response: The protocol buffer to be populated with the response.
      get_result_hook: An optional hook function used to process results. See
          `UserRPC.make_call()` for more information.
      rpc: An optional UserRPC object that will be used to make the call.

  Returns:
      A UserRPC object; either the object that was passed in as the RPC
      argument, or a new object if no RPC was passed in.
  """
  if rpc is None:
    rpc = create_rpc()
  assert rpc.service == 'taskqueue', repr(rpc.service)
  rpc.make_call(method, request, response, get_result_hook, None)
  return rpc


def _TranslateError(error, detail=''):
  """Translates a `TaskQueueServiceError` into an exception.

  Args:
      error: Value from TaskQueueServiceError enum.
      detail: A human-readable description of the error.

  Returns:
      The corresponding Exception sub-class for that error code.
  """
  if (error >= taskqueue_service_pb.TaskQueueServiceError.DATASTORE_ERROR
      and isinstance(error, int)):
    from google.appengine.api import datastore
    datastore_exception = datastore._DatastoreExceptionFromErrorCodeAndDetail(
        error - taskqueue_service_pb.TaskQueueServiceError.DATASTORE_ERROR,
        detail)

    class JointException(datastore_exception.__class__, DatastoreError):
      """There was a datastore error while accessing the queue."""
      __msg = (u'taskqueue.DatastoreError caused by: %s %s' %
               (datastore_exception.__class__, detail))

      def __str__(self):
        return JointException.__msg

    return JointException()
  else:
    exception_class = _ERROR_MAPPING.get(error, None)
    if exception_class:
      return exception_class(detail)
    else:
      return Error('Application error %s: %s' % (error, detail))


def _ValidateDeadline(deadline):
  if not isinstance(deadline, (int, long, float)):
    raise TypeError(
        'deadline must be numeric')

  if deadline <= 0:
    raise InvalidDeadlineError(
        'Negative or zero deadline requested')


def create_rpc(deadline=None, callback=None):
  """Creates an RPC object for use with the Task Queue API.

  Args:
    deadline: Optional deadline in seconds for the operation; the default
        value is a system-specific deadline, which is typically 5 seconds. After
        the deadline, a `DeadlineExceededError` error will be returned.
    callback: Optional function to be called with the Task Queue service
        returns results successfully when `get_result()`, `check_success()`,
        or `wait()` is invoked on the RPC object. The function is
        called without arguments. The function is not called in a background
        process or thread; the function is only called when one of the above
        methods is called by the application. The function is called even if
        the request fails or the RPC deadline elapses.

  Returns:
    An `apiproxy_stub_map.UserRPC` object specialized for this service.
  """
  if deadline is not None:
    _ValidateDeadline(deadline)
  return apiproxy_stub_map.UserRPC('taskqueue', deadline, callback)


class TaskRetryOptions(object):
  """The options used to decide when a failed task will be retried.

  Tasks executing in the task queue can fail for many reasons. If a task fails
  to execute, which is indicated by returning any HTTP status code outside of
  the range 200-299, App Engine retries the task until it succeeds. By default,
  the system gradually reduces the retry rate to avoid flooding your application
  with too many requests, but schedules retry attempts to recur at a maximum of
  once per hour until the task succeeds. 503 errors, however, are treated as
  special cases and should not be returned by user code.

  The `TaskRetryOptions` class provides the properties that you can use to
  decide when to retry a failed task at runtime.
  """

  __CONSTRUCTOR_KWARGS = frozenset(
      ['min_backoff_seconds', 'max_backoff_seconds',
       'task_age_limit', 'max_doublings', 'task_retry_limit'])

  def __init__(self, **kwargs):
    """Initializer.

    Args:
      min_backoff_seconds: Optional; the minimum number of seconds to wait
          before retrying a task after it fails.
      max_backoff_seconds: Optional; the maximum number of seconds to wait
          before retrying a task after it fails.
      task_age_limit: Optional; the number of seconds after creation that a
          failed task will no longer be retried. The given value is rounded up
          to the nearest integer. If `task_retry_limit` is also specified, the
          task will be retried until both limits are reached.
      max_doublings: Optional; the maximum number of times that the interval
          between failed task retries will be doubled before the increase
          becomes constant. The constant is:
          `2**(max_doublings - 1) * min_backoff_seconds`.
      task_retry_limit: Optional; the maximum number of times to retry a
          failed task before giving up. In push queues, the counter is
          incremented each time App Engine tries the tasks, up to the
          specified limit. If `task_age_limit` is also specified, the task
          will be retried until both limits are reached.

    Raises:
        InvalidTaskRetryOptionsError: If any of the parameters are invalid.
    """
    args_diff = set(kwargs.iterkeys()) - self.__CONSTRUCTOR_KWARGS
    if args_diff:
      raise TypeError('Invalid arguments: %s' % ', '.join(args_diff))

    self.__min_backoff_seconds = kwargs.get('min_backoff_seconds')
    if (self.__min_backoff_seconds is not None and
        self.__min_backoff_seconds < 0):
      raise InvalidTaskRetryOptionsError(
          'The minimum retry interval cannot be negative')

    self.__max_backoff_seconds = kwargs.get('max_backoff_seconds')
    if (self.__max_backoff_seconds is not None and
        self.__max_backoff_seconds < 0):
      raise InvalidTaskRetryOptionsError(
          'The maximum retry interval cannot be negative')

    if (self.__min_backoff_seconds is not None and
        self.__max_backoff_seconds is not None and
        self.__max_backoff_seconds < self.__min_backoff_seconds):
      raise InvalidTaskRetryOptionsError(
          'The maximum retry interval cannot be less than the '
          'minimum retry interval')

    self.__max_doublings = kwargs.get('max_doublings')
    if self.__max_doublings is not None and self.__max_doublings < 0:
      raise InvalidTaskRetryOptionsError(
          'The maximum number of retry interval doublings cannot be negative')

    self.__task_retry_limit = kwargs.get('task_retry_limit')
    if self.__task_retry_limit is not None and self.__task_retry_limit < 0:
      raise InvalidTaskRetryOptionsError(
          'The maximum number of retries cannot be negative')

    self.__task_age_limit = kwargs.get('task_age_limit')
    if self.__task_age_limit is not None:
      if self.__task_age_limit < 0:
        raise InvalidTaskRetryOptionsError(
            'The expiry countdown cannot be negative')
      self.__task_age_limit = int(math.ceil(self.__task_age_limit))

  @property
  def min_backoff_seconds(self):
    """The minimum number of seconds to wait before retrying a task."""
    return self.__min_backoff_seconds

  @property
  def max_backoff_seconds(self):
    """The maximum number of seconds to wait before retrying a task."""
    return self.__max_backoff_seconds

  @property
  def task_age_limit(self):
    """The number of seconds after which a failed task will not be retried."""
    return self.__task_age_limit

  @property
  def max_doublings(self):
    """The number of times that the retry interval will be doubled."""
    return self.__max_doublings

  @property
  def task_retry_limit(self):
    """The number of times that a failed task will be retried."""
    return self.__task_retry_limit

  def __repr__(self):
    properties = ['%s=%r' % (attr, getattr(self, attr)) for attr in
                  self.__CONSTRUCTOR_KWARGS]
    return 'TaskRetryOptions(%s)' % ', '.join(properties)


class Task(object):
  """Represents a single task on a queue.

  The `Task` class enables an application to queue background work. Work is done
  through webhooks that process tasks pushed from a push queue, or workers that
  manually pull tasks from a pull queue.

  In push queues, most tasks are delivered in best-effort order of ETA.

  Note:
      Occasionally, tasks might be delivered out of order of ETA. However, for
      corner cases, tasks are delivered out of order for an extended period of
      time. You should not rely on tasks being delivered in order, as the
      results aren't always consistent.

  Webhooks that fail cause tasks to be retried at a later time. You can
  configure the rate and number of retries for failed tasks. You can specify
  webhook URLs directly for push tasks. You can also use the default URL scheme,
  which translates task names into URLs that are relative to a queue's base
  path. A default queue is also provided for simple usage.

  In pull queues, workers are responsible for leasing tasks, processing them,
  and deleting them after processing. You can configure the number of task
  retries, which is based on how many times the task has been leased. You can
  define multiple queues with independent throttling controls.

  You set the various properties for a task in the constructor. Once the `Task`
  object is instantiated, you insert that task into a queue. A task instance can
  be inserted into one queue only.
  """


  __CONSTRUCTOR_KWARGS = frozenset([
      'countdown', 'eta', 'headers', 'method', 'name', 'params',
      'retry_options', 'tag', 'target', 'url', '_size_check'])


  __eta_posix = None
  __target = None


  def __init__(self, payload=None, **kwargs):
    """Initializer.

    Args:
      payload: Optional; the payload data for this task. This argument is only
          allowed for `POST` and `PUT` methods and pull tasks. In push queues,
          the payload is delivered to the webhook or backend in the body of an
          HTTP request. In pull queues, the payload is fetched by workers as
          part of the response from `lease_tasks()`.
      name: Optional; the name to give the task. If you do not specify a name,
          a name is auto-generated when added to a queue and assigned to this
          object. The name must match the `_TASK_NAME_PATTERN` regular
          expression. Avoid sequential names, such as counters or timestamps, as
          these names can lead to decreased availability and performance.
      method: Optional; the HTTP method to use when accessing the webhook. The
          default value is `POST`. This argument is not used for pull queues.
      url: Optional; the relative URL where the webhook that should handle
          this task is located for this application. You can include a query
          string in this value unless it is being used in a `POST` method. You
          cannot specify a URL for pull tasks.
      headers: Optional; a dictionary of headers to pass to the webhook. The
          values in the dictionary can be iterable to indicate repeated header
          fields. You cannot specify headers for pull tasks. In a push task,
          if you do not specify a `Content-Type` header, the default value of
          `text/plain` will be used. In a push task, if you specify a `Host`
          header, you cannot use the `target` keyword argument. Any headers that
          use the `X-AppEngine` prefix will also be dropped.
      params: Optional; a dictionary of parameters to use for the task. For
          `POST` requests and PULL tasks, these parameters are encoded as
          `application/x-www-form-urlencoded` and set to the payload. For both
          `POST` and pull requests, you cannot specify parameters if you
          already specified a `payload`. In `PUT` requests, parameters are
          converted to a query string if the URL contains a query string, or if
          the task already has a `payload`. Do not specify parameters if the URL
          contains a query string and the method is `GET`.
      countdown: Optional; time in seconds into the future that this task
          should run or be leased. The default value is zero. Do not specify a
          countdown if you also specified an `eta`, as it sets the ETA to a
          value of now + `countdown`.
      eta: Optional; a `datetime.datetime` specifying the absolute time at
          which the task should be run or leased. The `eta` argument must not
          be specified if `countdown` is specified. This value can be time
          zone-aware or time zone-naive. If the value is set to None, the
          default value is now. For pull tasks, no worker will be able to
          lease this task before the time indicated by the `eta` argument.
      retry_options: Optional; a `TaskRetryOptions` object used to control
          how the task will be retried if it fails. For pull tasks, only the
          `task_retry_limit` option is allowed. For push tasks, you can use
          the `min_backoff_seconds`, `max_backoff_seconds`, `task_age_limit`,
          `max_doublings`, and `task_retry_limit` options.
      target: Optional; a string that names a module or version, a frontend
          version, or a backend on which to run the task. The string is
          prepended to the domain name of your app. If you set the `target`,
          do not specify a `Host` header in the dictionary for the `headers`
          argument. For pull tasks, do not specify a target.
      tag: Optional; the tag to be used when grouping by tag (pull tasks only).

    Raises:
      InvalidEtaError: If the `eta` is too far into the future.
      InvalidTagError: If the tag is too long.
      InvalidTaskError: If any of the parameters are invalid.
      InvalidTaskNameError: If the task name is invalid.
      InvalidUrlError: If the task URL is invalid or too long.
      TaskTooLargeError: If the task with its associated payload is too large.
    """
    args_diff = set(kwargs.iterkeys()) - self.__CONSTRUCTOR_KWARGS
    if args_diff:
      raise TypeError('Invalid arguments: %s' % ', '.join(args_diff))

    self.__name = kwargs.get('name')
    if self.__name and not _TASK_NAME_RE.match(self.__name):
      raise InvalidTaskNameError(
          'The task name does not match expression "%s"; found %s' %
          (_TASK_NAME_PATTERN, self.__name))

    self.__default_url, self.__relative_url, query = Task.__determine_url(
        kwargs.get('url', ''))
    self.__headers = urlfetch._CaselessDict()
    self.__headers.update(kwargs.get('headers', {}))
    self.__method = kwargs.get('method', 'POST').upper()
    self.__tag = kwargs.get('tag')
    self.__payload = None
    self.__retry_count = 0
    self.__queue_name = None


    size_check = kwargs.get('_size_check', True)
    params = kwargs.get('params', {})


    for header_name, environ_name in _PRESERVE_ENVIRONMENT_HEADERS:
      value = os.environ.get(environ_name)
      if value is not None:
        self.__headers.setdefault(header_name, value)

    self.__headers.setdefault('X-AppEngine-Current-Namespace',
                              namespace_manager.get_namespace())
    if query and params:
      raise InvalidTaskError('Query string and parameters both present; '
                             'only one of these can be supplied')

    if self.__method != 'PULL' and self.__tag is not None:
      raise InvalidTaskError('tag can only be specified for pull tasks')

    if self.__method == 'PULL':
      if not self.__default_url:
        raise InvalidTaskError('url must not be specified for pull tasks')
      if kwargs.get('headers'):
        raise InvalidTaskError('headers must not be specified for pull tasks')
      if kwargs.get('target'):
        raise InvalidTaskError('target must not be specified for pull tasks')
      if params:
        if payload:
          raise InvalidTaskError(
              'Message body and parameters both present for '
              'PULL method; only one of these can be supplied')
        payload = Task.__encode_params(params)
      if payload is None:
        raise InvalidTaskError('payload must be specified for pull task')
      self.__payload = Task.__convert_payload(payload, self.__headers)
    elif self.__method == 'POST':
      if payload and params:
        raise InvalidTaskError('Message body and parameters both present for '
                               'POST method; only one of these can be '
                               'supplied')
      elif query:
        raise InvalidTaskError('POST method cannot have a query string; '
                               'use the "params" keyword argument instead')
      elif params:
        self.__payload = Task.__encode_params(params)
        self.__headers.setdefault(
            'content-type', 'application/x-www-form-urlencoded')
      elif payload is not None:
        self.__payload = Task.__convert_payload(payload, self.__headers)
    elif self.__method in _NON_POST_HTTP_METHODS:
      if payload and self.__method not in _BODY_METHODS:
        raise InvalidTaskError(
            'Payload can only be specified for methods %s' %
            ', '.join(_BODY_METHODS))
      if payload:
        self.__payload = Task.__convert_payload(payload, self.__headers)
      if params:
        query = Task.__encode_params(params)
      if query:
        self.__relative_url = '%s?%s' % (self.__relative_url, query)
    else:
      raise InvalidTaskError('Invalid method: %s' % self.__method)

    self.__target = kwargs.get('target')
    self.__resolve_hostname_and_target()

    self.__headers_list = _flatten_params(self.__headers)
    self.__eta_posix = Task.__determine_eta_posix(
        kwargs.get('eta'), kwargs.get('countdown'))
    self.__eta = None
    self.__retry_options = kwargs.get('retry_options')
    self.__enqueued = False
    self.__deleted = False

    if self.__eta_posix - time.time() > _MAX_COUNTDOWN_SECONDS:
      raise InvalidEtaError('ETA too far in the future')

    if size_check:
      if self.__method == 'PULL':
        max_task_size_bytes = MAX_PULL_TASK_SIZE_BYTES
      else:
        max_task_size_bytes = MAX_PUSH_TASK_SIZE_BYTES
      if self.size > max_task_size_bytes:
        raise TaskTooLargeError('Task size must be less than %d; found %d' %
                                (max_task_size_bytes, self.size))
      if self.__tag and len(self.__tag) > MAX_TAG_LENGTH:
        raise InvalidTagError(
            'Tag must be <= %d bytes. Got a %d byte tag.' % (
                MAX_TAG_LENGTH, len(self.__tag)))

  def __resolve_hostname_and_target(self):
    """Resolve the values of the target parameter and the `Host' header.

    Requires that the attributes __target and __headers exist before this method
    is called.

    This function should only be called once from the __init__ function of the
    Task class.

    Raises:
      InvalidTaskError: If the task is invalid.
    """





    if 'HTTP_HOST' not in os.environ:
      logging.warning(
          'The HTTP_HOST environment variable was not set, but is required '
          'to determine the correct value for the `Task.target\' property. '
          'Please update your unit tests to specify a correct value for this '
          'environment variable.')

    if self.__target is not None and 'Host' in self.__headers:
      raise InvalidTaskError(
          'A host header cannot be set when a target is specified.')
    elif self.__target is not None:
      host = self.__host_from_target(self.__target)
      if host:


        self.__headers['Host'] = host
    elif 'Host' in self.__headers:
      self.__target = self.__target_from_host(self.__headers['Host'])
    else:
      if 'HTTP_HOST' in os.environ:
        self.__headers['Host'] = os.environ['HTTP_HOST']
        self.__target = self.__target_from_host(self.__headers['Host'])
      else:


        self.__target = _UNKNOWN_APP_VERSION

  @staticmethod
  def __target_from_host(host):
    """Calculate the value of the target parameter from a host header.

    Args:
      host: A string representing the hostname for this task.

    Returns:
      A string containing the target of this task, or the constant
      `DEFAULT_APP_VERSION` if it is the default version.

      If this code is running in a unit-test where the environment variable
      `DEFAULT_VERSION_HOSTNAME` is not set then the constant
      `_UNKNOWN_APP_VERSION` is returned.
    """
    default_hostname = app_identity.get_default_version_hostname()
    if default_hostname is None:



      return _UNKNOWN_APP_VERSION

    if host.endswith(default_hostname):

      version_name = host[:-(len(default_hostname) + 1)]
      if version_name:
        return version_name





    return DEFAULT_APP_VERSION

  @staticmethod
  def __host_from_target(target):
    """Calculate the value of the host header from a target.

    Args:
      target: A string representing the target hostname or the constant
          `DEFAULT_APP_VERSION`.

    Returns:
      The string to be used as the host header, or None if it can not be
      determined.
    """
    default_hostname = app_identity.get_default_version_hostname()
    if default_hostname is None:



      return None

    server_software = os.environ.get('SERVER_SOFTWARE', '')
    if target is DEFAULT_APP_VERSION:
      return default_hostname
    elif server_software.startswith(
        'Dev') and server_software != 'Development/1.0 (testbed)':

      target_components = target.rsplit('.', 3)
      module = target_components[-1]
      version = len(target_components) > 1 and target_components[-2] or None
      instance = len(target_components) > 2 and target_components[-3] or None
      try:
        return modules.get_hostname(module=module, version=version,
                                    instance=instance)
      except modules.InvalidModuleError, e:


        if not version:
          return modules.get_hostname(module='default', version=module,
                                      instance=instance)
        else:
          raise e
    else:
      return '%s.%s' % (target, default_hostname)

  @staticmethod
  def __determine_url(relative_url):
    """Determines the URL of a task given a relative URL and a name.

    Args:
      relative_url: The relative URL for the task.

    Returns:
      Tuple (default_url, relative_url, query) where:
            default_url: `True` if this task is using the default URL scheme;
                `False` otherwise.
            relative_url: String containing the relative URL for this task.
            query: The query string for this task.

    Raises:
      InvalidUrlError: If the `relative_url` is invalid.
    """
    if not relative_url:
      default_url, query = True, ''
    else:
      default_url = False
      try:
        relative_url, query = _parse_relative_url(relative_url)
      except _RelativeUrlError, e:
        raise InvalidUrlError(e)

    if len(relative_url) > MAX_URL_LENGTH:
      raise InvalidUrlError(
          'The task URL must be less than %d characters; found %d' %
          (MAX_URL_LENGTH, len(relative_url)))

    return (default_url, relative_url, query)

  @staticmethod
  def __determine_eta_posix(eta=None, countdown=None, current_time=None):
    """Determines the ETA for a task.

    If `eta` and `countdown` are both None, the current time will be used.
    Otherwise, only one of them can be specified.

    Args:
      eta: A `datetime.datetime` object specifying the absolute ETA or `None`;
          this can be time zone-aware or time zone-naive.
      countdown: Count in seconds into the future from the present time that
          the `eta` should be assigned to.
      current_time: Function that returns the current datetime. Defaults to
          `time.time` if `None` is provided.

    Returns:
      A float giving a POSIX timestamp containing the ETA.

    Raises:
      InvalidTaskError: If the parameters are invalid.
    """
    if not current_time:
      current_time = time.time

    if eta is not None and countdown is not None:
      raise InvalidTaskError('Cannot use a countdown and ETA together')
    elif eta is not None:
      if not isinstance(eta, datetime.datetime):
        raise InvalidTaskError('ETA must be a datetime.datetime instance')
      elif eta.tzinfo is None:

        return time.mktime(eta.timetuple()) + eta.microsecond*1e-6
      else:

        return calendar.timegm(eta.utctimetuple()) + eta.microsecond*1e-6
    elif countdown is not None:
      try:
        countdown = float(countdown)
      except ValueError:
        raise InvalidTaskError('Countdown must be a number')
      except OverflowError:
        raise InvalidTaskError('Countdown out of range')
      else:
        return current_time() + countdown
    else:
      return current_time()

  @staticmethod
  def __encode_params(params):
    """URL-encodes a list of parameters.

    Args:
      params: Dictionary of parameters, possibly with iterable values.

    Returns:
      URL-encoded version of the parameters, ready to be added to a query string
          or HTTP `POST` body.
    """
    return urllib.urlencode(_flatten_params(params))

  @staticmethod
  def __convert_payload(payload, headers):
    """Converts a task payload into UTF-8 and sets headers if necessary.

    Args:
      payload: The payload data to convert.
      headers: Dictionary of headers.

    Returns:
      The payload as a non-unicode string.

    Raises:
      InvalidTaskError: If the payload is not a string or unicode instance.
    """
    if isinstance(payload, unicode):
      headers.setdefault('content-type', 'text/plain; charset=utf-8')
      payload = payload.encode('utf-8')
    elif not isinstance(payload, str):
      raise InvalidTaskError(
          'Task payloads must be strings; invalid payload: %r' % payload)
    return payload

  @classmethod
  def _FromQueryAndOwnResponseTask(cls, queue_name, response_task):
    kwargs = {
        '_size_check': False,
        'payload': response_task.body(),
        'name': response_task.task_name(),
        'method': 'PULL'}
    if response_task.has_tag():
      kwargs['tag'] = response_task.tag()
    self = cls(**kwargs)




    self.__eta_posix = response_task.eta_usec() * 1e-6
    self.__retry_count = response_task.retry_count()
    self.__queue_name = queue_name
    self.__enqueued = True
    return self

  @property
  def eta_posix(self):
    """Returns a POSIX timestamp of when this task will run or be leased."""
    if self.__eta_posix is None and self.__eta is not None:

      self.__eta_posix = Task.__determine_eta_posix(self.__eta)
    return self.__eta_posix

  @property
  def eta(self):
    """Returns a `datetime.datatime` when this task will run or be leased."""
    if self.__eta is None and self.__eta_posix is not None:
      self.__eta = datetime.datetime.fromtimestamp(self.__eta_posix, _UTC)
    return self.__eta

  @property
  def _eta_usec(self):
    """Returns a int microseconds timestamp when this task will run."""








    return int(round(self.eta_posix * 1e6))

  @property
  def headers(self):
    """Returns a copy of the HTTP headers for this task (push tasks only)."""
    return self.__headers.copy()

  @property
  def method(self):
    """Returns the method to use for this task."""
    return self.__method

  @property
  def name(self):
    """Returns the name of this task."""
    return self.__name

  @property
  def on_queue_url(self):
    """Returns True if task runs on queue's default URL (push tasks only)."""
    return self.__default_url

  @property
  def payload(self):
    """Returns the payload to be used when the task is invoked (can be None)."""
    return self.__payload

  @property
  def queue_name(self):
    """Returns the name of the queue with which this task is associated."""
    return self.__queue_name

  @property
  def retry_count(self):
    """Returns the number of retries or leases attempted on the task."""
    return self.__retry_count

  @property
  def retry_options(self):
    """Returns any or all the `TaskRetryOptions` tasks."""
    return self.__retry_options

  @property
  def size(self):
    """Returns the size of this task in bytes."""
    HEADER_SEPERATOR = len(': \r\n')
    header_size = sum((len(key) + len(value) + HEADER_SEPERATOR)
                      for key, value in self.__headers_list)
    return (len(self.__method) + len(self.__payload or '') +
            len(self.__relative_url) + header_size)

  @property
  def tag(self):
    """Returns the tag for this task."""
    return self.__tag

  @property
  def target(self):
    """Returns the target for this task."""
    return self.__target

  @property
  def url(self):
    """Returns the relative URL for this task (push tasks only)."""
    return self.__relative_url

  @property
  def was_enqueued(self):
    """Returns `True` if this task has been inserted into a queue."""
    return self.__enqueued

  @property
  def was_deleted(self):
    """Returns `True` if this task was successfully deleted."""
    return self.__deleted

  def add_async(self, queue_name=_DEFAULT_QUEUE, transactional=False, rpc=None):
    """Asynchronously adds this task to a queue. See `Queue.add_async`."""
    return Queue(queue_name).add_async(self, transactional, rpc)

  def add(self, queue_name=_DEFAULT_QUEUE, transactional=False):
    """Adds this task to a queue. See `Queue.add`."""
    return self.add_async(queue_name, transactional).get_result()

  def extract_params(self):
    """Returns the parameters for this task.

    If the same name parameter has several values, then the value is a list of
    strings. For `POST` requests and pull tasks, the parameters are extracted
    from the task payload; for all other methods, the parameters are extracted
    from the URL query string.

    Returns:
      A dictionary of strings that map parameter names to their values as
      strings. If the same name parameter has several values, the value
      will be a list of strings. For `POST` requests and pull tasks, the
      parameters are extracted from the task payload. For all other
      methods, the parameters are extracted from the URL query string. An
      empty dictionary is returned if the task contains an empty payload
      or query string.

    Raises:
      ValueError: If the payload does not contain valid
          `application/x-www-form-urlencoded` data (for `POST` requests and pull
          tasks) or the URL does not contain a valid query (all other requests).
    """
    if self.__method in ('PULL', 'POST'):

      query = self.__payload
    else:
      query = urlparse.urlparse(self.__relative_url).query

    p = {}
    if not query:
      return p

    for key, value in cgi.parse_qsl(
        query, keep_blank_values=True, strict_parsing=True):
      p.setdefault(key, []).append(value)

    for key, value in p.items():
      if len(value) == 1:
        p[key] = value[0]

    return p

  def __repr__(self):
    COMMON_ATTRS = ['eta', 'method', 'name', 'queue_name', 'payload', 'size',
                    'retry_options', 'was_enqueued', 'was_deleted']
    PULL_QUEUE_ATTRS = ['tag']
    PUSH_QUEUE_ATTRS = ['headers', 'url', 'target']

    if self.method == 'PULL':
      attrs = COMMON_ATTRS + PULL_QUEUE_ATTRS
    else:
      attrs = COMMON_ATTRS + PUSH_QUEUE_ATTRS

    properties = ['%s=%r' % (attr, getattr(self, attr))
                  for attr in sorted(attrs)]
    return 'Task<%s>' % ', '.join(properties)


class QueueStatistics(object):
  """Represents the current state of a queue."""

  _ATTRS = ['queue', 'tasks', 'oldest_eta_usec', 'executed_last_minute',
            'in_flight', 'enforced_rate']

  def __init__(self,
               queue,
               tasks,
               oldest_eta_usec=None,
               executed_last_minute=None,
               in_flight=None,
               enforced_rate=None):
    """Constructor.

    Args:
      queue: The queue instance for which you intend to use `QueueStatistics`.
      tasks: The number of tasks remaining in the queue.

          Note:
              It is not always possible to accurately determine the value for
              the `tasks` field. Use this field with caution.
      oldest_eta_usec: The `eta` of the oldest non-completed task for the
          queue; `None` if unknown or no incomplete tasks remain in the queue.
          The value `None` indicates that the queue has no backlog; the queue
          is empty or only contains tasks with an `eta` in the future. The
          `eta` represents the time at which a push queue task is eligible to
          run or a pull queue task can be leased. The value is expressed as
          microseconds since January 1, 1970. App Engine will not run a task
          before its `eta` time.

          Note:
              It is not always possible to accurately determine the value for
              the `oldest_eta_usec` field. Use this field with caution.
      executed_last_minute: The number of tasks ran or leased in the last
          minute.
      in_flight: For push queues, the number of tasks that are currently
          running. This value is not relevant for pull queues.
      enforced_rate: For push queues, the current enforced rate in tasks per
          second. This value is not relevant for pull queues. The enforced
          rate is the actual rate at which tasks are being processed after
          any throttling done by App Engine to prevent the queue from
          overwhelming your application. The enforced rate can be smaller than
          the rate you specify in the queue configuration. If the enforced
          rate is too small, you can change performance settings for your
          application.
    """
    self.queue = queue
    self.tasks = tasks
    self.oldest_eta_usec = oldest_eta_usec
    self.executed_last_minute = executed_last_minute
    self.in_flight = in_flight
    self.enforced_rate = enforced_rate

  def __eq__(self, o):
    if not isinstance(o, QueueStatistics):
      return NotImplemented

    result = True
    for attr in self._ATTRS:
      result = result and (getattr(self, attr) == getattr(o, attr))
    return result

  def __ne__(self, o):
    if not isinstance(o, QueueStatistics):
      return NotImplemented

    result = False
    for attr in self._ATTRS:
      result = result or (getattr(self, attr) != getattr(o, attr))
    return result

  def __repr__(self):
    properties = ['%s=%r' % (attr, getattr(self, attr)) for attr in self._ATTRS]
    return 'QueueStatistics(%s)' % ', '.join(properties)

  @classmethod
  def _ConstructFromFetchQueueStatsResponse(cls, queue, response):
    """Helper for converting from a `FetchQueueStatsResponse_QueueStats` proto.

    Args:
      queue: A queue instance.
      response: An instance of `FetchQueueStatsResponse_QueueStats`.

    Returns:
      A new QueueStatistics instance.
    """
    args = {'queue': queue, 'tasks': response.num_tasks()}
    if response.oldest_eta_usec() >= 0:
      args['oldest_eta_usec'] = response.oldest_eta_usec()
    else:
      args['oldest_eta_usec'] = None

    if response.has_scanner_info():
      scanner_info = response.scanner_info()
      args['executed_last_minute'] = scanner_info.executed_last_minute()
      if scanner_info.has_requests_in_flight():
        args['in_flight'] = scanner_info.requests_in_flight()
      if scanner_info.has_enforced_rate():
        args['enforced_rate'] = scanner_info.enforced_rate()
    return cls(**args)

  @classmethod
  def fetch_async(cls, queue_or_queues, rpc=None):
    """Asynchronously get the queue details for multiple queues.

    Example::

        rpc = taskqueue.create_rpc(deadline=1.0)
        taskqueue.QueueStatistics.fetch_async([taskqueue.Queue("foo"),
                                                taskqueue.Queue("bar")], rpc)
        statsList = rpc.get_result()


    Args:
      queue_or_queues: The queue or list of queues for which you are obtaining
          statistics. If you are retrieving statistics for a single queue,
          you can supply either a queue instance or the name of the queue. If
          you are retrieving a list of queues, you can supply an iterable
          list of queue instances or an iterable list of queue names.
      rpc: An optional UserRPC object.

    Returns:
      A UserRPC object. Call `get_result()` to complete the RPC and obtain the
      result.

      If an iterable (other than string) is provided as input, the result will
      be a list of of `QueueStatistics` objects, one for each queue in the
      order requested.

      Otherwise, if a single item was provided as input, then the result will
      be a single `QueueStatistics` object.

    Raises:
      TypeError: If `queue_or_queues` is not a queue instance, string, an
          iterable containing only queue instances, or an iterable containing
          only strings.
    """
    wants_list = True


    if isinstance(queue_or_queues, basestring):
      queue_or_queues = [queue_or_queues]
      wants_list = False

    try:
      queues_list = [queue for queue in queue_or_queues]
    except TypeError:
      queues_list = [queue_or_queues]
      wants_list = False

    contains_strs = any(isinstance(queue, basestring) for queue in queues_list)
    contains_queues = any(isinstance(queue, Queue) for queue in queues_list)

    if contains_strs and contains_queues:
      raise TypeError('queue_or_queues must contain either strings or queue '
                      'instances, not both.')

    if contains_strs:
      queues_list = [Queue(queue_name) for queue_name in queues_list]

    return cls._FetchMultipleQueues(queues_list, wants_list, rpc)

  @classmethod
  def fetch(cls, queue_or_queues, deadline=10):
    """Get the queue details for multiple queues.

    Use `QueueStatistics.fetch()` to get `QueueStatistics` objects for the
    queues you are interested in. You can specify one or more queues.

    Example::

        statsList = taskqueue.QueueStatistics.fetch([taskqueue.Queue("foo"),
                                                     taskqueue.Queue("bar")])


    Args:
      queue_or_queues: The queue or list of queues for which you are obtaining
          statistics. If you are retrieving statistics for a single queue,
          you can supply either a queue instance or the name of the queue.
          If you are retrieving statistics for a list of queues, you can
          supply an iterable list of queue instances or an iterable list of
          queue names.
      deadline: The maximum number of seconds to wait before aborting the
          method call.

    Returns:
      If an iterable (other than string) is provided as input, a list of of
      `QueueStatistics` objects will be returned, one for each queue in the
      order requested.

      Otherwise, if a single item was provided as input, then a single
      `QueueStatistics` object will be returned.

    Raises:
      TypeError: If `queue_or_queues` is not a queue instance, string, an
          iterable containing only queue instances, or an iterable containing
          only strings.
      Error-subclass on application errors.
    """
    _ValidateDeadline(deadline)

    if not queue_or_queues:

      return []

    rpc = create_rpc(deadline)
    cls.fetch_async(queue_or_queues, rpc)
    return rpc.get_result()

  @classmethod
  def _FetchMultipleQueues(cls, queues, multiple, rpc=None):
    """Internal implementation of fetch stats where queues must be a list."""

    def ResultHook(rpc):
      """Processes the TaskQueueFetchQueueStatsResponse."""
      try:
        rpc.check_success()
      except apiproxy_errors.ApplicationError, e:
        raise _TranslateError(e.application_error, e.error_detail)

      assert len(queues) == rpc.response.queuestats_size(), (
          'Expected %d results, got %d' % (
              len(queues), rpc.response.queuestats_size()))
      queue_stats = [cls._ConstructFromFetchQueueStatsResponse(queue, response)
                     for queue, response in zip(queues,
                                                rpc.response.queuestats_list())]
      if multiple:
        return queue_stats
      else:
        assert len(queue_stats) == 1
        return queue_stats[0]

    request = taskqueue_service_pb.TaskQueueFetchQueueStatsRequest()
    response = taskqueue_service_pb.TaskQueueFetchQueueStatsResponse()

    requested_app_id = queues[0]._app

    for queue in queues:
      request.add_queue_name(queue.name)
      if queue._app != requested_app_id:
        raise InvalidQueueError('Inconsistent app ids requested.')
    if requested_app_id:
      request.set_app_id(requested_app_id)

    return _MakeAsyncCall('FetchQueueStats',
                          request,
                          response,
                          ResultHook,
                          rpc)


class Queue(object):
  """Represents a queue.

  The `Queue` class is used to prepare tasks for offline execution by App
  Engine.

  A queue object is instantiated by name. The name must correspond either to the
  default queue (provided by the system) or a user-defined queue as specified in
  the application's `queue.yaml` configuration file. The queue object can then
  be used to insert new task instances for offline execution by App Engine.

  Multiple queue objects can correspond to the same underlying system queue.
  However, a single task object can only be added to one queue.
  """

  def __init__(self, name=_DEFAULT_QUEUE):
    """Initializer.

    Args:
      name: Optional; name of this queue, which must correspond to a
          user-defined queue name from `queue.yaml`. If not supplied, defaults
          to the default queue.

    Raises:
      InvalidQueueNameError: If the queue name is invalid.
    """


    if not _QUEUE_NAME_RE.match(name):
      raise InvalidQueueNameError(
          'Queue name does not match pattern "%s"; found %s' %
          (_QUEUE_NAME_PATTERN, name))
    self.__name = name
    self.__url = '%s/%s' % (_DEFAULT_QUEUE_PATH, self.__name)





    self._app = None

  def purge(self):
    """Removes all of the tasks in this queue.

    Purging the queue takes time, regardless of the queue size. Tasks continue
    to run until the backends recognize that the queue has been purged. This
    operation is permanent; purged tasks cannot be recovered.

    Raises:
      Error-subclass on application errors.
    """
    request = taskqueue_service_pb.TaskQueuePurgeQueueRequest()
    response = taskqueue_service_pb.TaskQueuePurgeQueueResponse()

    request.set_queue_name(self.__name)
    if self._app:
      request.set_app_id(self._app)

    try:
      apiproxy_stub_map.MakeSyncCall('taskqueue',
                                     'PurgeQueue',
                                     request,
                                     response)
    except apiproxy_errors.ApplicationError, e:
      raise _TranslateError(e.application_error, e.error_detail)

  def delete_tasks_by_name_async(self, task_name, rpc=None):
    """Asynchronously deletes a task or list of tasks in this queue, by name.

    This function is identical to `delete_tasks_by_name()` except that it
    returns an asynchronous object. You can call `get_result()` on the return
    value to block on the call.

    Args:
      task_name: A string corresponding to a task name, or an iterable of
          strings corresponding to task names.
      rpc: An optional UserRPC object.

    Returns:
      A UserRPC object. Call `get_result()` to complete the RPC and obtain
      the result.

      If an iterable, other than string, is provided as input, the result will
      be a list of of task objects, one for each task name in the order
      requested. The `Task.was_deleted` property will be `True` for each
      task deleted by this call, and will be `False` for unknown and
      tombstoned tasks.

      Otherwise, if a single string was provided as input, then the result
      will be a single task object.

    Raises:
      DuplicateTaskNameError: If a task name is repeated in the request.
    """
    if isinstance(task_name, str):
      return self.delete_tasks_async(Task(name=task_name), rpc)
    else:
      tasks = [Task(name=name) for name in task_name]
      return self.delete_tasks_async(tasks, rpc)

  def delete_tasks_by_name(self, task_name):
    """Deletes a task or list of tasks in this queue, by name.

    When multiple tasks are specified, an exception will be raised if any
    individual task fails to be deleted.

    Args:
      task_name: A string corresponding to a task name, or an iterable of
          strings corresponding to task names.

    Returns:
      If an iterable, other than string, is provided as input, a list of task
      objects is returned, one for each task name in the order requested.
      The `Task.was_deleted` property will be `True` for each task deleted
      by this call, and will be `False` for unknown and tombstoned tasks.

      Otherwise, if a single string was provided as input, a single task
      object is returned.

    Raises:
      DuplicateTaskNameError: If a task name is repeated in the request.
      Error-subclass on application errors.
    """
    return self.delete_tasks_by_name_async(task_name).get_result()

  def delete_tasks_async(self, task, rpc=None):
    """Asynchronously deletes a task or list of tasks in this queue.

    The task name is the only task attribute that is used to select tasks for
    deletion.

    This function is identical to `delete_tasks()` except that it returns an
    asynchronous object. You can call `get_result()` on the return value to
    block on the call.

    Args:
      task: A task instance or a list of task instances that will be deleted
          from the queue.
      rpc: An optional UserRPC object.

    Returns:
      A UserRPC object. Call `get_result()` to complete the RPC and obtain the
      task or list of tasks passed into this call.

    Raises:
      BadTaskStateError: If the tasks to be deleted do not have task names or
          have already been deleted.
      DuplicateTaskNameError: If a task is repeated in the request.
    """
    try:
      tasks = list(iter(task))
    except TypeError:
      tasks = [task]
      multiple = False
    else:
      multiple = True

    return self.__DeleteTasks(tasks, multiple, rpc)

  def delete_tasks(self, task):
    """Deletes a task or list of tasks in this queue.

    When multiple tasks are specified, an exception will be raised if any
    individual task fails to be deleted. Check the `task.was_deleted` property.

    The task name is the only task attribute that is used to select tasks for
    deletion. If any task exists that is unnamed or with the `was_deleted`
    property set to `True`, a `BadTaskStateError` will be raised immediately.

    Args:
      task: A task instance or a list of task instances that will be deleted
          from the queue.

    Returns:
      The task or list of tasks passed into this call.

    Raises:
      BadTaskStateError: If the tasks to be deleted do not have task names or
          have already been deleted.
      DuplicateTaskNameError: If a task is repeated in the request.
      Error-subclass on application errors.
    """
    return self.delete_tasks_async(task).get_result()

  def __DeleteTasks(self, tasks, multiple, rpc=None):
    """Internal implementation of delete_tasks_async(), tasks must be a list."""

    def ResultHook(rpc):
      """Processes the TaskQueueDeleteResponse."""
      try:
        rpc.check_success()
      except apiproxy_errors.ApplicationError, e:
        raise _TranslateError(e.application_error, e.error_detail)

      assert rpc.response.result_size() == len(tasks), (
          'expected %d results from delete(), got %d' % (
              len(tasks), rpc.response.result_size()))

      IGNORED_STATES = [
          taskqueue_service_pb.TaskQueueServiceError.UNKNOWN_TASK,
          taskqueue_service_pb.TaskQueueServiceError.TOMBSTONED_TASK]

      exception = None
      for task, result in zip(tasks, rpc.response.result_list()):
        if result == taskqueue_service_pb.TaskQueueServiceError.OK:

          task._Task__deleted = True
        elif result in IGNORED_STATES:

          task._Task__deleted = False
        elif exception is None:
          exception = _TranslateError(result)

      if exception is not None:
        raise exception

      if multiple:
        return tasks
      else:
        assert len(tasks) == 1
        return tasks[0]

    request = taskqueue_service_pb.TaskQueueDeleteRequest()
    response = taskqueue_service_pb.TaskQueueDeleteResponse()

    request.set_queue_name(self.__name)
    task_names = set()
    for task in tasks:
      if not task.name:
        raise BadTaskStateError('A task name must be specified for a task')
      if task.was_deleted:
        raise BadTaskStateError(
            'The task %s has already been deleted' % task.name)
      if task.name in task_names:
        raise DuplicateTaskNameError(
            'The task name %r is used more than once in the request' %
            task.name)
      task_names.add(task.name)
      request.add_task_name(task.name)

    return _MakeAsyncCall('Delete',
                          request,
                          response,
                          ResultHook,
                          rpc)

  @staticmethod
  def _ValidateLeaseSeconds(lease_seconds):

    if not isinstance(lease_seconds, (float, int, long)):
      raise TypeError(
          'lease_seconds must be a float or an integer')
    lease_seconds = float(lease_seconds)

    if lease_seconds < 0.0:
      raise InvalidLeaseTimeError(
          'lease_seconds must not be negative')
    if lease_seconds > MAX_LEASE_SECONDS:
      raise InvalidLeaseTimeError(
          'Lease time must not be greater than %d seconds' %
          MAX_LEASE_SECONDS)
    return lease_seconds

  @staticmethod
  def _ValidateMaxTasks(max_tasks):
    if not isinstance(max_tasks, (int, long)):
      raise TypeError(
          'max_tasks must be an integer')

    if max_tasks <= 0:
      raise InvalidMaxTasksError(
          'Negative or zero tasks requested')
    if max_tasks > MAX_TASKS_PER_LEASE:
      raise InvalidMaxTasksError(
          'Only %d tasks can be leased at once' %
          MAX_TASKS_PER_LEASE)

  def _QueryAndOwnTasks(self, request, response, queue_name, rpc=None):

    def ResultHook(rpc):
      """Processes the TaskQueueQueryAndOwnTasksResponse."""
      try:
        rpc.check_success()
      except apiproxy_errors.ApplicationError, e:
        raise _TranslateError(e.application_error, e.error_detail)

      tasks = []
      for response_task in rpc.response.task_list():
        tasks.append(
            Task._FromQueryAndOwnResponseTask(queue_name, response_task))
      return tasks

    return _MakeAsyncCall('QueryAndOwnTasks',
                          request,
                          response,
                          ResultHook,
                          rpc)

  def lease_tasks_async(self, lease_seconds, max_tasks, rpc=None):
    """Asynchronously leases a number of tasks from the queue.

    This method can only be performed on a pull queue. Attempts to lease tasks
    from a push queue results in an `InvalidQueueModeError`. All non-pull tasks
    in the pull queue will be converted into pull tasks when leased. If fewer
    than the specified value of `max_tasks` are available, all available tasks
    on a best-effort basis are returned. The `lease_tasks_async` method supports
    leasing at most 1000 tasks for no longer than a week in a single call.

    This function is identical to `lease_tasks()` except that it returns an
    asynchronous object. You can call `get_result()` on the return value to
    block on the call.

    Args:
      lease_seconds: Number of seconds to lease the tasks, up to one week
          (604,800 seconds). Must be a positive integer.
      max_tasks: Maximum number of tasks to lease from the pull queue, up to
          1000 tasks.
      rpc: An optional UserRPC object.

    Returns:
      A UserRPC object. Call `get_result()` to complete the RPC and obtain the
      list of tasks leased from the queue.

    Raises:
      InvalidLeaseTimeError: If `lease_seconds` is not a valid float or
          integer number or is outside the valid range.
      InvalidMaxTasksError: If `max_tasks` is not a valid integer or is
          outside the valid range.
    """
    lease_seconds = self._ValidateLeaseSeconds(lease_seconds)
    self._ValidateMaxTasks(max_tasks)

    request = taskqueue_service_pb.TaskQueueQueryAndOwnTasksRequest()
    response = taskqueue_service_pb.TaskQueueQueryAndOwnTasksResponse()

    request.set_queue_name(self.__name)
    request.set_lease_seconds(lease_seconds)
    request.set_max_tasks(max_tasks)

    return self._QueryAndOwnTasks(request, response, self.__name, rpc)

  def lease_tasks(self, lease_seconds, max_tasks, deadline=10):
    """Leases a number of tasks from the queue for a period of time.

    This method can only be performed on a pull queue. Any non-pull tasks in
    the pull queue will be converted into pull tasks when being leased. If
    fewer than the specified number of `max_tasks` are available, all available
    tasks will be returned. The `lease_tasks` method supports leasing at most
    1000 tasks for no longer than a week in a single call.

    Args:
      lease_seconds: Number of seconds to lease the tasks, up to one week
          (604,800 seconds). Must be a positive integer.
      max_tasks: The maximum number of tasks to lease from the pull queue, up
          to 1000 tasks.
      deadline: The maximum number of seconds to wait before aborting the
          method call.

    Returns:
      A list of tasks leased from the queue.

    Raises:
      InvalidLeaseTimeError: If `lease_seconds` is not a valid float or
          integer number or is outside the valid range.
      InvalidMaxTasksError: If `max_tasks` is not a valid integer or is
          outside the valid range.
      InvalidQueueModeError: If invoked on a queue that is not in pull mode.
      Error-subclass on application errors.
    """
    _ValidateDeadline(deadline)
    rpc = create_rpc(deadline)
    self.lease_tasks_async(lease_seconds, max_tasks, rpc)
    return rpc.get_result()

  def lease_tasks_by_tag_async(self,
                               lease_seconds,
                               max_tasks,
                               tag=None,
                               rpc=None):
    """Asynchronously leases tasks with the same tag from the queue.

    If a `tag` is specified, tasks with that tag are leased for a specified
    period of time. If a `tag` is not specified, the best-effort oldest tag of
    the queue's oldest task (specified by the `eta`) will be used.

    This function is identical to `lease_tasks_by_tag()` except that it returns
    an asynchronous object. You can call `get_result()` on the return value to
    block on the call.

    Args:
      lease_seconds: Number of seconds to lease the tasks.
      max_tasks: The maximum number of tasks to lease from the pull queue.
      tag: The tag to query for, or None to group by the first available tag.
      rpc: An optional UserRPC object.

    Returns:
      A UserRPC object. Call `get_result()` to complete the RPC and obtain the
      list of tasks leased from the queue.

    Raises:
      InvalidLeaseTimeError: If `lease_seconds` is not a valid float or
          integer number or is outside the valid range.
      InvalidMaxTasksError: If `max_tasks` is not a valid integer or is
          outside the valid range.
    """
    lease_seconds = self._ValidateLeaseSeconds(lease_seconds)
    self._ValidateMaxTasks(max_tasks)

    request = taskqueue_service_pb.TaskQueueQueryAndOwnTasksRequest()
    response = taskqueue_service_pb.TaskQueueQueryAndOwnTasksResponse()

    request.set_queue_name(self.__name)
    request.set_lease_seconds(lease_seconds)
    request.set_max_tasks(max_tasks)
    request.set_group_by_tag(True)
    if tag is not None:
      request.set_tag(tag)

    return self._QueryAndOwnTasks(request, response, self.__name, rpc)

  def lease_tasks_by_tag(self, lease_seconds, max_tasks, tag=None, deadline=10):
    """Leases tasks with the same tag from the queue.

    If a `tag` is specified, tasks with that tag are leased for a specified
    period of time. If a `tag` is not specified, the tag of the queue's oldest
    task (specified by the `eta`) will be used.

    This method can only be performed on a pull queue. Any non-pull tasks in
    the pull queue will be converted into pull tasks when being leased. If
    fewer than the specified value of `max_tasks` are available, all available
    tasks will be returned. The `lease_tasks` method supports leasing at most
    1000 tasks for no longer than a week in a single call.

    Args:
      lease_seconds: Number of seconds to lease the tasks.
      max_tasks: The maximum number of tasks to lease from the pull queue.
      tag: The tag to query for, or None to group by the first available
          tag.
      deadline: The maximum number of seconds to wait before aborting the
          method call.

    Returns:
      A list of tasks leased from the queue.

    Raises:
      InvalidLeaseTimeError: If `lease_seconds` is not a valid float or
          integer number or is outside the valid range.
      InvalidMaxTasksError: If `max_tasks` is not a valid integer or is
          outside the valid range.
      InvalidQueueModeError: If invoked on a queue that is not in pull mode.
      Error-subclass on application errors.
    """
    _ValidateDeadline(deadline)
    rpc = create_rpc(deadline)
    self.lease_tasks_by_tag_async(lease_seconds, max_tasks, tag, rpc)
    return rpc.get_result()

  def add_async(self, task, transactional=False, rpc=None):
    """Asynchronously adds a task or list of tasks into this queue.

    This function is identical to `add()` except that it returns an asynchronous
    object. You can call `get_result()` on the return value to block on the
    call.

    Args:
      task: A task instance or a list of task instances that will be added to
          the queue. If `task` is a list of task objects, all tasks are added
          to the queue. If `Transactional=True`, then all of the tasks are
          added in the same active Datastore transaction, and if any of the
          tasks cannot be added to the queue, none of the tasks are added to
          the queue and the Datastore transaction will fail. If
          `transactional=False`, a failure to add a task to the queue will
          raise an exception, but other tasks will be enqueued.
      transactional: If `True`, transactional tasks will be added to the queue
          but cannot be run or leased until after the transaction succeeds. If
          the transaction fails, the tasks will be removed from the queue
          (and therefore will never run). If `False`, the added tasks are
          available to run immediately; any enclosing transaction's success or
          failure is ignored.
      rpc: An optional UserRPC object.

    Returns:
      A UserRPC object. Call `get_result()` to complete the RPC and obtain the
      task or list of tasks that was supplied to this method. Successfully
      queued tasks will have a valid queue name and task name after the
      call; such task objects are marked as queued and cannot be added
      again.

      Note:
          Task objects that are returned from transactional adds are not
          notified or updated when the enclosing transaction succeeds or
          fails.

    Raises:
      BadTaskStateError: If the tasks have already been added to a queue.
      BadTransactionStateError: If the transactional argument is `True` but
          this call is being made outside of the context of a transaction.
      DuplicateTaskNameError: If a task name is repeated in the request.
      InvalidTaskError: If both push and pull tasks exist in the task list.
      InvalidTaskNameError: If a task name is provided but is not valid.
      TooManyTasksError: If the task contains more than `MAX_TASKS_PER_ADD`
          tasks.
      TransactionalRequestTooLargeError: If transactional is `True` and the
          total size of the tasks and supporting request data exceeds the
          `MAX_TRANSACTIONAL_REQUEST_SIZE_BYTES` quota.
    """
    try:
      tasks = list(iter(task))
    except TypeError:
      tasks = [task]
      multiple = False
    else:
      multiple = True




    has_push_task = False
    has_pull_task = False
    for task in tasks:
      if task.method == 'PULL':
        has_pull_task = True
      else:
        has_push_task = True

    if has_push_task and has_pull_task:
      raise InvalidTaskError(
          'You cannot add both push and pull tasks in a single call.')

    if has_push_task:
      fill_function = self.__FillAddPushTasksRequest
    else:
      fill_function = self.__FillAddPullTasksRequest
    return self.__AddTasks(tasks,
                           transactional,
                           fill_function,
                           multiple,
                           rpc)

  def add(self, task, transactional=False):
    """Adds a task or list of tasks into this queue.

    If a list of more than one tasks is given, a raised exception does not
    guarantee that no tasks were added to the queue (unless `transactional` is
    set to `True`). To determine which tasks were successfully added when an
    exception is raised, check the `Task.was_enqueued` property.

    Push tasks, or those with a method not equal to pull cannot be added to
    queues in pull mode. Similarly, pull tasks cannot be added to queues in
    push mode.

    If a `TaskAlreadyExistsError` or `TombstonedTaskError` is raised, the caller
    can be guaranteed that for each one of the provided tasks, either the
    corresponding task was successfully added, or a task with the given name was
    successfully added in the past.

    Args:
      task: A task instance or a list of task instances that will be added to
          the queue. If `task` is set to a list of task objects, all tasks are
          added to the queue. If `Transactional=True`, then all of the tasks
          are added in the same active Datastore transaction, and if any of
          the tasks cannot be added to the queue, none of the tasks are added
          to the queue, and the Datastore transaction will fail. If
          `transactional=False`, a failure to add any task to the queue will
          raise an exception, but other tasks will be added to the queue.
      transactional: If `True`, transactional tasks will be added to the queue
          but cannot be run or leased until after the transaction succeeds. If
          the transaction fails then the tasks will be removed from the queue
          (and therefore will never run). If `False`, the added tasks are
          available to run immediately; any enclosing transaction's success or
          failure is ignored.

    Returns:
      The task or list of tasks that was supplied to this method. Successfully
      queued tasks will have a valid queue name and task name after the
      call; these task objects are marked as queued and cannot be added
      again.

      Note:
          Task objects returned from transactional adds are not notified or
          updated when the enclosing transaction succeeds or fails.

    Raises:
      BadTaskStateError: If the task has already been added to a queue.
      BadTransactionStateError: If the `transactional` argument is `True` but
          this call is being made outside of the context of a transaction.
      DuplicateTaskNameError: If a task name is repeated in the request.
      InvalidTaskNameError: If a task name is provided but is not valid.
      InvalidTaskError: If both push and pull tasks exist in the task list.
      InvalidQueueModeError: If a pull task is added to a queue in push mode, or
          if a push task is added to a queue in pull mode.
      TaskAlreadyExistsError: If a task with the same name as a given name has
          previously been added to the queue.
      TombstonedTaskError: If a task with the same name as a given name has
          previously been added to the queue and deleted.
      TooManyTasksError: If a task contains more than `MAX_TASKS_PER_ADD`
          tasks.
      TransactionalRequestTooLargeError: If transactional is `True` and the
          total size of the tasks and supporting request data exceeds the
          `MAX_TRANSACTIONAL_REQUEST_SIZE_BYTES` quota.
      Error-subclass on application errors.
    """
    if task:
      return self.add_async(task, transactional).get_result()
    else:
      return []

  def __AddTasks(self, tasks, transactional, fill_request, multiple, rpc=None):
    """Internal implementation of adding tasks where tasks must be a list."""

    def ResultHook(rpc):
      """Processes the TaskQueueBulkAddResponse."""
      try:
        rpc.check_success()
      except apiproxy_errors.ApplicationError, e:
        raise _TranslateError(e.application_error, e.error_detail)

      assert rpc.response.taskresult_size() == len(tasks), (
          'expected %d results from BulkAdd(), got %d' % (
              len(tasks), rpc.response.taskresult_size()))

      exception = None
      for task, task_result in zip(tasks, rpc.response.taskresult_list()):
        if (task_result.result() ==
            taskqueue_service_pb.TaskQueueServiceError.OK):
          if task_result.has_chosen_task_name():
            task._Task__name = task_result.chosen_task_name()
          task._Task__queue_name = self.__name
          task._Task__enqueued = True
        elif (task_result.result() ==
              taskqueue_service_pb.TaskQueueServiceError.SKIPPED):
          pass
        elif (exception is None or isinstance(exception, TaskAlreadyExistsError)
              or isinstance(exception, TombstonedTaskError)):
          exception = _TranslateError(task_result.result())

      if exception is not None:
        raise exception

      if multiple:
        return tasks
      else:
        assert len(tasks) == 1
        return tasks[0]

    if len(tasks) > MAX_TASKS_PER_ADD:
      raise TooManyTasksError(
          'No more than %d tasks can be added in a single call' %
          MAX_TASKS_PER_ADD)

    request = taskqueue_service_pb.TaskQueueBulkAddRequest()
    response = taskqueue_service_pb.TaskQueueBulkAddResponse()

    task_names = set()
    for task in tasks:
      if task.name:
        if task.name in task_names:
          raise DuplicateTaskNameError(
              'The task name %r is used more than once in the request' %
              task.name)
        task_names.add(task.name)
      if task.was_enqueued:
        raise BadTaskStateError('The task has already been enqueued.')

      fill_request(task, request.add_add_request(), transactional)

    if transactional and (request.ByteSize() >
                          MAX_TRANSACTIONAL_REQUEST_SIZE_BYTES):
      raise TransactionalRequestTooLargeError(
          'Transactional request size must be less than %d; found %d' %
          (MAX_TRANSACTIONAL_REQUEST_SIZE_BYTES, request.ByteSize()))

    return _MakeAsyncCall('BulkAdd',
                          request,
                          response,
                          ResultHook,
                          rpc)

  def __FillTaskQueueRetryParameters(self,
                                     retry_options,
                                     retry_retry_parameters):
    """Populates a TaskQueueRetryParameters with data from a TaskRetryOptions.

    Args:
      retry_options: The TaskRetryOptions instance to use as a source for the
          data to be added to retry_retry_parameters.
      retry_retry_parameters: A taskqueue_service_pb.TaskQueueRetryParameters
          to populate.
    """
    if retry_options.min_backoff_seconds is not None:
      retry_retry_parameters.set_min_backoff_sec(
          retry_options.min_backoff_seconds)

    if retry_options.max_backoff_seconds is not None:
      retry_retry_parameters.set_max_backoff_sec(
          retry_options.max_backoff_seconds)

    if retry_options.task_retry_limit is not None:
      retry_retry_parameters.set_retry_limit(retry_options.task_retry_limit)

    if retry_options.task_age_limit is not None:
      retry_retry_parameters.set_age_limit_sec(retry_options.task_age_limit)

    if retry_options.max_doublings is not None:
      retry_retry_parameters.set_max_doublings(retry_options.max_doublings)

  def __FillAddPushTasksRequest(self, task, task_request, transactional):
    """Populates a TaskQueueAddRequest with the data from a push task instance.

    Args:
      task: The task instance to use as a source for the data to be added to
          task_request.
      task_request: The taskqueue_service_pb.TaskQueueAddRequest to populate.
      transactional: If True, the task_request.transaction message is
          populated with information from the enclosing transaction, if any.

    Raises:
      BadTaskStateError: If the task was already added to a queue.
      BadTransactionStateError: If the transactional argument is True and
          an enclosing transaction does not exist.
      InvalidTaskNameError: If the transactional argument is True and the task
          is named.
    """
    task_request.set_mode(taskqueue_service_pb.TaskQueueMode.PUSH)
    self.__FillTaskCommon(task, task_request, transactional)

    adjusted_url = task.url
    if task.on_queue_url:
      adjusted_url = self.__url + task.url








    task_request.set_method(_METHOD_MAP.get(task.method))
    task_request.set_url(adjusted_url)

    if task.payload:
      task_request.set_body(task.payload)
    for key, value in _flatten_params(task.headers):
      header = task_request.add_header()
      header.set_key(key)
      header.set_value(value)

    if task.retry_options:
      self.__FillTaskQueueRetryParameters(
          task.retry_options, task_request.mutable_retry_parameters())

  def __FillAddPullTasksRequest(self, task, task_request, transactional):
    """Populates a TaskQueueAddRequest with the data from a pull task instance.

    Args:
      task: The task instance to use as a source for the data to be added to
          task_request.
      task_request: The taskqueue_service_pb.TaskQueueAddRequest to populate.
      transactional: If True, then populates the task_request.transaction
          message with information from the enclosing transaction, if any.

    Raises:
      BadTaskStateError: If the task doesn't have a payload, or has already
          been added to a queue.
      BadTransactionStateError: If the transactional argument is True and
          an enclosing transaction does not exist.
      InvalidTaskNameError: If the transactional argument is True and the task
          is named.
    """
    task_request.set_mode(taskqueue_service_pb.TaskQueueMode.PULL)
    self.__FillTaskCommon(task, task_request, transactional)
    if task.payload is not None:
      task_request.set_body(task.payload)
    else:
      raise BadTaskStateError('Pull task must have a payload')

  def __FillTaskCommon(self, task, task_request, transactional):
    """Fills common fields for both push tasks and pull tasks."""
    if self._app:
      task_request.set_app_id(self._app)
    task_request.set_queue_name(self.__name)
    task_request.set_eta_usec(task._eta_usec)
    if task.name:
      task_request.set_task_name(task.name)
    else:
      task_request.set_task_name('')
    if task.tag:
      task_request.set_tag(task.tag)



    if transactional:
      from google.appengine.api import datastore
      if not datastore._MaybeSetupTransaction(task_request, []):
        raise BadTransactionStateError(
            'Transactional adds are not allowed outside of transactions')

    if task_request.has_transaction() and task.name:
      raise InvalidTaskNameError(
          'A task bound to a transaction cannot be named.')

  @property
  def name(self):
    """Returns the name of this queue."""
    return self.__name

  def modify_task_lease(self, task, lease_seconds):
    """Modifies the lease of a task in this queue.

    Args:
      task: A task instance that will have its lease modified.
      lease_seconds: Number of seconds, from the current time, that the task
          lease will be modified to. If `lease_seconds` is `0`, the task lease
          is removed and the task will be available for leasing again using
          the `lease_tasks` method.

    Raises:
      TypeError: If `lease_seconds` is not a valid float or integer.
      InvalidLeaseTimeError: If `lease_seconds` is outside of the valid range.
      Error-subclass on application errors.
    """
    lease_seconds = self._ValidateLeaseSeconds(lease_seconds)

    request = taskqueue_service_pb.TaskQueueModifyTaskLeaseRequest()
    response = taskqueue_service_pb.TaskQueueModifyTaskLeaseResponse()

    request.set_queue_name(self.__name)
    request.set_task_name(task.name)
    request.set_eta_usec(task._eta_usec)
    request.set_lease_seconds(lease_seconds)

    try:
      apiproxy_stub_map.MakeSyncCall('taskqueue',
                                     'ModifyTaskLease',
                                     request,
                                     response)
    except apiproxy_errors.ApplicationError, e:
      raise _TranslateError(e.application_error, e.error_detail)

    task._Task__eta_posix = response.updated_eta_usec() * 1e-6
    task._Task__eta = None

  def fetch_statistics_async(self, rpc=None):
    """Asynchronously gets the current details about this queue.

    Args:
      rpc: An optional UserRPC object.

    Returns:
      A UserRPC object. Call `get_result()` to complete the RPC and obtain a
      `QueueStatistics` instance that contains information about this queue.
    """
    return QueueStatistics.fetch_async(self, rpc)

  def fetch_statistics(self, deadline=10):
    """Gets the current details about this queue.

    Args:
      deadline: The maximum number of seconds to wait before aborting the
          method call.

    Returns:
      A `QueueStatistics` instance that contains information about this queue.
      Error-subclass on application errors.
    """
    _ValidateDeadline(deadline)
    rpc = create_rpc(deadline)
    self.fetch_statistics_async(rpc)
    return rpc.get_result()

  def __repr__(self):
    ATTRS = ['name']
    if self._app:

      ATTRS.append('_app')
    properties = ['%s=%r' % (attr, getattr(self, attr)) for attr in ATTRS]
    return 'Queue<%s>' % ', '.join(properties)

  def __eq__(self, o):
    if not isinstance(o, Queue):
      return NotImplemented
    return self.name == o.name and self._app == o._app

  def __ne__(self, o):
    if not isinstance(o, Queue):
      return NotImplemented
    return self.name != o.name or self._app != o._app




def add(*args, **kwargs):
  """Convenience method that creates a task and adds it to a queue.

  All parameters are optional.

  Push tasks cannot be added to queues in pull mode. Similarly, pull tasks
  cannot be added to queues in push mode.

  Args:
    payload: The payload data for this task that will be delivered to the
        webhook or backend as the HTTP request body (for push queues) or
        fetched by workers as part of the response from `lease_tasks()` (for
        pull queues). This argument is only allowed for `POST` and `PUT`
        methods and pull tasks.
    queue_name: Name of queue to insert task into. If a name is not supplied,
        defaults to the default queue.
    name: Name to give the task; if not specified, a name will be
        auto-generated when added to a queue and assigned to this object. The
        name must match the `_TASK_NAME_PATTERN` regular expression.
    method: Method to use when accessing the webhook; the default value is
        `POST`. Other accepted values are `GET`, `PUT`, `DELETE`, `HEAD`,
        or `PULL`. Do not specify a method for push tasks, as the method will
        default to `POST` and post assigned tasks to the web hook at the
        `url` you specify. If you set the `method` to `PULL`, the task will not
        be automatically delivered to the webhook; instead, it will remain in
        the queue until leased.
    url: Relative URL where the webhook that should handle this task is
        located for this application. You can use a query string unless this
        argument is used in a `POST` method. You cannot use this argument in a
        pull task.
    headers: Dictionary of headers to pass to the webhook. Values in the
        dictionary can be iterable to indicate repeated header fields. If you
        do not specify a `Content-Type` header for a `PUSH` method, the
        default header (`text/plain`) will be used. If you specify a `Host`
        header for a `PUSH` method, do not specify a `target` argument. You
        cannot use a `header` argument in a pull task. Any headers that use
        the `X-AppEngine` prefix will also be dropped.
    params: Dictionary of parameters to use for this task. For `POST` and
        pull requests, the values in the dictionary can be iterable to
        indicate repeated parameters, and these parameters will be encoded as
        `application/x-www-form-urlencoded` and set to the payload. For both
        `POST` and pull requests, do not specify parameters if you have
        already specified a payload. For `PUT` requests, parameters are
        converted to a query string if the URL contains a query string, or if
        the task already has a payload. For `PUT` requests, do not specify
        parameters if the URL already contains a query string and the method
        is `GET`. For all other methods, the parameters will be converted to a
        query string.
    transactional: Optional. If `True`, adds tasks if and only if the
        enclosing transaction is successfully committed. An error will be
        returned if this argument is set to `True` in the absence of an
        enclosing transaction. If `False`, adds the tasks immediately,
        ignoring any enclosing transaction's success or failure.
    countdown: Time in seconds into the future that this task should run or be
        leased. Defaults to zero. Do not specify this argument if you
        specified an `eta`.
    eta: A `datetime.datetime` that specifies the absolute earliest time at
        which the task should run. You cannot specify this argument if the
        `countdown` argument is specified. This argument can be time
        zone-aware or time zone-naive, or set to a time in the past. If the
        argument is set to None, the default value is now. For pull tasks, no
        worker can lease the task before the time indicated by the `eta`
        argument.
    retry_options: `TaskRetryOptions` used to control when the task will be
        retried if it fails. For pull tasks, you can only specify the
        `task_retry_limit` option to specify the number of times that a task
        can be leased before it is deleted from the queue. For push tasks,
        you can specify the `min_backoff_seconds`, `max_backoff_seconds`,
        `task_age_limit`, `max_doublings`, and `task_retry_limit` options.
    tag: The tag to be used when grouping by tag (pull tasks only).
    target: Push tasks only; specifies the alternate version or backend on
        which to run this task, or `DEFAULT_APP_VERSION` to run on the
        application's default version. You can specify a module or version, a
        frontend version, or a backend on which to run this task. The string
        that you specify will be prepended to the domain name of your app. If
        you specify the `target` argument, do not specify a `Host` header in
        the dictionary for the `headers` argument.

  Returns:
    The task that was added to the queue.

  Raises:
    BadTransactionStateError: If the transactional argument is true but this
        call is being made outside of the context of a transaction.
    InvalidEtaError: If the `eta` is set too far into the future.
    InvalidQueueModeError: If a pull task is added to a queue in push mode, or a
        task with `method` not equal to `PULL` is added to a queue in pull mode.
    InvalidTagError: If the tag is too long.
    InvalidTaskError: If any of the parameters are invalid.
    InvalidTaskNameError: If the task name is invalid.
    InvalidUrlError: If the task URL is invalid or too long.
    TaskTooLargeError: If the task with its associated payload is too large.
    TransactionalRequestTooLargeError: If transactional is `True` and the
        total size of the tasks and supporting request data exceeds the
        `MAX_TRANSACTIONAL_REQUEST_SIZE_BYTES` quota.
  """
  transactional = kwargs.pop('transactional', False)
  queue_name = kwargs.pop('queue_name', _DEFAULT_QUEUE)
  return Task(*args, **kwargs).add(
      queue_name=queue_name, transactional=transactional)
