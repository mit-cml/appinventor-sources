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

"""Submit a Hive job to a cluster."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.dataproc.jobs import hive
from googlecloudsdk.command_lib.dataproc.jobs import submitter


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Hive(hive.HiveBase, submitter.JobSubmitter):
  """Submit a Hive job to a cluster.

  Submit a Hive job to a cluster.

  ## EXAMPLES

  To submit a Hive job with a local script, run:

    $ {command} --cluster my_cluster --file my_queries.q

  To submit a Hive job with inline queries, run:

    $ {command} --cluster my_cluster -e "CREATE EXTERNAL TABLE foo(bar int) LOCATION 'gs://my_bucket/'" -e "SELECT * FROM foo WHERE bar > 2"
  """

  @staticmethod
  def Args(parser):
    hive.HiveBase.Args(parser)
    submitter.JobSubmitter.Args(parser)

  def ConfigureJob(self, messages, job, args):
    hive.HiveBase.ConfigureJob(messages, job, self.files_by_type, args)
    submitter.JobSubmitter.ConfigureJob(messages, job, args)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class HiveBeta(hive.HiveBase, submitter.JobSubmitterBeta):
  """Submit a Hive job to a cluster.

  Submit a Hive job to a cluster.

  ## EXAMPLES

  To submit a Hive job with a local script, run:

    $ {command} --cluster my_cluster --file my_queries.q

  To submit a Hive job with inline queries, run:

    $ {command} --cluster my_cluster -e "CREATE EXTERNAL TABLE foo(bar int) LOCATION 'gs://my_bucket/'" -e "SELECT * FROM foo WHERE bar > 2"
  """

  @staticmethod
  def Args(parser):
    hive.HiveBase.Args(parser)
    submitter.JobSubmitterBeta.Args(parser)

  def ConfigureJob(self, messages, job, args):
    hive.HiveBase.ConfigureJob(messages, job, self.files_by_type, args)
    submitter.JobSubmitterBeta.ConfigureJob(messages, job, args)
