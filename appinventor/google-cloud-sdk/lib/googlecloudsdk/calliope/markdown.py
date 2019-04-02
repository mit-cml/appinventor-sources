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

"""The Calliope command help document markdown generator."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import abc
import io
import re
import textwrap

from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import usage_text
from googlecloudsdk.core.console import console_io
import six


_SPLIT = 78  # Split lines longer than this.
_SECTION_INDENT = 8  # Section or list within section indent.
_FIRST_INDENT = 2  # First line indent.
_SUBSEQUENT_INDENT = 6  # Subsequent line indent.
_HANGING_OFFSET = 2  # Used to create hanging indentation using markdown.


def _GetIndexFromCapsule(capsule):
  """Returns a help doc index line for a capsule line.

  The capsule line is a formal imperative sentence, preceded by optional
  (RELEASE-TRACK) or [TAG] tags, optionally with markdown attributes. The index
  line has no tags, is not capitalized and has no period, period.

  Args:
    capsule: The capsule line to convert to an index line.

  Returns:
    The help doc index line for a capsule line.
  """
  # Strip leading tags: <markdown>(TAG)<markdown> or <markdown>[TAG]<markdown>.
  capsule = re.sub(r'(\*?[[(][A-Z]+[])]\*? +)*', '', capsule)
  # Lower case first word if not an abbreviation.
  match = re.match(r'([A-Z])([^A-Z].*)', capsule)
  if match:
    capsule = match.group(1).lower() + match.group(2)
  # Strip trailing period.
  return capsule.rstrip('.')


def GetFlagHeading(category):
  """Returns the flag section heading name for a flag category.

  Args:
    category: The flags category name.

  Returns:
    The flag section heading name for a flag category.
  """
  return category if 'FLAGS' in category else category + ' FLAGS'


class ExampleCommandLineSplitter(object):
  """Example command line splitter.

  Attributes:
    max_index: int, The max index to check in line.
    quote_char: str, The current quote char for quotes split across lines.
    quote_index: int, The index of quote_char in line or 0 if in previous line.
  """

  def __init__(self):
    self._max_index = _SPLIT - _SECTION_INDENT - _FIRST_INDENT
    self._quote_char = None
    self._quote_index = 0

  def _SplitInTwo(self, line):
    """Splits line into before and after, len(before) < self._max_index.

    Args:
      line: str, The line to split.

    Returns:
      (before, after)
        The line split into two parts. <before> is a list of strings that forms
        the first line of the split and <after> is a string containing the
        remainder of the line to split. The display width of <before> is
        < self._max_index. <before> contains the separator chars, including a
        newline.
    """
    punct_index = 0
    quoted_space_index = 0
    quoted_space_quote = None
    space_index = 0
    space_flag = False
    i = 0
    while i < self._max_index:
      c = line[i]
      i += 1
      if c == self._quote_char:
        self._quote_char = None
      elif self._quote_char:
        if c == ' ':
          quoted_space_index = i - 1
          quoted_space_quote = self._quote_char
      elif c in ('"', "'"):
        self._quote_char = c
        self._quote_index = i
        quoted_space_index = 0
      elif c == '\\':
        i += 1
      elif i < self._max_index:
        if c == ' ':
          # Split before a flag instead of the next arg; it could be the flag
          # value.
          if line[i] == '-':
            space_flag = True
            space_index = i
          elif space_flag:
            space_flag = False
          else:
            space_index = i
        elif c in (',', ';', '/', '|'):
          punct_index = i
        elif c == '=':
          space_flag = False
    separator = '\\\n'
    indent = _FIRST_INDENT
    if space_index:
      split_index = space_index
      indent = _SUBSEQUENT_INDENT
    elif quoted_space_index:
      split_index = quoted_space_index
      if quoted_space_quote == "'":
        separator = '\n'
    elif punct_index:
      split_index = punct_index
    else:
      split_index = self._max_index
    if split_index <= self._quote_index:
      self._quote_char = None
    else:
      self._quote_index = 0
    self._max_index = _SPLIT - _SECTION_INDENT - indent
    return [line[:split_index], separator, ' ' * indent], line[split_index:]

  def Split(self, line):
    """Splits a long example command line by inserting newlines.

    Args:
      line: str, The command line to split.

    Returns:
      str, The command line with newlines inserted.
    """
    lines = []
    while len(line) > self._max_index:
      before, line = self._SplitInTwo(line)
      lines.extend(before)
    lines.append(line)
    return ''.join(lines)


class MarkdownGenerator(six.with_metaclass(abc.ABCMeta, object)):
  """Command help markdown document generator base class.

  Attributes:
    _buf: Output document stream.
    _capsule: The one line description string.
    _command_name: The dotted command name.
    _command_path: The command path list.
    _doc: The output markdown document string.
    _docstring: The command docstring.
    _file_name: The command path name (used to name documents).
    _final_sections: The list of PrintFinalSections section names.
    _is_hidden: The command is hidden.
    _is_topic: True if the command is a help topic.
    _out: Output writer.
    _printed_sections: The set of already printed sections.
    _release_track: The calliope.base.ReleaseTrack.
  """

  def __init__(self, command_path, release_track, is_hidden):
    """Constructor.

    Args:
      command_path: The command path list.
      release_track: The base.ReleaseTrack of the command.
      is_hidden: The command is hidden if True.
    """
    self._command_path = command_path
    self._command_name = ' '.join(self._command_path)
    self._subcommands = None  # type: dict
    self._subgroups = None  # type: dict
    self._top = self._command_path[0] if self._command_path else ''
    self._buf = io.StringIO()
    self._out = self._buf.write
    self._capsule = ''
    self._docstring = ''
    self._final_sections = ['EXAMPLES', 'SEE ALSO']
    self._arg_sections = None  # type: list
    self._sections = {}
    self._file_name = '_'.join(self._command_path)
    self._global_flags = set()
    self._is_hidden = is_hidden
    self._is_root = self._IsRoot()
    self._release_track = release_track
    if (len(self._command_path) >= 3 and
        self._command_path[1] == release_track.prefix):
      command_index = 2
    else:
      command_index = 1
    self._is_topic = (len(self._command_path) >= (command_index + 1) and
                      self._command_path[command_index] == 'topic')
    self._printed_sections = set()

  @abc.abstractmethod
  def IsValidSubPath(self, sub_command_path):
    """Determines if the given sub command path is valid from this node.

    Args:
      sub_command_path: [str], The pieces of the command path.

    Returns:
      True, if the given path parts exist under this command or group node.
      False, if the sub path does not lead to a valid command or group.
    """
    pass

  @abc.abstractmethod
  def GetArguments(self):
    """Returns the command arguments."""
    pass

  def FormatExample(self, cmd, args, with_args):
    """Creates a link to the command reference from a command example.

    If with_args is False and the provided command includes args,
    returns None.

    Args:
      cmd: [str], a command.
      args: [str], args with the command.
      with_args: bool, whether the example is valid if it has args.

    Returns:
      (str) a representation of the command with a link to the reference, plus
      any args. | None, if the command isn't valid.
    """
    if args and not with_args:
      return None
    ref = '/'.join(cmd)
    command_link = 'link:' + ref + '[' + ' '.join(cmd) + ']'
    if args:
      command_link += ' ' + ' '.join(args)
    return command_link

  def _IsRoot(self):
    """Determine if this node should be treated as a "root" of the CLI tree.

    The top element is the root, but we also treat any additional release tracks
    as a root so that global flags are shown there as well.

    Returns:
      True if this node should be treated as a root, False otherwise.
    """
    if len(self._command_path) == 1:
      return True
    elif len(self._command_path) == 2:
      tracks = [t.prefix for t in base.ReleaseTrack.AllValues()]
      if self._command_path[-1] in tracks:
        return True
    return False

  def _ExpandHelpText(self, text):
    """Expand command {...} references in text.

    Args:
      text: The text chunk to expand.

    Returns:
      The expanded help text.
    """
    return console_io.LazyFormat(
        text or '',
        command=self._command_name,
        man_name=self._file_name,
        top_command=self._top,
        parent_command=' '.join(self._command_path[:-1]),
        grandparent_command=' '.join(self._command_path[:-2]),
        index=self._capsule,
        **self._sections
    )

  def _SetArgSections(self):
    """Sets self._arg_sections in document order."""
    if self._arg_sections is None:
      self._arg_sections, self._global_flags = usage_text.GetArgSections(
          self.GetArguments(), self._is_root)

  def _SplitCommandFromArgs(self, cmd):
    """Splits cmd into command and args lists.

    The command list part is a valid command and the args list part is the
    trailing args.

    Args:
      cmd: [str], A command + args list.

    Returns:
      (command, args): The command and args lists.
    """
    # The bare top level command always works.
    if len(cmd) <= 1:
      return cmd, []
    # Skip the top level command name.
    skip = 1
    i = skip
    while i <= len(cmd):
      i += 1
      if not self.IsValidSubPath(cmd[skip:i]):
        i -= 1
        break
    return cmd[:i], cmd[i:]

  def _UserInput(self, msg):
    """Returns msg with user input markdown.

    Args:
      msg: str, The user input string.

    Returns:
      The msg string with embedded user input markdown.
    """
    return (base.MARKDOWN_CODE + base.MARKDOWN_ITALIC +
            msg +
            base.MARKDOWN_ITALIC + base.MARKDOWN_CODE)

  def _ArgTypeName(self, arg):
    """Returns the argument type name for arg."""
    return 'positional' if arg.is_positional else 'flag'

  def PrintSectionHeader(self, name, sep=True):
    """Prints the section header markdown for name.

    Args:
      name: str, The manpage section name.
      sep: boolean, Add trailing newline.
    """
    self._printed_sections.add(name)
    self._out('\n\n## {name}\n'.format(name=name))
    if sep:
      self._out('\n')

  def PrintNameSection(self, disable_header=False):
    """Prints the command line name section.

    Args:
      disable_header: Disable printing the section header if True.
    """
    if not disable_header:
      self.PrintSectionHeader('NAME')
    self._out('{command} - {index}\n'.format(
        command=self._command_name,
        index=_GetIndexFromCapsule(self._capsule)))

  def PrintSynopsisSection(self, disable_header=False):
    """Prints the command line synopsis section.

    Args:
      disable_header: Disable printing the section header if True.
    """
    if self._is_topic:
      return
    self._SetArgSections()
    # MARKDOWN_CODE is the default SYNOPSIS font style.
    code = base.MARKDOWN_CODE
    em = base.MARKDOWN_ITALIC
    if not disable_header:
      self.PrintSectionHeader('SYNOPSIS')
    self._out('{code}{command}{code}'.format(code=code,
                                             command=self._command_name))

    if self._subcommands and self._subgroups:
      self._out(' ' + em + 'GROUP' + em + ' | ' + em + 'COMMAND' + em)
    elif self._subcommands:
      self._out(' ' + em + 'COMMAND' + em)
    elif self._subgroups:
      self._out(' ' + em + 'GROUP' + em)

    # Generate the arg usage string with flags in section order.
    remainder_usage = []
    for section in self._arg_sections:
      self._out(' ')
      self._out(usage_text.GetArgUsage(section.args, markdown=True, top=True,
                                       remainder_usage=remainder_usage))
    if self._global_flags:
      self._out(' [' + em + self._top.upper() + '_WIDE_FLAG ...' + em + ']')
    if remainder_usage:
      self._out(' ')
      self._out(' '.join(remainder_usage))

    self._out('\n')

  def _PrintArgDefinition(self, arg, depth=0, single=False):
    """Prints a positional or flag arg definition list item at depth."""
    usage = usage_text.GetArgUsage(arg, definition=True, markdown=True)
    if not usage:
      return
    self._out('\n{usage}{depth}\n'.format(
        usage=usage, depth=':' * (depth + _HANGING_OFFSET)))
    if arg.is_required and depth and not single:
      modal = ('\nThis {arg_type} must be specified if any of the other '
               'arguments in this group are specified.').format(
                   arg_type=self._ArgTypeName(arg))
    else:
      modal = ''
    self._out('\n{details}{modal}\n'.format(
        details=self.GetArgDetails(arg, depth=depth), modal=modal))

  def _PrintArgGroup(self, arg, depth=0, single=False):
    """Prints an arg group definition list at depth."""

    args = sorted(arg.arguments, key=usage_text.GetArgSortKey)
    heading = []
    if arg.help or arg.is_mutex or arg.is_required:
      if arg.help:
        heading.append(arg.help)
      if len(args) == 1 or args[0].is_required:
        if arg.is_required:
          # TODO (b/77314072): Put this before (NOTE) section in resource args.
          heading.append('This must be specified.')
      elif arg.is_mutex:
        if arg.is_required:
          heading.append('Exactly one of these must be specified:')
        else:
          heading.append('At most one of these may be specified:')
      elif arg.is_required:
        heading.append('At least one of these must be specified:')
    for a in args:
      if a.is_hidden:
        continue
      if heading:
        self._out('\n{0} {1}\n\n'.format(':' * (depth + _HANGING_OFFSET),
                                         ' '.join(heading)))
        heading = None
        depth += 1
      if a.is_group:
        single = False
        singleton = usage_text.GetSingleton(a)
        if singleton:
          if not a.help:
            a = singleton
          else:
            single = True
      if a.is_group:
        self._PrintArgGroup(a, depth=depth, single=single)
      else:
        self._PrintArgDefinition(a, depth=depth, single=single)

  def PrintPositionalDefinition(self, arg, depth=0):
    self._out('\n{usage}{depth}\n'.format(
        usage=usage_text.GetPositionalUsage(arg, markdown=True),
        depth=':' * (depth + _HANGING_OFFSET)))
    self._out('\n{arghelp}\n'.format(arghelp=self.GetArgDetails(arg)))

  def PrintFlagDefinition(self, flag, disable_header=False, depth=0):
    """Prints a flags definition list item.

    Args:
      flag: The flag object to display.
      disable_header: Disable printing the section header if True.
      depth: The indentation depth at which to print arg help text.
    """
    if not disable_header:
      self._out('\n')
    self._out('{usage}{depth}\n'.format(
        usage=usage_text.GetFlagUsage(flag, markdown=True),
        depth=':' * (depth + _HANGING_OFFSET)))
    self._out('\n{arghelp}\n'.format(arghelp=self.GetArgDetails(flag)))

  def PrintFlagSection(self, heading, arg, disable_header=False):
    """Prints a flag section.

    Args:
      heading: The flag section heading name.
      arg: The flag args / group.
      disable_header: Disable printing the section header if True.
    """
    if not disable_header:
      self.PrintSectionHeader(heading, sep=False)
    self._PrintArgGroup(arg)

  def PrintPositionalsAndFlagsSections(self, disable_header=False):
    """Prints the positionals and flags sections.

    Args:
      disable_header: Disable printing the section header if True.
    """
    if self._is_topic:
      return
    self._SetArgSections()

    # List the sections in order.
    for section in self._arg_sections:
      self.PrintFlagSection(
          section.heading, section.args, disable_header=disable_header)

    if self._global_flags:
      if not disable_header:
        self.PrintSectionHeader(
            '{} WIDE FLAGS'.format(self._top.upper()), sep=False)
      self._out('\nThese flags are available to all commands: {}.'
                '\nRun *$ {} help* for details.\n'
                .format(', '.join(sorted(self._global_flags)),
                        self._top))

  def PrintSubGroups(self, disable_header=False):
    """Prints the subgroup section if there are subgroups.

    Args:
      disable_header: Disable printing the section header if True.
    """
    if self._subgroups:
      self.PrintCommandSection('GROUP', self._subgroups,
                               disable_header=disable_header)

  def PrintSubCommands(self, disable_header=False):
    """Prints the subcommand section if there are subcommands.

    Args:
      disable_header: Disable printing the section header if True.
    """
    if self._subcommands:
      if self._is_topic:
        self.PrintCommandSection('TOPIC', self._subcommands, is_topic=True,
                                 disable_header=disable_header)
      else:
        self.PrintCommandSection('COMMAND', self._subcommands,
                                 disable_header=disable_header)

  def PrintSectionIfExists(self, name, default=None, disable_header=False):
    """Print a section name if it exists.

    Args:
      name: str, The manpage section name.
      default: str, Default help_stuff if section name is not defined.
      disable_header: Disable printing the section header if True.
    """
    if name in self._printed_sections:
      return
    help_stuff = self._sections.get(name, default)
    if not help_stuff:
      return
    if callable(help_stuff):
      help_message = help_stuff()
    else:
      help_message = help_stuff
    if not disable_header:
      self.PrintSectionHeader(name)
    self._out('{message}\n'.format(
        message=textwrap.dedent(help_message).strip()))

  def PrintExtraSections(self, disable_header=False):
    """Print extra sections not in excluded_sections.

    Extra sections are sections that have not been printed yet.
    PrintSectionIfExists() skips sections that have already been printed.

    Args:
      disable_header: Disable printing the section header if True.
    """
    excluded_sections = set(self._final_sections + ['NOTES'])
    for section in sorted(self._sections):
      if section.isupper() and section not in excluded_sections:
        self.PrintSectionIfExists(section, disable_header=disable_header)

  def PrintFinalSections(self, disable_header=False):
    """Print the final sections in order.

    Args:
      disable_header: Disable printing the section header if True.
    """
    for section in self._final_sections:
      self.PrintSectionIfExists(section, disable_header=disable_header)
    self.PrintNotesSection(disable_header=disable_header)

  def PrintCommandSection(self, name, subcommands, is_topic=False,
                          disable_header=False):
    """Prints a group or command section.

    Args:
      name: str, The section name singular form.
      subcommands: dict, The subcommand dict.
      is_topic: bool, True if this is a TOPIC subsection.
      disable_header: Disable printing the section header if True.
    """
    # Determine if the section has any content.
    content = ''
    for subcommand, help_info in sorted(six.iteritems(subcommands)):
      if self._is_hidden or not help_info.is_hidden:
        # If this group is already hidden, we can safely include hidden
        # sub-items.  Else, only include them if they are not hidden.
        content += '\n*link:{ref}[{cmd}]*::\n\n{txt}\n'.format(
            ref='/'.join(self._command_path + [subcommand]),
            cmd=subcommand,
            txt=help_info.help_text)
    if content:
      if not disable_header:
        self.PrintSectionHeader(name + 'S')
      if is_topic:
        self._out('The supplementary help topics are:\n')
      else:
        self._out('{cmd} is one of the following:\n'.format(
            cmd=self._UserInput(name)))
      self._out(content)

  def GetNotes(self):
    """Returns the explicit NOTES section contents."""
    return self._sections.get('NOTES')

  def PrintNotesSection(self, disable_header=False):
    """Prints the NOTES section if needed.

    Args:
      disable_header: Disable printing the section header if True.
    """
    notes = self.GetNotes()
    if notes:
      if not disable_header:
        self.PrintSectionHeader('NOTES')
      if notes:
        self._out(notes + '\n\n')

  def GetArgDetails(self, arg, depth=0):
    """Returns the detailed help message for the given arg."""
    if getattr(arg, 'detailed_help', None):
      raise ValueError(
          '{}: Use add_argument(help=...) instead of detailed_help="""{}""".'
          .format(self._command_name, getattr(arg, 'detailed_help')))
    return usage_text.GetArgDetails(arg, depth=depth)

  def _ExpandFormatReferences(self, doc):
    """Expand {...} references in doc."""
    doc = self._ExpandHelpText(doc)

    # Split long $ ... example lines.
    pat = re.compile(r'^ *(\$ .{%d,})$' % (
        _SPLIT - _FIRST_INDENT - _SECTION_INDENT), re.M)
    pos = 0
    rep = ''
    while True:
      match = pat.search(doc, pos)
      if not match:
        break
      rep += (doc[pos:match.start(1)] + ExampleCommandLineSplitter().Split(
          doc[match.start(1):match.end(1)]))
      pos = match.end(1)
    if rep:
      doc = rep + doc[pos:]
    return doc

  def _LinkMarkdown(self, doc, pat, with_args=True):
    """Build a representation of a doc, finding all command examples.

    Finds examples of both inline commands and commands on their own line.

    Args:
      doc: str, the doc to find examples in.
      pat: the compiled regexp pattern to match against (the "command" match
          group).
      with_args: bool, whether the examples are valid if they also have
          args.

    Returns:
      (str) The final representation of the doc.
    """
    pos = 0
    rep = ''
    while True:
      match = pat.search(doc, pos)
      if not match:
        break
      cmd, args = self._SplitCommandFromArgs(match.group('command').split(' '))
      lnk = self.FormatExample(cmd, args, with_args=with_args)
      if lnk:
        rep += doc[pos:match.start('command')] + lnk
      else:
        # Skip invalid commands.
        rep += doc[pos:match.end('command')]
      rep += doc[match.end('command'):match.end('end')]
      pos = match.end('end')
    if rep:
      doc = rep + doc[pos:]
    return doc

  def InlineCommandExamplePattern(self):
    """Regex to search for inline command examples enclosed in ` or *.

    Contains a 'command' group and an 'end' group which will be used
    by the regexp search later.

    Returns:
      (str) the regex pattern, including a format string for the 'top'
      command.
    """
    # This pattern matches "([`*]){top} {arg}*\1" where {top}...{arg} is a
    # known command. The negative lookbehind prefix prevents hyperlinks in
    # SYNOPSIS sections and as the first line in a paragraph.
    return (
        r'(?<!\n\n)(?<!\*\(ALPHA\)\* )(?<!\*\(BETA\)\* )'
        r'([`*])(?P<command>{top}( [a-z][-a-z0-9]*)*)(?P<end>\1)'
        .format(top=re.escape(self._top)))

  def _AddCommandLinkMarkdown(self, doc):
    r"""Add ([`*])command ...\1 link markdown to doc."""
    if not self._command_path:
      return doc
    pat = re.compile(self.InlineCommandExamplePattern())
    doc = self._LinkMarkdown(doc, pat, with_args=False)
    return doc

  def CommandLineExamplePattern(self):
    """Regex to search for command examples starting with '$ '.

    Contains a 'command' group and an 'end' group which will be used
    by the regexp search later.

    Returns:
      (str) the regex pattern, including a format string for the 'top'
      command.
    """
    # This pattern matches "$ {top} {arg}*" where each arg is lower case and
    # does not start with example-, my-, or sample-. This follows the style
    # guide rule that user-supplied args to example commands contain upper case
    # chars or start with example-, my-, or sample-. The trailing .? allows for
    # an optional punctuation character before end of line. This handles cases
    # like ``... run $ <top> foo bar.'' at the end of a sentence.
    # The <end> group ends at the same place as the command group, without
    # the punctuation or newlines.
    return (r'\$ (?P<end>(?P<command>{top}((?: (?!(example|my|sample)-)'
            r'[a-z][-a-z0-9]*)*))).?[ `\n]'.format(top=re.escape(self._top)))

  def _AddCommandLineLinkMarkdown(self, doc):
    """Add $ command ... link markdown to doc."""
    if not self._command_path:
      return doc
    pat = re.compile(self.CommandLineExamplePattern())
    doc = self._LinkMarkdown(doc, pat, with_args=True)
    return doc

  def _AddManPageLinkMarkdown(self, doc):
    """Add <top> ...(1) man page link markdown to doc."""
    if not self._command_path:
      return doc
    pat = re.compile(r'(\*?(' + self._top + r'(?:[-_ a-z])*)\*?)\(1\)')
    pos = 0
    rep = ''
    while True:
      match = pat.search(doc, pos)
      if not match:
        break
      cmd = match.group(2).replace('_', ' ')
      ref = cmd.replace(' ', '/')
      lnk = '*link:' + ref + '[' + cmd + ']*'
      rep += doc[pos:match.start(2)] + lnk
      pos = match.end(1)
    if rep:
      doc = rep + doc[pos:]
    return doc

  def _FixAirQuotesMarkdown(self, doc):
    """Change ``.*[[:alnum:]]{2,}.*'' quotes => _UserInput(*) in doc."""

    # Double ``air quotes'' on strings with no identifier chars or groups of
    # singleton identifier chars are literal. All other double air quote forms
    # are converted to unquoted strings with the _UserInput() font
    # embellishment. This is a subjective choice for aesthetically pleasing
    # renderings.
    pat = re.compile(r"[^`](``([^`']*)'')")
    pos = 0
    rep = ''
    for match in pat.finditer(doc):
      if re.search(r'\w\w', match.group(2)):
        quoted_string = self._UserInput(match.group(2))
      else:
        quoted_string = match.group(1)
      rep += doc[pos:match.start(1)] + quoted_string
      pos = match.end(1)
    if rep:
      doc = rep + doc[pos:]
    return doc

  def Edit(self, doc=None):
    """Applies edits to a copy of the generated markdown in doc.

    The sub-edit method call order might be significant. This method allows
    the combined edits to be tested without relying on the order.

    Args:
      doc: The markdown document string to edit, None for the output buffer.

    Returns:
      An edited copy of the generated markdown.
    """
    if doc is None:
      doc = self._buf.getvalue()
    doc = self._ExpandFormatReferences(doc)
    doc = self._AddCommandLineLinkMarkdown(doc)
    doc = self._AddCommandLinkMarkdown(doc)
    doc = self._AddManPageLinkMarkdown(doc)
    doc = self._FixAirQuotesMarkdown(doc)
    return doc

  def Generate(self):
    """Generates markdown for the command, group or topic, into a string."""
    self._out('# {0}(1)\n'.format(self._file_name.upper()))
    self.PrintNameSection()
    self.PrintSynopsisSection()
    self.PrintSectionIfExists('DESCRIPTION')
    self.PrintPositionalsAndFlagsSections()
    self.PrintSubGroups()
    self.PrintSubCommands()
    self.PrintExtraSections()
    self.PrintFinalSections()
    return self.Edit()


