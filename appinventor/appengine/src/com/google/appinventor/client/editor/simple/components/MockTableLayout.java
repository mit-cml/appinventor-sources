// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.output.OdeLog;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import java.util.Arrays;
import java.util.Map;

/**
 * A layout that arranges its children in a grid (with variable sized rows and
 * columns).
 * <p>
 * If multiple children claim to be in the same cell, the last child in the
 * children list wins.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
final class MockTableLayout extends MockLayout {
  private static class Cell {
    int row;
    int col;

    private Cell(int row, int col) {
      this.row = row;
      this.col = col;
    }
  };

  private class TableLayoutInfo extends LayoutInfo {
    MockComponent[][] cellChildren;

    TableLayoutInfo(Map<MockComponent, LayoutInfo> layoutInfoMap, MockComponent table) {
      super(layoutInfoMap, table);

      cellChildren = new MockComponent[nrows][ncols];
    }

    @Override
    protected void prepareToGatherDimensions() {
      prepareForLayout(this);
      super.prepareToGatherDimensions();
    }

    @Override
    int calculateAutomaticWidth() {
      return colLefts[ncols - 1] + colWidths[ncols - 1];
    }

    @Override
    int calculateAutomaticHeight() {
      return rowTops[nrows - 1] + rowHeights[nrows - 1];
    }

    @Override
    void cleanUp() {
      super.cleanUp();
      cellChildren = null;
    }
  }

  private static final int DEFAULT_NUM_ROWS = 2;
  private static final int DEFAULT_NUM_COLUMNS = 2;

  // The color of the drop-target area's border
  private static final String DROP_TARGET_AREA_COLOR = "#0000ff";

  // The preferred width and height of empty cells in the UI designer
  private static final int EMPTY_COL_WIDTH = 50;
  private static final int EMPTY_ROW_HEIGHT = 50;

  private int nrows;
  private int ncols;

  // Position and size of each row and column in the table;
  // calculated in layoutContainer; used to calculate the correct
  // drop location for a component dropped onto this layout's container.
  private int[] rowTops;
  private int[] rowHeights;
  private int[] colLefts;
  private int[] colWidths;

  // The DIV element that displays the drop-target area.
  private Element dropTargetArea; // lazily initialized

  // The cell onto which the currently hovering component will be dropped onto.
  private Cell dropTargetCell;

  /**
   * Creates a new table layout.
   */
  MockTableLayout() {
    nrows = DEFAULT_NUM_ROWS;
    ncols = DEFAULT_NUM_COLUMNS;

    // Allocate the arrays for position and size of rows and columns now.
    // We only need to reallocate them if the number of rows/columns changes.
    rowTops = new int[nrows];
    rowHeights = new int[nrows];
    colLefts = new int[ncols];
    colWidths = new int[ncols];

    // Initialize layoutHeight and layoutWidth because they are used in getPreferredHeight and
    // getPreferredWidth.
    layoutHeight = nrows * EMPTY_ROW_HEIGHT;
    layoutWidth = ncols * EMPTY_COL_WIDTH;
  }

  public void setRows(int nrows) {
    this.nrows = nrows;
    rowTops = new int[nrows];
    rowHeights = new int[nrows];
    container.refreshForm();
  }

  public void setColumns(int ncols) {
    this.ncols = ncols;
    colLefts = new int[ncols];
    colWidths = new int[ncols];
    container.refreshForm();
  }

  // Drop target area

  private void ensureDropTargetArea() {
    if (dropTargetArea == null) {
      dropTargetArea = DOM.createDiv();
      setDropTargetAreaVisible(false);
      DOM.setStyleAttribute(dropTargetArea, "border", "2px solid " + DROP_TARGET_AREA_COLOR);
      DOM.appendChild(container.getRootPanel().getElement(), dropTargetArea);
    }
  }

  private void setDropTargetCell(Cell cell) {
    dropTargetCell = cell;
    if (dropTargetCell != null) {
      // Display the drop target area at the cell that the user is hovering over.
      setDropTargetAreaBoundsAndShow(
          colLefts[dropTargetCell.col], rowTops[dropTargetCell.row],
          colWidths[dropTargetCell.col], rowHeights[dropTargetCell.row]);
    } else {
      setDropTargetAreaVisible(false);
    }
  }

  private void setDropTargetAreaVisible(boolean visible) {
    DOM.setStyleAttribute(dropTargetArea, "visibility", visible ? "visible" : "hidden");
  }

  private void setDropTargetAreaBoundsAndShow(int x, int y, int width, int height) {
    DOM.setStyleAttribute(dropTargetArea, "position", "absolute");
    DOM.setStyleAttribute(dropTargetArea, "left", x + "px");
    DOM.setStyleAttribute(dropTargetArea, "top", y + "px");
    DOM.setStyleAttribute(dropTargetArea, "width", width + "px");
    DOM.setStyleAttribute(dropTargetArea, "height", height + "px");
    setDropTargetAreaVisible(true);
  }

  // Table information

  private Cell getCellOfChild(MockComponent child) {
    String rowString = child.getPropertyValue(MockVisibleComponent.PROPERTY_NAME_ROW);
    String colString = child.getPropertyValue(MockVisibleComponent.PROPERTY_NAME_COLUMN);
    return new Cell(Integer.parseInt(rowString), Integer.parseInt(colString));
  }

  /**
   * Returns the cell that contains the specified point,
   * or {@code null} if the point is out of bounds.
   */
  private Cell getCellContainingPoint(int x, int y) {
    if (x < 0 || y < 0) {
      return null;
    }

    int cellRow = -1;
    int rowY = 0;
    for (int row = 0; row < nrows; row++) {
      rowY += rowHeights[row];
      if (y < rowY) {
        cellRow = row;
        break;
      }
    }
    if (cellRow == -1) {
      return null;
    }

    int cellCol = -1;
    int colX = 0;
    for (int col = 0; col < ncols; col++) {
      colX += colWidths[col];
      if (x < colX) {
        cellCol = col;
        break;
      }
    }
    if (cellCol == -1) {
      return null;
    }

    return new Cell(cellRow, cellCol);
  }

  // MockLayout methods

  // NOTE(lizlooney) - layout behavior:

  // In a TableArrangement, components are arranged in a grid of rows and columns, with not more
  // than one component visible in each cell. If multiple components occupy the same cell, only the
  // last one will be visible. Within each row, components are vertically center-aligned.
  // The width of a column is determined by the widest component in that column. When calculating
  // column width, the automatic width is used for components whose Width property is set to Fill
  // Parent. However, each component will always fill the full width of the column that it occupies.
  // The height of a row is determined by the tallest component in that row whose Height property
  // is not set to Fill Parent. If a row contains only components whose Height properties are set to
  // Fill Parent, the height of the row is calculated using the automatic heights of the components.

  @Override
  LayoutInfo createContainerLayoutInfo(Map<MockComponent, LayoutInfo> layoutInfoMap) {
    ensureDropTargetArea();
    return new TableLayoutInfo(layoutInfoMap, container);
  }

  private void prepareForLayout(TableLayoutInfo tableLayoutInfo) {
    // Figure out which child (if any) will be in each cell.
    // If multiple children claim to be in the same cell, only the last child in the children list
    // will be visible.

    MockForm form = container.getForm();

    for (MockComponent child : tableLayoutInfo.visibleChildren) {
      Cell cell = getCellOfChild(child);
      if (cell.row >= nrows || cell.col >= ncols) {
        // This child has an invalid cell. This can happen if the user changes the table's
        // dimensions after they've already dragged a component into a cell. It's not a big deal.
        // We just hide the child. The hidden component will still show up in the Components list.
        child.setVisible(false);
        continue;
      }

      if (tableLayoutInfo.cellChildren[cell.row][cell.col] != null) {
        // A previous child claimed to be in this cell.
        // Since the rule is that the last child in the children list wins, we hide the previous
        // child. The hidden component will still show up in the Components list.
        MockComponent previousCellChild = tableLayoutInfo.cellChildren[cell.row][cell.col];
        previousCellChild.setVisible(false);
      }

      tableLayoutInfo.cellChildren[cell.row][cell.col] = child;
    }

    // Figure out the column widths and row heights, using the automatic widths for all children
    // whose width is fill parent and ignoring any child's height that is fill parent.
    Arrays.fill(colWidths, 0);
    Arrays.fill(rowHeights, 0);
    boolean[] colEmpty = new boolean[ncols];
    boolean[] rowEmpty = new boolean[nrows];
    boolean[] rowAllFillParent = new boolean[nrows];
    Arrays.fill(colEmpty, true);
    Arrays.fill(rowEmpty, true);
    Arrays.fill(rowAllFillParent, true);
    for (int row = 0; row < nrows; row++) {
      for (int col = 0; col < ncols; col++) {
        MockComponent cellChild = tableLayoutInfo.cellChildren[row][col];
        if (cellChild != null) {
          LayoutInfo childLayoutInfo = tableLayoutInfo.layoutInfoMap.get(cellChild);
          colEmpty[col] = false;
          rowEmpty[row] = false;

          // Use automatic width for children whose width is fill parent.
          // or if our width is automatic or fill parent

          int childWidth = childLayoutInfo.width;
          if (childLayoutInfo.width == MockVisibleComponent.LENGTH_FILL_PARENT)
            childWidth = childLayoutInfo.calculateAutomaticWidth();
          else if (childLayoutInfo.width <= MockVisibleComponent.LENGTH_PERCENT_TAG) {
            // If childWidth is a percent tag... do it
            childWidth = (- (childWidth - MockVisibleComponent.LENGTH_PERCENT_TAG)) * form.screenWidth /100;
            childLayoutInfo.width = childWidth; // Side effect it...
            OdeLog.log("MockTableLayout: form.screenWidth = " + form.screenWidth + " childWidth = " + childWidth);
          }

          // int childWidth = (childLayoutInfo.width == MockVisibleComponent.LENGTH_FILL_PARENT)
          //     ? childLayoutInfo.calculateAutomaticWidth()
          //     : childLayoutInfo.width;

          colWidths[col] = Math.max(colWidths[col], childWidth + BORDER_SIZE);

          // Ignore child's height if it is fill parent.
          if (childLayoutInfo.height != MockVisibleComponent.LENGTH_FILL_PARENT) {

            int childHeight = childLayoutInfo.height;
            if (childHeight <= MockVisibleComponent.LENGTH_PERCENT_TAG) {
              childHeight = (- (childHeight - MockVisibleComponent.LENGTH_PERCENT_TAG) * form.usableScreenHeight) / 100;
              childLayoutInfo.height = childHeight; // Side effect it...
              rowAllFillParent[row] = false;
              OdeLog.log("MockTableLayout: form.usableScreenHeight = " + form.usableScreenHeight + " childHeight = " + childHeight);
              rowHeights[row] = Math.max(rowHeights[row], childHeight + BORDER_SIZE);
            }

            if ((childHeight != MockVisibleComponent.LENGTH_FILL_PARENT)
              && (childHeight > MockVisibleComponent.LENGTH_PERCENT_TAG)) {
              rowAllFillParent[row] = false;
              rowHeights[row] = Math.max(rowHeights[row], childHeight + BORDER_SIZE);
            }

          }
        }
      }
    }
    // If any row is completely empty then its height in the designer should be EMPTY_ROW_HEIGHT.
    // If any row contains only children with height set to fill parent, recalculate the row height
    // using the automatic heights of the children.
    for (int row = 0; row < nrows; row++) {
      if (rowEmpty[row]) {
        rowHeights[row] = EMPTY_ROW_HEIGHT;
      } else if (rowAllFillParent[row]) {
        for (int col = 0; col < ncols; col++) {
          MockComponent cellChild = tableLayoutInfo.cellChildren[row][col];
          if (cellChild != null) {
            LayoutInfo childLayoutInfo = tableLayoutInfo.layoutInfoMap.get(cellChild);
            int childHeight = childLayoutInfo.calculateAutomaticHeight();
            rowHeights[row] = Math.max(rowHeights[row], childHeight + BORDER_SIZE);
          }
        }
      }
    }
    // If any column is completely empty, then its width in the designer should be EMPTY_COL_WIDTH.
    for (int col = 0; col < ncols; col++) {
      if (colEmpty[col]) {
        colWidths[col] = EMPTY_COL_WIDTH;
      }
    }

    // Set all childrens' widths to the column width.
    // This matches what happens on an Android device.
    for (int row = 0; row < nrows; row++) {
      for (int col = 0; col < ncols; col++) {
        MockComponent cellChild = tableLayoutInfo.cellChildren[row][col];
        if (cellChild != null) {
          LayoutInfo childLayoutInfo = tableLayoutInfo.layoutInfoMap.get(cellChild);
          childLayoutInfo.width = colWidths[col] - BORDER_SIZE;
          if (childLayoutInfo.height == MockVisibleComponent.LENGTH_FILL_PARENT) {
            childLayoutInfo.height = rowHeights[row] - BORDER_SIZE;
          }
        }
      }
    }

    // Figure out the top coordinate of each row.
    for (int row = 0, y = 0; row < nrows; y += rowHeights[row++]) {
      rowTops[row] = y;
    }

    // Figure out the left coordinate of each column.
    for (int col = 0, x = 0; col < ncols; x += colWidths[col++]) {
      colLefts[col] = x;
    }
  }

  @Override
  void layoutChildren(LayoutInfo containerLayoutInfo) {
    TableLayoutInfo tableLayoutInfo = (TableLayoutInfo) containerLayoutInfo;

    // Call layoutChildren for children that are containers.
    // Position the children.
    for (int row = 0; row < nrows; row++) {
      // Within each row, components are vertically center-aligned.
      int centerY = rowTops[row] + rowHeights[row] / 2;
      for (int col = 0; col < ncols; col++) {
        MockComponent cellChild = tableLayoutInfo.cellChildren[row][col];
        if (cellChild != null) {
          LayoutInfo childLayoutInfo = tableLayoutInfo.layoutInfoMap.get(cellChild);
          // If the cell child is a container call layoutChildren for it.
          if (cellChild instanceof MockContainer) {
            ((MockContainer) cellChild).getLayout().layoutChildren(childLayoutInfo);
          }
          int childHeightWithBorder = childLayoutInfo.height + BORDER_SIZE;
          int y = centerY - (childHeightWithBorder / 2);
          container.setChildSizeAndPosition(cellChild, childLayoutInfo, colLefts[col], y);
        }
      }
    }

    // Update layoutWidth and layoutHeight.
    layoutHeight = rowTops[nrows - 1] + rowHeights[nrows - 1];
    layoutWidth = colLefts[ncols - 1] + colWidths[ncols - 1];
    OdeLog.log("MockTableLayout: setting layoutHeight = " + layoutHeight + " setting layoutWidth = " + layoutWidth);
  }

  @Override
  void onDragContinue(int x, int y) {
    // Find the cell the user is hovering over.
    setDropTargetCell(getCellContainingPoint(x, y));
  }

  @Override
  void onDragLeave() {
    // Hide the drop target area and clean up.
    setDropTargetCell(null);
  }

  @Override
  boolean onDrop(MockComponent source, int x, int y, int offsetX, int offsetY) {
    if (dropTargetCell != null) {
      Cell destCell = dropTargetCell;

      // Hide the drop target area and clean up.
      setDropTargetCell(null);

      // Perform drop.
      MockContainer srcContainer = source.getContainer();
      if (srcContainer != null) {
        // Pass false to indicate that the component isn't being permanently deleted.
        // It's just being moved from one container to another.
        srcContainer.removeComponent(source, false);
      }
      source.changeProperty(MockVisibleComponent.PROPERTY_NAME_COLUMN, "" + destCell.col);
      source.changeProperty(MockVisibleComponent.PROPERTY_NAME_ROW, "" + destCell.row);
      container.addComponent(source);
      return true;
    }
    return false;
  }

  @Override
  void dispose() {
    if (dropTargetArea != null) {
      DOM.removeChild(container.getRootPanel().getElement(), dropTargetArea);
    }
  }
}
