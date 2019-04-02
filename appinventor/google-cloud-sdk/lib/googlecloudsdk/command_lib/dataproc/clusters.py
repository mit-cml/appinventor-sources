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

"""Utilities for building the dataproc clusters CLI."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import collections
from apitools.base.py import encoding

from googlecloudsdk.api_lib.compute import utils as api_utils
from googlecloudsdk.api_lib.dataproc import compute_helpers
from googlecloudsdk.api_lib.dataproc import constants
from googlecloudsdk.api_lib.dataproc import exceptions
from googlecloudsdk.api_lib.dataproc import util
from googlecloudsdk.calliope import actions
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.instances import flags as instances_flags
from googlecloudsdk.command_lib.dataproc import flags
from googlecloudsdk.command_lib.kms import resource_args as kms_resource_args
from googlecloudsdk.command_lib.util.args import labels_util
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core import yaml
from googlecloudsdk.core.console import console_io
from googlecloudsdk.core.util import times

GENERATED_LABEL_PREFIX = 'goog-dataproc-'


# beta is unused but still useful when we add new beta features
def ArgsForClusterRef(parser, beta=False, include_deprecated=True):
  """Register flags for creating a dataproc cluster.

  Args:
    parser: The argparse.ArgParser to configure with dataproc cluster arguments.
    beta: whether or not this is a beta command (may affect flag visibility)
    include_deprecated: whether deprecated flags should be included
  """
  labels_util.AddCreateLabelsFlags(parser)
  instances_flags.AddTagsArgs(parser)
  # 30m is backend timeout + 5m for safety buffer.
  flags.AddTimeoutFlag(parser, default='35m')
  flags.AddZoneFlag(parser, short_flags=include_deprecated)
  flags.AddComponentFlag(parser, not beta)  # Hidden in GA track.

  parser.add_argument(
      '--metadata',
      type=arg_parsers.ArgDict(min_length=1),
      action='append',
      default=None,
      help=('Metadata to be made available to the guest operating system '
            'running on the instances'),
      metavar='KEY=VALUE')

  # Either allow creating a single node cluster (--single-node), or specifying
  # the number of workers in the multi-node cluster (--num-workers and
  # --num-preemptible-workers)
  node_group = parser.add_argument_group(mutex=True)  # Mutually exclusive
  node_group.add_argument(
      '--single-node',
      action='store_true',
      help="""\
      Create a single node cluster.

      A single node cluster has all master and worker components.
      It cannot have any separate worker nodes. If this flag is not
      specified, a cluster with separate workers is created.
      """)
  # Not mutually exclusive
  worker_group = node_group.add_argument_group(help='Multi-node cluster flags')
  worker_group.add_argument(
      '--num-workers',
      type=int,
      help='The number of worker nodes in the cluster. Defaults to '
      'server-specified.')
  worker_group.add_argument(
      '--num-preemptible-workers',
      type=int,
      help='The number of preemptible worker nodes in the cluster.')

  parser.add_argument(
      '--master-machine-type',
      help='The type of machine to use for the master. Defaults to '
      'server-specified.')
  parser.add_argument(
      '--worker-machine-type',
      help='The type of machine to use for workers. Defaults to '
      'server-specified.')
  image_parser = parser.add_mutually_exclusive_group()
  # TODO(b/73291743): Add external doc link to --image
  image_parser.add_argument(
      '--image',
      metavar='IMAGE',
      help='The full custom image URI or the custom image name that '
      'will be used to create a cluster.')
  image_parser.add_argument(
      '--image-version',
      metavar='VERSION',
      help='The image version to use for the cluster. Defaults to the '
      'latest version.')
  parser.add_argument(
      '--bucket',
      help='The Google Cloud Storage bucket to use with the Google Cloud '
      'Storage connector. A bucket is auto created when this parameter is '
      'not specified.')

  netparser = parser.add_mutually_exclusive_group()
  netparser.add_argument(
      '--network',
      help="""\
      The Compute Engine network that the VM instances of the cluster will be
      part of. This is mutually exclusive with --subnet. If neither is
      specified, this defaults to the "default" network.
      """)
  netparser.add_argument(
      '--subnet',
      help="""\
      Specifies the subnet that the cluster will be part of. This is mutally
      exclusive with --network.
      """)
  parser.add_argument(
      '--num-worker-local-ssds',
      type=int,
      help='The number of local SSDs to attach to each worker in a cluster.')
  parser.add_argument(
      '--num-master-local-ssds',
      type=int,
      help='The number of local SSDs to attach to the master in a cluster.')
  parser.add_argument(
      '--num-preemptible-worker-local-ssds',
      type=int,
      help="""\
      The number of local SSDs to attach to each preemptible worker in
      a cluster.
      """)
  parser.add_argument(
      '--initialization-actions',
      type=arg_parsers.ArgList(min_length=1),
      metavar='CLOUD_STORAGE_URI',
      help=('A list of Google Cloud Storage URIs of '
            'executables to run on each node in the cluster.'))
  parser.add_argument(
      '--initialization-action-timeout',
      type=arg_parsers.Duration(),
      metavar='TIMEOUT',
      default='10m',
      help=('The maximum duration of each initialization action. See '
            '$ gcloud topic datetimes for information on duration formats.'))
  parser.add_argument(
      '--num-masters',
      type=arg_parsers.CustomFunctionValidator(
          lambda n: int(n) in [1, 3],
          'Number of masters must be 1 (Standard) or 3 (High Availability)',
          parser=arg_parsers.BoundedInt(1, 3)),
      help="""\
      The number of master nodes in the cluster.

      Number of Masters | Cluster Mode
      --- | ---
      1 | Standard
      3 | High Availability
      """)
  parser.add_argument(
      '--properties',
      type=arg_parsers.ArgDict(),
      metavar='PREFIX:PROPERTY=VALUE',
      default={},
      help="""\
