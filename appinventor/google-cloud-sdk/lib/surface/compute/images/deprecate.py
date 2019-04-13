# -*- coding: utf-8 -*- #
# Copyright 2014 Google Inc. All Rights Reserved.
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
"""Command for deprecating images."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import datetime

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions as calliope_exceptions
from googlecloudsdk.command_lib.compute.images import flags


def _ResolveTime(absolute, relative_sec, current_time):
  """Get the RFC 3339 time string for a provided absolute or relative time."""
  if absolute:
    # TODO(b/36057353): It's unfortunate that datetime.datetime cannot
    # parse from RFC 3339, but it can output to it. It would be
    # super cool if we could verify the validity of the user's
    # input here and fail fast if an invalid date/time is given.
    # For now, I assume that the user's input is valid.
    return absolute
  elif relative_sec:
    return (
        current_time + datetime.timedelta(seconds=relative_sec)
    ).replace(microsecond=0).isoformat()
  else:
    return None


class DeprecateImages(base.SilentCommand):
  """Manage deprecation status of Google Compute Engine images.

  *{command}* is used to deprecate images.
  """

  @staticmethod
  def Args(parser):
    DeprecateImages.DISK_IMAGE_ARG = flags.MakeDiskImageArg()
    DeprecateImages.DISK_IMAGE_ARG.AddArgument(parser)
    flags.REPLACEMENT_DISK_IMAGE_ARG.AddArgument(parser)

    deprecation_statuses = {
        'ACTIVE': 'The image is currently supported.',
        'DELETED': (
            'New uses result in an error. Setting this state will not '
            'automatically delete the image. You must still make a request to '
            'delete the image to remove it from the image list.'),
        'DEPRECATED': (
            'Operations which create a new *DEPRECATED* resource return '
            'successfully, but with a warning indicating that the image is '
            'deprecated and recommending its replacement.'),
        'OBSOLETE': 'New uses result in an error.',
    }

    parser.add_argument(
        '--state',
        choices=deprecation_statuses,
        default='ACTIVE',
        type=lambda x: x.upper(),
        required=True,
        help='The deprecation state to set on the image.')

    delete_group = parser.add_mutually_exclusive_group()

    delete_group.add_argument(
        '--delete-on',
        help="""\
        Similar to *--delete-in*, but specifies an absolute time when the image
        will be marked as DELETED. Note: The image will not actually be
        deleted - this field is for informational purposes (see the description
        of --delete-in for more details). The date and time specified must be
        valid RFC 3339 full-date or date-time. For times in UTC, this looks
        like ``YYYY-MM-DDTHH:MM:SSZ''. For example: 2020-01-02T00:00:00Z for
        midnight on January 2, 2020 in UTC.
        This flag is mutually exclusive with *--delete-in*.
        """)

    delete_group.add_argument(
        '--delete-in',
        type=arg_parsers.Duration(),
        help="""\
       Specifies the amount of time until the image will be marked as DELETED.
       Note: The image will not actually be deleted - this field is only for
       informational purposes (see below). For instance, specifying ``30d'' will
       mark the image as DELETED in 30 days from the current system time.
       See $ gcloud topic datetimes for information on duration formats.

       Note that the image will not be deleted automatically. The image will
       only be marked as deleted. An explicit request to delete the image must
       be made in order to remove it from the image list.
       This flag is mutually exclusive with *--delete-on*.
       """)

    obsolete_group = parser.add_mutually_exclusive_group()

    obsolete_group.add_argument(
        '--obsolete-on',
        help="""\
       Specifies time (in the same format as *--delete-on*) when this image will
       be marked as OBSOLETE. State will not be changed - it has only
       informational purpose.
       This flag is mutually exclusive with *--obsolete-in*.
       """)

    obsolete_group.add_argument(
        '--obsolete-in',
        type=arg_parsers.Duration(),
        help="""\
       Specifies time (in the same format as *--delete-in*) until the image
       will be marked OBSOLETE. State will not be changed - it is only for
       informational purposes.
       This flag is mutually exclusive with *--obsolete-on*.
       """)

  def Run(self, args):
    """Invokes requests necessary for deprecating images."""
    # TODO(b/13695932): Note that currently there is a bug in the backend
    # whereby any request other than a completely empty request or a request
    # with state set to something other than ACTIVE will fail.
    # GCloud will be able to be made more permissive w.r.t. the checks
    # below when the API changes.

    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    if (any([args.delete_on, args.delete_in, args.obsolete_on, args.obsolete_in,
             args.replacement]) and args.state == 'ACTIVE'):
      raise calliope_exceptions.ToolException(
          'If the state is set to [ACTIVE] then none of [--delete-on], '
          '[--delete-in], [--obsolete-on], [--obsolete-in], or [--replacement] '
          'may be provided.')

    # Determine the date and time to deprecate for each flag set.
    current_time = datetime.datetime.now()
    delete_time = _ResolveTime(args.delete_on, args.delete_in, current_time)
    obsolete_time = _ResolveTime(
        args.obsolete_on, args.obsolete_in, current_time)

    # ACTIVE is not actually an option in the state enum.
    if args.state == 'ACTIVE':
      state = None
    else:
      state = client.messages.DeprecationStatus.StateValueValuesEnum(args.state)

    replacement_ref = flags.REPLACEMENT_DISK_IMAGE_ARG.ResolveAsResource(
        args, holder.resources)
    if replacement_ref:
      replacement_uri = replacement_ref.SelfLink()
    else:
      replacement_uri = None

    image_ref = DeprecateImages.DISK_IMAGE_ARG.ResolveAsResource(
        args, holder.resources)

    request = client.messages.ComputeImagesDeprecateRequest(
        deprecationStatus=client.messages.DeprecationStatus(
            state=state,
            deleted=delete_time,
            obsolete=obsolete_time,
            replacement=replacement_uri),
        image=image_ref.Name(),
        project=image_ref.project)

    return client.MakeRequests([(client.apitools_client.images,
                                 'Deprecate', request)])


DeprecateImages.detailed_help = {
    'EXAMPLES': """\
        To deprecate an image called 'IMAGE' immediately, mark it as
        obsolete in one day, and mark it as deleted in two days, use:

          $ {command} IMAGE --state DEPRECATED --obsolete-in 1d --delete-in 2d

        To un-deprecate an image called 'IMAGE', use:

          $ {command} IMAGE --state ACTIVE
        """,
}
