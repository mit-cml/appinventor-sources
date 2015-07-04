// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import java.util.HashMap;
import java.util.Map;

/**
 * A global configuration database of per-class properties. Configuration
 * settings apply to (objects of) a class and all of its subclasses, as long as
 * they are not overridden for a more specific subclass.
 *
 *
 * @param <K> class of the keys
 * @param <V> class of the values
 */
public class Registry<K, V> {
  // Internal store for the configured values.
  private final Map<Class<?>, V> map;

  // Root class of the keys.
  private final Class<K> rootClass;

  /**
   * Creates a new registry.
   *
   * @param rootClass root class of the keys
   */
  protected Registry(Class<K> rootClass) {
    this.rootClass = rootClass;
    map = new HashMap<Class<?>, V>();
  }

  /**
   * Adds a new configuration setting to the registry. The value applies to the
   * given class and all subclasses, as long as there is no more specific
   * configuration.
   *
   * @param key the class for which to add configuration
   * @param value the configuration value
   */
  // TODO(user): For now, registries are rather static. We do not allow to
  // add new entries to a registry after they have been created; all
  // initialization has to be done in the constructor of a base class. Later
  // this restriction could be relaxed for better extensibility.
  protected void register(Class<? extends K> key, V value) {
    map.put(key, value);
  }

  /**
   * Returns the configured value for a given class. If no value has been
   * configured for the given class, the configuration of its super-class (or
   * super-super-class, an so on) will be returned.
   *
   * @param key the class for which to retrieve the configured value
   * @return the configured value or null if none is found
   */
  public V get(Class<? extends K> key) {
    if (map.containsKey(key)) {
      return map.get(key);
    } else if (key == rootClass) {
      return null;
    } else {
      // We know that the superclass extends K
      @SuppressWarnings("unchecked")
      Class<? extends K> superclass = (Class<? extends K>) key.getSuperclass();

      return get(superclass);
    }
  }

  /**
   * Convenience method that returns the configuration of an object's class.
   *
   * @see #get(Class)
   * @param key the object for whose class to determine the configuration
   * @return the object class's configuration
   */
  public V get(K key) {
    // We know that the class extends K
    @SuppressWarnings("unchecked")
    Class<? extends K> clazz = (Class<? extends K>) key.getClass();

    return get(clazz);
  }
}
