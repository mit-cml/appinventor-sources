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

"""Creates or updates a Google Cloud Function."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.api_lib.functions import env_vars as env_vars_api_util
from googlecloudsdk.api_lib.functions import util as api_util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.functions import flags
from googlecloudsdk.command_lib.functions.deploy import env_vars_util
from googlecloudsdk.command_lib.functions.deploy import labels_util
from googlecloudsdk.command_lib.functions.deploy import source_util
from googlecloudsdk.command_lib.functions.deploy import trigger_util
from googlecloudsdk.command_lib.util.args import labels_util as args_labels_util
from googlecloudsdk.command_lib.util.args import map_util
from googlecloudsdk.core import log


def _ApplyEnvVarsArgsToFunction(function, args):
  updated_fields = []
  old_env_vars = env_vars_api_util.GetFunctionEnvVarsAsDict(function)
  env_var_flags = map_util.GetMapFlagsFromArgs('env-vars', args)
  new_env_vars = map_util.ApplyMapFlags(old_env_vars, **env_var_flags)
  if old_env_vars != new_env_vars:
    function.environmentVariables = env_vars_api_util.DictToEnvVarsProperty(
        new_env_vars)
    updated_fields.append('environmentVariables')
  return updated_fields


def _Run(args, track=None, enable_runtime=True, enable_max_instances=False,
         enable_connected_vpc=False):
  """Run a function deployment with the given args."""
  # Check for labels that start with `deployment`, which is not allowed.
  labels_util.CheckNoDeploymentLabels('--remove-labels', args.remove_labels)
  labels_util.CheckNoDeploymentLabels('--update-labels', args.update_labels)

  # Check that exactly one trigger type is specified properly.
  trigger_util.ValidateTriggerArgs(
      args.trigger_event, args.trigger_resource,
      args.IsSpecified('retry'), args.IsSpecified('trigger_http'))

  trigger_params = trigger_util.GetTriggerEventParams(
      args.trigger_http, args.trigger_bucket, args.trigger_topic,
      args.trigger_event, args.trigger_resource)

  function_ref = args.CONCEPTS.name.Parse()
  function_url = function_ref.RelativeName()

  messages = api_util.GetApiMessagesModule(track)

  # Get an existing function or create a new one.
  function = api_util.GetFunction(function_url)
  is_new_function = function is None
  if is_new_function:
    trigger_util.CheckTriggerSpecified(args)
    function = messages.CloudFunction()
    function.name = function_url
  elif trigger_params:
    # If the new deployment would implicitly change the trigger_event type
    # raise error
    trigger_util.CheckLegacyTriggerUpdate(function.eventTrigger,
                                          trigger_params['trigger_event'])

  # Keep track of which fields are updated in the case of patching.
  updated_fields = []

  # Populate function properties based on args.
  if args.entry_point:
    function.entryPoint = args.entry_point
    updated_fields.append('entryPoint')
  if args.timeout:
    function.timeout = '{}s'.format(args.timeout)
    updated_fields.append('timeout')
  if args.memory:
    function.availableMemoryMb = utils.BytesToMb(args.memory)
    updated_fields.append('availableMemoryMb')
  if args.service_account:
    function.serviceAccountEmail = args.service_account
    updated_fields.append('serviceAccountEmail')
  if enable_runtime:
    if args.IsSpecified('runtime'):
      function.runtime = args.runtime
      updated_fields.append('runtime')
    elif is_new_function:
      log.warning('Flag `--runtime` will become a required flag soon. '
                  'Please specify the value for this flag.')
  if enable_max_instances:
    if (args.IsSpecified('max_instances') or
        args.IsSpecified('clear_max_instances')):
      max_instances = 0 if args.clear_max_instances else args.max_instances
      function.maxInstances = max_instances
      updated_fields.append('maxInstances')
  if enable_connected_vpc:
    if args.connected_vpc:
      function.network = args.connected_vpc
      updated_fields.append('network')
    if args.vpc_connector:
      function.vpcConnector = args.vpc_connector
      updated_fields.append('vpcConnector')

  # Populate trigger properties of function based on trigger args.
  if args.trigger_http:
    function.httpsTrigger = messages.HttpsTrigger()
    function.eventTrigger = None
    updated_fields.extend(['eventTrigger', 'httpsTrigger'])
  if trigger_params:
    function.eventTrigger = trigger_util.CreateEventTrigger(**trigger_params)
    function.httpsTrigger = None
    updated_fields.extend(['eventTrigger', 'httpsTrigger'])
  if args.IsSpecified('retry'):
    updated_fields.append('eventTrigger.failurePolicy')
    if args.retry:
      function.eventTrigger.failurePolicy = messages.FailurePolicy()
      function.eventTrigger.failurePolicy.retry = messages.Retry()
    else:
      function.eventTrigger.failurePolicy = None
  elif function.eventTrigger:
    function.eventTrigger.failurePolicy = None

  # Populate source properties of function based on source args.
  # Only Add source to function if its explicitly provided, a new function,
  # using a stage bucket or deploy of an existing function that previously
  # used local source.
  if (args.source or args.stage_bucket or is_new_function or
      function.sourceUploadUrl):
    updated_fields.extend(source_util.SetFunctionSourceProps(
        function, function_ref, args.source, args.stage_bucket))

  # Apply label args to function
  if labels_util.SetFunctionLabels(function, args.update_labels,
                                   args.remove_labels, args.clear_labels):
    updated_fields.append('labels')

  # Apply environment variables args to function
  updated_fields.extend(_ApplyEnvVarsArgsToFunction(function, args))

  if is_new_function:
    return api_util.CreateFunction(function,
                                   function_ref.Parent().RelativeName())
  if updated_fields:
    return api_util.PatchFunction(function, updated_fields)
  log.status.Print('Nothing to update.')


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Deploy(base.Command):
  """Create or update a Google Cloud Function."""

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    flags.AddFunctionResourceArg(parser, 'to deploy')
    # Add args for function properties
    flags.AddFunctionMemoryFlag(parser)
    flags.AddFunctionTimeoutFlag(parser)
    flags.AddFunctionRetryFlag(parser)
    args_labels_util.AddUpdateLabelsFlags(
        parser,
        extra_update_message=labels_util.NO_LABELS_STARTING_WITH_DEPLOY_MESSAGE,
        extra_remove_message=labels_util.NO_LABELS_STARTING_WITH_DEPLOY_MESSAGE)

    flags.AddServiceAccountFlag(parser)

    # Add args for specifying the function source code
    flags.AddSourceFlag(parser)
    flags.AddStageBucketFlag(parser)
    flags.AddEntryPointFlag(parser)

    # Add args for specifying the function trigger
    flags.AddTriggerFlagGroup(parser)

    flags.AddRuntimeFlag(parser)

    # Add args for specifying environment variables
    env_vars_util.AddUpdateEnvVarsFlags(parser)

  def Run(self, args):
    return _Run(args, track=self.ReleaseTrack())


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class DeployBeta(base.Command):
  """Create or update a Google Cloud Function."""

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    Deploy.Args(parser)

  def Run(self, args):
    return _Run(args, track=self.ReleaseTrack())


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class DeployAlpha(base.Command):
  """Create or update a Google Cloud Function."""

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    Deploy.Args(parser)
    flags.AddMaxInstancesFlag(parser)
    flags.AddConnectedVPCMutexGroup(parser)

  def Run(self, args):
    return _Run(args, track=self.ReleaseTrack(), enable_max_instances=True,
                enable_connected_vpc=True)
