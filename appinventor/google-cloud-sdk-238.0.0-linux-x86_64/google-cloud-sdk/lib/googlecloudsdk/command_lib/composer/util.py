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
"""Common utilities for Composer commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from collections import OrderedDict
import contextlib
import io
import os
import re

from googlecloudsdk.api_lib.composer import util as api_util
from googlecloudsdk.api_lib.container import api_adapter as gke_api_adapter
from googlecloudsdk.api_lib.container import util as gke_util
from googlecloudsdk.api_lib.storage import storage_api
from googlecloudsdk.api_lib.storage import storage_util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.composer import parsers
from googlecloudsdk.core import config
from googlecloudsdk.core import exceptions as core_exceptions
from googlecloudsdk.core import execution_utils
from googlecloudsdk.core import log
from googlecloudsdk.core.console import console_io
from googlecloudsdk.core.resource import resource_printer
from googlecloudsdk.core.util import encoding
from googlecloudsdk.core.util import files
import six

GKE_API_VERSION = 'v1'

KUBECONFIG_ENV_VAR_NAME = 'KUBECONFIG'

ENVIRONMENT_NAME_PATTERN = re.compile('^[a-z](?:[-0-9a-z]{0,62}[0-9a-z])?$')
_GCLOUD_COMPONENT_NAME = 'gcloud'
_KUBECTL_COMPONENT_NAME = 'kubectl'

MISSING_GCLOUD_MSG = """\
Unable to find [gcloud] on path!
"""

MISSING_KUBECTL_MSG = """\
Accessing a Cloud Composer environment requires the kubernetes commandline
client [kubectl]. To install, run
  $ gcloud components install kubectl
