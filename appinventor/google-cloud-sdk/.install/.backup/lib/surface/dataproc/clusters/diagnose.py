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

"""Diagnose cluster command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import encoding

from googlecloudsdk.api_lib.dataproc import dataproc as dp
from googlecloudsdk.api_lib.dataproc import exceptions
from googlecloudsdk.api_lib.dataproc import storage_helpers
from googlecloudsdk.api_lib.dataproc import util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.dataproc import flags
from googlecloudsdk.core import log
from googlecloudsdk.core.util import retry


class Diagnose(base.Command):
  """Run a detailed diagnostic on a cluster."""

  @staticmethod
  def Args(parser):
    flags.AddTimeoutFlag(parser)
    parser.add_argument(
        'name',
        help='The name of the cluster to diagnose.')

  def Run(self, args):
    dataproc = dp.Dataproc(self.ReleaseTrack())

    cluster_ref = util.ParseCluster(args.name, dataproc)

    request = dataproc.messages.DataprocProjectsRegionsClustersDiagnoseRequest(
        clusterName=cluster_ref.clusterName,
        region=cluster_ref.region,
        projectId=cluster_ref.projectId)

    operation = dataproc.client.projects_regions_clusters.Diagnose(request)
    # TODO(b/36052522): Stream output during polling.
    operation = util.WaitForOperation(
        dataproc,
        operation,
        message='Waiting for cluster diagnose operation',
        timeout_s=args.timeout)

    if not operation.response:
      raise exceptions.OperationError('Operation is missing response')

    properties = encoding.MessageToDict(operation.response)
    output_uri = properties['outputUri']

    if not output_uri:
      raise exceptions.OperationError('Response is missing outputUri')

    log.err.Print('Output from diagnostic:')
    log.err.Print('-----------------------------------------------')
    driver_log_stream = storage_helpers.StorageObjectSeriesStream(
        output_uri)
    # A single read might not read whole stream. Try a few times.
    read_retrier = retry.Retryer(max_retrials=4, jitter_ms=None)
    try:
      read_retrier.RetryOnResult(
          lambda: driver_log_stream.ReadIntoWritable(log.err),
          sleep_ms=100,
          should_retry_if=lambda *_: driver_log_stream.open)
    except retry.MaxRetrialsException:
      log.warning(
          'Diagnostic finished successfully, '
          'but output did not finish streaming.')
    log.err.Print('-----------------------------------------------')
    return output_uri
