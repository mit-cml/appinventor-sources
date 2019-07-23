package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.SimpleObject;

@SimpleObject
public interface ChartDataSource<K, V> {
  public V getDataValue(K key);
}
