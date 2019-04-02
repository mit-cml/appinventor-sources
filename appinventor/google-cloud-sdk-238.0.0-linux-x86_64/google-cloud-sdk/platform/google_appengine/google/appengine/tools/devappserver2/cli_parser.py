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
"""Provides a command line parser for the dev_appserver and related tools.

The standard argparse library is subclassed within to provide configurability to
the parser and argument group __init__ and add_argument methods. This is a
convenient way to share flags between tools, eg dev_appserver.py and
api_server.py binaries, and to toggle flags on and off for certain tools.

The create_command_line_parser accepts a configuration argument:
  create_command_line_parser(DEV_APPSERVER_CONFIGURATION): returns a parser with
      all flags for the dev_appserver.py binary.
  create_command_line_parser(API_SERVER_CONFIGURATION): returns a parser with
      all flags for the api_server.py binary.
"""

import argparse
import os
import re

from google.appengine.api import appinfo
from google.appengine.datastore import datastore_stub_util
from google.appengine.tools import boolean_action
from google.appengine.tools.devappserver2 import application_configuration
from google.appengine.tools.devappserver2 import constants
from google.appengine.tools.devappserver2 import runtime_factories


# Configuration tokens used to determine which arguments are added to the
# parser.
DEV_APPSERVER_CONFIGURATION = 'dev_appserver_configuration'
API_SERVER_CONFIGURATION = 'api_server_configuration'


class PortParser(object):
  """An argparse type parser for ints that represent ports."""

  def __init__(self, allow_port_zero=True):
    self._min_port = 0 if allow_port_zero else 1

  def __call__(self, value):
    try:
      port = int(value)
    except ValueError:
      raise argparse.ArgumentTypeError('Invalid port: %r' % value)
    if port < self._min_port or port >= (1 << 16):
      raise argparse.ArgumentTypeError('Invalid port: %d' % port)
    return port


class ServicePortParser(PortParser):
  """An argparse type parser exclusively for --specified_service_port flag."""

  def __init__(self):
    super(ServicePortParser, self).__init__()

  def __call__(self, value):
    res = {}
    for service_port_str in value.split(','):
      service_port = service_port_str.split(':')
      if len(service_port) != 2:
        raise argparse.ArgumentTypeError(
            ' %s is not in the format of service-name:port,service-name:port'
            % value)
      service, port = service_port
      res[service] = super(ServicePortParser, self).__call__(port)
    return res


def parse_per_module_option(
    value, value_type, value_predicate,
    single_bad_type_error, single_bad_predicate_error,
    multiple_bad_type_error, multiple_bad_predicate_error,
    multiple_duplicate_module_error):
  """Parses command line options that may be specified per-module.

  Args:
    value: A str containing the flag value to parse. Two formats are supported:
        1. A universal value (may not contain a colon as that is use to
           indicate a per-module value).
        2. Per-module values. One or more comma separated module-value pairs.
           Each pair is a module_name:value. An empty module-name is shorthand
           for "default" to match how not specifying a module name in the yaml
           is the same as specifying "module: default".
    value_type: a callable that converts the string representation of the value
        to the actual value. Should raise ValueError if the string can not
        be converted.
    value_predicate: a predicate to call on the converted value to validate
        the converted value. Use "lambda _: True" if all values are valid.
    single_bad_type_error: the message to use if a universal value is provided
        and value_type throws a ValueError. The message must consume a single
        format parameter (the provided value).
    single_bad_predicate_error: the message to use if a universal value is
        provided and value_predicate returns False. The message does not
        get any format parameters.
    multiple_bad_type_error: the message to use if a per-module value
        either does not have two values separated by a single colon or if
        value_types throws a ValueError on the second string. The message must
        consume a single format parameter (the module_name:value pair).
    multiple_bad_predicate_error: the message to use if a per-module value if
        value_predicate returns False. The message must consume a single format
        parameter (the module name).
    multiple_duplicate_module_error: the message to use if the same module is
        repeated. The message must consume a single formater parameter (the
        module name).

  Returns:
    Either a single value of value_type for universal values or a dict of
    str->value_type for per-module values.

  Raises:
    argparse.ArgumentTypeError: the value is invalid.
  """
  if ':' not in value:
    try:
      single_value = value_type(value)
    except ValueError:
      raise argparse.ArgumentTypeError(single_bad_type_error % value)
    else:
      if not value_predicate(single_value):
        raise argparse.ArgumentTypeError(single_bad_predicate_error)
      return single_value
  else:
    module_to_value = {}
    for module_value in value.split(','):
      try:
        module_name, single_value = module_value.split(':')
        single_value = value_type(single_value)
      except ValueError:
        raise argparse.ArgumentTypeError(multiple_bad_type_error % module_value)
      else:
        module_name = module_name.strip()
        if not module_name:
          module_name = appinfo.DEFAULT_MODULE
        if module_name in module_to_value:
          raise argparse.ArgumentTypeError(
              multiple_duplicate_module_error % module_name)
        if not value_predicate(single_value):
          raise argparse.ArgumentTypeError(
              multiple_bad_predicate_error % module_name)
        module_to_value[module_name] = single_value
    return module_to_value


