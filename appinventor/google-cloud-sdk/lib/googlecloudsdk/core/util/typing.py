# -*- coding: utf-8 -*- #
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

"""A module that will mirror the typing package if it is available."""
# This file lazily imports the typing module which confuses pytype.
# pytype: skip-file

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

try:
  # pylint: disable=unused-import
  # pylint: disable=g-import-not-at-top
  # Pytype can generally only be imported when testing under pytype.
  import pytype
  # typing is a testing dependency used for pytype checking.
  # Importing * causes an error.
  from typing import (
      AbstractSet, Any, AnyStr, ByteString, Callable, Container, ContextManager,
      Counter, DefaultDict, Deque, Dict, FrozenSet, Generator, GenericMeta,
      Hashable, IO, ItemsView, Iterable, Iterator, KeysView, List, Mapping,
      MappingView, MutableMapping, MutableSequence, MutableSet, NamedTuple,
      NewType, Optional, Reversible, Sequence, Set, Sized, SupportsAbs,
      SupportsComplex, SupportsFloat, SupportsInt, Text, Tuple, Type, TypeVar,
      Union, ValuesView)
except ImportError:
  # pylint: disable=invalid-name
  # Ironically, pytype complains if these aren't all defined.
  AbstractSet = None
  Any = None
  AnyStr = None
  ByteString = None
  Callable = None
  Container = None
  ContextManager = None
  Counter = None
  DefaultDict = None
  Deque = None
  Dict = None
  FrozenSet = None
  Generator = None
  GenericMeta = None
  Hashable = None
  IO = None
  ItemsView = None
  Iterable = None
  Iterator = None
  KeysView = None
  List = None
  Mapping = None
  MappingView = None
  MutableMapping = None
  MutableSequence = None
  MutableSet = None
  NamedTuple = None
  NewType = None
  Optional = None
  Reversible = None
  Sequence = None
  Set = None
  Sized = None
  SupportsAbs = None
  SupportsComplex = None
  SupportsFloat = None
  SupportsInt = None
  Text = None
  Tuple = None
  Type = None
  TypeVar = None
  Union = None
  ValuesView = None
