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
"""Common sql utility functions for validating."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import exceptions


def ValidateInstanceName(instance_name):
  if ':' in instance_name:
    name_components = instance_name.split(':')
    possible_project = name_components[0]
    possible_instance = name_components[-1]
    raise exceptions.ToolException("""\
Instance names cannot contain the ':' character. If you meant to indicate the
project for [{instance}], use only '{instance}' for the argument, and either add
'--project {project}' to the command line or first run
  $ gcloud config set project {project}
""".format(project=possible_project, instance=possible_instance))
