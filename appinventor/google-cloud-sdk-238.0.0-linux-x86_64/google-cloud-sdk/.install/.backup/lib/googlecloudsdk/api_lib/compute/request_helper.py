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
"""Module for making API requests."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import copy
import json

from googlecloudsdk.api_lib.compute import batch_helper
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.api_lib.compute import waiters
from googlecloudsdk.core import log
import six
from six.moves import zip  # pylint: disable=redefined-builtin


def _RequestsAreListRequests(requests):
  list_requests = [method in ('List', 'AggregatedList')
                   for _, method, _ in requests]
  if all(list_requests):
    return True
  elif not any(list_requests):
    return False
  else:
    raise ValueError(
        'All requests must be either list requests or non-list requests.')


def _HandleJsonList(response, service, method, errors):
  """Extracts data from one *List response page as JSON and stores in dicts.

  Args:
    response: str, The *List response in JSON
    service: The service which responded to *List request
    method: str, Method used to list resources. One of 'List' or
    'AggregatedList'.
    errors: list, Errors from response will be appended to  this list.

  Returns:
    Pair of:
    - List of items returned in response as dicts
    - Next page token (if present, otherwise None).
  """
  items = []

  response = json.loads(response)

  # If the request is a list call, then yield the items directly.
  if method == 'List':
    items = response.get('items', [])

  # If the request is an aggregatedList call, then do all the
  # magic necessary to get the actual resources because the
  # aggregatedList responses are very complicated data
  # structures...
  elif method == 'AggregatedList':
    items_field_name = service.GetMethodConfig(
        'AggregatedList').relative_path.split('/')[-1]
    for scope_result in six.itervalues(response['items']):
      # If the given scope is unreachable, record the warning
      # message in the errors list.
      warning = scope_result.get('warning', None)
      if warning and warning['code'] == 'UNREACHABLE':
        errors.append((None, warning['message']))

      items.extend(scope_result.get(items_field_name, []))

  return items, response.get('nextPageToken', None)


def _HandleMessageList(response, service, method, errors):
  """Extracts data from one *List response page as Message object."""
  items = []

  # If the request is a list call, then yield the items directly.
  if method == 'List':
    items = response.items

  # If the request is an aggregatedList call, then do all the
  # magic necessary to get the actual resources because the
  # aggregatedList responses are very complicated data
  # structures...
  else:
    items_field_name = service.GetMethodConfig(
        'AggregatedList').relative_path.split('/')[-1]
    for scope_result in response.items.additionalProperties:
      # If the given scope is unreachable, record the warning
      # message in the errors list.
      warning = scope_result.value.warning
      if warning and warning.code == warning.CodeValueValuesEnum.UNREACHABLE:
        errors.append((None, warning.message))

      items.extend(getattr(scope_result.value, items_field_name))

  return items, response.nextPageToken


def _ListCore(requests, http, batch_url, errors, response_handler):
  """Makes a series of list and/or aggregatedList batch requests.

  Args:
    requests: A list of requests to make. Each element must be a 3-element
      tuple where the first element is the service, the second element is
      the method ('List' or 'AggregatedList'), and the third element
      is a protocol buffer representing either a list or aggregatedList
      request.
    http: An httplib2.Http-like object.
    batch_url: The handler for making batch requests.
    errors: A list for capturing errors. If any response contains an error,
      it is added to this list.
    response_handler: The function to extract information responses.

  Yields:
    Resources encapsulated in format chosen by response_handler as they are
      received from the server.
  """
  while requests:
    responses, request_errors = batch_helper.MakeRequests(
        requests=requests,
        http=http,
        batch_url=batch_url)
    errors.extend(request_errors)

    new_requests = []

    for i, response in enumerate(responses):
      if not response:
        continue

      service, method, request_protobuf = requests[i]

      items, next_page_token = response_handler(response, service, method,
                                                errors)
      for item in items:
        yield item

      if next_page_token:
        new_request_protobuf = copy.deepcopy(request_protobuf)
        new_request_protobuf.pageToken = next_page_token
        new_requests.append((service, method, new_request_protobuf))

    requests = new_requests


def _List(requests, http, batch_url, errors):
  """Makes a series of list and/or aggregatedList batch requests.

  Args:
    requests: A list of requests to make. Each element must be a 3-element
      tuple where the first element is the service, the second element is
      the method ('List' or 'AggregatedList'), and the third element
      is a protocol buffer representing either a list or aggregatedList
      request.
    http: An httplib2.Http-like object.
    batch_url: The handler for making batch requests.
    errors: A list for capturing errors. If any response contains an error,
      it is added to this list.

  Returns:
    Resources encapsulated as protocol buffers as they are received
      from the server.
  """
  return _ListCore(requests, http, batch_url, errors, _HandleMessageList)


def ListJson(requests, http, batch_url, errors):
  """Makes a series of list and/or aggregatedList batch requests.

  This function does all of:
  - Sends batch of List/AggragatedList requests
  - Extracts items from responses
  - Handles pagination

  All requests must be sent to the same client - Compute.

  Args:
    requests: A list of requests to make. Each element must be a 3-element
      tuple where the first element is the service, the second element is
      the method ('List' or 'AggregatedList'), and the third element
      is a protocol buffer representing either a list or aggregatedList
      request.
    http: An httplib2.Http-like object.
    batch_url: The handler for making batch requests.
    errors: A list for capturing errors. If any response contains an error,
      it is added to this list.

  Yields:
    Resources in dicts as they are received from the server.
  """
  # This is compute-specific helper. It is assumed at this point that all
  # requests are being sent to the same client (for example Compute).
  with requests[0][0].client.JsonResponseModel():
    for item in _ListCore(requests, http, batch_url, errors, _HandleJsonList):
      yield item


def MakeRequests(requests, http, batch_url, errors, progress_tracker=None):
  """Makes one or more requests to the API.

  Each request can be either a synchronous API call or an asynchronous
  one. For synchronous calls (e.g., get and list), the result from the
  server is yielded immediately. For asynchronous calls (e.g., calls
  that return operations like insert), this function waits until the
  operation reaches the DONE state and fetches the corresponding
  object and yields that object (nothing is yielded for deletions).

  Currently, a heterogenous set of synchronous calls can be made
  (e.g., get request to fetch a disk and instance), however, the
  asynchronous requests must be homogenous (e.g., they must all be the
  same verb on the same collection). In the future, heterogenous
  asynchronous requests will be supported. For now, it is up to the
  client to ensure that the asynchronous requests are
  homogenous. Synchronous and asynchronous requests can be mixed.

  Args:
    requests: A list of requests to make. Each element must be a 3-element
      tuple where the first element is the service, the second element is
      the string name of the method on the service, and the last element
      is a protocol buffer representing the request.
    http: An httplib2.Http-like object.
    batch_url: The handler for making batch requests.
    errors: A list for capturing errors. If any response contains an error,
      it is added to this list.
    progress_tracker: progress tracker to be ticked while waiting for operations
                      to finish.

  Yields:
    A response for each request. For deletion requests, no corresponding
    responses are returned.
  """
  if _RequestsAreListRequests(requests):
    for item in _List(
        requests=requests, http=http, batch_url=batch_url, errors=errors):
      yield item
    return

  responses, new_errors = batch_helper.MakeRequests(
      requests=requests, http=http, batch_url=batch_url)
  errors.extend(new_errors)

  operation_service = None
  resource_service = None
  project = None

  # Collects all operation objects in a list so they can be waited on
  # and yields all non-operation objects since non-operation responses
  # cannot be waited on.
  operations_data = []

  for request, response in zip(requests, responses):
    if response is None:
      continue

    service, _, request_body = request
    if (isinstance(response, service.client.MESSAGES_MODULE.Operation) and
        service.__class__.__name__ not in (
            'GlobalOperationsService',
            'RegionOperationsService',
            'ZoneOperationsService',
            'GlobalAccountsOperationsService')):

      resource_service = service
      project = request_body.project

      if response.zone:
        operation_service = service.client.zoneOperations
      elif response.region:
        operation_service = service.client.regionOperations
      else:
        operation_service = service.client.globalOperations

      operations_data.append(
          waiters.OperationData(response, project, operation_service,
                                resource_service))

    else:
      yield response

  if operations_data:
    warnings = []
    for response in waiters.WaitForOperations(
        operations_data=operations_data,
        http=http,
        batch_url=batch_url,
        warnings=warnings,
        progress_tracker=progress_tracker,
        errors=errors):
      yield response

    if warnings:
      log.warning(utils.ConstructList('Some requests generated warnings:',
                                      warnings))
