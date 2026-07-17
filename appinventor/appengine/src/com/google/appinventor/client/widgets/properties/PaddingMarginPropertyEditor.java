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
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Property editor for native Padding and Margin structural bounds.
 * Displays a compact, space-saving "+" directional cross layout.
 *
 * Values are stored/round-tripped as a JSON string of the form:
 *   {"top":T,"left":L,"right":R,"bottom":B}
 */
public class PaddingMarginPropertyEditor extends PropertyEditor {

  private static final String KEY_TOP = "top";
  private static final String KEY_LEFT = "left";
  private static final String KEY_RIGHT = "right";
  private static final String KEY_BOTTOM = "bottom";

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
    int[] sides = parseJsonSides(value);
    if (sides != null) {
      topBox.setText(String.valueOf(sides[0]));
      leftBox.setText(String.valueOf(sides[1]));
      rightBox.setText(String.valueOf(sides[2]));
      bottomBox.setText(String.valueOf(sides[3]));
      return;
    }
    // Reliable system defaults
    topBox.setText("0");
    leftBox.setText("0");
    rightBox.setText("0");
    bottomBox.setText("0");
  }

  /**
   * Parses a JSON string of the form {"top":T,"left":L,"right":R,"bottom":B}
   * into a 4-element int[] {top, left, right, bottom}. Returns null if the
   * input is null, empty, or malformed in any way.
   */
  private int[] parseJsonSides(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    try {
      JSONValue parsed = JSONParser.parseStrict(value);
      JSONObject obj = parsed.isObject();
      if (obj == null) {
        return null;
      }
      int t = getIntOrZero(obj, KEY_TOP);
      int l = getIntOrZero(obj, KEY_LEFT);
      int r = getIntOrZero(obj, KEY_RIGHT);
      int b = getIntOrZero(obj, KEY_BOTTOM);
      return new int[]{t, l, r, b};
    } catch (JSONException | IllegalArgumentException e) {
      // Malformed JSON — treat as absent, caller falls back to defaults
      return null;
    }
  }

  private int getIntOrZero(JSONObject obj, String key) {
    JSONValue v = obj.get(key);
    if (v == null) {
      return 0;
    }
    JSONNumber num = v.isNumber();
    return num != null ? (int) num.doubleValue() : 0;
  }

  /**
   * Evaluates input layout integrity, handles text parsing safely,
   * and normalizes malformed inputs instantly without infinite focus alert loops.
   */
  private void validateAndSetValues() {
    int t = parseNumericInput(topBox.getText());
    int l = parseNumericInput(leftBox.getText());
    int r = parseNumericInput(rightBox.getText());
    int b = parseNumericInput(bottomBox.getText());

    // Reflect any silently-cleaned values back into the text boxes
    topBox.setText(String.valueOf(t));
    leftBox.setText(String.valueOf(l));
    rightBox.setText(String.valueOf(r));
    bottomBox.setText(String.valueOf(b));

    JSONObject json = new JSONObject();
    json.put(KEY_TOP, new JSONNumber(t));
    json.put(KEY_LEFT, new JSONNumber(l));
    json.put(KEY_RIGHT, new JSONNumber(r));
    json.put(KEY_BOTTOM, new JSONNumber(b));
    String jsonValue = json.toString();

    // Only update and trigger a property change event if the values actually changed
    if (!jsonValue.equals(property.getValue())) {
      property.setValue(jsonValue);
    }
  }

  private int parseNumericInput(String text) {
    String cleanStr = text.trim();
    if (cleanStr.isEmpty()) {
      return 0;
    }
    try {
      return Integer.parseInt(cleanStr);
    } catch (NumberFormatException e) {
      // Silently clean bad data to prevent UI lockup loops
      return 0;
    }
  }
}