def parse_max_module_instances(value):
  """Returns the parsed value for the --max_module_instances flag.

  Args:
    value: A str containing the flag value for parse. The format should follow
        one of the following examples:
          1. "5" - All modules are limited to 5 instances.
          2. "default:3,backend:20" - The default module can have 3 instances,
             "backend" can have 20 instances and all other modules are
              unaffected. An empty name (i.e. ":3") is shorthand for default
              to match how not specifying a module name in the yaml is the
              same as specifying "module: default".
  Returns:
    The parsed value of the max_module_instances flag. May either be an int
    (for values of the form "5") or a dict of str->int (for values of the
    form "default:3,backend:20").

  Raises:
    argparse.ArgumentTypeError: the value is invalid.
  """
  return parse_per_module_option(
      value, int, lambda instances: instances > 0,
      'Invalid max instance count: %r',
      'Max instance count must be greater than zero',
      'Expected "module:max_instance_count": %r',
      'Max instance count for module %s must be greater than zero',
      'Duplicate max instance count for module %s')


def parse_threadsafe_override(value):
  """Returns the parsed value for the --threadsafe_override flag.

  Args:
    value: A str containing the flag value for parse. The format should follow
        one of the following examples:
          1. "False" - All modules override the YAML threadsafe configuration
             as if the YAML contained False.
          2. "default:False,backend:True" - The default module overrides the
             YAML threadsafe configuration as if the YAML contained False, the
             "backend" module overrides with a value of True and all other
             modules use the value in the YAML file. An empty name (i.e.
             ":True") is shorthand for default to match how not specifying a
             module name in the yaml is the same as specifying
             "module: default".
  Returns:
    The parsed value of the threadsafe_override flag. May either be a bool
    (for values of the form "False") or a dict of str->bool (for values of the
    form "default:False,backend:True").

  Raises:
    argparse.ArgumentTypeError: the value is invalid.
  """
  return parse_per_module_option(
      value, boolean_action.BooleanParse, lambda _: True,
      'Invalid threadsafe override: %r',
      None,
      'Expected "module:threadsafe_override": %r',
      None,
      'Duplicate threadsafe override value for module %s')


def parse_path(value):
  """Returns the given path with ~ and environment variables expanded."""
  return os.path.expanduser(os.path.expandvars(value))


