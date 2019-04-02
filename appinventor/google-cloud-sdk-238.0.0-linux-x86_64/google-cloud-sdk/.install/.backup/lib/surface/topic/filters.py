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

"""Resource filters supplementary help."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import textwrap

from googlecloudsdk.calliope import base
from googlecloudsdk.core.resource import resource_topics


class Filters(base.TopicCommand):
  """Resource filters supplementary help."""

  detailed_help = {
      'DESCRIPTION':
          textwrap.dedent("""\
          {description}
          +
          Note: Depending on the specific server API, filtering may be done
          entirely by the client, entirely by the server, or by a combination
          of both.

          ### Filter Expressions

          A filter expression is a Boolean function that selects the resources
          to print from a list of resources. Expressions are composed
          of terms connected by logic operators.

          *LogicOperator*::

          Expressions containing both *AND* and *OR* must be parenthesized to
          disambiguate precedence.

          *NOT* _term-1_:::

          True if _term-1_ is False, otherwise False.

          _term-1_ *AND* _term-2_:::

          True if both _term-1_ and _term-2_ are true.

          _term-1_ *OR* _term-2_:::

          True if at least one of _term-1_ or _term-2_ is true.

          _term-1_ _term-2_:::

          Term conjunction (implicit *AND*) is True if both _term-1_
          and _term-2_ are true.  Conjunction has lower precedence than *OR*.

          *Terms*::

          A term is a _key_ _operator_ _value_ tuple, where _key_ is a dotted
          name that evaluates to the value of a resource attribute, and _value_
          may be:

          *number*::: integer or floating point numeric constant

          *unquoted literal*::: character sequence terminated by space, ( or )

          *quoted literal*::: _"..."_ or _'...'_

          Most filter expressions need to be quoted in shell commands. If you
          use _'...'_ shell quotes then use _"..."_ filter string literal quotes
          and vice versa.

          *Operator Terms*::

          _key_ *:* _simple-pattern_:::

          *:* operator evaluation is changing for consistency across Google
          APIs.  The current default is deprecated and will be dropped shortly.
          A warning will be displayed when a --filter expression would return
          different matches using both the deprecated and new implementations.
          +
          The current deprecated default is True if _key_ contains
          _simple-pattern_.  The match is case insensitive.  It allows one
          ```*``` that matches any sequence of 0 or more characters.
          If ```*``` is specified then the match is anchored, meaning all
          characters from the beginning and end of the value must match.
          +
          The new implementation is True if _simple-pattern_ matches any
          _word_ in _key_.  Words are locale specific but typically consist of
          alpha-numeric characters.  Non-word characters that do not appear in
          _simple-pattern_ are ignored.  The matching is anchored and case
          insensitive.  An optional trailing ```*``` does a word prefix match.
          +
          Use _key_```:*``` to test if _key_ is defined and
          ```-```_key_```:*``` to test if _key_ is undefined.

          _key_ *:(* _simple-pattern_ ... *)*:::

          True if _key_ matches any _simple-pattern_ in the
          (space, tab, newline, comma) separated list.

          _key_ *=* _value_:::

          True if _key_ is equal to _value_.  Equivalent to *:*, with the
          exception that the trailing ```*``` prefix match is not supported.

          _key_ *=(* _value_ ... *)*:::

          True if _key_ is equal to any _value_ in the
          (space, tab, newline, *,*) separated list.

          _key_ *!=* _value_:::

          True if _key_ is not _value_. Equivalent to
          -_key_=_value_ and NOT _key_=_value_.

          _key_ *<* _value_:::

          True if _key_ is less than _value_. If both _key_ and
          _value_ are numeric then numeric comparison is used, otherwise
          lexicographic string comparison is used.

          _key_ *<=* _value_:::

          True if _key_ is less than or equal to _value_. If both
          _key_ and _value_ are numeric then numeric comparison is used,
          otherwise lexicographic string comparison is used.

          _key_ *>=* _value_:::

          True if _key_ is greater than or equal to _value_. If
          both _key_ and _value_ are numeric then numeric comparison is used,
          otherwise lexicographic string comparison is used.

          _key_ *>* _value_:::

          True if _key_ is greater than _value_. If both _key_ and
          _value_ are numeric then numeric comparison is used, otherwise
          lexicographic string comparison is used.

          _key_ *~* _value_:::

          True if _key_ matches the RE (regular expression) pattern _value_.

          _key_ *!*~ _value_:::

          True if _key_ does not match the RE (regular expression)
          pattern _value_.

          """)
          .format(description=resource_topics.ResourceDescription('filter')),
      'EXAMPLES':
          textwrap.dedent("""\
          List all Google Compute Engine instance resources:

            $ gcloud compute instances list

          List Compute Engine instance resources that have machineType
          *f1-micro*:

            $ gcloud compute instances list --filter="machineType:f1-micro"

          List Compute Engine instance resources with zone prefix *us* and not
          MachineType *f1-micro*:

            $ gcloud compute instances list --filter="zone ~ ^us AND -machineType:f1-micro"

          List Compute Engine instance resources with tag *my-tag*:

            $ gcloud compute instances list --filter="tags.items=my-tag"

          List Compute Engine instance resources with tag *my-tag* or
          *my-other-tag*:

            $ gcloud compute instances list --filter="tags.items=(my-tag,my-other-tag)"

          List Compute Engine instance resources with tag *my-tag* and
          *my-other-tag*:

            $ gcloud compute instances list --filter="tags.items=my-tag AND tags.items=my-other-tag"

          List Compute Engine instance resources which either have tag *my-tag*
          but not *my-other-tag* or have tag *alternative-tag*:

            $ gcloud compute instances list --filter="(tags.items=my-tag AND -tags.items=my-other-tag) OR tags.items=alternative-tag"

          List Compute Engine instance resources with label *my-label* with any
          value:

            $ gcloud compute instances list --filter="labels.my-label:*"

          List in JSON format those projects where the labels match specific
          values (e.g. label.env is 'test' and label.version is alpha):

            $ gcloud projects list --format="json" --filter="labels.env=test AND labels.version=alpha"

          List projects that were created on and after a specific date:

            $ gcloud projects list --format="table(projectNumber,projectId,createTime)" --filter="createTime>=2018-01-15"

          List projects that were created on and after a specific date and time
          and sort from oldest to newest (with dates and times listed according
          to the local timezone):

            $ gcloud projects list --format="table(projectNumber,projectId,createTime.date(tz=LOCAL))" --filter="createTime>=2018-01-15T12:00:00" --sort-by=createTime

          List projects that were created within the last two weeks, using
          ISO8601 durations:

            $ gcloud projects list --format="table(projectNumber,projectId,createTime)" --filter="createTime>-P2W"

          For more about ISO8601 durations, see: https://en.wikipedia.org/wiki/ISO_8601
          +
          The table below shows examples of pattern matching if used with
          the `:` operator:

          PATTERN | VALUE | MATCHES | DEPRECATED_MATCHES
          --- | --- | --- | ---
          abc* | abcpdqxyz | True | True
          abc | abcpdqxyz | False | True
          pdq* | abcpdqxyz | False | False
          pdq | abcpdqxyz | False | True
          xyz* | abcpdqxyz | False | False
          xyz | abcpdqxyz | False | True
          * | abcpdqxyz | True | True
          * | <None> | False | False
          * | <''> | False | False
          * | <otherwise> | True | True
          abc* | abc.pdq.xyz | True | True
          abc | abc.pdq.xyz | True | True
          abc.pdq | abc.pdq.xyz | True | True
          pdq* | abc.pdq.xyz | True | False
          pdq | abc.pdq.xyz | True | True
          pdq.xyz | abc.pdq.xyz | True | True
          xyz* | abc.pdq.xyz | True | False
          xyz | abc.pdq.xyz | True | True
          """),
  }
