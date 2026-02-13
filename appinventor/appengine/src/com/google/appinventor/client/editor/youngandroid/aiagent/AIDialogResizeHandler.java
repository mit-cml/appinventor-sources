// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

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
    VerticalPanel getMainPanel();
    ScrollPanel getChatScrollPanel();
  }

  private static final int RESIZE_HANDLE_SIZE = 12;
  private static final int MIN_DIALOG_WIDTH = 300;
  private static final int MIN_SCROLL_HEIGHT = 100;

  private static final int EDGE_RIGHT = 2;
  private static final int EDGE_BOTTOM = 4;

  private final ResizeTarget target;

  // Resize state
  private int resizeStartX;
  private int resizeStartY;
  private int resizeStartPanelWidth;
  private int resizeStartScrollHeight;
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
    resizeStartPanelWidth = target.getMainPanel().getOffsetWidth();
    resizeStartScrollHeight = target.getChatScrollPanel().getOffsetHeight();
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
   * Processes a mouse-move during an active resize, updating panel dimensions.
   * Only right and bottom edges are supported, so the dialog's top-left
   * position never changes.
   */
  private void doResize(int clientX, int clientY) {
    int dx = clientX - resizeStartX;
    int dy = clientY - resizeStartY;

    int newPanelWidth = resizeStartPanelWidth;
    int newScrollHeight = resizeStartScrollHeight;

    if ((resizeEdge & EDGE_RIGHT) != 0) {
      newPanelWidth = Math.max(resizeStartPanelWidth + dx, MIN_DIALOG_WIDTH);
    }
    if ((resizeEdge & EDGE_BOTTOM) != 0) {
      newScrollHeight = Math.max(resizeStartScrollHeight + dy, MIN_SCROLL_HEIGHT);
    }

    target.getMainPanel().setWidth(newPanelWidth + "px");
    target.getChatScrollPanel().setSize(newPanelWidth + "px", newScrollHeight + "px");
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
