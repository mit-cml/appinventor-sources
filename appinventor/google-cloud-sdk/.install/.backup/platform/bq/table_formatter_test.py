#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright 2012 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Tests for table_formatter.py."""

__author__ = 'craigcitro@google.com (Craig Citro)'

import StringIO

from google.apputils import googletest
import table_formatter


class TableFormatterTest(googletest.TestCase):

  def setUp(self):
    super(TableFormatterTest, self).setUp()
    if type(self) != TableFormatterTest:
      self.failUnless(hasattr(self, 'format_class'),
                      'Subclasses must provide self.format_class')
      self.formatter = self.format_class()
      self.formatter.AddColumns(('foo', 'longer header'),
                                kwdss=[{}, {'align': 'r'}])
      self.formatter.AddRow(['a', 3])
      self.formatter.AddRow(['abc', 123])

  def testStr(self):
    self.failIf(hasattr(self, 'format_class'),
                'Subclasses must override testStr')

  def testUnicodeRow(self):
    row = [11, 'chinese', u'你不能教老狗新把戏']
    if type(self) != TableFormatterTest:
      formatter = self.format_class()
      formatter.AddColumns(('count', 'language', 'message'))
      formatter.AddRow(row)
      # Note that we don't need any asserts here: the act of calling
      # Print will throw if unicode isn't being handled correctly.
      formatter.Print()

      formatter = self.format_class()
      formatter.AddColumns(('message',))
      formatter.AddRow(row[2:])
      formatter.Print()
      self.assertTrue(all(ord(c) <= 127 for c in str(formatter)))
      self.assertTrue(any(ord(c) > 127 for c in unicode(formatter)))

  def wrap_print(self, formatter):
    stringio = StringIO.StringIO()
    formatter.Print(stringio)
    printed = stringio.getvalue().rstrip('\n')
    stringio.close()
    return printed


