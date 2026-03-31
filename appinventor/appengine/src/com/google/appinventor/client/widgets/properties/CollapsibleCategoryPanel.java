// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A collapsible panel for property categories to replace GWT DisclosurePanel
 * Uses FlowPanel with CSS flexbox instead of VerticalPanel (which renders as tables)
 */
public class CollapsibleCategoryPanel extends FlowPanel {
  private final FlowPanel header;
  private final Image headerIcon;
  private final Label headerText;
  private final FlowPanel content;
  private boolean isOpen;

  /**
   * Note: Copy of the triangle icon from GWT 2.8.1 DisclosurePanel. These images are
   * not available to outside classes (not even using a ClientBundle).
   *
   * Update this if/when upgrading GWT versions to match new theme images and no
   * replacement for DisclosurePanel with alternative rendering is provided.
   */
  private static final String DISCLOSURE_ARROW_DATA_URI =
      "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAnklEQVR4XmNgoDZITk4WQhcjCaSlpZ1JTU3tBdL86HJEAaDGS0D8H4hfpaenJ6PLEwRIBsDweSC2QVeHE2AxAIaXArEcunoMgMcAEP4GDJ+6wsJCTnR9cEDAABjeGBoayoyuFwwIGPCcYMDiMOAnKGqzsrJ40NVjACwGbAQmLhV0dTgBkgFXgU71QJcnCIAaDwOdm48zkAgBvFE0KAEAjDyRV4CcerkAAAAASUVORK5CYII=";

  /**
   * Creates a new collapsible category panel.
   *
   * @param categoryName the name of the category to display in the header
   */
  public CollapsibleCategoryPanel(String categoryName) {
    setStylePrimaryName("ode-PropertyCategory");

    header = new FlowPanel();
    header.setStylePrimaryName("ode-PropertyCategoryHeader");

    headerIcon = new Image();
    headerIcon.setUrl(DISCLOSURE_ARROW_DATA_URI);
    headerIcon.setStylePrimaryName("ode-PropertyCategoryIcon");

    headerText = new Label(categoryName);
    headerText.setStylePrimaryName("ode-PropertyCategoryText");

    header.add(headerIcon);
    header.add(headerText);

    content = new FlowPanel();
    content.setStylePrimaryName("ode-PropertyCategoryContent");

    header.addDomHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        setOpen(!isOpen);
      }
    }, ClickEvent.getType());

    add(header);
    add(content);

    setOpen(!"Advanced".equals(categoryName));
  }

  /**
   * Sets whether the category is expanded or collapsed.
   *
   * @param open true to expand, false to collapse
   */
  public void setOpen(boolean open) {
    isOpen = open;
    if (open) {
      content.removeStyleName("ode-PropertyCategoryContent-closed");
      headerIcon.removeStyleName("ode-PropertyCategoryIcon-closed");
    } else {
      content.addStyleName("ode-PropertyCategoryContent-closed");
      headerIcon.addStyleName("ode-PropertyCategoryIcon-closed");
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
    content.add(widget);
  }

  /**
   * Gets the content FlowPanel for this category.
   *
   * @return the content FlowPanel
   */
  public FlowPanel getContent() {
    return content;
  }
}
