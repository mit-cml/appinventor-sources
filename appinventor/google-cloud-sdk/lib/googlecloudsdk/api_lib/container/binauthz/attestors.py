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

"""API helpers for interacting with attestors."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import list_pager
from googlecloudsdk.api_lib.container.binauthz import apis
from googlecloudsdk.api_lib.container.binauthz import util
from googlecloudsdk.command_lib.container.binauthz import exceptions


class Client(object):
  """A client for interacting with attestors."""

  def __init__(self, api_version=None):
    self.client = apis.GetClientInstance(api_version)
    self.messages = apis.GetMessagesModule(api_version)

  def Get(self, attestor_ref):
    """Get the specified attestor."""
    return self.client.projects_attestors.Get(
        self.messages.BinaryauthorizationProjectsAttestorsGetRequest(  # pylint: disable=line-too-long
            name=attestor_ref.RelativeName(),
        ))

  def List(self, project_ref, limit=None, batch_size=500):
    """List the attestors associated with the current project."""
    return list_pager.YieldFromList(
        self.client.projects_attestors,
        self.messages.BinaryauthorizationProjectsAttestorsListRequest(
            parent=project_ref.RelativeName(),
        ),
        batch_size=batch_size,
        limit=limit,
        field='attestors',
        batch_size_attribute='pageSize')

  def Create(self, attestor_ref, note_ref, description=None):
    """Create an attestors associated with the current project."""
    project_ref = attestor_ref.Parent(util.PROJECTS_COLLECTION)
    return self.client.projects_attestors.Create(
        self.messages.BinaryauthorizationProjectsAttestorsCreateRequest(
            attestor=self.messages.Attestor(
                name=attestor_ref.RelativeName(),
                description=description,
                userOwnedDrydockNote=self.messages.UserOwnedDrydockNote(
                    noteReference=note_ref.RelativeName(),
                ),
            ),
            attestorId=attestor_ref.Name(),
            parent=project_ref.RelativeName(),
        ))

  def AddKey(self, attestor_ref, key_content, comment=None):
    """Add a key to an attestor.

    Args:
      attestor_ref: ResourceSpec, The attestor to be updated.
      key_content: The contents of the public key file.
      comment: The comment on the public key.

    Returns:
      The added public key.

    Raises:
      AlreadyExistsError: If a public key with the same key content was found on
          the attestor.
    """
    attestor = self.Get(attestor_ref)

    existing_pub_keys = set(
        public_key.asciiArmoredPgpPublicKey
        for public_key in attestor.userOwnedDrydockNote.publicKeys)
    if key_content in existing_pub_keys:
      raise exceptions.AlreadyExistsError(
          'Provided public key already present on attestor [{}]'.format(
              attestor.name))

    attestor.userOwnedDrydockNote.publicKeys.append(
        self.messages.AttestorPublicKey(
            asciiArmoredPgpPublicKey=key_content,
            comment=comment))

    updated_attestor = self.client.projects_attestors.Update(attestor)

    return next(
        public_key
        for public_key in updated_attestor.userOwnedDrydockNote.publicKeys
        if public_key.asciiArmoredPgpPublicKey == key_content)

  def RemoveKey(self, attestor_ref, fingerprint_to_remove):
    """Remove a key on an attestor.

    Args:
      attestor_ref: ResourceSpec, The attestor to be updated.
      fingerprint_to_remove: The fingerprint of the key to remove.

    Raises:
      NotFoundError: If an expected public key could not be located by
          fingerprint.
    """
    attestor = self.Get(attestor_ref)

    existing_ids = set(
        public_key.id
        for public_key in attestor.userOwnedDrydockNote.publicKeys)
    if fingerprint_to_remove not in existing_ids:
      raise exceptions.NotFoundError(
          'No matching public key found on attestor [{}]'.format(
              attestor.name))

    attestor.userOwnedDrydockNote.publicKeys = [
        public_key for public_key in attestor.userOwnedDrydockNote.publicKeys
        if public_key.id != fingerprint_to_remove]

    self.client.projects_attestors.Update(attestor)

  def UpdateKey(
      self, attestor_ref, fingerprint, key_content=None, comment=None):
    """Update a key on an attestor.

    Args:
      attestor_ref: ResourceSpec, The attestor to be updated.
      fingerprint: The fingerprint of the key to update.
      key_content: The contents of the public key file.
      comment: The comment on the public key.

    Returns:
      The updated public key.

    Raises:
      NotFoundError: If an expected public key could not be located by
          fingerprint.
      InvalidStateError: If multiple public keys matched the provided
          fingerprint.
    """
    attestor = self.Get(attestor_ref)

    existing_keys = [
        public_key
        for public_key in attestor.userOwnedDrydockNote.publicKeys
        if public_key.id == fingerprint]

    if not existing_keys:
      raise exceptions.NotFoundError(
          'No matching public key found on attestor [{}]'.format(
              attestor.name))
    if len(existing_keys) > 1:
      raise exceptions.InvalidStateError(
          'Multiple matching public keys found on attestor [{}]'.format(
              attestor.name))

    existing_key = existing_keys[0]
    if key_content is not None:
      existing_key.asciiArmoredPgpPublicKey = key_content
    if comment is not None:
      existing_key.comment = comment

    updated_attestor = self.client.projects_attestors.Update(attestor)

    return next(
        public_key
        for public_key in updated_attestor.userOwnedDrydockNote.publicKeys
        if public_key.id == fingerprint)

  def Update(self, attestor_ref, description=None):
    """Update an attestor.

    Args:
      attestor_ref: ResourceSpec, The attestor to be updated.
      description: string, If provided, the new attestor description.

    Returns:
      The updated attestor.
    """
    attestor = self.Get(attestor_ref)

    if description is not None:
      attestor.description = description

    return self.client.projects_attestors.Update(attestor)

  def Delete(self, attestor_ref):
    """Delete the specified attestor."""
    req = self.messages.BinaryauthorizationProjectsAttestorsDeleteRequest(  # pylint: disable=line-too-long
        name=attestor_ref.RelativeName(),
    )

    self.client.projects_attestors.Delete(req)
