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
"""Flags and helpers for the compute networks peerings commands."""


def AddImportCustomRoutesFlag(parser):
  """Adds importCustomRoutes flag to the argparse.ArgumentParser."""
  parser.add_argument(
      '--import-custom-routes',
      action='store_true',
      default=None,
      help="""\
        If set, the network will import custom routes from peer network. Use
        --no-import-custom-routes to disable it.
      """)


def AddExportCustomRoutesFlag(parser):
  """Adds exportCustomRoutes flag to the argparse.ArgumentParser."""
  parser.add_argument(
      '--export-custom-routes',
      action='store_true',
      default=None,
      help="""\
        If set, the network will export custom routes to peer network. Use
        --no-export-custom-routes to disable it.
      """)
