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
"""Sosreport from Google Compute Engine VMs."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import ssh_utils
from googlecloudsdk.command_lib.compute.diagnose import sosreport_helper as soshelper
from googlecloudsdk.command_lib.compute.instances import flags
from googlecloudsdk.command_lib.util.ssh import ssh
from googlecloudsdk.core import log

# Defaults paths
SOSREPORT_INSTALL_PATH = "/tmp/git-sosreport"
REPORTS_PATH = "/tmp/gcloud-sosreport"


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class SosReport(base.Command):
  """Sosreport run from a Google Compute Engine VM.

  This command is designed to obtain relevant debug information from a VM in a
  stardard way for expediting support cases.

  The actual information scraping is done by the Sosreport tool
  (https://github.com/sosreport/sos). This command is a wrapper that handles
  installation, running and (optionally) copying the result.

  The location of the tool download and report generation are defaulted to the
  /tmp directory, but can be modified through flags. You can use the
  --download-dir flag to specify a location where the command can download the
  resulting from the VM.

  NOTE: For this command to work, git needs to be installed within the VM, in
  order to clone the repository and run the code from there.

  NOTE: Sosreport is somewhat geared towards Python 3.x, as it uses APIs that
  had to be back-ported to Python 2.7 (notably concurrent). If the default
  installation of Python is 2.7, it is possible that the Sosreport run fails.
  To fix this either install the dependencies for Python 2.7 or use
--python-path to specify the path to another Python installation that works,
  normally it being a Python 3.x binary.
  """

  @staticmethod
  def Args(parser):
    """Creates the flags stmts for the command."""
    flags.INSTANCE_ARG.AddArgument(
        parser, operation_type="run Sosreport on")

    parser.add_argument(
        "--sosreport-install-path",
        default=SOSREPORT_INSTALL_PATH,
        help="""\
            Remote location (within the VM) to clone sosreport into.
            """)

    parser.add_argument(
        "--reports-path",
        default=REPORTS_PATH,
        help="""\
            Remote location (within the VM) to write the reports into.
            """)

    parser.add_argument(
        "--download-dir",
        default=None,
        help="""\
            Local dir to which to download the report generated in the VM.
            If not specified, no download will be done.
            The download will be done using a no-configuration \
            gcloud compute scp command.
            For more complicated setups, manual download will be required.
            """)

    parser.add_argument(
        "--python-path",
        default=None,
        help="""\
            Path to the python binary to be called.
            Sosreport is a python tool which is called by default with the
            default python installation.
            This overrides that calls and uses the provided python binary.
            """)

    # Generate the flags needed to stub the SSH portion of the command
    ssh_utils.BaseSSHCLIHelper.Args(parser)

    # SSH flag
    parser.add_argument(
        "--ssh-flag",
        action="append",
        help="""\
        Additional flags to be passed to *ssh(1)*. It is recommended that flags
        be passed using an assignment operator and quotes. This flag will
        replace occurrences of ``%USER%"" and ``%INSTANCE%"" with their
        dereferenced values. Example:

          $ {command} example-instance --zone us-central1-a  --ssh-flag="-vvv" --ssh-flag="-L 80:%INSTANCE%:80"

        is equivalent to passing the flags ``--vvv"" and ``-L
        80:162.222.181.197:80"" to *ssh(1)* if the external IP address of
        "example-instance" is 162.222.181.197.
        """)

    parser.add_argument(
        "--user",
        help="""\
        User for login to the selected VMs.
        If not specified, the default user will be used.
        """)

  def Run(self, args):
    """Default run method implementation."""
    super(SosReport, self).Run(args)
    self._use_accounts_service = False

    # Obtain the gcloud variables
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    instance = self.GetInstance(holder, args)
    user = args.user if args.user else ssh.GetDefaultSshUsername()
    ssh_helper = ssh_utils.BaseSSHCLIHelper()
    ssh_helper.Run(args)

    # Create the context variables
    context = {
        "args": args,
        "instance": instance,
        "ssh_helper": ssh_helper,
        "user": user,
        "python_path": args.python_path,
    }
    install_path = args.sosreport_install_path
    reports_path = args.reports_path

    # We dowload Sosreport into the VM if needed (normally first time)
    soshelper.ObtainSosreport(context, install_path)

    # (If needed) We create the directory where the reports will be created
    log.out.Print("Creating the path where reports will be written if needed.")
    soshelper.CreatePath(context, reports_path)

    # We run the report
    soshelper.RunSosreport(context, install_path, reports_path)

    # Obtain and report the filename of the generated report
    report_path = soshelper.ObtainReportFilename(context, reports_path)
    msg = 'Report generated into "{report_path}".'
    log.status.Print(msg.format(report_path=report_path))

    # If download_dir is set, we download the report over
    if args.download_dir:
      report_path = soshelper.CopyReportFile(context, args.download_dir,
                                             report_path)
      msg = 'Successfully downloaded report to "{report_path}"'
      log.status.Print(msg.format(report_path=report_path))

  @classmethod
  def GetInstance(cls, holder, args):
    instance_ref = flags.INSTANCE_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=flags.GetInstanceZoneScopeLister(holder.client))
    request = holder.client.messages.ComputeInstancesGetRequest(
        **instance_ref.AsDict())
    return holder.client.MakeRequests([(holder.client.apitools_client.instances,
                                        "Get", request)])[0]
