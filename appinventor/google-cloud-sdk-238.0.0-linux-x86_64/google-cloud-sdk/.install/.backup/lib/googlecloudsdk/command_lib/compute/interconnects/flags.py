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
"""Flags and helpers for the compute interconnects commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import actions as calliope_actions
from googlecloudsdk.command_lib.compute import completers as compute_completers
from googlecloudsdk.command_lib.compute import flags as compute_flags


_INTERCONNECT_TYPE_CHOICES_GA = {
    'DEDICATED': 'Dedicated private interconnect.',
    'PARTNER': 'Partner interconnect. Only available to approved partners.',
}

_INTERCONNECT_TYPE_CHOICES_BETA_AND_ALPHA = {
    'IT_PRIVATE':
        'Dedicated private interconnect. (Warning: IT_PRIVATE is deprecated, '
        'use DEDICATED instead.)',
    'DEDICATED':
        'Dedicated private interconnect.',
    'PARTNER':
        'Partner interconnect. Only available to approved partners.',
}

_LINK_TYPE_CHOICES_100G = {
    'LINK_TYPE_ETHERNET_10G_LR': '10Gbps Ethernet, LR Optics.',
    'LINK_TYPE_ETHERNET_100G_LR': '100Gbps Ethernet, LR Optics.'
}

_LINK_TYPE_CHOICES = {
    'LINK_TYPE_ETHERNET_10G_LR': '10Gbps Ethernet, LR Optics.',
}


class InterconnectsCompleter(compute_completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(InterconnectsCompleter, self).__init__(
        collection='compute.interconnects',
        list_command='alpha compute interconnects list --uri',
        **kwargs)


def InterconnectArgument(required=True, plural=False):
  return compute_flags.ResourceArgument(
      resource_name='interconnect',
      completer=InterconnectsCompleter,
      plural=plural,
      required=required,
      global_collection='compute.interconnects')


def InterconnectArgumentForOtherResource(short_help,
                                         required=True,
                                         detailed_help=None):
  return compute_flags.ResourceArgument(
      name='--interconnect',
      resource_name='interconnect',
      completer=InterconnectsCompleter,
      plural=False,
      required=required,
      global_collection='compute.interconnects',
      short_help=short_help,
      detailed_help=detailed_help)


def GetInterconnectType(messages, interconnect_type_arg):
  """Converts the interconnect type flag to a message enum.

  Args:
    messages: The API messages holder.
    interconnect_type_arg: The interconnect type flag value.

  Returns:
    An InterconnectTypeValueValuesEnum of the flag value, or None if absent.
  """
  if interconnect_type_arg is None:
    return None
  else:
    return messages.Interconnect.InterconnectTypeValueValuesEnum(
        interconnect_type_arg)


def GetLinkType(messages, link_type_arg):
  """Converts the link type flag to a message enum.

  Args:
    messages: The API messages holder.
    link_type_arg: The link type flag value.
  Returns:
    An LinkTypeValueValuesEnum of the flag value, or None if absent.
  """
  if link_type_arg is None:
    return None
  else:
    return messages.Interconnect.LinkTypeValueValuesEnum(link_type_arg)


def AddCreateCommonArgs(parser, supports_100g=False):
  """Adds shared flags for create command to the argparse.ArgumentParser."""
  AddAdminEnabled(parser)
  AddDescription(parser)
  AddCustomerName(parser)
  AddLinkType(parser, supports_100g)
  AddNocContactEmail(parser)
  AddRequestedLinkCount(parser)


def AddCreateGaArgs(parser):
  """Adds GA flags for create command to the argparse.ArgumentParser."""
  AddCreateCommonArgs(parser)
  AddInterconnectTypeGA(parser)


def AddCreateBetaArgs(parser, supports_100g=False):
  """Adds beta flags for create command to the argparse.ArgumentParser."""
  AddCreateCommonArgs(parser, supports_100g)
  AddInterconnectTypeBetaAndAlpha(parser)


def AddDescription(parser):
  """Adds description flag to the argparse.ArgumentParser."""
  parser.add_argument(
      '--description',
      help='An optional, textual description for the interconnect.')


def AddInterconnectTypeGA(parser):
  """Adds interconnect-type flag to the argparse.ArgumentParser."""
  parser.add_argument(
      '--interconnect-type',
      choices=_INTERCONNECT_TYPE_CHOICES_GA,
      required=True,
      help="""\
      Type of the interconnect.
      """)


def _ShouldShowDeprecatedWarning(value):
  return value and value.upper() == 'IT_PRIVATE'


def AddInterconnectTypeBetaAndAlpha(parser):
  """Adds interconnect-type flag to the argparse.ArgumentParser."""
  parser.add_argument(
      '--interconnect-type',
      choices=_INTERCONNECT_TYPE_CHOICES_BETA_AND_ALPHA,
      action=calliope_actions.DeprecationAction(
          'interconnect-type',
          removed=False,
          show_message=_ShouldShowDeprecatedWarning,
          warn=('IT_PRIVATE will be deprecated '
                'for {flag_name}. '
                'Please use DEDICATED instead.'),
          error='Value IT_PRIVATE for {flag_name} has been removed. '
                'Please use DEDICATED instead.'),
      required=True,
      help="""\
      Type of the interconnect.
      """)


def AddLinkType(parser, supports_100g=False):
  """Adds link-type flag to the argparse.ArgumentParser."""
  link_types = _LINK_TYPE_CHOICES
  if supports_100g:
    link_types = _LINK_TYPE_CHOICES_100G
  parser.add_argument(
      '--link-type',
      choices=link_types,
      required=True,
      help="""\
      Type of the link for the interconnect.
      """)


def AddRequestedLinkCount(parser):
  """Adds requestedLinkCount flag to the argparse.ArgumentParser."""
  parser.add_argument(
      '--requested-link-count',
      required=True,
      type=int,
      help="""\
      Target number of physical links in the link bundle.
      """)


def AddRequestedLinkCountForUpdate(parser):
  """Adds requestedLinkCount flag to the argparse.ArgumentParser."""
  parser.add_argument(
      '--requested-link-count',
      type=int,
      help="""\
      Target number of physical links in the link bundle.
      """)


def AddNocContactEmail(parser):
  """Adds nocContactEmail flag to the argparse.ArgumentParser."""
  parser.add_argument(
      '--noc-contact-email',
      help="""\
      Email address to contact the customer NOC for operations and maintenance
      notifications regarding this interconnect.
      """)


def AddCustomerName(parser):
  """Adds customerName flag to the argparse.ArgumentParser."""
  parser.add_argument(
      '--customer-name',
      required=True,
      help="""\
      Customer name to put in the Letter of Authorization as the party
      authorized to request an interconnect.
      """)


def AddAdminEnabled(parser):
  """Adds adminEnabled flag to the argparse.ArgumentParser."""
  admin_enabled_args = parser.add_mutually_exclusive_group()
  admin_enabled_args.add_argument(
      '--admin-enabled',
      action='store_true',
      default=None,
      help="""\
      Administrative status of the interconnect. If not provided on creation,
      defaults to enabled.
      When this is enabled, the interconnect is operational and will carry
      traffic across any functioning linked interconnect attachments. Use
      --no-admin-enabled to disable it.
      """)


def AddAdminEnabledForUpdate(parser):
  """Adds adminEnabled flag to the argparse.ArgumentParser."""
  admin_enabled_args = parser.add_mutually_exclusive_group()
  admin_enabled_args.add_argument(
      '--admin-enabled',
      action='store_true',
      default=None,
      help="""\
      Administrative status of the interconnect.
      When this is enabled, the interconnect is operational and will carry
      traffic across any functioning linked interconnect attachments. Use
      --no-admin-enabled to disable it.
      """)
