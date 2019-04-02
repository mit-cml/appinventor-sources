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
"""Set cluster selector for workflow-template command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.dataproc import dataproc as dp
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.dataproc import flags
from googlecloudsdk.command_lib.util.args import labels_util


def _CommonArgs(parser):
  parser.add_argument(
      '--cluster-labels',
      metavar='KEY=VALUE',
      type=arg_parsers.ArgDict(
          key_type=labels_util.KEY_FORMAT_VALIDATOR,
          value_type=labels_util.VALUE_FORMAT_VALIDATOR,
          min_length=1),
      action=arg_parsers.UpdateAction,
      help='A list of label KEY=VALUE pairs to add.')


@base.ReleaseTracks(base.ReleaseTrack.GA)
class SetClusterSelector(base.UpdateCommand):
  """Set cluster selector for the workflow template."""

  @staticmethod
  def Args(parser):
    _CommonArgs(parser)
    flags.AddTemplateResourceArg(
        parser, 'set cluster selector', api_version='v1')

  def Run(self, args):
    dataproc = dp.Dataproc(self.ReleaseTrack())

    template_ref = args.CONCEPTS.template.Parse()

    workflow_template = dataproc.GetRegionsWorkflowTemplate(
        template_ref, args.version)

    labels = labels_util.Diff(additions=args.cluster_labels).Apply(
        dataproc.messages.ClusterSelector.ClusterLabelsValue).GetOrNone()

    cluster_selector = dataproc.messages.ClusterSelector(clusterLabels=labels)

    workflow_template.placement = dataproc.messages.WorkflowTemplatePlacement(
        clusterSelector=cluster_selector)

    response = dataproc.client.projects_regions_workflowTemplates.Update(
        workflow_template)
    return response


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class SetClusterSelectorBeta(SetClusterSelector):
  """Set cluster selector for the workflow template."""

  @staticmethod
  def Args(parser):
    _CommonArgs(parser)
    flags.AddTemplateResourceArg(
        parser, 'set cluster selector', api_version='v1beta2')
