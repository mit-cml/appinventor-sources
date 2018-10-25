// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Mock CheckBox component.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class MockSwitch extends MockWrapper {

  /**
   * Component type name.
   */
  public static final String TYPE = "Switch";

  // GWT checkbox widget used to mock a Simple CheckBox
  private CheckBox switchWidget;

  /**
   * Creates a new MockCheckbox component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockSwitch(SimpleEditor editor) {
    super(editor, TYPE, images.toggleswitch());

    // Initialize mock checkbox UI
    switchWidget = new CheckBox();
    switchWidget.getElement().setClassName("gwt-Switch");
    DOM.setElementAttribute(switchWidget.getElement(), "name", "real-switch");
    initWrapper(switchWidget);
  }

  /**
   * Class that extends CheckBox so we can use a protected constructor.
   *
   * <p/>The purpose of this class is to create a clone of the CheckBox
   * passed to the constructor. It will be used to determine the preferred size
   * of the CheckBox, without having the size constrained by its parent,
   * since the cloned CheckBox won't have a parent.
   */
  static class ClonedSwitch extends CheckBox {
    ClonedSwitch(CheckBox ptb) {
      // Get the Element from the CheckBox.
      // Call DOM.clone to make a deep clone of that element.
      // Pass that cloned element to the super constructor.
      super(DOM.clone(ptb.getElement(), true));
    }
  }

  @Override
  protected Widget createClonedWidget() {
    return new ClonedSwitch(switchWidget);
  }

  @Override
  public void onCreateFromPalette() {
    // Change checkbox caption to component name
    changeProperty(PROPERTY_NAME_TEXT, MESSAGES.textPropertyValue(getName()));
  }

  /*
   * Sets the checkbox's BackgroundColor property to a new value.
   */
  private void setBackgroundColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  // white
    }
    MockComponentsUtil.setWidgetBackgroundColor(switchWidget, text);
  }

  /*
   * Sets the checkbox's BackgroundColor property to a new value.
   */
  private void setThumbColorActiveProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  // white
    }
    MockComponentsUtil.setWidgetTextColor(switchWidget, text);
  }
  private void setThumbColorInactiveProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  // white
    }
    MockComponentsUtil.setWidgetTextColor(switchWidget, text);
  }

  /*
   * Sets the checkbox's BackgroundColor property to a new value.
   */
  private void setTrackColorActiveProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  // white
    }
    MockComponentsUtil.setWidgetTextColor(switchWidget, text);
  }
  private void setTrackColorInactiveProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  // white
    }
    MockComponentsUtil.setWidgetTextColor(switchWidget, text);
  }

  /*
   * Sets the checkbox's Enabled property to a new value.
   */
  private void setEnabledProperty(String text) {
    MockComponentsUtil.setEnabled(this, text);
  }

  /*
   * Sets the checkbox's FontBold property to a new value.
   */
  private void setFontBoldProperty(String text) {
    MockComponentsUtil.setWidgetFontBold(switchWidget, text);
    updatePreferredSize();
  }

  /*
   * Sets the checkbox's FontItalic property to a new value.
   */
  private void setFontItalicProperty(String text) {
    MockComponentsUtil.setWidgetFontItalic(switchWidget, text);
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
   * Sets the checkbox's FontSize property to a new value.
   */
  private void setFontSizeProperty(String text) {
    MockComponentsUtil.setWidgetFontSize(switchWidget, text);
    updatePreferredSize();
  }

  /*
   * Sets the checkbox's FontTypeface property to a new value.
   */
  private void setFontTypefaceProperty(String text) {
    MockComponentsUtil.setWidgetFontTypeface(switchWidget, text);
    updatePreferredSize();
  }

  /*
   * Sets the checkbox's Text property to a new value.
   */
  private void setTextProperty(String text) {
    switchWidget.setText(text);
    updatePreferredSize();
  }

  /*
   * Sets the checkbox's TextColor property to a new value.
   */
  private void setTextColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFF000000";  // black
    }
    MockComponentsUtil.setWidgetTextColor(switchWidget, text);
  }

  /*
   * Sets the checkbox's Checked property to a new value.
   */
  private void setCheckedProperty(String text) {
    switchWidget.setChecked(Boolean.parseBoolean(text));
  }

  // PropertyChangeListener implementation

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
      setBackgroundColorProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_THUMBCOLORACTIVE)) {
      setThumbColorActiveProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_THUMBCOLORINACTIVE)) {
      setThumbColorInactiveProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_TRACKCOLORACTIVE)) {
      setTrackColorActiveProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_TRACKCOLORINACTIVE)) {
      setTrackColorInactiveProperty(newValue);
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
