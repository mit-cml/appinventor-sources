#!/usr/bin/env python
#
# Copyright 2012 Google Inc. All Rights Reserved.
"""Python script for interacting with BigQuery."""

__author__ = 'craigcitro@google.com (Craig Citro)'

import argparse
import cmd
import codecs
import collections
import datetime
import functools
import httplib
import json
import os
import pdb
import pipes
import pkgutil
import platform
import re
import shlex
import sys
import time
import traceback
import types

# Add to path dependecies if present.
_THIRD_PARTY_DIR = os.path.join(os.path.dirname(__file__), 'third_party')
if os.path.isdir(_THIRD_PARTY_DIR):
  sys.path.insert(0, _THIRD_PARTY_DIR)

# This strange import below ensures that the correct 'google' is imported.
# We reload after sys.path is updated, so we know if will find our google
# before any other.
# pylint:disable=g-import-not-at-top
if 'google' in sys.modules:
  import google
  try:
    reload(google)
  except NameError:
    import imp
    imp.reload(google)

from google_reauth.reauth_creds import Oauth2WithReauthCredentials

import googleapiclient
import httplib2

import oauth2client_4_0
import oauth2client_4_0.client
import oauth2client_4_0.contrib.devshell
import oauth2client_4_0.contrib.gce
import oauth2client_4_0.file
import oauth2client_4_0.service_account
import oauth2client_4_0.tools

import yaml

from google.apputils import app
from google.apputils import appcommands
import gflags as flags

import table_formatter
import bigquery_client
import bq_auth_flags
import bq_flags
import bq_utils


flags.ADOPT_module_key_flags(bq_flags)

FLAGS = flags.FLAGS
# These are long names.
# pylint: disable=g-bad-name
JobReference = bigquery_client.ApiClientHelper.JobReference
ProjectReference = bigquery_client.ApiClientHelper.ProjectReference
DatasetReference = bigquery_client.ApiClientHelper.DatasetReference
TableReference = bigquery_client.ApiClientHelper.TableReference
TransferConfigReference = (
    bigquery_client.ApiClientHelper.TransferConfigReference)
TransferRunReference = bigquery_client.ApiClientHelper.TransferRunReference
TransferLogReference = bigquery_client.ApiClientHelper.TransferLogReference
NextPageTokenReference = bigquery_client.ApiClientHelper.NextPageTokenReference
EncryptionServiceAccount = (
    bigquery_client.ApiClientHelper.EncryptionServiceAccount)
BigqueryClient = bigquery_client.BigqueryClient
JobIdGenerator = bigquery_client.JobIdGenerator
JobIdGeneratorIncrementing = bigquery_client.JobIdGeneratorIncrementing
JobIdGeneratorRandom = bigquery_client.JobIdGeneratorRandom
JobIdGeneratorFingerprint = bigquery_client.JobIdGeneratorFingerprint
ReservationReference = bigquery_client.ApiClientHelper.ReservationReference
SlotPoolReference = bigquery_client.ApiClientHelper.SlotPoolReference
ReservationGrantReference = bigquery_client.ApiClientHelper.ReservationGrantReference  # pylint: disable=line-too-long

# pylint: enable=g-bad-name


def _GetVersion():
  """Returns content of VERSION file which is same directory as this file."""
  root = 'bq'
  return pkgutil.get_data(root, 'VERSION')


_VERSION_NUMBER = _GetVersion()

if os.environ.get('CLOUDSDK_WRAPPER') == '1':
  _CLIENT_ID = '32555940559.apps.googleusercontent.com'
  _CLIENT_SECRET = 'ZmssLNjJy2998hD4CTg2ejr2'
  _CLIENT_USER_AGENT = 'google-cloud-sdk' + os.environ.get(
      'CLOUDSDK_VERSION', _VERSION_NUMBER)
else:
  _CLIENT_ID = '977385342095.apps.googleusercontent.com'
  _CLIENT_SECRET = 'wbER7576mc_1YOII0dGk7jEE'
  _CLIENT_USER_AGENT = 'bq/' + _VERSION_NUMBER

_GDRIVE_SCOPE = 'https://www.googleapis.com/auth/drive'
_BIGQUERY_SCOPE = 'https://www.googleapis.com/auth/bigquery'
_CLOUD_PLATFORM_SCOPE = 'https://www.googleapis.com/auth/cloud-platform'
_REAUTH_SCOPE = 'https://www.googleapis.com/auth/accounts.reauth'


_CLIENT_INFO = {
    'client_id': _CLIENT_ID,
    'client_secret': _CLIENT_SECRET,
    'user_agent': _CLIENT_USER_AGENT,
}
_BIGQUERY_TOS_MESSAGE = (
    'In order to get started, please visit the Google APIs Console to '
    'create a project and agree to our Terms of Service:\n'
    '\thttp://code.google.com/apis/console\n\n'
    'For detailed sign-up instructions, please see our Getting Started '
    'Guide:\n'
    '\thttps://developers.google.com/bigquery/docs/getting-started\n\n'
    'Once you have completed the sign-up process, please try your command '
    'again.')
_DELIMITER_MAP = {
    'tab': '\t',
    '\\t': '\t',
}
_DDL_OPERATION_MAP = {
    'SKIP': 'Skipped',
    'CREATE': 'Created',
    'REPLACE': 'Replaced',
    'ALTER': 'Altered',
    'DROP': 'Dropped',
}

# These aren't relevant for user-facing docstrings:
# pylint: disable=g-doc-return-or-yield
# pylint: disable=g-doc-args
# TODO(user): Write some explanation of the structure of this file.

####################
# flags processing
####################


def _FormatDataTransferIdentifiers(client, transfer_identifier):
  """Formats a transfer config or run identifier.

  Transfer configuration/run commands should be able to support different
  formats of how the user could input the project information. This function
  will take the user input and create a uniform transfer config or
  transfer run reference that can be used for various commands.

  This function will also set the client's project id to the specified
  project id.

  Returns:
    The formatted transfer config or run.
  """

  formatted_identifier = transfer_identifier
  match = re.search(r'projects/([^/]+)', transfer_identifier)
  if not match:
    formatted_identifier = ('projects/' + client.GetProjectReference().projectId
                            + '/' + transfer_identifier)
  else:
    client.project_id = match.group(1)

  return formatted_identifier


def _FormatProjectIdentifier(client, project_id):
  """Formats a project identifier.

  If the user specifies a project with "projects/${PROJECT_ID}", isolate the
  project id and return it.

  This function will also set the client's project id to the specified
  project id.

  Returns:
    The project is.
  """

  formatted_identifier = project_id
  match = re.search(r'projects/([^/]+)', project_id)
  if match:
    formatted_identifier = match.group(1)
    client.project_id = formatted_identifier

  return formatted_identifier


def _ValidateGlobalFlags():
  """Validate combinations of global flag values."""
  if FLAGS.service_account and FLAGS.use_gce_service_account:
    raise app.UsageError(
        'Cannot specify both --service_account and --use_gce_service_account.')


def ValidateAtMostOneSelected(*args):
  """Validates that at most one of the argument flags is selected.

  Returns:
    True if more than 1 flag was selected, False if 1 or 0 were selected.
  """
  count = 0
  for arg in args:
    if arg:
      count += 1
  return count > 1


def _ConfigureLogging(client):
  try:
    client.ConfigurePythonLogger(FLAGS.apilog)
  except IOError as e:
    if e.errno == 2:
      print 'Could not configure logging. %s: %s' % (e.strerror, e.filename)
      sys.exit(1)
    raise e


def _UseServiceAccount():
  return bool(FLAGS.use_gce_service_account or FLAGS.service_account
             )


class CredentialLoader(object):
  """Base class for credential loader."""

  def Load(self):
    """Loads credential."""
    cred = self._Load()
    cred._user_agent = _CLIENT_USER_AGENT  # pylint: disable=protected-access
    return cred

  def _Load(self):
    raise NotImplementedError()


class CachedCredentialLoader(CredentialLoader):
  """Base class to add cache capability to credential loader.

  It will attempt to load credential from local cache file first before calling
  derived class to load credential from source. Once credential is retrieved, it
  will save to local cache file for future use.
  """

  def __init__(self, credential_cache_file, read_cache_first=True):
    """Creates CachedCredentialLoader instance.

    Args:
      credential_cache_file: path to a local file to cache credential.
      read_cache_first: whether to load credential from cache first.
    Raises:
      BigqueryError: if cache file cannot be created to store credential.
    """
    self.credential_cache_file = credential_cache_file
    self._read_cache_first = read_cache_first
    try:
      self._storage = oauth2client_4_0.file.Storage(credential_cache_file)
    except OSError as e:
      raise bigquery_client.BigqueryError(
          'Cannot create credential file %s: %s' % (credential_cache_file, e))
    self._verify_storage = False

  @property
  def storage(self):
    return self._storage

  def Load(self):
    cred = self._LoadFromCache() if self._read_cache_first else None
    if cred:
      return cred

    cred = super(CachedCredentialLoader, self).Load()
    if not cred:
      return None

    # Save credentials to storage now to reuse and also avoid a warning message.
    self._storage.put(cred)

    if self._verify_storage:
      # Verify credentials storage is ok now.
      try:
        self._storage.get()
      except BaseException as e:  # pylint: disable=broad-except
        self._RaiseCredentialsCorrupt(e)

    cred.set_store(self._storage)
    return cred

  def _LoadFromCache(self):
    """Loads credential from cache file."""
    if not os.path.exists(self.credential_cache_file):
      return None

    try:
      return self._storage.get()
    except ImportError as e:
      # This is a workaround for switching between oauth2client versions.
      is_v2_storage = (str(e).startswith('No module named oauth2client.'))
      if is_v2_storage:
        os.remove(self.credential_cache_file)
        self._storage = oauth2client_4_0.file.Storage(
            self.credential_cache_file)
        self._storage._create_file_if_needed()  # pylint: disable=protected-access
        # Verify credentials storage is ok now. We don't want to silently
        # recreate credentials every time, if this didn't work for some reason.
        self._verify_storage = True
      return None
    except BaseException as e:  # pylint: disable=broad-except
      self._RaiseCredentialsCorrupt(e)

  def _RaiseCredentialsCorrupt(self, e):
    BigqueryCmd.ProcessError(
        e,
        name='GetCredentialsFromFlags',
        message_prefix=(
            'Credentials appear corrupt. Please delete the credential file '
            'and try your command again. You can delete your credential '
            'file using "bq init --delete_credentials".\n\nIf that does '
            'not work, you may have encountered a bug in the BigQuery CLI.'))
    sys.exit(1)


class ServiceAccountPrivateKeyLoader(CachedCredentialLoader):
  """Base class for loading credential from service account."""

  def Load(self):
    if not oauth2client_4_0.client.HAS_OPENSSL:
      raise app.UsageError(
          'BigQuery requires OpenSSL to be installed in order to use '
          'service account credentials. Please install OpenSSL '
          'and the Python OpenSSL package.')
    return super(ServiceAccountPrivateKeyLoader, self).Load()


class ServiceAccountPrivateKeyFileLoader(ServiceAccountPrivateKeyLoader):
  """Credential loader for private key stored in a file."""

  def __init__(self, service_account, file_path, password, *args, **kwargs):
    """Creates ServiceAccountPrivateKeyFileLoader instance.

    Args:
      service_account: service account the private key is for.
      file_path: path to the file containing private key (in P12 format).
      password: password to uncrypt the private key file.
      *args: additional arguments to apply to base class.
      **kwargs: additional keyword arguments to apply to base class.
    """
    super(ServiceAccountPrivateKeyFileLoader, self).__init__(*args, **kwargs)
    self._service_account = service_account
    self._file_path = file_path
    self._password = password

  def _Load(self):
    try:
      return (oauth2client_4_0.service_account.ServiceAccountCredentials
              .from_p12_keyfile(
                  service_account_email=self._service_account,
                  filename=self._file_path,
                  scopes=_GetClientScopeFromFlags(),
                  private_key_password=self._password,
                  token_uri=oauth2client_4_0.GOOGLE_TOKEN_URI,
                  revoke_uri=oauth2client_4_0.GOOGLE_REVOKE_URI))
    except IOError as e:
      raise app.UsageError(
          'Service account specified, but private key in file "%s" '
          'cannot be read:\n%s' % (self._file_path, e))






class ApplicationDefaultCredentialFileLoader(CachedCredentialLoader):
  """Credential loader for application default credential file."""

  def __init__(self, credential_file, *args, **kwargs):
    """Creates ApplicationDefaultCredentialFileLoader instance.

    Args:
      credential_file: path to credential file in json format.
      *args: additional arguments to apply to base class.
      **kwargs: additional keyword arguments to apply to base class.
    """
    super(ApplicationDefaultCredentialFileLoader, self).__init__(
        *args, **kwargs)
    self._credential_file = credential_file

  def _Load(self):
    """Loads credentials from given application default credential file."""
    with open(self._credential_file) as file_obj:
      credentials = json.load(file_obj)

    client_scope = _GetClientScopeFromFlags()
    if credentials['type'] == oauth2client_4_0.client.AUTHORIZED_USER:
      return Oauth2WithReauthCredentials(
          access_token=None,
          client_id=credentials['client_id'],
          client_secret=credentials['client_secret'],
          refresh_token=credentials['refresh_token'],
          token_expiry=None,
          token_uri=oauth2client_4_0.GOOGLE_TOKEN_URI,
          user_agent=_CLIENT_USER_AGENT,
          scopes=client_scope)
    else:  # Service account
      credentials['type'] = oauth2client_4_0.client.SERVICE_ACCOUNT
      service_account_credentials = (
          oauth2client_4_0.service_account.ServiceAccountCredentials
          .from_json_keyfile_dict(
              keyfile_dict=credentials,
              scopes=client_scope,
              token_uri=oauth2client_4_0.GOOGLE_TOKEN_URI,
              revoke_uri=oauth2client_4_0.GOOGLE_REVOKE_URI))
      service_account_credentials._user_agent = _CLIENT_USER_AGENT  # pylint: disable=protected-access
      return service_account_credentials


def _GetClientScopeFromFlags():
  """Returns auth scopes based on user supplied flags."""
  client_scope = [_BIGQUERY_SCOPE, _CLOUD_PLATFORM_SCOPE]
  if FLAGS.enable_gdrive is None or FLAGS.enable_gdrive:
    client_scope.append(_GDRIVE_SCOPE)
  client_scope.append(_REAUTH_SCOPE)
  return client_scope


def _GetCredentialsFromFlags():
  """Returns credentials based on user supplied flags."""


  if FLAGS.use_gce_service_account:
    # In the case of a GCE service account, we can skip the entire
    # process of loading from storage.
    return oauth2client_4_0.contrib.gce.AppAssertionCredentials()


  if FLAGS.service_account:
    if not FLAGS.service_account_credential_file:
      raise app.UsageError(
          'The flag --service_account_credential_file must be specified '
          'if --service_account is used.')

    if FLAGS.service_account_private_key_file:
      loader = ServiceAccountPrivateKeyFileLoader(
          credential_cache_file=FLAGS.service_account_credential_file,
          read_cache_first=not FLAGS.enable_gdrive,
          service_account=FLAGS.service_account,
          file_path=FLAGS.service_account_private_key_file,
          password=FLAGS.service_account_private_key_password)
    else:
      raise app.UsageError('Service account authorization requires '
                           '--service_account_private_key_file flag to be set.')
  elif FLAGS.application_default_credential_file:
    if not FLAGS.credential_file:
      raise app.UsageError('The flag --credential_file must be specified if '
                           '--application_default_credential_file is used.')
    loader = ApplicationDefaultCredentialFileLoader(
        credential_cache_file=FLAGS.credential_file,
        read_cache_first=not FLAGS.enable_gdrive,
        credential_file=FLAGS.application_default_credential_file)
  else:
    raise app.UsageError(
        'bq.py should not be invoked. Use bq command instead.')

  credentials = loader.Load()


  if type(credentials) == oauth2client_4_0.client.OAuth2Credentials:  # pylint: disable=unidiomatic-typecheck
    credentials = Oauth2WithReauthCredentials.from_OAuth2Credentials(
        credentials)

  return credentials


def _GetFormatterFromFlags(secondary_format='sparse'):
  if FLAGS['format'].present:
    return table_formatter.GetFormatter(FLAGS.format)
  else:
    return table_formatter.GetFormatter(secondary_format)


def _PrintDryRunInfo(job):
  """Prints the dry run info."""
  num_bytes = job['statistics']['query']['totalBytesProcessed']
  num_bytes_accuracy = job['statistics']['query'].get(
      'totalBytesProcessedAccuracy', 'PRECISE')
  if FLAGS.format in ['prettyjson', 'json']:
    _PrintFormattedJsonObject(job)
  elif FLAGS.format == 'csv':
    print num_bytes
  else:
    if num_bytes_accuracy == 'PRECISE':
      print(
          'Query successfully validated. Assuming the tables are not modified, '
          'running this query will process %s bytes of data.' % (num_bytes,))
    elif num_bytes_accuracy == 'LOWER_BOUND':
      print(
          'Query successfully validated. Assuming the tables are not modified, '
          'running this query will process lower bound of %s bytes of data.' %
          (num_bytes,))
    elif num_bytes_accuracy == 'UPPER_BOUND':
      print(
          'Query successfully validated. Assuming the tables are not modified, '
          'running this query will process upper bound of %s bytes of data.' %
          (num_bytes,))
    else:
      print(
          'Query successfully validated. Assuming the tables are not modified, '
          'running this query will process %s bytes of data and the accuracy '
          'is unknown because of federated tables or clustered tables.' %
          (num_bytes,))


def _PrintFormattedJsonObject(obj):
  if FLAGS.format == 'prettyjson':
    print json.dumps(obj, sort_keys=True, indent=2)
  else:
    print json.dumps(obj, separators=(',', ':'))


def _GetJobIdFromFlags():
  """Returns the job id or job generator from the flags."""
  if FLAGS.fingerprint_job_id and FLAGS.job_id:
    raise app.UsageError(
        'The fingerprint_job_id flag cannot be specified with the job_id '
        'flag.')
  if FLAGS.fingerprint_job_id:
    return JobIdGeneratorFingerprint()
  elif FLAGS.job_id is None:
    return JobIdGeneratorIncrementing(JobIdGeneratorRandom())
  elif FLAGS.job_id:
    return FLAGS.job_id
  else:
    # User specified a job id, but it was empty. Let the
    # server come up with a job id.
    return None


def _GetWaitPrinterFactoryFromFlags():
  """Returns the default wait_printer_factory to use while waiting for jobs."""
  if FLAGS.quiet:
    return BigqueryClient.QuietWaitPrinter
  if FLAGS.headless:
    return BigqueryClient.TransitionWaitPrinter
  return BigqueryClient.VerboseWaitPrinter


def _RawInput(message):
  try:
    return raw_input(message)
  except EOFError:
    if sys.stdin.isatty():
      print '\nGot EOF; exiting.'
    else:
      print '\nGot EOF; exiting. Is your input from a terminal?'
    sys.exit(1)


def _PromptWithDefault(message):
  """Prompts user with message, return key pressed or '' on enter."""
  if FLAGS.headless:
    print 'Running --headless, accepting default for prompt: %s' % (message,)
    return ''
  return _RawInput(message).lower()


def _PromptYN(message):
  """Prompts user with message, returning the key 'y', 'n', or '' on enter."""
  response = None
  while response not in ['y', 'n', '']:
    response = _PromptWithDefault(message)
  return response


def _NormalizeFieldDelimiter(field_delimiter):
  """Validates and returns the correct field_delimiter."""
  # The only non-string delimiter we allow is None, which represents
  # no field delimiter specified by the user.
  if field_delimiter is None:
    return field_delimiter
  try:
    # We check the field delimiter flag specifically, since a
    # mis-entered Thorn character generates a difficult to
    # understand error during request serialization time.
    _ = field_delimiter.decode(sys.stdin.encoding or 'utf8')
  except UnicodeDecodeError:
    raise app.UsageError(
        'The field delimiter flag is not valid. Flags must be '
        'specified in your default locale. For example, '
        'the Latin 1 representation of Thorn is byte code FE, '
        'which in the UTF-8 locale would be expressed as C3 BE.')

  # Allow TAB and \\t substitution.
  key = field_delimiter.lower()
  return _DELIMITER_MAP.get(key, field_delimiter)


def _ValidateHivePartitioningOption(hive_partitioning_mode):
  """Validates the string provided is one the API accepts.

  Should not receive None as an input, since that will fail the comparison.
  Args:
    hive_partitioning_mode: String representing which hive partitioning mode is
      requested.  Only 'AUTO' and 'STRINGS' are supported.
  """
  if hive_partitioning_mode != 'AUTO' and hive_partitioning_mode != 'STRINGS':
    raise app.UsageError(
        'Only the following hive partitioning modes are supported: "AUTO" and '
        '"STRINGS"')




def _ParseLabels(labels):
  """Parses a list of user-supplied strings representing labels.

  Args:
    labels: A list of user-supplied strings representing labels.  It is expected
        to be in the format "key:value".

  Returns:
    A dict mapping label keys to label values.

  Raises:
    UsageError: Incorrect label arguments were supplied.
  """
  labels_dict = {}
  for key_value in labels:
    k, _, v = key_value.partition(':')
    k = k.strip()
    if k in labels_dict:
      raise app.UsageError('Cannot specify label key "%s" multiple times' % k)
    if k.strip():
      labels_dict[k.strip()] = v.strip()
  return labels_dict


