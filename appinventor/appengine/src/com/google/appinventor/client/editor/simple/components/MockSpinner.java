// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.ListBox;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Spinner component.
 */
public final class MockSpinner extends MockVisibleComponent {

  /**
   * Component type name.
   */
  public static final String TYPE = "Spinner";
  private ListBox spinnerWidget;

  /**
   * Creates a new MockSpinner component.
   *
   * @param editor editor of source file the component belongs to
   */
  public MockSpinner(SimpleEditor editor) {
    super(editor, TYPE, images.spinner());

    // Initialize mock label UI
    spinnerWidget = new ListBox();
    spinnerWidget.addItem(MESSAGES.MockSpinnerAddItems());
    spinnerWidget.setStylePrimaryName("ode-SimpleMockComponent");
    spinnerWidget.addStyleName("spinnerComponentStyle");
    initComponent(spinnerWidget);
    refreshForm();
  }

  /*
   * Sets the spinner's Enabled property to a new value.
   */
  private void setEnabledProperty(String text) {
    MockComponentsUtil.setEnabled(this, text);
  }

  /*
   * Sets the spinner's FontBold property to a new value.
   */
  private void setFontBoldProperty(String text) {
    MockComponentsUtil.setWidgetFontBold(spinnerWidget, text);
  }

  /*
   * Sets the spinner's FontItalic property to a new value.
   */
  private void setFontItalicProperty(String text) {
    MockComponentsUtil.setWidgetFontItalic(spinnerWidget, text);
  }

  /*
   * Sets the spinner's FontSize property to a new value.
   */
  private void setFontSizeProperty(String text) {
    MockComponentsUtil.setWidgetFontSize(spinnerWidget, text);
  }

  /*
   * Sets the spinner's FontTypeface property to a new value.
   */
  private void setFontTypefaceProperty(String text) {
    MockComponentsUtil.setWidgetFontTypeface(spinnerWidget, text);
  }

  /*
   * Sets the spinner's TextColor property to a new value.
   */
  private void setTextColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFF000000";  // black
    }
    MockComponentsUtil.setWidgetTextColor(spinnerWidget, text);
  }

  /*
   * Sets the spinner's TextAlignment property to a new value.
   */
  private void setTextAlignmentProperty(String text) {
    MockComponentsUtil.setWidgetTextAlign(spinnerWidget, text);
  }

  /*
   * Sets the spinner's BackgroundColor property to a new value.
   */
  private void setBackgroundColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  // white
    }
    MockComponentsUtil.setWidgetBackgroundColor(spinnerWidget, text);
  }

  // PropertyChangeListener implementation
  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_ENABLED)) {
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
    } else if (propertyName.equals(PROPERTY_NAME_TEXTCOLOR)) {
      setTextColorProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_TEXTALIGNMENT)) {
      setTextAlignmentProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
      setBackgroundColorProperty(newValue);
    }
  }
}