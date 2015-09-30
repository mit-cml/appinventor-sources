// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import java.util.Map;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;



/**
 * A base class for layouts that arrange the children of a container in a single
 * column or a single row.
 *
 * @author markf@google.com (Mark Friedman)
 * @author sharon@google.com (Sharon Perl)
 * @author lizlooney@google.com (Liz Looney)
 * @author hal@mit.edu (Hal Abelson)
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

  // constants to indicate horizontal and vertical alignment
  private enum HorizontalAlignment {Left, Center, Right};
  private HorizontalAlignment alignH;

  private enum VerticalAlignment {Top, Center, Bottom};
  private VerticalAlignment alignV;

  private enum Dim { HEIGHT, WIDTH };

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

    // These initial values are assuming that the default values in ComponentConstants are
    // defined as LEFT and TOP
    alignH = HorizontalAlignment.Left;
    alignV = VerticalAlignment.Top;
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
  // center-aligned, even if the vertical aligned is "top", in which case they are
  // center-aligned at the top of the arrangement.  This is unlike VerticalArrangement, where
  // the components are flush-left at the left of the arrangement if the horizontal alignment
  // is "left".

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
      int childHeight = childLayoutInfo.height;
      if (childHeight == MockVisibleComponent.LENGTH_FILL_PARENT) {
        childHeight = childLayoutInfo.calculateAutomaticHeight();
      } else if (childHeight <= MockVisibleComponent.LENGTH_PERCENT_TAG) {
        childHeight = convertFromPercent(childHeight, Dim.HEIGHT);
      }
      height += childHeight + BORDER_SIZE;
    }
    height += COMPONENT_SPACING;
    return height;
  }

  private void layoutVertical(LayoutInfo containerLayoutInfo) {
    // Components are arranged along the vertical axis.  They are left-justified and
    // at the top of the arrangement by default, but this can be changed with the
    // contentCentering property.  Note that the screen itself does a vertical layout

    // Calculate the height used up by children whose height is not fill parent.
    // NOTE(halabelson) Here and in calculateAutomaticHeightVertical, we're putting
    // COMPONENT_SPACING above the first child and below the last child.  We probably don't
    // really want to do this, but need to think about it.
    int usedHeight = 0;
    int countFillParent = 0;
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      usedHeight += COMPONENT_SPACING;
      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      int childHeight = childLayoutInfo.height;
      if (childHeight < MockVisibleComponent.LENGTH_PERCENT_TAG)
        childHeight = convertFromPercent(childHeight, Dim.HEIGHT);
      if (childHeight == MockVisibleComponent.LENGTH_FILL_PARENT) {
        countFillParent++;
      } else {
        usedHeight += childHeight + BORDER_SIZE;
      }
    }
    usedHeight += COMPONENT_SPACING;

    // The remaining height, after allocating vertical space for the contents
    int remainingHeight = containerLayoutInfo.height - usedHeight;
    if (remainingHeight < 0) {
      remainingHeight = 0;
    }

    // The final remaining height, which will be zero if any of the components have height
    // fill-parent
   int finalRemainingHeight = remainingHeight;

    // Resolve any child's width or height that is fill-parent, and call layoutChildren for
    // children that are containers.
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      if (childLayoutInfo.width == MockVisibleComponent.LENGTH_FILL_PARENT) {
        childLayoutInfo.width = containerLayoutInfo.width - BORDER_SIZE;
      }
      if (childLayoutInfo.width <= MockVisibleComponent.LENGTH_PERCENT_TAG) {
        childLayoutInfo.width = convertFromPercent(childLayoutInfo.width, Dim.WIDTH);
      }
      if (childLayoutInfo.height == MockVisibleComponent.LENGTH_FILL_PARENT) {
        childLayoutInfo.height = remainingHeight / countFillParent - BORDER_SIZE;
        // if any component has height fill-parent then there's no remaining height
        finalRemainingHeight = 0;
      }
      if (childLayoutInfo.height <= MockVisibleComponent.LENGTH_PERCENT_TAG) {
        childLayoutInfo.height = convertFromPercent(childLayoutInfo.height, Dim.HEIGHT);
      }

      // If the child is a container call layoutChildren for it.
      if (child instanceof MockContainer) {
        ((MockContainer) child).getLayout().layoutChildren(childLayoutInfo);
      }
    }

    // topY is where the top of each component goes.  It starts out either at zero, or offset
    // so the entire stack is vertically centered in the arrangement (i.e., offset by
    // finalRemainingHeight/2) or offset so that the bottom of the stack of components is at
    // the bottom of the layout (i.e., offset by finalRemainingHeight).
    int topY = 0;

    switch (alignV) {
    case Top:
      topY = 0;
      break;
    case Center:
      topY = finalRemainingHeight / 2;
      break;
    case Bottom:
      topY = finalRemainingHeight;
      break;
    default:
      OdeLog.elog("System error: Bad value for vertical alignment -- MockHVLayoutBase");
    }

    int index = 0;

    // Position the children and update layoutWidth and layoutHeight.

    // NOTE(halabelson)  What is this for?
    layoutWidth = 0;

    // iterate through the children, setting the leftX and topY positions

    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      dividerLocations[index] = topY;
      topY += COMPONENT_SPACING;

      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      int childWidthWithBorder = childLayoutInfo.width + BORDER_SIZE;
      int childHeightWithBorder = childLayoutInfo.height + BORDER_SIZE;

      // leftX is where the left edge of the child should be.  For a vertical alignment
      // it's either zero (left align) or set so the center of child is at the centered
      // (center-align) or set so the right edge of the child is at the right edge of the
      // layout (right-align).
      int leftX = 0;
      switch (alignH) {
      case Left:
        leftX = 0;
        break;
      case Center:
        leftX = (containerLayoutInfo.width - childWidthWithBorder) / 2 ;
        break;
      case Right:
        leftX = containerLayoutInfo.width - childWidthWithBorder;
        break;
      default:
        OdeLog.elog("System error: Bad value for horizontal alignment -- MockHVLayoutBase");
      }

      container.setChildSizeAndPosition(child, childLayoutInfo, leftX, topY);
      childMidpoints[index] = topY + (childHeightWithBorder / 2);
      topY += childHeightWithBorder;
      index++;
    }
    dividerLocations[index] = topY;
    topY += COMPONENT_SPACING;
    layoutHeight = topY;
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

  // TODO: (hal)  This next method is incorrect because the width need to be constrained by
  // the room remaining in the parent container.  The overall automatic layout algorithms
  // need to be reviewed.

  private int calculateAutomaticWidthHorizontal(LayoutInfo containerLayoutInfo) {
    // The width will be the sum of the child widths.
    int width = 0;
    boolean firstChild = true;
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      // add spacing, but not before first child
      if (firstChild) {
        firstChild = false;
      } else {
        width += COMPONENT_SPACING;
      }
      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      // If the width is fill parent, use automatic width.
      int childWidth = childLayoutInfo.width;
      if (childWidth <= MockVisibleComponent.LENGTH_PERCENT_TAG)
        childWidth = convertFromPercent(childWidth, Dim.WIDTH);
      childWidth = (childLayoutInfo.width == MockVisibleComponent.LENGTH_FILL_PARENT)
        ? childLayoutInfo.calculateAutomaticWidth()
        : childWidth;
      width += childWidth + BORDER_SIZE;
    }
    return width;
  }

  // TODO(hal): Check this to see if we're putting the component vertical spacing in the right
  // places and think about rewriting this to match how layoutHorizontal handles width.

  private void layoutHorizontal(LayoutInfo containerLayoutInfo) {
    // Children are arranged along the horizontal axis.   They are
    // and vertically center-aligned at the top of the arrangement by default, and at vertical
    // center of the arrangement if vertical contentCentering is specified.
    // Horizontally, they can be left-aligned, centered, or right-aligned.
    int usedWidth = 0;
    boolean firstChild = true;
    int countFillParent = 0;

    // Calculate the width used up by children whose width is not fill parent.
    // Include spacing between the children
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      // add spacing, but not before first child
      if (firstChild) {
        firstChild = false;
      } else {
        usedWidth += COMPONENT_SPACING;
      }
      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      if (childLayoutInfo.width <= MockVisibleComponent.LENGTH_PERCENT_TAG)
        childLayoutInfo.width = convertFromPercent(childLayoutInfo.width, Dim.WIDTH);
      if (childLayoutInfo.width == MockVisibleComponent.LENGTH_FILL_PARENT) {
        countFillParent++;
      } else {
        usedWidth += childLayoutInfo.width + BORDER_SIZE;
      }
    }

    // The remaining width, after allocating horizontal space for the
    // contents, not counting the width of fill-parent children
    // If there are fill-parent children, this remaining width will be
    // divided equally among them.
    int remainingWidth = containerLayoutInfo.width - usedWidth;
    if (remainingWidth < 0) {
      remainingWidth = 0;
    }

    // The final remaining width after all children have been accounted for.  This will be 0 if
    // any of the children have width fill-parent
    int finalRemainingWidth = remainingWidth;

    // Resolve any child's width or height that is fill parent, and call layoutChildren for
    // children that are containers.
    // Figure out the height of the largest child so we can middle-align all the children.
    // Note that layoutVertical does not do the analogous center aligning unless horizontal
    // centering is explicitly specified.
    int maxHeight = 0;
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      if (childLayoutInfo.width == MockVisibleComponent.LENGTH_FILL_PARENT) {
        // at this point, remaining width is too small and gets smaller if there
        // are more components
        childLayoutInfo.width = remainingWidth / countFillParent - BORDER_SIZE;
        // if any component has width fill parent then there will be no final remaining width
        finalRemainingWidth = 0;
      }

      if (childLayoutInfo.height == MockVisibleComponent.LENGTH_FILL_PARENT) {
        childLayoutInfo.height = containerLayoutInfo.height - BORDER_SIZE;
      }

      if (childLayoutInfo.height <= MockVisibleComponent.LENGTH_PERCENT_TAG)
        childLayoutInfo.height = convertFromPercent(childLayoutInfo.height, Dim.HEIGHT);

      maxHeight = Math.max(maxHeight, childLayoutInfo.height + BORDER_SIZE);

      // If the child is a container then call layoutChildren for it.
      if (child instanceof MockContainer) {
        ((MockContainer) child).getLayout().layoutChildren(childLayoutInfo);
      }
    }

    // NOTE(hal) What is this for?
    layoutHeight = 0;

    // Now we've computed the actual widths of the components, so we can lay them out.

    // leftX is where the left edge of the next child should be.  For a horizontal alignment
    // leftX starts out either at zero (left align) or set so the center of the arrangement
    // is at the center of the layout (center-align) or set so the right edge of the arrangement
    // child is at the right edge of the layout (right-align).
    int leftX = 0;
    switch (alignH) {
    case Left:
      leftX = 0;
      break;
    case Center:
      leftX = finalRemainingWidth / 2;
      break;
    case Right:
      leftX = finalRemainingWidth;
      break;
    default:
      OdeLog.elog("System error: Bad value for horizontal justification -- MockHVLayoutBase");
    }

    // Position each child and update layoutWidth and layoutHeight.
    int index = 0;
    firstChild = true;
    for (MockComponent child : containerLayoutInfo.visibleChildren) {
      // add spacing, but not before first child
      if (firstChild) {
        firstChild = false;
      } else {
        leftX += COMPONENT_SPACING;
      }
      dividerLocations[index] = leftX;
      LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
      int childWidthWithBorder = childLayoutInfo.width + BORDER_SIZE;
      int childHeightWithBorder = childLayoutInfo.height + BORDER_SIZE;

      // topY is where the top of each component goes.  It starts out either at zero, or offset
      // so the entire stack is vertically centered in the arrangement (i.e., offset by
      // (containerLayoutInfo.height / 2) - (childHeightWithBorder / 2)) or offset so that
      // the bottom of the stack of components is at the bottom of the layout
      // (i.e., offset by containerLayoutInfo.height - childHeightWithBorder).
      int topY = 0;

      switch (alignV) {
        case Top:
          topY = 0;
          break;
        case Center:
          topY = (containerLayoutInfo.height / 2) - (childHeightWithBorder / 2);
          break;
        case Bottom:
          topY = containerLayoutInfo.height - childHeightWithBorder;
        default:
          OdeLog.elog("System error: Bad value for vertical alignment -- MockHVLayoutBase");
      }

      container.setChildSizeAndPosition(child, childLayoutInfo, leftX, topY);
      layoutHeight = Math.max(layoutHeight, topY + childHeightWithBorder);
      childMidpoints[index] = leftX + (childWidthWithBorder / 2);
      leftX += childWidthWithBorder;
      index++;
    }
    dividerLocations[index] = leftX;
    layoutWidth = leftX;
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

  /**
   * Set the layout flags centerH and centerV that govern whether the layout performs
   * horizontal or vertical centering.   Called by the arrangement that uses this layout
   * @param centering is the string value of the centering property "0", "1", "2", or "3"
   */


  public void setHAlignmentFlags(String alignment) {
    try {
      switch (Integer.parseInt(alignment)) {
      case ComponentConstants.GRAVITY_LEFT:
        alignH = HorizontalAlignment.Left;
        break;
      case ComponentConstants.GRAVITY_CENTER_HORIZONTAL:
        alignH = HorizontalAlignment.Center;
        break;
      case ComponentConstants.GRAVITY_RIGHT:
        alignH = HorizontalAlignment.Right;
        break;
      default:
        // This error should not happen because the higher level
        // setter for HorizontalAlignment should screen out illegal inputs.
        ErrorReporter.reportError(MESSAGES.badValueForHorizontalAlignment(alignment));
      }
    } catch (NumberFormatException e) {
      // As above, this error should not happen
      ErrorReporter.reportError(MESSAGES.badValueForHorizontalAlignment(alignment));
    }
  }

  public void setVAlignmentFlags(String alignment) {
    try {
      switch (Integer.parseInt(alignment)) {
      case ComponentConstants.GRAVITY_TOP:
        alignV = VerticalAlignment.Top;
        break;
      case ComponentConstants.GRAVITY_CENTER_VERTICAL:
        alignV = VerticalAlignment.Center;
        break;
      case ComponentConstants.GRAVITY_BOTTOM:
        alignV = VerticalAlignment.Bottom;
        break;
      default:
        // This error should not happen because the higher level
        // setter for VerticalAlignment should screen out illegal inputs.
        ErrorReporter.reportError(MESSAGES.badValueForVerticalAlignment(alignment));
      }
    } catch (NumberFormatException e) {
      // As above, this error should not happen
      ErrorReporter.reportError(MESSAGES.badValueForVerticalAlignment(alignment));
    }
  }

  private int convertFromPercent(int childLength, Dim dim) {
    MockForm form = container.getForm();
    int parentLength;
    if (dim == Dim.WIDTH) {
      parentLength = form.screenWidth;
    } else {
      parentLength = form.usableScreenHeight;
    }

    if (childLength > MockVisibleComponent.LENGTH_PERCENT_TAG)
      return childLength;       // Shouldn't happen

    childLength = parentLength * (- (childLength - MockVisibleComponent.LENGTH_PERCENT_TAG)) / 100;
    return childLength;
  }

}
