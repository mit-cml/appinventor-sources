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

import com.google.appinventor.client.widgets.dnd.DragSource;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A layout class that arranges its components absolutely.
 */
final class MockAbsoluteLayout extends MockLayout {

  private static final int EMPTY_WIDTH = ComponentConstants.EMPTY_A_ARRANGEMENT_WIDTH;
  private static final int EMPTY_HEIGHT = ComponentConstants.EMPTY_A_ARRANGEMENT_HEIGHT;

  // The color of the drop-target area's border
  private static final String DROP_TARGET_AREA_COLOR = "#0000ff";

  // Snap / guideline constants — snapEnabled state lives in MockContainer for cross-package access
  private static final int SNAP_THRESHOLD = 16;
  private static final int GRID_SIZE = 8;
  private static final String ALIGNMENT_COLOR = "#ff0000";
  private static final String CENTER_COLOR = "#0000ff";

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

  private enum GuidelineType { HORIZONTAL, VERTICAL }

  private static class GuidelineSpec {
    final GuidelineType type;
    final int value;
    final String color;

    GuidelineSpec(GuidelineType type, int value, String color) {
      this.type = type;
      this.value = value;
      this.color = color;
    }
  }

  private static class SnapResult {
    final int left;
    final int top;
    final String storedLeft;   // non-null only on left-left snap: sibling's stored Left value
    final String storedTop;    // non-null only on top-top snap:  sibling's stored Top value
    final List<GuidelineSpec> guidelines;

    SnapResult(int left, int top, String storedLeft, String storedTop,
        List<GuidelineSpec> guidelines) {
      this.left = left;
      this.top = top;
      this.storedLeft = storedLeft;
      this.storedTop = storedTop;
      this.guidelines = guidelines;
    }
  }

  // The DIV element that displays the drop-target area.
  private Element dropTargetArea; // lazily initialized

  // Guideline overlay infrastructure
  private Element guidelineOverlay; // lazily initialized transparent full-size div
  private final List<Element> activeGuidelineElements = new ArrayList<>();

  // Snap state for the current drag
  private MockComponent currentDragSource;
  private int lastSnappedLeft;
  private int lastSnappedTop;
  private boolean hasSnappedPosition;
  private SnapResult lastSnapResult;

  // Drag geometry — cached once on drag-enter to avoid re-reading modified CSS
  private int dragOffsetX;
  private int dragOffsetY;
  private int dragWidth;
  private int dragHeight;
  private boolean dragGeometryCached;

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

  private void ensureGuidelineOverlay() {
    if (guidelineOverlay == null) {
      guidelineOverlay = DOM.createDiv();
      Style style = guidelineOverlay.getStyle();
      style.setPosition(ABSOLUTE);
      style.setLeft(0, PX);
      style.setTop(0, PX);
      style.setProperty("pointerEvents", "none");
      style.setZIndex(1000);
      style.setVisibility(HIDDEN);
      DOM.appendChild(container.getRootPanel().getElement(), guidelineOverlay);
    }
  }

  private void showGuidelines(List<GuidelineSpec> specs) {
    ensureGuidelineOverlay();
    // Remove previously added guideline child elements
    for (Element e : activeGuidelineElements) {
      guidelineOverlay.removeChild(e);
    }
    activeGuidelineElements.clear();

    for (GuidelineSpec spec : specs) {
      Element line = DOM.createDiv();
      Style s = line.getStyle();
      s.setPosition(ABSOLUTE);
      s.setBackgroundColor(spec.color);
      if (spec.type == GuidelineType.VERTICAL) {
        s.setLeft(spec.value, PX);
        s.setTop(0, PX);
        s.setWidth(1, PX);
        s.setHeight(layoutHeight, PX);
      } else {
        s.setTop(spec.value, PX);
        s.setLeft(0, PX);
        s.setHeight(1, PX);
        s.setWidth(layoutWidth, PX);
      }
      guidelineOverlay.appendChild(line);
      activeGuidelineElements.add(line);
    }
    guidelineOverlay.getStyle().setVisibility(VISIBLE);
  }

  private void hideGuidelines() {
    if (guidelineOverlay != null) {
      for (Element e : activeGuidelineElements) {
        guidelineOverlay.removeChild(e);
      }
      activeGuidelineElements.clear();
      guidelineOverlay.getStyle().setVisibility(HIDDEN);
    }
  }

