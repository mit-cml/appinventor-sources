// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.boxes;

import com.google.appinventor.client.widgets.boxes.Box.BoxDescriptor;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONUtil;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.DragStartEvent;
import com.allen_sauer.gwt.dnd.client.drop.IndexedDropController;

import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.Map;

/**
 * Defines a column-based layout for the boxes on a work area panel.
 *
 */
public final class ColumnLayout extends Layout {

  /**
   * Drag handler for detecting changes to the layout.
   */
  public class ChangeDetector implements DragHandler {

    @Override
    public void onDragEnd(DragEndEvent event) {
      fireLayoutChange();
    }

    @Override
    public void onDragStart(DragStartEvent event) {
    }

    @Override
    public void onPreviewDragEnd(DragEndEvent event) {
    }

    @Override
    public void onPreviewDragStart(DragStartEvent event) {
    }
  }

  /**
   * Represents a column in the layout.
   */
  public static final class Column extends IndexedDropController {

    // Field names for JSON object encoding of layout
    private static final String NAME_WIDTH = "width";
    private static final String NAME_BOXES = "boxes";

    // Associated column container (this is the state of the layout when it is active)
    private final VerticalPanel columnPanel;

    // Relative column width (in percent of work area width)
    private int relativeWidth;

    // Absolute width (in pixel)
    private int absoluteWidth;

    // List of box description (this is the state of the layout when it is inactive)
    private final List<BoxDescriptor> boxes;

    /**
     * Creates new column.
     */
    private Column(int relativeWidth) {
      this(new VerticalPanel(), relativeWidth);
    }

    /**
     * Creates new column.
     */
    private Column(VerticalPanel columnPanel, int relativeWidth) {
      super(columnPanel);

      boxes = new ArrayList<BoxDescriptor>();

      this.columnPanel = columnPanel;
      this.relativeWidth = relativeWidth;

      columnPanel.setWidth(relativeWidth + "%");
      columnPanel.setSpacing(SPACING);
    }

    @Override
    protected void insert(Widget widget, int beforeIndex) {
      // columnPanel always contains at least one widget: the 'invisible' end marker. Therefore
      // beforeIndex cannot become negative.
      if (beforeIndex == columnPanel.getWidgetCount()) {
        beforeIndex--;
      }

      super.insert(widget, beforeIndex);

      if (widget instanceof Box) {
        ((Box) widget).onResize(absoluteWidth);
      }
    }

    /**
     * Invoked upon resizing of the work area panel.
     *
     * @see WorkAreaPanel#onResize(int, int)
     *
     * @param width column width in pixel
     */
    public void onResize(int width) {
      absoluteWidth = width * relativeWidth / 100;
      columnPanel.setWidth(absoluteWidth + "px");
      for (Widget w : columnPanel) {
        if (w instanceof Box) {
          ((Box) w).onResize(absoluteWidth);
        } else {
          // Top-of-column marker (otherwise invisible widget)
          w.setWidth(absoluteWidth + "px");
        }
      }
    }

    /**
     * Add a new box to the column.
     *
     * @param type  type of box
     * @param height  height of box in pixels if not minimized
     * @param minimized  indicates whether box is minimized
     */
    public void add(Class<? extends Box> type, int height, boolean minimized) {
      boxes.add(new BoxDescriptor(type, absoluteWidth, height, minimized));
    }

    /**
     * Updates the box descriptors for the boxes in the column.
     */
    private void updateBoxDescriptors() {
      boxes.clear();
      for (Widget w : columnPanel) {
        if (w instanceof Box) {
          boxes.add(((Box) w).getLayoutSettings());
        }
      }
    }

    /**
     * Returns JSON encoding for the boxes in a column.
     */
    private String toJson() {
      List<String> jsonBoxes = new ArrayList<String>();
      for (int i = 0; i < columnPanel.getWidgetCount(); i++) {
        Widget w = columnPanel.getWidget(i);
        if (w instanceof Box) {
          jsonBoxes.add(((Box) w).getLayoutSettings().toJson());
        }
      }

      return "{" +
            "\"" + NAME_WIDTH + "\":" + JSONUtil.toJson(relativeWidth) + "," +
            "\"" + NAME_BOXES + "\":[" + StringUtils.join(",", jsonBoxes) + "]" +
          "}";
    }

    /**
     * Creates a new column from a JSON encoded layout.
     *
     * @param columnIndex  index of column
     * @param object  column in JSON format
     */
    private static Column fromJson(int columnIndex, JSONObject object) {
      Column column = new Column(columnIndex);

      Map<String, JSONValue> properties = object.getProperties();
      column.relativeWidth = JSONUtil.intFromJsonValue(properties.get(NAME_WIDTH));

      for (JSONValue boxObject : properties.get(NAME_BOXES).asArray().getElements()) {
        column.boxes.add(BoxDescriptor.fromJson(boxObject.asObject()));
      }

      return column;
    }

