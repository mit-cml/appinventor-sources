// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import com.google.appinventor.client.explorer.project.ComponentDatabaseChangeListener;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Panel to display properties.
 *
 */
public class PropertiesPanel extends Composite implements ComponentDatabaseChangeListener {

  // UI elements
  private final FlowPanel panel;
  private final Label componentName;
  private final Map<String, FlowPanel> propertyPanels;
  private final Map<String, CollapsibleCategoryPanel> headers;

  /**
   * Creates a new properties panel.
   */
  public PropertiesPanel() {
    // Initialize UI
    FlowPanel outerPanel = new FlowPanel();
    outerPanel.setStylePrimaryName("ode-PropertiesPanelOuter");

    propertyPanels = new HashMap<String, FlowPanel>();
    headers = new HashMap<String, CollapsibleCategoryPanel>();

    componentName = new Label("");
    componentName.setStyleName("ode-PropertiesComponentName");
    outerPanel.add(componentName);

    panel = new FlowPanel();
    panel.setStylePrimaryName("ode-PropertiesPanel");
    outerPanel.add(panel);

    initWidget(outerPanel);
  }

  boolean hasValidDescription(EditableProperty p) {
    return p.getDescription() != null &&
        !p.getDescription().isEmpty() &&
        !p.getDescription().equals(p.getName());
  }

  private final FlowPanel getContainer(String category) {
    if ( category == null || category.equals( "Internal" ) ) {
      return null;
    }
    if ( !propertyPanels.containsKey( category ) ) {
      FlowPanel child = new FlowPanel();
      child.setStylePrimaryName("ode-PropertyCategoryContentInner");
      propertyPanels.put( category, child );

      CollapsibleCategoryPanel categoryPanel = new CollapsibleCategoryPanel( category );
      categoryPanel.addToContent( child );
      headers.put( category, categoryPanel );
    }
    return propertyPanels.get( category );
  }

  private final void updateStackPanel() {
    // Sort the categories Alphabetically, except Advanced should always come last
    Set<String> categories = new TreeSet<String>(new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        if (o1.equals("Advanced")) {
          if (o2.equals("Advanced")) {
            return 0;
          } else {
            return 1;
          }
        } else if (o2.equals("Advanced")) {
          return -1;
        } else {
          return o1.compareTo(o2);
        }
      }
    });
    categories.addAll(headers.keySet());
    for ( String category : categories ) {
      panel.add( headers.get( category ) );
    }
  }

  /**
   * Adds a new property to be displayed in the UI.
   *
   * @param property  new property to be shown
   */
  void addProperty(EditableProperty property) {
    FlowPanel parent = getContainer(property.getCategory());
    if ( parent != null ) {
      FlowPanel propertyRow = new FlowPanel();
      propertyRow.setStylePrimaryName("ode-PropertyRow");

      FlowPanel header = new FlowPanel();
      header.setStylePrimaryName("ode-PropertyHeader");

      Label label = new Label(property.getCaption());
      label.setStyleName("ode-PropertyLabel");
      header.add(label);

      if ( hasValidDescription(property) ) {
        PropertyHelpWidget helpImage = new PropertyHelpWidget(property);
        header.add(helpImage);
        helpImage.setStylePrimaryName("ode-PropertyHelpWidget");
      }

      propertyRow.add(header);
      // Since UIObject#setStyleName(String) clears existing styles, only
      // style the editor if it hasn't already been styled during instantiation.
      PropertyEditor editor = property.getEditor();
      if (!editor.getStyleName().contains("PropertyEditor")) {
        editor.setStyleName("ode-PropertyEditor");
      }
      propertyRow.add(editor);
      parent.add(propertyRow);
    }
  }

  /**
   * Removes all properties from the properties panel.
   */
  public void clear() {
    propertyPanels.clear();
    headers.clear();
    panel.clear();
    componentName.setText("");
  }

  /**
   * Shows a set of properties in the panel. Any previous content will be
   * removed.
   *
   * @param properties  properties to be shown
   */
  public void setProperties(EditableProperties properties) {
    clear();
    properties.addToPropertiesPanel(this);
    updateStackPanel();
  }

  /**
   * Set the label at the top of the properties panel. Note that you have
   * to do this after calling setProperties because it clears the label!
   * @param name
   */
  public void setPropertiesCaption(String name) {
    componentName.setText(name);
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