class PrettyFormatterTest(TableFormatterTest):

  def setUp(self):
    # Static method names are too long without abbreviations.
    self.PF = table_formatter.PrettyFormatter  # pylint: disable=g-bad-name
    self.format_class = self.PF
    super(PrettyFormatterTest, self).setUp()

  def testStr(self):
    table_repr = '\n'.join((
        '+-----+---------------+',
        '| foo | longer header |',
        '+-----+---------------+',
        '| a   |             3 |',
        '| abc |           123 |',
        '+-----+---------------+'))
    self.assertEquals(table_repr, str(self.formatter))

  def testCenteredPadding(self):
    self.assertEquals((1, 1), self.PF.CenteredPadding(8, 6))
    self.assertEquals((2, 1), self.PF.CenteredPadding(8, 5, left_justify=False))
    self.assertEquals((1, 2), self.PF.CenteredPadding(8, 5))
    self.assertRaises(table_formatter.FormatterException,
                      self.PF.CenteredPadding, 1, 5)

  def testAbbreviate(self):
    self.assertEquals('', self.PF.Abbreviate('abc', 0))
    self.assertEquals('.', self.PF.Abbreviate('abc', 1))
    self.assertEquals('ab...', self.PF.Abbreviate('abcdef', 5))
    self.assertEquals('abcdef', self.PF.Abbreviate('abcdef', 6))
    self.assertEquals('abcdef', self.PF.Abbreviate('abcdef', 7))

  def testFormatCell(self):
    entry = 'abc'
    self.assertEquals(
        [' abc '], list(self.PF.FormatCell(entry, 3)))
    self.assertEquals(
        [' abc   '], list(self.PF.FormatCell(entry, 5, align='l')))
    self.assertEquals(
        ['  abc  '], list(self.PF.FormatCell(entry, 5)))
    self.assertEquals(
        ['   abc '], list(self.PF.FormatCell(entry, 5, align='r')))
    self.assertEquals(
        ['  abc   '], list(self.PF.FormatCell(entry, 6)))

    lines = [
        '  abc   ',
        '        ',
        '        ',
        ]
    self.assertEquals(lines, list(self.PF.FormatCell(entry, 6, cell_height=3)))
    lines.append(lines[-1])
    self.assertEquals(lines, list(self.PF.FormatCell(entry, 6, cell_height=4)))

    lines = [
        '        ',
        ' abc... ',
        ' ab     ',
        '        ',
        ]
    self.assertEquals(lines, list(self.PF.FormatCell(
        'abcdefghi\nab', 6, cell_height=4, align='l', valign='c')))

    lines = [
        ' abc... ',
        ' ab     ',
        '        ',
        '        ',
        ]
    self.assertEquals(lines, list(self.PF.FormatCell(
        'abcdefghi\nab', 6, cell_height=4, align='l')))

    lines = [
        '        ',
        '        ',
        ' abc... ',
        ' ab     ',
        ]
    self.assertEquals(lines, list(self.PF.FormatCell(
        'abcdefghi\nab', 6, cell_height=4, align='l', valign='b')))

    self.assertRaises(table_formatter.FormatterException,
                      self.PF.FormatCell, 'ab\na', 5)

  def testFormatRow(self):
    formatter = table_formatter.PrettyFormatter()
    formatter.AddColumns(('one', 'two'))
    formatter.AddRow(['a', 'b'])
    self.assertEquals(
        ['| a   | b   |'],
        list(formatter.FormatRow(formatter.rows[0], 1)))
    formatter.AddRow(['a', 'b\nc'])
    self.assertEquals(
        ['| a   | b   |',
         '|     | c   |',
        ],
        list(formatter.FormatRow(formatter.rows[1], 2)))
    self.assertRaises(table_formatter.FormatterException,
                      formatter.FormatRow, formatter.rows[1], 1)
    formatter.AddRow(['a', '\nbbbbbb\nc'])
    self.assertEquals(
        ['| a   |        |',
         '|     | bbbbbb |',
         '|     | c      |',
        ],
        list(formatter.FormatRow(formatter.rows[2], 3)))
    self.assertEquals(
        ['| a   |      |',
         '|     | b... |',
         '|     | c    |',
        ],
        list(formatter.FormatRow(formatter.rows[2], 3, column_widths=[3, 4])))

  def testHeaderLines(self):
    formatter = table_formatter.PrettyFormatter()
    formatter.AddColumns(('a', 'b'))
    formatter.AddRow(['really long string', ''])
    self.assertEquals(
        ['|         a          | b |'],
        list(formatter.HeaderLines()))

  def testFormatHeader(self):
    formatter = table_formatter.PrettyFormatter()
    formatter.AddColumns(('a', 'bcd\nefgh'))
    formatter.AddRow(['really long string', ''])
    self.assertEquals(
        ['+--------------------+------+',
         '|         a          | bcd  |',
         '|                    | efgh |',
         '+--------------------+------+'],
        list(formatter.FormatHeader()))

  def testAddRow(self):
    formatter = table_formatter.PrettyFormatter()
    formatter.AddColumns(('a', 'b'))
    formatter.AddRow(['foo', 'x'])
    self.assertEquals(1, len(formatter))
    self.assertEquals([3, 1], formatter.column_widths)
    self.assertEquals([1], formatter.row_heights)
    formatter.AddRow(['foo\nbar', 'xxxxxxx'])
    self.assertEquals(2, len(formatter))
    self.assertEquals([3, 7], formatter.column_widths)
    self.assertEquals([1, 2], formatter.row_heights)
    # Check that we can add non-string entries.
    formatter.AddRow([3, {'a': 5}])

  def testAddColumn(self):
    formatter = table_formatter.PrettyFormatter()
    formatter.AddColumn('abc\ndef', align='r')
    self.assertEquals([3], formatter.column_widths)
    self.assertEquals(2, formatter.header_height)
    self.assertRaises(table_formatter.FormatterException,
                      formatter.AddColumn, 'bad', align='d')
    formatter.AddRow([3])
    self.assertRaises(table_formatter.FormatterException,
                      formatter.AddColumn, 'anything')

  def testPrintEmptyTable(self):
    formatter = table_formatter.PrettyFormatter(skip_header_when_empty=False)
    formatter.AddColumns(('a', 'b'))
    table_repr = '\n'.join((
        '+---+---+',
        '| a | b |',
        '+---+---+',
        '+---+---+'))

    self.assertEqual(table_repr, str(formatter))
    self.assertEqual('', self.wrap_print(formatter))

    formatter = table_formatter.PrettyFormatter()
    formatter.AddColumns(('a', 'b'))
    self.assertEqual(table_repr, str(formatter))
    self.assertEqual('', self.wrap_print(formatter))

    formatter = table_formatter.PrettyFormatter(skip_header_when_empty=True)
    formatter.AddColumns(('a', 'b'))
    self.assertEqual('', str(formatter))
    self.assertEqual('', self.wrap_print(formatter))


