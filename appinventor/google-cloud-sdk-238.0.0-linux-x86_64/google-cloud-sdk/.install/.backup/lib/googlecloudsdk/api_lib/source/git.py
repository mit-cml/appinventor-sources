# -*- coding: utf-8 -*- #
# Copyright 2015 Google Inc. All Rights Reserved.
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

"""Wrapper to manipulate GCP git repository."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import errno
import os
import re
import subprocess
import textwrap

from googlecloudsdk.core import exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.util import encoding
from googlecloudsdk.core.util import files
from googlecloudsdk.core.util import platforms
import six
from six.moves import range  # pylint: disable=redefined-builtin
import uritemplate


# This regular expression is used to extract the URL of the 'origin' remote by
# scraping 'git remote show origin'.
_ORIGIN_URL_RE = re.compile(r'remote origin\n.*Fetch URL: (?P<url>.+)\n', re.M)
# This is the minimum version of git required to use credential helpers.
_HELPER_MIN = (2, 0, 1)
_WINDOWS_HELPER_MIN = (2, 15, 0)

_TRAILING_SPACES = re.compile(r'(^|^.*[^\\ ]|^.*\\ ) *$')


class Error(exceptions.Error):
  """Exceptions for this module."""


class UnknownRepositoryAliasException(Error):
  """Exception to be thrown when a repository alias provided cannot be found."""


class CannotInitRepositoryException(Error):
  """Exception to be thrown when a repository cannot be created."""


class CannotFetchRepositoryException(Error):
  """Exception to be thrown when a repository cannot be fetched."""


class CannotPushToRepositoryException(Error):
  """Exception to be thrown when a repository cannot be pushed to."""


class GitVersionException(Error):
  """Exceptions for when git version is too old."""

  def __init__(self, fmtstr, cur_version, min_version):
    self.cur_version = cur_version
    super(GitVersionException, self).__init__(
        fmtstr.format(cur_version=cur_version, min_version=min_version))


class InvalidGitException(Error):
  """Exceptions for when git version is empty or invalid."""


class GcloudIsNotInPath(Error):
  """Exception for when the gcloud cannot be found."""


def CheckGitVersion(version_lower_bound=None):
  """Returns true when version of git is >= min_version.

  Args:
    version_lower_bound: (int,int,int), The lowest allowed version, or None to
      just check for the presence of git.

  Returns:
    True if version >= min_version.

  Raises:
    GitVersionException: if `git` was found, but the version is incorrect.
    InvalidGitException: if `git` was found, but the output of `git version` is
      not as expected.
    NoGitException: if `git` was not found.
  """
  try:
    cur_version = encoding.Decode(subprocess.check_output(['git', 'version']))
    if not cur_version:
      raise InvalidGitException('The git version string is empty.')
    if not cur_version.startswith('git version '):
      raise InvalidGitException(('The git version string must start with '
                                 'git version .'))
    match = re.search(r'(\d+)\.(\d+)\.(\d+)', cur_version)
    if not match:
      raise InvalidGitException('The git version string must contain a '
                                'version number.')

    current_version = tuple([int(item) for item in match.group(1, 2, 3)])
    if version_lower_bound and current_version < version_lower_bound:
      min_version = '.'.join(six.text_type(i) for i in version_lower_bound)
      raise GitVersionException(
          'Your git version {cur_version} is older than the minimum version '
          '{min_version}. Please install a newer version of git.',
          cur_version=cur_version, min_version=min_version)
  except OSError as e:
    if e.errno == errno.ENOENT:
      raise NoGitException()
    raise
  return True


class NoGitException(Error):
  """Exceptions for when git is not available."""

  def __init__(self):
    super(NoGitException, self).__init__(
        textwrap.dedent("""\
        Cannot find git. Please install git and try again.

        You can find git installers at [http://git-scm.com/downloads], or use
        your favorite package manager to install it on your computer. Make sure
        it can be found on your system PATH.
        """))


def _GetRepositoryURI(project, alias):
  """Get the URI for a repository, given its project and alias.

  Args:
    project: str, The project name.
    alias: str, The repository alias.

  Returns:
    str, The repository URI.
  """
  return uritemplate.expand(
      'https://source.developers.google.com/p/{project}/r/{alias}',
      {'project': project, 'alias': alias})


def _GetGcloudScript(full_path=False):
  """Get name of the gcloud script.

  Args:
    full_path: boolean, True if the gcloud full path should be used if free
      of spaces.

  Returns:
    str, command to use to execute gcloud

  Raises:
    GcloudIsNotInPath: if gcloud is not found in the path
  """

  if (platforms.OperatingSystem.Current() ==
      platforms.OperatingSystem.WINDOWS):
    gcloud_ext = '.cmd'
  else:
    gcloud_ext = ''

  gcloud_name = 'gcloud'
  gcloud = files.FindExecutableOnPath(gcloud_name, pathext=[gcloud_ext])

  if not gcloud:
    raise GcloudIsNotInPath(
        'Could not verify that gcloud is in the PATH. '
        'Please make sure the Cloud SDK bin folder is in PATH.')
  if full_path:
    if not re.match(r'[-a-zA-Z0-9_/]+$', gcloud):
      log.warning(
          textwrap.dedent("""\
          You specified the option to use the full gcloud path in the git
          credential.helper, but the path contains non alphanumberic characters
          so the credential helper may not work correctly."""))
    return gcloud
  else:
    return gcloud_name + gcloud_ext


def _GetCredHelperCommand(uri, full_path=False, min_version=_HELPER_MIN):
  """Returns the gcloud credential helper command for a remote repository.

  The command will be of the form '!gcloud auth git-helper --account=EMAIL
  --ignore-unknown $@`. See https://git-scm.com/docs/git-config. If the
  installed version of git or the remote repository does not support
  the gcloud credential helper, then returns None.

  Args:
    uri: str, The uri of the remote repository.
    full_path: bool, If true, use the full path to gcloud.
    min_version: minimum git version; if found git is earlier than this, warn
        and return None

  Returns:
    str, The credential helper command if it is available.
  """
  credentialed_hosts = ['source.developers.google.com']
  extra = properties.VALUES.core.credentialed_hosted_repo_domains.Get()
  if extra:
    credentialed_hosts.extend(extra.split(','))
  if any(
      uri.startswith('https://' + host + '/') for host in credentialed_hosts):
    try:
      CheckGitVersion(min_version)
    except GitVersionException as e:
      helper_min_str = '.'.join(six.text_type(i) for i in min_version)
      log.warning(
          textwrap.dedent("""\
          You are using a Google-hosted repository with a
          {current} which is older than {min_version}. If you upgrade
          to {min_version} or later, gcloud can handle authentication to
          this repository. Otherwise, to authenticate, use your Google
          account and the password found by running the following command.
           $ gcloud auth print-access-token""".format(
               current=e.cur_version, min_version=helper_min_str)))
      return None
    # Use git alias "!shell command" syntax so we can configure
    # the helper with options. Also git-credential is not
    # prefixed when it starts with "!".
    return '!{0} auth git-helper --account={1} --ignore-unknown $@'.format(
        _GetGcloudScript(full_path),
        properties.VALUES.core.account.Get(required=True))
  return None


class Git(object):
  """Represents project git repo."""

  def __init__(self, project_id, repo_name, uri=None):
    """Constructor.

    Args:
      project_id: str, The name of the project that has a repository associated
          with it.
      repo_name: str, The name of the repository to clone.
      uri: str, The URI of the repository, or None if it will be inferred from
          the name.

    Raises:
      UnknownRepositoryAliasException: If the repo name is not known to be
          associated with the project.
    """
    self._project_id = project_id
    self._repo_name = repo_name
    self._uri = uri or _GetRepositoryURI(project_id, repo_name)
    if not self._uri:
      raise UnknownRepositoryAliasException()

  def GetName(self):
    return self._repo_name

  def Clone(self, destination_path, dry_run=False, full_path=False):
    """Clone a git repository into a gcloud workspace.

    If the resulting clone does not have a .gcloud directory, create one. Also,
    sets the credential.helper to use the gcloud credential helper.

    Args:
      destination_path: str, The relative path for the repository clone.
      dry_run: bool, If true do not run but print commands instead.
      full_path: bool, If true use the full path to gcloud.

    Returns:
      str, The absolute path of cloned repository.

    Raises:
      CannotInitRepositoryException: If there is already a file or directory in
          the way of creating this repository.
      CannotFetchRepositoryException: If there is a problem fetching the
          repository from the remote host, or if the repository is otherwise
          misconfigured.
    """
    abs_repository_path = os.path.abspath(destination_path)
    if os.path.exists(abs_repository_path):
      CheckGitVersion()  # Do this here, before we start running git commands
      if os.listdir(abs_repository_path):
        # Raise exception if dir is not empty and not a git repository
        raise CannotInitRepositoryException(
            'Directory path specified exists and is not empty')
    # Make a brand new repository if directory does not exist or
    # clone if directory exists and is empty
    try:
      # If this is a Google-hosted repo, clone with the cred helper.
      cmd = ['git', 'clone', self._uri, abs_repository_path]
      min_git = _HELPER_MIN
      if (platforms.OperatingSystem.Current() ==
          platforms.OperatingSystem.WINDOWS):
        min_git = _WINDOWS_HELPER_MIN
      cred_helper_command = _GetCredHelperCommand(
          self._uri, full_path=full_path, min_version=min_git)
      if cred_helper_command:
        cmd += [
            '--config', 'credential.helper=', '--config',
            'credential.helper=' + cred_helper_command
        ]
      self._RunCommand(cmd, dry_run)
    except subprocess.CalledProcessError as e:
      raise CannotFetchRepositoryException(e)
    return abs_repository_path

  def ForcePushFilesToBranch(self,
                             branch,
                             base_path,
                             paths,
                             dry_run=False,
                             full_path=False):
    """Force pushes a set of files to a branch on the remote repository.

    This is mainly to be used with source captures, where the user wants to
    upload files associated with a deployment to view later.

    Args:
      branch: str, The name of the branch to push to.
      base_path: str, The base path to use for the files.
      paths: list of str, The paths for the files to upload.
          Their paths in the repository will be relative to base_path.
          For example, if base_path is '/a/b/c', the path '/a/b/c/d/file1' will
          appear as 'd/file1' in the repository.
      dry_run: bool, If true do not run but print commands instead.
      full_path: bool, If true use the full path to gcloud.

    Raises:
      CannotPushToRepositoryException: If the operation fails in any way.
    """
    # Git treats relative paths strangely with the --work-tree flag.
    # git add --work-tree=a a/b will try to look for a/a/b.
    # To avoid the lookup, always convert to absolute paths.
    paths = [os.path.abspath(p) for p in paths]

    # Check for git files and don't allow upload if they are included.
    for path in paths:
      for segment in path.split(os.sep):
        if segment in ('.git', '.gitignore'):
          message = ("Can't upload the file tree. "
                     'Uploading a directory containing a git repository as a '
                     'subdirectory is not supported. '
                     'Please either upload from the top level git repository '
                     'or any of its subdirectories. '
                     'Unsupported git file detected: %s' % path)
          raise CannotPushToRepositoryException(message)

    with files.TemporaryDirectory() as temp_dir:
      def RunGitCommand(*args):
        git_dir = '--git-dir=' + os.path.join(temp_dir, '.git')
        work_tree = '--work-tree=' + base_path
        self._RunCommand(['git', git_dir, work_tree] + list(args), dry_run)

      # Init empty repository in a temporary location
      self._RunCommand(['git', 'init', temp_dir], dry_run)

      # Create new branch and add files
      RunGitCommand('checkout', '-b', branch)
      args_len = 100
      for i in range(0, len(paths), args_len):
        RunGitCommand('add', *paths[i:i + args_len])
      RunGitCommand('commit', '-m', 'source capture uploaded from gcloud')

      # Add remote and force push
      cred_helper_command = _GetCredHelperCommand(
          self._uri, full_path=full_path)
      if cred_helper_command:
        RunGitCommand('config', 'credential.helper', cred_helper_command)
      try:
        RunGitCommand('remote', 'add', 'origin', self._uri)
        RunGitCommand('push', '-f', 'origin', branch)
      except subprocess.CalledProcessError as e:
        raise CannotPushToRepositoryException(e)

  def _RunCommand(self, cmd, dry_run):
    log.debug('Executing %s', cmd)
    if dry_run:
      log.out.Print(' '.join(cmd))
    else:
      subprocess.check_call(cmd)
