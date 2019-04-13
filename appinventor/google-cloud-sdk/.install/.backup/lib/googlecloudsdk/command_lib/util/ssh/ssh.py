# -*- coding: utf-8 -*- #
# Copyright 2016 Google Inc. All Rights Reserved.
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

"""SSH client utilities for key-generation, dispatching the ssh commands etc."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import errno
import getpass
import os
import re
import enum

from googlecloudsdk.api_lib.oslogin import client as oslogin_client
from googlecloudsdk.command_lib.oslogin import oslogin_utils
from googlecloudsdk.command_lib.util import gaia
from googlecloudsdk.core import config
from googlecloudsdk.core import exceptions as core_exceptions
from googlecloudsdk.core import execution_utils
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.console import console_io
from googlecloudsdk.core.util import files
from googlecloudsdk.core.util import platforms
from googlecloudsdk.core.util import retry

import six


PER_USER_SSH_CONFIG_FILE = os.path.join('~', '.ssh', 'config')
OSLOGIN_ENABLE_METADATA_KEY = 'enable-oslogin'


class InvalidKeyError(core_exceptions.Error):
  """Indicates a key file was not found."""


class MissingCommandError(core_exceptions.Error):
  """Indicates that an external executable couldn't be found."""


class CommandError(core_exceptions.Error):
  """Raise for a failure when invoking ssh, scp, or similar."""

  def __init__(self, cmd, message=None, return_code=None):
    if not (message or return_code):
      raise ValueError('One of message or return_code is required.')

    self.cmd = cmd

    message_text = '[{0}]'.format(message) if message else None
    return_code_text = ('return code [{0}]'.format(return_code)
                        if return_code else None)
    why_failed = ' and '.join(
        [f for f in [message_text, return_code_text] if f])

    super(CommandError, self).__init__(
        '[{0}] exited with {1}.'.format(self.cmd, why_failed),
        exit_code=return_code)


class InvalidConfigurationError(core_exceptions.Error):
  """When arguments provided have misconfigured sources/destinations."""

  def __init__(self, msg, sources, destination):
    super(InvalidConfigurationError, self).__init__(
        msg + '  Got sources: {}, destination: {}'
        .format(sources, destination))


class Suite(enum.Enum):
  """Represents an SSH implementation suite."""
  OPENSSH = 'OpenSSH'
  PUTTY = 'PuTTY'


class Environment(object):
  """Environment maps SSH commands to executable location on file system.

    Recommended usage:

    env = Environment.Current()
    env.RequireSSH()
    cmd = [env.ssh, 'user@host']

  An attribute which is None indicates that the executable couldn't be found.

  Attributes:
    suite: Suite, The suite for this environment.
    bin_path: str, The path where the commands are located. If None, use
        standard `$PATH`.
    ssh: str, Location of ssh command (or None if not found).
    ssh_term: str, Location of ssh terminal command (or None if not found), for
        interactive sessions.
    scp: str, Location of scp command (or None if not found).
    keygen: str, Location of the keygen command (or None if not found).
    ssh_exit_code: int, Exit code indicating SSH command failure.
  """

  # Each suite supports ssh and non-interactive ssh, scp and keygen.
  COMMANDS = {
      Suite.OPENSSH: {
          'ssh': 'ssh',
          'ssh_term': 'ssh',
          'scp': 'scp',
          'keygen': 'ssh-keygen',
      },
      Suite.PUTTY: {
          'ssh': 'plink',
          'ssh_term': 'putty',
          'scp': 'pscp',
          'keygen': 'winkeygen',
      }
  }

  # Exit codes indicating that the `ssh` command (not remote) failed
  SSH_EXIT_CODES = {
      Suite.OPENSSH: 255,
      Suite.PUTTY: 1,  # Only `plink`, `putty` always gives 0
  }

  def __init__(self, suite, bin_path=None):
    """Create a new environment by supplying a suite and command directory.

    Args:
      suite: Suite, the suite for this environment.
      bin_path: str, the path where the commands are located. If None, use
          standard $PATH.
    """
    self.suite = suite
    self.bin_path = bin_path
    # So pytype is aware of attributes.
    self.ssh = None
    self.ssh_term = None
    self.scp = None
    self.keygen = None
    for key, cmd in six.iteritems(self.COMMANDS[suite]):
      setattr(self, key, files.FindExecutableOnPath(cmd, path=self.bin_path))
    self.ssh_exit_code = self.SSH_EXIT_CODES[suite]

  def SupportsSSH(self):
    """Whether all SSH commands are supported.

    Returns:
      True if and only if all commands are supported, else False.
    """
    return all((self.ssh, self.ssh_term, self.scp, self.keygen))

  def RequireSSH(self):
    """Simply raises an error if any SSH command is not supported.

    Raises:
      MissingCommandError: One or more of the commands were not found.
    """
    if not self.SupportsSSH():
      raise MissingCommandError('Your platform does not support SSH.')

  @classmethod
  def Current(cls):
    """Retrieve the current environment.

    Returns:
      Environment, the active and current environment on this machine.
    """
    if platforms.OperatingSystem.IsWindows():
      suite = Suite.PUTTY
      bin_path = _SdkHelperBin()
    else:
      suite = Suite.OPENSSH
      bin_path = None
    return Environment(suite, bin_path)


def _IsValidSshUsername(user):
  # All characters must be ASCII, and no spaces are allowed
  # This may grant false positives, but will prevent backwards-incompatible
  # behavior.
  return all(ord(c) < 128 and c != ' ' for c in user)


