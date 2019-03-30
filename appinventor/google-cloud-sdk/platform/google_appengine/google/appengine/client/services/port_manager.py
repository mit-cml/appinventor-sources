#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
"""A helper file with a helper class for opening ports."""

import logging
import re

# These ports are reserved for future usage.
RESERVED_INTERNAL_PORTS = range(10400, 10500)

# These ports are used by our code or critical system daemons.
RESERVED_HOST_PORTS = [22,  # SSH
                       10000,  # For unlocking?
                       10001,  # Nanny stubby proxy endpoint
                       11211,
                      ] + RESERVED_INTERNAL_PORTS
# We allow users to forward traffic to our HTTP server internally.
RESERVED_DOCKER_PORTS = [22,  # SSH
                         10001,  # Nanny stubby proxy endpoint
                         11211,
                        ] + RESERVED_INTERNAL_PORTS

PROTOCOL_RE = '^tcp|udp$'  # Matches only exactly tcp or udp.


class InconsistentPortConfigurationError(Exception):
  """The port is already in use."""
  pass


class IllegalPortConfigurationError(Exception):
  """Raised if the port configuration is illegal."""
  pass


class PortManager(object):
  """A helper class for VmManager to deal with port mappings."""

  def __init__(self):
    self.used_host_ports = {'tcp': {},
                            'udp': {}}
    self._port_mappings = {'tcp': {},
                           'udp': {}}
    self._port_names = {}

  def Add(self, ports, kind, allow_privileged=False, prohibited_host_ports=(),
          default_protocols=('tcp',)):
    """Load port configurations and adds them to an internal dict.

    Args:
      ports: A list of strings or a CSV representing port forwarding.
      kind: what kind of port configuration this is, only used for error
        reporting.
      allow_privileged: Allow to bind to ports under 1024.
      prohibited_host_ports: A list of ports that are used outside of
        the container and may not be mapped to this port manager.
      default_protocols: A list of protocols that will be used if the protocol
        isn't specified with the port.

    Raises:
      InconsistentPortConfigurationError: If a port is configured to do
        two different conflicting things.
      IllegalPortConfigurationError: If the port is out of range or
        is not a number.

    Returns:
      A dictionary with forwarding rules as external_port => local_port.
    """
    if not ports:
      # Obviously nothing to do.
      return

    if isinstance(ports, int):
      ports = str(ports)
    if isinstance(ports, basestring):
      # split a csv
      ports = [port.strip() for port in ports.split(',')]
    port_translations = {'tcp': {}, 'udp': {}}
    for port in ports:
      try:
        if '/' in port:
          tmp = port.split('/')
          if len(tmp) != 2 or not re.match(PROTOCOL_RE, tmp[1].lower()):
            raise IllegalPortConfigurationError(
                '%r was not recognized as a valid port configuration.' % port)
          port = tmp[0]
          protocols = (tmp[1].lower(),)
        else:
          protocols = default_protocols
        if ':' in port:
          host_port, docker_port = (int(p.strip()) for p in port.split(':'))
          for p in protocols:
            port_translations[p][host_port] = docker_port
        else:
          host_port = int(port)
          docker_port = host_port
          for p in protocols:
            port_translations[p][host_port] = host_port
        if host_port in prohibited_host_ports:
          raise InconsistentPortConfigurationError(
              'Configuration conflict, port %d cannot be used by the '
              'application.' % host_port)
        if (host_port in self.used_host_ports and
            self.used_host_ports[host_port] != docker_port):
          raise InconsistentPortConfigurationError(
              'Configuration conflict, port %d configured to forward '
              'differently.' % host_port)
        self.used_host_ports[host_port] = docker_port
        if (host_port < 1 or host_port > 65535 or
            docker_port < 1 or docker_port > 65535):
          raise IllegalPortConfigurationError(
              'Failed to load %s port configuration: invalid port %s'
              % (kind, port))
        if docker_port < 1024 and not allow_privileged:
          raise IllegalPortConfigurationError(
              'Cannot listen on port %d as it is priviliged, use a forwarding '
              'port.' % docker_port)
        if docker_port in RESERVED_DOCKER_PORTS:
          raise IllegalPortConfigurationError(
              'Cannot use port %d as it is reserved on the VM.'
              % docker_port)
        if host_port in RESERVED_HOST_PORTS:
          raise IllegalPortConfigurationError(
              'Cannot use port %d as it is reserved on the VM.'
              % host_port)
      except ValueError as e:
        logging.exception('Bad port description')
        raise IllegalPortConfigurationError(
            'Failed to load %s port configuration: "%s" error: "%s"'
            % (kind, port, e))
    # At this point we know they are not destructive.
    self._port_mappings['tcp'].update(port_translations['tcp'])
    self._port_mappings['udp'].update(port_translations['udp'])
    # TODO: This is a bit of a hack.
    self._port_names[kind] = port_translations
    return port_translations

  def GetAllMappedPorts(self):
    """Returns all mapped ports.

    Returns:
      A dict of port mappings {host: docker}
    """
    return self._port_mappings

  # TODO: look into moving this into a DockerManager.
  def _BuildDockerPublishArgumentString(self):
    """Generates a string of ports to expose to the Docker container.

    Returns:
      A string with --publish=host:docker pairs.
    """
    port_map = self.GetAllMappedPorts()
    result = ''
    for protocol in sorted(port_map):
      for k, v in sorted(port_map[protocol].items()):
        result += '--publish=%d:%s/%s ' % (k, v, protocol)
    return result

  def GetReplicaPoolParameters(self):
    """Returns the contribution to the replica template."""
    publish_ports = self._BuildDockerPublishArgumentString()
    maps = {
        'template': {
            'vmParams': {
                'metadata': {
                    'items': [
                        {'key': 'gae_publish_ports', 'value': publish_ports}
                        ]
                    }
                }
            }
        }
    return maps

  def GetPortDict(self, name):
    """Get the port translation dict.

    Args:
      name: Name used when adding the ports to port manager.

    Returns:
      A dict of mappings {protocol: {host: docker}}.
    """
    return self._port_names.get(name) or {'tcp': {}, 'udp': {}}
