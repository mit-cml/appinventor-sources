// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Mock PasswordTextBox component.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class MockPasswordTextBox extends MockWrapper {

  /**
   * Component type name.
   */
  public static final String TYPE = "PasswordTextBox";

  // GWT PasswordTextBox widget used to mock a Simple PasswordTextBox.
  private final PasswordTextBox passwordTextBoxWidget;

  /**
   * Creates a new MockPasswordTextBox component.
   *
   * @param editor editor of source file the component belongs to.
   */
  public MockPasswordTextBox(SimpleEditor editor) {
    super(editor, TYPE, images.passwordtextbox());

    // Initialize mock PasswordTextBox UI.
    passwordTextBoxWidget = new PasswordTextBox();
    // Change PasswordTextBox text so that it doesn't show up as a blank box in the designer.
    passwordTextBoxWidget.setText("**********");
    initWrapper(passwordTextBoxWidget);
  }

  /**
   * Class that extends PasswordTextBox so we can use a protected constructor.
   *
   * <p/>The purpose of this class is to create a clone of the PasswordTextBox
   * passed to the constructor. It will be used to determine the preferred size
   * of the PasswordTextBox, without having the size constrained by its parent,
   * since the cloned PasswordTextBox won't have a parent.
   */
  static class ClonedPasswordTextBox extends PasswordTextBox {
    ClonedPasswordTextBox(PasswordTextBox ptb) {
      // Get the Element from the PasswordTextBox.
      // Call DOM.clone to make a deep clone of that element.
      // Pass that cloned element to the super constructor.
      super(DOM.clone(ptb.getElement(), true));
    }
  }

  @Override
  protected Widget createClonedWidget() {
    return new ClonedPasswordTextBox(passwordTextBoxWidget);
  }

  @Override
  public int getPreferredWidth() {
    return ComponentConstants.TEXTBOX_PREFERRED_WIDTH;
  }

  /*
   * Sets the PasswordTextBox's TextAlignment property to a new value.
   */
  private void setTextAlignmentProperty(String text) {
    MockComponentsUtil.setWidgetTextAlign(passwordTextBoxWidget, text);
  }

  /*
   * Sets the PasswordTextBox's BackgroundColor property to a new value.
   */
  private void setBackgroundColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  // white
    }
    MockComponentsUtil.setWidgetBackgroundColor(passwordTextBoxWidget, text);
  }

  /*
   * Sets the PasswordTextBox's Enabled property to a new value.
   */
  private void setEnabledProperty(String text) {
    MockComponentsUtil.setEnabled(this, text);
  }

  /*
   * Sets the PasswordTextBox's FontBold property to a new value.
   */
  private void setFontBoldProperty(String text) {
    MockComponentsUtil.setWidgetFontBold(passwordTextBoxWidget, text);
    updatePreferredSize();
  }

  /*
   * Sets the PasswordTextBox's FontItalic property to a new value.
   */
  private void setFontItalicProperty(String text) {
    MockComponentsUtil.setWidgetFontItalic(passwordTextBoxWidget, text);
    updatePreferredSize();
  }

  /*
   * Sets the PasswordTextBox's FontSize property to a new value.
   */
  private void setFontSizeProperty(String text) {
    MockComponentsUtil.setWidgetFontSize(passwordTextBoxWidget, text);
    updatePreferredSize();
  }

  /*
   * Sets the PasswordTextBox's FontTypeface property to a new value.
   */
  private void setFontTypefaceProperty(String text) {
    MockComponentsUtil.setWidgetFontTypeface(passwordTextBoxWidget, text);
    updatePreferredSize();
  }

  /*
   * Sets the PasswordTextBox's Hint property to a new value.
   */
  private void setHintProperty(String text) {
    passwordTextBoxWidget.setTitle(text);
  }

  /*
   * Sets the PasswordTextBox's TextColor property to a new value.
   */
  private void setTextColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFF000000";  // black
    }
    MockComponentsUtil.setWidgetTextColor(passwordTextBoxWidget, text);
  }

  // PropertyChangeListener implementation

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component.
    if (propertyName.equals(PROPERTY_NAME_TEXTALIGNMENT)) {
      setTextAlignmentProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
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
    } else if (propertyName.equals(PROPERTY_NAME_HINT)) {
      setHintProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_TEXTCOLOR)) {
      setTextColorProperty(newValue);
    }
  }
}
