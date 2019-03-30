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

"""Common utilities for the gcloud dataproc tool."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import os
import time
import uuid
from apitools.base.py import encoding
from apitools.base.py import exceptions as apitools_exceptions
from googlecloudsdk.api_lib.dataproc import exceptions
from googlecloudsdk.api_lib.dataproc import storage_helpers
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.command_lib.export import util as export_util
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.console import console_attr
from googlecloudsdk.core.console import console_io
from googlecloudsdk.core.console import progress_tracker
import six

SCHEMA_DIR = os.path.join(os.path.dirname(__file__), 'schemas')


def FormatRpcError(error):
  """Returns a printable representation of a failed Google API's status.proto.

  Args:
    error: the failed Status to print.

  Returns:
    A ready-to-print string representation of the error.
  """
  log.debug('Error:\n' + encoding.MessageToJson(error))
  return error.message


def WaitForResourceDeletion(
    request_method,
    resource_ref,
    message,
    timeout_s=60,
    poll_period_s=5):
  """Poll Dataproc resource until it no longer exists."""
  with progress_tracker.ProgressTracker(message, autotick=True):
    start_time = time.time()
    while timeout_s > (time.time() - start_time):
      try:
        request_method(resource_ref)
      except apitools_exceptions.HttpNotFoundError:
        # Object deleted
        return
      except apitools_exceptions.HttpError as error:
        log.debug('Get request for [{0}] failed:\n{1}', resource_ref, error)

        # Do not retry on 4xx errors
        if IsClientHttpException(error):
          raise
      time.sleep(poll_period_s)
  raise exceptions.OperationTimeoutError(
      'Deleting resource [{0}] timed out.'.format(resource_ref))


def GetUniqueId():
  return uuid.uuid4().hex


class Bunch(object):
  """Class that converts a dictionary to javascript like object.

  For example:
      Bunch({'a': {'b': {'c': 0}}}).a.b.c == 0
  """

  def __init__(self, dictionary):
    for key, value in six.iteritems(dictionary):
      if isinstance(value, dict):
        value = Bunch(value)
      self.__dict__[key] = value


def AddJvmDriverFlags(parser):
  parser.add_argument(
      '--jar',
      dest='main_jar',
      help='The HCFS URI of jar file containing the driver jar.')
  parser.add_argument(
      '--class',
      dest='main_class',
      help=('The class containing the main method of the driver. Must be in a'
            ' provided jar or jar that is already on the classpath'))


def IsClientHttpException(http_exception):
  """Returns true if the http exception given is an HTTP 4xx error."""
  return http_exception.status_code >= 400 and http_exception.status_code < 500


# TODO(b/36056506): Use api_lib.utils.waiter
def WaitForOperation(dataproc, operation, message, timeout_s, poll_period_s=5):
  """Poll dataproc Operation until its status is done or timeout reached.

  Args:
    dataproc: wrapper for Dataproc messages, resources, and client
    operation: Operation, message of the operation to be polled.
    message: str, message to display to user while polling.
    timeout_s: number, seconds to poll with retries before timing out.
    poll_period_s: number, delay in seconds between requests.

  Returns:
    Operation: the return value of the last successful operations.get
    request.

  Raises:
    OperationError: if the operation times out or finishes with an error.
  """
  request = dataproc.messages.DataprocProjectsRegionsOperationsGetRequest(
      name=operation.name)
  log.status.Print('Waiting on operation [{0}].'.format(operation.name))
  start_time = time.time()
  warnings_so_far = 0
  is_tty = console_io.IsInteractive(error=True)
  tracker_separator = '\n' if is_tty else ''

  def _LogWarnings(warnings):
    new_warnings = warnings[warnings_so_far:]
    if new_warnings:
      # Drop a line to print nicely with the progress tracker.
      log.err.write(tracker_separator)
      for warning in new_warnings:
        log.warning(warning)

  with progress_tracker.ProgressTracker(message, autotick=True):
    while timeout_s > (time.time() - start_time):
      try:
        operation = dataproc.client.projects_regions_operations.Get(request)
        metadata = ParseOperationJsonMetadata(
            operation.metadata, dataproc.messages.ClusterOperationMetadata)
        _LogWarnings(metadata.warnings)
        warnings_so_far = len(metadata.warnings)
        if operation.done:
          break
      except apitools_exceptions.HttpError as http_exception:
        # Do not retry on 4xx errors.
        if IsClientHttpException(http_exception):
          raise
      time.sleep(poll_period_s)
  metadata = ParseOperationJsonMetadata(
      operation.metadata, dataproc.messages.ClusterOperationMetadata)
  _LogWarnings(metadata.warnings)
  if not operation.done:
    raise exceptions.OperationTimeoutError(
        'Operation [{0}] timed out.'.format(operation.name))
  elif operation.error:
    raise exceptions.OperationError('Operation [{0}] failed: {1}.'.format(
        operation.name, FormatRpcError(operation.error)))

  log.info('Operation [%s] finished after %.3f seconds', operation.name,
           (time.time() - start_time))
  return operation


def PrintWorkflowMetadata(metadata, status, operations, errors):
  """Print workflow and job status for the running workflow template.

  This method will detect any changes of state in the latest metadata and print
  all the new states in a workflow template.

  For example:
    Workflow template template-name RUNNING
    Creating cluster: Operation ID create-id.
    Job ID job-id-1 RUNNING
    Job ID job-id-1 COMPLETED
    Deleting cluster: Operation ID delete-id.
    Workflow template template-name DONE

  Args:
    metadata: Dataproc WorkflowMetadata message object, contains the latest
        states of a workflow template.
    status: Dictionary, stores all jobs' status in the current workflow
        template, as well as the status of the overarching workflow.
    operations: Dictionary, stores cluster operation status for the workflow
        template.
    errors: Dictionary, stores errors from the current workflow template.
  """
  # Key chosen to avoid collision with job ids, which are at least 3 characters.
  template_key = 'wt'
  if template_key not in status or metadata.state != status[template_key]:
    if metadata.template is not None:
      log.status.Print('WorkflowTemplate [{0}] {1}'.format(
          metadata.template, metadata.state))
    else:
      # Workflows instantiated inline do not store an id in their metadata.
      log.status.Print('WorkflowTemplate {0}'.format(metadata.state))
    status[template_key] = metadata.state
  if metadata.createCluster != operations['createCluster']:
    if hasattr(metadata.createCluster,
               'error') and metadata.createCluster.error is not None:
      log.status.Print(metadata.createCluster.error)
    elif hasattr(metadata.createCluster,
                 'done') and metadata.createCluster.done is not None:
      log.status.Print('Created cluster: {0}.'.format(metadata.clusterName))
    elif hasattr(
        metadata.createCluster,
        'operationId') and metadata.createCluster.operationId is not None:
      log.status.Print('Creating cluster: Operation ID [{0}].'.format(
          metadata.createCluster.operationId))
    operations['createCluster'] = metadata.createCluster
  if hasattr(metadata.graph, 'nodes'):
    for node in metadata.graph.nodes:
      if not node.jobId:
        continue
      if node.jobId not in status or status[node.jobId] != node.state:
        log.status.Print('Job ID {0} {1}'.format(node.jobId, node.state))
        status[node.jobId] = node.state
      if node.error and (node.jobId not in errors or
                         errors[node.jobId] != node.error):
        log.status.Print('Job ID {0} error: {1}'.format(node.jobId, node.error))
        errors[node.jobId] = node.error
  if metadata.deleteCluster != operations['deleteCluster']:
    if hasattr(metadata.deleteCluster,
               'error') and metadata.deleteCluster.error is not None:
      log.status.Print(metadata.deleteCluster.error)
    elif hasattr(metadata.deleteCluster,
                 'done') and metadata.deleteCluster.done is not None:
      log.status.Print('Deleted cluster: {0}.'.format(metadata.clusterName))
    elif hasattr(
        metadata.deleteCluster,
        'operationId') and metadata.deleteCluster.operationId is not None:
      log.status.Print('Deleting cluster: Operation ID [{0}].'.format(
          metadata.deleteCluster.operationId))
    operations['deleteCluster'] = metadata.deleteCluster


# TODO(b/36056506): Use api_lib.utils.waiter
def WaitForWorkflowTemplateOperation(dataproc,
                                     operation,
                                     timeout_s=None,
                                     poll_period_s=5):
  """Poll dataproc Operation until its status is done or timeout reached.

  Args:
    dataproc: wrapper for Dataproc messages, resources, and client
    operation: Operation, message of the operation to be polled.
    timeout_s: number, seconds to poll with retries before timing out.
    poll_period_s: number, delay in seconds between requests.

  Returns:
    Operation: the return value of the last successful operations.get
    request.

  Raises:
    OperationError: if the operation times out or finishes with an error.
  """
  request = dataproc.messages.DataprocProjectsRegionsOperationsGetRequest(
      name=operation.name)
  log.status.Print('Waiting on operation [{0}].'.format(operation.name))
  start_time = time.time()
  operations = {'createCluster': None, 'deleteCluster': None}
  status = {}
  errors = {}

  # If no timeout is specified, poll forever.
  while timeout_s is None or timeout_s > (time.time() - start_time):
    try:
      operation = dataproc.client.projects_regions_operations.Get(request)
      metadata = ParseOperationJsonMetadata(operation.metadata,
                                            dataproc.messages.WorkflowMetadata)

      PrintWorkflowMetadata(metadata, status, operations, errors)
      if operation.done:
        break
    except apitools_exceptions.HttpError as http_exception:
      # Do not retry on 4xx errors.
      if IsClientHttpException(http_exception):
        raise
    time.sleep(poll_period_s)
  metadata = ParseOperationJsonMetadata(operation.metadata,
                                        dataproc.messages.WorkflowMetadata)

  if not operation.done:
    raise exceptions.OperationTimeoutError(
        'Operation [{0}] timed out.'.format(operation.name))
  elif operation.error:
    raise exceptions.OperationError('Operation [{0}] failed: {1}.'.format(
        operation.name, FormatRpcError(operation.error)))
  for op in ['createCluster', 'deleteCluster']:
    if op in operations and operations[op] is not None and operations[op].error:
      raise exceptions.OperationError('Operation [{0}] failed: {1}.'.format(
          operations[op].operationId, operations[op].error))

  log.info('Operation [%s] finished after %.3f seconds', operation.name,
           (time.time() - start_time))
  return operation


class NoOpProgressDisplay(object):
  """For use in place of a ProgressTracker in a 'with' block."""

  def __enter__(self):
    pass

  def __exit__(self, *unused_args):
    pass


def WaitForJobTermination(dataproc,
                          job,
                          message,
                          goal_state,
                          stream_driver_log=False,
                          log_poll_period_s=1,
                          dataproc_poll_period_s=10,
                          timeout_s=None):
  """Poll dataproc Job until its status is terminal or timeout reached.

  Args:
    dataproc: wrapper for datarpoc resources, client and messages
    job: The job to wait to finish.
    message: str, message to display to user while polling.
    goal_state: JobStatus.StateValueValuesEnum, the state to define success
    stream_driver_log: bool, Whether to show the Job's driver's output.
    log_poll_period_s: number, delay in seconds between checking on the log.
    dataproc_poll_period_s: number, delay in seconds between requests to
        the Dataproc API.
    timeout_s: number, time out for job completion. None means no timeout.

  Returns:
    Operation: the return value of the last successful operations.get
    request.

  Raises:
    OperationError: if the operation times out or finishes with an error.
  """
  job_ref = ParseJob(job.reference.jobId, dataproc)
  request = dataproc.messages.DataprocProjectsRegionsJobsGetRequest(
      projectId=job_ref.projectId, region=job_ref.region, jobId=job_ref.jobId)
  driver_log_stream = None
  last_job_poll_time = 0
  job_complete = False
  wait_display = None
  driver_output_uri = None

  def ReadDriverLogIfPresent():
    if driver_log_stream and driver_log_stream.open:
      # TODO(b/36049794): Don't read all output.
      driver_log_stream.ReadIntoWritable(log.err)

  def PrintEqualsLine():
    attr = console_attr.GetConsoleAttr()
    log.err.Print('=' * attr.GetTermSize()[0])

  if stream_driver_log:
    log.status.Print('Waiting for job output...')
    wait_display = NoOpProgressDisplay()
  else:
    wait_display = progress_tracker.ProgressTracker(message, autotick=True)
  start_time = now = time.time()
  with wait_display:
    while not timeout_s or timeout_s > (now - start_time):
      # Poll logs first to see if it closed.
      ReadDriverLogIfPresent()
      log_stream_closed = driver_log_stream and not driver_log_stream.open
      if (not job_complete and
          job.status.state in dataproc.terminal_job_states):
        job_complete = True
        # Wait an 10s to get trailing output.
        timeout_s = now - start_time + 10

      if job_complete and (not stream_driver_log or log_stream_closed):
        # Nothing left to wait for
        break

      regular_job_poll = (
          not job_complete
          # Poll less frequently on dataproc API
          and now >= last_job_poll_time + dataproc_poll_period_s)
      # Poll at regular frequency before output has streamed and after it has
      # finished.
      expecting_output_stream = stream_driver_log and not driver_log_stream
      expecting_job_done = not job_complete and log_stream_closed
      if regular_job_poll or expecting_output_stream or expecting_job_done:
        last_job_poll_time = now
        try:
          job = dataproc.client.projects_regions_jobs.Get(request)
        except apitools_exceptions.HttpError as error:
          log.warning('GetJob failed:\n{}'.format(str(error)))
          # Do not retry on 4xx errors.
          if IsClientHttpException(error):
            raise
        if (stream_driver_log and job.driverOutputResourceUri and
            job.driverOutputResourceUri != driver_output_uri):
          if driver_output_uri:
            PrintEqualsLine()
            log.warning("Job attempt failed. Streaming new attempt's output.")
            PrintEqualsLine()
          driver_output_uri = job.driverOutputResourceUri
          driver_log_stream = storage_helpers.StorageObjectSeriesStream(
              job.driverOutputResourceUri)
      time.sleep(log_poll_period_s)
      now = time.time()

  # TODO(b/34836493): Get better test coverage of the next 20 lines.
  state = job.status.state
  if state is not goal_state and job.status.details:
    # Just log details, because the state will be in the error message.
    log.info(job.status.details)

  if state in dataproc.terminal_job_states:
    if stream_driver_log:
      if not driver_log_stream:
        log.warning('Expected job output not found.')
      elif driver_log_stream.open:
        log.warning('Job terminated, but output did not finish streaming.')
    if state is goal_state:
      return job
    raise exceptions.JobError(
        'Job [{0}] entered state [{1}] while waiting for [{2}].'.format(
            job_ref.jobId, state, goal_state))
  raise exceptions.JobTimeoutError(
      'Job [{0}] timed out while in state [{1}].'.format(job_ref.jobId, state))


def ParseCluster(name, dataproc):
  """Parse Cluster name, ID, or URL into Cloud SDK reference."""
  ref = dataproc.resources.Parse(
      name,
      params={
          'region': properties.VALUES.dataproc.region.GetOrFail,
          'projectId': properties.VALUES.core.project.GetOrFail
      },
      collection='dataproc.projects.regions.clusters')
  return ref


def ParseJob(job_id, dataproc):
  """Parse Job name, ID, or URL into Cloud SDK reference."""
  ref = dataproc.resources.Parse(
      job_id,
      params={
          'region': properties.VALUES.dataproc.region.GetOrFail,
          'projectId': properties.VALUES.core.project.GetOrFail
      },
      collection='dataproc.projects.regions.jobs')
  return ref


def ParseOperation(operation, dataproc):
  """Parse Operation name, ID, or URL into Cloud SDK reference."""
  collection = 'dataproc.projects.regions.operations'
  return dataproc.resources.Parse(
      operation,
      params={
          'regionsId': properties.VALUES.dataproc.region.GetOrFail,
          'projectsId': properties.VALUES.core.project.GetOrFail
      },
      collection=collection)


def ParseOperationJsonMetadata(metadata_value, metadata_type):
  """Returns an Operation message for a metadata value."""
  if not metadata_value:
    return metadata_type()
  return encoding.JsonToMessage(metadata_type,
                                encoding.MessageToJson(metadata_value))


def ParseWorkflowTemplates(template,
                           dataproc,
                           region=properties.VALUES.dataproc.region.GetOrFail):
  """Returns a workflow template reference given name, ID or URL."""
  # TODO(b/65845794): make caller to pass in region explicitly
  ref = dataproc.resources.Parse(
      template,
      params={
          'regionsId': region,
          'projectsId': properties.VALUES.core.project.GetOrFail
      },
      collection='dataproc.projects.regions.workflowTemplates')
  return ref


def ParseRegion(dataproc):
  """Returns a region reference given name, ID or URL."""
  ref = dataproc.resources.Parse(
      None,
      params={
          'regionId': properties.VALUES.dataproc.region.GetOrFail,
          'projectId': properties.VALUES.core.project.GetOrFail
      },
      collection='dataproc.projects.regions')
  return ref


def ReadAutoscalingPolicy(dataproc, policy_id, policy_file_name=None):
  """Returns autoscaling policy read from YAML file.

  Validates it using the schema for the API version corresponding to the
  dataproc instance, and backfills necessary fields.

  Args:
    dataproc: wrapper for dataproc resources, client and messages.
    policy_id: The autoscaling policy id (last piece of the resource name).
    policy_file_name: if set, location of the YAML file to read from. Otherwise,
      reads from stdin.

  Raises:
    argparse.ArgumentError if duration formats are invalid or out of bounds.
  """
  # Read template from YAML file, validate it using the schema for the
  # API version corresponding to the dataproc instance.
  data = console_io.ReadFromFileOrStdin(policy_file_name or '-', binary=False)
  schema_path = export_util.GetSchemaPath(
      'dataproc', dataproc.api_version, 'AutoscalingPolicy', for_help=False)
  policy = export_util.Import(
      message_type=dataproc.messages.AutoscalingPolicy,
      stream=data,
      schema_path=schema_path)

  # Ignore user set id in the file (if any), and overwrite with the policy_ref
  # provided with this command
  policy.id = policy_id

  # Similarly, ignore the set resource name. This field is OUTPUT_ONLY, so we
  # can just clear it.
  policy.name = None

  # Set duration fields to their seconds values
  policy.basicAlgorithm.cooldownPeriod = str(
      arg_parsers.Duration(lower_bound='2m', upper_bound='1d')(
          policy.basicAlgorithm.cooldownPeriod)) + 's'
  policy.basicAlgorithm.yarnConfig.gracefulDecommissionTimeout = str(
      arg_parsers.Duration(lower_bound='0s', upper_bound='1d')(
          policy.basicAlgorithm.yarnConfig.gracefulDecommissionTimeout)) + 's'

  return policy


def CreateAutoscalingPolicy(dataproc, name, policy):
  """Returns the server-resolved policy after creating the given policy.

  Args:
    dataproc: wrapper for dataproc resources, client and messages.
    name: The autoscaling policy resource name.
    policy: The AutoscalingPolicy message to create.
  """
  # TODO(b/109837200) make the dataproc discovery doc parameters consistent
  # Parent() fails for the collection because of projectId/projectsId and
  # regionId/regionsId inconsistencies.
  # parent = template_ref.Parent().RelativePath()
  parent = '/'.join(name.split('/')[0:4])

  request = \
    dataproc.messages.DataprocProjectsRegionsAutoscalingPoliciesCreateRequest(
        parent=parent,
        autoscalingPolicy=policy)
  policy = dataproc.client.projects_regions_autoscalingPolicies.Create(request)
  log.status.Print('Created [{0}].'.format(policy.id))
  return policy


def UpdateAutoscalingPolicy(dataproc, name, policy):
  """Returns the server-resolved policy after updating the given policy.

  Args:
    dataproc: wrapper for dataproc resources, client and messages.
    name: The autoscaling policy resource name.
    policy: The AutoscalingPolicy message to create.
  """
  # Though the name field is OUTPUT_ONLY in the API, the Update() method of the
  # gcloud generated dataproc client expects it to be set.
  policy.name = name

  policy = \
    dataproc.client.projects_regions_autoscalingPolicies.Update(policy)
  log.status.Print('Updated [{0}].'.format(policy.id))
  return policy
