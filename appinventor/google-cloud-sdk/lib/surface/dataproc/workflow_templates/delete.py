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
"""Delete workflow template command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.dataproc import dataproc as dp
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.dataproc import flags
from googlecloudsdk.core.console import console_io


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Delete(base.DeleteCommand):
  """Delete a workflow template."""

  @staticmethod
  def Args(parser):
    flags.AddTemplateResourceArg(parser, 'delete', api_version='v1')

  def Run(self, args):
    dataproc = dp.Dataproc(self.ReleaseTrack())
    messages = dataproc.messages

    template_ref = args.CONCEPTS.template.Parse()

    request = messages.DataprocProjectsRegionsWorkflowTemplatesDeleteRequest(
        name=template_ref.RelativeName())

    console_io.PromptContinue(
        message="The workflow template '[{0}]' will be deleted.".format(
            template_ref.Name()),
        cancel_on_no=True)

    dataproc.client.projects_regions_workflowTemplates.Delete(request)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class DeleteBeta(Delete):
  """Delete a workflow template."""

  @staticmethod
  def Args(parser):
    flags.AddTemplateResourceArg(parser, 'delete', api_version='v1beta2')
