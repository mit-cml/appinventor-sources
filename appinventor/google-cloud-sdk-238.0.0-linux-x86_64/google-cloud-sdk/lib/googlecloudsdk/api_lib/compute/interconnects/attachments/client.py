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
"""Interconnect Attachment."""

from __future__ import unicode_literals


class InterconnectAttachment(object):
  """Abstracts Interconnect attachment resource."""

  _BANDWIDTH_CONVERSION = {
      'bps-50m': 'BPS_50M',
      'bps-100m': 'BPS_100M',
      'bps-200m': 'BPS_200M',
      'bps-300m': 'BPS_300M',
      'bps-400m': 'BPS_400M',
      'bps-500m': 'BPS_500M',
      'bps-1g': 'BPS_1G',
      'bps-2g': 'BPS_2G',
      'bps-5g': 'BPS_5G',
      'bps-10g': 'BPS_10G',
      '50m': 'BPS_50M',
      '100m': 'BPS_100M',
      '200m': 'BPS_200M',
      '300m': 'BPS_300M',
      '400m': 'BPS_400M',
      '500m': 'BPS_500M',
      '1g': 'BPS_1G',
      '2g': 'BPS_2G',
      '5g': 'BPS_5G',
      '10g': 'BPS_10G',
  }

  _EDGE_AVAILABILITY_DOMAIN_CONVERSION = {
      'availability-domain-1': 'AVAILABILITY_DOMAIN_1',
      'availability-domain-2': 'AVAILABILITY_DOMAIN_2',
      'any': 'AVAILABILITY_DOMAIN_ANY'
  }

  def __init__(self, ref, compute_client=None):
    self.ref = ref
    self._compute_client = compute_client

  @property
  def _client(self):
    return self._compute_client.apitools_client

  @property
  def _messages(self):
    return self._compute_client.messages

  def _MakeCreateRequestTuple(self, description, interconnect, router):
    return (self._client.interconnectAttachments, 'Insert',
            self._messages.ComputeInterconnectAttachmentsInsertRequest(
                project=self.ref.project,
                region=self.ref.region,
                interconnectAttachment=self._messages.InterconnectAttachment(
                    name=self.ref.Name(),
                    description=description,
                    interconnect=interconnect.SelfLink(),
                    router=router.SelfLink())))

  def _MakeCreateRequestTupleAlpha(
      self, description, interconnect, router, attachment_type,
      edge_availability_domain, admin_enabled, bandwidth, pairing_key,
      vlan_tag_802_1q, candidate_subnets, partner_metadata, partner_asn):
    interconnect_self_link = None
    if interconnect:
      interconnect_self_link = interconnect.SelfLink()
    router_self_link = None
    if router:
      router_self_link = router.SelfLink()
    return (self._client.interconnectAttachments, 'Insert',
            self._messages.ComputeInterconnectAttachmentsInsertRequest(
                project=self.ref.project,
                region=self.ref.region,
                interconnectAttachment=self._messages.InterconnectAttachment(
                    name=self.ref.Name(),
                    description=description,
                    interconnect=interconnect_self_link,
                    router=router_self_link,
                    type=attachment_type,
                    edgeAvailabilityDomain=edge_availability_domain,
                    adminEnabled=admin_enabled,
                    bandwidth=bandwidth,
                    pairingKey=pairing_key,
                    vlanTag8021q=vlan_tag_802_1q,
                    candidateSubnets=candidate_subnets,
                    partnerMetadata=partner_metadata,
                    partnerAsn=partner_asn)))

  def _MakePatchRequestTupleAlpha(self, description, admin_enabled, bandwidth,
                                  partner_metadata, labels, label_fingerprint):
    return (self._client.interconnectAttachments, 'Patch',
            self._messages.ComputeInterconnectAttachmentsPatchRequest(
                project=self.ref.project,
                region=self.ref.region,
                interconnectAttachment=self.ref.Name(),
                interconnectAttachmentResource=self._messages.
                InterconnectAttachment(
                    name=self.ref.Name(),
                    description=description,
                    adminEnabled=admin_enabled,
                    labels=labels,
                    labelFingerprint=label_fingerprint,
                    bandwidth=bandwidth,
                    partnerMetadata=partner_metadata)))

  def _MakePatchRequestTupleGa(self, description, admin_enabled, bandwidth,
                               partner_metadata):
    return (self._client.interconnectAttachments, 'Patch',
            self._messages.ComputeInterconnectAttachmentsPatchRequest(
                project=self.ref.project,
                region=self.ref.region,
                interconnectAttachment=self.ref.Name(),
                interconnectAttachmentResource=self._messages.
                InterconnectAttachment(
                    name=self.ref.Name(),
                    description=description,
                    adminEnabled=admin_enabled,
                    bandwidth=bandwidth,
                    partnerMetadata=partner_metadata)))

  def _MakeDescribeRequestTuple(self):
    return (self._client.interconnectAttachments, 'Get',
            self._messages.ComputeInterconnectAttachmentsGetRequest(
                project=self.ref.project,
                region=self.ref.region,
                interconnectAttachment=self.ref.Name()))

  def _MakeDeleteRequestTuple(self):
    return (self._client.interconnectAttachments, 'Delete',
            self._messages.ComputeInterconnectAttachmentsDeleteRequest(
                project=self.ref.project,
                region=self.ref.region,
                interconnectAttachment=self.ref.Name()))

  def Create(self,
             description='',
             interconnect=None,
             router=None,
             only_generate_request=False):
    """create an interconnectAttachment."""
    requests = [self._MakeCreateRequestTuple(description, interconnect, router)]
    if not only_generate_request:
      resources = self._compute_client.MakeRequests(requests)
      return resources[0]
    return requests

  def CreateAlpha(self,
                  description='',
                  interconnect=None,
                  router=None,
                  attachment_type=None,
                  edge_availability_domain=None,
                  admin_enabled=None,
                  bandwidth=None,
                  pairing_key=None,
                  vlan_tag_802_1q=None,
                  candidate_subnets=None,
                  partner_name=None,
                  partner_interconnect=None,
                  partner_portal_url=None,
                  partner_asn=None,
                  only_generate_request=False):
    """Create an interconnectAttachment."""
    if edge_availability_domain:
      edge_availability_domain = (
          self._messages.InterconnectAttachment.
          EdgeAvailabilityDomainValueValuesEnum(
              self._EDGE_AVAILABILITY_DOMAIN_CONVERSION[
                  edge_availability_domain]))
    if bandwidth:
      bandwidth = (
          self._messages.InterconnectAttachment.BandwidthValueValuesEnum(
              self._BANDWIDTH_CONVERSION[bandwidth]))
    if attachment_type:
      attachment_type = (
          self._messages.InterconnectAttachment.TypeValueValuesEnum(
              attachment_type))
    if (partner_interconnect is not None or partner_name is not None or
        partner_portal_url is not None):
      partner_metadata = self._messages.InterconnectAttachmentPartnerMetadata(
          interconnectName=partner_interconnect,
          partnerName=partner_name,
          portalUrl=partner_portal_url)
    else:
      partner_metadata = None
    if candidate_subnets is None:
      candidate_subnets = []
    requests = [
        self._MakeCreateRequestTupleAlpha(
            description, interconnect, router, attachment_type,
            edge_availability_domain, admin_enabled, bandwidth, pairing_key,
            vlan_tag_802_1q, candidate_subnets, partner_metadata, partner_asn)
    ]
    if not only_generate_request:
      resources = self._compute_client.MakeRequests(requests)
      return resources[0]
    return requests

  def PatchAlphaAndBeta(self,
                        description='',
                        admin_enabled=None,
                        bandwidth=None,
                        partner_name=None,
                        partner_interconnect=None,
                        partner_portal_url=None,
                        labels=None,
                        label_fingerprint=None,
                        only_generate_request=False):
    """Patch an interconnectAttachment."""
    if bandwidth:
      bandwidth = (
          self._messages.InterconnectAttachment.BandwidthValueValuesEnum(
              self._BANDWIDTH_CONVERSION[bandwidth]))
    if (partner_interconnect is not None or partner_name is not None or
        partner_portal_url is not None):
      partner_metadata = self._messages.InterconnectAttachmentPartnerMetadata(
          interconnectName=partner_interconnect,
          partnerName=partner_name,
          portalUrl=partner_portal_url)
    else:
      partner_metadata = None
    requests = [
        self._MakePatchRequestTupleAlpha(description, admin_enabled, bandwidth,
                                         partner_metadata, labels,
                                         label_fingerprint)
    ]
    if not only_generate_request:
      resources = self._compute_client.MakeRequests(requests)
      return resources[0]
    return requests

  def PatchGa(self,
              description='',
              admin_enabled=None,
              bandwidth=None,
              partner_name=None,
              partner_interconnect=None,
              partner_portal_url=None,
              only_generate_request=False):
    """Patch an interconnectAttachment."""
    if bandwidth:
      bandwidth = (
          self._messages.InterconnectAttachment.BandwidthValueValuesEnum(
              self._BANDWIDTH_CONVERSION[bandwidth]))
    if (partner_interconnect is not None or partner_name is not None or
        partner_portal_url is not None):
      partner_metadata = self._messages.InterconnectAttachmentPartnerMetadata(
          interconnectName=partner_interconnect,
          partnerName=partner_name,
          portalUrl=partner_portal_url)
    else:
      partner_metadata = None
    requests = [
        self._MakePatchRequestTupleGa(description, admin_enabled, bandwidth,
                                      partner_metadata)
    ]
    if not only_generate_request:
      resources = self._compute_client.MakeRequests(requests)
      return resources[0]
    return requests

  def Describe(self, only_generate_request=False):
    requests = [self._MakeDescribeRequestTuple()]
    if not only_generate_request:
      resources = self._compute_client.MakeRequests(requests)
      return resources[0]
    return requests

  def Delete(self, only_generate_request=False):
    requests = [self._MakeDeleteRequestTuple()]
    if not only_generate_request:
      return self._compute_client.MakeRequests(requests)
    return requests
