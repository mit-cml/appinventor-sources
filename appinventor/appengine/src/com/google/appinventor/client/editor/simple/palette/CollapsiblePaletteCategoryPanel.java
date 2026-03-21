// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.palette;

import com.google.appinventor.client.widgets.AbstractCollapsiblePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A collapsible panel for palette categories to replace GWT DisclosurePanel.
 * Uses FlowPanel with CSS flexbox instead of table-rendering widgets.
 */
public class CollapsiblePaletteCategoryPanel extends AbstractCollapsiblePanel {
  private final FlowPanel contentInner;

  /**
   * Creates a new collapsible palette category panel.
   *
   * @param categoryName the name of the category to display in the header
   */
  public CollapsiblePaletteCategoryPanel(String categoryName) {
    setStylePrimaryName("ode-PaletteCategory");

    header.setStylePrimaryName("ode-PaletteCategoryHeader");

    Label headerText = new Label(categoryName);
    headerText.setStylePrimaryName("ode-PaletteCategoryText");
    header.add(headerText);

    content.setStylePrimaryName("ode-PaletteCategoryContent");

    contentInner = new FlowPanel();
    contentInner.setStylePrimaryName("ode-PaletteCategoryContentInner");
    contentInner.getElement().setAttribute("role", "list");
    contentInner.getElement().setAttribute("aria-label", categoryName + " components");
    content.add(contentInner);

    finishInit();

    // Default to closed; callers control initial state via setOpen()
    setOpen(false);
  }

  @Override
  public void addToContent(Widget widget) {
    contentInner.add(widget);
  }

  @Override
  public FlowPanel getContent() {
    return contentInner;
  }
}
