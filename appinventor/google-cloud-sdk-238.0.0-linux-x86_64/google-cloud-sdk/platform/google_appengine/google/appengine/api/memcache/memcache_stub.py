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



"""Stub version of the memcache API, keeping all data in process memory."""









import logging
import time

from google.appengine.api import apiproxy_stub
from google.appengine.api.memcache import memcache_service_pb
from google.appengine.runtime import apiproxy_errors


MemcacheSetResponse = memcache_service_pb.MemcacheSetResponse
MemcacheSetRequest = memcache_service_pb.MemcacheSetRequest
MemcacheIncrementRequest = memcache_service_pb.MemcacheIncrementRequest
MemcacheIncrementResponse = memcache_service_pb.MemcacheIncrementResponse
MemcacheDeleteResponse = memcache_service_pb.MemcacheDeleteResponse










_MEMCACHE_OVERHEAD = 73


_ONE_MEGABYTE = 1024 * 1024
_MAX_ITEM_TOTAL_SIZE = _ONE_MEGABYTE
_MAX_KEY_SIZE = 250
_MAX_VALUE_SIZE = _MAX_ITEM_TOTAL_SIZE - _MAX_KEY_SIZE - _MEMCACHE_OVERHEAD
MAX_REQUEST_SIZE = 32 << 20





DEFAULT_MAX_SIZE_BYTES = 1e15


class _LRUChainableElement(object):
  """A base class for elements in the LRU cache."""

  def __init__(self, key='', value=''):
    """Initializes an _LRUChainableElement."""
    self.newer = None
    self.older = None
    self.value = value
    self.key = key

  @property
  def byte_size(self):
    return len(self.key + self.value)


class CacheEntry(_LRUChainableElement):
  """An entry in the cache."""

  def __init__(self, value, expiration, flags, cas_id, gettime, namespace, key):
    """Initializer.

    Args:
      value: String containing the data for this entry.
      expiration: Number containing the expiration time or offset in seconds
        for this entry.
      flags: Opaque flags used by the memcache implementation.
      cas_id: Unique Compare-And-Set ID.
      gettime: Used for testing. Function that works like time.time().
      namespace: String namespace that this entry is stored under.
      key: String key used to retrieve the item from the namespace.
    """
    super(CacheEntry, self).__init__(key, value)
    assert isinstance(value, basestring)
    assert len(value) <= _MAX_VALUE_SIZE
    assert isinstance(expiration, (int, long))



    self.gettime = gettime
    self.flags = flags
    self.cas_id = cas_id
    self.created_time = self.gettime()
    self.will_expire = expiration != 0
    self.locked = False
    self.namespace = namespace
    self._SetExpiration(expiration)

  def _SetExpiration(self, expiration):
    """Sets the expiration for this entry.

    Args:
      expiration: Number containing the expiration time or offset in seconds
        for this entry. If expiration is above one month, then it's considered
        an absolute time since the UNIX epoch.
    """
    if expiration > (86400 * 30):
      self.expiration_time = expiration
    else:
      self.expiration_time = self.gettime() + expiration

  def CheckExpired(self):
    """Returns True if this entry has expired; False otherwise."""
    return self.will_expire and self.gettime() >= self.expiration_time

  def ExpireAndLock(self, timeout):
    """Marks this entry as deleted and locks it for the expiration time.

    Used to implement memcache's delete timeout behavior.

    Args:
      timeout: Parameter originally passed to memcache.delete or
        memcache.delete_multi to control deletion timeout.
    """
    self.will_expire = True
    self.locked = True
    self._SetExpiration(timeout)

  def CheckLocked(self):
    """Returns True if this entry was deleted but has not yet timed out."""
    return self.locked and not self.CheckExpired()


