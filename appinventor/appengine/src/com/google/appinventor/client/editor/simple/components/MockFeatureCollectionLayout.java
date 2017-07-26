// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.components.common.ComponentConstants;

import java.util.Map;

public class MockFeatureCollectionLayout extends MockLayout {

  MockFeatureCollectionLayout() {
    layoutWidth = 0;
    layoutHeight = 0;
  }

  @Override
  LayoutInfo createContainerLayoutInfo(Map<MockComponent, LayoutInfo> layoutInfoMap) {
    return new LayoutInfo(layoutInfoMap, container) {
      @Override
      int calculateAutomaticWidth() {
        return ComponentConstants.MAP_PREFERRED_WIDTH;
      }

      @Override
      int calculateAutomaticHeight() {
        return ComponentConstants.MAP_PREFERRED_HEIGHT;
      }
    };
  }

  @Override
  void layoutChildren(LayoutInfo containerLayoutInfo) {
    layoutWidth = ComponentConstants.MAP_PREFERRED_WIDTH;
    layoutHeight = ComponentConstants.MAP_PREFERRED_HEIGHT;
    ((MockFeatureCollection) container).getMap().invalidateMap();
  }

  @Override
  boolean onDrop(MockComponent source, int x, int y, int offsetX, int offsetY) {
    return MockMapLayout.onDrop(((MockFeatureCollection) container).getMap(), source, x, y, offsetX, offsetY);
  }

}
