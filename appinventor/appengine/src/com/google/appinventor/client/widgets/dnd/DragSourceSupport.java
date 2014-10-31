// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.dnd;

import com.google.appinventor.client.output.OdeLog;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides support for dragging from a {@link DragSource}
 * (typically a widget) to a {@link DropTarget}.
 *
 */
public final class DragSourceSupport implements MouseListener {
  /**
   * Interface to functionality provided by the {@link DOM} class.
   * Used as a testing seam.
   *
   */
  // @VisibleForTesting
  static interface IDom {
    public void setCapture(Element elem);
    public void releaseCapture(Element elem);
    public void eventPreventDefaultOfCurrentEvent();
    public com.google.gwt.dom.client.Element getFromElementOfCurrentEvent();
    public com.google.gwt.dom.client.Element getToElementOfCurrentEvent();
  }

  /**
   * Implementation of {@link IDom} that delegates to the real
   * {@link DOM} class.
   *
   */
  private static class RealDom implements IDom {
    private static final RealDom INSTANCE = new RealDom();

    /**
     * Prevent instantiation of static class.
     */
    private RealDom() {
      // nothing
    }

    public void setCapture(Element elem) {
      DOM.setCapture(elem);
    }

    public void releaseCapture(Element elem) {
      DOM.releaseCapture(elem);
    }

    public void eventPreventDefaultOfCurrentEvent() {
      DOM.eventPreventDefault(DOM.eventGetCurrentEvent());
    }

    public com.google.gwt.dom.client.Element getFromElementOfCurrentEvent() {
      return DOM.eventGetCurrentEvent().getFromElement();
    }

    public com.google.gwt.dom.client.Element getToElementOfCurrentEvent() {
      return DOM.eventGetCurrentEvent().getToElement();
    }
  }

  /**
   * This class is used to show a widget while dragging. This could be anything
   * from a simple outline to a copy of the {@code DragSource} widget.
   */
  private static class DragWidgetPopup extends PopupPanel {
    public DragWidgetPopup(Widget w) {
      super(true);
      setWidget(w);
    }
  }

  /**
   * Number of pixels away from the click-point that a drag-source must be
   * dragged to initiate a drag action.
   */
  // @VisibleForTesting
  static final int DRAG_THRESHOLD = 5;

  // Provider of the drag widget and the set of permissible drop targets
  private final DragSource dragSource;
  // DOM implementation
  private final IDom dom;

  // Location (in the drag-widget coordinate system) where the last mouse-down originated.
  // When a drag is in progress, this is the origin of the click that initiated the drag.
  private int startX;
  private int startY;

  private boolean captured;
  private boolean mouseIsDown;
  private boolean dragInProgress;

  // Location (in the drag-widget coordinate system) where the last mouse-move originated
  // while the mouse button was down.
  private int dragX;
  private int dragY;

  // Array of widgets that the drag source widget can be dropped on
  private DropTarget[] dropTargets;

  // Popup containing the widget being shown while dragging
  private DragWidgetPopup dragWidgetPopup;

  // The drop target that the cursor is hovering over currently
  private DropTarget hoverDropTarget;

  /**
   * Creates a new instance of this class to provide support for dragging
   * from the specified drag source to any of the drop targets that it defines.
   * <p>
   * After creation, the caller must add this {@link DragSourceSupport} as
   * a {@link MouseListener} to whatever actual {@link UIObject} will
   * receive drag gestures.
   */
  public DragSourceSupport(DragSource dragSource) {
    this(dragSource, RealDom.INSTANCE);
  }

  // @VisibleForTesting
  DragSourceSupport(DragSource dragSource, IDom dom) {
    this.dragSource = dragSource;
    this.dom = dom;

    startX = -1;
    startY = -1;
    mouseIsDown = false;
    dragInProgress = false;
    dragX = -1;
    dragY = -1;

    dropTargets = null;
    dragWidgetPopup = null;
    hoverDropTarget = null;
  }

  // Private utility methods

  /**
   * Clears any existing selections in the browser.
   * <p>
   * While we are normally trying to avoid falling back to using embedded Javascript, it seems
   * that this cannot currently be done using the GWT APIs.
   */
  private static native void clearSelections() /*-{
    try {
      if ($doc.selection && $doc.selection.empty) {
        $doc.selection.empty();
      } else if ($wnd.getSelection) {
        var sel = $wnd.getSelection();
        if (sel) {
          if (sel.removeAllRanges) {
            sel.removeAllRanges();
          }
          if (sel.collapse) {
            sel.collapse();
          }
        }
      }
    } catch (ignore) {
      // Well, we tried...
    }
  }-*/;

