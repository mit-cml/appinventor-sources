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
"""Implementation of gcloud dataflow jobs run command.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.dataflow import apis
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.core import properties


def _CommonArgs(parser):
  """Register flags for this command.

  Args:
    parser: argparse.ArgumentParser to register arguments with.

  """
  parser.add_argument(
      'job_name',
      metavar='JOB_NAME',
      help='The unique name to assign to the job.')

  parser.add_argument(
      '--gcs-location',
      help=('The Google Cloud Storage location of the job template to run. '
            "(Must be a URL beginning with 'gs://'.)"),
      type=arg_parsers.RegexpValidator(r'^gs://.*',
                                       'Must begin with \'gs://\''),
      required=True)

  parser.add_argument(
      '--staging-location',
      help=('The Google Cloud Storage location to stage temporary files. '
            "(Must be a URL beginning with 'gs://'.)"),
      type=arg_parsers.RegexpValidator(r'^gs://.*',
                                       'Must begin with \'gs://\''))

  parser.add_argument(
      '--zone',
      type=arg_parsers.RegexpValidator(r'\w+-\w+\d-\w',
                                       'must provide a valid zone'),
      help='The zone to run the workers in.')

  parser.add_argument(
      '--service-account-email',
      type=arg_parsers.RegexpValidator(r'.*@.*\..*',
                                       'must provide a valid email address'),
      help='The service account to run the workers as.')

  parser.add_argument(
      '--max-workers', type=int, help='The maximum number of workers to run.')

  parser.add_argument(
      '--parameters',
      metavar='PARAMETERS',
      type=arg_parsers.ArgDict(),
      action=arg_parsers.UpdateAction,
      help='The parameters to pass to the job.')

  parser.add_argument(
      '--region',
      metavar='REGION_ID',
      help='The region ID of the job\'s regional endpoint.')


def _CommonRun(args, support_beta_features=False):
  """Runs the command.

  Args:
    args: The arguments that were provided to this command invocation.
    support_beta_features: True, if using the beta command.

  Returns:
    A Job message.
  """
  num_workers = None
  worker_machine_type = None
  network = None
  subnetwork = None

  if support_beta_features:
    num_workers = args.num_workers
    worker_machine_type = args.worker_machine_type
    network = args.network
    subnetwork = args.subnetwork

  job = apis.Templates.Create(
      project_id=properties.VALUES.core.project.Get(required=True),
      region_id=args.region,
      gcs_location=args.gcs_location,
      staging_location=args.staging_location,
      job_name=args.job_name,
      parameters=args.parameters,
      service_account_email=args.service_account_email,
      zone=args.zone,
      max_workers=args.max_workers,
      num_workers=num_workers,
      worker_machine_type=worker_machine_type,
      network=network,
      subnetwork=subnetwork)

  return job


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Run(base.Command):
  """Runs a job from the specified path."""

  @staticmethod
  def Args(parser):
    _CommonArgs(parser)

  def Run(self, args):
    return _CommonRun(args)


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class RunBeta(Run):
  """Runs a job from the specified path."""

  @staticmethod
  def Args(parser):
    _CommonArgs(parser)

    parser.add_argument(
        '--num-workers', type=int, help='The initial number of workers to use.')

    parser.add_argument(
        '--worker-machine-type',
        help='The type of machine to use for workers. Defaults to '
        'server-specified.')

    parser.add_argument(
        '--subnetwork',
        help='The Compute Engine subnetwork for launching instances '
        'to run your pipeline.')

    parser.add_argument(
        '--network',
        help='The Compute Engine network for launching instances to '
        'run your pipeline.')

  def Run(self, args):
    return _CommonRun(args, True)
