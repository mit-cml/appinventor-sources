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

"""Base template using which the apis_map.py is generated."""

from __future__ import unicode_literals


class APIDef(object):
  """Struct for info required to instantiate clients/messages for API versions.

  Attributes:
    class_path: str, Path to the package containing api related modules.
    client_classpath: str, Relative path to the client class for an API version.
    messages_modulepath: str, Relative path to the messages module for an
      API version.
    default_version: bool, Whether this API version is the default version for
    the API.
  """

  def __init__(self,
               class_path,
               client_classpath,
               messages_modulepath,
               default_version=False):
    self.class_path = class_path
    self.client_classpath = client_classpath
    self.messages_modulepath = messages_modulepath
    self.default_version = default_version

  @property
  def client_full_classpath(self):
    return self.class_path + '.' + self.client_classpath

  @property
  def messages_full_modulepath(self):
    return self.class_path + '.' + self.messages_modulepath

  def __eq__(self, other):
    return (isinstance(other, self.__class__)
            and self.__dict__ == other.__dict__)

  def __ne__(self, other):
    return not self.__eq__(other)

  def get_init_source(self):
    src_fmt = 'APIDef("{0}", "{1}", "{2}", {3})'
    return src_fmt.format(self.class_path,
                          self.client_classpath,
                          self.messages_modulepath,
                          self.default_version)

  def __repr__(self):
    return self.get_init_source()


