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
"""Import image command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import abc
import os.path
import string
import uuid

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import daisy_utils
from googlecloudsdk.api_lib.compute import image_utils
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.api_lib.storage import storage_api
from googlecloudsdk.api_lib.storage import storage_util
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute.images import flags
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources
from googlecloudsdk.core.console import progress_tracker

import six

_OS_CHOICES_MAP = {
    'debian-8': 'debian/translate_debian_8.wf.json',
    'debian-9': 'debian/translate_debian_9.wf.json',
    'centos-6': 'enterprise_linux/translate_centos_6.wf.json',
    'centos-7': 'enterprise_linux/translate_centos_7.wf.json',
    'rhel-6': 'enterprise_linux/translate_rhel_6_licensed.wf.json',
    'rhel-7': 'enterprise_linux/translate_rhel_7_licensed.wf.json',
    'rhel-6-byol': 'enterprise_linux/translate_rhel_6_byol.wf.json',
    'rhel-7-byol': 'enterprise_linux/translate_rhel_7_byol.wf.json',
    'ubuntu-1404': 'ubuntu/translate_ubuntu_1404.wf.json',
    'ubuntu-1604': 'ubuntu/translate_ubuntu_1604.wf.json',
    'windows-2008r2': 'windows/translate_windows_2008_r2.wf.json',
    'windows-2012': 'windows/translate_windows_2012.wf.json',
    'windows-2012r2': 'windows/translate_windows_2012_r2.wf.json',
    'windows-2016': 'windows/translate_windows_2016.wf.json',
    'windows-2008r2-byol': 'windows/translate_windows_2008_r2_byol.wf.json',
    'windows-2012-byol': 'windows/translate_windows_2012_byol.wf.json',
    'windows-2012r2-byol': 'windows/translate_windows_2012_r2_byol.wf.json',
    'windows-2016-byol': 'windows/translate_windows_2016_byol.wf.json',
    'windows-7-byol': 'windows/translate_windows_7_byol.wf.json',
    'windows-10-byol': 'windows/translate_windows_10_byol.wf.json',
}
_OS_CHOICES_GA = [
    'debian-8',
    'debian-9',
    'centos-6',
    'centos-7',
    'rhel-6',
    'rhel-7',
    'rhel-6-byol',
    'rhel-7-byol',
    'ubuntu-1404',
    'ubuntu-1604',
    'windows-2008r2',
    'windows-2012',
    'windows-2012r2',
    'windows-2016',
]
_OS_CHOICES_BETA = _OS_CHOICES_GA + [
    'windows-2008r2-byol',
    'windows-2012-byol',
    'windows-2012r2-byol',
    'windows-2016-byol',
    'windows-7-byol',
    'windows-10-byol',
]
_OS_CHOICES_ALPHA = _OS_CHOICES_BETA + [
]

_WORKFLOW_DIR = '../workflows/image_import/'
_IMPORT_WORKFLOW = _WORKFLOW_DIR + 'import_image.wf.json'
_IMPORT_FROM_IMAGE_WORKFLOW = _WORKFLOW_DIR + 'import_from_image.wf.json'
_IMPORT_AND_TRANSLATE_WORKFLOW = _WORKFLOW_DIR + 'import_and_translate.wf.json'
_WORKFLOWS_URL = ('https://github.com/GoogleCloudPlatform/compute-image-tools/'
                  'tree/master/daisy_workflows/image_import')
_OUTPUT_FILTER = ['[Daisy', '[import-', 'starting build', '  import', 'ERROR']


def _IsLocalFile(file_name):
  return not (file_name.startswith('gs://') or
              file_name.startswith('https://'))


def _UploadToGcs(is_async, local_path, daisy_bucket, image_uuid,
                 storage_client):
  """Uploads a local file to GCS. Returns the gs:// URI to that file."""
  file_name = os.path.basename(local_path).replace(' ', '-')
  dest_path = 'gs://{0}/tmpimage/{1}-{2}'.format(
      daisy_bucket, image_uuid, file_name)
  if is_async:
    log.status.Print('Async: Once upload is complete, your image will be '
                     'imported from Cloud Storage asynchronously.')
  with progress_tracker.ProgressTracker(
      'Copying [{0}] to [{1}]'.format(local_path, dest_path)):
    # TODO(b/109938541): Remove gsutil implementation after the new
    # implementation seems stable.
    use_gsutil = properties.VALUES.storage.use_gsutil.GetBool()
    if use_gsutil:
      return _UploadToGcsGsutil(local_path, dest_path)
    else:
      return _UploadToGcsStorageApi(local_path, dest_path, storage_client)


