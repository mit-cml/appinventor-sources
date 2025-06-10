// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;

import com.google.appinventor.client.editor.simple.SimpleEditor;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.google.gwt.user.client.ui.Image;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mock component for the Data File component.
 * Is a non-visible component due to serving as a File.
 */
public class MockDataFile extends MockNonVisibleComponent {
  public static final String TYPE = "DataFile";

  private static final String PROPERTY_NAME_SOURCE_FILE = "SourceFile";

  private List<String> columnNames; // First row of the Data File's contents

  private List<List<String>> columns; // Parsed columns of the Data File

  private final Set<DataFileChangeListener> dataFileChangeListeners = new HashSet<>();

  /**
   * Creates a new instance of a DataFile component whose icon is
   * loaded dynamically (not part of the icon image bundle).
   */
  public MockDataFile(SimpleEditor editor, String type, Image iconImage) {
    super(editor, type, iconImage);
  }

  /**
   * Changes the Source File property of the MockDataFile.
   * Upon change, the new file is imported and parsed, the
   * contents are stored, and DataFileChangeListeners are
   * updated.
   *
   * @param fileSource  name of the new Source File
   */
  private void setSourceFileProperty(String fileSource) {
    // Update the source file property & reset the
    // columnNames and rows property
    columnNames = new ArrayList<>();

    // Update DataFileChangeListeners to notify that
    // the columns list is (at least temporarily) empty
    updateDataFileChangeListeners();

    // Check that the SourceFile property is a valid file
    if (fileSource == null || fileSource.equals("")) {
      return;
    }

    // Read the media file
    long projectId = editor.getProjectId();

    Ode.getInstance().getProjectService().loadDataFile(projectId, "assets/" + fileSource,
        new AsyncCallback<List<List<String>>>() {
          @Override
          public void onFailure(Throwable caught) {
            ErrorReporter.reportError(caught.getMessage());
          }

          @Override
          public void onSuccess(List<List<String>> result) {
            // Update columns property
            columns = result;

            if (result.isEmpty()) {
              ErrorReporter.reportError(MESSAGES.emptyFileError());
              return;
            }

            // Populate columnNames List (it was empty before)
            for (List<String> column : columns) {
              // Add first element of the column if the column is not empty
              if (!column.isEmpty()) {
                columnNames.add(column.get(0));
              }
            }

            // Notify DataFileChangeListeners of the changes
            updateDataFileChangeListeners();

            // Hide the info message shown after setting the Source File property
            ErrorReporter.hide();
          }
        });

    // Show message to indicate parsing of the files
    // (since this is an asynchronous operation)
    ErrorReporter.reportInfo(MESSAGES.dataFileParsingMessage(fileSource, this.getName()));
  }

  /**
   * Get the Column Names (first row) of the MockDataFile's parsed content.
   *
   * @return  column names of the Data File (list of Strings)
   */
  public List<String> getColumnNames() {
    return columnNames;
  }

  /**
   * Returns a List of the specified columns.
   *
   * <p>If a column is not found, it is substituted by
   * an empty List.
   *
   * @param columns  List of columns to get
   * @return  List of columns (a column is a List of Strings)
   */
  public List<List<String>> getColumns(List<String> columns) {
    List<List<String>> dataColumns = new ArrayList<>();

    for (String column : columns) {
      // Get the index of the column and get the column itself
      int index = columnNames.indexOf(column);
      List<String> dataColumn = getColumn(index);

      // Add the column to the result
      dataColumns.add(dataColumn);
    }

    return dataColumns;
  }

  /**
   * Gets the column in the specified index.
   *
   * @param index  index of the column
   * @return  List of Strings representing the column
   * */
  private List<String> getColumn(int index) {
    List<String> column = new ArrayList<>();

    // If index is invalid, return empty List
    if (index < 0) {
      return column;
    }

    return columns.get(index);
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_SOURCE_FILE)) {
      setSourceFileProperty(newValue);
    }
  }

  /**
   * Adds a new Data File Change listener to the Mock Data File component.
   *
   * @param listener  Listener to add
   */
  public void addDataFileChangeListener(DataFileChangeListener listener) {
    dataFileChangeListeners.add(listener);
  }

  /**
   * Removes a Data File Change Listener from the Mock Data File component.
   *
   * @param listener  Listener to remove
   */
  public void removeDataFileChangeListener(DataFileChangeListener listener) {
    dataFileChangeListeners.remove(listener);
  }

  /**
   * Updates all the attached DataFileChangeListeners.
   */
  private void updateDataFileChangeListeners() {
    /* To prevent errors, and extra check here is needed.
       It only makes sense updating the listeners only when
       the data file is attached.
    */
    if (this.isAttached()) {
      for (DataFileChangeListener listener : dataFileChangeListeners) {
        // Call onColumnsChange event
        listener.onColumnsChange(this);
      }
    }
  }
}
