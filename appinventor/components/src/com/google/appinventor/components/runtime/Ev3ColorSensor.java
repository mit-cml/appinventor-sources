// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ColorSensorMode;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import android.os.Handler;

/**
 * ![EV3 component icon](images/legoMindstormsEv3.png)
 *
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
  private static final int DEFAULT_BOTTOM_OF_RANGE = 30;
  private static final int DEFAULT_TOP_OF_RANGE = 60;
  private static final int DELAY_MILLISECONDS = 50;

  private ColorSensorMode mode = ColorSensorMode.Reflected;
  private Handler eventHandler;
  private final Runnable sensorValueChecker;
  private int bottomOfRange;
  private int topOfRange;

  private int previousColor = -1;
  private int previousLightLevel = 0;
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
          if (mode == ColorSensorMode.Color) {
            int currentColor = getSensorValue(functionName);

            if (previousColor < 0) {
              previousColor = currentColor;
              eventHandler.postDelayed(this, DELAY_MILLISECONDS);
              return;
            }

            if (currentColor != previousColor && colorChangedEventEnabled)
              ColorChanged(currentColor, toColorName(currentColor));

            previousColor = currentColor;
          } else { // Reflected or ambient mode.
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
    ModeAbstract(ColorSensorMode.Reflected);
  }

  /**
   * It returns the light level in percentage.
   */
  @SimpleFunction(description = "It returns the light level in percentage, or " +
                                "-1 when the light level cannot be read.")
  public int GetLightLevel() {
    if (mode == ColorSensorMode.Color) {
      return -1;
    }
    return getSensorValue("GetLightLevel");
  }

  /**
   * It returns the color code for the detected color.
   */
  @SimpleFunction(description = "It returns the color code from 0 to 7 corresponding to no color, black, blue, green, yellow, red, white and brown.")
  public int GetColorCode() {
    if (mode != ColorSensorMode.Color) {
      return 0;
    }
    return getSensorValue("GetColorCode");
  }

  /**
   * Returns the name of the detected color.
   */
  @SimpleFunction(description = "Return the color name in one of \"No Color\", \"Black\", \"Blue\", \"Green\", \"Yellow\", \"Red\", \"White\", \"Brown\".")
  public String GetColorName() {
    if (mode != ColorSensorMode.Color) {
      return "No Color";
    }
    return toColorName(getSensorValue("GetColorName"));
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
                                    mode.toInt());

    if (mode != ColorSensorMode.Color) {
      return level;  // No need to clean value.
    }

    // Map values according to LEGO's convention.
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
  }

  private String toColorName(int colorCode) {
    if (mode != ColorSensorMode.Color) {
      return "No Color";
    }

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
                    defaultValue = "reflected")
  @SimpleProperty
  public void Mode(@Options(ColorSensorMode.class) String modeName) {
    // Make sure modeName is a valid ColorSensorMode.
    ColorSensorMode mode = ColorSensorMode.fromUnderlyingValue(modeName);
    if (mode == null) {
      form.dispatchErrorOccurredEvent(
          this, "Mode", ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, modeName);
      return;
    }
    setMode(mode);
  }

  /**
   * Sets the sensing mode of this color sensor.
   */
  @SuppressWarnings("RegularMethodName")
  public void ModeAbstract(ColorSensorMode mode) {
    setMode(mode);
  }

  /**
   * Returns the current sensing mode of this color sensor.
   * @return the current sensing mode of this color sensor.
   */
  @SuppressWarnings({"RegularMethodName", "unused"})
  public ColorSensorMode ModeAbstract() {
    return mode;
  }

  /**
   * The current mode of the sensor. One of:
   *     Reflected: Senses the current light level including light reflected by the sensor.
   *     Ambient: Senses the current light level *not* including light reflected by the sensor.
   *     Color: Senses the color the sensor is pointing at.
   */
  @SimpleProperty(description = "Get the current sensor mode.",
                  category = PropertyCategory.BEHAVIOR)
  public @Options(ColorSensorMode.class) String Mode() {
    return mode.toUnderlyingValue();
  }

  /**
   * Enter the color detection mode.
   */
  @SimpleFunction(description = "Enter the color detection mode.")
  @Deprecated
  public void SetColorMode() {
    setMode(ColorSensorMode.Color);
  }

  /**
   * Make the sensor read the light level with reflected light.
   */
  @SimpleFunction(description = "Make the sensor read the light level with reflected light.")
  @Deprecated
  public void SetReflectedMode() {
    setMode(ColorSensorMode.Reflected);
  }

  /**
   * Make the sensor read the light level without reflected light.
   */
  @SimpleFunction(description = "Make the sensor read the light level without reflected light.")
  @Deprecated
  public void SetAmbientMode() {
    setMode(ColorSensorMode.Ambient);
  }

  /**
   * Sets the current mode of this sensor, and resets previousColor and previousLightLevel.
   */
  private void setMode(ColorSensorMode newMode) {
    previousColor = -1;
    previousLightLevel = -1;
    mode = newMode;
  }

  // interface Deleteable implementation
  @Override
  public void onDelete() {
    eventHandler.removeCallbacks(sensorValueChecker);
    super.onDelete();
  }
}
