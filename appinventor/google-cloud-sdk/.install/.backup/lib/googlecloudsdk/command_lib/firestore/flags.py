# -*- coding: utf-8 -*- #
# Copyright 2016 Google Inc. All Rights Reserved.
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
"""Flags and helpers for the firestore related commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.firestore import util
from googlecloudsdk.command_lib.util.apis import arg_utils


def AddCollectionIdsFlag(parser):
  """Adds flag for collection ids to the given parser."""
  parser.add_argument(
      '--collection-ids',
      metavar='COLLECTION_IDS',
      type=arg_parsers.ArgList(),
      help="""
      A list specifying which collections will be included in the operation.
      When omitted, all collections are included.

      For example, to operate on only the 'customers' and 'orders'
      collections:

        $ {command} --collection-ids='customers','orders'
      """)


def GetIndexArg():
  """Returns the --index arg for field updates."""
  messages = util.GetMessagesModule()

  def _OrderToEnum(order):
    return arg_utils.ChoiceToEnum(
        order,
        messages.GoogleFirestoreAdminV1beta2IndexField.OrderValueValuesEnum,
        valid_choices=['ascending', 'descending'])

  def _ArrayConfigToEnum(array_config):
    return arg_utils.ChoiceToEnum(
        array_config,
        (messages.GoogleFirestoreAdminV1beta2IndexField.
         ArrayConfigValueValuesEnum),
        valid_choices=['contains'])

  spec = {'order': _OrderToEnum,
          'array-config': _ArrayConfigToEnum}
  help_text = """\
An index for the field.

This flag can be repeated to provide multiple indexes. Any existing indexes will
be overwritten with the ones provided. Any omitted indexes will be deleted if
they currently exist. For example, to explicitly set the list of indexes for the
*tags* field in the *Events* collection group to *[ascending, contains]*, run:

  $ {command} tags \\
      --collection-group=Events \\
      --index order=ascending --index array-config=contains

The following keys are allowed:

*order*:::: Specifies the order. Valid options are: 'ascending', 'descending'.
Exactly one of 'order' or 'array-config' must be specified.

*array-config*:::: Specifies the configuration for an array field. The only
valid option is 'contains'. Exactly one of 'order' or 'array-config' must be
specified."""

  index_arg = base.Argument(
      '--index',
      type=arg_parsers.ArgDict(spec=spec),
      help=help_text,
      action='append')

  return index_arg


def GetDisableIndexesArg():
  """Returns the --disable-indexes arg for field updates."""
  return base.Argument(
      '--disable-indexes',
      action='store_true',
      help='If provided, the field will no longer be indexed at all.')


def GetClearExemptionArg():
  """Returns the --clear-exemption arg for field updates."""
  help_text = ("If provided, the field's current index configuration will be "
               "reverted to inherit from its ancestor index configurations.")
  return base.Argument(
      '--clear-exemption',
      action='store_true',
      help=help_text)


# TODO(b/120915050): Remove this and use native argument group in the spec
def AddFieldUpdateArgs():
  """Python hook to add the argument group for field updates.

  Returns:
    List consisting of the field update arg group.
  """
  group = base.ArgumentGroup(mutex=True, required=True)
  group.AddArgument(GetIndexArg())
  group.AddArgument(GetDisableIndexesArg())
  group.AddArgument(GetClearExemptionArg())
  return [group]
