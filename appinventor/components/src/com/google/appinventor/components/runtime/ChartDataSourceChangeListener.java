package com.google.appinventor.components.runtime;

/**
 * Interface for observing Data Source value changes.
 */
public interface ChartDataSourceChangeListener {
  public void onDataSourceValueChange(ChartDataSource component, String key, Object newValue);
}
