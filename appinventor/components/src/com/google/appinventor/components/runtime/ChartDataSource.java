package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;

/**
 * Interface for acceptable Chart Data Source components.
 * Contains the necessary methods for the Chart Data component
 * to interact with the Data Source in importing data.
 *
 * TODO: REMARK:
 * TODO: Sometimes, ant compilation fails due to ChartDataSource
 * TODO: parameter parsing. In most cases this does not occur.
 * TODO: This might require more investigation.
 *
 * @param <K>  key (data identifier)
 * @param <V>  value (returned data type)
 */
@SimpleObject
public interface ChartDataSource<K, V> {
  /**
   * Gets the specified data value
   *
   * @param key  identifier of the value
   * @return  value identified by the key
   */
  public V getDataValue(K key);
}
