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
"""Flags for the compute instance groups managed commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base

DEFAULT_LIST_FORMAT = """\
    table(
      name,
      location():label=LOCATION,
      location_scope():label=SCOPE,
      baseInstanceName,
      size,
      targetSize,
      instanceTemplate.basename(),
      autoscaled
    )
"""


def AddTypeArg(parser):
  parser.add_argument(
      '--type',
      choices={
          'opportunistic': 'Do not proactively replace instances. Create new '
                           'instances and delete old on resizes of the group.',
          'proactive': 'Replace instances proactively.',
      },
      default='proactive',
      category=base.COMMONLY_USED_FLAGS,
      help='Desired update type.')


def AddMaxSurgeArg(parser):
  parser.add_argument(
      '--max-surge',
      type=str,
      help=('Maximum additional number of instances that '
            'can be created during the update process. '
            'This can be a fixed number (e.g. 5) or '
            'a percentage of size to the managed instance '
            'group (e.g. 10%)'))


def AddMaxUnavailableArg(parser):
  parser.add_argument(
      '--max-unavailable',
      type=str,
      help=('Maximum number of instances that can be '
            'unavailable during the update process. '
            'This can be a fixed number (e.g. 5) or '
            'a percentage of size to the managed instance '
            'group (e.g. 10%)'))


def AddMinReadyArg(parser):
  parser.add_argument(
      '--min-ready',
      type=arg_parsers.Duration(lower_bound='0s'),
      help=('Minimum time for which a newly created instance '
            'should be ready to be considered available. For example `10s` '
            'for 10 seconds. See $ gcloud topic datetimes for information '
            'on duration formats.'))


def AddForceArg(parser):
  parser.add_argument(
      '--force',
      action='store_true',
      help=('If set, accepts any original or new version '
            'configurations without validation.'))