  /**
   * Converts the child's position properties into a {@link Coordinates} object.
   *
   * @param child child component
   * @return the top left coordinates of the child component
   */
  private Coordinates getCoordinatesOfChild(MockComponent child) {
    try {
      int x = Integer.parseInt(child.getPropertyValue(MockVisibleComponent.PROPERTY_NAME_LEFT));
      int y = Integer.parseInt(child.getPropertyValue(MockVisibleComponent.PROPERTY_NAME_TOP));
      return new Coordinates(x, y);
    } catch (NumberFormatException e) {
      return new Coordinates(0, 0);
    }
  }

  /**
   * Sets the component being dragged over this layout, for use in snap calculations.
   * Caches drag geometry (offset, size) from the drag widget CSS immediately, before any
   * ghost-snapping modifies those values on subsequent mouse-move events.
   */
  void setDragSource(DragSource source) {
    currentDragSource = (source instanceof MockComponent) ? (MockComponent) source : null;
    hasSnappedPosition = false;
    lastSnapResult = null;
    dragGeometryCached = false;
    dragOffsetX = 0;
    dragOffsetY = 0;
    dragWidth = 0;
    dragHeight = 0;
  }

  /**
   * Called during drag with the source available, enabling snap and guideline logic.
   * Updates the drag ghost to show the projected snap landing position.
   */
  void onDragContinueWithSource(int x, int y, DragSource source) {
    setDropTargetAreaBoundsAndShow();

    Widget dw = source.getDragWidget();

    // Cache drag geometry once from the unmodified CSS, before we start moving the ghost.
    if (!dragGeometryCached && dw != null) {
      dragOffsetX = -parsePx(DOM.getStyleAttribute(dw.getElement(), "left"));
      dragOffsetY = -parsePx(DOM.getStyleAttribute(dw.getElement(), "top"));
      dragWidth = dw.getOffsetWidth();
      dragHeight = dw.getOffsetHeight();
      dragGeometryCached = true;
    }

    if (!MockContainer.absoluteLayoutSnapEnabled) {
      hideGuidelines();
      // Restore ghost to natural cursor-follow position
      if (dw != null) {
        dw.getElement().getStyle().setLeft(-dragOffsetX, PX);
        dw.getElement().getStyle().setTop(-dragOffsetY, PX);
      }
      return;
    }

    int candidateLeft = x - dragOffsetX;
    int candidateTop = y - dragOffsetY;

    SnapResult result = calculateSnap(candidateLeft, candidateTop, dragWidth, dragHeight);
    lastSnappedLeft = result.left;
    lastSnappedTop = result.top;
    lastSnapResult = result;
    hasSnappedPosition = true;
    showGuidelines(result.guidelines);

    // Move the drag ghost to show exactly where the component will land on drop.
    // Ghost CSS left/top encodes offset from cursor; snapped left = cursor.x - x_in_container + snappedLeft.
    // Since x is cursor relative to container: ghostLeft = snappedLeft - x (i.e. snapped - cursor offset).
    if (dw != null) {
      dw.getElement().getStyle().setLeft(result.left - x, PX);
      dw.getElement().getStyle().setTop(result.top - y, PX);
    }
  }

  private static int parsePx(String s) {
    if (s != null && s.endsWith("px")) {
      try {
        return Integer.parseInt(s.substring(0, s.length() - 2));
      } catch (NumberFormatException e) {
        return 0;
      }
    }
    return 0;
  }

  private int snapToGrid(int value) {
    return Math.round((float) value / GRID_SIZE) * GRID_SIZE;
  }

