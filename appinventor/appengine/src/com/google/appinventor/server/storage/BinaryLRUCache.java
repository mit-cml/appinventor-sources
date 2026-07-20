// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An LRU Cache that limits storage based on the total byte size
 * of the binary values (byte arrays).
 *
 * WARNING: This version assumes that the key is a hash of the content
 * so values are effectively immutable.
 */
public class BinaryLRUCache {

  private final long maxByteSize;
  private long currentByteSize;
  private final LinkedHashMap<String, byte[]> map;

  /**
   * @param maxByteSize The maximum aggregate size of byte arrays allowed.
   */
  public BinaryLRUCache(long maxByteSize) {
    this.maxByteSize = maxByteSize;
    this.currentByteSize = 0;

    // Initial capacity 16, load factor 0.75, and 'true' for access-order
    this.map = new LinkedHashMap<String, byte[]>(16, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
        // If we are over capacity, remove the eldest entry
        if (currentByteSize > maxByteSize) {
          currentByteSize -= eldest.getValue().length;
          return true;
        }
        return false;
      }
    };
  }

  /**
   * Associates the specified binary value with the specified key.
   * If the key already exists, the size delta is calculated.
   */
  public synchronized void put(String key, byte[] value) {
    if (value == null) return;

    // Note: We just return because the key is a hash of the value
    // so the value is effectively immutable
    if (map.containsKey(key)) {
      map.put(key, value);      // So the LRU is updated
      return;
    }

    // // If key exists, subtract its old size before adding new size
    // if (map.containsKey(key)) {
    //   currentByteSize -= map.get(key).length;
    // }

    currentByteSize += value.length;
    map.put(key, value);

    // Explicitly trigger removal if the single new item is huge,
    // or if multiple items need to be purged.
    while (currentByteSize > maxByteSize && !map.isEmpty()) {
      String eldestKey = map.keySet().iterator().next();
      byte[] removedValue = map.remove(eldestKey);
      if (removedValue != null) {
        currentByteSize -= removedValue.length;
      }
    }
  }

  /**
   * Returns the value to which the specified key is mapped.
   * This moves the accessed key to the "most recently used" position.
   */
  public synchronized byte[] get(String key) {
    return map.get(key);
  }

  public synchronized long getCurrentByteSize() {
    return currentByteSize;
  }

  public synchronized int getEntryCount() {
    return map.size();
  }
}
