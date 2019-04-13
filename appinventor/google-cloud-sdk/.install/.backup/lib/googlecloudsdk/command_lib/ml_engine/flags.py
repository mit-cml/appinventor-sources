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
"""Provides common arguments for the ML Engine command surface."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import argparse
import functools
import itertools
import sys

from googlecloudsdk.api_lib.ml_engine import jobs
from googlecloudsdk.api_lib.ml_engine import versions_api
from googlecloudsdk.api_lib.storage import storage_util
from googlecloudsdk.calliope import actions
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope.concepts import concepts
from googlecloudsdk.command_lib.iam import completers as iam_completers
from googlecloudsdk.command_lib.ml_engine import models_util
from googlecloudsdk.command_lib.util.apis import arg_utils
from googlecloudsdk.command_lib.util.args import repeated
from googlecloudsdk.command_lib.util.args import update_util
from googlecloudsdk.command_lib.util.concepts import concept_parsers
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core import properties


_JOB_SUMMARY = """\
table[box,title="Job Overview"](
  jobId,
  createTime,
  startTime,
  endTime,
  state,
  {INPUT},
  {OUTPUT})
"""

_JOB_TRAIN_INPUT_SUMMARY_FORMAT = """\
trainingInput:format='table[box,title="Training Input Summary"](
  runtimeVersion:optional,
  region,
  scaleTier:optional,
  pythonModule,
  parameterServerType:optional,
  parameterServerCount:optional,
  masterType:optional,
  workerType:optional,
  workerCount:optional,
  jobDir:optional
)'
"""

_JOB_TRAIN_OUTPUT_SUMMARY_FORMAT = """\
trainingOutput:format='table[box,title="Training Output Summary"](
  completedTrialCount:optional:label=TRIALS,
  consumedMLUnits:label=ML_UNITS)'
  {HP_OUTPUT}
"""

_JOB_TRAIN_OUTPUT_TRIALS_FORMAT = """\
,trainingOutput.trials.sort(trialId):format='table[box,title="Training Output Trials"](
  trialId:label=TRIAL,
  finalMetric.objectiveValue:label=OBJECTIVE_VALUE,
  finalMetric.trainingStep:label=STEP,
  hyperparameters.list(separator="\n"))'
"""

_JOB_PREDICT_INPUT_SUMMARY_FORMAT = """\
predictionInput:format='table[box,title="Predict Input Summary"](
  runtimeVersion:optional,
  region,
  model.basename():optional,
  versionName.basename(),
  outputPath,
  uri:optional,
  dataFormat,
  batchSize:optional
)'
"""

_JOB_PREDICT_OUTPUT_SUMMARY_FORMAT = """\
predictionOutput:format='table[box,title="Predict Output Summary"](
  errorCount,
  nodeHours,
  outputPath,
  predictionCount
  )'
"""


class ArgumentError(exceptions.Error):
  pass


class MlEngineIamRolesCompleter(iam_completers.IamRolesCompleter):

  def __init__(self, **kwargs):
    super(MlEngineIamRolesCompleter, self).__init__(
        resource_collection=models_util.MODELS_COLLECTION,
        resource_dest='model',
        **kwargs)


def GetDescriptionFlag(noun):
  return base.Argument(
      '--description',
      required=False,
      default=None,
      help='The description of the {noun}.'.format(noun=noun))

# Run flags
DISTRIBUTED = base.Argument(
    '--distributed',
    action='store_true',
    default=False,
    help=('Runs the provided code in distributed mode by providing cluster '
          'configurations as environment variables to subprocesses'))
PARAM_SERVERS = base.Argument(
    '--parameter-server-count',
    type=int,
    help=('Number of parameter servers with which to run. '
          'Ignored if --distributed is not specified. Default: 2'))
WORKERS = base.Argument(
    '--worker-count',
    type=int,
    help=('Number of workers with which to run. '
          'Ignored if --distributed is not specified. Default: 2'))
START_PORT = base.Argument(
    '--start-port',
    type=int,
    default=27182,
    help="""\
Start of the range of ports reserved by the local cluster. This command will use
a contiguous block of ports equal to parameter-server-count + worker-count + 1.

