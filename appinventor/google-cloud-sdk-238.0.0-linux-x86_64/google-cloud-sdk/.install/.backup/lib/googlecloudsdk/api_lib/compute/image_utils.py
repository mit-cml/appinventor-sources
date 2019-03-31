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
"""Common classes and functions for images."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import constants
from googlecloudsdk.api_lib.compute import request_helper
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.console import console_io

FAMILY_PREFIX = 'family/'
GUEST_OS_FEATURES = ['MULTI_IP_SUBNET',
                     'SECURE_BOOT',
                     'UEFI_COMPATIBLE',
                     'VIRTIO_SCSI_MULTIQUEUE',
                     'WINDOWS',
                    ]
GUEST_OS_FEATURES_BETA = ['MULTI_IP_SUBNET',
                          'SECURE_BOOT',
                          'UEFI_COMPATIBLE',
                          'VIRTIO_SCSI_MULTIQUEUE',
                          'WINDOWS',
                         ]
GUEST_OS_FEATURES_ALPHA = ['MULTI_IP_SUBNET',
                           'SECURE_BOOT',
                           'UEFI_COMPATIBLE',
                           'VIRTIO_SCSI_MULTIQUEUE',
                           'WINDOWS',
                           'GVNIC'
                          ]


class ImageExpander(object):
  """Class for expanding image aliases."""

  def __init__(self, compute_client, resources):
    """Instantiate ImageExpander and embed all required data into it.

    ImageExpander is a class depending on "base_classes"
    class layout (properties side-derived from one of base_class class). This
    function can be used to avoid unfeasible inheritance and use composition
    instead when refactoring away from base_classes into stateless style.

    This constructor embeds following properties into ImageExpander instance:
     - compute
     - messages
     - http
     - batch_url
     - resources

    Example:
      compute_holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
      client = compute_holder.client
      resources = compute_holder.resources

      image_expander = ImageExpander(client, resources)
        or
      image_expander = ImageExpander(self.compute_client, self.resources)
        to use in a class derived from some of base_classes

      image_expander.ExpandImageFlag(...)

    Args:
      compute_client: compute_holder.client
      resources: compute_holder.resources
    """
    self._compute = compute_client.apitools_client
    self._messages = compute_client.messages
    self._http = compute_client.apitools_client.http
    self._batch_url = compute_client.batch_url
    self._resources = resources

  def GetMatchingImages(self, user_project, image, alias, errors):
    """Yields images from a public image project and the user's project."""
    service = self._compute.images
    requests = [
        (service,
         'List',
         self._messages.ComputeImagesListRequest(
             filter='name eq ^{0}(-.+)*-v.+'.format(alias.name_prefix),
             maxResults=constants.MAX_RESULTS_PER_PAGE,
             project=alias.project)),
        (service,
         'List',
         self._messages.ComputeImagesListRequest(
             filter='name eq ^{0}$'.format(image),
             maxResults=constants.MAX_RESULTS_PER_PAGE,
             project=user_project)),
    ]

    return request_helper.MakeRequests(
        requests=requests,
        http=self._http,
        batch_url=self._batch_url,
        errors=errors)

  def GetImage(self, image_ref):
    """Returns the image resource corresponding to the given reference."""
    errors = []
    requests = []
    name = image_ref.Name()
    if name.startswith(FAMILY_PREFIX):
      requests.append((self._compute.images,
                       'GetFromFamily',
                       self._messages.ComputeImagesGetFromFamilyRequest(
                           family=name[len(FAMILY_PREFIX):],
                           project=image_ref.project)))
    else:
      requests.append((self._compute.images,
                       'Get',
                       self._messages.ComputeImagesGetRequest(
                           image=name,
                           project=image_ref.project)))

    res = list(request_helper.MakeRequests(
        requests=requests,
        http=self._http,
        batch_url=self._batch_url,
        errors=errors))
    if errors:
      utils.RaiseException(
          errors,
          utils.ImageNotFoundError,
          error_message='Could not fetch image resource:')
    return res[0]

  def ExpandImageFlag(self,
                      user_project,
                      image=None,
                      image_family=None,
                      image_project=None,
                      return_image_resource=False):
    """Resolves the image or image-family value.

    If the value of image is one of the aliases defined in the
    constants module, both the user's project and the public image
    project for the alias are queried. Otherwise, only the user's
    project is queried. If image is an alias and image-project is
    provided, only the given project is queried.

    Args:
      user_project: The user's project.
      image: The name of the image.
      image_family: The family of the image. Is ignored if image name is
        specified.
      image_project: The project of the image.
      return_image_resource: If True, always makes an API call to also
        fetch the image resource.

    Returns:
      A tuple where the first element is the self link of the image. If
        return_image_resource is False, the second element is None, otherwise
        it is the image resource.
    """

    # If an image project was specified, then assume that image refers
    # to an image in that project.
    if image_project:
      image_project_ref = self._resources.Parse(
          image_project, collection='compute.projects')
      image_project = image_project_ref.Name()

    image_ref = None

    if image:
      image_ref = self._resources.Parse(
          image,
          params={
              'project': image_project
                         or properties.VALUES.core.project.GetOrFail,
          },
          collection='compute.images')
    else:
      if image_family is not None:
        image_ref = self._resources.Parse(
            image_family,
            params={
                'project': image_project
                           or properties.VALUES.core.project.GetOrFail
            },
            collection='compute.images')
      else:
        image_ref = self._resources.Parse(
            constants.DEFAULT_IMAGE_FAMILY,
            collection='compute.images',
            params={'project': 'debian-cloud'})
      if not image_ref.image.startswith(FAMILY_PREFIX):
        relative_name = image_ref.RelativeName()
        relative_name = (relative_name[:-len(image_ref.image)] +
                         FAMILY_PREFIX + image_ref.image)
        image_ref = self._resources.ParseRelativeName(
            relative_name, image_ref.Collection())

    if image_project:
      return (image_ref.SelfLink(),
              self.GetImage(image_ref) if return_image_resource else None)

    alias = constants.IMAGE_ALIASES.get(image_ref.Name())

    # Check for hidden aliases.
    if not alias:
      alias = constants.HIDDEN_IMAGE_ALIASES.get(image_ref.Name())

    # If the image name given is not an alias and no image project was
    # provided, then assume that the image value refers to an image in
    # the user's project.
    if not alias:
      return (image_ref.SelfLink(),
              self.GetImage(image_ref) if return_image_resource else None)

    # At this point, the image is an alias and now we have to find the
    # latest one among the public image project and the user's
    # project.

    WarnAlias(alias)

    errors = []
    images = self.GetMatchingImages(user_project, image_ref.Name(), alias,
                                    errors)

    user_image = None
    public_images = []

    for image in images:
      if image.deprecated:
        continue
      image_ref2 = self._resources.Parse(
          image.selfLink, collection='compute.images', enforce_collection=True)
      if image_ref2.project == user_project:
        user_image = image
      else:
        public_images.append(image)

    if errors or not public_images:
      # This should happen only if there is something wrong with the
      # image project (e.g., operator error) or the global control
      # plane is down.
      utils.RaiseToolException(
          errors,
          'Failed to find image for alias [{0}] in public image project [{1}].'
          .format(image_ref.Name(), alias.project))

    def GetVersion(image):
      """Extracts the "20140718" from an image name like "debian-v20140718"."""
      parts = image.name.rsplit('v', 1)
      if len(parts) != 2:
        log.debug('Skipping image with malformed name [%s].', image.name)
        return ''
      return parts[1]

    public_candidate = max(public_images, key=GetVersion)
    if user_image:
      options = [user_image, public_candidate]

      idx = console_io.PromptChoice(
          options=[image.selfLink for image in options],
          default=0,
          message=('Found two possible choices for [--image] value [{0}].'
                   .format(image_ref.Name())))

      res = options[idx]

    else:
      res = public_candidate

    log.debug('Image resolved to [%s].', res.selfLink)
    return (res.selfLink, res if return_image_resource else None)


