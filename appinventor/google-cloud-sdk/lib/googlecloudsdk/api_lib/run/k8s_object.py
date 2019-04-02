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
"""Utilities for wrapping/dealing with a k8s-style objects."""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import abc
import collections
from apitools.base.protorpclite import messages
from googlecloudsdk.api_lib.run import condition
from googlecloudsdk.core.console import console_attr

import six


REGION_LABEL = 'cloud.googleapis.com/location'


def InitializedInstance(msg_cls):
  """Produce an instance of msg_cls, with all sub-messages initialized.

  Args:
    msg_cls: A message-class to be instantiated.
  Returns:
    An instance of the given class, with all fields initialized blank objects.
  """
  args = {
      field.name: ([] if field.repeated else
                   InitializedInstance(field.message_type))
      for field in msg_cls.all_fields()
      if isinstance(field, messages.MessageField)
  }
  return msg_cls(**args)


@six.add_metaclass(abc.ABCMeta)
class KubernetesObject(object):
  """Base class for wrappers around Kubernetes-style Object messages.

  Requires subclasses to provide class-level constants KIND for the k8s Kind
  field, and API_CATEGORY for the k8s API Category. It infers the API version
  from the version of the client object.

  Additionally, you can set READY_CONDITION and TERMINAL_CONDITIONS to be the
  name of a condition that indicates readiness, and a set of conditions
  indicating a steady state, respectively.
  """

  READY_CONDITION = 'Ready'

  @classmethod
  def SpecOnly(cls, spec, messages_mod):
    """Produce a wrapped message with only the given spec.

    It is meant to be used as part of another message; it will error if you
    try to access the metadata or status.

    Arguments:
      spec: The spec to include
      messages_mod: the messages module

    Returns:
      a new k8s_object with only the given spec.
    """
    msg_cls = getattr(messages_mod, cls.KIND)
    return cls(msg_cls(spec=spec), messages_mod, spec_only=True)

  @classmethod
  def New(cls, client, namespace):
    """Produce a new wrapped message of the appropriate type.

    All the sub-objects in it are recursively initialized to the appropriate
    message types, and the kind, apiVersion, and namespace set.

    Arguments:
      client: the API client to use
      namespace: The namespace to create the object in

    Returns:
      The newly created wrapped message.
    """
    api_version = '{}/{}'.format(cls.API_CATEGORY, getattr(client, '_VERSION'))
    messages_mod = client.MESSAGES_MODULE
    ret = InitializedInstance(getattr(messages_mod, cls.KIND))
    try:
      ret.kind = cls.KIND
      ret.apiVersion = api_version
    except AttributeError:
      # TODO(b/113172423): Workaround. Some top-level messages don't have
      # apiVersion and kind yet but they should
      pass
    ret.metadata.namespace = namespace
    return cls(ret, messages_mod)

  def __init__(self, to_wrap, messages_mod, spec_only=False):
    if not isinstance(to_wrap, getattr(messages_mod, self.KIND)):
      raise ValueError('Oops, trying to wrap wrong kind of message')
    self._m = to_wrap
    self._messages = messages_mod
    self._spec_only = spec_only

  def MessagesModule(self):
    """Return the messages module."""
    return self._messages

  def AssertFullObject(self):
    if self._spec_only:
      raise ValueError('This instance is spec-only.')

  # Access the "raw" k8s message parts. When subclasses want to allow mutability
  # they should provide their own convenience properties with setters.
  @property
  def kind(self):
    self.AssertFullObject()
    return self._m.kind

  @property
  def apiVersion(self):  # pylint: disable=invalid-name
    self.AssertFullObject()
    return self._m.apiVersion

  @property
  def spec(self):
    return self._m.spec

  @property
  def status(self):
    self.AssertFullObject()
    return self._m.status

  @property
  def metadata(self):
    self.AssertFullObject()
    return self._m.metadata

  # Alias common bits of metadata to the top level, for convenience.
  @property
  def name(self):
    self.AssertFullObject()
    return self._m.metadata.name

  @name.setter
  def name(self, value):
    self.AssertFullObject()
    self._m.metadata.name = value

  @property
  def creation_timestamp(self):
    return self.metaddata.creationTimestamp

  @property
  def namespace(self):
    self.AssertFullObject()
    return self._m.metadata.namespace

  @namespace.setter
  def namespace(self, value):
    self.AssertFullObject()
    self._m.metadata.namespace = value

  @property
  def resource_version(self):
    self.AssertFullObject()
    return self._m.metadata.resourceVersion

  @property
  def region(self):
    self.AssertFullObject()
    return self.labels[REGION_LABEL]

  @property
  def generation(self):
    self.AssertFullObject()
    # For the hack where generation is in spec, it's worse than unpopulated in
    # the metadata; it's always `1`.
    # TODO(b/110275620): remove this hack.
    if getattr(self._m.spec, 'generation', None) is not None:
      return self._m.spec.generation
    return self._m.metadata.generation

  @property
  def conditions(self):
    self.AssertFullObject()
    if self._m.status:
      c = self._m.status.conditions
    else:
      c = []
    return condition.Conditions(
        c,
        self.READY_CONDITION,
        getattr(self._m.status, 'observedGeneration', None),
        self.generation,
    )

  @property
  def annotations(self):
    self.AssertFullObject()
    return AnnotationsFromMetadata(self._messages, self._m.metadata)

  @property
  def labels(self):
    self.AssertFullObject()

    if not self._m.metadata.labels:
      self._m.metadata.labels = self._messages.ObjectMeta.LabelsValue(
          additionalProperties=[])

    return ListAsDictionaryWrapper(
        self._m.metadata.labels.additionalProperties,
        self._messages.ObjectMeta.LabelsValue.AdditionalProperty,
        key_field='key',
        value_field='value',
    )

  @property
  def ready(self):
    assert hasattr(self, 'READY_CONDITION')
    cond = self.conditions
    if self.READY_CONDITION in cond:
      return cond[self.READY_CONDITION]['status']
    return None

  def _PickSymbol(self, best, alt, encoding):
    """Choose the best symbol (if it's in this encoding) or an alternate."""
    try:
      best.encode(encoding)
      return best
    except UnicodeError:
      return alt

  @property
  def ready_symbol(self):
    """Return a symbol summarizing the status of this object."""
    # NB: This can be overridden by subclasses to allow symbols for more
    # complex reasons the object isn't ready. Ex: Service overrides it to
    # provide '!' for "I'm serving, but not the revision you wanted."
    encoding = console_attr.GetConsoleAttr().GetEncoding()
    if self.ready is None:
      return self._PickSymbol('\N{HORIZONTAL ELLIPSIS}', '.', encoding)
    elif self.ready:
      return self._PickSymbol('\N{HEAVY CHECK MARK}', '+', encoding)
    else:
      return 'X'

  def Message(self):
    """Return the actual message we've wrapped."""
    return self._m

  def MakeSerializable(self):
    return self.Message()