If --distributed is not specified, this flag is ignored.
""")


OPERATION_NAME = base.Argument('operation', help='Name of the operation.')


CONFIG = base.Argument(
    '--config',
    help="""\
Path to the job configuration file. This file should be a YAML document (JSON
also accepted) containing a Job resource as defined in the API (all fields are
optional): https://cloud.google.com/ml/reference/rest/v1/projects.jobs

EXAMPLES:\n
JSON:

  {
    "jobId": "my_job",
    "labels": {
      "type": "prod",
      "owner": "alice"
    },
    "trainingInput": {
      "scaleTier": "BASIC",
      "packageUris": [
        "gs://my/package/path"
      ],
      "region": "us-east1"
    }
  }

YAML:

  jobId: my_job
  labels:
    type: prod
    owner: alice
  trainingInput:
    scaleTier: BASIC
    packageUris:
    - gs://my/package/path
    region: us-east1



If an option is specified both in the configuration file **and** via command line
arguments, the command line arguments override the configuration file.
""")
JOB_NAME = base.Argument('job', help='Name of the job.')
PACKAGE_PATH = base.Argument(
    '--package-path',
    help="""\
Path to a Python package to build. This should point to a directory containing
the Python source for the job. It will be built using *setuptools* (which must be
installed) using its *parent* directory as context. If the parent directory
contains a `setup.py` file, the build will use that; otherwise, it will use a
simple built-in one.
""")
PACKAGES = base.Argument(
    '--packages',
    default=[],
    type=arg_parsers.ArgList(),
    metavar='PACKAGE',
    help="""\
Path to Python archives used for training. These can be local paths
(absolute or relative), in which case they will be uploaded to the Cloud
Storage bucket given by `--staging-bucket`, or Cloud Storage URLs
(`gs://bucket-name/path/to/package.tar.gz`).
""")
# As of Alpha/Beta release, the backend service processes this as a string
# (despite having only 2 valid values). It is a string here since there is no
# enum validation but can be refactored if that changes before GA.
MACHINE_TYPE = base.Argument(
    '--machine-type',
    required=False,
    help="""\
Type of machine on which to serve the model. Currently only applies to
online prediction. Currently supported machine_types are:

* `mls1-c1-m2` - A virtual machine with 1 core and 2 Gb RAM (default).
* `mls1-c4-m2` - A virtual machine with 4 core and 2 Gb RAM.
""")
ALPHA_MACHINE_TYPE = base.Argument(
    '--machine-type',
    required=False,
    help="""\
Type of machine on which to serve the model. Currently only applies to
online prediction. Currently supported machine_types are:

* `mls1-c1-m2` - A virtual machine with 1 core and 2 Gb RAM (default).
* `mls1-c4-m2` - A virtual machine with 4 core and 2 Gb RAM.
* `mls1-highmem-1` - A virtual machine with 1 core and 2 Gb RAM (will be deprecated soon).
* `mls1-highcpu-4` - A virtual machine with 4 core and 2 Gb RAM (will be deprecated soon).
""")


SERVICE_ACCOUNT = base.Argument(
    '--service-account',
    required=False,
    help='Specifies the service account for resource access control.')


def GetModuleNameFlag(required=True):
  return base.Argument(
      '--module-name',
      required=required,
      help='Name of the module to run.')


def GetJobDirFlag(upload_help=True, allow_local=False):
  """Get base.Argument() for `--job-dir`.

  If allow_local is provided, this Argument gives a str when parsed; otherwise,
  it gives a (possibly empty) ObjectReference.

  Args:
    upload_help: bool, whether to include help text related to object upload.
      Only useful in remote situations (`jobs submit training`).
    allow_local: bool, whether to allow local directories (only useful in local
      situations, like `local train`) or restrict input to directories in Cloud
      Storage.

  Returns:
    base.Argument() for the corresponding `--job-dir` flag.
  """
  help_ = """\
{dir_type} in which to store training outputs and other data
needed for training.

This path will be passed to your TensorFlow program as the `--job_dir` command-line
arg. The benefit of specifying this field is that Cloud ML Engine will validate
the path for use in training.
""".format(dir_type=('Google Cloud Storage path' +
                     (' or local_directory' if allow_local else '')))
  if upload_help:
    help_ += """\

