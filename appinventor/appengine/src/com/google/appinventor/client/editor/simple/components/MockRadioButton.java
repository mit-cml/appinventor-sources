// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.core.client.JavaScriptException;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.widgets.properties.BooleanPropertyEditor;

import java.util.Random;
import java.util.List;

/**
 * Mock RadioButton component.
 *
 * @author thamihardik8@gmail.com (Modified setCheckedProperty for MockRadioGroup)
 */
public final class MockRadioButton extends MockVisibleComponent {

  /**
   * Component type name.
   */
  public static final String TYPE = "RadioButton";

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
    int x = random.nextInt(1000000000);

    // Note: GWT RadioButton is not really a radiobutton! why call
    // it a radiobutton then !??? It's exactly like a radiogroup
    // when given the same group name. Read it's docs!
    
    radioButtonWidget = new RadioButton(String.valueOf(x)) {
      @Override
      public void onLoad() {
        super.onLoad();
        refreshForm();
      }
    };

    radioButtonWidget.setStylePrimaryName("ode-SimpleMockComponent");
    initComponent(radioButtonWidget);
  }

  @Override
  public void onCreateFromPalette() {
    // Change radio button text to component name.
    changeProperty(PROPERTY_NAME_TEXT, MESSAGES.textPropertyValue(getName()));
  }

  /*
   * Returns the RadioButton's Checked Property.
   */
  public Boolean isChecked() {
    return radioButtonWidget.getValue();
  }

  /*
   * Sets the RadioButton's BackgroundColor Property to a new value.
   */
  private void setBackgroundColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  // white
    }
    MockComponentsUtil.setWidgetBackgroundColor(radioButtonWidget, text);
  }

  /*
   * Sets the RadioButton's Enabled Property to a new value.
   */
  private void setEnabledProperty(String text) {
    MockComponentsUtil.setEnabled(this, text);
  }

  /*
   * Sets the RadioButton's FontBold Property to a new value.
   */
  private void setFontBoldProperty(String text) {
    MockComponentsUtil.setWidgetFontBold(radioButtonWidget, text);
  }

  /*
   * Sets the RadioButton's FontItalic Property to a new value.
   */
  private void setFontItalicProperty(String text) {
    MockComponentsUtil.setWidgetFontItalic(radioButtonWidget, text);
  }

  /*
   * Sets the RadioButton's FontSize Property to a new value.
   */
  private void setFontSizeProperty(String text) {
    MockComponentsUtil.setWidgetFontSize(radioButtonWidget, text);
  }

  /*
   * Sets the RadioButton's FontTypeface Property to a new value.
   */
  private void setFontTypefaceProperty(String text) {
    MockComponentsUtil.setWidgetFontTypeface(radioButtonWidget, text);
  }

  /*
   * Sets the RadioButton's Text Property to a new value.
   */
  private void setTextProperty(String text) {
    radioButtonWidget.setText(text);
  }

  /*
   * Sets the RadioButton's TextColor Property to a new value.
   */
  private void setTextColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFF000000";  // black
    }
    MockComponentsUtil.setWidgetTextColor(radioButtonWidget, text);
  }

  /*
   * Sets the RadioButton's Checked Property to a new value
   * and if nested in a MockRadioGroup sets other RadioButton's
   * in that MockRadioGroup to unchecked.
   * 
   */
  private void setCheckedProperty(String text) {
    Boolean value = Boolean.parseBoolean(text);
    MockContainer parent = getContainer();
    MockRadioGroup radioGroupParent = null;
    
    try {
      if (parent instanceof MockRadioGroup) {
        radioGroupParent = (MockRadioGroup) parent;
        MockRadioButton checkedbutton = radioGroupParent.getCheckedRadioButton();
        if (checkedbutton != null && checkedbutton != this && value) {
          this.changeCheckedInPropertyPanel(checkedbutton, !value);
        }
      }
    } catch (JavaScriptException exception) {
      consoleError(exception);
      // throw exception;
    } finally {
      radioButtonWidget.setValue(value);
      if (radioGroupParent != null){
        if (value) {
          radioGroupParent.setCheckedRadioButton(this);
        } else {
          radioGroupParent.setCheckedRadioButton(null);
        }
      }
    }
  }

  /**
   * Changes the Checked Property in Properties Panel for MockRadioButton.
   * To be used only when component has CHECKED Property otherwise 
   * removeProperty will throw an error.
   * 
   * @param child component whose value is being changed.
   * @param value new value of checked property.
   */
  private void changeCheckedInPropertyPanel(MockRadioButton child, Boolean value) {
    // Spellings for true and false values 
    String trueValue = "true", falseValue = "false";

    child.removeProperty(PROPERTY_NAME_CHECKED);
    if (value) {
      // Currently, there is no need for true clause. 

      // child.addProperty(PROPERTY_NAME_CHECKED, trueValue, PROPERTY_NAME_CHECKED, 
      //  new BooleanPropertyEditor(trueValue, falseValue));
    } else {
      child.addProperty(PROPERTY_NAME_CHECKED, falseValue, PROPERTY_NAME_CHECKED, 
        new BooleanPropertyEditor(trueValue, falseValue));
    }
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
      refreshForm();
    }
  }

  // For Testing Purposes
  private static native void consoleLog(String name) /*-{
    console.log(name)
  }-*/;

  private static native void consoleError(JavaScriptException ex) /*-{
    console.error(ex)
  }-*/;

}
