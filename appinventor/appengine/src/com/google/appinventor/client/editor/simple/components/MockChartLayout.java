// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.components.common.ComponentConstants;

import java.util.Map;

public class MockChartLayout extends MockLayout {

  MockChartLayout() {
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

  }

  @Override
  boolean onDrop(MockComponent source, int x, int y, int offsetX, int offsetY) {
    if (source instanceof MockChartData) {
      container.addComponent(source);
      ((MockChartData) source).addToChart((MockChart) container);
      return true;
    } else if (source instanceof MockDataFile) {
      ((MockChart) container).addDataFile((MockDataFile) source);
      return true;
    }

    return false;
  }
}
