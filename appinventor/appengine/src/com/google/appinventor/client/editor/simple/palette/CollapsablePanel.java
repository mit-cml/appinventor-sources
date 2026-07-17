// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.palette;

import com.google.appinventor.client.widgets.properties.PropertiesPanel;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

public class CollapsablePanel extends Composite {
  private static final Logger LOG = Logger.getLogger(PropertiesPanel.class.getName());

  // UI elements
  private final FlowPanel panel;
  private final HashMap<ComponentCategory, CollapsiblePaletteCategoryPanel> categoryPanels;

  /**
   * Creates a new Collapsable Panel.
   */
  public CollapsablePanel() {
    panel = new FlowPanel();
    panel.setWidth("100%");

    categoryPanels = new HashMap<>();

    initWidget(panel);
  }

  /**
   * Add the given category of components to the panel with title {@code title}.
   *
   * @param componentPanel the content panel for this category
   * @param category       a component category
   * @param title          the name of the component category
   */
  public void add(FlowPanel componentPanel, ComponentCategory category, String title) {
    CollapsiblePaletteCategoryPanel innerPanel = new CollapsiblePaletteCategoryPanel(title);
    innerPanel.addToContent(componentPanel);
    innerPanel.setWidth("100%");
    categoryPanels.put(category, innerPanel);
    panel.add(innerPanel);
  }

  /**
   * Insert a component category into the panel.
   *
   * @param componentPanel the content panel for this category
   * @param category       the abstract category
   * @param title          the category name to be displayed on the panel
   * @param insertIdx      where to insert the category
   */
  public void insert(FlowPanel componentPanel, ComponentCategory category, String title,
      int insertIdx) {
    CollapsiblePaletteCategoryPanel innerPanel = new CollapsiblePaletteCategoryPanel(title);
    innerPanel.addToContent(componentPanel);
    innerPanel.setWidth("100%");
    categoryPanels.put(category, innerPanel);
    panel.insert(innerPanel, insertIdx);
  }

  public FlowPanel getCategoryPanel(ComponentCategory category) {
    if (categoryPanels.containsKey(category)) {
      return categoryPanels.get(category).getContent();
    } else {
      return null;
    }
  }

  public Set<ComponentCategory> getCategories() {
    return categoryPanels.keySet();
  }

  /**
   * Remove the component category from the panel.
   *
   * @param componentPanel the panel that was added to this category
   * @param category       the component category to be removed
   */
  public void remove(FlowPanel componentPanel, ComponentCategory category) {
    CollapsiblePaletteCategoryPanel categoryPanel = categoryPanels.get(category);
    categoryPanel.getContent().remove(componentPanel);
    panel.remove(categoryPanel);
    categoryPanels.remove(category);
  }

  public void clear() {
    categoryPanels.clear();
    panel.clear();
  }

  /**
   * Show (expand) the category at index {@code idx}.
   *
   * @param idx the widget index of the category
   */
  public void show(int idx) {
    ((CollapsiblePaletteCategoryPanel) panel.getWidget(idx)).setOpen(true);
  }

  public void close(int idx) {
    ((CollapsiblePaletteCategoryPanel) panel.getWidget(idx)).setOpen(false);
  }
}
