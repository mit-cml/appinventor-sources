# -*- coding: utf-8 -*- #
# Copyright 2019 Google Inc. All Rights Reserved.
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
"""Extra help for .gcloudignore."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base


class AccessibilityHelp(base.TopicCommand):
  r"""Reference for `Accessibility` features.

  The `accessibility/screen_reader` property when set to true will change some
  behavior to make gcloud more screen reader friendly. Currently the following
  changes are implemented:

  * For progress trackers, instead of unicode spinners, the phrase 'working'
    will be displayed on stderr, every second while gcloud is working.
  * For progress bars, progress will be displayed as a percentage, outputted to
    stderr.
  * Boxed tables will be drawn with ascii characters instead of unicode
    characters. Boxed tables are the default outputs of many list commands.
    Also consider using the --format flag to define your own format.

  To turn this on, run:

    $ gcloud config set accessibility/screen_reader true

  Accessibiliy support is still in early stages. Please report any issues that
  you would like fixed using `gcloud feedback`.
  """
