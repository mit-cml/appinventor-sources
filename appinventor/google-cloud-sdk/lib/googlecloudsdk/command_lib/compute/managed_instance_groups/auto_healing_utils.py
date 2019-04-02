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
"""Functions for dealing with managed instances groups updates."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals


from googlecloudsdk.calliope import actions
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.command_lib.compute.health_checks import flags as health_checks_flags

HEALTH_CHECK_ARG = health_checks_flags.HealthCheckArgument(
    '', '--health-check', required=False)


def AddAutohealingArgs(autohealing_params_group):
  """Adds autohealing-related commandline arguments to parser."""
  autohealing_params_group.add_argument(
      '--initial-delay',
      type=arg_parsers.Duration(),
      help="""\
      Specifies the length of time during which the instance is known to be
      initializing and should not be autohealed even if unhealthy.
      This value cannot be greater than 1 hour.
      See $ gcloud topic datetimes for information on duration formats.
      """)
  health_check_group = autohealing_params_group.add_mutually_exclusive_group()
  health_check_group.add_argument(
      '--http-health-check',
      help=('HTTP health check object used for autohealing instances in this '
            'group.'),
      action=actions.DeprecationAction(
          'http-health-check',
          warn='HttpHealthCheck is deprecated. Use --health-check instead.'))
  health_check_group.add_argument(
      '--https-health-check',
      help=('HTTPS health check object used for autohealing instances in this '
            'group.'),
      action=actions.DeprecationAction(
          'https-health-check',
          warn='HttpsHealthCheck is deprecated. Use --health-check instead.'))
  HEALTH_CHECK_ARG.AddArgument(health_check_group)
