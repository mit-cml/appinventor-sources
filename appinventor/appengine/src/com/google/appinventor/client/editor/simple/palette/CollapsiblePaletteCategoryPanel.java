// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.palette;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A collapsible panel for palette categories to replace GWT DisclosurePanel.
 * Uses FlowPanel with CSS flexbox instead of table-rendering widgets.
 */
public class CollapsiblePaletteCategoryPanel extends FlowPanel {
  private static int idCounter = 0;

  private final FlowPanel header;
  private final Label headerText;
  private final FlowPanel content;
  private final FlowPanel contentInner;
  private boolean isOpen;

  /**
   * Creates a new collapsible palette category panel.
   *
   * @param categoryName the name of the category to display in the header
   */
  public CollapsiblePaletteCategoryPanel(String categoryName) {
    String contentId = "ode-palette-cat-" + (idCounter++);

    setStylePrimaryName("ode-PaletteCategory");

    header = new FlowPanel();
    header.setStylePrimaryName("ode-PaletteCategoryHeader");
    header.getElement().setAttribute("role", "button");
    header.getElement().setAttribute("tabindex", "0");
    header.getElement().setAttribute("aria-expanded", "false");
    header.getElement().setAttribute("aria-controls", contentId);

    headerText = new Label(categoryName);
    headerText.setStylePrimaryName("ode-PaletteCategoryText");
    header.add(headerText);

    content = new FlowPanel();
    content.setStylePrimaryName("ode-PaletteCategoryContent");
    content.getElement().setId(contentId);

    contentInner = new FlowPanel();
    contentInner.setStylePrimaryName("ode-PaletteCategoryContentInner");
    contentInner.getElement().setAttribute("role", "list");
    contentInner.getElement().setAttribute("aria-label", categoryName + " components");
    content.add(contentInner);

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

    add(header);
    add(content);

    // Default to closed; callers control initial state via setOpen()
    setOpen(false);
  }

  /**
   * Sets whether the category is expanded or collapsed.
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
  }

  /**
   * Returns whether the category is currently expanded.
   *
   * @return true if expanded, false if collapsed
   */
  public boolean isOpen() {
    return isOpen;
  }

  /**
   * Adds a widget to the category's content area.
   *
   * @param widget the widget to add
   */
  public void addToContent(Widget widget) {
    contentInner.add(widget);
  }

  /**
   * Gets the content FlowPanel for this category.
   *
   * @return the inner content FlowPanel
   */
  public FlowPanel getContent() {
    return contentInner;
  }
}
