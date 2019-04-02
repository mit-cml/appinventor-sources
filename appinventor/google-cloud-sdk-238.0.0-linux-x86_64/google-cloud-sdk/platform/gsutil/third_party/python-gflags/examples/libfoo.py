#!/usr/bin/env python
"""Defines some flags."""

import gflags

gflags.DEFINE_integer('num_replicas', 3, 'Number of replicas to start')
gflags.DEFINE_boolean('rpc2', True, 'Turn on the usage of RPC2.')
