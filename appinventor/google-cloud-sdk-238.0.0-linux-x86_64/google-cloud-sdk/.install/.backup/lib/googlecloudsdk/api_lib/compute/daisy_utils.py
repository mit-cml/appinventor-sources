# -*- coding: utf-8 -*- #
# Copyright 2017 Google Inc. All Rights Reserved.
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
"""Utilities for running Daisy builds on Google Container Builder."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import time

from apitools.base.py import encoding

from googlecloudsdk.api_lib.cloudbuild import cloudbuild_util
from googlecloudsdk.api_lib.cloudbuild import logs as cb_logs
from googlecloudsdk.api_lib.cloudresourcemanager import projects_api
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.api_lib.services import enable_api as services_api
from googlecloudsdk.api_lib.services import services_util
from googlecloudsdk.api_lib.storage import storage_api
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.cloudbuild import execution
from googlecloudsdk.command_lib.projects import util as projects_util
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import execution_utils
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources
from googlecloudsdk.core.console import console_io

_BUILDER = 'gcr.io/compute-image-tools/daisy:release'


class FilteredLogTailer(cb_logs.LogTailer):
  """Subclass of LogTailer that allows for filtering."""

  def _PrintLogLine(self, text):
    """Override PrintLogLine method to use self.filter."""
    if self.filter:
      output_lines = text.splitlines()
      for line in output_lines:
        for match in self.filter:
          if line.startswith(match):
            self.out.Print(line)
            break
    else:
      self.out.Print(text)


class DaisyCloudBuildClient(cb_logs.CloudBuildClient):
  """Subclass of CloudBuildClient that allows filtering."""

  def StreamWithFilter(self, build_ref, output_filter=None):
    """Stream the logs for a build using whitelist filter.

    Args:
      build_ref: Build reference, The build whose logs shall be streamed.
      output_filter: List of strings, The output will only be shown if the
        line starts with one of the strings in the list.

    Raises:
      NoLogsBucketException: If the build does not specify a logsBucket.

    Returns:
      Build message, The completed or terminated build as read for the final
      poll.
    """
    build = self.GetBuild(build_ref)
    log_tailer = FilteredLogTailer.FromBuild(build)
    log_tailer.filter = output_filter

    statuses = self.messages.Build.StatusValueValuesEnum
    working_statuses = [
        statuses.QUEUED,
        statuses.WORKING,
    ]

    while build.status in working_statuses:
      log_tailer.Poll()
      time.sleep(1)
      build = self.GetBuild(build_ref)

    # Poll the logs one final time to ensure we have everything. We know this
    # final poll will get the full log contents because GCS is strongly
    # consistent and Container Builder waits for logs to finish pushing before
    # marking the build complete.
    log_tailer.Poll(is_last=True)

    return build


class FailedBuildException(exceptions.Error):
  """Exception for builds that did not succeed."""

  def __init__(self, build):
    super(FailedBuildException, self).__init__(
        'build {id} completed with status "{status}"'.format(
            id=build.id, status=build.status))


class SubnetException(exceptions.Error):
  """Exception for subnet related errors."""


class ImageOperation(object):
  """Enum representing image operation"""
  IMPORT = 'import'
  EXPORT = 'export'


def AddCommonDaisyArgs(parser):
  """Common arguments for Daisy builds."""
  parser.add_argument(
      '--log-location',
      help='Directory in Google Cloud Storage to hold build logs. If not '
      'set, ```gs://<project num>.cloudbuild-logs.googleusercontent.com/``` '
      'will be created and used.',
  )
  parser.add_argument(
      '--timeout',
      type=arg_parsers.Duration(),
      default='2h',
      help="""\
          Maximum time a build can last before it is failed as "TIMEOUT".
          For example, specifying ``2h'' will fail the process after  2 hours.
          See $ gcloud topic datetimes for information on duration formats.
          """
  )
  base.ASYNC_FLAG.AddToParser(parser)


def _CheckIamPermissions(project_id, service_account_roles):
  """Check for needed IAM permissions and prompt to add if missing.

  Args:
    project_id: A string with the name of the project.
    service_account_roles: roles to be used by service account in addition to
      compute.admin.
  """
  project = projects_api.Get(project_id)
  # If the user's project doesn't have cloudbuild enabled yet, then the service
  # account won't even exist. If so, then ask to enable it before continuing.
  # Also prompt them to enable Stackdriver Logging if they haven't yet.
  expected_services = ['cloudbuild.googleapis.com',
                       'logging.googleapis.com']
  for service_name in expected_services:
    if not services_api.IsServiceEnabled(project.projectId,
                                         service_name):
      # TODO(b/112757283): Split this out into a separate library.
      prompt_message = ('The "{0}" service is not enabled for this project. '
                        'It is required for this operation.\n').format(
                            service_name)
      console_io.PromptContinue(prompt_message,
                                'Would you like to enable this service?',
                                throw_if_unattended=True,
                                cancel_on_no=True)
      operation = services_api.EnableServiceApiCall(project.projectId,
                                                    service_name)
      # Wait for the operation to finish.
      services_util.ProcessOperationResult(operation, is_async=False)

  # Now that we're sure the service account exists, actually check permissions.
  service_account = 'serviceAccount:{0}@cloudbuild.gserviceaccount.com'.format(
      project.projectNumber)
  expected_permissions = {'roles/compute.admin': service_account}
  if service_account_roles:
    for role in service_account_roles:
      expected_permissions[role] = service_account

  permissions = projects_api.GetIamPolicy(project_id)
  for binding in permissions.bindings:
    if expected_permissions.get(binding.role) in binding.members:
      del expected_permissions[binding.role]

  if expected_permissions:
    ep_table = ['{0} {1}'.format(role, account) for role, account
                in expected_permissions.items()]
    prompt_message = (
        'The following IAM permissions are needed for this operation:\n'
        '[{0}]\n'.format('\n'.join(ep_table)))
    console_io.PromptContinue(
        message=prompt_message,
        prompt_string='Would you like to add the permissions',
        throw_if_unattended=True,
        cancel_on_no=True)

    for role, account in expected_permissions.items():
      log.info('Adding [{0}] to [{1}]'.format(account, role))
      projects_api.AddIamPolicyBinding(project_id, account, role)


def _CreateCloudBuild(build_config, client, messages):
  """Create a build in cloud build.

  Args:
    build_config: A cloud build Build message.
    client: The cloud build api client.
    messages: The cloud build api messages module.

  Returns:
    Tuple containing a cloud build build object and the resource reference
    for that build.
  """
  log.debug('submitting build: {0}'.format(repr(build_config)))
  op = client.projects_builds.Create(
      messages.CloudbuildProjectsBuildsCreateRequest(
          build=build_config,
          projectId=properties.VALUES.core.project.Get()))
  json = encoding.MessageToJson(op.metadata)
  build = encoding.JsonToMessage(messages.BuildOperationMetadata, json).build

  build_ref = resources.REGISTRY.Create(
      collection='cloudbuild.projects.builds',
      projectId=build.projectId,
      id=build.id)

  log.CreatedResource(build_ref)

  if build.logUrl:
    log.status.Print('Logs are available at [{0}].'.format(build.logUrl))
  else:
    log.status.Print('Logs are available in the Cloud Console.')

  return build, build_ref


def GetAndCreateDaisyBucket(bucket_name=None, storage_client=None,
                            bucket_location=None):
  """Determine the name of the GCS bucket to use and create if necessary.

  Args:
    bucket_name: str, bucket name to use, otherwise the bucket will be named
      based on the project id.
    storage_client: The storage_api client object.
    bucket_location: str, bucket location

  Returns:
    A string containing the name of the GCS bucket to use.
  """
  project = properties.VALUES.core.project.GetOrFail()
  safe_project = project.replace(':', '-')
  safe_project = safe_project.replace('.', '-')
  if not bucket_name:
    bucket_name = '{0}-daisy-bkt'.format(safe_project)
    if bucket_location:
      bucket_name = '{0}-{1}'.format(bucket_name, bucket_location).lower()

  safe_bucket_name = bucket_name.replace('google', 'elgoog')

  if not storage_client:
    storage_client = storage_api.StorageClient()

  # TODO (b/117668144): Make Daisy scratch bucket ACLs same as
  # source/destination bucket
  storage_client.CreateBucketIfNotExists(
      safe_bucket_name, location=bucket_location)

  return safe_bucket_name


def GetSubnetRegion():
  """Gets region from global properties/args that should be used for subnet arg.

  Returns:
    str, region
  Raises:
    SubnetException: if region couldn't be inferred.
  """
  if properties.VALUES.compute.zone.Get():
    return utils.ZoneNameToRegionName(
        properties.VALUES.compute.zone.Get())
  elif properties.VALUES.compute.region.Get():
    return properties.VALUES.compute.region.Get()

  raise SubnetException('Region or zone should be specified.')


def ExtractNetworkAndSubnetDaisyVariables(args, operation):
  """Extracts network/subnet out of CLI args in the form of Daisy variables.

  Args:
    args: CLI args that might contain network/subnet args.
    operation: ImageOperation, specifies if this call is for import or export

  Returns:
    list of strs, network/subnet variables, if specified in args. Can be empty.
  """
  variables = []
  add_network_variable = False
  if args.subnet:
    variables.append('{0}_subnet=regions/{1}/subnetworks/{2}'.format(
        operation, GetSubnetRegion(), args.subnet.lower()))

    # network variable should be empty string in case subnet is specified
    # and network is not. Otherwise, Daisy will default network to
    # `global/networks/default` which will fail except for default networks
    network_full_path = ''
    add_network_variable = True

  if args.network:
    add_network_variable = True
    network_full_path = 'global/networks/{0}'.format(args.network.lower())

  if add_network_variable:
    variables.append('{0}_network={1}'.format(
        operation, network_full_path))

  return variables


def RunDaisyBuild(args, workflow, variables, daisy_bucket=None, tags=None,
                  user_zone=None, output_filter=None,
                  service_account_roles=None):
  """Run a build with Daisy on Google Cloud Builder.

  Args:
    args: an argparse namespace. All the arguments that were provided to this
      command invocation.
    workflow: The path to the Daisy workflow to run.
    variables: A string of key-value pairs to pass to Daisy.
    daisy_bucket: A string containing the name of the GCS bucket that daisy
      should use.
    tags: A list of strings for adding tags to the Argo build.
    user_zone: The GCP zone to tell Daisy to do work in. If unspecified,
      defaults to wherever the Argo runner happens to be.
    output_filter: A list of strings indicating what lines from the log should
      be output. Only lines that start with one of the strings in output_filter
      will be displayed.
    service_account_roles: roles to be used by service account in addition to
      compute.admin.

  Returns:
    A build object that either streams the output or is displayed as a
    link to the build.

  Raises:
    FailedBuildException: If the build is completed and not 'SUCCESS'.
  """
  client = cloudbuild_util.GetClientInstance()
  messages = cloudbuild_util.GetMessagesModule()
  project_id = projects_util.ParseProject(
      properties.VALUES.core.project.GetOrFail())

  _CheckIamPermissions(
      project_id, service_account_roles or ['roles/iam.serviceAccountActor'])

  # Make Daisy time out before gcloud by shaving off 2% from the timeout time,
  # up to a max of 5m (300s).
  two_percent = int(args.timeout * 0.02)
  daisy_timeout = args.timeout - min(two_percent, 300)

  daisy_bucket = daisy_bucket or GetAndCreateDaisyBucket()

  daisy_args = ['-gcs_path=gs://{0}/'.format(daisy_bucket),
                '-default_timeout={0}s'.format(daisy_timeout),
                '-variables={0}'.format(variables),
                workflow,
               ]
  if user_zone is not None:
    daisy_args = ['-zone={0}'.format(user_zone)] + daisy_args

  build_tags = ['gce-daisy']
  if tags:
    build_tags.extend(tags)

  # First, create the build request.
  build_config = messages.Build(
      steps=[
          messages.BuildStep(
              name=_BUILDER,
              args=daisy_args,
          ),
      ],
      tags=build_tags,
      timeout='{0}s'.format(args.timeout),
  )
  if args.log_location:
    gcs_log_dir = resources.REGISTRY.Parse(
        args.log_location, collection='storage.objects')

    build_config.logsBucket = (
        'gs://{0}/{1}'.format(gcs_log_dir.bucket, gcs_log_dir.object))

  # Start the build.
  build, build_ref = _CreateCloudBuild(build_config, client, messages)

  # If the command is run --async, we just print out a reference to the build.
  if args.async:
    return build

  mash_handler = execution.MashHandler(
      execution.GetCancelBuildHandler(client, messages, build_ref))

  # Otherwise, logs are streamed from GCS.
  with execution_utils.CtrlCSection(mash_handler):
    build = DaisyCloudBuildClient(client, messages).StreamWithFilter(
        build_ref, output_filter=output_filter)

  if build.status == messages.Build.StatusValueValuesEnum.TIMEOUT:
    log.status.Print(
        'Your build timed out. Use the [--timeout=DURATION] flag to change '
        'the timeout threshold.')

  if build.status != messages.Build.StatusValueValuesEnum.SUCCESS:
    raise FailedBuildException(build)

  return build

