# -*- coding: utf-8 -*- #
# Copyright 2016 Google Inc. All Rights Reserved.
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

"""Flags and helpers for the container related commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import constants as compute_constants
from googlecloudsdk.api_lib.container import api_adapter
from googlecloudsdk.api_lib.container import util
from googlecloudsdk.calliope import actions
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.container import constants
from googlecloudsdk.core import log
from googlecloudsdk.core import properties


def AddBasicAuthFlags(parser):
  """Adds basic auth flags to the given parser.

  Basic auth flags are: --username, --enable-basic-auth, and --password.

  Args:
    parser: A given parser.
  """
  basic_auth_group = parser.add_group(help='Basic auth')
  username_group = basic_auth_group.add_group(
      mutex=True, help='Options to specify the username.')
  username_help_text = """\
The user name to use for basic auth for the cluster. Use `--password` to specify
a password; if not, the server will randomly generate one."""
  username_group.add_argument('--username', '-u', help=username_help_text)

  enable_basic_auth_help_text = """\
Enable basic (username/password) auth for the cluster.  `--enable-basic-auth` is
an alias for `--username=admin`; `--no-enable-basic-auth` is an alias for
`--username=""`. Use `--password` to specify a password; if not, the server will
randomly generate one. For cluster versions before 1.12, if neither
`--enable-basic-auth` nor `--username` is specified, `--enable-basic-auth` will
default to `true`. After 1.12, `--enable-basic-auth` will default to `false`."""
  username_group.add_argument(
      '--enable-basic-auth',
      help=enable_basic_auth_help_text,
      action='store_true',
      default=None)

  basic_auth_group.add_argument(
      '--password',
      help='The password to use for cluster auth. Defaults to a '
      'server-specified randomly-generated string.')


def MungeBasicAuthFlags(args):
  """Munges flags associated with basic auth.

  If --enable-basic-auth is specified, converts it --username value, and checks
  that --password is only specified if it makes sense.

  Args:
    args: an argparse namespace. All the arguments that were provided to this
      command invocation.

  Raises:
    util.Error, if flags conflict.
  """
  if args.IsSpecified('enable_basic_auth'):
    if not args.enable_basic_auth:
      args.username = ''
    else:
      args.username = 'admin'
  if not args.username and args.IsSpecified('password'):
    raise util.Error(constants.USERNAME_PASSWORD_ERROR_MSG)


# TODO(b/28318474): move flags common across commands here.
def AddImageTypeFlag(parser, target):
  """Adds a --image-type flag to the given parser."""
  help_text = """\
The image type to use for the {target}. Defaults to server-specified.

Image Type specifies the base OS that the nodes in the {target} will run on.
If an image type is specified, that will be assigned to the {target} and all
future upgrades will use the specified image type. If it is not specified the
server will pick the default image type.

The default image type and the list of valid image types are available
using the following command.

  $ gcloud container get-server-config
""".format(target=target)

  parser.add_argument('--image-type', help=help_text)


def AddImageFlag(parser, hidden=False):
  """Adds an --image flag to the given parser.

  Args:
    parser: A given parser.
    hidden: if true, suppress help text for this option
  """

  help_text = """\
A specific image to use on the new instances.
"""

  parser.add_argument('--image', help=help_text, hidden=hidden)


def AddImageProjectFlag(parser, hidden=False):
  """Adds an --image-project flag to the given parser.

  Args:
    parser: A given parser.
    hidden: if true, suppresses help text for this option.
  """
  help_text = """/
A specific project from which contains the os image or image family.  This is
required when using --image-type=CUSTOM.
"""

  parser.add_argument('--image-project', help=help_text, hidden=hidden)


def AddImageFamilyFlag(parser, hidden=False):
  """Adds an --image-family flag to the given parser.

  Args:
    parser: A given parser.
    hidden: if true, suppresses help text for this option.
  """

  help_text = """/
A specific image-family from which the most recent image is used on new
instances.  If both image and image family are specified, the image must be in
the image family, and the image is used.
"""
  parser.add_argument('--image-family', help=help_text, hidden=hidden)


def AddNodeVersionFlag(parser, hidden=False):
  """Adds a --node-version flag to the given parser."""
  help_text = """\
The Kubernetes version to use for nodes. Defaults to server-specified.

The default Kubernetes version is available using the following command.

  $ gcloud container get-server-config
"""

  return parser.add_argument('--node-version', help=help_text, hidden=hidden)


def AddClusterVersionFlag(parser, suppressed=False, help=None):  # pylint: disable=redefined-builtin
  """Adds a --cluster-version flag to the given parser."""
  if help is None:
    help = """\
The Kubernetes version to use for the master and nodes. Defaults to
server-specified.

The default Kubernetes version is available using the following command.

  $ gcloud container get-server-config
"""

  return parser.add_argument('--cluster-version', help=help, hidden=suppressed)


def AddClusterAutoscalingFlags(parser, update_group=None, hidden=False):
  """Adds autoscaling related flags to parser.

  Autoscaling related flags are: --enable-autoscaling
  --min-nodes --max-nodes flags.

  Args:
    parser: A given parser.
    update_group: An optional group of mutually exclusive flag options
        to which an --enable-autoscaling flag is added.
    hidden: If true, suppress help text for added options.
  Returns:
    Argument group for autoscaling flags.
  """

  group = parser.add_argument_group('Cluster autoscaling')
  autoscaling_group = group if update_group is None else update_group
  autoscaling_group.add_argument(
      '--enable-autoscaling',
      default=None,
      help="""\
Enables autoscaling for a node pool.

Enables autoscaling in the node pool specified by --node-pool or
the default node pool if --node-pool is not provided.""",
      hidden=hidden,
      action='store_true')
  group.add_argument(
      '--max-nodes',
      help="""\
Maximum number of nodes in the node pool.

Maximum number of nodes to which the node pool specified by --node-pool
(or default node pool if unspecified) can scale. Ignored unless
--enable-autoscaling is also specified.""",
      hidden=hidden,
      type=int)
  group.add_argument(
      '--min-nodes',
      help="""\
Minimum number of nodes in the node pool.

Minimum number of nodes to which the node pool specified by --node-pool
(or default node pool if unspecified) can scale. Ignored unless
--enable-autoscaling is also specified.""",
      hidden=hidden,
      type=int)
  return group


def AddNodePoolAutoprovisioningFlag(parser, hidden=True):
  """Adds --enable-autoprovisioning flag for node-pool to parser.

  Args:
    parser: A given parser.
    hidden: If true, suppress help text for added options.
  """
  parser.add_argument(
      '--enable-autoprovisioning',
      help="""\
Enables Cluster Autoscaler to treat the node pool as if it was autoprovisioned.

Cluster Autoscaler will be able to delete the node pool if it's unneeded.""",
      hidden=hidden,
      default=None,
      action='store_true')


def AddLocalSSDFlag(parser, suppressed=False, help_text=''):
  """Adds a --local-ssd-count flag to the given parser."""
  help_text += """\
The number of local SSD disks to provision on each node.

Local SSDs have a fixed 375 GB capacity per device. The number of disks that
can be attached to an instance is limited by the maximum number of disks
available on a machine, which differs by compute zone. See
https://cloud.google.com/compute/docs/disks/local-ssd for more information."""
  parser.add_argument(
      '--local-ssd-count',
      help=help_text,
      hidden=suppressed,
      type=int,
      default=0)


def AddAcceleratorArgs(parser):
  """Adds Accelerator-related args."""
  parser.add_argument(
      '--accelerator',
      type=arg_parsers.ArgDict(
          spec={
              'type': str,
              'count': int,
          },
          required_keys=['type'],
          max_length=2),
      metavar='type=TYPE,[count=COUNT]',
      help="""\
      Attaches accelerators (e.g. GPUs) to all nodes.

      *type*::: (Required) The specific type (e.g. nvidia-tesla-k80 for nVidia Tesla K80)
      of accelerator to attach to the instances. Use ```gcloud compute
      accelerator-types list``` to learn about all available accelerator types.

      *count*::: (Optional) The number of accelerators to attach to the
      instances. The default value is 1.
      """)


