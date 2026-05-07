// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import com.google.appinventor.client.widgets.AbstractCollapsiblePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * A collapsible panel for property categories to replace GWT DisclosurePanel.
 * Uses FlowPanel with CSS flexbox instead of VerticalPanel (which renders as tables).
 */
public class CollapsibleCategoryPanel extends AbstractCollapsiblePanel {
  /**
   * Note: Copy of the triangle icon from GWT 2.8.1 DisclosurePanel. These images are
   * not available to outside classes (not even using a ClientBundle).
   *
   * Update this if/when upgrading GWT versions to match new theme images and no
   * replacement for DisclosurePanel with alternative rendering is provided.
   */
  private static final String DISCLOSURE_ARROW_DATA_URI =
      "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAnklEQVR4XmNgoDZITk4WQhcjCaSlpZ1JTU3tBdL86HJEAaDGS0D8H4hfpaenJ6PLEwRIBsDweSC2QVeHE2AxAIaXArEcunoMgMcAEP4GDJ+6wsJCTnR9cEDAABjeGBoayoyuFwwIGPCcYMDiMOAnKGqzsrJ40NVjACwGbAQmLhV0dTgBkgFXgU71QJcnCIAaDwOdm48zkAgBvFE0KAEAjDyRV4CcerkAAAAASUVORK5CYII=";

  private final Image headerIcon;

  /**
   * Creates a new collapsible category panel.
   *
   * @param categoryName the name of the category to display in the header
   */
  public CollapsibleCategoryPanel(String categoryName) {
    setStylePrimaryName("ode-PropertyCategory");

    header.setStylePrimaryName("ode-PropertyCategoryHeader");

    headerIcon = new Image(DISCLOSURE_ARROW_DATA_URI);
    headerIcon.setStylePrimaryName("ode-PropertyCategoryIcon");

    Label headerText = new Label(categoryName);
    headerText.setStylePrimaryName("ode-PropertyCategoryText");

    header.add(headerIcon);
    header.add(headerText);

    content.setStylePrimaryName("ode-PropertyCategoryContent");

    setOpen(!"Advanced".equals(categoryName));
  }

  @Override
  protected void onOpenStateChanged(boolean open) {
    if (open) {
      headerIcon.removeStyleDependentName("closed");
    } else {
      headerIcon.addStyleDependentName("closed");
    }
  }
}