def _UploadToGcsGsutil(local_path, dest_path):
  """Uploads a local file to GCS using gsutil."""
  retcode = storage_util.RunGsutilCommand('cp', [local_path, dest_path])
  if retcode != 0:
    log.err.Print('Failed to upload file. See {} for details.'.format(
        log.GetLogFilePath()))
    raise exceptions.FailedSubCommand(
        ['gsutil', 'cp', local_path, dest_path], retcode)
  return dest_path


def _UploadToGcsStorageApi(local_path, dest_path, storage_client):
  """Uploads a local file to GCS using the gcloud storage api client."""
  dest_object = storage_util.ObjectReference.FromUrl(dest_path)
  storage_client.CopyFileToGCS(local_path, dest_object)
  return dest_path


def _CopyToScratchBucket(source_uri, image_uuid, storage_client, daisy_bucket):
  """Copy image from source_uri to daisy scratch bucket."""
  image_file = os.path.basename(source_uri)
  dest_uri = 'gs://{0}/tmpimage/{1}-{2}'.format(
      daisy_bucket, image_uuid, image_file)
  src_object = resources.REGISTRY.Parse(source_uri,
                                        collection='storage.objects')
  dest_object = resources.REGISTRY.Parse(dest_uri,
                                         collection='storage.objects')
  with progress_tracker.ProgressTracker(
      'Copying [{0}] to [{1}]'.format(source_uri, dest_uri)):
    storage_client.Rewrite(src_object, dest_object)
  return dest_uri


def _GetTranslateWorkflow(args):
  if args.os:
    return _OS_CHOICES_MAP[args.os]
  return args.custom_workflow


def _MakeGcsUri(uri):
  obj_ref = resources.REGISTRY.Parse(uri)
  return 'gs://{0}/{1}'.format(obj_ref.bucket, obj_ref.object)


def _CheckImageName(image_name):
  """Checks for a valid GCE image name."""
  name_message = ('Name must start with a lowercase letter followed by up to '
                  '63 lowercase letters, numbers, or hyphens, and cannot end '
                  'with a hyphen.')
  name_ok = True
  valid_chars = string.digits + string.ascii_lowercase + '-'
  if len(image_name) > 64:
    name_ok = False
  elif image_name[0] not in string.ascii_lowercase:
    name_ok = False
  elif not all(char in valid_chars for char in image_name):
    name_ok = False
  elif image_name[-1] == '-':
    name_ok = False

  if not name_ok:
    raise exceptions.InvalidArgumentException('IMAGE_NAME', name_message)


def _CheckForExistingImage(image_name, compute_holder):
  """Check that the destination image does not already exist."""
  _CheckImageName(image_name)
  image_ref = resources.REGISTRY.Parse(
      image_name,
      collection='compute.images',
      params={'project': properties.VALUES.core.project.GetOrFail})

  image_expander = image_utils.ImageExpander(compute_holder.client,
                                             compute_holder.resources)
  try:
    _ = image_expander.GetImage(image_ref)
    image_exists = True
  except utils.ImageNotFoundError:
    image_exists = False

  if image_exists:
    message = 'The image [{0}] already exists.'.format(image_name)
    raise exceptions.InvalidArgumentException('IMAGE_NAME', message)