class MemcacheServiceStub(apiproxy_stub.APIProxyStub):
  """Python only memcache service stub.

  This stub keeps all data in the local process' memory, not in any
  external servers.
  """

  THREADSAFE = True

  def __init__(self,
               gettime=time.time,
               service_name='memcache',
               max_size_bytes=DEFAULT_MAX_SIZE_BYTES):
    """Initializer.

    Args:
      gettime: time.time()-like function used for testing.
      service_name: Service name expected for all calls.
      max_size_bytes: The maximum total cache size in bytes, used for LRU
        evictions.
    """
    super(MemcacheServiceStub, self).__init__(
        service_name, max_request_size=MAX_REQUEST_SIZE)
    self._lru = LRU()
    self._next_cas_id = 1
    self._gettime = gettime
    self._ResetStats()
    self._max_size_bytes = max_size_bytes


    self._the_cache = {}




    self._static_clock_time = None

  def _ResetStats(self):
    """Resets statistics information."""

    self._hits = 0
    self._misses = 0
    self._byte_hits = 0

    self._cache_creation_time = self._gettime()

  @apiproxy_stub.Synchronized
  def _GetKey(self, namespace, key):
    """Retrieves a CacheEntry from the cache if it hasn't expired.

    Does not take deletion timeout into account.

    Args:
      namespace: The namespace that keys are stored under.
      key: The key to retrieve from the cache.

    Returns:
      The corresponding CacheEntry instance, or None if it was not found or
      has already expired.
    """
    namespace_dict = self._the_cache.get(namespace, None)
    if namespace_dict is None:
      return None
    entry = namespace_dict.get(key, None)
    if entry is None:
      return None
    elif entry.CheckExpired():
      self._lru.Remove(entry)
      del namespace_dict[key]
      return None
    elif not entry.will_expire:
      self._lru.Update(entry)

    return entry

  @apiproxy_stub.Synchronized
  def _Dynamic_Get(self, request, response):
    """Implementation of MemcacheService::Get().

    Args:
      request: A MemcacheGetRequest.
      response: A MemcacheGetResponse.
    """
    namespace = request.name_space()
    keys = set(request.key_list())
    for key in keys:
      entry = self._GetKey(namespace, key)
      if entry is None or entry.CheckLocked():
        self._misses += 1
        continue
      self._hits += 1
      self._byte_hits += len(key) + len(entry.value)
      item = response.add_item()
      item.set_key(key)
      item.set_value(entry.value)
      item.set_flags(entry.flags)
      if request.for_cas():
        item.set_cas_id(entry.cas_id)

  @apiproxy_stub.Synchronized
  def _Dynamic_Set(self, request, response):
    """Implementation of MemcacheService::Set().

    Args:
      request: A MemcacheSetRequest.
      response: A MemcacheSetResponse.
    """
    namespace = request.name_space()
    for item in request.item_list():
      key = item.key()
      set_policy = item.set_policy()
      old_entry = self._GetKey(namespace, key)

      set_status = MemcacheSetResponse.NOT_STORED
      if ((set_policy == MemcacheSetRequest.SET) or
          (set_policy == MemcacheSetRequest.ADD and old_entry is None) or
          (set_policy == MemcacheSetRequest.REPLACE and old_entry is not None)):


        if (old_entry is None or set_policy == MemcacheSetRequest.SET or
            not old_entry.CheckLocked()):
          set_status = MemcacheSetResponse.STORED

      elif (set_policy == MemcacheSetRequest.CAS and item.has_cas_id()):
        if old_entry is None or old_entry.CheckLocked():
          set_status = MemcacheSetResponse.NOT_STORED
        elif old_entry.cas_id != item.cas_id():
          set_status = MemcacheSetResponse.EXISTS
        else:
          set_status = MemcacheSetResponse.STORED

      if set_status == MemcacheSetResponse.STORED:
        if namespace not in self._the_cache:
          self._the_cache[namespace] = {}
        if old_entry:
          self._lru.Remove(old_entry)
        self._the_cache[namespace][key] = CacheEntry(
            item.value(),
            item.expiration_time(),
            item.flags(),
            self._next_cas_id,
            gettime=self._gettime,
            namespace=namespace,
            key=key)
        self._lru.Update(self._the_cache[namespace][key])
        while self._lru.total_byte_size > self._max_size_bytes:
          oldest = self._lru.oldest
          self._lru.Remove(oldest)
          del self._the_cache[oldest.namespace][oldest.key]
        self._next_cas_id += 1

      response.add_set_status(set_status)

  @apiproxy_stub.Synchronized
  def _Dynamic_Delete(self, request, response):
    """Implementation of MemcacheService::Delete().

    Args:
      request: A MemcacheDeleteRequest.
      response: A MemcacheDeleteResponse.
    """
    namespace = request.name_space()
    for item in request.item_list():
      key = item.key()
      entry = self._GetKey(namespace, key)

      delete_status = MemcacheDeleteResponse.DELETED
      if entry is None:
        delete_status = MemcacheDeleteResponse.NOT_FOUND
      elif item.delete_time() == 0:
        self._lru.Remove(entry)
        del self._the_cache[namespace][key]
      else:

        self._lru.Remove(entry)
        entry.ExpireAndLock(item.delete_time())

      response.add_delete_status(delete_status)

  @apiproxy_stub.Synchronized
  def _internal_increment(self, namespace, request, is_batch_increment=False):
    """Internal function for incrementing from a MemcacheIncrementRequest.

    Args:
      namespace: A string containing the namespace for the request, if any.
        Pass an empty string if there is no namespace.
      request: A MemcacheIncrementRequest instance.
      is_batch_increment: True if this is part of a batch increment request.
        InvalidValue errors are not raised during batch increments.

    Returns:
      An integer or long if the offset was successful, None on error.
    """
    key = request.key()
    entry = self._GetKey(namespace, key)
    if entry is None or entry.CheckLocked():
      if not request.has_initial_value():
        return None
      if namespace not in self._the_cache:
        self._the_cache[namespace] = {}
      flags = 0
      if request.has_initial_flags():
        flags = request.initial_flags()
      self._the_cache[namespace][key] = CacheEntry(
          str(request.initial_value()),
          expiration=0,
          flags=flags,
          cas_id=self._next_cas_id,
          gettime=self._gettime,
          namespace=namespace,
          key=key)
      self._next_cas_id += 1
      entry = self._GetKey(namespace, key)
      assert entry is not None

    try:
      old_value = long(entry.value)
    except ValueError:

      logging.error('Increment/decrement failed: Could not interpret '
                    'value for key = "%s" as an unsigned integer.', key)
      return self._RaiseInvalidValueError(is_batch_increment)

    if old_value < 0:




      logging.error('Increment/decrement failed: Could not interpret '
                    'value for key = "%s" as an unsigned integer.', key)
      return self._RaiseInvalidValueError(is_batch_increment)

    if old_value >= 2**64:
      logging.error('Increment value for key = "%s" will be higher than the '
                    'max value for a 64-bit integer.', key)
      return self._RaiseInvalidValueError(is_batch_increment)

    delta = request.delta()
    if request.direction() == MemcacheIncrementRequest.DECREMENT:
      delta = -delta


    new_value = max(old_value + delta, 0) % (2**64)

    entry.value = str(new_value)
    return new_value

  def _RaiseInvalidValueError(self, is_batch_increment):
    """Raise an InvalidValue error unless using a batch increment."""
    if not is_batch_increment:
      raise apiproxy_errors.ApplicationError(
          memcache_service_pb.MemcacheServiceError.INVALID_VALUE)

  def _Dynamic_Increment(self, request, response):
    """Implementation of MemcacheService::Increment().

    Args:
      request: A MemcacheIncrementRequest.
      response: A MemcacheIncrementResponse.
    """
    namespace = request.name_space()
    new_value = self._internal_increment(namespace, request)
    if new_value is None:
      raise apiproxy_errors.ApplicationError(
          memcache_service_pb.MemcacheServiceError.UNSPECIFIED_ERROR)
    response.set_new_value(new_value)

  @apiproxy_stub.Synchronized
  def _Dynamic_BatchIncrement(self, request, response):
    """Implementation of MemcacheService::BatchIncrement().

    Args:
      request: A MemcacheBatchIncrementRequest.
      response: A MemcacheBatchIncrementResponse.
    """
    namespace = request.name_space()
    for request_item in request.item_list():
      new_value = self._internal_increment(
          namespace, request_item, is_batch_increment=True)
      item = response.add_item()
      if new_value is None:
        item.set_increment_status(MemcacheIncrementResponse.NOT_CHANGED)
      else:
        item.set_increment_status(MemcacheIncrementResponse.OK)
        item.set_new_value(new_value)

  @apiproxy_stub.Synchronized
  def _Dynamic_FlushAll(self, request, response):
    """Implementation of MemcacheService::FlushAll().

    Args:
      request: A MemcacheFlushRequest.
      response: A MemcacheFlushResponse.
    """
    self.Clear()

  @apiproxy_stub.Synchronized
  def _Dynamic_Stats(self, request, response):
    """Implementation of MemcacheService::Stats().

    Args:
      request: A MemcacheStatsRequest.
      response: A MemcacheStatsResponse.
    """
    stats = response.mutable_stats()
    stats.set_hits(self._hits)
    stats.set_misses(self._misses)
    stats.set_byte_hits(self._byte_hits)
    stats.set_items(len(self._lru))
    stats.set_bytes(self._lru.total_byte_size)
    if self._lru.oldest:
      stats.set_oldest_item_age(
          int(self._gettime() - self._lru.oldest.created_time))
    else:
      stats.set_oldest_item_age(0)

  @apiproxy_stub.Synchronized
  def _Dynamic_SetMaxSize(self, request, response):
    """Implementation of MemcacheStubService::SetMaxSize().

    Args:
      request: A SetMaxSizeRequest.
      response: A base_pb.VoidProto.
    """
    self._max_size_bytes = request.max_size_bytes()

  @apiproxy_stub.Synchronized
  def _Dynamic_GetLruChainLength(self, request, response):
    """Implementation of MemcacheStubService::GetLruChainLength().

    Args:
      request: A base_pb.VoidProto.
      response: A GetLruChainLengthResponse.
    """
    response.set_chain_length(len(self._lru))

  @apiproxy_stub.Synchronized
  def _Dynamic_SetClock(self, request, response):
    """Implementation of MemcacheStubService::SetClock().

    Args:
      request: A SetClockRequest.
      response: A base_pb.VoidProto.
    """
    self._static_clock_time = request.clock_time_milliseconds() / 1000.0
    self._gettime = lambda: self._static_clock_time
    for namespace in self._the_cache.itervalues():
      for item in namespace.itervalues():
        item.gettime = lambda: self._static_clock_time

  @apiproxy_stub.Synchronized
  def _Dynamic_AdvanceClock(self, request, response):
    """Implementation of MemcacheStubService::AdvanceClock().

    Args:
      request: An AdvanceClockRequest.
      response: An AdvanceClockResponse.
    """
    self._static_clock_time += request.milliseconds() / 1000.0
    response.set_clock_time_milliseconds(self._static_clock_time * 1000)

  @apiproxy_stub.Synchronized
  def Clear(self):
    """Clears the memcache stub and resets stats."""
    self._the_cache.clear()
    self._max_size_bytes = DEFAULT_MAX_SIZE_BYTES
    self._gettime = time.time
    self._ResetStats()
    self._lru.Clear()


