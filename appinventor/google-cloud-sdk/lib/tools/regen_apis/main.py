# -*- coding: utf-8 -*- #
# Copyright 2016 Google Inc. All Rights Reserved.
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
"""Script to regenerate samples with latest client generator."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import argparse
import logging
import pprint
import sys

from googlecloudsdk.api_lib.regen import generate
from googlecloudsdk.core import yaml
from googlecloudsdk.core.util import files
import six


class Error(Exception):
  """Errors raised by this module."""


class UnknownApi(Error):
  """Raised when api is not found."""


def main(argv=None):
  if argv is None:
    argv = sys.argv

  parser = argparse.ArgumentParser(
      description='Regenerates apitools clients in given directory.')

  parser.add_argument('--config',
                      required=True,
                      help='Regeneration config filename.')

  parser.add_argument('--base-dir',
                      default=files.GetCWD(),
                      help='Regeneration config filename.')

  parser.add_argument('--api',
                      help='api_name or api_name/api_version to regenerate. '
                           'If api_version is ommitted then all versions are '
                           'regenerated. If this argument is ommitted all apis '
                           'and their versions will be regenerated.')

  parser.add_argument('-l',
                      '--log-level',
                      choices=['DEBUG', 'INFO', 'WARNING', 'ERROR', 'CRITICAL'],
                      default='INFO',
                      help='Set the logging level')

  args = parser.parse_args(argv[1:])

  if args.log_level:
    logging.basicConfig(
        format='%(asctime)s %(filename)s:%(lineno)d %(message)s',
        level=getattr(logging, args.log_level))

  config = yaml.load_path(args.config)

  logging.debug('Config %s', pprint.pformat(config))

  root_dir = config['root_dir']
  logging.debug('Based dir %s', args.base_dir)

  if args.api is not None:
    if '/' in args.api:
      api_name, api_version = args.api.split('/')
    else:
      api_name, api_version = args.api, None
    api_section = config['apis'].get(api_name)
    if api_section is None:
      raise UnknownApi('api [{api_name}] not found in "apis" section of '
                       '{config_file}'
                       .format(api_name=api_name, config_file=args.config))
    if api_version:
      api_config = api_section.get(api_version)
      if api_config is None:
        raise UnknownApi('api version [{api_version}] is not one of the '
                         'defined versions [{defined_versions}] of '
                         '[{api_name}] found in "apis" section of {config_file}'
                         .format(api_version=api_version,
                                 api_name=api_name,
                                 defined_versions=','.join(
                                     sorted(api_section.keys())),
                                 config_file=args.config))
      regenerate_list = [(api_name, api_version, api_config)]
    else:
      regenerate_list = [
          (api_name, api_version, api_config)
          for api_version, api_config in six.iteritems(api_section)]
  else:
    regenerate_list = [
        (api_name, api_version, api_config)
        for api_name, api_version_config in six.iteritems(config['apis'])
        for api_version, api_config in six.iteritems(api_version_config)
    ]

  for api_name, api_version, api_config in sorted(regenerate_list):
    logging.info('Generating %s %s', api_name, api_version)
    generate.GenerateApi(args.base_dir, root_dir,
                         api_name, api_version, api_config)
    generate.GenerateResourceModule(args.base_dir, root_dir,
                                    api_name, api_version,
                                    api_config['discovery_doc'],
                                    api_config.get('resources', {}))

  generate.GenerateApiMap(args.base_dir, root_dir, config['apis'])


if __name__ == '__main__':
  main()