class KeyFileStatus(enum.Enum):
  PRESENT = 'OK'
  ABSENT = 'NOT FOUND'
  BROKEN = 'BROKEN'


class _KeyFileKind(enum.Enum):
  """List of supported (by gcloud) key file kinds."""
  PRIVATE = 'private'
  PUBLIC = 'public'
  PPK = 'PuTTY PPK'


class Keys(object):
  """Manages private and public SSH key files.

  This class manages the SSH public and private key files, and verifies
  correctness of them. A Keys object is instantiated with a path to a
  private key file. The public key locations are inferred by the private
  key file by simply appending a different file ending (`.pub` and `.ppk`).

  If the keys are broken or do not yet exist, the EnsureKeysExist method
  can be utilized to shell out to the system SSH keygen and write new key
  files.

  By default, there is an SSH key for the gcloud installation,
  `DEFAULT_KEY_FILE` which should likely be used. Note that SSH keys are
  generated and managed on a per-installation basis. Strictly speaking,
  there is no 1:1 relationship between installation and user account.

  Verifies correctness of key files:
   - Populates list of SSH key files (key pair, ppk key on Windows).
   - Checks if files are present and (to basic extent) correct.
   - Can remove broken key (if permitted by user).
   - Provides status information.
  """

  DEFAULT_KEY_FILE = os.path.join('~', '.ssh', 'google_compute_engine')

  class PublicKey(object):
    """Represents a public key.

    Attributes:
      key_type: str, Key generation type, e.g. `ssh-rsa` or `ssh-dss`.
      key_data: str, Base64-encoded key data.
      comment: str, Non-semantic comment, may be empty string or contain spaces.
    """

    def __init__(self, key_type, key_data, comment=''):
      self.key_type = key_type
      self.key_data = key_data
      self.comment = comment

    @classmethod
    def FromKeyString(cls, key_string):
      """Construct a public key from a typical OpenSSH-style key string.

      Args:
        key_string: str, on the format `TYPE DATA [COMMENT]`. Example:
          `ssh-rsa ABCDEF me@host.com`.

      Raises:
        InvalidKeyError: The public key file does not contain key (heuristic).

      Returns:
        Keys.PublicKey, the parsed public key.
      """
      # We get back a unicode list of keys for the remaining metadata, so
      # convert to unicode. Assume UTF 8, but if we miss a character we can just
      # replace it with a '?'. The only source of issues would be the hostnames,
      # which are relatively inconsequential.
      decoded_key = key_string.strip()
      if isinstance(key_string, six.binary_type):
        decoded_key = decoded_key.decode('utf8', 'replace')
      parts = decoded_key.split(' ', 2)
      if len(parts) < 2:
        raise InvalidKeyError('Public key [{}] is invalid.'.format(key_string))
      comment = parts[2].strip() if len(parts) > 2 else ''  # e.g. `me@host`
      return cls(parts[0], parts[1], comment)

    def ToEntry(self, include_comment=False):
      """Format this key into a text entry.

      Args:
        include_comment: str, Include the comment part in this entry.

      Returns:
        str, A key string on the form `TYPE DATA` or `TYPE DATA COMMENT`.
      """
      out_format = '{type} {data}'
      if include_comment and self.comment:
        out_format += ' {comment}'
      return out_format.format(
          type=self.key_type, data=self.key_data, comment=self.comment)

  class KeyFileData(object):

    def __init__(self, filename):
      # We keep filename as file handle. Filesystem race is impossible to avoid
      # in this design as we spawn a subprocess and pass in filename.
      # TODO(b/33288605) fix it.
      self.filename = filename
      self.status = None

  def __init__(self, key_file, env=None):
    """Create a Keys object which manages the given files.

    Args:
      key_file: str, The file path to the private SSH key file (other files are
          derived from this name). Automatically handles symlinks and user
          expansion.
      env: Environment, Current environment or None to infer from current.
    """
    private_key_file = os.path.realpath(files.ExpandHomeDir(key_file))
    self.dir = os.path.dirname(private_key_file)
    self.env = env or Environment.Current()
    # TODO(b/71388306): Enums aren't handled well by pytype.
    self.keys = {
        _KeyFileKind.PRIVATE: self.KeyFileData(private_key_file),
        _KeyFileKind.PUBLIC: self.KeyFileData(private_key_file + '.pub')
    }  # type: dict[enum.Enum, Keys.KeyFileData]
    if self.env.suite is Suite.PUTTY:
      self.keys[_KeyFileKind.PPK] = self.KeyFileData(private_key_file + '.ppk')

  @classmethod
  def FromFilename(cls, filename=None, env=None):
    """Create Keys object given a file name.

    Args:
      filename: str or None, the name to the file or DEFAULT_KEY_FILE if None
      env: Environment, Current environment or None to infer from current.

    Returns:
      Keys, an instance which manages the keys with the given name.
    """
    return cls(filename or Keys.DEFAULT_KEY_FILE, env)

  @property
  def key_file(self):
    """Filename of the private key file."""
    return self.keys[_KeyFileKind.PRIVATE].filename

  def _StatusMessage(self):
    """Prepares human readable SSH key status information."""
    messages = []
    key_padding = 0
    status_padding = 0
    for kind in self.keys:
      data = self.keys[kind]
      key_padding = max(key_padding, len(kind.value))  # pytype: disable=attribute-error
      status_padding = max(status_padding, len(data.status.value))  # pytype: disable=attribute-error
    for kind in self.keys:
      data = self.keys[kind]
      messages.append('{} {} [{}]\n'.format(
          (kind.value + ' key').ljust(key_padding + 4),  # pytype: disable=attribute-error
          ('(' + data.status.value + ')') .ljust(status_padding + 2),  # pytype: disable=attribute-error
          data.filename))
    messages.sort()
    return ''.join(messages)

  def Validate(self):
    """Performs minimum key files validation.

    Note that this is a simple best-effort parser intended for machine
    generated keys. If the file has been user modified, there's a risk
    of both false positives and false negatives.

    Returns:
      KeyFileStatus.PRESENT if key files meet minimum requirements.
      KeyFileStatus.ABSENT if neither private nor public keys exist.
      KeyFileStatus.BROKEN if there is some key, but it is broken or incomplete.
    """
    def ValidateFile(kind):
      status_or_line = self._WarnOrReadFirstKeyLine(self.keys[kind].filename,
                                                    kind.value)  # pytype: disable=attribute-error
      if isinstance(status_or_line, KeyFileStatus):
        return status_or_line
      else:  # returned line - present
        self.keys[kind].first_line = status_or_line
        return KeyFileStatus.PRESENT

    for file_kind in self.keys:
      self.keys[file_kind].status = ValidateFile(file_kind)

    # The remaining checks are for the public key file.

    # Additional validation for public keys.
    if self.keys[_KeyFileKind.PUBLIC].status is KeyFileStatus.PRESENT:
      try:
        self.GetPublicKey()
      except InvalidKeyError:
        log.warning('The public SSH key file [{}] is corrupt.'
                    .format(self.keys[_KeyFileKind.PUBLIC]))
        self.keys[_KeyFileKind.PUBLIC].status = KeyFileStatus.BROKEN

    # Summary
    collected_values = [x.status for x in six.itervalues(self.keys)]
    if all(x == KeyFileStatus.ABSENT for x in collected_values):
      return KeyFileStatus.ABSENT
    elif all(x == KeyFileStatus.PRESENT for x in collected_values):
      return KeyFileStatus.PRESENT
    else:
      return KeyFileStatus.BROKEN

  def RemoveKeyFilesIfPermittedOrFail(self, force_key_file_overwrite=None):
    """Removes all SSH key files if user permitted this behavior.

    Precondition: The SSH key files are currently in a broken state.

    Depending on `force_key_file_overwrite`, delete all SSH key files:

    - If True, delete key files.
    - If False, cancel immediately.
    - If None and
      - interactive, prompt the user.
      - non-interactive, cancel.

    Args:
      force_key_file_overwrite: bool or None, overwrite broken key files.

    Raises:
      console_io.OperationCancelledError: Operation intentionally cancelled.
      OSError: Error deleting the broken file(s).
    """
    message = 'Your SSH key files are broken.\n' + self._StatusMessage()
    if force_key_file_overwrite is False:
      raise console_io.OperationCancelledError(message + 'Operation aborted.')
    message += 'We are going to overwrite all above files.'
    log.warning(message)
    if force_key_file_overwrite is None:
      # - Interactive when pressing 'Y', continue
      # - Interactive when pressing enter or 'N', raise OperationCancelledError
      # - Non-interactive, raise OperationCancelledError
      console_io.PromptContinue(default=False, cancel_on_no=True)

    # Remove existing broken key files.
    for key_file in six.viewvalues(self.keys):
      try:
        os.remove(key_file.filename)
      except OSError as e:
        if e.errno == errno.EISDIR:
          # key_file.filename points to a directory
          raise

  def _WarnOrReadFirstKeyLine(self, path, kind):
    """Returns the first line from the key file path.

    A None return indicates an error and is always accompanied by a log.warning
    message.

    Args:
      path: The path of the file to read from.
      kind: The kind of key file, 'private' or 'public'.

    Returns:
      None (and prints a log.warning message) if the file does not exist, is not
      readable, or is empty. Otherwise returns the first line utf8 decoded.
    """
    try:
      with files.FileReader(path) as f:
        line = f.readline().strip()
        if line:
          return line
        msg = 'is empty'
        status = KeyFileStatus.BROKEN
    except files.MissingFileError:
      msg = 'does not exist'
      status = KeyFileStatus.ABSENT
    except files.Error:
      msg = 'is not readable'
      status = KeyFileStatus.BROKEN
    log.warning('The %s SSH key file for gcloud %s.', kind, msg)
    return status

  def GetPublicKey(self):
    # type: () -> Keys.PublicKey
    """Returns the public key verbatim from file as a string.

    Precondition: The public key must exist. Run Keys.EnsureKeysExist() prior.

    Raises:
      InvalidKeyError: If the public key file does not contain key (heuristic).

    Returns:
      Keys.PublicKey, a public key (that passed primitive validation).
    """
    filepath = self.keys[_KeyFileKind.PUBLIC].filename
    with files.FileReader(filepath) as f:
      # TODO(b/33467618): Currently we enforce that key exists on the first
      # line, but OpenSSH does not enforce that.
      first_line = f.readline()
      return self.PublicKey.FromKeyString(first_line)

  def EnsureKeysExist(self, overwrite, allow_passphrase=True):
    """Generate ssh key files if they do not yet exist.

    Precondition: Environment.SupportsSSH()

    Args:
      overwrite: bool or None, overwrite key files if they are broken.
      allow_passphrase: bool, if keygeneration occurs, let the user specfiy
        a passphrase for private key encryption. See `ssh.KeygenCommand` for
        details on when this is possible.

    Raises:
      console_io.OperationCancelledError: if interrupted by user
      CommandError: if the ssh-keygen command failed.
    """
    key_files_validity = self.Validate()

    if key_files_validity is KeyFileStatus.BROKEN:
      self.RemoveKeyFilesIfPermittedOrFail(overwrite)
      # Fallthrough
    if key_files_validity is not KeyFileStatus.PRESENT:
      if key_files_validity is KeyFileStatus.ABSENT:
        # If key is broken, message is already displayed
        log.warning('You do not have an SSH key for gcloud.')
        log.warning('SSH keygen will be executed to generate a key.')

      if not os.path.exists(self.dir):
        msg = ('This tool needs to create the directory [{0}] before being '
               'able to generate SSH keys.'.format(self.dir))
        console_io.PromptContinue(
            message=msg, cancel_on_no=True,
            cancel_string='SSH key generation aborted by user.')
        files.MakeDir(self.dir, 0o700)

      cmd = KeygenCommand(self.key_file, allow_passphrase=allow_passphrase)
      cmd.Run(self.env)


