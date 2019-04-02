# -*- coding: utf-8 -*- #
# Copyright 2017 Google Inc. All Rights Reserved.
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
"""Flags for binauthz command group."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope.concepts import concepts
from googlecloudsdk.command_lib.util.concepts import concept_parsers
from googlecloudsdk.command_lib.util.concepts import presentation_specs as presentation_specs_lib


def _GetNoteResourceSpec():
  return concepts.ResourceSpec(
      'containeranalysis.projects.notes',
      resource_name='note',
      projectsId=concepts.ResourceParameterAttributeConfig(
          name='project',
          help_text=(
              'The Container Analysis project for the {resource}.'),
      ),
      notesId=concepts.ResourceParameterAttributeConfig(
          name='note',
          help_text='The Container Analysis Note ID for the {resource}.',
      )
  )


def _FormatArgName(base_name, positional):
  if positional:
    return base_name.replace('-', '_').upper()
  else:
    return '--' + base_name.replace('_', '-').lower()


def GetAuthorityNotePresentationSpec(group_help,
                                     base_name='authority-note',
                                     required=True,
                                     positional=True,
                                     use_global_project_flag=False):
  """Construct a resource spec for an attestation authority note flag."""
  flag_overrides = None
  if not use_global_project_flag:
    flag_overrides = {
        'project': _FormatArgName('{}-project'.format(base_name), positional),
    }
  return presentation_specs_lib.ResourcePresentationSpec(
      name=_FormatArgName(base_name, positional),
      concept_spec=_GetNoteResourceSpec(),
      group_help=group_help,
      required=required,
      flag_name_overrides=flag_overrides,
  )


def _GetAttestorResourceSpec():
  return concepts.ResourceSpec(
      'binaryauthorization.projects.attestors',
      resource_name='attestor',
      projectsId=concepts.DEFAULT_PROJECT_ATTRIBUTE_CONFIG,
      attestorsId=concepts.ResourceParameterAttributeConfig(
          name='name',
          help_text='The ID of the {resource}.',
      ))


def GetAttestorPresentationSpec(group_help,
                                base_name='attestor',
                                required=True,
                                positional=True,
                                use_global_project_flag=True):
  """Construct a resource spec for an attestor flag."""
  flag_overrides = None
  if not use_global_project_flag:
    flag_overrides = {
        'project': _FormatArgName('{}-project'.format(base_name), positional),
    }
  return presentation_specs_lib.ResourcePresentationSpec(
      name=_FormatArgName(base_name, positional),
      concept_spec=_GetAttestorResourceSpec(),
      group_help=group_help,
      required=required,
      flag_name_overrides=flag_overrides,
  )


def AddConcepts(parser, *presentation_specs):
  concept_parsers.ConceptParser(presentation_specs).AddToParser(parser)


def AddArtifactUrlFlag(parser, required=True):
  parser.add_argument(
      '--artifact-url',
      required=required,
      type=str,
      help=('Container URL.  May be in the '
            '`*.gcr.io/repository/image` format, or may '
            'optionally contain the `http` or `https` scheme'))
