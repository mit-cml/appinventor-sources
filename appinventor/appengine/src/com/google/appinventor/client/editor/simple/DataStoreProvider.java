// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple;

import com.google.appinventor.shared.rpc.clouddb.DataEntry;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * Implemented by designer mock components that back a data store (e.g. CloudDB,
 * TinyWebDB). Allows the generic {@link
 * com.google.appinventor.client.editor.simple.dialogs.DataVisualizerPanel} to fetch and
 * display the store's contents without knowing the specific component type.
 *
 * <p>Future storage components need only implement this interface and override
 * {@link com.google.appinventor.client.editor.simple.components.MockComponent#getPropertiesPanelExtension()}
 * — no changes to the panel or properties panel are required.
 */
public interface DataStoreProvider {

  /**
   * Returns the designer instance name of this component (e.g. "CloudDB1").
   */
  String getDataStoreName();

  /**
   * Returns the component type string (e.g. "CloudDB").
   */
  String getDataStoreType();

  /**
   * Asynchronously fetches all tag/value entries from the backing store.
   * The callback receives a list of {@link DataEntry} objects on success, or
   * a {@link Throwable} on failure.
   */
  void fetchEntries(AsyncCallback<List<DataEntry>> callback);
}
