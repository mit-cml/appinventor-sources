// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import java.util.Map;

import com.google.appinventor.client.editor.simple.components.MockMap.LatLng;
import com.google.appinventor.components.common.ComponentConstants;

final class MockMapLayout extends MockLayout {
  private static final String PROPERTY_NAME_LATITUDE = "Latitude";
  private static final String PROPERTY_NAME_LONGITUDE = "Longitude";

  MockMapLayout() {
    layoutWidth = ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH;
    layoutHeight = ComponentConstants.VIDEOPLAYER_PREFERRED_HEIGHT;
  }

  @Override
  LayoutInfo createContainerLayoutInfo(Map<MockComponent, LayoutInfo> layoutInfoMap) {
    return new LayoutInfo(layoutInfoMap, container) {
      @Override
      int calculateAutomaticWidth() {
        return ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH;
      }

      @Override
      int calculateAutomaticHeight() {
        return ComponentConstants.VIDEOPLAYER_PREFERRED_HEIGHT;
      }
    };
  }

  @Override
  void layoutChildren(LayoutInfo containerLayoutInfo) {
    layoutWidth = ComponentConstants.MAP_PREFERRED_WIDTH;
    layoutHeight = ComponentConstants.MAP_PREFERRED_HEIGHT;
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      if (child instanceof MockMapFeature) {
        MockMapFeature feature = (MockMapFeature) child;
        feature.addToMap((MockMap) container);
      }
    }
    ((MockMap) container).invalidateMap();
  }

  @Override
  boolean onDrop(MockComponent source, int x, int y, int offsetX, int offsetY) {
    return onDrop((MockMap) container, source, x, y, offsetX, offsetY);
  }

  static boolean onDrop(MockMap map, MockComponent source, int x, int y, int offsetX, int offsetY) {
    if (source instanceof MockMapFeature) {
      MockMapFeature feature = (MockMapFeature) source;
      if (feature.onDrop(map, x, y, offsetX, offsetY)) {
        map.addComponent(source);
        feature.addToMap(map);
      }
      return true;
    }
    return false;
  }
}
