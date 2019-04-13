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
"""Create workflow template command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.dataproc import dataproc as dp
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.dataproc import flags
from googlecloudsdk.command_lib.util.args import labels_util


def _CommonArgs(parser):
  labels_util.AddCreateLabelsFlags(parser)


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Create(base.CreateCommand):
  """Create a workflow template."""

  @staticmethod
  def Args(parser):
    _CommonArgs(parser)
    flags.AddTemplateResourceArg(parser, 'create', api_version='v1')

  def Run(self, args):
    dataproc = dp.Dataproc(self.ReleaseTrack())
    messages = dataproc.messages

    template_ref = args.CONCEPTS.template.Parse()
    # TODO(b/109837200) make the dataproc discovery doc parameters consistent
    # Parent() fails for the collection because of projectId/projectsId and
    # regionId/regionsId inconsistencies.
    # parent = template_ref.Parent().RelativePath()
    parent = '/'.join(template_ref.RelativeName().split('/')[0:4])

    workflow_template = messages.WorkflowTemplate(
        id=template_ref.Name(), name=template_ref.RelativeName(),
        labels=labels_util.ParseCreateArgs(
            args, messages.WorkflowTemplate.LabelsValue))

    request = messages.DataprocProjectsRegionsWorkflowTemplatesCreateRequest(
        parent=parent, workflowTemplate=workflow_template)

    template = dataproc.client.projects_regions_workflowTemplates.Create(
        request)
    return template


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class CreateBeta(Create):
  """Create a workflow template."""

  @staticmethod
  def Args(parser):
    _CommonArgs(parser)
    flags.AddTemplateResourceArg(parser, 'create', api_version='v1beta2')
