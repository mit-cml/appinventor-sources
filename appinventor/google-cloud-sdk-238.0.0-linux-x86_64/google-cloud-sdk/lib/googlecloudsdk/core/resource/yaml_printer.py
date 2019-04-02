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

"""YAML format printer."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import collections

from googlecloudsdk.core.resource import resource_printer_base
from googlecloudsdk.core.resource import resource_transform
from googlecloudsdk.core.yaml import dict_like
from googlecloudsdk.core.yaml import list_like

import six
from six.moves import range  # pylint: disable=redefined-builtin


class YamlPrinter(resource_printer_base.ResourcePrinter):
  """Prints the YAML representations of JSON-serializable objects.

  [YAML](http://www.yaml.org), YAML ain't markup language.

  Printer attributes:
    null=string: Display string instead of `null` for null/None values.
    no-undefined: Does not display resource data items with null values.

  For example:

    printer = YamlPrinter(log.out)
    printer.AddRecord({'a': ['hello', 'world'], 'b': {'x': 'bye'}})

  produces:

    ---
    a:
      - hello
      - world
    b:
      - x: bye

  Attributes:
    _yaml: Reference to the `yaml` module. Imported locally to improve startup
        performance.
  """

  def __init__(self, *args, **kwargs):
    super(YamlPrinter, self).__init__(*args, retain_none_values=True, **kwargs)
    # pylint:disable=g-import-not-at-top, Delay import for performance.
    from ruamel import yaml
    self._yaml = yaml
    null = self.attributes.get('null')

    def _FloatPresenter(unused_dumper, data):
      return yaml.nodes.ScalarNode(
          'tag:yaml.org,2002:float', resource_transform.TransformFloat(data))

    def _LiteralLinesPresenter(dumper, data):
      return dumper.represent_scalar('tag:yaml.org,2002:str', data, style='|')

    def _NullPresenter(dumper, unused_data):
      if null in ('null', None):
        return dumper.represent_scalar('tag:yaml.org,2002:null', 'null')
      return dumper.represent_scalar('tag:yaml.org,2002:str', null)

    def _OrderedDictPresenter(dumper, data):
      return dumper.represent_mapping('tag:yaml.org,2002:map', data.items())

    def _UndefinedPresenter(dumper, data):
      r = repr(data)
      if r == '[]':
        return dumper.represent_list([])
      if r == '{}':
        return dumper.represent_dict({})
      dumper.represent_undefined(data)

    self._yaml.add_representer(float,
                               _FloatPresenter,
                               Dumper=yaml.dumper.SafeDumper)
    self._yaml.add_representer(YamlPrinter._LiteralLines,
                               _LiteralLinesPresenter,
                               Dumper=yaml.dumper.SafeDumper)
    self._yaml.add_representer(None,
                               _UndefinedPresenter,
                               Dumper=yaml.dumper.SafeDumper)
    self._yaml.add_representer(type(None),
                               _NullPresenter,
                               Dumper=yaml.dumper.SafeDumper)
    self._yaml.add_representer(collections.OrderedDict,
                               _OrderedDictPresenter,
                               Dumper=yaml.dumper.SafeDumper)

  class _LiteralLines(six.text_type):
    """A yaml representer hook for literal strings containing newlines."""

  def _UpdateTypesForOutput(self, val):
    """Dig through a dict of list of primitives to help yaml output.

    Args:
      val: A dict, list, or primitive object.

    Returns:
      An updated version of val.
    """
    if isinstance(val, six.string_types) and '\n' in val:
      return YamlPrinter._LiteralLines(val)
    if list_like(val):
      for i in range(len(val)):
        val[i] = self._UpdateTypesForOutput(val[i])
      return val
    if dict_like(val):
      for key in val:
        val[key] = self._UpdateTypesForOutput(val[key])
      return val
    return val

  def _AddRecord(self, record, delimit=True):
    """Immediately prints the given record as YAML.

    Args:
      record: A YAML-serializable Python object.
      delimit: Prints resource delimiters if True.
    """
    record = self._UpdateTypesForOutput(record)
    self._yaml.safe_dump(
        record,
        stream=self._out,
        default_flow_style=False,
        indent=resource_printer_base.STRUCTURED_INDENTATION,
        explicit_start=delimit,
        # By default, the yaml module uses encoding=None on Py3 and
        # encoding=utf8 on Py2. This is probably so you can write it directly to
        # stdout and have it work, but since we put everything through the log
        # module that handles the encoding there, we want to maintain everything
        # as unicode strings here.
        encoding=None)