  /**
   * Returns whether the specified widget contains a position given
   * by the absolute coordinates.
   *
   * @param w  widget to test
   * @param absX  absolute x coordinate of position
   * @param absY  absolute y coordinate of position
   * @return  {@code true} if the position is within the widget, {@code false}
   *          otherwise
   */
  private static boolean isInside(Widget w, int absX, int absY) {
    int wx = w.getAbsoluteLeft();
    int wy = w.getAbsoluteTop();
    int ww = w.getOffsetWidth();
    int wh = w.getOffsetHeight();

    return (wx <= absX) && (absX < wx + ww) && (wy <= absY) && (absY < wy + wh);
  }

  // Drag-widget positioning

  /**
   * Configures the specified drag-widget (that will be returned by
   * {@link DragSource#createDragWidget(int, int)}) so that the cursor's hot spot
   * will appear at the point (x,y) in the widget's coordinate system.
   */
  public static void configureDragWidgetToAppearWithCursorAt(Widget w, int x, int y) {
    Element e = w.getElement();
    DOM.setStyleAttribute(e, "position", "absolute");
    DOM.setStyleAttribute(e, "left", -x + "px");
    DOM.setStyleAttribute(e, "top", -y + "px");
  }

  /**
   * Returns the x-coordinate where the cursor appears in the specified
   * drag-widget's coordinate system.
   */
  private static int getDragWidgetOffsetX(Widget w) {
    return -parsePixelValue(DOM.getStyleAttribute(w.getElement(), "left"));
  }

  /**
   * Returns the y-coordinate where the cursor appears in the specified
   * drag-widget's coordinate system.
   */
  private static int getDragWidgetOffsetY(Widget w) {
    return -parsePixelValue(DOM.getStyleAttribute(w.getElement(), "top"));
  }

  private static int parsePixelValue(String pixelValueStr) {
    if ((pixelValueStr != null) && pixelValueStr.endsWith("px")) {
      try {
        return Integer.parseInt(pixelValueStr.substring(0, pixelValueStr.length() - "px".length()));
      } catch (NumberFormatException e) {
        return 0;
      }
    } else {
      return 0;
    }
  }

  // MouseListener implementation

  @Override
  public void onMouseDown(Widget sender, int x, int y) {
    if (mouseIsDown) {
      OdeLog.wlog("received onMouseDown event when we thought the mouse was already down");
    }
    mouseIsDown = true;

    startX = x;
    startY = y;

    if (!captured) {
      // Force browser to keep sending us events until the mouse is released
      dom.setCapture(sender.getElement());
      captured = true;
    }

    // Prevent default actions like image-dragging and text selections from being triggered
    dom.eventPreventDefaultOfCurrentEvent();
    // TODO(user): Consider removing this, since it seems to have
    //                    less effect (at least on Firefox 2) than the line above,
    //                    is more complex, and is browser-dependent.
    DeferredCommand.addCommand(new Command() {
      @Override
      public void execute() {
        clearSelections();
      }
    });
  }

  // NOTE: At least in Firefox 2, if the user drags outside of the browser window,
  //       mouse-move (and even mouse-down) events will not be received until
  //       the user drags back inside the window. A workaround for this issue
  //       exists in the implementation for onMouseLeave().
  @Override
  public void onMouseMove(Widget sender, int x, int y) {
    if (mouseIsDown) {
      dragX = x;
      dragY = y;

      if (dragInProgress) {
        onDragContinue(sender, x, y);
      } else {
        dragInProgress = (manhattanDist(x, y, startX, startY) >= DRAG_THRESHOLD);
        if (dragInProgress) {
          onDragStart(sender, x, y);

          // Check whether we are already hovering over a potential drop target
          onDragContinue(sender, x, y);
        }
      }

      // Prevent default actions from being triggered
      dom.eventPreventDefaultOfCurrentEvent();
    }
  }

  @Override
  public void onMouseUp(Widget sender, int x, int y) {
    if (!mouseIsDown) {
      OdeLog.wlog("received onMouseUp event when we thought the mouse was already up");
    }
    mouseIsDown = false;

    if (captured) {
      // Allow other elements to receive events after the drag/click
      dom.releaseCapture(sender.getElement());
      captured = false;
    }

    if (dragInProgress) {
      onDragEnd(sender, x, y);
    }

    startX = -1;
    startY = -1;
    dragInProgress = false;

    // Prevent default actions from being triggered
    dom.eventPreventDefaultOfCurrentEvent();
  }

  @Override
  public void onMouseEnter(Widget sender) {
    if (dragInProgress) {
      // Firefox 2 specific. IE6 does not need this.
      if (dom.getFromElementOfCurrentEvent() == getDragWidget().getElement() &&
          isRootHtmlElement(dom.getToElementOfCurrentEvent())) {
        // The user moved the mouse outside the browser window.
        //
        // Simulate a mouse-moved event to a position offscreen,
        // since this is not done automatically in Firefox 2.
        onMouseMove(sender,
            /*localX*/ (/*absX*/ -1) - sender.getAbsoluteLeft(),
            /*localY*/ (/*absY*/ -1) - sender.getAbsoluteTop());
        return;
      }
    }
  }

