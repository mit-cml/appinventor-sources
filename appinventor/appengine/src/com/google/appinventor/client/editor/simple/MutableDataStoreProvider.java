// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Extension of {@link DataStoreProvider} for stores that support write operations.
 * Implementing classes support create/update ({@link #setEntry}) and delete
 * ({@link #deleteEntry}) in addition to the read operations from the base interface.
 *
 * <p>The {@link com.google.appinventor.client.editor.simple.dialogs.DataVisualizerPanel}
 * detects this interface at runtime and shows/hides CRUD controls accordingly, so
 * read-only stores that only implement {@link DataStoreProvider} continue to work
 * without modification.
 */
public interface MutableDataStoreProvider extends DataStoreProvider {

  /**
   * Creates or overwrites a single entry (Redis SET).
   * Values should be JSON-encoded strings matching CloudDB's serialization format.
   */
  void setEntry(String tag, String value, AsyncCallback<Void> callback);

  /**
   * Deletes the entry with the given tag (Redis DEL).
   */
  void deleteEntry(String tag, AsyncCallback<Void> callback);
}
