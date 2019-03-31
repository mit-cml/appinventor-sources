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
"""Resource arguments for GCE Image Packages."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope.concepts import concepts
from googlecloudsdk.command_lib.util.concepts import presentation_specs


def ImagesAttributeConfig():
  return concepts.ResourceParameterAttributeConfig(
      name='image',
      help_text='Name of the image.')


def GetImageResourceSpec():
  return concepts.ResourceSpec(
      'compute.images',
      resource_name='image',
      project=concepts.DEFAULT_PROJECT_ATTRIBUTE_CONFIG,
      disable_auto_completers=False)


def CreateImageResourcePresentationSpec(group_help, image_prefix=''):
  name, flag_name_overrides = '--image', {}
  if image_prefix:
    name = '--{}-image'.format(image_prefix)
    flag_name_overrides = {'project': '--{}-project'.format(image_prefix)}
  return presentation_specs.ResourcePresentationSpec(
      name,
      GetImageResourceSpec(),
      group_help=group_help,
      required=True,
      prefixes=False,
      flag_name_overrides=flag_name_overrides,
  )
