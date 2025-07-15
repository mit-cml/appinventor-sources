// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.gwt.dom.client.Style.Position.ABSOLUTE;
import static com.google.gwt.dom.client.Style.Unit.PX;
import static com.google.gwt.dom.client.Style.Visibility.HIDDEN;
import static com.google.gwt.dom.client.Style.Visibility.VISIBLE;

import com.google.appinventor.client.Ode;

import com.google.appinventor.components.common.ComponentConstants;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;

import java.util.Map;

/**
 * A layout class that arranges its components absolutely.
 */
final class MockAbsoluteLayout extends MockLayout {

  private static final int EMPTY_WIDTH = ComponentConstants.EMPTY_A_ARRANGEMENT_WIDTH;
  private static final int EMPTY_HEIGHT = ComponentConstants.EMPTY_A_ARRANGEMENT_HEIGHT;

  // The color of the drop-target area's border
  private static final String DROP_TARGET_AREA_COLOR = "#0000ff";

  /**
   * Defines the x and y coordinates of a component inside relative layout.
   */
  @SuppressWarnings("MemberName")
  private static class Coordinates {
    int x;
    int y;

    private Coordinates(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public int getX() {
      return x;
    }

    public int getY() {
      return y;
    }
  }

  private class RelativeLayoutInfo extends LayoutInfo {
    RelativeLayoutInfo(Map<MockComponent, LayoutInfo> layoutInfoMap, MockComponent relativeLayout) {
      super(layoutInfoMap, relativeLayout);
    }

    @Override
    protected void prepareToGatherDimensions() {
      super.prepareToGatherDimensions();
    }

    @Override
    int calculateAutomaticWidth() {
      return layoutWidth;
    }

    @Override
    int calculateAutomaticHeight() {
      return layoutHeight;
    }

    @Override
    void cleanUp() {
      super.cleanUp();
    }
  }

  // The DIV element that displays the drop-target area.
  private Element dropTargetArea; // lazily initialized

  /**
   * Creates a new relative layout.
   */
  MockAbsoluteLayout() {

    layoutWidth = EMPTY_WIDTH;
    layoutHeight = EMPTY_HEIGHT;
  }

  private void ensureDropTargetArea() {
    if (dropTargetArea == null) {
      dropTargetArea = DOM.createDiv();
      setDropTargetAreaVisible(false);
      dropTargetArea.getStyle().setBorderStyle(Style.BorderStyle.SOLID);
      dropTargetArea.getStyle().setBorderColor(DROP_TARGET_AREA_COLOR);
      dropTargetArea.getStyle().setBorderWidth(2, PX);
      DOM.appendChild(container.getRootPanel().getElement(), dropTargetArea);
    }
  }

  private void setDropTargetAreaVisible(boolean visible) {
    dropTargetArea.getStyle().setVisibility(visible ? VISIBLE : HIDDEN);
  }

  private void setDropTargetAreaBoundsAndShow() {
    final Style style = dropTargetArea.getStyle();
    style.setPosition(ABSOLUTE);
    style.setLeft(0, PX);
    style.setTop(0, PX);
    // I shifted layoutWidth and layoutHeight by 4 in order to make the border
    // appear on the bottom and right sides of the layout
    // -TODO there might be a better way to do this
    style.setWidth(layoutWidth - 4, PX);
    style.setHeight(layoutHeight - 4, PX);
    setDropTargetAreaVisible(true);
  }

  /**
   * Converts the child's position properties into a {@link Coordinates} object.
   *
   * @param child child component
   * @return the top left coordinates of the child component
   */
  private Coordinates getCoordinatesOfChild(MockComponent child) {
    int x = Integer.parseInt(child.getPropertyValue(MockVisibleComponent.PROPERTY_NAME_LEFT));
    int y = Integer.parseInt(child.getPropertyValue(MockVisibleComponent.PROPERTY_NAME_TOP));
    return new Coordinates(x, y);
  }

  @Override
  LayoutInfo createContainerLayoutInfo(Map<MockComponent, LayoutInfo> layoutInfoMap) {
    ensureDropTargetArea();
    return new RelativeLayoutInfo(layoutInfoMap, container);
  }