def _CreateImportStager(storage_client, args):
  if args.source_image:
    return ImportFromImageStager(storage_client, args)
  elif _IsLocalFile(args.source_file):
    return ImportFromLocalFileStager(storage_client, args)
  else:
    return ImportFromGSFileStager(storage_client, args)


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Import(base.CreateCommand):
  """Import an image into Google Compute Engine."""

  _OS_CHOICES = _OS_CHOICES_GA

  @classmethod
  def Args(cls, parser):
    Import.DISK_IMAGE_ARG = flags.MakeDiskImageArg()
    Import.DISK_IMAGE_ARG.AddArgument(parser, operation_type='create')

    flags.compute_flags.AddZoneFlag(
        parser, 'image', 'import',
        explanation='The zone in which to do the work of importing the image.')

    source = parser.add_mutually_exclusive_group(required=True)
    source.add_argument(
        '--source-file',
        help=("""A local file, or the Google Cloud Storage URI of the virtual
              disk file to import. For example: ``gs://my-bucket/my-image.vmdk''
              or ``./my-local-image.vmdk''"""),
    )
    flags.SOURCE_IMAGE_ARG.AddArgument(source, operation_type='import')

    workflow = parser.add_mutually_exclusive_group(required=True)
    workflow.add_argument(
        '--os',
        choices=sorted(cls._OS_CHOICES),
        help='Specifies the OS of the image being imported.'
    )
    workflow.add_argument(
        '--data-disk',
        help=('Specifies that the disk has no bootable OS installed on it. '
              'Imports the disk without making it bootable or installing '
              'Google tools on it.'),
        action='store_true'
    )
    workflow.add_argument(
        '--custom-workflow',
        help=("""\
              Specifies a custom workflow to use for image translation.
              Workflow should be relative to the image_import directory here:
              []({0}). For example: ``{1}''""".format(
                  _WORKFLOWS_URL, _OS_CHOICES_MAP[sorted(cls._OS_CHOICES)[0]])),
        hidden=True
    )

    daisy_utils.AddCommonDaisyArgs(parser)
    parser.display_info.AddCacheUpdater(flags.ImagesCompleter)

  def Run(self, args):
    compute_holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    # Fail early if the requested image name is invalid or already exists.
    _CheckImageName(args.image_name)
    _CheckForExistingImage(args.image_name, compute_holder)

    storage_client = storage_api.StorageClient()
    daisy_bucket = daisy_utils.GetAndCreateDaisyBucket(
        storage_client=storage_client)
    image_uuid = uuid.uuid4()

    daisy_vars = ['image_name={}'.format(args.image_name)]
    if args.source_image:
      # If we're starting from an image, then we've already imported it.
      workflow = _IMPORT_FROM_IMAGE_WORKFLOW
      daisy_vars.append(
          'translate_workflow={}'.format(_GetTranslateWorkflow(args)))
      ref = resources.REGISTRY.Parse(
          args.source_image,
          collection='compute.images',
          params={'project': properties.VALUES.core.project.GetOrFail})
      # source_name should be of the form 'global/images/image-name'.
      source_name = ref.RelativeName()[len(ref.Parent().RelativeName() + '/'):]
      daisy_vars.append('source_image={}'.format(source_name))
    else:
      # If the file is an OVA file, print a warning.
      if args.source_file.endswith('.ova'):
        log.warning(
            'The specified input file may contain more than one virtual disk. '
            'Only the first vmdk disk will be imported.')
      elif (args.source_file.endswith('.tar.gz')
            or args.source_file.endswith('.tgz')):
        raise exceptions.BadFileException(
            '`gcloud compute images import` does not support compressed '
            'archives. Please extract your image and try again.\n If you got '
            'this file by exporting an image from Compute Engine (e.g. by '
            'using `gcloud compute images export`) then you can instead use '
            '`gcloud compute images create` to create your image from your '
            '.tar.gz file.')

      # Get the image into the scratch bucket, wherever it is now.
      if _IsLocalFile(args.source_file):
        gcs_uri = _UploadToGcs(args.async, args.source_file,
                               daisy_bucket, image_uuid, storage_client)
      else:
        source_file = _MakeGcsUri(args.source_file)
        gcs_uri = _CopyToScratchBucket(source_file, image_uuid,
                                       storage_client, daisy_bucket)

      # Import and (maybe) translate from the scratch bucket.
      daisy_vars.append('source_disk_file={}'.format(gcs_uri))
      if args.data_disk:
        workflow = _IMPORT_WORKFLOW
      else:
        workflow = _IMPORT_AND_TRANSLATE_WORKFLOW
        daisy_vars.append(
            'translate_workflow={}'.format(_GetTranslateWorkflow(args)))

    self._ProcessAdditionalArgs(args, daisy_vars)

    # TODO(b/79591894): Once we've cleaned up the Argo output, replace this
    # warning message with a ProgressTracker spinner.
    log.warning('Importing image. This may take up to 2 hours.')
    return daisy_utils.RunDaisyBuild(args, workflow, ','.join(daisy_vars),
                                     daisy_bucket=daisy_bucket,
                                     user_zone=args.zone,
                                     output_filter=_OUTPUT_FILTER)

  def _ProcessAdditionalArgs(self, args, daisy_vars):
    """Hook for subclasses to implement additional argument processing."""
    pass


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class ImportBeta(Import):
  """Import an image into Google Compute Engine for Alpha and Beta releases."""

  _OS_CHOICES = _OS_CHOICES_BETA

  @classmethod
  def Args(cls, parser):
    super(ImportBeta, cls).Args(parser)
    parser.add_argument(
        '--no-guest-environment',
        action='store_true',
        help='Google Guest Environment will not be installed on the image.')

    parser.add_argument(
        '--network',
        help=('Name of the network in your project to use for the image import.'
              ' The network must have access to Google Cloud Storage. If not '
              'specified, the network named `default` is used.'),
    )

    parser.add_argument(
        '--subnet',
        help=('Name of the subnetwork in your project to use for the image '
              'import. If the network resource is in legacy mode, do not '
              'provide this property. If the network is in auto subnet mode, '
              'providing the subnetwork is optional. If the network is in '
              'custom subnet mode, then this field should be specified. '
              'Region or zone should be specified if this field is specified.'),
    )

  def Run(self, args):
    compute_holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    # Fail early if the requested image name is invalid or already exists.
    _CheckImageName(args.image_name)
    _CheckForExistingImage(args.image_name, compute_holder)

    storage_client = storage_api.StorageClient()
    import_stager = _CreateImportStager(storage_client, args)
    daisy_vars, workflow = import_stager.Stage()

    self._ProcessAdditionalArgs(args, daisy_vars)

    # TODO(b/79591894): Once we've cleaned up the Argo output, replace this
    # warning message with a ProgressTracker spinner.
    log.warning('Importing image. This may take up to 2 hours.')
    tags = ['gce-daisy-image-import']
    return daisy_utils.RunDaisyBuild(
        args, workflow, ','.join(daisy_vars), tags=tags,
        daisy_bucket=import_stager.GetDaisyBucket(),
        user_zone=properties.VALUES.compute.zone.Get(),
        output_filter=_OUTPUT_FILTER,
        service_account_roles=self._GetServiceAccountRoles())

  def _GetServiceAccountRoles(self):
    return None

  def _ProcessAdditionalArgs(self, args, daisy_vars):
    if args.no_guest_environment:
      daisy_vars.append('install_gce_packages={}'.format('false'))
    daisy_vars.extend(daisy_utils.ExtractNetworkAndSubnetDaisyVariables(
        args, daisy_utils.ImageOperation.IMPORT))


