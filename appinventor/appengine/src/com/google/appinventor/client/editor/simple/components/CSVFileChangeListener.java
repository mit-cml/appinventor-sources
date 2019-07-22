package com.google.appinventor.client.editor.simple.components;

/**
 * Listener interface for receiving CSVFile column change events.
 *
 * Classes referencing a CSVFile source should implement this
 * interface to receive events when the Source file of the CSVFile
 * is changed, and the columns are changed as a result.
 *
 * The columns/rows of the MockCSVFile change asynchronously,
 * therefore this listener is intended to be used to notify
 * all the attached listeners after asynchronous reading
 * finishes.
 */
public interface CSVFileChangeListener {
  /**
   * Invoked when the columns of a specified CSVFile change.
   * @param csvFile  MockCSVFile component of which the columns changed
   */
  public void onColumnsChange(MockCSVFile csvFile);
}
