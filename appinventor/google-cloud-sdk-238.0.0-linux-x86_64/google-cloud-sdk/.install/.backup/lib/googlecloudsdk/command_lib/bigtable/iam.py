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
"""Provides helper methods for dealing with JSON files for Bigtable IAM."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.bigtable import instances
from googlecloudsdk.api_lib.bigtable import util
from googlecloudsdk.command_lib.iam import iam_util


def AddInstanceIamPolicyBinding(instance_ref, member, role):
  """Adds a policy binding to an instance IAM policy."""
  msgs = util.GetAdminMessages()
  policy = instances.GetIamPolicy(instance_ref)
  iam_util.AddBindingToIamPolicy(msgs.Binding, policy, member, role)
  return instances.SetPolicy(instance_ref, policy)


def SetInstanceIamPolicy(instance_ref, policy):
  """Sets the IAM policy on an instance."""
  msgs = util.GetAdminMessages()
  policy = iam_util.ParsePolicyFile(policy, msgs.Policy)
  return instances.SetPolicy(instance_ref, policy)


def RemoveInstanceIamPolicyBinding(instance_ref, member, role):
  """Removes a policy binding from an instance IAM policy."""
  policy = instances.GetIamPolicy(instance_ref)
  iam_util.RemoveBindingFromIamPolicy(policy, member, role)
  return instances.SetPolicy(instance_ref, policy)