class TablePrinter(object):
  """Base class for printing a table, with a default implementation."""

  def __init__(self, **kwds):
    super(TablePrinter, self).__init__()
    # Most extended classes will require state.
    for key, value in kwds.iteritems():
      setattr(self, key, value)

  @staticmethod
  def _ValidateFields(fields, formatter):
    if isinstance(formatter, table_formatter.CsvFormatter):
      for field in fields:
        if field['type'].upper() == 'RECORD':
          raise app.UsageError(('Error printing table: Cannot print record '
                                'field "%s" in CSV format.') % field['name'])
        if field.get('mode', 'NULLABLE').upper() == 'REPEATED':
          raise app.UsageError(('Error printing table: Cannot print repeated '
                                'field "%s" in CSV format.') % (field['name']))

  @staticmethod
  def _NormalizeRecord(field, value):
    """Returns bq-specific formatting of a RECORD type."""
    result = collections.OrderedDict()
    for subfield, subvalue in zip(field.get('fields', []), value):
      result[subfield.get('name', '')] = TablePrinter._NormalizeField(
          subfield, subvalue)
    return result

  @staticmethod
  def _NormalizeTimestamp(unused_field, value):
    """Returns bq-specific formatting of a TIMESTAMP type."""
    try:
      date = datetime.datetime.utcfromtimestamp(float(value))
      # Our goal is the equivalent of '%Y-%m-%d %H:%M:%S' via strftime but that
      # doesn't work for dates with years prior to 1900.  Instead we zero out
      # fractional seconds then call isoformat with a space separator.
      date = date.replace(microsecond=0)
      return date.isoformat(' ')
    except ValueError:
      return '<date out of range for display>'

  _FIELD_NORMALIZERS = {
      'RECORD': _NormalizeRecord.__func__,
      'TIMESTAMP': _NormalizeTimestamp.__func__,
  }

  @staticmethod
  def _NormalizeField(field, value):
    """Returns bq-specific formatting of a field."""
    if value is None:
      return None
    normalizer = TablePrinter._FIELD_NORMALIZERS.get(
        field.get('type', '').upper(), lambda _, x: x)
    if field.get('mode', '').upper() == 'REPEATED':
      return [normalizer(field, value) for value in value]
    return normalizer(field, value)

  @staticmethod
  def _MaybeConvertToJson(value):
    """Converts dicts and lists to JSON; returns everything else as-is."""
    if isinstance(value, dict) or isinstance(value, list):
      return json.dumps(value, separators=(',', ':'), ensure_ascii=False)
    return value

  @staticmethod
  def _FormatRow(fields, row, formatter):
    """Convert fields in a single row to bq-specific formatting."""
    values = [
        TablePrinter._NormalizeField(field, value)
        for field, value in zip(fields, row)
    ]
    # Convert complex values to JSON if we're not already outputting as such.
    if not isinstance(formatter, table_formatter.JsonFormatter):
      values = map(TablePrinter._MaybeConvertToJson, values)
    # Convert NULL values to strings for CSV and non-JSON formats.
    if isinstance(formatter, table_formatter.CsvFormatter):
      values = ['' if value is None else value for value in values]
    elif not isinstance(formatter, table_formatter.JsonFormatter):
      values = ['NULL' if value is None else value for value in values]
    return values

  def PrintTable(self, fields, rows):
    formatter = _GetFormatterFromFlags(secondary_format='pretty')
    self._ValidateFields(fields, formatter)
    formatter.AddFields(fields)
    formatter.AddRows(
        TablePrinter._FormatRow(fields, row, formatter) for row in rows)
    formatter.Print()


class Factory(object):
  """Class encapsulating factory creation of BigqueryClient."""
  _BIGQUERY_CLIENT_FACTORY = None

  class ClientTablePrinter(object):
    _TABLE_PRINTER = None

    @classmethod
    def GetTablePrinter(cls):
      if cls._TABLE_PRINTER is None:
        cls._TABLE_PRINTER = TablePrinter()
      return cls._TABLE_PRINTER

    @classmethod
    def SetTablePrinter(cls, printer):
      if not isinstance(printer, TablePrinter):
        raise TypeError('Printer must be an instance of TablePrinter.')
      cls._TABLE_PRINTER = printer

  @classmethod
  def GetBigqueryClientFactory(cls):
    if cls._BIGQUERY_CLIENT_FACTORY is None:
      cls._BIGQUERY_CLIENT_FACTORY = bigquery_client.BigqueryClient
    return cls._BIGQUERY_CLIENT_FACTORY

  @classmethod
  def SetBigqueryClientFactory(cls, factory):
    if not issubclass(factory, bigquery_client.BigqueryClient):
      raise TypeError('Factory must be subclass of BigqueryClient.')
    cls._BIGQUERY_CLIENT_FACTORY = factory


class Client(object):
  """Class wrapping a singleton bigquery_client.BigqueryClient."""
  client = None

  @staticmethod
  def Create(**kwds):
    """Build a new BigqueryClient configured from kwds and FLAGS."""

    def KwdsOrFlags(name):
      return kwds[name] if name in kwds else getattr(FLAGS, name)

    # Note that we need to handle possible initialization tasks
    # for the case of being loaded as a library.
    bq_utils.ProcessBigqueryrc()
    _ConfigureLogging(bigquery_client)

    if FLAGS.httplib2_debuglevel:
      httplib2.debuglevel = FLAGS.httplib2_debuglevel

    if 'credentials' in kwds:
      credentials = kwds.pop('credentials')
    else:
      credentials = _GetCredentialsFromFlags()
    assert credentials is not None

    client_args = {}
    global_args = (
        'credential_file',
        'job_property',
        'project_id',
        'dataset_id',
        'trace',
        'sync',
        'api',
        'api_version')
    for name in global_args:
      client_args[name] = KwdsOrFlags(name)

    client_args['wait_printer_factory'] = _GetWaitPrinterFactoryFromFlags()
    if FLAGS.discovery_file:
      with open(FLAGS.discovery_file) as f:
        client_args['discovery_document'] = f.read()
    bigquery_client_factory = Factory.GetBigqueryClientFactory()
    return bigquery_client_factory(credentials=credentials, **client_args)

  @classmethod
  def Get(cls):
    """Return a BigqueryClient initialized from flags."""
    if cls.client is None:
      try:
        cls.client = Client.Create()
      except ValueError as e:
        # Convert constructor parameter errors into flag usage errors.
        raise app.UsageError(e)
    return cls.client

  @classmethod
  def Delete(cls):
    """Delete the existing client.

    This is needed when flags have changed, and we need to force
    client recreation to reflect new flag values.
    """
    cls.client = None


def _Typecheck(obj, types, message=None):  # pylint: disable=redefined-outer-name
  """Raises a user error if obj is not an instance of types."""
  if not isinstance(obj, types):
    message = message or 'Type of %s is not one of %s' % (obj, types)
    raise app.UsageError(message)


# TODO(user): This code uses more than the average amount of
# Python magic. Explain what the heck is going on throughout.
class NewCmd(appcommands.Cmd):
  """Featureful extension of appcommands.Cmd."""

  def __init__(self, name, flag_values):
    super(NewCmd, self).__init__(name, flag_values)
    run_with_args = getattr(self, 'RunWithArgs', None)
    self._new_style = isinstance(run_with_args, types.MethodType)
    if self._new_style:
      func = run_with_args.im_func
      code = func.func_code  # pylint: disable=redefined-outer-name
      self._full_arg_list = list(code.co_varnames[:code.co_argcount])
      # TODO(user): There might be some corner case where this
      # is *not* the right way to determine bound vs. unbound method.
      if isinstance(run_with_args.im_self, run_with_args.im_class):
        self._full_arg_list.pop(0)
      self._max_args = len(self._full_arg_list)
      self._min_args = self._max_args - len(func.func_defaults or [])
      self._star_args = bool(code.co_flags & 0x04)
      self._star_kwds = bool(code.co_flags & 0x08)
      if self._star_args:
        self._max_args = sys.maxint
      self._debug_mode = FLAGS.debug_mode
      self.surface_in_shell = True
      self.__doc__ = self.RunWithArgs.__doc__
    elif self.Run.im_func is NewCmd.Run.im_func:
      raise appcommands.AppCommandsError(
          'Subclasses of NewCmd must override Run or RunWithArgs')

  def __getattr__(self, name):
    if name in self._command_flags:
      return self._command_flags[name].value
    return super(NewCmd, self).__getattribute__(name)

  def _GetFlag(self, flagname):
    if flagname in self._command_flags:
      return self._command_flags[flagname]
    else:
      return None

  def _CheckFlags(self):
    """Validate flags after command specific flags have been loaded.

    This function will run through all values in appcommands._cmd_argv and
    pick out any unused flags and verify their validity.  If the flag is
    not defined, we will print the flags.FlagsError text and exit; otherwise,
    we will print a positioning error message and exit.  Print statements
    were used in this case because raising app.UsageError caused the usage
    help text to be printed.

    If no extraneous flags exist, this function will do nothing.
    """
    unused_flags = [
        f for f in appcommands.GetCommandArgv()
        if f.startswith('--') or f.startswith('-')
    ]
    for flag in unused_flags:
      flag_name = flag[4:] if flag.startswith('--no') else flag[2:]
      flag_name = flag_name.split('=')[0]
      if flag_name not in FLAGS:
        print("FATAL Flags parsing error: Unknown command line flag '%s'\n"
              "Run 'bq help' to get help" % flag)
        sys.exit(1)
      else:
        print("FATAL Flags positioning error: Flag '%s' appears after final "
              'command line argument. Please reposition the flag.\n'
              "Run 'bq help' to get help." % flag)
        sys.exit(1)

  def Run(self, argv):
    """Run this command.

    If self is a new-style command, we set up arguments and call
    self.RunWithArgs, gracefully handling exceptions. If not, we
    simply call self.Run(argv).

    Args:
      argv: List of arguments as strings.

    Returns:
      0 on success, nonzero on failure.
    """
    self._CheckFlags()
    if not self._new_style:
      return super(NewCmd, self).Run(argv)

    original_values = {
        name: self._command_flags[name].value for name in self._command_flags
    }
    try:
      args = self._command_flags(argv)[1:]
      for flag_name in self._command_flags:
        value = self._command_flags[flag_name].value
        setattr(self, flag_name, value)
        if value == original_values[flag_name]:
          original_values.pop(flag_name)
      new_args = []
      for argname in self._full_arg_list[:self._min_args]:
        flag = self._GetFlag(argname)
        if flag is not None and flag.present:
          new_args.append(flag.value)
        elif args:
          new_args.append(args.pop(0))
        else:
          print 'Not enough positional args, still looking for %s' % (argname,)
          if self.usage:
            print 'Usage: %s' % (self.usage,)
          return 1

      new_kwds = {}
      for argname in self._full_arg_list[self._min_args:]:
        flag = self._GetFlag(argname)
        if flag is not None and flag.present:
          new_kwds[argname] = flag.value
        elif args:
          new_kwds[argname] = args.pop(0)

      if args and not self._star_args:
        print 'Too many positional args, still have %s' % (args,)
        return 1
      new_args.extend(args)

      if self._debug_mode:
        return self.RunDebug(new_args, new_kwds)
      else:
        return self.RunSafely(new_args, new_kwds)
    finally:
      for flag, value in original_values.iteritems():
        setattr(self, flag, value)
        self._command_flags[flag].Parse(value)

  def RunCmdLoop(self, argv):
    """Hook for use in cmd.Cmd-based command shells."""
    try:
      args = shlex.split(argv)
    except ValueError as e:
      raise SyntaxError(bigquery_client.EncodeForPrinting(e))
    return self.Run([self._command_name] + args)

  def _HandleError(self, e):
    message = bigquery_client.EncodeForPrinting(e)
    if isinstance(e, bigquery_client.BigqueryClientConfigurationError):
      message += ' Try running "bq init".'
    print 'Exception raised in %s operation: %s' % (self._command_name, message)
    return 1

  def RunDebug(self, args, kwds):
    """Run this command in debug mode."""
    try:
      return_value = self.RunWithArgs(*args, **kwds)
    # pylint: disable=broad-except
    except (BaseException, googleapiclient.errors.ResumableUploadError) as e:
      # Don't break into the debugger for expected exceptions.
      if (isinstance(e, app.UsageError) or
          (isinstance(e, bigquery_client.BigqueryError) and
           not isinstance(e, bigquery_client.BigqueryInterfaceError)) or
          isinstance(e, googleapiclient.errors.ResumableUploadError)):
        return self._HandleError(e)
      print
      print '****************************************************'
      print '**  Unexpected Exception raised in bq execution!  **'
      if FLAGS.headless:
        print '**  --headless mode enabled, exiting.             **'
        print '**  See STDERR for traceback.                     **'
      else:
        print '**  --debug_mode enabled, starting pdb.           **'
      print '****************************************************'
      print
      traceback.print_exc()
      print
      if not FLAGS.headless:
        pdb.post_mortem()
      return 1
    return return_value

  def RunSafely(self, args, kwds):
    """Run this command, turning exceptions into print statements."""
    try:
      return_value = self.RunWithArgs(*args, **kwds)
    except BaseException as e:
      return self._HandleError(e)
    return return_value


class BigqueryCmd(NewCmd):
  """Bigquery-specific NewCmd wrapper."""

  def _NeedsInit(self):
    """Returns true if this command requires the init command before running.

    Subclasses will override for any exceptional cases.
    """
    return (not _UseServiceAccount() and
            not (os.path.exists(bq_utils.GetBigqueryRcFilename()) or
                 os.path.exists(FLAGS.credential_file)))

  def Run(self, argv):
    """Bigquery commands run `init` before themselves if needed."""

    if FLAGS.debug_mode:
      cmd_flags = [
          FLAGS[f].Serialize().strip() for f in FLAGS if FLAGS[f].present
      ]
      print ' '.join(sorted(set(f for f in cmd_flags if f)))

    if self._NeedsInit():
      appcommands.GetCommandByName('init').Run(['init'])
    return super(BigqueryCmd, self).Run(argv)

  def RunSafely(self, args, kwds):
    """Run this command, printing information about any exceptions raised."""
    try:
      return_value = self.RunWithArgs(*args, **kwds)
    except BaseException as e:
      return BigqueryCmd.ProcessError(e, name=self._command_name)
    return return_value

  @staticmethod
  def ProcessError(
      e,
      name='unknown',
      message_prefix='You have encountered a bug in the BigQuery CLI.'):
    """Translate an error message into some printing and a return code."""

    if isinstance(e, SystemExit):
      return e.code  # sys.exit called somewhere, hopefully intentionally.

    response = []
    retcode = 1

    (etype, value, tb) = sys.exc_info()
    trace = ''.join(traceback.format_exception(etype, value, tb))
    # pragma pylint: disable=line-too-long
    contact_us_msg = (
        'Please file a bug report in our '
        'public '
        'issue tracker:\n'
        '  https://issuetracker.google.com/issues/new?component=187149&template=0\n'
        'Please include a brief description of '
        'the steps that led to this issue, as well as '
        'any rows that can be made public from '
        'the following information: \n\n')
    error_details = ('========================================\n'
                     '== Platform ==\n'
                     '  %s\n'
                     '== bq version ==\n'
                     '  %s\n'
                     '== Command line ==\n'
                     '  %s\n'
                     '== UTC timestamp ==\n'
                     '  %s\n'
                     '== Error trace ==\n'
                     '%s'
                     '========================================\n') % (
                         ':'.join([
                             platform.python_implementation(),
                             platform.python_version(),
                             platform.platform()
                         ]), _VERSION_NUMBER, sys.argv,
                         time.strftime('%Y-%m-%d %H:%M:%S', time.gmtime()),
                         trace)

    codecs.register_error('strict', codecs.replace_errors)
    message = bigquery_client.EncodeForPrinting(e)
    if isinstance(e, (bigquery_client.BigqueryNotFoundError,
                      bigquery_client.BigqueryDuplicateError)):
      response.append('BigQuery error in %s operation: %s' % (name, message))
      retcode = 2
    elif isinstance(e, bigquery_client.BigqueryTermsOfServiceError):
      response.append(str(e) + '\n')
      response.append(_BIGQUERY_TOS_MESSAGE)
    elif isinstance(e, bigquery_client.BigqueryInvalidQueryError):
      response.append('Error in query string: %s' % (message,))
    elif (isinstance(e, bigquery_client.BigqueryError) and
          not isinstance(e, bigquery_client.BigqueryInterfaceError)):
      response.append('BigQuery error in %s operation: %s' % (name, message))
    elif isinstance(e, (app.UsageError, TypeError)):
      response.append(message)
    elif (isinstance(e, SyntaxError) or
          isinstance(e, bigquery_client.BigquerySchemaError)):
      response.append('Invalid input: %s' % (message,))
    elif isinstance(e, flags.FlagsError):
      response.append('Error parsing command: %s' % (message,))
    elif isinstance(e, KeyboardInterrupt):
      response.append('')
    else:  # pylint: disable=broad-except
      # Errors with traceback information are printed here.
      # The traceback module has nicely formatted the error trace
      # for us, so we don't want to undo that via TextWrap.
      if isinstance(e, bigquery_client.BigqueryInterfaceError):
        message_prefix = (
            'Bigquery service returned an invalid reply in %s operation: %s.'
            '\n\n'
            'Please make sure you are using the latest version '
            'of the bq tool and try again. '
            'If this problem persists, you may have encountered a bug in the '
            'bigquery client.' % (name, message))
      elif isinstance(e, oauth2client_4_0.client.Error):
        message_prefix = (
            'Authorization error. This may be a network connection problem, '
            'so please try again. If this problem persists, the credentials '
            'may be corrupt. Try deleting and re-creating your credentials. '
            'You can delete your credentials using '
            '"bq init --delete_credentials".'
            '\n\n'
            'If this problem still occurs, you may have encountered a bug '
            'in the bigquery client.')
      elif (isinstance(e, httplib.HTTPException) or
            isinstance(e, googleapiclient.errors.Error) or
            isinstance(e, httplib2.HttpLib2Error)):
        message_prefix = (
            'Network connection problem encountered, please try again.'
            '\n\n'
            'If this problem persists, you may have encountered a bug in the '
            'bigquery client.')

      message = message_prefix + ' ' + contact_us_msg
      wrap_error_message = True
      if wrap_error_message:
        message = flags.TextWrap(message)
      print message
      print error_details
      response.append(
          'Unexpected exception in %s operation: %s' % (name, message))

    response_message = '\n'.join(response)
    wrap_error_message = True
    if wrap_error_message:
      response_message = flags.TextWrap(response_message)
    print response_message
    return retcode

  def PrintJobStartInfo(self, job):
    """Print a simple status line."""
    if FLAGS.format in ['prettyjson', 'json']:
      _PrintFormattedJsonObject(job)
    else:
      reference = BigqueryClient.ConstructObjectReference(job)
      print 'Successfully started %s %s' % (self._command_name, reference)

  def _ProcessCommandRc(self, fv):
    bq_utils.ProcessBigqueryrcSection(self._command_name, fv)


