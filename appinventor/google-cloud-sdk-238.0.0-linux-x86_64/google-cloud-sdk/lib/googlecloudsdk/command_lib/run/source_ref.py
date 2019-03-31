# -*- coding: utf-8 -*- #
# Copyright 2018 Google Inc. All Rights Reserved.
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
"""Class representing a source container repository or directory."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import os
import enum

from googlecloudsdk.core import exceptions

import six


class UnknownSourceError(exceptions.Error):
  """The provided source could not be identified."""
  pass


class SourceRef(object):
  """Reference to image or local directory."""

  class SourceType(enum.Enum):
    DIRECTORY = 1
    IMAGE = 2

  def __str__(self):
    return 'SourceRef({}, {})'.format(self.source_type, self.source_path)

  def __repr__(self):
    return str(self)

  def __eq__(self, other):
    if not isinstance(other, SourceRef):
      return False
    return (other.source_type == self.source_type and
            other.source_path == self.source_path)

  def __init__(self, source_type, source_path):
    self.source_type = source_type
    self.source_path = source_path

  @classmethod
  def MakeImageRef(cls, image_arg):
    """Create a SourceRef from provided image name."""
    return cls(cls.SourceType.IMAGE, six.text_type(image_arg))

  @classmethod
  def MakeDirRef(cls, source_arg):
    """Create a SourceRef from the provided directory name."""
    if os.path.isdir(source_arg):
      return cls(cls.SourceType.DIRECTORY, source_arg)

    raise UnknownSourceError(
        'Could not identify source [{}]'.format(source_arg))
