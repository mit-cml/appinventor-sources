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
"""Shared resource flags for kms resources."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope.concepts import concepts
from googlecloudsdk.calliope.concepts import deps
from googlecloudsdk.command_lib.util.concepts import concept_parsers
from googlecloudsdk.command_lib.util.concepts import presentation_specs
from googlecloudsdk.core import properties


def KeyAttributeConfig():
  return concepts.ResourceParameterAttributeConfig(
      name='kms-key', help_text='The KMS key of the {resource}.')


def KeyringAttributeConfig():
  return concepts.ResourceParameterAttributeConfig(
      name='kms-keyring', help_text='The KMS keyring of the {resource}.')


def LocationAttributeConfig(region_fallthrough=False):
  fallthroughs = []
  if region_fallthrough:
    fallthroughs.append(deps.ArgFallthrough('--region'))
  return concepts.ResourceParameterAttributeConfig(
      name='kms-location',
      help_text='The Cloud location for the {resource}.',
      fallthroughs=fallthroughs)


def ProjectAttributeConfig():
  return concepts.ResourceParameterAttributeConfig(
      name='kms-project',
      help_text='The Cloud project for the {resource}.',
      fallthroughs=[deps.PropertyFallthrough(properties.VALUES.core.project)])


def GetKmsKeyResourceSpec(region_fallthrough=False):
  return concepts.ResourceSpec(
      'cloudkms.projects.locations.keyRings.cryptoKeys',
      resource_name='key',
      cryptoKeysId=KeyAttributeConfig(),
      keyRingsId=KeyringAttributeConfig(),
      locationsId=LocationAttributeConfig(
          region_fallthrough=region_fallthrough),
      projectsId=ProjectAttributeConfig(),
      disable_auto_completers=False)


def GetKmsKeyPresentationSpec(resource,
                              region_fallthrough=False,
                              flag_overrides=None,
                              permission_info=None):
  """Return a Presentation Spec for kms key resource argument.

  Args:
    resource: str, the name of the resource that the cryptokey will be used to
    protect.
    region_fallthrough: bool, True if the command has a region flag that should
      be used as a fallthrough for the kms location.
    flag_overrides: dict, The default flag names are 'kms-key', 'kms-keyring',
      'kms-location' and 'kms-project'. You can pass a dict of overrides where
      the keys of the dict are the default flag names, and the values are the
      override names.
    permission_info: str, optional permission info that overrides default
      permission info group help.

  Returns:
    Presentation spec suitable for adding to concept parser.
  """

  if not permission_info:
    permission_info = '{} must hold permission {}'.format(
        "The 'Compute Engine Service Agent' service account",
        "'Cloud KMS CryptoKey Encrypter/Decrypter'")

  group_help = ('The Cloud KMS (Key Management Service) cryptokey that will be'
                ' used to protect the {}. {}.'.format(
                    resource, permission_info))

  presentation_spec = presentation_specs.ResourcePresentationSpec(
      '--kms-key',
      GetKmsKeyResourceSpec(region_fallthrough=region_fallthrough),
      group_help,
      flag_name_overrides=flag_overrides or {})
  return presentation_spec


def AddKmsKeyResourceArg(parser,
                         resource,
                         region_fallthrough=False,
                         flag_overrides=None,
                         permission_info=None,
                         required=False,
                         name='--kms-key'):
  """Add a resource argument for a KMS key.

  Args:
    parser: the parser for the command.
    resource: str, the name of the resource that the cryptokey will be used to
      protect.
    region_fallthrough: bool, True if the command has a region flag that should
      be used as a fallthrough for the kms location.
    flag_overrides: dict, The default flag names are 'kms-key', 'kms-keyring',
      'kms-location' and 'kms-project'. You can pass a dict of overrides where
      the keys of the dict are the default flag names, and the values are the
      override names.
    permission_info: str, optional permission info that overrides default
      permission info group help.
    required: bool, optional. True if the flag must be parsable by the parser.
      The default value is False.
    name: str, optional name of the arg for the KMS resource.
      Defaults to '--kms-key'.
  """
  if not permission_info:
    permission_info = '{} must hold permission {}'.format(
        "The 'Compute Engine Service Agent' service account",
        "'Cloud KMS CryptoKey Encrypter/Decrypter'")
  concept_parsers.ConceptParser.ForResource(
      name,
      GetKmsKeyResourceSpec(region_fallthrough=region_fallthrough),
      'The Cloud KMS (Key Management Service) cryptokey that will be used to '
      'protect the {}. {}.'.format(resource, permission_info),
      flag_name_overrides=flag_overrides,
      required=required).AddToParser(parser)
