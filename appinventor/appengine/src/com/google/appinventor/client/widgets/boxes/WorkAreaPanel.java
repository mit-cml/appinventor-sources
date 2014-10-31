// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.boxes;

import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.appinventor.client.widgets.boxes.Box.BoxDescriptor;
import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.allen_sauer.gwt.dnd.client.PickupDragController;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Desktop-like container for boxes.
 *
 * <p>Implements a panel which arranges boxes in columns. Boxes can be dragged
 * from one column to another or to a different position within the same
 * column.
 *
 * @see Box
 *
 */
public final class WorkAreaPanel extends SimplePanel implements LayoutChangeListener {

  // Field names for JSON object encoding of work area state
  private static final String NAME_ACTIVE_LAYOUT = "ActiveLayout";
  private static final String NAME_LAYOUTS = "Layouts";

  // Box registry for creating boxes
  private final BoxRegistry boxRegistry;

  // Active layout for work area
  private Layout layout;

  // List of available layouts
  private final Map<String, Layout> layouts;

  // Base panel of work area
  private final AbsolutePanel boundaryPanel;

  // Drag and drop controllers
  private final PickupDragController widgetDragController;

  // Last resize width and height
  private int width;
  private int height;

  /**
   * Creates a new work area.
   *
   * @param boxRegistry  box registry for creating boxes
   * @param layout  initial layout for work area
   */
  public WorkAreaPanel(BoxRegistry boxRegistry, Layout layout) {
    this.boxRegistry = boxRegistry;
    this.layout = layout;

    layouts = new HashMap<String, Layout>();
    layouts.put(layout.getName(), layout);

    boundaryPanel = new AbsolutePanel();
    boundaryPanel.setSize("100%", "100%");
    setWidget(boundaryPanel);
    setSize("100%", "100%");

    widgetDragController = new PickupDragController(boundaryPanel, false);
    widgetDragController.setBehaviorMultipleSelection(false);

    switchLayout(layout.getName());
  }

  @Override
  public void clear() {
    boundaryPanel.clear();
  }

  @Override
  public void add(Widget widget) {
    boundaryPanel.add(widget);
  }

  /**
   * Must be invoked upon resizing of the work area panel.
   *
   * @param width  new work width in pixel
   * @param height  new work height in pixel
   */
  public void onResize(int width, int height) {
    this.width = width;
    this.height = height;

    layout.onResize(width, height);
  }

  /**
   * Create a new box from the definition by the given box descriptor.
   *
   * @param bd  box descriptor describing box to be created
   * @return  new box
   */
  public Box createBox(BoxDescriptor bd) {
    String boxType = bd.getType();
    Box box = boxRegistry.getBox(boxType);
    if (box == null) {
      OdeLog.wlog("Unknown box type: " + boxType);
    } else {
      widgetDragController.makeDraggable(box, box.getHeader());
    }
    return box;
  }

  /**
   * Returns work area's drag controller.
   *
   * @return drag controller
   */
  public PickupDragController getWidgetDragController() {
    return widgetDragController;
  }

  /**
   * Returns a map of the layouts for the work area.
   *
   * @return  map of layouts for work area
   */
  public Map<String, Layout> getLayouts() {
    return layouts;
  }

  /**
   * Return state of work area and its layouts converted into JSON format.
   *
   * @return  work area state in JSON format
   */
  public String toJson() {
    StringBuilder sb =  new StringBuilder();
    sb.append("{\"" + NAME_ACTIVE_LAYOUT + "\":\"");
    sb.append(layout.getName());
    sb.append("\",\"" + NAME_LAYOUTS + "\":[");
    String separator = "";
    for (Layout layout : layouts.values()) {
      sb.append(separator);
      sb.append(layout.toJson());
      separator = ",";
    }
    sb.append("]}");
    return sb.toString();
  }

  /**
   * Restores state of work area and its layouts from JSON.
   *
   * @param encodedState  work area state in JSON format
   */
  public void fromJson(String encodedState) {
    if (!encodedState.isEmpty()) {
      JSONObject state = new ClientJsonParser().parse(encodedState).asObject();
      Map<String, JSONValue> properties = state.getProperties();

      JSONArray layoutArray = properties.get(NAME_LAYOUTS).asArray();

      // check if boxes encoded in JSON is the same set as in the registry
      HashSet<String> boxTypes = new HashSet<String>();
      for (JSONValue layoutObject : layoutArray.getElements()) {
        ColumnLayout.boxTypesFromJson(layoutObject.asObject(), boxTypes);
      }

      // only restore state if the saved boxes match expected boxes
      if (boxTypes.equals(boxRegistry.getBoxTypes())) {
         for (JSONValue layoutObject : layoutArray.getElements()) {
           // TODO(user): Should we decide to support multiple layouts we
           //                 should select the layout type from a registry
           Layout layout = ColumnLayout.fromJson(layoutObject.asObject(), this);
           layouts.put(layout.getName(), layout);
         }

         switchLayout(properties.get(NAME_ACTIVE_LAYOUT).asString().getString());
      }
    }
  }

  private void switchLayout(String layoutName) {
    if (layout != null) {
      layout.removeLayoutChangeListener(this);
    }

    layout = layouts.get(layoutName);
    layout.apply(this);
    layout.addLayoutChangeListener(this);

    // To ensure all boxes are appropriately sized
    onResize(width, height);
  }

  // LayoutChangeListener

  @Override
  public void onLayoutChange(Layout layout) {
  }
}
