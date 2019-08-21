package com.google.appinventor.components.runtime;

/**
 * Interface for observing Data Source value changes.
 */
public interface ChartDataSourceChangeListener {
  /**
   * Event called when the value of the observed ChartDataSource component changes.
   *
   * @param component component that triggered the event
   * @param key       key of the value that changed
   * @param newValue  the new value of the observed value
   */
  public void onDataSourceValueChange(ChartDataSource component, String key, Object newValue);
}
