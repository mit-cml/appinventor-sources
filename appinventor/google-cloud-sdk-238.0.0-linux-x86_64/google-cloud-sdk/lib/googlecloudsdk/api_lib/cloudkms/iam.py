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
"""IAM-related helpers for working with the Cloud KMS API."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.cloudkms import base
from googlecloudsdk.command_lib.iam import iam_util


def GetKeyRingIamPolicy(key_ring_ref):
  """Fetch the IAM Policy attached to the named KeyRing.

  Args:
      key_ring_ref: A resources.Resource naming the KeyRing.

  Returns:
      An apitools wrapper for the IAM Policy.
  """
  client = base.GetClientInstance()
  messages = base.GetMessagesModule()

  req = messages.CloudkmsProjectsLocationsKeyRingsGetIamPolicyRequest(
      resource=key_ring_ref.RelativeName())

  return client.projects_locations_keyRings.GetIamPolicy(req)


def SetKeyRingIamPolicy(key_ring_ref, policy, update_mask):
  """Set the IAM Policy attached to the named KeyRing to the given policy.

  If 'policy' has no etag specified, this will BLINDLY OVERWRITE the IAM policy!

  Args:
      key_ring_ref: A resources.Resource naming the KeyRing.
      policy: An apitools wrapper for the IAM Policy.
      update_mask: str, FieldMask represented as comma-separated field names.

  Returns:
      The IAM Policy.
  """
  client = base.GetClientInstance()
  messages = base.GetMessagesModule()

  req = messages.CloudkmsProjectsLocationsKeyRingsSetIamPolicyRequest(
      resource=key_ring_ref.RelativeName(),
      setIamPolicyRequest=messages.SetIamPolicyRequest(
          policy=policy, updateMask=update_mask))

  return client.projects_locations_keyRings.SetIamPolicy(req)


def AddPolicyBindingToKeyRing(key_ring_ref, member, role):
  """Does an atomic Read-Modify-Write, adding the member to the role."""
  messages = base.GetMessagesModule()

  policy = GetKeyRingIamPolicy(key_ring_ref)
  iam_util.AddBindingToIamPolicy(messages.Binding, policy, member, role)
  return SetKeyRingIamPolicy(key_ring_ref, policy, update_mask='bindings,etag')


def RemovePolicyBindingFromKeyRing(key_ring_ref, member, role):
  """Does an atomic Read-Modify-Write, removing the member from the role."""
  policy = GetKeyRingIamPolicy(key_ring_ref)
  iam_util.RemoveBindingFromIamPolicy(policy, member, role)
  return SetKeyRingIamPolicy(key_ring_ref, policy, update_mask='bindings,etag')


def GetCryptoKeyIamPolicy(crypto_key_ref):
  """Fetch the IAM Policy attached to the named CryptoKey.

  Args:
      crypto_key_ref: A resources.Resource naming the CryptoKey.

  Returns:
      An apitools wrapper for the IAM Policy.
  """
  client = base.GetClientInstance()
  messages = base.GetMessagesModule()

  req = messages.CloudkmsProjectsLocationsKeyRingsCryptoKeysGetIamPolicyRequest(
      resource=crypto_key_ref.RelativeName())

  return client.projects_locations_keyRings_cryptoKeys.GetIamPolicy(req)


def SetCryptoKeyIamPolicy(crypto_key_ref, policy, update_mask):
  """Set the IAM Policy attached to the named CryptoKey to the given policy.

  If 'policy' has no etag specified, this will BLINDLY OVERWRITE the IAM policy!

  Args:
      crypto_key_ref: A resources.Resource naming the CryptoKey.
      policy: An apitools wrapper for the IAM Policy.
      update_mask: str, FieldMask represented as comma-separated field names.

  Returns:
      The IAM Policy.
  """
  client = base.GetClientInstance()
  messages = base.GetMessagesModule()

  req = messages.CloudkmsProjectsLocationsKeyRingsCryptoKeysSetIamPolicyRequest(
      resource=crypto_key_ref.RelativeName(),
      setIamPolicyRequest=messages.SetIamPolicyRequest(
          policy=policy, updateMask=update_mask))

  return client.projects_locations_keyRings_cryptoKeys.SetIamPolicy(req)


def AddPolicyBindingToCryptoKey(crypto_key_ref, member, role):
  """Does an atomic Read-Modify-Write, adding the member to the role."""
  messages = base.GetMessagesModule()

  policy = GetCryptoKeyIamPolicy(crypto_key_ref)
  iam_util.AddBindingToIamPolicy(messages.Binding, policy, member, role)
  return SetCryptoKeyIamPolicy(
      crypto_key_ref, policy, update_mask='bindings,etag')


def RemovePolicyBindingFromCryptoKey(crypto_key_ref, member, role):
  """Does an atomic Read-Modify-Write, removing the member from the role."""
  policy = GetCryptoKeyIamPolicy(crypto_key_ref)
  iam_util.RemoveBindingFromIamPolicy(policy, member, role)
  return SetCryptoKeyIamPolicy(
      crypto_key_ref, policy, update_mask='bindings,etag')