MAP = {
    'accesscontextmanager': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.accesscontextmanager.v1',
            client_classpath='accesscontextmanager_v1_client.AccesscontextmanagerV1',
            messages_modulepath='accesscontextmanager_v1_messages',
            default_version=False
        ),
        'v1alpha': APIDef(
            class_path='googlecloudsdk.third_party.apis.accesscontextmanager.v1alpha',
            client_classpath='accesscontextmanager_v1alpha_client.AccesscontextmanagerV1alpha',
            messages_modulepath='accesscontextmanager_v1alpha_messages',
            default_version=False
        ),
        'v1beta': APIDef(
            class_path='googlecloudsdk.third_party.apis.accesscontextmanager.v1beta',
            client_classpath='accesscontextmanager_v1beta_client.AccesscontextmanagerV1beta',
            messages_modulepath='accesscontextmanager_v1beta_messages',
            default_version=True
        ),
    },
    'appengine': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.appengine.v1',
            client_classpath='appengine_v1_client.AppengineV1',
            messages_modulepath='appengine_v1_messages',
            default_version=True
        ),
        'v1alpha': APIDef(
            class_path='googlecloudsdk.third_party.apis.appengine.v1alpha',
            client_classpath='appengine_v1alpha_client.AppengineV1alpha',
            messages_modulepath='appengine_v1alpha_messages',
            default_version=False
        ),
        'v1beta': APIDef(
            class_path='googlecloudsdk.third_party.apis.appengine.v1beta',
            client_classpath='appengine_v1beta_client.AppengineV1beta',
            messages_modulepath='appengine_v1beta_messages',
            default_version=False
        ),
    },
    'bigquery': {
        'v2': APIDef(
            class_path='googlecloudsdk.third_party.apis.bigquery.v2',
            client_classpath='bigquery_v2_client.BigqueryV2',
            messages_modulepath='bigquery_v2_messages',
            default_version=True
        ),
    },
    'bigquerydatatransfer': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.bigquerydatatransfer.v1',
            client_classpath='bigquerydatatransfer_v1_client.BigquerydatatransferV1',
            messages_modulepath='bigquerydatatransfer_v1_messages',
            default_version=True
        ),
    },
    'bigtableadmin': {
        'v2': APIDef(
            class_path='googlecloudsdk.third_party.apis.bigtableadmin.v2',
            client_classpath='bigtableadmin_v2_client.BigtableadminV2',
            messages_modulepath='bigtableadmin_v2_messages',
            default_version=True
        ),
    },
    'bigtableclusteradmin': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.bigtableclusteradmin.v1',
            client_classpath='bigtableclusteradmin_v1_client.BigtableclusteradminV1',
            messages_modulepath='bigtableclusteradmin_v1_messages',
            default_version=True
        ),
    },
    'binaryauthorization': {
        'v1alpha2': APIDef(
            class_path='googlecloudsdk.third_party.apis.binaryauthorization.v1alpha2',
            client_classpath='binaryauthorization_v1alpha2_client.BinaryauthorizationV1alpha2',
            messages_modulepath='binaryauthorization_v1alpha2_messages',
            default_version=True
        ),
        'v1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.binaryauthorization.v1beta1',
            client_classpath='binaryauthorization_v1beta1_client.BinaryauthorizationV1beta1',
            messages_modulepath='binaryauthorization_v1beta1_messages',
            default_version=False
        ),
    },
    'bio': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.bio.v1',
            client_classpath='bio_v1_client.BioV1',
            messages_modulepath='bio_v1_messages',
            default_version=True
        ),
    },
    'cloudasset': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudasset.v1',
            client_classpath='cloudasset_v1_client.CloudassetV1',
            messages_modulepath='cloudasset_v1_messages',
            default_version=False
        ),
        'v1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudasset.v1beta1',
            client_classpath='cloudasset_v1beta1_client.CloudassetV1beta1',
            messages_modulepath='cloudasset_v1beta1_messages',
            default_version=True
        ),
    },
    'cloudbilling': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudbilling.v1',
            client_classpath='cloudbilling_v1_client.CloudbillingV1',
            messages_modulepath='cloudbilling_v1_messages',
            default_version=True
        ),
    },
    'cloudbuild': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudbuild.v1',
            client_classpath='cloudbuild_v1_client.CloudbuildV1',
            messages_modulepath='cloudbuild_v1_messages',
            default_version=True
        ),
        'v1alpha1': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudbuild.v1alpha1',
            client_classpath='cloudbuild_v1alpha1_client.CloudbuildV1alpha1',
            messages_modulepath='cloudbuild_v1alpha1_messages',
            default_version=False
        ),
    },
    'clouddebugger': {
        'v2': APIDef(
            class_path='googlecloudsdk.third_party.apis.clouddebugger.v2',
            client_classpath='clouddebugger_v2_client.ClouddebuggerV2',
            messages_modulepath='clouddebugger_v2_messages',
            default_version=True
        ),
    },
    'clouderrorreporting': {
        'v1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.clouderrorreporting.v1beta1',
            client_classpath='clouderrorreporting_v1beta1_client.ClouderrorreportingV1beta1',
            messages_modulepath='clouderrorreporting_v1beta1_messages',
            default_version=True
        ),
    },
    'cloudfunctions': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudfunctions.v1',
            client_classpath='cloudfunctions_v1_client.CloudfunctionsV1',
            messages_modulepath='cloudfunctions_v1_messages',
            default_version=True
        ),
    },
    'cloudiot': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudiot.v1',
            client_classpath='cloudiot_v1_client.CloudiotV1',
            messages_modulepath='cloudiot_v1_messages',
            default_version=True
        ),
    },
    'cloudkms': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudkms.v1',
            client_classpath='cloudkms_v1_client.CloudkmsV1',
            messages_modulepath='cloudkms_v1_messages',
            default_version=True
        ),
    },
    'cloudresourcemanager': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudresourcemanager.v1',
            client_classpath='cloudresourcemanager_v1_client.CloudresourcemanagerV1',
            messages_modulepath='cloudresourcemanager_v1_messages',
            default_version=True
        ),
        'v1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudresourcemanager.v1beta1',
            client_classpath='cloudresourcemanager_v1beta1_client.CloudresourcemanagerV1beta1',
            messages_modulepath='cloudresourcemanager_v1beta1_messages',
            default_version=False
        ),
        'v2': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudresourcemanager.v2',
            client_classpath='cloudresourcemanager_v2_client.CloudresourcemanagerV2',
            messages_modulepath='cloudresourcemanager_v2_messages',
            default_version=False
        ),
        'v2alpha1': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudresourcemanager.v2alpha1',
            client_classpath='cloudresourcemanager_v2alpha1_client.CloudresourcemanagerV2alpha1',
            messages_modulepath='cloudresourcemanager_v2alpha1_messages',
            default_version=False
        ),
        'v2beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudresourcemanager.v2beta1',
            client_classpath='cloudresourcemanager_v2beta1_client.CloudresourcemanagerV2beta1',
            messages_modulepath='cloudresourcemanager_v2beta1_messages',
            default_version=False
        ),
    },
    'cloudresourcesearch': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudresourcesearch.v1',
            client_classpath='cloudresourcesearch_v1_client.CloudresourcesearchV1',
            messages_modulepath='cloudresourcesearch_v1_messages',
            default_version=True
        ),
    },
    'cloudscheduler': {
        'v1alpha1': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudscheduler.v1alpha1',
            client_classpath='cloudscheduler_v1alpha1_client.CloudschedulerV1alpha1',
            messages_modulepath='cloudscheduler_v1alpha1_messages',
            default_version=False
        ),
        'v1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudscheduler.v1beta1',
            client_classpath='cloudscheduler_v1beta1_client.CloudschedulerV1beta1',
            messages_modulepath='cloudscheduler_v1beta1_messages',
            default_version=True
        ),
    },
    'cloudshell': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudshell.v1',
            client_classpath='cloudshell_v1_client.CloudshellV1',
            messages_modulepath='cloudshell_v1_messages',
            default_version=False
        ),
        'v1alpha1': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudshell.v1alpha1',
            client_classpath='cloudshell_v1alpha1_client.CloudshellV1alpha1',
            messages_modulepath='cloudshell_v1alpha1_messages',
            default_version=True
        ),
    },
    'cloudtasks': {
        'v2beta2': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudtasks.v2beta2',
            client_classpath='cloudtasks_v2beta2_client.CloudtasksV2beta2',
            messages_modulepath='cloudtasks_v2beta2_messages',
            default_version=True
        ),
        'v2beta3': APIDef(
            class_path='googlecloudsdk.third_party.apis.cloudtasks.v2beta3',
            client_classpath='cloudtasks_v2beta3_client.CloudtasksV2beta3',
            messages_modulepath='cloudtasks_v2beta3_messages',
            default_version=False
        ),
    },
    'composer': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.composer.v1',
            client_classpath='composer_v1_client.ComposerV1',
            messages_modulepath='composer_v1_messages',
            default_version=True
        ),
        'v1alpha2': APIDef(
            class_path='googlecloudsdk.third_party.apis.composer.v1alpha2',
            client_classpath='composer_v1alpha2_client.ComposerV1alpha2',
            messages_modulepath='composer_v1alpha2_messages',
            default_version=False
        ),
        'v1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.composer.v1beta1',
            client_classpath='composer_v1beta1_client.ComposerV1beta1',
            messages_modulepath='composer_v1beta1_messages',
            default_version=False
        ),
    },
    'compute': {
        'alpha': APIDef(
            class_path='googlecloudsdk.third_party.apis.compute.alpha',
            client_classpath='compute_alpha_client.ComputeAlpha',
            messages_modulepath='compute_alpha_messages',
            default_version=False
        ),
        'beta': APIDef(
            class_path='googlecloudsdk.third_party.apis.compute.beta',
            client_classpath='compute_beta_client.ComputeBeta',
            messages_modulepath='compute_beta_messages',
            default_version=False
        ),
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.compute.v1',
            client_classpath='compute_v1_client.ComputeV1',
            messages_modulepath='compute_v1_messages',
            default_version=True
        ),
    },
    'container': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.container.v1',
            client_classpath='container_v1_client.ContainerV1',
            messages_modulepath='container_v1_messages',
            default_version=True
        ),
        'v1alpha1': APIDef(
            class_path='googlecloudsdk.third_party.apis.container.v1alpha1',
            client_classpath='container_v1alpha1_client.ContainerV1alpha1',
            messages_modulepath='container_v1alpha1_messages',
            default_version=False
        ),
        'v1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.container.v1beta1',
            client_classpath='container_v1beta1_client.ContainerV1beta1',
            messages_modulepath='container_v1beta1_messages',
            default_version=False
        ),
    },
    'containeranalysis': {
        'v1alpha1': APIDef(
            class_path='googlecloudsdk.third_party.apis.containeranalysis.v1alpha1',
            client_classpath='containeranalysis_v1alpha1_client.ContaineranalysisV1alpha1',
            messages_modulepath='containeranalysis_v1alpha1_messages',
            default_version=True
        ),
        'v1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.containeranalysis.v1beta1',
            client_classpath='containeranalysis_v1beta1_client.ContaineranalysisV1beta1',
            messages_modulepath='containeranalysis_v1beta1_messages',
            default_version=False
        ),
    },
    'datacatalog': {
        'v1alpha3': APIDef(
            class_path='googlecloudsdk.third_party.apis.datacatalog.v1alpha3',
            client_classpath='datacatalog_v1alpha3_client.DatacatalogV1alpha3',
            messages_modulepath='datacatalog_v1alpha3_messages',
            default_version=True
        ),
    },
    'dataflow': {
        'v1b3': APIDef(
            class_path='googlecloudsdk.third_party.apis.dataflow.v1b3',
            client_classpath='dataflow_v1b3_client.DataflowV1b3',
            messages_modulepath='dataflow_v1b3_messages',
            default_version=True
        ),
    },
    'dataproc': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.dataproc.v1',
            client_classpath='dataproc_v1_client.DataprocV1',
            messages_modulepath='dataproc_v1_messages',
            default_version=True
        ),
        'v1beta2': APIDef(
            class_path='googlecloudsdk.third_party.apis.dataproc.v1beta2',
            client_classpath='dataproc_v1beta2_client.DataprocV1beta2',
            messages_modulepath='dataproc_v1beta2_messages',
            default_version=False
        ),
    },
    'datastore': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.datastore.v1',
            client_classpath='datastore_v1_client.DatastoreV1',
            messages_modulepath='datastore_v1_messages',
            default_version=True
        ),
        'v1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.datastore.v1beta1',
            client_classpath='datastore_v1beta1_client.DatastoreV1beta1',
            messages_modulepath='datastore_v1beta1_messages',
            default_version=False
        ),
    },
    'deploymentmanager': {
        'alpha': APIDef(
            class_path='googlecloudsdk.third_party.apis.deploymentmanager.alpha',
            client_classpath='deploymentmanager_alpha_client.DeploymentmanagerAlpha',
            messages_modulepath='deploymentmanager_alpha_messages',
            default_version=False
        ),
        'v2': APIDef(
            class_path='googlecloudsdk.third_party.apis.deploymentmanager.v2',
            client_classpath='deploymentmanager_v2_client.DeploymentmanagerV2',
            messages_modulepath='deploymentmanager_v2_messages',
            default_version=True
        ),
        'v2beta': APIDef(
            class_path='googlecloudsdk.third_party.apis.deploymentmanager.v2beta',
            client_classpath='deploymentmanager_v2beta_client.DeploymentmanagerV2beta',
            messages_modulepath='deploymentmanager_v2beta_messages',
            default_version=False
        ),
    },
    'discovery': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.discovery.v1',
            client_classpath='discovery_v1_client.DiscoveryV1',
            messages_modulepath='discovery_v1_messages',
            default_version=True
        ),
    },
    'dlp': {
        'v2': APIDef(
            class_path='googlecloudsdk.third_party.apis.dlp.v2',
            client_classpath='dlp_v2_client.DlpV2',
            messages_modulepath='dlp_v2_messages',
            default_version=True
        ),
    },
    'dns': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.dns.v1',
            client_classpath='dns_v1_client.DnsV1',
            messages_modulepath='dns_v1_messages',
            default_version=True
        ),
        'v1beta2': APIDef(
            class_path='googlecloudsdk.third_party.apis.dns.v1beta2',
            client_classpath='dns_v1beta2_client.DnsV1beta2',
            messages_modulepath='dns_v1beta2_messages',
            default_version=False
        ),
    },
    'edge': {
        'v1alpha1': APIDef(
            class_path='googlecloudsdk.third_party.apis.edge.v1alpha1',
            client_classpath='edge_v1alpha1_client.EdgeV1alpha1',
            messages_modulepath='edge_v1alpha1_messages',
            default_version=True
        ),
    },
    'edgeml': {
        'v1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.edgeml.v1beta1',
            client_classpath='edgeml_v1beta1_client.EdgemlV1beta1',
            messages_modulepath='edgeml_v1beta1_messages',
            default_version=True
        ),
    },
    'eventflow': {
        'v1beta2': APIDef(
            class_path='googlecloudsdk.third_party.apis.eventflow.v1beta2',
            client_classpath='eventflow_v1beta2_client.EventflowV1beta2',
            messages_modulepath='eventflow_v1beta2_messages',
            default_version=True
        ),
    },
    'file': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.file.v1',
            client_classpath='file_v1_client.FileV1',
            messages_modulepath='file_v1_messages',
            default_version=False
        ),
        'v1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.file.v1beta1',
            client_classpath='file_v1beta1_client.FileV1beta1',
            messages_modulepath='file_v1beta1_messages',
            default_version=False
        ),
        'v1p1alpha1': APIDef(
            class_path='googlecloudsdk.third_party.apis.file.v1p1alpha1',
            client_classpath='file_v1p1alpha1_client.FileV1p1alpha1',
            messages_modulepath='file_v1p1alpha1_messages',
            default_version=True
        ),
    },
    'firestore': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.firestore.v1',
            client_classpath='firestore_v1_client.FirestoreV1',
            messages_modulepath='firestore_v1_messages',
            default_version=False
        ),
        'v1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.firestore.v1beta1',
            client_classpath='firestore_v1beta1_client.FirestoreV1beta1',
            messages_modulepath='firestore_v1beta1_messages',
            default_version=False
        ),
        'v1beta2': APIDef(
            class_path='googlecloudsdk.third_party.apis.firestore.v1beta2',
            client_classpath='firestore_v1beta2_client.FirestoreV1beta2',
            messages_modulepath='firestore_v1beta2_messages',
            default_version=True
        ),
    },
    'genomics': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.genomics.v1',
            client_classpath='genomics_v1_client.GenomicsV1',
            messages_modulepath='genomics_v1_messages',
            default_version=True
        ),
        'v1alpha2': APIDef(
            class_path='googlecloudsdk.third_party.apis.genomics.v1alpha2',
            client_classpath='genomics_v1alpha2_client.GenomicsV1alpha2',
            messages_modulepath='genomics_v1alpha2_messages',
            default_version=False
        ),
        'v2alpha1': APIDef(
            class_path='googlecloudsdk.third_party.apis.genomics.v2alpha1',
            client_classpath='genomics_v2alpha1_client.GenomicsV2alpha1',
            messages_modulepath='genomics_v2alpha1_messages',
            default_version=False
        ),
    },
    'gkehub': {
        'v1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.gkehub.v1beta1',
            client_classpath='gkehub_v1beta1_client.GkehubV1beta1',
            messages_modulepath='gkehub_v1beta1_messages',
            default_version=True
        ),
    },
    'healthcare': {
        'v1alpha': APIDef(
            class_path='googlecloudsdk.third_party.apis.healthcare.v1alpha',
            client_classpath='healthcare_v1alpha_client.HealthcareV1alpha',
            messages_modulepath='healthcare_v1alpha_messages',
            default_version=False
        ),
        'v1alpha2': APIDef(
            class_path='googlecloudsdk.third_party.apis.healthcare.v1alpha2',
            client_classpath='healthcare_v1alpha2_client.HealthcareV1alpha2',
            messages_modulepath='healthcare_v1alpha2_messages',
            default_version=True
        ),
    },
    'iam': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.iam.v1',
            client_classpath='iam_v1_client.IamV1',
            messages_modulepath='iam_v1_messages',
            default_version=True
        ),
    },
    'iamcredentials': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.iamcredentials.v1',
            client_classpath='iamcredentials_v1_client.IamcredentialsV1',
            messages_modulepath='iamcredentials_v1_messages',
            default_version=True
        ),
    },
    'iap': {
        'v1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.iap.v1beta1',
            client_classpath='iap_v1beta1_client.IapV1beta1',
            messages_modulepath='iap_v1beta1_messages',
            default_version=True
        ),
    },
    'language': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.language.v1',
            client_classpath='language_v1_client.LanguageV1',
            messages_modulepath='language_v1_messages',
            default_version=True
        ),
        'v1beta2': APIDef(
            class_path='googlecloudsdk.third_party.apis.language.v1beta2',
            client_classpath='language_v1beta2_client.LanguageV1beta2',
            messages_modulepath='language_v1beta2_messages',
            default_version=False
        ),
    },
    'logging': {
        'v2': APIDef(
            class_path='googlecloudsdk.third_party.apis.logging.v2',
            client_classpath='logging_v2_client.LoggingV2',
            messages_modulepath='logging_v2_messages',
            default_version=True
        ),
    },
    'ml': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.ml.v1',
            client_classpath='ml_v1_client.MlV1',
            messages_modulepath='ml_v1_messages',
            default_version=True
        ),
    },
    'monitoring': {
        'v3': APIDef(
            class_path='googlecloudsdk.third_party.apis.monitoring.v3',
            client_classpath='monitoring_v3_client.MonitoringV3',
            messages_modulepath='monitoring_v3_messages',
            default_version=True
        ),
    },
    'oslogin': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.oslogin.v1',
            client_classpath='oslogin_v1_client.OsloginV1',
            messages_modulepath='oslogin_v1_messages',
            default_version=True
        ),
        'v1alpha': APIDef(
            class_path='googlecloudsdk.third_party.apis.oslogin.v1alpha',
            client_classpath='oslogin_v1alpha_client.OsloginV1alpha',
            messages_modulepath='oslogin_v1alpha_messages',
            default_version=False
        ),
        'v1beta': APIDef(
            class_path='googlecloudsdk.third_party.apis.oslogin.v1beta',
            client_classpath='oslogin_v1beta_client.OsloginV1beta',
            messages_modulepath='oslogin_v1beta_messages',
            default_version=False
        ),
    },
    'pubsub': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.pubsub.v1',
            client_classpath='pubsub_v1_client.PubsubV1',
            messages_modulepath='pubsub_v1_messages',
            default_version=True
        ),
    },
    'redis': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.redis.v1',
            client_classpath='redis_v1_client.RedisV1',
            messages_modulepath='redis_v1_messages',
            default_version=True
        ),
        'v1alpha1': APIDef(
            class_path='googlecloudsdk.third_party.apis.redis.v1alpha1',
            client_classpath='redis_v1alpha1_client.RedisV1alpha1',
            messages_modulepath='redis_v1alpha1_messages',
            default_version=False
        ),
        'v1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.redis.v1beta1',
            client_classpath='redis_v1beta1_client.RedisV1beta1',
            messages_modulepath='redis_v1beta1_messages',
            default_version=False
        ),
    },
    'remotebuildexecution': {
        'v1alpha': APIDef(
            class_path='googlecloudsdk.third_party.apis.remotebuildexecution.v1alpha',
            client_classpath='remotebuildexecution_v1alpha_client.RemotebuildexecutionV1alpha',
            messages_modulepath='remotebuildexecution_v1alpha_messages',
            default_version=True
        ),
    },
    'run': {
        'v1alpha1': APIDef(
            class_path='googlecloudsdk.third_party.apis.run.v1alpha1',
            client_classpath='run_v1alpha1_client.RunV1alpha1',
            messages_modulepath='run_v1alpha1_messages',
            default_version=True
        ),
    },
    'runtimeconfig': {
        'v1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.runtimeconfig.v1beta1',
            client_classpath='runtimeconfig_v1beta1_client.RuntimeconfigV1beta1',
            messages_modulepath='runtimeconfig_v1beta1_messages',
            default_version=True
        ),
    },
    'securitycenter': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.securitycenter.v1',
            client_classpath='securitycenter_v1_client.SecuritycenterV1',
            messages_modulepath='securitycenter_v1_messages',
            default_version=True
        ),
    },
    'serverless': {
        'v1alpha1': APIDef(
            class_path='googlecloudsdk.third_party.apis.serverless.v1alpha1',
            client_classpath='serverless_v1alpha1_client.ServerlessV1alpha1',
            messages_modulepath='serverless_v1alpha1_messages',
            default_version=True
        ),
    },
    'servicemanagement': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.servicemanagement.v1',
            client_classpath='servicemanagement_v1_client.ServicemanagementV1',
            messages_modulepath='servicemanagement_v1_messages',
            default_version=True
        ),
    },
    'servicenetworking': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.servicenetworking.v1',
            client_classpath='servicenetworking_v1_client.ServicenetworkingV1',
            messages_modulepath='servicenetworking_v1_messages',
            default_version=True
        ),
        'v1beta': APIDef(
            class_path='googlecloudsdk.third_party.apis.servicenetworking.v1beta',
            client_classpath='servicenetworking_v1beta_client.ServicenetworkingV1beta',
            messages_modulepath='servicenetworking_v1beta_messages',
            default_version=False
        ),
    },
    'serviceusage': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.serviceusage.v1',
            client_classpath='serviceusage_v1_client.ServiceusageV1',
            messages_modulepath='serviceusage_v1_messages',
            default_version=True
        ),
        'v1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.serviceusage.v1beta1',
            client_classpath='serviceusage_v1beta1_client.ServiceusageV1beta1',
            messages_modulepath='serviceusage_v1beta1_messages',
            default_version=False
        ),
    },
    'serviceuser': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.serviceuser.v1',
            client_classpath='serviceuser_v1_client.ServiceuserV1',
            messages_modulepath='serviceuser_v1_messages',
            default_version=True
        ),
    },
    'source': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.source.v1',
            client_classpath='source_v1_client.SourceV1',
            messages_modulepath='source_v1_messages',
            default_version=True
        ),
    },
    'sourcerepo': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.sourcerepo.v1',
            client_classpath='sourcerepo_v1_client.SourcerepoV1',
            messages_modulepath='sourcerepo_v1_messages',
            default_version=True
        ),
    },
    'spanner': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.spanner.v1',
            client_classpath='spanner_v1_client.SpannerV1',
            messages_modulepath='spanner_v1_messages',
            default_version=True
        ),
    },
    'speech': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.speech.v1',
            client_classpath='speech_v1_client.SpeechV1',
            messages_modulepath='speech_v1_messages',
            default_version=True
        ),
        'v1p1beta1': APIDef(
            class_path='googlecloudsdk.third_party.apis.speech.v1p1beta1',
            client_classpath='speech_v1p1beta1_client.SpeechV1p1beta1',
            messages_modulepath='speech_v1p1beta1_messages',
            default_version=False
        ),
    },
    'sqladmin': {
        'v1beta3': APIDef(
            class_path='googlecloudsdk.third_party.apis.sqladmin.v1beta3',
            client_classpath='sqladmin_v1beta3_client.SqladminV1beta3',
            messages_modulepath='sqladmin_v1beta3_messages',
            default_version=True
        ),
        'v1beta4': APIDef(
            class_path='googlecloudsdk.third_party.apis.sqladmin.v1beta4',
            client_classpath='sqladmin_v1beta4_client.SqladminV1beta4',
            messages_modulepath='sqladmin_v1beta4_messages',
            default_version=False
        ),
    },
    'storage': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.storage.v1',
            client_classpath='storage_v1_client.StorageV1',
            messages_modulepath='storage_v1_messages',
            default_version=True
        ),
    },
    'testing': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.testing.v1',
            client_classpath='testing_v1_client.TestingV1',
            messages_modulepath='testing_v1_messages',
            default_version=True
        ),
    },
    'toolresults': {
        'v1beta3': APIDef(
            class_path='googlecloudsdk.third_party.apis.toolresults.v1beta3',
            client_classpath='toolresults_v1beta3_client.ToolresultsV1beta3',
            messages_modulepath='toolresults_v1beta3_messages',
            default_version=True
        ),
    },
    'tpu': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.tpu.v1',
            client_classpath='tpu_v1_client.TpuV1',
            messages_modulepath='tpu_v1_messages',
            default_version=False
        ),
        'v1alpha1': APIDef(
            class_path='googlecloudsdk.third_party.apis.tpu.v1alpha1',
            client_classpath='tpu_v1alpha1_client.TpuV1alpha1',
            messages_modulepath='tpu_v1alpha1_messages',
            default_version=True
        ),
    },
    'videointelligence': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.videointelligence.v1',
            client_classpath='videointelligence_v1_client.VideointelligenceV1',
            messages_modulepath='videointelligence_v1_messages',
            default_version=True
        ),
    },
    'vision': {
        'v1': APIDef(
            class_path='googlecloudsdk.third_party.apis.vision.v1',
            client_classpath='vision_v1_client.VisionV1',
            messages_modulepath='vision_v1_messages',
            default_version=True
        ),
    },
    'vpcaccess': {
        'v1alpha1': APIDef(
            class_path='googlecloudsdk.third_party.apis.vpcaccess.v1alpha1',
            client_classpath='vpcaccess_v1alpha1_client.VpcaccessV1alpha1',
            messages_modulepath='vpcaccess_v1alpha1_messages',
            default_version=True
        ),
    },
    'websecurityscanner': {
        'v1beta': APIDef(
            class_path='googlecloudsdk.third_party.apis.websecurityscanner.v1beta',
            client_classpath='websecurityscanner_v1beta_client.WebsecurityscannerV1beta',
            messages_modulepath='websecurityscanner_v1beta_messages',
            default_version=True
        ),
    },
}