class KnownHosts(object):
  """Represents known hosts file, supports read, write and basic key management.

  Currently a very naive, but sufficient, implementation where each entry is
  simply a string, and all entries are list of those strings.
  """

  # TODO(b/33467618): Rename the file itself
  DEFAULT_PATH = os.path.realpath(files.ExpandHomeDir(
      os.path.join('~', '.ssh', 'google_compute_known_hosts')))

  def __init__(self, known_hosts, file_path):
    """Construct a known hosts representation based on a list of key strings.

    Args:
      known_hosts: str, list each corresponding to a line in known_hosts_file.
      file_path: str, path to the known_hosts_file.
    """
    self.known_hosts = known_hosts
    self.file_path = file_path

  @classmethod
  def FromFile(cls, file_path):
    """Create a KnownHosts object given a known_hosts_file.

    Args:
      file_path: str, path to the known_hosts_file.

    Returns:
      KnownHosts object corresponding to the file. If the file could not be
      opened, the KnownHosts object will have no entries.
    """
    try:
      known_hosts = files.ReadFileContents(file_path).splitlines()
    except files.Error as e:
      known_hosts = []
      log.debug('SSH Known Hosts File [{0}] could not be opened: {1}'
                .format(file_path, e))
    return KnownHosts(known_hosts, file_path)

  @classmethod
  def FromDefaultFile(cls):
    """Create a KnownHosts object from the default known_hosts_file.

    Returns:
      KnownHosts object corresponding to the default known_hosts_file.
    """
    return KnownHosts.FromFile(KnownHosts.DEFAULT_PATH)

  def ContainsAlias(self, host_key_alias):
    """Check if a host key alias exists in one of the known hosts.

    Args:
      host_key_alias: str, the host key alias

    Returns:
      bool, True if host_key_alias is in the known hosts file. If the known
      hosts file couldn't be opened it will be treated as if empty and False
      returned.
    """
    return any(host_key_alias in line for line in self.known_hosts)

  def Add(self, hostname, host_key, overwrite=False):
    """Add or update the entry for the given hostname.

    If there is no entry for the given hostname, it will be added. If there is
    an entry already and overwrite_keys is False, nothing will be changed. If
    there is an entry and overwrite_keys is True, the key will be updated if it
    has changed.

    Args:
      hostname: str, The hostname for the known_hosts entry.
      host_key: str, The host key for the given hostname.
      overwrite: bool, If true, will overwrite the entry corresponding to
        hostname with the new host_key if it already exists. If false and an
        entry already exists for hostname, will ignore the new host_key value.
    """
    new_key_entry = '{0} {1}'.format(hostname, host_key)
    for i, key in enumerate(self.known_hosts):
      if key.startswith(hostname):
        if overwrite:
          self.known_hosts[i] = new_key_entry
        break
    else:
      self.known_hosts.append(new_key_entry)

  def Write(self):
    """Writes the file to disk."""
    files.WriteFileContents(
        self.file_path, '\n'.join(self.known_hosts) + '\n', private=True)


