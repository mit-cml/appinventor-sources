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
"""Flags and helpers for the compute images packages commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.command_lib.compute.images.packages import resource_args
from googlecloudsdk.command_lib.util.concepts import concept_parsers


def AddImageResourceArg(parser):
  """Add resource arg of image for 'packages list' command."""
  concept_parsers.ConceptParser(
      [
          resource_args.CreateImageResourcePresentationSpec(
              'Name of the disk image.'),
      ]
  ).AddToParser(parser)


def AddResourceArgs(parser):
  """Add resource args of images for 'packages diff' command."""
  concept_parsers.ConceptParser(
      [
          resource_args.CreateImageResourcePresentationSpec(
              'Name of the disk image as the diff base.', 'base'
          ),
          resource_args.CreateImageResourcePresentationSpec(
              'Name of the disk image to diff with base image.', 'diff'
          ),
      ]
  ).AddToParser(parser)


def AddShowAddedPackagesFlag(parser, use_default_value=True):
  """Add --show-added-packages Boolean flag."""
  help_text = ('Show only the packages added to the diff image.')
  action = ('store_true' if use_default_value else
            arg_parsers.StoreTrueFalseAction)
  parser.add_argument(
      '--show-added-packages',
      help=help_text,
      action=action)


def AddShowRemovedPackagesFlag(parser, use_default_value=True):
  """Add --show-removed-packages Boolean flag."""
  help_text = ('Show only the packages removed from the base image.')
  action = ('store_true' if use_default_value else
            arg_parsers.StoreTrueFalseAction)
  parser.add_argument(
      '--show-removed-packages',
      help=help_text,
      action=action)


def AddShowUpdatedPackagesFlag(parser, use_default_value=True):
  """Add --show-updated-packages Boolean flag."""
  help_text = ('Show only the packages updated between two images.')
  action = ('store_true' if use_default_value else
            arg_parsers.StoreTrueFalseAction)
  parser.add_argument(
      '--show-updated-packages',
      help=help_text,
      action=action)
