# -*- coding: utf-8 -*- #
# Copyright 2014 Google Inc. All Rights Reserved.
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
"""Create cluster command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import exceptions as apitools_exceptions

from googlecloudsdk.api_lib.compute import metadata_utils
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.api_lib.container import api_adapter
from googlecloudsdk.api_lib.container import kubeconfig as kconfig
from googlecloudsdk.api_lib.container import util
from googlecloudsdk.calliope import actions
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.container import constants
from googlecloudsdk.command_lib.container import flags
from googlecloudsdk.command_lib.container import messages
from googlecloudsdk.command_lib.kms import resource_args as kms_resource_args
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.console import console_io


def _AddAdditionalZonesFlag(parser, deprecated=True):
  action = None
  if deprecated:
    action = actions.DeprecationAction(
        'additional-zones',
        warn='This flag is deprecated. '
        'Use --node-locations=PRIMARY_ZONE,[ZONE,...] instead.')
  parser.add_argument(
      '--additional-zones',
      type=arg_parsers.ArgList(min_length=1),
      action=action,
      metavar='ZONE',
      help="""\
The set of additional zones in which the specified node footprint should be
replicated. All zones must be in the same region as the cluster's primary zone.
If additional-zones is not specified, all nodes will be in the cluster's primary
zone.

Note that `NUM_NODES` nodes will be created in each zone, such that if you
specify `--num-nodes=4` and choose one additional zone, 8 nodes will be created.

Multiple locations can be specified, separated by commas. For example:

  $ {command} example-cluster --zone us-central1-a --additional-zones us-central1-b,us-central1-c
""")


def _Args(parser):
  """Register flags for this command.

  Args:
    parser: An argparse.ArgumentParser-like object. It is mocked out in order to
      capture some information, but behaves like an ArgumentParser.
  """
  parser.add_argument(
      'name',
      help="""\
The name of the cluster to create.

The name may contain only lowercase alphanumerics and '-', must start with a
letter and end with an alphanumeric, and must be no longer than 40
characters.
""")
  # Timeout in seconds for operation
  parser.add_argument(
      '--timeout',
      type=int,
      default=1800,
      hidden=True,
      help='THIS ARGUMENT NEEDS HELP TEXT.')
  flags.AddAsyncFlag(parser)
  parser.add_argument(
      '--num-nodes',
      type=arg_parsers.BoundedInt(1),
      help='The number of nodes to be created in each of the cluster\'s zones.',
      default=3)
  flags.AddMachineTypeFlag(parser)
  parser.add_argument(
      '--subnetwork',
      help="""\
