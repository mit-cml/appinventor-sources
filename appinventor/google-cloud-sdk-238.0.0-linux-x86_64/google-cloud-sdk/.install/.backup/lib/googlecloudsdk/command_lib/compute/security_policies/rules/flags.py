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
"""Flags and helpers for the compute security policies rules commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.command_lib.compute import completers as compute_completers


class SecurityPolicyRulesCompleter(compute_completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(SecurityPolicyRulesCompleter, self).__init__(
        collection='compute.securityPolicyRules',
        **kwargs)


def AddPriority(parser, operation, is_plural=False):
  """Adds the priority argument to the argparse."""
  parser.add_argument(
      'name' + ('s' if is_plural else ''),
      metavar='PRIORITY',
      nargs='*' if is_plural else None,
      completer=SecurityPolicyRulesCompleter,
      help=('The priority of the rule{0} to {1}. Rules are evaluated in order '
            'from highest priority to lowest priority where 0 is the highest '
            'priority and 2147483647 is the lowest priority.'.format(
                's' if is_plural else '', operation)))


def AddMatcher(parser, required=True):
  """Adds the matcher arguments to the argparse."""
  matcher = parser.add_group(mutex=True,
                             required=required,
                             help='Security policy rule matcher.')
  matcher.add_argument(
      '--src-ip-ranges',
      type=arg_parsers.ArgList(),
      metavar='SRC_IP_RANGE',
      help=('The source IPs/IP ranges to match for this rule. '
            'To match all IPs specify *.'))
  matcher.add_argument(
      '--expression',
      help='The Cloud Armor rules language expression to match for this rule.')


def AddAction(parser, required=True):
  """Adds the action argument to the argparse."""
  parser.add_argument(
      '--action',
      choices=['allow', 'deny-403', 'deny-404', 'deny-502'],
      type=lambda x: x.lower(),
      required=required,
      help='The action to take if the request matches the match condition.')


def AddDescription(parser):
  """Adds the preview argument to the argparse."""
  parser.add_argument(
      '--description',
      help='An optional, textual description for the rule.')


def AddPreview(parser, default):
  """Adds the preview argument to the argparse."""
  parser.add_argument(
      '--preview',
      action='store_true',
      default=default,
      help='If specified, the action will not be enforced.')
