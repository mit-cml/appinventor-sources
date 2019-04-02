# -*- coding: utf-8 -*- #
#
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

"""Utility for lazy importing modules."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import imp
import sys
import types

import six

try:
  # pytype: disable=import-error
  # pylint: disable=g-import-not-at-top
  from importlib._bootstrap import _ImportLockContext
except ImportError:
  # _ImportLockContext not available in PY2

  class _ImportLockContext(object):
    """Context manager for the import lock."""

    def __enter__(self):
      imp.acquire_lock()

    def __exit__(self, exc_type, exc_value, exc_traceback):
      imp.release_lock()


def _find_module(module_name):
  parent_module_name, _, submodule_name = module_name.rpartition('.')
  parent_path = None
  if parent_module_name:
    parent_path = [_find_module(parent_module_name)[1]]
  # pytype: disable=wrong-arg-types
  return imp.find_module(submodule_name, parent_path)
  # pytype: enable=wrong-arg-types


def _load_module(module):
  """Load a module and its ancenstor modules as necessary."""
  if not getattr(module, 'IS_UNLOADED_LAZY_MODULE'):
    return

  with _ImportLockContext():
    module_name = six.text_type(module.__name__)
    parent_module_name, _, submodule_name = module_name.rpartition('.')

    module_class = type(module)
    module_class.IS_LOADING = True

    if parent_module_name:
      # Set the submodule attribute and force the parent module to load.
      setattr(sys.modules[parent_module_name], submodule_name, module)

      # Loading the parent may have caused the submodule to load as well.
      if not getattr(module_class, 'IS_LOADING', None):
        return

    # Can't remove inherited method, so set it to be same as __getattribute__
    module_class.__getattr__ = types.ModuleType.__getattribute__
    module_class.__setattr__ = types.ModuleType.__setattr__
    module_class.__repr__ = types.ModuleType.__repr__

    del module_class.IS_LOADING
    del module_class.IS_UNLOADED_LAZY_MODULE

    # Actually load the module from source and add all its attributes.
    module_file = getattr(module, '__file__', None)
    if module_file:
      module_file = open(module_file.name)
    module_path = getattr(module, '__path__', [None])[0]
    module_desc = getattr(module, '__desc__')
    del module.__desc__
    # pytype: disable=wrong-arg-types
    real_module = imp.load_module(
        module_name, module_file, module_path, module_desc)
    # pytype: enable=wrong-arg-types
    if module_file:
      module_file.close()
    module.__dict__.update(real_module.__dict__)


class LazyImporter(types.ModuleType):
  """Class to put in sys.modules that will import the real module if necessary.
  """

  def __init__(self, module_name, *args, **kwargs):
    # Module name must be bytes on PY2 and unicode for PY3
    module_name = str(module_name)
    # Can't call super on old-style class
    # pylint: disable=non-parent-init-called
    types.ModuleType.__init__(self, module_name, *args, **kwargs)

  def __repr__(self):
    # Useful when debugging
    return '<Lazy module: {}>'.format(self.__name__)

  def __getattr__(self, attr):
    # Don't cause module to load if accessing a submodule
    if self.__name__ + '.' + attr in sys.modules:
      return sys.modules[self.__name__ + '.' + attr]

    _load_module(self)
    return getattr(self, attr)

  def __setattr__(self, attr, value):
    _load_module(self)
    return setattr(self, attr, value)


def lazy_load_module(module_name):
  """Put a fake module class in sys.modules for lazy loading the real module.

  Args:
    module_name: The dotted path name of the module to be lazy loaded.
  Returns:
    The module that is now in sys.modules (it may have been there before).
  """
  with _ImportLockContext():
    if module_name in sys.modules:
      return sys.modules[module_name]

    # Prevent lazy loading from masking an import failure by finding the module.
    module_file, path, description = _find_module(module_name)

    class _LazyImporter(LazyImporter):
      """This subclass makes it possible to reset class functions after loading.
      """
      IS_UNLOADED_LAZY_MODULE = True
      IS_LOADING = False

    module = _LazyImporter(module_name)
    # Use ModuleType.__setattr__ to avoid triggering full loading.
    if module_file:
      module_file.close()
      types.ModuleType.__setattr__(module, '__file__', module_file)
    if path:
      types.ModuleType.__setattr__(module, '__path__', [path])
    types.ModuleType.__setattr__(module, '__desc__', description)

    # Set this lazy module as a property on the parent (possibly lazy) module
    parent_module_name, _, submodule_name = module_name.rpartition('.')
    if parent_module_name:
      parent_module = lazy_load_module(parent_module_name)
      if parent_module:
        types.ModuleType.__setattr__(parent_module, submodule_name, module)

    sys.modules[module_name] = module
    return sys.modules[module_name]
