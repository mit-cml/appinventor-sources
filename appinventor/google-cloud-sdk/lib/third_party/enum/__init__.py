"""Python Enumerations."""

import contextlib
import os
import sys


@contextlib.contextmanager
def no_third_party_dir_on_path():
  old_path = sys.path
  try:
    # Find and remove the third_party_dir.
    third_party_path = os.path.join('lib', 'third_party')
    sys.path = [p for p in sys.path if not p.endswith(third_party_path)]
    yield
  finally:
    sys.path = old_path

if sys.version_info < (3, 4,):
  from enum.less_than_python_3_4 import *
else:
  import importlib.util
  import types
  del sys.modules['enum']
  with no_third_party_dir_on_path():
    spec = importlib.util.find_spec('enum')
    if sys.version_info < (3, 5,):
      enum_module = types.ModuleType('enum')
    else:
      enum_module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(enum_module)
    sys.modules['enum'] = enum_module