def GetDefaultSshUsername(warn_on_account_user=False):
  """Returns the default username for ssh.

  The default username is the local username, unless that username is invalid.
  In that case, the default username is the username portion of the current
  account.

  Emits a warning if it's not using the local account username.

  Args:
    warn_on_account_user: bool, whether to warn if using the current account
      instead of the local username.

  Returns:
    str, the default SSH username.
  """
  user = getpass.getuser()
  if not _IsValidSshUsername(user):
    full_account = properties.VALUES.core.account.Get(required=True)
    account_user = gaia.MapGaiaEmailToDefaultAccountName(full_account)
    if warn_on_account_user:
      log.warning(
          'Invalid characters in local username [{0}]. '
          'Using username corresponding to active account: [{1}]'.format(
              user, account_user))
    user = account_user
  return user


def _MetadataHasOsloginEnable(metadata):
  """Return true if the metadata has 'oslogin-enable' set and 'true'.

  Args:
    metadata: Instance or Project metadata.

  Returns:
    True if Enabled, False if Disabled, None if key is not present.
  """
  if not (metadata and metadata.items):
    return None
  matching_values = [item.value for item in metadata.items
                     if item.key == OSLOGIN_ENABLE_METADATA_KEY]
  if not matching_values:
    return None
  return matching_values[0].lower() == 'true'