def AddAutoprovisioningFlags(parser, hidden=False):
  """Adds node autoprovisioning related flags to parser.

  Autoprovisioning related flags are: --enable-autoprovisioning
  --min-cpu --max-cpu --min-memory --max-memory flags.

  Args:
    parser: A given parser.
    hidden: If true, suppress help text for added options.
  """

  group = parser.add_argument_group('Node autoprovisioning', hidden=hidden)
  group.add_argument(
      '--enable-autoprovisioning',
      required=True,
      default=None,
      help="""\
Enables  node autoprovisioning for a cluster.

Cluster Autoscaler will be able to create new node pools. Requires maximum CPU
and memory limits to be specified.""",
      hidden=hidden,
      action='store_true')

  limits_group = group.add_mutually_exclusive_group()
  limits_group.add_argument(
      '--autoprovisioning-config-file',
      type=arg_parsers.BufferedFileInput(),
      hidden=hidden,
      help="""\
Path of the JSON/YAML file which contains information about the
cluster's autoscaling configuration. Currently it only contains
a list of resource limits of the cluster.

Each resource limits definition contains three fields:
resourceType, maximum and minimum.
Resource type can be "cpu", "memory" or an accelerator (e.g.
"nvidia-tesla-k80" for nVidia Tesla K80). Use gcloud compute accelerator-types
list to learn about available accelerator types.
Maximum is the maximum allowed amount with the unit of the resource.
Minimum is the minimum allowed amount with the unit of the resource.
""")

  from_flags_group = limits_group.add_argument_group('Flags to configure '
                                                     'resource limits:')
  from_flags_group.add_argument(
      '--max-cpu',
      required=True,
      help="""\
Maximum number of cores in the cluster.

Maximum number of cores to which the cluster can scale.""",
      hidden=hidden,
      type=int)
  from_flags_group.add_argument(
      '--min-cpu',
      help="""\
Minimum number of cores in the cluster.

Minimum number of cores to which the cluster can scale.""",
      hidden=hidden,
      type=int)
  from_flags_group.add_argument(
      '--max-memory',
      required=True,
      help="""\
Maximum memory in the cluster.

Maximum number of gigabytes of memory to which the cluster can scale.""",
      hidden=hidden,
      type=int)
  from_flags_group.add_argument(
      '--min-memory',
      help="""\
Minimum memory in the cluster.

Minimum number of gigabytes of memory to which the cluster can scale.""",
      hidden=hidden,
      type=int)
  accelerator_group = from_flags_group.add_argument_group(
      'Arguments to set limits on accelerators:')
  accelerator_group.add_argument(
      '--max-accelerator',
      type=arg_parsers.ArgDict(spec={
          'type': str,
          'count': int,
      }, required_keys=['type', 'count'], max_length=2),
      required=True,
      metavar='type=TYPE,count=COUNT',
      hidden=hidden,
      help="""\
Sets maximum limit for a single type of accelerators (e.g. GPUs) in cluster.

*type*::: (Required) The specific type (e.g. nvidia-tesla-k80 for nVidia Tesla K80)
of accelerator for which the limit is set. Use ```gcloud compute
accelerator-types list``` to learn about all available accelerator types.

*count*::: (Required) The maximum number of accelerators
to which the cluster can be scaled.
""")
  accelerator_group.add_argument(
      '--min-accelerator',
      type=arg_parsers.ArgDict(spec={
          'type': str,
          'count': int,
      }, required_keys=['type', 'count'], max_length=2),
      metavar='type=TYPE,count=COUNT',
      hidden=hidden,
      help="""\
Sets minimum limit for a single type of accelerators (e.g. GPUs) in cluster. Defaults
to 0 for all accelerator types if it isn't set.

*type*::: (Required) The specific type (e.g. nvidia-tesla-k80 for nVidia Tesla K80)
of accelerator for which the limit is set. Use ```gcloud compute
accelerator-types list``` to learn about all available accelerator types.

*count*::: (Required) The minimum number of accelerators
to which the cluster can be scaled.
""")


def AddEnableBinAuthzFlag(parser, hidden=False):
  """Adds a --enable-binauthz flag to parser."""
  help_text = """Enable Binary Authorization for this cluster."""
  parser.add_argument(
      '--enable-binauthz',
      action='store_true',
      default=None,
      help=help_text,
      hidden=hidden,
  )


def AddZoneAndRegionFlags(parser):
  """Adds the --zone and --region flags to the parser."""
  # TODO(b/33343238): Remove the short form of the zone flag.
  # TODO(b/18105938): Add zone prompting
  group = parser.add_mutually_exclusive_group()
  group.add_argument(
      '--zone',
      '-z',
      help='Compute zone (e.g. us-central1-a) for the cluster',
      action=actions.StoreProperty(properties.VALUES.compute.zone))
  group.add_argument(
      '--region',
      help='Compute region (e.g. us-central1) for the cluster.')


def AddAsyncFlag(parser):
  """Adds the --async flags to the given parser."""
  parser.add_argument(
      '--async',
      action='store_true',
      default=None,
      help='Don\'t wait for the operation to complete.')


def AddEnableKubernetesAlphaFlag(parser):
  """Adds a --enable-kubernetes-alpha flag to parser."""
  help_text = """\
Enable Kubernetes alpha features on this cluster. Selecting this
option will result in the cluster having all Kubernetes alpha API groups and
features turned on. Cluster upgrades (both manual and automatic) will be
disabled and the cluster will be automatically deleted after 30 days.

Alpha clusters are not covered by the Kubernetes Engine SLA and should not be
used for production workloads."""
  parser.add_argument(
      '--enable-kubernetes-alpha',
      action='store_true',
      help=help_text)


def AddEnableStackdriverKubernetesFlag(parser):
  """Adds a --enable-stackdriver-kubernetes flag to parser."""
  help_text = """Enable Stackdriver Kubernetes monitoring and logging."""
  parser.add_argument(
      '--enable-stackdriver-kubernetes', action='store_true', help=help_text)


def AddNodeLabelsFlag(parser, for_node_pool=False):
  """Adds a --node-labels flag to the given parser."""
  if for_node_pool:
    help_text = """\
Applies the given kubernetes labels on all nodes in the new node-pool. Example:

  $ {command} node-pool-1 --cluster=example-cluster --node-labels=label1=value1,label2=value2
"""
  else:
    help_text = """\
Applies the given kubernetes labels on all nodes in the new node-pool. Example:

  $ {command} example-cluster --node-labels=label-a=value1,label-2=value2
"""
  help_text += """
New nodes, including ones created by resize or recreate, will have these labels
on the kubernetes API node object and can be used in nodeSelectors.
See [](http://kubernetes.io/docs/user-guide/node-selection/) for examples.

Note that kubernetes labels, intended to associate cluster components
and resources with one another and manage resource lifecycles, are different
from Kubernetes Engine labels that are used for the purpose of tracking billing
and usage information."""

  parser.add_argument(
      '--node-labels',
      metavar='NODE_LABEL',
      type=arg_parsers.ArgDict(),
      help=help_text)


def AddLocalSSDAndLocalSSDVolumeConfigsFlag(parser, for_node_pool=False,
                                            suppressed=False):
  """Adds the --local-ssd-count and --local-ssd-volumes flags to the parser."""
  help_text = """\
--local-ssd-volumes enables the ability to request local SSD with variable count, interfaces, and format\n
--local-ssd-count is the equivalent of using --local-ssd-volumes with type=scsi,format=fs

"""
  group = parser.add_mutually_exclusive_group()
  AddLocalSSDVolumeConfigsFlag(group, for_node_pool=for_node_pool,
                               help_text=help_text)
  AddLocalSSDFlag(group, suppressed=suppressed, help_text=help_text)


def AddLocalSSDVolumeConfigsFlag(parser, for_node_pool=False, help_text=''):
  """Adds a --local-ssd-volumes flag to the given parser."""
  help_text += """\
Adds the requested local SSDs on all nodes in default node-pool(s) in new cluster. Example:

  $ {{command}} {0} --local-ssd-volumes count=2,type=nvme,format=fs

'count' must be between 1-8\n
'type' must be either scsi or nvme\n
'format' must be either fs or block

New nodes, including ones created by resize or recreate, will have these local SSDs.

Local SSDs have a fixed 375 GB capacity per device. The number of disks that
can be attached to an instance is limited by the maximum number of disks
available on a machine, which differs by compute zone. See
https://cloud.google.com/compute/docs/disks/local-ssd for more information.
""".format('node-pool-1 --cluster=example-cluster' if for_node_pool else
           'example_cluster')
  count_validator = arg_parsers.RegexpValidator(
      r'^[1-8]$', 'Count must be a number between 1 and 8')
  type_validator = arg_parsers.RegexpValidator(
      r'^(scsi|nvme)$', 'Type must be either "scsi" or "nvme"')
  format_validator = arg_parsers.RegexpValidator(
      r'^(fs|block)$', 'Format must be either "fs" or "block"')
  parser.add_argument(
      '--local-ssd-volumes',
      metavar='[count=COUNT],[type=TYPE],[format=FORMAT]',
      type=arg_parsers.ArgDict(
          spec={
              'count': count_validator,
              'type': type_validator,
              'format': format_validator,
          },
          required_keys=['count', 'type', 'format'],
          max_length=3),
      action='append',
      help=help_text)


