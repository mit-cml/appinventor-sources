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
"""Constructs to poll compute operations."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import exceptions
from googlecloudsdk.api_lib.util import waiter
from googlecloudsdk.core import exceptions as core_exceptions
from googlecloudsdk.core import resources
from six.moves import zip


class Error(exceptions.Error):
  """Errors raised by this module."""


class OperationErrors(Error):
  """Encapsulates multiple errors reported about single operation."""

  def __init__(self, errors):
    messages = [error.message for error in errors]
    super(OperationErrors, self).__init__(', '.join(messages))


class Poller(waiter.OperationPoller):
  """Compute operations poller."""

  def __init__(self, resource_service, target_ref=None):
    """Initializes poller for compute operations.

    Args:
      resource_service: apitools.base.py.base_api.BaseApiService,
          service representing the target of operation.
      target_ref: Resource, optional reference to the expected target of the
          operation. If not provided operation.targetLink will be used instead.
    """
    self.resource_service = resource_service
    self.client = resource_service.client
    self.messages = self.client.MESSAGES_MODULE
    self.status_enum = self.messages.Operation.StatusValueValuesEnum
    self.target_ref = target_ref

  def IsDone(self, operation):
    """Overrides."""
    if operation.error:
      raise OperationErrors(operation.error.errors)

    return operation.status == self.status_enum.DONE

  def Poll(self, operation_ref):
    """Overrides."""
    if hasattr(operation_ref, 'zone'):
      service = self.client.zoneOperations
    elif hasattr(operation_ref, 'region'):
      service = self.client.regionOperations
    else:
      service = self.client.globalOperations

    return service.Get(service.GetRequestType('Get')(
        **operation_ref.AsDict()))

  def GetResult(self, operation):
    """Overrides."""
    request_type = self.resource_service.GetRequestType('Get')
    target_ref = (self.target_ref
                  or resources.REGISTRY.Parse(operation.targetLink))
    return self.resource_service.Get(
        request_type(**target_ref.AsDict()))


class OperationBatch(object):
  """Wrapper class for a set of batched operations."""

  def __init__(self, operation_refs):
    self._operation_refs = operation_refs or []
    self._responses = {}

  def SetResponse(self, operation_ref, response):
    self._responses[operation_ref] = response

  def GetResponse(self, operation_ref):
    return self._responses.get(operation_ref)

  def GetWithResponse(self, response_func):
    for op in self._operation_refs:
      if response_func(self._responses.get(op)):
        yield op

  def __iter__(self):
    return iter(self._operation_refs)

  def __str__(self):
    return '[{0}]'.format(', '.join(str(r) for r in self._operation_refs))


class BatchPoller(waiter.OperationPoller):
  """Compute operations batch poller."""

  def __init__(self, compute_adapter, resource_service, target_refs=None):
    """Initializes poller for compute operations.

    Args:
      compute_adapter: googlecloudsdk.api_lib.compute.client_adapter
          .ClientAdapter.
      resource_service: apitools.base.py.base_api.BaseApiService,
          service representing the target of operation.
      target_refs: Resources, optional references to the expected targets of the
          operations. If not provided operation.targetLink will be used instead.
    """
    self._compute_adapter = compute_adapter
    self._resource_service = resource_service
    self._client = compute_adapter.apitools_client
    self._messages = compute_adapter.messages
    self._status_enum = self._messages.Operation.StatusValueValuesEnum
    self._target_refs = target_refs

  def IsDone(self, operation_batch):
    """Overrides."""
    is_done = True
    for operation_ref in operation_batch:
      response = operation_batch.GetResponse(operation_ref)
      is_done = is_done and response.status == self._status_enum.DONE
    return is_done

  def Poll(self, operation_batch):
    """Overrides."""
    requests = []

    not_done = list(operation_batch.GetWithResponse(
        lambda r: r is None or r.status != self._status_enum.DONE))
    for operation_ref in not_done:
      if hasattr(operation_ref, 'zone'):
        service = self._client.zoneOperations
      elif hasattr(operation_ref, 'region'):
        service = self._client.regionOperations
      else:
        service = self._client.globalOperations

      request_type = service.GetRequestType('Get')
      requests.append((service,
                       'Get',
                       request_type(**operation_ref.AsDict())))

    errors_to_collect = []
    responses = self._compute_adapter.BatchRequests(
        requests, errors_to_collect)
    for response, operation_ref in zip(responses, not_done):
      operation_batch.SetResponse(operation_ref, response)
      if response is not None and response.error:
        errors_to_collect.append(OperationErrors(response.error.errors))

    if errors_to_collect:
      raise core_exceptions.MultiError(errors_to_collect)

    return operation_batch

  def GetResult(self, operation_batch):
    """Overrides."""
    requests = []
    request_type = self._resource_service.GetRequestType('Get')
    target_refs = (
        self._target_refs or
        [resources.REGISTRY.Parse(
            operation_batch.GetResponse(operation_ref).targetLink)
         for operation_ref in operation_batch])

    for target_ref in target_refs:
      requests.append((
          self._resource_service,
          'Get',
          request_type(**target_ref.AsDict())))

    errors_to_collect = []
    responses = self._compute_adapter.BatchRequests(requests, errors_to_collect)
    if errors_to_collect:
      raise core_exceptions.MultiError(errors_to_collect)
    return responses
