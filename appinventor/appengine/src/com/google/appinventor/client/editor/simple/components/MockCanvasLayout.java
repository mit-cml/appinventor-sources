// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Image;

import java.util.Map;

/**
 * A layout that positions and sizes each child to specific pixel
 * positions/lengths that are provided by the user.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
final class MockCanvasLayout extends MockLayout {
  private static final String PROPERTY_NAME_X = "X";
  private static final String PROPERTY_NAME_Y = "Y";
  private final Image image;
  private String imageUrl;

  MockCanvasLayout() {
    layoutWidth = ComponentConstants.CANVAS_PREFERRED_WIDTH;
    layoutHeight = ComponentConstants.CANVAS_PREFERRED_HEIGHT;
    image = new Image();
    image.addErrorHandler(new ErrorHandler() {
      @Override
      public void onError(ErrorEvent event) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
          OdeLog.elog("Error occurred while loading image " + imageUrl);
        }
        container.refreshForm();
      }
    });
    image.addLoadHandler(new LoadHandler() {
      @Override
      public void onLoad(LoadEvent event) {
        container.refreshForm();
      }
    });
  }

  void setBackgroundImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
    image.setUrl(imageUrl);
  }

  // MockLayout methods

  // NOTE(lizlooney) - layout behavior:

  // Only Balls and ImageSprites can be placed in a Canvas.
  // A Ball's width and height is always the diameter of the ball.
  // The actual width/height of an ImageSprite whose Width/Height property is set to Automatic or
  // Fill Parent will be the width/height of the image.

  // TODO(lizlooney) - We should not allow users to choose Fill Parent for the Width/Height of an
  // ImageSprite.

  @Override
  LayoutInfo createContainerLayoutInfo(Map<MockComponent, LayoutInfo> layoutInfoMap) {
    return new LayoutInfo(layoutInfoMap, container) {
      @Override
      int calculateAutomaticWidth() {
        if (imageUrl == null || imageUrl.equals("")) {
          return ComponentConstants.CANVAS_PREFERRED_WIDTH;
        } else {
          return image.getWidth();
        }
      }

      @Override
      int calculateAutomaticHeight() {
        if (imageUrl == null || imageUrl.equals("")) {
          return ComponentConstants.CANVAS_PREFERRED_HEIGHT;
        } else {
          return image.getHeight();
        }
      }
    };
  }

  @Override
  void layoutChildren(LayoutInfo containerLayoutInfo) {

    // Resolve any child's width or height that is fill parent.
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      // If the width is fill parent, use automatic width.
      if (childLayoutInfo.width == MockVisibleComponent.LENGTH_FILL_PARENT) {
        childLayoutInfo.calculateAndStoreAutomaticWidth();
      }
      // If the height is fill parent, use automatic height.
      if (childLayoutInfo.height == MockVisibleComponent.LENGTH_FILL_PARENT) {
        childLayoutInfo.calculateAndStoreAutomaticHeight();
      }
    }

    // Position the children.
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      int x;
      try {
        x = (int) Math.round(Double.parseDouble(child.getPropertyValue(PROPERTY_NAME_X)));
      } catch (NumberFormatException e) {
        // Ignore this. If we throw an exception here, the project is unrecoverable.
        x = 0;
      }
      int y;
      try {
        y = (int) Math.round(Double.parseDouble(child.getPropertyValue(PROPERTY_NAME_Y)));
      } catch (NumberFormatException e) {
        // Ignore this. If we throw an exception here, the project is unrecoverable.
        y = 0;
      }
      container.setChildSizeAndPosition(child, childLayoutInfo, x, y);
    }

    // Update layoutWidth and layoutHeight.
    // layoutWidth and layoutHeight are based on the background image, not on where the children
    // (sprites) are located.
    if (imageUrl == null || imageUrl.equals("")) {
      layoutWidth = ComponentConstants.CANVAS_PREFERRED_WIDTH;
      layoutHeight = ComponentConstants.CANVAS_PREFERRED_HEIGHT;
    } else {
      layoutWidth = image.getWidth();
      layoutHeight = image.getHeight();
    }
  }

  @Override
  boolean onDrop(MockComponent source, int x, int y, int offsetX, int offsetY) {
    // Set position of component
    source.changeProperty(PROPERTY_NAME_X, toIntegerString(x - offsetX));
    source.changeProperty(PROPERTY_NAME_Y, toIntegerString(y - offsetY));

    // Perform drop
    MockContainer srcContainer = source.getContainer();
    if (srcContainer != null) {
      // Pass false to indicate that the component isn't being permanently deleted.
      // It's just being moved from one container to another.
      srcContainer.removeComponent(source, false);
    }
    container.addComponent(source);
    ((MockCanvas) container).reorderComponents((MockSprite) source);
    return true;
  }

  /*
   * So this one is truly priceless: FF3 returns coordinates that are actually not integers, but
   * doubles. And since our code is translated into untyped Javascript coordinates we are now
   * suddenly handling doubles instead of integers which causes lots of problems down the road...
   */
  private String toIntegerString(int n) {
    return Integer.toString(n).split("\\.")[0];
  }
}