def AddNodeTaintsFlag(parser, for_node_pool=False, hidden=False):
  """Adds a --node-taints flag to the given parser."""
  if for_node_pool:
    help_text = """\
Applies the given kubernetes taints on all nodes in the new node-pool, which can be used with tolerations for pod scheduling. Example:

  $ {command} node-pool-1 --cluster=example-cluster --node-taints=key1=val1:NoSchedule,key2=val2:PreferNoSchedule
"""
  else:
    help_text = """\
Applies the given kubernetes taints on all nodes in default node-pool(s) in new cluster, which can be used with tolerations for pod scheduling. Example:

  $ {command} example-cluster --node-taints=key1=val1:NoSchedule,key2=val2:PreferNoSchedule
"""
  help_text += """
Note, this feature uses `gcloud beta` commands. To use gcloud beta commands,
you must configure `gcloud` to use the v1beta1 API as described here: https://cloud.google.com/kubernetes-engine/docs/reference/api-organization#beta.
To read more about node-taints, see https://cloud.google.com/kubernetes-engine/docs/node-taints.
"""

  parser.add_argument(
      '--node-taints',
      metavar='NODE_TAINT',
      type=arg_parsers.ArgDict(),
      help=help_text,
      hidden=hidden)


def AddPreemptibleFlag(parser, for_node_pool=False, suppressed=False):
  """Adds a --preemptible flag to parser."""
  if for_node_pool:
    help_text = """\
Create nodes using preemptible VM instances in the new nodepool.

  $ {command} node-pool-1 --cluster=example-cluster --preemptible
"""
  else:
    help_text = """\
Create nodes using preemptible VM instances in the new cluster.

  $ {command} example-cluster --preemptible
"""
  help_text += """
New nodes, including ones created by resize or recreate, will use preemptible
VM instances. See https://cloud.google.com/kubernetes-engine/docs/preemptible-vm
for more information on how to use Preemptible VMs with Kubernetes Engine."""

  parser.add_argument(
      '--preemptible',
      action='store_true',
      help=help_text,
      hidden=suppressed)


def AddNodePoolNameArg(parser, help_text):
  """Adds a name flag to the given parser.

  Args:
    parser: A given parser.
    help_text: The help text describing the operation being performed.
  """
  parser.add_argument('name', metavar='NAME', help=help_text)


def AddNodePoolClusterFlag(parser, help_text):
  """Adds a --cluster flag to the parser.

  Args:
    parser: A given parser.
    help_text: The help text describing usage of the --cluster flag being set.
  """
  parser.add_argument(
      '--cluster',
      help=help_text,
      action=actions.StoreProperty(properties.VALUES.container.cluster))


def AddEnableAutoRepairFlag(parser, for_node_pool=False, for_create=False):
  """Adds a --enable-autorepair flag to parser."""
  if for_node_pool:
    help_text = """\
Enable node autorepair feature for a node-pool.

  $ {command} node-pool-1 --cluster=example-cluster --enable-autorepair
"""
    if for_create:
      help_text += """
Node autorepair is enabled by default for node pools using COS as a base image,
use --no-enable-autorepair to disable.
"""
  else:
    help_text = """\
Enable node autorepair feature for a cluster's default node-pool(s).

  $ {command} example-cluster --enable-autorepair
"""
    if for_create:
      help_text += """
Node autorepair is enabled by default for clusters using COS as a base image,
use --no-enable-autorepair to disable.
"""
  help_text += """
See https://cloud.google.com/kubernetes-engine/docs/how-to/node-auto-repair for \
more info."""

  parser.add_argument(
      '--enable-autorepair', action='store_true', default=None,
      help=help_text)


def AddEnableAutoUpgradeFlag(parser, for_node_pool=False, suppressed=False):
  """Adds a --enable-autoupgrade flag to parser."""
  if for_node_pool:
    help_text = """\
Sets autoupgrade feature for a node-pool.

  $ {command} node-pool-1 --cluster=example-cluster --enable-autoupgrade
"""
  else:
    help_text = """\
Sets autoupgrade feature for a cluster's default node-pool(s).

  $ {command} example-cluster --enable-autoupgrade
"""
  help_text += """
See https://cloud.google.com/kubernetes-engine/docs/node-management for more \
info."""

  parser.add_argument(
      '--enable-autoupgrade',
      action='store_true',
      default=None,
      help=help_text,
      hidden=suppressed)


def AddTagsFlag(parser, help_text):
  """Adds a --tags to the given parser."""
  parser.add_argument(
      '--tags',
      metavar='TAG',
      type=arg_parsers.ArgList(min_length=1),
      help=help_text)


def AddMasterAuthorizedNetworksFlags(parser, enable_group_for_update=None):
  """Adds Master Authorized Networks related flags to parser.

  Master Authorized Networks related flags are:
  --enable-master-authorized-networks --master-authorized-networks.

  Args:
    parser: A given parser.
    enable_group_for_update: An optional group of mutually exclusive flag
        options to which an --enable-master-authorized-networks flag is added
        in an update command.
  """
  if enable_group_for_update is None:
    # Flags are being added to the same group.
    master_flag_group = parser.add_argument_group('Master Authorized Networks')
    enable_flag_group = master_flag_group
  else:
    # Flags are being added to different groups, so the new one should have no
    # help text (has only one arg).
    master_flag_group = parser.add_argument_group('')
    enable_flag_group = enable_group_for_update

  enable_flag_group.add_argument(
      '--enable-master-authorized-networks',
      default=None,
      help="""\
Allow only specified set of CIDR blocks (specified by the
`--master-authorized-networks` flag) to connect to Kubernetes master through
HTTPS. Besides these blocks, the following have access as well:\n
  1) The private network the cluster connects to if
  `--enable-private-nodes` is specified.
  2) Google Compute Engine Public IPs if `--enable-private-nodes` is not
  specified.\n
Use `--no-enable-master-authorized-networks` to disable. When disabled, public
internet (0.0.0.0/0) is allowed to connect to Kubernetes master through HTTPS.
""",
      action='store_true')
  master_flag_group.add_argument(
      '--master-authorized-networks',
      type=arg_parsers.ArgList(min_length=1),
      metavar='NETWORK',
      help='The list of CIDR blocks (up to {max}) that are allowed to connect '
      'to Kubernetes master through HTTPS. Specified in CIDR notation (e.g. '
      '1.2.3.4/30). Can not be specified unless '
      '`--enable-master-authorized-networks` is also specified.'.format(
          max=api_adapter.MAX_AUTHORIZED_NETWORKS_CIDRS))


def AddNetworkPolicyFlags(parser, hidden=False):
  """Adds --enable-network-policy flags to parser."""
  parser.add_argument(
      '--enable-network-policy',
      action='store_true',
      default=None,
      hidden=hidden,
      help='Enable network policy enforcement for this cluster. If you are '
      'enabling network policy on an existing cluster the network policy '
      'addon must first be enabled on the master by using '
      '--update-addons=NetworkPolicy=ENABLED flag.')