"""

SUBCOMMAND_WHITELIST = [
    'backfill',
    'clear',
    'connections',
    'dag_state',
    'delete_dag',
    'initdb',
    'kerberos',
    'list_dags',
    'list_tasks',
    'pause',
    'pool',
    'render',
    'resetdb',
    'run',
    'task_failed_deps',
    'task_state',
    'test',
    'trigger_dag',
    'unpause',
    'upgradedb',
    'variables',
    'version',
]

NAMESPACE_ARG_NAME = '--namespace'
NAMESPACE_ARG_ALIAS = '-n'


class Error(core_exceptions.Error):
  """Class for errors raised by Composer commands."""


class KubectlError(Error):
  """Class for errors raised when shelling out to kubectl."""


class GsutilError(Error):
  """Class for errors raised when shelling out to gsutil."""


class OperationError(Error):
  """Class for errors raised when a polled operation completes with an error."""

  def __init__(self, operation_name, description):
    super(OperationError, self).__init__('Operation [{}] failed: {}'.format(
        operation_name, description))


class EnvironmentCreateError(Error):
  """Class for errors raised when creating an environment."""


class EnvironmentDeleteError(Error):
  """Class for errors raised when deleting an environment."""


class InvalidUserInputError(Error):
  """Class for errors raised when a user input fails client-side validation."""


def ConstructList(title, items):
  """Constructs text output listing the elements of items and a title.

  Args:
    title: string, the listing title
    items: iterable, the iterable whose elements to list

  Returns:
    string, text representing list title and elements.
  """
  buf = io.StringIO()
  resource_printer.Print(items, 'list[title="{0}"]'.format(title), out=buf)
  return buf.getvalue()


# pylint: disable=g-doc-return-or-yield
@contextlib.contextmanager
def TemporaryKubeconfig(location_id, cluster_id):
  """Context manager that manages a temporary kubeconfig file for a GKE cluster.

  The kubeconfig file will be automatically created and destroyed and will
  contain only the credentials for the specified GKE cluster. The 'KUBECONFIG'
  value in `os.environ` will be temporarily updated with the temporary
  kubeconfig's path. Consequently, subprocesses started with
  googlecloudsdk.core.execution_utils.Exec while in this context manager will
  see the temporary KUBECONFIG environment variable.

  Args:
    location_id: string, the id of the location to which the cluster belongs
    cluster_id: string, the id of the cluster

  Raises:
    Error: If unable to get credentials for kubernetes cluster.

  Returns:
    the path to the temporary kubeconfig file

  Yields:
    Due to b/73533917, linter crashes without yields.
  """
  gke_util.CheckKubectlInstalled()
  with files.TemporaryDirectory() as tempdir:
    kubeconfig = os.path.join(tempdir, 'kubeconfig')
    old_kubeconfig = encoding.GetEncodedValue(os.environ,
                                              KUBECONFIG_ENV_VAR_NAME)
    try:
      encoding.SetEncodedValue(os.environ, KUBECONFIG_ENV_VAR_NAME, kubeconfig)
      gke_api = gke_api_adapter.NewAPIAdapter(GKE_API_VERSION)
      cluster_ref = gke_api.ParseCluster(cluster_id, location_id)
      cluster = gke_api.GetCluster(cluster_ref)
      auth = cluster.masterAuth
      missing_creds = not (auth and auth.clientCertificate and auth.clientKey)
      if missing_creds and not gke_util.ClusterConfig.UseGCPAuthProvider():
        raise Error('Unable to get cluster credentials. User must have edit '
                    'permission on {}'.format(cluster_ref.projectId))
      gke_util.ClusterConfig.Persist(cluster, cluster_ref.projectId)
      yield kubeconfig
    finally:
      encoding.SetEncodedValue(os.environ, KUBECONFIG_ENV_VAR_NAME,
                               old_kubeconfig)


def ExtractGkeClusterLocationId(env_object):
  """Finds the location ID of the GKE cluster running the provided environment.

  Args:
    env_object: Environment, the environment, likely returned by an API call,
        whose cluster location to extract

  Raises:
    Error: if Kubernetes cluster is not found.

  Returns:
    str, the location ID (a short name like us-central1-b) of the GKE cluster
    running the environment
  """
  if env_object.config.nodeConfig.location:
    return env_object.config.nodeConfig.location[
        env_object.config.nodeConfig.location.rfind('/') + 1:]

  gke_cluster = env_object.config.gkeCluster[
      env_object.config.gkeCluster.rfind('/') + 1:]

  gke_api = gke_api_adapter.NewAPIAdapter(GKE_API_VERSION)
  # GKE is in the middle of deprecating zones in favor of locations, so we
  # read from whichever one has a value.
  cluster_zones = [
      c.location[c.location.rfind('/') + 1:] or c.zone
      for c in gke_api.ListClusters(parsers.GetProject()).clusters
      if c.name == gke_cluster
  ]
  if not cluster_zones:
    # This should never happen unless the user has deleted their cluster out of
    # band.
    raise Error('Kubernetes Engine cluster not found.')
  elif len(cluster_zones) == 1:
    return cluster_zones[0]

  return cluster_zones[console_io.PromptChoice(
      ['[{}]'.format(z) for z in cluster_zones],
      default=0,
      message=
      'Cluster found in more than one location. Please select the desired '
      'location:')]


def GetGkePod(pod_substr=None):
  """Returns the name of a running pod in a GKE cluster.

  Retrieves pods in the GKE cluster pointed to by the current kubeconfig
  context. To target a specific cluster, this command should be called within
  the context of a TemporaryKubeconfig context manager.

  If pod_substr is not None, the name of an arbitrary running pod
  whose name contains pod_substr is returned; if no pod's name contains
  pod_substr, an Error is raised. If pod_substr is None, an arbitrary running
  pod is reeturned.

  Args:
    pod_substr: string, a filter to apply to pods. The returned pod name must
        contain pod_substr (if it is not None).

  Raises:
    Error: if GKE pods cannot be retrieved or desired pod is not found.
  """
  pod_out = io.StringIO()
  args = [
      'get', 'pods', '--output',
      r'jsonpath={range .items[*]}{.metadata.name}{"\t"}{.status.phase}{"\n"}'
  ]

  try:
    RunKubectlCommand(args, out_func=pod_out.write, err_func=log.err.write)
  except KubectlError as e:
    raise Error('Error retrieving GKE pods: %s' % e)

  running_pods = [
      pod_status.split('\t')[0]
      for pod_status in pod_out.getvalue().split('\n')
      if pod_status.lower().endswith('\trunning')
  ]

  if not running_pods:
    raise Error('No running GKE pods found. If the environment '
                'was recently started, please wait and retry.')

  if pod_substr is None:
    return running_pods[0]

  try:
    return next(pod for pod in running_pods if pod_substr in pod)
  except StopIteration:
    raise Error('Desired GKE pod not found. If the environment '
                'was recently started, please wait and retry.')


def IsValidEnvironmentName(name):
  """Returns True if the provided name is a valid environment name."""
  return ENVIRONMENT_NAME_PATTERN.match(name) is not None


def RunKubectlCommand(args, out_func=None, err_func=None):
  """Shells out a command to kubectl.

  This command should be called within the context of a TemporaryKubeconfig
  context manager in order for kubectl to be configured to access the correct
  cluster.

  Args:
    args: list of strings, command line arguments to pass to the kubectl
        command. Should omit the kubectl command itself. For example, to
        execute 'kubectl get pods', provide ['get', 'pods'].
    out_func: str->None, a function to call with the stdout of the kubectl
        command
    err_func: str->None, a function to call with the stderr of the kubectl
        command

  Raises:
    Error: if kubectl could not be called
    KubectlError: if the invocation of kubectl was unsuccessful
  """
  # Check for 'kubectl' along Cloud SDK path. This will fail if component
  # manager is disabled. In this case, check entire path.
  kubectl_path = files.FindExecutableOnPath(_KUBECTL_COMPONENT_NAME,
                                            config.Paths().sdk_bin_path)
  if kubectl_path is None:
    kubectl_path = files.FindExecutableOnPath(_KUBECTL_COMPONENT_NAME)
  if kubectl_path is None:
    raise Error(MISSING_KUBECTL_MSG)

  try:
    # All kubectl requests will execute within the scope of the 'default'
    # namespace, unless the namespace scope has been explicitly set within the
    # args.
    retval = execution_utils.Exec(
        AddKubectlNamespace(
            'default',
            execution_utils.ArgsForExecutableTool(kubectl_path, *args)
        ),
        no_exit=True,
        out_func=out_func,
        err_func=err_func,
        universal_newlines=True)
  except (execution_utils.PermissionError,
          execution_utils.InvalidCommandError) as e:
    raise KubectlError(six.text_type(e))
  if retval:
    raise KubectlError('kubectl returned non-zero status code.')


def AddKubectlNamespace(namespace, kubectl_args):
  """Adds namespace arguments to the provided list of kubectl args.

  If a namespace arg is not already present, insert `--namespace <namespace>`
  after the `kubectl` command and before all other arg elements.

  Resulting in this general format:
    ['kubectl', '--namespace', 'namespace_foo', ... <remaining args> ... ]

  Args:
    namespace: name of the namespace scope
    kubectl_args: list of kubectl command arguments. Expects that the first
      element will be the `kubectl` command, followed by all additional
      arguments.

  Returns:
    list of kubectl args with the additional namespace args (if necessary).
  """
  # Checks for existing namespace args before adding new ones.
  if {NAMESPACE_ARG_NAME, NAMESPACE_ARG_ALIAS}.isdisjoint(set(kubectl_args)):
    # Inserts new namespace arguments to a fixed index of the kubectl_args list,
    # so the list of new args will be inserted in reverse.
    for new_arg in [namespace, NAMESPACE_ARG_NAME]:
      # Expects `kubectl` command to be the first argument.
      kubectl_args.insert(1, new_arg)
  return kubectl_args


def ParseRequirementsFile(requirements_file_path):
  """Parses the given requirements file into a requirements dictionary.

  If the file path is GCS file path, use GCS file parser to parse requirements
  file. Otherwise, use local file parser.

  Args:
    requirements_file_path: Filepath to the requirements file.

  Returns:
    {string: string}, dict mapping from PyPI package name to extras and version
    specifier, if provided.

  Raises:
    Error: if requirements file cannot be read.
  """
  try:
    is_gcs_file_path = requirements_file_path.startswith('gs://')
    if is_gcs_file_path:
      storage_client = storage_api.StorageClient()
      object_ref = storage_util.ObjectReference.FromUrl(requirements_file_path)
      file_content = storage_client.ReadObject(object_ref)
    else:
      file_content = files.FileReader(requirements_file_path)

    requirements = {}
    with file_content as requirements_file:
      for requirement_specifier in requirements_file:
        requirement_specifier = requirement_specifier.strip()
        if not requirement_specifier:
          continue
        package, version = SplitRequirementSpecifier(requirement_specifier)
        # Ensure package not already in entry list.
        if package in requirements:
          raise Error(
              'Duplicate package in requirements file: {0}'.format(package))
        requirements[package] = version
      return requirements
  except (files.Error, storage_api.Error, storage_util.Error):
    # Raise error when it fails to read requirements file.
    core_exceptions.reraise(Error(
        'Unable to read requirements file {0}'.format(requirements_file_path)))


def SplitRequirementSpecifier(requirement_specifier):
  """Splits the package name from the other components of a requirement spec.

  Only supports PEP 508 `name_req` requirement specifiers. Does not support
  requirement specifiers containing environment markers.

  Args:
    requirement_specifier: str, a PEP 508 requirement specifier that does not
      contain an environment marker.

  Returns:
    (string, string), a 2-tuple of the extracted package name and the tail of
    the requirement specifier which could contain extras and/or a version
    specifier.

  Raises:
    Error: No package name was found in the requirement spec.
  """
  package = requirement_specifier.strip()
  tail_start_regex = r'(\[|\(|==|>=|!=|<=|<|>|~=|===)'
  tail_match = re.search(tail_start_regex, requirement_specifier)
  tail = ''
  if tail_match:
    package = requirement_specifier[:tail_match.start()].strip()
    tail = requirement_specifier[tail_match.start():].strip()
  if not package:
    raise Error(r'Missing package name in requirement specifier: \'{}\''.format(
        requirement_specifier))
  return package, tail


def BuildPartialUpdate(clear, remove_keys, set_entries, field_mask_prefix,
                       entry_cls, env_builder):
  """Builds the field mask and patch environment for an environment update.

  Follows the environments update semantic which applies operations
  in an effective order of clear -> remove -> set.

  Leading and trailing whitespace is stripped from elements in remove_keys
  and the keys of set_entries.

  Args:
    clear: bool, If true, the patch removes existing keys.
    remove_keys: iterable(string), Iterable of keys to remove.
    set_entries: {string: string}, Dict containing entries to set.
    field_mask_prefix: string, The prefix defining the path to the base of the
      proto map to be patched.
    entry_cls: AdditionalProperty, The AdditionalProperty class for the type
      of entry being updated.
    env_builder: [AdditionalProperty] -> Environment, A function which produces
      a patch Environment with the given list of entry_cls properties.


  Returns:
    (string, Environment), a 2-tuple of the field mask defined by the arguments
    and a patch environment produced by env_builder.
  """
  remove_keys = set(k.strip() for k in remove_keys or [])
  # set_entries is sorted by key to make it easier for tests to set the
  # expected patch object.
  set_entries = OrderedDict(
      (k.strip(), v) for k, v in sorted(six.iteritems(set_entries or {})))
  if clear:
    entries = [
        entry_cls(key=key, value=value)
        for key, value in six.iteritems(set_entries)
    ]
    return field_mask_prefix, env_builder(entries)

  field_mask_entries = []
  seen_keys = set()
  for key in remove_keys:
    field_mask_entries.append('{}.{}'.format(field_mask_prefix, key))
    seen_keys.add(key)
  entries = []
  for key, value in six.iteritems(set_entries):
    entries.append(entry_cls(key=key, value=value))
    if key not in seen_keys:
      field_mask_entries.append('{}.{}'.format(field_mask_prefix, key))
  # Sorting field mask entries makes it easier for tests to set the expected
  # field mask since dictionary iteration order is undefined.
  field_mask_entries.sort()
  return ','.join(field_mask_entries), env_builder(entries)


def BuildFullMapUpdate(clear, remove_keys, set_entries, initial_entries,
                       entry_cls, env_builder):
  """Builds the patch environment for an environment update.

  To be used when BuildPartialUpdate cannot be used due to lack of support for
  field masks containing map keys.

  Follows the environments update semantic which applies operations
  in an effective order of clear -> remove -> set.

  Leading and trailing whitespace is stripped from elements in remove_keys
  and the keys of set_entries.

  Args:
    clear: bool, If true, the patch removes existing keys.
    remove_keys: iterable(string), Iterable of keys to remove.
    set_entries: {string: string}, Dict containing entries to set.
    initial_entries: [AdditionalProperty], list of AdditionalProperty class with
      key and value fields, representing starting dict to update from.
    entry_cls: AdditionalProperty, The AdditionalProperty class for the type
      of entry being updated.
    env_builder: [AdditionalProperty] -> Environment, A function which produces
      a patch Environment with the given list of entry_cls properties.


  Returns:
    Environment, a patch environment produced by env_builder.
  """
  # Transform initial entries list to dictionary for easy processing
  entries_dict = OrderedDict(
      (entry.key, entry.value) for entry in initial_entries)
  # Remove values that are no longer desired
  if clear:
    entries_dict = OrderedDict()
  remove_keys = set(k.strip() for k in remove_keys or [])
  for key in remove_keys:
    if key in entries_dict:
      del entries_dict[key]
  # Update dictionary with new values
  # set_entries is sorted by key to make it easier for tests to set the
  # expected patch object.
  set_entries = OrderedDict(
      (k.strip(), v) for k, v in sorted(six.iteritems(set_entries or {})))
  entries_dict.update(set_entries)
  # Transform dictionary back into list of entry_cls
  return env_builder([
      entry_cls(key=key, value=value)
      for key, value in six.iteritems(entries_dict)
  ])


def IsInRunningState(environment, release_track=base.ReleaseTrack.GA):
  """Returns whether an environment currently is in the RUNNING state.

  Args:
    environment: Environment, an object returned by an API call representing
        the environment to check.
    release_track: base.ReleaseTrack, the release track of command. Will dictate
        which Composer client library will be used.
  """
  running_state = (
      api_util.GetMessagesModule(
          release_track=release_track).Environment.StateValueValuesEnum.RUNNING)

  return environment.state == running_state