def CheckForOsloginAndGetUser(instance, project, requested_user, public_key,
                              release_track):
  """Check instance/project metadata for oslogin and return updated username.

  Check to see if OS Login is enabled in metadata and if it is, return
  the OS Login user and a boolean value indicating if OS Login is being used.

  Args:
    instance: instance, The object representing the instance we are
      connecting to. If None, instance metadata will be ignored.
    project: project, The object representing the current project.
    requested_user: str, The default or requested username to connect as.
    public_key: str, The public key of the user connecting.
    release_track: release_track, The object representing the release track.

  Returns:
    tuple, A string containing the oslogin username and a boolean indicating
      wheather oslogin is being used.
  """
  # Instance metadata has priority
  use_oslogin = False
  oslogin_enabled = None
  if instance is not None:
    oslogin_enabled = _MetadataHasOsloginEnable(instance.metadata)
  if oslogin_enabled is None:
    project_metadata = project.commonInstanceMetadata
    oslogin_enabled = _MetadataHasOsloginEnable(project_metadata)

  if not oslogin_enabled:
    return requested_user, use_oslogin

  # Connect to the oslogin API and add public key to oslogin user account.
  oslogin = oslogin_client.OsloginClient(release_track)
  if not oslogin:
    log.warning(
        'OS Login is enabled on Instance/Project, but is not available '
        'in the {0} version of gcloud.'.format(release_track.id))
    return requested_user, use_oslogin
  user_email = properties.VALUES.core.account.Get()

  # Check to see if public key is already in profile, and import if not.
  login_profile = oslogin.GetLoginProfile(user_email, project.name)
  keys = oslogin_utils.GetKeyDictionaryFromProfile(
      user_email, oslogin, profile=login_profile)
  fingerprint = oslogin_utils.FindKeyInKeyList(public_key, keys)
  if not fingerprint:
    import_response = oslogin.ImportSshPublicKey(user_email, public_key)
    login_profile = import_response.loginProfile
  use_oslogin = True

  # Get the username for the oslogin user. If the username is the same as the
  # default user, return that one. Otherwise, return the 'primary' username.
  # If no 'primary' exists, return the first username.
  oslogin_user = None
  for pa in login_profile.posixAccounts:
    oslogin_user = oslogin_user or pa.username
    if pa.username == requested_user:
      return requested_user, use_oslogin
    elif pa.primary:
      oslogin_user = pa.username

  log.warning('Using OS Login user [{0}] instead of default user [{1}]'
              .format(oslogin_user, requested_user))
  return oslogin_user, use_oslogin


def ParseAndSubstituteSSHFlags(args, remote, ip_address):
  """Obtain extra flags from the command arguments."""
  extra_flags = []
  if args.ssh_flag:
    for flag in args.ssh_flag:
      for flag_part in flag.split():  # We want grouping here
        dereferenced_flag = (
            flag_part.replace('%USER%', remote.user)
            .replace('%INSTANCE%', ip_address))
        extra_flags.append(dereferenced_flag)
  return extra_flags


def _SdkHelperBin():
  """Returns the SDK helper executable bin directory."""
  # TODO(b/33467618): Remove this method?
  return os.path.join(config.Paths().sdk_root, 'bin', 'sdk')


class Remote(object):
  """A reference to an SSH remote, consisting of a host and user.

  Hashing and equality methods are implemented for this class,
  so remotes can be put in sets for de-duplication.

  Attributes:
    user: str or None, SSH user name (optional).
    host: str or None, Host name.
  """

  # A remote has two parts `[user@]host`, where `user` is optional.
  #   A user:
  #   - cannot contain ':', '@'
  #   A host:
  #   - cannot start with '.'
  #   - cannot contain ':', '/', '\\', '@'
  # This regular expression matches if and only if the above requirements are
  # satisfied. The capture groups are (user, host) where `user` will be
  # None if omitted.
  _REMOTE_REGEX = re.compile(r'^(?:([^:@]+)@)?([^.:/\\@][^:/\\@]*)$')

  def __init__(self, host, user=None):
    """Constructor for FileReference.

    Args:
      host: str or None, Host name.
      user: str or None, SSH user name.
    """
    self.host = host
    self.user = user

  def ToArg(self):
    """Convert to a positional argument, in the form expected by `ssh`/`plink`.

    Returns:
      str, A string on the form `[user@]host`.
    """
    return self.user + '@' + self.host if self.user else self.host

  @classmethod
  def FromArg(cls, arg):
    """Convert an SSH-style positional argument to a remote.

    Args:
      arg: str, A path on the canonical ssh form `[user@]host`.

    Returns:
      Remote, the constructed object or None if arg is malformed.
    """
    match = cls._REMOTE_REGEX.match(arg)
    if match:
      user, host = match.groups()
      return cls(host, user=user)
    else:
      return None

  def __hash__(self):
    return hash(self.ToArg())

  def __eq__(self, other):
    return type(self) is type(other) and self.ToArg() == other.ToArg()

  def __ne__(self, other):
    return not self.__eq__(other)

  def __repr__(self):
    return self.ToArg()