If packages must be uploaded and `--staging-bucket` is not provided, this path
will be used instead.
"""

  if allow_local:
    type_ = str
  else:
    type_ = functools.partial(storage_util.ObjectReference.FromArgument,
                              allow_empty_object=True)  # pytype: disable=wrong-arg-types
  return base.Argument('--job-dir', type=type_, help=help_)


def GetUserArgs(local=False):
  if local:
    help_text = """\
Additional user arguments to be forwarded to user code. Any relative paths will
be relative to the *parent* directory of `--package-path`.
"""
  else:
    help_text = 'Additional user arguments to be forwarded to user code'
  return base.Argument(
      'user_args',
      nargs=argparse.REMAINDER,
      help=help_text)


VERSION_NAME = base.Argument('version', help='Name of the model version.')

RUNTIME_VERSION = base.Argument(
    '--runtime-version',
    help=(
        'Google Cloud ML Engine runtime version for this job. Defaults '
        'to a stable version, which is defined in documentation along '
        'with the list of supported versions: '
        'https://cloud.google.com/ml-engine/docs/tensorflow/runtime-version-list'  # pylint: disable=line-too-long
    ))


POLLING_INTERVAL = base.Argument(
    '--polling-interval',
    type=arg_parsers.BoundedInt(1, sys.maxsize, unlimited=True),
    required=False,
    default=60,
    action=actions.StoreProperty(properties.VALUES.ml_engine.polling_interval),
    help='Number of seconds to wait between efforts to fetch the latest '
    'log messages.')
ALLOW_MULTILINE_LOGS = base.Argument(
    '--allow-multiline-logs',
    action='store_true',
    help='Output multiline log messages as single records.')
TASK_NAME = base.Argument(
    '--task-name',
    required=False,
    default=None,
    help='If set, display only the logs for this particular task.')


_FRAMEWORK_CHOICES = {
    'TENSORFLOW': 'tensorflow',
    'SCIKIT_LEARN': 'scikit-learn',
    'XGBOOST': 'xgboost'
}
FRAMEWORK_MAPPER = arg_utils.ChoiceEnumMapper(
    '--framework',
    (versions_api.GetMessagesModule().
     GoogleCloudMlV1Version.FrameworkValueValuesEnum),
    custom_mappings=_FRAMEWORK_CHOICES,
    help_str=('The ML framework used to train this version of the model. '
              'If not specified, defaults to `tensorflow`'))


def AddPythonVersionFlag(parser, context):
  help_str = (
      'Version of Python used {context}. If not set, the default '
      'version is 2.7. Python 3.5 is available when `runtime_version` is '
      'set to 1.4 and above. Python 2.7 works with all supported runtime '
      'versions.').format(context=context)
  version = base.Argument(
      '--python-version',
      help=help_str)
  version.AddToParser(parser)


def GetModelName(positional=True, required=False):
  help_text = 'Name of the model.'
  if positional:
    return base.Argument('model', help=help_text)
  else:
    return base.Argument('--model', help=help_text, required=required)


# TODO(b/33234717): remove after PACKAGES nargs=+ deprecation period.
def ProcessPackages(args):
  """Flatten PACKAGES flag and warn if multiple arguments were used."""
  if args.packages is not None:
    if len(args.packages) > 1:
      log.warning('Use of --packages with space separated values is '
                  'deprecated and will not work in the future. Use comma '
                  'instead.')
    # flatten packages into a single list
    args.packages = list(itertools.chain.from_iterable(args.packages))


STAGING_BUCKET = base.Argument(
    '--staging-bucket',
    type=storage_util.BucketReference.FromArgument,
    help="""\
        Bucket in which to stage training archives.

        Required only if a file upload is necessary (that is, other flags
        include local paths) and no other flags implicitly specify an upload
        path.
        """)

SIGNATURE_NAME = base.Argument(
    '--signature-name',
    required=False,
    type=str,
    help="""\
    The name of the signature defined in the SavedModel to use for
    this job. Defaults to DEFAULT_SERVING_SIGNATURE_DEF_KEY in
    https://www.tensorflow.org/api_docs/python/tf/saved_model/signature_constants,
    which is "serving_default". Only applies to TensorFlow models.
    """)


def GetSummarizeFlag():
  return base.Argument(
      '--summarize',
      action='store_true',
      required=False,
      help="""\
      Summarize job output in a set of human readable tables instead of
      rendering the entire resource as json or yaml. The tables currently rendered
      are:

      * `Job Overview`: Overview of job including, jobId, status and create time.
      * `Training Input Summary`: Summary of input for a training job including
         region, main training python module and scale tier.
      * `Training Output Summary`: Summary of output for a training job including
         the amount of ML units consumed by the job.
      * `Training Output Trials`: Summary of hyperparameter trials run for a
         hyperparameter tuning training job.
      * `Predict Input Summary`: Summary of input for a prediction job including
         region, model verion and output path.
      * `Predict Output Summary`: Summary of output for a prediction job including
         prediction count and output path.

      This flag overrides the `--format` flag. If
      both are present on the command line, a warning is displayed.
      """)


def GetStandardTrainingJobSummary():
  """Get tabular format for standard ml training job."""
  return _JOB_SUMMARY.format(
      INPUT=_JOB_TRAIN_INPUT_SUMMARY_FORMAT,
      OUTPUT=_JOB_TRAIN_OUTPUT_SUMMARY_FORMAT.format(HP_OUTPUT=''))


def GetHPTrainingJobSummary():
  """Get tablular format to HyperParameter tuning ml job."""
  return _JOB_SUMMARY.format(
      INPUT=_JOB_PREDICT_INPUT_SUMMARY_FORMAT,
      OUTPUT=_JOB_TRAIN_OUTPUT_SUMMARY_FORMAT.format(
          HP_OUTPUT=_JOB_TRAIN_OUTPUT_TRIALS_FORMAT))


def GetPredictJobSummary():
  """Get table format for ml prediction job."""
  return _JOB_SUMMARY.format(
      INPUT=_JOB_PREDICT_INPUT_SUMMARY_FORMAT,
      OUTPUT=_JOB_PREDICT_OUTPUT_SUMMARY_FORMAT)


def ModelAttributeConfig():
  return concepts.ResourceParameterAttributeConfig(
      name='model',
      help_text='The model for the {resource}.')


def VersionAttributeConfig():
  return concepts.ResourceParameterAttributeConfig(
      name='version',
      help_text='The version for the {resource}.')


def GetVersionResourceSpec():
  return concepts.ResourceSpec(
      'ml.projects.models.versions',
      resource_name='version',
      versionsId=VersionAttributeConfig(),
      modelsId=ModelAttributeConfig(),
      projectsId=concepts.DEFAULT_PROJECT_ATTRIBUTE_CONFIG)


def AddVersionResourceArg(parser, verb):
  """Add a resource argument for a Cloud ML Engine version."""
  concept_parsers.ConceptParser.ForResource(
      'version',
      GetVersionResourceSpec(),
      'The Cloud ML Engine model {}.'.format(verb),
      required=True).AddToParser(parser)


def AddUserCodeArgs(parser):
  """Add args that configure user prediction code."""
  user_code_group = base.ArgumentGroup(
      help="""\
          Configure user code in prediction.

          Cloud ML Engine allows a model to have user-provided prediction
          code; these options configure that code.
          """)
  user_code_group.AddArgument(base.Argument(
      '--model-class',
      help="""\
          The fully-qualified name of the custom Model class in the package
          provided for custom prediction.

          For example, `--model-class my_package.SequenceModel`.
          """))
  user_code_group.AddArgument(base.Argument(
      '--package-uris',
      type=arg_parsers.ArgList(),
      metavar='PACKAGE_URI',
      help="""\
          Comma-separated list of Google Cloud Storage URIs (`gs://...`) for
          user-supplied Python packages to use.
          """))
  user_code_group.AddToParser(parser)


def AddUserCodeUpdateArgs(parser):
  """Add args that configure user prediction code."""
  user_code_group = parser.add_group(
      help="""\
          Configure user code in prediction.

          Cloud ML Engine allows a model to have user-provided prediction
          code; these options configure that code.
          """)
  repeated.AddPrimitiveArgs(
      user_code_group, 'version', 'package-uris',
      'user-supplied packages to use for prediction',
      additional_help=('The values should be given as a comma-separated list '
                       'of Google Cloud Storage URIs (`gs://...`).'))
  update_util.AddClearableField(
      user_code_group, 'model-class', 'custom Model class', 'version',
      ('The fully-qualified name of the custom Model class in the package '
       'provided for custom prediction.\n\n'
       'For example, `my_package.SequenceModel`.'))


def GetAcceleratorFlag():
  return base.Argument(
      '--accelerator',
      type=arg_parsers.ArgDict(spec={
          'type': str,
          'count': int,
      }, required_keys=['type', 'count']),
      help="""\