class _Load(BigqueryCmd):
  usage = """load <destination_table> <source> <schema>"""

  def __init__(self, name, fv):
    super(_Load, self).__init__(name, fv)
    flags.DEFINE_string(
        'field_delimiter',
        None,
        'The character that indicates the boundary between columns in the '
        'input file. "\\t" and "tab" are accepted names for tab.',
        short_name='F',
        flag_values=fv)
    flags.DEFINE_enum(
        'encoding',
        None, ['UTF-8', 'ISO-8859-1'],
        'The character encoding used by the input file.  Options include:'
        '\n ISO-8859-1 (also known as Latin-1)'
        '\n UTF-8',
        short_name='E',
        flag_values=fv)
    flags.DEFINE_integer(
        'skip_leading_rows',
        None,
        'The number of rows at the beginning of the source file to skip.',
        flag_values=fv)
    flags.DEFINE_string(
        'schema',
        None,
        'Either a filename or a comma-separated list of fields in the form '
        'name[:type].',
        flag_values=fv)
    flags.DEFINE_boolean(
        'replace',
        False,
        'If true existing data is erased when new data is loaded.',
        flag_values=fv)
    flags.DEFINE_string(
        'quote',
        None,
        'Quote character to use to enclose records. Default is ". '
        'To indicate no quote character at all, use an empty string.',
        flag_values=fv)
    flags.DEFINE_integer(
        'max_bad_records',
        0,
        'Maximum number of bad records allowed before the entire job fails.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'allow_quoted_newlines',
        None,
        'Whether to allow quoted newlines in CSV import data.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'allow_jagged_rows',
        None,
        'Whether to allow missing trailing optional columns '
        'in CSV import data.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'ignore_unknown_values',
        None,
        'Whether to allow and ignore extra, unrecognized values in CSV or JSON '
        'import data.',
        flag_values=fv)
    flags.DEFINE_enum(
        'source_format',
        None, [
            'CSV', 'NEWLINE_DELIMITED_JSON', 'DATASTORE_BACKUP', 'AVRO',
            'PARQUET', 'ORC'
        ],
        'Format of source data. Options include:'
        '\n CSV'
        '\n NEWLINE_DELIMITED_JSON'
        '\n DATASTORE_BACKUP'
        '\n AVRO'
        '\n PARQUET'
        '\n ORC',
        flag_values=fv)
    flags.DEFINE_list(
        'projection_fields', [],
        'If sourceFormat is set to "DATASTORE_BACKUP", indicates which entity '
        'properties to load into BigQuery from a Cloud Datastore backup. '
        'Property names are case sensitive and must refer to top-level '
        'properties.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'autodetect',
        None,
        'Enable auto detection of schema and options for formats that are not '
        'self describing like CSV and JSON.',
        flag_values=fv)
    flags.DEFINE_multistring(
        'schema_update_option',
        None,
        'Can be specified when append to a table, or replace a table partition.'
        ' When specified, the schema of the destination table will be updated '
        'with the schema of the new data. One or more of the following options '
        'can be specified:'
        '\n ALLOW_FIELD_ADDITION: allow new fields to be added'
        '\n ALLOW_FIELD_RELAXATION: allow relaxing required fields to nullable',
        flag_values=fv)
    flags.DEFINE_string(
        'null_marker',
        None,
        'An optional custom string that will represent a NULL value'
        'in CSV import data.',
        flag_values=fv)
    flags.DEFINE_string(
        'time_partitioning_type',
        None,
        'Enables time based partitioning on the table and set the type. The '
        'only type accepted is DAY, which will generate one partition per day.',
        flag_values=fv)
    flags.DEFINE_integer(
        'time_partitioning_expiration',
        None,
        'Enables time based partitioning on the table and sets the number of '
        'seconds for which to keep the storage for the partitions in the table.'
        ' The storage in a partition will have an expiration time of its '
        'partition time plus this value. A negative number means no '
        'expiration.',
        flag_values=fv)
    flags.DEFINE_string(
        'range_partitioning',
        None, 'Enables range partitioning on the table. The format should be '
        '"field,start,end,interval". The table will be partitioned based on the'
        ' value of the field. Field must be a top-level, non-repeated INT64 '
        'field. Start, end, and interval are INT64 values defining the ranges.',
        flag_values=fv)
    flags.DEFINE_string(
        'time_partitioning_field',
        None,
        'Enables time based partitioning on the table and the table will be '
        'partitioned based on the value of this field. If time based '
        'partitioning is enabled without this value, the table will be '
        'partitioned based on the loading time.',
        flag_values=fv)
    flags.DEFINE_string(
        'destination_kms_key',
        None,
        'Cloud KMS key for encryption of the destination table data.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'require_partition_filter',
        None,
        'Whether to require partition filter for queries over this table. '
        'Only apply to partitioned table.',
        flag_values=fv)
    flags.DEFINE_string(
        'clustering_fields',
        None,
        'Comma separated field names. Can only be specified with time based '
        'partitioning. Data will be first partitioned and subsequently "'
        'clustered on these fields.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'use_avro_logical_types',
        None,
        'If sourceFormat is set to "AVRO", indicates whether to enable '
        'interpreting logical types into their corresponding types '
        '(ie. TIMESTAMP), instead of only using their raw types (ie. INTEGER).',
        flag_values=fv)
    flags.DEFINE_string(
        'hive_partitioning_mode',
        None,
        '(experimental) Enables hive partitioning.  AUTO indicates to perform '
        'automatic type inference.  STRINGS indicates to treat all hive '
        'partition keys as STRING typed.  No other values are accepted',
        flag_values=fv)
    self._ProcessCommandRc(fv)

  def RunWithArgs(self, destination_table, source, schema=None):
    """Perform a load operation of source into destination_table.

    Usage:
      load <destination_table> <source> [<schema>]

    The <destination_table> is the fully-qualified table name of table to
    create, or append to if the table already exists.

    The <source> argument can be a path to a single local file, or a
    comma-separated list of URIs.

    The <schema> argument should be either the name of a JSON file or a text
    schema. This schema should be omitted if the table already has one.

    In the case that the schema is provided in text form, it should be a
    comma-separated list of entries of the form name[:type], where type will
    default to string if not specified.

    In the case that <schema> is a filename, it should contain a
    single array object, each entry of which should be an object with
    properties 'name', 'type', and (optionally) 'mode'. See the online
    documentation for more detail:
      https://developers.google.com/bigquery/preparing-data-for-bigquery

    Note: the case of a single-entry schema with no type specified is
    ambiguous; one can use name:string to force interpretation as a
    text schema.

    Examples:
      bq load ds.new_tbl ./info.csv ./info_schema.json
      bq load ds.new_tbl gs://mybucket/info.csv ./info_schema.json
      bq load ds.small gs://mybucket/small.csv name:integer,value:string
      bq load ds.small gs://mybucket/small.csv field1,field2,field3

    Arguments:
      destination_table: Destination table name.
      source: Name of local file to import, or a comma-separated list of
        URI paths to data to import.
      schema: Either a text schema or JSON file, as above.
    """
    client = Client.Get()
    table_reference = client.GetTableReference(destination_table)
    opts = {
        'encoding': self.encoding,
        'skip_leading_rows': self.skip_leading_rows,
        'max_bad_records': self.max_bad_records,
        'allow_quoted_newlines': self.allow_quoted_newlines,
        'job_id': _GetJobIdFromFlags(),
        'source_format': self.source_format,
        'projection_fields': self.projection_fields,
    }
    if FLAGS.location:
      opts['location'] = FLAGS.location
    if self.replace:
      opts['write_disposition'] = 'WRITE_TRUNCATE'
    if self.field_delimiter is not None:
      opts['field_delimiter'] = _NormalizeFieldDelimiter(self.field_delimiter)
    if self.quote is not None:
      opts['quote'] = _NormalizeFieldDelimiter(self.quote)
    if self.allow_jagged_rows is not None:
      opts['allow_jagged_rows'] = self.allow_jagged_rows
    if self.ignore_unknown_values is not None:
      opts['ignore_unknown_values'] = self.ignore_unknown_values
    if self.autodetect is not None:
      opts['autodetect'] = self.autodetect
    if self.schema_update_option:
      opts['schema_update_options'] = self.schema_update_option
    if self.null_marker:
      opts['null_marker'] = self.null_marker
    time_partitioning = _ParseTimePartitioning(
        self.time_partitioning_type,
        self.time_partitioning_expiration,
        self.time_partitioning_field,
        None,
        self.require_partition_filter)
    if time_partitioning is not None:
      opts['time_partitioning'] = time_partitioning
    range_partitioning = _ParseRangePartitioning(self.range_partitioning)
    if range_partitioning:
      opts['range_partitioning'] = range_partitioning
    clustering = _ParseClustering(self.clustering_fields)
    if clustering:
      opts['clustering'] = clustering
    if self.destination_kms_key is not None:
      opts['destination_encryption_configuration'] = {
          'kmsKeyName': self.destination_kms_key
      }
    if self.use_avro_logical_types is not None:
      opts['use_avro_logical_types'] = self.use_avro_logical_types
    if self.hive_partitioning_mode is not None:
      _ValidateHivePartitioningOption(self.hive_partitioning_mode)
      opts['hive_partitioning_mode'] = self.hive_partitioning_mode
    job = client.Load(table_reference, source, schema=schema, **opts)
    if FLAGS.sync:
      _PrintJobMessages(client.FormatJobInfo(job))
    else:
      self.PrintJobStartInfo(job)


def _CreateExternalTableDefinition(source_format,
                                   source_uris,
                                   schema,
                                   autodetect,
                                   ignore_unknown_values=False,
                                   hive_partitioning_mode=None):
  """Create an external table definition with the given URIs and the schema.

  Arguments:
    source_format: Format of source data.
      For CSV files, specify 'CSV'.
      For Google spreadsheet files, specify 'GOOGLE_SHEETS'.
      For newline-delimited JSON, specify 'NEWLINE_DELIMITED_JSON'.
      For Cloud Datastore backup, specify 'DATASTORE_BACKUP'.
      For Avro files, specify 'AVRO'.
    source_uris: Comma separated list of URIs that contain data for this table.
    schema: Either an inline schema or path to a schema file.
    autodetect: Indicates if format options, compression mode and schema be auto
      detected from the source data. True - means that autodetect is on,
      False means that it is off. None means format specific default:
        - For CSV it means autodetect is OFF
        - For JSON it means that autodetect is ON.
      For JSON, defaulting to autodetection is safer because the only option
      autodetected is compression. If a schema is passed,
      then the user-supplied schema is used.
    ignore_unknown_values:  Indicates if BigQuery should allow extra values that
       are not represented in the table schema. If true, the extra values are
       ignored. If false, records with extra columns are treated as bad records,
       and if there are too many bad records, an invalid error is returned in
       the job result. The default value is false.
       The sourceFormat property determines what BigQuery treats as an
       extra value:
         - CSV: Trailing columns
         - JSON: Named values that don't match any column names.
    hive_partitioning_mode: Enables hive partitioning.  AUTO indicates to
       perform automatic type inference.  STRINGS indicates to treat all hive
       partition keys as STRING typed.  No other values are accepted.
  Returns:
    A python dictionary that contains a external table definition for the given
    format with the most common options set.
  """
  try:
    supported_formats = [
        'CSV',
        'NEWLINE_DELIMITED_JSON',
        'DATASTORE_BACKUP',
        'AVRO',
        'ORC',
        'PARQUET',
        'GOOGLE_SHEETS'
    ]

    if source_format not in supported_formats:
      raise app.UsageError(('%s is not a supported format.') % source_format)
    external_table_def = {'sourceFormat': source_format}

    if external_table_def['sourceFormat'] == 'CSV':
      if autodetect:
        external_table_def['autodetect'] = True
        external_table_def['csvOptions'] = yaml.load("""
            {
                "quote": '"',
                "encoding": "UTF-8"
            }
        """)
      else:
        external_table_def['csvOptions'] = yaml.load("""
            {
                "allowJaggedRows": false,
                "fieldDelimiter": ",",
                "allowQuotedNewlines": false,
                "quote": '"',
                "skipLeadingRows": 0,
                "encoding": "UTF-8"
            }
        """)
    elif external_table_def['sourceFormat'] == 'NEWLINE_DELIMITED_JSON':
      if autodetect is None or autodetect:
        external_table_def['autodetect'] = True
    elif external_table_def['sourceFormat'] == 'GOOGLE_SHEETS':
      if autodetect is None or autodetect:
        external_table_def['autodetect'] = True
      else:
        external_table_def['googleSheetsOptions'] = yaml.load("""
            {
                "skipLeadingRows": 0
            }
        """)
    if ignore_unknown_values:
      external_table_def['ignoreUnknownValues'] = True
    if hive_partitioning_mode is not None:
      _ValidateHivePartitioningOption(hive_partitioning_mode)
      external_table_def['hivePartitioningMode'] = hive_partitioning_mode
    if schema:
      fields = BigqueryClient.ReadSchema(schema)
      external_table_def['schema'] = {'fields': fields}

    external_table_def['sourceUris'] = source_uris.split(',')
    return external_table_def
  except ValueError, e:
    raise app.UsageError(
        ('Error occured while creating table definition: %s') % e)


class _MakeExternalTableDefinition(BigqueryCmd):
  usage = """mkdef <source_uri> [<schema>]"""

  def __init__(self, name, fv):
    super(_MakeExternalTableDefinition, self).__init__(name, fv)
    flags.DEFINE_boolean(
        'autodetect',
        None,
        'Should schema and format options be autodetected.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'ignore_unknown_values',
        None,
        'Ignore any values in a row that are not present in the schema.',
        short_name='i',
        flag_values=fv)
    flags.DEFINE_string(
        'hive_partitioning_mode',
        None,
        '(experimental) Enables hive partitioning.  AUTO indicates to perform '
        'automatic type inference.  STRINGS indicates to treat all hive '
        'partition keys as STRING typed.  No other values are accepted',
        flag_values=fv)
    flags.DEFINE_enum(
        'source_format',
        'CSV',
        [
            'CSV',
            'GOOGLE_SHEETS',
            'NEWLINE_DELIMITED_JSON',
            'DATASTORE_BACKUP',
            'ORC',
            'PARQUET',
            'AVRO'
        ],
        'Format of source data. Options include:'
        '\n CSV'
        '\n GOOGLE_SHEETS'
        '\n NEWLINE_DELIMITED_JSON'
        '\n DATASTORE_BACKUP'
        '\n ORC (experimental)'
        '\n PARQUET (experimental)'
        '\n AVRO',
        flag_values=fv)
    self._ProcessCommandRc(fv)

  def RunWithArgs(self, source_uris, schema=None):
    """Emits a definition in JSON for a GCS backed table.

    The output of this command can be redirected to a file and used for the
    external_table_definition flag with the "bq query" and "bq mk" commands.
    It produces a definition with the most commonly used values for options.
    You can modify the output to override option values.

    Usage:
      mkdef <source_uris> [<schema>]

    Examples:
      bq mkdef 'gs://bucket/file.csv' field1:integer,field2:string

    Arguments:
      source_uris: a comma-separated list of uris.
      schema: The <schema> argument should be either the name of a JSON file or
        a text schema.

        In the case that the schema is provided in text form, it should be a
        comma-separated list of entries of the form name[:type], where type will
        default to string if not specified.

        In the case that <schema> is a filename, it should contain a
        single array object, each entry of which should be an object with
        properties 'name', 'type', and (optionally) 'mode'. See the online
        documentation for more detail:
          https://developers.google.com/bigquery/preparing-data-for-bigquery

        Note: the case of a single-entry schema with no type specified is
        ambiguous; one can use name:string to force interpretation as a
        text schema.
    """
    json.dump(
        _CreateExternalTableDefinition(
            self.source_format,
            source_uris,
            schema,
            self.autodetect,
            self.ignore_unknown_values,
            self.hive_partitioning_mode),
        sys.stdout,
        sort_keys=True,
        indent=2)


class _Query(BigqueryCmd):
  usage = """query <sql>"""

  def __init__(self, name, fv):
    super(_Query, self).__init__(name, fv)
    flags.DEFINE_string(
        'destination_table',
        '',
        'Name of destination table for query results.',
        flag_values=fv)
    flags.DEFINE_string(
        'destination_schema',
        '',
        'Schema for the destination table. Either a filename or '
        'a comma-separated list of fields in the form name[:type].',
        flag_values=fv)
    flags.DEFINE_integer(
        'start_row',
        0,
        'First row to return in the result.',
        short_name='s',
        flag_values=fv)
    flags.DEFINE_integer(
        'max_rows',
        100,
        'How many rows to return in the result.',
        short_name='n',
        flag_values=fv)
    flags.DEFINE_boolean(
        'batch',
        False,
        'Whether to run the query in batch mode.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'append_table',
        False,
        'When a destination table is specified, whether or not to append.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'rpc',
        False,
        'If true, use rpc-style query API instead of jobs.insert().',
        flag_values=fv)
    flags.DEFINE_boolean(
        'replace',
        False,
        'If true, erase existing contents before loading new data.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'allow_large_results',
        None,
        'Enables larger destination table sizes for legacy SQL queries.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'dry_run',
        None,
        'Whether the query should be validated without executing.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'require_cache',
        None,
        'Whether to only run the query if it is already cached.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'use_cache',
        None,
        'Whether to use the query cache to avoid rerunning cached queries.',
        flag_values=fv)
    flags.DEFINE_float(
        'min_completion_ratio',
        None,
        '[Experimental] The minimum fraction of data that must be scanned '
        'before a query returns. If not set, the default server value (1.0) '
        'will be used.',
        lower_bound=0,
        upper_bound=1.0,
        flag_values=fv)
    flags.DEFINE_boolean(
        'flatten_results',
        None,
        'Whether to flatten nested and repeated fields in the result schema '
        'for legacy SQL queries. '
        'If not set, the default behavior is to flatten.',
        flag_values=fv)
    flags.DEFINE_multistring(
        'external_table_definition',
        None,
        'Specifies a table name and either an inline table definition '
        'or a path to a file containing a JSON table definition to use in the '
        'query. The format is "table_name::path_to_file_with_json_def" or '
        '"table_name::schema@format=uri".'
        'For example, '
        '"--external_table_definition=Example::/tmp/example_table_def.txt" '
        'will define a table named "Example" using the URIs and schema '
        'encoded in example_table_def.txt.',
        flag_values=fv)
    flags.DEFINE_multistring(
        'udf_resource',
        None,
        'The URI or local filesystem path of a code file to load and '
        'evaluate immediately as a User-Defined Function resource.',
        flag_values=fv)
    flags.DEFINE_integer(
        'maximum_billing_tier',
        None,
        'The upper limit of billing tier for the query.',
        flag_values=fv)
    flags.DEFINE_integer(
        'maximum_bytes_billed',
        None,
        'The upper limit of bytes billed for the query.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'use_legacy_sql',
        None,
        ('Whether to use Legacy SQL for the query. If not set, the default '
         'value is true.'),
        flag_values=fv)
    flags.DEFINE_multistring(
        'schema_update_option',
        None,
        'Can be specified when append to a table, or replace a table partition.'
        ' When specified, the schema of the destination table will be updated '
        'with the schema of the new data. One or more of the following options '
        'can be specified:'
        '\n ALLOW_FIELD_ADDITION: allow new fields to be added'
        '\n ALLOW_FIELD_RELAXATION: allow relaxing required fields to nullable',
        flag_values=fv)
    flags.DEFINE_multistring(
        'label',
        None,
        'A label to set on a query job. The format is "key:value"',
        flag_values=fv)
    flags.DEFINE_multistring(
        'parameter',
        None,
        ('Either a file containing a JSON list of query parameters, or a query '
         'parameter in the form "name:type:value". An empty name produces '
         'a positional parameter. The type may be omitted to assume STRING: '
         'name::value or ::value. The value "NULL" produces a null value.'),
        flag_values=fv)
    flags.DEFINE_string(
        'time_partitioning_type',
        None,
        'Enables time based partitioning on the table and set the type. The '
        'only type accepted is DAY, which will generate one partition per day.',
        flag_values=fv)
    flags.DEFINE_integer(
        'time_partitioning_expiration',
        None,
        'Enables time based partitioning on the table and sets the number of '
        'seconds for which to keep the storage for the partitions in the table.'
        ' The storage in a partition will have an expiration time of its '
        'partition time plus this value. A negative number means no '
        'expiration.',
        flag_values=fv)
    flags.DEFINE_string(
        'time_partitioning_field',
        None,
        'Enables time based partitioning on the table and the table will be '
        'partitioned based on the value of this field. If time based '
        'partitioning is enabled without this value, the table will be '
        'partitioned based on the loading time.',
        flag_values=fv)
    flags.DEFINE_string(
        'range_partitioning',
        None, 'Enables range partitioning on the table. The format should be '
        '"field,start,end,interval". The table will be partitioned based on the'
        ' value of the field. Field must be a top-level, non-repeated INT64 '
        'field. Start, end, and interval are INT64 values defining the ranges.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'require_partition_filter',
        None,
        'Whether to require partition filter for queries over this table. '
        'Only apply to partitioned table.',
        flag_values=fv)
    flags.DEFINE_string(
        'clustering_fields',
        None,
        'Comma separated field names. Can only be specified with time based '
        'partitioning. Data will be first partitioned and subsequently "'
        'clustered on these fields.',
        flag_values=fv)
    flags.DEFINE_string(
        'destination_kms_key',
        None,
        'Cloud KMS key for encryption of the destination table data.',
        flag_values=fv)
    self._ProcessCommandRc(fv)

  def RunWithArgs(self, *args):
    # pylint: disable=g-doc-exception
    """Execute a query.

    Query should be specifed on command line, or passed on stdin.

    Examples:
      bq query 'select count(*) from publicdata:samples.shakespeare'
      echo 'select count(*) from publicdata:samples.shakespeare' | bq query

    Usage:
      query [<sql_query>]
    """
    # Set up the params that are the same for rpc-style and jobs.insert()-style
    # queries.
    kwds = {
        'dry_run': self.dry_run,
        'use_cache': self.use_cache,
        'min_completion_ratio': self.min_completion_ratio,
    }
    if self.external_table_definition:
      external_table_defs = {}
      for raw_table_def in self.external_table_definition:
        table_name_and_def = raw_table_def.split('::', 1)
        if len(table_name_and_def) < 2:
          raise app.UsageError(
              'external_table_definition parameter is invalid, expected :: as '
              'the separator.')
        external_table_defs[table_name_and_def[0]] = _GetExternalDataConfig(
            table_name_and_def[1])
      kwds['external_table_definitions_json'] = dict(external_table_defs)
    if self.udf_resource:
      kwds['udf_resources'] = _ParseUdfResources(self.udf_resource)
    if self.maximum_billing_tier:
      kwds['maximum_billing_tier'] = self.maximum_billing_tier
    if self.maximum_bytes_billed:
      kwds['maximum_bytes_billed'] = self.maximum_bytes_billed
    if self.schema_update_option:
      kwds['schema_update_options'] = self.schema_update_option
    if self.label is not None:
      kwds['labels'] = _ParseLabels(self.label)
    if self.parameter:
      kwds['query_parameters'] = _ParseParameters(self.parameter)
    query = ' '.join(args)
    if not query:
      query = sys.stdin.read()
    client = Client.Get()
    if FLAGS.location:
      kwds['location'] = FLAGS.location
    kwds['use_legacy_sql'] = self.use_legacy_sql
    time_partitioning = _ParseTimePartitioning(
        self.time_partitioning_type,
        self.time_partitioning_expiration,
        self.time_partitioning_field,
        None,
        self.require_partition_filter)
    if time_partitioning is not None:
      kwds['time_partitioning'] = time_partitioning
    range_partitioning = _ParseRangePartitioning(self.range_partitioning)
    if range_partitioning:
      kwds['range_partitioning'] = range_partitioning
    clustering = _ParseClustering(self.clustering_fields)
    if clustering:
      kwds['clustering'] = clustering
    if self.destination_schema and not self.destination_table:
      raise app.UsageError(
          'destination_schema can only be used with destination_table.')
    if self.destination_kms_key:
      kwds['destination_encryption_configuration'] = {
          'kmsKeyName': self.destination_kms_key
      }

    if self.rpc:
      if self.allow_large_results:
        raise app.UsageError(
            'allow_large_results cannot be specified in rpc mode.')
      if self.destination_table:
        raise app.UsageError(
            'destination_table cannot be specified in rpc mode.')
      if FLAGS.job_id or FLAGS.fingerprint_job_id:
        raise app.UsageError(
            'job_id and fingerprint_job_id cannot be specified in rpc mode.')
      if self.batch:
        raise app.UsageError('batch cannot be specified in rpc mode.')
      if self.flatten_results:
        raise app.UsageError('flatten_results cannot be specified in rpc mode.')
      kwds['max_results'] = self.max_rows
      fields, rows, execution = client.RunQueryRpc(query, **kwds)
      if self.dry_run:
        _PrintDryRunInfo(execution)
      else:
        Factory.ClientTablePrinter.GetTablePrinter().PrintTable(fields, rows)
        # If we are here, the job succeeded, but print warnings if any.
        _PrintJobMessages(execution)
    else:
      if self.destination_table and self.append_table:
        kwds['write_disposition'] = 'WRITE_APPEND'
      if self.destination_table and self.replace:
        kwds['write_disposition'] = 'WRITE_TRUNCATE'
      if self.require_cache:
        kwds['create_disposition'] = 'CREATE_NEVER'
      if self.batch:
        kwds['priority'] = 'BATCH'

      kwds['destination_table'] = self.destination_table
      kwds['allow_large_results'] = self.allow_large_results
      kwds['flatten_results'] = self.flatten_results
      kwds['job_id'] = _GetJobIdFromFlags()
      job = client.Query(query, **kwds)
      if self.dry_run:
        _PrintDryRunInfo(job)
      elif not FLAGS.sync:
        self.PrintJobStartInfo(job)
      else:
        self._PrintQueryJobResults(client, job)
        # If we are here, the job succeeded, but print warnings if any.
        _PrintJobMessages(client.FormatJobInfo(job))
    if self.destination_schema:
      client.UpdateTable(
          client.GetTableReference(self.destination_table),
          BigqueryClient.ReadSchema(self.destination_schema))

  def _PrintQueryJobResults(self, client, job):
    fields, rows = client.ReadSchemaAndJobRows(
        job['jobReference'], start_row=self.start_row, max_rows=self.max_rows)
    Factory.ClientTablePrinter.GetTablePrinter().PrintTable(fields, rows)


