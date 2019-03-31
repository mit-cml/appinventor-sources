# -*- coding: utf-8 -*- #
# Copyright 2013 Google Inc. All Rights Reserved.
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

"""Config for Google Cloud Platform CLIs."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import json
import os
import time

import googlecloudsdk
from googlecloudsdk.core import exceptions
from googlecloudsdk.core.util import encoding
from googlecloudsdk.core.util import files as file_utils
from googlecloudsdk.core.util import pkg_resources
from googlecloudsdk.core.util import platforms


class Error(exceptions.Error):
  """Exceptions for the cli module."""

# Environment variable for the directory containing Cloud SDK configuration.
CLOUDSDK_CONFIG = 'CLOUDSDK_CONFIG'

# Environment variable for overriding the Cloud SDK active named config
CLOUDSDK_ACTIVE_CONFIG_NAME = 'CLOUDSDK_ACTIVE_CONFIG_NAME'


class InstallationConfig(object):
  """Loads configuration constants from the core config file.

  Attributes:
    version: str, The version of the core component.
    revision: long, A revision number from a component snapshot.  This is a
      long int but formatted as an actual date in seconds (i.e 20151009132504).
      It is *NOT* seconds since the epoch.
    user_agent: str, The base string of the user agent to use when making API
      calls.
    documentation_url: str, The URL where we can redirect people when they need
      more information.
    release_notes_url: str, The URL where we host a nice looking version of our
      release notes.
    snapshot_url: str, The url for the component manager to look at for
      updates.
    disable_updater: bool, True to disable the component manager for this
      installation.  We do this for distributions through another type of
      package manager like apt-get.
    disable_usage_reporting: bool, True to disable the sending of usage data by
      default.
    snapshot_schema_version: int, The version of the snapshot schema this code
      understands.
    release_channel: str, The release channel for this Cloud SDK distribution.
    config_suffix: str, A string to add to the end of the configuration
      directory name so that different release channels can have separate
      config.
  """

  REVISION_FORMAT_STRING = '%Y%m%d%H%M%S'

  @staticmethod
  def Load():
    """Initializes the object with values from the config file.

    Returns:
      InstallationSpecificData: The loaded data.
    """
    data = json.loads(
        encoding.Decode(pkg_resources.GetResource(__name__, 'config.json')))
    return InstallationConfig(**data)

  @staticmethod
  def FormatRevision(time_struct):
    """Formats a given time as a revision string for a component snapshot.

    Args:
      time_struct: time.struct_time, The time you want to format.

    Returns:
      int, A revision number from a component snapshot.  This is a int but
      formatted as an actual date in seconds (i.e 20151009132504).  It is *NOT*
      seconds since the epoch.
    """
    return int(time.strftime(
        InstallationConfig.REVISION_FORMAT_STRING, time_struct))  # pytype: disable=wrong-arg-types

  @staticmethod
  def ParseRevision(revision):
    """Parse the given revision into a time.struct_time.

    Args:
      revision: long, A revision number from a component snapshot.  This is a
        long int but formatted as an actual date in seconds
        (i.e 20151009132504). It is *NOT* seconds since the epoch.

    Returns:
      time.struct_time, The parsed time.
    """
    return time.strptime(str(revision),
                         InstallationConfig.REVISION_FORMAT_STRING)  # pytype: disable=wrong-arg-types

  @staticmethod
  def ParseRevisionAsSeconds(revision):
    """Parse the given revision into seconds since the epoch.

    Args:
      revision: long, A revision number from a component snapshot.  This is a
        long int but formatted as an actual date in seconds
        (i.e 20151009132504). It is *NOT* seconds since the epoch.

    Returns:
      int, The number of seconds since the epoch that this revision represents.
    """
    return time.mktime(InstallationConfig.ParseRevision(revision))

  def __init__(self, version, revision, user_agent, documentation_url,
               release_notes_url, snapshot_url, disable_updater,
               disable_usage_reporting, snapshot_schema_version,
               release_channel, config_suffix):
    # JSON returns all unicode.  We know these are regular strings and using
    # unicode in environment variables on Windows doesn't work.
    self.version = str(version)
    self.revision = revision
    self.user_agent = str(user_agent)
    self.documentation_url = str(documentation_url)
    self.release_notes_url = str(release_notes_url)
    self.snapshot_url = str(snapshot_url)
    self.disable_updater = disable_updater
    self.disable_usage_reporting = disable_usage_reporting
    # This one is an int, no need to convert
    self.snapshot_schema_version = snapshot_schema_version
    self.release_channel = str(release_channel)
    self.config_suffix = str(config_suffix)

  def IsAlternateReleaseChannel(self):
    """Determines if this distribution is using an alternate release channel.

    Returns:
      True if this distribution is not one of the 'stable' release channels,
      False otherwise.
    """
    return self.release_channel != 'rapid'


INSTALLATION_CONFIG = InstallationConfig.Load()

CLOUD_SDK_VERSION = INSTALLATION_CONFIG.version
# TODO(b/35848109): Distribute a clientsecrets.json to avoid putting it in code.
CLOUDSDK_CLIENT_ID = '32555940559.apps.googleusercontent.com'
CLOUDSDK_CLIENT_NOTSOSECRET = 'ZmssLNjJy2998hD4CTg2ejr2'

CLOUDSDK_USER_AGENT = INSTALLATION_CONFIG.user_agent

# Do not add more scopes here.
CLOUDSDK_SCOPES = (
    'https://www.googleapis.com/auth/userinfo.email',
    'https://www.googleapis.com/auth/cloud-platform',
    # TODO(b/19019218): remove the following now that 'cloud-platform'
    # is sufficient.
    'https://www.googleapis.com/auth/appengine.admin',
    'https://www.googleapis.com/auth/compute',  # needed by autoscaler
)

REAUTH_SCOPE = 'https://www.googleapis.com/auth/accounts.reauth'


def EnsureSDKWriteAccess(sdk_root_override=None):
  """Error if the current user does not have write access to the sdk root.

  Args:
    sdk_root_override: str, The full path to the sdk root to use instead of
      using config.Paths().sdk_root.

  Raises:
    exceptions.RequiresAdminRightsError: If the sdk root is defined and the user
      does not have write access.
  """
  sdk_root = sdk_root_override or Paths().sdk_root
  if sdk_root and not file_utils.HasWriteAccessInDir(sdk_root):
    raise exceptions.RequiresAdminRightsError(sdk_root)


# Doesn't work in par or stub files.
def GcloudPath():
  """Gets the path the main gcloud entrypoint.

  Returns:
    str: The path to gcloud.py
  """
  return os.path.join(
      os.path.dirname(os.path.dirname(googlecloudsdk.__file__)), 'gcloud.py')


_CLOUDSDK_GLOBAL_CONFIG_DIR_NAME = ('gcloud' +
                                    INSTALLATION_CONFIG.config_suffix)


def _GetGlobalConfigDir():
  """Returns the path to the user's global config area.

  Returns:
    str: The path to the user's global config area.
  """
  # Name of the directory that roots a cloud SDK workspace.
  global_config_dir = encoding.GetEncodedValue(os.environ, CLOUDSDK_CONFIG)
  if global_config_dir:
    return global_config_dir
  if platforms.OperatingSystem.Current() != platforms.OperatingSystem.WINDOWS:
    return os.path.join(file_utils.GetHomeDir(), '.config',
                        _CLOUDSDK_GLOBAL_CONFIG_DIR_NAME)
  appdata = encoding.GetEncodedValue(os.environ, 'APPDATA')
  if appdata:
    return os.path.join(appdata, _CLOUDSDK_GLOBAL_CONFIG_DIR_NAME)
  # This shouldn't happen unless someone is really messing with things.
  drive = encoding.GetEncodedValue(os.environ, 'SystemDrive', 'C:')
  return os.path.join(drive, os.path.sep, _CLOUDSDK_GLOBAL_CONFIG_DIR_NAME)


class Paths(object):
  """Class to encapsulate the various directory paths of the Cloud SDK.

  Attributes:
    global_config_dir: str, The path to the user's global config area.
    workspace_dir: str, The path of the current workspace or None if not in a
      workspace.
    workspace_config_dir: str, The path to the config directory under the
      current workspace, or None if not in a workspace.
  """
  CLOUDSDK_STATE_DIR = '.install'
  CLOUDSDK_PROPERTIES_NAME = 'properties'

  def __init__(self):
    self.global_config_dir = _GetGlobalConfigDir()

  @property
  def sdk_root(self):
    """Searches for the Cloud SDK root directory.

    Returns:
      str, The path to the root of the Cloud SDK or None if it could not be
      found.
    """
    return file_utils.FindDirectoryContaining(
        os.path.dirname(encoding.Decode(__file__)),
        Paths.CLOUDSDK_STATE_DIR)

  @property
  def sdk_bin_path(self):
    """Forms a path to bin directory by using sdk_root.

    Returns:
      str, The path to the bin directory of the Cloud SDK or None if it could
      not be found.
    """
    sdk_root = self.sdk_root
    return os.path.join(sdk_root, 'bin') if sdk_root else None

  @property
  def cache_dir(self):
    """Gets the dir path that will contain all cache objects."""
    return os.path.join(self.global_config_dir, 'cache')

  # TODO(b/36751527): drop this in 2017Q3
  @property
  def completion_cache_dir(self):
    """Gets the legacy completion cache dir path."""
    return os.path.join(self.global_config_dir, 'completion_cache')

  @property
  def credentials_path(self):
    """Gets the path to the file to store Oauth2Client credentials in.

    This is oauth2client.contrib.multistore_file format file.

    Returns:
      str, The path to the credential file.
    """
    return os.path.join(self.global_config_dir, 'credentials')

  @property
  def credentials_db_path(self):
    """Gets the path to the file to store credentials in.

    This is generic key/value store format using sqlite.

    Returns:
      str, The path to the credential db file.
    """
    return os.path.join(self.global_config_dir, 'credentials.db')

  @property
  def access_token_db_path(self):
    """Gets the path to the file to store cached access tokens in.

    This is generic key/value store format using sqlite.

    Returns:
      str, The path to the access token db file.
    """
    return os.path.join(self.global_config_dir, 'access_tokens.db')

  @property
  def logs_dir(self):
    """Gets the path to the directory to put logs in for calliope commands.

    Returns:
      str, The path to the directory to put logs in.
    """
    return os.path.join(self.global_config_dir, 'logs')

  @property
  def analytics_cid_path(self):
    """Gets the path to the file to store the client id for analytics.

    This is always stored in the global location because it is per install.

    Returns:
      str, The path to the file.
    """
    return os.path.join(self.global_config_dir, '.metricsUUID')

  @property
  def update_check_cache_path(self):
    """Gets the path to the file to cache information about update checks.

    This is stored in the config directory instead of the installation state
    because if the SDK is installed as root, it will fail to persist the cache
    when you are running gcloud as a normal user.

    Returns:
      str, The path to the file.
    """
    return os.path.join(self.global_config_dir, '.last_update_check.json')

  @property
  def survey_prompting_cache_path(self):
    """Gets the path to the file to cache information about survey prompting.

    This is stored in the config directory instead of the installation state
    because if the SDK is installed as root, it will fail to persist the cache
    when you are running gcloud as a normal user.

    Returns:
      str, The path to the file.
    """
    return os.path.join(self.global_config_dir, '.last_survey_prompt.yaml')

  @property
  def installation_properties_path(self):
    """Gets the path to the installation-wide properties file.

    Returns:
      str, The path to the file.
    """
    sdk_root = self.sdk_root
    if not sdk_root:
      return None
    return os.path.join(sdk_root, self.CLOUDSDK_PROPERTIES_NAME)

  @property
  def user_properties_path(self):
    """Gets the path to the properties file in the user's global config dir.

    Returns:
      str, The path to the file.
    """
    return os.path.join(self.global_config_dir, self.CLOUDSDK_PROPERTIES_NAME)

  @property
  def named_config_activator_path(self):
    """Gets the path to the file pointing at the user's active named config.

    This is the file that stores the name of the user's active named config,
    not the path to the configuration file itself.

    Returns:
      str, The path to the file.
    """
    return os.path.join(self.global_config_dir, 'active_config')

  @property
  def named_config_directory(self):
    """Gets the path to the directory that stores the named configs.

    Returns:
      str, The path to the directory.
    """
    return os.path.join(self.global_config_dir, 'configurations')

  @property
  def config_sentinel_file(self):
    """Gets the path to the config sentinel.

    The sentinel is a file that we touch any time there is a change to config.
    External tools can check this file to see if they need to re-query gcloud's
    credential/config helper to get updated configuration information. Nothing
    is ever written to this file, it's timestamp indicates the last time config
    was changed.

    This does not take into account config changes made through environment
    variables as they are transient by nature. There is also the edge case of
    when a user updated installation config. That user's sentinel will be
    updated but other will not be.

    Returns:
      str, The path to the sentinel file.
    """
    return os.path.join(self.global_config_dir, 'config_sentinel')

  @property
  def container_config_path(self):
    return os.path.join(self.global_config_dir, 'kubernetes')

  def LegacyCredentialsDir(self, account):
    """Gets the path to store legacy credentials in.

    Args:
      account: str, Email account tied to the authorizing credentials.

    Returns:
      str, The path to the credentials file.
    """
    if not account:
      account = 'default'
    return os.path.join(self.global_config_dir, 'legacy_credentials', account)

  def LegacyCredentialsBqPath(self, account):
    """Gets the path to store legacy bq credentials in.

    Args:
      account: str, Email account tied to the authorizing credentials.

    Returns:
      str, The path to the bq credentials file.
    """
    return os.path.join(self.LegacyCredentialsDir(account),
                        'singlestore_bq.json')

  def LegacyCredentialsGSUtilPath(self, account):
    """Gets the path to store legacy gsutil credentials in.

    Args:
      account: str, Email account tied to the authorizing credentials.

    Returns:
      str, The path to the gsutil credentials file.
    """
    return os.path.join(self.LegacyCredentialsDir(account), '.boto')

  def LegacyCredentialsP12KeyPath(self, account):
    """Gets the path to store legacy key file in.

    Args:
      account: str, Email account tied to the authorizing credentials.

    Returns:
      str, The path to the key file.
    """
    return os.path.join(self.LegacyCredentialsDir(account), 'private_key.p12')

  def LegacyCredentialsAdcPath(self, account):
    """Gets the file path to store application default credentials in.

    Args:
      account: str, Email account tied to the authorizing credentials.

    Returns:
      str, The path to the file.
    """
    return os.path.join(self.LegacyCredentialsDir(account), 'adc.json')

  def GCECachePath(self):
    """Get the path to cache whether or not we're on a GCE machine.

    Returns:
      str, The path to the GCE cache.
    """
    return os.path.join(self.global_config_dir, 'gce')