class KeygenCommand(object):
  """Platform independent SSH client key generation command.

  For OpenSSH, we use `ssh-keygen(1)`. For PuTTY, we use a custom binary.
  Currently, the only supported algorithm is 'rsa'. The command generates the
  following files:
  - `<identity_file>`: Private key, on OpenSSH format (possibly encrypted).
  - `<identity_file>.pub`: Public key, on OpenSSH format.
  - `<identity_file>.ppk`: Unencrypted PPK key-pair, on PuTTY format.

  The PPK-file is only generated from a PuTTY environment, and encodes the same
  private- and public keys as the other files.

  Attributes:
    identity_file: str, path to private key file.
    allow_passphrase: bool, If True, attempt at prompting the user for a
      passphrase for private key encryption, given that the following
      conditions are also true:
      - Running in an OpenSSH environment (Linux and Mac)
      - Running in interactive mode (from an actual TTY)
      - Prompts are enabled in gcloud
  """

  def __init__(self, identity_file, allow_passphrase=True):
    """Construct a suite independent `ssh-keygen` command."""
    self.identity_file = identity_file
    self.allow_passphrase = allow_passphrase

  def Build(self, env=None):
    """Construct the actual command according to the given environment.

    Args:
      env: Environment, to construct the command for (or current if None).

    Raises:
      MissingCommandError: If keygen command was not found.

    Returns:
      [str], the command args (where the first arg is the command itself).
    """
    env = env or Environment.Current()
    if not env.keygen:
      raise MissingCommandError('Keygen command not found in the current '
                                'environment.')
    args = [env.keygen]
    if env.suite is Suite.OPENSSH:
      prompt_passphrase = self.allow_passphrase and console_io.CanPrompt()
      if not prompt_passphrase:
        args.extend(['-N', ''])  # Empty passphrase
      args.extend(['-t', 'rsa', '-f', self.identity_file])
    else:
      args.append(self.identity_file)

    return args

  def Run(self, env=None):
    """Run the keygen command in the given environment.

    Args:
      env: Environment, environment to run in (or current if None).

    Raises:
      MissingCommandError: Keygen command not found.
      CommandError: Keygen command failed.
    """
    args = self.Build(env)
    log.debug('Running command [{}].'.format(' '.join(args)))
    status = execution_utils.Exec(args, no_exit=True)
    if status:
      raise CommandError(args[0], return_code=status)


class SSHCommand(object):
  """Represents a platform independent SSH command.

  This class is intended to manage the most important suite- and platform
  specifics. We manage the following data:
  - The executable to call, either `ssh`, `putty` or `plink`.
  - User and host, through the `remote` arg.
  - Potential remote command to execute, `remote_command` arg.

  In addition, it manages these flags:
  -t, -T      Pseudo-terminal allocation
  -p, -P      Port
  -i          Identity file (private key)
  -o Key=Val  OpenSSH specific options that should be added, `options` arg.

  For flexibility, SSHCommand also accepts `extra_flags`. Always use these
  with caution -- they will be added as-is to the command invocation without
  validation. Specifically, do not add any of the above mentioned flags.
  """

  def __init__(self, remote, port=None, identity_file=None,
               options=None, extra_flags=None, remote_command=None, tty=None,
               remainder=None):
    """Construct a suite independent SSH command.

    Note that `extra_flags` and `remote_command` arguments are lists of strings:
    `remote_command=['echo', '-e', 'hello']` is different from
    `remote_command=['echo', '-e hello']` -- the former is likely desired.
    For the same reason, `extra_flags` should be passed like `['-k', 'v']`.

    Args:
      remote: Remote, the remote to connect to.
      port: str, port.
      identity_file: str, path to private key file.
      options: {str: str}, options (`-o`) for OpenSSH, see `ssh_config(5)`.
      extra_flags: [str], extra flags to append to ssh invocation. Both binary
        style flags `['-b']` and flags with values `['-k', 'v']` are accepted.
      remote_command: [str], command to run remotely.
      tty: bool, launch a terminal. If None, determine automatically based on
        presence of remote command.
      remainder: [str], NOT RECOMMENDED. Arguments to be appended directly to
        the native tool invocation, after the `[user@]host` part but prior to
        the remote command. On PuTTY, this can only be a remote command. On
        OpenSSH, this can be flags followed by a remote command. Cannot be
        combined with `remote_command`. Use `extra_flags` and `remote_command`
        instead.
    """
    self.remote = remote
    self.port = port
    self.identity_file = identity_file
    self.options = options or {}
    self.extra_flags = extra_flags or []
    self.remote_command = remote_command or []
    self.tty = tty
    self.remainder = remainder

  def Build(self, env=None):
    """Construct the actual command according to the given environment.

    Args:
      env: Environment, to construct the command for (or current if None).

    Raises:
      MissingCommandError: If SSH command(s) required were not found.

    Returns:
      [str], the command args (where the first arg is the command itself).
    """
    env = env or Environment.Current()
    if not (env.ssh and env.ssh_term):
      raise MissingCommandError('The current environment lacks SSH.')

    tty = self.tty if self.tty in [True, False] else not self.remote_command
    args = [env.ssh_term, '-t'] if tty else [env.ssh, '-T']

    if self.port:
      port_flag = '-P' if env.suite is Suite.PUTTY else '-p'
      args.extend([port_flag, self.port])

    if self.identity_file:
      identity_file = self.identity_file
      if env.suite is Suite.PUTTY and not identity_file.endswith('.ppk'):
        identity_file += '.ppk'
      args.extend(['-i', identity_file])

    if env.suite is Suite.OPENSSH:
      # Always, always deterministic order
      for key, value in sorted(six.iteritems(self.options)):
        args.extend(['-o', '{k}={v}'.format(k=key, v=value)])
    args.extend(self.extra_flags)
    args.append(self.remote.ToArg())

    # TODO(b/38179637): Remove when compute have separated flags from
    # positionals.
    if self.remainder:
      args.extend(self.remainder)

    if self.remote_command:
      if env.suite is Suite.OPENSSH:  # Putty doesn't like double dash
        args.append('--')
      args.extend(self.remote_command)
    return args

  def Run(self, env=None, force_connect=False,
          explicit_output_file=None,
          explicit_error_file=None):
    """Run the SSH command using the given environment.

    Args:
      env: Environment, environment to run in (or current if None).
      force_connect: bool, whether to inject 'y' into the prompts for `plink`,
        which is insecure and not recommended. It serves legacy compatibility
        purposes only.
      explicit_output_file: Pipe stdout into this file-like object
      explicit_error_file: Pipe stderr into this file-like object

    Raises:
      MissingCommandError: If SSH command(s) not found.
      CommandError: SSH command failed (not to be confused with the eventual
        failure of the remote command).

    Returns:
      int, The exit code of the remote command, forwarded from the client.
    """
    env = env or Environment.Current()
    args = self.Build(env)
    log.debug('Running command [{}].'.format(' '.join(args)))
    # PuTTY and friends always ask on fingerprint mismatch
    in_str = 'y\n' if env.suite is Suite.PUTTY and force_connect else None

    # We pipe stdout to a specific file
    extra_popen_kwargs = {}
    if explicit_output_file:
      extra_popen_kwargs['stdout'] = explicit_output_file
    if explicit_error_file:
      extra_popen_kwargs['stderr'] = explicit_error_file

    status = execution_utils.Exec(args, no_exit=True, in_str=in_str,
                                  **extra_popen_kwargs)
    if status == env.ssh_exit_code:
      raise CommandError(args[0], return_code=status)
    return status


