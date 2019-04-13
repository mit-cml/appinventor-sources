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
"""Command to create an environment."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.composer import environments_util as environments_api_util
from googlecloudsdk.api_lib.composer import operations_util as operations_api_util
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.composer import flags
from googlecloudsdk.command_lib.composer import parsers
from googlecloudsdk.command_lib.composer import resource_args
from googlecloudsdk.command_lib.composer import util as command_util
from googlecloudsdk.command_lib.util.args import labels_util
from googlecloudsdk.core import log


def _ImageVersionFromAirflowVersion(airflow_version):
  return 'composer-latest-airflow-{}'.format(airflow_version)


def _CommonArgs(parser):
  """Common arguments that apply to all ReleaseTracks."""
  resource_args.AddEnvironmentResourceArg(parser, 'to create')
  base.ASYNC_FLAG.AddToParser(parser)
  parser.add_argument(
      '--node-count',
      type=int,
      help='The number of nodes to create to run the environment.')
  parser.add_argument(
      '--zone',
      help='The Compute Engine zone in which the environment will '
      'be created. For example `--zone=us-central1-a`.')
  parser.add_argument(
      '--machine-type',
      help='The Compute Engine machine type '
      '(https://cloud.google.com/compute/docs/machine-types) to use for '
      'nodes. For example `--machine-type=n1-standard-1`.')
  parser.add_argument(
      '--disk-size',
      default='100GB',
      type=arg_parsers.BinarySize(
          lower_bound='20GB',
          upper_bound='64TB',
          suggested_binary_size_scales=['GB', 'TB']),
      help='The disk size for each VM node in the environment. The minimum '
      'size is 20GB, and the maximum is 64TB. Specified value must be an '
      'integer multiple of gigabytes. Cannot be updated after the '
      'environment has been created. If units are not provided, defaults to '
      'GB.')
  networking_group = parser.add_group(help='Virtual Private Cloud networking')
  networking_group.add_argument(
      '--network',
      required=True,
      help='The Compute Engine Network to which the environment will '
      'be connected. If a \'Custom Subnet Network\' is provided, '
      '`--subnetwork` must be specified as well.')
  networking_group.add_argument(
      '--subnetwork',
      help='The Compute Engine subnetwork '
      '(https://cloud.google.com/compute/docs/subnetworks) to which the '
      'environment will be connected.')
  labels_util.AddCreateLabelsFlags(parser)
  flags.CREATE_ENV_VARS_FLAG.AddToParser(parser)
  # Default is provided by API server.
  parser.add_argument(
      '--service-account',
      help='The Google Cloud Platform service account to be used by the node '
      'VMs. If a service account is not specified, the "default" Compute '
      'Engine service account for the project is used. Cannot be updated.')
  # Default is provided by API server.
  parser.add_argument(
      '--oauth-scopes',
      help='The set of Google API scopes to be made available on all of the '
      'node VMs. Defaults to '
      '[\'https://www.googleapis.com/auth/cloud-platform\']. Cannot be '
      'updated.',
      type=arg_parsers.ArgList(),
      metavar='SCOPE',
      action=arg_parsers.UpdateAction)
  parser.add_argument(
      '--tags',
      help='The set of instance tags applied to all node VMs. Tags are used '
      'to identify valid sources or targets for network firewalls. Each tag '
      'within the list must comply with RFC 1035. Cannot be updated.',
      type=arg_parsers.ArgList(),
      metavar='TAG',
      action=arg_parsers.UpdateAction)

  # API server will validate key/value pairs.
  parser.add_argument(
      '--airflow-configs',
      help="""\
