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
"""Defines tool-wide constants."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import collections

import six

BYTES_IN_ONE_MB = 2 ** 20
BYTES_IN_ONE_GB = 2 ** 30

DEFAULT_STANDARD_DISK_SIZE_GB = 500
DEFAULT_SSD_DISK_SIZE_GB = 100
STANDARD_DISK_PERFORMANCE_WARNING_GB = 200
SSD_DISK_PERFORMANCE_WARNING_GB = 10

# The maximum number of results that can be returned in a single list
# response.
MAX_RESULTS_PER_PAGE = 500

# Defaults for instance creation.
DEFAULT_ACCESS_CONFIG_NAME = 'external-nat'

DEFAULT_MACHINE_TYPE = 'n1-standard-1'
DEFAULT_NETWORK = 'default'
DEFAULT_NETWORK_INTERFACE = 'nic0'
NETWORK_TIER_CHOICES_FOR_INSTANCE = ['PREMIUM', 'SELECT', 'STANDARD']

DEFAULT_IMAGE_FAMILY = 'debian-9'

ImageAlias = collections.namedtuple(
    'ImageAlias', ['project', 'name_prefix', 'family'])

IMAGE_ALIASES = {
    'centos-6': ImageAlias(
        project='centos-cloud',
        name_prefix='centos-6',
        family='centos-6'),
    'centos-7': ImageAlias(
        project='centos-cloud',
        name_prefix='centos-7',
        family='centos-7'),
    'container-vm': ImageAlias(
        project='google-containers',
        name_prefix='container-vm',
        family='container-vm'),
    'coreos': ImageAlias(
        project='coreos-cloud',
        name_prefix='coreos-stable',
        family='coreos-stable'),
    'cos': ImageAlias(
        project='cos-cloud',
        name_prefix='cos',
        family='cos'),
    'debian-8': ImageAlias(
        project='debian-cloud',
        name_prefix='debian-8-jessie',
        family='debian-8'),
    'rhel-6': ImageAlias(
        project='rhel-cloud',
        name_prefix='rhel-6',
        family='rhel-6'),
    'rhel-7': ImageAlias(
        project='rhel-cloud',
        name_prefix='rhel-7',
        family='rhel-7'),
    'sles-11': ImageAlias(
        project='suse-cloud',
        name_prefix='sles-11',
        family=None),
    'sles-12': ImageAlias(
        project='suse-cloud',
        name_prefix='sles-12',
        family=None),
    'ubuntu-12-04': ImageAlias(
        project='ubuntu-os-cloud',
        name_prefix='ubuntu-1204-precise',
        family='ubuntu-1204-lts'),
    'ubuntu-14-04': ImageAlias(
        project='ubuntu-os-cloud',
        name_prefix='ubuntu-1404-trusty',
        family='ubuntu-1404-lts'),
    'windows-2008-r2': ImageAlias(
        project='windows-cloud',
        name_prefix='windows-server-2008-r2',
        family='windows-2008-r2'),
    'windows-2012-r2': ImageAlias(
        project='windows-cloud',
        name_prefix='windows-server-2012-r2',
        family='windows-2012-r2'),
}

# These are like IMAGE_ALIASES, but don't show up in the alias list.
HIDDEN_IMAGE_ALIASES = {
    'gae-builder-vm': ImageAlias(
        project='goog-vmruntime-images',
        name_prefix='gae-builder-vm',
        family=None),
    'opensuse-13': ImageAlias(
        project='opensuse-cloud',
        name_prefix='opensuse-13',
        family=None),
}

WINDOWS_IMAGE_PROJECTS = [
    'windows-cloud',
    'windows-sql-cloud'
]
PUBLIC_IMAGE_PROJECTS = [
    'centos-cloud',
    'coreos-cloud',
    'debian-cloud',
    'cos-cloud',
    'rhel-cloud',
    'rhel-sap-cloud',
    'suse-cloud',
    'suse-sap-cloud',
    'ubuntu-os-cloud',
] + WINDOWS_IMAGE_PROJECTS
PREVIEW_IMAGE_PROJECTS = []

# SSH-related constants.
SSH_KEYS_METADATA_KEY = 'ssh-keys'
SSH_KEYS_LEGACY_METADATA_KEY = 'sshKeys'
SSH_KEYS_BLOCK_METADATA_KEY = 'block-project-ssh-keys'
MAX_METADATA_VALUE_SIZE_IN_BYTES = 262144
SSH_KEY_TYPES = ('ssh-dss', 'ecdsa-sha2-nistp256', 'ssh-ed25519', 'ssh-rsa')

_STORAGE_RO = 'https://www.googleapis.com/auth/devstorage.read_only'
_LOGGING_WRITE = 'https://www.googleapis.com/auth/logging.write'
_MONITORING_WRITE = 'https://www.googleapis.com/auth/monitoring.write'
_MONITORING = 'https://www.googleapis.com/auth/monitoring'
_SERVICE_CONTROL_SCOPE = 'https://www.googleapis.com/auth/servicecontrol'
_SERVICE_MANAGEMENT_SCOPE = 'https://www.googleapis.com/auth/service.management.readonly'
_SOURCE_REPOS = 'https://www.googleapis.com/auth/source.full_control'
_SOURCE_REPOS_RO = 'https://www.googleapis.com/auth/source.read_only'
_PUBSUB = 'https://www.googleapis.com/auth/pubsub'
_STACKDRIVER_TRACE = 'https://www.googleapis.com/auth/trace.append'

DEFAULT_SCOPES = sorted([
    _STORAGE_RO, _LOGGING_WRITE, _MONITORING_WRITE, _SERVICE_CONTROL_SCOPE,
    _SERVICE_MANAGEMENT_SCOPE, _PUBSUB, _STACKDRIVER_TRACE,
])

GKE_DEFAULT_SCOPES = sorted([
    _STORAGE_RO,
    _LOGGING_WRITE,
    _MONITORING,
    _SERVICE_CONTROL_SCOPE,
    _SERVICE_MANAGEMENT_SCOPE,
    _STACKDRIVER_TRACE,
])

DEPRECATED_SQL_SCOPE_MSG = """\
DEPRECATION WARNING: https://www.googleapis.com/auth/sqlservice account scope
and `sql` alias do not provide SQL instance management capabilities and have
been deprecated. Please, use https://www.googleapis.com/auth/sqlservice.admin
or `sql-admin` to manage your Google SQL Service instances.
"""

DEPRECATED_SCOPES_MESSAGES = DEPRECATED_SQL_SCOPE_MSG

DEPRECATED_SCOPE_ALIASES = {'sql'}

SCOPES = {
    'bigquery': ['https://www.googleapis.com/auth/bigquery'],
    'cloud-platform': ['https://www.googleapis.com/auth/cloud-platform'],
    'cloud-source-repos': [_SOURCE_REPOS],
    'cloud-source-repos-ro': [_SOURCE_REPOS_RO],
    'compute-ro': ['https://www.googleapis.com/auth/compute.readonly'],
    'compute-rw': ['https://www.googleapis.com/auth/compute'],
    'default':
        DEFAULT_SCOPES,
    'gke-default':
        GKE_DEFAULT_SCOPES,
    'datastore': ['https://www.googleapis.com/auth/datastore'],
    'logging-write': [_LOGGING_WRITE],
    'monitoring': [_MONITORING],
    'monitoring-write': [_MONITORING_WRITE],
    'service-control': [_SERVICE_CONTROL_SCOPE],
    'service-management': [_SERVICE_MANAGEMENT_SCOPE],
    'sql': ['https://www.googleapis.com/auth/sqlservice'],
    'sql-admin': ['https://www.googleapis.com/auth/sqlservice.admin'],
    'trace': [_STACKDRIVER_TRACE],
    'storage-full': ['https://www.googleapis.com/auth/devstorage.full_control'],
    'storage-ro': [_STORAGE_RO],
    'storage-rw': ['https://www.googleapis.com/auth/devstorage.read_write'],
    'taskqueue': ['https://www.googleapis.com/auth/taskqueue'],
    'userinfo-email': ['https://www.googleapis.com/auth/userinfo.email'],
    'pubsub': ['https://www.googleapis.com/auth/pubsub'],
}


def ScopesHelp():
  """Returns the command help text markdown for scopes.

  Returns:
    The command help text markdown with scope intro text, aliases, and optional
    notes and/or warnings.
  """
  aliases = []
  for alias, value in sorted(six.iteritems(SCOPES)):
    if alias in DEPRECATED_SCOPE_ALIASES:
      alias = '{} (deprecated)'.format(alias)
    aliases.append('{0} | {1}'.format(alias, value[0]))
    for item in value[1:]:
      aliases.append('| ' + item)
  return """\
SCOPE can be either the full URI of the scope or an alias. *default* scopes are
assigned to all instances. Available aliases are:

Alias | URI
--- | ---
{aliases}

{scope_deprecation_msg}
""".format(
    aliases='\n'.join(aliases),
    scope_deprecation_msg=DEPRECATED_SCOPES_MESSAGES)