The Google Compute Engine subnetwork
(https://cloud.google.com/compute/docs/subnetworks) to which the cluster is
connected. The subnetwork must belong to the network specified by --network.

Cannot be used with the "--create-subnetwork" option.
""")
  parser.add_argument(
      '--network',
      help='The Compute Engine Network that the cluster will connect to. '
      'Google Kubernetes Engine will use this network when creating routes '
      'and firewalls for the clusters. Defaults to the \'default\' network.')
  parser.add_argument(
      '--cluster-ipv4-cidr',
      help='The IP address range for the pods in this cluster in CIDR '
      'notation (e.g. 10.0.0.0/14).  Prior to Kubernetes version 1.7.0 '
      'this must be a subset of 10.0.0.0/8; however, starting with version '
      '1.7.0 can be any RFC 1918 IP range.')
  parser.add_argument(
      '--enable-cloud-logging',
      action='store_true',
      default=True,
      help='Automatically send logs from the cluster to the '
      'Google Cloud Logging API.')
  parser.set_defaults(enable_cloud_logging=True)
  parser.add_argument(
      '--enable-cloud-monitoring',
      action='store_true',
      default=True,
      help='Automatically send metrics from pods in the cluster to the '
      'Google Cloud Monitoring API. VM metrics will be collected by Google '
      'Compute Engine regardless of this setting.')
  parser.set_defaults(enable_cloud_monitoring=True)
  parser.add_argument(
      '--disk-size',
      type=arg_parsers.BinarySize(lower_bound='10GB'),
      help='Size for node VM boot disks. Defaults to 100GB.')
  flags.AddBasicAuthFlags(parser)
  parser.add_argument(
      '--max-nodes-per-pool',
      type=arg_parsers.BoundedInt(100, api_adapter.MAX_NODES_PER_POOL),
      help='The maximum number of nodes to allocate per default initial node '
      'pool. Kubernetes Engine will automatically create enough nodes pools '
      'such that each node pool contains less than '
      '--max-nodes-per-pool nodes. Defaults to {nodes} nodes, but can be set '
      'as low as 100 nodes per pool on initial create.'.format(
          nodes=api_adapter.MAX_NODES_PER_POOL))
  flags.AddImageTypeFlag(parser, 'cluster')
  flags.AddImageFlag(parser, hidden=True)
  flags.AddImageProjectFlag(parser, hidden=True)
  flags.AddImageFamilyFlag(parser, hidden=True)
  flags.AddNodeLabelsFlag(parser)
  flags.AddTagsFlag(
      parser, """\
Applies the given Compute Engine tags (comma separated) on all nodes in the new
node-pool. Example:

  $ {command} example-cluster --tags=tag1,tag2

New nodes, including ones created by resize or recreate, will have these tags
on the Compute Engine API instance object and can be used in firewall rules.
See https://cloud.google.com/sdk/gcloud/reference/compute/firewall-rules/create
for examples.
""")
  flags.AddClusterVersionFlag(parser)
  flags.AddEnableAutoUpgradeFlag(parser)
  parser.display_info.AddFormat(util.CLUSTERS_FORMAT)
  flags.AddNodeVersionFlag(parser)
  flags.AddIssueClientCertificateFlag(parser)
  flags.AddAcceleratorArgs(parser)
  flags.AddDiskTypeFlag(parser)
  flags.AddMetadataFlags(parser)


def ValidateBasicAuthFlags(args):
  """Validates flags associated with basic auth.

  Overwrites username if enable_basic_auth is specified; checks that password is
  set if username is non-empty.

  Args:
    args: an argparse namespace. All the arguments that were provided to this
      command invocation.

  Raises:
    util.Error, if username is non-empty and password is not set.
  """
  if args.IsSpecified('enable_basic_auth'):
    if not args.enable_basic_auth:
      args.username = ''
    # `enable_basic_auth == true` is a no-op defaults are resoved server-side
    # based on the version of the cluster. For versions before 1.12, this is
    # 'admin', otherwise '' (disabled).
  if not args.username and args.IsSpecified('password'):
    raise util.Error(constants.USERNAME_PASSWORD_ERROR_MSG)


def ParseCreateOptionsBase(args):
  """Parses the flags provided with the cluster creation command."""
  if not (args.IsSpecified('enable_basic_auth') or
          args.IsSpecified('username')):
    log.warning('Starting in 1.12, new clusters will have basic '
                'authentication disabled by default. Basic authentication '
                'can be enabled (or disabled) manually using the '
                '`--[no-]enable-basic-auth` flag.')
  if not args.IsSpecified('issue_client_certificate'):
    log.warning('Starting in 1.12, new clusters will not have a client '
                'certificate issued. You can manually enable (or disable) the '
                'issuance of the client certificate using the '
                '`--[no-]issue-client-certificate` flag.')

  flags.MungeBasicAuthFlags(args)

  if args.IsSpecified('issue_client_certificate') and not (
      args.IsSpecified('enable_basic_auth') or args.IsSpecified('username')):
    log.warning('If `--issue-client-certificate` is specified but '
                '`--enable-basic-auth` or `--username` is not, our API will '
                'treat that as `--no-enable-basic-auth`.')

  if (args.IsSpecified('enable_cloud_endpoints') and
      properties.VALUES.container.new_scopes_behavior.GetBool()):
    raise util.Error('Flag --[no-]enable-cloud-endpoints is not allowed if '
                     'property container/ new_scopes_behavior is set to true.')
  if args.IsSpecified('enable_autorepair'):
    enable_autorepair = args.enable_autorepair
  else:
    # Node pools using COS support auto repairs, enable it for them by default.
    # Other node pools using (Ubuntu, custom images) don't support node auto
    # repairs, attempting to enable autorepair for them will result in API call
    # failing so don't do it.
    enable_autorepair = ((args.image_type or '').lower() in ['', 'cos'])
  flags.WarnForUnspecifiedIpAllocationPolicy(args)
  flags.WarnForNodeModification(args, enable_autorepair)
  metadata = metadata_utils.ConstructMetadataDict(args.metadata,
                                                  args.metadata_from_file)
  return api_adapter.CreateClusterOptions(
      accelerators=args.accelerator,
      additional_zones=args.additional_zones,
      addons=args.addons,
      cluster_ipv4_cidr=args.cluster_ipv4_cidr,
      cluster_secondary_range_name=args.cluster_secondary_range_name,
      cluster_version=args.cluster_version,
      node_version=args.node_version,
      create_subnetwork=args.create_subnetwork,
      disk_type=args.disk_type,
      enable_autorepair=enable_autorepair,
      enable_autoscaling=args.enable_autoscaling,
      enable_autoupgrade=args.enable_autoupgrade,
      enable_cloud_endpoints=args.enable_cloud_endpoints,
      enable_cloud_logging=args.enable_cloud_logging,
      enable_cloud_monitoring=args.enable_cloud_monitoring,
      enable_ip_alias=args.enable_ip_alias,
      enable_kubernetes_alpha=args.enable_kubernetes_alpha,
      enable_legacy_authorization=args.enable_legacy_authorization,
      enable_master_authorized_networks=args.enable_master_authorized_networks,
      enable_network_policy=args.enable_network_policy,
      enable_private_nodes=args.enable_private_nodes,
      enable_private_endpoint=args.enable_private_endpoint,
      image_type=args.image_type,
      image=args.image,
      image_project=args.image_project,
      image_family=args.image_family,
      issue_client_certificate=args.issue_client_certificate,
      labels=args.labels,
      local_ssd_count=args.local_ssd_count,
      maintenance_window=args.maintenance_window,
      master_authorized_networks=args.master_authorized_networks,
      master_ipv4_cidr=args.master_ipv4_cidr,
      max_nodes=args.max_nodes,
      max_nodes_per_pool=args.max_nodes_per_pool,
      min_cpu_platform=args.min_cpu_platform,
      min_nodes=args.min_nodes,
      network=args.network,
      node_disk_size_gb=utils.BytesToGb(args.disk_size),
      node_labels=args.node_labels,
      node_locations=args.node_locations,
      node_machine_type=args.machine_type,
      node_taints=args.node_taints,
      num_nodes=args.num_nodes,
      password=args.password,
      preemptible=args.preemptible,
      scopes=args.scopes,
      service_account=args.service_account,
      services_ipv4_cidr=args.services_ipv4_cidr,
      services_secondary_range_name=args.services_secondary_range_name,
      subnetwork=args.subnetwork,
      tags=args.tags,
      user=args.username,
      metadata=metadata)


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Create(base.CreateCommand):
  """Create a cluster for running containers."""

  @staticmethod
  def Args(parser):
    _Args(parser)
    _AddAdditionalZonesFlag(parser, deprecated=True)
    flags.AddNodeLocationsFlag(parser)
    flags.AddAddonsFlags(parser)
    flags.AddClusterAutoscalingFlags(parser)
    flags.AddEnableAutoRepairFlag(parser, for_create=True)
    flags.AddEnableKubernetesAlphaFlag(parser)
    flags.AddEnableLegacyAuthorizationFlag(parser)
    flags.AddIPAliasFlags(parser)
    flags.AddLabelsFlag(parser)
    flags.AddLocalSSDFlag(parser)
    flags.AddMaintenanceWindowFlag(parser)
    flags.AddMasterAuthorizedNetworksFlags(parser)
    flags.AddMinCpuPlatformFlag(parser)
    flags.AddNetworkPolicyFlags(parser)
    flags.AddNodeTaintsFlag(parser)
    flags.AddPreemptibleFlag(parser)
    flags.AddDeprecatedClusterNodeIdentityFlags(parser)
    flags.AddPrivateClusterFlags(
        parser, with_deprecated=False, with_alpha=False)

  def ParseCreateOptions(self, args):
    return ParseCreateOptionsBase(args)

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Returns:
      Cluster message for the successfully created cluster.

    Raises:
      util.Error, if creation failed.
    """
    if args.async and not args.IsSpecified('format'):
      args.format = util.OPERATIONS_FORMAT

    util.CheckKubectlInstalled()

    adapter = self.context['api_adapter']
    location_get = self.context['location_get']
    location = location_get(args)

    cluster_ref = adapter.ParseCluster(args.name, location)
    options = self.ParseCreateOptions(args)

    if options.private_cluster and not (
        options.enable_master_authorized_networks or
        options.master_authorized_networks):
      log.warning(
          '`--private-cluster` makes the master inaccessible from '
          'cluster-external IP addresses, by design. To allow limited '
          'access to the master, see the `--master-authorized-networks` flags '
          'and our documentation on setting up private clusters: '
          'https://cloud.google.com'
          '/kubernetes-engine/docs/how-to/private-clusters')

    if not (options.metadata and
            'disable-legacy-endpoints' in options.metadata):
      log.warning('Starting in 1.12, default node pools in new clusters '
                  'will have their legacy Compute Engine instance metadata '
                  'endpoints disabled by default. To create a cluster with '
                  'legacy instance metadata endpoints disabled in the default '
                  'node pool, run `clusters create` with the flag '
                  '`--metadata disable-legacy-endpoints=true`.')

    if options.enable_kubernetes_alpha:
      console_io.PromptContinue(
          message=constants.KUBERNETES_ALPHA_PROMPT,
          throw_if_unattended=True,
          cancel_on_no=True)

    if options.enable_autorepair is not None:
      log.status.Print(
          messages.AutoUpdateUpgradeRepairMessage(options.enable_autorepair,
                                                  'autorepair'))

    if options.enable_autoupgrade is not None:
      log.status.Print(
          messages.AutoUpdateUpgradeRepairMessage(options.enable_autoupgrade,
                                                  'autoupgrade'))

    if options.accelerators is not None:
      log.status.Print(constants.KUBERNETES_GPU_LIMITATION_MSG)

    operation = None
    try:
      operation_ref = adapter.CreateCluster(cluster_ref, options)
      if args.async:
        return adapter.GetCluster(cluster_ref)

      operation = adapter.WaitForOperation(
          operation_ref,
          'Creating cluster {0} in {1}'.format(cluster_ref.clusterId,
                                               cluster_ref.zone),
          timeout_s=args.timeout)
      cluster = adapter.GetCluster(cluster_ref)
    except apitools_exceptions.HttpError as error:
      raise exceptions.HttpException(error, util.HTTP_ERROR_FORMAT)

    log.CreatedResource(cluster_ref)
    cluster_url = util.GenerateClusterUrl(cluster_ref)
    log.status.Print('To inspect the contents of your cluster, go to: ' +
                     cluster_url)
    if operation.detail:
      # Non-empty detail on a DONE create operation should be surfaced as
      # a warning to end user.
      log.warning(operation.detail)

    try:
      util.ClusterConfig.Persist(cluster, cluster_ref.projectId)
    except kconfig.MissingEnvVarError as error:
      log.warning(error)

    return [cluster]


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class CreateBeta(Create):
  """Create a cluster for running containers."""

  @staticmethod
  def Args(parser):
    _Args(parser)
    group = parser.add_mutually_exclusive_group()
    _AddAdditionalZonesFlag(group, deprecated=True)
    flags.AddNodeLocationsFlag(group)
    flags.AddBetaAddonsFlags(parser)
    flags.AddClusterAutoscalingFlags(parser)
    flags.AddMaxPodsPerNodeFlag(parser)
    flags.AddEnableAutoRepairFlag(parser, for_create=True)
    flags.AddEnableBinAuthzFlag(parser)
    flags.AddEnableKubernetesAlphaFlag(parser)
    flags.AddEnableLegacyAuthorizationFlag(parser)
    flags.AddIPAliasFlags(parser)
    flags.AddIstioConfigFlag(parser)
    flags.AddLabelsFlag(parser)
    flags.AddLocalSSDFlag(parser)
    flags.AddMaintenanceWindowFlag(parser)
    flags.AddMasterAuthorizedNetworksFlags(parser)
    flags.AddMinCpuPlatformFlag(parser)
    flags.AddWorkloadMetadataFromNodeFlag(parser)
    flags.AddNetworkPolicyFlags(parser)
    flags.AddNodeTaintsFlag(parser)
    flags.AddPreemptibleFlag(parser)
    flags.AddPodSecurityPolicyFlag(parser)
    flags.AddAllowRouteOverlapFlag(parser)
    flags.AddClusterNodeIdentityFlags(parser)
    flags.AddPrivateClusterFlags(parser, with_deprecated=True, with_alpha=False)
    flags.AddEnableStackdriverKubernetesFlag(parser)
    flags.AddTpuFlags(parser, hidden=False)
    flags.AddAutoprovisioningFlags(parser)
    flags.AddVerticalPodAutoscalingFlag(parser)
    flags.AddResourceUsageExportFlags(parser)
    flags.AddAuthenticatorSecurityGroupFlags(parser, hidden=True)
    kms_flag_overrides = {
        'kms-key': '--database-encryption-key',
        'kms-keyring': '--database-encryption-key-keyring',
        'kms-location': '--database-encryption-key-location',
        'kms-project': '--database-encryption-key-project'
    }
    kms_resource_args.AddKmsKeyResourceArg(
        parser, 'cluster', flag_overrides=kms_flag_overrides)

  def ParseCreateOptions(self, args):
    ops = ParseCreateOptionsBase(args)
    ops.enable_autoprovisioning = args.enable_autoprovisioning
    ops.autoprovisioning_config_file = args.autoprovisioning_config_file
    ops.min_cpu = args.min_cpu
    ops.max_cpu = args.max_cpu
    ops.min_memory = args.min_memory
    ops.max_memory = args.max_memory
    ops.min_accelerator = args.min_accelerator
    ops.max_accelerator = args.max_accelerator
    ops.min_cpu_platform = args.min_cpu_platform
    ops.workload_metadata_from_node = args.workload_metadata_from_node
    ops.enable_pod_security_policy = args.enable_pod_security_policy
    ops.allow_route_overlap = args.allow_route_overlap
    ops.new_scopes_behavior = True
    ops.private_cluster = args.private_cluster
    ops.enable_stackdriver_kubernetes = args.enable_stackdriver_kubernetes
    ops.enable_binauthz = args.enable_binauthz
    ops.enable_tpu = args.enable_tpu
    ops.tpu_ipv4_cidr = args.tpu_ipv4_cidr
    ops.istio_config = args.istio_config
    ops.enable_vertical_pod_autoscaling = args.enable_vertical_pod_autoscaling
    ops.default_max_pods_per_node = args.default_max_pods_per_node
    ops.max_pods_per_node = args.max_pods_per_node
    ops.resource_usage_bigquery_dataset = args.resource_usage_bigquery_dataset
    ops.enable_network_egress_metering = args.enable_network_egress_metering
    ops.security_group = args.security_group
    flags.ValidateIstioConfigCreateArgs(args.istio_config, args.addons)
    kms_ref = args.CONCEPTS.kms_key.Parse()
    if kms_ref:
      ops.database_encryption = kms_ref.RelativeName()
    else:
      # Check for partially specified database-encryption-key.
      for keyword in [
          'database-encryption-key', 'database-encryption-key-keyring',
          'database-encryption-key-location', 'database-encryption-key-project'
      ]:
        if getattr(args, keyword.replace('-', '_'), None):
          raise exceptions.InvalidArgumentException('--database-encryption-key',
                                                    'not fully specified.')
    return ops


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class CreateAlpha(Create):
  """Create a cluster for running containers."""

  @staticmethod
  def Args(parser):
    _Args(parser)
    group = parser.add_mutually_exclusive_group()
    _AddAdditionalZonesFlag(group, deprecated=True)
    flags.AddNodeLocationsFlag(group)
    flags.AddAlphaAddonsFlags(parser)
    flags.AddClusterAutoscalingFlags(parser)
    flags.AddMaxPodsPerNodeFlag(parser)
    flags.AddEnableAutoRepairFlag(parser, for_create=True)
    flags.AddEnableBinAuthzFlag(parser)
    flags.AddEnableKubernetesAlphaFlag(parser)
    flags.AddEnableLegacyAuthorizationFlag(parser)
    flags.AddIPAliasFlags(parser)
    flags.AddIstioConfigFlag(parser)
    flags.AddLabelsFlag(parser)
    flags.AddLocalSSDAndLocalSSDVolumeConfigsFlag(parser)
    flags.AddMaintenanceWindowFlag(parser)
    flags.AddMasterAuthorizedNetworksFlags(parser)
    flags.AddMinCpuPlatformFlag(parser)
    flags.AddWorkloadMetadataFromNodeFlag(parser)
    flags.AddNetworkPolicyFlags(parser)
    flags.AddAutoprovisioningFlags(parser, hidden=False)
    flags.AddNodeTaintsFlag(parser)
    flags.AddPreemptibleFlag(parser)
    flags.AddPodSecurityPolicyFlag(parser)
    flags.AddAllowRouteOverlapFlag(parser)
    flags.AddPrivateClusterFlags(parser, with_deprecated=True, with_alpha=True)
    flags.AddClusterNodeIdentityFlags(parser)
    flags.AddTpuFlags(parser, hidden=False, enable_tpu_service_networking=True)
    flags.AddEnableStackdriverKubernetesFlag(parser)
    flags.AddManagedPodIdentityFlags(parser)
    flags.AddResourceUsageExportFlags(parser)
    flags.AddAuthenticatorSecurityGroupFlags(parser)
    flags.AddVerticalPodAutoscalingFlag(parser)
    flags.AddSecurityProfileForCreateFlags(parser)
    flags.AddInitialNodePoolNameArg(parser, hidden=False)
    flags.AddEnablePrivateIpv6AccessFlag(parser, hidden=True)
    kms_flag_overrides = {
        'kms-key': '--database-encryption-key',
        'kms-keyring': '--database-encryption-key-keyring',
        'kms-location': '--database-encryption-key-location',
        'kms-project': '--database-encryption-key-project'
    }
    kms_resource_args.AddKmsKeyResourceArg(
        parser, 'cluster', flag_overrides=kms_flag_overrides)

  def ParseCreateOptions(self, args):
    ops = ParseCreateOptionsBase(args)
    ops.enable_autoprovisioning = args.enable_autoprovisioning
    ops.autoprovisioning_config_file = args.autoprovisioning_config_file
    ops.min_cpu = args.min_cpu
    ops.max_cpu = args.max_cpu
    ops.min_memory = args.min_memory
    ops.max_memory = args.max_memory
    ops.min_accelerator = args.min_accelerator
    ops.max_accelerator = args.max_accelerator
    ops.local_ssd_volume_configs = args.local_ssd_volumes
    ops.enable_binauthz = args.enable_binauthz
    ops.workload_metadata_from_node = args.workload_metadata_from_node
    ops.enable_pod_security_policy = args.enable_pod_security_policy
    ops.allow_route_overlap = args.allow_route_overlap
    ops.private_cluster = args.private_cluster
    ops.enable_private_nodes = args.enable_private_nodes
    ops.enable_private_endpoint = args.enable_private_endpoint
    ops.master_ipv4_cidr = args.master_ipv4_cidr
    ops.new_scopes_behavior = True
    ops.enable_tpu = args.enable_tpu
    ops.tpu_ipv4_cidr = args.tpu_ipv4_cidr
    ops.enable_tpu_service_networking = args.enable_tpu_service_networking
    ops.istio_config = args.istio_config
    ops.enable_stackdriver_kubernetes = args.enable_stackdriver_kubernetes
    ops.default_max_pods_per_node = args.default_max_pods_per_node
    ops.max_pods_per_node = args.max_pods_per_node
    ops.enable_managed_pod_identity = args.enable_managed_pod_identity
    ops.federating_service_account = args.federating_service_account
    ops.resource_usage_bigquery_dataset = args.resource_usage_bigquery_dataset
    ops.security_group = args.security_group
    flags.ValidateIstioConfigCreateArgs(args.istio_config, args.addons)
    ops.enable_vertical_pod_autoscaling = args.enable_vertical_pod_autoscaling
    ops.security_profile = args.security_profile
    ops.security_profile_runtime_rules = args.security_profile_runtime_rules
    ops.node_pool_name = args.node_pool_name
    ops.enable_network_egress_metering = args.enable_network_egress_metering
    ops.enable_private_ipv6_access = args.enable_private_ipv6_access
    ops.enable_peering_route_sharing = args.enable_peering_route_sharing
    kms_ref = args.CONCEPTS.kms_key.Parse()
    if kms_ref:
      ops.database_encryption = kms_ref.RelativeName()
    else:
      # Check for partially specified database-encryption-key.
      for keyword in [
          'database-encryption-key', 'database-encryption-key-keyring',
          'database-encryption-key-location', 'database-encryption-key-project'
      ]:
        if getattr(args, keyword.replace('-', '_'), None):
          raise exceptions.InvalidArgumentException('--database-encryption-key',
                                                    'not fully specified.')
    return ops
