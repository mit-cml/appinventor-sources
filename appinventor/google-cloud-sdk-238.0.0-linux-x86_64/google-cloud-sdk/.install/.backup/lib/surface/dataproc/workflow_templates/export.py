# -*- coding: utf-8 -*- #
# Copyright 2018 Google Inc. All Rights Reserved.
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
"""Export workflow template command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import sys
from googlecloudsdk.api_lib.dataproc import dataproc as dp
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.dataproc import flags
from googlecloudsdk.command_lib.export import util as export_util
from googlecloudsdk.core.util import files


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA,
                    base.ReleaseTrack.GA)
class Describe(base.DescribeCommand):
  """Export a workflow template.

  Exports a workflow template's configuration to a file.
  This configuration can be imported at a later time.
  """

  @classmethod
  def GetApiVersion(cls):
    """Returns the API version based on the release track."""
    if cls.ReleaseTrack() == base.ReleaseTrack.BETA:
      return 'v1beta2'
    return 'v1'

  @classmethod
  def GetSchemaPath(cls, for_help=False):
    """Returns the resource schema path."""
    return export_util.GetSchemaPath(
        'dataproc', cls.GetApiVersion(), 'WorkflowTemplate', for_help=for_help)

  @classmethod
  def Args(cls, parser):
    flags.AddTemplateResourceArg(
        parser, 'export', api_version=cls.GetApiVersion())
    export_util.AddExportFlags(parser, cls.GetSchemaPath(for_help=True))
    flags.AddVersionFlag(parser)

  def Run(self, args):
    dataproc = dp.Dataproc(self.ReleaseTrack())

    template_ref = args.CONCEPTS.template.Parse()

    # Get specified version, or most recent version if no version arg provided.
    workflow_template = dataproc.GetRegionsWorkflowTemplate(
        template_ref, args.version)

    if args.destination:
      with files.FileWriter(args.destination) as stream:
        export_util.Export(message=workflow_template,
                           stream=stream,
                           schema_path=self.GetSchemaPath())
    else:
      export_util.Export(message=workflow_template,
                         stream=sys.stdout,
                         schema_path=self.GetSchemaPath())
