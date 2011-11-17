// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import java.util.Map;

/**
 * A base class for layouts that arrange the children of a container in a single
 * column or a single row. This base class does not allow changing the
 * orientation dynamically.
 *
 * @author markf@google.com (Mark Friedman)
 * @author sharon@google.com (Sharon Perl)
 * @author lizlooney@google.com (Liz Looney)
 */
abstract class MockHVLayoutBase extends MockLayout {
  // Gap between adjacent components to allow for the insertion divider
  private static final int COMPONENT_SPACING = 5;

  // The color of the insertion divider
  private static final String DIVIDER_COLOR = "#0000ff";

  private static final int EMPTY_WIDTH = ComponentConstants.EMPTY_HV_ARRANGEMENT_WIDTH;
  private static final int EMPTY_HEIGHT = ComponentConstants.EMPTY_HV_ARRANGEMENT_HEIGHT;

  protected final int orientation;

  // Possible locations for the insertion divider;
  // calculated in layoutContainer.
  private int[] dividerLocations;

  /**
   * The location of the insertion divider that shows where a dragged component
   * that is hovering over this layout's container will be inserted if the
   * component is dropped.
   * <p>
   * Legal values include {@code -1} (divider invisible) and {@code 0} (before
   * the first child) to {@code numChildren} (after the last child).
   */
  private int dividerPos;

  // The DIV element that displays the insertion divider.
  // Is added to the root panel of the associated container.
  private Element dividerElement; // lazily initialized

  // Offset of each child's center along the axis of this layout;
  // calculated in layoutContainer; used to calculate the correct
  // drop location for a component dropped onto this layout's container
  private int[] childMidpoints;

  /**
   * Creates a new linear layout with the specified orientation.
   */
  MockHVLayoutBase(int orientation) {
    if (orientation != ComponentConstants.LAYOUT_ORIENTATION_VERTICAL &&
        orientation != ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL) {
      throw new IllegalArgumentException("Illegal orientation: " + orientation);
    }
    this.orientation = orientation;
    layoutWidth = EMPTY_WIDTH;
    layoutHeight = EMPTY_HEIGHT;
    dividerPos = -1;
  }

  // Divider

  private void ensureDividerInited() {
    if (dividerElement == null) {
      dividerElement = DOM.createDiv();
      DOM.setStyleAttribute(dividerElement, "backgroundColor", DIVIDER_COLOR);
      setDividerVisible(false);
      DOM.appendChild(container.getRootPanel().getElement(), dividerElement);
    }
  }

  private void setDividerLocation(int dividerPos) {
    if (dividerPos >= dividerLocations.length) {
      throw new IllegalArgumentException("Illegal dividerPos: " + dividerPos);
    }
    if (this.dividerPos != dividerPos) {
      this.dividerPos = dividerPos;
      if (dividerPos == -1) {
        setDividerVisible(false);
      } else {
        if (orientation == ComponentConstants.LAYOUT_ORIENTATION_VERTICAL) {
          setDividerBoundsAndShow(0, dividerLocations[dividerPos],
              container.getOffsetWidth(), COMPONENT_SPACING);
        } else {
          setDividerBoundsAndShow(dividerLocations[dividerPos], 0,
              COMPONENT_SPACING, container.getOffsetHeight());
        }
      }
    }
  }

  private void setDividerVisible(boolean visible) {
    DOM.setStyleAttribute(dividerElement, "visibility", visible ? "visible" : "hidden");
  }

  private void setDividerBoundsAndShow(int x, int y, int width, int height) {
    DOM.setStyleAttribute(dividerElement, "position", "absolute");
    DOM.setStyleAttribute(dividerElement, "left", x + "px");
    DOM.setStyleAttribute(dividerElement, "top", y + "px");
    DOM.setStyleAttribute(dividerElement, "width", width + "px");
    DOM.setStyleAttribute(dividerElement, "height", height + "px");
    setDividerVisible(true);
  }

  // MockLayout methods

  // NOTE(lizlooney) - layout behavior:

  // The Screen component has a Scrollable property. When the Scrollable property is checked, the
  // Screen component behaves like a VerticalArrangement whose Height property is set to Automatic.
  // When the Scrollable property is not checked, the Screen component behaves like a
  // VerticalArrangement whose Height property is specified in pixels.

