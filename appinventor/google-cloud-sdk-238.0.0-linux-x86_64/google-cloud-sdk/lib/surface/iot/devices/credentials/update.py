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
"""`gcloud iot credentials describe` command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.cloudiot import devices
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.iot import flags
from googlecloudsdk.command_lib.iot import resource_args
from googlecloudsdk.command_lib.iot import util
from googlecloudsdk.core import log
from googlecloudsdk.core.util import times


class Update(base.DescribeCommand):
  """Update a specific device credential."""

  @staticmethod
  def Args(parser):
    resource_args.AddDeviceResourceArg(parser,
                                       'for which to update credentials',
                                       positional=False)
    flags.GetIndexFlag('credential', 'to update').AddToParser(parser)
    flags.AddDeviceCredentialFlagsToParser(parser, combine_flags=False,
                                           only_modifiable=True)

  def Run(self, args):
    client = devices.DevicesClient()

    device_ref = args.CONCEPTS.device.Parse()

    credentials = client.Get(device_ref).credentials
    try:
      if args.expiration_time:
        credentials[args.index].expirationTime = (
            times.FormatDateTime(args.expiration_time))
    except IndexError:
      raise util.BadCredentialIndexError(device_ref.Name(), credentials,
                                         args.index)
    response = client.Patch(device_ref, credentials=credentials)
    log.UpdatedResource(device_ref.Name(), 'credentials for device')
    return response