  private SnapResult calculateSnap(int candidateLeft, int candidateTop,
      int dragWidth, int dragHeight) {
    List<GuidelineSpec> guidelines = new ArrayList<>();
    int sl = snapToGrid(candidateLeft);
    int st = snapToGrid(candidateTop);
    boolean hasVSnap = false;
    boolean hasHSnap = false;

    // Screen center checks (blue)
    int scX = layoutWidth / 2;
    int scY = layoutHeight / 2;
    int dcX = candidateLeft + dragWidth / 2;
    int dcY = candidateTop + dragHeight / 2;

    if (Math.abs(dcX - scX) <= SNAP_THRESHOLD) {
      sl = scX - dragWidth / 2;
      hasVSnap = true;
      guidelines.add(new GuidelineSpec(GuidelineType.VERTICAL, scX, CENTER_COLOR));
    }
    if (Math.abs(dcY - scY) <= SNAP_THRESHOLD) {
      st = scY - dragHeight / 2;
      hasHSnap = true;
      guidelines.add(new GuidelineSpec(GuidelineType.HORIZONTAL, scY, CENTER_COLOR));
    }

    // Sibling component checks (red) — find closest within threshold across all siblings
    int bestDeltaX = SNAP_THRESHOLD + 1;
    int bestSnapLeft = sl;
    int bestLineX = 0;
    int bestDeltaY = SNAP_THRESHOLD + 1;
    int bestSnapTop = st;
    int bestLineY = 0;
    String bestStoredLeft = null;
    String bestStoredTop = null;

    for (MockComponent sibling : container.getChildren()) {
      if (sibling == currentDragSource || !sibling.isVisibleComponent()) {
        continue;
      }
      int sibL = container.getRootPanel().getWidgetLeft(sibling);
      int sibT = container.getRootPanel().getWidgetTop(sibling);
      int sibR = sibL + sibling.getOffsetWidth();
      int sibB = sibT + sibling.getOffsetHeight();
      int sibCX = (sibL + sibR) / 2;
      int sibCY = (sibT + sibB) / 2;
      int dragR = candidateLeft + dragWidth;
      int dragCX = candidateLeft + dragWidth / 2;
      int dragB = candidateTop + dragHeight;
      int dragCY = candidateTop + dragHeight / 2;

      if (!hasVSnap) {
        // left-left: copy sibling's stored Left to avoid percent round-trip error
        int d = Math.abs(candidateLeft - sibL);
        if (d < bestDeltaX) { bestDeltaX = d; bestSnapLeft = sibL; bestLineX = sibL;
          bestStoredLeft = sibling.getPropertyValue(MockVisibleComponent.PROPERTY_NAME_LEFT); }
        // left-right
        d = Math.abs(candidateLeft - sibR);
        if (d < bestDeltaX) { bestDeltaX = d; bestSnapLeft = sibR; bestLineX = sibR;
          bestStoredLeft = null; }
        // right-right
        d = Math.abs(dragR - sibR);
        if (d < bestDeltaX) { bestDeltaX = d; bestSnapLeft = sibR - dragWidth; bestLineX = sibR;
          bestStoredLeft = null; }
        // right-left
        d = Math.abs(dragR - sibL);
        if (d < bestDeltaX) { bestDeltaX = d; bestSnapLeft = sibL - dragWidth; bestLineX = sibL;
          bestStoredLeft = null; }
        // center-center
        d = Math.abs(dragCX - sibCX);
        if (d < bestDeltaX) { bestDeltaX = d; bestSnapLeft = sibCX - dragWidth / 2; bestLineX = sibCX;
          bestStoredLeft = null; }
      }

      if (!hasHSnap) {
        // top-top: copy sibling's stored Top to avoid percent round-trip error
        int d = Math.abs(candidateTop - sibT);
        if (d < bestDeltaY) { bestDeltaY = d; bestSnapTop = sibT; bestLineY = sibT;
          bestStoredTop = sibling.getPropertyValue(MockVisibleComponent.PROPERTY_NAME_TOP); }
        // top-bottom
        d = Math.abs(candidateTop - sibB);
        if (d < bestDeltaY) { bestDeltaY = d; bestSnapTop = sibB; bestLineY = sibB;
          bestStoredTop = null; }
        // bottom-bottom
        d = Math.abs(dragB - sibB);
        if (d < bestDeltaY) { bestDeltaY = d; bestSnapTop = sibB - dragHeight; bestLineY = sibB;
          bestStoredTop = null; }
        // bottom-top
        d = Math.abs(dragB - sibT);
        if (d < bestDeltaY) { bestDeltaY = d; bestSnapTop = sibT - dragHeight; bestLineY = sibT;
          bestStoredTop = null; }
        // center-center
        d = Math.abs(dragCY - sibCY);
        if (d < bestDeltaY) { bestDeltaY = d; bestSnapTop = sibCY - dragHeight / 2; bestLineY = sibCY;
          bestStoredTop = null; }
      }
    }

    if (!hasVSnap && bestDeltaX <= SNAP_THRESHOLD) {
      sl = bestSnapLeft;
      guidelines.add(new GuidelineSpec(GuidelineType.VERTICAL, bestLineX, ALIGNMENT_COLOR));
    } else {
      bestStoredLeft = null;
    }
    if (!hasHSnap && bestDeltaY <= SNAP_THRESHOLD) {
      st = bestSnapTop;
      guidelines.add(new GuidelineSpec(GuidelineType.HORIZONTAL, bestLineY, ALIGNMENT_COLOR));
    } else {
      bestStoredTop = null;
    }

    return new SnapResult(sl, st, bestStoredLeft, bestStoredTop, guidelines);
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

      // Resolve percent-encoded coordinates to pixel positions for rendering and bounds checking.
      int x = coords.getX();
      int y = coords.getY();
      if (x <= MockVisibleComponent.LENGTH_PERCENT_TAG) {
        x = -(x - MockVisibleComponent.LENGTH_PERCENT_TAG) * layoutWidth / 100;
      }
      if (y <= MockVisibleComponent.LENGTH_PERCENT_TAG) {
        y = -(y - MockVisibleComponent.LENGTH_PERCENT_TAG) * layoutHeight / 100;
      }

      if (!withinLayoutBounds(x, y)) {
        child.setVisible(false);
        continue;
      }

      LayoutInfo childLayoutInfo = relativeLayoutInfo.layoutInfoMap.get(child);

      if (childLayoutInfo.width == MockVisibleComponent.LENGTH_FILL_PARENT) {
        // Fill Parent width: stretch from Left position to right edge of parent.
        childLayoutInfo.width = layoutWidth - x;
      } else if (childLayoutInfo.width <= MockVisibleComponent.LENGTH_PERCENT_TAG) {
        int childWidth = (- (childLayoutInfo.width - MockVisibleComponent.LENGTH_PERCENT_TAG))
            * form.screenWidth / 100;
        childLayoutInfo.width = childWidth; // Side effect it...
        Ode.CLog("MockAbsoluteLayout: form.screenWidth = " + form.screenWidth
            + " childWidth = " + childWidth);
      }

      if (childLayoutInfo.height == MockVisibleComponent.LENGTH_FILL_PARENT) {
        // Fill Parent height: stretch from Top position to bottom edge of parent.
        childLayoutInfo.height = layoutHeight - y;
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
    hideGuidelines();
    currentDragSource = null;
    hasSnappedPosition = false;
    dragGeometryCached = false;
  }

  @Override
  boolean onDrop(MockComponent source, int x, int y, int offsetX, int offsetY) {
    int leftMargin;
    int topMargin;

    boolean wasSnapped = MockContainer.absoluteLayoutSnapEnabled && hasSnappedPosition;
    SnapResult snapResult = lastSnapResult;

    if (wasSnapped) {
      leftMargin = lastSnappedLeft;
      topMargin = lastSnappedTop;
    } else {
      leftMargin = x - offsetX;
      topMargin = y - offsetY;
    }

    setDropTargetAreaVisible(false);
    hideGuidelines();
    currentDragSource = null;
    hasSnappedPosition = false;
    lastSnapResult = null;

    // A Fill Parent component spans the full container width, so the user may grab it far from
    // its left edge. This makes offsetX large, causing leftMargin = x - offsetX to go negative.
    // Negative margins are never valid — clamp to zero so the drop succeeds rather than spring-back.
    leftMargin = Math.max(0, leftMargin);
    topMargin = Math.max(0, topMargin);

    if (withinLayoutBounds(leftMargin, topMargin)) {
      MockContainer srcContainer = source.getContainer();
      if (srcContainer != null) {
        // Pass false to indicate that the component isn't being permanently deleted.
        // It's just being moved from one container to another.
        srcContainer.removeComponent(source, false);
      }

      // In Responsive mode, convert pixel drop coordinates to percent encoding so the
      // component repositions proportionally on any screen size.
      // For left-left / top-top snaps, copy the sibling's stored value directly to avoid
      // the percent round-trip rounding error that would shift the component by a few pixels.
      String sizing = container.getForm().getPropertyValue("Sizing");
      boolean responsive = layoutWidth > 0 && layoutHeight > 0 && "Responsive".equals(sizing);

      String newLeft;
      if (wasSnapped && snapResult != null && snapResult.storedLeft != null) {
        newLeft = snapResult.storedLeft;
      } else {
        int sl = responsive
            ? MockVisibleComponent.LENGTH_PERCENT_TAG
                - Math.round((float) leftMargin * 100 / layoutWidth)
            : leftMargin;
        newLeft = "" + sl;
      }

      String newTop;
      if (wasSnapped && snapResult != null && snapResult.storedTop != null) {
        newTop = snapResult.storedTop;
      } else {
        int st = responsive
            ? MockVisibleComponent.LENGTH_PERCENT_TAG
                - Math.round((float) topMargin * 100 / layoutHeight)
            : topMargin;
        newTop = "" + st;
      }

      try {
        source.changeProperty(MockVisibleComponent.PROPERTY_NAME_LEFT, newLeft);
        source.changeProperty(MockVisibleComponent.PROPERTY_NAME_TOP, newTop);
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
    if (guidelineOverlay != null) {
      container.getRootPanel().getElement().removeChild(guidelineOverlay);
    }
  }
}
