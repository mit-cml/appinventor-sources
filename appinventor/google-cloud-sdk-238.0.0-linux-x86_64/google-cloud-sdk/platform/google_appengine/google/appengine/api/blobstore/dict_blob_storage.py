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




"""In-memory implementation of Blobstore stub storage.

This module contains an implementation of `blob_storage.BlobStorage`.
"""











import StringIO

from google.appengine.api import blobstore
from google.appengine.api.blobstore import blob_storage


class DictBlobStorage(blob_storage.BlobStorage):
  """Stores blobs in a dictionary."""

  def __init__(self):
    """Constructor."""
    self._blobs = {}

  def StoreBlob(self, blob_key, blob_stream):
    """Stores a blob stream."""
    content = StringIO.StringIO()
    try:
      while True:
        block = blob_stream.read(1 << 20)
        if not block:
          break
        content.write(block)
      self.CreateBlob(blob_key, content.getvalue())
    finally:
      content.close()

  def CreateBlob(self, blob_key, blob):
    """Stores a blob in a map."""
    self._blobs[blobstore.BlobKey(unicode(blob_key))] = blob

  def OpenBlob(self, blob_key):
    """Gets the blob contents as a stream."""
    return StringIO.StringIO(
        self._blobs[blobstore.BlobKey(unicode(blob_key))])

  def DeleteBlob(self, blob_key):
    """Deletes blob content."""
    try:
      del self._blobs[blobstore.BlobKey(unicode(blob_key))]
    except KeyError:
      pass
