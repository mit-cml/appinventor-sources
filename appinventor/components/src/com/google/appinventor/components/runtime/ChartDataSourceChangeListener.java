package com.google.appinventor.components.runtime;

public interface ChartDataSourceChangeListener {
  public void onDataSourceValueChange(ChartDataSource component, String key, Object oldValue, Object newValue);
}
