// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Abstract base class for collapsible category panels used in the palette and properties panel.
 *
 * <p>Subclasses set their own CSS primary names and populate the header, then call
 * {@link #finishInit()} at the end of their constructor to attach header and content to the panel.
 */
public abstract class AbstractCollapsiblePanel extends FlowPanel {
  private static int idCounter = 0;

  protected final FlowPanel header;
  protected final FlowPanel content;
  private boolean isOpen;

  /**
   * Sets up the shared header/content structure, ARIA attributes, and keyboard/click handlers.
   * Subclasses must call {@link #finishInit()} after completing their own construction.
   */
  protected AbstractCollapsiblePanel() {
    String contentId = "ode-collapsible-" + (idCounter++);

    header = new FlowPanel();
    header.getElement().setAttribute("role", "button");
    header.getElement().setAttribute("tabindex", "0");
    header.getElement().setAttribute("aria-expanded", "false");
    header.getElement().setAttribute("aria-controls", contentId);

    content = new FlowPanel();
    content.getElement().setId(contentId);

    header.addDomHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        setOpen(!isOpen);
      }
    }, ClickEvent.getType());

    header.addDomHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        int key = event.getNativeKeyCode();
        if (key == KeyCodes.KEY_ENTER || key == 32 /* SPACE */) {
          event.preventDefault();
          setOpen(!isOpen);
        }
      }
    }, KeyDownEvent.getType());
  }

  /**
   * Attaches header and content to the panel. Call at the end of the subclass constructor,
   * after setting style names and populating the header.
   */
  protected final void finishInit() {
    add(header);
    add(content);
  }

  /**
   * Hook for subclasses to react to open/close state changes (e.g. rotating a disclosure icon).
   *
   * @param open true if the panel is now open, false if closed
   */
  protected void onOpenStateChanged(boolean open) {
  }

  /**
   * Sets whether the panel is expanded or collapsed.
   *
   * @param open true to expand, false to collapse
   */
  public void setOpen(boolean open) {
    isOpen = open;
    header.getElement().setAttribute("aria-expanded", String.valueOf(open));
    if (open) {
      content.removeStyleDependentName("closed");
    } else {
      content.addStyleDependentName("closed");
    }
    onOpenStateChanged(open);
  }

  /**
   * Returns whether the panel is currently expanded.
   *
   * @return true if expanded, false if collapsed
   */
  public boolean isOpen() {
    return isOpen;
  }

  /**
   * Adds a widget to the content area.
   *
   * @param widget the widget to add
   */
  public void addToContent(Widget widget) {
    content.add(widget);
  }

  /**
   * Returns the content panel.
   *
   * @return the content FlowPanel
   */
  public FlowPanel getContent() {
    return content;
  }
}