def AddPrivateClusterFlags(parser, with_deprecated=False, with_alpha=False):
  """Adds flags related to private clusters to parser."""
  group = parser.add_argument_group('Private Clusters')
  if with_deprecated:
    group.add_argument(
        '--private-cluster',
        help=('Cluster is created with no public IP addresses on the cluster '
              'nodes.'),
        default=None,
        action=actions.DeprecationAction(
            'private-cluster',
            warn='The --private-cluster flag is deprecated and will be removed '
            'in a future release. Use --enable-private-nodes instead.',
            action='store_true'))
  group.add_argument(
      '--enable-private-nodes',
      help=('Cluster is created with no public IP addresses on the cluster '
            'nodes.'),
      default=None,
      action='store_true')
  group.add_argument(
      '--enable-private-endpoint',
      help=('Cluster is managed using the private IP address of the master '
            'API endpoint.'),
      default=None,
      action='store_true')
  if with_alpha:
    AddPeeringRouteSharingFlag(group)
  group.add_argument(
      '--master-ipv4-cidr',
      help=('IPv4 CIDR range to use for the master network.  This should have '
            'a netmask of size /28 and should be used in conjunction with the '
            '--enable-private-nodes flag.'),
      default=None)


def AddPeeringRouteSharingFlag(group):
  group.add_argument(
      '--enable-peering-route-sharing',
      help=(
          'Enable custom route sharing between the master and node VPCs, which '
          'ensures clients running in networks connected via a Cloud Router, '
          'VPN, or Interconnect can reach the API server.'),
      default=None,
      action='store_true')


def AddEnableLegacyAuthorizationFlag(parser, hidden=False):
  """Adds a --enable-legacy-authorization flag to parser."""
  help_text = """\
Enables the legacy ABAC authentication for the cluster.
User rights are granted through the use of policies which combine attributes
together. For a detailed look at these properties and related formats, see
https://kubernetes.io/docs/admin/authorization/abac/. To use RBAC permissions
instead, create or update your cluster with the option
`--no-enable-legacy-authorization`.
"""
  parser.add_argument(
      '--enable-legacy-authorization',
      action='store_true',
      default=None,
      hidden=hidden,
      help=help_text)


def AddAuthenticatorSecurityGroupFlags(parser, hidden=False):
  """Adds --security-group to parser."""
  help_text = """\
The name of the RBAC security group for use with Google security groups
in Kubernetes RBAC
(https://kubernetes.io/docs/reference/access-authn-authz/rbac/).

To include group membership as part of the claims issued by Google
during authentication, a group must be designated as a security group by
including it as a direct member of this group.

If unspecified, no groups will be returned for use with RBAC."""
  parser.add_argument(
      '--security-group',
      help=help_text,
      default=None,
      hidden=hidden)


def AddStartIpRotationFlag(parser, hidden=False):
  """Adds a --start-ip-rotation flag to parser."""
  help_text = """\
Start the rotation of this cluster to a new IP. For example:

  $ {command} example-cluster --start-ip-rotation

This causes the cluster to serve on two IPs, and will initiate a node upgrade \
to point to the new IP."""
  parser.add_argument(
      '--start-ip-rotation',
      action='store_true',
      default=False,
      hidden=hidden,
      help=help_text)


def AddStartCredentialRotationFlag(parser, hidden=False):
  """Adds a --start-credential-rotation flag to parser."""
  help_text = """\
Start the rotation of IP and credentials for this cluster. For example:

  $ {command} example-cluster --start-credential-rotation

This causes the cluster to serve on two IPs, and will initiate a node upgrade \
to point to the new IP."""
  parser.add_argument(
      '--start-credential-rotation',
      action='store_true',
      default=False,
      hidden=hidden,
      help=help_text)


def AddCompleteIpRotationFlag(parser, hidden=False):
  """Adds a --complete-ip-rotation flag to parser."""
  help_text = """\
Complete the IP rotation for this cluster. For example:

  $ {command} example-cluster --complete-ip-rotation

This causes the cluster to stop serving its old IP, and return to a single IP \
state."""
  parser.add_argument(
      '--complete-ip-rotation',
      action='store_true',
      default=False,
      hidden=hidden,
      help=help_text)


def AddCompleteCredentialRotationFlag(parser, hidden=False):
  """Adds a --complete-credential-rotation flag to parser."""
  help_text = """\
Complete the IP and credential rotation for this cluster. For example:

  $ {command} example-cluster --complete-credential-rotation

This causes the cluster to stop serving its old IP, return to a single IP, and \
invalidate old credentials."""
  parser.add_argument(
      '--complete-credential-rotation',
      action='store_true',
      default=False,
      hidden=hidden,
      help=help_text)


def AddMaintenanceWindowFlag(parser, hidden=False, add_unset_text=False):
  """Adds a --maintenance-window flag to parser."""
  help_text = """\
Set a time of day when you prefer maintenance to start on this cluster. \
For example:

  $ {command} example-cluster --maintenance-window=12:43

The time corresponds to the UTC time zone, and must be in HH:MM format.
"""
  unset_text = """\
  To remove an existing maintenance window from the cluster, use \
\'--maintenance-window=None\'
"""
  description = 'Maintenance windows must be passed in using HH:MM format.'
  unset_description = ' They can also be removed by using the word \"None\".'

  if add_unset_text:
    help_text += unset_text
    description += unset_description

  type_ = arg_parsers.RegexpValidator(
      r'^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$|^None$', description)
  parser.add_argument(
      '--maintenance-window',
      default=None,
      hidden=hidden,
      type=type_,
      help=help_text)


def AddLabelsFlag(parser, suppressed=False):
  """Adds Labels related flags to parser.

  Args:
    parser: A given parser.
    suppressed: Whether or not to suppress help text.
  """

  help_text = """\
Labels to apply to the Google Cloud resources in use by the Kubernetes Engine
cluster. These are unrelated to Kubernetes labels.
Example:

  $ {command} example-cluster --labels=label_a=value1,label_b=,label_c=value3
"""
  parser.add_argument(
      '--labels',
      metavar='KEY=VALUE',
      type=arg_parsers.ArgDict(),
      help=help_text,
      hidden=suppressed)


def AddUpdateLabelsFlag(parser):
  """Adds Update Labels related flags to parser.

  Args:
    parser: A given parser.
  """

  help_text = """\
Labels to apply to the Google Cloud resources in use by the Kubernetes Engine
cluster. These are unrelated to Kubernetes labels.
Example:

  $ {command} example-cluster --update-labels=label_a=value1,label_b=value2
"""
  parser.add_argument(
      '--update-labels',
      metavar='KEY=VALUE',
      type=arg_parsers.ArgDict(),
      help=help_text)


def AddRemoveLabelsFlag(parser):
  """Adds Remove Labels related flags to parser.

  Args:
    parser: A given parser.
  """

  help_text = """\
Labels to remove from the Google Cloud resources in use by the Kubernetes Engine
cluster. These are unrelated to Kubernetes labels.
Example:

  $ {command} example-cluster --remove-labels=label_a,label_b
"""
  parser.add_argument(
      '--remove-labels',
      metavar='KEY',
      type=arg_parsers.ArgList(),
      help=help_text)


def AddDiskTypeFlag(parser):
  """Adds a --disk-type flag to the given parser.

  Args:
    parser: A given parser.
  """
  help_text = """\
Type of the node VM boot disk. Defaults to pd-standard.
"""
  parser.add_argument(
      '--disk-type',
      help=help_text,
      choices=['pd-standard', 'pd-ssd'])