  @Override
  public void onMouseLeave(Widget sender) {
    if (dragInProgress) {
      // Firefox 2 specific. IE6 does not need this.
      if (isRootHtmlElement(dom.getFromElementOfCurrentEvent()) &&
          dom.getToElementOfCurrentEvent() == null) {
        // The user released the mouse button while
        // the mouse was outside the browser window.
        //
        // Simulate a mouse-release event, since this
        // is not done automatically in Firefox 2.
        onMouseUp(sender, dragX, dragY);
        return;
      }
    }
  }

  private static int manhattanDist(int x1, int y1, int x2, int y2) {
    return Math.abs(x1 - x2) + Math.abs(y1 - y2);
  }

  /**
   * Returns whether the specified element is the root HTML element of the web page.
   */
  private static boolean isRootHtmlElement(com.google.gwt.dom.client.Element element) {
    return "html".equalsIgnoreCase(element.getTagName());
  }

  /**
   * Returns the drag widget created by the last call to
   * {@link DragSource#createDragWidget(int, int)}.
   */
  public Widget getDragWidget() {
    return dragWidgetPopup.getWidget();
  }

  // Drag handling

  private void onDragStart(Widget sender, int x, int y) {
    // Notify drag source of the drag starting
    dragSource.onDragStart();

    // Cache the set of permissible drop targets
    dropTargets = dragSource.getDropTargets();

    // Show drag proxy widget
    dragWidgetPopup = new DragWidgetPopup(dragSource.createDragWidget(startX, startY));
    dragWidgetPopup.setPopupPosition(
        /*absX*/ x + sender.getAbsoluteLeft(),
        /*absY*/ y + sender.getAbsoluteTop());
    dragWidgetPopup.show();

    // Initialize hover state
    hoverDropTarget = null;
  }

  private void onDragContinue(Widget sender, int x, int y) {
    int absX = x + sender.getAbsoluteLeft();
    int absY = y + sender.getAbsoluteTop();

    // Move drag proxy to new position
    dragWidgetPopup.setPopupPosition(absX, absY);

    // Find drop target that the cursor is currently hovering over
    for (DropTarget target : dropTargets) {
      Widget targetWidget = target.getDropTargetWidget();
      if (target == sender) {
        // can't drop onto self - only an issue if sender is a container
        continue;
      }

      boolean isInsideTargetWidget = isInside(targetWidget, absX, absY);

      if (target == hoverDropTarget) {
        if (isInsideTargetWidget) {
          // The last identified drop-target "captures" the attention
          // of the drag and drop system while the user is still dragging
          // within its bounds and no other contained drop target accepts the drag
          break;
        } else {
          // Drag has left the bounds of the current hover-target
          hoverDropTarget.onDragLeave(dragSource);
          hoverDropTarget = null;

          // Continue searching for enclosing and non-intersecting
          // drop targets to accept the current drag
          continue;
        }
      }

      if (isInsideTargetWidget) {
        int localX = absX - targetWidget.getAbsoluteLeft();
        int localY = absY - targetWidget.getAbsoluteTop();
        if (target.onDragEnter(dragSource, localX, localY)) {
          if (hoverDropTarget != null) {
            // Drag exits the old hover-target because it has entered
            // the bounds of an accepting drop target that is within
            // the bounds of the old hover-target
            hoverDropTarget.onDragLeave(dragSource);
          }

          // Drag accepted; current target becomes the new hover-target
          hoverDropTarget = target;

          // The guaranteed onDragContinue() event that follows all invocations
          // of onDragEnter() that accept the drag is fired later in this method
          break;
        }
      }
    }

    // Inform the hover-target of the continuing drag
    if (hoverDropTarget != null) {
      Widget targetWidget = hoverDropTarget.getDropTargetWidget();
      hoverDropTarget.onDragContinue(dragSource,
          /*localX*/ absX - targetWidget.getAbsoluteLeft(),
          /*localY*/ absY - targetWidget.getAbsoluteTop());
    }
  }

  private void onDragEnd(Widget sender, int x, int y) {
    // Make sure the current hover-target is still valid,
    // and send the guaranteed onDragContinue() prior to onDrop()
    onDragContinue(sender, x, y);

    // Hide drag widget popup
    dragWidgetPopup.hide();

    // Inform the hover-target of the drop
    if (hoverDropTarget != null) {
      Widget targetWidget = hoverDropTarget.getDropTargetWidget();
      Widget dragWidget = getDragWidget();
      hoverDropTarget.onDrop(dragSource,
          /*localX*/ (/*absX*/ x + sender.getAbsoluteLeft()) - targetWidget.getAbsoluteLeft(),
          /*localY*/ (/*absY*/ y + sender.getAbsoluteTop()) - targetWidget.getAbsoluteTop(),
          getDragWidgetOffsetX(dragWidget),
          getDragWidgetOffsetY(dragWidget));
    }

    // Notify drag source of the drag end
    dragSource.onDragEnd();

    // Clean up
    dropTargets = null;
    dragWidgetPopup = null;
    hoverDropTarget = null;
  }
}