@six.add_metaclass(abc.ABCMeta)
class BaseImportStager(object):
  """Base class for image import stager.

  An abstract class which is responsible for preparing import parameters, such
  as Daisy parameters and workflow, as well as creating Daisy scratch bucket in
  the appropriate location.
  """

  def __init__(self, storage_client, args):
    self.storage_client = storage_client
    self.args = args

    self._CreateDaisyBucket()

  def _CreateDaisyBucket(self):
    # Create Daisy bucket in default GS location (US Multi-regional)
    # This is default behaviour for all types of import except from a file in GS
    self.daisy_bucket = daisy_utils.GetAndCreateDaisyBucket(
        storage_client=self.storage_client)

  def GetDaisyBucket(self):
    """Returns the name of Daisy scratch bucket.

    Returns:
      A string. Name of the Daisy scratch bucket used for running import.
    """
    return self.daisy_bucket

  def Stage(self):
    """Prepares import for execution and returns daisy variables/workflow.

    Returns:
      Tuple (daisy_vars, workflow).
      daisy_vars - array of strings, Daisy variables.
      workflow - str, Daisy workflow.
    """
    daisy_vars = []
    self._BuildDaisyVars(daisy_vars)
    return daisy_vars, self._GetDaisyWorkflow()

  def _BuildDaisyVars(self, daisy_workflow):
    daisy_workflow.append('image_name={}'.format(self.args.image_name))

  @abc.abstractmethod
  def _GetDaisyWorkflow(self):
    raise NotImplementedError


class ImportFromImageStager(BaseImportStager):
  """Image import stager from an existing image."""

  def _BuildDaisyVars(self, daisy_vars):
    super(ImportFromImageStager, self)._BuildDaisyVars(daisy_vars)
    daisy_vars.append(
        'translate_workflow={}'.format(_GetTranslateWorkflow(self.args)))

    ref = resources.REGISTRY.Parse(
        self.args.source_image, collection='compute.images',
        params={'project': properties.VALUES.core.project.GetOrFail})
    # source_name should be of the form 'global/images/image-name'.
    source_name = ref.RelativeName()[len(ref.Parent().RelativeName() + '/'):]
    daisy_vars.append('source_image={}'.format(source_name))

  def _GetDaisyWorkflow(self):
    return _IMPORT_FROM_IMAGE_WORKFLOW


