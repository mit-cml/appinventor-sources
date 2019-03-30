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

"""Helper methods for importing record-sets."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import re

from dns import rdatatype
from dns import zone
from googlecloudsdk.api_lib.util import apis as core_apis
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import yaml
from googlecloudsdk.core.util import encoding


class Error(exceptions.Error):
  """Base exception for all import errors."""


class RecordsFileNotFound(Error):
  """The specified records file was not found."""


class RecordsFileIsADirectory(Error):
  """The specified records file is a directory."""


class UnableToReadRecordsFile(Error):
  """Unable to read record sets from the specified records file."""


class ConflictingRecordsFound(Error):
  """Conflicts found between records being imported and current records."""


def _AddressTranslation(rdata, unused_origin):
  """Returns the address of the given rdata.

  Args:
    rdata: Rdata, The data to be translated.
    unused_origin: Name, The origin domain name.

  Returns:
    str, The address of the given rdata.
  """
  return rdata.address


def _CAATranslation(rdata, unused_origin):
  """Returns the translation of the given CAA rdata.

  Args:
    rdata: Rdata, The data to be translated.
    unused_origin: Name, The origin domain name.

  Returns:
    str, The translation of the given CAA rdata. See RFC 6844.
  """
  return '{0} {1} {2}'.format(rdata.flags, encoding.Decode(rdata.tag),
                              QuotedText(rdata.value))


def _MXTranslation(rdata, origin):
  """Returns the translation of the given MX rdata.

  Args:
    rdata: Rdata, The data to be translated.
    origin: Name, The origin domain name.

  Returns:
    str, The translation of the given MX rdata which includes the preference and
    qualified exchange name.
  """
  return '{0} {1}'.format(rdata.preference, rdata.exchange.derelativize(origin))


def _SOATranslation(rdata, origin):
  """Returns the translation of the given SOA rdata.

  Args:
    rdata: Rdata, The data to be translated.
    origin: Name, The origin domain name.

  Returns:
    str, The translation of the given SOA rdata which includes all the required
    SOA fields. Note that the master NS name is left in a substitutable form
    because it is always provided by Cloud DNS.
  """
  return ' '.join(
      str(x) for x in [
          '{0}',
          rdata.rname.derelativize(origin),
          rdata.serial,
          rdata.refresh,
          rdata.retry,
          rdata.expire,
          rdata.minimum])


def _SRVTranslation(rdata, origin):
  """Returns the translation of the given SRV rdata.

  Args:
    rdata: Rdata, The data to be translated.
    origin: Name, The origin domain name.

  Returns:
    str, The translation of the given SRV rdata which includes all the required
    SRV fields. Note that the translated target name is always qualified.
  """
  return ' '.join(
      str(x) for x in [
          rdata.priority,
          rdata.weight,
          rdata.port,
          rdata.target.derelativize(origin)])


def _TargetTranslation(rdata, origin):
  """Returns the qualified target of the given rdata.

  Args:
    rdata: Rdata, The data to be translated.
    origin: Name, The origin domain name.

  Returns:
    str, The qualified target of the given rdata.
  """
  return rdata.target.derelativize(origin).to_text()


def QuotedText(text):
  """Returns the given text within quotes.

  Args:
    text: str, The text to be escaped.

  Returns:
    str, The given text within quotes. For further details on why this is
    necessary, please look at the TXT section at:
    https://cloud.google.com/dns/what-is-cloud-dns#supported_record_types.
  """
  text = encoding.Decode(text)
  if text.startswith('"') and text.endswith('"'):
    # Nothing to do if already escaped.
    return text
  else:
    return '"{0}"'.format(text)


def _QuotedTextTranslation(rdata, unused_origin=None):
  """Returns the escaped translation of the given text rdata.

  Args:
    rdata: Rdata, The data to be translated.
    unused_origin: Name, The origin domain name.

  Returns:
    str, The translation of the given text rdata, which is the concatenation of
    all its strings. The result is escaped with quotes. For further details,
    please refer to the TXT section at:
    https://cloud.google.com/dns/what-is-cloud-dns#supported_record_types.
  """
  return ' '.join([QuotedText(string) for string in rdata.strings])


def _NullTranslation(rdata, origin=None):
  """Returns the given rdata as text (formatted by its .to_text() method).

  Args:
    rdata: Rdata, The data to be translated.
    origin: Name, The origin domain name.

  Returns:
    str, The textual presentation form of the given rdata.
  """
  return rdata.to_text(origin=origin)


def GetRdataTranslation(rr_type):
  """Returns the rdata translation function for a record type.

  Args:
    rr_type: The record type

  Returns:
    The record type's translation function.
  """
  rdata_translations = {
      rdatatype.A: _AddressTranslation,
      rdatatype.AAAA: _AddressTranslation,
      rdatatype.CNAME: _TargetTranslation,
      rdatatype.DNSKEY: _NullTranslation,
      rdatatype.DS: _NullTranslation,
      rdatatype.IPSECKEY: _NullTranslation,
      rdatatype.MX: _MXTranslation,
      rdatatype.PTR: _TargetTranslation,
      rdatatype.SOA: _SOATranslation,
      rdatatype.SPF: _QuotedTextTranslation,
      rdatatype.SRV: _SRVTranslation,
      rdatatype.SSHFP: _NullTranslation,
      rdatatype.TXT: _QuotedTextTranslation,
      rdatatype.TLSA: _NullTranslation,
      rdatatype.NAPTR: _NullTranslation,
      rdatatype.NS: _TargetTranslation,
  }
  # This is needed for the rest of gcloud to work when dnspython doesn't support
  # CAA records.  Otherwise, this fails when we try to access the rdatatype.CAA
  # attribute, making anything that calls this code fail.
  try:
    rdata_translations[rdatatype.CAA] = _CAATranslation
  except AttributeError:
    pass

  return rdata_translations.get(rr_type)


def _FilterOutRecord(name, rdtype, origin, replace_origin_ns=False):
  """Returns whether the given record should be filtered out.

  Args:
    name: string, The name of the resord set we are considering
    rdtype: RDataType, type of Record we are considering approving.
    origin: Name, The origin domain of the zone we are considering
    replace_origin_ns: Bool, Whether origin NS records should be imported

  Returns:
    True if the given record should be filtered out, false otherwise.
  """

  if replace_origin_ns:
    return False
  elif name == origin and rdtype == rdatatype.NS:
    return True
  else:
    return False


def _RecordSetFromZoneRecord(name, rdset, origin, api_version='v1'):
  """Returns the Cloud DNS ResourceRecordSet for the given zone file record.

  Args:
    name: Name, Domain name of the zone record.
    rdset: Rdataset, The zone record object.
    origin: Name, The origin domain of the zone file.
    api_version: [str], the api version to use for creating the records.

  Returns:
    The ResourceRecordSet equivalent for the given zone record, or None for
    unsupported record types.
  """
  if GetRdataTranslation(rdset.rdtype) is None:
    return None

  messages = core_apis.GetMessagesModule('dns', api_version)
  record_set = messages.ResourceRecordSet()
  # Need to assign kind to default value for useful equals comparisons.
  record_set.kind = record_set.kind
  record_set.name = name.derelativize(origin).to_text()
  record_set.ttl = rdset.ttl
  record_set.type = rdatatype.to_text(rdset.rdtype)
  rdatas = []
  for rdata in rdset:
    rdatas.append(GetRdataTranslation(rdset.rdtype)(rdata, origin))
  record_set.rrdatas = rdatas
  return record_set


def RecordSetsFromZoneFile(zone_file, domain, api_version='v1'):
  """Returns record-sets for the given domain imported from the given zone file.

  Args:
    zone_file: file, The zone file with records for the given domain.
    domain: str, The domain for which record-sets should be obtained.
    api_version: [str], the api version to use for creating the records.

  Returns:
    A (name, type) keyed dict of ResourceRecordSets that were obtained from the
    zone file. Note that only A, AAAA, CNAME, MX, PTR, SOA, SPF, SRV, and TXT
    record-sets are retrieved. Other record-set types are not supported by Cloud
    DNS. Also, the master NS field for SOA records is discarded since that is
    provided by Cloud DNS.
  """
  zone_contents = zone.from_file(zone_file, domain, check_origin=False)
  record_sets = {}
  for name, rdset in zone_contents.iterate_rdatasets():
    record_set = _RecordSetFromZoneRecord(
        name, rdset, zone_contents.origin, api_version=api_version)
    if record_set:
      record_sets[(record_set.name, record_set.type)] = record_set
  return record_sets


def RecordSetsFromYamlFile(yaml_file, api_version='v1'):
  """Returns record-sets read from the given yaml file.

  Args:
    yaml_file: file, A yaml file with records.
    api_version: [str], the api version to use for creating the records.

  Returns:
    A (name, type) keyed dict of ResourceRecordSets that were obtained from the
    yaml file. Note that only A, AAAA, CNAME, MX, PTR, SOA, SPF, SRV, and TXT
    record-sets are retrieved. Other record-set types are not supported by Cloud
    DNS. Also, the master NS field for SOA records is discarded since that is
    provided by Cloud DNS.
  """
  record_sets = {}
  messages = core_apis.GetMessagesModule('dns', api_version)

  yaml_record_sets = yaml.load_all(yaml_file)
  for yaml_record_set in yaml_record_sets:
    rdata_type = rdatatype.from_text(yaml_record_set['type'])
    if GetRdataTranslation(rdata_type) is None:
      continue

    record_set = messages.ResourceRecordSet()
    # Need to assign kind to default value for useful equals comparisons.
    record_set.kind = record_set.kind
    record_set.name = yaml_record_set['name']
    record_set.ttl = yaml_record_set['ttl']
    record_set.type = yaml_record_set['type']
    record_set.rrdatas = yaml_record_set['rrdatas']

    if rdata_type is rdatatype.SOA:
      # Make master NS name substitutable.
      record_set.rrdatas[0] = re.sub(r'\S+', '{0}', record_set.rrdatas[0],
                                     count=1)

    record_sets[(record_set.name, record_set.type)] = record_set

  return record_sets


def _RecordSetCopy(record_set, api_version='v1'):
  """Returns a copy of the given record-set.

  Args:
    record_set: ResourceRecordSet, Record-set to be copied.
    api_version: [str], the api version to use for creating the records.

  Returns:
    Returns a copy of the given record-set.
  """
  messages = core_apis.GetMessagesModule('dns', api_version)
  copy = messages.ResourceRecordSet()
  copy.kind = record_set.kind
  copy.name = record_set.name
  copy.type = record_set.type
  copy.ttl = record_set.ttl
  copy.rrdatas = list(record_set.rrdatas)
  return copy


def _SOAReplacement(current_record, record_to_be_imported, api_version='v1'):
  """Returns the replacement SOA record with restored master NS name.

  Args:
    current_record: ResourceRecordSet, Current record-set.
    record_to_be_imported: ResourceRecordSet, Record-set to be imported.
    api_version: [str], the api version to use for creating the records.

  Returns:
    ResourceRecordSet, the replacement SOA record with restored master NS name.
  """
  replacement = _RecordSetCopy(record_to_be_imported, api_version=api_version)
  replacement.rrdatas[0] = replacement.rrdatas[0].format(
      current_record.rrdatas[0].split()[0])

  if replacement == current_record:
    # There should always be a different 'next' SOA record.
    return NextSOARecordSet(replacement, api_version)
  else:
    return replacement


def _RDataReplacement(current_record, record_to_be_imported, api_version='v1'):
  """Returns a record-set containing rrdata to be imported.

  Args:
    current_record: ResourceRecordSet, Current record-set.
    record_to_be_imported: ResourceRecordSet, Record-set to be imported.
    api_version: [str], the api version to use for creating the records.

  Returns:
    ResourceRecordSet, a record-set containing rrdata to be imported.
    None, if rrdata to be imported is identical to current rrdata.
  """
  replacement = _RecordSetCopy(record_to_be_imported, api_version=api_version)
  if replacement == current_record:
    return None
  else:
    return replacement


# Map of functions for replacing rdata of a record-set with rdata from another
# record-set with the same name and type.
_RDATA_REPLACEMENTS = {
    rdatatype.A: _RDataReplacement,
    rdatatype.AAAA: _RDataReplacement,
    rdatatype.CAA: _RDataReplacement,
    rdatatype.CNAME: _RDataReplacement,
    rdatatype.DNSKEY: _RDataReplacement,
    rdatatype.DS: _RDataReplacement,
    rdatatype.IPSECKEY: _RDataReplacement,
    rdatatype.MX: _RDataReplacement,
    rdatatype.PTR: _RDataReplacement,
    rdatatype.SOA: _SOAReplacement,
    rdatatype.SPF: _RDataReplacement,
    rdatatype.SRV: _RDataReplacement,
    rdatatype.SSHFP: _RDataReplacement,
    rdatatype.TXT: _RDataReplacement,
    rdatatype.TLSA: _RDataReplacement,
    rdatatype.NAPTR: _RDataReplacement,
    rdatatype.NS: _RDataReplacement,
}


def NextSOARecordSet(soa_record_set, api_version='v1'):
  """Returns a new SOA record set with an incremented serial number.

  Args:
    soa_record_set: ResourceRecordSet, Current SOA record-set.
    api_version: [str], the api version to use for creating the records.

  Returns:
    A a new SOA record-set with an incremented serial number.
  """
  next_soa_record_set = _RecordSetCopy(soa_record_set, api_version=api_version)
  rdata_parts = soa_record_set.rrdatas[0].split()
  # Increment the 32 bit serial number by one and wrap around if needed.
  rdata_parts[2] = str((int(rdata_parts[2]) + 1) % (1 << 32))
  next_soa_record_set.rrdatas[0] = ' '.join(rdata_parts)
  return next_soa_record_set


def IsOnlySOAIncrement(change, api_version='v1'):
  """Returns True if the change only contains an SOA increment, False otherwise.

  Args:
    change: Change, the change to be checked
    api_version: [str], the api version to use for creating the records.

  Returns:
    True if the change only contains an SOA increment, False otherwise.
  """
  return (
      len(change.additions) == len(change.deletions) == 1 and
      rdatatype.from_text(change.deletions[0].type) is rdatatype.SOA and
      NextSOARecordSet(change.deletions[0], api_version) == change.additions[0])


def _NameAndType(record):
  return '{0} {1}'.format(record.name, record.type)


def ComputeChange(current, to_be_imported, replace_all=False,
                  origin=None, replace_origin_ns=False, api_version='v1'):
  """Returns a change for importing the given record-sets.

  Args:
    current: dict, (name, type) keyed dict of current record-sets.
    to_be_imported: dict, (name, type) keyed dict of record-sets to be imported.
    replace_all: bool, Whether the record-sets to be imported should replace the
      current record-sets.
    origin: string, the name of the apex zone ex. "foo.com"
    replace_origin_ns: bool, Whether origin NS records should be imported.
    api_version: [str], the api version to use for creating the records.

  Raises:
    ConflictingRecordsFound: If conflicting records are found.

  Returns:
    A Change that describes the actions required to import the given
    record-sets.
  """
  messages = core_apis.GetMessagesModule('dns', api_version)
  change = messages.Change()
  change.additions = []
  change.deletions = []

  current_keys = set(current.keys())
  keys_to_be_imported = set(to_be_imported.keys())

  intersecting_keys = current_keys.intersection(keys_to_be_imported)
  if not replace_all and intersecting_keys:
    raise ConflictingRecordsFound(
        'The following records (name type) already exist: {0}'.format(
            [_NameAndType(current[key]) for key in sorted(intersecting_keys)]))

  for key in intersecting_keys:
    current_record = current[key]
    record_to_be_imported = to_be_imported[key]
    rdtype = rdatatype.from_text(key[1])
    if not _FilterOutRecord(current_record.name,
                            rdtype,
                            origin,
                            replace_origin_ns):
      replacement = _RDATA_REPLACEMENTS[rdtype](
          current_record, record_to_be_imported, api_version=api_version)
      if replacement:
        change.deletions.append(current_record)
        change.additions.append(replacement)

  for key in keys_to_be_imported.difference(current_keys):
    change.additions.append(to_be_imported[key])

  for key in current_keys.difference(keys_to_be_imported):
    current_record = current[key]
    rdtype = rdatatype.from_text(key[1])
    if rdtype is rdatatype.SOA:
      change.deletions.append(current_record)
      change.additions.append(NextSOARecordSet(current_record, api_version))
    elif replace_all and not _FilterOutRecord(current_record.name,
                                              rdtype,
                                              origin,
                                              replace_origin_ns):
      change.deletions.append(current_record)

  # If the only change is an SOA increment, there is nothing to be done.
  if IsOnlySOAIncrement(change, api_version):
    return None

  change.additions.sort(key=_NameAndType)
  change.deletions.sort(key=_NameAndType)
  return change
