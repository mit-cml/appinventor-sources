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
"""ml-engine versions create command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.ml_engine import operations
from googlecloudsdk.api_lib.ml_engine import versions_api
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.ml_engine import flags
from googlecloudsdk.command_lib.ml_engine import versions_util
from googlecloudsdk.command_lib.util.args import labels_util


def _AddCreateArgs(parser):
  """Add common arguments for `versions create` command."""
  flags.GetModelName(positional=False, required=True).AddToParser(parser)
  flags.GetDescriptionFlag('version').AddToParser(parser)
  flags.VERSION_NAME.AddToParser(parser)
  base.Argument(
      '--origin',
      help="""\
          Location of ```model/``` "directory" (as output by
          https://www.tensorflow.org/versions/r0.12/api_docs/python/state_ops.html#Saver).

          This overrides `deploymentUri` in the `--config` file. If this flag is
          not passed, `deploymentUri` *must* be specified in the file from
          `--config`.

          Can be a Google Cloud Storage (`gs://`) path or local file path (no
          prefix). In the latter case the files will be uploaded to Google Cloud
          Storage and a `--staging-bucket` argument is required.
      """).AddToParser(parser)
  flags.RUNTIME_VERSION.AddToParser(parser)
  base.ASYNC_FLAG.AddToParser(parser)
  flags.STAGING_BUCKET.AddToParser(parser)
  base.Argument(
      '--config',
      help="""\
          Path to a YAML configuration file containing configuration parameters
          for the
          [Version](https://cloud.google.com/ml/reference/rest/v1/projects.models.versions)
          to create.

          The file is in YAML format. Note that not all attributes of a Version
          are configurable; available attributes (with example values) are:

              description: A free-form description of the version.
              deploymentUri: gs://path/to/source
              runtimeVersion: '1.0'
              manualScaling:
                nodes: 10  # The number of nodes to allocate for this model.
              autoScaling:
                minNodes: 0  # The minimum number of nodes to allocate for this model.
              labels:
                user-defined-key: user-defined-value

          The name of the version must always be specified via the required
          VERSION argument.

          Only one of manualScaling or autoScaling must be specified. If both
          are specified in same yaml file an error will be returned.

          If an option is specified both in the configuration file and via
          command line arguments, the command line arguments override the
          configuration file.
      """
  ).AddToParser(parser)
  labels_util.AddCreateLabelsFlags(parser)
  flags.FRAMEWORK_MAPPER.choice_arg.AddToParser(parser)
  flags.AddPythonVersionFlag(parser, 'when creating the version')


@base.ReleaseTracks(base.ReleaseTrack.GA)
class CreateGA(base.CreateCommand):
  """Create a new Cloud ML Engine version.

  Creates a new version of a Cloud ML Engine model.

  For more details on managing ML Engine models and versions see
  https://cloud.google.com/ml-engine/docs/how-tos/managing-models-jobs
  """

  @staticmethod
  def Args(parser):
    _AddCreateArgs(parser)

  def Run(self, args):
    versions_client = versions_api.VersionsClient()
    labels = versions_util.ParseCreateLabels(versions_client, args)
    framework = flags.FRAMEWORK_MAPPER.GetEnumForChoice(args.framework)
    return versions_util.Create(
        versions_client,
        operations.OperationsClient(),
        args.version,
        model=args.model,
        origin=args.origin,
        staging_bucket=args.staging_bucket,
        runtime_version=args.runtime_version,
        config_file=args.config,
        asyncronous=args.async,
        description=args.description,
        labels=labels,
        framework=framework,
        python_version=args.python_version)


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class CreateBeta(base.CreateCommand):
  """Create a new Cloud ML Engine version.

  Creates a new version of a Cloud ML Engine model.

  For more details on managing ML Engine models and versions see
  https://cloud.google.com/ml-engine/docs/how-tos/managing-models-jobs
  """

  @staticmethod
  def Args(parser):
    _AddCreateArgs(parser)
    flags.MACHINE_TYPE.AddToParser(parser)

  def Run(self, args):
    versions_client = versions_api.VersionsClient()
    labels = versions_util.ParseCreateLabels(versions_client, args)
    framework = flags.FRAMEWORK_MAPPER.GetEnumForChoice(args.framework)
    return versions_util.Create(
        versions_client,
        operations.OperationsClient(),
        args.version,
        model=args.model,
        origin=args.origin,
        staging_bucket=args.staging_bucket,
        runtime_version=args.runtime_version,
        config_file=args.config,
        asyncronous=args.async,
        description=args.description,
        labels=labels,
        machine_type=args.machine_type,
        framework=framework,
        python_version=args.python_version)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class CreateAlpha(base.CreateCommand):
  """Create a new Cloud ML Engine version.

  Creates a new version of a Cloud ML Engine model.

  For more details on managing ML Engine models and versions see
  https://cloud.google.com/ml-engine/docs/how-tos/managing-models-jobs
  """

  @staticmethod
  def Args(parser):
    _AddCreateArgs(parser)
    flags.ALPHA_MACHINE_TYPE.AddToParser(parser)
    flags.SERVICE_ACCOUNT.AddToParser(parser)
    flags.AddUserCodeArgs(parser)
    flags.GetAcceleratorFlag().AddToParser(parser)

  def Run(self, args):
    versions_client = versions_api.VersionsClient()
    labels = versions_util.ParseCreateLabels(versions_client, args)
    framework = flags.FRAMEWORK_MAPPER.GetEnumForChoice(args.framework)
    accelerator = flags.ParseAcceleratorFlag(args.accelerator)
    return versions_util.Create(versions_client,
                                operations.OperationsClient(),
                                args.version,
                                model=args.model,
                                origin=args.origin,
                                staging_bucket=args.staging_bucket,
                                runtime_version=args.runtime_version,
                                config_file=args.config,
                                asyncronous=args.async,
                                labels=labels,
                                description=args.description,
                                machine_type=args.machine_type,
                                framework=framework,
                                python_version=args.python_version,
                                model_class=args.model_class,
                                package_uris=args.package_uris,
                                service_account=args.service_account,
                                accelerator_config=accelerator)