def _GetExternalDataConfig(file_path_or_simple_spec):
  """Returns a ExternalDataConfiguration from the file or specification string.

  Determines if the input string is a file path or a string,
  then returns either the parsed file contents, or the parsed configuration from
  string. The file content is expected to be JSON representation of
  ExternalDataConfiguration. The specification is expected to be of the form
  schema@format=uri i.e. schema is separated from format and uri by '@'. If the
  uri itself contains '@' or '=' then the JSON file option should be used.
  "format=" can be omitted for CSV files.

  Raises:
    UsageError: when incorrect usage or invalid args are used.
  """

  if os.path.isfile(file_path_or_simple_spec):
    try:
      with open(file_path_or_simple_spec) as external_config_file:
        return yaml.load(external_config_file)
    except ValueError, e:
      raise app.UsageError(
          ('Error decoding JSON external table definition from '
           'file %s: %s') % (file_path_or_simple_spec, e))
  else:
    source_format = 'CSV'
    schema = None
    error_msg = ('Error decoding external_table_definition. '
                 'external_table_definition should either be the name of a '
                 'JSON file or the text representation of an external table '
                 'definition. Given:%s') % (
                     file_path_or_simple_spec)

    parts = file_path_or_simple_spec.split('@')
    if len(parts) == 1:
      # Schema is not specified.
      format_and_uri = parts[0]
    elif len(parts) == 2:
      # Schema is specified.
      schema = parts[0]
      format_and_uri = parts[1]
    else:
      raise app.UsageError(error_msg)

    separator_pos = format_and_uri.find('=')
    if separator_pos < 0:
      # Format is not specified
      uri = format_and_uri
    else:
      source_format = format_and_uri[0:separator_pos]
      uri = format_and_uri[separator_pos + 1:]

    if not uri:
      raise app.UsageError(error_msg)
    # When using short notation for external table definition
    # autodetect is always performed.
    return _CreateExternalTableDefinition(
        source_format, uri, schema, autodetect=True)


class _Extract(BigqueryCmd):
  usage = """extract <source_table> <destination_uris>"""

  def __init__(self, name, fv):
    super(_Extract, self).__init__(name, fv)
    flags.DEFINE_string(
        'field_delimiter',
        None,
        'The character that indicates the boundary between columns in the '
        'output file. "\\t" and "tab" are accepted names for tab.',
        short_name='F',
        flag_values=fv)
    flags.DEFINE_enum(
        'destination_format',
        None, ['CSV', 'NEWLINE_DELIMITED_JSON', 'AVRO'],
        'The format with which to write the extracted data. Tables with '
        'nested or repeated fields cannot be extracted to CSV.',
        flag_values=fv)
    flags.DEFINE_enum(
        'compression',
        'NONE', ['GZIP', 'DEFLATE', 'SNAPPY', 'NONE'],
        'The compression type to use for exported files. Possible values '
        'include GZIP, DEFLATE, SNAPPY and NONE. The default value is NONE.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'print_header',
        None,
        'Whether to print header rows for formats that '
        'have headers. Prints headers by default.',
        flag_values=fv)
    self._ProcessCommandRc(fv)

  def RunWithArgs(self, source_table, destination_uris):
    """Perform an extract operation of source_table into destination_uris.

    Usage:
      extract <source_table> <destination_uris>

    Examples:
      bq extract ds.summary gs://mybucket/summary.csv

    Arguments:
      source_table: Source table to extract.
      destination_uris: One or more Google Cloud Storage URIs, separated by
        commas.
    """
    client = Client.Get()
    kwds = {
        'job_id': _GetJobIdFromFlags(),
    }
    if FLAGS.location:
      kwds['location'] = FLAGS.location

    table_reference = client.GetTableReference(source_table)
    job = client.Extract(
        table_reference,
        destination_uris,
        print_header=self.print_header,
        field_delimiter=_NormalizeFieldDelimiter(self.field_delimiter),
        destination_format=self.destination_format,
        compression=self.compression,
        **kwds)
    if FLAGS.sync:
      # If we are here, the job succeeded, but print warnings if any.
      _PrintJobMessages(client.FormatJobInfo(job))
    else:
      self.PrintJobStartInfo(job)


class _Partition(BigqueryCmd):  # pylint: disable=missing-docstring
  usage = """partition source_prefix destination_table"""

  def __init__(self, name, fv):
    super(_Partition, self).__init__(name, fv)
    flags.DEFINE_boolean(
        'no_clobber',
        False,
        'Do not overwrite an existing partition.',
        short_name='n',
        flag_values=fv)
    flags.DEFINE_string(
        'time_partitioning_type',
        'DAY',
        'Enables time based partitioning on the table and set the type. The '
        'only type accepted is DAY, which will generate one partition per day.',
        flag_values=fv)
    flags.DEFINE_integer(
        'time_partitioning_expiration',
        None,
        'Enables time based partitioning on the table and sets the number of '
        'seconds for which to keep the storage for the partitions in the table.'
        ' The storage in a partition will have an expiration time of its '
        'partition time plus this value. A negative number means no '
        'expiration.',
        flag_values=fv)
    self._ProcessCommandRc(fv)

  def RunWithArgs(self, source_prefix, destination_table):
    """Copies source tables into partitioned tables.

    Usage:
    bq partition <source_table_prefix> <destination_partitioned_table>

    Copies tables of the format <source_table_prefix><YYYYmmdd> to a destination
    partitioned table, with the date suffix of the source tables
    becoming the partition date of the destination table partitions.

    If the destination table does not exist, one will be created with
    a schema and that matches the last table that matches the supplied
    prefix.

    Examples:
      bq partition dataset1.sharded_ dataset2.partitioned_table
    """

    client = Client.Get()
    formatter = _GetFormatterFromFlags()

    source_table_prefix = client.GetReference(source_prefix)
    _Typecheck(source_table_prefix, TableReference,
               'Cannot determine table associated with "%s"' % (source_prefix,))
    destination_table = client.GetReference(destination_table)
    _Typecheck(
        destination_table, TableReference,
        'Cannot determine table associated with "%s"' % (destination_table,))

    source_dataset = source_table_prefix.GetDatasetReference()
    source_id_prefix = source_table_prefix.tableId
    source_id_len = len(source_id_prefix)

    job_id_prefix = _GetJobIdFromFlags()
    if isinstance(job_id_prefix, JobIdGenerator):
      job_id_prefix = job_id_prefix.Generate(
          [source_table_prefix, destination_table])

    destination_dataset = destination_table.GetDatasetReference()

    BigqueryClient.ConfigureFormatter(formatter, TableReference)
    results = map(client.FormatTableInfo,
                  client.ListTables(source_dataset, max_results=1000 * 1000))

    dates = []
    representative_table = None
    for result in results:
      if result['tableId'].startswith(source_id_prefix):
        suffix = result['tableId'][source_id_len:]
        try:
          table_date = datetime.datetime.strptime(suffix, '%Y%m%d')

          dates.append(table_date.strftime('%Y%m%d'))
          representative_table = result
        except ValueError:
          pass

    if not representative_table:
      print 'No matching source tables found'
      return

    print 'Copying %d source partitions to %s' % (len(dates), destination_table)

    # Check to see if we need to create the destination table.
    if not client.TableExists(destination_table):
      source_table_id = representative_table['tableId']
      source_table_ref = source_dataset.GetTableReference(source_table_id)
      source_table_schema = client.GetTableSchema(source_table_ref)
      # Get fields in the schema.
      if source_table_schema:
        source_table_schema = source_table_schema['fields']

      time_partitioning = _ParseTimePartitioning(
          self.time_partitioning_type, self.time_partitioning_expiration)

      print 'Creating table: %s with schema from %s and partition spec %s' % (
          destination_table, source_table_ref, time_partitioning)

      client.CreateTable(
          destination_table,
          schema=source_table_schema,
          time_partitioning=time_partitioning)
      print '%s successfully created.' % (destination_table,)

    for date_str in dates:
      destination_table_id = '%s$%s' % (destination_table.tableId, date_str)
      source_table_id = '%s%s' % (source_id_prefix, date_str)
      current_job_id = '%s%s' % (job_id_prefix, date_str)

      source_table = source_dataset.GetTableReference(source_table_id)
      destination_partition = destination_dataset.GetTableReference(
          destination_table_id)

      avoid_copy = False
      if self.no_clobber:
        maybe_destination_partition = client.TableExists(destination_partition)
        avoid_copy = (
            maybe_destination_partition and
            int(maybe_destination_partition['numBytes']) > 0)

      if avoid_copy:
        print "Table '%s' already exists, skipping" % (destination_partition,)
      else:
        print 'Copying %s to %s' % (source_table, destination_partition)
        kwds = {
            'write_disposition': 'WRITE_TRUNCATE',
            'job_id': current_job_id,
        }
        if FLAGS.location:
          kwds['location'] = FLAGS.location
        job = client.CopyTable([source_table], destination_partition, **kwds)
        if not FLAGS.sync:
          self.PrintJobStartInfo(job)
        else:
          print 'Successfully copied %s to %s' % (source_table,
                                                  destination_partition)


