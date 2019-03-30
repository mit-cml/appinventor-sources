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
"""Flags for commands that deal with the CRM API."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.resource_manager import exceptions
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions as calliope_exceptions


def FolderIdArg(use_description):
  return base.Argument(
      'id',
      metavar='FOLDER_ID',
      help='ID for the folder {0}'.format(use_description))


@base.Hidden
def FolderIdFlag(use_description):
  return base.Argument(
      '--folder',
      metavar='FOLDER_ID',
      default=None,
      help='ID for the folder {0}'.format(use_description))


def OrganizationIdFlag(use_description):
  return base.Argument(
      '--organization',
      metavar='ORGANIZATION_ID',
      help='ID for the organization {0}'.format(use_description))


def OperationIdArg(use_description):
  return base.Argument(
      'id',
      metavar='OPERATION_ID',
      help='ID for the operation {0}'.format(use_description))


def OperationAsyncFlag():
  return base.Argument(
      '--async',
      action='store_true',
      help=(
          'Whether to return an asynchronous long-running operation immediately'
          ' instead of waiting for the operation to finish'))


def LienIdArg(use_description):
  return base.Argument(
      'id',
      metavar='LIEN_ID',
      help='ID for the lien {0}'.format(use_description))


def AddParentFlagsToParser(parser):
  FolderIdFlag('to use as a parent').AddToParser(parser)
  OrganizationIdFlag('to use as a parent').AddToParser(parser)


def GetParentFromFlags(args):
  if getattr(args, 'folder', None):
    return 'folders/{0}'.format(args.folder)
  elif args.organization:
    return 'organizations/{0}'.format(args.organization)
  else:
    return None


def CheckParentFlags(args, parent_required=True):
  """Assert that there are no conflicts with parent flags.

  Ensure that both the organization flag and folder flag are not set at the
  same time. This is a little tricky since the folder flag doesn't exist for
  all commands which accept a parent specification.

  Args:
    args: The argument object
    parent_required: True to assert that a parent flag was set
  """
  if getattr(args, 'folder', None) and args.organization:
    raise calliope_exceptions.ConflictingArgumentsException(
        '--folder', '--organization')
  if parent_required:
    if 'folder' in args and not args.folder and not args.organization:
      raise exceptions.ArgumentError(
          'Neither --folder nor --organization provided, exactly one required')
    elif 'folder' not in args and not args.organization:
      raise exceptions.ArgumentError('--organization is required')
