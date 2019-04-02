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
"""Helpers for digesting a file."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import hashlib
from googlecloudsdk.api_lib.cloudkms import base as cloudkms_base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.core.util import files

_READ_SIZE = 4 * 1024 * 1024


def _MultiStepsDigest(digest, file_to_digest):
  # TODO(b/77481291) Refactor this to allow reading from stdin.
  with files.BinaryFileReader(file_to_digest) as f:
    while True:
      chunk = f.read(_READ_SIZE)
      if not chunk:
        break
      digest.update(chunk)
  return digest


_DIGEST_ALGORITHMS = {
    'sha256': hashlib.sha256,
    'sha384': hashlib.sha384,
    'sha512': hashlib.sha512,
}


def GetDigest(digest_algorithm, file_to_digest):
  """Digest the file_to_digest based on digest_algorithm.

  Args:
    digest_algorithm: The algorithm used to digest the file, can be one of
      'sha256', 'sha384', or 'sha512'.
    file_to_digest: A valid file path.

  Returns:
    The digest of the file_to_digest.

  Raises:
    InvalidArgumentException: An exception raised when the digest_algorithm is
    invalid.
  """
  messages = cloudkms_base.GetMessagesModule()
  algorithm = _DIGEST_ALGORITHMS.get(digest_algorithm)
  if not algorithm:
    raise exceptions.InvalidArgumentException('digest',
                                              'digest_algorithm is invalid.')
  digest = _MultiStepsDigest(algorithm(), file_to_digest)
  kwargs = {digest_algorithm: digest.digest()}
  return messages.Digest(**kwargs)