Manage the accelerator config for GPU serving. When deploying a model with the
new Alpha Google Compute Engine Machine Types, a GPU accelerator may also be selected.
You can see what accelerators are supported in what location via the
'gcloud alpha ml-engine locations list' command.

*type*::: The type of the accelerator. Choices are {}.

*count*::: The number of accelerators to attach to each machine running the job.
""".format(', '.join(["'{}'".format(c) for c in _ACCELERATOR_TYPE_MAPPER.choices])))


def ParseAcceleratorFlag(accelerator):
  """Validates and returns a accelerator config message object."""
  types = [c for c in _ACCELERATOR_TYPE_MAPPER.choices]
  if accelerator is None:
    return None
  raw_type = accelerator.get('type', None)
  if raw_type not in types:
    raise ArgumentError("""\
The type of the accelerator can only be one of the following: {}.
""".format(', '.join(["'{}'".format(c) for c in types])))
  accelerator_count = accelerator.get('count', 0)
  if accelerator_count <= 0:
    raise ArgumentError("""\
The count of the accelerator must be greater than 0.
""")
  accelerator_msg = (versions_api.GetMessagesModule().
                     GoogleCloudMlV1AcceleratorConfig)
  accelerator_type = arg_utils.ChoiceToEnum(
      raw_type, accelerator_msg.TypeValueValuesEnum)
  return accelerator_msg(
      count=accelerator_count,
      type=accelerator_type)


def AddCustomContainerFlags(parser):
  """Add Custom container flags to parser."""
  GetMasterMachineType().AddToParser(parser)
  GetMasterAccelerator().AddToParser(parser)
  GetMasterImageUri().AddToParser(parser)
  GetParameterServerMachineTypeConfig().AddToParser(parser)
  GetParameterServerAccelerator().AddToParser(parser)
  GetParameterServerImageUri().AddToParser(parser)
  GetWorkerMachineConfig().AddToParser(parser)
  GetWorkerAccelerator().AddToParser(parser)
  GetWorkerImageUri().AddToParser(parser)

# Custom Container Flags
_ACCELERATOR_TYPE_MAPPER = arg_utils.ChoiceEnumMapper(
    'generic-accelerator',
    jobs.GetMessagesModule(
    ).GoogleCloudMlV1AcceleratorConfig.TypeValueValuesEnum,
    help_str='The available types of accelerators.',
    include_filter=lambda x: x != 'ACCELERATOR_TYPE_UNSPECIFIED',
    required=False)

_ACCELERATOR_TYPE_HELP = """\
   Hardware accelerator config for the {worker_type}. Must specify
   both the accelerator type (TYPE) for each server and the number of
   accelerators to attach to each server (COUNT).
  """


def _ConvertAcceleratorTypeToEnumValue(choice_str):
  return arg_utils.ChoiceToEnum(choice_str, _ACCELERATOR_TYPE_MAPPER.enum,
                                item_type='accelerator',
                                valid_choices=_ACCELERATOR_TYPE_MAPPER.choices)


def _ValidateAcceleratorCount(accelerator_count):
  count = int(accelerator_count)
  if count <= 0:
    raise arg_parsers.ArgumentTypeError(
        'The count of the accelerator must be greater than 0.')
  return count


def _MakeAcceleratorArgConfigArg(arg_name, arg_help, required=False):
  """Creates an ArgDict representing an AcceleratorConfig message."""
  type_help = '*type*::: The type of the accelerator. Choices are {}'.format(
      ','.join(_ACCELERATOR_TYPE_MAPPER.choices))
  count_help = ('*count*::: The number of accelerators to attach to each '
                'machine running the job. Must be greater than 0.')
  arg = base.Argument(
      arg_name,
      required=required,
      type=arg_parsers.ArgDict(spec={
          'type': _ConvertAcceleratorTypeToEnumValue,
          'count': _ValidateAcceleratorCount,
      }, required_keys=['type', 'count']),
      help="""\
{arg_help}