def AddIPAliasFlags(parser):
  """Adds flags related to IP aliases to the parser.

  Args:
    parser: A given parser.
  """

  parser.add_argument(
      '--enable-ip-alias',
      action='store_true',
      default=None,
      help="""\
Enable use of alias IPs (https://cloud.google.com/compute/docs/alias-ip/)
for pod IPs. This will create two secondary ranges, one for the pod IPs
and another to reserve space for the services range.
""")
  parser.add_argument(
      '--services-ipv4-cidr',
      metavar='CIDR',
      help="""\
Set the IP range for the services IPs.

Can be specified as a netmask size (e.g. '/20') or as in CIDR notion
(e.g. '10.100.0.0/20'). If given as a netmask size, the IP range will
be chosen automatically from the available space in the network.

If unspecified, the services CIDR range will be chosen with a default
mask size.

Can not be specified unless '--enable-ip-alias' is also specified.
""")
  parser.add_argument(
      '--create-subnetwork',
      metavar='KEY=VALUE',
      type=arg_parsers.ArgDict(),
      help="""\
Create a new subnetwork for the cluster. The name and range of the
subnetwork can be customized via optional 'name' and 'range' key-value
pairs.

'name' specifies the name of the subnetwork to be created.

'range' specifies the IP range for the new subnetwork. This can either
be a netmask size (e.g. '/20') or a CIDR range (e.g. '10.0.0.0/20').
If a netmask size is specified, the IP is automatically taken from the
free space in the cluster's network.

Examples:

Create a new subnetwork with a default name and size.

      $ {command} --create-subnetwork ""

Create a new subnetwork named "my-subnet" with netmask of size 21.

      $ {command} --create-subnetwork name=my-subnet,range=/21

Create a new subnetwork with a default name with the primary range of
10.100.0.0/16.

      $ {command} --create-subnetwork range=10.100.0.0/16

Create a new subnetwork with the name "my-subnet" with a default range.

      $ {command} --create-subnetwork name=my-subnet

Can not be specified unless '--enable-ip-alias' is also specified. Can
not be used in conjunction with the '--subnetwork' option.
""")
  parser.add_argument(
      '--cluster-secondary-range-name',
      metavar='NAME',
      help="""\
Set the secondary range to be used as the source for pod IPs. Alias
ranges will be allocated from this secondary range.  NAME must be the
name of an existing secondary range in the cluster subnetwork.

Must be used in conjunction with '--enable-ip-alias'. Cannot be used
with --create-subnetwork.
""")
  parser.add_argument(
      '--services-secondary-range-name',
      metavar='NAME',
      help="""\
Set the secondary range to be used for services (e.g. ClusterIPs).
NAME must be the name of an existing secondary range in the cluster
subnetwork.

Must be used in conjunction with '--enable-ip-alias'. Cannot be used
with --create-subnetwork.
""")


def AddMaxPodsPerNodeFlag(parser, for_node_pool=False, hidden=False):
  """Adds max pod number constraints flags to the parser.

  Args:
    parser: A given parser.
    for_node_pool: True if it's applied to a node pool.
                   False if it's applied to a cluster.
    hidden: Whether or not to hide the help text.
  """
  parser.add_argument(
      '--max-pods-per-node',
      default=None,
      help="""\
The max number of pods per node for this node pool.

This flag sets the maximum number of pods that can be run at the same time on a
node. This will override the value given with --default-max-pods-per-node flag
set at the cluster level.

Must be used in conjunction with '--enable-ip-alias'.
""",
      hidden=hidden,
      type=int)
  if not for_node_pool:
    parser.add_argument(
        '--default-max-pods-per-node',
        default=None,
        help="""\
The default max number of pods per node for node pools in the cluster.

This flag sets the default max-pods-per-node for node pools in the cluster. If
--max-pods-per-node is not specified explicitly for a node pool, this flag
value will be used.

Must be used in conjunction with '--enable-ip-alias'.
""",
        hidden=hidden,
        type=int)


def AddMinCpuPlatformFlag(parser, for_node_pool=False, hidden=False):
  """Adds the --min-cpu-platform flag to the parser.

  Args:
    parser: A given parser.
    for_node_pool: True if it's applied a non-default node pool.
    hidden: Whether or not to hide the help text.
  """
  if for_node_pool:
    help_text = """\
When specified, the nodes for the new node pool will be scheduled on host with
specified CPU architecture or a newer one.

Examples:

  $ {command} node-pool-1 --cluster=example-cluster --min-cpu-platform=PLATFORM

"""
  else:
    help_text = """\
When specified, the nodes for the new cluster's default node pool will be
scheduled on host with specified CPU architecture or a newer one.

Examples:

  $ {command} example-cluster --min-cpu-platform=PLATFORM

"""

  help_text += """\
To list available CPU platforms in given zone, run:

  $ gcloud beta compute zones describe ZONE --format="value(availableCpuPlatforms)"

CPU platform selection is available only in selected zones.
"""

  parser.add_argument(
      '--min-cpu-platform', metavar='PLATFORM', hidden=hidden, help=help_text)


def AddWorkloadMetadataFromNodeFlag(parser, hidden=False):
  """Adds the --workload-metadata-from-node flag to the parser.

  Args:
    parser: A given parser.
    hidden: Whether or not to hide the help text.
  """
  help_text = """\
Sets the node metadata option for workload metadata configuration. This feature
is scheduled to be deprecated in the future and later removed.
"""

  parser.add_argument(
      '--workload-metadata-from-node',
      default=None,
      choices={
          'SECURE': 'Prevents workloads not in hostNetwork from accessing '
                    'certain VM metadata, specifically kube-env, which '
                    'contains Kubelet credentials, and the instance identity '
                    'token. This is a temporary security solution available '
                    'while the bootstrapping process for cluster nodes is '
                    'being redesigned with significant security improvements.',
          'EXPOSED': 'Exposes all VM metadata to workloads.',
          'UNSPECIFIED': 'Chooses the default.',
      },
      type=lambda x: x.upper(),
      hidden=hidden,
      help=help_text)


def AddTagOrDigestPositional(parser,
                             verb,
                             repeated=True,
                             tags_only=False,
                             arg_name=None,
                             metavar=None):
  """Adds a tag or digest positional arg."""
  digest_str = '*.gcr.io/PROJECT_ID/IMAGE_PATH@sha256:DIGEST or'
  if tags_only:
    digest_str = ''

  if not arg_name:
    arg_name = 'image_names' if repeated else 'image_name'
    metavar = metavar or 'IMAGE_NAME'

  parser.add_argument(
      arg_name,
      metavar=metavar or arg_name.upper(),
      nargs='+' if repeated else None,
      help=('The fully qualified name(s) of image(s) to {verb}. '
            'The name(s) should be formatted as {digest_str} '
            '*.gcr.io/PROJECT_ID/IMAGE_PATH:TAG.'.format(
                verb=verb, digest_str=digest_str)))


def AddImagePositional(parser, verb):
  parser.add_argument(
      'image_name',
      help=('The name of the image to {verb}. The name format should be '
            '*.gcr.io/PROJECT_ID/IMAGE_PATH[:TAG|@sha256:DIGEST]. '.format(
                verb=verb)))


def AddNodeLocationsFlag(parser):
  parser.add_argument(
      '--node-locations',
      type=arg_parsers.ArgList(min_length=1),
      metavar='ZONE',
      help="""\
The set of zones in which the specified node footprint should be replicated.
All zones must be in the same region as the cluster's master(s), specified by
the `--zone` or `--region` flag. Additionally, for zonal clusters,
`--node-locations` must contain the cluster's primary zone. If not specified,
all nodes will be in the cluster's primary zone (for zonal clusters) or spread
across three randomly chosen zones within the cluster's region (for regional
clusters).

Note that `NUM_NODES` nodes will be created in each zone, such that if you
specify `--num-nodes=4` and choose two locations, 8 nodes will be created.

Multiple locations can be specified, separated by commas. For example:

  $ {command} example-cluster --zone us-central1-a --node-locations us-central1-a,us-central1-b
""")


def AddLoggingServiceFlag(parser, enable_kubernetes):
  """Adds a --logging-service flag to the parser.

  Args:
    parser: A given parser.
    enable_kubernetes: Mention Kubernetes-native resource model in help string
  """
  help_str = """\
Logging service to use for the cluster. Options are:
"logging.googleapis.com" (the Google Cloud Logging service),
"none" (logs will not be exported from the cluster)
"""

  if enable_kubernetes:
    help_str = """\
Logging service to use for the cluster. Options are:
"logging.googleapis.com/kubernetes" (the Google Cloud Logging
service with Kubernetes-native resource model enabled),
"logging.googleapis.com" (the Google Cloud Logging service),
"none" (logs will not be exported from the cluster)
"""

  parser.add_argument('--logging-service', help=help_str)


def AddMonitoringServiceFlag(parser, enable_kubernetes):
  """Adds a --monitoring-service flag to the parser.

  Args:
    parser: A given parser.
    enable_kubernetes: Mention Kubernetes-native resource model in help string
  """

  help_str = """\
Monitoring service to use for the cluster. Options are:
"monitoring.googleapis.com" (the Google Cloud Monitoring service),
"none" (no metrics will be exported from the cluster)
"""

  if enable_kubernetes:
    help_str = """\
Monitoring service to use for the cluster. Options are:
"monitoring.googleapis.com/kubernetes" (the Google Cloud
Monitoring service with Kubernetes-native resource model enabled),
"monitoring.googleapis.com" (the Google Cloud Monitoring service),
"none" (no metrics will be exported from the cluster)
"""

  parser.add_argument('--monitoring-service', help=help_str)


