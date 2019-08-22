package com.google.appinventor.components.runtime;

/**
 * Interface for observable real-time data
 * producing Chart Data Source components.
 *
 * @param <K>  key (data identifier)
 * @param <V>  value (returned data type)
 */
public interface RealTimeChartDataSource<K, V> extends ObservableChartDataSource<K, V> {
}
