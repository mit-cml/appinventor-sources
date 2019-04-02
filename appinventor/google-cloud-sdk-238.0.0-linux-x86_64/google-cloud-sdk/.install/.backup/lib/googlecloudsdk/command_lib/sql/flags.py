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
"""Common flags for some of the SQL commands.

Flags are specified with functions that take in a single argument, the parser,
and add the newly constructed flag to that parser.

Example:

def AddFlagName(parser):
  parser.add_argument(
    '--flag-name',
    ... // Other flag details.
  )
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import utils as compute_utils
from googlecloudsdk.api_lib.storage import storage_util
from googlecloudsdk.calliope import actions
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.util import completers


_IP_ADDRESS_PART = r'(25[0-5]|2[0-4][0-9]|1?[0-9]{1,2})'  # Match decimal 0-255
_CIDR_PREFIX_PART = r'([0-9]|[1-2][0-9]|3[0-2])'  # Match decimal 0-32
# Matches either IPv4 range in CIDR notation or a naked IPv4 address.
_CIDR_REGEX = r'{addr_part}(\.{addr_part}){{3}}(\/{prefix_part})?$'.format(
    addr_part=_IP_ADDRESS_PART, prefix_part=_CIDR_PREFIX_PART)


class DatabaseCompleter(completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(DatabaseCompleter, self).__init__(
        collection='sql.databases',
        api_version='v1beta4',
        list_command='sql databases list --uri',
        flags=['instance'],
        **kwargs)


class InstanceCompleter(completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(InstanceCompleter, self).__init__(
        collection='sql.instances',
        list_command='sql instances list --uri',
        **kwargs)


class UserCompleter(completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(UserCompleter, self).__init__(
        collection=None,  # TODO(b/62961917): Should be 'sql.users',
        api_version='v1beta4',
        list_command='sql users list --flatten=name[] --format=disable',
        flags=['instance'],
        **kwargs)


def AddInstance(parser):
  parser.add_argument(
      '--instance',
      '-i',
      required=True,
      completer=InstanceCompleter,
      help='Cloud SQL instance ID.')


def AddInstanceArgument(parser):
  """Add the 'instance' argument to the parser."""
  parser.add_argument(
      'instance',
      completer=InstanceCompleter,
      help='Cloud SQL instance ID.')


# Currently, 10230 is the max storage size one can set, and 10 is the minimum.
def AddInstanceResizeLimit(parser):
  parser.add_argument(
      '--storage-auto-increase-limit',
      type=arg_parsers.BoundedInt(10, 10230, unlimited=True),
      help='Allows you to set a maximum storage capacity, in GB. Automatic '
      'increases to your capacity will stop once this limit has been reached. '
      'Default capacity is *unlimited*.')


def AddUsername(parser):
  parser.add_argument(
      'username',
      completer=UserCompleter,
      help='Cloud SQL username.')


def AddHost(parser):
  """Add the 'deprecated_host' arg and '--host' flag to the parser."""
  help_text = ('Cloud SQL user\'s host name expressed as a specific IP address'
               ' or address range. `%` denotes an unrestricted host name. '
               'Applicable flag for MySQL instances; ignored for PostgreSQL '
               'instances. Note, if you connect to your instance using IP '
               'addresses, you must add your client IP address as an Authorized'
               ' Address, even if your host name is unrestricted. For help on '
               'how to do so, read: '
               'https://cloud.google.com/sql/docs/mysql/configure-ip')

  host = parser.add_argument_group(mutex=True)
  # NOTICE: is_required = False is a short-term solution -- don't use elsewhere.
  host.add_argument(
      'deprecated_host',
      help=help_text,
      action=actions.DeprecationAction(
          'deprecated_host',
          removed=True,
          error='Positional argument {flag_name} has been removed. Use '
                '--host instead.'),
      hidden=True).is_required = False
  host.add_argument(
      '--host',
      help=help_text)


def AddAvailabilityType(parser):
  """Add the '--availability-type' flag to the parser."""
  availabilty_type_flag = base.ChoiceArgument(
      '--availability-type',
      required=False,
      choices={
          'regional': 'Provides high availability and is recommended for '
                      'production instances; instance automatically fails over '
                      'to another zone within your selected region.',
          'zonal': 'Provides no failover capability. This is the default.'
      },
      help_str=('Specifies level of availability. Only applies to PostgreSQL '
                'instances.'))
  availabilty_type_flag.AddToParser(parser)


def AddPassword(parser):
  parser.add_argument(
      '--password',
      help='Cloud SQL user\'s password.')


def AddPromptForPassword(parser):
  parser.add_argument(
      '--prompt-for-password',
      action='store_true',
      help=('Prompt for the Cloud SQL user\'s password with character echo '
            'disabled. The password is all typed characters up to but not '
            'including the RETURN or ENTER key.'))


# Instance create and patch flags


def AddActivationPolicy(parser):
  base.ChoiceArgument(
      '--activation-policy',
      required=False,
      choices=['always', 'never', 'on-demand'],
      default=None,
      help_str=('Activation policy for this instance. This specifies when '
                'the instance should be activated and is applicable only when '
                'the instance state is `RUNNABLE`. The default is `on-demand`. '
                'More information on activation policies can be found here: '
                'https://cloud.google.com/sql/faq#activation_policy')
  ).AddToParser(parser)


def AddAssignIp(parser, show_negated_in_help=False):
  kwargs = _GetKwargsForBoolFlag(show_negated_in_help)
  parser.add_argument(
      '--assign-ip',
      help='Assign an IPv4 external address to this instance.  This setting is '
           'enabled by default.  To create an instance which only has a '
           'private IP, use --no-assign-ip and specify a private network.',
      **kwargs)


def AddAuthorizedGAEApps(parser, update=False):
  help_ = (
      'First Generation instances only. List of project IDs for App Engine '
      'applications running in the Standard environment that '
      'can access this instance.')
  if update:
    help_ += (
        '\n\nThe value given for this argument *replaces* the existing list.')
  parser.add_argument(
      '--authorized-gae-apps',
      type=arg_parsers.ArgList(min_length=1),
      metavar='APP',
      required=False,
      help=help_)


def AddAuthorizedNetworks(parser, update=False):
  """Adds the `--authorized-networks` flag."""
  cidr_validator = arg_parsers.RegexpValidator(
      _CIDR_REGEX, ('Must be specified in CIDR notation, also known as '
                    '\'slash\' notation (e.g. 192.168.100.0/24).'))
  help_ = ('The list of external networks that are allowed to connect to '
           'the instance. Specified in CIDR notation, also known as '
           '\'slash\' notation (e.g. 192.168.100.0/24).')
  if update:
    help_ += (
        '\n\nThe value given for this argument *replaces* the existing list.')
  parser.add_argument(
      '--authorized-networks',
      type=arg_parsers.ArgList(min_length=1, element_type=cidr_validator),
      metavar='NETWORK',
      required=False,
      default=[],
      help=help_)


def AddBackupStartTime(parser):
  parser.add_argument(
      '--backup-start-time',
      required=False,
      help=('Start time of daily backups, specified in the 24 hour '
            'format - HH:MM, in the UTC timezone.'))


def AddDatabaseFlags(parser, update=False):
  """Adds the `--database-flags` flag."""
  help_ = ('Comma-separated list of database flags to set on the '
           'instance. Use an equals sign to separate flag name and value. '
           'Flags without values, like skip_grant_tables, can be written '
           'out without a value after, e.g., `skip_grant_tables=`. Use '
           'on/off for booleans. View the Instance Resource API for allowed '
           'flags. (e.g., `--database-flags max_allowed_packet=55555,'
           'skip_grant_tables=,log_output=1`)')
  if update:
    help_ += (
        '\n\nThe value given for this argument *replaces* the existing list.')
  parser.add_argument(
      '--database-flags',
      type=arg_parsers.ArgDict(min_length=1),
      metavar='FLAG=VALUE',
      required=False,
      help=help_)


def AddDatabaseVersion(parser, restrict_choices=True):
  """Adds `--database-version` to the parser with choices restricted or not."""
  choices = ['MYSQL_5_5', 'MYSQL_5_6', 'MYSQL_5_7', 'POSTGRES_9_6']
  help_text = (
      'The database engine type and version. If left unspecified, the API '
      'defaults will be used.')
  if not restrict_choices:
    help_text = ' '.join([
        help_text,
        'See the list of database versions at '
        'https://cloud.google.com/sql/docs/mysql/admin-api/v1beta4/instances'
        '#databaseVersion'
    ])
  parser.add_argument(
      '--database-version',
      required=False,
      choices=choices if restrict_choices else None,
      help=help_text)


def AddCPU(parser):
  parser.add_argument(
      '--cpu',
      type=int,
      required=False,
      help=('Whole number value indicating how many cores are desired in '
            'the machine. Both --cpu and --memory must be specified if a '
            'custom machine type is desired, and the --tier flag must be '
            'omitted.'))


def _GetKwargsForBoolFlag(show_negated_in_help):
  if show_negated_in_help:
    return {
        'action': arg_parsers.StoreTrueFalseAction,
    }
  else:
    return {
        'action': 'store_true',
        'default': None
    }


def AddEnableBinLog(parser, show_negated_in_help=False):
  kwargs = _GetKwargsForBoolFlag(show_negated_in_help)
  parser.add_argument(
      '--enable-bin-log',
      required=False,
      help=(
          'Specified if binary log should be enabled. If backup '
          'configuration is disabled, binary log must be disabled as well.'),
      **kwargs)


def AddExternalMasterGroup(parser):
  """Add flags to the parser for creating an external master and replica."""

  # Group for creating external primary instances.
  external_master_group = parser.add_group(
      required=False,
      help='Options for creating a wrapper for an external data source.')
  external_master_group.add_argument(
      '--source-ip-address',
      required=True,
      type=compute_utils.IPV4Argument,
      help=('Public IP address used to connect to and replicate from '
            'the external data source.'))
  external_master_group.add_argument(
      '--source-port',
      type=arg_parsers.BoundedInt(lower_bound=1, upper_bound=65535),
      # Default MySQL port number.
      default=3306,
      help=('Port number used to connect to and replicate from the '
            'external data source.'))

  # Group for creating replicas of external primary instances.
  internal_replica_group = parser.add_group(
      required=False,
      help=('Options for creating an internal replica of an external data '
            'source.'))
  internal_replica_group.add_argument(
      '--master-username',
      required=True,
      help='Name of the replication user on the external data source.')

  # TODO(b/78648703): Make group required when mutex required status is fixed.
  # For entering the password of the replication user of an external primary.
  master_password_group = internal_replica_group.add_group(
      'Password group.', mutex=True)
  master_password_group.add_argument(
      '--master-password',
      help='Password of the replication user on the external data source.')
  master_password_group.add_argument(
      '--prompt-for-master-password',
      action='store_true',
      help=('Prompt for the password of the replication user on the '
            'external data source. The password is all typed characters up '
            'to but not including the RETURN or ENTER key.'))
  internal_replica_group.add_argument(
      '--master-dump-file-path',
      required=True,
      type=storage_util.ObjectReference.FromArgument,
      help=('Path to the MySQL dump file in Google Cloud Storage from '
            'which the seed import is made. The URI is in the form '
            'gs://bucketName/fileName. Compressed gzip files (.gz) are '
            'also supported.'))

  # For specifying SSL certs for connecting to an external primary.
  credential_group = internal_replica_group.add_group(
      'Client and server credentials.', required=False)
  credential_group.add_argument(
      '--master-ca-certificate-path',
      required=True,
      help=('Path to a file containing the X.509v3 (RFC5280) PEM encoded '
            'certificate of the CA that signed the external data source\'s '
            'certificate.'))

  # For specifying client certs for connecting to an external primary.
  client_credential_group = credential_group.add_group(
      'Client credentials.', required=False)
  client_credential_group.add_argument(
      '--client-certificate-path',
      required=True,
      help=('Path to a file containing the X.509v3 (RFC5280) PEM encoded '
            'certificate that will be used by the replica to authenticate '
            'against the external data source.'))
  client_credential_group.add_argument(
      '--client-key-path',
      required=True,
      help=('Path to a file containing the unencrypted PKCS#1 or PKCS#8 '
            'PEM encoded private key associated with the '
            'clientCertificate.'))


def AddFollowGAEApp(parser):
  parser.add_argument(
      '--follow-gae-app',
      required=False,
      help=('First Generation instances only. The App Engine app '
            'this instance should follow. It must be in the same region as '
            'the instance. WARNING: Instance may be restarted.'))


def AddMaintenanceReleaseChannel(parser):
  base.ChoiceArgument(
      '--maintenance-release-channel',
      choices={
          'production': 'Production updates are stable and recommended '
                        'for applications in production.',
          'preview': 'Preview updates release prior to production '
                     'updates. You may wish to use the preview channel '
                     'for dev/test applications so that you can preview '
                     'their compatibility with your application prior '
                     'to the production release.'
      },

      help_str=("Which channel's updates to apply during the maintenance "
                "window. If not specified, Cloud SQL chooses the timing of "
                "updates to your instance.")).AddToParser(parser)


def AddMaintenanceWindowDay(parser):
  # TODO(b/79740068) Convert to ChoiceArgument when resolved.
  parser.add_argument(
      '--maintenance-window-day',
      choices=arg_parsers.DayOfWeek.DAYS,
      type=arg_parsers.DayOfWeek.Parse,
      help='Day of week for maintenance window, in UTC time zone.')


def AddMaintenanceWindowHour(parser):
  parser.add_argument(
      '--maintenance-window-hour',
      type=arg_parsers.BoundedInt(lower_bound=0, upper_bound=23),
      help='Hour of day for maintenance window, in UTC time zone.')


def AddMemory(parser):
  parser.add_argument(
      '--memory',
      type=arg_parsers.BinarySize(),
      required=False,
      help=('Whole number value indicating how much memory is desired in '
            'the machine. A size unit should be provided (eg. 3072MiB or '
            '9GiB) - if no units are specified, GiB is assumed. Both --cpu '
            'and --memory must be specified if a custom machine type is '
            'desired, and the --tier flag must be omitted.'))


def AddNetwork(parser):
  """Adds the `--network` flag to the parser."""
  parser.add_argument(
      '--network',
      help=('Network in the current project that the instance will be part '
            'of. To specify using a network with a shared VPC, use the full '
            'URL of the network. For an example host project, \'testproject\', '
            'and shared network, \'testsharednetwork\', this would be of the '
            'form:'
            '`--network`=`projects/testproject/global/networks/'
            'testsharednetwork`'))


def AddReplication(parser):
  base.ChoiceArgument(
      '--replication',
      required=False,
      choices=['synchronous', 'asynchronous'],
      default=None,
      help_str='Type of replication this instance uses. The default is '
      'synchronous.').AddToParser(parser)


def AddStorageAutoIncrease(parser):
  parser.add_argument(
      '--storage-auto-increase',
      action='store_true',
      default=None,
      help=('Storage size can be increased, but it cannot be decreased; '
            'storage increases are permanent for the life of the instance. '
            'With this setting enabled, a spike in storage requirements '
            'can result in permanently increased storage costs for your '
            'instance. However, if an instance runs out of available space, '
            'it can result in the instance going offline, dropping existing '
            'connections. This setting is enabled by default.'))


def AddStorageSize(parser):
  parser.add_argument(
      '--storage-size',
      type=arg_parsers.BinarySize(
          lower_bound='10GB',
          upper_bound='10230GB',
          suggested_binary_size_scales=['GB']),
      help=('Amount of storage allocated to the instance. Must be an integer '
            'number of GB between 10GB and 10230GB inclusive. The default is '
            '10GB.'))


def AddTier(parser, is_patch=False):
  """Adds the '--tier' flag to the parser."""
  help_text = ('The tier for this instance. For Second Generation instances, '
               'TIER is the instance\'s machine type (e.g., db-n1-standard-1). '
               'For PostgreSQL instances, only shared-core machine types '
               '(e.g., db-f1-micro) apply. A complete list of tiers is '
               'available here: https://cloud.google.com/sql/pricing.')
  if is_patch:
    help_text += ' WARNING: Instance will be restarted.'
  # TODO(b/122660263): Remove when V1 instances are no longer supported.
  # V1 deprecation notice.
  help_text += ('\n\nIMPORTANT: First Generation instances are deprecated. '
                'If you\'re considering any First Generation tiers, we '
                'recommend using Second Generation instead.')

  parser.add_argument('--tier', '-t', required=False, help=help_text)


def AddZone(parser, help_text):
  """Adds the mutually exclusive `--gce-zone` and `--zone` to the parser."""
  zone_group = parser.add_mutually_exclusive_group()
  zone_group.add_argument(
      '--gce-zone',
      required=False,
      action=actions.DeprecationAction(
          '--gce-zone',
          removed=False,
          warn=('Flag `{flag_name}` is deprecated and will be removed by '
                'release 255.0.0. Use `--zone` instead.')),
      help=help_text)
  zone_group.add_argument(
      '--zone',
      required=False,
      help=help_text)


# Database specific flags


def AddDatabaseName(parser):
  parser.add_argument(
      'database',
      completer=DatabaseCompleter,
      help='Cloud SQL database name.')


def AddCharset(parser):
  parser.add_argument(
      '--charset',
      help='Cloud SQL database charset setting, which specifies the '
      'set of symbols and encodings used to store the data in your database. '
      'Each database version may support a different set of charsets.')


def AddCollation(parser):
  parser.add_argument(
      '--collation',
      help='Cloud SQL database collation setting, which specifies '
      'the set of rules for comparing characters in a character set. Each'
      ' database version may support a different set of collations.')


def AddOperationArgument(parser):
  parser.add_argument(
      'operation',
      nargs='+',
      help='An identifier that uniquely identifies the operation.')


# Instance export / import flags.


def AddUriArgument(parser, help_text):
  """Add the 'uri' argument to the parser, with help text help_text."""
  parser.add_argument(
      'uri',
      help=help_text)


def AddDatabase(parser, help_text, required=False):
  """Add the '--database' flag to the parser, with help text help_text."""
  parser.add_argument(
      '--database',
      '-d',
      required=required,
      help=help_text)


def AddDatabaseList(parser, help_text):
  """Add the '--database' list flag to the parser, with help text help_text."""
  parser.add_argument(
      '--database',
      '-d',
      type=arg_parsers.ArgList(min_length=1),
      metavar='DATABASE',
      required=False,
      help=help_text)


def AddUser(parser, help_text):
  """Add the '--user' flag to the parser, with help text help_text."""
  parser.add_argument('--user', help=help_text)

INSTANCES_USERLABELS_FORMAT = ':(settings.userLabels:alias=labels:label=LABELS)'

INSTANCES_FORMAT_COLUMNS = [
    'name', 'databaseVersion', 'firstof(gceZone,region):label=LOCATION',
    'settings.tier',
    'ip_addresses.filter("type:PRIMARY").*extract(ip_address).flatten()'
    '.yesno(no="-"):label=PRIMARY_ADDRESS',
    'ip_addresses.filter("type:PRIVATE").*extract(ip_address).flatten()'
    '.yesno(no="-"):label=PRIVATE_ADDRESS',
    'state:label=STATUS'
]


def GetInstanceListFormat():
  """Returns the table format for listing instances."""
  table_format = '{} table({})'.format(INSTANCES_USERLABELS_FORMAT,
                                       ','.join(INSTANCES_FORMAT_COLUMNS))
  return table_format


OPERATION_FORMAT = """
  table(
    operation,
    operationType:label=TYPE,
    startTime.iso():label=START,
    endTime.iso():label=END,
    error.errors[0].code.yesno(no="-"):label=ERROR,
    state:label=STATUS
  )
"""

OPERATION_FORMAT_BETA = """
  table(
    name,
    operationType:label=TYPE,
    startTime.iso():label=START,
    endTime.iso():label=END,
    error.errors[0].code.yesno(no="-"):label=ERROR,
    status:label=STATUS
  )
"""

CLIENT_CERTS_FORMAT = """
  table(
    commonName:label=NAME,
    sha1Fingerprint,
    expirationTime.yesno(no="-"):label=EXPIRATION
  )
"""

SERVER_CA_CERTS_FORMAT = """
  table(
    sha1Fingerprint,
    expirationTime.yesno(no="-"):label=EXPIRATION
  )
"""

TIERS_FORMAT = """
  table(
    tier,
    region.list():label=AVAILABLE_REGIONS,
    RAM.size(),
    DiskQuota.size():label=DISK
  )
"""

USERS_FORMAT_BETA = """
  table(
    name.yesno(no='(anonymous)'),
    host
  )
"""