def AddNodeIdentityFlags(parser, example_target, new_behavior=True):
  """Adds node identity flags to the given parser.

  Node identity flags are --scopes, --[no-]enable-cloud-endpoints (deprecated),
  and --service-account.  --service-account is mutually exclusive with the
  others.  --[no-]enable-cloud-endpoints is not allowed if property
  container/new_scopes_behavior is set to true, and is removed completely if
  new_behavior is set to true.

  Args:
    parser: A given parser.
    example_target: the target for the command, e.g. mycluster.
    new_behavior: Use new (alpha & beta) behavior: remove
    --[no-]enable-cloud-endpoints.
  """
  node_identity_group = parser.add_group(
      mutex=True, help='Options to specify the node identity.')
  scopes_group = node_identity_group.add_group(help='Scopes options.')

  if new_behavior:
    track_help = """
Unless container/new_scopes_behavior property is true, compute-rw and storage-ro
are always added, even if not explicitly specified, and --enable-cloud-endpoints
(by default) adds service-control and service-management scopes.

If container/new_scopes_behavior property is true, none of the above scopes are
added (though storage-ro, service-control, and service-management are all
included in the default scopes.  In a future release, this will be the default
behavior.
"""
  else:
    track_help = ''
  scopes_group.add_argument(
      '--scopes',
      type=arg_parsers.ArgList(),
      metavar='SCOPE',
      default='gke-default',
      help="""\
Specifies scopes for the node instances. Examples:

  $ {{command}} {example_target} --scopes=https://www.googleapis.com/auth/devstorage.read_only

  $ {{command}} {example_target} --scopes=bigquery,storage-rw,compute-ro

Multiple SCOPEs can be specified, separated by commas. `logging-write`
and/or `monitoring` are added unless Cloud Logging and/or Cloud Monitoring
are disabled (see `--enable-cloud-logging` and `--enable-cloud-monitoring`
for more information).
{track_help}
{scopes_help}
""".format(
    example_target=example_target,
    track_help=track_help,
    scopes_help=compute_constants.ScopesHelp()))

  cloud_endpoints_help_text = """\
Automatically enable Google Cloud Endpoints to take advantage of API management
features by adding service-control and service-management scopes.

If `--no-enable-cloud-endpoints` is set, remove service-control and
service-management scopes, even if they are implicitly (via default) or
explicitly set via `--scopes`.

`--[no-]enable-cloud-endpoints` is not allowed if
`container/new_scopes_behavior` property is set to true.
"""
  scopes_group.add_argument(
      '--enable-cloud-endpoints',
      action=actions.DeprecationAction(
          '--[no-]enable-cloud-endpoints',
          warn='Flag --[no-]enable-cloud-endpoints is deprecated and will be '
          'removed in a future release.  Scopes necessary for Google Cloud '
          'Endpoints are now included in the default set and may be '
          'excluded using --scopes.',
          removed=new_behavior,
          action='store_true'),
      default=True,
      help=cloud_endpoints_help_text)

  sa_help_text = (
      'The Google Cloud Platform Service Account to be used by the node VMs. '
      'If a service account is specified, the cloud-platform and '
      'userinfo.email scopes are used. If no Service Account is specified, the '
      'project default service account is used.')
  node_identity_group.add_argument('--service-account', help=sa_help_text)


def AddClusterNodeIdentityFlags(parser):
  """Adds node identity flags to the given parser.

  This is a wrapper around AddNodeIdentityFlags for [alpha|beta] cluster, as it
  provides example-cluster as the example and uses non-deprecated scopes
  behavior.

  Args:
    parser: A given parser.
  """
  AddNodeIdentityFlags(parser, example_target='example-cluster')


def AddDeprecatedClusterNodeIdentityFlags(parser):
  """Adds node identity flags to the given parser.

  This is a wrapper around AddNodeIdentityFlags for [alpha|beta] cluster, as it
  provides example-cluster as the example and uses non-deprecated scopes
  behavior.

  Args:
    parser: A given parser.
  """
  AddNodeIdentityFlags(
      parser, example_target='example-cluster', new_behavior=False)


def AddNodePoolNodeIdentityFlags(parser):
  """Adds node identity flags to the given parser.

  This is a wrapper around AddNodeIdentityFlags for (GA) node-pools, as it
  provides node-pool-1 as the example and uses non-deprecated scopes behavior.

  Args:
    parser: A given parser.
  """
  AddNodeIdentityFlags(
      parser, example_target='node-pool-1 --cluster=example-cluster')


def AddDeprecatedNodePoolNodeIdentityFlags(parser):
  """Adds node identity flags to the given parser.

  This is a wrapper around AddNodeIdentityFlags for (GA) node-pools, as it
  provides node-pool-1 as the example and uses non-deprecated scopes behavior.

  Args:
    parser: A given parser.
  """
  AddNodeIdentityFlags(
      parser,
      example_target='node-pool-1 --cluster=example-cluster',
      new_behavior=False)


def AddAddonsFlagsWithOptions(parser, addon_options):
  """Adds the --addons flag to the parser with the given addon options."""
  parser.add_argument(
      '--addons',
      type=arg_parsers.ArgList(choices=addon_options),
      metavar='ADDON',
      # TODO(b/65264376): Replace the doc link when a better doc is ready.
      help="""\
Default set of addons includes {0}. Addons
(https://cloud.google.com/kubernetes-engine/reference/rest/v1/projects.zones.clusters#AddonsConfig)
are additional Kubernetes cluster components. Addons specified by this flag will
be enabled. The others will be disabled.
""".format(', '.join(api_adapter.DEFAULT_ADDONS)))


def AddAddonsFlags(parser):
  """Adds the --addons flag to the parser for the beta and GA tracks."""
  AddAddonsFlagsWithOptions(parser, api_adapter.ADDONS_OPTIONS)


def AddAlphaAddonsFlags(parser):
  """Adds the --addons flag to the parser for the alpha track."""
  AddAddonsFlagsWithOptions(parser, api_adapter.ALPHA_ADDONS_OPTIONS)


def AddBetaAddonsFlags(parser):
  """Adds the --addons flag to the parser for the beta track."""
  AddAddonsFlagsWithOptions(parser, api_adapter.BETA_ADDONS_OPTIONS)


def AddPodSecurityPolicyFlag(parser, hidden=False):
  """Adds a --enable-pod-security-policy flag to parser."""
  help_text = """\
Enables the pod security policy admission controller for the cluster.  The pod
security policy admission controller adds fine-grained pod create and update
authorization controls through the PodSecurityPolicy API objects. For more
information, see
https://cloud.google.com/kubernetes-engine/docs/how-to/pod-security-policies.
"""
  parser.add_argument(
      '--enable-pod-security-policy',
      action='store_true',
      default=None,
      hidden=hidden,
      help=help_text)


def AddAllowRouteOverlapFlag(parser):
  """Adds a --allow-route-overlap flag to parser."""
  help_text = """\
Allows the provided cluster CIDRs to overlap with existing routes
that are less specific and do not terminate at a VM.

When enabled, `--cluster-ipv4-cidr` must be fully specified (e.g. `10.96.0.0/14`
, but not `/14`). If `--enable-ip-alias` is also specified, both
`--cluster-ipv4-cidr` and `--services-ipv4-cidr` must be fully specified.
"""
  parser.add_argument(
      '--allow-route-overlap',
      action='store_true',
      default=None,
      help=help_text)


def AddTpuFlags(parser, hidden=False, enable_tpu_service_networking=False):
  """Adds flags related to TPUs to the parser.

  Args:
    parser: A given parser.
    hidden: Whether or not to hide the help text.
    enable_tpu_service_networking: Whether to add the
    enable_tpu_service_networking flag.
  """

  tpu_group = parser.add_group(help='Flags relating to Cloud TPUs:')

  tpu_group.add_argument(
      '--enable-tpu',
      action='store_true',
      hidden=hidden,
      help="""\
Enable Cloud TPUs for this cluster.

Can not be specified unless `--enable-kubernetes-alpha` and `--enable-ip-alias`
are also specified.
""")

  group = tpu_group

  if enable_tpu_service_networking:
    group = tpu_group.add_mutually_exclusive_group()
    group.add_argument(
        '--enable-tpu-service-networking',
        action='store_true',
        hidden=hidden,
        help="""\
Enable Cloud TPU's Service Networking mode. In this mode, the CIDR blocks used
by the Cloud TPUs will be allocated and managed by Service Networking, instead
of Kubernetes Engine.

This cannot be specified if `tpu-ipv4-cidr` is specified.
""")

  group.add_argument(
      '--tpu-ipv4-cidr',
      metavar='CIDR',
      hidden=hidden,
      help="""\
Set the IP range for the Cloud TPUs.

Can be specified as a netmask size (e.g. '/20') or as in CIDR notion
(e.g. '10.100.0.0/20'). If given as a netmask size, the IP range will be chosen
automatically from the available space in the network.

If unspecified, the TPU CIDR range will use automatic default '/20'.

Can not be specified unless '--enable-tpu' and '--enable-ip-alias' are also
specified.
""")


