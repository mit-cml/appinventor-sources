package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.editor.simple.components.CSVFileChangeListener;
import com.google.appinventor.client.editor.simple.components.MockCSVFile;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.widgets.properties.AdditionalChoicePropertyEditor;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.List;

import static com.google.appinventor.client.Ode.MESSAGES;

public class YoungAndroidCsvFileSelectorPropertyEditor
    extends AdditionalChoicePropertyEditor implements CSVFileChangeListener {
  // UI elements
  private final ListBox columnsList;

  protected ListWithNone choices;

  // The YaFormEditor associated with this property editor.
  private final YaFormEditor editor;

  private MockCSVFile csvFile;

  /**
   * Creates a new property editor for selecting a component.
   *
   * @param editor the editor that this property editor belongs to
   */
  public YoungAndroidCsvFileSelectorPropertyEditor(final YaFormEditor editor) {
    this.editor = editor;

    VerticalPanel selectorPanel = new VerticalPanel();
    columnsList = new ListBox();
    columnsList.setVisibleItemCount(10);
    columnsList.setWidth("100%");
    selectorPanel.add(columnsList);
    selectorPanel.setWidth("100%");

    initializeChoices();

    // At this point, the editor hasn't finished loading.
    // Use a DeferredCommand to finish the initialization after the editor has finished loading.
    DeferredCommand.addCommand(new Command() {
      @Override
      public void execute() {
        if (editor.isLoadComplete()) {
          finishInitialization();
        } else {
          // Editor still hasn't finished loading.
          DeferredCommand.addCommand(this);
        }
      }
    });

    initAdditionalChoicePanel(selectorPanel);
  }

  private void finishInitialization() {
    // Previous version had a bug where the value could be accidentally saved as "None".
    // If the property value is "None" and choices doesn't contain the value "None", set the
    // property value to "".
    String value = property.getValue();
    if (value.equals("None") && !choices.containsValue(value)) {
      property.setValue("");
    }
  }

  @Override
  protected void openAdditionalChoiceDialog() {
    choices.selectValue(property.getValue());
    super.openAdditionalChoiceDialog();
    columnsList.setFocus(true);
  }

  @Override
  protected String getPropertyValueSummary() {
    String value = property.getValue();
    if (choices.containsValue(value)) {
      return choices.getDisplayItemForValue(value);
    }
    return value;
  }

  @Override
  protected boolean okAction() {
    int selected = columnsList.getSelectedIndex();
    if (selected == -1) {
      Window.alert(MESSAGES.noComponentSelected());
      return false;
    }
    property.setValue(choices.getValueAtIndex(selected));
    return true;
  }

  private void initializeChoices() {
    columnsList.clear();

    choices = new ListWithNone(MESSAGES.noneCaption(), new ListWithNone.ListBoxWrapper() {
      @Override
      public void addItem(String item) {
        columnsList.addItem(item);
      }

      @Override
      public String getItem(int index) {
        return columnsList.getItemText(index);
      }

      @Override
      public void removeItem(int index) {
        columnsList.removeItem(index);
      }

      @Override
      public void setSelectedIndex(int index) {
        columnsList.setSelectedIndex(index);
      }
    });
  }

  public void changeSource(MockCSVFile source) {
    if (csvFile != null) {
      csvFile.removeColumnChangeListener(this);
    }

    if (source == null) {
      return;
    }

    csvFile = source;
    csvFile.addColumnChageListener(this);

    updateColumns();
  }

  public void updateColumns() {
    List<String> columns = csvFile.getColumnNames();
    initializeChoices();

    if (columns == null || columns.isEmpty()) {
      return;
    }

    boolean found = false;

    for (String column : columns) {
      if (!found) {
        found = property.getValue().equals(column);
      }

      choices.addItem(column);
    }

    if (!found) {
      property.setValue("");
    }
  }

  @Override
  public void onColumnsChange(MockCSVFile csvFile) {
    if (!this.csvFile.equals(csvFile)) {
      return;
    }

    updateColumns();
  }
}
