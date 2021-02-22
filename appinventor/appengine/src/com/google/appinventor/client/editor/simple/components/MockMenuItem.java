// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.InlineHTML;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Mock Menu Item component.
 *
 * @author xy93@cornell.edu (Steven Ye)
 */
public final class MockMenuItem extends MockVisibleComponent {

  // Component type name
  public static final String TYPE = "MenuItem";

  // Property names
  private static final String PROPERTY_NAME_TEXT = "Text";
  private static final String PROPERTY_NAME_ICON = "Icon";
  private static final String PROPERTY_NAME_ENABLED = "Enabled";
  private static final String PROPERTY_NAME_VISIBLE = "Visible";
  private static final String PROPERTY_NAME_SHOWASACTION = "ShowOnActionBar";

  // GWT widget used to mock a menu item
  private InlineHTML itemWidget;

  /**
   * Creates a new MockMenuItem component.
   *
   * @param editor editor of source file the component belongs to
   */
  public MockMenuItem(SimpleEditor editor) {
    super(editor, TYPE, images.menuitem());

    // Initialize mock menu item UI
    itemWidget = new InlineHTML();
    itemWidget.setStylePrimaryName("ode-SimpleMockComponent");
    initComponent(itemWidget);
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_WIDTH) ||
            propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      return false;
    }
    return super.isPropertyVisible(propertyName);
  }

  @Override
  public void onCreateFromPalette() {
    // Change item text to component name
    changeProperty(PROPERTY_NAME_TEXT, MESSAGES.textPropertyValue(getName()));
  }

  @Override
  protected void onSelectedChange(boolean selected) {
    getContainer().setVisible(selected);
    super.onSelectedChange(selected);
  }

  /*
   * Sets the item's Text property to a new value.
   */
  private void setTextProperty(String text) {
    itemWidget.setText(text);
  }

  /*
   * Sets the item's Icon property to a new value.
   */
  private void setIconProperty(String text) {
    // Implement this to reflect icon change in designer
  }

  /*
   * Sets the item's Enabled property to a new value.
   */
  private void setEnabledProperty(String text) {
    MockComponentsUtil.setEnabled(this, text);
  }

  /*
   * Sets the item's Visible property to a new value.
   */
  private void setVisibleProperty(String text) {
    boolean visible = Boolean.parseBoolean(text);
    if (!visible && !editor.isLoadComplete()) {
      expanded = false;
    }
  }

  /*
   * Sets the item's ShowOnActionBar property to a new value.
   */
  private void setShowOnActionBarProperty(String text) {
    // Implement this to reflect whether item is shown on action bar in designer
  }

  // PropertyChangeListener implementation
  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_TEXT)) {
      setTextProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_ICON)) {
      setIconProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_ENABLED)) {
      setEnabledProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_VISIBLE)) {
      setVisibleProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_SHOWASACTION)) {
      setShowOnActionBarProperty(newValue);
    }
  }
}
