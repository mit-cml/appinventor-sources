# Copyright 2013 Google Inc. All Rights Reserved.



import os

if 'GAE_USE_SOCKETS_HTTPLIB' in os.environ:
  from python_std_lib import httplib
else:
  from gae_override import httplib

# Can't just do from version.httplib import * as that skips variables
# prefixed with an underscore. As this proxy should be transparent, we need
# every variable.
globals().update(httplib.__dict__)
