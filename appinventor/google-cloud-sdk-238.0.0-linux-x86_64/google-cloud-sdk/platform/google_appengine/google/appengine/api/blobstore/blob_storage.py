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
"""Definition of BlobStorage base class."""


class BlobStorage(object):
  """Base class for defining how blobs are stored.

  This base class merely defines an interface that all stub blob-storage
  mechanisms must implement.
  """

  def StoreBlob(self, blob_key, blob_stream):
    """Store blob stream.

    Implement this method to persist blob data.

    Args:
      blob_key: Blob key of blob to store.
      blob_stream: Stream or stream-like object that will generate blob content.
    """
    raise NotImplementedError('Storage class must override StoreBlob method.')

  def OpenBlob(self, blob_key):
    """Open blob for streaming.

    Args:
      blob_key: Blob-key of existing blob to open for reading.

    Returns:
      Open file stream for reading blob. Caller is responsible for closing file.
    """
    raise NotImplementedError('Storage class must override OpenBlob method.')

  def DeleteBlob(self, blob_key):
    """Delete blob data from storage.

    Args:
      blob_key: Blob-key of existing blob to delete.
    """
    raise NotImplementedError('Storage class must override DeleteBlob method.')
