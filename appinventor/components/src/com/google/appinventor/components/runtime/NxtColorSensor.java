// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
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

import java.util.HashMap;
import java.util.Map;

/**
 * A component that provides a high-level interface to a color sensor on a LEGO
 * MINDSTORMS NXT robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.NXT_COLORSENSOR_COMPONENT_VERSION,
    description = "A component that provides a high-level interface to a color sensor on a " +
    "LEGO MINDSTORMS NXT robot.",
    category = ComponentCategory.LEGOMINDSTORMS,
    nonVisible = true,
    iconName = "images/legoMindstormsNxt.png")
@SimpleObject
public class NxtColorSensor extends LegoMindstormsNxtSensor implements Deleteable {

  private enum State { UNKNOWN, BELOW_RANGE, WITHIN_RANGE, ABOVE_RANGE }
  private static final String DEFAULT_SENSOR_PORT = "3";
  private static final int DEFAULT_BOTTOM_OF_RANGE = 256;
  private static final int DEFAULT_TOP_OF_RANGE = 767;

  static final int SENSOR_TYPE_COLOR_FULL = 0x0D;  // Color detector mode
  static final int SENSOR_TYPE_COLOR_RED = 0x0E;   // Light sensor mode with red light on
  static final int SENSOR_TYPE_COLOR_GREEN = 0x0F; // Light sensor mode with green light on
  static final int SENSOR_TYPE_COLOR_BLUE = 0x10;  // Light sensor mode with blue light on
  static final int SENSOR_TYPE_COLOR_NONE = 0x11;  // Light sensor mode with no light

  private static final Map<Integer, Integer> mapColorToSensorType;
  private static final Map<Integer, Integer> mapSensorValueToColor;
  static {
    mapColorToSensorType = new HashMap<Integer, Integer>();
    mapColorToSensorType.put(Component.COLOR_RED, SENSOR_TYPE_COLOR_RED);
    mapColorToSensorType.put(Component.COLOR_GREEN, SENSOR_TYPE_COLOR_GREEN);
    mapColorToSensorType.put(Component.COLOR_BLUE, SENSOR_TYPE_COLOR_BLUE);
    mapColorToSensorType.put(Component.COLOR_NONE, SENSOR_TYPE_COLOR_NONE);

    mapSensorValueToColor = new HashMap<Integer, Integer>();
    mapSensorValueToColor.put(0x01, Component.COLOR_BLACK);
    mapSensorValueToColor.put(0x02, Component.COLOR_BLUE);
    mapSensorValueToColor.put(0x03, Component.COLOR_GREEN);
    mapSensorValueToColor.put(0x04, Component.COLOR_YELLOW);
    mapSensorValueToColor.put(0x05, Component.COLOR_RED);
    mapSensorValueToColor.put(0x06, Component.COLOR_WHITE);
  }

  private boolean detectColor;
  private Handler handler;
  private final Runnable sensorReader;

  // Fields related to detecting color
  private int previousColor;
  private boolean colorChangedEventEnabled;

  // Fields related to detecting light
  private State previousState;
  private int bottomOfRange;
  private int topOfRange;
  private boolean belowRangeEventEnabled;
  private boolean withinRangeEventEnabled;
  private boolean aboveRangeEventEnabled;
  private int generateColor;

  /**
   * Creates a new NxtColorSensor component.
   */
  public NxtColorSensor(ComponentContainer container) {
    super(container, "NxtColorSensor");
    handler = new Handler();
    previousState = State.UNKNOWN;
    previousColor = Component.COLOR_NONE;
    sensorReader = new Runnable() {
      public void run() {
        if (bluetooth != null && bluetooth.IsConnected()) {
          if (detectColor) {
            // Detecting color
            SensorValue<Integer> sensorValue = getColorValue("");
            if (sensorValue.valid) {
              int currentColor = sensorValue.value;

              if (currentColor != previousColor) {
                ColorChanged(currentColor);
              }

              previousColor = currentColor;
            }

          } else {
            // Detecting light
            SensorValue<Integer> sensorValue = getLightValue("");
            if (sensorValue.valid) {
              State currentState;
              if (sensorValue.value < bottomOfRange) {
                currentState = State.BELOW_RANGE;
              } else if (sensorValue.value > topOfRange) {
                currentState = State.ABOVE_RANGE;
              } else {
                currentState = State.WITHIN_RANGE;
              }

              if (currentState != previousState) {
                if (currentState == State.BELOW_RANGE && belowRangeEventEnabled) {
                  BelowRange();
                }
                if (currentState == State.WITHIN_RANGE && withinRangeEventEnabled) {
                  WithinRange();
                }
                if (currentState == State.ABOVE_RANGE && aboveRangeEventEnabled) {
                  AboveRange();
                }
              }

              previousState = currentState;
            }
          }
        }
        if (isHandlerNeeded()) {
          handler.post(sensorReader);
        }
      }
    };

    SensorPort(DEFAULT_SENSOR_PORT);

    // Detecting color
    DetectColor(true);
    ColorChangedEventEnabled(false);

    // Detecting light
    BottomOfRange(DEFAULT_BOTTOM_OF_RANGE);
    TopOfRange(DEFAULT_TOP_OF_RANGE);
    BelowRangeEventEnabled(false);
    WithinRangeEventEnabled(false);
    AboveRangeEventEnabled(false);
    GenerateColor(Component.COLOR_NONE);
  }

  @Override
  protected void initializeSensor(String functionName) {
    int sensorType = detectColor ? SENSOR_TYPE_COLOR_FULL : mapColorToSensorType.get(generateColor);
    setInputMode(functionName, port, sensorType, SENSOR_MODE_RAWMODE);
    resetInputScaledValue(functionName, port);
  }

  /**
   * Specifies the sensor port that the sensor is connected to.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LEGO_NXT_SENSOR_PORT,
      defaultValue = DEFAULT_SENSOR_PORT)
  @SimpleProperty(userVisible = false)
  public void SensorPort(String sensorPortLetter) {
    setSensorPort(sensorPortLetter);
  }

  /**
   * Returns whether the sensor should detect color or light. True indicates that
   * the sensor should detect color; False indicates that the sensor should
   * detect light.
   *
   * The ColorChanged event will not occur if the DetectColor property is set
   * to False.
   * The BelowRange, WithinRange, and AboveRange events will not occur if the
   * DetectColor property is set to True.
   * The sensor will not generate color when the DetectColor property is set to
   * True.
   */
  @SimpleProperty(description = "Whether the sensor should detect color or light. " +
      "True indicates that the sensor should detect color; False indicates that the sensor " +
      "should detect light. " +
      "If the DetectColor property is set to True, the BelowRange, WithinRange, and AboveRange " +
      "events will not occur and the sensor will not generate color. " +
      "If the DetectColor property is set to False, the ColorChanged event will not occur.",
      category = PropertyCategory.BEHAVIOR)
  public boolean DetectColor() {
    return detectColor;
  }

  /**
   * Specifies whether the sensor should detect color light. True indicates
   * that the sensor should detect color; False indicates that the sensor
   * should detect light.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty
  public void DetectColor(boolean detectColor) {
    boolean handlerWasNeeded = isHandlerNeeded();

    this.detectColor = detectColor;
    if (bluetooth != null && bluetooth.IsConnected()) {
      initializeSensor("DetectColor");
    }

    boolean handlerIsNeeded = isHandlerNeeded();
    if (handlerWasNeeded && !handlerIsNeeded) {
      handler.removeCallbacks(sensorReader);
    }
    previousColor = Component.COLOR_NONE;
    previousState = State.UNKNOWN;
    if (!handlerWasNeeded && handlerIsNeeded) {
      handler.post(sensorReader);
    }
  }

  // Methods for detecting color

  @SimpleFunction(description = "Returns the current detected color, or the color None if the " +
      "color can not be read or if the DetectColor property is set to False.")
  public int GetColor() {
    String functionName = "GetColor";
    if (!checkBluetooth(functionName)) {
      return Component.COLOR_NONE;
    }
    if (!detectColor) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_CANNOT_DETECT_COLOR);
      return Component.COLOR_NONE;
    }

    SensorValue<Integer> sensorValue = getColorValue(functionName);
    if (sensorValue.valid) {
      return sensorValue.value;
    }

    // invalid response
    return Component.COLOR_NONE;
  }

  private SensorValue<Integer> getColorValue(String functionName) {
    byte[] returnPackage = getInputValues(functionName, port);
    if (returnPackage != null) {
      boolean valid = getBooleanValueFromBytes(returnPackage, 4);
      if (valid) {
        int scaledValue = getSWORDValueFromBytes(returnPackage, 12);
        if (mapSensorValueToColor.containsKey(scaledValue)) {
          int color = mapSensorValueToColor.get(scaledValue);
          return new SensorValue<Integer>(true, color);
        }
      }
    }

    // invalid response
    return new SensorValue<Integer>(false, null);
  }

  /**
   * Returns whether the ColorChanged event should fire when the DetectColor
   * property is set to True and the detected color changes.
   */
  @SimpleProperty(description = "Whether the ColorChanged event should fire when the DetectColor" +
      " property is set to True and the detected color changes.",
      category = PropertyCategory.BEHAVIOR)
  public boolean ColorChangedEventEnabled() {
    return colorChangedEventEnabled;
  }

  /**
   * Specifies whether the ColorChanged event should fire when the DetectColor
   * property is set to True and the detected color changes
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty
  public void ColorChangedEventEnabled(boolean enabled) {
    boolean handlerWasNeeded = isHandlerNeeded();

    colorChangedEventEnabled = enabled;

    boolean handlerIsNeeded = isHandlerNeeded();
    if (handlerWasNeeded && !handlerIsNeeded) {
      handler.removeCallbacks(sensorReader);
    }
    if (!handlerWasNeeded && handlerIsNeeded) {
      previousColor = Component.COLOR_NONE;
      handler.post(sensorReader);
    }
  }

  @SimpleEvent(description = "Detected color has changed. " +
      "The ColorChanged event will not occur if the DetectColor property is set to False or if " +
      "the ColorChangedEventEnabled property is set to False.")
  public void ColorChanged(int color) {
    EventDispatcher.dispatchEvent(this, "ColorChanged", color);
  }

  // Methods for detecting light

  @SimpleFunction(description = "Returns the current light level as a value between 0 and 1023, " +
      "or -1 if the light level can not be read or if the DetectColor property is set to True.")
  public int GetLightLevel() {
    String functionName = "GetLightLevel";
    if (!checkBluetooth(functionName)) {
      return -1;
    }
    if (detectColor) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_CANNOT_DETECT_LIGHT);
      return -1;
    }

    SensorValue<Integer> sensorValue = getLightValue(functionName);
    if (sensorValue.valid) {
      return sensorValue.value;
    }

    // invalid response
    return -1;
  }

  private SensorValue<Integer> getLightValue(String functionName) {
    byte[] returnPackage = getInputValues(functionName, port);
    if (returnPackage != null) {
      boolean valid = getBooleanValueFromBytes(returnPackage, 4);
      if (valid) {
        int normalizedValue = getUWORDValueFromBytes(returnPackage, 10);
        return new SensorValue<Integer>(true, normalizedValue);
      }
    }

    // invalid response
    return new SensorValue<Integer>(false, null);
  }

  /**
   * Returns the bottom of the range used for the BelowRange, WithinRange,
   * and AboveRange events.
   */
  @SimpleProperty(description = "The bottom of the range used for the BelowRange, WithinRange," +
      " and AboveRange events.",
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
    previousState = State.UNKNOWN;
  }

  /**
   * Returns the top of the range used for the BelowRange, WithinRange, and
   * AboveRange events.
   */
  @SimpleProperty(description = "The top of the range used for the BelowRange, WithinRange, and" +
      " AboveRange events.",
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
    previousState = State.UNKNOWN;
  }

  /**
   * Returns whether the BelowRange event should fire when the DetectColor
   * property is set to False and the light level goes below the BottomOfRange.
   */
  @SimpleProperty(description = "Whether the BelowRange event should fire when the DetectColor" +
      " property is set to False and the light level goes below the BottomOfRange.",
      category = PropertyCategory.BEHAVIOR)
  public boolean BelowRangeEventEnabled() {
    return belowRangeEventEnabled;
  }

  /**
   * Specifies whether the BelowRange event should fire when the DetectColor
   * property is set to False and the light level goes below the BottomOfRange.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty
  public void BelowRangeEventEnabled(boolean enabled) {
    boolean handlerWasNeeded = isHandlerNeeded();

    belowRangeEventEnabled = enabled;

    boolean handlerIsNeeded = isHandlerNeeded();
    if (handlerWasNeeded && !handlerIsNeeded) {
      handler.removeCallbacks(sensorReader);
    }
    if (!handlerWasNeeded && handlerIsNeeded) {
      previousState = State.UNKNOWN;
      handler.post(sensorReader);
    }
  }

  @SimpleEvent(description = "Light level has gone below the range. " +
      "The BelowRange event will not occur if the DetectColor property is set to True or if " +
      "the BelowRangeEventEnabled property is set to False.")
  public void BelowRange() {
    EventDispatcher.dispatchEvent(this, "BelowRange");
  }

  /**
   * Returns whether the WithinRange event should fire when the DetectColor
   * property is set to False and the light level goes between the
   * BottomOfRange and the TopOfRange.
   */
  @SimpleProperty(description = "Whether the WithinRange event should fire when the DetectColor" +
      " property is set to False and the light level goes between the BottomOfRange and the " +
      "TopOfRange.",
      category = PropertyCategory.BEHAVIOR)
  public boolean WithinRangeEventEnabled() {
    return withinRangeEventEnabled;
  }

  /**
   * Specifies whether the WithinRange event should fire when the DetectColor
   * property is set to False and the light level goes between the
   * BottomOfRange and the TopOfRange.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty
  public void WithinRangeEventEnabled(boolean enabled) {
    boolean handlerWasNeeded = isHandlerNeeded();

    withinRangeEventEnabled = enabled;

    boolean handlerIsNeeded = isHandlerNeeded();
    if (handlerWasNeeded && !handlerIsNeeded) {
      handler.removeCallbacks(sensorReader);
    }
    if (!handlerWasNeeded && handlerIsNeeded) {
      previousState = State.UNKNOWN;
      handler.post(sensorReader);
    }
  }

  @SimpleEvent(description = "Light level has gone within the range. " +
      "The WithinRange event will not occur if the DetectColor property is set to True or if " +
      "the WithinRangeEventEnabled property is set to False.")
  public void WithinRange() {
    EventDispatcher.dispatchEvent(this, "WithinRange");
  }

  /**
   * Returns whether the AboveRange event should fire when the DetectColor
   * property is set to False and the light level goes above the TopOfRange.
   */
  @SimpleProperty(description = "Whether the AboveRange event should fire when the DetectColor" +
      " property is set to False and the light level goes above the TopOfRange.",
      category = PropertyCategory.BEHAVIOR)
  public boolean AboveRangeEventEnabled() {
    return aboveRangeEventEnabled;
  }

  /**
   * Specifies whether the AboveRange event should fire when the DetectColor
   * property is set to False and the light level goes above the TopOfRange.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty
  public void AboveRangeEventEnabled(boolean enabled) {
    boolean handlerWasNeeded = isHandlerNeeded();

    aboveRangeEventEnabled = enabled;

    boolean handlerIsNeeded = isHandlerNeeded();
    if (handlerWasNeeded && !handlerIsNeeded) {
      handler.removeCallbacks(sensorReader);
    }
    if (!handlerWasNeeded && handlerIsNeeded) {
      previousState = State.UNKNOWN;
      handler.post(sensorReader);
    }
  }

  @SimpleEvent(description = "Light level has gone above the range. " +
      "The AboveRange event will not occur if the DetectColor property is set to True or if " +
      "the AboveRangeEventEnabled property is set to False.")
  public void AboveRange() {
    EventDispatcher.dispatchEvent(this, "AboveRange");
  }

  /**
   * Returns the color that should generated by the sensor.
   * Only None, Red, Green, or Blue are valid values.
   * The sensor will not generate color when the DetectColor property is set to
   * True.
   */
  @SimpleProperty(description = "The color that should generated by the sensor. " +
      "Only None, Red, Green, or Blue are valid values. " +
      "The sensor will not generate color when the DetectColor property is set to True.",
      category = PropertyCategory.BEHAVIOR)
  public int GenerateColor() {
    return generateColor;
  }

  /**
   * Specifies the color that should generated by the sensor.
   * Only None, Red, Green, or Blue are valid values.
   * The sensor will not generate color when the DetectColor property is set to
   * True.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LEGO_NXT_GENERATED_COLOR,
  defaultValue = Component.DEFAULT_VALUE_COLOR_NONE)
  @SimpleProperty
  public void GenerateColor(int generateColor) {
    String functionName = "GenerateColor";
    if (mapColorToSensorType.containsKey(generateColor)) {
      this.generateColor = generateColor;
      if (bluetooth != null && bluetooth.IsConnected()) {
        initializeSensor(functionName);
      }
    } else {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_GENERATE_COLOR);
    }
  }

  private boolean isHandlerNeeded() {
    if (detectColor) {
      return colorChangedEventEnabled;
    } else {
      return belowRangeEventEnabled || withinRangeEventEnabled || aboveRangeEventEnabled;
    }
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    handler.removeCallbacks(sensorReader);
    super.onDelete();
  }
}
