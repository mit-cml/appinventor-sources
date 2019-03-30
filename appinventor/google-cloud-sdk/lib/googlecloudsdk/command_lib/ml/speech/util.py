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

"""Wrapper for interacting with speech API."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import os

from googlecloudsdk.api_lib.storage import storage_util
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.core import exceptions
from googlecloudsdk.core.util import files


SPEECH_API = 'speech'
SPEECH_API_VERSION = 'v1'


class Error(exceptions.Error):
  """Exceptions for this module."""


class AudioException(Error):
  """Raised if audio is not found."""


def GetAudioHook(version=SPEECH_API_VERSION):
  """Returns a hook to get the RecognitionAudio message for an API version."""
  def GetAudioFromPath(path):
    """Determine whether path to audio is local, build RecognitionAudio message.

    Args:
      path: str, the path to the audio.

    Raises:
      AudioException: If audio is not found locally and does not appear to be
        Google Cloud Storage URL.

    Returns:
      speech_v1_messages.RecognitionAudio, the audio message.
    """
    messages = apis.GetMessagesModule(SPEECH_API, version)
    audio = messages.RecognitionAudio()

    if os.path.isfile(path):
      audio.content = files.ReadBinaryFileContents(path)
    elif storage_util.ObjectReference.IsStorageUrl(path):
      audio.uri = path
    else:
      raise AudioException(
          'Invalid audio source [{}]. The source must either be a local path '
          'or a Google Cloud Storage URL (such as gs://bucket/object).'
          .format(path))

    return audio
  return GetAudioFromPath
