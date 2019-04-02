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

"""service-management enable helper functions."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import exceptions as apitools_exceptions
from apitools.base.py import list_pager

from googlecloudsdk.api_lib.services import exceptions
from googlecloudsdk.api_lib.services import services_util
from googlecloudsdk.core import log


def EnableServiceApiCall(project_id, service_name):
  """Make API call to enable a specific API.

  Args:
    project_id: The ID of the project for which to enable the service.
    service_name: The name of the service to enable on the project.

  Raises:
    exceptions.EnableServicePermissionDeniedException: when enabling the API
        fails.
    apitools_exceptions.HttpError: Another miscellaneous error with the enabling
        service.

  Returns:
    The result of the Enable operation
  """

  client = services_util.GetClientInstance()
  messages = services_util.GetMessagesModule()

  request = messages.ServicemanagementServicesEnableRequest(
      serviceName=service_name,
      enableServiceRequest=messages.EnableServiceRequest(
          consumerId='project:' + project_id
      )
  )

  try:
    return client.services.Enable(request)
  except (apitools_exceptions.HttpForbiddenError,
          apitools_exceptions.HttpNotFoundError) as e:
    # TODO(b/36865980): When backend supports it, differentiate errors.
    exceptions.ReraiseError(e,
                            exceptions.EnableServicePermissionDeniedException)


def IsServiceEnabled(project_id, service_name):
  """Return true if the service is enabled.

  Args:
    project_id: The ID of the project we want to query.
    service_name: The name of the service.

  Raises:
    exceptions.ListServicesPermissionDeniedException: if a 403 or 404
        error is returned by the List request.
    apitools_exceptions.HttpError: Another miscellaneous error with the listing
        service.

  Returns:
    True if the service is enabled, false otherwise.
  """

  client = services_util.GetClientInstance()

  # Get the list of enabled services.
  request = services_util.GetEnabledListRequest(project_id)
  try:
    for service in list_pager.YieldFromList(
        client.services,
        request,
        batch_size_attribute='pageSize',
        field='services'):
      # If the service is present in the list of enabled services, return
      # True, otherwise return False
      if service.serviceName.lower() == service_name.lower():
        return True
  except (apitools_exceptions.HttpForbiddenError,
          apitools_exceptions.HttpNotFoundError) as e:
    # TODO(b/36865980): When backend supports it, differentiate errors.
    exceptions.ReraiseError(e, exceptions.ListServicesPermissionDeniedException)
  return False


def EnableService(project_id, service_name, is_async=False):
  """Enable a service without checking if it is already enabled.

  Args:
    project_id: The ID of the project for which to enable the service.
    service_name: The name of the service to enable on the project.
    is_async: bool, if True, print the operation ID and return immediately,
           without waiting for the op to complete.

  Raises:
    exceptions.EnableServicePermissionDeniedException: when enabling the API
        fails with a 403 or 404 error code.
    api_lib_exceptions.HttpException: Another miscellaneous error with the
        servicemanagement service.
  """
  log.status.Print('Enabling service [{0}] on project [{1}]...'.format(
      service_name, project_id))

  # Enable the service
  operation = EnableServiceApiCall(project_id, service_name)

  # Process the enable operation
  services_util.ProcessOperationResult(operation, is_async)


def EnableServiceIfDisabled(project_id, service_name, is_async=False):
  """Check to see if the service is enabled, and if it is not, do so.

  Args:
    project_id: The ID of the project for which to enable the service.
    service_name: The name of the service to enable on the project.
    is_async: bool, if True, print the operation ID and return immediately,
           without waiting for the op to complete.

  Raises:
    exceptions.ListServicesPermissionDeniedException: if a 403 or 404 error
        is returned by the listing service.
    exceptions.EnableServicePermissionDeniedException: when enabling the API
        fails with a 403 or 404 error code.
    api_lib_exceptions.HttpException: Another miscellaneous error with the
        servicemanagement service.
  """

  # If the service is enabled, we can return
  if IsServiceEnabled(project_id, service_name):
    log.debug('Service [{0}] is already enabled for project [{1}]'.format(
        service_name, project_id))
    return

  EnableService(project_id, service_name, is_async=is_async)

