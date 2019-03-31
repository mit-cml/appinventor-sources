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

"""The targets command group for the gcloud debug command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.debug import flags


class Logpoints(base.Group):
  """Commands for interacting with Cloud Debugger logpoints.

  Logpoints allow you to inject logging into running services without
  restarting or interfering with the normal function of the service. Log output
  will be sent to the appropriate log for the target's environment.
  On App Engine, for example, output will go to the request log.
  """

  detailed_help = {
      'EXAMPLES': """
      The following command would log the value of the "name" attribute
      of the "product" variable whenever any request executes line 123 of
      the file product.py:

          $ {command} create product.py:123 \
              "No description for {product.name}"

      The log output will appear wherever explicit logging output from your
      program is normally written. For example, for an App Engine Standard
      application, the output would appear in the request log.

      If you want to log output only when certain runtime conditions are met,
      you can add a "--condition" option:

          $ {command} create product.py:123 \
              "Suspicious price: {product.name} costs {price}" \
              --condition "price < .50"

      Logpoints remain active for 24 hours after creation. If you want to
      disable a logpoint, use the logpoints delete command:

          $ {command} delete product.*

      The above command would delete all logpoints in any file whose name
      begins with "product". If you want to delete only a single logpoint, you
      should first determine the logpoint ID using the logpoints list command,
      then delete that specific ID:

          $ {command} list
          ID                        LOCATION    ...
          567890abcdef1-1234-56789  product.py:123  ...
          $ {command} delete 567890abcdef1-1234-56789

      For App Engine services, logpoint resources include the "logQuery"
      property, which is suitable for use with the "gcloud beta logging read"
      command. You can save this property's value and use it to read logs from
      the command line:

          $ log_query=$({command} create product.py:123 \
              "No description for {product.name}" --format="value(logQuery)")
          $ gcloud logging read "$log_query"
      """
  }

  @staticmethod
  def Args(parser):
    flags.AddTargetOption(parser)