def AddIssueClientCertificateFlag(parser):
  """Adds --issue-client-certificate flag to the parser."""
  help_text = """\
Issue a TLS client certificate with admin permissions.

When enabled, the certificate and private key pair will be present in
MasterAuth field of the Cluster object. For cluster versions before 1.12, a
client certificate will be issued by default. As of 1.12, client certificates
are disabled by default.
"""
  parser.add_argument(
      '--issue-client-certificate',
      action='store_true',
      default=None,
      help=help_text)


def AddIstioConfigFlag(parser, suppressed=False):
  """Adds --istio-config flag to the parser.

  Args:
    parser: A given parser.
    suppressed: Whether or not to suppress help text.
  """

  help_text = """\
Configurations for Istio addon, requires --addons contains Istio for create,
or --update-addons Istio=ENABLED for update.

*auth*:::Optional Type of auth MTLS_PERMISSIVE or MTLS_STRICT
Example:

  $ {command} example-cluster --istio-config=auth=MTLS_PERMISSIVE
"""
  parser.add_argument(
      '--istio-config',
      metavar='auth=MTLS_PERMISSIVE',
      type=arg_parsers.ArgDict(
          spec={
              'auth': (lambda x: x.upper()),
          }),
      help=help_text,
      hidden=suppressed)


def ValidateIstioConfigCreateArgs(istio_config_args, addons_args):
  """Validates flags specifying Istio config for create.

  Args:
    istio_config_args: parsed comandline arguments for --istio_config.
    addons_args: parsed comandline arguments for --addons.
  Raises:
    InvalidArgumentException: when auth is not MTLS_PERMISSIVE nor MTLS_STRICT,
    or --addon=Istio is not specified
  """
  if istio_config_args:
    auth = istio_config_args.get('auth', '')
    if auth not in ['MTLS_PERMISSIVE', 'MTLS_STRICT']:
      raise exceptions.InvalidArgumentException(
          '--istio-config', 'auth is either MTLS_PERMISSIVE or MTLS_STRICT'
          'e.g. --istio-config auth=MTLS_PERMISSIVE')
    if 'Istio' not in addons_args:
      raise exceptions.InvalidArgumentException(
          '--istio-config', '--addon=Istio must be specified when '
          '--istio-config is given')


def ValidateIstioConfigUpdateArgs(istio_config_args, disable_addons_args):
  """Validates flags specifying Istio config for update.

  Args:
    istio_config_args: parsed comandline arguments for --istio_config.
    disable_addons_args: parsed comandline arguments for --update-addons.
  Raises:
    InvalidArgumentException: when auth is not MTLS_PERMISSIVE nor MTLS_STRICT,
    or --update-addons=Istio=ENABLED is not specified
  """
  if istio_config_args:
    auth = istio_config_args.get('auth', '')
    if auth not in ['MTLS_PERMISSIVE', 'MTLS_STRICT']:
      raise exceptions.InvalidArgumentException(
          '--istio-config', 'auth must be one of MTLS_PERMISSIVE or '
          'MTLS_STRICT e.g. --istio-config auth=MTLS_PERMISSIVE')
    disable_istio = disable_addons_args.get('Istio')
    if disable_istio is None or disable_istio:
      raise exceptions.InvalidArgumentException(
          '--istio-config', '--update-addons=Istio=ENABLED must be specified '
          'when --istio-config is given')


def AddConcurrentNodeCountFlag(parser):
  help_text = """\
The number of nodes to upgrade concurrently. Valid values are [1, {max}].
It is a recommended best practice to set this value to no higher than 3% of
your cluster size.'
""".format(max=api_adapter.MAX_CONCURRENT_NODE_COUNT)

  parser.add_argument(
      '--concurrent-node-count',
      type=arg_parsers.BoundedInt(1, api_adapter.MAX_CONCURRENT_NODE_COUNT),
      help=help_text)


# TODO(b/110368338): Drop this warning when changing the default value of the
# flag.
def WarnForUnspecifiedIpAllocationPolicy(args):
  if not args.IsSpecified('enable_ip_alias'):
    log.warning(
        'Currently VPC-native is not the default mode during cluster creation. '
        'In the future, this will become the default mode and can be disabled '
        'using `--no-enable-ip-alias` flag. Use `--[no-]enable-ip-alias` flag '
        'to suppress this warning.')


def WarnForNodeModification(args, enable_autorepair):
  if (args.image_type or '').lower() != 'ubuntu':
    return
  if enable_autorepair or args.enable_autoupgrade:
    log.warning('Modifications on the boot disks of node VMs do not persist '
                'across node recreations. Nodes are recreated during '
                'manual-upgrade, auto-upgrade, auto-repair, and auto-scaling. '
                'To preserve modifications across node recreation, use a '
                'DaemonSet.')


def AddMachineTypeFlag(parser):
  """Adds --machine-type flag to the parser.

  Args:
    parser: A given parser.
  """

  help_text = """\
The type of machine to use for nodes. Defaults to n1-standard-1.
The list of predefined machine types is available using the following command:

  $ gcloud compute machine-types list

You can also specify custom machine types with the string "custom-CPUS-RAM"
where ```CPUS``` is the number of virtual CPUs and ```RAM``` is the amount of
RAM in MiB.

For example, to create a node pool using custom machines with 2 vCPUs and 12 GB
of RAM:

  $ {command} high-mem-pool --machine-type=custom-2-12288
"""

  parser.add_argument(
      '--machine-type', '-m',
      help=help_text)


def AddManagedPodIdentityFlags(parser):
  """Adds Managed Pod Identity flags to the parser."""
  enable_help_text = """\
Enable Managed Pod Identity on the cluster.

When enabled, pods with cloud.google.com/service-account annotations will be
able to authenticate to Google Cloud Platform APIs on behalf of service account
specified in the annotation.
"""
  parser.add_argument(
      '--enable-managed-pod-identity',
      action='store_true',
      default=False,
      # TODO(b/109942548): unhide this flag for Beta
      hidden=True,
      help=enable_help_text)
  sa_help_text = """\
Federating Service Account to use with Managed Pod Identity.

Sets the name (email) of the GCP Service Account used to connect
Kubernetes Service Accounts to GCP Service Accounts.

Must be set with `--enable-managed-pod-identity`.
"""
  parser.add_argument(
      '--federating-service-account',
      default=None,
      # TODO(b/109942548): unhide this flag for Beta
      hidden=True,
      help=sa_help_text)


def AddResourceUsageExportFlags(parser, add_clear_flag=False, hidden=False):
  """Adds flags about exporting cluster resource usage to BigQuery."""

  group = parser.add_group(
      "Exports cluster's usage of cloud resources",
      hidden=hidden)
  if add_clear_flag:
    group.is_mutex = True
    group.add_argument(
        '--clear-resource-usage-bigquery-dataset',
        action='store_true',
        hidden=hidden,
        default=None,
        help='Disables exporting cluster resource usage to BigQuery.')
    group = group.add_group()

  dataset_help_text = """\
The name of the BigQuery dataset to which the cluster's usage of cloud
resources is exported. A table will be created in the specified dataset to
store cluster resource usage. The resulting table can be joined with BigQuery
Billing Export to produce a fine-grained cost breakdown.

Example:

  $ {command} example-cluster --resource-usage-bigquery-dataset=example_bigquery_dataset_name
"""

  group.add_argument(
      '--resource-usage-bigquery-dataset',
      default=None,
      hidden=hidden,
      help=dataset_help_text)

  network_egress_help_text = """`
Enable network egress metering on this cluster.

When enabled, a DaemonSet is deployed into the cluster. Each DaemonSet pod
meters network egress traffic by collecting data from the conntrack table, and
exports the metered metrics to the specified destination.

Network egress metering is disabled if this flag is omitted, or when
`--no-enable-network-egress-metering` is set.
"""
  group.add_argument(
      '--enable-network-egress-metering',
      action='store_true',
      default=None,
      help=network_egress_help_text)