class SCPCommand(object):
  """Represents a platform independent SCP command.

  This class is intended to manage the most important suite- and platform
  specifics. We manage the following data:
  - The executable to call, either `scp` or `pscp`.
  - User and host, through either `sources` or `destination` arg. Multiple
    remote sources are allowed but not supported under PuTTY. Multiple local
    sources are always allowed.
  - Potential remote command to execute, `remote_command` arg.

  In addition, it manages these flags:
  -r          Recursive copy
  -C          Compression
  -P          Port
  -i          Identity file (private key)
  -o Key=Val  OpenSSH specific options that should be added, `options` arg.

  For flexibility, SCPCommand also accepts `extra_flags`. Always use these
  with caution -- they will be added as-is to the command invocation without
  validation. Specifically, do not add any of the above mentioned flags.
  """

  def __init__(self, sources, destination, recursive=False, compress=False,
               port=None, identity_file=None, options=None, extra_flags=None):
    """Construct a suite independent SCP command.

    Args:
      sources: [FileReference] or FileReference, the source(s) for this copy. At
        least one source is required. NOTE: Multiple remote sources are not
        supported in PuTTY and is discouraged for consistency.
      destination: FileReference, the destination file or directory. If remote
        source, this must be local, and vice versa.
      recursive: bool, recursive directory copy.
      compress: bool, enable compression.
      port: str, port.
      identity_file: str, path to private key file.
      options: {str: str}, options (`-o`) for OpenSSH, see `ssh_config(5)`.
      extra_flags: [str], extra flags to append to scp invocation. Both binary
        style flags `['-b']` and flags with values `['-k', 'v']` are accepted.
    """
    self.sources = [sources] if isinstance(sources, FileReference) else sources
    self.destination = destination
    self.recursive = recursive
    self.compress = compress
    self.port = port
    self.identity_file = identity_file
    self.options = options or {}
    self.extra_flags = extra_flags or []

  @classmethod
  def Verify(cls, sources, destination, single_remote=False, env=None):
    """Verify that the source- and destination config is sound.

    Checks that sources are remote if destination is local and vice versa,
    plus raises error for multiple remote sources in PuTTY, which is not
    supported by `pscp`.

    Args:
      sources: [FileReference], see SCPCommand.sources.
      destination: FileReference, see SCPCommand.destination.
      single_remote: bool, if True, enforce that all remote sources refer
        to the same Remote (user and host).
      env: Environment, the current environment.

    Raises:
      InvalidConfigurationError: The source/destination configuration is
        invalid.
    """
    env = env or Environment.Current()

    if not sources:
      raise InvalidConfigurationError('No sources provided.', sources,
                                      destination)

    if destination.remote:  # local -> remote
      if any([src.remote for src in sources]):
        raise InvalidConfigurationError(
            'All sources must be local files when destination is remote.',
            sources, destination)
    else:  # remote -> local
      if env.suite is Suite.PUTTY and len(sources) != 1:
        raise InvalidConfigurationError(
            'Multiple remote sources not supported by PuTTY.',
            sources, destination)
      if not all([src.remote for src in sources]):
        raise InvalidConfigurationError(
            'Source(s) must be remote when destination is local.',
            sources, destination)
      if single_remote and len(set([src.remote for src in sources])) != 1:
        raise InvalidConfigurationError(
            'All sources must refer to the same remote when destination is '
            'local.', sources, destination)

  def Build(self, env=None):
    """Construct the actual command according to the given environment.

    Args:
      env: Environment, to construct the command for (or current if None).

    Raises:
      InvalidConfigurationError: The source/destination configuration is
        invalid.
      MissingCommandError: If SCP command(s) required were not found.

    Returns:
      [str], the command args (where the first arg is the command itself).
    """
    env = env or Environment.Current()
    if not env.scp:
      raise MissingCommandError('The current environment lacks an SCP (secure '
                                'copy) client.')
    self.Verify(self.sources, self.destination, env=env)

    args = [env.scp]

    if self.recursive:
      args.append('-r')

    if self.compress:
      args.append('-C')

    if self.port:
      args.extend(['-P', self.port])

    if self.identity_file:
      identity_file = self.identity_file
      if env.suite is Suite.PUTTY and not identity_file.endswith('.ppk'):
        identity_file += '.ppk'
      args.extend(['-i', identity_file])

    # SSH config options
    if env.suite is Suite.OPENSSH:
      # Always, always deterministic order
      for key, value in sorted(six.iteritems(self.options)):
        args.extend(['-o', '{k}={v}'.format(k=key, v=value)])

    args.extend(self.extra_flags)

    # Positionals
    args.extend([source.ToArg() for source in self.sources])
    args.append(self.destination.ToArg())
    return args

  def Run(self, env=None, force_connect=False):
    """Run the SCP command using the given environment.

    Args:
      env: Environment, environment to run in (or current if None).
      force_connect: bool, whether to inject 'y' into the prompts for `pscp`,
        which is insecure and not recommended. It serves legacy compatibility
        purposes only.

    Raises:
      InvalidConfigurationError: The source/destination configuration is
        invalid.
      MissingCommandError: If SCP command(s) not found.
      CommandError: SCP command failed to copy the file(s).
    """
    env = env or Environment.Current()
    args = self.Build(env)
    log.debug('Running command [{}].'.format(' '.join(args)))
    # pscp asks on (1) first connection and (2) fingerprint mismatch.
    # This ensures pscp will always allow the connection.
    # TODO(b/35355795): Work out a better solution for PuTTY.
    in_str = 'y\n' if env.suite is Suite.PUTTY and force_connect else None
    status = execution_utils.Exec(args, no_exit=True, in_str=in_str)
    if status:
      raise CommandError(args[0], return_code=status)


