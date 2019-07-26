package com.google.appinventor.components.runtime;

public interface ObservableChartDataSource<K,V> extends ChartDataSource<K,V> {
  public void addDataSourceObserver(ChartDataBase dataComponent);

  public void removeDataSourceObserver(ChartDataBase dataComponent);

  public void notifyDataSourceObservers(String key, Object oldValue, Object newValue);
}