    /**
     * Collects box types encoded in the JSON.
     *
     * @param object    column in JSON format
     * @param boxTypes  set of box types encountered so far
     */
    private static void boxTypesFromJson(JSONObject object,
                                         Set<String> boxTypes) {
      Map<String, JSONValue> properties = object.getProperties();

      for (JSONValue boxObject : properties.get(NAME_BOXES).asArray().getElements()) {
        boxTypes.add(BoxDescriptor.boxTypeFromJson(boxObject.asObject()));
      }
    }

  }

  // Spacing between columns in pixels
  private static final int SPACING = 5;

  // Field names for JSON object encoding of layout
  private static final String NAME_NAME = "name";
  private static final String NAME_COLUMNS = "columns";

  // List of columns
  private final List<Column> columns;

  // Drag handler for detecting changes to the layout
  private final DragHandler changeDetector;

  /**
   * Creates a new layout.
   */
  public ColumnLayout(String name) {
    super(name);

    columns = new ArrayList<Column>();
    changeDetector = new ChangeDetector();
  }

  /**
   * Clears the layout (removes all existing columns etc).
   */
  private void clear(WorkAreaPanel workArea) {
    for (Column column : columns) {
      workArea.getWidgetDragController().unregisterDropController(column);
    }
    workArea.getWidgetDragController().removeDragHandler(changeDetector);
    columns.clear();
  }

  /**
   * Adds a new column to the layout.
   *
   * @param relativeWidth  relative width of column (width of all columns
   *                        should add up to 100)
   * @return  new layout column
   */
  public Column addColumn(int relativeWidth) {
    Column column = new Column(relativeWidth);
    columns.add(column);
    return column;
  }

  @Override
  public void apply(WorkAreaPanel workArea) {

    // Clear base panel
    workArea.clear();

    // Horizontal panel to hold columns
    HorizontalPanel horizontalPanel = new HorizontalPanel();
    horizontalPanel.setSize("100%", "100%");
    workArea.add(horizontalPanel);

    // Initialize columns
    for (Column column : columns) {
      horizontalPanel.add(column.columnPanel);
      workArea.getWidgetDragController().registerDropController(column);

      // Add invisible dummy widget to prevent column from collapsing when it contains no boxes
      column.columnPanel.add(new Label());

      // Add boxes from layout
      List<BoxDescriptor> boxes = column.boxes;
      for (int index = 0; index < boxes.size(); index++) {
        BoxDescriptor bd = boxes.get(index);
        Box box = workArea.createBox(bd);
        if (box != null) {
          column.insert(box, index);
          box.restoreLayoutSettings(bd);
        }
      }
    }

    workArea.getWidgetDragController().addDragHandler(changeDetector);
  }

  @Override
  public void onResize(int width, int height) {
    // Calculate the usable width for the columns (which is the width of the browser client area
    // minus the spacing on each side of the boxes).
    int usableWidth = (width - ((columns.size() + 1) * SPACING));

    // On startup it can happen that we receive a window resize event before the boxes are attached
    // to the DOM. In that case, width and height are 0, we can safely ignore because there will
    // soon be another resize event after the boxes are attached to the DOM.
    if (width > 0) {
      for (Column column : columns) {
        column.onResize(usableWidth);
      }
    }
  }

  @Override
  public String toJson() {
    List<String> jsonColumns = new ArrayList<String>(columns.size());
    for (Column column : columns) {
      jsonColumns.add(column.toJson());
    }

    return "{" +
          "\"" + NAME_NAME + "\":" + JSONUtil.toJson(getName()) + "," +
          "\"" + NAME_COLUMNS + "\":[" + StringUtils.join(",", jsonColumns) + "]" +
        "}";
  }

  /**
   * Creates a new layout from a JSON encoded layout.
   *
   * @param object  layout in JSON format
   */
  public static Layout fromJson(JSONObject object, WorkAreaPanel workArea) {

    Map<String, JSONValue> properties = object.getProperties();

    String name = properties.get(NAME_NAME).asString().getString();
    ColumnLayout layout = (ColumnLayout) workArea.getLayouts().get(name);
    if (layout == null) {
      layout = new ColumnLayout(name);
    }

    layout.clear(workArea);
    for (JSONValue columnObject : properties.get(NAME_COLUMNS).asArray().getElements()) {
      layout.columns.add(Column.fromJson(layout.columns.size(), columnObject.asObject()));
    }

    return layout;
  }

  /**
   * Collects box types encoded in the JSON.
   *
   * @param object    layout in JSON format
   * @param boxTypes  box types encountered so far
   */
  public static void boxTypesFromJson(JSONObject object, Set<String> boxTypes) {

    Map<String, JSONValue> properties = object.getProperties();

    for (JSONValue columnObject : properties.get(NAME_COLUMNS).asArray().getElements()) {
      Column.boxTypesFromJson(columnObject.asObject(), boxTypes);
    }

  }

  @Override
  protected void fireLayoutChange() {
    // Need to update box descriptors before firing change event.
    // It is easier (maintenance-wise) to do this here instead of doing this in multiple places.
    for (Column column : columns) {
      column.updateBoxDescriptors();
    }

    super.fireLayoutChange();
  }
}