class SSHPoller(object):
  """Represents an SSH command that polls for connectivity.

  Using a poller is not ideal, because each attempt is a separate connection
  attempt, meaning that the user might be prompted for a passphrase or to
  approve a server identity by the underlying ssh tool that we do not control.
  Always assume that polling for connectivity using this method is an operation
  that requires user action.
  """

  def __init__(self, remote, port=None, identity_file=None,
               options=None, extra_flags=None, max_wait_ms=60*1000,
               sleep_ms=5*1000):
    """Construct a poller for an SSH connection.

    Args:
      remote: Remote, the remote to poll.
      port: str, port to poll.
      identity_file: str, path to private key file.
      options: {str: str}, options (`-o`) for OpenSSH, see `ssh_config(5)`.
      extra_flags: [str], extra flags to append to ssh invocation. Both binary
        style flags `['-b']` and flags with values `['-k', 'v']` are accepted.
      max_wait_ms: int, number of ms to wait before raising.
      sleep_ms: int, time between trials.
    """
    self.ssh_command = SSHCommand(
        remote, port=port, identity_file=identity_file, options=options,
        extra_flags=extra_flags, remote_command=['true'], tty=False)
    self._sleep_ms = sleep_ms
    self._retryer = retry.Retryer(max_wait_ms=max_wait_ms, jitter_ms=0)

  def Poll(self, env=None, force_connect=False):
    """Poll a remote for connectivity within the given timeout.

    The SSH command may prompt the user. It is recommended to wrap this call in
    a progress tracker. If this method returns, a connection was successfully
    established. If not, this method will raise.

    Args:
      env: Environment, environment to run in (or current if None).
      force_connect: bool, whether to inject 'y' into the prompts for `plink`,
        which is insecure and not recommended. It serves legacy compatibility
        purposes only.

    Raises:
      MissingCommandError: If SSH command(s) not found.
      core.retry.WaitException: SSH command failed, possibly due to short
        timeout. There is no way to distinguish between a timeout error and a
        misconfigured connection.
    """
    self._retryer.RetryOnException(
        self.ssh_command.Run,
        kwargs={'env': env, 'force_connect': force_connect},
        should_retry_if=lambda exc_type, *args: exc_type is CommandError,
        sleep_ms=self._sleep_ms)


class FileReference(object):
  """A reference to a local or remote file (or directory) for SCP.

  Attributes:
    path: str, The path to the file.
    remote: Remote or None, the remote referred or None if local.
  """

  def __init__(self, path, remote=None):
    """Constructor for FileReference.

    Args:
      path: str, The path to the file.
      remote: Remote or None, the remote referred or None if local.
    """
    self.path = path
    self.remote = remote

  def ToArg(self):
    """Convert to a positional argument, in the form expected by `scp`/`pscp`.

    Returns:
      str, A string on the form `remote:path` if remote or `path` if local.
    """
    if not self.remote:
      return self.path
    return '{remote}:{path}'.format(remote=self.remote.ToArg(), path=self.path)

  @classmethod
  def FromPath(cls, path):
    """Convert an SCP-style positional argument to a file reference.

    Note that this method does not raise. No lookup of either local or remote
    file presence exists.

    Args:
      path: str, A path on the canonical scp form `[remote:]path`. If
        remote, `path` can be empty, e.g. `me@host:`.

    Returns:
      FileReference, the constructed object.
    """
    # If local drive given, it overrides a potential remote pattern match
    local_drive = os.path.splitdrive(path)[0]
    remote_arg, sep, file_path = path.partition(':')
    remote = Remote.FromArg(remote_arg) if sep else None
    if remote and not local_drive:
      return cls(path=file_path, remote=remote)
    else:
      return cls(path=path)

  def __eq__(self, other):
    return type(self) is type(other) and self.ToArg() == other.ToArg()

  def __ne__(self, other):
    return not self.__eq__(other)

  def __repr__(self):
    return self.ToArg()
