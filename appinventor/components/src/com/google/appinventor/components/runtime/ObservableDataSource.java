// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

/**
 * Interface for observable Data Source components.
 * Contains the necessary methods to link, unlink and
 * notify observers
 *
 * @param <K>  key (data identifier)
 * @param <V>  value (returned data type)
 */
public interface ObservableDataSource<K,V> extends DataSource<K,V> {
  /**
   * Adds a new Chart Data observer to the Data Source.
   *
   * @param dataComponent  Chart Data object to add as an observer
   */
  void addDataObserver(DataSourceChangeListener dataComponent);

  /**
   * Removes the specified Chart Data observer from the observers list,
   * if it exists.
   *
   * @param dataComponent  Chart Data object to remove
   */
  void removeDataObserver(DataSourceChangeListener dataComponent);

  /**
   * Notifies the observers of a value change.
   *
   * @param key  key of the value that changed
   * @param newValue  new value
   */
  void notifyDataObservers(K key, Object newValue);
}
