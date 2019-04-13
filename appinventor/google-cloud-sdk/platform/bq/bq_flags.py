#!/usr/bin/env python
"""Flags for calling BigQuery."""

import os


import gflags as flags

FLAGS = flags.FLAGS
flags.DEFINE_string(
    'apilog', None,
    'Log all API requests and responses to the file specified by this flag. '
    'Also accepts "stdout" and "stderr". Specifying the empty string will '
    'direct to stdout.')
flags.DEFINE_string(
    'api',
    'https://www.googleapis.com',
    'API endpoint to talk to.'
)
flags.DEFINE_string('api_version', 'v2', 'API version to use.')
flags.DEFINE_boolean('debug_mode', False,
                     'Show tracebacks on Python exceptions.')
flags.DEFINE_string(
    'trace',
    None,
    'A tracing token of the form "token:<token>" '
    'to include in api requests.'
)
flags.DEFINE_string(
    'httplib2_debuglevel', None,
    'Instruct httplib2 to print debugging messages by setting debuglevel to '
    'the given value.')

flags.DEFINE_string(
    'bigqueryrc', os.path.join(os.path.expanduser('~'), '.bigqueryrc'),
    'Path to configuration file. The configuration file specifies '
    'new defaults for any flags, and can be overrridden by specifying the '
    'flag on the command line. If the --bigqueryrc flag is not specified, the '
    'BIGQUERYRC environment variable is used. If that is not specified, the '
    'path "~/.bigqueryrc" is used.')
flags.DEFINE_string('discovery_file', '',
                    'Filename for JSON document to read for discovery.')

flags.DEFINE_boolean(
    'disable_ssl_validation', False,
    'Disables HTTPS certificates validation. This is off by default.')
flags.DEFINE_string('ca_certificates_file', '',
                    'Location of CA certificates file.')
flags.DEFINE_string(
    'proxy_address', '',
    'The name or IP address of the proxy host to use for connecting to GCP.')
flags.DEFINE_string('proxy_port', '',
                    'The port number to use to connect to the proxy host.')
flags.DEFINE_string(
    'proxy_username', '',
    'The user name to use when authenticating with proxy host.')
flags.DEFINE_string('proxy_password', '',
                    'The password to use when authenticating with proxy host.')

flags.DEFINE_boolean(
    'synchronous_mode',
    True,
    'If True, wait for command completion before returning, and use the '
    'job completion status for error codes. If False, simply create the '
    'job, and use the success of job creation as the error code.',
    short_name='sync')
flags.DEFINE_string('project_id', '', 'Default project to use for requests.')
flags.DEFINE_string(
    'dataset_id', '',
    'Default dataset reference to use for requests (Ignored when not '
    'applicable.). Can be set as "project:dataset" or "dataset". If project '
    'is missing, the value of the project_id flag will be used.')
flags.DEFINE_string(
    'location', None,
    'Default geographic location to use when creating datasets or determining '
    'where jobs should run (Ignored when not applicable.)')

# This flag is "hidden" at the global scope to avoid polluting help
# text on individual commands for rarely used functionality.
flags.DEFINE_string(
    'job_id', None,
    'A unique job_id to use for the request. If not specified, this client '
    'will generate a job_id. Applies only to commands that launch jobs, '
    'such as cp, extract, load, and query.')
flags.DEFINE_boolean(
    'fingerprint_job_id', False,
    'Whether to use a job id that is derived from a fingerprint of the job '
    'configuration. This will prevent the same job from running multiple times '
    'accidentally.')
flags.DEFINE_boolean(
    'quiet',
    False,
    'If True, ignore status updates while jobs are running.',
    short_name='q')
flags.DEFINE_boolean(
    'headless', False,
    'Whether this bq session is running without user interaction. This '
    'affects behavior that expects user interaction, like whether '
    'debug_mode will break into the debugger and lowers the frequency '
    'of informational printing.')
flags.DEFINE_enum(
    'format', None, ['none', 'json', 'prettyjson', 'csv', 'sparse', 'pretty'],
    'Format for command output. Options include:'
    '\n pretty: formatted table output'
    '\n sparse: simpler table output'
    '\n prettyjson: easy-to-read JSON format'
    '\n json: maximally compact JSON'
    '\n csv: csv format with header'
    '\nThe first three are intended to be human-readable, and the latter '
    'three are for passing to another program. If no format is selected, '
    'one will be chosen based on the command run.')
flags.DEFINE_multistring(
    'job_property', None,
    'Additional key-value pairs to include in the properties field of '
    'the job configuration')  # No period: Multistring adds flagspec suffix.
flags.DEFINE_integer('max_rows_per_request', None,
                     'Specifies the max number of rows to return per read.')

flags.DEFINE_boolean(
    'enable_gdrive', None,
    'When set to true, requests new OAuth token with GDrive scope. '
    'When set to false, requests new OAuth token without GDrive scope.')

