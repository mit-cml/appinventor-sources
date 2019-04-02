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

"""Common flags for projects commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.resource_manager import completers


def GetProjectFlag(verb):
  return base.Argument(
      'id',
      metavar='PROJECT_ID',
      completer=completers.ProjectCompleter,
      help='ID for the project you want to {0}.'.format(verb))


CREATE_DELETE_IN_CONSOLE_SEE_ALSO = (
    'See https://support.google.com/cloud/answer/6251787 for information on '
    'creating or deleting projects from the Google Cloud Platform Console.')
