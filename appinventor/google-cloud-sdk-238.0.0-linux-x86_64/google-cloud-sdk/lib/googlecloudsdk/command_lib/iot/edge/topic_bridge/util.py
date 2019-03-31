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
"""Utilities for reading topic bridging table from file."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.cloudbuild import cloudbuild_util
from googlecloudsdk.api_lib.util import apis

_API_NAME = 'edge'
_API_VERSION = 'v1alpha1'


def _GetMessagesModule():
  return apis.GetMessagesModule(_API_NAME, _API_VERSION)


def ParseTopicBridgingTable(unused_ref, args, req):
  messages = _GetMessagesModule()
  parsed_table = cloudbuild_util.LoadMessageFromPath(
      args.rule_file, messages.TopicBridgingTable, 'topic bridging table')
  req.rules = parsed_table.rules
  return req
