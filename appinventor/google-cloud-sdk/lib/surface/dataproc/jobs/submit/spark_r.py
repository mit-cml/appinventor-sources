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
"""Submit a SparkR job to a cluster."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.dataproc.jobs import spark_r
from googlecloudsdk.command_lib.dataproc.jobs import submitter


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class SparkRBeta(spark_r.SparkRBase, submitter.JobSubmitterBeta):
  r"""Submit a SparkR job to a cluster.

  Submit a SparkR job to a cluster.

  ## EXAMPLES

  To submit a SparkR job with a local script, run:

    $ {command} --cluster my_cluster my_script.R

  To submit a Spark job that runs a script already on the cluster, run:

    $ {command} --cluster my_cluster file:///.../my_script.R \
        -- gs://my_bucket/data.csv
  """

  @staticmethod
  def Args(parser):
    spark_r.SparkRBase.Args(parser)
    submitter.JobSubmitterBeta.Args(parser)

  def ConfigureJob(self, messages, job, args):
    spark_r.SparkRBase.ConfigureJob(
        messages, job, self.files_by_type,
        self.BuildLoggingConfig(messages, args.driver_log_levels), args)
    submitter.JobSubmitterBeta.ConfigureJob(messages, job, args)
