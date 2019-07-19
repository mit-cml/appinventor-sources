package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.editor.simple.components.FormChangeListener;
import com.google.appinventor.client.editor.simple.components.MockCSVFile;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.widgets.properties.AdditionalChoicePropertyEditor;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.List;
import java.util.Set;

import static com.google.appinventor.client.Ode.MESSAGES;

public class YoungAndroidCsvFileColumnSelectorPropertyEditor
    extends AdditionalChoicePropertyEditor {
  // UI elements
  private final ListBox columnsList;

  protected ListWithNone choices;

  // The YaFormEditor associated with this property editor.
  private final YaFormEditor editor;

  /**
   * Creates a new property editor for selecting a component.
   *
   * @param editor the editor that this property editor belongs to
   */
  public YoungAndroidCsvFileColumnSelectorPropertyEditor(final YaFormEditor editor) {
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
    // Fill choices with the components.
//    for (MockComponent component : editor.getComponents().values()) {
//      if (componentTypes == null || componentTypes.contains(component.getType())) {
//        choices.addItem(component.getName());
//      }
//    }

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

  public void changeSource(String source) {
    List<String> columns = ((MockCSVFile)editor.getComponents().get(source)).getColumnNames();

    initializeChoices();

    if (columns == null) {
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
}