def AddEnablePrivateIpv6AccessFlag(parser, hidden=False):
  """Adds --enable-private-ipv6-access flag to the parser.

  When enabled, this allows gRPC clients on this cluster's pods a fast
  path to access Google hosted services (eg. Cloud Spanner,
  Cloud Dataflow, Cloud Bigtable)
  This is currently only available on Alpha clusters, and needs
  '--enable-kubernetes-alpha' to be specified also.

  Args:
    parser: A given parser.
    hidden: If true, suppress help text for added options.
  """
  parser.add_argument(
      '--enable-private-ipv6-access',
      default=None,
      help="""\
Enables private access to Google services over IPv6.

When enabled, this allows gRPC clients on this cluster's pods a fast path to
access Google hosted services (eg. Cloud Spanner, Cloud Dataflow, Cloud
Bigtable).

This is currently only available on Alpha clusters, specified by using
--enable-kubernetes-alpha.
      """,
      hidden=hidden,
      action='store_true')


def AddVerticalPodAutoscalingFlag(parser, hidden=False):
  """Adds vertical pod autoscaling related flag to the parser.

  VerticalPodAutoscaling related flag is: --enable-vertical-pod-autoscaling

  Args:
    parser: A given parser.
    hidden: If true, suppress help text for added options.
  """

  parser.add_argument(
      '--enable-vertical-pod-autoscaling',
      default=None,
      help='Enables vertical pod autoscaling for a cluster.',
      hidden=hidden,
      action='store_true')


# TODO(b/112194849): Explain limitation to the sandbox pods and the nodes.
def AddSandboxFlag(parser, hidden=False):
  """Adds a --sandbox flag to the given parser.

  Args:
    parser: A given parser.
    hidden: Whether or not to hide the help text.
  """
  type_validator = arg_parsers.RegexpValidator(
      r'^gvisor$', 'Type must be "gvisor"')
  parser.add_argument(
      '--sandbox',
      type=arg_parsers.ArgDict(
          spec={'type': type_validator},
          required_keys=['type'],
          max_length=1),
      metavar='type=TYPE',
      hidden=hidden,
      help="""\
Enables the requested sandbox on all nodes in the node-pool. Example:

  $ {command} node-pool-1 --cluster=example-cluster --sandbox type=gvisor

The only supported type is 'gvisor'.
      """)


def AddSecurityProfileForCreateFlags(parser, hidden=False):
  """Adds flags related to Security Profile to the parser for cluster creation.

  Args:
    parser: A given parser.
    hidden: Whether or not to hide the help text.
  """

  group = parser.add_group(help='Flags for Security Profile:')

  group.add_argument(
      '--security-profile',
      hidden=hidden,
      help="""\
Name and version of the security profile to be applied to the cluster.

Example:

  $ {command} example-cluster --security-profile=default-1.0-gke.0
""")

  group.add_argument(
      '--security-profile-runtime-rules',
      default=True,
      action='store_true',
      hidden=hidden,
      help="""\
Apply runtime rules in the specified security profile to the cluster.
When enabled (by default), a security profile controller and webhook
are deployed on the cluster to enforce the runtime rules. If
--no-security-profile-runtime-rules is specified to disable this
feature, only bootstrapping rules are applied, and no security profile
controller or webhook are installed.
""")


def AddSecurityProfileForUpdateFlag(parser, hidden=False):
  """Adds --security-profile to specify security profile for cluster update.

  Args:
    parser: A given parser.
    hidden: Whether or not to hide the help text.
  """

  parser.add_argument(
      '--security-profile',
      hidden=hidden,
      help="""\
Name and version of the security profile to be applied to the cluster.
If not specified, the current setting of security profile will be
preserved.

Example:

  $ {command} example-cluster --security-profile=default-1.0-gke.1
""")


def AddSecurityProfileForUpgradeFlags(parser, hidden=False):
  """Adds flags related to Security Profile to the parser for cluster upgrade.

  Args:
    parser: A given parser.
    hidden: Whether or not to hide the help text.
  """

  group = parser.add_group(help='Flags for Security Profile:')

  group.add_argument(
      '--security-profile',
      hidden=hidden,
      help="""\
Name and version of the security profile to be applied to the cluster.
If not specified, the current security profile settings are preserved.
If the current security profile is not supported in the new cluster
version, this option must be explicitly specified with a supported
security profile, otherwise the operation will fail.

Example:

  $ {command} example-cluster --security-profile=default-1.0-gke.1
""")

  group.add_argument(
      '--security-profile-runtime-rules',
      default=None,
      action='store_true',
      hidden=hidden,
      help="""\
Apply runtime rules in the specified security profile to the cluster.
When enabled, a security profile controller and webhook
are deployed on the cluster to enforce the runtime rules. If
--no-security-profile-runtime-rules is specified to disable this
feature, only bootstrapping rules are applied, and no security profile
controller or webhook are installed.
""")


def AddNodeGroupFlag(parser):
  """Adds --node-group flag to the parser."""
  help_text = """\
Assign instances of this pool to run on the specified GCE node group.
This is useful for running workloads on sole tenant nodes.

To see available sole tenant node-groups, run:

  $ gcloud compute sole-tenancy node-groups list

To create a sole tenant node group, run:

  $ gcloud compute sole-tenancy node-groups create [GROUP_NAME] \
    --zone [ZONE] --node-template [TEMPLATE_NAME] --target-size [TARGET_SIZE]

See https://cloud.google.com/compute/docs/nodes for more
information on sole tenancy and node groups.
"""

  parser.add_argument(
      '--node-group',
      hidden=True,
      help=help_text)


def AddInitialNodePoolNameArg(parser, hidden=True):
  """Adds --node-pool-name argument to the parser."""
  help_text = """\
Name of the initial node pool that will be created for the cluster.

Specifies the name to use for the initial node pool that will be created
with the cluster.  If the settings specified require multiple node pools
to be created, the name for each pool will be prefixed by this name.  For
example running the following will result in three node pools being
created, example-node-pool-0, example-node-pool-1 and
example-node-pool-2:

  $ {command} example-cluster --num-nodes 9 --max-nodes-per-pool 3 \
    --node-pool-name example-node-pool
"""

  parser.add_argument('--node-pool-name', hidden=hidden, help=help_text)


def AddMetadataFlags(parser):
  """Adds --metadata and --metadata-from-file flags to the given parser."""
  metadata_help = """\
      Compute Engine metadata to be made available to the guest operating system
      running on nodes within the node pool.

      Each metadata entry is a key/value pair separated by an equals sign.
      Metadata keys must be unique and less than 128 bytes in length. Values
      must be less than or equal to 32,768 bytes in length. The total size of
      all keys and values must be less than 512 KB. Multiple arguments can be
      passed to this flag. For example:

      ``--metadata key-1=value-1,key-2=value-2,key-3=value-3''

      Additionally, the following keys are reserved for use by Kubernetes
      Engine:

      * ``cluster-location''
      * ``cluster-name''
      * ``cluster-uid''
      * ``configure-sh''
      * ``enable-os-login''
      * ``gci-update-strategy''
      * ``gci-ensure-gke-docker''
      * ``instance-template''
      * ``kube-env''
      * ``startup-script''
      * ``user-data''

      See also Compute Engine's
      link:https://cloud.google.com/compute/docs/storing-retrieving-metadata[documentation]
      on storing and retrieving instance metadata.
      """

  parser.add_argument(
      '--metadata',
      type=arg_parsers.ArgDict(min_length=1),
      default={},
      help=metadata_help,
      metavar='KEY=VALUE',
      action=arg_parsers.StoreOnceAction)

  metadata_from_file_help = """\
      Same as ``--metadata'' except that the value for the entry will
      be read from a local file.
      """

  parser.add_argument(
      '--metadata-from-file',
      type=arg_parsers.ArgDict(min_length=1),
      default={},
      help=metadata_from_file_help,
      metavar='KEY=LOCAL_FILE_PATH')
