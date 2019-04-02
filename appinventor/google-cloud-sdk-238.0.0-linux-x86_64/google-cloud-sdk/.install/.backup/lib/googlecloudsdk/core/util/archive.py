# -*- coding: utf-8 -*- #
# Copyright 2015 Google Inc. All Rights Reserved.
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

"""Set of utilities for dealing with archives."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import os
import zipfile

try:
  # pylint: disable=unused-import
  # pylint: disable=g-import-not-at-top
  import zlib
  _ZIP_COMPRESSION = zipfile.ZIP_DEFLATED
except ImportError:
  _ZIP_COMPRESSION = zipfile.ZIP_STORED


def MakeZipFromDir(dest_zip_file, src_dir, predicate=None):
  """Similar to shutil.make_archive (which is available in python >=2.7).

  Examples:
    Filesystem:
    /tmp/a/
    /tmp/b/B

    >>> MakeZipFromDir('my.zip', '/tmp')
    Creates zip with content:
    a/
    b/B

  Note this is caller responsibility to use appropriate platform-dependent
  path separator.

  Note filenames containing path separator are supported.

  Args:
    dest_zip_file: str, filesystem path to the zip file to be created. Note that
      directory should already exist for destination zip file.
    src_dir: str, filesystem path to the directory to zip up
    predicate: callable, takes one argument (file path). File will be included
               in the zip if and only if the predicate(file_path). Defaults to
               always true.
  """
  if predicate is None:
    predicate = lambda x: True
  zip_file = zipfile.ZipFile(dest_zip_file, 'w', _ZIP_COMPRESSION)
  try:
    for root, _, filelist in os.walk(src_dir):
      # In case this is empty directory.
      path = os.path.normpath(os.path.relpath(root, src_dir))
      if not predicate(path):
        continue
      if path and path != os.curdir:
        zip_file.write(root, path)
      for f in filelist:
        filename = os.path.normpath(os.path.join(root, f))
        relpath = os.path.relpath(filename, src_dir)
        if not predicate(relpath):
          continue
        if os.path.isfile(filename):
          zip_file.write(filename, relpath)
  finally:
    zip_file.close()
