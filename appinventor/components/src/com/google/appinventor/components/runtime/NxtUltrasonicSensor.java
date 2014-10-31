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

import android.os.Handler;

/**
 * A component that provides a high-level interface to an ultrasonic sensor on a LEGO
 * MINDSTORMS NXT robot.
 *
 */
@DesignerComponent(version = YaVersion.NXT_ULTRASONICSENSOR_COMPONENT_VERSION,
    description = "A component that provides a high-level interface to an ultrasonic sensor on a " +
    "LEGO MINDSTORMS NXT robot.",
    category = ComponentCategory.LEGOMINDSTORMS,
    nonVisible = true,
    iconName = "images/legoMindstormsNxt.png")
@SimpleObject
public class NxtUltrasonicSensor extends LegoMindstormsNxtSensor implements Deleteable {

  private enum State { UNKNOWN, BELOW_RANGE, WITHIN_RANGE, ABOVE_RANGE }
  private static final String DEFAULT_SENSOR_PORT = "4";
  private static final int DEFAULT_BOTTOM_OF_RANGE = 30;
  private static final int DEFAULT_TOP_OF_RANGE = 90;

  private Handler handler;
  private final Runnable sensorReader;
  private State previousState;
  private int bottomOfRange;
  private int topOfRange;
  private boolean belowRangeEventEnabled;
  private boolean withinRangeEventEnabled;
  private boolean aboveRangeEventEnabled;

  /**
   * Creates a new NxtUltrasonicSensor component.
   */
  public NxtUltrasonicSensor(ComponentContainer container) {
    super(container, "NxtUltrasonicSensor");
    handler = new Handler();
    previousState = State.UNKNOWN;
    sensorReader = new Runnable() {
      public void run() {
        if (bluetooth != null && bluetooth.IsConnected()) {
          SensorValue<Integer> sensorValue = getDistanceValue("");
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
        if (isHandlerNeeded()) {
          handler.post(sensorReader);
        }
      }
    };

    SensorPort(DEFAULT_SENSOR_PORT);
    BottomOfRange(DEFAULT_BOTTOM_OF_RANGE);
    TopOfRange(DEFAULT_TOP_OF_RANGE);
    BelowRangeEventEnabled(false);
    WithinRangeEventEnabled(false);
    AboveRangeEventEnabled(false);
  }

  @Override
  protected void initializeSensor(String functionName) {
    setInputMode(functionName, port, SENSOR_TYPE_LOWSPEED_9V, SENSOR_MODE_RAWMODE);
    configureUltrasonicSensor(functionName);
  }

  private void configureUltrasonicSensor(String functionName) {
    // Set to continuous read mode.
    byte[] data = new byte[3];
    data[0] = (byte) 0x02;
    data[1] = (byte) 0x41;
    data[2] = (byte) 0x02;
    lsWrite(functionName, port, data, 0);
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

  @SimpleFunction(description = "Returns the current distance in centimeters as a value between " +
      "0 and 254, or -1 if the distance can not be read.")
  public int GetDistance() {
    String functionName = "GetDistance";
    if (!checkBluetooth(functionName)) {
      return -1;
    }

    SensorValue<Integer> sensorValue = getDistanceValue(functionName);
    if (sensorValue.valid) {
      return sensorValue.value;
    }

    // invalid response
    return -1;
  }

  private SensorValue<Integer> getDistanceValue(String functionName) {
    // Send command to initiate read
    byte[] data = new byte[2];
    data[0] = (byte) 0x02;
    data[1] = (byte) 0x42;
    lsWrite(functionName, port, data, 1);

    // Wait for read data to be ready, but only try 3 times.
    for (int i = 0; i < 3; i++) {
      int countAvailableBytes = lsGetStatus(functionName, port);
      if (countAvailableBytes > 0) {
        // Read data.
        byte[] returnPackage = lsRead(functionName, port);
        if (returnPackage != null) {
          int value = getUBYTEValueFromBytes(returnPackage, 4);
          if (value >= 0 && value <= 254) {
            return new SensorValue<Integer>(true, value);
          }
        }
        break;
      }
    }

    // invalid response or not ready after 3 tries
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
   * Returns whether the BelowRange event should fire when the distance
   * goes below the BottomOfRange.
   */
  @SimpleProperty(description = "Whether the BelowRange event should fire when the distance" +
      " goes below the BottomOfRange.",
      category = PropertyCategory.BEHAVIOR)
  public boolean BelowRangeEventEnabled() {
    return belowRangeEventEnabled;
  }

  /**
   * Specifies whether the BelowRange event should fire when the distance
   * goes below the BottomOfRange.
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

  @SimpleEvent(description = "Distance has gone below the range.")
  public void BelowRange() {
    EventDispatcher.dispatchEvent(this, "BelowRange");
  }

  /**
   * Returns whether the WithinRange event should fire when the distance
   * goes between the BottomOfRange and the TopOfRange.
   */
  @SimpleProperty(description = "Whether the WithinRange event should fire when the distance" +
      " goes between the BottomOfRange and the TopOfRange.",
      category = PropertyCategory.BEHAVIOR)
  public boolean WithinRangeEventEnabled() {
    return withinRangeEventEnabled;
  }

  /**
   * Specifies whether the WithinRange event should fire when the distance
   * goes between the BottomOfRange and the TopOfRange.
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

  @SimpleEvent(description = "Distance has gone within the range.")
  public void WithinRange() {
    EventDispatcher.dispatchEvent(this, "WithinRange");
  }

  /**
   * Returns whether the AboveRange event should fire when the distance
   * goes above the TopOfRange.
   */
  @SimpleProperty(description = "Whether the AboveRange event should fire when the distance" +
      " goes above the TopOfRange.",
      category = PropertyCategory.BEHAVIOR)
  public boolean AboveRangeEventEnabled() {
    return aboveRangeEventEnabled;
  }

  /**
   * Specifies whether the AboveRange event should fire when the distance
   * goes above the TopOfRange.
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

  @SimpleEvent(description = "Distance has gone above the range.")
  public void AboveRange() {
    EventDispatcher.dispatchEvent(this, "AboveRange");
  }

  private boolean isHandlerNeeded() {
    return belowRangeEventEnabled || withinRangeEventEnabled || aboveRangeEventEnabled;
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    handler.removeCallbacks(sensorReader);
    super.onDelete();
  }
}