def AnnotationsFromMetadata(messages_mod, metadata):
  if not metadata.annotations:
    metadata.annotations = messages_mod.ObjectMeta.AnnotationsValue()
  return ListAsDictionaryWrapper(
      metadata.annotations.additionalProperties,
      messages_mod.ObjectMeta.AnnotationsValue.AdditionalProperty,
      key_field='key',
      value_field='value')


class LazyListWrapper(collections.MutableSequence):
  """Wraps a list that does not exist at object creation time.

  We sometimes have a need to allow access to a list property of a nested
  message, when we're not sure if all the layers above the list exist yet.
  We want to arrange it so that when you write to the list, all the above
  messages are lazily created.

  When you create a LazyListWrapper, you pass in a create function, which
  must do whatever setup you need to do, and then return the list that it
  creates in an underlying message.

  As soon as you start adding items to the LazyListWrapper, it will do the
  setup for you. Until then, it won't create any underlying messages.
  """

  def __init__(self, create):
    self._create = create
    self._l = None

  def __getitem__(self, i):
    if self._l:
      return self._l[i]
    raise IndexError()

  def __setitem__(self, i, v):
    if self._l is None:
      self._l = self._create()
    self._l[i] = v

  def __delitem__(self, i):
    if self._l:
      del self._l[i]
    else:
      raise IndexError()

  def __len__(self):
    if self._l:
      return len(self._l)
    return 0

  def insert(self, i, v):
    if self._l is None:
      self._l = self._create()
    self._l.insert(i, v)


class ListAsDictionaryWrapper(collections.MutableMapping):
  """Wraps repeated messages field with name and value in a dict-like object.

  Properties which resemble dictionaries (e.g. environment variables, build
  template arguments) are represented in the underlying messages fields as a
  list of objects, each of which has a name and value field. This class wraps
  that list in a dict-like object that can be used to mutate the underlying
  fields in a more Python-idiomatic way.
  """

  def __init__(self, to_wrap, item_class,
               key_field='name', value_field='value'):
    """Wrap a list of messages to be accessible as a dictionary.

    Arguments:
      to_wrap: List[Message], List of messages to treat as a dictionary.
      item_class: type of the underlying Message objects
      key_field: attribute to use as the keys of the dictionary
      value_field: attribute to use as the values of the dictionary

    """
    self._m = to_wrap
    self._item_class = item_class
    self._key_field = key_field
    self._value_field = value_field

  def __getitem__(self, key):
    """Implements evaluation of `self[key]`."""
    for item in self._m:
      if getattr(item, self._key_field) == key:
        return getattr(item, self._value_field)
    raise KeyError(key)

  def __setitem__(self, key, value):
    """Implements evaluation of `self[key] = value`."""
    for item in self._m:
      if getattr(item, self._key_field) == key:
        setattr(item, self._value_field, value)
        break
    else:
      self._m.append(self._item_class(**{
          self._key_field: key,
          self._value_field: value}))

  def __delitem__(self, key):
    """Implements evaluation of `del self[key]`."""
    index_to_delete = 0
    for index, elem in enumerate(self._m):
      if getattr(elem, self._key_field) == key:
        index_to_delete = index
        break
    else:
      raise KeyError(key)

    del self._m[index_to_delete]

  def __contains__(self, item):
    """Implements evaluation of `item in self`."""
    for list_elem in self._m:
      if getattr(list_elem, self._key_field) == item:
        return True
    return False

  def __len__(self):
    """Implements evaluation of `len(self)`."""
    return len(self._m)

  def __iter__(self):
    """Returns a generator yielding the env var keys."""
    for item in self._m:
      yield getattr(item, self._key_field)

  def MakeSerializable(self):
    return self._m

  def __str__(self):
    return ', '.join('{}: {}'.format(k, v) for k, v in self.items())
