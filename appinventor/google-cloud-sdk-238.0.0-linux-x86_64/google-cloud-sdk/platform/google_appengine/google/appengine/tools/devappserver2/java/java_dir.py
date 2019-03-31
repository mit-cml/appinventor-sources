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
"""This module contains logic for finding the path to the java runtime lib."""
import os


def get_java_dir():
  """Return the directory the java lib is expected to be in.

  The path to the java lib can be provided via the environment variable
  `APP_ENGINE_JAVA_PATH`. If not provided, it defaults to a directory called
  `java` in a directory three levels up from the directory containing this
  file.

  For example, this file should be in
  .../google/appengine/tools/devappserver2/java/instance_factory.py
  and we want to find .../google/appengine/tools and from there,
  .../google/appengine/tools/java/lib.

  Returns:
    Path to the location where the java runtime lib is expected to be.
  """
  java_dir = os.environ.get('APP_ENGINE_JAVA_PATH', None)

  if not java_dir or not os.path.exists(java_dir):
    tools_dir = os.path.abspath(_up_n_dirs(__file__, 3))
    java_dir = os.path.join(tools_dir, 'java')
  return java_dir


def _up_n_dirs(path, n):
  """Return: the path that's up n dirs from the given path."""
  for _ in range(n):
    path = os.path.dirname(path)
  return path
