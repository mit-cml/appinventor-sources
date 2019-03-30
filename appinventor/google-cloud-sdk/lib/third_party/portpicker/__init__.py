#!/usr/bin/python3
#
# Copyright 2007 Google Inc. All Rights Reserved.
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
"""Pure python code for finding unused ports on a host.

This module provides a pick_unused_port() function.
It can also be called via the command line for use in shell scripts.
When called from the command line, it takes one optional argument, which,
if given, is sent to portserver instead of portpicker's PID.
To reserve a port for the lifetime of a bash script, use $BASHPID as this
argument.

There is a race condition between picking a port and your application code
binding to it.  The use of a port server to prevent that is recommended on
loaded test hosts running many tests at a time.

If your code can accept a bound socket as input rather than being handed a
port number consider using socket.bind(('localhost', 0)) to bind to an
available port without a race condition rather than using this library.

Typical usage:
  test_port = portpicker.pick_unused_port()
"""

from __future__ import print_function

import logging
import os
import random
import socket
import sys

# The legacy Bind, IsPortFree, etc. names are not exported.
__all__ = ('bind', 'is_port_free', 'pick_unused_port', 'return_port',
           'add_reserved_port', 'get_port_from_port_server')

_PROTOS = [(socket.SOCK_STREAM, socket.IPPROTO_TCP),
           (socket.SOCK_DGRAM, socket.IPPROTO_UDP)]


# Ports that are currently available to be given out.
_free_ports = set()

# Ports that are reserved or from the portserver that may be returned.
_owned_ports = set()

# Ports that we chose randomly that may be returned.
_random_ports = set()


def add_reserved_port(port):
    """Add a port that was acquired by means other than the port server."""
    _free_ports.add(port)


def return_port(port):
    """Return a port that is no longer being used so it can be reused."""
    if port in _random_ports:
        _random_ports.remove(port)
    elif port in _owned_ports:
        _owned_ports.remove(port)
        _free_ports.add(port)
    elif port in _free_ports:
        logging.info("Returning a port that was already returned: %s", port)
    else:
        logging.info("Returning a port that wasn't given by portpicker: %s",
                     port)


def bind(port, socket_type, socket_proto):
    """Try to bind to a socket of the specified type, protocol, and port.

    This is primarily a helper function for PickUnusedPort, used to see
    if a particular port number is available.

    For the port to be considered available, the kernel must support at least
    one of (IPv6, IPv4), and the port must be available on each supported
    family.

    Args:
      port: The port number to bind to, or 0 to have the OS pick a free port.
      socket_type: The type of the socket (ex: socket.SOCK_STREAM).
      socket_proto: The protocol of the socket (ex: socket.IPPROTO_TCP).

    Returns:
      The port number on success or None on failure.
    """
    got_socket = False
    for family in (socket.AF_INET6, socket.AF_INET):
        try:
            sock = socket.socket(family, socket_type, socket_proto)
            got_socket = True
        except socket.error:
            continue
        try:
            sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            sock.bind(('', port))
            if socket_type == socket.SOCK_STREAM:
                sock.listen(1)
            port = sock.getsockname()[1]
        except socket.error:
            return None
        finally:
            sock.close()
    return port if got_socket else None

Bind = bind  # legacy API. pylint: disable=invalid-name


def is_port_free(port):
    """Check if specified port is free.

    Args:
      port: integer, port to check
    Returns:
      boolean, whether it is free to use for both TCP and UDP
    """
    return bind(port, *_PROTOS[0]) and bind(port, *_PROTOS[1])

IsPortFree = is_port_free  # legacy API. pylint: disable=invalid-name


def pick_unused_port(pid=None):
    """A pure python implementation of PickUnusedPort.

    Args:
      pid: PID to tell the portserver to associate the reservation with. If
        None,
        the current process's PID is used.

    Returns:
      A port number that is unused on both TCP and UDP.
    """
    if _free_ports:
        port = _free_ports.pop()
        _owned_ports.add(port)
        return port
    # Provide access to the portserver on an opt-in basis.
    if 'PORTSERVER_ADDRESS' in os.environ:
        port = get_port_from_port_server(os.environ['PORTSERVER_ADDRESS'],
                                         pid=pid)
        if port:
            return port
    return _pick_unused_port_without_server()

PickUnusedPort = pick_unused_port  # legacy API. pylint: disable=invalid-name


def _pick_unused_port_without_server():  # Protected. pylint: disable=invalid-name
    """Pick an available network port without the help of a port server.

    This code ensures that the port is available on both TCP and UDP.

    This function is an implementation detail of PickUnusedPort(), and
    should not be called by code outside of this module.

    Returns:
      A port number that is unused on both TCP and UDP.  None on error.
    """
    # Try random ports first.
    rng = random.Random()
    for _ in range(10):
        port = int(rng.randrange(15000, 25000))
        if is_port_free(port):
            _random_ports.add(port)
            return port

    # Try OS-assigned ports next.
    # Ambrose discovered that on the 2.6 kernel, calling Bind() on UDP socket
    # returns the same port over and over. So always try TCP first.
    while True:
        # Ask the OS for an unused port.
        port = bind(0, _PROTOS[0][0], _PROTOS[0][1])
        # Check if this port is unused on the other protocol.
        if port and bind(port, _PROTOS[1][0], _PROTOS[1][1]):
            _random_ports.add(port)
            return port


def get_port_from_port_server(portserver_address, pid=None):
    """Request a free a port from a system-wide portserver.

    This follows a very simple portserver protocol:
    The request consists of our pid (in ASCII) followed by a newline.
    The response is a port number and a newline, 0 on failure.

    This function is an implementation detail of pick_unused_port().
    It should not normally be called by code outside of this module.

    Args:
      portserver_address: The address (path) of a unix domain socket
        with which to connect to the portserver.  A leading '@'
        character indicates an address in the "abstract namespace."
      pid: The PID to tell the portserver to associate the reservation with.
        If None, the current process's PID is used.

    Returns:
      The port number on success or None on failure.
    """
    if not portserver_address:
        return None
    # An AF_UNIX address may start with a zero byte, in which case it is in the
    # "abstract namespace", and doesn't have any filesystem representation.
    # See 'man 7 unix' for details.
    # The convention is to write '@' in the address to represent this zero byte.
    if portserver_address[0] == '@':
        portserver_address = '\0' + portserver_address[1:]

    if pid is None:
        pid = os.getpid()

    try:
        # Create socket.
        sock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
        try:
            # Connect to portserver.
            sock.connect(portserver_address)

            # Write request.
            sock.sendall(('%d\n' % pid).encode('ascii'))

            # Read response.
            # 1K should be ample buffer space.
            buf = sock.recv(1024)
        finally:
            sock.close()
    except socket.error as e:
        print('Socket error when connecting to portserver:', e,
              file=sys.stderr)
        return None

    try:
        port = int(buf.split(b'\n')[0])
    except ValueError:
        print('Portserver failed to find a port.', file=sys.stderr)
        return None
    _owned_ports.add(port)
    return port


GetPortFromPortServer = get_port_from_port_server  # legacy API. pylint: disable=invalid-name


def main(argv):
    """If passed an arg, treat it as a PID, otherwise portpicker uses getpid."""
    port = pick_unused_port(pid=int(argv[1]) if len(argv) > 1 else None)
    if not port:
        sys.exit(1)
    print(port)


if __name__ == '__main__':
    main(sys.argv)
