// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.core.client.JavaScriptException;
import com.google.appinventor.client.editor.simple.SimpleEditor;

/**
 * Superclass for RadioGroup based mock components
 *
 * @author thamihardik8@gmail.com (Hardik Thami)
 */
public class MockRadioGroup extends MockHVArrangement {

  /** 
  * Instance of MockRadioButton that is checked
  */ 
  private MockRadioButton checkedRadioButton;

  /**
   * Creates a new MockRadioGroup component.
   */
  public MockRadioGroup(SimpleEditor editor, String type, ImageResource icon, int orientation,
    boolean scrollable) {
    super(editor, type, icon, orientation, scrollable);
    this.checkedRadioButton = null;
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
  }

  /**
  * Sets the MockRadioButton that is checked in this MockRadioGroup.
  */
  public void setCheckedRadioButton(MockRadioButton button){
    this.checkedRadioButton = button;
  }

  /**
  * Returns the MockRadioButton that is checked in this MockRadioGroup.
  */
  public MockRadioButton getCheckedRadioButton(){
    return this.checkedRadioButton;
  }

  /** 
  * For Testing Purposes
  */ 
  private static native void consoleLog(String name) /*-{
    console.log(name)
  }-*/;

  private static native void consoleError(JavaScriptException ex) /*-{
    console.error(ex)
  }-*/;

}