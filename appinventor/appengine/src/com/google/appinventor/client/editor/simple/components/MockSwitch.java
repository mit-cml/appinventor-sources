// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;


import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * Mock Switch component.
 *
 * @author srlane@mit.edu (Susan Rati Lane)
 */
public final class MockSwitch extends MockWrapper {

  /**
   * Component type name.
   */
  public static final String TYPE = "Switch";

  // GWT togglebutton widget used to mock a Switch
  private ToggleButton toggleWidget;

  /**
   * Creates a new MockSwitch component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockSwitch(SimpleEditor editor) {
    super(editor, TYPE, images.togglebutton());

    // Initialize mock switch UI
    toggleWidget = new ToggleButton();
    initWrapper(toggleWidget);
  }

  /**
   * Class that extends ToggleButton (Switch) so we can use a protected constructor.
   *
   * <p/>The purpose of this class is to create a clone of the ToggleButton
   * passed to the constructor. It will be used to determine the preferred size
   * of the ToggleButton, without having the size constrained by its parent,
   * since the cloned ToggleButton won't have a parent.
   */
  static class ClonedToggleButton extends ToggleButton {
    ClonedToggleButton(ToggleButton ptb) {
      // Get the Element from the ToggleButton.
      // Call DOM.clone to make a deep clone of that element.
      // Pass that cloned element to the super constructor.
      super(ptb.getUpFace().getText(), ptb.getDownFace().getText());
    }
  }

  @Override
  protected Widget createClonedWidget() {
    return new ClonedToggleButton(toggleWidget);
  }

  @Override
  public void onCreateFromPalette() {
    // Change togglebutton caption to component name
    changeProperty(PROPERTY_NAME_TEXT, MESSAGES.textPropertyValue(getName()));
  }

  /*
   * Sets the togglebutton's BackgroundColor property to a new value.
   */
  private void setBackgroundColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  // white
    }
    MockComponentsUtil.setWidgetBackgroundColor(toggleWidget, text);
  }

  /*
   * Sets the togglebutton's Enabled property to a new value.
   */
  private void setEnabledProperty(String text) {
    MockComponentsUtil.setEnabled(this, text);
  }

  /*
   * Sets the togglebutton's FontBold property to a new value.
   */
  private void setFontBoldProperty(String text) {
    MockComponentsUtil.setWidgetFontBold(toggleWidget, text);
    updatePreferredSize();
  }

  /*
   * Sets the togglebutton's FontItalic property to a new value.
   */
  private void setFontItalicProperty(String text) {
    MockComponentsUtil.setWidgetFontItalic(toggleWidget, text);
    updatePreferredSize();
  }

  @Override
  int getHeightHint() {
    int hint = super.getHeightHint();
    if (hint == MockVisibleComponent.LENGTH_PREFERRED) {
      float height = Float.parseFloat(getPropertyValue(MockVisibleComponent.PROPERTY_NAME_FONTSIZE));
      return Math.round(height);
    } else {
      return hint;
    }
  }

  @Override
  int getWidthHint() {
    return hint = super.getWidthHint();
//    if (hint == MockVisibleComponent.LENGTH_PREFERRED) {
//      float height = Float.parseFloat(getPropertyValue(MockVisibleComponent.PROPERTY_NAME_FONTSIZE));
//      return Math.round(height);
//    } else {
//      return hint;
//    }
  }

  /*
   * Sets the togglebutton's FontSize property to a new value.
   */
  private void setFontSizeProperty(String text) {
    MockComponentsUtil.setWidgetFontSize(toggleWidget, text);
    updatePreferredSize();
  }

  /*
   * Sets the togglebutton's FontTypeface property to a new value.
   */
  private void setFontTypefaceProperty(String text) {
    MockComponentsUtil.setWidgetFontTypeface(toggleWidget, text);
    updatePreferredSize();
  }

  /*
   * Sets the togglebutton's Text property to a new value.
   */
  private void setTextProperty(String text) {
    toggleWidget.setText(text);
    updatePreferredSize();
  }

  /*
   * Sets the togglebutton's TextColor property to a new value.
   */
  private void setTextColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFF000000";  // black
    }
    MockComponentsUtil.setWidgetTextColor(toggleWidget, text);
  }

  /*
   * Sets the togglebutton's Down property to a new value.
   */
  private void setDownProperty(String text) {
    toggleWidget.setDown(Boolean.parseBoolean(text));
  }

  // PropertyChangeListener implementation

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
      setBackgroundColorProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_ENABLED)) {
      setEnabledProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_FONTBOLD)) {
      setFontBoldProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_FONTITALIC)) {
      setFontItalicProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_FONTSIZE)) {
      setFontSizeProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_FONTTYPEFACE)) {
      setFontTypefaceProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_TEXT)) {
      setTextProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_TEXTCOLOR)) {
      setTextColorProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_DOWN)) {
      setDownProperty(newValue);
    }
  }
}
