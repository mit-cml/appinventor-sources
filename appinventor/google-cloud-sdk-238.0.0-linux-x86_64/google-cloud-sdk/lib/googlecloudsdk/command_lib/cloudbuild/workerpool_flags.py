# -*- coding: utf-8 -*- #
# Copyright 2018 Google Inc. All Rights Reserved.
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
"""Set up flags for creating or updating a workerpool."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import arg_parsers

_CREATE_FILE_DESC = ('A file that contains the configuration for the '
                     'WorkerPool to be created.')
_UPDATE_FILE_DESC = ('A file that contains updates to the configuration for '
                     'the WorkerPool.')


def AddWorkerpoolArgs(parser, update=False):
  """Set up all the argparse flags for creating or updating a workerpool.

  Args:
    parser: An argparse.ArgumentParser-like object.
    update: If true, use the version of the flags for updating a workerpool.
      Otherwise, use the version for creating a workerpool.

  Returns:
    The parser argument with workerpool flags added in.
  """
  verb = 'update' if update else 'create'
  file_or_flags = parser.add_mutually_exclusive_group(required=True)
  file_or_flags.add_argument(
      '--config-from-file',
      help=(_UPDATE_FILE_DESC if update else _CREATE_FILE_DESC),
  )
  flags = file_or_flags.add_argument_group(
      'Command-line flags to configure the WorkerPool:')
  flags.add_argument(
      'WORKER_POOL',
      help='The WorkerPool to %s.' % verb,
  )
  flags.add_argument(
      '--worker-count',
      help='Total number of workers to be created across all requested '
      'regions.',
  )
  if update:
    region_flags = flags.add_argument_group(help="""\
Update the Cloud region or regions in which the Workerpool is located.
To overwrite regions, use --clear-regions followed by --add-regions in the same
command.
Choices: us-central1, us-west1, us-east1, and us-east4.
""")
    region_flags.add_argument(
        '--add-regions',
        type=arg_parsers.ArgList(),
        metavar='REGION',
        help='Add regions, separated by comma.',
    )
    region_flags.add_argument(
        '--clear-regions',
        action='store_true',
        help='Remove all regions.',
    )
    region_flags.add_argument(
        '--remove-regions',
        type=arg_parsers.ArgList(),
        metavar='REGION',
        help='Remove regions, separated by comma.',
    )
  else:
    flags.add_argument(
        '--regions',
        type=arg_parsers.ArgList(),
        metavar='REGION',
        help="""\
The Cloud region or regions in which to create the WorkerPool.

Choices: us-central1, us-west1, us-east1, us-east4.
""")
  worker_flags = flags.add_argument_group(
      'Configuration to be used for creating workers in the WorkerPool:')
  worker_flags.add_argument(
      '--worker-machine-type',
      help="""\
Machine Type of the worker, such as n1-standard-1.

See https://cloud.google.com/compute/docs/machine-types.

If left blank, Cloud Build will use a standard unspecified machine to create the
worker pool.

`--worker-machine-type` is overridden if you specify a different machine type
using `--machine-type` during `gcloud builds submit`.
""")
  worker_flags.add_argument(
      '--worker-disk-size',
      type=arg_parsers.BinarySize(lower_bound='100GB'),
      help="""\
Size of the disk attached to the worker.

If not given, Cloud Build will use a standard disk size. `--worker-disk-size` is
overridden if you specify a different disk size using `--disk-size` during
`gcloud builds submit`.
""")
  worker_network_flags = worker_flags.add_argument_group(help="""\
The network definition used to create the worker.

If all of these flags are unused, the workers will be created in the
WorkerPool's project on the default network. You cannot specify just one of
these flags: it is all or none. However, you can set them to the empty string in
order to use the default settings.
""")
  worker_network_flags.add_argument(
      '--worker-network-project',
      help="""\
ID of the project containing the given network and subnet.

The workerpool's project is used if empty string.
""")
  worker_network_flags.add_argument(
      '--worker-network-name',
      help="""\
Network on which the workers are created.

`default` network is used if empty string.
""")
  worker_network_flags.add_argument(
      '--worker-network-subnet',
      help="""\
Subnet on which the workers are created.

`default` subnet is used if empty string.
""")
  worker_flags.add_argument(
      '--worker-tag',
      help="""\
The tag applied to the worker, and the same tag used by the firewall rule.

It is used to identify the Cloud Build workers among other VMs. The default
value for tag is `worker`.
""")
  return parser


def AddWorkerpoolCreateArgs(parser):
  """Set up all the argparse flags for creating a workerpool.

  Args:
    parser: An argparse.ArgumentParser-like object.

  Returns:
    The parser argument with workerpool flags added in.
  """
  return AddWorkerpoolArgs(parser, update=False)


def AddWorkerpoolUpdateArgs(parser):
  """Set up all the argparse flags for updating a workerpool.

  Args:
    parser: An argparse.ArgumentParser-like object.

  Returns:
    The parser argument with workerpool flags added in.
  """
  return AddWorkerpoolArgs(parser, update=True)
