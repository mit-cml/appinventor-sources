// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Abstract class for on/off toggle components, e.g. Checkbox and Switch
 *
 * @author srlane@mit.edu (Susan Rati Lane)
 */
abstract class MockToggleBase<T extends Widget> extends MockWrapper {

  // Set toggle widget in child classes
  protected T toggleWidget;

  public MockToggleBase(SimpleEditor editor, String type, ImageResource icon) {
    super(editor, type, icon);
  }

  abstract protected Widget createClonedWidget();

  @Override
  public void onCreateFromPalette() {
    // Change toggle caption to component name
    changeProperty(PROPERTY_NAME_TEXT, MESSAGES.textPropertyValue(getName()));
  }

  /*
   * Sets the toggle's BackgroundColor property to a new value.
   */
  private void setBackgroundColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  // white
    }
    MockComponentsUtil.setWidgetBackgroundColor(toggleWidget, text);
  }

  /*
   * Sets the toggle's Enabled property to a new value.
   */
  private void setEnabledProperty(String text) {
    MockComponentsUtil.setEnabled(this, text);
  }

  /*
   * Sets the toggle's FontBold property to a new value.
   */
  private void setFontBoldProperty(String text) {
    MockComponentsUtil.setWidgetFontBold(toggleWidget, text);
    updatePreferredSize();
  }

  /*
   * Sets the toggle's FontItalic property to a new value.
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

  /*
   * Sets the toggle's FontSize property to a new value.
   */
  protected void setFontSizeProperty(String text) {
    MockComponentsUtil.setWidgetFontSize(toggleWidget, text);
    updatePreferredSize();
  }

  /*
   * Sets the toggle's FontTypeface property to a new value.
   */
  protected void setFontTypefaceProperty(String text) {
    MockComponentsUtil.setWidgetFontTypeface(toggleWidget, text);
    updatePreferredSize();
  }

  /*
   * The toggle's set text must be implemented in child classes
   */
  abstract void setTextProperty(String text);

  /*
   * Sets the toggle's TextColor property to a new value.
   */
  private void setTextColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFF000000";  // black
    }
    MockComponentsUtil.setWidgetTextColor(toggleWidget, text);
  }

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
    }
  }
}