  @Override
  void layoutChildren(LayoutInfo containerLayoutInfo) {
    // -TODO to add things after taking care of automatic sizing of parent layout
    // and other things
    RelativeLayoutInfo relativeLayoutInfo = (RelativeLayoutInfo) containerLayoutInfo;
    MockForm form = container.getForm();

    layoutWidth = relativeLayoutInfo.width;
    layoutHeight = relativeLayoutInfo.height;

    Ode.CLog("layoutChildren width: " + layoutWidth);
    Ode.CLog("layoutChildren height " + layoutHeight);

    for (MockComponent child : relativeLayoutInfo.visibleChildren) {
      Coordinates coords;
      try {
        coords = getCoordinatesOfChild(child);
      } catch (Exception e) {
        Ode.CLog("Couldn't get coordinates of child: " + e.getMessage());
        continue;
      }
      Ode.CLog("parent length: " + layoutWidth);
      Ode.CLog("parent height: " + layoutHeight);
      if (! withinLayoutBounds(coords.getX(), coords.getY())) {
        child.setVisible(false);
        continue;
      }

      LayoutInfo childLayoutInfo = relativeLayoutInfo.layoutInfoMap.get(child);

      if (childLayoutInfo.width == MockVisibleComponent.LENGTH_FILL_PARENT) {
        // If the child's width is set to fill parent, we want to shift the child's top
        // left corner to be on the left frame so that it fills the parent entirely.
        // Then we set its width to be the same as the parent layout's width
        child.changeProperty(MockVisibleComponent.PROPERTY_NAME_LEFT, "0");
        childLayoutInfo.width = layoutWidth;
      } else if (childLayoutInfo.width <= MockVisibleComponent.LENGTH_PERCENT_TAG) {
        int childWidth = (- (childLayoutInfo.width - MockVisibleComponent.LENGTH_PERCENT_TAG))
            * form.screenWidth / 100;
        childLayoutInfo.width = childWidth; // Side effect it...
        Ode.CLog("MockAbsoluteLayout: form.screenWidth = " + form.screenWidth
            + " childWidth = " + childWidth);
      }

      if (childLayoutInfo.height == MockVisibleComponent.LENGTH_FILL_PARENT) {
        // If the child's height is set to fill parent, we want to shift the child's top
        // left corner to be on the top frame so that it fills the parent entirely.
        // Then we set its height to be the same as the parent layout's height
        child.changeProperty(MockVisibleComponent.PROPERTY_NAME_TOP, "0");
        childLayoutInfo.height = layoutHeight;
      } else if (childLayoutInfo.height <= MockVisibleComponent.LENGTH_PERCENT_TAG) {
        int childHeight = (- (childLayoutInfo.height - MockVisibleComponent.LENGTH_PERCENT_TAG))
            * form.usableScreenHeight / 100;
        childLayoutInfo.height = childHeight; // Side effect it...
        Ode.CLog("MockAbsoluteLayout: form.usableScreenHeight = " + form.usableScreenHeight
            + " childHeight = " + childHeight);
      }

      if (child instanceof MockContainer) {
        ((MockContainer) child).getLayout().layoutChildren(childLayoutInfo);
      }
      int x = Integer.parseInt(child.getPropertyValue(MockVisibleComponent.PROPERTY_NAME_LEFT));
      int y = Integer.parseInt(child.getPropertyValue(MockVisibleComponent.PROPERTY_NAME_TOP));
      container.setChildSizeAndPosition(child, childLayoutInfo, x, y);
    }

  }

  /**
   * Checks to see if the given values of a mock child are within the layout width
   * and height bounds.
   *
   * @param leftMargin left margin of the mock child component
   * @param topMargin top margin of the mock child component
   * @return true iff mock child component is within layout bounds
   */
  private boolean withinLayoutBounds(int leftMargin, int topMargin) {
    return (0 <= leftMargin && 0 <= topMargin)
         && (leftMargin < layoutWidth || layoutWidth == MockVisibleComponent.LENGTH_FILL_PARENT)
         && (topMargin < layoutHeight || layoutHeight == MockVisibleComponent.LENGTH_FILL_PARENT);
  }

  @Override
  void onDragContinue(int x, int y) {
    setDropTargetAreaBoundsAndShow();
  }

  @Override
  void onDragLeave() {
    setDropTargetAreaVisible(false);
  }

  @Override
  boolean onDrop(MockComponent source, int x, int y, int offsetX, int offsetY) {
    int leftMargin = x - offsetX;
    int topMargin = y - offsetY;

    setDropTargetAreaVisible(false);

    // another way to do this is to allow the top-left corner of the dropped
    // component to be outside the arrangement, in which case we can just take
    // max(0, topMargin) or max(0, leftMargin) and place the component normally
    if (withinLayoutBounds(leftMargin, topMargin)) {
      MockContainer srcContainer = source.getContainer();
      if (srcContainer != null) {
        // Pass false to indicate that the component isn't being permanently deleted.
        // It's just being moved from one container to another.
        srcContainer.removeComponent(source, false);
      }
      try {
        source.changeProperty(MockVisibleComponent.PROPERTY_NAME_LEFT, "" + leftMargin);
        source.changeProperty(MockVisibleComponent.PROPERTY_NAME_TOP, "" + topMargin);
      } catch (Exception e) {
        return false;
      }

      try {
        container.addComponent(source, leftMargin, topMargin);
      } catch (Exception e) {
        return false;
      }
      return true;
    }

    return false;
  }

  @Override
  void dispose() {
    if (dropTargetArea != null) {
      container.getRootPanel().getElement().removeChild(dropTargetArea);
    }
  }
}
