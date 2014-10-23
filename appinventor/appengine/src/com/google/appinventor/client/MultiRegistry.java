// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A registry that manages lists of settings for classes. Multiple configuration
 * values can be added to the same class.
 *
 *
 * @param <K> class of the keys
 * @param <V> class of the individual values
 */
public class MultiRegistry<K, V> {
  // Internal store for the configured values.
  // TODO(user): We could consider to use sets instead of lists to avoid
  // duplicates.
  private final Map<Class<? extends K>, List<V>> map = new HashMap<Class<? extends K>, List<V>>();

  // Root class of the keys.
  private final Class<K> rootClass;

  /**
   * Creates a new registry.
   *
   * @param rootClass root class of the keys
   */
  protected MultiRegistry(Class<K> rootClass) {
    this.rootClass = rootClass;
  }

  /**
   * Registers a single configuration setting that is added at the end of the
   * list of configured values.
   *
   * @param key the class for which to add a configuration setting
   * @param value the configured value
   */
  protected void register(Class<? extends K> key, V value) {
    List<V> values = map.get(key);
    if (values == null) {
      values = new ArrayList<V>();
      map.put(key, values);
    }
    values.add(value);
  }

  /**
   * Returns the list of configuration settings for the key class and all
   * superclasses.
   */
  public List<V> get(Class<? extends K> key) {
    List<V> result = new ArrayList<V>();

    Class<?> c = key;
    while (true) {
      List<V> values = map.get(c);
      if (values != null) {
        result.addAll(values);
      }
      if (c == rootClass) {
        break;
      }
      c = c.getSuperclass();
    }

    return result;
  }

  /**
   * Convenience method that returns the configuration values of an object's
   * class.
   *
   * @see #get(Class)
   * @param key the object for whose class to determine the configuration
   * @return the object class's configuration values
   */
  public List<V> get(K key) {
    // We know that the class extends K
    @SuppressWarnings("unchecked")
    Class<? extends K> clazz = (Class<? extends K>) key.getClass();

    return get(clazz);
  }
}