Specifies configuration properties for installed packages, such as Hadoop
and Spark.

Properties are mapped to configuration files by specifying a prefix, such as
"core:io.serializations". The following are supported prefixes and their
mappings:

Prefix | File | Purpose of file
--- | --- | ---
capacity-scheduler | capacity-scheduler.xml | Hadoop YARN Capacity Scheduler configuration
core | core-site.xml | Hadoop general configuration
distcp | distcp-default.xml | Hadoop Distributed Copy configuration
hadoop-env | hadoop-env.sh | Hadoop specific environment variables
hdfs | hdfs-site.xml | Hadoop HDFS configuration
hive | hive-site.xml | Hive configuration
mapred | mapred-site.xml | Hadoop MapReduce configuration
mapred-env | mapred-env.sh | Hadoop MapReduce specific environment variables
pig | pig.properties | Pig configuration
spark | spark-defaults.conf | Spark configuration
spark-env | spark-env.sh | Spark specific environment variables
yarn | yarn-site.xml | Hadoop YARN configuration
yarn-env | yarn-env.sh | Hadoop YARN specific environment variables

See https://cloud.google.com/dataproc/docs/concepts/configuring-clusters/cluster-properties
for more information.

""")
  parser.add_argument(
      '--service-account',
      help='The Google Cloud IAM service account to be authenticated as.')
  parser.add_argument(
      '--scopes',
      type=arg_parsers.ArgList(min_length=1),
      metavar='SCOPE',
      help="""\
Specifies scopes for the node instances. Multiple SCOPEs can be specified,
separated by commas.
Examples:

  $ {{command}} example-cluster --scopes https://www.googleapis.com/auth/bigtable.admin

  $ {{command}} example-cluster --scopes sqlservice,bigquery

The following *minimum scopes* are necessary for the cluster to function
properly and are always added, even if not explicitly specified:

  {minimum_scopes}

If the `--scopes` flag is not specified, the following *default scopes*
are also included:

  {additional_scopes}

If you want to enable all scopes use the 'cloud-platform' scope.

