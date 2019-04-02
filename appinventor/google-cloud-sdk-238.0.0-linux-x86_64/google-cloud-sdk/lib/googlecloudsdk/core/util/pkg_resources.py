# -*- coding: utf-8 -*- #
# Copyright 2014 Google Inc. All Rights Reserved.
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

"""Utilities for accessing local pakage resources."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import imp
import os
import pkgutil
import sys

from googlecloudsdk.core.util import files

import six


def _GetPackageName(module_name):
  """Returns package name for given module name."""
  last_dot_idx = module_name.rfind('.')
  if last_dot_idx > 0:
    return module_name[:last_dot_idx]
  return ''


def GetResource(module_name, resource_name):
  """Get a resource as a byte string for given resource in same package."""
  return pkgutil.get_data(_GetPackageName(module_name), resource_name)


def GetResourceFromFile(path):
  """Gets the given resource as a byte string.

  This is similar to GetResource(), but uses file paths instead of module names.

  Args:
    path: str, filesystem like path to a file/resource.

  Returns:
    The contents of the resource as a byte string.

  Raises:
    IOError: if resource is not found under given path.
  """
  if os.path.isfile(path):
    return files.ReadBinaryFileContents(path)

  importer = pkgutil.get_importer(os.path.dirname(path))
  if hasattr(importer, 'get_data'):
    return importer.get_data(path)

  raise IOError('File not found {0}'.format(path))


def IsImportable(name, path):
  """Checks if given name can be imported at given path.

  Args:
    name: str, module name without '.' or suffixes.
    path: str, filesystem path to location of the module.

  Returns:
    True, if name is importable.
  """

  if os.path.isdir(path):
    if not os.path.isfile(os.path.join(path, '__init__.py')):
      return path in sys.path
    name_path = os.path.join(path, name)
    if os.path.isdir(name_path):
      # Subdirectory is considered subpackage if it has __init__.py file.
      return os.path.isfile(os.path.join(name_path, '__init__.py'))
    return os.path.exists(name_path + '.py')

  try:
    result = imp.find_module(name, [path])
    if result:
      return True
  except ImportError:
    pass

  if not hasattr(pkgutil, 'get_importer'):
    return False

  name_path = name.split('.')
  importer = pkgutil.get_importer(os.path.join(path, *name_path[:-1]))

  return importer and importer.find_module(name_path[-1])


def _GetPathRoot(path):
  """Returns longest path from sys.path which is prefix of given path."""

  longest_path = ''
  for p in sys.path:
    if path.startswith(p) and len(longest_path) < len(p):
      longest_path = p
  return longest_path


def GetModuleFromPath(name_to_give, module_path):
  """Loads module at given path under given name.

  Note that it also updates sys.modules with name_to_give.

  Args:
    name_to_give: str, name to assign to loaded module
    module_path: str, python path to location of the module, this is either
        filesystem path or path into egg or zip package

  Returns:
    Imported module

  Raises:
    ImportError: if module cannot be imported.
  """
  module_dir, module_name = os.path.split(module_path)
  try:
    result = imp.find_module(module_name, [module_dir])
  except ImportError:
    # imp.find_module does not respects PEP 302 import hooks, and does not work
    # over package archives. Try pkgutil import hooks.
    return _GetModuleFromPathViaPkgutil(module_path, name_to_give)
  else:
    try:
      f, file_path, items = result  # pytype: disable=attribute-error
      module = imp.load_module(name_to_give, f, file_path, items)
      if module.__name__ not in sys.modules:
        # Python 2.6 does not add this to sys.modules. This is to make sure
        # we get uniform behaviour with 2.7.
        sys.modules[module.__name__] = module
      return module
    finally:
      if f:
        f.close()


def _GetModuleFromPathViaPkgutil(module_path, name_to_give):
  """Loads module by using pkgutil.get_importer mechanism."""
  importer = pkgutil.get_importer(os.path.dirname(module_path))
  if importer:
    if hasattr(importer, '_par'):
      # par zipimporters must have full path from the zip root.
      # pylint:disable=protected-access
      module_name = '.'.join(
          module_path[len(importer._par._zip_filename) + 1:].split(os.sep))
    else:
      module_name = os.path.basename(module_path)

    if importer.find_module(module_name):
      return _LoadModule(importer, module_path, module_name, name_to_give)

  raise ImportError('{0} not found'.format(module_path))


def _LoadModule(importer, module_path, module_name, name_to_give):
  """Loads the module or package under given name."""
  code = importer.get_code(module_name)
  module = imp.new_module(name_to_give)
  package_path_parts = name_to_give.split('.')
  if importer.is_package(module_name):
    module.__path__ = [module_path]
    module.__file__ = os.path.join(module_path, '__init__.pyc')
  else:
    package_path_parts.pop()  # Don't treat module as a package.
    module.__file__ = module_path + '.pyc'

  # Define package if it does not exists.
  if six.PY2:
    # This code does not affect the official installations of the cloud sdk.
    # This function does not work on python 3, but removing this call will
    # generate runtime warnings when running gcloud as a zip archive. So we keep
    # this call in python 2 so it can continue to work as intended.
    imp.load_module('.'.join(package_path_parts), None,
                    os.path.join(_GetPathRoot(module_path),
                                 *package_path_parts),
                    ('', '', imp.PKG_DIRECTORY))  # pytype: disable=wrong-arg-types

  # pylint: disable=exec-used
  exec(code, module.__dict__)
  sys.modules[name_to_give] = module
  return module


def _IterModules(file_list, extra_extensions, prefix=None):
  """Yields module names from given list of file paths with given prefix."""
  yielded = set()
  if extra_extensions is None:
    extra_extensions = []
  if prefix is None:
    prefix = ''
  for file_path in file_list:
    if not file_path.startswith(prefix):
      continue

    file_path_parts = file_path[len(prefix):].split(os.sep)

    if (len(file_path_parts) == 2
        and file_path_parts[1].startswith('__init__.py')):
      if file_path_parts[0] not in yielded:
        yielded.add(file_path_parts[0])
        yield file_path_parts[0], True

    if len(file_path_parts) != 1:
      continue

    filename = os.path.basename(file_path_parts[0])
    modname, ext = os.path.splitext(filename)
    if modname == '__init__' or (ext != '.py' and ext not in extra_extensions):
      continue

    to_yield = modname if ext == '.py' else filename
    if '.' not in modname and to_yield not in yielded:
      yielded.add(to_yield)
      yield to_yield, False


def _ListPackagesAndFiles(path):
  """List packages or modules which can be imported at given path."""
  importables = []
  for filename in os.listdir(path):
    if os.path.isfile(os.path.join(path, filename)):
      importables.append(filename)
    else:
      pkg_init_filepath = os.path.join(path, filename, '__init__.py')
      if os.path.isfile(pkg_init_filepath):
        importables.append(os.path.join(filename, '__init__.py'))
  return importables


def ListPackage(path, extra_extensions=None):
  """Returns list of packages and modules in given path.

  Args:
    path: str, filesystem path
    extra_extensions: [str], The list of file extra extensions that should be
      considered modules for the purposes of listing (in addition to .py).

  Returns:
    tuple([packages], [modules])
  """
  iter_modules = []
  if os.path.isdir(path):
    iter_modules = _IterModules(_ListPackagesAndFiles(path), extra_extensions)
  else:
    importer = pkgutil.get_importer(path)
    if hasattr(importer, '_files'):
      # pylint:disable=protected-access
      iter_modules = _IterModules(
          importer._files, extra_extensions, importer.prefix)
    elif hasattr(importer, '_par'):
      # pylint:disable=protected-access
      prefix = os.path.join(*importer._prefix.split('.'))
      iter_modules = _IterModules(
          importer._par._filename_list, extra_extensions, prefix)
    elif hasattr(importer, 'ziparchive'):
      prefix = os.path.join(*importer.prefix.split('.'))
      # pylint:disable=protected-access
      iter_modules = _IterModules(
          importer.ziparchive._files, extra_extensions, prefix)
  packages, modules = [], []
  for name, ispkg in iter_modules:
    if ispkg:
      packages.append(name)
    else:
      modules.append(name)
  return sorted(packages), sorted(modules)


def _IterPrefixFiles(file_list, prefix=None, depth=0):
  """Returns list of files located at specified prefix dir.

  Args:
    file_list: list(str), filepaths, usually absolute.
    prefix: str, filepath prefix, usually proper path itself. Used to filter
        out files in files_list.
    depth: int, relative to prefix, of whether to returns files in
        subdirectories. Depth of 0 would return files in prefix directory.

  Yields:
    file paths, relative to prefix at given depth or less.
  """
  if prefix is None:
    prefix = ''
  for file_path in file_list:
    if not file_path.startswith(prefix):
      continue

    rel_file_path = file_path[len(prefix):]

    sep_count = depth
    if rel_file_path.endswith(os.sep):
      sep_count += 1
    if rel_file_path.count(os.sep) > sep_count:
      continue
    yield rel_file_path


def ListPackageResources(path):
  """Returns list of resources at given path.

  Similar to pkg_resources.resource_listdir.

  Args:
    path: filesystem like path to a directory/package.

  Returns:
    list of files/resources at specified path.
  """
  if os.path.isdir(path):
    return [f + os.sep if os.path.isdir(os.path.join(path, f)) else f
            for f in os.listdir(path)]

  importer = pkgutil.get_importer(path)
  if hasattr(importer, '_files'):
    # pylint:disable=protected-access
    return _IterPrefixFiles(importer._files, importer.prefix, 0)

  if hasattr(importer, '_par'):
    # pylint:disable=protected-access
    prefix = os.path.join(*importer._prefix.split('.'))
    return _IterPrefixFiles(importer._par._filename_list, prefix, 0)

  return []