class CommandMarkdownGenerator(MarkdownGenerator):
  """Command help markdown document generator.

  Attributes:
    _command: The CommandCommon instance for command.
    _root_command: The root CLI command instance.
    _subcommands: The dict of subcommand help indexed by subcommand name.
    _subgroups: The dict of subgroup help indexed by subcommand name.
  """

  def __init__(self, command):
    """Constructor.

    Args:
      command: A calliope._CommandCommon instance. Help is extracted from this
        calliope command, group or topic.
    """
    self._command = command
    command.LoadAllSubElements()
    # pylint: disable=protected-access
    self._root_command = command._TopCLIElement()
    super(CommandMarkdownGenerator, self).__init__(
        command.GetPath(),
        command.ReleaseTrack(),
        command.IsHidden())
    self._capsule = self._command.short_help
    self._docstring = self._command.long_help
    self._ExtractSectionsFromDocstring(self._docstring)
    self._sections['description'] = self._sections.get('DESCRIPTION', '')
    self._sections.update(getattr(self._command, 'detailed_help', {}))
    self._subcommands = command.GetSubCommandHelps()
    self._subgroups = command.GetSubGroupHelps()

  def _SetSectionHelp(self, name, lines):
    """Sets section name help composed of lines.

    Args:
      name: The section name.
      lines: The list of lines in the section.
    """
    # Strip leading empty lines.
    while lines and not lines[0]:
      lines = lines[1:]
    # Strip trailing empty lines.
    while lines and not lines[-1]:
      lines = lines[:-1]
    if lines:
      self._sections[name] = '\n'.join(lines)

  def _ExtractSectionsFromDocstring(self, docstring):
    """Extracts section help from the command docstring."""
    name = 'DESCRIPTION'
    lines = []
    for line in textwrap.dedent(docstring).strip().splitlines():
      # '## \n' is not section markdown.
      if len(line) >= 4 and line.startswith('## '):
        self._SetSectionHelp(name, lines)
        name = line[3:]
        lines = []
      else:
        lines.append(line)
    self._SetSectionHelp(name, lines)

  def IsValidSubPath(self, sub_command_path):
    """Returns True if the given sub command path is valid from this node."""
    return self._root_command.IsValidSubPath(sub_command_path)

  def GetArguments(self):
    """Returns the command arguments."""
    return self._command.ai.arguments

  def GetNotes(self):
    """Returns the explicit and auto-generated NOTES section contents."""
    return self._command.GetNotesHelpSection(self._sections.get('NOTES'))


def Markdown(command):
  """Generates and returns the help markdown document for command.

  Args:
    command: The CommandCommon command instance.

  Returns:
    The markdown document string.
  """
  return CommandMarkdownGenerator(command).Generate()