{scopes_help}
""".format(
    minimum_scopes='\n  '.join(constants.MINIMUM_SCOPE_URIS),
    additional_scopes='\n  '.join(constants.ADDITIONAL_DEFAULT_SCOPE_URIS),
    scopes_help=compute_helpers.SCOPES_HELP))

  if include_deprecated:
    _AddDiskArgsDeprecated(parser)
  else:
    _AddDiskArgs(parser)

  # --no-address is an exception to the no negative-flag style guildline to be
  # consistent with gcloud compute instances create --no-address
  parser.add_argument(
      '--no-address',
      action='store_true',
      help="""\
      If provided, the instances in the cluster will not be assigned external
      IP addresses.

      If omitted the instances in the cluster will each be assigned an
      ephemeral external IP address.

      Note: Dataproc VMs need access to the Dataproc API. This can be achieved
      without external IP addresses using Private Google Access
      (https://cloud.google.com/compute/docs/private-google-access).
      """)

  boot_disk_type_detailed_help = """\
      The type of the boot disk. The value must be ``pd-standard'' or
      ``pd-ssd''.
      """
  parser.add_argument(
      '--master-boot-disk-type', help=boot_disk_type_detailed_help)
  parser.add_argument(
      '--worker-boot-disk-type', help=boot_disk_type_detailed_help)
  parser.add_argument(
      '--preemptible-worker-boot-disk-type', help=boot_disk_type_detailed_help)


def _AddDiskArgs(parser):
  """Adds disk related args to the parser."""
  boot_disk_size_detailed_help = """\
      The size of the boot disk. The value must be a
      whole number followed by a size unit of ``KB'' for kilobyte, ``MB''
      for megabyte, ``GB'' for gigabyte, or ``TB'' for terabyte. For example,
      ``10GB'' will produce a 10 gigabyte disk. The minimum size a boot disk
      can have is 10 GB. Disk size must be a multiple of 1 GB.
      """
  parser.add_argument(
      '--master-boot-disk-size',
      type=arg_parsers.BinarySize(lower_bound='10GB'),
      help=boot_disk_size_detailed_help)
  parser.add_argument(
      '--worker-boot-disk-size',
      type=arg_parsers.BinarySize(lower_bound='10GB'),
      help=boot_disk_size_detailed_help)
  parser.add_argument(
      '--preemptible-worker-boot-disk-size',
      type=arg_parsers.BinarySize(lower_bound='10GB'),
      help=boot_disk_size_detailed_help)


def _AddDiskArgsDeprecated(parser):
  """Adds deprecated disk related args to the parser."""
  master_boot_disk_size = parser.add_mutually_exclusive_group()
  worker_boot_disk_size = parser.add_mutually_exclusive_group()

  # Deprecated, to be removed at a future date.
  master_boot_disk_size.add_argument(
      '--master-boot-disk-size-gb',
      action=actions.DeprecationAction(
          '--master-boot-disk-size-gb',
          warn=('The `--master-boot-disk-size-gb` flag is deprecated. '
                'Use `--master-boot-disk-size` flag with "GB" after value.')),
      type=int,
      hidden=True,
      help='Use `--master-boot-disk-size` flag with "GB" after value.')
  worker_boot_disk_size.add_argument(
      '--worker-boot-disk-size-gb',
      action=actions.DeprecationAction(
          '--worker-boot-disk-size-gb',
          warn=('The `--worker-boot-disk-size-gb` flag is deprecated. '
                'Use `--worker-boot-disk-size` flag with "GB" after value.')),
      type=int,
      hidden=True,
      help='Use `--worker-boot-disk-size` flag with "GB" after value.')

  boot_disk_size_detailed_help = """\
      The size of the boot disk. The value must be a
      whole number followed by a size unit of ``KB'' for kilobyte, ``MB''
      for megabyte, ``GB'' for gigabyte, or ``TB'' for terabyte. For example,
      ``10GB'' will produce a 10 gigabyte disk. The minimum size a boot disk
      can have is 10 GB. Disk size must be a multiple of 1 GB.
      """
  master_boot_disk_size.add_argument(
      '--master-boot-disk-size',
      type=arg_parsers.BinarySize(lower_bound='10GB'),
      help=boot_disk_size_detailed_help)
  worker_boot_disk_size.add_argument(
      '--worker-boot-disk-size',
      type=arg_parsers.BinarySize(lower_bound='10GB'),
      help=boot_disk_size_detailed_help)
  parser.add_argument(
      '--preemptible-worker-boot-disk-size',
      type=arg_parsers.BinarySize(lower_bound='10GB'),
      help=boot_disk_size_detailed_help)


def BetaArgsForClusterRef(parser):
  """Register beta-only flags for creating a Dataproc cluster."""
  flags.AddMinCpuPlatformArgs(parser, base.ReleaseTrack.BETA)

  autoscaling_group = parser.add_argument_group(hidden=True)
  flags.AddAutoscalingPolicyResourceArgForCluster(
      autoscaling_group, api_version='v1beta2')

  AddKerberosGroup(parser)

  parser.add_argument(
      '--enable-component-gateway',
      hidden=True,
      action='store_true',
      help="""\
        Enable access to the web UIs of selected components on the cluster
        through the component gateway.
        """)

  parser.add_argument(
      '--max-idle',
      type=arg_parsers.Duration(),
      help="""\
        The duration before cluster is auto-deleted after last job completes,
        such as "2h" or "1d".
        See $ gcloud topic datetimes for information on duration formats.
        """)

  auto_delete_group = parser.add_mutually_exclusive_group()
  auto_delete_group.add_argument(
      '--max-age',
      type=arg_parsers.Duration(),
      help="""\
        The lifespan of the cluster before it is auto-deleted, such as
        "2h" or "1d".
        See $ gcloud topic datetimes for information on duration formats.
        """)

  auto_delete_group.add_argument(
      '--expiration-time',
      type=arg_parsers.Datetime.Parse,
      help="""\
        The time when cluster will be auto-deleted, such as
        "2017-08-29T18:52:51.142Z." See $ gcloud topic datetimes for
        information on time formats.
        """)

  for instance_type in ('master', 'worker'):
    help_msg = """\
      Attaches accelerators (e.g. GPUs) to the {instance_type}
      instance(s).
      """.format(instance_type=instance_type)
    if instance_type == 'worker':
      help_msg += """
      Note:
      No accelerators will be attached to preemptible workers, because
      preemptible VMs do not support accelerators.
      """
    help_msg += """
      *type*::: The specific type (e.g. nvidia-tesla-k80 for nVidia Tesla
      K80) of accelerator to attach to the instances. Use 'gcloud compute
      accelerator-types list' to learn about all available accelerator
      types.

      *count*::: The number of pieces of the accelerator to attach to each
      of the instances. The default value is 1.
      """
    parser.add_argument(
        '--{0}-accelerator'.format(instance_type),
        type=arg_parsers.ArgDict(spec={
            'type': str,
            'count': int,
        }),
        metavar='type=TYPE,[count=COUNT]',
        help=help_msg)

  AddAllocationAffinityGroup(parser)


def GetClusterConfig(args,
                     dataproc,
                     project_id,
                     compute_resources,
                     beta=False,
                     include_deprecated=True):
  """Get dataproc cluster configuration.

  Args:
    args: Arguments parsed from argparse.ArgParser.
    dataproc: Dataproc object that contains client, messages, and resources
    project_id: Dataproc project ID
    compute_resources: compute resource for cluster
    beta: use BETA only features
    include_deprecated: whether to include deprecated args

  Returns:
    cluster_config: Dataproc cluster configuration
  """
  master_accelerator_type = None
  worker_accelerator_type = None
  master_accelerator_count = None
  worker_accelerator_count = None
  if beta:
    if args.master_accelerator:
      master_accelerator_type = args.master_accelerator['type']
      master_accelerator_count = args.master_accelerator.get('count', 1)
    if args.worker_accelerator:
      worker_accelerator_type = args.worker_accelerator['type']
      worker_accelerator_count = args.worker_accelerator.get('count', 1)

  # Resolve non-zonal GCE resources
  # We will let the server resolve short names of zonal resources because
  # if auto zone is requested, we will not know the zone before sending the
  # request
  image_ref = args.image and compute_resources.Parse(
      args.image, params={'project': project_id}, collection='compute.images')
  network_ref = args.network and compute_resources.Parse(
      args.network,
      params={'project': project_id},
      collection='compute.networks')
  subnetwork_ref = args.subnet and compute_resources.Parse(
      args.subnet,
      params={
          'project': project_id,
          'region': properties.VALUES.compute.region.GetOrFail,
      },
      collection='compute.subnetworks')
  timeout_str = str(args.initialization_action_timeout) + 's'
  init_actions = [
      dataproc.messages.NodeInitializationAction(
          executableFile=exe, executionTimeout=timeout_str)
      for exe in (args.initialization_actions or [])
  ]
  # Increase the client timeout for each initialization action.
  args.timeout += args.initialization_action_timeout * len(init_actions)

  expanded_scopes = compute_helpers.ExpandScopeAliases(args.scopes)

  software_config = dataproc.messages.SoftwareConfig(
      imageVersion=args.image_version)

  if include_deprecated:
    master_boot_disk_size_gb = args.master_boot_disk_size_gb
  else:
    master_boot_disk_size_gb = None
  if args.master_boot_disk_size:
    master_boot_disk_size_gb = (api_utils.BytesToGb(args.master_boot_disk_size))

  if include_deprecated:
    worker_boot_disk_size_gb = args.worker_boot_disk_size_gb
  else:
    worker_boot_disk_size_gb = None
  if args.worker_boot_disk_size:
    worker_boot_disk_size_gb = (api_utils.BytesToGb(args.worker_boot_disk_size))

  preemptible_worker_boot_disk_size_gb = (
      api_utils.BytesToGb(args.preemptible_worker_boot_disk_size))

  if args.single_node or args.num_workers == 0:
    # Explicitly specifying --num-workers=0 gives you a single node cluster,
    # but if --num-workers is omitted, args.num_workers is None (not 0), and
    # this property will not be set
    args.properties[constants.ALLOW_ZERO_WORKERS_PROPERTY] = 'true'

  if args.properties:
    software_config.properties = encoding.DictToAdditionalPropertyMessage(
        args.properties, dataproc.messages.SoftwareConfig.PropertiesValue)

  if args.components:
    software_config_cls = dataproc.messages.SoftwareConfig
    software_config.optionalComponents.extend(list(map(
        software_config_cls.OptionalComponentsValueListEntryValuesEnum,
        args.components)))

  gce_cluster_config = dataproc.messages.GceClusterConfig(
      networkUri=network_ref and network_ref.SelfLink(),
      subnetworkUri=subnetwork_ref and subnetwork_ref.SelfLink(),
      internalIpOnly=args.no_address,
      serviceAccount=args.service_account,
      serviceAccountScopes=expanded_scopes,
      zoneUri=properties.VALUES.compute.zone.GetOrFail())

  if beta:
    allocation_affinity = GetAllocationAffinity(args, dataproc)
    gce_cluster_config.allocationAffinity = allocation_affinity

  if args.tags:
    gce_cluster_config.tags = args.tags

  if args.metadata:
    flat_metadata = collections.OrderedDict(
        [(k, v) for d in args.metadata for k, v in d.items()])
    gce_cluster_config.metadata = encoding.DictToAdditionalPropertyMessage(
        flat_metadata, dataproc.messages.GceClusterConfig.MetadataValue)

  master_accelerators = []
  if master_accelerator_type:
    master_accelerators.append(
        dataproc.messages.AcceleratorConfig(
            acceleratorTypeUri=master_accelerator_type,
            acceleratorCount=master_accelerator_count))
  worker_accelerators = []
  if worker_accelerator_type:
    worker_accelerators.append(
        dataproc.messages.AcceleratorConfig(
            acceleratorTypeUri=worker_accelerator_type,
            acceleratorCount=worker_accelerator_count))

  cluster_config = dataproc.messages.ClusterConfig(
      configBucket=args.bucket,
      gceClusterConfig=gce_cluster_config,
      masterConfig=dataproc.messages.InstanceGroupConfig(
          numInstances=args.num_masters,
          imageUri=image_ref and image_ref.SelfLink(),
          machineTypeUri=args.master_machine_type,
          accelerators=master_accelerators,
          diskConfig=GetDiskConfig(
              dataproc,
              args.master_boot_disk_type,
              master_boot_disk_size_gb,
              args.num_master_local_ssds
          )),
      workerConfig=dataproc.messages.InstanceGroupConfig(
          numInstances=args.num_workers,
          imageUri=image_ref and image_ref.SelfLink(),
          machineTypeUri=args.worker_machine_type,
          accelerators=worker_accelerators,
          diskConfig=GetDiskConfig(
              dataproc,
              args.worker_boot_disk_type,
              worker_boot_disk_size_gb,
              args.num_worker_local_ssds,
          )),
      initializationActions=init_actions,
      softwareConfig=software_config,
  )

  if beta:
    if args.kerberos_config_file or args.kerberos_root_principal_password_uri:
      cluster_config.securityConfig = dataproc.messages.SecurityConfig()
      if args.kerberos_config_file:
        cluster_config.securityConfig.kerberosConfig = ParseKerberosConfigFile(
            dataproc, args.kerberos_config_file)
      else:
        kerberos_config = dataproc.messages.KerberosConfig()
        kerberos_config.enableKerberos = True
        if args.kerberos_root_principal_password_uri:
          kerberos_config.rootPrincipalPasswordUri = \
            args.kerberos_root_principal_password_uri
          kerberos_kms_ref = args.CONCEPTS.kerberos_kms_key.Parse()
          kerberos_config.kmsKeyUri = kerberos_kms_ref.RelativeName()
        cluster_config.securityConfig.kerberosConfig = kerberos_config

  if beta:
    if args.enable_component_gateway:
      cluster_config.endpointConfig = dataproc.messages.EndpointConfig(
          enableHttpPortAccess=args.enable_component_gateway)
    if args.autoscaling_policy:
      cluster_config.autoscalingConfig = dataproc.messages.AutoscalingConfig(
          policyUri=args.CONCEPTS.autoscaling_policy.Parse().RelativeName())

    cluster_config.masterConfig.minCpuPlatform = args.master_min_cpu_platform
    cluster_config.workerConfig.minCpuPlatform = args.worker_min_cpu_platform

    lifecycle_config = dataproc.messages.LifecycleConfig()
    changed_config = False
    if args.max_age is not None:
      lifecycle_config.autoDeleteTtl = str(args.max_age) + 's'
      changed_config = True
    if args.expiration_time is not None:
      lifecycle_config.autoDeleteTime = times.FormatDateTime(
          args.expiration_time)
      changed_config = True
    if args.max_idle is not None:
      lifecycle_config.idleDeleteTtl = str(args.max_idle) + 's'
      changed_config = True
    if changed_config:
      cluster_config.lifecycleConfig = lifecycle_config

  if hasattr(args.CONCEPTS, 'kms_key'):
    kms_ref = args.CONCEPTS.kms_key.Parse()
    if kms_ref:
      encryption_config = dataproc.messages.EncryptionConfig()
      encryption_config.gcePdKmsKeyName = kms_ref.RelativeName()
      cluster_config.encryptionConfig = encryption_config
    else:
      # Did user use any gce-pd-kms-key flags?
      for keyword in [
          'gce-pd-kms-key', 'gce-pd-kms-key-project', 'gce-pd-kms-key-location',
          'gce-pd-kms-key-keyring'
      ]:
        if getattr(args, keyword.replace('-', '_'), None):
          raise exceptions.ArgumentError(
              '--gce-pd-kms-key was not fully specified.')

  # Secondary worker group is optional. However, users may specify
  # future pVMs configuration at creation time.
  if (args.num_preemptible_workers is not None or
      preemptible_worker_boot_disk_size_gb is not None or
      args.preemptible_worker_boot_disk_type is not None or
      args.num_preemptible_worker_local_ssds is not None or
      (beta and args.worker_min_cpu_platform is not None)):
    cluster_config.secondaryWorkerConfig = (
        dataproc.messages.InstanceGroupConfig(
            numInstances=args.num_preemptible_workers,
            diskConfig=GetDiskConfig(
                dataproc,
                args.preemptible_worker_boot_disk_type,
                preemptible_worker_boot_disk_size_gb,
                args.num_preemptible_worker_local_ssds,
            )))
    if beta and args.worker_min_cpu_platform:
      cluster_config.secondaryWorkerConfig.minCpuPlatform = (
          args.worker_min_cpu_platform)

  return cluster_config


def GetDiskConfig(dataproc,
                  boot_disk_type,
                  boot_disk_size,
                  num_local_ssds):
  """Get dataproc cluster disk configuration.

  Args:
    dataproc: Dataproc object that contains client, messages, and resources
    boot_disk_type: Type of the boot disk
    boot_disk_size: Size of the boot disk
    num_local_ssds: Number of the Local SSDs

  Returns:
    disk_config: Dataproc cluster disk configuration
  """

  return dataproc.messages.DiskConfig(
      bootDiskType=boot_disk_type,
      bootDiskSizeGb=boot_disk_size,
      numLocalSsds=num_local_ssds)


def CreateCluster(dataproc, cluster, is_async, timeout):
  """Create a cluster.

  Args:
    dataproc: Dataproc object that contains client, messages, and resources
    cluster: Cluster to create
    is_async: Whether to wait for the operation to complete
    timeout: Timeout used when waiting for the operation to complete

  Returns:
    Created cluster, or None if async
  """
  # Get project id and region.
  cluster_ref = util.ParseCluster(cluster.clusterName, dataproc)
  request_id = util.GetUniqueId()
  request = dataproc.messages.DataprocProjectsRegionsClustersCreateRequest(
      cluster=cluster,
      projectId=cluster_ref.projectId,
      region=cluster_ref.region,
      requestId=request_id)
  operation = dataproc.client.projects_regions_clusters.Create(request)

  if is_async:
    log.status.write('Creating [{0}] with operation [{1}].'.format(
        cluster_ref, operation.name))
    return

  operation = util.WaitForOperation(
      dataproc,
      operation,
      message='Waiting for cluster creation operation',
      timeout_s=timeout)

  get_request = dataproc.messages.DataprocProjectsRegionsClustersGetRequest(
      projectId=cluster_ref.projectId,
      region=cluster_ref.region,
      clusterName=cluster_ref.clusterName)
  cluster = dataproc.client.projects_regions_clusters.Get(get_request)
  if cluster.status.state == (
      dataproc.messages.ClusterStatus.StateValueValuesEnum.RUNNING):

    zone_uri = cluster.config.gceClusterConfig.zoneUri
    zone_short_name = zone_uri.split('/')[-1]

    # Log the URL of the cluster
    log.CreatedResource(
        cluster_ref,
        # Also indicate which zone the cluster was placed in. This is helpful
        # if the server picked a zone (auto zone)
        details='Cluster placed in zone [{0}]'.format(zone_short_name))
  else:
    log.error('Create cluster failed!')
    if operation.details:
      log.error('Details:\n' + operation.details)
  return cluster


def DeleteGeneratedLabels(cluster, dataproc):
  """Filter out Dataproc-generated cluster labels.

  Args:
    cluster: Cluster to filter
    dataproc: Dataproc object that contains client, messages, and resources
  """
  # Filter out Dataproc-generated labels.
  if cluster.labels:
    labels = encoding.MessageToPyValue(cluster.labels)
    labels_to_delete = []
    for label in labels:
      if label.startswith(GENERATED_LABEL_PREFIX):
        labels_to_delete.append(label)
    for label in labels_to_delete:
      del labels[label]
    if not labels:
      cluster.labels = None
    else:
      cluster.labels = encoding.DictToAdditionalPropertyMessage(
          labels, dataproc.messages.Cluster.LabelsValue)


def AddAllocationAffinityGroup(parser):
  """Adds the argument group to handle allocation affinity configurations."""
  group = parser.add_group(help='Manage the configuration of desired'
                                'allocation which this instance could'
                                'take capacity from.'
                          )
  group.add_argument(
      '--allocation-affinity',
      choices=['any', 'none', 'specific'],
      default='any',
      hidden=True,
      help="""
Specifies the configuration of desired allocation which this instance could
take capacity from. Choices are 'any', 'none' and 'specific', default is 'any'.
""")
  group.add_argument(
      '--allocation-label',
      type=arg_parsers.ArgDict(spec={
          'key': str,
          'value': str,
      }),
      hidden=True,
      help="""
The key and values of the label of the allocation resource. Required if the
value of `--allocation-affinity` is `specific`.

*key*::: The label key of allocation resource.

*value*::: The label value of allocation resource.
""")


def GetAllocationAffinity(args, client):
  """Returns the message of allocation affinity for the instance."""
  if not args.IsSpecified('allocation_affinity'):
    return None

  type_msgs = (client.messages.
               AllocationAffinity.ConsumeAllocationTypeValueValuesEnum)

  if args.allocation_affinity == 'none':
    allocation_type = type_msgs.NO_ALLOCATION
    allocation_key = None
    allocation_values = []
  elif args.allocation_affinity == 'specific':
    allocation_type = type_msgs.SPECIFIC_ALLOCATION
    # Currently, the key is fixed and the value is the name of the allocation.
    # The value being a repeated field is reserved for future use when user
    # can specify more than one allocation names from which the Vm can take
    # capacity from.
    allocation_key = args.allocation_label.get('key', None)
    allocation_values = [args.allocation_label.get('value', None)]
  else:
    allocation_type = type_msgs.ANY_ALLOCATION
    allocation_key = None
    allocation_values = []

  return client.messages.AllocationAffinity(
      consumeAllocationType=allocation_type,
      key=allocation_key,
      values=allocation_values)


def AddKerberosGroup(parser):
  """Adds the argument group to handle Kerberos configurations."""
  kerberos_group = parser.add_argument_group(
      mutex=True,
      help='Specifying these flags will enable Kerberos for the cluster.')
  # Not mutually exclusive
  kerberos_flag_group = kerberos_group.add_argument_group()
  kerberos_flag_group.add_argument(
      '--kerberos-root-principal-password-uri',
      required=True,
      help="""\
        Google Cloud Storage URI of a KMS encrypted file containing
        the root principal password. Must be a URL beginning with 'gs://'.
        """)
  # Add kerberos-kms-key args
  kerberos_kms_flag_overrides = \
      {'kms-key': '--kerberos-kms-key',
       'kms-keyring': '--kerberos-kms-key-keyring',
       'kms-location': '--kerberos-kms-key-location',
       'kms-project': '--kerberos-kms-key-project'}
  kms_resource_args.AddKmsKeyResourceArg(
      kerberos_flag_group,
      'password',
      flag_overrides=kerberos_kms_flag_overrides,
      required=True,
      name='--kerberos-kms-key')

  kerberos_group.add_argument(
      '--kerberos-config-file',
      help="""\
Path to a YAML (or JSON) file containing the configuration for Kerberos on the
cluster. If you pass `-` as the value of the flag the file content will be read
from stdin.

The YAML file is formatted as follows:

```
  # Optional. Flag to indicate whether to Kerberize the cluster.
  # The default value is true.
  enable_kerberos: true

  # Required. The Google Cloud Storage URI of a KMS encrypted file
  # containing the root principal password.
  root_principal_password_uri: gs://bucket/password.encrypted

  # Required. The URI of the KMS key used to encrypt various
  # sensitive files.
  kms_key_uri:
    projects/myproject/locations/global/keyRings/mykeyring/cryptoKeys/my-key

  # Configuration of SSL encryption. If specified, all sub-fields
  # are required. Otherwise, Dataproc will provide a self-signed
  # certificate and generate the passwords.
  ssl:
    # Optional. The Google Cloud Storage URI of the keystore file.
    keystore_uri: gs://bucket/keystore.jks

    # Optional. The Google Cloud Storage URI of a KMS encrypted
    # file containing the password to the keystore.
    keystore_password_uri: gs://bucket/keystore_password.encrypted

    # Optional. The Google Cloud Storage URI of a KMS encrypted
    # file containing the password to the user provided key.
    key_password_uri: gs://bucket/key_password.encrypted

    # Optional. The Google Cloud Storage URI of the truststore
    # file.
    truststore_uri: gs://bucket/truststore.jks

    # Optional. The Google Cloud Storage URI of a KMS encrypted
    # file containing the password to the user provided
    # truststore.
    truststore_password_uri:
      gs://bucket/truststore_password.encrypted

  # Configuration of cross realm trust.
  cross_realm_trust:
    # Optional. The remote realm the Dataproc on-cluster KDC will
    # trust, should the user enable cross realm trust.
    realm: REMOTE.REALM

    # Optional. The KDC (IP or hostname) for the remote trusted
    # realm in a cross realm trust relationship.
    kdc: kdc.remote.realm

    # Optional. The admin server (IP or hostname) for the remote
    # trusted realm in a cross realm trust relationship.
    admin_server: admin-server.remote.realm

    # Optional. The Google Cloud Storage URI of a KMS encrypted
    # file containing the shared password between the on-cluster
    # Kerberos realm and the remote trusted realm, in a cross
    # realm trust relationship.
    shared_password_uri:
      gs://bucket/cross-realm.password.encrypted

  # Optional. The Google Cloud Storage URI of a KMS encrypted file
  # containing the master key of the KDC database.
  kdc_db_key_uri: gs://bucket/kdc_db_key.encrypted

  # Optional. The lifetime of the ticket granting ticket, in
  # hours. If not specified, or user specifies 0, then default
  # value 10 will be used.
  tgt_lifetime_hours: 1
```
        """)


def ParseKerberosConfigFile(dataproc, kerberos_config_file):
  """Parse a kerberos-config-file into the KerberosConfig message."""
  data = console_io.ReadFromFileOrStdin(kerberos_config_file, binary=False)
  try:
    kerberos_config_data = yaml.load(data)
  except Exception as e:
    raise exceptions.ParseError('Cannot parse YAML:[{0}]'.format(e))

  ssl_config = kerberos_config_data.get('ssl', {})
  keystore_uri = ssl_config.get('keystore_uri')
  truststore_uri = ssl_config.get('truststore_uri')
  keystore_password_uri = ssl_config.get('keystore_password_uri')
  key_password_uri = ssl_config.get('key_password_uri')
  truststore_password_uri = ssl_config.get('truststore_password_uri')

  cross_realm_trust_config = kerberos_config_data.get('cross_realm_trust', {})
  cross_realm_trust_realm = cross_realm_trust_config.get('realm')
  cross_realm_trust_kdc = cross_realm_trust_config.get('kdc')
  cross_realm_trust_admin_server = cross_realm_trust_config.get('admin_server')
  cross_realm_trust_shared_password_uri = cross_realm_trust_config.get(
      'shared_password_uri')
  kerberos_config_msg = dataproc.messages.KerberosConfig(
      # Unless user explicitly disable kerberos in kerberos config,
      # consider the existence of the kerberos config is enabling
      # kerberos, explicitly or implicitly.
      enableKerberos=kerberos_config_data.get('enable_kerberos', True),
      rootPrincipalPasswordUri=kerberos_config_data.get(
          'root_principal_password_uri'),
      kmsKeyUri=kerberos_config_data.get('kms_key_uri'),
      kdcDbKeyUri=kerberos_config_data.get('kdc_db_key_uri'),
      tgtLifetimeHours=kerberos_config_data.get('tgt_lifetime_hours'),
      keystoreUri=keystore_uri,
      keystorePasswordUri=keystore_password_uri,
      keyPasswordUri=key_password_uri,
      truststoreUri=truststore_uri,
      truststorePasswordUri=truststore_password_uri,
      crossRealmTrustRealm=cross_realm_trust_realm,
      crossRealmTrustKdc=cross_realm_trust_kdc,
      crossRealmTrustAdminServer=cross_realm_trust_admin_server,
      crossRealmTrustSharedPasswordUri=cross_realm_trust_shared_password_uri)

  return kerberos_config_msg
