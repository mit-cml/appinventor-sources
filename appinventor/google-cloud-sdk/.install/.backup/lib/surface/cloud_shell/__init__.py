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
"""The command group for the shell CLI."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import textwrap

from googlecloudsdk.calliope import base


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class Shell(base.Group):
  """Manage Google Cloud Shell."""

  detailed_help = {
      'DESCRIPTION':
          """\
          The gcloud shell command group lets you interact with and connect to
          your Google Cloud Shell environment.

          More information on Cloud Shell can be found at
          https://cloud.google.com/shell/docs/.
          """,
      'NOTES':
          textwrap.dedent("""\
          The previous *gcloud alpha shell* command to launch an interactive
          shell was renamed to *gcloud alpha interactive*.
          """),
  }

  @staticmethod
  def Args(parser):
    pass
