# -*- coding: utf-8 -*- #
# Copyright 2018 Google Inc. All Rights Reserved.
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

"""Helper functions for the `compute diagnose sosreport` command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import os
import sys
import tempfile

from googlecloudsdk.command_lib.compute.diagnose import external_helper
from googlecloudsdk.core import exceptions as core_exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core.util import files

SOSREPORT_GITHUB_PATH = 'https://github.com/sosreport/sos.git'


class InstallSosreportError(core_exceptions.Error):
  pass


def ObtainSosreport(context, sosreport_path):
  """Downloads Sosreport from git into the VM.

  Will create the path if it doesn't exist.

  Args:
    context: The command running context
    sosreport_path: The path where the Sosreport should be installed

  Returns:
    If the method returns, means that Sosreport is available.
    Will raise otherwise.

  Raises:
    InstallSosreportError: When installing the tool was not possible.
                           Reason contained in Error message.
  """
  dry_run = context.get('args').dry_run
  if dry_run:
    return ObtainSosreportDryrun(context, sosreport_path)

  log.status.Print('Checking if sosreport is already installed.')
  if PathExists(context, sosreport_path, 'sosreport'):
    return

  log.status.Print('Create the install path if needed.')
  if not CreatePath(context, sosreport_path):
    error_msg = 'Could not create sosreport path "{path}"'
    raise InstallSosreportError(error_msg.format(path=sosreport_path))

  DownloadSosreport(context, sosreport_path)


def ObtainSosreportDryrun(context, sosreport_path):
  """Runs the dry-run version of ObtainSosreport.

  We need this method because the external_helper SSH method is set so that
  dry-run calls returns successful. This is useful for dry-run as permits to
  use the same script both for normal execution and dry-run.

  However, in this case, if the method actually finds sosreport installed,
  it will return and not run the rest of the obtaining commands. This would
  mean the dry-run would omit some commands, most noticeably the git cloning.

  Args:
    context: The command running context
    sosreport_path: The path where the Sosreport should be installed

  Returns:
     True
  """
  log.status.Print('Checking if sosreport is already installed.')
  PathExists(context, sosreport_path, 'sosreport')

  log.status.Print('Create the install path if needed.')
  CreatePath(context, sosreport_path)

  DownloadSosreport(context, sosreport_path)
  return True


def DownloadSosreport(context, sosreport_path):
  """Obtains the Sosreport from github.

  If this method returns, it means that it worked correctly.
  It will raise otherwise.

  Args:
    context: The command running context
    sosreport_path: The path where the Sosreport should be installed

  Raises:
    ssh.CommandError: there was an error running a SSH command
  """
  log.status.Print('Cloning sosreport')
  RunSSHCommand(
      context,
      'git',
      'clone',
      SOSREPORT_GITHUB_PATH,
      sosreport_path,
      dry_run=context.get('args').dry_run)


def RunSosreport(context, sosreport_path, reports_path):
  """Runs the Sosreport command within the VM.

  Args:
    context: The command running context
    sosreport_path: The path where the Sosreport should be installed
    reports_path: The path where the reports will be stored

  Returns:
    Whether the run was successful
  Raises:
    ssh.CommandError: there was an error running a SSH command
    InstallSosreportError: If there was an error running the tool or owning the
                           file. This is different than an SSH error, which
                           concerns itself with the actual SSH connection,
                           rather than the actual command being ran in the VM.
  """
  # We run the command as sudo (we need it to scrap system values)

  cmd_list = ['sudo']
  # We use the provided python if available
  python_path = context.get('python_path')
  if python_path:
    cmd_list.append(python_path)
  cmd_list += [
      os.path.join(sosreport_path, 'sosreport'),
      '--batch',
      '--compression-type', 'gzip',
      '--config-file', os.path.join(sosreport_path, 'sos.conf'),
      '--tmp-dir', reports_path
  ]
  return_code = RunSSHCommand(
      context, *cmd_list,
      dry_run=context.get('args').dry_run,
      # The SSH infrastructure now pipes the output of the command to a logfile.
      # Due to the interactive nature of the command, piping the command output
      # to stdout for the user to read is nice to have.
      explicit_output_file=sys.stdout,
      explicit_error_file=sys.stderr)

  # The Sosreport is completely piped to our output, so any error raised by the
  # actual command would be already outputted by the tool.
  if return_code != 0:
    raise InstallSosreportError('Error running Sosreport. See output above.')

  # Because we ran the command as sudo, the user doesn't own the file,
  # We need to run chown to change that
  return_code = RunSSHCommand(
      context,
      'sudo',
      'chown',
      context['user'],
      os.path.join(reports_path, '*'),
      dry_run=context.get('args').dry_run)
  if return_code != 0:
    raise InstallSosreportError(('Could not chown the report file. '
                                 'Please chown and copy the file manually.'))


def ObtainReportFilename(context, reports_path):
  """Obtaints the filename of the generated report.

  Args:
    context: The command running context
    reports_path: The path where the reports will be stored

  Returns:
    The filename of the generated report
  Raises:
    ssh.CommandError: there was an error running a SSH command
  """
  dry_run = context.get('args').dry_run
  if dry_run:
    return reports_path

  # The SSH command run will output to stdout the name of the latest report
  # generated. We capture that output into a temporary file.
  temp = tempfile.TemporaryFile('w+')
  RunSSHCommand(
      context,
      'ls',
      '-t',
      os.path.join(reports_path, '*.tar.gz'),
      '|',
      'head',
      '-n',
      '1',
      explicit_output_file=temp,
      dry_run=dry_run)
  temp.seek(0)
  return temp.read().strip()


def CopyReportFile(context, download_dir, report_filepath):
  """Copies the report file from the VM to the local machine.

  Runs `gcloud compute scp` as a subprocess with no configuration.
  Any other setup will require manual copying from the user.

  Args:
    context: The command running context
    download_dir: Local path where the report will be downloaded
    report_filepath: Path to the report within the VM

  Returns:
    The path of the local file.
  """
  instance = context['instance']
  local_path = files.ExpandHomeDir(
      os.path.join(download_dir, os.path.basename(report_filepath)))

  log.status.Print('Copying file by running "gcloud compute scp"')
  cmd = [
      'gcloud', 'compute', 'scp', '--zone', instance.zone,
      instance.name + ':' + report_filepath, local_path
  ]
  external_helper.CallSubprocess(
      'gcloud_copy', cmd, dry_run=context.get('args').dry_run)
  return local_path


def PathExists(context, *path_args):
  """Checks whether a path exists within a VM.

  Args:
    context: Structure created by the Sosreport command to contain the
             necessary information for executing an SSH command.
             Containts command args, instance data, etc.
    *path_args: The path to look for. Can use "magic variables", such as ~.
                Will be joined with os.path.join

  Returns:
    Whether the path exists or not.

  Raises:
    ssh.CommandError: there was an error running a SSH command
  """
  # This command is silent, so it writes stdout to devnull
  path = os.path.join(*path_args)

  # gcloud checks doesn't like python open
  temp_stdout = tempfile.TemporaryFile('w+')
  temp_stderr = tempfile.TemporaryFile('w+')
  return_code = RunSSHCommand(
      context,
      'ls',
      path,
      explicit_output_file=temp_stdout,
      explicit_error_file=temp_stderr,
      dry_run=context.get('args').dry_run)
  # The return code messages whether `ls` was successful, which is true when
  # the path being searched actually exists.
  return return_code == 0


def CreatePath(context, path):
  """Creates a path within a VM.

  Args:
    context: Structure created by the Sosreport command to contain the
             necessary information for executing an SSH command.
             Containts command args, instance data, etc.
    path: The path to look for. Can use "magic variables", such as ~.

  Returns:
    The return code of the command.
  Raises:
    ssh.CommandError: there was an error running a SSH command
  """
  return_code = RunSSHCommand(
      context, 'mkdir', '-p', path, dry_run=context.get('args').dry_run)
  return return_code == 0


def RunSSHCommand(context, *cmd_list, **kwargs):
  """Runs an SSH command to an instance unpacking the sosreport context.

  Args:
    context: Structure created by the Sosreport command to contain the
             necessary information for executing an SSH command.
             Containts command args, instance data, etc.
    *cmd_list: List of arguments that will be bundled into a command array,
               similar to the args of subprocess.popen.
               Passed directly to RunSSHCommandToInstance.
    **kwargs: Configuration variables for the command and the underlying SSH
              helper function.
  Returns:
    The return code of the SSH command.
  Raises:
    ssh.CommandError: there was an error running a SSH command
  """
  command = ' '.join(str(i) for i in cmd_list)
  log.out.Print('Running: "{command}"'.format(command=command))
  return_code = external_helper.RunSSHCommandToInstance(
      command_list=cmd_list,
      instance=context['instance'],
      user=context['user'],
      args=context['args'],
      ssh_helper=context['ssh_helper'],
      explicit_output_file=kwargs.get('explicit_output_file'),
      explicit_error_file=kwargs.get('explicit_error_file'),
      dry_run=kwargs.get('dry_run'))
  return return_code
