// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Property editor for native Padding and Margin structural bounds.
 * Displays a compact, space-saving "+" directional cross layout.
 */
public class PaddingMarginPropertyEditor extends PropertyEditor {

  private final Grid crossGrid;
  private final TextBox topBox;
  private final TextBox leftBox;
  private final TextBox rightBox;
  private final TextBox bottomBox;

  public PaddingMarginPropertyEditor() {
    crossGrid = new Grid(3, 3);
    // Add styling class to handle clean rendering in the side-panel wrapper
    crossGrid.addStyleName("ode-PaddingMarginGrid");

    topBox = new TextBox();
    leftBox = new TextBox();
    rightBox = new TextBox();
    bottomBox = new TextBox();

    // Enforce clean styling rules
    String boxWidth = "38px";
    topBox.setWidth(boxWidth);
    leftBox.setWidth(boxWidth);
    rightBox.setWidth(boxWidth);
    bottomBox.setWidth(boxWidth);

    // Arrange within the 3x3 structural visual layout grid
    crossGrid.setWidget(0, 1, topBox);
    crossGrid.setWidget(1, 0, leftBox);
    crossGrid.setWidget(1, 1, new HTML("<div style='text-align:center; font-weight:bold; line-height:25px; color:#5a5a5a;'>+</div>"));
    crossGrid.setWidget(1, 2, rightBox);
    crossGrid.setWidget(2, 1, bottomBox);

    // Event handler to capture keystrokes and handle escape cancellations
    KeyUpHandler keyHandler = new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        int keyCode = event.getNativeKeyCode();
        if (keyCode == KeyCodes.KEY_ESCAPE) {
          updateValue(); // Reset fields back to last valid state
          ((TextBox) event.getSource()).setFocus(false);
        } else if (keyCode == KeyCodes.KEY_ENTER) {
          validateAndSetValues();
          ((TextBox) event.getSource()).setFocus(false);
        }
      }
    };

    // Standard BlurHandler tracking ensures values save when a user clicks away
    BlurHandler blurHandler = new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        validateAndSetValues();
      }
    };

    // Wire up events across all four boxes
    TextBox[] boxes = {topBox, leftBox, rightBox, bottomBox};
    for (TextBox box : boxes) {
      box.addKeyUpHandler(keyHandler);
      box.addBlurHandler(blurHandler);
    }

    initWidget(crossGrid);
  }

  @Override
  protected void onUnload() {
    validateAndSetValues();
    super.onUnload();
  }

  @Override
  protected void updateValue() {
    String value = property.getValue();
    if (value != null && value.contains(",")) {
      String[] parts = value.split(",");
      if (parts.length == 4) {
        topBox.setText(parts[0].trim());
        leftBox.setText(parts[1].trim());
        rightBox.setText(parts[2].trim());
        bottomBox.setText(parts[3].trim());
        return;
      }
    }
    // Reliable system defaults
    topBox.setText("0");
    leftBox.setText("0");
    rightBox.setText("0");
    bottomBox.setText("0");
  }

  /**
   * Evaluates input layout integrity, handles text parsing safely,
   * and normalizes malformed inputs instantly without infinite focus alert loops.
   */
  private void validateAndSetValues() {
    String t = parseNumericInput(topBox.getText());
    String l = parseNumericInput(leftBox.getText());
    String r = parseNumericInput(rightBox.getText());
    String b = parseNumericInput(bottomBox.getText());

    String csvValue = t + "," + l + "," + r + "," + b;

    // Only update and trigger a property change event if the values actually changed
    if (!csvValue.equals(property.getValue())) {
      property.setValue(csvValue);
    }
  }

  private String parseNumericInput(String text) {
    String cleanStr = text.trim();
    if (cleanStr.isEmpty()) {
      return "0";
    }
    try {
      // If it's valid code bytecode representation, keep it intact
      Integer.parseInt(cleanStr);
      return cleanStr;
    } catch (NumberFormatException e) {
      // Silently clean bad data to prevent UI lockup loops
      return "0";
    }
  }
}