class ConfigurableArgumentParser(argparse.ArgumentParser):
  """Provides configurations option to the argument parser.

  This provides a convenient way to share flags between tools, and to toggle
  flags on and off for tools, eg for dev_appserver.py vs api_server.py.

  Example usage (with a helper create_parser function):

    def create_parser(config):
      parser = ConfigurableArgumentParser(config)
      parser.add_argument('flag-for-all-configs')
      parser.add_argument('foo-flag',
                          restrict_configurations=['my-configuration'])
      parser.add_argument('bar-flag',
                          restrict_configurations=['another-configuration'])
      parser.add_argument('foobar-flag',
                          restrict_configurations=[
                              'my-configuration', 'another-configuration'])
      return parser

    create_parser('my-configuration')  ->  contains [
        'flag-for-all-configs', 'foo-flag', 'foobar-flag']
    create_parser('another-configuration')  ->  contains [
        'flag-for-all-configs', 'bar-flag', 'foobar-flag']
    create_parser('yet-another-configuration')  ->  contains [
        'flag-for-all-configs']
  """

  def __init__(self, *args, **kwargs):
    """Initializes the argument parser.

    Args:
      *args: Arguments passed on to the parent init method.
      **kwargs: Keyword arguments passed on to the parent init method, can
          optionally contain a 'configuration' kwarg that will be popped and
          stored on the instance. This should be the string configuration
          accepted by the parser.
    """
    self._configuration = kwargs.pop('configuration', None)
    super(ConfigurableArgumentParser, self).__init__(*args, **kwargs)

  def add_argument(self, *args, **kwargs):
    """Adds an argument to the parser.

    Args:
      *args: Arguments passed on to the argument group.
      **kwargs: Keyword arguments passed on to the argument group, can
          optionally contain a 'restrict_configuration' kwarg that will be
          popped. This should be the list of configurations the argument is
          applicable for. Omitting this kwarg, or providing an empty list,
          signifies that the added argument is valid for all configurations.
    """
    restrict_configuration = kwargs.pop('restrict_configuration', [])
    if (not restrict_configuration or
        self._configuration in restrict_configuration):
      super(ConfigurableArgumentParser, self).add_argument(*args, **kwargs)

  def add_argument_group(self, *args, **kwargs):
    """Adds an argument group to the parser.

    The parsers's configuration is set on the argument group.

    Args:
      *args: Arguments passed on to the argument group.
      **kwargs: Keyword arguments passed on to the argument group.

    Returns:
      An instance of ConfigurableArgumentGroup.
    """
    group = ConfigurableArgumentGroup(
        self, configuration=self._configuration, *args, **kwargs)
    self._action_groups.append(group)
    return group


class ConfigurableArgumentGroup(argparse._ArgumentGroup):  # pylint: disable=protected-access
  """Provides configuration options to the argument group.

  This provides a convenient way to share flags between tools, and to toggle
  flags on and off for tools, eg for dev_appserver.py vs api_server.py.
  """

  def __init__(self, *args, **kwargs):
    """Initializes the argument group.

    Args:
      *args: Arguments passed on to the parent init method.
      **kwargs: Keyword arguments passed on to the parent init method, can
          optionally contain a 'configuration' kwarg that will be popped and
          stored on the instance. This should be the string configuration
          accepted by the parser.
    """
    self._configuration = kwargs.pop('configuration', None)
    super(ConfigurableArgumentGroup, self).__init__(*args, **kwargs)

  def add_argument(self, *args, **kwargs):
    """Adds an argument to the group.

    Args:
      *args: Arguments passed on to the argument group.
      **kwargs: Keyword arguments passed on to the argument group, can
          optionally contain a 'restrict_configuration' kwarg that will be
          popped. This should be the list of configurations the argument is
          applicable for. Omitting this kwarg, or providing an empty list,
          signifies that the added argument is valid for all configurations.
    """
    restrict_configuration = kwargs.pop('restrict_configuration', [])
    if (not restrict_configuration or
        self._configuration in restrict_configuration):
      super(ConfigurableArgumentGroup, self).add_argument(*args, **kwargs)


