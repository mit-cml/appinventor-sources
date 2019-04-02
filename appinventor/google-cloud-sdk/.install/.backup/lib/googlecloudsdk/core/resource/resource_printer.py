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

r"""Methods for formatting and printing Python objects.

Each printer has three main attributes, all accessible as strings in the
--format='NAME[ATTRIBUTES](PROJECTION)' option:

  NAME: str, The printer name.

  [ATTRIBUTES]: str, An optional [no-]name[=value] list of attributes. Unknown
    attributes are silently ignored. Attributes are added to a printer local
    dict indexed by name.

  (PROJECTION): str, List of resource names to be included in the output
    resource. Unknown names are silently ignored. Resource names are
    '.'-separated key identifiers with an implicit top level resource name.

Example:

  gcloud compute instances list \
      --format='table[box](name, networkInterfaces[0].networkIP)'
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.core import exceptions as core_exceptions
from googlecloudsdk.core.resource import config_printer
from googlecloudsdk.core.resource import csv_printer
from googlecloudsdk.core.resource import diff_printer
from googlecloudsdk.core.resource import flattened_printer
from googlecloudsdk.core.resource import json_printer
from googlecloudsdk.core.resource import list_printer
from googlecloudsdk.core.resource import object_printer
from googlecloudsdk.core.resource import resource_lex
from googlecloudsdk.core.resource import resource_printer_base
from googlecloudsdk.core.resource import resource_projection_spec
from googlecloudsdk.core.resource import resource_projector
from googlecloudsdk.core.resource import resource_property
from googlecloudsdk.core.resource import resource_transform
from googlecloudsdk.core.resource import table_printer
from googlecloudsdk.core.resource import yaml_printer
from googlecloudsdk.core.util import typing  # pylint: disable=unused-import


class Error(core_exceptions.Error):
  """Exceptions for this module."""


class UnknownFormatError(Error):
  """Unknown format name exception."""


class ProjectionFormatRequiredError(Error):
  """Projection key missing required format attribute."""


class DefaultPrinter(yaml_printer.YamlPrinter):
  """An alias for YamlPrinter.

  An alias for the *yaml* format.
  """


class DisablePrinter(resource_printer_base.ResourcePrinter):
  """Disables formatted output and does not consume the resources.

  Disables formatted output and does not consume the resources.
  """

  def __init__(self, *args, **kwargs):
    super(DisablePrinter, self).__init__(*args, **kwargs)
    self.attributes = {'disable': 1}


class NonePrinter(resource_printer_base.ResourcePrinter):
  """Disables formatted output and consumes the resources.

  Disables formatted output and consumes the resources.
  """


class TextPrinter(flattened_printer.FlattenedPrinter):
  """An alias for FlattenedPrinter.

  An alias for the *flattened* format.
  """


class MultiPrinter(resource_printer_base.ResourcePrinter):
  """A printer that prints different formats for each projection key.

  Each projection key must have a subformat defined by the
  :format=FORMAT-STRING attribute. For example,

    `--format="multi(data:format=json, info:format='table[box](a, b, c)')"`

  formats the *data* field as JSON and the *info* field as a boxed table.
  """

  def __init__(self, *args, **kwargs):
    super(MultiPrinter, self).__init__(*args, **kwargs)
    # pylint: disable=line-too-long
    self.columns = []  # type: typing.List[typing.Tuple[resource_projection_spec.ProjectionSpec._Column, resource_printer_base.ResourcePrinter]]
    # pylint: disable=line-too-long
    for col in self.column_attributes.Columns():
      if not col.attribute.subformat:
        raise ProjectionFormatRequiredError(
            '{key} requires format attribute.'.format(
                key=resource_lex.GetKeyName(col.key)))
      self.columns.append(
          (col, Printer(col.attribute.subformat, out=self._out)))

  def _AddRecord(self, record, delimit=True):
    for col, printer in self.columns:
      printer.Print(resource_property.Get(record, col.key))


class PrinterAttributes(resource_printer_base.ResourcePrinter):
  """Attributes for all printers. This docstring is used to generate topic docs.

  All formats have these attributes.

  Printer attributes:
    disable: Disables formatted output and does not consume the resources.
    json-decode: Decodes string values that are JSON compact encodings of list
      and dictionary objects. This may become the default.
    pager: If True, sends output to a pager.
    private: Disables log file output. Use this for sensitive resource data
      that should not be displayed in log files. Explicit command line IO
      redirection overrides this attribute.
    transforms: Apply projection transforms to the resource values. The default
      is format specific. Use *no-transforms* to disable.
  """


_FORMATTERS = {
    'config': config_printer.ConfigPrinter,
    'csv': csv_printer.CsvPrinter,
    'default': DefaultPrinter,
    'diff': diff_printer.DiffPrinter,
    'disable': DisablePrinter,
    'flattened': flattened_printer.FlattenedPrinter,
    'get': csv_printer.GetPrinter,
    'json': json_printer.JsonPrinter,
    'list': list_printer.ListPrinter,
    'multi': MultiPrinter,
    'none': NonePrinter,
    'object': object_printer.ObjectPrinter,
    'table': table_printer.TablePrinter,
    'text': TextPrinter,
    'value': csv_printer.ValuePrinter,
    'yaml': yaml_printer.YamlPrinter,
}


def GetFormatRegistry():
  """Returns the (format-name => Printer) format registry dictionary.

  Returns:
    The (format-name => Printer) format registry dictionary.
  """
  return _FORMATTERS


def SupportedFormats():
  """Returns a sorted list of supported format names."""
  return sorted(_FORMATTERS)


def Printer(print_format, out=None, defaults=None, console_attr=None):
  """Returns a resource printer given a format string.

  Args:
    print_format: The _FORMATTERS name with optional attributes and projection.
    out: Output stream, log.out if None.
    defaults: Optional resource_projection_spec.ProjectionSpec defaults.
    console_attr: The console attributes for the output stream. Ignored by some
      printers. If None then printers that require it will initialize it to
      match out.

  Raises:
    UnknownFormatError: The print_format is invalid.

  Returns:
    An initialized ResourcePrinter class or None if printing is disabled.
  """
  projector = resource_projector.Compile(
      expression=print_format, defaults=resource_projection_spec.ProjectionSpec(
          defaults=defaults, symbols=resource_transform.GetTransforms()))
  printer_name = projector.Projection().Name()
  if not printer_name:
    # Do not print, do not consume resources.
    return None
  try:
    printer_class = _FORMATTERS[printer_name]
  except KeyError:
    raise UnknownFormatError("""\
Format must be one of {0}; received [{1}].

For information on output formats:
  $ gcloud topic formats
""".format(', '.join(SupportedFormats()), printer_name))
  printer = printer_class(out=out,
                          name=printer_name,
                          printer=Printer,
                          projector=projector,
                          console_attr=console_attr)
  return printer


def Print(resources, print_format, out=None, defaults=None, single=False):
  """Prints the given resources.

  Args:
    resources: A singleton or list of JSON-serializable Python objects.
    print_format: The _FORMATTER name with optional projection expression.
    out: Output stream, log.out if None.
    defaults: Optional resource_projection_spec.ProjectionSpec defaults.
    single: If True then resources is a single item and not a list.
      For example, use this to print a single object as JSON.
  """
  printer = Printer(print_format, out=out, defaults=defaults)
  # None means the printer is disabled.
  if printer:
    printer.Print(resources, single)
