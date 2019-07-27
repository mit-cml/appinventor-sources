package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;

/**
 * Interface for acceptable Chart Data Source components.
 * Contains the necessary methods for the Chart Data component
 * to interact with the Data Source in importing data.
 *
 * @param <K>  key (data identifier)
 * @param <V>  value (returned data type)
 */
@SimpleObject
@DesignerComponent(version = 0) // This is required to prevent cast crashes
public interface ChartDataSource<K, V> {
  /**
   * Gets the specified data value
   *
   * @param key  identifier of the value
   * @return  value identified by the key
   */
  public V getDataValue(K key);
}