class BaseImportFromFileStager(BaseImportStager):
  """Abstract image import stager for import from a file."""

  def _BuildDaisyVars(self, daisy_vars):
    super(BaseImportFromFileStager, self)._BuildDaisyVars(daisy_vars)
    # Import and (maybe) translate from the scratch bucket.
    daisy_vars.append('source_disk_file={}'.format(self.gcs_uri))
    if not self.args.data_disk:
      daisy_vars.append(
          'translate_workflow={}'.format(_GetTranslateWorkflow(self.args)))

  def _GetDaisyWorkflow(self):
    if self.args.data_disk:
      return _IMPORT_WORKFLOW
    else:
      return _IMPORT_AND_TRANSLATE_WORKFLOW

  def Stage(self):
    # If the file is an OVA file, print a warning.
    if self.args.source_file.endswith('.ova'):
      log.warning(
          'The specified input file may contain more than one virtual disk. '
          'Only the first vmdk disk will be imported.')
    elif (self.args.source_file.endswith('.tar.gz')
          or self.args.source_file.endswith('.tgz')):
      raise exceptions.BadFileException(
          '`gcloud compute images import` does not support compressed '
          'archives. Please extract your image and try again.\n If you got '
          'this file by exporting an image from Compute Engine (e.g. by '
          'using `gcloud compute images export`) then you can instead use '
          '`gcloud compute images create` to create your image from your '
          '.tar.gz file.')

    self.gcs_uri = self._CopySourceFileToScratchBucket()
    return super(BaseImportFromFileStager, self).Stage()

  @abc.abstractmethod
  def _CopySourceFileToScratchBucket(self):
    raise NotImplementedError


class ImportFromLocalFileStager(BaseImportFromFileStager):
  """Image import stager from a local file."""

  def _CopySourceFileToScratchBucket(self):
    return _UploadToGcs(
        self.args.async, self.args.source_file, self.daisy_bucket, uuid.uuid4(),
        self.storage_client)


class ImportFromGSFileStager(BaseImportFromFileStager):
  """Image import stager from a file in GCS."""

  def __init__(self, storage_client, args):
    self.source_file_gcs_uri = _MakeGcsUri(args.source_file)
    super(ImportFromGSFileStager, self).__init__(storage_client, args)

  def _CreateDaisyBucket(self):
    # Create a Daisy bucket in the same region as the source file in GS.
    self.daisy_bucket = daisy_utils.GetAndCreateDaisyBucket(
        storage_client=self.storage_client,
        bucket_location=self.storage_client.GetBucketLocationForFile(
            self.source_file_gcs_uri))

  def _CopySourceFileToScratchBucket(self):
    return _CopyToScratchBucket(
        self.source_file_gcs_uri, uuid.uuid4(), self.storage_client,
        self.daisy_bucket)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class ImportAlpha(ImportBeta):

  _OS_CHOICES = _OS_CHOICES_ALPHA

  def _GetServiceAccountRoles(self):
    return ['roles/iam.serviceAccountUser',
            'roles/iam.serviceAccountTokenCreator']


Import.detailed_help = {
    'brief': 'Import an image into Google Compute Engine',
    'DESCRIPTION': """\
        *{command}* imports Virtual Disk images, such as VMWare VMDK files
        and VHD files, into Google Compute Engine.

        Importing images involves 3 steps:
        *  Upload the virtual disk file to Google Cloud Storage.
        *  Import the image to Google Compute Engine.
        *  Translate the image to make a bootable image.
        This command will perform all three of these steps as necessary,
        depending on the input arguments specified by the user.

        This command uses the `--os` flag to choose the appropriate translation.
        You can omit the translation step using the `--data-disk` flag.

        If you exported your disk from Google Compute Engine then you do not
        need to re-import it. Instead, use the `create` command to create
        further images from it.

        Files stored on Cloud Storage and images in Compute Engine incur
        charges. See [](https://cloud.google.com/compute/docs/images/importing-virtual-disks#resource_cleanup).
        """,
}
