package com.google.appinventor.components.runtime;

/**
 * Interface for observable Chart Data Source components.
 * Contains the necessary methods to link, unlink and
 * notify observers
 *
 * @param <K>  key (data identifier)
 * @param <V>  value (returned data type)
 */
public interface ObservableChartDataSource<K,V> extends ChartDataSource<K,V> {
  /**
   * Adds a new Chart Data observer to the Data Source
   * @param dataComponent  Chart Data object to add as an observer
   */
  public void addDataObserver(ChartDataBase dataComponent);

  /**
   * Removes the specified Chart Data observer from the observers list,
   * if it exists.
   * @param dataComponent  Chart Data object to remove
   */
  public void removeDataObserver(ChartDataBase dataComponent);

  /**
   * Notifies the observers of a value change
   * @param key  key of the value that changed
   * @param newValue  new value
   */
  public void notifyDataObservers(K key, Object newValue);
}
