# -*- coding: utf-8 -*- #
# Copyright 2018 Google Inc. All Rights Reserved.
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
"""services helper functions."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import exceptions as apitools_exceptions
from apitools.base.py import list_pager
from googlecloudsdk.api_lib.services import exceptions
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.util import retry

_PROJECT_RESOURCE = 'projects/%s'
_PROJECT_SERVICE_RESOURCE = 'projects/%s/services/%s'


def EnableApiCall(project, service):
  """Make API call to enable a specific service.

  Args:
    project: The project for which to enable the service.
    service: The identifier of the service to enable, for example
      'serviceusage.googleapis.com'.

  Raises:
    exceptions.EnableServicePermissionDeniedException: when enabling API fails.
    apitools_exceptions.HttpError: Another miscellaneous error with the service.

  Returns:
    The result of the operation
  """
  client = _GetClientInstance()
  messages = client.MESSAGES_MODULE

  # TODO(b/78464430): use resource argument.
  request = messages.ServiceusageServicesEnableRequest(
      name=_PROJECT_SERVICE_RESOURCE % (project, service))
  try:
    return client.services.Enable(request)
  except (apitools_exceptions.HttpForbiddenError,
          apitools_exceptions.HttpNotFoundError) as e:
    exceptions.ReraiseError(e,
                            exceptions.EnableServicePermissionDeniedException)


def BatchEnableApiCall(project, services):
  """Make API call to batch enable services.

  Args:
    project: The project for which to enable the services.
    services: Iterable of identifiers of services to enable.

  Raises:
    exceptions.EnableServicePermissionDeniedException: when enabling API fails.
    apitools_exceptions.HttpError: Another miscellaneous error with the service.

  Returns:
    The result of the operation
  """
  client = _GetClientInstance()
  messages = client.MESSAGES_MODULE

  request = messages.ServiceusageServicesBatchEnableRequest(
      batchEnableServicesRequest=messages.BatchEnableServicesRequest(
          serviceIds=services),
      parent=_PROJECT_RESOURCE % project)
  try:
    return client.services.BatchEnable(request)
  except (apitools_exceptions.HttpForbiddenError,
          apitools_exceptions.HttpNotFoundError) as e:
    exceptions.ReraiseError(e,
                            exceptions.EnableServicePermissionDeniedException)


def DisableApiCall(project, service, force=False):
  """Make API call to disable a specific service.

  Args:
    project: The project for which to enable the service.
    service: The identifier of the service to disable, for example
      'serviceusage.googleapis.com'.
    force: disable the service even if there are enabled services which depend
      on it. This also disables the services which depend on the service to be
      disabled.

  Raises:
    exceptions.EnableServicePermissionDeniedException: when disabling API fails.
    apitools_exceptions.HttpError: Another miscellaneous error with the service.

  Returns:
    The result of the operation
  """
  client = _GetClientInstance()
  messages = client.MESSAGES_MODULE

  request = messages.ServiceusageServicesDisableRequest(
      name=_PROJECT_SERVICE_RESOURCE % (project, service),
      disableServiceRequest=messages.DisableServiceRequest(
          disableDependentServices=force,),
  )
  try:
    return client.services.Disable(request)
  except (apitools_exceptions.HttpForbiddenError,
          apitools_exceptions.HttpNotFoundError) as e:
    exceptions.ReraiseError(e,
                            exceptions.EnableServicePermissionDeniedException)
  except apitools_exceptions.HttpBadRequestError as e:
    log.status.Print('Provide the --force flag if you wish to disable '
                     'dependent services.')
    exceptions.ReraiseError(e, exceptions.Error)


def ListServices(project, enabled, page_size, limit):
  """Make API call to list services.

  Args:
    project: The project for which to list services.
    enabled: List only enabled services.
    page_size: The page size to list.
    limit: The max number of services to display.

  Raises:
    exceptions.ListServicesPermissionDeniedException: when listing services
    fails.
    apitools_exceptions.HttpError: Another miscellaneous error with the service.

  Returns:
    The list of services
  """
  client = _GetClientInstance()
  messages = client.MESSAGES_MODULE

  if enabled:
    service_filter = 'state:ENABLED'
  else:
    service_filter = None
  request = messages.ServiceusageServicesListRequest(
      filter=service_filter, parent=_PROJECT_RESOURCE % project)
  try:
    return list_pager.YieldFromList(
        client.services,
        request,
        limit=limit,
        batch_size_attribute='pageSize',
        batch_size=page_size,
        field='services')
  except (apitools_exceptions.HttpForbiddenError,
          apitools_exceptions.HttpNotFoundError) as e:
    exceptions.ReraiseError(e,
                            exceptions.EnableServicePermissionDeniedException)


def GetOperation(name):
  """Make API call to get an operation.

  Args:
    name: The name of operation.

  Raises:
    exceptions.OperationErrorException: when the getting operation API fails.
    apitools_exceptions.HttpError: Another miscellaneous error with the service.

  Returns:
    The result of the operation
  """
  client = _GetClientInstance()
  messages = client.MESSAGES_MODULE
  request = messages.ServiceusageOperationsGetRequest(name=name)
  try:
    return client.operations.Get(request)
  except (apitools_exceptions.HttpForbiddenError,
          apitools_exceptions.HttpNotFoundError) as e:
    exceptions.ReraiseError(e, exceptions.OperationErrorException)


def WaitOperation(name):
  """Wait till the operation is done.

  Args:
    name: The name of operation.

  Raises:
    exceptions.OperationErrorException: when the getting operation API fails.
    apitools_exceptions.HttpError: Another miscellaneous error with the service.

  Returns:
    The result of the operation
  """

  def _CheckOp(name, result):
    op = GetOperation(name)
    if op.done:
      result.append(op)
    return not op.done

  # Wait for no more than 30 minutes while retrying the Operation retrieval
  result = []
  try:
    retry.Retryer(
        exponential_sleep_multiplier=1.1,
        wait_ceiling_ms=10000,
        max_wait_ms=30 * 60 * 1000).RetryOnResult(
            _CheckOp, [name, result], should_retry_if=True, sleep_ms=2000)
  except retry.MaxRetrialsException:
    raise exceptions.TimeoutError('Timed out while waiting for '
                                  'operation {0}. Note that the operation '
                                  'is still pending.'.format(name))
  return result[0] if result else None


def _GetClientInstance():
  # Specifically disable resource quota in all cases for service management.
  # We need to use this API to turn on APIs and sometimes the user doesn't have
  # this API turned on. We should always use the shared project to do this
  # so we can bootstrap users getting the appropriate APIs enabled. If the user
  # has explicitly set the quota project, then respect that.
  enable_resource_quota = (
      properties.VALUES.billing.quota_project.IsExplicitlySet())
  return apis.GetClientInstance(
      'serviceusage', 'v1', enable_resource_quota=enable_resource_quota)
