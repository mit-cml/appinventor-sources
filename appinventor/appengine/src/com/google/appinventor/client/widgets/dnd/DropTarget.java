// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.dnd;

import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for GWT components that are willing to accept drag sources dropped on them.
 *
 */
public interface DropTarget {
  /**
   * Returns the GWT component associated with this {@code DropTarget}.
   * The location of this widget is used to determine the bounds of the drop region.
   * <p>
   * This method may be called frequently.
   *
   * @return  widget
   */
  public Widget getDropTargetWidget();

  /**
   * Invoked when a drag source that is currently being dragged enters the bounds
   * of this target's widget.
   * <p>
   * Returns {@code true} if the drag source is accepted, {@code false} otherwise.
   * <p>
   * If this method accepts that drag, it is guaranteed that
   * {@link #onDragContinue(DragSource, int, int)} will be invoked immediately
   * afterwards with the same {@code x} and {@code y} arguments.
   *
   * @param source  the drag source that is hovering over this drop target's widget
   * @param x  the x-coordinate of the cursor,
   *           relative to the top-left corner of this drop target's widget
   * @param y  the y-coordinate of the cursor,
   *           relative to the top-left corner of this drop target's widget
   * @return  {@code true} if the source is accepted, {@code false} otherwise
   */
  public boolean onDragEnter(DragSource source, int x, int y);

  /**
   * Invoked while an accepted drag source is being dragged over this target's widget.
   * <p>
   * It is guaranteed that:
   * <ol>
   *   <li> this method will be invoked immediately after an invocation of
   *        {@link #onDragEnter(DragSource, int, int)} that accepts its drag source
   *   <li> this method will be invoked immediately before an invocation of
   *        {@link #onDrop(DragSource, int, int, int, int)}
   * </ol>
   *
   * @param source  the accepted drag source that is hovering over this drop target's widget
   * @param x  the x-coordinate of the cursor,
   *           relative to the top-left corner of this drop target's widget
   * @param y  the y-coordinate of the cursor,
   *           relative to the top-left corner of this drop target's widget
   */
  public void onDragContinue(DragSource source, int x, int y);

  /**
   * Invoked while an accepted drag source has been dragged outside the bounds
   * of this target's widget.
   *
   * @param source  the accepted drag source that is hovering over this drop target's widget
   */
  public void onDragLeave(DragSource source);

  /**
   * Invoked when the user drops an accepted drag source on this target's widget.
   * <p>
   * It is guaranteed that {@link #onDragContinue(DragSource, int, int)} will
   * have been invoked immediately beforehand with the same {@code x} and
   * {@code y} arguments.
   *
   * @param source  the accepted drag source that has been dropped on this drop target's widget
   * @param x  the x-coordinate of the cursor,
   *           relative to the top-left corner of this drop target's widget
   * @param y  the y-coordinate of the cursor,
   *           relative to the top-left corner of this drop target's widget
   * @param offsetX  the x-coordinate of the cursor,
   *                 relative to the top-left corner of the drag-widget
   * @param offsetY  the y-coordinate of the cursor,
   *                 relative to the top-left corner of the drag-widget
   */
  public void onDrop(DragSource source, int x, int y, int offsetX, int offsetY);
}
