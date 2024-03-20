// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.components.common.ComponentConstants;

import java.util.Map;

/**
 * Class that allows controlling the layout of the MockChart container.
 */
public class MockChartLayout extends MockLayout {

  MockChartLayout() {
    layoutWidth = ComponentConstants.CHART_PREFERRED_WIDTH;
    layoutHeight = ComponentConstants.CHART_PREFERRED_HEIGHT;
  }

  @Override
  LayoutInfo createContainerLayoutInfo(Map<MockComponent, LayoutInfo> layoutInfoMap) {
    return new LayoutInfo(layoutInfoMap, container) {
      @Override
      int calculateAutomaticWidth() {
        return ComponentConstants.CHART_PREFERRED_WIDTH;
      }

      @Override
      int calculateAutomaticHeight() {
        return ComponentConstants.CHART_PREFERRED_HEIGHT;
      }
    };
  }

  @Override
  void layoutChildren(LayoutInfo containerLayoutInfo) {

  }

  @Override
  boolean onDrop(MockComponent source, int x, int y, int offsetX, int offsetY) {
    if (source instanceof MockChartData) {
      // When dropping MockChartData onto a MockChart, the
      // ChartData should be attached to this Mock Chart container.
      container.addComponent(source);
      ((MockChartData) source).addToChart((MockChart) container);
      return true;
    } else if (source instanceof MockDataFile) {
      // When dropping a MockDataFile onto a MockChart, the
      // DataFile should be attached to this Mock Chart container.
      ((MockChart) container).addDataFile((MockDataFile) source);
      return true;
    } else if (source instanceof MockTrendline) {
      container.addComponent(source);
      return true;
    }

    return false;
  }
}
