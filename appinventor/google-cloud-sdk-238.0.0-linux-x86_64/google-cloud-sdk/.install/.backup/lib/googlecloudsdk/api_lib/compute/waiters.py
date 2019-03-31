# -*- coding: utf-8 -*- #
# Copyright 2014 Google Inc. All Rights Reserved.
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
"""Utilities for waiting on Compute Engine operations."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import batch_helper
from googlecloudsdk.api_lib.compute import path_simplifier
from googlecloudsdk.command_lib.util import time_util
from googlecloudsdk.core import log

_POLLING_TIMEOUT_SEC = 60 * 30
_MAX_TIME_BETWEEN_POLLS_SEC = 5

# The set of possible operation types is {insert, delete, update,
# *.insert, *.delete, *.update} + all verbs. For example,
#
#        verb                    op type
#   Instances.setMedata        setMetdata
#   Instances.insert           insert
#   InsetanceTempalte.delete   compute.instanceTemplates.delete
# In our status reporting, we use the following
# mapping. Anything not in the map is reported as "Updated".
_HUMAN_FRIENDLY_OPERATION_TYPE_SUFFIXES = {
    'createSnapshot': {'past': 'created', 'present': 'create'},
    'recreateInstancesInstanceGroupManager': {
        'past': 'recreated', 'present': 'recreate'},
    'insert': {'past': 'created', 'present': 'create'},
    'delete': {'past': 'deleted', 'present': 'delete'},
    'update': {'past': 'updated', 'present': 'update'},
    'invalidateCache': {
        'past': 'completed invalidation for',
        'present': 'complete invalidation for'}}


def _HumanFrieldlyNamesForOp(op_type):
  for s in _HUMAN_FRIENDLY_OPERATION_TYPE_SUFFIXES:
    if op_type.endswith(s):
      return _HUMAN_FRIENDLY_OPERATION_TYPE_SUFFIXES.get(s)

  return {'past': 'updated', 'present': 'update'}


def _HumanFrieldlyNameForOpPastTense(op_type):
  return _HumanFrieldlyNamesForOp(op_type)['past']


def _HumanFrieldlyNameForOpPresentTense(op_type):
  return _HumanFrieldlyNamesForOp(op_type)['present']


def _IsDeleteOp(op_type):
  return _HumanFrieldlyNameForOpPastTense(op_type) == 'deleted'


def _RecordProblems(operation, warnings, errors):
  """Records any warnings and errors into the given lists."""
  for warning in operation.warnings or []:
    warnings.append(warning.message)
  if operation.error:
    for error in operation.error.errors or []:
      errors.append((operation.httpErrorStatusCode, error.message))


def _RecordUnfinishedOperations(operations, errors):
  """Adds error messages stating that the given operations timed out."""
  pending_resources = [operation.targetLink for operation in operations]
  errors.append(
      (None, ('Did not {action} the following resources within '
              '{timeout}s: {links}. These operations may still be '
              'underway remotely and may still succeed; use gcloud list '
              'and describe commands or '
              'https://console.developers.google.com/ to '
              'check resource state').format(
                  action=_HumanFrieldlyNameForOpPresentTense(
                      operations[0].operationType),
                  timeout=_POLLING_TIMEOUT_SEC,
                  links=', '.join(pending_resources))))


class OperationData(object):
  """Holds all information necessary to poll given operation.

  Fields:
    operation: An Operation object to poll.
    project: str, The project to which the resource belong.
    operation_service: The service that can be used to get operation
      object.
    resource_service: The service of the collection being mutated by
      the operation. If the operation type is not delete, this service
      is used to fetch the mutated object after the operation is done.
  """

  def __init__(self, operation, project, operation_service, resource_service):
    self.operation = operation
    self.project = project
    self.operation_service = operation_service
    self.resource_service = resource_service

  def __eq__(self, o):
    if not isinstance(o, OperationData):
      return False
    return (self.operation == o.operation and self.project == o.project and
            self.operation_service == o.operation_service and
            self.resource_service == o.resource_service)

  def __hash__(self):
    return (hash(self.operation) ^ hash(self.project) ^
            hash(self.operation_service) ^ hash(self.resource_service))

  def __ne__(self, o):
    return not self == o


def WaitForOperations(
    operations_data, http, batch_url, warnings, errors,
    progress_tracker=None, timeout=None):
  """Blocks until the given operations are done or until a timeout is reached.

  Args:
    operations_data: A list of OperationData objects holding Operations to poll.
    http: An HTTP object.
    batch_url: The URL to which batch requests should be sent.
    warnings: An output parameter for capturing warnings.
    errors: An output parameter for capturing errors.
    progress_tracker: progress tracker to tick while waiting for operations to
                      finish.
    timeout: The maximum amount of time, in seconds, to wait for the
      operations to reach the DONE state.

  Yields:
    The resources pointed to by the operations' targetLink fields if
    the operation type is not delete. Only resources whose
    corresponding operations reach done are yielded.
  """
  timeout = timeout or _POLLING_TIMEOUT_SEC

  # Operation -> OperationData mapping will be used to reify operation_service
  # and resource_service from operation_service.Get(operation) response.
  # It is necessary because poll operation is returning only response, but we
  # also need to get operation details to know the service to poll for all
  # unfinished_operations.
  operation_details = {}
  unfinished_operations = []
  for operation in operations_data:
    operation_details[operation.operation.selfLink] = operation
    unfinished_operations.append(operation.operation)

  responses = []
  start = time_util.CurrentTimeSec()
  sleep_sec = 0

  while unfinished_operations:
    if progress_tracker:
      progress_tracker.Tick()
    resource_requests = []
    operation_requests = []

    log.debug('Operations to inspect: %s', unfinished_operations)
    for operation in unfinished_operations:
      # Reify operation
      data = operation_details[operation.selfLink]
      project = data.project
      operation_service = data.operation_service
      resource_service = data.resource_service

      operation_type = operation_service.GetResponseType('Get')

      if operation.status == operation_type.StatusValueValuesEnum.DONE:
        # The operation has reached the DONE state, so we record any
        # problems it contains (if any) and proceed to get the target
        # resource if there were no problems and the operation is not
        # a deletion.

        _RecordProblems(operation, warnings, errors)

        # We shouldn't attempt to get the target resource if there was
        # anything wrong with the operation. Note that
        # httpErrorStatusCode is set only when the operation is not
        # successful.
        if (operation.httpErrorStatusCode and
            operation.httpErrorStatusCode != 200):  # httplib.OK
          continue

        # Just in case the server did not set httpErrorStatusCode but
        # the operation did fail, we check the "error" field.
        if operation.error:
          continue

        target_link = operation.targetLink

        # We shouldn't get the target resource if the operation type
        # is delete because there will be no resource left.
        if not _IsDeleteOp(operation.operationType):
          request = resource_service.GetRequestType('Get')(project=project)
          if operation.zone:
            request.zone = path_simplifier.Name(operation.zone)
          elif operation.region:
            request.region = path_simplifier.Name(operation.region)
          name_field = resource_service.GetMethodConfig(
              'Get').ordered_params[-1]
          setattr(request, name_field,
                  path_simplifier.Name(operation.targetLink))
          resource_requests.append((resource_service, 'Get', request))

        log.status.write('{0} [{1}].\n'.format(
            _HumanFrieldlyNameForOpPastTense(
                operation.operationType).capitalize(),
            target_link))

      else:
        # The operation has not reached the DONE state, so we add a
        # get request to poll the operation.
        request = operation_service.GetRequestType('Get')(
            operation=operation.name,
            project=project)
        if operation.zone:
          request.zone = path_simplifier.Name(operation.zone)
        elif operation.region:
          request.region = path_simplifier.Name(operation.region)
        operation_requests.append((operation_service, 'Get', request))

    requests = resource_requests + operation_requests
    if not requests:
      break

    responses, request_errors = batch_helper.MakeRequests(
        requests=requests,
        http=http,
        batch_url=batch_url)
    errors.extend(request_errors)

    unfinished_operations = []
    for response in responses:
      if isinstance(response, operation_type):
        unfinished_operations.append(response)
      else:
        yield response

    # If there are no more operations, we are done.
    if not unfinished_operations:
      break

    # Did we time out? If so, record the operations that timed out so
    # they can be reported to the user.
    if time_util.CurrentTimeSec() - start > timeout:
      log.debug('Timeout of %ss reached.', timeout)
      _RecordUnfinishedOperations(unfinished_operations, errors)
      break

    # Sleeps before trying to poll the operations again.
    sleep_sec += 1
    # Don't re-use sleep_sec, since we want to keep the same time increment
    sleep_time = min(sleep_sec, _MAX_TIME_BETWEEN_POLLS_SEC)
    log.debug('Sleeping for %ss.', sleep_time)
    time_util.Sleep(sleep_time)
