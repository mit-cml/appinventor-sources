package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidCsvFileColumnSelectorPropertyEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import java.util.*;

import static com.google.appinventor.client.Ode.MESSAGES;

public class MockCSVFile extends MockNonVisibleComponent {
  public static final String TYPE = "CSVFile";

  private static final int MAX_ROWS = 10; // The maximum rows to save in the CSV File
  private static final String PROPERTY_NAME_SOURCE_FILE = "SourceFile";

  private List<String> columnNames;
  private List<List<String>> rows;
  private Set<CSVFileColumnChangeListener> columnChangeListeners;

  private String sourceFile;

  /**
   * Creates a new instance of a CSVFile component whose icon is
   * loaded dynamically (not part of the icon image bundle)
   */
  public MockCSVFile(SimpleEditor editor, String type, Image iconImage) {
    super(editor, type, iconImage);

    columnChangeListeners = new HashSet<CSVFileColumnChangeListener>();
  }

  private void setSourceFileProperty(String fileSource) {
    this.sourceFile = fileSource;

    columnNames = new ArrayList<String>();

    // Check that the SourceFile property is a valid file
    if (fileSource == null || fileSource.equals("")) {
      updateColumnChangeListeners();
      return;
    }

    // Read the media file
    long projectId = editor.getProjectId();

    ErrorReporter.reportInfo(MESSAGES.csvFileReadingMessage(sourceFile, this.getName()));

    Ode.getInstance().getProjectService().loadDataFile(projectId, "assets/" + fileSource,
        new AsyncCallback<List<List<String>>>() {
      @Override
      public void onFailure(Throwable caught) {
        ErrorReporter.reportError(caught.getMessage());
      }

      @Override
      public void onSuccess(List<List<String>> result) {
        rows = result;
        columnNames = result.get(0);
        updateColumnChangeListeners();
        ErrorReporter.hide();
      }
    });
  }

  public List<String> getColumnNames() {
    return columnNames;
  }

  public List<List<String>> getRows() {
    return rows;
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_SOURCE_FILE)) {
      setSourceFileProperty(newValue);
    }
  }

  public void addColumnChageListener(CSVFileColumnChangeListener listener) {
    columnChangeListeners.add(listener);
  }

  public void removeColumnChangeListener(CSVFileColumnChangeListener listener) {
    columnChangeListeners.remove(listener);
  }

  private void updateColumnChangeListeners() {
    if (columnChangeListeners == null) {
      return;
    }

    for (CSVFileColumnChangeListener listener : columnChangeListeners) {
      listener.onColumnChange();
    }
  }
}
