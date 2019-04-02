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
"""protobuf stub that searches for another 'google.protobuf' on sys.path."""

import imp
import os
import sys

import google


default_path = os.path.normpath(
    os.path.join(os.path.dirname(__file__), '..', '..'))
protobuf_paths = []

default_google_path = os.path.join(default_path, 'google')
for path in google.__path__:
  if path != default_google_path:
    protobuf_paths.append(path)

for path in sys.path:
  if path != default_path:
    protobuf_path = os.path.join(path, 'google')
    if os.path.exists(protobuf_path):
      protobuf_paths.append(protobuf_path)

protobuf_file, pathname, description = imp.find_module('protobuf',
                                                       protobuf_paths)


imp.load_module('google.protobuf', protobuf_file, pathname, description)
