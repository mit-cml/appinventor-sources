// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.gwt.user.client.ui.RadioButton;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.widgets.properties.BooleanPropertyEditor;

import java.util.Random;
import java.util.List;

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

    Random random = new Random();   
    // Generates random integers 0 to 999999 
    int x = random.nextInt(1000000);
    String randomName = "RadioButton" + String.valueOf(x);

    // Initialize mock radioButton UI

    // Note : GWT RadioButton is not really a radiobutton! It's exactly like 
    // a radiogroup when given the same randomName. Read it's docs!
    // WHY CALL IT A RADIOBUTTON THEN !???
    
    radioButtonWidget = new RadioButton(randomName);
    radioButtonWidget.setStylePrimaryName("ode-SimpleMockComponent");
    initComponent(radioButtonWidget);
  }

  @Override
  public void onCreateFromPalette() {
    // Change radio button text to component name.
    changeProperty(PROPERTY_NAME_TEXT, MESSAGES.textPropertyValue(getName()));
  }

  public Boolean isChecked() {
    return radioButtonWidget.getValue();
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
    Boolean value = Boolean.parseBoolean(text);
    MockContainer parent = getContainer();
    MockRadioButton button;
    
    if (value && parent instanceof MockRadioGroup) {
      for (MockComponent child : parent.getChildren()) {
        if (child instanceof MockRadioButton) {
          button = (MockRadioButton) child;
          if (button != this && button.isChecked()){
            changeCheckedInPropertyPanel(child, !value);
          }
        }
      }
    }
    
    radioButtonWidget.setValue(value);
  }

  private void changeCheckedInPropertyPanel(MockComponent child, Boolean value) {
    // Spellings for true and false values 
    String trueValue = "true", falseValue = "false";

    child.removeProperty(PROPERTY_NAME_CHECKED);
    if (value){
      child.addProperty(PROPERTY_NAME_CHECKED, trueValue, PROPERTY_NAME_CHECKED, 
        new BooleanPropertyEditor(trueValue, trueValue));
    } else {
      child.addProperty(PROPERTY_NAME_CHECKED, falseValue, PROPERTY_NAME_CHECKED, 
        new BooleanPropertyEditor(trueValue, falseValue));
    }
  }

  private static native void consoleLog(String name) /*-{
    console.log(name)
  }-*/;

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
      refreshForm();
    }
  }
}