def HasWindowsLicense(resource, resource_parser):
  """Returns True if the given image or disk has a Windows license."""
  for license_uri in resource.licenses:
    license_ref = resource_parser.Parse(
        license_uri, collection='compute.licenses')
    if license_ref.project in constants.WINDOWS_IMAGE_PROJECTS:
      return True
  return False


def AddImageProjectFlag(parser):
  """Adds the --image flag to the given parser."""
  parser.add_argument(
      '--image-project',
      help="""\
      The project against which all image and image family references will be
      resolved. It is best practice to define image-project.
          * If specifying one of our public images, image-project must be
            provided.
          * If there are several of the same image-family value in multiple
            projects, image-project must be specified to clarify the image to
            be used.
          * If not specified and either image or image-family is provided, the
            current default project is used.
        """)


def WarnAlias(alias):
  """WarnAlias outputs a warning telling users to not use the given alias."""
  msg = ('Image aliases are deprecated and will be removed in a future '
         'version. ')
  if alias.family is not None:
    msg += ('Please use --image-family={family} and --image-project={project} '
            'instead.').format(family=alias.family, project=alias.project)
  else:
    msg += 'Please use --image-family and --image-project instead.'

  log.warning(msg)


def AddGuestOsFeaturesArg(parser, release_track):
  """Add the guest-os-features arg."""
  # Alpha and Beta Args
  guest_os_features = []
  if release_track == base.ReleaseTrack.GA:
    guest_os_features = GUEST_OS_FEATURES
  elif release_track == base.ReleaseTrack.BETA:
    guest_os_features = GUEST_OS_FEATURES_BETA
  elif release_track == base.ReleaseTrack.ALPHA:
    guest_os_features = GUEST_OS_FEATURES_ALPHA

  if not guest_os_features:
    return
  parser.add_argument(
      '--guest-os-features',
      metavar='GUEST_OS_FEATURE',
      type=arg_parsers.ArgList(
          element_type=lambda x: x.upper(), choices=guest_os_features),
      help="""\
      This parameter enables one or more features for VM instances that use the
      image for their boot disks. The following features are available:

       * MULTI_IP_SUBNET - For configuring interfaces with a netmask other than
         /32.

       * SECURE_BOOT - Enables UEFI secure boot, which restrics unsigned
         software from booting or unsigned drivers from loading on the VM
         instance.

       * UEFI_COMPATIBLE - Enables UEFI booting, which is an alternative system
         boot method. Most public images use the GRUB bootloader as their
         primary boot method.

       * VIRTIO_SCSI_MULTIQUEUE - Enables multiqueue SCSI capabilities for
         Local SSD devices. This option is an alternative to NVMe.
          * For Linux images, you can enable VIRTIO_SCSI_MULTIQUEUE on images
            with kernel versions 3.17 and higher.
          * For Windows images, you can enable VIRTIO_SCSI_MULTIQUEUE on images
            with driver version 1.2.0.1621 or higher.

       * WINDOWS - Required for Windows Server images. Newer public images for
         Windows server include the WINDOWS parameter to indicate that it is a
         Windows image.
        """)