  // In a VerticalArrangement, components are arranged along the vertical axis, left-aligned.
  // If a VerticalArrangement's Width property is set to Automatic, the actual width of the
  // arrangement is determined by the widest component whose Width property is not set to Fill
  // Parent. If a VerticalArrangement's Width property is set to Automatic and it contains only
  // components whose Width properties are set to Fill Parent, the actual width of the
  // arrangement is calculated using the automatic widths of the components. If a
  // VerticalArrangement's Width property is set to Automatic and it is empty, the width will be
  // 100.
  // If a VerticalArrangement's Height property is set to Automatic, the actual height of the
  // arrangement is determined by the sum of the heights of the components. If a
  // VerticalArrangement's Height property is set to Automatic, any components whose Height
  // properties are set to Fill Parent will behave as if they were set to Automatic.
  // If a VerticalArrangement's Height property is set to Fill Parent or specified in pixels, any
  // components whose Height properties are set to Fill Parent will equally take up the height not
  // occupied by other components.

  // In a HorizontalArrangement, components are arranged along the horizontal axis, vertically
  // center-aligned.
  // If a HorizontalArrangement's Height property is set to Automatic, the actual height of the
  // arrangement is determined by the tallest component whose Height property is not set to Fill
  // Parent. If a HorizontalArrangement's Height property is set to Automatic and it contains only
  // components whose Height properties are set to Fill Parent, the actual height of the
  // arrangement is calculated using the automatic heights of the components. If a
  // HorizontalArrangement's Height property is set to Automatic and it is empty, the height will be
  // 100.
  // If a HorizontalArrangement's Width property is set to Automatic, the actual width of the
  // arrangement is determined by the sum of the widths of the components. If a
  // HorizontalArrangement's Width property is set to Automatic, any components whose Width
  // properties are set to Fill Parent will behave as if they were set to Automatic.
  // If a HorizontalArrangement's Width property is set to Fill Parent or specified in pixels, any
  // components whose Width properties are set to Fill Parent will equally take up the width not
  // occupied by other components.

  @Override
  LayoutInfo createContainerLayoutInfo(Map<MockComponent, LayoutInfo> layoutInfoMap) {
    ensureDividerInited();

    return new LayoutInfo(layoutInfoMap, container) {
      @Override
      void calculateAndStoreAutomaticWidth() {
        if (orientation == ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL) {
          // In a HorizontalArrangement whose width is automatic, a child whose width is fill
          // parent will behave as if it were automatic.
          for (MockComponent child : visibleChildren) {
            LayoutInfo childLayoutInfo = layoutInfoMap.get(child);
            if (childLayoutInfo.width == MockVisibleComponent.LENGTH_FILL_PARENT) {
              childLayoutInfo.calculateAndStoreAutomaticWidth();
            }
          }
        }
        super.calculateAndStoreAutomaticWidth();
      }

      @Override
      void calculateAndStoreAutomaticHeight() {
        if (orientation == ComponentConstants.LAYOUT_ORIENTATION_VERTICAL) {
          // In a VerticalArrangement whose height is automatic, a child whose height is fill
          // parent will behave as if it were automatic.
          for (MockComponent child : visibleChildren) {
            LayoutInfo childLayoutInfo = layoutInfoMap.get(child);
            if (childLayoutInfo.height == MockVisibleComponent.LENGTH_FILL_PARENT) {
              childLayoutInfo.calculateAndStoreAutomaticHeight();
            }
          }
        }
        super.calculateAndStoreAutomaticHeight();
      }

      @Override
      int calculateAutomaticWidth() {
        if (visibleChildren.isEmpty()) {
          return EMPTY_WIDTH;
        }

        if (orientation == ComponentConstants.LAYOUT_ORIENTATION_VERTICAL) {
          return calculateAutomaticWidthVertical(this);
        } else {
          return calculateAutomaticWidthHorizontal(this);
        }
      }

      @Override
      int calculateAutomaticHeight() {
        if (visibleChildren.isEmpty()) {
          return EMPTY_HEIGHT;
        }

        if (orientation == ComponentConstants.LAYOUT_ORIENTATION_VERTICAL) {
          return calculateAutomaticHeightVertical(this);
        } else {
          return calculateAutomaticHeightHorizontal(this);
        }
      }
    };
  }

  @Override
  void layoutChildren(LayoutInfo containerLayoutInfo) {
    int visibleChildrenSize = containerLayoutInfo.visibleChildren.size();
    dividerLocations = new int[visibleChildrenSize + 1];
    childMidpoints = new int[visibleChildrenSize];
    if (visibleChildrenSize > 0) {
      if (orientation == ComponentConstants.LAYOUT_ORIENTATION_VERTICAL) {
        layoutVertical(containerLayoutInfo);
      } else {
        layoutHorizontal(containerLayoutInfo);
      }
    } else {
      layoutWidth = EMPTY_WIDTH;
      layoutHeight = EMPTY_HEIGHT;
    }
  }

