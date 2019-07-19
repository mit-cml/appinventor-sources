package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;

import java.util.Arrays;
import java.util.List;

public class MockCSVFile extends MockNonVisibleComponent {
  public static final String TYPE = "CSVFile";

  private static final int MAX_ROWS = 10; // The maximum rows to save in the CSV File
  private static final String PROPERTY_NAME_SOURCE_FILE = "SourceFile";

  private List<String> columnNames;
  private List<List<String>> rows;

  /**
   * Creates a new instance of a CSVFile component whose icon is
   * loaded dynamically (not part of the icon image bundle)
   */
  public MockCSVFile(SimpleEditor editor, String type, Image iconImage) {
    super(editor, type, iconImage);
  }

  private void setSourceFileProperty(String fileSource) {
    // Check that the SourceFile property is a valid file
    if (fileSource == null || fileSource.equals("")) {
      return;
    }

    // Read the media file
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();

    Ode.getInstance().getProjectService().load(projectId, "assets/" + fileSource, new AsyncCallback<String>() {
      @Override
      public void onFailure(Throwable caught) {
        ErrorReporter.reportError(caught.getMessage());
      }

      @Override
      public void onSuccess(String result) {
        // TODO: Switch to CSVParser
        columnNames = Arrays.asList(result.split("\n")[0].split(","));
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
}
