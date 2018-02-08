// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

/**
 * Marker interface for map items
 *
 */
public interface MockMapFeature {
  static final double DEFAULT_Z_LAYER = 1.0;
  static final String PROPERTY_NAME_Z = "Z";
  static final String PROPERTY_NAME_LATITUDE = "Latitude";
  static final String PROPERTY_NAME_LONGITUDE = "Longitude";
  void addToMap(MockMap map);

  /**
   * When the MockMapFeature is dropped on the map, the onDrop handler is called to possibly update the feature. If it
   * is valid to add the feature to this map, then the feature should return true.
   *
   * @param map The map the feature is being dropped in
   * @param x X coordinate of the mouse
   * @param y Y coordinate of the mouse
   * @param offsetX X offset of the mouse to the top-left
   * @param offsetY Y offset of the mouse to the top-left
   * @return true if the MockMapFeature should be added to the map
   */
  boolean onDrop(MockMap map, int x, int y, int offsetX, int offsetY);
}
