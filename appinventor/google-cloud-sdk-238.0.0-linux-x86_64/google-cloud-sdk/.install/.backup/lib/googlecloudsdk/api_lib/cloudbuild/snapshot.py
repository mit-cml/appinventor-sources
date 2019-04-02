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
"""Move local source snapshots to GCP.

"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import os
import os.path
import tarfile

from googlecloudsdk.api_lib.cloudbuild import metric_names
from googlecloudsdk.api_lib.util import apis as core_apis
from googlecloudsdk.command_lib.util import gcloudignore
from googlecloudsdk.core import log
from googlecloudsdk.core import metrics
from googlecloudsdk.core.util import files

_IGNORED_FILE_MESSAGE = """\
Some files were not included in the source upload.

Check the gcloud log [{log_file}] to see which files and the contents of the
default gcloudignore file used (see `$ gcloud topic gcloudignore` to learn
more).
"""


class FileMetadata(object):
  """FileMetadata contains information about a file destined for GCP upload.

  Attributes:
      root: str, The root directory for considering file metadata.
      path: str, The path of this file, relative to the root.
      size: int, The size of this file, in bytes.
  """

  def __init__(self, root, path):
    """Collect file metadata.

    Args:
      root: str, The root directory for considering file metadata.
      path: str, The path of this file, relative to the root.
    """
    self.root = root
    self.path = path
    self.size = os.path.getsize(os.path.join(root, path))


class Snapshot(object):
  """Snapshot is a manifest of the source in a directory.

  Attributes:
    src_dir: str, The root of the snapshot source on the local disk.
    files: {str: FileMetadata}, A mapping from file path (relative to the
        snapshot root) to file metadata.
    dir: [str], The list of dirs (possibly empty) in the snapshot.
    uncompressed_size: int, The number of bytes needed to store all of the
        files in this snapshot, uncompressed.
  """

  def __init__(self, src_dir):
    self.src_dir = src_dir
    self.files = {}
    self.dirs = []
    self.uncompressed_size = 0
    self._client = core_apis.GetClientInstance('storage', 'v1')
    self._messages = core_apis.GetMessagesModule('storage', 'v1')
    file_chooser = gcloudignore.GetFileChooserForDir(self.src_dir,
                                                     write_on_disk=False)
    self.any_files_ignored = False
    for (dirpath, dirnames, filenames) in os.walk(self.src_dir):
      relpath = os.path.relpath(dirpath, self.src_dir)
      if (dirpath != self.src_dir and  # don't ever ignore the main source dir!
          not file_chooser.IsIncluded(relpath, is_dir=True)):
        self.any_files_ignored = True
        continue
      for fname in filenames:
        path = os.path.join(relpath, fname)
        if os.path.islink(path) and not os.path.exists(path):
          # The file is a broken symlink; ignore it.
          log.info(
              'Ignoring [{}] which is a symlink to non-existent path'.format(
                  path))
          continue
        # Join file paths with Linux path separators, avoiding ./ prefix.
        # GCB workers are Linux VMs so os.path.join produces incorrect output.
        fpath = '/'.join([relpath, fname]) if relpath != '.' else fname
        if not file_chooser.IsIncluded(fpath):
          self.any_files_ignored = True
          continue
        fm = FileMetadata(self.src_dir, fpath)
        self.files[fpath] = fm
        self.uncompressed_size += fm.size
      # NOTICE: Modifying dirnames is explicitly allowed by os.walk(). The
      # modified dirnames is used in the next loop iteration which is also
      # the next os.walk() iteration.
      for dname in dirnames[:]:  # Make a copy since we modify the original.
        # Join dir paths with Linux path separators, avoiding ./ prefix.
        # GCB workers are Linux VMs so os.path.join produces incorrect output.
        dpath = '/'.join([relpath, dname]) if relpath != '.' else dname
        if not file_chooser.IsIncluded(dpath, is_dir=True):
          dirnames.remove(dname)  # Don't recurse into dpath at all.
          continue
        self.dirs.append(dpath)

  def _MakeTarball(self, archive_path):
    """Constructs a tarball of snapshot contents.

    Args:
      archive_path: Path to place tar file.

    Returns:
      tarfile.TarFile, The constructed tar file.
    """
    tf = tarfile.open(archive_path, mode='w:gz')
    for dpath in self.dirs:
      t = tarfile.TarInfo(dpath)
      t.type = tarfile.DIRTYPE
      t.mode = os.stat(dpath).st_mode
      tf.addfile(t)
      log.debug('Added dir [%s]', dpath)
    for path in self.files:
      tf.add(path)
      log.debug('Added [%s]', path)
    return tf

  def CopyTarballToGCS(self, storage_client, gcs_object):
    """Copy a tarball of the snapshot to GCS.

    Args:
      storage_client: storage_api.StorageClient, The storage client to use for
                      uploading.
      gcs_object: storage.objects Resource, The GCS object to write.

    Returns:
      storage_v1_messages.Object, The written GCS object.
    """
    with metrics.RecordDuration(metric_names.UPLOAD_SOURCE):
      with files.ChDir(self.src_dir):
        with files.TemporaryDirectory() as tmp:
          archive_path = os.path.join(tmp, 'file.tgz')
          tf = self._MakeTarball(archive_path)
          tf.close()
          ignore_file_path = os.path.join(self.src_dir,
                                          gcloudignore.IGNORE_FILE_NAME)
          if self.any_files_ignored:
            if os.path.exists(ignore_file_path):
              log.info('Using gcloudignore file [{}]'.format(ignore_file_path))
            else:
              log.status.Print(_IGNORED_FILE_MESSAGE.format(
                  log_file=log.GetLogFilePath()))
          log.status.write(
              'Uploading tarball of [{src_dir}] to '
              '[gs://{bucket}/{object}]\n'.format(
                  src_dir=self.src_dir,
                  bucket=gcs_object.bucket,
                  object=gcs_object.object,
              ),
          )
          return storage_client.CopyFileToGCS(archive_path, gcs_object)
