// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * Handles resize interactions for the AI chat dialog.
 *
 * <p>Creates invisible resize handles on the right edge (horizontal),
 * bottom edge (vertical), and bottom-right corner (both). The actual
 * dialog is accessed through the {@link ResizeTarget} interface.</p>
 */
public class AIDialogResizeHandler {

  /**
   * Interface for the dialog that can be resized.
   */
  public interface ResizeTarget {
    Element getDialogElement();

    /**
     * Returns the outer container whose size defines the dialog's content
     * area. The resize handler sizes this panel directly.
     */
    Panel getResizableContainer();

    /**
     * Returns the chat scroll panel. The resize handler also sizes this
     * imperatively alongside the outer container so the chat area grows
     * immediately when the user drags — belt-and-suspenders to any
     * flex-layout subtleties inside the dialog body.
     */
    ScrollPanel getChatScrollPanel();

    /**
     * Returns the pixel overhead (all fixed rows + padding) subtracted from
     * the outer container's height to compute the chat scroll panel's
     * height during a drag. Implementations measure this once; the
     * handler re-reads it on every resize in case visibility toggles
     * (debug banner, edit-mode warning, operation preview) changed it.
     */
    int getChatOverheadHeight();
  }

  private static final int RESIZE_HANDLE_SIZE = 12;
  private static final int MIN_DIALOG_WIDTH = 300;

  /**
   * Minimum pixel height for the outer container, sized so the always-visible
   * fixed rows (top bar + chat-scroll border/margin + input + bottom bar +
   * mainPanel padding) plus {@code AIChatDialog.CHAT_MIN_HEIGHT} all fit.
   * Calculation (rough, in px):
   *   topBar        ~34   (title + Rename button with padding)
   *   topBar margin   4
   *   chat border     2   (top+bottom 1px each)
   *   chat margin     6
   *   chat min      120   (CHAT_MIN_HEIGHT)
   *   input handler  80   (3-row textarea + border + padding)
   *   bottom margin   6
   *   bottom bar     32   (buttons row)
   *   main padding   16   (8 top + 8 bottom)
   *   safety         ~20
   *   -----------------
   *   total         ~320
   * Bumped to 400 so the dialog is usable even with the debug banner
   * visible (whose long wrapped warning contributes ~80px) and with a
   * transient row or two on top (edit-mode warning, auto-accept) without
   * clipping the bottom toolbar.
   */
  private static final int MIN_DIALOG_HEIGHT = 400;

  private static final int EDGE_RIGHT = 2;
  private static final int EDGE_BOTTOM = 4;

  private final ResizeTarget target;

  // Resize state
  private int resizeStartX;
  private int resizeStartY;
  private int resizeStartContainerWidth;
  private int resizeStartContainerHeight;
  private int resizeEdge;
  private HandlerRegistration resizePreviewHandler;

  /**
   * Constructs a resize handler for the given target.
   *
   * @param target the dialog to resize
   */
  public AIDialogResizeHandler(ResizeTarget target) {
    this.target = target;
  }

  /**
   * Adds invisible resize handles: right edge, bottom edge, and
   * bottom-right corner.  Must be called after the dialog element is
   * available.
   */
  public void setupResizeHandles() {
    String sz = RESIZE_HANDLE_SIZE + "px";
    Element dialog = target.getDialogElement();

    // Right edge — horizontal resize only
    Element rightEdge = Document.get().createDivElement();
    rightEdge.getStyle().setProperty("position", "absolute");
    rightEdge.getStyle().setProperty("top", "0px");
    rightEdge.getStyle().setProperty("right", "0px");
    rightEdge.getStyle().setProperty("bottom", sz);
    rightEdge.getStyle().setProperty("width", sz);
    rightEdge.getStyle().setProperty("cursor", "e-resize");
    rightEdge.getStyle().setProperty("zIndex", "10");
    dialog.appendChild(rightEdge);
    attachMouseDown(rightEdge, EDGE_RIGHT);

    // Bottom edge — vertical resize only
    Element bottomEdge = Document.get().createDivElement();
    bottomEdge.getStyle().setProperty("position", "absolute");
    bottomEdge.getStyle().setProperty("bottom", "0px");
    bottomEdge.getStyle().setProperty("left", "0px");
    bottomEdge.getStyle().setProperty("right", sz);
    bottomEdge.getStyle().setProperty("height", sz);
    bottomEdge.getStyle().setProperty("cursor", "s-resize");
    bottomEdge.getStyle().setProperty("zIndex", "10");
    dialog.appendChild(bottomEdge);
    attachMouseDown(bottomEdge, EDGE_BOTTOM);

    // Bottom-right corner — both directions
    Element corner = Document.get().createDivElement();
    corner.getStyle().setProperty("position", "absolute");
    corner.getStyle().setProperty("bottom", "0px");
    corner.getStyle().setProperty("right", "0px");
    corner.getStyle().setProperty("width", sz);
    corner.getStyle().setProperty("height", sz);
    corner.getStyle().setProperty("cursor", "se-resize");
    corner.getStyle().setProperty("zIndex", "11");
    dialog.appendChild(corner);
    attachMouseDown(corner, EDGE_RIGHT | EDGE_BOTTOM);
  }

