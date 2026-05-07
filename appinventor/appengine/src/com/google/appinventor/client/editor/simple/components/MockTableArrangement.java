// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.widgets.dnd.DragSourceSupport;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.user.client.ui.AbsolutePanel;

/**
 * Mock TableArrangement component.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class MockTableArrangement extends MockContainer {

  /**
   * Component type name.
   */
  public static final String TYPE = "TableArrangement";

  // Property names
  private static final String PROPERTY_NAME_COLUMNS = "Columns";
  private static final String PROPERTY_NAME_ROWS = "Rows";

  // Form UI components
  protected final AbsolutePanel layoutWidget;

  private int rows;
  private int columns;

  /**
   * Creates a new MockTableArrangement component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockTableArrangement(SimpleEditor editor) {
    super(editor, TYPE, images.table(), new MockTableLayout());

    rootPanel.setHeight("100%");

    layoutWidget = new AbsolutePanel();
    layoutWidget.setStylePrimaryName("ode-SimpleMockContainer");
    layoutWidget.add(rootPanel);

    initComponent(layoutWidget);
  }

  @Override
  public boolean canPasteComponentOfType(String type) {
    if (!super.canPasteComponentOfType(type)) {
      return false;
    }

    // See if we can find a free cell to obtain the pasted component
    MockComponent[][] cells = computeTable();
    for (int row = 0; row < rows; row++) {
      for (int column = 0; column < columns; column++) {
        if (cells[row][column] == null) {
          return true;
        }
      }
    }

    // No free spaces available so abort.
    return false;
  }

  @Override
  public void onPaste(MockComponent child) {
    super.onPaste(child);
    MockTableLayout.Cell cell = ((MockTableLayout) layout).getCellContainingPoint(
        DragSourceSupport.absX - child.getAbsoluteLeft(),
        DragSourceSupport.absY - child.getAbsoluteTop());
    MockComponent[][] cells = computeTable();

    // Do we have a valid, empty cell?
    if (cell != null && cells[cell.row][cell.col] == null) {
      child.changeProperty(MockVisibleComponent.PROPERTY_NAME_ROW, "" + cell.row);
      child.changeProperty(MockVisibleComponent.PROPERTY_NAME_COLUMN, "" + cell.col);
      return;
    }

    // Either cell is null (no valid cell) or cell is valid but not empty, so find an empty cell
    // and move the component there.
    for (int row = 0; row < rows; row++) {
      for (int column = 0; column < columns; column++) {
        if (cells[row][column] == null) {
          child.changeProperty(MockVisibleComponent.PROPERTY_NAME_ROW, "" + row);
          child.changeProperty(MockVisibleComponent.PROPERTY_NAME_COLUMN, "" + column);
          return;
        }
      }
    }
  }

  private MockComponent[][] computeTable() {
    MockComponent[][] cells = new MockComponent[rows][columns];
    for (MockComponent component : children) {
      int row = Integer.parseInt(component.getPropertyValue(MockVisibleComponent.PROPERTY_NAME_ROW));
      int column = Integer.parseInt(component.getPropertyValue(MockVisibleComponent.PROPERTY_NAME_COLUMN));
      if (0 <= row && row < rows && 0 <= column && column < columns) {
        cells[row][column] = component;
      }
    }
    return cells;
  }

  public void removeComponent(MockComponent component, boolean permanentlyDeleted) {
    component.changeProperty(MockVisibleComponent.PROPERTY_NAME_ROW,
        "" + ComponentConstants.DEFAULT_ROW_COLUMN);
    component.changeProperty(MockVisibleComponent.PROPERTY_NAME_COLUMN,
        "" + ComponentConstants.DEFAULT_ROW_COLUMN);
    super.removeComponent(component, permanentlyDeleted);
  }

  private void setColumnsProperty(String value) {
    try {
      columns = Integer.parseInt(value);
      ((MockTableLayout) layout).setColumns(columns);
    } catch (NumberFormatException e) {
      // Ignore this. If we throw an exception here, the project is unrecoverable.
    }
  }

  private void setRowsProperty(String value) {
    try {
      rows = Integer.parseInt(value);
      ((MockTableLayout) layout).setRows(rows);
    } catch (NumberFormatException e) {
      // Ignore this. If we throw an exception here, the project is unrecoverable.
    }
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_COLUMNS)) {
      setColumnsProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_ROWS)) {
      setRowsProperty(newValue);
    }
  }
}
