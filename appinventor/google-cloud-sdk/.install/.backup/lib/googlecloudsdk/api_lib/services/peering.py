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
"""services vpc-peering helper functions."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import exceptions as apitools_exceptions
from googlecloudsdk.api_lib.services import exceptions
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.core.util import retry


def CreateConnection(project_number, service, network, ranges):
  """Make API call to create a connection a specific service.

  Args:
    project_number: The number of the project for which to peer the service.
    service: The name of the service to peer with.
    network: The network in consumer project to peer with.
    ranges: The IP CIDR ranges for peering service to use.

  Raises:
    exceptions.CreateConnectionsPermissionDeniedException: when the create
        connection API fails.
    apitools_exceptions.HttpError: Another miscellaneous error with the peering
        service.

  Returns:
    The result of the peering operation
  """
  client = _GetClientInstance()
  messages = client.MESSAGES_MODULE

  # the API only takes project number, so we cannot use resource parser.
  request = messages.ServicenetworkingServicesConnectionsCreateRequest(
      parent='services/' + service,
      connection=messages.Connection(
          network='projects/%s/global/networks/%s' % (project_number, network),
          reservedPeeringRanges=ranges))
  try:
    return client.services_connections.Create(request)
  except (apitools_exceptions.HttpForbiddenError,
          apitools_exceptions.HttpNotFoundError) as e:
    exceptions.ReraiseError(
        e, exceptions.CreateConnectionsPermissionDeniedException)


def UpdateConnection(project_number, service, network, ranges, force):
  """Make API call to update a connection a specific service.

  Args:
    project_number: The number of the project for which to peer the service.
    service: The name of the service to peer with.
    network: The network in consumer project to peer with.
    ranges: The IP CIDR ranges for peering service to use.
    force: update the connection even if the update can be destructive.

  Raises:
    exceptions.CreateConnectionsPermissionDeniedException: when the create
        connection API fails.
    apitools_exceptions.HttpError: Another miscellaneous error with the peering
        service.

  Returns:
    The result of the peering operation
  """
  client = _GetClientInstance()
  messages = client.MESSAGES_MODULE

  # the API only takes project number, so we cannot use resource parser.
  request = messages.ServicenetworkingServicesConnectionsPatchRequest(
      name='services/%s/connections/-' % service,
      connection=messages.Connection(
          network='projects/%s/global/networks/%s' % (project_number, network),
          reservedPeeringRanges=ranges),
      force=force)
  try:
    return client.services_connections.Patch(request)
  except (apitools_exceptions.HttpForbiddenError,
          apitools_exceptions.HttpNotFoundError) as e:
    exceptions.ReraiseError(
        e, exceptions.CreateConnectionsPermissionDeniedException)


def ListConnections(project_number, service, network):
  """Make API call to list connections of a network for a specific service.

  Args:
    project_number: The number of the project for which to peer the service.
    service: The name of the service to peer with.
    network: The network in consumer project to peer with.

  Raises:
    exceptions.ListConnectionsPermissionDeniedException: when the list
    connections API fails.
    apitools_exceptions.HttpError: Another miscellaneous error with the peering
        service.

  Returns:
    The result of the peering operation
  """
  client = _GetClientInstance()
  messages = client.MESSAGES_MODULE

  # The API only takes project number, so we cannot use resource parser.
  request = messages.ServicenetworkingServicesConnectionsListRequest(
      parent='services/' + service,
      network='projects/{0}/global/networks/{1}'.format(project_number,
                                                        network))
  try:
    return client.services_connections.List(request).connections
  except (apitools_exceptions.HttpForbiddenError,
          apitools_exceptions.HttpNotFoundError) as e:
    exceptions.ReraiseError(e,
                            exceptions.ListConnectionsPermissionDeniedException)


def GetOperation(name):
  """Make API call to get an operation.

  Args:
    name: The name of operation.

  Raises:
    exceptions.OperationErrorException: when the getting operation API fails.
    apitools_exceptions.HttpError: Another miscellaneous error with the peering
        service.

  Returns:
    The result of the peering operation
  """
  client = _GetClientInstance()
  messages = client.MESSAGES_MODULE
  request = messages.ServicenetworkingOperationsGetRequest(name=name)
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
    apitools_exceptions.HttpError: Another miscellaneous error with the peering
        service.

  Returns:
    The result of the peering operation
  """

  def _CheckOp(name, result):  # pylint: disable=missing-docstring
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
  return apis.GetClientInstance('servicenetworking', 'v1', no_http=False)