class _List(BigqueryCmd):  # pylint: disable=missing-docstring
  usage = """ls [(-j|-p|-d)] [-a] [-n <number>] [<identifier>]"""

  def __init__(self, name, fv):
    super(_List, self).__init__(name, fv)
    flags.DEFINE_boolean(
        'all',
        None,
        'Show all results. For jobs, will show jobs from all users. For '
        'datasets, will list hidden datasets.'
        'For transfer configs and runs, '
        'this flag is redundant and not necessary.'
        '',
        short_name='a',
        flag_values=fv)
    flags.DEFINE_boolean(
        'all_jobs', None, 'DEPRECATED. Use --all instead', flag_values=fv)
    flags.DEFINE_boolean(
        'jobs',
        False,
        'Show jobs described by this identifier.',
        short_name='j',
        flag_values=fv)
    flags.DEFINE_integer(
        'max_results',
        None,
        'Maximum number to list.',
        short_name='n',
        flag_values=fv)
    flags.DEFINE_integer(
        'min_creation_time',
        None,
        'Timestamp in milliseconds. Return jobs created after this timestamp.',
        flag_values=fv)
    flags.DEFINE_integer(
        'max_creation_time',
        None,
        'Timestamp in milliseconds. Return jobs created before this timestamp.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'projects', False, 'Show all projects.', short_name='p', flag_values=fv)
    flags.DEFINE_boolean(
        'datasets',
        False,
        'Show datasets described by this identifier.',
        short_name='d',
        flag_values=fv)
    flags.DEFINE_string(
        'transfer_location',
        None,
        'Location for list transfer config (e.g., "eu" or "us").',
        flag_values=fv)
    flags.DEFINE_boolean(
        'transfer_config',
        False,
        'Show transfer configurations described by this identifier. '
        'This requires setting --transfer_location.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'transfer_run', False, 'List the transfer runs.', flag_values=fv)
    flags.DEFINE_string(
        'run_attempt',
        'LATEST',
        'For transfer run, respresents which runs should be '
        'pulled. See https://cloud.google.com/bigquery/docs/reference/'
        'datatransfer/rest/v1/projects.transferConfigs.runs/list#RunAttempt '
        'for details',
        flag_values=fv)
    flags.DEFINE_bool(
        'transfer_log',
        False,
        'List messages under the run specified',
        flag_values=fv)
    flags.DEFINE_string(
        'message_type',
        None,
        'usage:- messageTypes:INFO '
        'For transferlog, represents which messages should '
        'be listed. See '
        'https://cloud.google.com/bigquery/docs/reference'
        '/datatransfer/rest/v1/projects.transferConfigs'
        '.runs.transferLogs#MessageSeverity '
        'for details.',
        flag_values=fv)
    flags.DEFINE_string(
        'page_token',
        None,
        'Start listing from this page token.',
        short_name='k',
        flag_values=fv)
    flags.DEFINE_boolean(
        'print_last_token',
        False,
        'If true and format in [json, prettyjson], also print the next page '
        'token for the jobs list.',
        flag_values=fv)
    flags.DEFINE_string(
        'filter',
        None,
        'Show datasets that match the filter expression. '
        'Use a space-separated list of label keys and values '
        'in the form "labels.key:value". Datasets must match '
        'all provided filter expressions. See '
        'https://cloud.google.com/bigquery/docs/labeling-datasets'
        '#filtering_datasets_using_labels '
        'for details'
        '\nFor transfer configurations, the filter expression, '
        'in the form "dataSourceIds:value(s)", will show '
        'transfer configurations with '
        ' the specified dataSourceId. '
        '\nFor transfer runs, the filter expression, '
        'in the form "states:VALUE(s)", will show '
        'transfer runs with the specified states. See '
        'https://cloud.google.com/bigquery/docs/reference/datatransfer/rest/v1/'
        'TransferState '
        'for details',
        flag_values=fv)
    flags.DEFINE_boolean(
        'reservation',
        None,
        'List all reservations for the given project and location.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'slot_pool',
        None,
        'List all slot pools for the given reservation.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'reservation_grant', None,
        'List all reservation grants for given project/location or '
        'reservation.',
        flag_values=fv)
    self._ProcessCommandRc(fv)

  def RunWithArgs(self, identifier=''):
    """List the objects contained in the named collection.

    List the objects in the named project or dataset. A trailing : or
    . can be used to signify a project or dataset.
     * With -j, show the jobs in the named project.
     * With -p, show all projects.

    Examples:
      bq ls
      bq ls -j proj
      bq ls -p -n 1000
      bq ls mydataset
      bq ls -a
      bq ls --filter labels.color:red
      bq ls --filter 'labels.color:red labels.size:*'
      bq ls --transfer_config --transfer_location='us'
          --filter='dataSourceIds:play,adwords'
      bq ls --transfer_run --filter='states:SUCCESSED,PENDING'
          --run_attempt='LATEST' projects/p/locations/l/transferConfigs/c
      bq ls --transfer_log --message_type='messageTypes:INFO,ERROR'
          projects/p/locations/l/transferConfigs/c/runs/r
      bq ls --reservation_grant --project_id=proj --location='us'
      bq ls --reservation_grant --project_id=proj --location='us' --reservation
          <reservation_ref>
    """

    # pylint: disable=g-doc-exception
    if ValidateAtMostOneSelected(self.j, self.p, self.d):
      raise app.UsageError('Cannot specify more than one of -j, -p, or -d.')
    if self.j and self.p:
      raise app.UsageError('Cannot specify more than one of -j and -p.')
    if self.p and identifier:
      raise app.UsageError('Cannot specify an identifier with -p')

    # Copy deprecated flag specifying 'all' to current one.
    if self.all_jobs is not None:
      self.a = self.all_jobs

    client = Client.Get()
    if identifier:
      reference = client.GetReference(identifier)
    else:
      try:
        reference = client.GetReference(identifier)
      except bigquery_client.BigqueryError:
        # We want to let through the case of no identifier, which
        # will fall through to the second case below.
        reference = None
    # If we got a TableReference, we might be able to make sense
    # of it as a DatasetReference, as in 'ls foo' with dataset_id
    # set.
    if isinstance(reference, TableReference):
      try:
        reference = client.GetDatasetReference(identifier)
      except bigquery_client.BigqueryError:
        pass
    _Typecheck(reference, (types.NoneType, ProjectReference, DatasetReference),
               ('Invalid identifier "%s" for ls, cannot call list on object '
                'of type %s') % (identifier, type(reference).__name__))

    if self.d and isinstance(reference, DatasetReference):
      reference = reference.GetProjectReference()

    page_token = self.k
    results = None
    object_type = None
    if self.j:
      object_type = JobReference
      reference = client.GetProjectReference(identifier)
      _Typecheck(reference, ProjectReference,
                 'Cannot determine job(s) associated with "%s"' % (identifier,))
      if self.print_last_token:
        results = client.ListJobsAndToken(
            reference=reference,
            max_results=self.max_results,
            all_users=self.a,
            min_creation_time=self.min_creation_time,
            max_creation_time=self.max_creation_time,
            page_token=page_token)
        assert object_type is not None
        _PrintObjectsArrayWithToken(results, object_type)
        return
      results = client.ListJobs(
          reference=reference,
          max_results=self.max_results,
          all_users=self.a,
          min_creation_time=self.min_creation_time,
          max_creation_time=self.max_creation_time,
          page_token=page_token
      )
    elif self.reservation_grant:
      try:
        if self.reservation:
          object_type = ReservationGrantReference
          reference = client.GetReservationReference(
              identifier, FLAGS.location, ' ')
          response = client.ListReservationGrantsForReservation(
              reference, self.max_results, self.page_token)
        else:
          object_type = ReservationGrantReference
          reference = client.GetReservationGrantReference(
              identifier=identifier, default_location=FLAGS.location,
              default_reservation_grant_id=' ')
          response = client.ListReservationGrants(
              reference, self.max_results, self.page_token)
        if 'reservationGrants' in response:
          results = response['reservationGrants']
        if 'nextPageToken' in response:
          _PrintPageToken(response)
      except BaseException as e:
        raise bigquery_client.BigqueryError(
            "Failed to list reservation grants '%s': %s" % (identifier, e))
    elif self.reservation:
      if self.slot_pool:
        object_type = SlotPoolReference
        reference = client.GetReservationReference(identifier, FLAGS.location)
        try:
          response = client.ListSlotPools(
              reference, self.max_results, self.page_token)
        except BaseException as e:
          raise bigquery_client.BigqueryError(
              "Failed to list slots pools in '%s': %s" % (identifier, e))
        if 'slotPools' in response:
          results = response['slotPools']
        else:
          print 'No slot pools found.'
          results = None
      else:
        bi_response = None
        response = []
        object_type = ReservationReference
        reference = client.GetReservationReference(
            identifier, FLAGS.location, ' ')
        try:
          bi_response = client.ListBiReservations(reference)
          if 'size' in bi_response:
            print 'BI Engine reservation: %s' % (bi_response['size'])
        except BaseException as e:
          if 'was not found' not in e.message and (
              'is disabled' not in e.message):
            raise bigquery_client.BigqueryError(
                "Failed to list reservations '%s': %s" % (identifier, e))

        try:
          response = client.ListReservations(
              reference, self.max_results, self.page_token)
        except BaseException as e:
          if 'is disabled' not in e.message:
            raise bigquery_client.BigqueryError(
                "Failed to list reservations '%s': %s" % (identifier, e))
        if 'reservations' in response:
          results = response['reservations']
        else:
          if bi_response is None:
            print 'No reservations found.'
      if 'nextPageToken' in response:
        _PrintPageToken(response)
    elif self.transfer_config:
      object_type = TransferConfigReference
      reference = client.GetProjectReference(
          _FormatProjectIdentifier(client, identifier))
      _Typecheck(
          reference, ProjectReference,
          'Cannot determine transfer configuration(s) '
          'associated with "%s"' % (identifier,))

      if self.transfer_location is None:
        raise app.UsageError(('Need to specify transfer_location for '
                              'list transfer configs.'))

      # transfer_configs tuple contains transfer configs at index 0 and
      # next page token at index 1 if there is one.
      transfer_configs = client.ListTransferConfigs(
          reference=reference,
          location=self.transfer_location,
          page_size=self.max_results,
          page_token=page_token,
          data_source_ids=self.filter)
      # If the max_results flag is set and the length of transfer_configs is 2
      # then it also contains the next_page_token.
      if self.max_results and len(transfer_configs) == 2:
        page_token = dict(nextPageToken=transfer_configs[1])
        _PrintPageToken(page_token)
      results = transfer_configs[0]
    elif self.transfer_run:
      object_type = TransferRunReference
      run_attempt = self.run_attempt
      formatted_identifier = _FormatDataTransferIdentifiers(client, identifier)
      reference = TransferRunReference(transferRunName=formatted_identifier)
      # list_transfer_runs_result tuple contains transfer runs at index 0 and
      # next page token at index 1 if there is next page token.
      list_transfer_runs_result = client.ListTransferRuns(
          reference,
          run_attempt,
          max_results=self.max_results,
          page_token=self.page_token,
          states=self.filter)
      # If the max_results flag is set and the length of response is 2
      # then it also contains the next_page_token.
      if self.max_results and len(list_transfer_runs_result) == 2:
        page_token = dict(nextPageToken=list_transfer_runs_result[1])
        _PrintPageToken(page_token)
      results = list_transfer_runs_result[0]
    elif self.transfer_log:
      object_type = TransferLogReference
      formatted_identifier = _FormatDataTransferIdentifiers(client, identifier)
      reference = TransferLogReference(transferRunName=formatted_identifier)
      # list_transfer_log_result tuple contains transfer logs at index 0 and
      # next page token at index 1 if there is one.
      list_transfer_log_result = client.ListTransferLogs(
          reference,
          message_type=self.message_type,
          max_results=self.max_results,
          page_token=self.page_token)
      if self.max_results and len(list_transfer_log_result) == 2:
        page_token = dict(nextPageToken=list_transfer_log_result[1])
        _PrintPageToken(page_token)
      results = list_transfer_log_result[0]
    elif self.p or reference is None:
      object_type = ProjectReference
      results = client.ListProjects(
          max_results=self.max_results, page_token=page_token)
    elif isinstance(reference, ProjectReference):
      object_type = DatasetReference
      results = client.ListDatasets(
          reference,
          max_results=self.max_results,
          list_all=self.a,
          page_token=page_token,
          filter_expression=self.filter)
    else:  # isinstance(reference, DatasetReference):
      object_type = TableReference
      results = client.ListTables(
          reference, max_results=self.max_results, page_token=page_token)
    if results:
      assert object_type is not None
      _PrintObjectsArray(results, object_type)


def _PrintPageToken(page_token):
  """Prints the page token in the pretty format.

  Args:
    page_token: The dictonary mapping of pageToken with string 'nextPageToken'.
  """
  formatter = _GetFormatterFromFlags(secondary_format='pretty')
  BigqueryClient.ConfigureFormatter(formatter, NextPageTokenReference)
  formatter.AddDict(page_token)
  formatter.Print()


class _Delete(BigqueryCmd):
  usage = """rm [-f] [-r] [(-d|-t)] <identifier>"""

  def __init__(self, name, fv):
    super(_Delete, self).__init__(name, fv)
    flags.DEFINE_boolean(
        'dataset',
        False,
        'Remove dataset described by this identifier.',
        short_name='d',
        flag_values=fv)
    flags.DEFINE_boolean(
        'table',
        False,
        'Remove table described by this identifier.',
        short_name='t',
        flag_values=fv)
    flags.DEFINE_boolean(
        'transfer_config',
        False,
        'Remove transfer configuration described by this identifier.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'force',
        False,
        "Ignore existing tables and datasets, don't prompt.",
        short_name='f',
        flag_values=fv)
    flags.DEFINE_boolean(
        'recursive',
        False,
        'Remove dataset and any tables it may contain.',
        short_name='r',
        flag_values=fv)
    flags.DEFINE_boolean(
        'reservation',
        None,
        'Deletes the reservation described by this identifier.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'slot_pool',
        None,
        'Deletes the slot pool described by this identifier.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'reservation_grant', None, 'Delete a reservation grant.',
        flag_values=fv)
    self._ProcessCommandRc(fv)

  def RunWithArgs(self, identifier):
    """Delete the dataset, table, or transfer config described by identifier.

    Always requires an identifier, unlike the show and ls commands.
    By default, also requires confirmation before deleting. Supports
    the -d -t flags to signify that the identifier is a dataset
    or table.
     * With -f, don't ask for confirmation before deleting.
     * With -r, remove all tables in the named dataset.

    Examples:
      bq rm ds.table
      bq rm -r -f old_dataset
      bq rm --transfer_config=projects/p/locations/l/transferConfigs/c
      bq rm --reservation_grant --project_id=proj --location=us query_proj_dev
    """

    client = Client.Get()

    # pylint: disable=g-doc-exception
    if self.d and self.t:
      raise app.UsageError('Cannot specify more than one of -d and -t.')
    if not identifier:
      raise app.UsageError('Must provide an identifier for rm.')

    if self.t:
      reference = client.GetTableReference(identifier)
    elif self.d:
      reference = client.GetDatasetReference(identifier)
    elif self.transfer_config:
      formatted_identifier = _FormatDataTransferIdentifiers(client, identifier)
      reference = TransferConfigReference(
          transferConfigName=formatted_identifier)
    elif self.reservation:
      if self.slot_pool:
        try:
          reference = client.GetSlotPoolReference(
              identifier=identifier, default_location=FLAGS.location)
          client.DeleteSlotPool(reference)
          print "Slot pool '%s' successfully deleted." % identifier
        except BaseException as e:
          raise bigquery_client.BigqueryError(
              "Failed to delete slot pool in '%s': %s" % (identifier, e))
      else:
        try:
          reference = client.GetReservationReference(identifier, FLAGS.location)
          client.DeleteReservation(reference, self.force)
          print "Reservation '%s' successfully deleted." % identifier
        except BaseException as e:
          raise bigquery_client.BigqueryError(
              "Failed to delete reservation '%s': %s" % (identifier, e))
    elif self.reservation_grant:
      try:
        reference = client.GetReservationGrantReference(
            identifier=identifier, default_location=FLAGS.location)
        client.DeleteReservationGrant(reference)
        print "Reservation grant '%s' successfully deleted." % identifier
      except BaseException as e:
        raise bigquery_client.BigqueryError(
            "Failed to delete reservation grant '%s': %s" % (identifier, e))
    else:
      reference = client.GetReference(identifier)
      _Typecheck(reference, (DatasetReference, TableReference),
                 'Invalid identifier "%s" for rm.' % (identifier,))

    if isinstance(reference, TableReference) and self.r:
      raise app.UsageError('Cannot specify -r with %r' % (reference,))

    if not self.force:
      if ((isinstance(reference, DatasetReference) and
           client.DatasetExists(reference)) or
          (isinstance(reference, TableReference) and
           client.TableExists(reference)) or
          (isinstance(reference, TransferConfigReference) and
           client.TransferExists(reference))):
        if 'y' != _PromptYN('rm: remove %r? (y/N) ' % (reference,)):
          print 'NOT deleting %r, exiting.' % (reference,)
          return 0

    if isinstance(reference, DatasetReference):
      client.DeleteDataset(
          reference,
          ignore_not_found=self.force,
          delete_contents=self.recursive)
    elif isinstance(reference, TableReference):
      client.DeleteTable(reference, ignore_not_found=self.force)
    elif isinstance(reference, TransferConfigReference):
      client.DeleteTransferConfig(reference, ignore_not_found=self.force)


class _Copy(BigqueryCmd):
  usage = """cp [-n] <source_table>[,<source_table>]* <dest_table>"""

  def __init__(self, name, fv):
    super(_Copy, self).__init__(name, fv)
    flags.DEFINE_boolean(
        'no_clobber',
        False,
        'Do not overwrite an existing table.',
        short_name='n',
        flag_values=fv)
    flags.DEFINE_boolean(
        'force',
        False,
        "Ignore existing destination tables, don't prompt.",
        short_name='f',
        flag_values=fv)
    flags.DEFINE_boolean(
        'append_table',
        False,
        'Append to an existing table.',
        short_name='a',
        flag_values=fv)
    flags.DEFINE_string(
        'destination_kms_key',
        None,
        'Cloud KMS key for encryption of the destination table data.',
        flag_values=fv)
    self._ProcessCommandRc(fv)

  def RunWithArgs(self, source_tables, dest_table):
    """Copies one table to another.

    Examples:
      bq cp dataset.old_table dataset2.new_table
      bq cp --destination_kms_key=kms_key dataset.old_table dataset2.new_table
    """
    client = Client.Get()
    source_references = [
        client.GetTableReference(src) for src in source_tables.split(',')
    ]
    source_references_str = ', '.join(str(src) for src in source_references)
    dest_reference = client.GetTableReference(dest_table)

    if self.append_table:
      write_disposition = 'WRITE_APPEND'
      ignore_already_exists = True
    elif self.no_clobber:
      write_disposition = 'WRITE_EMPTY'
      ignore_already_exists = True
    else:
      write_disposition = 'WRITE_TRUNCATE'
      ignore_already_exists = False
      if not self.force:
        if client.TableExists(dest_reference):
          if 'y' != _PromptYN('cp: replace %s? (y/N) ' % (dest_reference,)):
            print 'NOT copying %s, exiting.' % (source_references_str,)
            return 0
    kwds = {
        'write_disposition': write_disposition,
        'ignore_already_exists': ignore_already_exists,
        'job_id': _GetJobIdFromFlags(),
    }
    if FLAGS.location:
      kwds['location'] = FLAGS.location

    if self.destination_kms_key:
      kwds['encryption_configuration'] = {
          'kmsKeyName': self.destination_kms_key
      }
    job = client.CopyTable(source_references, dest_reference, **kwds)
    if job is None:
      print "Table '%s' already exists, skipping" % (dest_reference,)
    elif not FLAGS.sync:
      self.PrintJobStartInfo(job)
    else:
      plurality = 's' if len(source_references) > 1 else ''
      print "Table%s '%s' successfully copied to '%s'" % (
          plurality, source_references_str, dest_reference)
      # If we are here, the job succeeded, but print warnings if any.
      _PrintJobMessages(client.FormatJobInfo(job))


def _ParseTimePartitioning(partitioning_type=None,
                           partitioning_expiration=None,
                           partitioning_field=None,
                           partitioning_minimum_partition_date=None,
                           partitioning_require_partition_filter=None):
  """Parses time partitioning from the arguments.

  Args:
    partitioning_type: type for the time partitioning. The default value is DAY
      when other arguments are specified, which generates one partition per day.
    partitioning_expiration: number of seconds to keep the storage for a
      partition. A negative value clears this setting.
    partitioning_field: if not set, the table is partitioned based on the
      loading time; if set, the table is partitioned based on the value of this
      field.
    partitioning_minimum_partition_date: lower boundary of partition date for
      field based partitioning table.
    partitioning_require_partition_filter: if true, queries on the table must
      have a partition filter so not all partitions are scanned.

  Returns:
    Time partitioning if any of the arguments is not None, otherwise None.

  Raises:
    UsageError: when failed to parse.
  """

  time_partitioning = {}
  key_type = 'type'
  key_expiration = 'expirationMs'
  key_field = 'field'
  key_minimum_partition_date = 'minimumPartitionDate'
  key_require_partition_filter = 'requirePartitionFilter'
  if partitioning_type is not None:
    time_partitioning[key_type] = partitioning_type
  if partitioning_expiration is not None:
    time_partitioning[key_expiration] = partitioning_expiration * 1000
  if partitioning_field is not None:
    time_partitioning[key_field] = partitioning_field
  if partitioning_minimum_partition_date is not None:
    if partitioning_field is not None:
      time_partitioning[
          key_minimum_partition_date] = partitioning_minimum_partition_date
    else:
      raise app.UsageError('Need to specify --time_partitioning_field for '
                           '--time_partitioning_minimum_partition_date.')
  if partitioning_require_partition_filter is not None:
    if time_partitioning:
      time_partitioning[
          key_require_partition_filter] = partitioning_require_partition_filter

  if time_partitioning:
    if key_type not in time_partitioning:
      time_partitioning[key_type] = 'DAY'
    if (key_expiration in time_partitioning and
        time_partitioning[key_expiration] <= 0):
      time_partitioning[key_expiration] = None
    return time_partitioning
  else:
    return None


def _ParseClustering(clustering_fields=None):
  """Parses clustering from the arguments.

  Args:
    clustering_fields: Comma-separated field names.

  Returns:
    Clustering if any of the arguments is not None, otherwise None.
  """

  clustering = {}
  key_field = 'fields'
  if clustering_fields:
    clustering[key_field] = clustering_fields.split(',')
    return clustering
  else:
    return None


def _ParseRangePartitioning(range_partitioning_spec=None):
  """Parses range partitioning from the arguments.

  Args:
    range_partitioning_spec: specification for range partitioning in the format
    of field,start,end,interval.
  Returns:
    Range partitioning if range_partitioning_spec is not None, otherwise None.
  Raises:
    UsageError: when the spec fails to parse.
  """

  range_partitioning = {}
  key_field = 'field'
  key_range = 'range'
  key_range_start = 'start'
  key_range_end = 'end'
  key_range_interval = 'interval'

  if range_partitioning_spec is not None:
    parts = range_partitioning_spec.split(',')
    if len(parts) != 4:
      raise app.UsageError(
          'Error parsing range_partitioning. range_partitioning should be in '
          'the format of "field,start,end,interval"')
    range_partitioning[key_field] = parts[0]
    range_spec = {}
    range_spec[key_range_start] = parts[1]
    range_spec[key_range_end] = parts[2]
    range_spec[key_range_interval] = parts[3]
    range_partitioning[key_range] = range_spec

  if range_partitioning:
    return range_partitioning
  else:
    return None


class _Make(BigqueryCmd):
  usage = """mk [-d] <identifier>  OR  mk [-t] <identifier> [<schema>]"""

  def __init__(self, name, fv):
    super(_Make, self).__init__(name, fv)
    flags.DEFINE_boolean(
        'force',
        False,
        'Ignore errors reporting that the object already exists.',
        short_name='f',
        flag_values=fv)
    flags.DEFINE_boolean(
        'dataset',
        False,
        'Create dataset with this name.',
        short_name='d',
        flag_values=fv)
    flags.DEFINE_boolean(
        'table',
        False,
        'Create table with this name.',
        short_name='t',
        flag_values=fv)
    flags.DEFINE_boolean(
        'transfer_config', None, 'Create transfer config.', flag_values=fv)
    flags.DEFINE_string(
        'target_dataset',
        '',
        'Target dataset for the created transfer configuration.',
        flag_values=fv)
    flags.DEFINE_string(
        'display_name',
        '',
        'Display name for the created transfer configuration.',
        flag_values=fv)
    flags.DEFINE_string(
        'data_source',
        '',
        'Data source for the created transfer configuration.',
        flag_values=fv)
    flags.DEFINE_integer(
        'refresh_window_days',
        0,
        'Refresh window days for the created transfer configuration.',
        flag_values=fv)
    flags.DEFINE_string(
        'params',
        None,
        'Parameters for the created transfer configuration in JSON format. '
        'For example: --params=\'{\"param\":\"param_value\"}\'',
        short_name='p',
        flag_values=fv)
    flags.DEFINE_bool(
        'transfer_run',
        False,
        'Creates transfer runs for a time range.',
        flag_values=fv)
    flags.DEFINE_string(
        'start_time',
        None,
        'Start time of the range of transfer runs. '
        'The format for the time stamp is RFC3339 UTC "Zulu". '
        'Read more: '
        'https://developers.google.com/protocol-buffers/docs/'
        'reference/google.protobuf#google.protobuf.Timestamp ',
        flag_values=fv)
    flags.DEFINE_string(
        'end_time',
        None,
        'End time of the range of transfer runs. '
        'The format for the time stamp is RFC3339 UTC "Zulu". '
        'Read more: '
        'https://developers.google.com/protocol-buffers/docs/'
        'reference/google.protobuf#google.protobuf.Timestamp ',
        flag_values=fv)
    flags.DEFINE_string(
        'schedule_start_time',
        None, 'Time to start scheduling transfer runs for the given '
        'transfer configuration. If empty, the default value for '
        'the start time will be used to start runs immediately.'
        'The format for the time stamp is RFC3339 UTC "Zulu". '
        'Read more: '
        'https://developers.google.com/protocol-buffers/docs/'
        'reference/google.protobuf#google.protobuf.Timestamp',
        flag_values=fv)
    flags.DEFINE_string(
        'schedule_end_time',
        None, 'Time to stop scheduling transfer runs for the given '
        'transfer configuration. If empty, the default value for '
        'the end time will be used to schedule runs indefinitely.'
        'The format for the time stamp is RFC3339 UTC "Zulu". '
        'Read more: '
        'https://developers.google.com/protocol-buffers/docs/'
        'reference/google.protobuf#google.protobuf.Timestamp',
        flag_values=fv)
    flags.DEFINE_string(
        'schedule',
        None,
        'Data transfer schedule. If the data source does not support a custom '
        'schedule, this should be empty. If empty, the default '
        'value for the data source will be used. The specified times are in '
        'UTC. Examples of valid format: 1st,3rd monday of month 15:30, '
        'every wed,fri of jan,jun 13:15, and first sunday of quarter 00:00. '
        'See more explanation about the format here: '
        'https://cloud.google.com/appengine/docs/flexible/python/scheduling-jobs-with-cron-yaml#the_schedule_format',  # pylint: disable=line-too-long
        flag_values=fv)
    flags.DEFINE_bool(
        'no_auto_scheduling',
        False, 'Disables automatic scheduling of data transfer runs for this '
        'configuration.',
        flag_values=fv)
    flags.DEFINE_string(
        'schema',
        '', 'Either a filename or a comma-separated list of fields in the form '
        'name[:type].',
        flag_values=fv)
    flags.DEFINE_string(
        'description',
        None,
        'Description of the dataset or table.',
        flag_values=fv)
    flags.DEFINE_string(
        'data_location',
        None,
        'Geographic location of the data. See details at '
        'https://cloud.google.com/bigquery/docs/dataset-locations.',
        flag_values=fv)
    flags.DEFINE_integer(
        'expiration',
        None,
        'Expiration time, in seconds from now, of a table.',
        flag_values=fv)
    flags.DEFINE_integer(
        'default_table_expiration',
        None,
        'Default lifetime, in seconds, for newly-created tables in a '
        'dataset. Newly-created tables will have an expiration time of '
        'the current time plus this value.',
        flag_values=fv)
    flags.DEFINE_integer(
        'default_partition_expiration',
        None,
        'Default partition expiration for all partitioned tables in the dataset'
        ', in seconds. The storage in a partition will have an expiration time '
        'of its partition time plus this value. If this property is set, '
        'partitioned tables created in the dataset will use this instead of '
        'default_table_expiration.',
        flag_values=fv)
    flags.DEFINE_string(
        'external_table_definition',
        None,
        'Specifies a table definition to use to create an external table. '
        'The value can be either an inline table definition or a path to a '
        'file containing a JSON table definition. '
        'The format of inline definition is "schema@format=uri".',
        flag_values=fv)
    flags.DEFINE_string(
        'view', '', 'Create view with this SQL query.', flag_values=fv)
    flags.DEFINE_multistring(
        'view_udf_resource',
        None,
        'The URI or local filesystem path of a code file to load and '
        'evaluate immediately as a User-Defined Function resource used '
        'by the view.',
        flag_values=fv)
    flags.DEFINE_string(
        'materialized_view',
        None,
        '[Experimental] Create materialized view with this Standard SQL query.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'use_legacy_sql',
        None,
        ('Whether to use Legacy SQL for the view. If not set, the default '
         'behavior is true.'),
        flag_values=fv)
    flags.DEFINE_string(
        'time_partitioning_type',
        None,
        'Enables time based partitioning on the table and set the type. The '
        'only type accepted is DAY, which will generate one partition per day.',
        flag_values=fv)
    flags.DEFINE_integer(
        'time_partitioning_expiration',
        None,
        'Enables time based partitioning on the table and sets the number of '
        'seconds for which to keep the storage for the partitions in the table.'
        ' The storage in a partition will have an expiration time of its '
        'partition time plus this value. A negative number means no '
        'expiration.',
        flag_values=fv)
    flags.DEFINE_string(
        'time_partitioning_field',
        None,
        'Enables time based partitioning on the table and the table will be '
        'partitioned based on the value of this field. If time based '
        'partitioning is enabled without this value, the table will be '
        'partitioned based on the loading time.',
        flag_values=fv)
    flags.DEFINE_string(
        'destination_kms_key',
        None,
        'Cloud KMS key for encryption of the destination table data.',
        flag_values=fv)
    flags.DEFINE_multistring(
        'label',
        None,
        'A label to set on the table or dataset. The format is "key:value"',
        flag_values=fv)
    flags.DEFINE_boolean(
        'require_partition_filter',
        None,
        'Whether to require partition filter for queries over this table. '
        'Only apply to partitioned table.',
        flag_values=fv)
    flags.DEFINE_string(
        'clustering_fields',
        None,
        'Comma separated field names. Can only be specified with time based '
        'partitioning. Data will be first partitioned and subsequently "'
        'clustered on these fields.',
        flag_values=fv)
    flags.DEFINE_string(
        'range_partitioning',
        None,
        'Enables range partitioning on the table. The format should be '
        '"field,start,end,interval". The table will be partitioned based on the'
        ' value of the field. Field must be a top-level, non-repeated INT64 '
        'field. Start, end, and interval are INT64 values defining the ranges.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'reservation',
        None,
        'Creates a reservation described by this identifier. Reservation '
        'hierarchies can be specified by separating reservations with a slash.'
        'For example foo/bar/baz.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'slot_pool',
        None,
        'Creates a slot pool in the specified reservation. You do not need to '
        'specify a slot pool id, this will be assigned automatically.',
        flag_values=fv)
    flags.DEFINE_integer(
        'slots',
        0,
        'The number of slots associated with the reservation subtree rooted at '
        'this reservation node.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'use_parent',
        True,
        'If true, any query using this reservation will also be submitted to '
        'the parent reservation.',
        flag_values=fv)
    flags.DEFINE_enum(
        'plan',
        None, ['ADHOC', 'ONE_DAY', 'THIRTY_DAYS'],
        'Commitment plan for this slot pool. Plans cannot be deleted before '
        'their commitment period is over. Options include:'
        '\n ADHOC'
        '\n ONE_DAY'
        '\n THIRTY_DAYS',
        flag_values=fv)
    flags.DEFINE_boolean(
        'reservation_grant',
        None,
        'Create a reservation grant.',
        flag_values=fv)
    flags.DEFINE_enum(
        'job_type',
        None, ['QUERY', 'PIPELINE'],
        'Type of jobs to create reservation grant for. Options include:'
        '\n QUERY'
        '\n PIPELINE',
        flag_values=fv)
    flags.DEFINE_string(
        'reservation_id', None,
        'Reservation ID used to create reservation grant for. '
        'Used in conjuction with --reservation_grant.',
        flag_values=fv)
    self._ProcessCommandRc(fv)

  def RunWithArgs(self, identifier='', schema=''):
    # pylint: disable=g-doc-exception
    """Create a dataset, table, view, or transfer configuration with this name.

    See 'bq help load' for more information on specifying the schema.

    Examples:
      bq mk new_dataset
      bq mk new_dataset.new_table
      bq --dataset_id=new_dataset mk table
      bq mk -t new_dataset.newtable name:integer,value:string
      bq mk --view='select 1 as num' new_dataset.newview
         (--view_udf_resource=path/to/file.js)
      bq mk --materialized_view='select sum(x) as sum_x from dataset.table'
          new_dataset.newview
      bq mk -d --data_location=EU new_dataset
      bq mk --transfer_config --target_dataset=dataset --display_name=name
          -p='{"param":"value"}' --data_source=source
          --schedule_start_time={schedule_start_time}
          --schedule_end_time={schedule_end_time}
      bq mk --transfer_run --start_time={start_time} --end_time={end_time}
          projects/p/locations/l/transferConfigs/c
      bq mk ---reservation_grant -project_id=proj --location=us
          --reservation_id=project:us.dev --job_type=QUERY
    """

    client = Client.Get()

    if self.d and self.t:
      raise app.UsageError('Cannot specify both -d and -t.')
    if ValidateAtMostOneSelected(self.schema, self.view,
                                 self.materialized_view):
      raise app.UsageError('Cannot specify more than one of'
                           ' --schema or --view or --materialized_view.')
    if self.t:
      reference = client.GetTableReference(identifier)
    elif self.view:
      reference = client.GetTableReference(identifier)
    elif self.materialized_view:
      reference = client.GetTableReference(identifier)
    elif self.reservation:
      object_info = None
      reference = client.GetReservationReference(identifier, FLAGS.location)
      if self.slot_pool:
        try:
          result = client.CreateSlotPool(reference, self.slots, self.plan)
        except BaseException as e:
          raise bigquery_client.BigqueryError(
              "Failed to create slot pool in '%s': %s" % (identifier, e))
        if result['done']:
          object_info = result['response']
          reference = client.GetSlotPoolReference(
              path=object_info['name'], default_location=FLAGS.location)
        else:
          print 'Slot pool creation is pending: %s' % result['name']
      else:
        try:
          object_info = client.CreateReservation(reference, self.slots,
                                                 self.use_parent)
        except BaseException as e:
          raise bigquery_client.BigqueryError(
              "Failed to create reservation '%s': %s" % (identifier, e))
      if object_info is not None:
        _PrintObjectInfo(object_info, reference, custom_format='show')
    elif self.reservation_grant:
      try:
        reference = client.GetReservationGrantReference(
            identifier=identifier, default_location=FLAGS.location,
            default_reservation_grant_id=' ')
        object_info = client.CreateReservationGrant(
            reference, self.reservation_id, self.job_type)
        _PrintObjectInfo(object_info, reference, custom_format='show')
      except BaseException as e:
        raise bigquery_client.BigqueryError(
            "Failed to create reservation grant '%s': %s" % (identifier, e))
    elif self.transfer_config:
      transfer_client = client.GetTransferV1ApiClient()
      reference = 'projects/' + (client.GetProjectReference().projectId)
      credentials = False
      if self.data_source:
        data_sources_reference = (
            reference + '/dataSources/' + self.data_source)
        try:
          transfer_client.projects().dataSources().get(
              name=data_sources_reference).execute()
        except:
          raise bigquery_client.BigqueryNotFoundError(
              'Unknown data source %r' % (self.data_source),
              {'reason': 'notFound'}, [])
        credentials = transfer_client.projects().dataSources().checkValidCreds(
            name=data_sources_reference, body={}).execute()
      else:
        raise bigquery_client.BigqueryError('A data source must be provided.')
      authorization_code = ''
      if not credentials and self.data_source != 'loadtesting':
        authorization_code = RetrieveAuthorizationCode(
            reference, self.data_source, transfer_client)
      schedule_args = bigquery_client.TransferScheduleArgs(
          schedule=self.schedule,
          start_time=self.schedule_start_time,
          end_time=self.schedule_end_time,
          disable_auto_scheduling=self.no_auto_scheduling)
      transfer_name = client.CreateTransferConfig(
          reference=reference,
          data_source=self.data_source,
          target_dataset=self.target_dataset,
          display_name=self.display_name,
          refresh_window_days=self.refresh_window_days,
          params=self.params,
          authorization_code=authorization_code,
          schedule_args=schedule_args)
      print(
          'Transfer configuration \'%s\' successfully created.' % transfer_name)
    elif self.transfer_run:
      formatter = _GetFormatterFromFlags()
      formatted_identifier = _FormatDataTransferIdentifiers(client, identifier)
      reference = TransferConfigReference(
          transferConfigName=formatted_identifier)
      if not self.start_time or not self.end_time:
        raise app.UsageError(
            'Need to specify both --start_time and --end_time.')
      start_time = self.start_time
      end_time = self.end_time
      results = map(client.FormatTransferRunInfo,
                    client.ScheduleTransferRun(reference, start_time, end_time))
      BigqueryClient.ConfigureFormatter(
          formatter,
          TransferRunReference,
          print_format='make',
          object_info=results[0])
      for result in results:
        formatter.AddDict(result)
      formatter.Print()
    elif self.d or not identifier:
      reference = client.GetDatasetReference(identifier)
    else:
      reference = client.GetReference(identifier)
      _Typecheck(reference, (DatasetReference, TableReference),
                 "Invalid identifier '%s' for mk." % (identifier,))
    if isinstance(reference, DatasetReference):
      if self.schema:
        raise app.UsageError('Cannot specify schema with a dataset.')
      if self.expiration:
        raise app.UsageError('Cannot specify an expiration for a dataset.')
      if self.external_table_definition is not None:
        raise app.UsageError(
            'Cannot specify an external_table_definition for a dataset.')
      if client.DatasetExists(reference):
        message = "Dataset '%s' already exists." % (reference,)
        if not self.f:
          raise bigquery_client.BigqueryError(message)
        else:
          print message
          return
      default_table_exp_ms = None
      if self.default_table_expiration is not None:
        default_table_exp_ms = self.default_table_expiration * 1000
      default_partition_exp_ms = None
      if self.default_partition_expiration is not None:
        default_partition_exp_ms = self.default_partition_expiration * 1000

      location = self.data_location or FLAGS.location
      labels = None
      if self.label is not None:
        labels = _ParseLabels(self.label)

      client.CreateDataset(
          reference,
          ignore_existing=True,
          description=self.description,
          default_table_expiration_ms=default_table_exp_ms,
          default_partition_expiration_ms=default_partition_exp_ms,
          data_location=location,
          labels=labels)
      print "Dataset '%s' successfully created." % (reference,)
    elif isinstance(reference, TableReference):
      object_name = 'Table'
      if self.view:
        object_name = 'View'
      if self.materialized_view:
        object_name = 'Materialized View'
      if client.TableExists(reference):
        message = ("%s '%s' could not be created; a table with this name "
                   'already exists.') % (
                       object_name,
                       reference,
                   )
        if not self.f:
          raise bigquery_client.BigqueryError(message)
        else:
          print message
          return
      if schema:
        schema = bigquery_client.BigqueryClient.ReadSchema(schema)
      else:
        schema = None
      expiration = None
      labels = None
      if self.label is not None:
        labels = _ParseLabels(self.label)
      if self.data_location:
        raise app.UsageError('Cannot specify data location for a table.')
      if self.default_table_expiration:
        raise app.UsageError('Cannot specify default expiration for a table.')
      if self.expiration:
        expiration = int(self.expiration + time.time()) * 1000
      view_query_arg = self.view or None
      materialized_view_query_arg = self.materialized_view or None
      external_data_config = None
      if self.external_table_definition is not None:
        external_data_config = _GetExternalDataConfig(
            self.external_table_definition)
      view_udf_resources = None
      if self.view_udf_resource:
        view_udf_resources = _ParseUdfResources(self.view_udf_resource)
      time_partitioning = _ParseTimePartitioning(
          self.time_partitioning_type,
          self.time_partitioning_expiration,
          self.time_partitioning_field,
          None,
          self.require_partition_filter)
      clustering = _ParseClustering(self.clustering_fields)
      range_partitioning = _ParseRangePartitioning(self.range_partitioning)
      client.CreateTable(
          reference,
          ignore_existing=True,
          schema=schema,
          description=self.description,
          expiration=expiration,
          view_query=view_query_arg,
          materialized_view_query=materialized_view_query_arg,
          view_udf_resources=view_udf_resources,
          use_legacy_sql=self.use_legacy_sql,
          external_data_config=external_data_config,
          labels=labels,
          time_partitioning=time_partitioning,
          clustering=clustering,
          range_partitioning=range_partitioning,
          require_partition_filter=self.require_partition_filter,
          destination_kms_key=(self.destination_kms_key))
      print "%s '%s' successfully created." % (
          object_name,
          reference,
      )


class _Update(BigqueryCmd):
  usage = """update [-d] [-t] <identifier> [<schema>]"""

  def __init__(self, name, fv):
    super(_Update, self).__init__(name, fv)
    flags.DEFINE_boolean(
        'dataset',
        False,
        'Updates a dataset with this name.',
        short_name='d',
        flag_values=fv)
    flags.DEFINE_boolean(
        'table',
        False,
        'Updates a table with this name.',
        short_name='t',
        flag_values=fv)
    flags.DEFINE_boolean(
        'reservation',
        None,
        'Updates a reservation described by this identifier.',
        flag_values=fv)
    flags.DEFINE_integer(
        'slots',
        None,
        'The number of slots associated with the reservation subtree rooted at '
        'this reservation node.',
        flag_values=fv)
    flags.DEFINE_string(
        'reservation_size',
        None, 'BI reservation size. Can be specified in bytes '
        '(--reservation_size=2147483648) or in GB (--reservation_size=2G). '
        'Minimum 2GB. Use 0 to remove reservation.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'use_parent',
        None,
        'If true, any query using this reservation will also be submitted to '
        'the parent reservation.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'transfer_config',
        False,
        'Updates a transfer configuration for a configuration resource name.',
        flag_values=fv)
    flags.DEFINE_string(
        'target_dataset',
        '',
        'Updated dataset ID for the transfer configuration.',
        flag_values=fv)
    flags.DEFINE_string(
        'display_name',
        '',
        'Updated display name for the updated transfer configuration.',
        flag_values=fv)
    flags.DEFINE_integer(
        'refresh_window_days',
        None,
        'Updated refresh window days for the updated transfer configuration.',
        flag_values=fv)
    flags.DEFINE_string(
        'params',
        None,
        'Updated parameters for the updated transfer configuration '
        'in JSON format.'
        'For example: --params=\'{\"param\":\"param_value\"}\'',
        short_name='p',
        flag_values=fv)
    flags.DEFINE_boolean(
        'update_credentials',
        False,
        'Update the transfer configuration credentials.',
        flag_values=fv)
    flags.DEFINE_string(
        'schedule_start_time',
        None, 'Time to start scheduling transfer runs for the given '
        'transfer configuration. If empty, the default value for '
        'the start time will be used to start runs immediately.'
        'The format for the time stamp is RFC3339 UTC "Zulu". '
        'Read more: '
        'https://developers.google.com/protocol-buffers/docs/'
        'reference/google.protobuf#google.protobuf.Timestamp',
        flag_values=fv)
    flags.DEFINE_string(
        'schedule_end_time',
        None, 'Time to stop scheduling transfer runs for the given '
        'transfer configuration. If empty, the default value for '
        'the end time will be used to schedule runs indefinitely.'
        'The format for the time stamp is RFC3339 UTC "Zulu". '
        'Read more: '
        'https://developers.google.com/protocol-buffers/docs/'
        'reference/google.protobuf#google.protobuf.Timestamp',
        flag_values=fv)
    flags.DEFINE_string(
        'schedule',
        None,
        'Data transfer schedule. If the data source does not support a custom '
        'schedule, this should be empty. If empty, the default '
        'value for the data source will be used. The specified times are in '
        'UTC. Examples of valid format: 1st,3rd monday of month 15:30, '
        'every wed,fri of jan,jun 13:15, and first sunday of quarter 00:00. '
        'See more explanation about the format here: '
        'https://cloud.google.com/appengine/docs/flexible/python/scheduling-jobs-with-cron-yaml#the_schedule_format',  # pylint: disable=line-too-long
        flag_values=fv)
    flags.DEFINE_bool(
        'no_auto_scheduling',
        False, 'Disables automatic scheduling of data transfer runs for this '
        'configuration.',
        flag_values=fv)
    flags.DEFINE_string(
        'schema',
        '',
        'Either a filename or a comma-separated list of fields in the form '
        'name[:type].',
        flag_values=fv)
    flags.DEFINE_string(
        'description',
        None,
        'Description of the dataset, table or view.',
        flag_values=fv)
    flags.DEFINE_multistring(
        'set_label',
        None,
        'A label to set on a dataset or a table. The format is "key:value"',
        flag_values=fv)
    flags.DEFINE_multistring(
        'clear_label',
        None,
        'A label key to remove from a dataset or a table.',
        flag_values=fv)
    flags.DEFINE_integer(
        'expiration',
        None,
        'Expiration time, in seconds from now, of a table or view. '
        'Specifying 0 removes expiration time.',
        flag_values=fv)
    flags.DEFINE_integer(
        'default_table_expiration',
        None,
        'Default lifetime, in seconds, for newly-created tables in a '
        'dataset. Newly-created tables will have an expiration time of '
        'the current time plus this value. Specify "0" to remove existing '
        'expiration.',
        flag_values=fv)
    flags.DEFINE_integer(
        'default_partition_expiration',
        None,
        'Default partition expiration for all partitioned tables in the dataset'
        ', in seconds. The storage in a partition will have an expiration time '
        'of its partition time plus this value. If this property is set, '
        'partitioned tables created in the dataset will use this instead of '
        'default_table_expiration. Specify "0" to remove existing expiration.',
        flag_values=fv)
    flags.DEFINE_string(
        'source',
        None,
        'Path to file with JSON payload for an update',
        flag_values=fv)
    flags.DEFINE_string('view', '', 'SQL query of a view.', flag_values=fv)
    flags.DEFINE_string(
        'materialized_view',
        None,
        'Standard SQL query of a materialized view.',
        flag_values=fv)
    flags.DEFINE_string(
        'external_table_definition',
        None,
        'Specifies a table definition to use to update an external table. '
        'The value can be either an inline table definition or a path to a '
        'file containing a JSON table definition.'
        'The format of inline definition is "schema@format=uri".',
        flag_values=fv)
    flags.DEFINE_multistring(
        'view_udf_resource',
        None,
        'The URI or local filesystem path of a code file to load and '
        'evaluate immediately as a User-Defined Function resource used '
        'by the view.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'use_legacy_sql',
        None,
        ('Whether to use Legacy SQL for the view. If not set, the default '
         'behavior is true.'),
        flag_values=fv)
    flags.DEFINE_string(
        'time_partitioning_type',
        None,
        'Enables time based partitioning on the table and set the type. The '
        'only type accepted is DAY, which will generate one partition per day.',
        flag_values=fv)
    flags.DEFINE_integer(
        'time_partitioning_expiration',
        None,
        'Enables time based partitioning on the table and sets the number of '
        'seconds for which to keep the storage for the partitions in the table.'
        ' The storage in a partition will have an expiration time of its '
        'partition time plus this value. A negative number means no '
        'expiration.',
        flag_values=fv)
    flags.DEFINE_string(
        'time_partitioning_field',
        None,
        'Enables time based partitioning on the table and the table will be '
        'partitioned based on the value of this field. If time based '
        'partitioning is enabled without this value, the table will be '
        'partitioned based on the loading time.',
        flag_values=fv)
    flags.DEFINE_string(
        'etag', None, 'Only update if etag matches.', flag_values=fv)
    flags.DEFINE_string(
        'destination_kms_key',
        None,
        'Cloud KMS key for encryption of the destination table data.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'require_partition_filter',
        None,
        'Whether to require partition filter for queries over this table. '
        'Only apply to partitioned table.',
        flag_values=fv)
    flags.DEFINE_string(
        'range_partitioning',
        None,
        'Enables range partitioning on the table. The format should be '
        '"field,start,end,interval". The table will be partitioned based on the'
        ' value of the field. Field must be a top-level, non-repeated INT64 '
        'field. Start, end, and interval are INT64 values defining the ranges.',
        flag_values=fv)
    self._ProcessCommandRc(fv)

  def RunWithArgs(self, identifier='', schema=''):
    # pylint: disable=g-doc-exception
    """Updates a dataset, table, view or transfer configuration with this name.

    See 'bq help load' for more information on specifying the schema.

    Examples:
      bq update --description "Dataset description" existing_dataset
      bq update --description "My table" existing_dataset.existing_table
      bq update -t existing_dataset.existing_table name:integer,value:string
      bq update --destination_kms_key
          projects/p/locations/l/keyRings/r/cryptoKeys/k
          existing_dataset.existing_table
      bq update --view='select 1 as num' existing_dataset.existing_view
         (--view_udf_resource=path/to/file.js)
      bq update --transfer_config --display_name=name -p='{"param":"value"}'
          projects/p/locations/l/transferConfigs/c
      bq update --transfer_config --target_dataset=dataset
          --refresh_window_days=5 --update_credentials
          projects/p/locations/l/transferConfigs/c
      bq update --reservation --location=US --project_id=my-project
          --reservation_size=2G
    """
    client = Client.Get()
    if self.d and self.t:
      raise app.UsageError('Cannot specify both -d and -t.')
    if ValidateAtMostOneSelected(self.schema, self.view,
                                 self.materialized_view):
      raise app.UsageError('Cannot specify more than one of'
                           ' --schema or --view or --materialized_view.')
    if self.t:
      reference = client.GetTableReference(identifier)
    elif self.view:
      reference = client.GetTableReference(identifier)
    elif self.materialized_view:
      reference = client.GetTableReference(identifier)
    elif self.reservation:
      try:
        if self.reservation_size is not None:
          reference = client.GetBiReservationReference(FLAGS.location)
          object_info = client.UpdateBiReservation(reference,
                                                   self.reservation_size)
          print object_info
        else:
          reference = client.GetReservationReference(identifier, FLAGS.location)
          object_info = client.UpdateReservation(reference, self.slots,
                                                 self.use_parent)
          _PrintObjectInfo(object_info, reference, custom_format='show')
      except BaseException as e:
        raise bigquery_client.BigqueryError(
            "Failed to update reservation '%s': %s" % (identifier, e))
    elif self.d or not identifier:
      reference = client.GetDatasetReference(identifier)
    elif self.transfer_config:
      formatted_identifier = _FormatDataTransferIdentifiers(client, identifier)
      reference = TransferConfigReference(
          transferConfigName=formatted_identifier)
    else:
      reference = client.GetReference(identifier)
      _Typecheck(reference, (DatasetReference, TableReference),
                 "Invalid identifier '%s' for update." % (identifier,))

    label_keys_to_remove = None
    labels_to_set = None
    if self.set_label is not None:
      labels_to_set = _ParseLabels(self.set_label)
    if self.clear_label is not None:
      label_keys_to_remove = set(self.clear_label)

    if isinstance(reference, DatasetReference):
      if self.schema:
        raise app.UsageError('Cannot specify schema with a dataset.')
      if self.view:
        raise app.UsageError('Cannot specify view with a dataset.')
      if self.materialized_view:
        raise app.UsageError('Cannot specify materialized view with a dataset.')
      if self.expiration:
        raise app.UsageError('Cannot specify an expiration for a dataset.')
      if self.external_table_definition is not None:
        raise app.UsageError(
            'Cannot specify an external_table_definition for a dataset.')
      if self.source and self.description:
        raise app.UsageError('Cannot specify description with a source.')
      default_table_exp_ms = None
      if self.default_table_expiration is not None:
        default_table_exp_ms = self.default_table_expiration * 1000
      default_partition_exp_ms = None
      if self.default_partition_expiration is not None:
        default_partition_exp_ms = self.default_partition_expiration * 1000
      _UpdateDataset(
          client,
          reference,
          description=self.description,
          source=self.source,
          default_table_expiration_ms=default_table_exp_ms,
          default_partition_expiration_ms=default_partition_exp_ms,
          labels_to_set=labels_to_set,
          label_keys_to_remove=label_keys_to_remove,
          etag=self.etag)
      print "Dataset '%s' successfully updated." % (reference,)
    elif isinstance(reference, TableReference):
      object_name = 'Table'
      if self.view:
        object_name = 'View'
      if self.materialized_view:
        object_name = 'Materialized View'
      if self.source:
        raise app.UsageError(
            '%s update does not support --source.' % object_name)
      if schema:
        schema = bigquery_client.BigqueryClient.ReadSchema(schema)
      else:
        schema = None
      expiration = None
      if self.expiration is not None:
        if self.expiration == 0:
          expiration = 0
        else:
          expiration = int(self.expiration + time.time()) * 1000
      if self.default_table_expiration:
        raise app.UsageError('Cannot specify default expiration for a table.')
      external_data_config = None
      if self.external_table_definition is not None:
        external_data_config = _GetExternalDataConfig(
            self.external_table_definition)
        # When updating, move the schema out of the external_data_config.
        # If schema is set explicitly on this update, prefer it over the
        # external_data_config schema.
        if schema is None:
          schema = external_data_config['schema']['fields']
        del external_data_config['schema']
      view_query_arg = self.view or None
      materialized_view_query_arg = self.materialized_view or None
      view_udf_resources = None
      if self.view_udf_resource:
        view_udf_resources = _ParseUdfResources(self.view_udf_resource)
      time_partitioning = _ParseTimePartitioning(
          self.time_partitioning_type, self.time_partitioning_expiration,
          self.time_partitioning_field, None, self.require_partition_filter)
      range_partitioning = _ParseRangePartitioning(self.range_partitioning)

      encryption_configuration = None
      if self.destination_kms_key:
        encryption_configuration = {'kmsKeyName': self.destination_kms_key}

      client.UpdateTable(
          reference,
          schema=schema,
          description=self.description,
          expiration=expiration,
          view_query=view_query_arg,
          materialized_view_query=materialized_view_query_arg,
          view_udf_resources=view_udf_resources,
          use_legacy_sql=self.use_legacy_sql,
          external_data_config=external_data_config,
          labels_to_set=labels_to_set,
          label_keys_to_remove=label_keys_to_remove,
          time_partitioning=time_partitioning,
          range_partitioning=range_partitioning,
          require_partition_filter=self.require_partition_filter,
          etag=self.etag,
          encryption_configuration=encryption_configuration)

      print "%s '%s' successfully updated." % (
          object_name,
          reference,
      )
    elif isinstance(reference, TransferConfigReference):
      if client.TransferExists(reference):
        authorization_code = ''
        if self.update_credentials:
          transfer_config_name = _FormatDataTransferIdentifiers(
              client, reference.transferConfigName)
          current_config = client.GetTransferConfig(transfer_config_name)
          authorization_code = RetrieveAuthorizationCode(
              'projects/' + client.GetProjectReference().projectId,
              current_config['dataSourceId'], client.GetTransferV1ApiClient())
        schedule_args = bigquery_client.TransferScheduleArgs(
            schedule=self.schedule,
            start_time=self.schedule_start_time,
            end_time=self.schedule_end_time,
            disable_auto_scheduling=self.no_auto_scheduling)
        client.UpdateTransferConfig(
            reference=reference,
            target_dataset=self.target_dataset,
            display_name=self.display_name,
            refresh_window_days=self.refresh_window_days,
            params=self.params,
            authorization_code=authorization_code,
            schedule_args=schedule_args)
        print "Transfer configuration '%s' successfully updated." % (reference,)
      else:
        raise bigquery_client.BigqueryNotFoundError(
            'Not found: %r' % (reference,), {'reason': 'notFound'}, [])


def RetrieveAuthorizationCode(reference, data_source, transfer_client):
  """Retrieves the authorization code.

  An authorization code is needed if the Data Transfer Service does not
  have credentials for the requesting user and data source. The Data
  Transfer Service will convert this authorization code into a refresh
  token to perform transfer runs on the user's behalf.

  Args:
    reference: The project reference.
    data_source: The data source of the transfer config.
    transfer_client: The transfer api client.

  Returns:
    authentication_code: Authentication from user.

  """
  data_source_retrieval = reference + '/dataSources/' + data_source
  data_source_info = transfer_client.projects().dataSources().get(
      name=data_source_retrieval).execute()
  auth_uri = ('https://www.gstatic.com/bigquerydatatransfer/oauthz/'
              'auth?client_id=' + data_source_info['clientId'] + '&scope=' +
              '%20'.join(data_source_info['scopes']) +
              '&redirect_uri=urn:ietf:wg:oauth:2.0:oob')
  print '\n' + auth_uri
  print('Please copy and paste the above URL into your web browser'
        ' and follow the instructions to retrieve an authentication code.')
  authentication_code = _RawInput('Enter your authentication code here: ')
  return authentication_code


def _UpdateDataset(
    client,
    reference,
    description=None,
    source=None,
    default_table_expiration_ms=None,
    default_partition_expiration_ms=None,
    labels_to_set=None,
    label_keys_to_remove=None,
    etag=None):
  """Updates a dataset.

  Reads JSON file if specified and loads updated values, before calling bigquery
  dataset update.

  Args:
    client: the BigQuery client.
    reference: the DatasetReference to update.
    description: an optional dataset description.
    source: an optional filename containing the JSON payload.
    default_table_expiration_ms: optional number of milliseconds for the
      default expiration duration for new tables created in this dataset.
    default_partition_expiration_ms: optional number of milliseconds for the
      default partition expiration duration for new partitioned tables created
      in this dataset.
    labels_to_set: an optional dict of labels to set on this dataset.
    label_keys_to_remove: an optional list of label keys to remove from this
      dataset.

  Raises:
    UsageError: when incorrect usage or invalid args are used.
  """
  acl = None
  if source is not None:
    if not os.path.exists(source):
      raise app.UsageError('Source file not found: %s' % (source,))
    if not os.path.isfile(source):
      raise app.UsageError('Source path is not a file: %s' % (source,))
    with open(source) as f:
      try:
        payload = json.load(f)
        if payload.__contains__('description'):
          description = payload['description']
        if payload.__contains__('access'):
          acl = payload['access']
      except ValueError as e:
        raise app.UsageError(
            'Error decoding JSON schema from file %s: %s' % (source, e))
  client.UpdateDataset(
      reference,
      description=description,
      acl=acl,
      default_table_expiration_ms=default_table_expiration_ms,
      default_partition_expiration_ms=default_partition_expiration_ms,
      labels_to_set=labels_to_set,
      label_keys_to_remove=label_keys_to_remove,
      etag=etag)


class _Show(BigqueryCmd):
  usage = """show [<identifier>]"""

  def __init__(self, name, fv):
    super(_Show, self).__init__(name, fv)
    flags.DEFINE_boolean(
        'job',
        False,
        'If true, interpret this identifier as a job id.',
        short_name='j',
        flag_values=fv)
    flags.DEFINE_boolean(
        'dataset',
        False,
        'Show dataset with this name.',
        short_name='d',
        flag_values=fv)
    flags.DEFINE_boolean(
        'view',
        False,
        'Show view specific details instead of general table details.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'materialized_view',
        False,
        'Show materialized view specific details instead of general table '
        'details.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'schema',
        False,
        'Show only the schema instead of general table details.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'encryption_service_account',
        False,
        'Show the service account for a user if it exists, or create one '
        'if it does not exist.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'transfer_config',
        False,
        'Show transfer configuration for configuration resource name.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'transfer_run',
        False,
        'Show information about the particular transfer run.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'reservation',
        None,
        'Shows details for the reservation described by this identifier.',
        flag_values=fv)
    flags.DEFINE_boolean(
        'slot_pool',
        None,
        'Shows details for the slot pool described by this identifier.',
        flag_values=fv)
    self._ProcessCommandRc(fv)

  def RunWithArgs(self, identifier=''):
    """Show all information about an object.

    Examples:
      bq show -j <job_id>
      bq show dataset
      bq show [--schema] dataset.table
      bq show [--view] dataset.view
      bq show [--materialized_view] dataset.materialized_view
      bq show --transfer_config projects/p/locations/l/transferConfigs/c
      bq show --transfer_run projects/p/locations/l/transferConfigs/c/runs/r
      bq show --encryption_service_account
    """
    # pylint: disable=g-doc-exception
    client = Client.Get()
    custom_format = 'show'
    object_info = None
    if self.j:
      reference = client.GetJobReference(identifier, FLAGS.location)
    elif self.d:
      reference = client.GetDatasetReference(identifier)
    elif self.view:
      reference = client.GetTableReference(identifier)
      custom_format = 'view'
    elif self.materialized_view:
      reference = client.GetTableReference(identifier)
      custom_format = 'materialized_view'
    elif self.schema:
      if FLAGS.format not in [None, 'prettyjson', 'json']:
        raise app.UsageError(
            'Table schema output format must be json or prettyjson.')
      reference = client.GetTableReference(identifier)
      custom_format = 'schema'
    elif self.transfer_config:
      formatted_identifier = _FormatDataTransferIdentifiers(client, identifier)
      reference = TransferConfigReference(
          transferConfigName=formatted_identifier)
      object_info = client.GetTransferConfig(formatted_identifier)
    elif self.transfer_run:
      formatted_identifier = _FormatDataTransferIdentifiers(client, identifier)
      reference = TransferRunReference(transferRunName=formatted_identifier)
      object_info = client.GetTransferRun(formatted_identifier)
    elif self.reservation:
      if self.slot_pool:
        reference = client.GetSlotPoolReference(
            identifier=identifier, default_location=FLAGS.location)
        object_info = client.GetSlotPool(reference)
      else:
        reference = client.GetReservationReference(identifier, FLAGS.location)
        object_info = client.GetReservation(reference)
    elif self.encryption_service_account:
      object_info = client.apiclient.projects().getServiceAccount(
          projectId=client.GetProjectReference().projectId).execute()
      email = object_info['email']
      object_info = {'ServiceAccountID': email}
      reference = EncryptionServiceAccount(serviceAccount='serviceAccount')
    else:
      reference = client.GetReference(identifier)
    if reference is None:
      raise app.UsageError('Must provide an identifier for show.')

    if object_info is None:
      object_info = client.GetObjectInfo(reference)
    _PrintObjectInfo(object_info, reference, custom_format=custom_format)


def _PrintJobMessages(printable_job_info):
  """Prints additional info from a job formatted for printing.

  If the job had a fatal error, non-fatal warnings are not shown.

  If any error/warning does not have a 'message' key, printable_job_info must
  have 'jobReference' identifying the job.
  """

  job_ref = '(unknown)'  # Should never be seen, but beats a weird crash.
  if 'jobReference' in printable_job_info:
    job_ref = printable_job_info['jobReference']

  # For failing jobs, display the error but not any warnings, because those
  # may be more distracting than helpful.
  if printable_job_info['State'] == 'FAILURE':
    error_result = printable_job_info['status']['errorResult']
    error_ls = printable_job_info['status'].get('errors', [])
    error = bigquery_client.BigqueryError.Create(error_result, error_result,
                                                 error_ls)
    print 'Error encountered during job execution:\n%s\n' % (error,)
  elif 'errors' in printable_job_info['status']:
    warnings = printable_job_info['status']['errors']
    print('Warning%s encountered during job execution:\n' %
          ('' if len(warnings) == 1 else 's'))
    recommend_show = False
    for w in warnings:
      # Some warnings include detailed error messages, and some just
      # include programmatic error codes.  Some have a 'location'
      # separately, and some put it in the 'message' text.
      if 'message' not in w:
        recommend_show = True
      else:
        if 'location' in w:
          message = '[%s] %s' % (w['location'], w['message'])
        else:
          message = w['message']
        if message is not None:
          message = message.encode('utf-8')
        print '%s\n' % message
    if recommend_show:
      print 'Use "bq show -j %s" to view job warnings.' % job_ref


def _PrintObjectInfo(object_info, reference, custom_format):
  # The JSON formats are handled separately so that they don't print
  # the record as a list of one record.
  if custom_format == 'schema':
    if 'schema' not in object_info or 'fields' not in object_info['schema']:
      raise app.UsageError('Unable to retrieve schema from specified table.')
    _PrintFormattedJsonObject(object_info['schema']['fields'])
  elif FLAGS.format in ['prettyjson', 'json']:
    _PrintFormattedJsonObject(object_info)
  elif FLAGS.format in [None, 'sparse', 'pretty']:
    formatter = _GetFormatterFromFlags()
    BigqueryClient.ConfigureFormatter(
        formatter,
        type(reference),
        print_format=custom_format,
        object_info=object_info)
    object_info = BigqueryClient.FormatInfoByType(object_info, type(reference))
    if object_info:
      formatter.AddDict(object_info)
    if reference.typename:
      print '%s %s\n' % (reference.typename.capitalize(), reference)
    formatter.Print()
    print
    if isinstance(reference, JobReference):
      _PrintJobMessages(object_info)
  else:
    formatter = _GetFormatterFromFlags()
    formatter.AddColumns(object_info.keys())
    formatter.AddDict(object_info)
    formatter.Print()


def _PrintObjectsArray(object_infos, objects_type):
  if FLAGS.format in ['prettyjson', 'json']:
    _PrintFormattedJsonObject(object_infos)
  elif FLAGS.format in [None, 'sparse', 'pretty']:
    if not object_infos:
      return
    formatter = _GetFormatterFromFlags()
    BigqueryClient.ConfigureFormatter(
        formatter, objects_type, print_format='list')
    formatted_infos = map(
        functools.partial(
            BigqueryClient.FormatInfoByType, object_type=objects_type),
        object_infos)
    for info in formatted_infos:
      formatter.AddDict(info)
    formatter.Print()
  elif object_infos:
    formatter = _GetFormatterFromFlags()
    formatter.AddColumns(object_infos[0].keys())
    for info in object_infos:
      formatter.AddDict(info)
    formatter.Print()


def _PrintObjectsArrayWithToken(object_infos, objects_type):
  if FLAGS.format in ['prettyjson', 'json']:
    _PrintFormattedJsonObject(object_infos)
  elif FLAGS.format in [None, 'sparse', 'pretty']:
    _PrintObjectsArray(object_infos['list'], objects_type)




class _Cancel(BigqueryCmd):
  """Attempt to cancel the specified job if it is running."""
  usage = """cancel [--nosync] [<job_id>]"""

  def __init__(self, name, fv):
    super(_Cancel, self).__init__(name, fv)
    self._ProcessCommandRc(fv)

  def RunWithArgs(self, job_id=''):
    # pylint: disable=g-doc-exception
    """Request a cancel and waits for the job to be cancelled.

    Requests a cancel and then either:
    a) waits until the job is done if the sync flag is set [default], or
    b) returns immediately if the sync flag is not set.
    Not all job types support a cancel, an error is returned if it cannot be
    cancelled. Even for jobs that support a cancel, success is not guaranteed,
    the job may have completed by the time the cancel request is noticed, or
    the job may be in a stage where it cannot be cancelled.

    Examples:
      bq cancel job_id  # Requests a cancel and waits until the job is done.
      bq --nosync cancel job_id  # Requests a cancel and returns immediately.

    Arguments:
      job_id: Job ID to cancel.
    """
    client = Client.Get()
    job_reference_dict = dict(client.GetJobReference(job_id, FLAGS.location))
    job = client.CancelJob(
        job_id=job_reference_dict['jobId'],
        location=job_reference_dict['location'])
    _PrintObjectInfo(
        job, JobReference.Create(**job['jobReference']), custom_format='show')
    status = job['status']
    if status['state'] == 'DONE':
      if ('errorResult' in status and 'reason' in status['errorResult'] and
          status['errorResult']['reason'] == 'stopped'):
        print 'Job has been cancelled successfully.'
      else:
        print 'Job completed before it could be cancelled.'
    else:
      print 'Job cancel has been requested.'
    return 0


class _Head(BigqueryCmd):
  usage = """head [-n <max rows>] [-j] [-t] <identifier>"""

  def __init__(self, name, fv):
    super(_Head, self).__init__(name, fv)
    flags.DEFINE_boolean(
        'job',
        False,
        'Reads the results of a query job.',
        short_name='j',
        flag_values=fv)
    flags.DEFINE_boolean(
        'table',
        False,
        'Reads rows from a table.',
        short_name='t',
        flag_values=fv)
    flags.DEFINE_integer(
        'start_row',
        0,
        'The number of rows to skip before showing table data.',
        short_name='s',
        flag_values=fv)
    flags.DEFINE_integer(
        'max_rows',
        100,
        'The number of rows to print when showing table data.',
        short_name='n',
        flag_values=fv)
    flags.DEFINE_string(
        'selected_fields',
        None,
        'A subset of fields (including nested fields) to return when showing '
        'table data. If not specified, full row will be retrieved. '
        'For example, "-c:a,b".',
        short_name='c',
        flag_values=fv)
    self._ProcessCommandRc(fv)

  def RunWithArgs(self, identifier=''):
    # pylint: disable=g-doc-exception
    """Displays rows in a table.

    Examples:
      bq head dataset.table
      bq head -j job
      bq head -n 10 dataset.table
      bq head -s 5 -n 10 dataset.table
    """
    client = Client.Get()
    if self.j and self.t:
      raise app.UsageError('Cannot specify both -j and -t.')

    if self.j:
      reference = client.GetJobReference(identifier, FLAGS.location)
    else:
      reference = client.GetTableReference(identifier)

    if isinstance(reference, JobReference):
      fields, rows = client.ReadSchemaAndJobRows(
          dict(reference), start_row=self.s, max_rows=self.n)
    elif isinstance(reference, TableReference):
      fields, rows = client.ReadSchemaAndRows(
          dict(reference),
          start_row=self.s,
          max_rows=self.n,
          selected_fields=self.c)
    else:
      raise app.UsageError("Invalid identifier '%s' for head." % (identifier,))

    Factory.ClientTablePrinter.GetTablePrinter().PrintTable(fields, rows)


class _Insert(BigqueryCmd):
  usage = """insert [-s] [-i] [-x=<suffix>] <table identifier> [file]"""

  def __init__(self, name, fv):
    super(_Insert, self).__init__(name, fv)
    flags.DEFINE_boolean(
        'skip_invalid_rows',
        None,
        'Attempt to insert any valid rows, even if invalid rows are present.',
        short_name='s',
        flag_values=fv)
    flags.DEFINE_boolean(
        'ignore_unknown_values',
        None,
        'Ignore any values in a row that are not present in the schema.',
        short_name='i',
        flag_values=fv)
    flags.DEFINE_string(
        'template_suffix',
        None,
        'If specified, treats the destination table as a base template, and '
        'inserts the rows into an instance table named '
        '"{destination}{templateSuffix}". BigQuery will manage creation of the '
        'instance table, using the schema of the base template table.',
        short_name='x',
        flag_values=fv)
    self._ProcessCommandRc(fv)

  def RunWithArgs(self, identifier='', filename=None):
    """Inserts rows in a table.

    Inserts the records formatted as newline delimited JSON from file
    into the specified table. If file is not specified, reads from stdin.
    If there were any insert errors it prints the errors to stdout.

    Examples:
      bq insert dataset.table /tmp/mydata.json
      echo '{"a":1, "b":2}' | bq insert dataset.table

    Template table examples:
    Insert to dataset.template_suffix table using dataset.template table as
    its template.
      bq insert -x=_suffix dataset.table /tmp/mydata.json
    """
    if filename:
      with open(filename, 'r') as json_file:
        return self._DoInsert(
            identifier,
            json_file,
            skip_invalid_rows=self.skip_invalid_rows,
            ignore_unknown_values=self.ignore_unknown_values,
            template_suffix=self.template_suffix)
    else:
      return self._DoInsert(
          identifier,
          sys.stdin,
          skip_invalid_rows=self.skip_invalid_rows,
          ignore_unknown_values=self.ignore_unknown_values,
          template_suffix=self.template_suffix)

  def _DoInsert(self,
                identifier,
                json_file,
                skip_invalid_rows=None,
                ignore_unknown_values=None,
                template_suffix=None):
    """Insert the contents of the file into a table."""
    client = Client.Get()
    reference = client.GetReference(identifier)
    _Typecheck(reference, (TableReference,),
               'Must provide a table identifier for insert.')
    reference = dict(reference)
    batch = []

    def Flush():
      result = client.InsertTableRows(
          reference,
          batch,
          skip_invalid_rows=skip_invalid_rows,
          ignore_unknown_values=ignore_unknown_values,
          template_suffix=template_suffix)
      del batch[:]
      return result, result.get('insertErrors', None)

    result = {}
    errors = None
    lineno = 1
    for line in json_file:
      try:
        batch.append(bigquery_client.JsonToInsertEntry(None, line))
        lineno += 1
      except bigquery_client.BigqueryClientError as e:
        raise app.UsageError('Line %d: %s' % (lineno, str(e)))
      if (FLAGS.max_rows_per_request and
          len(batch) == FLAGS.max_rows_per_request):
        result, errors = Flush()
      if errors:
        break
    if batch and not errors:
      result, errors = Flush()

    if FLAGS.format in ['prettyjson', 'json']:
      _PrintFormattedJsonObject(result)
    elif FLAGS.format in [None, 'sparse', 'pretty']:
      if errors:
        for entry in result['insertErrors']:
          entry_errors = entry['errors']
          sys.stdout.write('record %d errors: ' % (entry['index'],))
          for error in entry_errors:
            print '\t%s: %s' % (error['reason'], error.get('message'))
    return 1 if errors else 0


class _Wait(BigqueryCmd):  # pylint: disable=missing-docstring
  usage = """wait [<job_id>] [<secs>]"""

  def __init__(self, name, fv):
    super(_Wait, self).__init__(name, fv)
    flags.DEFINE_boolean(
        'fail_on_error',
        True,
        'When done waiting for the job, exit the process with an error '
        'if the job is still running, or ended with a failure.',
        flag_values=fv)
    flags.DEFINE_string(
        'wait_for_status',
        'DONE',
        'Wait for the job to have a certain status. Default is DONE.',
        flag_values=fv)
    self._ProcessCommandRc(fv)

  def RunWithArgs(self, job_id='', secs=sys.maxint):
    # pylint: disable=g-doc-exception
    """Wait some number of seconds for a job to finish.

    Poll job_id until either (1) the job is DONE or (2) the
    specified number of seconds have elapsed. Waits forever
    if unspecified. If no job_id is specified, and there is
    only one running job, we poll that job.

    Examples:
      bq wait # Waits forever for the currently running job.
      bq wait job_id  # Waits forever
      bq wait job_id 100  # Waits 100 seconds
      bq wait job_id 0  # Polls if a job is done, then returns immediately.
      # These may exit with a non-zero status code to indicate "failure":
      bq wait --fail_on_error job_id  # Succeeds if job succeeds.
      bq wait --fail_on_error job_id 100  # Succeeds if job succeeds in 100 sec.

    Arguments:
      job_id: Job ID to wait on.
      secs: Number of seconds to wait (must be >= 0).
    """
    try:
      secs = BigqueryClient.NormalizeWait(secs)
    except ValueError:
      raise app.UsageError('Invalid wait time: %s' % (secs,))

    client = Client.Get()
    if not job_id:
      running_jobs = client.ListJobRefs(state_filter=['PENDING', 'RUNNING'])
      if len(running_jobs) != 1:
        raise bigquery_client.BigqueryError(
            'No job_id provided, found %d running jobs' % (len(running_jobs),))
      job_reference = running_jobs.pop()
    else:
      job_reference = client.GetJobReference(job_id, FLAGS.location)
    try:
      job = client.WaitJob(
          job_reference=job_reference, wait=secs, status=FLAGS.wait_for_status)
      _PrintObjectInfo(
          job, JobReference.Create(**job['jobReference']), custom_format='show')
      return 1 if self.fail_on_error and BigqueryClient.IsFailedJob(job) else 0
    except StopIteration as e:
      print
      print e
    # If we reach this point, we have not seen the job succeed.
    return 1 if self.fail_on_error else 0


# pylint: disable=g-bad-name
class CommandLoop(cmd.Cmd):
  """Instance of cmd.Cmd built to work with NewCmd."""

  class TerminateSignal(Exception):
    """Exception type used for signaling loop completion."""
    pass

  def __init__(self, commands, prompt=None):
    cmd.Cmd.__init__(self)
    self._commands = {'help': commands['help']}
    self._special_command_names = ['help', 'repl', 'EOF']
    for name, command in commands.iteritems():
      if (name not in self._special_command_names and
          isinstance(command, NewCmd) and command.surface_in_shell):
        self._commands[name] = command
        setattr(self, 'do_%s' % (name,), command.RunCmdLoop)
    self._default_prompt = prompt or 'BigQuery> '
    self._set_prompt()
    self._last_return_code = 0

  @property
  def last_return_code(self):
    return self._last_return_code

  def _set_prompt(self):
    client = Client().Get()
    if client.project_id:
      path = str(client.GetReference())
      self.prompt = '%s> ' % (path,)
    else:
      self.prompt = self._default_prompt

  def do_EOF(self, *unused_args):
    """Terminate the running command loop.

    This function raises an exception to avoid the need to do
    potentially-error-prone string parsing inside onecmd.

    Returns:
      Never returns.

    Raises:
      CommandLoop.TerminateSignal: always.
    """
    raise CommandLoop.TerminateSignal()

  def postloop(self):
    print 'Goodbye.'

  def completedefault(self, unused_text, line, unused_begidx, unused_endidx):
    if not line:
      return []
    else:
      command_name = line.partition(' ')[0].lower()
      usage = ''
      if command_name in self._commands:
        usage = self._commands[command_name].usage
      elif command_name == 'set':
        usage = 'set (project_id|dataset_id) <name>'
      elif command_name == 'unset':
        usage = 'unset (project_id|dataset_id)'
      if usage:
        print
        print usage
        print '%s%s' % (self.prompt, line),
      return []

  def emptyline(self):
    print 'Available commands:',
    print ' '.join(list(self._commands))

  def precmd(self, line):
    """Preprocess the shell input."""
    if line == 'EOF':
      return line
    if line.startswith('exit') or line.startswith('quit'):
      return 'EOF'
    words = line.strip().split()
    if len(words) > 1 and words[0].lower() == 'select':
      return 'query %s' % (pipes.quote(line),)
    if len(words) == 1 and words[0] not in ['help', 'ls', 'version']:
      return 'help %s' % (line.strip(),)
    return line

  def onecmd(self, line):
    """Process a single command.

    Runs a single command, and stores the return code in
    self._last_return_code. Always returns False unless the command
    was EOF.

    Args:
      line: (str) Command line to process.

    Returns:
      A bool signaling whether or not the command loop should terminate.
    """
    try:
      self._last_return_code = cmd.Cmd.onecmd(self, line)
    except CommandLoop.TerminateSignal:
      return True
    except BaseException as e:
      name = line.split(' ')[0]
      BigqueryCmd.ProcessError(e, name=name)
      self._last_return_code = 1
    return False

  def get_names(self):
    names = dir(self)
    commands = (
        name for name in self._commands
        if name not in self._special_command_names)
    names.extend('do_%s' % (name,) for name in commands)
    names.append('do_select')
    names.remove('do_EOF')
    return names

  def do_set(self, line):
    """Set the value of the project_id or dataset_id flag."""
    client = Client().Get()
    name, value = (line.split(' ') + ['', ''])[:2]
    if (name not in ('project_id', 'dataset_id') or
        not 1 <= len(line.split(' ')) <= 2):
      print 'set (project_id|dataset_id) <name>'
    elif name == 'dataset_id' and not client.project_id:
      print 'Cannot set dataset_id with project_id unset'
    else:
      setattr(client, name, value)
      self._set_prompt()
    return 0

  def do_unset(self, line):
    """Unset the value of the project_id or dataset_id flag."""
    name = line.strip()
    client = Client.Get()
    if name not in ('project_id', 'dataset_id'):
      print 'unset (project_id|dataset_id)'
    else:
      setattr(client, name, '')
      if name == 'project_id':
        client.dataset_id = ''
      self._set_prompt()
    return 0

  def do_help(self, command_name):
    """Print the help for command_name (if present) or general help."""

    # TODO(user): Add command-specific flags.
    def FormatOneCmd(name, command, command_names):
      indent_size = appcommands.GetMaxCommandLength() + 3
      if len(command_names) > 1:
        indent = ' ' * indent_size
        command_help = flags.TextWrap(
            command.CommandGetHelp('', cmd_names=command_names),
            indent=indent,
            firstline_indent='')
        first_help_line, _, rest = command_help.partition('\n')
        first_line = '%-*s%s' % (indent_size, name + ':', first_help_line)
        return '\n'.join((first_line, rest))
      else:
        default_indent = '  '
        return '\n' + flags.TextWrap(
            command.CommandGetHelp('', cmd_names=command_names),
            indent=default_indent,
            firstline_indent=default_indent) + '\n'

    if not command_name:
      print '\nHelp for Bigquery commands:\n'
      command_names = list(self._commands)
      print '\n\n'.join(
          FormatOneCmd(name, command, command_names)
          for name, command in self._commands.iteritems()
          if name not in self._special_command_names)
      print
    elif command_name in self._commands:
      print FormatOneCmd(
          command_name,
          self._commands[command_name],
          command_names=[command_name])
    return 0

  def postcmd(self, stop, line):
    return bool(stop) or line == 'EOF'


# pylint: enable=g-bad-name


class _Repl(BigqueryCmd):
  """Start an interactive bq session."""

  def __init__(self, name, fv):
    super(_Repl, self).__init__(name, fv)
    self.surface_in_shell = False
    flags.DEFINE_string(
        'prompt', '', 'Prompt to use for BigQuery shell.', flag_values=fv)
    self._ProcessCommandRc(fv)

  def RunWithArgs(self):
    """Start an interactive bq session."""
    repl = CommandLoop(appcommands.GetCommandList(), prompt=self.prompt)
    print 'Welcome to BigQuery! (Type help for more information.)'
    while True:
      try:
        repl.cmdloop()
        break
      except KeyboardInterrupt:
        print
    return repl.last_return_code


class _Init(BigqueryCmd):
  """Create a .bigqueryrc file and set up OAuth credentials."""

  def __init__(self, name, fv):
    super(_Init, self).__init__(name, fv)
    self.surface_in_shell = False
    flags.DEFINE_boolean(
        'delete_credentials',
        False,
        'If specified, the credentials file associated with this .bigqueryrc '
        'file is deleted.',
        flag_values=fv)

  def _NeedsInit(self):
    """Init never needs to call itself before running."""
    return False

  def DeleteCredentials(self):
    """Deletes this user's credential file."""
    bq_utils.ProcessBigqueryrc()
    filename = FLAGS.service_account_credential_file or FLAGS.credential_file
    if not os.path.exists(filename):
      print 'Credential file %s does not exist.' % (filename,)
      return 0
    try:
      if 'y' != _PromptYN('Delete credential file %s? (y/N) ' % (filename,)):
        print 'NOT deleting %s, exiting.' % (filename,)
        return 0
      os.remove(filename)
    except OSError as e:
      print 'Error removing %s: %s' % (filename, e)
      return 1

  def RunWithArgs(self):
    """Authenticate and create a default .bigqueryrc file."""
    bq_utils.ProcessBigqueryrc()
    _ConfigureLogging(bigquery_client)
    if self.delete_credentials:
      return self.DeleteCredentials()
    bigqueryrc = bq_utils.GetBigqueryRcFilename()
    # Delete the old one, if it exists.
    print
    print 'Welcome to BigQuery! This script will walk you through the '
    print 'process of initializing your .bigqueryrc configuration file.'
    print
    if os.path.exists(bigqueryrc):
      print ' **** NOTE! ****'
      print 'An existing .bigqueryrc file was found at %s.' % (bigqueryrc,)
      print 'Are you sure you want to continue and overwrite your existing '
      print 'configuration?'
      print

      if 'y' != _PromptYN('Overwrite %s? (y/N) ' % (bigqueryrc,)):
        print 'NOT overwriting %s, exiting.' % (bigqueryrc,)
        return 0
      print
      try:
        os.remove(bigqueryrc)
      except OSError as e:
        print 'Error removing %s: %s' % (bigqueryrc, e)
        return 1

    print 'First, we need to set up your credentials if they do not '
    print 'already exist.'
    print

    client = Client.Get()
    entries = {'credential_file': FLAGS.credential_file}
    projects = client.ListProjects(max_results=1000)
    print 'Credential creation complete. Now we will select a default project.'
    print
    if not projects:
      print 'No projects found for this user. Please go to '
      print '  https://code.google.com/apis/console'
      print 'and create a project.'
      print
    else:
      print 'List of projects:'
      formatter = _GetFormatterFromFlags()
      formatter.AddColumn('#')
      BigqueryClient.ConfigureFormatter(formatter, ProjectReference)
      for index, project in enumerate(projects):
        result = BigqueryClient.FormatProjectInfo(project)
        result.update({'#': index + 1})
        formatter.AddDict(result)
      formatter.Print()

      if len(projects) == 1:
        project_reference = BigqueryClient.ConstructObjectReference(projects[0])
        print 'Found only one project, setting %s as the default.' % (
            project_reference,)
        print
        entries['project_id'] = project_reference.projectId
      else:
        print 'Found multiple projects. Please enter a selection for '
        print 'which should be the default, or leave blank to not '
        print 'set a default.'
        print

        response = None
        while not isinstance(response, int):
          response = _PromptWithDefault(
              'Enter a selection (1 - %s): ' % (len(projects),))
          try:
            if not response or 1 <= int(response) <= len(projects):
              response = int(response or 0)
          except ValueError:
            pass
        print
        if response:
          project_reference = BigqueryClient.ConstructObjectReference(
              projects[response - 1])
          entries['project_id'] = project_reference.projectId

    try:
      with open(bigqueryrc, 'w') as rcfile:
        for flag, value in entries.iteritems():
          print >> rcfile, '%s = %s' % (flag, value)
    except IOError as e:
      print 'Error writing %s: %s' % (bigqueryrc, e)
      return 1

    print 'BigQuery configuration complete! Type "bq" to get started.'
    print
    bq_utils.ProcessBigqueryrc()
    # Destroy the client we created, so that any new client will
    # pick up new flag values.
    Client.Delete()
    return 0


class _Version(BigqueryCmd):
  usage = """version"""

  def _NeedsInit(self):
    """If just printing the version, don't run `init` first."""
    return False

  def RunWithArgs(self):
    """Return the version of bq."""
    print 'This is BigQuery CLI %s' % (_VERSION_NUMBER,)


def _ParseUdfResources(udf_resources):

  if udf_resources is None:
    return None
  inline_udf_resources = []
  external_udf_resources = []
  for uris in udf_resources:
    for uri in uris.split(','):
      if os.path.isfile(uri):
        with open(uri) as udf_file:
          inline_udf_resources.append(udf_file.read())
      else:
        if not uri.startswith('gs://'):
          raise app.UsageError(
              'Non-inline resources must be Google Cloud Storage '
              '(gs://) URIs')
        external_udf_resources.append(uri)
  udfs = []
  if inline_udf_resources:
    for udf_code in inline_udf_resources:
      udfs.append({'inlineCode': udf_code})
  if external_udf_resources:
    for uri in external_udf_resources:
      udfs.append({'resourceUri': uri})
  return udfs


def _ParseParameters(parameters):
  if not parameters:
    return None
  results = []
  for param_string in parameters:
    if os.path.isfile(param_string):
      with open(param_string) as f:
        results += json.load(f)
    else:
      results.append(_ParseParameter(param_string))
  return results


def _ParseParameter(param_string):
  name, param_string = param_string.split(':', 1)
  type_dict, value_dict = _ParseParameterTypeAndValue(param_string)
  result = {'parameterType': type_dict, 'parameterValue': value_dict}
  if name:
    result['name'] = name
  return result


def _ParseParameterTypeAndValue(param_string):
  """Parse a string of the form <recursive_type>:<value> into each part."""
  type_string, value_string = param_string.split(':', 1)
  if not type_string:
    type_string = 'STRING'
  type_dict = _ParseParameterType(type_string)
  return type_dict, _ParseParameterValue(type_dict, value_string)


def _ParseParameterType(type_string):
  """Parse a parameter type string into a JSON dict for the BigQuery API."""
  type_dict = {'type': type_string.upper()}
  if type_string.upper().startswith('ARRAY<') and type_string.endswith('>'):
    type_dict = {
        'type': 'ARRAY',
        'arrayType': _ParseParameterType(type_string[6:-1])
    }
  if type_string.startswith('STRUCT<') and type_string.endswith('>'):
    type_dict = {
        'type': 'STRUCT',
        'structTypes': _ParseStructType(type_string[7:-1])
    }
  if not type_string:
    raise app.UsageError('Query parameter missing type')
  return type_dict


def _ParseStructType(type_string):
  """Parse a Struct QueryParameter type into a JSON dict form."""
  subtypes = []
  for name, sub_type in _StructTypeSplit(type_string):
    subtypes.append({'type': _ParseParameterType(sub_type), 'name': name})
  return subtypes


def _StructTypeSplit(type_string):
  """Yields single field-name, sub-types tuple from a StructType string.

  Raises:
    UsageError: When a field name is missing.
  """
  while type_string:
    next_span = type_string.split(',', 1)[0]
    if '<' in next_span:
      angle_count = 0
      i = 0
      for i in xrange(next_span.find('<'), len(type_string)):
        if type_string[i] == '<':
          angle_count += 1
        if type_string[i] == '>':
          angle_count -= 1
        if angle_count == 0:
          break
      if angle_count != 0:
        raise app.UsageError('Malformatted struct type')
      next_span = type_string[:i + 1]
    type_string = type_string[len(next_span) + 1:]
    splits = next_span.split(None, 1)
    if len(splits) != 2:
      raise app.UsageError('Struct parameter missing name for field')
    yield splits


def _ParseParameterValue(type_dict, value_input):
  """Parse a parameter value of type `type_dict` from value_input.

  Arguments:
    type_dict: The JSON-dict type as which to parse `value_input`.
    value_input: Either a string representing the value, or a JSON dict for
        array and value types.
  """
  if 'structTypes' in type_dict:
    if isinstance(value_input, str):
      if value_input == 'NULL':
        return {'structValues': None}
      value_input = json.loads(value_input)
    type_map = dict([(x['name'], x['type']) for x in type_dict['structTypes']])
    values = {}
    for (field_name, value) in value_input.iteritems():
      values[field_name] = _ParseParameterValue(type_map[field_name], value)
    return {'structValues': values}
  if 'arrayType' in type_dict:
    if isinstance(value_input, str):
      if value_input == 'NULL':
        return {'arrayValues': None}
      value_input = json.loads(value_input)
    values = [
        _ParseParameterValue(type_dict['arrayType'], x) for x in value_input
    ]
    return {'arrayValues': values}
  return {'value': value_input if value_input != 'NULL' else None}


def main(unused_argv):
  # Avoid using global flags in main(). In this command line:
  # bq <global flags> <command> <global and local flags> <command args>,
  # only "<global flags>" will parse before main, not "<global and local flags>"
  try:
    _ValidateGlobalFlags()

    bq_commands = {
        # Keep the commands alphabetical.
        'cancel': _Cancel,
        'cp': _Copy,
        'extract': _Extract,
        'head': _Head,
        'init': _Init,
        'insert': _Insert,
        'load': _Load,
        'ls': _List,
        'mk': _Make,
        'mkdef': _MakeExternalTableDefinition,
        'partition': _Partition,
        'query': _Query,
        'rm': _Delete,
        'shell': _Repl,
        'show': _Show,
        'update': _Update,
        'version': _Version,
        'wait': _Wait,
    }

    for command, function in bq_commands.iteritems():
      if command not in appcommands.GetCommandList():
        appcommands.AddCmd(command, function)

  except KeyboardInterrupt as e:
    print 'Control-C pressed, exiting.'
    sys.exit(1)
  except BaseException as e:  # pylint: disable=broad-except
    print 'Error initializing bq client: %s' % (e,)
    # Use global flags if they're available, but we're exitting so we can't
    # count on global flag parsing anyways.
    if FLAGS.debug_mode or FLAGS.headless:
      traceback.print_exc()
      if not FLAGS.headless:
        pdb.post_mortem()
    sys.exit(1)




# pylint: disable=g-bad-name
def run_main():
  """Function to be used as setuptools script entry point.

  Appcommands assumes that it always runs as __main__, but launching
  via a setuptools-generated entry_point breaks this rule. We do some
  trickery here to make sure that appcommands and flags find their
  state where they expect to by faking ourselves as __main__.
  """

  # Put the flags for this module somewhere the flags module will look
  # for them.
  new_name = sys.argv[0]
  sys.modules[new_name] = sys.modules['__main__']
  # pylint: disable=protected-access
  for flag in FLAGS.FlagsByModuleDict().get(__name__, []):
    FLAGS._RegisterFlagByModule(new_name, flag)
    for key_flag in FLAGS.KeyFlagsByModuleDict().get(__name__, []):
      FLAGS._RegisterKeyFlagForModule(new_name, key_flag)
  # pylint: enable=protected-access

  # Now set __main__ appropriately so that appcommands will be
  # happy.
  sys.modules['__main__'] = sys.modules[__name__]
  appcommands.Run()
  sys.modules['__main__'] = sys.modules.pop(new_name)


if __name__ == '__main__':
  appcommands.Run()
