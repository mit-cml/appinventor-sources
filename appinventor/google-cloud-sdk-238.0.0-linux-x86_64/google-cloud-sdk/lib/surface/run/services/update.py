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
"""Command for updating env vars and other configuration info."""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.run import connection_context
from googlecloudsdk.command_lib.run import exceptions
from googlecloudsdk.command_lib.run import flags
from googlecloudsdk.command_lib.run import pretty_print
from googlecloudsdk.command_lib.run import resource_args
from googlecloudsdk.command_lib.run import serverless_operations
from googlecloudsdk.command_lib.run import stages
from googlecloudsdk.command_lib.util.concepts import concept_parsers
from googlecloudsdk.command_lib.util.concepts import presentation_specs
from googlecloudsdk.core.console import progress_tracker


class Update(base.Command):
  """Update Cloud Run environment variables and other configuration settings.
  """

  detailed_help = {
      'DESCRIPTION': """\
          {description}
          """,
      'EXAMPLES': """\
          To update one or more env vars:

              $ {command} myservice --update-env-vars KEY1=VALUE1,KEY2=VALUE2
         """,
  }

  @staticmethod
  def Args(parser):
    service_presentation = presentation_specs.ResourcePresentationSpec(
        'SERVICE',
        resource_args.GetServiceResourceSpec(prompt=True),
        'Service to update the configuration of.',
        required=True,
        prefixes=False)
    flags.AddRegionArg(parser)
    flags.AddMutexEnvVarsFlags(parser)
    flags.AddMemoryFlag(parser)
    flags.AddConcurrencyFlag(parser)
    flags.AddTimeoutFlag(parser)
    flags.AddAsyncFlag(parser)
    concept_parsers.ConceptParser([
        resource_args.CLUSTER_PRESENTATION,
        service_presentation]).AddToParser(parser)

  def Run(self, args):
    """Update configuration information about the service.

    Does not change the running code.

    Args:
      args: Args!
    """
    conn_context = connection_context.GetConnectionContext(args)
    service_ref = flags.GetService(args)

    with serverless_operations.Connect(conn_context) as client:
      changes = flags.GetConfigurationChanges(args)
      if not changes:
        raise exceptions.NoConfigurationChangeError(
            'No configuration change requested. '
            'Did you mean to include the flags `--update-env-vars`, '
            '`--memory`, `--concurrency`, or `--timeout`?')
      deployment_stages = stages.ServiceStages()
      with progress_tracker.StagedProgressTracker(
          'Deploying...',
          deployment_stages,
          failure_message='Deployment failed',
          suppress_output=args.async) as tracker:
        client.ReleaseService(service_ref, changes, tracker, args.async)
      if args.async:
        pretty_print.Success(
            'Deploying asynchronously.')
      else:
        url = client.GetServiceUrl(service_ref)
        active_revs = client.GetActiveRevisions(service_ref)

        msg = ('{{bold}}Service [{serv}] revision{plural} {rev_msg} is active'
               ' and serving traffic at{{reset}} {url}')

        rev_msg = ' '.join(['[{}]'.format(rev) for rev in active_revs])

        msg = msg.format(
            serv=service_ref.servicesId,
            plural='s' if len(active_revs) > 1 else '',
            rev_msg=rev_msg,
            url=url)

        pretty_print.Success(msg)
