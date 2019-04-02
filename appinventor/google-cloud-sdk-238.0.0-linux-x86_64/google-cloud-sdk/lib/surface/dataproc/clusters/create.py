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

"""Create cluster command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.dataproc import compute_helpers
from googlecloudsdk.api_lib.dataproc import constants
from googlecloudsdk.api_lib.dataproc import dataproc as dp
from googlecloudsdk.api_lib.dataproc import util
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.dataproc import clusters
from googlecloudsdk.command_lib.kms import resource_args as kms_resource_args
from googlecloudsdk.command_lib.util.args import labels_util


def _CommonArgs(parser, beta=False):
  """Register flags common to all tracks."""
  base.ASYNC_FLAG.AddToParser(parser)
  parser.add_argument('name', help='The name of this cluster.')
  clusters.ArgsForClusterRef(parser, beta)
  # Add gce-pd-kms-key args
  kms_flag_overrides = {'kms-key': '--gce-pd-kms-key',
                        'kms-keyring': '--gce-pd-kms-key-keyring',
                        'kms-location': '--gce-pd-kms-key-location',
                        'kms-project': '--gce-pd-kms-key-project'}
  kms_resource_args.AddKmsKeyResourceArg(
      parser, 'cluster', flag_overrides=kms_flag_overrides)


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Create(base.CreateCommand):
  """Create a cluster."""

  detailed_help = {
      'EXAMPLES': """\
          To create a cluster, run:

            $ {command} my_cluster
      """
  }

  @staticmethod
  def Args(parser):
    _CommonArgs(parser, beta=False)

  @staticmethod
  def ValidateArgs(args):

    if args.single_node:
      # --num-workers and --num-preemptible-workers must be None (unspecified)
      # or 0
      if args.num_workers:
        raise exceptions.ConflictingArgumentsException(
            '--single-node', '--num-workers')
      if args.num_preemptible_workers:
        raise exceptions.ConflictingArgumentsException(
            '--single-node', '--num-preemptible-workers')

    if constants.ALLOW_ZERO_WORKERS_PROPERTY in args.properties:
      raise exceptions.InvalidArgumentException(
          '--properties',
          'Instead of %s, use gcloud beta dataproc clusters create '
          '--single-node to deploy single node clusters' %
          constants.ALLOW_ZERO_WORKERS_PROPERTY)

  def Run(self, args):
    self.ValidateArgs(args)

    dataproc = dp.Dataproc(self.ReleaseTrack())

    cluster_ref = util.ParseCluster(args.name, dataproc)

    compute_resources = compute_helpers.GetComputeResources(
        self.ReleaseTrack(), args.name)

    beta = self.ReleaseTrack() == base.ReleaseTrack.BETA
    cluster_config = clusters.GetClusterConfig(
        args, dataproc, cluster_ref.projectId, compute_resources, beta)

    cluster = dataproc.messages.Cluster(
        config=cluster_config,
        clusterName=cluster_ref.clusterName,
        projectId=cluster_ref.projectId)

    self.ConfigureCluster(dataproc.messages, args, cluster)

    return clusters.CreateCluster(dataproc, cluster, args.async, args.timeout)

  @staticmethod
  def ConfigureCluster(messages, args, cluster):
    """Performs any additional configuration of the cluster."""
    cluster.labels = labels_util.ParseCreateArgs(args,
                                                 messages.Cluster.LabelsValue)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class CreateBeta(Create):
  """Create a cluster."""

  @staticmethod
  def Args(parser):
    _CommonArgs(parser, beta=True)
    clusters.BetaArgsForClusterRef(parser)

  @staticmethod
  def ValidateArgs(args):
    super(CreateBeta, CreateBeta).ValidateArgs(args)
    if args.master_accelerator and 'type' not in args.master_accelerator:
      raise exceptions.InvalidArgumentException(
          '--master-accelerator', 'accelerator type must be specified. '
          'e.g. --master-accelerator type=nvidia-tesla-k80,count=2')
    if args.worker_accelerator and 'type' not in args.worker_accelerator:
      raise exceptions.InvalidArgumentException(
          '--worker-accelerator', 'accelerator type must be specified. '
          'e.g. --worker-accelerator type=nvidia-tesla-k80,count=2')
