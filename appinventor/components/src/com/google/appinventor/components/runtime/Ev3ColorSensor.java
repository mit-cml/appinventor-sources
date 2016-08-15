// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import android.os.Handler;

/**
 * A component that provides a high-level interface to a color sensor on a
 * LEGO MINDSTORMS EV3 robot.
 *
 * @author jerry73204@gmail.com (jerry73204)
 * @author spaded06543@gmail.com (Alvin Chang)
 */
@DesignerComponent(version = YaVersion.EV3_COLORSENSOR_COMPONENT_VERSION,
                   description = "A component that provides a high-level interface to a color sensor on a " +
                                 "LEGO MINDSTORMS EV3 robot.",
                   category = ComponentCategory.LEGOMINDSTORMS,
                   nonVisible = true,
                   iconName = "images/legoMindstormsEv3.png")
@SimpleObject
public class Ev3ColorSensor extends LegoMindstormsEv3Sensor implements Deleteable {
  private static final int SENSOR_TYPE = 29;
  private static final int SENSOR_MODE_REFLECTED = 0;
  private static final int SENSOR_MODE_AMBIENT = 1;
  private static final int SENSOR_MODE_COLOR = 2;
  private static final String SENSOR_MODE_REFLECTED_STRING = "reflected";
  private static final String SENSOR_MODE_AMBIENT_STRING = "ambient";
  private static final String SENSOR_MODE_COLOR_STRING = "color";
  private static final int DEFAULT_BOTTOM_OF_RANGE = 30;
  private static final int DEFAULT_TOP_OF_RANGE = 60;
  private static final String DEFAULT_SENSOR_MODE_STRING = SENSOR_MODE_REFLECTED_STRING;
  private static final int DELAY_MILLISECONDS = 50;

  private int mode = 0;
  private String modeString = SENSOR_MODE_REFLECTED_STRING;
  private Handler eventHandler;
  private final Runnable sensorValueChecker;
  private int bottomOfRange;
  private int topOfRange;
  private int previousLightLevel = 0;

  private int previousColor = -1;
  private boolean belowRangeEventEnabled;
  private boolean withinRangeEventEnabled;
  private boolean aboveRangeEventEnabled;
  private boolean colorChangedEventEnabled;

  /**
   * Creates a new Ev3ColorSensor component.
   */
  public Ev3ColorSensor(ComponentContainer container) {
    super(container, "Ev3ColorSensor");

    eventHandler = new Handler();
    sensorValueChecker = new Runnable() {
      public void run() {
        String functionName = "";

        if (bluetooth != null && bluetooth.IsConnected()) {
          if (mode == SENSOR_MODE_COLOR) {
            int currentColor = getSensorValue(functionName);

            if (previousColor < 0) {
              previousColor = currentColor;
              eventHandler.postDelayed(this, DELAY_MILLISECONDS);
              return;
            }

            if (currentColor != previousColor && colorChangedEventEnabled)
              ColorChanged(currentColor, toColorName(functionName, currentColor));

            previousColor = currentColor;
          } else {                // mode == SENSOR_MODE_REFLECTED or mode == SENSOR_MODE_AMBIENT
            int currentLightLevel = getSensorValue(functionName);
            if (previousLightLevel < 0) {
              previousLightLevel = currentLightLevel;
              eventHandler.postDelayed(this, DELAY_MILLISECONDS);
              return;
            }

            // trigger events according to the conditions
            if (currentLightLevel < bottomOfRange) {
              if (belowRangeEventEnabled && previousLightLevel >= bottomOfRange)
                BelowRange();
            } else if (currentLightLevel > topOfRange) {
              if (aboveRangeEventEnabled && previousLightLevel <= topOfRange)
                AboveRange();
            } else {
              if (withinRangeEventEnabled && (previousLightLevel < bottomOfRange || previousLightLevel > topOfRange))
                WithinRange();
            }

            previousLightLevel = currentLightLevel;
          }
        }

        eventHandler.postDelayed(this, DELAY_MILLISECONDS);
      }
    };
    eventHandler.post(sensorValueChecker);

    TopOfRange(DEFAULT_TOP_OF_RANGE);
    BottomOfRange(DEFAULT_BOTTOM_OF_RANGE);
    BelowRangeEventEnabled(false);
    AboveRangeEventEnabled(false);
    WithinRangeEventEnabled(false);
    ColorChangedEventEnabled(false);
    Mode(DEFAULT_SENSOR_MODE_STRING);
  }

  /**
   * It returns the light level in percentage.
   */
  @SimpleFunction(description = "It returns the light level in percentage, or " +
                                "-1 when the light level cannot be read.")
  public int GetLightLevel() {
    if (mode == SENSOR_MODE_COLOR)
      return -1;

    String functionName = "GetLightLevel";
    return getSensorValue(functionName);
  }

