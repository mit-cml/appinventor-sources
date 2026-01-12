// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.SimpleObject;

/**
 * Interface for acceptable Data Source components.
 * Contains the necessary methods to interact with the
 * data source.
 *
 * @param <K> key (data identifier)
 * @param <V> value (returned data type)
 */
@SimpleObject
public interface DataSource<K, V> {
  /**
   * Gets the specified data value.
   *
   * @param key identifier of the value
   * @return value identified by the key
   */
  V getDataValue(K key);
}
