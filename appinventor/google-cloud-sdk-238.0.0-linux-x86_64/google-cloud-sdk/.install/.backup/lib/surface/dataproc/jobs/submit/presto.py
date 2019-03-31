# -*- coding: utf-8 -*- #
# Copyright 2019 Google Inc. All Rights Reserved.
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
"""Submit a Presto job to a cluster."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.dataproc.jobs import presto
from googlecloudsdk.command_lib.dataproc.jobs import submitter


@base.Hidden
@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class PrestoBeta(presto.PrestoBase, submitter.JobSubmitterBeta):
  r"""Submit a Presto job to a cluster.

  Submit a Presto job to a cluster

  ## EXAMPLES

  To submit a Presto job with a local script, run:

    $ {command} --cluster my_cluster --file my_script.R

  To submit a Presto job with inline queries, run:

    $ {command} --cluster my_cluster -e "SELECT * FROM foo WHERE bar > 2"
  """

  @staticmethod
  def Args(parser):
    presto.PrestoBase.Args(parser)
    submitter.JobSubmitter.Args(parser)

  def ConfigureJob(self, messages, job, args):
    presto.PrestoBase.ConfigureJob(
        messages, job, self.files_by_type,
        self.BuildLoggingConfig(messages, args.driver_log_levels), args)
    submitter.JobSubmitterBeta.ConfigureJob(messages, job, args)