class LRU(object):
  """Implements an LRU cache by intrusive chaining on elements.

  Also keeps track of the total size of the elements in bytes, as well as the
  length of the LRU chain.

  Heavily inspired by //j/c/g/appengine/api/memcache/dev/LRU.java.
  """

  def __init__(self):
    self._newest = None
    self._oldest = None
    self._total_byte_size = 0
    self._length = 0

  def __len__(self):
    return self._length

  @property
  def newest(self):
    return self._newest

  @property
  def oldest(self):
    return self._oldest

  def Clear(self):
    """Clears the LRU chain."""
    self._newest = None
    self._oldest = None
    self._total_byte_size = 0
    self._length = 0

  @property
  def total_byte_size(self):
    return self._total_byte_size

  def IsEmpty(self):
    """Checks if the LRU chain is empty."""
    return self._newest is None and self._oldest is None

  def Update(self, element):
    """Updates an item in the LRU chain."""
    if not element:
      raise ValueError('LRU Cache element cannot be empty')
    self.Remove(element)
    if self._newest is not None:
      self._newest.newer = element
    element.newer = None
    element.older = self._newest
    self._newest = element
    if self._oldest is None:
      self._oldest = element
    self._total_byte_size += element.byte_size
    self._length += 1

  def Remove(self, element):
    """Removes an item from the LRU chain."""
    if not element:
      raise ValueError('LRU Cache element cannot be empty')
    newer = element.newer
    older = element.older
    element_acted_on = False
    if newer is not None:
      element_acted_on = True
      newer.older = older
    if older is not None:
      element_acted_on = True
      older.newer = newer
    if element == self._newest:
      element_acted_on = True
      self._newest = older
    if element == self._oldest:
      element_acted_on = True
      self._oldest = newer
    element.newer = None
    element.older = None
    if element_acted_on:
      self._length -= 1
      self._total_byte_size -= element.byte_size

  def RemoveOldest(self):
    """Removes the oldest item from the LRU chain."""
    self.Remove(self.oldest)
