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
"""Flags and helpers for the compute instance groups commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import managed_instance_groups_utils
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute import completers as compute_completers
from googlecloudsdk.command_lib.compute import flags
from googlecloudsdk.command_lib.compute import scope as compute_scope
from googlecloudsdk.command_lib.util import completers


# TODO(b/110191362): resign from passing whole args to functions in this file


class RegionalInstanceGroupManagersCompleter(
    compute_completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(RegionalInstanceGroupManagersCompleter, self).__init__(
        collection='compute.regionInstanceGroupManagers',
        list_command=('compute instance-groups managed list --uri '
                      '--filter=region:*'),
        **kwargs)


class ZonalInstanceGroupManagersCompleter(
    compute_completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(ZonalInstanceGroupManagersCompleter, self).__init__(
        collection='compute.instanceGroupManagers',
        list_command=('compute instance-groups managed list --uri '
                      '--filter=zone:*'),
        **kwargs)


class InstanceGroupManagersCompleter(completers.MultiResourceCompleter):

  def __init__(self, **kwargs):
    super(InstanceGroupManagersCompleter, self).__init__(
        completers=[RegionalInstanceGroupManagersCompleter,
                    ZonalInstanceGroupManagersCompleter],
        **kwargs)


def MakeZonalInstanceGroupArg(plural=False):
  return flags.ResourceArgument(
      resource_name='instance group',
      completer=compute_completers.InstanceGroupsCompleter,
      plural=plural,
      zonal_collection='compute.instanceGroups',
      zone_explanation=flags.ZONE_PROPERTY_EXPLANATION)

MULTISCOPE_INSTANCE_GROUP_ARG = flags.ResourceArgument(
    resource_name='instance group',
    completer=compute_completers.InstanceGroupsCompleter,
    zonal_collection='compute.instanceGroups',
    regional_collection='compute.regionInstanceGroups',
    zone_explanation=flags.ZONE_PROPERTY_EXPLANATION_NO_DEFAULT,
    region_explanation=flags.REGION_PROPERTY_EXPLANATION_NO_DEFAULT)

MULTISCOPE_INSTANCE_GROUP_MANAGER_ARG = flags.ResourceArgument(
    resource_name='managed instance group',
    completer=InstanceGroupManagersCompleter,
    zonal_collection='compute.instanceGroupManagers',
    regional_collection='compute.regionInstanceGroupManagers',
    zone_explanation=flags.ZONE_PROPERTY_EXPLANATION_NO_DEFAULT,
    region_explanation=flags.REGION_PROPERTY_EXPLANATION_NO_DEFAULT)

MULTISCOPE_INSTANCE_GROUP_MANAGERS_ARG = flags.ResourceArgument(
    resource_name='managed instance group',
    plural=True,
    name='names',
    completer=InstanceGroupManagersCompleter,
    zonal_collection='compute.instanceGroupManagers',
    regional_collection='compute.regionInstanceGroupManagers',
    zone_explanation=flags.ZONE_PROPERTY_EXPLANATION_NO_DEFAULT,
    region_explanation=flags.REGION_PROPERTY_EXPLANATION_NO_DEFAULT)


def AddGroupArg(parser):
  parser.add_argument(
      'group',
      help='The name of the instance group.')


def AddNamedPortsArgs(parser):
  """Adds flags for handling named ports."""
  parser.add_argument(
      '--named-ports',
      required=True,
      type=arg_parsers.ArgList(),
      metavar='NAME:PORT',
      help="""\
          The comma-separated list of key:value pairs representing
          the service name and the port that it is running on.

          To clear the list of named ports pass empty list as flag value.
          For example:

            $ {command} example-instance-group --named-ports ""
          """)


def AddScopeArgs(parser, multizonal):
  """Adds flags for group scope."""
  if multizonal:
    scope_parser = parser.add_mutually_exclusive_group()
    flags.AddRegionFlag(
        scope_parser,
        resource_type='instance group',
        operation_type='set named ports for',
        explanation=flags.REGION_PROPERTY_EXPLANATION_NO_DEFAULT)
    flags.AddZoneFlag(
        scope_parser,
        resource_type='instance group',
        operation_type='set named ports for',
        explanation=flags.ZONE_PROPERTY_EXPLANATION_NO_DEFAULT)
  else:
    flags.AddZoneFlag(
        parser,
        resource_type='instance group',
        operation_type='set named ports for')


def AddZonesFlag(parser):
  """Add flags for choosing zones for regional managed instance group."""
  parser.add_argument(
      '--zones',
      metavar='ZONE',
      help="""\
          If this flag is specified a regional managed instance group will be
          created. The managed instance group will be in the same region as
          specified zones and will spread instances in it between specified
          zones.

          All zones must belong to the same region. You may specify --region
          flag but it must be the region to which zones belong. This flag is
          mutually exclusive with --zone flag.""",
      type=arg_parsers.ArgList(min_length=1),
      completer=compute_completers.ZonesCompleter,
      default=[])


def ValidateManagedInstanceGroupScopeArgs(args, resources):
  """Validate arguments specifying scope of the managed instance group."""
  ignored_required_params = {'project': 'fake'}
  if args.zones and args.zone:
    raise exceptions.ConflictingArgumentsException('--zone', '--zones')
  zone_names = []
  for zone in args.zones:
    zone_ref = resources.Parse(
        zone, collection='compute.zones', params=ignored_required_params)
    zone_names.append(zone_ref.Name())

  zone_regions = set([utils.ZoneNameToRegionName(z) for z in zone_names])
  if len(zone_regions) > 1:
    raise exceptions.InvalidArgumentException(
        '--zones', 'All zones must be in the same region.')
  elif len(zone_regions) == 1 and args.region:
    zone_region = zone_regions.pop()
    region_ref = resources.Parse(args.region, collection='compute.regions',
                                 params=ignored_required_params)
    region = region_ref.Name()
    if zone_region != region:
      raise exceptions.InvalidArgumentException(
          '--zones', 'Specified zones not in specified region.')


def ValidateManagedInstanceGroupStatefulProperties(args):
  if args.IsSpecified('stateful_disks') and args.IsSpecified(
      'stateful_names') and not args.GetValue('stateful_names'):
    raise exceptions.ConflictingArgumentsException('--stateful-disks',
                                                   '--no-stateful-names')


def GetInstanceGroupManagerArg(zones_flag=False, region_flag=True):
  """Returns ResourceArgument for working with instance group managers."""
  if zones_flag:
    extra_region_info_about_zones_flag = (
        '\n\nIf you specify `--zones` flag this flag must be unspecified '
        'or specify the region to which the zones you listed belong.'
    )
    region_explanation = (flags.REGION_PROPERTY_EXPLANATION_NO_DEFAULT +
                          extra_region_info_about_zones_flag)
  else:
    region_explanation = flags.REGION_PROPERTY_EXPLANATION_NO_DEFAULT
  if region_flag:
    regional_collection = 'compute.regionInstanceGroupManagers'
  else:
    regional_collection = None
  return flags.ResourceArgument(
      resource_name='managed instance group',
      completer=InstanceGroupManagersCompleter,
      zonal_collection='compute.instanceGroupManagers',
      regional_collection=regional_collection,
      zone_explanation=flags.ZONE_PROPERTY_EXPLANATION_NO_DEFAULT,
      region_explanation=region_explanation)


def CreateGroupReference(client, resources, args):
  resource_arg = GetInstanceGroupManagerArg()
  default_scope = compute_scope.ScopeEnum.ZONE
  scope_lister = flags.GetDefaultScopeLister(client)
  return resource_arg.ResolveAsResource(
      args, resources, default_scope=default_scope,
      scope_lister=scope_lister)


def AddSettingStatefulDisksFlag(parser, required=False):
  """Add --stateful-disks and --no-stateful-disks flags to the parser."""
  # TODO(b/69900323): merge this function with AddMigStatefulFlags
  stateful_disks = parser.add_mutually_exclusive_group(required=required)
  stateful_disks.add_argument(
      '--stateful-disks',
      metavar='DEVICE_NAME',
      type=arg_parsers.ArgList(min_length=1),
      help=('Disks considered stateful by the instance group. Usually, the '
            'managed instance group deletes disks when deleting instances; '
            'however, in the case of stateful disks, these disks are detached '
            'from the deleted instance and attached to new instances the '
            'managed instance group creates.'),
  )
  stateful_disks.add_argument(
      '--no-stateful-disks',
      action='store_true',
      help='The group will have no stateful disks.',
  )


STATEFUL_DISKS_HELP = """
      Disks considered stateful by the instance group. Usually, the
      managed instance group deletes disks when deleting instances;
      however, in the case of stateful disks, these disks are detached
      from the deleted instance and attached to new instances the
      managed instance group creates.
      """


def AddMigStatefulNamesFlag(parser):
  parser.add_argument(
      '--stateful-names',
      action='store_true',
      default=False,
      help='Enable stateful names of instances. Whenever instances with those '
      'names are restarted or recreated, they will have the same names as '
      'before. Use --no-stateful-names to disable stateful names.')


def AddMigCreateStatefulFlags(parser):
  """Adding stateful flags for disks and names to the parser."""
  parser.add_argument(
      '--stateful-disks',
      metavar='DEVICE_NAME',
      type=arg_parsers.ArgList(min_length=1),
      help=STATEFUL_DISKS_HELP,
  )
  AddMigStatefulNamesFlag(parser)


def AddMigStatefulFlagsForInstanceConfigs(parser, for_update=False):
  """Adding stateful flags for creating and updating instance configs."""
  parser.add_argument(
      '--instance',
      required=True,
      help="""
        URI to existing or non existing instance.

        Name - last part of URI - will be preserved for existing per instance
        configs.

        For zonal managed instance groups there is no need to specify the whole
        URI to the instance - for this case instance name can be applied instead
        of URI.
      """)

  stateful_disks_help = STATEFUL_DISKS_HELP + """
      Besides preserving disks already attached to the instance by specifying
      only device names, user have an option to attach (and preserve) other
      existing persistent disk(s) to the given instance.

      The same disk can be attached to many instances but only in read-only
      mode.
      """
  if for_update:
    stateful_disks_help += """
      Use this argument multiple times to update multiple disks.

      If stateful disk with given `device-name` exists in current instance
      config, its properties will be replaced by the newly provided ones. In
      other case new stateful disk definition will be added to the instance
      config.
      """
    stateful_disk_argument_name = '--update-stateful-disk'
  else:
    stateful_disks_help += """
      Use this argument multiple times to attach more disks.
      """
    stateful_disk_argument_name = '--stateful-disk'
  stateful_disks_help += """
      *device-name*::: Name under which disk is or will be attached.

      *source*::: Optional argument used to specify URI of existing persistent
      disk to attach under specified `device-name`.

      *mode*::: Specifies the mode of the disk to attach. Supported options are
      `ro` for read-only and `rw` for read-write. If omitted when source is
      specified, `rw` is used as a default.
      """
  parser.add_argument(
      stateful_disk_argument_name,
      type=arg_parsers.ArgDict(spec={
          'device-name': str,
          'source': str,
          'mode': str,
      }),
      action='append',
      help=stateful_disks_help,
  )
  if for_update:
    parser.add_argument(
        '--remove-stateful-disks',
        metavar='DEVICE_NAME',
        type=arg_parsers.ArgList(min_length=1),
        help=('List all device names which should be removed from current '
              'instance config.'),
    )

  if for_update:
    stateful_metadata_argument_name = '--update-stateful-metadata'
  else:
    stateful_metadata_argument_name = '--stateful-metadata'
  stateful_metadata_help = """
      Additional metadata to be made available to the guest operating system
      on top of the metadata defined in the instance template.

      Stateful metadata may be used to define a key/value pair specific for
      the one given instance to differentiate it from the other instances in
      the managed instance group.

      Stateful metadata have priority over the metadata defined in the
      instance template. It means that stateful metadata defined for the keys
      already existing in the instance template override their values.

      Each metadata entry is a key/value pair separated by an equals sign.
      Metadata keys must be unique and less than 128 bytes in length. Multiple
      entries can be passed to this flag, e.g.,
      ``{argument_name} key-1=value-1,key-2=value-2,key-3=value-3''.
  """.format(argument_name=stateful_metadata_argument_name)
  if for_update:
    stateful_metadata_help += """
      If stateful metadata with the given key exists in current instance config,
      its value will be overridden with the newly provided one. If the key does
      not exist in the current instance config, a new key/value pair will be
      added.
    """
  parser.add_argument(
      stateful_metadata_argument_name,
      type=arg_parsers.ArgDict(min_length=1),
      default={},
      action=arg_parsers.StoreOnceAction,
      metavar='KEY=VALUE',
      help=stateful_metadata_help)
  if for_update:
    parser.add_argument(
        '--remove-stateful-metadata',
        metavar='KEY',
        type=arg_parsers.ArgList(min_length=1),
        help=('List all stateful metadata keys which should be removed from '
              'current instance config.'),
    )


def AddMigStatefulForceInstanceUpdateFlag(parser):
  parser.add_argument(
      '--force-instance-update',
      action='store_true',
      help="""
        The changes will be applied immediately to the instances. If this flag
        is not provided, the changes will be applied once the instances are
        restarted or recreated.

        Example: let's say we have an instance with a disk attached to it and an
        override for the disk. If we decide to delete the override and provide
        this flag, this will instantly recreate the instance and detach the disk
        from it. Similarly if we have attached new disk or changed its
        definition - with this flag it will instantly recreate instance with
        newly applied overrides.

        If we omit this flag, the instance will continue to exist with no
        overrides changes applied until it gets restarted or recreated either
        manually or by autohealer or updater.""")


def ValidateMigStatefulFlagsForInstanceConfigs(args, for_update=False):
  """Validates the values of stateful flags for instance configs."""
  if for_update:
    stateful_disks = args.update_stateful_disk
    flag_name = '--update-stateful-disk'
  else:
    stateful_disks = args.stateful_disk
    flag_name = '--stateful-disk'
  device_names = set()
  for stateful_disk in stateful_disks or []:
    if not stateful_disk.get('device-name'):
      raise exceptions.InvalidArgumentException(
          parameter_name=flag_name, message='[device-name] is required')

    if stateful_disk.get('device-name') in device_names:
      raise exceptions.InvalidArgumentException(
          parameter_name=flag_name,
          message='[device-name] `{0}` is not unique in the collection'.format(
              stateful_disk.get('device-name')))
    device_names.add(stateful_disk.get('device-name'))

    mode_value = stateful_disk.get('mode')
    if mode_value and mode_value not in ('rw', 'ro'):
      raise exceptions.InvalidArgumentException(
          parameter_name=flag_name,
          message='Value for [mode] must be [rw] or [ro], not [{0}]'.format(
              mode_value))

    if mode_value and not stateful_disk.get('source'):
      raise exceptions.InvalidArgumentException(
          parameter_name=flag_name,
          message='[mode] can be set then and only then when [source] is given')

  if for_update:
    remove_stateful_disks_set = set(args.remove_stateful_disks or [])
    for stateful_disk_to_update in args.update_stateful_disk or []:
      if stateful_disk_to_update.get(
          'device-name') in remove_stateful_disks_set:
        raise exceptions.InvalidArgumentException(
            parameter_name=flag_name,
            message=('the same [device-name] `{0}` cannot be updated and'
                     ' removed in one command call'.format(
                         stateful_disk_to_update.get('device-name'))))

    remove_stateful_metadata_set = set(args.remove_stateful_metadata or [])
    update_stateful_metadata_set = set(args.update_stateful_metadata.keys())
    keys_intersection = remove_stateful_metadata_set.intersection(
        update_stateful_metadata_set)
    if keys_intersection:
      raise exceptions.InvalidArgumentException(
          parameter_name=flag_name,
          message=('the same metadata key(s) `{0}` cannot be updated and'
                   ' removed in one command call'.format(
                       ', '.join(keys_intersection))))


def AddMigUpdateStatefulFlags(parser):
  """Add --add-stateful-disks and --remove-stateful-disks to the parser."""
  parser.add_argument(
      '--add-stateful-disks',
      metavar='DEVICE_NAME',
      type=arg_parsers.ArgList(min_length=1),
      help=('Add more disks to be considered stateful by the instance group. '
            'Usually, the managed instance group deletes disks when deleting '
            'instances; however, in the case of stateful disks, these disks '
            'are detached from the deleted instance and attached to new '
            'instances the managed instance group creates.'),
  )
  parser.add_argument(
      '--remove-stateful-disks',
      metavar='DEVICE_NAME',
      type=arg_parsers.ArgList(min_length=1),
      help='Stop considering the disks stateful by the instance group.',
  )
  AddMigStatefulNamesFlag(parser)


def GetValidatedUpdateStatefulPolicyParams(args, current_stateful_policy):
  """Check stateful properties of update request; returns final device list."""
  current_device_names = set(
      managed_instance_groups_utils.GetDeviceNamesFromStatefulPolicy(
          current_stateful_policy))
  if args.add_stateful_disks:
    if any(
        args.add_stateful_disks.count(x) > 1 for x in args.add_stateful_disks):
      raise exceptions.InvalidArgumentException(
          parameter_name='update',
          message=(
              'When adding device names to Stateful Policy, please provide '
              'each name exactly once.'))
  if args.remove_stateful_disks:
    if any(
        args.remove_stateful_disks.count(x) > 1
        for x in args.remove_stateful_disks):
      raise exceptions.InvalidArgumentException(
          parameter_name='update',
          message=(
              'When removing device names from Stateful Policy, please provide '
              'each name exactly once.'))

  add_set = set(args.add_stateful_disks or [])
  remove_set = set(args.remove_stateful_disks or [])
  intersection = add_set.intersection(remove_set)

  if intersection:
    raise exceptions.InvalidArgumentException(
        parameter_name='update',
        message=
        ('You cannot simultaneously add and remove the same device names {} to '
         'Stateful Policy.'.format(str(intersection))))
  not_current_device_names = remove_set - current_device_names
  if not_current_device_names:
    raise exceptions.InvalidArgumentException(
        parameter_name='update',
        message=('Disks [{}] are not currently set as stateful, '
                 'so they cannot be removed from Stateful Policy.'.format(
                     str(not_current_device_names))))
  already_added_device_names = current_device_names & add_set
  if already_added_device_names:
    raise exceptions.InvalidArgumentException(
        parameter_name='update',
        message=('Disks [{}] are currently set as stateful, '
                 'so they cannot be added to Stateful Policy.'))
  final_disks = current_device_names.union(add_set).difference(remove_set)
  if final_disks and args.IsSpecified(
      'stateful_names') and not args.GetValue('stateful_names'):
    raise exceptions.InvalidArgumentException(
        parameter_name='update',
        message=(
            'Stateful Policy is not empty, so you cannot mark instance names '
            'as non-stateful. Current device names are [{}]'.format(
                str(final_disks))))
  return sorted(list(final_disks))


INSTANCE_REDISTRIBUTION_TYPES = ['NONE', 'PROACTIVE']


def AddMigInstanceRedistributionTypeFlag(parser):
  """Add --instance-redistribution-type flag to the parser."""
  parser.add_argument(
      '--instance-redistribution-type',
      metavar='TYPE',
      type=lambda x: x.upper(),
      choices=INSTANCE_REDISTRIBUTION_TYPES,
      help="""\
      Specify type of instance redistribution policy. Instance redistribution
      type gives possibility to enable or disable automatic instance
      redistribution between zones to its target distribution. Target
      distribution is a state of regional managed instance group where all
      instances are spread out equally between all target zones.

      Instance redistribution type may be specified for non-autoscaled regional
      managed instance group only. By default it is set to PROACTIVE.

      The following types are available:

       * NONE - managed instance group will not take any action to bring
         instances to its target distribution.

       * PROACTIVE - managed instance group will actively converge all instances
         between zones to its target distribution.
      """)


def ValidateMigInstanceRedistributionTypeFlag(instance_redistribution_type,
                                              group_ref):
  """Check correctness of instance-redistribution-type flag value."""
  if instance_redistribution_type and (group_ref.Collection() !=
                                       'compute.regionInstanceGroupManagers'):
    raise exceptions.InvalidArgumentException(
        parameter_name='--instance-redistribution-type',
        message=(
            'Flag --instance-redistribution-type may be specified for regional '
            'managed instance groups only.'))
