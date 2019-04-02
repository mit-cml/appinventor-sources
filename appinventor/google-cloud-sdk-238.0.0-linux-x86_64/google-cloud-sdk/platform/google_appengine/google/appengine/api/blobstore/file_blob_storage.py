#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#




"""In-memory implementation of Blobstore stub storage based on file system.

This module contains an implementation of `blob_storage.BlobStorage` that
writes blobs directly to a file system.
"""











import errno
import os

from google.appengine.api import blobstore
from google.appengine.api.blobstore import blob_storage


__all__ = ['FileBlobStorage']



import __builtin__
_local_open = __builtin__.open


class FileBlobStorage(blob_storage.BlobStorage):
  """The storage mechanism that stores blob data on a local disk."""

  def __init__(self, storage_directory, app_id):
    """Constructor.

    Args:
      storage_directory: Directory within which to store blobs.
      app_id: The application ID on whose behalf to store blobs.
    """
    self._storage_directory = storage_directory
    self._app_id = app_id

  @classmethod
  def _BlobKey(cls, blob_key):
    """Normalizes to an instance of `BlobKey`."""
    if not isinstance(blob_key, blobstore.BlobKey):
      return blobstore.BlobKey(unicode(blob_key))
    return blob_key

  def _DirectoryForBlob(self, blob_key):
    """Determines the directory in which a blob is stored.

    Each blob is written to a directory underneath the storage objects
    storage directory based on the kind of blobs, application ID, and first
    character of its name. For example, the following blob keys...::

        _ACFDEDG
        _MNOPQRS
        _RSTUVWX


    ...are stored in the following locations::

        <storage-dir>/blob/myapp/A
        <storage-dir>/blob/myapp/M
        <storage-dir>/R


    Args:
      blob_key: Blob key for which you need to determine the directory.

    Returns:
      A directory where the blob is stored or should be stored, relative to this
      object's storage directory.
    """
    blob_key = self._BlobKey(blob_key)
    return os.path.join(self._storage_directory,
                        self._app_id,
                        str(blob_key)[1])

  def _FileForBlob(self, blob_key):
    """Calculates the full file name in which to store blob contents.

    This method does not check to see if the file already exists.

    Args:
      blob_key: The blob key of the blob for which to calculate the file name.

    Returns:
      A string that contains the complete path for the file that is used to
      store the blob.
    """
    blob_key = self._BlobKey(blob_key)
    return os.path.join(self._DirectoryForBlob(blob_key), str(blob_key)[1:])

  def StoreBlob(self, blob_key, blob_stream):
    """Stores a blob stream .

    Args:
      blob_key: The blob key of the blob that you want to store.
      blob_stream: A stream or a stream-like object that will generate blob
          content.
    """
    blob_key = self._BlobKey(blob_key)
    blob_directory = self._DirectoryForBlob(blob_key)
    if not os.path.exists(blob_directory):
      os.makedirs(blob_directory)
    blob_file = self._FileForBlob(blob_key)
    output = _local_open(blob_file, 'wb')

    try:


      while True:
        block = blob_stream.read(1 << 20)
        if not block:
          break
        output.write(block)
    finally:
      output.close()

  def OpenBlob(self, blob_key):
    """Opens a blob file for streaming.

    Args:
      blob_key: The blob key of an existing blob that you want to open.

    Returns:
      An open file stream to read the blob.
    """
    return _local_open(self._FileForBlob(blob_key), 'rb')

  def DeleteBlob(self, blob_key):
    """Deletes blob data.

    Deleting an unknown blob will not raise an error.

    Args:
      blob_key: The blob key of an existing blob that you want to delete.
    """
    try:
      os.remove(self._FileForBlob(blob_key))
    except OSError, e:
      if e.errno != errno.ENOENT:
        raise e