class SparsePrettyFormatterTest(TableFormatterTest):

  def setUp(self):
    self.format_class = table_formatter.SparsePrettyFormatter
    super(SparsePrettyFormatterTest, self).setUp()

  def testStr(self):
    table_repr = '\n'.join((
        '  foo   longer header  ',
        ' ----- --------------- ',
        '  a                 3  ',
        '  abc             123  '))
    self.assertEquals(table_repr, str(self.formatter))

  def testFormatHeader(self):
    formatter = table_formatter.SparsePrettyFormatter()
    formatter.AddColumns(('a', 'bcd\nefgh'))
    formatter.AddRow(['really long string', ''])
    self.assertEquals(
        ['          a            bcd   ',
         '                       efgh  ',
         ' -------------------- ------ '],
        list(formatter.FormatHeader()))

  def testPrintEmptyTable(self):
    formatter = table_formatter.SparsePrettyFormatter(
        skip_header_when_empty=False)
    formatter.AddColumns(('a', 'b'))
    table_repr = '\n'.join((
        '  a   b  ',
        ' --- --- '))
    self.assertEqual(table_repr, str(formatter))
    self.assertEqual('', self.wrap_print(formatter))

    formatter = table_formatter.SparsePrettyFormatter()
    formatter.AddColumns(('a', 'b'))
    self.assertEqual(table_repr, str(formatter))
    self.assertEqual('', self.wrap_print(formatter))

    formatter = table_formatter.SparsePrettyFormatter(
        skip_header_when_empty=True)
    formatter.AddColumns(('a', 'b'))
    self.assertEqual('', str(formatter))
    self.assertEqual('', self.wrap_print(formatter))


class PrettyJsonFormatterTest(TableFormatterTest):

  def setUp(self):
    self.format_class = table_formatter.PrettyJsonFormatter
    super(PrettyJsonFormatterTest, self).setUp()

  def testStr(self):
    table_repr = '\n'.join((
        '[',
        '  {',
        '    "foo": "a", ',
        '    "longer header": 3',
        '  }, ',
        '  {',
        '    "foo": "abc", ',
        '    "longer header": 123',
        '  }',
        ']'))
    self.assertEquals(table_repr, str(self.formatter))

  def testEmptyStr(self):
    formatter = self.format_class()
    formatter.AddColumns(('a', 'b'))

    self.assertEqual('[]', self.wrap_print(formatter))


class JsonFormatterTest(TableFormatterTest):

  def setUp(self):
    self.format_class = table_formatter.JsonFormatter
    super(JsonFormatterTest, self).setUp()

  def testStr(self):
    table_repr = ('[{"longer header":3,"foo":"a"},'
                  '{"longer header":123,"foo":"abc"}]')
    self.assertEquals(table_repr, str(self.formatter))

  def testEmptyStr(self):
    formatter = self.format_class()
    formatter.AddColumns(('a', 'b'))

    self.assertEqual('[]', self.wrap_print(formatter))


class CsvFormatterTest(TableFormatterTest):

  def setUp(self):
    self.format_class = table_formatter.CsvFormatter
    super(CsvFormatterTest, self).setUp()

  def testStr(self):
    table_repr = '\n'.join((
        'foo,longer header',
        'a,3',
        'abc,123'))
    self.assertEquals(table_repr, str(self.formatter))


class NullFormatterTest(TableFormatterTest):

  def setUp(self):
    self.format_class = table_formatter.NullFormatter
    super(NullFormatterTest, self).setUp()

  def testStr(self):
    self.assertEquals('', str(self.formatter))

  def testUnicodeRow(self):
    self.assertEquals('', unicode(self.formatter))


if __name__ == '__main__':
  googletest.main()
