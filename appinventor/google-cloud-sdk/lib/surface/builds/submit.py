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
"""Submit build command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import os.path
import uuid

from apitools.base.py import encoding

from googlecloudsdk.api_lib.cloudbuild import cloudbuild_util
from googlecloudsdk.api_lib.cloudbuild import config
from googlecloudsdk.api_lib.cloudbuild import logs as cb_logs
from googlecloudsdk.api_lib.cloudbuild import snapshot
from googlecloudsdk.api_lib.compute import utils as compute_utils
from googlecloudsdk.api_lib.storage import storage_api
from googlecloudsdk.calliope import actions
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions as c_exceptions
from googlecloudsdk.command_lib.cloudbuild import execution
from googlecloudsdk.command_lib.util.apis import arg_utils
from googlecloudsdk.core import exceptions as core_exceptions
from googlecloudsdk.core import execution_utils
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources
from googlecloudsdk.core.resource import resource_transform
from googlecloudsdk.core.util import times

_ALLOWED_SOURCE_EXT = ['.zip', '.tgz', '.gz']


class FailedBuildException(core_exceptions.Error):
  """Exception for builds that did not succeed."""

  def __init__(self, build):
    super(FailedBuildException,
          self).__init__('build {id} completed with status "{status}"'.format(
              id=build.id, status=build.status))


class Submit(base.CreateCommand):
  """Submit a build using Google Cloud Build.

  Submit a build using Google Cloud Build.

  ## NOTES

  You can also run a build locally using the
  separate component: `gcloud components install cloud-build-local`.
  """

  detailed_help = {
      'DESCRIPTION': """\
          {description}

          When the `builds/use_kaniko` property is `True`, builds submitted with
          `--tag` will use Kaniko
          (https://github.com/GoogleContainerTools/kaniko) to execute builds.
          Kaniko executes directives in a Dockerfile, with remote layer caching
          for faster builds. By default, Kaniko will cache layers for 6 hours.
          To override this, set the `builds/kaniko_cache_ttl` property.
      """,
  }

  _machine_type_flag_map = arg_utils.ChoiceEnumMapper(
      '--machine-type',
      (cloudbuild_util.GetMessagesModule()
      ).BuildOptions.MachineTypeValueValuesEnum,
      include_filter=lambda s: str(s) != 'UNSPECIFIED',
      help_str='Machine type used to run the build.')

  @staticmethod
  def Args(parser):
    """Register flags for this command.

    Args:
      parser: An argparse.ArgumentParser-like object. It is mocked out in order
        to capture some information, but behaves like an ArgumentParser.
    """
    source = parser.add_mutually_exclusive_group()
    source.add_argument(
        'source',
        nargs='?',
        default='.',  # By default, the current directory is used.
        help='The location of the source to build. The location can be a '
        'directory on a local disk or a gzipped archive file (.tar.gz) in '
        'Google Cloud Storage. If the source is a local directory, this '
        'command skips the files specified in the `.gcloudignore` file. If a '
        '`.gitignore` file is present in the local source directory, gcloud '
        'will use a Git-compatible `.gcloudignore` file that respects your '
        '.gitignored files. The global `.gitignore` is not respected. For more '
        'information on `.gcloudignore`, see `gcloud topic gcloudignore`.',
    )
    source.add_argument(
        '--no-source',
        action='store_true',
        help='Specify that no source should be uploaded with this build.')

    parser.add_argument(
        '--gcs-source-staging-dir',
        help='A directory in Google Cloud Storage to copy the source used for '
        'staging the build. If the specified bucket does not exist, Cloud '
        'Build will create one. If you don\'t set this field, '
        '```gs://[PROJECT_ID]_cloudbuild/source``` is used.',
    )
    parser.add_argument(
        '--gcs-log-dir',
        help='A directory in Google Cloud Storage to hold build logs. If this '
        'field is not set, '
        '```gs://[PROJECT_NUMBER].cloudbuild-logs.googleusercontent.com/``` '
        'will be created and used.',
    )
    parser.add_argument(
        '--timeout',
        help='Maximum time a build is run before it is failed as `TIMEOUT`. It '
        'is specified as a duration; for example, "2h15m5s" is two hours, '
        'fifteen minutes, and five seconds. If you don\'t specify a unit, '
        'seconds is assumed. For example, "10" is 10 seconds.',
        action=actions.StoreProperty(properties.VALUES.builds.timeout),
    )

    Submit._machine_type_flag_map.choice_arg.AddToParser(parser)

    parser.add_argument(
        '--disk-size',
        type=arg_parsers.BinarySize(lower_bound='100GB', upper_bound='1TB'),
        help='Machine disk size (GB) to run the build.',
    )
    parser.add_argument(
        '--substitutions',
        metavar='KEY=VALUE',
        type=arg_parsers.ArgDict(),
        help="""\
Parameters to be substituted in the build specification.

For example (using some nonsensical substitution keys; all keys must begin with
an underscore):

    $ gcloud builds submit . --config config.yaml \\
        --substitutions _FAVORITE_COLOR=blue,_NUM_CANDIES=10

This will result in a build where every occurrence of ```${_FAVORITE_COLOR}```
in certain fields is replaced by "blue", and similarly for ```${_NUM_CANDIES}```
and "10".

Only the following built-in variables can be specified with the
`--substitutions` flag: REPO_NAME, BRANCH_NAME, TAG_NAME, REVISION_ID,
COMMIT_SHA, SHORT_SHA.

For more details, see:
https://cloud.google.com/cloud-build/docs/api/build-requests#substitutions
""")

    build_config = parser.add_mutually_exclusive_group()
    build_config.add_argument(
        '--tag',
        '-t',
        help='The tag to use with a "docker build" image creation. '
        'Cloud Build will run a remote "docker build -t '
        '$TAG .", where $TAG is the tag provided by this flag. The tag '
        'must be in the gcr.io/* or *.gcr.io/* namespaces. Specify a tag '
        'if you want Cloud Build to build using a Dockerfile '
        'instead of a build config file. If you specify a tag in this '
        'command, your source must include a Dockerfile. For instructions '
        'on building using a Dockerfile see '
        'https://cloud.google.com/cloud-build/docs/quickstart-docker.',
    )
    build_config.add_argument(
        '--config',
        default='cloudbuild.yaml',  # By default, find this in the current dir
        help='The YAML or JSON file to use as the build configuration file.',
    )

    parser.add_argument(
        '--no-cache',
        action='store_true',
        help='If set, disable layer caching when building with Kaniko.\n'
        '\n'
        'This has the same effect as setting the builds/kaniko_cache_ttl '
        'property to 0 for this build.  This can be useful in cases where '
        'Dockerfile builds are non-deterministic and a non-deterministic '
        'result should not be cached.'
    )
    base.ASYNC_FLAG.AddToParser(parser)
    parser.display_info.AddFormat("""
          table(
            id,
            createTime.date('%Y-%m-%dT%H:%M:%S%Oz', undefined='-'),
            duration(start=startTime,end=finishTime,precision=0,calendar=false,undefined="  -").slice(2:).join(""):label=DURATION,
            build_source(undefined="-"):label=SOURCE,
            build_images(undefined="-"):label=IMAGES,
            status
          )
        """)
    # Do not try to create a URI to update the cache.
    parser.display_info.AddCacheUpdater(None)

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Returns:
      Some value that we want to have printed later.

    Raises:
      FailedBuildException: If the build is completed and not 'SUCCESS'.
    """

    project = properties.VALUES.core.project.Get(required=True)
    safe_project = project.replace(':', '_')
    safe_project = safe_project.replace('.', '_')
    # The string 'google' is not allowed in bucket names.
    safe_project = safe_project.replace('google', 'elgoog')

    default_bucket_name = '{}_cloudbuild'.format(safe_project)

    default_gcs_source = False
    if args.gcs_source_staging_dir is None:
      default_gcs_source = True
      args.gcs_source_staging_dir = 'gs://{}/source'.format(default_bucket_name)

    client = cloudbuild_util.GetClientInstance()
    messages = cloudbuild_util.GetMessagesModule()

    gcs_client = storage_api.StorageClient()

    # First, create the build request.
    build_timeout = properties.VALUES.builds.timeout.Get()

    if build_timeout is not None:
      try:
        # A bare number is interpreted as seconds.
        build_timeout_secs = int(build_timeout)
      except ValueError:
        build_timeout_duration = times.ParseDuration(build_timeout)
        build_timeout_secs = int(build_timeout_duration.total_seconds)
      timeout_str = str(build_timeout_secs) + 's'
    else:
      timeout_str = None

    if args.tag is not None:
      if (properties.VALUES.builds.check_tag.GetBool() and
          'gcr.io/' not in args.tag):
        raise c_exceptions.InvalidArgumentException(
            '--tag',
            'Tag value must be in the gcr.io/* or *.gcr.io/* namespace.')
      if properties.VALUES.builds.use_kaniko.GetBool():
        if args.no_cache:
          ttl = '0h'
        else:
          ttl = '{}h'.format(properties.VALUES.builds.kaniko_cache_ttl.Get())
        build_config = messages.Build(
            steps=[
                messages.BuildStep(
                    name=properties.VALUES.builds.kaniko_image.Get(),
                    args=['--destination', args.tag,
                          '--cache', 'true',
                          '--cache-ttl', ttl],
                ),
            ],
            timeout=timeout_str,
            substitutions=cloudbuild_util.EncodeSubstitutions(
                args.substitutions, messages))
      else:
        if args.no_cache:
          raise c_exceptions.InvalidArgumentException(
              'no-cache',
              'Cannot specify --no-cache if builds/use_kaniko property is '
              'False')
        build_config = messages.Build(
            images=[args.tag],
            steps=[
                messages.BuildStep(
                    name='gcr.io/cloud-builders/docker',
                    args=['build', '--no-cache', '-t', args.tag, '.'],
                ),
            ],
            timeout=timeout_str,
            substitutions=cloudbuild_util.EncodeSubstitutions(
                args.substitutions, messages))
    elif args.config is not None:
      if args.no_cache:
        raise c_exceptions.ConflictingArgumentsException(
            '--config', '--no-cache')
      if not args.config:
        raise c_exceptions.InvalidArgumentException(
            '--config', 'Config file path must not be empty.')
      build_config = config.LoadCloudbuildConfigFromPath(
          args.config, messages, params=args.substitutions)
    else:
      raise c_exceptions.OneOfArgumentsRequiredException(
          ['--tag', '--config'],
          'Requires either a docker tag or a config file.')

    # If timeout was set by flag, overwrite the config file.
    if timeout_str:
      build_config.timeout = timeout_str

    # --no-source overrides the default --source.
    if not args.IsSpecified('source') and args.no_source:
      args.source = None

    gcs_source_staging = None
    if args.source:
      suffix = '.tgz'
      if args.source.startswith('gs://') or os.path.isfile(args.source):
        _, suffix = os.path.splitext(args.source)

      # Next, stage the source to Cloud Storage.
      staged_object = '{stamp}-{uuid}{suffix}'.format(
          stamp=times.GetTimeStampFromDateTime(times.Now()),
          uuid=uuid.uuid4().hex,
          suffix=suffix,
      )
      gcs_source_staging_dir = resources.REGISTRY.Parse(
          args.gcs_source_staging_dir, collection='storage.objects')

      # We create the bucket (if it does not exist) first. If we do an existence
      # check and then create the bucket ourselves, it would be possible for an
      # attacker to get lucky and beat us to creating the bucket. Block on this
      # creation to avoid this race condition.
      gcs_client.CreateBucketIfNotExists(gcs_source_staging_dir.bucket)

      # If no bucket is specified (for the source `default_gcs_source`), check
      # that the default bucket is also owned by the project (b/33046325).
      if default_gcs_source:
        # This request returns only the buckets owned by the project.
        bucket_list_req = gcs_client.messages.StorageBucketsListRequest(
            project=project, prefix=default_bucket_name)
        bucket_list = gcs_client.client.buckets.List(bucket_list_req)
        found_bucket = False
        for bucket in bucket_list.items:
          if bucket.id == default_bucket_name:
            found_bucket = True
            break
        if not found_bucket:
          if default_gcs_source:
            raise c_exceptions.RequiredArgumentException(
                'gcs_source_staging_dir',
                'A bucket with name {} already exists and is owned by '
                'another project. Specify a bucket using '
                '--gcs_source_staging_dir.'.format(default_bucket_name))

      if gcs_source_staging_dir.object:
        staged_object = gcs_source_staging_dir.object + '/' + staged_object
      gcs_source_staging = resources.REGISTRY.Create(
          collection='storage.objects',
          bucket=gcs_source_staging_dir.bucket,
          object=staged_object)

      if args.source.startswith('gs://'):
        gcs_source = resources.REGISTRY.Parse(
            args.source, collection='storage.objects')
        staged_source_obj = gcs_client.Rewrite(gcs_source, gcs_source_staging)
        build_config.source = messages.Source(
            storageSource=messages.StorageSource(
                bucket=staged_source_obj.bucket,
                object=staged_source_obj.name,
                generation=staged_source_obj.generation,
            ))
      else:
        if not os.path.exists(args.source):
          raise c_exceptions.BadFileException(
              'could not find source [{src}]'.format(src=args.source))
        if os.path.isdir(args.source):
          source_snapshot = snapshot.Snapshot(args.source)
          size_str = resource_transform.TransformSize(
              source_snapshot.uncompressed_size)
          log.status.Print(
              'Creating temporary tarball archive of {num_files} file(s)'
              ' totalling {size} before compression.'.format(
                  num_files=len(source_snapshot.files), size=size_str))
          staged_source_obj = source_snapshot.CopyTarballToGCS(
              gcs_client, gcs_source_staging)
          build_config.source = messages.Source(
              storageSource=messages.StorageSource(
                  bucket=staged_source_obj.bucket,
                  object=staged_source_obj.name,
                  generation=staged_source_obj.generation,
              ))
        elif os.path.isfile(args.source):
          unused_root, ext = os.path.splitext(args.source)
          if ext not in _ALLOWED_SOURCE_EXT:
            raise c_exceptions.BadFileException(
                'Local file [{src}] is none of ' +
                ', '.join(_ALLOWED_SOURCE_EXT))
          log.status.Print('Uploading local file [{src}] to '
                           '[gs://{bucket}/{object}].'.format(
                               src=args.source,
                               bucket=gcs_source_staging.bucket,
                               object=gcs_source_staging.object,
                           ))
          staged_source_obj = gcs_client.CopyFileToGCS(
              args.source, gcs_source_staging)
          build_config.source = messages.Source(
              storageSource=messages.StorageSource(
                  bucket=staged_source_obj.bucket,
                  object=staged_source_obj.name,
                  generation=staged_source_obj.generation,
              ))
    else:
      # No source
      if not args.no_source:
        raise c_exceptions.InvalidArgumentException(
            '--no-source', 'To omit source, use the --no-source flag.')

    if args.gcs_log_dir:
      gcs_log_dir = resources.REGISTRY.Parse(
          args.gcs_log_dir, collection='storage.objects')

      build_config.logsBucket = (
          'gs://' + gcs_log_dir.bucket + '/' + gcs_log_dir.object)

    # Machine type.
    if args.machine_type is not None:
      machine_type = Submit._machine_type_flag_map.GetEnumForChoice(
          args.machine_type)
      if not build_config.options:
        build_config.options = messages.BuildOptions()
      build_config.options.machineType = machine_type

    # Disk size.
    if args.disk_size is not None:
      disk_size = compute_utils.BytesToGb(args.disk_size)
      if not build_config.options:
        build_config.options = messages.BuildOptions()
      build_config.options.diskSizeGb = int(disk_size)

    log.debug('submitting build: ' + repr(build_config))

    # Start the build.
    op = client.projects_builds.Create(
        messages.CloudbuildProjectsBuildsCreateRequest(
            build=build_config, projectId=properties.VALUES.core.project.Get()))
    json = encoding.MessageToJson(op.metadata)
    build = encoding.JsonToMessage(messages.BuildOperationMetadata, json).build

    build_ref = resources.REGISTRY.Create(
        collection='cloudbuild.projects.builds',
        projectId=build.projectId,
        id=build.id)

    log.CreatedResource(build_ref)
    if build.logUrl:
      log.status.Print(
          'Logs are available at [{log_url}].'.format(log_url=build.logUrl))
    else:
      log.status.Print('Logs are available in the Cloud Console.')

    # If the command is run --async, we just print out a reference to the build.
    if args.async:
      return build

    mash_handler = execution.MashHandler(
        execution.GetCancelBuildHandler(client, messages, build_ref))

    # Otherwise, logs are streamed from GCS.
    with execution_utils.CtrlCSection(mash_handler):
      build = cb_logs.CloudBuildClient(client, messages).Stream(build_ref)

    if build.status == messages.Build.StatusValueValuesEnum.TIMEOUT:
      log.status.Print(
          'Your build timed out. Use the [--timeout=DURATION] flag to change '
          'the timeout threshold.')

    if build.status != messages.Build.StatusValueValuesEnum.SUCCESS:
      raise FailedBuildException(build)

    return build
