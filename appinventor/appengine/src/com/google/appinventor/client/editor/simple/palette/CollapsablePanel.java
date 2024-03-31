// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.palette;

import com.google.appinventor.client.explorer.project.ComponentDatabaseChangeListener;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollapsablePanel extends Composite implements ComponentDatabaseChangeListener {
  // UI elements
  private final VerticalPanel panel;
  private final Map<Integer, DisclosurePanel> categoryPanels;
  private final Map<ComponentCategory, Integer> categoryLocations;

  /**
   * Creates a new Collapsable Panel.
   */
  public CollapsablePanel() {
    // Initialize UI
    panel = new VerticalPanel();
    panel.setWidth("100%");

    categoryPanels = new HashMap<>();
    categoryLocations = new HashMap<>();

    initWidget(panel);
  }

  /**
   * Add the given category of components to the panel with title `title`.
   *
   * @param category a component category
   * @param title    the name of the component category
   */
  public void add(VerticalPanel componentPanel, ComponentCategory category, String title) {
    // create the DP for the component category
    DisclosurePanel innerPanel = new DisclosurePanel(title);
    innerPanel.add(componentPanel);
    innerPanel.setWidth("100%");
    // find new idx TODO should think abot this
    Integer newIdx = categoryPanels.size();
    categoryLocations.put(category, newIdx);
    categoryPanels.put(newIdx, innerPanel);

    // add the DP to the panel
    panel.add(innerPanel);
  }

  /**
   * Insert a component category into the panel.
   *
   * @param componentPanel the components within the category
   * @param category   the abstract category
   * @param title      the category name to be displayed on the panel
   * @param insertIdx  where to insert the category
   */
  public void insert(VerticalPanel componentPanel, ComponentCategory category, String title,
      int insertIdx) {
    // create the DP for the component category
    DisclosurePanel innerPanel = new DisclosurePanel(title);
    innerPanel.add(componentPanel);
    innerPanel.setWidth("100%");
    if (!categoryPanels.containsKey(insertIdx)) {

      categoryLocations.put(category, insertIdx);
      categoryPanels.put(insertIdx, innerPanel);
      // add the DP to the panel
      panel.add(innerPanel);
    }
  }

  /**
   * Remove the component category from the panel.
   * The user should clean the panel that was added to the disclosure panel
   * to be added to the collapsable panel before removing it from the
   * collapsable panel
   *
   * @param category the component category to be removed - should be in the panel
   */
  public void remove(VerticalPanel componentPanel, ComponentCategory category) {
    Integer categoryIdx = categoryLocations.get(category);
    DisclosurePanel categoryPanel = categoryPanels.get(categoryIdx);
    categoryPanel.remove(componentPanel);
    panel.remove(categoryPanel);
    // remove from the data
    categoryPanels.remove(categoryIdx);
    categoryLocations.remove(category);
  }

  /**
   * Show the category at idx.
   *
   * @param idx the component category location
   */
  public void show(int idx) {
    DisclosurePanel categoryPanel = categoryPanels.get(idx);
    categoryPanel.setOpen(true);
  }

  @Override
  public void onComponentTypeAdded(List<String> componentTypes) {

  }

  @Override
  public boolean beforeComponentTypeRemoved(List<String> componentTypes) {
    return true;
  }

  @Override
  public void onComponentTypeRemoved(Map<String, String> componentTypes) {

  }

  @Override
  public void onResetDatabase() {

  }
}
