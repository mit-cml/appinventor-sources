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

"""Implements the command for starting a tunnel with Cloud IAP."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions as calliope_exceptions
from googlecloudsdk.command_lib.compute import iap_tunnel
from googlecloudsdk.command_lib.compute import scope
from googlecloudsdk.command_lib.compute import ssh_utils
from googlecloudsdk.command_lib.compute.instances import flags
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import log


class ArgumentError(exceptions.Error):
  pass


class NoInterfacesError(exceptions.Error):
  pass


@base.ReleaseTracks(base.ReleaseTrack.BETA, base.ReleaseTrack.ALPHA)
class StartIapTunnel(base.Command):
  """Starts an IAP TCP forwarding tunnel over WebSocket connection.

  Starts a tunnel to the Cloud Identity-Aware Proxy through which another
  process can create a connection (eg. SSH, RDP) to a Google Compute Engine
  instance.
  """

  @staticmethod
  def Args(parser):
    iap_tunnel.AddProxyServerHelperArgs(parser)
    flags.INSTANCE_ARG.AddArgument(parser)
    parser.add_argument(
        'instance_port',
        type=arg_parsers.BoundedInt(lower_bound=1, upper_bound=65535),
        help="The name or number of the instance's port to connect to.")
    parser.add_argument(
        '--local-host-port',
        type=lambda arg: arg_parsers.HostPort.Parse(arg, ipv6_enabled=True),
        default='localhost:0',
        help='Host:port to which the proxy should be bound.')
    # It would be logical to put --local-host-port and --listen-on-stdin in a
    # mutex group, but then the help text would display a message saying "At
    # most one of these may be specified" even though it only shows
    # --local-host-port.
    parser.add_argument(
        '--listen-on-stdin',
        action='store_true',
        hidden=True,
        help=('Whether to get/put local data on stdin/stdout instead of '
              'listening on a socket.  It is an error to specify '
              '--local-host-port with this, because that flag has no meaning '
              'with this.'))
    parser.add_argument(
        '--network-interface',
        default='nic0',
        help=('Specifies the name of the instance network interface to connect '
              'to. If this is not provided, then "nic0" is used as the '
              'default.'))

  def Run(self, args):
    if args.listen_on_stdin and args.IsSpecified('local_host_port'):
      raise calliope_exceptions.ConflictingArgumentsException(
          '--listen-on-stdin', '--local-host-port')
    project, zone, instance, interface, port = self._GetTargetArgs(args)

    if args.listen_on_stdin:
      iap_tunnel_helper = iap_tunnel.IapTunnelStdinHelper(
          args, project, zone, instance, interface, port)
      iap_tunnel_helper.Run()
    else:
      local_host, local_port = self._GetLocalHostPort(args)
      iap_tunnel_helper = iap_tunnel.IapTunnelProxyServerHelper(
          args, project, zone, instance, interface, port, local_host,
          local_port)
      iap_tunnel_helper.StartProxyServer()

  def _GetTargetArgs(self, args):
    holder = base_classes.ComputeApiHolder(base.ReleaseTrack.GA)
    client = holder.client
    ssh_helper = ssh_utils.BaseSSHCLIHelper()

    instance_ref = flags.SSH_INSTANCE_RESOLVER.ResolveResources(
        [args.instance_name], scope.ScopeEnum.ZONE, args.zone, holder.resources,
        scope_lister=flags.GetInstanceZoneScopeLister(client))[0]
    instance_obj = ssh_helper.GetInstance(client, instance_ref)

    project = instance_ref.project
    zone = instance_ref.zone
    instance = instance_obj.name
    port = args.instance_port
    interface = self._GetInterface(args, instance_obj)

    return project, zone, instance, interface, port

  def _GetInterface(self, args, instance_obj):
    if not instance_obj.networkInterfaces:
      raise NoInterfacesError('No network interfaces found for instance [%s]' %
                              instance_obj.name)
    interface = args.network_interface
    if interface:
      available_interfaces = [ni.name for ni in instance_obj.networkInterfaces]
      if interface not in available_interfaces:
        raise ArgumentError(
            'Invalid value for [--network-interface]: interface [%s] not found'
            % interface)
    else:
      interface = instance_obj.networkInterfaces[0].name
      log.status.Print('Defaulting to first interface found [%s].' % interface)
    return interface

  def _GetLocalHostPort(self, args):
    local_host_arg = args.local_host_port.host or 'localhost'
    port_arg = (
        int(args.local_host_port.port) if args.local_host_port.port else 0)
    local_port = iap_tunnel.DetermineLocalPort(port_arg=port_arg)
    if not port_arg:
      log.status.Print('Picking local unused port [%d].' % local_port)
    return local_host_arg, local_port
