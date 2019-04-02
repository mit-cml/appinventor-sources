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
"""Code that's shared between multiple backend-buckets subcommands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.command_lib.compute.backend_buckets import flags as backend_buckets_flags


def AddUpdatableArgs(cls, parser, operation_type):
  """Adds top-level backend bucket arguments that can be updated.

  Args:
    cls: type, Class to add backend bucket argument to.
    parser: The argparse parser.
    operation_type: str, operation_type forwarded to AddArgument(...)
  """
  cls.BACKEND_BUCKET_ARG = backend_buckets_flags.BackendBucketArgument()
  cls.BACKEND_BUCKET_ARG.AddArgument(parser, operation_type=operation_type)

  parser.add_argument(
      '--description',
      help='An optional, textual description for the backend bucket.')

  parser.add_argument(
      '--enable-cdn',
      action='store_true',
      default=None,  # Tri-valued, None => don't change the setting.
      help="""\
      Enable Cloud CDN for the backend bucket. Cloud CDN can cache HTTP
      responses from a backend bucket at the edge of the network, close to
      users.""")