  private int calculateAutomaticWidthVertical(LayoutInfo containerLayoutInfo) {
    // The width will be the widest child, ignoring any child's width that is fill parent.
    boolean allFillParent = true;
    int width = 0;
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      if (childLayoutInfo.width != MockVisibleComponent.LENGTH_FILL_PARENT) {
        width = Math.max(width, childLayoutInfo.width + BORDER_SIZE);
        allFillParent = false;
      }
    }
    // If all children have widths that are fill parent, find the widest child using the
    // automatic widths of the children.
    if (allFillParent) {
      for (MockComponent child : containerLayoutInfo.visibleChildren) {
        LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
        int childWidth = childLayoutInfo.calculateAutomaticWidth();
        width = Math.max(width, childWidth + BORDER_SIZE);
      }
    }
    return width;
  }

  private int calculateAutomaticHeightVertical(LayoutInfo containerLayoutInfo) {
    // The height will be the sum of the child heights.
    int height = 0;
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      height += COMPONENT_SPACING;
      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      // If the height is fill parent, use automatic height.
      int childHeight = (childLayoutInfo.height == MockVisibleComponent.LENGTH_FILL_PARENT)
          ? childLayoutInfo.calculateAutomaticHeight()
          : childLayoutInfo.height;
      height += childHeight + BORDER_SIZE;
    }
    height += COMPONENT_SPACING;
    return height;
  }

  private void layoutVertical(LayoutInfo containerLayoutInfo) {
    // Components are arranged along the vertical axis, left-aligned.

    // Calculate the height used up by children whose height is not fill parent.
    int usedHeight = 0;
    int countFillParent = 0;
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      usedHeight += COMPONENT_SPACING;
      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      if (childLayoutInfo.height == MockVisibleComponent.LENGTH_FILL_PARENT) {
        countFillParent++;
      } else {
        usedHeight += childLayoutInfo.height + BORDER_SIZE;
      }
    }
    usedHeight += COMPONENT_SPACING;
    int remainingHeight = containerLayoutInfo.height - usedHeight;
    if (remainingHeight < 0) {
      remainingHeight = 0;
    }

    // Resolve any child's width or height that is fill parent, and call layoutChildren for
    // children that are containers.
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      if (childLayoutInfo.width == MockVisibleComponent.LENGTH_FILL_PARENT) {
        childLayoutInfo.width = containerLayoutInfo.width - BORDER_SIZE;
      }
      if (childLayoutInfo.height == MockVisibleComponent.LENGTH_FILL_PARENT) {
        childLayoutInfo.height = remainingHeight / countFillParent - BORDER_SIZE;
      }

      // If the child is a container call layoutChildren for it.
      if (child instanceof MockContainer) {
        ((MockContainer) child).getLayout().layoutChildren(childLayoutInfo);
      }
    }

    // Position the children and update layoutWidth and layoutHeight.
    layoutWidth = 0;
    int x = 0;
    int y = 0;
    int index = 0;
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      dividerLocations[index] = y;
      y += COMPONENT_SPACING;

      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      int childWidthWithBorder = childLayoutInfo.width + BORDER_SIZE;
      int childHeightWithBorder = childLayoutInfo.height + BORDER_SIZE;

      container.setChildSizeAndPosition(child, childLayoutInfo, x, y);
      layoutWidth = Math.max(layoutWidth, x + childWidthWithBorder);
      childMidpoints[index] = y + (childHeightWithBorder / 2);
      y += childHeightWithBorder;
      index++;
    }
    dividerLocations[index] = y;
    y += COMPONENT_SPACING;
    layoutHeight = y;
  }

  private int calculateAutomaticHeightHorizontal(LayoutInfo containerLayoutInfo) {
    // The height will be the tallest child, ignoring any child's height that is fill parent.
    boolean allFillParent = true;
    int height = 0;
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      if (childLayoutInfo.height != MockVisibleComponent.LENGTH_FILL_PARENT) {
        height = Math.max(height, childLayoutInfo.height + BORDER_SIZE);
        allFillParent = false;
      }
    }
    // If all children have heights that are fill parent, find the tallest child using the
    // automatic heights of the children.
    if (allFillParent) {
      for (MockComponent child : containerLayoutInfo.visibleChildren) {
        LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
        int childHeight = childLayoutInfo.calculateAutomaticHeight();
        height = Math.max(height, childHeight + BORDER_SIZE);
      }
    }
    return height;
  }

  private int calculateAutomaticWidthHorizontal(LayoutInfo containerLayoutInfo) {
    // The width will be the sum of the child widths.
    int width = 0;
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      width += COMPONENT_SPACING;
      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      // If the width is fill parent, use automatic width.
      int childWidth = (childLayoutInfo.width == MockVisibleComponent.LENGTH_FILL_PARENT)
          ? childLayoutInfo.calculateAutomaticWidth()
          : childLayoutInfo.width;
      width += childWidth + BORDER_SIZE;
    }
    width += COMPONENT_SPACING;
    return width;
  }

  private void layoutHorizontal(LayoutInfo containerLayoutInfo) {
    // Components are arranged along the horizontal axis, vertically center-aligned.

    // Calculate the width used up by children whose width is not fill parent.
    int usedWidth = 0;
    int countFillParent = 0;
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      usedWidth += COMPONENT_SPACING;
      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      if (childLayoutInfo.width == MockVisibleComponent.LENGTH_FILL_PARENT) {
        countFillParent++;
      } else {
        usedWidth += childLayoutInfo.width + BORDER_SIZE;
      }
    }
    usedWidth += COMPONENT_SPACING;
    int remainingWidth = containerLayoutInfo.width - usedWidth;
    if (remainingWidth < 0) {
      remainingWidth = 0;
    }

    // Resolve any child's width or height that is fill parent, and call layoutChildren for
    // children that are containers.
    // Figure out the height of the largest child so we can center-align all the children.
    int maxHeight = 0;
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      if (childLayoutInfo.width == MockVisibleComponent.LENGTH_FILL_PARENT) {
        childLayoutInfo.width = remainingWidth / countFillParent - BORDER_SIZE;
      }
      if (childLayoutInfo.height == MockVisibleComponent.LENGTH_FILL_PARENT) {
        childLayoutInfo.height = containerLayoutInfo.height - BORDER_SIZE;
      }

      maxHeight = Math.max(maxHeight, childLayoutInfo.height + BORDER_SIZE);

      // If the child is a container call layoutChildren for it.
      if (child instanceof MockContainer) {
        ((MockContainer) child).getLayout().layoutChildren(childLayoutInfo);
      }
    }

    int centerY = maxHeight / 2;

    // Position the children and update layoutWidth and layoutHeight.
    layoutHeight = 0;
    int x = 0;
    int index = 0;
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      dividerLocations[index] = x;
      x += COMPONENT_SPACING;

      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      int childWidthWithBorder = childLayoutInfo.width + BORDER_SIZE;
      int childHeightWithBorder = childLayoutInfo.height + BORDER_SIZE;

      int y = centerY - (childHeightWithBorder / 2);
      container.setChildSizeAndPosition(child, childLayoutInfo, x, y);
      layoutHeight = Math.max(layoutHeight, y + childHeightWithBorder);
      childMidpoints[index] = x + (childWidthWithBorder / 2);
      x += childWidthWithBorder;
      index++;
    }
    dividerLocations[index] = x;
    x += COMPONENT_SPACING;
    layoutWidth = x;
  }

  @Override
  void onDragContinue(int x, int y) {
    if (childMidpoints != null) {
      // Calculate the position where the hovering component should be inserted
      int insertPos = -1;
      int dropOffset = (orientation == ComponentConstants.LAYOUT_ORIENTATION_VERTICAL) ? y : x;
      for (int i = 0; i < childMidpoints.length; i++) {
        if (dropOffset <= childMidpoints[i]) {
          insertPos = i;
          break;
        }
      }
      if (insertPos == -1) {
        insertPos = childMidpoints.length;
      }

      // Display the divider at the insert location
      setDividerLocation(insertPos);
    }
  }

  @Override
  void onDragLeave() {
    // Hide the divider and clean up
    setDividerLocation(-1);
  }

  @Override
  boolean onDrop(MockComponent source, int x, int y, int offsetX, int offsetY) {
    if (dividerPos != -1) {
      int dstPos = dividerPos;

      // Hide the divider.
      setDividerLocation(-1);

      // Calculate drop information
      MockContainer srcContainer = source.getContainer();
      MockContainer dstContainer = container;
      if (srcContainer == dstContainer) {
        final int srcPos = srcContainer.getShowingVisibleChildren().indexOf(source);
        if (dstPos > srcPos) {
          // Compensate the insertion index for the removal of the original
          // component from the same container
          dstPos--;
        }
      }

      // Perform drop
      if (srcContainer != null) {
        // Pass false to indicate that the component isn't being permanently deleted.
        // It's just being moved from one container to another.
        srcContainer.removeComponent(source, false);
      }
      dstContainer.addVisibleComponent(source, dstPos);
      return true;
    }
    return false;
  }

  @Override
  void dispose() {
    if (dividerElement != null) {
      DOM.removeChild(container.getRootPanel().getElement(), dividerElement);
    }
  }
}
