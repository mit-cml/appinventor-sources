// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;

/**
 * Property editor for boolean values.
 *
 */
public class BooleanPropertyEditor extends PropertyEditor implements ValueChangeHandler<Boolean> {

  // UI for boolean values will be represented by a CheckBox
  private final CheckBox checkbox;

  // Spellings for true and false values (in Java and C++ it is 'true' and 'false', but in Simple
  // it is 'True' and 'False'
  private final String trueValue;
  private final String falseValue;

  /**
   * Creates a new property editor for boolean values.
   *
   * @param trueValue  spelling of true value
   * @param falseValue  spelling of false value
   */
  public BooleanPropertyEditor(String trueValue, String falseValue) {
    this.trueValue = trueValue;
    this.falseValue = falseValue;

    checkbox = new CheckBox();
    checkbox.addValueChangeHandler(this);

    initWidget(checkbox);
  }

  // Updates the property value shown in the editor
  @Override
  protected void updateValue() {
    checkbox.setValue(property.getValue().equals(trueValue));
  }

  // ValueChangeHandler implementation

  @Override
  public void onValueChange(ValueChangeEvent<Boolean> event) {
    property.setValue(checkbox.getValue() ? trueValue : falseValue);
  }
}
