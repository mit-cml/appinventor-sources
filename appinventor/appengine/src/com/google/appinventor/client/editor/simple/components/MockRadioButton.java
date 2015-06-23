// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.RadioButton;

/**
 * Mock RadioButton component.
 *
 */
public final class MockRadioButton extends MockVisibleComponent {

  /**
   * Component type name.
   */
  public static final String TYPE = "RadioButton";

  // Property names
  private static final String PROPERTY_NAME_GROUP = "Group";

  // GWT radioButton widget used to mock a Simple RadioButton
  private final RadioButton radioButtonWidget;

  /**
   * Creates a new MockRadioButton component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockRadioButton(SimpleEditor editor) {
    super(editor, TYPE, images.radiobutton());

    // Initialize mock radioButton UI
    radioButtonWidget = new RadioButton("dummy-group");
    radioButtonWidget.setStylePrimaryName("ode-SimpleMockComponent");
    initComponent(radioButtonWidget);
  }

  @Override
  public void onCreateFromPalette() {
    // Change radio button text to component name.
    changeProperty(PROPERTY_NAME_TEXT, MESSAGES.textPropertyValue(getName()));
  }

  /*
   * Sets the radioButton's BackgroundColor property to a new value.
   */
  private void setBackgroundColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  // white
    }
    MockComponentsUtil.setWidgetBackgroundColor(radioButtonWidget, text);
  }

  /*
   * Sets the radioButton's Enabled property to a new value.
   */
  private void setEnabledProperty(String text) {
    MockComponentsUtil.setEnabled(this, text);
  }

  /*
   * Sets the radioButton's FontBold property to a new value.
   */
  private void setFontBoldProperty(String text) {
    MockComponentsUtil.setWidgetFontBold(radioButtonWidget, text);
  }

  /*
   * Sets the radioButton's FontItalic property to a new value.
   */
  private void setFontItalicProperty(String text) {
    MockComponentsUtil.setWidgetFontItalic(radioButtonWidget, text);
  }

  /*
   * Sets the radioButton's FontSize property to a new value.
   */
  private void setFontSizeProperty(String text) {
    MockComponentsUtil.setWidgetFontSize(radioButtonWidget, text);
  }

  /*
   * Sets the radioButton's FontTypeface property to a new value.
   */
  private void setFontTypefaceProperty(String text) {
    MockComponentsUtil.setWidgetFontTypeface(radioButtonWidget, text);
  }

  /*
   * Sets the radioButton's Text property to a new value.
   */
  private void setTextProperty(String text) {
    radioButtonWidget.setText(text);
  }

  /*
   * Sets the radioButton's TextColor property to a new value.
   */
  private void setTextColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFF000000";  // black
    }
    MockComponentsUtil.setWidgetTextColor(radioButtonWidget, text);
  }

  /*
   * Sets the radioButton's Checked property to a new value.
   */
  private void setCheckedProperty(String text) {
    radioButtonWidget.setChecked(Boolean.parseBoolean(text));
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
    } else if (propertyName.equals(PROPERTY_NAME_CHECKED)) {
      setCheckedProperty(newValue);
    }
  }
}
