// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.components.MockVisibleComponent;
import com.google.appinventor.client.widgets.properties.AdditionalChoicePropertyEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Property editor for length properties (i.e. width and height).
 *
 */
public class YoungAndroidLengthPropertyEditor extends AdditionalChoicePropertyEditor {
  public static final String CONST_AUTOMATIC = "" + MockVisibleComponent.LENGTH_PREFERRED;
  public static final String CONST_FILL_PARENT = "" + MockVisibleComponent.LENGTH_FILL_PARENT;

  private static int uniqueIdSeed = 0;

  private final RadioButton automaticRadioButton;
  private final RadioButton fillParentRadioButton;
  private final RadioButton customLengthRadioButton;
  private final TextBox customLengthField;

  /**
   * Creates a new length property editor.
   */
  public YoungAndroidLengthPropertyEditor() {
    // The radio button group cannot be shared across all instances, so we append a unique id.
    int uniqueId = ++uniqueIdSeed;
    String radioButtonGroup = "LengthType-" + uniqueId;
    automaticRadioButton = new RadioButton(radioButtonGroup, MESSAGES.automaticCaption());
    fillParentRadioButton = new RadioButton(radioButtonGroup, MESSAGES.fillParentCaption());
    customLengthRadioButton = new RadioButton(radioButtonGroup);
    customLengthField = new TextBox();
    customLengthField.setVisibleLength(4);
    customLengthField.setMaxLength(4);

    Panel customRow = new HorizontalPanel();
    customRow.add(customLengthRadioButton);
    customRow.add(customLengthField);
    Label dps = new Label(MESSAGES.dpsCaption());
    dps.setStylePrimaryName("ode-DPsLabel");
    customRow.add(dps);

    Panel panel = new VerticalPanel();
    panel.add(automaticRadioButton);
    panel.add(fillParentRadioButton);
    panel.add(customRow);

    automaticRadioButton.addValueChangeHandler(new ValueChangeHandler() {
      @Override
      public void onValueChange(ValueChangeEvent event) {
        // Clear the custom length field.
        customLengthField.setText("");
      }
    });
    fillParentRadioButton.addValueChangeHandler(new ValueChangeHandler() {
      @Override
      public void onValueChange(ValueChangeEvent event) {
        // Clear the custom length field.
        customLengthField.setText("");
      }
    });
    customLengthField.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // If the user clicks on the custom length field, but the radio button for a custom length
        // is not checked, check it.
        if (!customLengthRadioButton.isChecked()) {
          customLengthRadioButton.setChecked(true);
        }
      }
    });

    initAdditionalChoicePanel(panel);
  }

  @Override
  protected void updateValue() {
    super.updateValue();

    String propertyValue = property.getValue();
    if (propertyValue.equals(CONST_AUTOMATIC)) {
      automaticRadioButton.setChecked(true);
    } else if (propertyValue.equals(CONST_FILL_PARENT)) {
      fillParentRadioButton.setChecked(true);
    } else {
      customLengthRadioButton.setChecked(true);
      customLengthField.setText(propertyValue);
    }
  }

  @Override
  protected String getPropertyValueSummary() {
    String lengthHint = property.getValue();
    if (lengthHint.equals(CONST_AUTOMATIC)) {
      return MESSAGES.automaticCaption();
    } else if (lengthHint.equals(CONST_FILL_PARENT)) {
      return MESSAGES.fillParentCaption();
    } else {
      return MESSAGES.dpsSummary(lengthHint);
    }
  }

  @Override
  protected boolean okAction() {
    if (automaticRadioButton.isChecked()) {
      property.setValue(CONST_AUTOMATIC);
    } else if (fillParentRadioButton.isChecked()) {
      property.setValue(CONST_FILL_PARENT);
    } else {
      // Custom length
      String text = customLengthField.getText();
      // Make sure it's a non-negative number.  It is important
      // that this check stay within the custom length case because
      // CONST_AUTOMATIC and CONST_FILL_PARENT are deliberately negative.
      boolean success = false;
      try {
        if (Integer.parseInt(text) >= 0) {
          success = true;
        }
      } catch (NumberFormatException e) {
        // fall through with success == false
      }
      if (!success) {
        Window.alert(MESSAGES.nonnumericInputError());
        return false;
      }
      property.setValue(text);
    }
    return true;
  }
}
