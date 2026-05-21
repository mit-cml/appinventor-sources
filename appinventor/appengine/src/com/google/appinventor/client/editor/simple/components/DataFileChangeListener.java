// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

/**
 * Listener interface for receiving DataFile column change events.
 *
 * <p>Classes referencing a DataFile source should implement this
 * interface to receive events when the Source file of the DataFile
 * is changed, and the columns are changed as a result.
 *
 * <p>The columns/rows of the MockDataFile change asynchronously,
 * therefore this listener is intended to be used to notify
 * all the attached listeners after asynchronous reading
 * finishes.
 */
public interface DataFileChangeListener {
  /**
   * Invoked when the columns of a specified DataFile change.
   * @param csvFile  MockDataFile component of which the columns changed
   */
  void onColumnsChange(MockDataFile csvFile);
}