def create_command_line_parser(configuration=None):
  """Returns an argparse.ArgumentParser to parse command line arguments.

  Args:
    configuration: A string token containing the configuration to generate a
      command line parser for.

  Returns:
    An instance of ConfigurableArgumentParser.
  """
  # TODO: Add more robust argument validation. Consider what flags
  # are actually needed.

  parser = ConfigurableArgumentParser(
      configuration=configuration,
      formatter_class=argparse.ArgumentDefaultsHelpFormatter)
  arg_name = 'yaml_path'
  arg_help = 'Path to one or more app.yaml files'
  if application_configuration.java_supported():
    arg_name = 'yaml_or_war_path'
    arg_help += ', or a directory containing WEB-INF/web.xml'

  # dev_appserver.py requires config_paths, api_server.py does not.
  parser.add_argument(
      'config_paths', restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
      metavar=arg_name, nargs='+', help=arg_help)
  parser.add_argument(
      'config_paths', restrict_configuration=[API_SERVER_CONFIGURATION],
      metavar=arg_name, nargs='*', help=arg_help)

  if constants.DEVSHELL_ENV in os.environ:
    default_server_host = '0.0.0.0'
  else:
    default_server_host = 'localhost'

  common_group = parser.add_argument_group('Common')
  common_group.add_argument(
      '-A', '--application', action='store', dest='app_id',
      help='Set the application, overriding the application value from the '
      'app.yaml file.')
  # The default application ID prefix for use in API stubs. This flag is
  # suppressed because users should not be changing this. It is used only for
  # testing purposes.
  # TODO: Unify py/java use of dev~ prefix.
  common_group.add_argument(
      '--application_prefix', dest='app_id_prefix', default='dev~',
      help=argparse.SUPPRESS)
  common_group.add_argument(
      '--host', default=default_server_host,
      help='host name to which application modules should bind')
  common_group.add_argument(
      '--port', type=PortParser(), default=8080,
      help='lowest port to which application modules should bind')
  common_group.add_argument(
      '--specified_service_ports', type=ServicePortParser(), default=None,
      help='A sequence of service-name:port-number to port number mapping. E.g:'
      ' service-a:22222,service-b:33333')
  common_group.add_argument(
      '--admin_host', default=default_server_host,
      help='host name to which the admin server should bind')
  common_group.add_argument(
      '--admin_port', type=PortParser(), default=8000,
      help='port to which the admin server should bind')
  # TODO: Change this. Eventually we want a way to associate ports
  # with external modules, with default values. For now we allow only one
  # external module, with a port number that must be passed in here.
  common_group.add_argument(
      '--external_port', type=PortParser(), default=None,
      help=argparse.SUPPRESS)
  common_group.add_argument(
      '--auth_domain', default='gmail.com',
      help='name of the authorization domain to use')
  common_group.add_argument(
      '--storage_path', metavar='PATH',
      type=parse_path,
      help='path to the data (datastore, blobstore, etc.) associated with the '
      'application.')
  common_group.add_argument(
      '--log_level', default='info',
      choices=constants.LOG_LEVEL_TO_RUNTIME_CONSTANT.keys(),
      help='the log level below which logging messages generated by '
      'application code will not be displayed on the console')
  common_group.add_argument(
      '--max_module_instances',
      type=parse_max_module_instances,
      restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
      help='the maximum number of runtime instances that can be started for a '
      'particular module - the value can be an integer, in what case all '
      'modules are limited to that number of instances or a comma-seperated '
      'list of module:max_instances e.g. "default:5,backend:3"')
  common_group.add_argument(
      '--use_mtime_file_watcher',
      action=boolean_action.BooleanAction,
      const=True,
      default=False,
      restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
      help='use mtime polling for detecting source code changes - useful if '
      'modifying code from a remote machine using a distributed file system')
  common_group.add_argument(
      '--threadsafe_override',
      type=parse_threadsafe_override,
      restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
      help='override the application\'s threadsafe configuration - the value '
      'can be a boolean, in which case all modules threadsafe setting will '
      'be overridden or a comma-separated list of module:threadsafe_override '
      'e.g. "default:False,backend:True"')
  common_group.add_argument('--enable_mvm_logs',
                            action=boolean_action.BooleanAction,
                            const=True,
                            default=False,
                            help=argparse.SUPPRESS)
  enable_host_checking_help = ('determines whether to enforce HTTP Host '
                               'checking for application modules, API server, '
                               'and admin server. host checking protects '
                               'against DNS rebinding attacks, so only disable '
                               'after understanding the security implications.')





  common_group.add_argument('--enable_host_checking',
                            action=boolean_action.BooleanAction,
                            const=True,
                            default=True,
                            help=enable_host_checking_help)
  common_group.add_argument('--enable_console',
                            action=boolean_action.BooleanAction,
                            const=True,
                            default=False,
                            help='Enable interactive console in admin view.')
  common_group.add_argument(
      '--java_app_base_url',
      default=None,
      restrict_configuration=[API_SERVER_CONFIGURATION],
      help='Base URL of the java app in the form '
      'http://host[:port], e.g. http://localhost:8080. '
      'Should only be used to specify the url of a java '
      'app running with the classic Java SDK tooling, '
      'and not Java apps running on devappserver2.')
  common_group.add_argument(
      '--ssl_certificate_path',
      default=None,
      help='Path to SSL certificate. Must also provide '
      '--ssl_certificate_key_path if using this option.')
  common_group.add_argument(
      '--ssl_certificate_key_path',
      default=None,
      help='Path to corresponding SSL private key. Must also provide '
      '--ssl_certificate_path if using this option.')

  # PHP
  php_group = parser.add_argument_group('PHP')
  php_group.add_argument('--php_executable_path', metavar='PATH',
                         type=parse_path,
                         restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
                         help='path to the PHP executable')
  php_group.add_argument('--php_remote_debugging',
                         action=boolean_action.BooleanAction,
                         const=True,
                         default=False,
                         restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
                         help='enable XDebug remote debugging')
  php_group.add_argument('--php_gae_extension_path', metavar='PATH',
                         type=parse_path,
                         restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
                         help='path to the GAE PHP extension')
  php_group.add_argument('--php_xdebug_extension_path', metavar='PATH',
                         type=parse_path,
                         restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
                         help='path to the xdebug extension')

  # App Identity
  appidentity_group = parser.add_argument_group('Application Identity')
  appidentity_group.add_argument(
      '--appidentity_email_address',
      help='email address associated with a service account that has a '
      'downloadable key. May be None for no local application identity.')
  appidentity_group.add_argument(
      '--appidentity_private_key_path',
      help='path to private key file associated with service account '
      '(.pem format). Must be set if appidentity_email_address is set.')
  # Supressing the help text, as it is unlikely any typical user outside
  # of Google has an appropriately set up test oauth server that devappserver2
  # could talk to.
  # URL to the oauth server that devappserver2 should  use to authenticate the
  # appidentity private key (defaults to the standard Google production server.
  appidentity_group.add_argument(
      '--appidentity_oauth_url',
      help=argparse.SUPPRESS)

  # Python
  python_group = parser.add_argument_group('Python')
  python_group.add_argument(
      '--python_startup_script',
      restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
      help='the script to run at the startup of new Python runtime instances '
      '(useful for tools such as debuggers).')
  python_group.add_argument(
      '--python_startup_args',
      restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
      help='the arguments made available to the script specified in '
      '--python_startup_script.')

  # Java
  java_group = parser.add_argument_group('Java')
  java_group.add_argument(
      '--jvm_flag', action='append',
      restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
      help='additional arguments to pass to the java command when launching '
      'an instance of the app. May be specified more than once. Example: '
      '--jvm_flag=-Xmx1024m --jvm_flag=-Xms256m')

  # Go
  go_group = parser.add_argument_group('Go')
  go_group.add_argument(
      '--go_work_dir',
      restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
      help='working directory of compiled Go packages. Defaults to temporary '
      'directory. Contents of the working directory are persistent and need to '
      'be cleaned up manually.')
  go_group.add_argument(
      '--enable_watching_go_path',
      action=boolean_action.BooleanAction,
      const=True,
      default=True,
      restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
      help='Enable watching $GOPATH for go app dependency changes. If file '
      'watcher complains about too many files to watch, you can set it to '
      'False.')
  go_group.add_argument(
      '--go_debugging',
      restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
      action=boolean_action.BooleanAction,
      const=True,
      default=False,
      help='Enable debugging. Connect to the running app with delve.')

  # Custom
  custom_group = parser.add_argument_group('Custom VM Runtime')
  custom_group.add_argument(
      '--custom_entrypoint',
      restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
      help='specify an entrypoint for custom runtime modules. This is '
      'required when such modules are present. Include "{port}" in the '
      'string (without quotes) to pass the port number in as an argument. For '
      'instance: --custom_entrypoint="gunicorn -b localhost:{port} '
      'mymodule:application"',
      default='')
  custom_group.add_argument(
      '--runtime',
      restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
      help='specify the default runtimes you would like to use.  Valid '
      'runtimes are %s.' % runtime_factories.valid_runtimes(),
      default='')

  # Blobstore
  blobstore_group = parser.add_argument_group('Blobstore API')
  blobstore_group.add_argument(
      '--blobstore_path',
      type=parse_path,
      help='path to directory used to store blob contents '
      '(defaults to a subdirectory of --storage_path if not set)',
      default=None)
  # TODO: Remove after the Files API is really gone.
  blobstore_group.add_argument(
      '--blobstore_warn_on_files_api_use',
      action=boolean_action.BooleanAction,
      const=True,
      default=True,
      help=argparse.SUPPRESS)
  blobstore_group.add_argument(
      '--blobstore_enable_files_api',
      action=boolean_action.BooleanAction,
      const=True,
      default=False,
      help=argparse.SUPPRESS)

  # Cloud SQL
  cloud_sql_group = parser.add_argument_group('Cloud SQL')
  cloud_sql_group.add_argument(
      '--mysql_host',
      default='localhost',
      help='host name of a running MySQL server used for simulated Google '
      'Cloud SQL storage')
  cloud_sql_group.add_argument(
      '--mysql_port', type=PortParser(allow_port_zero=False),
      default=3306,
      help='port number of a running MySQL server used for simulated Google '
      'Cloud SQL storage')
  cloud_sql_group.add_argument(
      '--mysql_user',
      default='',
      help='username to use when connecting to the MySQL server specified in '
      '--mysql_host and --mysql_port or --mysql_socket')
  cloud_sql_group.add_argument(
      '--mysql_password',
      default='',
      help='password to use when connecting to the MySQL server specified in '
      '--mysql_host and --mysql_port or --mysql_socket')
  cloud_sql_group.add_argument(
      '--mysql_socket',
      help='path to a Unix socket file to use when connecting to a running '
      'MySQL server used for simulated Google Cloud SQL storage')

  # Datastore
  datastore_group = parser.add_argument_group('Datastore API')
  datastore_group.add_argument(
      '--datastore_path',
      type=parse_path,
      default=None,
      help='path to a file used to store datastore contents '
      '(defaults to a file in --storage_path if not set)',)
  datastore_group.add_argument('--clear_datastore',
                               action=boolean_action.BooleanAction,
                               const=True,
                               default=False,
                               help='clear the datastore on startup')
  datastore_group.add_argument(
      '--datastore_consistency_policy',
      default='time',
      choices=['consistent', 'random', 'time'],
      help='the policy to apply when deciding whether a datastore write should '
      'appear in global queries')
  datastore_group.add_argument(
      '--require_indexes',
      action=boolean_action.BooleanAction,
      const=True,
      default=False,
      help='generate an error on datastore queries that '
      'requires a composite index not found in index.yaml')
  datastore_group.add_argument(
      '--auto_id_policy',
      default=datastore_stub_util.SCATTERED,
      choices=[datastore_stub_util.SEQUENTIAL,
               datastore_stub_util.SCATTERED],
      help='the type of sequence from which the datastore stub '
      'assigns automatic IDs. NOTE: Sequential IDs are '
      'deprecated. This flag will be removed in a future '
      'release. Please do not rely on sequential IDs in your '
      'tests.')




  datastore_group.add_argument(
      '--support_datastore_emulator',
      action=boolean_action.BooleanAction,
      const=True,
      default=None,
      help='Support datastore local emulation with Cloud Datastore emulator.')
  # Port number on which dev_appserver should launch Cloud Datastore emulator.
  datastore_group.add_argument(
      '--running_datastore_emulator_host', default=None,
      help='Overrides the environment variable DATASTORE_EMULATOR_HOST, which'
      ' means the hostname:port of a running Cloud Datastore emulator that'
      ' dev_appserver can connect to.')
  # Port number on which dev_appserver should launch Cloud Datastore emulator.
  datastore_group.add_argument(
      '--datastore_emulator_port', type=PortParser(), default=0,
      help='The port number that dev_appserver should launch Cloud Datastore '
      'emulator on.')
  # The path to an executable shell script that invokes Cloud Datastore
  # emulator.
  datastore_group.add_argument(
      '--datastore_emulator_cmd', type=parse_path,
      default=None,
      help='The path to a script that invokes cloud datastore emulator. If '
      'left empty, dev_appserver will try to find datastore emulator in the '
      'Google Cloud SDK.')
  datastore_group.add_argument(
      '--datastore_emulator_is_test_mode',
      action=boolean_action.BooleanAction,
      const=True,
      default=False,
      help=argparse.SUPPRESS)

  # Logs
  logs_group = parser.add_argument_group('Logs API')
  logs_group.add_argument(
      '--logs_path', default=None,
      help='path to a file used to store request logs (defaults to a file in '
      '--storage_path if not set)',)

  # Mail
  mail_group = parser.add_argument_group('Mail API')
  mail_group.add_argument(
      '--show_mail_body',
      action=boolean_action.BooleanAction,
      const=True,
      default=False,
      help='logs the contents of e-mails sent using the Mail API')
  mail_group.add_argument(
      '--enable_sendmail',
      action=boolean_action.BooleanAction,
      const=True,
      default=False,
      help='use the "sendmail" tool to transmit e-mail sent '
      'using the Mail API (ignored if --smtp_host is set)')
  mail_group.add_argument(
      '--smtp_host', default='',
      help='host name of an SMTP server to use to transmit '
      'e-mail sent using the Mail API')
  mail_group.add_argument(
      '--smtp_port', default=25,
      type=PortParser(allow_port_zero=False),
      help='port number of an SMTP server to use to transmit '
      'e-mail sent using the Mail API (ignored if --smtp_host '
      'is not set)')
  mail_group.add_argument(
      '--smtp_user', default='',
      help='username to use when connecting to the SMTP server '
      'specified in --smtp_host and --smtp_port')
  mail_group.add_argument(
      '--smtp_password', default='',
      help='password to use when connecting to the SMTP server '
      'specified in --smtp_host and --smtp_port')
  mail_group.add_argument(
      '--smtp_allow_tls',
      action=boolean_action.BooleanAction,
      const=True,
      default=True,
      help='Allow TLS to be used when the SMTP server announces TLS support '
      '(ignored if --smtp_host is not set)')

  # Search
  search_group = parser.add_argument_group('Search API')
  search_group.add_argument(
      '--search_indexes_path', default=None,
      type=parse_path,
      help='path to a file used to store search indexes '
      '(defaults to a file in --storage_path if not set)',)
  search_group.add_argument(
      '--clear_search_indexes',
      action=boolean_action.BooleanAction,
      const=True,
      default=False,
      help='clear the search indexes')

  # Taskqueue
  taskqueue_group = parser.add_argument_group('Task Queue API')
  taskqueue_group.add_argument(
      '--enable_task_running',
      action=boolean_action.BooleanAction,
      const=True,
      default=True,
      help='run "push" tasks created using the taskqueue API automatically')

  # Misc
  misc_group = parser.add_argument_group('Miscellaneous')
  misc_group.add_argument(
      '--allow_skipped_files',
      action=boolean_action.BooleanAction,
      const=True,
      default=False,
      restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
      help='make files specified in the app.yaml "skip_files" or "static" '
      'handles readable by the application.')
  misc_group.add_argument(
      '--watcher_ignore_re',
      type=re.compile,
      help='Regex string to specify files to be ignored by the filewatcher.',
      default=None)
  # No help to avoid lengthening help message for rarely used feature:
  # host name to which the server for API calls should bind.
  misc_group.add_argument(
      '--api_host', default=default_server_host,
      help='host name to which the api server should bind.')
  misc_group.add_argument(
      '--api_port', type=PortParser(), default=0,
      help='port to which the server for API calls should bind')
  misc_group.add_argument(
      '--api_server_supports_grpc',
      action=boolean_action.BooleanAction,
      const=True,
      default=False,
      help=argparse.SUPPRESS)
  misc_group.add_argument(
      '--grpc_api_port', type=PortParser(), default=0,
      help='port on which the gRPC API server listens.')
  misc_group.add_argument(
      '--automatic_restart',
      action=boolean_action.BooleanAction,
      const=True,
      default=True,
      restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
      help=('restart instances automatically when files relevant to their '
            'module are changed'))
  misc_group.add_argument(
      '--dev_appserver_log_level', default='info',
      choices=constants.LOG_LEVEL_TO_PYTHON_CONSTANT.keys(),
      help='the log level below which logging messages generated by '
      'the development server will not be displayed on the console (this '
      'flag is more useful for diagnosing problems in dev_appserver.py rather '
      'than in application code)')
  misc_group.add_argument(
      '--skip_sdk_update_check',
      action=boolean_action.BooleanAction,
      const=True,
      default=False,
      restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
      help='skip checking for SDK updates (if false, use .appcfg_nag to '
      'decide)')
  misc_group.add_argument(
      '--default_gcs_bucket_name', default=None,
      help='default Google Cloud Storage bucket name')
  misc_group.add_argument(
      '--env_var', action='append',
      restrict_configuration=[DEV_APPSERVER_CONFIGURATION],
      type=lambda kv: kv.split('=', 1), dest='env_variables',
      help='user defined environment variable for the runtime. each env_var is '
      'in the format of key=value, and you can define multiple envrionment '
      'variables. For example: --env_var KEY_1=val1 --env_var KEY_2=val2. '
      'You can also define environment variables in app.yaml.')
  misc_group.add_argument(
      '--check_java_for_cloud_datastore_emulator',
      action=boolean_action.BooleanAction,
      const=True,
      default=True,
      help=argparse.SUPPRESS)
  # The client id used for Google Analytics usage reporting. If this is set,
  # usage metrics will be sent to Google Analytics. This should only be set by
  # the Cloud SDK dev_appserver.py wrapper.
  misc_group.add_argument(
      '--google_analytics_client_id', default=None,
      help=argparse.SUPPRESS)
  # The user agent to use for Google Analytics usage reporting. This should only
  # be set by the Cloud SDK dev_appserver.py wrapper.
  misc_group.add_argument(
      '--google_analytics_user_agent', default=None,
      help=argparse.SUPPRESS)

  return parser
