package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;

@SimpleObject
@DesignerComponent(version = 0)
public interface ChartDataSource<K, V> {
  public V getDataValue(K key);
}
