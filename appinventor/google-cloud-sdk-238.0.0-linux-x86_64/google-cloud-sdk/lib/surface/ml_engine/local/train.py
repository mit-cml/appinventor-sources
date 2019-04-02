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
"""ml-engine local train command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import os

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.ml_engine import flags
from googlecloudsdk.command_lib.ml_engine import local_train
from googlecloudsdk.core import log
from googlecloudsdk.core.util import files

_BAD_FLAGS_WARNING_MESSAGE = """\
{flag} is ignored if --distributed is not provided.
Did you mean to run distributed training?\
"""


# TODO(b/31687602) Add link to documentation of env vars once created
class RunLocal(base.Command):
  r"""Run a Cloud ML Engine training job locally.

  This command runs the specified module in an environment
  similar to that of a live Cloud ML Engine Training Job.

  This is especially useful in the case of testing distributed models,
  as it allows you to validate that you are properly interacting with the
  Cloud ML Engine cluster configuration. If your model expects a specific
  number of parameter servers or workers (i.e. you expect to use the CUSTOM
  machine type), use the --parameter-server-count and --worker-count flags to
  further specify the desired cluster configuration, just as you would in
  your cloud training job configuration:

      $ {command} --module-name trainer.task \
              --package-path /path/to/my/code/trainer \
              --distributed \
              --parameter-server-count 4 \
              --worker-count 8

  Unlike submitting a training job, the --package-path parameter can be
  omitted, and will use your current working directory.
  """

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    flags.PACKAGE_PATH.AddToParser(parser)
    flags.GetModuleNameFlag().AddToParser(parser)
    flags.DISTRIBUTED.AddToParser(parser)
    flags.PARAM_SERVERS.AddToParser(parser)
    flags.GetJobDirFlag(upload_help=False, allow_local=True).AddToParser(parser)
    flags.WORKERS.AddToParser(parser)
    flags.START_PORT.AddToParser(parser)
    flags.GetUserArgs(local=True).AddToParser(parser)

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Returns:
      Some value that we want to have printed later.
    """
    package_path = args.package_path or files.GetCWD()
    # Mimic behavior of ml-engine jobs submit training
    package_root = os.path.dirname(os.path.abspath(package_path))
    user_args = args.user_args or []
    if args.job_dir:
      user_args.extend(('--job-dir', args.job_dir))
    if args.distributed:
      retval = local_train.RunDistributed(
          args.module_name,
          package_root,
          args.parameter_server_count or 2,
          args.worker_count or 2,
          args.start_port,
          user_args=user_args)
    else:
      if args.parameter_server_count:
        log.warning(_BAD_FLAGS_WARNING_MESSAGE.format(
            flag='--parameter-server-count'))
      if args.worker_count:
        log.warning(_BAD_FLAGS_WARNING_MESSAGE.format(flag='--worker-count'))
      retval = local_train.MakeProcess(args.module_name,
                                       package_root,
                                       args=user_args,
                                       task_type='master')
    # Don't raise an exception because the users will already see the message.
    # We want this to mimic calling the script directly as much as possible.
    self.exit_code = retval
