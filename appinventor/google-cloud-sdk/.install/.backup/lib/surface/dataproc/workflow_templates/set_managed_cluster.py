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
"""Set managed cluster for workflow template command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.dataproc import compute_helpers
from googlecloudsdk.api_lib.dataproc import dataproc as dp
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.dataproc import clusters
from googlecloudsdk.command_lib.dataproc import flags
from googlecloudsdk.command_lib.util.args import labels_util


def _CommonArgs(parser, beta, include_deprecated):
  parser.add_argument(
      '--cluster-name',
      help="""\
        The name of the managed dataproc cluster.
        If unspecified, the workflow template ID will be used.""")
  clusters.ArgsForClusterRef(parser, beta, include_deprecated)


@base.ReleaseTracks(base.ReleaseTrack.GA)
class SetManagedCluster(base.UpdateCommand):
  """Set a managed cluster for the workflow template."""
  BETA = False

  @staticmethod
  def Args(parser):
    _CommonArgs(parser, beta=False, include_deprecated=False)
    flags.AddTemplateResourceArg(
        parser, 'set managed cluster', api_version='v1')

  def GetCluster(self, cluster_name):
    return compute_helpers.GetComputeResources(
        base.ReleaseTrack.GA, cluster_name)

  def Run(self, args):
    dataproc = dp.Dataproc(self.ReleaseTrack())

    template_ref = args.CONCEPTS.template.Parse()

    workflow_template = dataproc.GetRegionsWorkflowTemplate(
        template_ref, args.version)

    if args.cluster_name:
      cluster_name = args.cluster_name
    else:
      cluster_name = template_ref.workflowTemplatesId

    compute_resources = self.GetCluster(cluster_name)

    cluster_config = clusters.GetClusterConfig(
        args,
        dataproc,
        template_ref.projectsId,
        compute_resources,
        self.BETA,
        include_deprecated=self.BETA)

    labels = labels_util.ParseCreateArgs(
        args, dataproc.messages.ManagedCluster.LabelsValue)

    managed_cluster = dataproc.messages.ManagedCluster(
        clusterName=cluster_name, config=cluster_config, labels=labels)

    workflow_template.placement = dataproc.messages.WorkflowTemplatePlacement(
        managedCluster=managed_cluster)

    response = dataproc.client.projects_regions_workflowTemplates.Update(
        workflow_template)
    return response


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class SetManagedClusterBeta(SetManagedCluster):
  """Set a managed cluster for the workflow template."""
  BETA = True

  @staticmethod
  def Args(parser):
    _CommonArgs(parser, beta=True, include_deprecated=True)
    flags.AddTemplateResourceArg(
        parser, 'set managed cluster', api_version='v1beta2')
    clusters.BetaArgsForClusterRef(parser)

  def GetCluster(self, cluster_name):
    return compute_helpers.GetComputeResources(
        base.ReleaseTrack.BETA, cluster_name)