A list of Airflow software configuration override KEY=VALUE pairs to set. For
information on how to structure KEYs and VALUEs, run
`$ {top_command} help composer environments update`.""",
      type=arg_parsers.ArgDict(),
      metavar='KEY=VALUE',
      action=arg_parsers.UpdateAction)

  parser.add_argument(
      '--python-version',
      type=str,
      choices={
          '2': 'Created environment will use Python 2',
          '3': 'Created environment will use Python 3'
      },
      help='The Python version to be used within the created environment. '
      'Supplied value should represent the desired major Python version. '
      'Cannot be updated.')

  version_group = parser.add_mutually_exclusive_group()
  airflow_version_type = arg_parsers.RegexpValidator(
      r'^(\d+\.\d+(?:\.\d+)?)', 'must be in the form X.Y[.Z].')
  version_group.add_argument(
      '--airflow-version',
      type=airflow_version_type,
      help="""Version of Airflow to run in the environment.

      Must be of the form `X.Y[.Z]`.

      The latest supported Cloud Composer version will be used within
      the created environment.""")

  image_version_type = arg_parsers.RegexpValidator(
      r'^composer-(\d+\.\d+.\d+|latest)-airflow-(\d+\.\d+(?:\.\d+)?)',
      'must be in the form \'composer-A.B.C-airflow-X.Y[.Z]\' or '
      '\'latest\' can be provided in place of the Cloud Composer version '
      'string. For example: \'composer-latest-airflow-1.10.0\'.')
  version_group.add_argument(
      '--image-version',
      type=image_version_type,
      help="""Version of the image to run in the environment.

      Value consists of version information for both Cloud Composer and
      Apache Airflow. Must be of the form `composer-A.B.C-airflow-X.Y[.Z]`.

      The Cloud Composer and Airflow versions are semantic versions.
      `latest` can be provided instead of an explicit Cloud Composer
      version number indicating that the server will replace `latest`
      with the current Cloud Composer version. For the Apache Airflow
      portion, the patch version can be omitted and the current
      version will be selected. The version numbers that are used will
      be stored.""")


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Create(base.Command):
  """Create and initialize a Cloud Composer environment.

  If run asynchronously with `--async`, exits after printing an operation
  that can be used to poll the status of the creation operation via:

    {top_command} composer operations describe
  """

  @staticmethod
  def Args(parser):
    _CommonArgs(parser)

  def Run(self, args):
    flags.ValidateDiskSize('--disk-size', args.disk_size)
    self.env_ref = args.CONCEPTS.environment.Parse()
    env_name = self.env_ref.Name()
    if not command_util.IsValidEnvironmentName(env_name):
      raise command_util.InvalidUserInputError(
          'Invalid environment name: [{}]. Must match pattern: {}'.format(
              env_name, command_util.ENVIRONMENT_NAME_PATTERN.pattern))

    self.zone_ref = parsers.ParseZone(args.zone) if args.zone else None
    self.zone = self.zone_ref.RelativeName() if self.zone_ref else None
    self.machine_type = None
    self.network = None
    self.subnetwork = None
    if args.machine_type:
      self.machine_type = parsers.ParseMachineType(
          args.machine_type,
          fallback_zone=self.zone_ref.Name()
          if self.zone_ref else None).RelativeName()
    if args.network:
      self.network = parsers.ParseNetwork(args.network).RelativeName()
    if args.subnetwork:
      self.subnetwork = parsers.ParseSubnetwork(
          args.subnetwork,
          fallback_region=self.env_ref.Parent().Name()).RelativeName()

    self.image_version = None
    if args.airflow_version:
      self.image_version = _ImageVersionFromAirflowVersion(args.airflow_version)
    elif args.image_version:
      self.image_version = args.image_version

    operation = self.GetOperationMessage(args)

    details = 'with operation [{0}]'.format(operation.name)
    if args.async:
      log.CreatedResource(
          self.env_ref.RelativeName(),
          kind='environment',
          is_async=True,
          details=details)
      return operation
    else:
      try:
        operations_api_util.WaitForOperation(
            operation,
            'Waiting for [{}] to be created with [{}]'.format(
                self.env_ref.RelativeName(), operation.name),
            release_track=self.ReleaseTrack())
      except command_util.OperationError as e:
        raise command_util.EnvironmentCreateError(
            'Error creating [{}]: {}'.format(self.env_ref.RelativeName(),
                                             str(e)))

  def GetOperationMessage(self, args):
    """Constructs Create message."""
    return environments_api_util.Create(
        self.env_ref,
        args.node_count,
        labels=args.labels,
        location=self.zone,
        machine_type=self.machine_type,
        network=self.network,
        subnetwork=self.subnetwork,
        env_variables=args.env_variables,
        airflow_config_overrides=args.airflow_configs,
        service_account=args.service_account,
        oauth_scopes=args.oauth_scopes,
        tags=args.tags,
        disk_size_gb=args.disk_size >> 30,
        python_version=args.python_version,
        image_version=self.image_version,
        release_track=self.ReleaseTrack())


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class CreateBeta(Create):
  """Create and initialize a Cloud Composer environment.

  If run asynchronously with `--async`, exits after printing an operation
  that can be used to poll the status of the creation operation via:

    {top_command} composer operations describe
  """


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class CreateAlpha(CreateBeta):
  """Create and initialize a Cloud Composer environment.

  If run asynchronously with `--async`, exits after printing an operation
  that can be used to poll the status of the creation operation via:

    {top_command} composer operations describe
  """

  @staticmethod
  def Args(parser):
    CreateBeta.Args(parser)

    # Adding alpha arguments
    parser.add_argument(
        '--airflow-executor-type',
        hidden=True,
        choices={
            'CELERY': 'Task instances will run by CELERY executor',
            'KUBERNETES': 'Task instances will run by KUBERNETES executor'
        },
        help="""The type of executor by which task instances are run on Airflow;
        currently supported executor types are CELERY and KUBERNETES.
        Defaults to CELERY. Cannot be updated.""")

  def GetOperationMessage(self, args):
    """See base class."""
    return environments_api_util.Create(
        self.env_ref,
        args.node_count,
        labels=args.labels,
        location=self.zone,
        machine_type=self.machine_type,
        network=self.network,
        subnetwork=self.subnetwork,
        env_variables=args.env_variables,
        airflow_config_overrides=args.airflow_configs,
        service_account=args.service_account,
        oauth_scopes=args.oauth_scopes,
        tags=args.tags,
        disk_size_gb=args.disk_size >> 30,
        python_version=args.python_version,
        image_version=self.image_version,
        airflow_executor_type=args.airflow_executor_type,
        release_track=self.ReleaseTrack())
