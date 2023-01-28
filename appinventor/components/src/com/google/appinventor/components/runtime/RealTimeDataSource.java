// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

/**
 * Interface for observable real-time data
 * producing Data Source components.
 *
 * @param <K> key (data identifier)
 * @param <V> value (returned data type)
 */
public interface RealTimeDataSource<K, V> extends ObservableDataSource<K, V> {
}
