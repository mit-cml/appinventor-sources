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
"""Flags for the `compute network-endpoint-groups` commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags as compute_flags


def MakeNetworkEndpointGroupsArg():
  return compute_flags.ResourceArgument(
      resource_name='network endpoint group',
      zonal_collection='compute.networkEndpointGroups',
      zone_explanation=compute_flags.ZONE_PROPERTY_EXPLANATION)


def AddCreateNegArgsToParser(parser, support_neg_type):
  """Adds flags for creating a network endpoint group to the parser."""
  if support_neg_type:
    base.ChoiceArgument(
        '--neg-type',
        hidden=True,
        choices=['load-balancing'],
        default='load-balancing',
        help_str='The type of network endpoint group to create.'
    ).AddToParser(parser)
  base.ChoiceArgument(
      '--network-endpoint-type',
      hidden=True,
      choices=['gce-vm-ip-port'],
      default='gce-vm-ip-port',
      help_str='The network endpoint type.'
  ).AddToParser(parser)
  parser.add_argument(
      '--network',
      help='Name of the network in which the NEG is created. `default` project '
           'network is used if unspecified.')
  parser.add_argument(
      '--subnet',
      help='Name of the subnet to which all network endpoints belong.\n\n'
           'If not specified, network endpoints may belong to any subnetwork '
           'in the region where the network endpoint group is created.')
  parser.add_argument(
      '--default-port',
      type=int,
      help="""\
      The default port to use if the port number is not specified in the network
      endpoint.

      If this flag isn't specified, then every network endpoint in the network
      endpoint group must have a port specified.
      """)


def AddUpdateNegArgsToParser(parser):
  """Adds flags for updating a network endpoint group to the parser."""
  endpoint_group = parser.add_group(
      mutex=True,
      help='These flags can be specified multiple times to add/remove '
           'multiple endpoints.')
  endpoint_spec = {
      'instance': str,
      'ip': str,
      'port': int
  }
  endpoint_group.add_argument(
      '--add-endpoint',
      action='append',
      type=arg_parsers.ArgDict(spec=endpoint_spec, required_keys=['instance']),
      help="""\
          The network endpoint to add to the network endpoint group. Allowed
          keys are:

          * instance - Name of instance in same zone as network endpoint
            group.

            The VM instance must belong to the network / subnetwork associated
            with the network endpoint group. If the VM instance is deleted, then
            any network endpoint group that has a reference to it is updated.
            The delete causes all network endpoints on the VM to be removed
            from the network endpoint group.

          * ip - Optional IP address of the network endpoint.

            Optional IP address of the network endpoint. If the IP address is
            not specified then, we use the primary IP address for the VM
            instance in the network that the NEG belongs to.

          * port - Optional port for the network endpoint.

            Optional port for the network endpoint. If not specified and the
            networkEndpointType is `GCE_VM_IP_PORT`, the defaultPort for the
            network endpoint group will be used.
        """)

  endpoint_group.add_argument(
      '--remove-endpoint',
      action='append',
      type=arg_parsers.ArgDict(spec=endpoint_spec, required_keys=['instance']),
      help="""\
          The network endpoint to detach from the network endpoint group.
          Allowed keys are:

          * instance - Name of instance in same zone as network endpoint
            group.

          * ip - Optional IP address of the network endpoint.

            If the IP address is not specified then all network endpoints that
            belong to the instance are removed from the NEG.

          * port - Optional port for the network endpoint. Required if the
            network endpoint type is `GCE_VM_IP_PORT`.
      """)
