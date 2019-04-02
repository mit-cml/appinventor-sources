# -*- coding: utf-8 -*- #
# Copyright 2017 Google Inc. All Rights Reserved.
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
"""Facilitates managing multiple emulators at once."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import tempfile
import contextlib2 as contextlib

from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.emulators import config
from googlecloudsdk.command_lib.emulators import proxy_util
from googlecloudsdk.command_lib.emulators import util
from googlecloudsdk.command_lib.util import java
from googlecloudsdk.core import log
import portpicker
import six


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class Start(base.Command):
  """Start a number of emulators behind a proxy."""

  @staticmethod
  def Args(parser):
    parser.add_argument(
        '--proxy-port',
        required=False,
        type=int,
        help='port the proxy will be bound to')

    emulator_options = ','.join(sorted(config.EMULATORS.keys()))
    parser.add_argument(
        '--emulators',
        required=False,
        type=arg_parsers.ArgList(element_type=lambda val: val.lower()),
        metavar='EMULATORS',
        help='list of local emulators to start. \'all\' will attempt to start '
        'all of the emulators. Valid options: ' + emulator_options)

    parser.add_argument(
        '--route-to-public',
        required=False,
        help='If set, will route traffic for APIs not being emulated to prod')

  def Run(self, args):
    if 'all' in args.emulators:
      if len(args.emulators) > 1:
        raise util.EmulatorArgumentsError(
            "Cannot specify 'all' with other emulators")
      if args.route_to_public:
        raise util.EmulatorArgumentsError(
            'Cannot specify --route-to-public and --emulators=all')
    else:
      unknown_emulators = [x for x in args.emulators
                           if x not in config.EMULATORS]
      if unknown_emulators:
        raise util.EmulatorArgumentsError('Specified unrecognized emulators: '
                                          ','.join(unknown_emulators))

    proxy_port = args.proxy_port
    if args.proxy_port is None:
      proxy_port = util.DefaultPortIfAvailable()

    if not portpicker.is_port_free(proxy_port):
      raise util.EmulatorArgumentsError(
          'Specified proxy port [{}] is not available'.format(proxy_port))

    util.EnsureComponentIsInstalled('emulator-reverse-proxy',
                                    'gcloud emulators start')
    for flag, emulator in six.iteritems(config.EMULATORS):
      title = emulator.emulator_title
      component = emulator.emulator_component
      if (args.emulators is not None and
          (flag in args.emulators or 'all' in args.emulators)):
        java.RequireJavaInstalled(title)
        util.EnsureComponentIsInstalled(component, title)

    with contextlib.ExitStack() as stack:  # pytype: disable=module-attr

      local_emulator_ports = {}
      for emulator in args.emulators:
        port = portpicker.pick_unused_port()
        local_emulator_ports[emulator] = port
        stack.enter_context(config.EMULATORS[emulator].Start(port))

      _, routes_config_file = tempfile.mkstemp()
      config.WriteRoutesConfig(config.EMULATORS, routes_config_file)
      log.status.Print(
          'routes configuration written to file: {}'.format(routes_config_file))

      proxy_config = config.ProxyConfiguration(local_emulator_ports,
                                               args.route_to_public,
                                               proxy_port)

      _, proxy_config_file = tempfile.mkstemp()
      proxy_config.WriteJsonToFile(proxy_config_file)
      log.status.Print(
          'proxy configuration written to file: {}'.format(proxy_config_file))

      # TODO(b/35872500) for some reason, in this case, this will block. Maybe
      #   we need to flush something, maybe not. Regardless, this is fine for
      #   now, but would be nice for it to not block like everything else
      with proxy_util.StartEmulatorProxy(
          args=[routes_config_file, proxy_config_file]) as proxy_process:
        # This will block the console
        util.PrefixOutput(proxy_process, 'emulator-reverse-proxy')
