#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
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
#
# Copyright 2011 Google Inc. All Rights Reserved.

"""A thread-compatible singleton decorator.

Sometimes, only a single instance of a class should ever be created. This file
provides a wrapper that turns a class into a Singleton. The constructor may
only be called once; a static method Singleton() provides access to the
constructed instance. If Singleton() is called before the constructor, or if
the constructor is called multiple times, Errors are raised.

The only thread-safety guarantees this wrapper provides are:

* Multiple calls to Singleton() may occur concurrently, and no two calls will
  return different objects.
* `__init__` will never be called twice, even in the face of concurrent calls.
  Exactly one attempt will succeed, all others will raise an Error.

All calls to Singleton() must happen after the constructor call -- so either
the constructor must be invoked before any threads would try to access the
singleton (e.g. in main()), or else the threads must use an additional lock
to synchronize attempts to invoke the constructor. In that case, using a
memoized factory function or a non-singleton global guarded by a lock may be
more appropriate.

Singletons are often associated with bad coding practices; see
http://wiki/Main/SingletonsConsideredDangerous and decide if you should
really be using this functionality.

To make your singletons more testable, use the following idiom:


class A(...):
  "All the complicated code goes in here, can be tested normally..."
  ..
  ..


@singleton.Singleton
class B(A):
  "Singleton instance of A"


Example usage:

from google.pyglib import singleton


@singleton.Singleton
class Foo(object):
  "Example singleton"


a = Foo()
b = Foo.Singleton()
c = Foo.Singleton()
assert a == b
assert b == c
"""



import threading

_CLASS_LOCKS = {}  # Holds the per-class locks.
_CLASS_LOCKS_LOCK = threading.Lock()  # Lock for obtaining a per-class lock.
_INSTANCES = {}  # Mapping from class to instantiated object.


class Error(Exception):
  """Base error class."""


class AlreadyHasSingletonMethodError(Error):
  """Raised if the class already defines a Singleton() method."""

  def __init__(self, cls):
    Error.__init__(self)
    self.cls = cls

  def __str__(self):
    return 'Class already has a Singleton() method: %s' % self.cls


class NotConstructedError(Error):
  """Raised if the constructor has not been called yet."""

  def __init__(self, cls):
    Error.__init__(self)
    self.cls = cls

  def __str__(self):
    return 'Constructor has not yet been called for class %s' % self.cls


class ConstructorCalledAgainError(Error):
  """Raised if the constructor is called twice for a singleton."""

  def __init__(self, cls, args, kws):
    Error.__init__(self)
    self.cls = cls
    self.args = args
    self.kws = kws

  def __str__(self):
    return ('Constructor called (again) on class %s with args %s and kws %s'
            % (self.cls, self.args, self.kws))


def _GetClassLock(cls):
  """Returns the lock associated with the class."""
  with _CLASS_LOCKS_LOCK:
    if cls not in _CLASS_LOCKS:
      _CLASS_LOCKS[cls] = threading.Lock()
    return _CLASS_LOCKS[cls]


def Singleton(cls):
  """Turn a class into a singleton.

  Exactly one call to the constructor is required. After that, all future calls
  must be to the Singleton() static method.

  This code is multithread-safe. Shared locks are held for brief periods
  of time; when a class is instantiated, it uses a class specific lock.

  Args:
    cls: The class to decorate.

  Returns:
    The singleton class. Note that this class is a sub-class
    of the class it is decorating.

  Raises:
    AlreadyHasSingletonMethodError: If the class has a Singleton method
      defined.
  """
  if hasattr(cls, 'Singleton'):
    raise AlreadyHasSingletonMethodError(cls)

  class _Singleton(cls):

    def __init__(self, *args, **kws):
      class_lock = _GetClassLock(cls)
      with class_lock:
        if cls not in _INSTANCES:
          cls.__init__(self, *args, **kws)
          _INSTANCES[cls] = self
        else:
          raise ConstructorCalledAgainError(cls, args, kws)

    @staticmethod
    def Singleton():
      class_lock = _GetClassLock(cls)
      with class_lock:
        if cls not in _INSTANCES:
          raise NotConstructedError(cls)
        else:
          return _INSTANCES[cls]

  return _Singleton
