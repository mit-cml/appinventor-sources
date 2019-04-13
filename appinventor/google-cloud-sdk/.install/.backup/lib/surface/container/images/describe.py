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
"""Command to show Container Analysis Data for a specified image."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.container.images import container_data_util
from googlecloudsdk.api_lib.container.images import util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.container import flags

# Add to this as we add more container analysis data.
_DEFAULT_KINDS = [
    'BUILD_DETAILS',
    'PACKAGE_VULNERABILITY',
    'IMAGE_BASIS',
    'DEPLOYABLE',
    'DISCOVERY',
]


def _CommonArgs(parser):
  flags.AddTagOrDigestPositional(parser, verb='describe', repeated=False)


# pylint: disable=line-too-long
@base.ReleaseTracks(base.ReleaseTrack.GA)
class Describe(base.DescribeCommand):
  r"""Lists information about the specified image.

  ## EXAMPLES

  Describe the specified image:

    $ {command} gcr.io/myproject/myimage@digest
          OR
    $ {command} gcr.io/myproject/myimage:tag

  Find the digest for a tag:

    $ {command} gcr.io/myproject/myimage:tag \
      --format='value(image_summary.digest)'
          OR
    $ {command} gcr.io/myproject/myimage:tag \
      --format='value(image_summary.fully_qualified_digest)'

  """

  @staticmethod
  def Args(parser):
    _CommonArgs(parser)

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Raises:
      InvalidImageNameError: If the user specified an invalid image name.
    Returns:
      Some value that we want to have printed later.
    """

    with util.WrapExpectedDockerlessErrors(args.image_name):
      img_name = util.GetDigestFromName(args.image_name)
      return container_data_util.ContainerData(
          registry=img_name.registry,
          repository=img_name.repository,
          digest=img_name.digest)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class DescribeAlphaAndBeta(Describe):
  r"""Lists container analysis data for a given image.

  Lists container analysis data for a valid image.

  ## EXAMPLES

  Describe the specified image:

    $ {command} gcr.io/myproject/myimage@digest
          OR
    $ {command} gcr.io/myproject/myimage:tag

  Find the digest for a tag:

    $ {command} gcr.io/myproject/myimage:tag \
      --format='value(image_summary.digest)'
          OR
    $ {command} gcr.io/myproject/myimage:tag \
      --format='value(image_summary.fully_qualified_digest)'

  See package vulnerabilities found by the Container Analysis API for the
  specified image:

    $ {command} gcr.io/myproject/myimage@digest --show-package-vulnerability
  """

  @staticmethod
  def Args(parser):
    _CommonArgs(parser)

    # TODO(b/116048537): Refactor these flags to comply with gcloud style.
    parser.add_argument(
        '--metadata-filter',
        default='',
        help=('Additional filter to fetch metadata for '
              'a given fully qualified image reference.'))
    parser.add_argument(
        '--show-build-details',
        action='store_true',
        help='Include build metadata in the output.')
    parser.add_argument(
        '--show-package-vulnerability',
        action='store_true',
        help='Include vulnerability metadata in the output.')
    parser.add_argument(
        '--show-image-basis',
        action='store_true',
        help='Include base image metadata in the output.')
    parser.add_argument(
        '--show-deployment',
        action='store_true',
        help='Include deployment metadata in the output.')
    parser.add_argument(
        '--show-all-metadata',
        action='store_true',
        help='Include all metadata in the output.')

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Raises:
      InvalidImageNameError: If the user specified an invalid image name.
    Returns:
      Some value that we want to have printed later.
    """

    filter_kinds = []
    if args.show_build_details:
      filter_kinds.append('BUILD_DETAILS')
    if args.show_package_vulnerability:
      filter_kinds.append('PACKAGE_VULNERABILITY')
      filter_kinds.append('DISCOVERY')
    if args.show_image_basis:
      filter_kinds.append('IMAGE_BASIS')
    if args.show_deployment:
      filter_kinds.append('DEPLOYABLE')

    if args.show_all_metadata:
      filter_kinds = _DEFAULT_KINDS

    if filter_kinds or args.metadata_filter:
      if filter_kinds:
        filter_from_flags = ' OR '.join(
            ['kind = "{kind}"'.format(kind=fk) for fk in filter_kinds])

        if not args.metadata_filter:
          occ_filter = filter_from_flags
        else:
          occ_filter = '({occf}) AND ({flagf})'.format(
              occf=args.metadata_filter, flagf=filter_from_flags)
      else:
        occ_filter = args.metadata_filter

      with util.WrapExpectedDockerlessErrors(args.image_name):
        img_name = util.GetDigestFromName(args.image_name)
        data = util.TransformContainerAnalysisData(
            img_name,
            occ_filter,
            deployments=(args.show_deployment or args.show_all_metadata))
        # Clear out fields that weren't asked for and have no data.
        if (not data.build_details_summary.build_details and
            not args.show_build_details and not args.show_all_metadata):
          del data.build_details_summary
        if (not data.package_vulnerability_summary.vulnerabilities and
            not args.show_package_vulnerability and not args.show_all_metadata):
          del data.package_vulnerability_summary
        if (not data.discovery_summary.discovery and
            not args.show_package_vulnerability and not args.show_all_metadata):
          del data.discovery_summary
        if (not data.image_basis_summary.base_images and
            not args.show_image_basis and not args.show_all_metadata):
          del data.image_basis_summary
        if (not data.deployment_summary.deployments and
            not args.show_deployment and not args.show_all_metadata):
          del data.deployment_summary
        return data
    else:
      with util.WrapExpectedDockerlessErrors(args.image_name):
        img_name = util.GetDigestFromName(args.image_name)
        return container_data_util.ContainerData(
            registry=img_name.registry,
            repository=img_name.repository,
            digest=img_name.digest)
