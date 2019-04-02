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
"""Resource definitions for cloud platform apis."""

import enum


BASE_URL = 'https://www.googleapis.com/compute/v1/'
DOCS_URL = 'https://developers.google.com/compute/docs/reference/latest/'


class Collections(enum.Enum):
  """Collections for all supported apis."""

  ACCELERATORTYPES = (
      'acceleratorTypes',
      'projects/{project}/zones/{zone}/acceleratorTypes/{acceleratorType}',
      {},
      [u'project', u'zone', u'acceleratorType'],
      True
  )
  ADDRESSES = (
      'addresses',
      'projects/{project}/regions/{region}/addresses/{address}',
      {},
      [u'project', u'region', u'address'],
      True
  )
  AUTOSCALERS = (
      'autoscalers',
      'projects/{project}/zones/{zone}/autoscalers/{autoscaler}',
      {},
      [u'project', u'zone', u'autoscaler'],
      True
  )
  BACKENDBUCKETS = (
      'backendBuckets',
      'projects/{project}/global/backendBuckets/{backendBucket}',
      {},
      [u'project', u'backendBucket'],
      True
  )
  BACKENDSERVICES = (
      'backendServices',
      'projects/{project}/global/backendServices/{backendService}',
      {},
      [u'project', u'backendService'],
      True
  )
  DISKTYPES = (
      'diskTypes',
      'projects/{project}/zones/{zone}/diskTypes/{diskType}',
      {},
      [u'project', u'zone', u'diskType'],
      True
  )
  DISKS = (
      'disks',
      'projects/{project}/zones/{zone}/disks/{disk}',
      {},
      [u'project', u'zone', u'disk'],
      True
  )
  FIREWALLS = (
      'firewalls',
      'projects/{project}/global/firewalls/{firewall}',
      {},
      [u'project', u'firewall'],
      True
  )
  FORWARDINGRULES = (
      'forwardingRules',
      'projects/{project}/regions/{region}/forwardingRules/{forwardingRule}',
      {},
      [u'project', u'region', u'forwardingRule'],
      True
  )
  GLOBALADDRESSES = (
      'globalAddresses',
      'projects/{project}/global/addresses/{address}',
      {},
      [u'project', u'address'],
      True
  )
  GLOBALFORWARDINGRULES = (
      'globalForwardingRules',
      'projects/{project}/global/forwardingRules/{forwardingRule}',
      {},
      [u'project', u'forwardingRule'],
      True
  )
  GLOBALOPERATIONS = (
      'globalOperations',
      'projects/{project}/global/operations/{operation}',
      {},
      [u'project', u'operation'],
      True
  )
  HEALTHCHECKS = (
      'healthChecks',
      'projects/{project}/global/healthChecks/{healthCheck}',
      {},
      [u'project', u'healthCheck'],
      True
  )
  HTTPHEALTHCHECKS = (
      'httpHealthChecks',
      'projects/{project}/global/httpHealthChecks/{httpHealthCheck}',
      {},
      [u'project', u'httpHealthCheck'],
      True
  )
  HTTPSHEALTHCHECKS = (
      'httpsHealthChecks',
      'projects/{project}/global/httpsHealthChecks/{httpsHealthCheck}',
      {},
      [u'project', u'httpsHealthCheck'],
      True
  )
  IMAGES = (
      'images',
      'projects/{project}/global/images/{image}',
      {},
      [u'project', u'image'],
      True
  )
  INSTANCEGROUPMANAGERS = (
      'instanceGroupManagers',
      'projects/{project}/zones/{zone}/instanceGroupManagers/'
      '{instanceGroupManager}',
      {},
      [u'project', u'zone', u'instanceGroupManager'],
      True
  )
  INSTANCEGROUPS = (
      'instanceGroups',
      'projects/{project}/zones/{zone}/instanceGroups/{instanceGroup}',
      {},
      [u'project', u'zone', u'instanceGroup'],
      True
  )
  INSTANCETEMPLATES = (
      'instanceTemplates',
      'projects/{project}/global/instanceTemplates/{instanceTemplate}',
      {},
      [u'project', u'instanceTemplate'],
      True
  )
  INSTANCES = (
      'instances',
      'projects/{project}/zones/{zone}/instances/{instance}',
      {},
      [u'project', u'zone', u'instance'],
      True
  )
  INTERCONNECTATTACHMENTS = (
      'interconnectAttachments',
      'projects/{project}/regions/{region}/interconnectAttachments/'
      '{interconnectAttachment}',
      {},
      [u'project', u'region', u'interconnectAttachment'],
      True
  )
  INTERCONNECTLOCATIONS = (
      'interconnectLocations',
      'projects/{project}/global/interconnectLocations/{interconnectLocation}',
      {},
      [u'project', u'interconnectLocation'],
      True
  )
  INTERCONNECTS = (
      'interconnects',
      'projects/{project}/global/interconnects/{interconnect}',
      {},
      [u'project', u'interconnect'],
      True
  )
  LICENSECODES = (
      'licenseCodes',
      'projects/{project}/global/licenseCodes/{licenseCode}',
      {},
      [u'project', u'licenseCode'],
      True
  )
  LICENSES = (
      'licenses',
      'projects/{project}/global/licenses/{license}',
      {},
      [u'project', u'license'],
      True
  )
  MACHINETYPES = (
      'machineTypes',
      'projects/{project}/zones/{zone}/machineTypes/{machineType}',
      {},
      [u'project', u'zone', u'machineType'],
      True
  )
  NETWORKS = (
      'networks',
      'projects/{project}/global/networks/{network}',
      {},
      [u'project', u'network'],
      True
  )
  NEXTHOPGATEWAYS = (
      'nextHopGateways',
      'projects/{project}/global/gateways/{nextHopGateway}',
      {},
      [u'project', u'nextHopGateway'],
      True
  )
  NODEGROUPS = (
      'nodeGroups',
      'projects/{project}/zones/{zone}/nodeGroups/{nodeGroup}',
      {},
      [u'project', u'zone', u'nodeGroup'],
      True
  )
  NODETEMPLATES = (
      'nodeTemplates',
      'projects/{project}/regions/{region}/nodeTemplates/{nodeTemplate}',
      {},
      [u'project', u'region', u'nodeTemplate'],
      True
  )
  NODETYPES = (
      'nodeTypes',
      'projects/{project}/zones/{zone}/nodeTypes/{nodeType}',
      {},
      [u'project', u'zone', u'nodeType'],
      True
  )
  PROJECTS = (
      'projects',
      'projects/{project}',
      {},
      [u'project'],
      True
  )
  REGIONAUTOSCALERS = (
      'regionAutoscalers',
      'projects/{project}/regions/{region}/autoscalers/{autoscaler}',
      {},
      [u'project', u'region', u'autoscaler'],
      True
  )
  REGIONBACKENDSERVICES = (
      'regionBackendServices',
      'projects/{project}/regions/{region}/backendServices/{backendService}',
      {},
      [u'project', u'region', u'backendService'],
      True
  )
  REGIONCOMMITMENTS = (
      'regionCommitments',
      'projects/{project}/regions/{region}/commitments/{commitment}',
      {},
      [u'project', u'region', u'commitment'],
      True
  )
  REGIONDISKTYPES = (
      'regionDiskTypes',
      'projects/{project}/regions/{region}/diskTypes/{diskType}',
      {},
      [u'project', u'region', u'diskType'],
      True
  )
  REGIONDISKS = (
      'regionDisks',
      'projects/{project}/regions/{region}/disks/{disk}',
      {},
      [u'project', u'region', u'disk'],
      True
  )
  REGIONINSTANCEGROUPMANAGERS = (
      'regionInstanceGroupManagers',
      'projects/{project}/regions/{region}/instanceGroupManagers/'
      '{instanceGroupManager}',
      {},
      [u'project', u'region', u'instanceGroupManager'],
      True
  )
  REGIONINSTANCEGROUPS = (
      'regionInstanceGroups',
      'projects/{project}/regions/{region}/instanceGroups/{instanceGroup}',
      {},
      [u'project', u'region', u'instanceGroup'],
      True
  )
  REGIONOPERATIONS = (
      'regionOperations',
      'projects/{project}/regions/{region}/operations/{operation}',
      {},
      [u'project', u'region', u'operation'],
      True
  )
  REGIONS = (
      'regions',
      'projects/{project}/regions/{region}',
      {},
      [u'project', u'region'],
      True
  )
  ROUTERS = (
      'routers',
      'projects/{project}/regions/{region}/routers/{router}',
      {},
      [u'project', u'region', u'router'],
      True
  )
  ROUTES = (
      'routes',
      'projects/{project}/global/routes/{route}',
      {},
      [u'project', u'route'],
      True
  )
  SECURITYPOLICIES = (
      'securityPolicies',
      'projects/{project}/global/securityPolicies/{securityPolicy}',
      {},
      [u'project', u'securityPolicy'],
      True
  )
  SECURITYPOLICYRULES = (
      'securityPolicyRules',
      'projects/{project}/global/securityPolicies/{securityPolicy}/'
      'securityPolicyRules/{securityPolicyRule}',
      {},
      [u'project', u'securityPolicy', u'securityPolicyRule'],
      True
  )
  SNAPSHOTS = (
      'snapshots',
      'projects/{project}/global/snapshots/{snapshot}',
      {},
      [u'project', u'snapshot'],
      True
  )
  SSLCERTIFICATES = (
      'sslCertificates',
      'projects/{project}/global/sslCertificates/{sslCertificate}',
      {},
      [u'project', u'sslCertificate'],
      True
  )
  SSLPOLICIES = (
      'sslPolicies',
      'projects/{project}/global/sslPolicies/{sslPolicy}',
      {},
      [u'project', u'sslPolicy'],
      True
  )
  SUBNETWORKS = (
      'subnetworks',
      'projects/{project}/regions/{region}/subnetworks/{subnetwork}',
      {},
      [u'project', u'region', u'subnetwork'],
      True
  )
  TARGETHTTPPROXIES = (
      'targetHttpProxies',
      'projects/{project}/global/targetHttpProxies/{targetHttpProxy}',
      {},
      [u'project', u'targetHttpProxy'],
      True
  )
  TARGETHTTPSPROXIES = (
      'targetHttpsProxies',
      'projects/{project}/global/targetHttpsProxies/{targetHttpsProxy}',
      {},
      [u'project', u'targetHttpsProxy'],
      True
  )
  TARGETINSTANCES = (
      'targetInstances',
      'projects/{project}/zones/{zone}/targetInstances/{targetInstance}',
      {},
      [u'project', u'zone', u'targetInstance'],
      True
  )
  TARGETPOOLS = (
      'targetPools',
      'projects/{project}/regions/{region}/targetPools/{targetPool}',
      {},
      [u'project', u'region', u'targetPool'],
      True
  )
  TARGETSSLPROXIES = (
      'targetSslProxies',
      'projects/{project}/global/targetSslProxies/{targetSslProxy}',
      {},
      [u'project', u'targetSslProxy'],
      True
  )
  TARGETTCPPROXIES = (
      'targetTcpProxies',
      'projects/{project}/global/targetTcpProxies/{targetTcpProxy}',
      {},
      [u'project', u'targetTcpProxy'],
      True
  )
  TARGETVPNGATEWAYS = (
      'targetVpnGateways',
      'projects/{project}/regions/{region}/targetVpnGateways/'
      '{targetVpnGateway}',
      {},
      [u'project', u'region', u'targetVpnGateway'],
      True
  )
  URLMAPS = (
      'urlMaps',
      'projects/{project}/global/urlMaps/{urlMap}',
      {},
      [u'project', u'urlMap'],
      True
  )
  VPNTUNNELS = (
      'vpnTunnels',
      'projects/{project}/regions/{region}/vpnTunnels/{vpnTunnel}',
      {},
      [u'project', u'region', u'vpnTunnel'],
      True
  )
  ZONEOPERATIONS = (
      'zoneOperations',
      'projects/{project}/zones/{zone}/operations/{operation}',
      {},
      [u'project', u'zone', u'operation'],
      True
  )
  ZONES = (
      'zones',
      'projects/{project}/zones/{zone}',
      {},
      [u'project', u'zone'],
      True
  )

  def __init__(self, collection_name, path, flat_paths, params,
               enable_uri_parsing):
    self.collection_name = collection_name
    self.path = path
    self.flat_paths = flat_paths
    self.params = params
    self.enable_uri_parsing = enable_uri_parsing