  /**
   * Attaches a native mousedown listener to a resize handle element.
   */
  private native void attachMouseDown(Element el, int edge) /*-{
    var self = this;
    el.addEventListener('mousedown', function(e) {
      e.preventDefault();
      e.stopPropagation();
      self.@com.google.appinventor.client.editor.youngandroid.aiagent.AIDialogResizeHandler::startResize(III)(
          e.clientX, e.clientY, edge);
    });
  }-*/;

  /**
   * Begins a resize operation, capturing mouse events globally.
   */
  private void startResize(int clientX, int clientY, int edge) {
    resizeStartX = clientX;
    resizeStartY = clientY;
    Panel container = target.getResizableContainer();
    resizeStartContainerWidth = container.getOffsetWidth();
    resizeStartContainerHeight = container.getOffsetHeight();
    resizeEdge = edge;

    if (resizePreviewHandler != null) {
      resizePreviewHandler.removeHandler();
    }
    resizePreviewHandler = Event.addNativePreviewHandler(
        new Event.NativePreviewHandler() {
          @Override
          public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
            int type = event.getTypeInt();
            if (type == Event.ONMOUSEMOVE) {
              event.cancel();
              doResize(event.getNativeEvent().getClientX(),
                  event.getNativeEvent().getClientY());
            } else if (type == Event.ONMOUSEUP) {
              event.cancel();
              stopResize();
            }
          }
        });
  }

  /**
   * Processes a mouse-move during an active resize, updating the outer
   * container dimensions. Inner panels (mainPanel via position:absolute
   * inset:0, chatScrollPanel via flex:1, conversationListPanel via
   * position:absolute inset:0) follow automatically. Only right and bottom
   * edges are supported, so the dialog's top-left position never changes.
   */
  private void doResize(int clientX, int clientY) {
    int dx = clientX - resizeStartX;
    int dy = clientY - resizeStartY;

    int newWidth = resizeStartContainerWidth;
    int newHeight = resizeStartContainerHeight;

    if ((resizeEdge & EDGE_RIGHT) != 0) {
      newWidth = Math.max(resizeStartContainerWidth + dx, MIN_DIALOG_WIDTH);
    }
    if ((resizeEdge & EDGE_BOTTOM) != 0) {
      newHeight = Math.max(resizeStartContainerHeight + dy, MIN_DIALOG_HEIGHT);
    }

    target.getResizableContainer().setSize(newWidth + "px", newHeight + "px");
    // Also imperatively size the chat scroll panel so it grows with the
    // drag. The flex layout should theoretically cascade this from the
    // outer container, but in practice GWT's ScrollPanel + nested flex
    // doesn't always reflow on parent resize; sizing it directly is the
    // behaviour that reliably worked pre-refactor.
    int chatHeight = Math.max(newHeight - target.getChatOverheadHeight(),
        /* matches AIChatDialog.CHAT_MIN_HEIGHT */ 120);
    target.getChatScrollPanel().setSize(
        (newWidth - 16) + "px", chatHeight + "px");
  }

  /**
   * Ends the resize operation and removes the global event handler.
   */
  private void stopResize() {
    if (resizePreviewHandler != null) {
      resizePreviewHandler.removeHandler();
      resizePreviewHandler = null;
    }
  }
}
