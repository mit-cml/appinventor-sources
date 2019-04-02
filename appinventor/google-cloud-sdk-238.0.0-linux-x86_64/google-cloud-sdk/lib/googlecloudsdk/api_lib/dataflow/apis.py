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
"""Helpers for interacting with the Cloud Dataflow API.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import exceptions as apitools_exceptions

from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.api_lib.util import exceptions
from googlecloudsdk.core import properties
import six

DATAFLOW_API_NAME = 'dataflow'
DATAFLOW_API_VERSION = 'v1b3'
DATAFLOW_API_DEFAULT_REGION = 'us-central1'


def GetMessagesModule():
  return apis.GetMessagesModule(DATAFLOW_API_NAME, DATAFLOW_API_VERSION)


def GetClientInstance():
  return apis.GetClientInstance(DATAFLOW_API_NAME, DATAFLOW_API_VERSION)


def GetProject():
  return properties.VALUES.core.project.Get(required=True)


class Jobs(object):
  """The Jobs set of Dataflow API functions."""

  GET_REQUEST = GetMessagesModule().DataflowProjectsLocationsJobsGetRequest
  LIST_REQUEST = GetMessagesModule().DataflowProjectsLocationsJobsListRequest
  AGGREGATED_LIST_REQUEST = GetMessagesModule(
  ).DataflowProjectsJobsAggregatedRequest
  UPDATE_REQUEST = GetMessagesModule(
  ).DataflowProjectsLocationsJobsUpdateRequest

  @staticmethod
  def GetService():
    return GetClientInstance().projects_locations_jobs

  @staticmethod
  def Get(job_id, project_id=None, region_id=None, view=None):
    """Calls the Dataflow Jobs.Get method.

    Args:
      job_id: Identifies a single job.
      project_id: The project which owns the job.
      region_id: The regional endpoint where the job lives.
      view: (DataflowProjectsJobsGetRequest.ViewValueValuesEnum) Level of
        information requested in response.
    Returns:
      (Job)
    """
    project_id = project_id or GetProject()
    region_id = region_id or DATAFLOW_API_DEFAULT_REGION
    request = GetMessagesModule().DataflowProjectsLocationsJobsGetRequest(
        jobId=job_id, location=region_id, projectId=project_id, view=view)
    try:
      return Jobs.GetService().Get(request)
    except apitools_exceptions.HttpError as error:
      raise exceptions.HttpException(error)

  @staticmethod
  def Cancel(job_id, project_id=None, region_id=None):
    """Cancels a job by calling the Jobs.Update method.

    Args:
      job_id: Identifies a single job.
      project_id: The project which owns the job.
      region_id: The regional endpoint where the job lives.
    Returns:
      (Job)
    """
    project_id = project_id or GetProject()
    region_id = region_id or DATAFLOW_API_DEFAULT_REGION
    job = GetMessagesModule().Job(requestedState=(GetMessagesModule(
    ).Job.RequestedStateValueValuesEnum.JOB_STATE_CANCELLED))
    request = GetMessagesModule().DataflowProjectsLocationsJobsUpdateRequest(
        jobId=job_id, location=region_id, projectId=project_id, job=job)
    try:
      return Jobs.GetService().Update(request)
    except apitools_exceptions.HttpError as error:
      raise exceptions.HttpException(error)

  @staticmethod
  def Drain(job_id, project_id=None, region_id=None):
    """Drains a job by calling the Jobs.Update method.

    Args:
      job_id: Identifies a single job.
      project_id: The project which owns the job.
      region_id: The regional endpoint where the job lives.
    Returns:
      (Job)
    """
    project_id = project_id or GetProject()
    region_id = region_id or DATAFLOW_API_DEFAULT_REGION
    job = GetMessagesModule().Job(requestedState=(
        GetMessagesModule().Job.RequestedStateValueValuesEnum.JOB_STATE_DRAINED
    ))
    request = GetMessagesModule().DataflowProjectsLocationsJobsUpdateRequest(
        jobId=job_id, location=region_id, projectId=project_id, job=job)
    try:
      return Jobs.GetService().Update(request)
    except apitools_exceptions.HttpError as error:
      raise exceptions.HttpException(error)


class Metrics(object):
  """The Metrics set of Dataflow API functions."""

  GET_REQUEST = GetMessagesModule(
  ).DataflowProjectsLocationsJobsGetMetricsRequest

  @staticmethod
  def GetService():
    return GetClientInstance().projects_locations_jobs

  @staticmethod
  def Get(job_id, project_id=None, region_id=None, start_time=None):
    """Calls the Dataflow Metrics.Get method.

    Args:
      job_id: The job to get messages for.
      project_id: The project which owns the job.
      region_id: The regional endpoint of the job.
      start_time: Return only metric data that has changed since this time.
        Default is to return all information about all metrics for the job.
    Returns:
      (MetricUpdate)
    """
    project_id = project_id or GetProject()
    region_id = region_id or DATAFLOW_API_DEFAULT_REGION
    request = GetMessagesModule(
    ).DataflowProjectsLocationsJobsGetMetricsRequest(
        jobId=job_id,
        location=region_id,
        projectId=project_id,
        startTime=start_time)
    try:
      return Metrics.GetService().GetMetrics(request)
    except apitools_exceptions.HttpError as error:
      raise exceptions.HttpException(error)


class Templates(object):
  """The Templates set of Dataflow API functions."""

  CREATE_REQUEST = GetMessagesModule().CreateJobFromTemplateRequest
  PARAMETERS_VALUE = CREATE_REQUEST.ParametersValue

  @staticmethod
  def GetService():
    return GetClientInstance().projects_locations_templates

  @staticmethod
  def Create(project_id=None,
             region_id=None,
             gcs_location=None,
             staging_location=None,
             parameters=None,
             job_name=None,
             service_account_email=None,
             zone=None,
             max_workers=None,
             num_workers=None,
             worker_machine_type=None,
             network=None,
             subnetwork=None):
    """Calls the Dataflow Templates.CreateFromJob method.

    Args:
      project_id: The project which owns the job.
      region_id: The regional endpoint where the job lives.
      gcs_location: The location of the template.
      staging_location: The location to stage temporary files.
      parameters: Parameters to pass to the template.
      job_name: The name to assign to the job.
      service_account_email: The service account to run the workers as.
      zone: The zone to run the workers in.
      max_workers: The maximum number of workers to run.
      num_workers: The initial number of workers to use.
      worker_machine_type: The type of machine to use for workers.
      network: The network for launching instances to run your pipeline.
      subnetwork: The subnetwork for launching instances to run your pipeline.

    Returns:
      (Job)
    """
    params_list = []
    for k, v in six.iteritems(parameters) if parameters else {}:
      params_list.append(
          Templates.PARAMETERS_VALUE.AdditionalProperty(
              key=k, value=v))

    region_id = region_id or DATAFLOW_API_DEFAULT_REGION
    body = Templates.CREATE_REQUEST(
        gcsPath=gcs_location,
        jobName=job_name,
        location=region_id,
        environment=GetMessagesModule().RuntimeEnvironment(
            serviceAccountEmail=service_account_email,
            zone=zone,
            maxWorkers=max_workers,
            numWorkers=num_workers,
            network=network,
            subnetwork=subnetwork,
            machineType=worker_machine_type,
            tempLocation=staging_location),
        parameters=Templates.PARAMETERS_VALUE(additionalProperties=params_list)
        if parameters else None)
    request = GetMessagesModule(
    ).DataflowProjectsLocationsTemplatesCreateRequest(
        projectId=project_id or GetProject(),
        location=region_id,
        createJobFromTemplateRequest=body)

    try:
      return Templates.GetService().Create(request)
    except apitools_exceptions.HttpError as error:
      raise exceptions.HttpException(error)


class Messages(object):
  """The Messages set of Dataflow API functions."""

  LIST_REQUEST = GetMessagesModule(
  ).DataflowProjectsLocationsJobsMessagesListRequest

  @staticmethod
  def GetService():
    return GetClientInstance().projects_locations_jobs_messages

  @staticmethod
  def List(job_id,
           project_id=None,
           region_id=None,
           minimum_importance=None,
           start_time=None,
           end_time=None,
           page_size=None,
           page_token=None):
    """Calls the Dataflow Metrics.Get method.

    Args:
      job_id: The job to get messages about.
      project_id: The project which owns the job.
      region_id: The regional endpoint of the job.
      minimum_importance: Filter to only get messages with importance >= level
      start_time: If specified, return only messages with timestamps >=
        start_time. The default is the job creation time (i.e. beginning of
        messages).
      end_time: Return only messages with timestamps < end_time. The default is
        now (i.e. return up to the latest messages available).
      page_size: If specified, determines the maximum number of messages to
        return.  If unspecified, the service may choose an appropriate default,
        or may return an arbitrarily large number of results.
      page_token: If supplied, this should be the value of next_page_token
        returned by an earlier call. This will cause the next page of results to
        be returned.
    Returns:
      (ListJobMessagesResponse)
    """
    project_id = project_id or GetProject()
    region_id = region_id or DATAFLOW_API_DEFAULT_REGION
    request = GetMessagesModule(
    ).DataflowProjectsLocationsJobsMessagesListRequest(
        jobId=job_id,
        location=region_id,
        projectId=project_id,
        startTime=start_time,
        endTime=end_time,
        minimumImportance=minimum_importance,
        pageSize=page_size,
        pageToken=page_token)
    try:
      return Messages.GetService().List(request)
    except apitools_exceptions.HttpError as error:
      raise exceptions.HttpException(error)