  /**
   * It returns the color code for the detected color.
   */
  @SimpleFunction(description = "It returns the color code from 0 to 7 corresponding to no color, black, blue, green, yellow, red, white and brown.")
  public int GetColorCode() {
    if (mode != SENSOR_MODE_COLOR)
      return 0;

    String functionName = "GetColorCode";
    return getSensorValue(functionName);
  }

  /**
   * Returns the name of the detected color.
   */
  @SimpleFunction(description = "Return the color name in one of \"No Color\", \"Black\", \"Blue\", \"Green\", \"Yellow\", \"Red\", \"White\", \"Brown\".")
  public String GetColorName() {
    if (mode != SENSOR_MODE_COLOR)
      return "No Color";

    String functionName = "GetColorName";
    int colorCode = getSensorValue(functionName);
    return toColorName(functionName, colorCode);
  }

  /**
   * Returns the bottom of the range used for the BelowRange, WithinRange,
   * and AboveRange events.
   */
  @SimpleProperty(description = "The bottom of the range used for the BelowRange, WithinRange, " +
                                "and AboveRange events.",
                  category = PropertyCategory.BEHAVIOR)
  public int BottomOfRange() {
    return bottomOfRange;
  }

  /**
   * Specifies the bottom of the range used for the BelowRange, WithinRange,
   * and AboveRange events.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
                    defaultValue = "" + DEFAULT_BOTTOM_OF_RANGE)
  @SimpleProperty
  public void BottomOfRange(int bottomOfRange) {
    this.bottomOfRange = bottomOfRange;
  }

  /**
   * Returns the top of the range used for the BelowRange, WithinRange, and
   * AboveRange events.
   */
  @SimpleProperty(description = "The top of the range used for the BelowRange, WithinRange, and " +
                                "AboveRange events.",
                  category = PropertyCategory.BEHAVIOR)
  public int TopOfRange() {
    return topOfRange;
  }

  /**
   * Specifies the top of the range used for the BelowRange, WithinRange, and
   * AboveRange events.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
                    defaultValue = "" + DEFAULT_TOP_OF_RANGE)
  @SimpleProperty
  public void TopOfRange(int topOfRange) {
    this.topOfRange = topOfRange;
  }

  /**
   * Returns whether the BelowRange event should fire when the light level
   * goes below the BottomOfRange.
   */
  @SimpleProperty(description = "Whether the BelowRange event should fire when the light level" +
                                " goes below the BottomOfRange.",
                  category = PropertyCategory.BEHAVIOR)
  public boolean BelowRangeEventEnabled() {
    return belowRangeEventEnabled;
  }

  /**
   * Specifies whether the BelowRange event should fire when the light level
   * goes below the BottomOfRange.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
                    defaultValue = "False")
  @SimpleProperty
  public void BelowRangeEventEnabled(boolean enabled) {
    belowRangeEventEnabled = enabled;
  }

  @SimpleEvent(description = "Light level has gone below the range.")
  public void BelowRange() {
    EventDispatcher.dispatchEvent(this, "BelowRange");
  }

  /**
   * Returns whether the WithinRange event should fire when the light level
   * goes between the BottomOfRange and the TopOfRange.
   */
  @SimpleProperty(description = "Whether the WithinRange event should fire when the light level " +
                                "goes between the BottomOfRange and the TopOfRange.",
                  category = PropertyCategory.BEHAVIOR)
  public boolean WithinRangeEventEnabled() {
    return withinRangeEventEnabled;
  }

  /**
   * Specifies whether the WithinRange event should fire when the light level
   * goes between the BottomOfRange and the TopOfRange.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
                    defaultValue = "False")
  @SimpleProperty
  public void WithinRangeEventEnabled(boolean enabled) {
    withinRangeEventEnabled = enabled;
  }

  @SimpleEvent(description = "Light level has gone within the range.")
  public void WithinRange() {
    EventDispatcher.dispatchEvent(this, "WithinRange");
  }

  /**
   * Returns whether the AboveRange event should fire when the light level
   * goes above the TopOfRange.
   */
  @SimpleProperty(description = "Whether the AboveRange event should fire when the light level " +
                                "goes above the TopOfRange.",
                  category = PropertyCategory.BEHAVIOR)
  public boolean AboveRangeEventEnabled() {
    return aboveRangeEventEnabled;
  }