{type_help}

{count_help}

""".format(arg_help=arg_help, type_help=type_help, count_help=count_help))
  return arg


def GetMasterMachineType():
  """Build master-machine-type flag."""
  help_text = """\
  Specifies the type of virtual machine to use for training job's master worker.

  You must set this value when `scaleTier` is set to `CUSTOM`.
  """
  return base.Argument(
      '--master-machine-type', required=False, help=help_text)


def GetMasterAccelerator():
  """Build master-accelerator flag."""
  help_text = _ACCELERATOR_TYPE_HELP.format(worker_type='master worker')
  return _MakeAcceleratorArgConfigArg(
      '--master-accelerator', arg_help=help_text)


def GetMasterImageUri():
  """Build master-image-uri flag."""
  return base.Argument(
      '--master-image-uri',
      required=False,
      help=('Docker image to run on each master worker. '
            'This image must be in Google Container Registry. Only one of '
            '`master-image-uri` and `runtimeVersion` must be specified.'))


def GetParameterServerMachineTypeConfig():
  """Build parameter-server machine type config group."""
  machine_type = base.Argument(
      '--parameter-server-machine-type',
      required=True,
      help=('Type of virtual machine to use for training job\'s'
            'parameter servers. This flag must be specified if any of the '
            'other arguments in this group are specified machine to use for '
            'training job\'s parameter servers.'))

  machine_count = base.Argument(
      '--parameter-server-count',
      type=arg_parsers.BoundedInt(1, sys.maxsize, unlimited=True),
      required=True,
      help=('The number of parameter servers to use for the training job.'))

  machine_type_group = base.ArgumentGroup(
      required=False,
      help='Configure parameter server machine type settings.')
  machine_type_group.AddArgument(machine_type)
  machine_type_group.AddArgument(machine_count)
  return machine_type_group


def GetParameterServerAccelerator():
  """Build parameter-server-accelerator flag."""
  help_text = _ACCELERATOR_TYPE_HELP.format(worker_type='parameter servers')
  return _MakeAcceleratorArgConfigArg(
      '--parameter-server-accelerator', arg_help=help_text)


def GetParameterServerImageUri():
  """Build parameter-server-image-uri flag."""
  return base.Argument(
      '--parameter-server-image-uri',
      required=False,
      help=('Docker image to run on each parameter server. '
            'This image must be in Google Container Registry. If not '
            'specified, the value of `master-image-uri` is used.'))


def GetWorkerMachineConfig():
  """Build worker machine type config group."""
  machine_type = base.Argument(
      '--worker-machine-type',
      required=True,
      help=('Type of virtual '
            'machine to use for training '
            'job\'s worker nodes.'))

  machine_count = base.Argument(
      '--worker-server-count',
      type=arg_parsers.BoundedInt(1, sys.maxsize, unlimited=True),
      required=True,
      help=('The number of worker nodes to use for the training job.'))

  machine_type_group = base.ArgumentGroup(
      required=False,
      help='Configure worker node machine type settings.')
  machine_type_group.AddArgument(machine_type)
  machine_type_group.AddArgument(machine_count)
  return machine_type_group


def GetWorkerAccelerator():
  """Build worker-accelerator flag."""
  help_text = _ACCELERATOR_TYPE_HELP.format(worker_type='worker nodes')
  return _MakeAcceleratorArgConfigArg(
      '--worker-accelerator', arg_help=help_text)


def GetWorkerImageUri():
  """Build worker-image-uri flag."""
  return base.Argument(
      '--worker-image-uri',
      required=False,
      help=('Docker image to run on each worker node. '
            'This image must be in Google Container Registry. If not '
            'specified, the value of `master-image-uri` is used.'))
