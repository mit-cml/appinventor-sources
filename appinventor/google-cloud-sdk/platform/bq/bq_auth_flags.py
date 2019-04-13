#!/usr/bin/env python
"""Auth flags for calling BigQuery."""


import os


import gflags as flags

FLAGS = flags.FLAGS


flags.DEFINE_boolean(
    'use_gce_service_account', False,
    'Only for the gcloud wrapper use.'
)
flags.DEFINE_string(
    'credential_file',
    os.path.join(
        os.path.expanduser('~'),
        '.bigquery.v2.token'
    ),
    'Only for the gcloud wrapper use.'
)
flags.DEFINE_string(
    'application_default_credential_file', '',
    'Only for the gcloud wrapper use.'
)
flags.DEFINE_string(
    'service_account',
    '',
    'Only for the gcloud wrapper use.'
)
flags.DEFINE_string(
    'service_account_private_key_file',
    '',
    'Only for the gcloud wrapper use.'
)
flags.DEFINE_string(
    'service_account_private_key_password', 'notasecret',
    'Only for the gcloud wrapper use.'
)
flags.DEFINE_string(
    'service_account_credential_file', None,
    'Only for the gcloud wrapper use.'
)
