// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
  private final RadioButton percentfillRadioButton;
  private final TextBox customLengthField;
  private final TextBox percentLengthField;


  public YoungAndroidLengthPropertyEditor() {
    this(true);
  }

  /**
   * Creates a new length property editor.
   *
   * @param includePercent  whether to include percent of screen option
   */
  public YoungAndroidLengthPropertyEditor(boolean includePercent) {
    // The radio button group cannot be shared across all instances, so we append a unique id.
    int uniqueId = ++uniqueIdSeed;
    String radioButtonGroup = "LengthType-" + uniqueId;
    automaticRadioButton = new RadioButton(radioButtonGroup, MESSAGES.automaticCaption());
    fillParentRadioButton = new RadioButton(radioButtonGroup, MESSAGES.fillParentCaption());
    percentfillRadioButton = new RadioButton(radioButtonGroup);
    customLengthRadioButton = new RadioButton(radioButtonGroup);
    customLengthField = new TextBox();
    customLengthField.setVisibleLength(4);
    customLengthField.setMaxLength(4);
    percentLengthField = new TextBox();
    percentLengthField.setVisibleLength(4);
    percentLengthField.setMaxLength(4);

    Panel customRow = new HorizontalPanel();
    customRow.add(customLengthRadioButton);
    customRow.add(customLengthField);
    Label pixels = new Label(MESSAGES.pixelsCaption());
    pixels.setStylePrimaryName("ode-PixelsLabel");
    customRow.add(pixels);

    Panel percentRow = new HorizontalPanel();
    percentRow.add(percentfillRadioButton);
    percentRow.add(percentLengthField);
    Label percent = new Label(MESSAGES.percentCaption());
    percent.setStylePrimaryName("ode-PixelsLabel"); // recycle css definition
    percentRow.add(percent);

    Panel panel = new VerticalPanel();
    panel.add(automaticRadioButton);
    panel.add(fillParentRadioButton);
    panel.add(customRow);

    if ( includePercent ) {
      panel.add(percentRow);
    }

    automaticRadioButton.addValueChangeHandler(new ValueChangeHandler() {
      @Override
      public void onValueChange(ValueChangeEvent event) {
        // Clear the custom and percent length fields.
        customLengthField.setText("");
        percentLengthField.setText("");
      }
    });
    fillParentRadioButton.addValueChangeHandler(new ValueChangeHandler() {
      @Override
      public void onValueChange(ValueChangeEvent event) {
        // Clear the custom and percent length fields.
        customLengthField.setText("");
        percentLengthField.setText("");
      }
    });
    customLengthField.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // If the user clicks on the custom length field, but the radio button for a custom length
        // is not checked, check it.
        if (!customLengthRadioButton.isChecked()) {
          customLengthRadioButton.setChecked(true);
          percentLengthField.setText("");
        }
      }
    });

    percentLengthField.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // If the user clicks on the percent length field, but the radio button for a custom length
        // is not checked, check it.
        if (!percentfillRadioButton.isChecked()) {
          percentfillRadioButton.setChecked(true);
          customLengthField.setText("");
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
      int v = Integer.parseInt(propertyValue);
      if (v <= MockVisibleComponent.LENGTH_PERCENT_TAG) {
        v = (-v) + MockVisibleComponent.LENGTH_PERCENT_TAG;
        percentfillRadioButton.setChecked(true);
        percentLengthField.setText("" + v);
      } else {
        customLengthRadioButton.setChecked(true);
        customLengthField.setText(propertyValue);
      }
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
      int v = Integer.parseInt(lengthHint);
      if (v <= MockVisibleComponent.LENGTH_PERCENT_TAG) {
        v = (-v) + MockVisibleComponent.LENGTH_PERCENT_TAG;
        return MESSAGES.percentSummary("" + v);
      } else {
        return MESSAGES.pixelsSummary(lengthHint);
      }
    }
  }

  @Override
  protected boolean okAction() {
    if (automaticRadioButton.isChecked()) {
      property.setValue(CONST_AUTOMATIC);
    } else if (fillParentRadioButton.isChecked()) {
      property.setValue(CONST_FILL_PARENT);
    } else if (customLengthRadioButton.isChecked()) {
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
    } else {                    // Percent field!
      String text = percentLengthField.getText();
      boolean success = false;
      try {
        int v = Integer.parseInt(text);
        if (v > 0 && v <= 100) {
          success = true;
          property.setValue("" + (-v + MockVisibleComponent.LENGTH_PERCENT_TAG));
        }
      } catch (NumberFormatException e) {
        // fall through with success == false
      }
      if (!success) {
        Window.alert(MESSAGES.nonvalidPercentValue());
        return false;
      }
    }
    return true;
  }
}
