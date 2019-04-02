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
"""Instances utilities for `gcloud redis` commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import encoding
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources


def ParseInstanceNetworkArg(network):
  project = properties.VALUES.core.project.GetOrFail()
  network_ref = resources.REGISTRY.Create(
      'compute.networks', project=project, network=network)
  return network_ref.RelativeName()


def PackageInstanceLabels(labels, messages):
  return encoding.DictToAdditionalPropertyMessage(
      labels, messages.Instance.LabelsValue, sort_items=True)
