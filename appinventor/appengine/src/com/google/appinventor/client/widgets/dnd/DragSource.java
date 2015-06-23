// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.dnd;

import com.google.gwt.user.client.ui.Widget;

/**
 * Describes an area in which drag gestures can initiate a drag-and-drop action.
 *
 */
public interface DragSource {
  /**
   * Invoked when a drag-gesture is detected on this drag source
   * that initiates a drag-and-drop action.
   */
  public void onDragStart();

  /**
   * Returns a new widget that will be shown while a drag is in progress.
   * <p>
   * This widget is owned by the caller and the callee should not modify
   * it or embed it anywhere else in the UI.
   * <p>
   * The widget will be positioned relative to the cursor based on its
   * CSS positional attributes. For example, if the returned widget has
   * the properties {@code position: absolute; left: 5px; top: 10px}, the
   * widget will be positioned so that the hot spot of the cursor is
   * 5px to the left of and 10px to the top of the top-left corner of the widget.
   * It is recommended that implementors use the
   * {@link DragSourceSupport#configureDragWidgetToAppearWithCursorAt(Widget, int, int)}
   * method to configure where the widget will appear relative to the cursor.
   * <p>
   * This will be called once for every start of a drag gesture.
   *
   * @param x  the x position of the cursor in the drag source's coordinate system
   *           where the mouse-down event that initiated the drag occurred;
   *           not necessarily the same as the <i>current</i> location of the cursor
   * @param y  the y position of the cursor in the drag source's coordinate system
   *           where the mouse-down event that initiated the drag occurred;
   *           not necessarily the same as the <i>current</i> location of the cursor
   * @return  widget to be shown while dragging;
   *          owned by the caller
   */
  public Widget createDragWidget(int x, int y);

  /**
   * Returns the widget that is being dragged.
   */
  public Widget getDragWidget();

  /**
   * Returns all possible drop targets for this drag source.
   * <p>
   * The returned targets will be checked in order to determine a valid drop
   * target; therefore the order of the returned targets is significant if
   * multiple targets overlap. In particular, overlapping sets of targets must
   * be specified in depth first order.
   * <p>
   * This will be called once for every start of a drag gesture.
   *
   * @return  array of drop targets
   */
  public DropTarget[] getDropTargets();

  /**
   * Invoked when a drag-gesture has ended, regardless of whether a drop
   * actually occurred.
   * <p>
   * If a drop has actually occurred and
   * {@link DropTarget#onDrop(DragSource, int, int, int, int)} is called,
   * the invocation of this method will immediately follow it.
   */
  public void onDragEnd();
}