  /**
   * Specifies whether the AboveRange event should fire when the light level
   * goes above the TopOfRange.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
                    defaultValue = "False")
  @SimpleProperty
  public void AboveRangeEventEnabled(boolean enabled) {
    aboveRangeEventEnabled = enabled;
  }

  @SimpleEvent(description = "Light level has gone above the range.")
  public void AboveRange() {
    EventDispatcher.dispatchEvent(this, "AboveRange");
  }

  /**
   * Returns whether the ColorChanged event should fire when the DetectColor
   * property is set to True and the detected color changes.
   */
  @SimpleProperty(description = "Whether the ColorChanged event should fire when the Mode" +
                                " property is set to \"color\" and the detected color changes.",
                  category = PropertyCategory.BEHAVIOR)
  public boolean ColorChangedEventEnabled() {
    return colorChangedEventEnabled;
  }

  /**
   * Specifies whether the ColorChanged event should fire when the DetectColor
   * property is set to True and the detected color changes
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
                    defaultValue = "False")
  @SimpleProperty
  public void ColorChangedEventEnabled(boolean enabled) {
    colorChangedEventEnabled = enabled;
  }

  /**
   * Called when the detected color has changed.
   */
  @SimpleEvent(description = "Called when the detected color has changed. The ColorChanged event will occur " +
                             "if the Mode property is set to \"color\" and the ColorChangedEventEnabled property " +
                             "is set to True.")
  public void ColorChanged(int colorCode, String colorName) {
    EventDispatcher.dispatchEvent(this, "ColorChanged", colorCode, colorName);
  }

  private int getSensorValue(String functionName) {
    int level = readInputPercentage(functionName,
                                    0, // assume layer = 0
                                    sensorPortNumber,
                                    SENSOR_TYPE,
                                    mode);

    // map values according to LEGO's convention
    if (mode == SENSOR_MODE_COLOR) {
      switch (level) {
      case 0:
        return 0;
      case 12:
        return 1;
      case 25:
        return 2;
      case 37:
        return 3;
      case 50:
        return 4;
      case 62:
        return 5;
      case 75:
        return 6;
      case 87:
        return 7;
      default:
        return 0;
      }
    } else {
      return level;
    }
  }

  private String toColorName(String functionName, int colorCode) {
    if (mode != SENSOR_MODE_COLOR)
      return "No Color";

    switch (colorCode) {
    case 0:
      return "No Color";
    case 1:
      return "Black";
    case 2:
      return "Blue";
    case 3:
      return "Green";
    case 4:
      return "Yellow";
    case 5:
      return "Red";
    case 6:
      return "White";
    case 7:
      return "Brown";
    default:
      return "No Color";
    }
  }

  /**
   * Specifies the mode of the sensor.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LEGO_EV3_COLOR_SENSOR_MODE,
                    defaultValue = DEFAULT_SENSOR_MODE_STRING)
  @SimpleProperty
  public void Mode(String modeName) {
    String functionName = "Mode";
    try {
      setMode(modeName);
    } catch(IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
    }
  }

  /**
   * Returns the mode of the sensor.
   */
  @SimpleProperty(description = "Get the current sensor mode.",
                  category = PropertyCategory.BEHAVIOR)
  public String Mode() {
    return modeString;
  }

  /**
   * Enter the color detection mode.
   */
  @SimpleFunction(description = "Enter the color detection mode.")
  public void SetColorMode() {
    String functionName = "SetColorMode";
    try {
      setMode(SENSOR_MODE_COLOR_STRING);
    } catch(IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
    }
  }

  /**
   * Make the sensor read the light level with reflected light.
   */
  @SimpleFunction(description = "Make the sensor read the light level with reflected light.")
  public void SetReflectedMode() {
    String functionName = "SetReflectedMode";
    try {
      setMode(SENSOR_MODE_REFLECTED_STRING);
    } catch(IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
    }
  }

  /**
   * Make the sensor read the light level without reflected light.
   */
  @SimpleFunction(description = "Make the sensor read the light level without reflected light.")
  public void SetAmbientMode() {
    String functionName = "SetAmbientMode";
    try{
      setMode(SENSOR_MODE_AMBIENT_STRING);
    } catch(IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
    }
  }

  private void setMode(String newModeString) {
    previousColor = -1;
    previousLightLevel = -1;

    if (SENSOR_MODE_REFLECTED_STRING.equals(newModeString))
      mode = SENSOR_MODE_REFLECTED;
    else if (SENSOR_MODE_AMBIENT_STRING.equals(newModeString))
      mode = SENSOR_MODE_AMBIENT;
    else if (SENSOR_MODE_COLOR_STRING.equals(newModeString))
      mode = SENSOR_MODE_COLOR;
    else
      throw new IllegalArgumentException();

    this.modeString = newModeString;
  }

  // interface Deleteable implementation
  @Override
  public void onDelete() {
    eventHandler.removeCallbacks(sensorValueChecker);
    super.onDelete();
  }
}
