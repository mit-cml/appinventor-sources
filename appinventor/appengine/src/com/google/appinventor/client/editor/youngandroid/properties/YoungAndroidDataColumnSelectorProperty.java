// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.appinventor.client.editor.simple.components.DataFileChangeListener;
import com.google.appinventor.client.editor.simple.components.MockDataFile;

import com.google.appinventor.client.editor.youngandroid.YaFormEditor;

import com.google.appinventor.client.widgets.properties.AdditionalChoicePropertyEditor;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.List;
import java.util.Objects;

/**
 * Property setter for selecting columns in Chart Data components
 * for the attached DataFile sources.
 *
 * <p>TODO: Reduce redundancy (a lot of reuse from ComponentSelector)
 */
public class YoungAndroidDataColumnSelectorProperty
    extends AdditionalChoicePropertyEditor implements DataFileChangeListener {

  protected ListWithNone choices;

  // UI elements
  private final ListBox columnsList;

  private MockDataFile dataFile; // Associated source MockDataFile

  /**
   * Creates a new property editor for selecting a column.
   *
   * @param editor the editor that this property editor belongs to
   */
  public YoungAndroidDataColumnSelectorProperty(final DesignerEditor<?, ?, ?, ?, ?> editor) {

    final VerticalPanel selectorPanel = new VerticalPanel();
    columnsList = new ListBox();
    columnsList.setVisibleItemCount(10);
    columnsList.setWidth("100%");
    selectorPanel.add(columnsList);
    selectorPanel.setWidth("100%");

    // Initializes the choices list
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

    // Check if the current value is exactly the None value
    if (Objects.equals(value, choices.getValueAtIndex(0))) {
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
      Window.alert(MESSAGES.noColumnSelected());
      return false;
    }
    property.setValue(choices.getValueAtIndex(selected));
    return true;
  }

  /**
   * Clears the current columns and reinitializes the choices list.
   *
   * <p>Re-initialization is needed to clear old entries of the list.
   * Used when updating the DataFile source.
   */
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

  /**
   * Changes the MockDataFile source of the Data column selector.
   *
   * @param source new MockDataFile source
   */
  public void changeSource(MockDataFile source) {
    // Source is equal to DataFile; No need
    // to change anything.
    if (source == dataFile) {
      return;
    }

    // Check if a DataFile source is currently referenced by the
    // selector, and if it is, de-attach this selector from it.
    if (dataFile != null) {
      dataFile.removeDataFileChangeListener(this);
    }

    // Update the dataFile source
    dataFile = source;

    if (dataFile != null) {
      // Register to listen for the DataFile column change events
      dataFile.addDataFileChangeListener(this);

      // Update the columns of the selector
      updateColumns();
    }
  }

  /**
   * Updates the columns from the local DataFile source.
   */
  public void updateColumns() {
    List<String> columns = dataFile.getColumnNames();

    initializeChoices(); // Re-initialize choices list

    // If columns are null or empty, no action is done.
    // This allows to change the Source property (in MockChartDatA)
    // and still retain the same columns (if they match)
    if (columns == null || columns.isEmpty()) {
      return;
    }

    // Keep track whether the current property value matches any of
    // the newly added column entries.
    boolean found = false;

    for (String column : columns) {
      // Matching value not yet found
      if (!found) {
        // Check if current column matches property
        found = property.getValue().equals(column);
      }

      // Add the column to the choices list
      choices.addItem(column);
    }

    // Current property value not found in the new columns, set to None
    if (!found) {
      property.setValue("");
    }
  }

  @Override
  public void onColumnsChange(MockDataFile dataFile) {
    // Edge case: Event originates from the wrong MockDataFile source
    if (this.dataFile != dataFile) {
      return;
    }

    // Update the column entries
    updateColumns();
  }
}
