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

"""gcloud dns record-sets import command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import os

from apitools.base.py import exceptions as apitools_exceptions
from apitools.base.py import list_pager
from googlecloudsdk.api_lib.dns import import_util
from googlecloudsdk.api_lib.dns import util
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions as calliope_exceptions
from googlecloudsdk.command_lib.dns import flags
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.util import files


@base.UnicodeIsSupported
class Import(base.Command):
  """Import record-sets into your managed-zone.

  This command imports record-sets contained within the specified record-sets
  file into your managed-zone. Note that NS records for the origin of the zone,
  and the SOA NS field, are not imported since name-servers are managed by
  Cloud DNS. By default, record-sets cannot be imported if there are any
  conflicts. A conflict exists if an existing record-set has the same name and
  type as a record-set that is being imported. In contrast, if the
  --delete-all-existing flag is used, the imported record-sets will replace all
  the records-sets currently in the managed-zone.

  ## EXAMPLES

  To import record-sets from a yaml record-sets file, run:

    $ {command} YAML_RECORDS_FILE --zone MANAGED_ZONE

  To import record-sets from a zone file, run:

    $ {command} ZONE_FILE --zone-file-format --zone MANAGED_ZONE

  To replace all the record-sets in your zone with records from a yaml
  file, run:

    $ {command} YAML_RECORDS_FILE --delete-all-existing --zone MANAGED_ZONE
  """

  @staticmethod
  def Args(parser):
    flags.GetZoneArg().AddToParser(parser)
    parser.add_argument('records_file',
                        help='File from which record-sets should be imported.')
    parser.add_argument(
        '--zone-file-format',
        required=False,
        action='store_true',
        help=('Indicates that the input records-file is in BIND zone format. '
              'If omitted, indicates that the records-file is in YAML format.'))
    parser.add_argument(
        '--delete-all-existing',
        required=False,
        action='store_true',
        help='Indicates that all existing record-sets should be deleted before'
        ' importing the record-sets in the records-file.')
    parser.add_argument(
        '--replace-origin-ns',
        required=False,
        action='store_true',
        help='Indicates that NS records for the origin of a zone should be'
        ' imported if defined')
    parser.display_info.AddFormat(flags.CHANGES_FORMAT)

  def Run(self, args):
    api_version = 'v1'
    # If in the future there are differences between API version, do NOT use
    # this patter of checking ReleaseTrack. Break this into multiple classes.
    if self.ReleaseTrack() == base.ReleaseTrack.BETA:
      api_version = 'v1beta2'

    if not os.path.exists(args.records_file):
      raise import_util.RecordsFileNotFound(
          'Specified record file [{0}] not found.'.format(args.records_file))
    if os.path.isdir(args.records_file):
      raise import_util.RecordsFileIsADirectory(
          'Specified record file [{0}] is a directory'.format(
              args.records_file))

    dns = apis.GetClientInstance('dns', api_version)

    # Get the managed-zone.
    zone_ref = util.GetRegistry(api_version).Parse(
        args.zone,
        params={
            'project': properties.VALUES.core.project.GetOrFail,
        },
        collection='dns.managedZones')

    try:
      zone = dns.managedZones.Get(
          dns.MESSAGES_MODULE.DnsManagedZonesGetRequest(
              project=zone_ref.project,
              managedZone=zone_ref.managedZone))
    except apitools_exceptions.HttpError as error:
      raise calliope_exceptions.HttpException(error)

    # Get the current record-sets.
    current = {}
    for record in list_pager.YieldFromList(
        dns.resourceRecordSets,
        dns.MESSAGES_MODULE.DnsResourceRecordSetsListRequest(
            project=zone_ref.project,
            managedZone=zone_ref.Name()),
        field='rrsets'):
      current[(record.name, record.type)] = record

    # Get the imported record-sets.
    try:
      with files.FileReader(args.records_file) as import_file:
        if args.zone_file_format:
          imported = import_util.RecordSetsFromZoneFile(
              import_file, zone.dnsName, api_version=api_version)
        else:
          imported = import_util.RecordSetsFromYamlFile(
              import_file, api_version=api_version)
    except Exception as exp:
      msg = ('Unable to read record-sets from specified records-file [{0}] '
             'because [{1}]')
      msg = msg.format(args.records_file, exp.message)
      raise import_util.UnableToReadRecordsFile(msg)

    # Get the change resulting from the imported record-sets.
    change = import_util.ComputeChange(current, imported,
                                       args.delete_all_existing,
                                       zone.dnsName, args.replace_origin_ns,
                                       api_version=api_version)
    if not change:
      msg = 'Nothing to do, all the records in [{0}] already exist.'.format(
          args.records_file)
      log.status.Print(msg)
      return None

    # Send the change to the service.
    result = dns.changes.Create(
        dns.MESSAGES_MODULE.DnsChangesCreateRequest(
            change=change,
            managedZone=zone.name,
            project=zone_ref.project))
    change_ref = util.GetRegistry(api_version).Create(
        collection='dns.changes',
        project=zone_ref.project,
        managedZone=zone.name,
        changeId=result.id)
    msg = 'Imported record-sets from [{0}] into managed-zone [{1}].'.format(
        args.records_file, zone_ref.Name())
    log.status.Print(msg)
    log.CreatedResource(change_ref)
    return result
