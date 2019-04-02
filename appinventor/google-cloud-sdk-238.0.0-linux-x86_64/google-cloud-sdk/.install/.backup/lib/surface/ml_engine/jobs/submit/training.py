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
"""ml-engine jobs submit training command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.ml_engine import jobs
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.ml_engine import flags
from googlecloudsdk.command_lib.ml_engine import jobs_util
from googlecloudsdk.command_lib.util.args import labels_util


def _AddSubmitTrainingArgs(parser, supports_container=False):
  """Add arguments for `jobs submit training` command."""
  flags.JOB_NAME.AddToParser(parser)
  flags.PACKAGE_PATH.AddToParser(parser)
  flags.PACKAGES.AddToParser(parser)
  flags.GetModuleNameFlag(required=not supports_container).AddToParser(parser)
  compute_flags.AddRegionFlag(parser, 'machine learning training job',
                              'submit')
  flags.CONFIG.AddToParser(parser)
  flags.STAGING_BUCKET.AddToParser(parser)
  flags.GetJobDirFlag(upload_help=True).AddToParser(parser)
  flags.GetUserArgs(local=False).AddToParser(parser)
  jobs_util.ScaleTierFlagMap().choice_arg.AddToParser(parser)
  flags.RUNTIME_VERSION.AddToParser(parser)
  flags.AddPythonVersionFlag(parser, 'during training')

  sync_group = parser.add_mutually_exclusive_group()
  # TODO(b/36195821): Use the flag deprecation machinery when it supports the
  # store_true action
  sync_group.add_argument(
      '--async', action='store_true', help=(
          '(DEPRECATED) Display information about the operation in progress '
          'without waiting for the operation to complete. '
          'Enabled by default and can be omitted; use `--stream-logs` to run '
          'synchronously.'))
  sync_group.add_argument(
      '--stream-logs', action='store_true', help=(
          'Block until job completion and stream the logs while the job runs.'
          '\n\n'
          'Note that even if command execution is halted, the job will still '
          'run until cancelled with\n\n'
          '    $ gcloud ml-engine jobs cancel JOB_ID'))
  labels_util.AddCreateLabelsFlags(parser)


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Train(base.Command):
  """Submit a Cloud Machine Learning training job."""

  @staticmethod
  def Args(parser):
    _AddSubmitTrainingArgs(parser)
    parser.display_info.AddFormat(jobs_util.JOB_FORMAT)

  def Run(self, args):
    return self._Run(args)

  def _Run(self, args, supports_container_training=False,
           supports_custom_container=False):
    stream_logs = jobs_util.GetStreamLogs(args.async, args.stream_logs)
    scale_tier = jobs_util.ScaleTierFlagMap().GetEnumForChoice(args.scale_tier)
    scale_tier_name = scale_tier.name if scale_tier else None
    jobs_client = jobs.JobsClient()
    labels = jobs_util.ParseCreateLabels(jobs_client, args)
    if supports_custom_container:
      custom_container_config = (
          jobs_util.TrainingCustomInputServerConfig.FromArgs(args))
      custom_container_config.ValidateConfig()
    else:
      custom_container_config = None

    job = jobs_util.SubmitTraining(
        jobs_client, args.job,
        job_dir=args.job_dir,
        staging_bucket=args.staging_bucket,
        packages=args.packages,
        package_path=args.package_path,
        scale_tier=scale_tier_name,
        config=args.config,
        module_name=args.module_name,
        runtime_version=args.runtime_version,
        python_version=args.python_version,
        labels=labels,
        stream_logs=stream_logs,
        user_args=args.user_args,
        supports_container_training=supports_container_training,
        custom_train_server_config=custom_container_config)
    # If the job itself failed, we will return a failure status.
    if stream_logs and job.state is not job.StateValueValuesEnum.SUCCEEDED:
      self.exit_code = 1
    return job


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class TrainAlpha(Train):
  """Submit a Cloud Machine Learning training job."""

  @staticmethod
  def Args(parser):
    _AddSubmitTrainingArgs(parser, supports_container=True)
    flags.AddCustomContainerFlags(parser)
    parser.display_info.AddFormat(jobs_util.JOB_FORMAT)

  def Run(self, args):
    return self._Run(args, supports_container_training=True,
                     supports_custom_container=True)


_DETAILED_HELP = {
    'DESCRIPTION': r"""Submit a Cloud Machine Learning training job.

This creates temporary files and executes Python code staged
by a user on Google Cloud Storage. Model code can either be
specified with a path, e.g.:

    $ {command} my_job \
            --module-name trainer.task \
            --staging-bucket gs://my-bucket \
            --package-path /my/code/path/trainer \
            --packages additional-dep1.tar.gz,dep2.whl

Or by specifying an already built package:

    $ {command} my_job \
            --module-name trainer.task \
            --staging-bucket gs://my-bucket \
            --packages trainer-0.0.1.tar.gz,additional-dep1.tar.gz,dep2.whl

If `--package-path=/my/code/path/trainer` is specified and there is a
`setup.py` file at `/my/code/path/setup.py`, the setup file will be invoked
with `sdist` and the generated tar files will be uploaded to Cloud Storage.
Otherwise, a temporary `setup.py` file will be generated for the build.

By default, this command runs asynchronously; it exits once the job is
successfully submitted.

To follow the progress of your job, pass the `--stream-logs` flag (note that
even with the `--stream-logs` flag, the job will continue to run after this
command exits and must be cancelled with `gcloud ml-engine jobs cancel JOB_ID`).

For more information, see:
https://cloud.google.com/ml/docs/concepts/training-overview
"""
}

Train.detailed_help = _DETAILED_HELP
