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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockCSVFile extends MockNonVisibleComponent {
  public static final String TYPE = "CSVFile";

  private static final int MAX_ROWS = 10; // The maximum rows to save in the CSV File
  private static final String PROPERTY_NAME_SOURCE_FILE = "SourceFile";

  private List<String> columnNames;
  private List<List<String>> rows;
  private List<YoungAndroidCsvFileColumnSelectorPropertyEditor> columnSelectors;

  private String sourceFile;

  /**
   * Creates a new instance of a CSVFile component whose icon is
   * loaded dynamically (not part of the icon image bundle)
   */
  public MockCSVFile(SimpleEditor editor, String type, Image iconImage) {
    super(editor, type, iconImage);

    columnSelectors = new ArrayList<YoungAndroidCsvFileColumnSelectorPropertyEditor>();
  }

  private void setSourceFileProperty(String fileSource) {
    this.sourceFile = fileSource;

    columnNames = new ArrayList<String>();

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
        columnNames = result.get(0);

        for (YoungAndroidCsvFileColumnSelectorPropertyEditor selector : columnSelectors) {
          selector.updateColumns();
        }
      }
    });
  }

  public List<String> getColumnNames() {
    return columnNames;
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_SOURCE_FILE)) {
      setSourceFileProperty(newValue);
    }
  }

  public void addColumnSelector(YoungAndroidCsvFileColumnSelectorPropertyEditor selector) {
    if (!columnSelectors.contains(selector)) {
      columnSelectors.add(selector);
    }
  }

  public void removeColumnSelector(YoungAndroidCsvFileColumnSelectorPropertyEditor selector) {
    columnSelectors.remove(selector);
  }
}
