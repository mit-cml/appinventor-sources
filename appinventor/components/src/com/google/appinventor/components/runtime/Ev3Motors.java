// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.Ev3Constants;
import com.google.appinventor.components.runtime.util.Ev3BinaryParser;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import android.os.Handler;

/**
 * ![EV3 component icon](images/legoMindstormsEv3.png)
 *
 * A component that provides both high- and low-level interfaces to
 * control the motors on LEGO MINDSTORMS EV3.
 *
 * @author jerry73204@gmail.com (jerry73204)
 * @author spaded06543@gmail.com (Alvin Chang)
 */
@DesignerComponent(version = YaVersion.EV3_MOTORS_COMPONENT_VERSION,
                   description = "A component that provides both high- and low-level interfaces to a LEGO MINDSTORMS EV3 " +
                                 "robot, with functions that can control the motors.",
                   category = ComponentCategory.LEGOMINDSTORMS,
                   nonVisible = true,
                   iconName = "images/legoMindstormsEv3.png")
@SimpleObject
public class Ev3Motors extends LegoMindstormsEv3Base {
  private static final int DELAY_MILLISECONDS = 50;
  private static final String DEFAULT_MOTOR_PORTS = "ABC";
  private static final double DEFAULT_WHEEL_DIAMETER = 4.32;

  private int motorPortBitField = 1;
  private double wheelDiameter = DEFAULT_WHEEL_DIAMETER;
  private boolean directionReversed = false;
  private boolean regulationEnabled = true;
  private boolean stopBeforeDisconnect = true;
  private boolean tachoCountChangedEventEnabled = false;
  private final Runnable sensorValueChecker;
  private Handler eventHandler;
  private int previousValue = 0;
  private boolean ifReset = false;

  /**
   * Creates a new Ev3Motors component.
   */
  public Ev3Motors(ComponentContainer container) {
    super(container, "Ev3Motors");
    eventHandler = new Handler();
    sensorValueChecker = new Runnable() {
      public void run() {
        String functionName = "";

        if (bluetooth != null && bluetooth.IsConnected()) {
          int sensorValue = getOutputCount(functionName, 0, motorPortBitField);

          if (!ifReset) {
            if (sensorValue != previousValue && tachoCountChangedEventEnabled) {
              TachoCountChanged(sensorValue);
            }
          } else {
            ifReset = false;
          }
          previousValue = sensorValue;
        }

        eventHandler.postDelayed(this, DELAY_MILLISECONDS);
      }
    };

    eventHandler.post(sensorValueChecker);

    MotorPorts(DEFAULT_MOTOR_PORTS);
    StopBeforeDisconnect(true);
    EnableSpeedRegulation(true);
    ReverseDirection(false);
    WheelDiameter(DEFAULT_WHEEL_DIAMETER);
    TachoCountChangedEventEnabled(false);
  }

  /**
   * Returns the motor port.
   */
  @SimpleProperty(description = "The motor ports that the motors are connected to. The ports are specified by a sequence of port letters.",
                  category = PropertyCategory.BEHAVIOR,
                  userVisible = false)
  public String MotorPorts() {
    return bitFieldToMotorPortLetters(motorPortBitField);
  }

  /**
   * Specifies the motor port.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
                    defaultValue = DEFAULT_MOTOR_PORTS)
  @SimpleProperty
  public void MotorPorts(String motorPortLetters) {
    String functionName = "MotorPorts";
    try {
      motorPortBitField = motorPortLettersToBitField(motorPortLetters);
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_MOTOR_PORT, motorPortLetters);
    }
  }

  /**
   * Specifies the diameter of the wheels attached on motors.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
                    defaultValue = "" + DEFAULT_WHEEL_DIAMETER)
  @SimpleProperty
  public void WheelDiameter(double diameter) {
    wheelDiameter = diameter;
  }

  /**
   * Returns the diameter of the wheels attached on motors.
   */
  @SimpleProperty(description = "The diameter of the wheels attached on the motors in centimeters.",
                  category = PropertyCategory.BEHAVIOR,
                  userVisible = false)
  public double WheelDiameter() {
    return wheelDiameter;
  }

  /**
   * Set whether the direction of motors is reversed or not.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
                    defaultValue = "False")
  @SimpleProperty
  public void ReverseDirection(boolean reversed) {
    String functionName = "ReverseDirection";
    try {
      setOutputDirection(functionName, 0, motorPortBitField, reversed ? -1 : 1); // assume layer = 0
      directionReversed = reversed;
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
    }
  }

  /**
   * Returns if the direction of the motors is reversed.
   */
  @SimpleProperty(description = "It specifies if the direction of the motors is reversed.",
                  category = PropertyCategory.BEHAVIOR)
  public boolean ReverseDirection() {
    return directionReversed;
  }

  /**
   * Specifies whether to keep motor rotation at constant speed.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
                    defaultValue = "True")
  @SimpleProperty
  public void EnableSpeedRegulation(boolean enabled) {
    regulationEnabled = enabled;
  }

  /**
   * Returns whether to keep motor rotation at constant speed.
   */
  @SimpleProperty(description = "The robot adjusts the power to maintain the speed if speed regulation is enabled.",
                  category = PropertyCategory.BEHAVIOR)
  public boolean EnableSpeedRegulation() {
    return regulationEnabled;
  }

  /**
   * Returns whether to stop the motor before disconnecting.
   */
  @SimpleProperty(description = "Whether to stop the motor before disconnecting.",
                  category = PropertyCategory.BEHAVIOR)
  public boolean StopBeforeDisconnect() {
    return stopBeforeDisconnect;
  }

  /**
   * Specifies whether to stop the drive motors before disconnecting.
   *
   * @param stopBeforeDisconnect whether to stop the motors before disconnecting
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
                    defaultValue = "True")
  @SimpleProperty
  public void StopBeforeDisconnect(boolean stopBeforeDisconnect) {
    this.stopBeforeDisconnect = stopBeforeDisconnect;
  }

  /**
   * Returns whether the TachoCountChanged event should fire when the motor angle is increaing.
   */
  @SimpleProperty(description = "Whether the TachoCountChanged event should fire when the angle is changed.",
                  category = PropertyCategory.BEHAVIOR)
  public boolean TachoCountChangedEventEnabled() {
    return tachoCountChangedEventEnabled;
  }

  /**
   * Returns whether the TachoCountChanged event should fire when the motor angle is increaing.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
                    defaultValue = "False")
  @SimpleProperty
  public void TachoCountChangedEventEnabled(boolean enabled) {
    tachoCountChangedEventEnabled = enabled;
  }

  /**
   * Start to rotate the motors.
   */
  @SimpleFunction(description = "Start to rotate the motors.")
  public void RotateIndefinitely(int power) {
    String functionName = "RotateIndefinitely";
    try {
      if (regulationEnabled)
        setOutputPower(functionName, 0, motorPortBitField, power);
      else
        setOutputSpeed(functionName, 0, motorPortBitField, power);

      startOutput(functionName, 0, motorPortBitField);
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
    }
  }

  /**
   * Rotate the motors in a number of tacho counts.
   */
  @SimpleFunction(description = "Rotate the motors in a number of tacho counts.")
  public void RotateInTachoCounts(int power, int tachoCounts, boolean useBrake) {
    String functionName = "RotateInTachoCounts";
    try {
      if (regulationEnabled)
        outputStepSpeed(functionName, 0, motorPortBitField, power, 0, tachoCounts, 0, useBrake);
      else
        outputStepPower(functionName, 0, motorPortBitField, power, 0, tachoCounts, 0, useBrake);
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
    }
  }

  /**
   * Rotate the motors in a period of time.
   */
  @SimpleFunction(description = "Rotate the motors in a period of time.")
  public void RotateInDuration(int power, int milliseconds, boolean useBrake) {
    String functionName = "RotateInDuration";
    try {
      if (regulationEnabled)
        outputTimeSpeed(functionName, 0, motorPortBitField, power, 0, milliseconds, 0, useBrake);
      else
        outputTimePower(functionName, 0, motorPortBitField, power, 0, milliseconds, 0, useBrake);
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
    }
  }

  /**
   * Rotate the motors in a distance.
   */
  @SimpleFunction(description = "Rotate the motors in a distance.")
  public void RotateInDistance(int power, double distance, boolean useBrake) {
    String functionName = "RotateInDistance";
    int tachoCounts = (int) (distance * 360.0 / wheelDiameter / Math.PI);

    try {
      if (regulationEnabled)
        outputStepSpeed(functionName, 0, motorPortBitField, power, 0, tachoCounts, 0, useBrake);
      else
        outputStepPower(functionName, 0, motorPortBitField, power, 0, tachoCounts, 0, useBrake);
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
    }
  }

  /**
   * Start to rotate the motors at the same speed.
   */
  @SimpleFunction(description = "Start to rotate the motors at the same speed.")
  public void RotateSyncIndefinitely(int power, int turnRatio) {
    String functionName = "RotateSyncIndefinitely";

    try {
      // avoid the issue that output sync commands with single motor causes the EV3 to hang
      if (motorPortBitField != 0) {
        if (isOneShotInteger(motorPortBitField))
          setOutputSpeed(functionName, 0, motorPortBitField, power);
        else
          outputStepSync(functionName, 0, motorPortBitField, power, turnRatio, 0, true);
      }

    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
    }
  }

  /**
   * Rotate the motors at the same speed for a distance in cm.
   */
  @SimpleFunction(description = "Rotate the motors at the same speed for a distance in cm.")
  public void RotateSyncInDistance(int power, int distance, int turnRatio, boolean useBrake) {
    String functionName = "RotateSyncInDuration";
    int tachoCounts = (int) (distance * 360.0 / wheelDiameter / Math.PI);

    try {
      // avoid the issue that output sync commands with single motor causes the EV3 to hang
      if (motorPortBitField != 0) {
        if (isOneShotInteger(motorPortBitField))
          outputStepSpeed(functionName, 0, motorPortBitField, power, 0, tachoCounts, 0, useBrake);
        else
          outputStepSync(functionName, 0, motorPortBitField, power, turnRatio, tachoCounts, useBrake);
      }

    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
    }
  }

  /**
   * Rotate the motors at the same speed in a period of time.
   */
  @SimpleFunction(description = "Rotate the motors at the same speed in a period of time.")
  public void RotateSyncInDuration(int power, int milliseconds, int turnRatio, boolean useBrake) {
    String functionName = "RotateSyncInDuration";

    try {
      // avoid the issue that output sync commands with single motor causes the EV3 to hang
      if (motorPortBitField != 0) {
        if (isOneShotInteger(motorPortBitField))
          outputTimeSpeed(functionName, 0, motorPortBitField, power, 0, milliseconds, 0, useBrake);
        else
          outputTimeSync(functionName, 0, motorPortBitField, power, turnRatio, milliseconds, useBrake);
      }

    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
    }
  }

  /**
   * Rotate the motors at the same speed in a number of tacho counts.
   */
  @SimpleFunction(description = "Rotate the motors at the same speed in a number of tacho counts.")
  public void RotateSyncInTachoCounts(int power, int tachoCounts, int turnRatio, boolean useBrake) {
    String functionName = "RotateSyncInTachoCounts";

    try {
      // avoid the issue that output sync commands with single motor causes the EV3 to hang
      if (motorPortBitField != 0) {
        if (isOneShotInteger(motorPortBitField))
          outputStepSpeed(functionName, 0, motorPortBitField, power, 0, tachoCounts, 0, useBrake);
        else
          outputStepSync(functionName, 0, motorPortBitField, power, turnRatio, tachoCounts, useBrake);
      }

    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
    }
  }

  /**
   * Stop the motors of the robot.
   */
  @SimpleFunction(description = "Stop the motors of the robot.")
  public void Stop(boolean useBrake) {
    String functionName = "Stop";
    try {
      stopOutput(functionName, 0, motorPortBitField, useBrake); // assume layer = 0
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
    }
  }

  /**
   * Toggle the direction of motors.
   */
  @SimpleFunction(description = "Toggle the direction of motors.")
  public void ToggleDirection() {
    String functionName = "ToggleDirection";
    try {
      setOutputDirection(functionName, 0, motorPortBitField, 0); // assume layer = 0
      directionReversed = !directionReversed;
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
    }
  }

  /**
   * Set the current tacho count to zero.
   */
  @SimpleFunction(description = "Set the current tacho count to zero.")
  public void ResetTachoCount() {
    String functionName = "ResetTachoCount";
    try {
      clearOutputCount(functionName, 0, motorPortBitField); // assume layer = 0
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
    }
  }

  /**
   * Get the current tacho count.
   */
  @SimpleFunction(description = "Get the current tacho count.")
  public int GetTachoCount() {
    String functionName = "GetTachoCount";
    try {
      return getOutputCount(functionName, 0, motorPortBitField); // assume layer = 0
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
      return 0;
    }
  }

  /**
   * Called when the tacho count has changed.
   */
  @SimpleEvent(description = "Called when the tacho count has changed.")
  public void TachoCountChanged(int tachoCount) {
    EventDispatcher.dispatchEvent(this, "TachoCountChanged", tachoCount);
  }

  private int roundValue(int value, int minimum, int maximum) {
    return value < minimum ? minimum : (value > maximum ? maximum : value);
  }

  private boolean isOneShotInteger(int value) {
    return (value != 0) && ((value & ~(value ^ (value - 1))) == 0);
  }

  private void resetOutput(String functionName, int layer, int nos) {
    if (layer < 0 || layer > 3 || nos < 0 || nos > 15)
      throw new IllegalArgumentException();
    ifReset = true;
    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.OUTPUT_RESET,
                                                         false,
                                                         0,
                                                         0,
                                                         "cc",
                                                         (byte) layer,
                                                         (byte) nos);
    sendCommand(functionName, command, false);
  }

  private void startOutput(String functionName, int layer, int nos) {
    if (layer < 0 || layer > 3 || nos < 0 || nos > 15)
      throw new IllegalArgumentException();

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.OUTPUT_START,
                                                         false,
                                                         0,
                                                         0,
                                                         "cc",
                                                         (byte) layer,
                                                         (byte) nos);
    sendCommand(functionName, command, false);
  }

  private void stopOutput(String functionName, int layer, int nos, boolean useBrake) {
    if (layer < 0 || layer > 3 || nos < 0 || nos > 15)
      throw new IllegalArgumentException();

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.OUTPUT_STOP,
                                                         false,
                                                         0,
                                                         0,
                                                         "ccc",
                                                         (byte) layer,
                                                         (byte) nos,
                                                         useBrake ? (byte) 1 : (byte) 0);
    sendCommand(functionName, command, false);
  }

  private void outputStepPower(String functionName, int layer, int nos, int power, int step1, int step2, int step3, boolean brake) {
    if (layer < 0 || layer > 3 || nos < 0 || nos > 15 || step1 < 0 || step2 < 0 || step3 < 0)
      throw new IllegalArgumentException();

    power = roundValue(power, -100, 100);

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.OUTPUT_STEP_POWER,
                                                         false,
                                                         0,
                                                         0,
                                                         "ccccccc",
                                                         (byte) layer,
                                                         (byte) nos,
                                                         (byte) power,
                                                         step1,
                                                         step2,
                                                         step3,
                                                         (byte) (brake ? 1 : 0));
    sendCommand(functionName, command, false);
  }

  private void outputStepSpeed(String functionName, int layer, int nos, int speed, int step1, int step2, int step3, boolean brake) {
    if (layer < 0 || layer > 3 || nos < 0 || nos > 15 || step1 < 0 || step2 < 0 || step3 < 0)
      throw new IllegalArgumentException();

    speed = roundValue(speed, -100, 100);

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.OUTPUT_STEP_SPEED,
                                                         false,
                                                         0,
                                                         0,
                                                         "ccccccc",
                                                         (byte) layer,
                                                         (byte) nos,
                                                         (byte) speed,
                                                         step1,
                                                         step2,
                                                         step3,
                                                         (byte) (brake ? 1 : 0));
    sendCommand(functionName, command, false);
  }

  private void outputStepSync(String functionName, int layer, int nos, int speed, int turnRatio, int tachoCounts, boolean brake) {
    if (layer < 0 || layer > 3 || nos < 0 || nos > 15 || turnRatio < -200 || turnRatio > 200)
      throw new IllegalArgumentException();

    speed = roundValue(speed, -100, 100);

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.OUTPUT_STEP_SYNC,
                                                         false,
                                                         0,
                                                         0,
                                                         "cccccc",
                                                         (byte) layer,
                                                         (byte) nos,
                                                         (byte) speed,
                                                         (short) turnRatio,
                                                         tachoCounts,
                                                         (byte) (brake ? 1 : 0));
    sendCommand(functionName, command, false);
  }

  private void outputTimePower(String functionName, int layer, int nos, int power, int step1, int step2, int step3, boolean brake) {
    if (layer < 0 || layer > 3 || nos < 0 || nos > 15 || step1 < 0 || step2 < 0 || step3 < 0)
      throw new IllegalArgumentException();

    power = roundValue(power, -100, 100);

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.OUTPUT_TIME_POWER,
                                                         false,
                                                         0,
                                                         0,
                                                         "ccccccc",
                                                         (byte) layer,
                                                         (byte) nos,
                                                         (byte) power,
                                                         step1,
                                                         step2,
                                                         step3,
                                                         (byte) (brake ? 1 : 0));
    sendCommand(functionName, command, false);
  }

  private void outputTimeSpeed(String functionName, int layer, int nos, int speed, int step1, int step2, int step3, boolean brake) {
    if (layer < 0 || layer > 3 || nos < 0 || nos > 15 || step1 < 0 || step2 < 0 || step3 < 0)
      throw new IllegalArgumentException();

    speed = roundValue(speed, -100, 100);

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.OUTPUT_TIME_SPEED,
                                                         false,
                                                         0,
                                                         0,
                                                         "ccccccc",
                                                         (byte) layer,
                                                         (byte) nos,
                                                         (byte) speed,
                                                         step1,
                                                         step2,
                                                         step3,
                                                         (byte) (brake ? 1 : 0));
    sendCommand(functionName, command, false);
  }

  private void outputTimeSync(String functionName, int layer, int nos, int speed, int turnRatio, int milliseconds, boolean brake) {
    if (layer < 0 || layer > 3 || nos < 0 || nos > 15 || milliseconds < 0)
      throw new IllegalArgumentException();

    speed = roundValue(speed, -100, 100);

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.OUTPUT_TIME_SYNC,
                                                         false,
                                                         0,
                                                         0,
                                                         "cccccc",
                                                         (byte) layer,
                                                         (byte) nos,
                                                         (byte) speed,
                                                         (short) turnRatio,
                                                         milliseconds,
                                                         (byte) (brake ? 1 : 0));
    sendCommand(functionName, command, false);
  }

  private void setOutputDirection(String functionName, int layer, int nos, int direction) {
    if (layer < 0 || layer > 3 || nos < 0 || nos > 15 || direction < -1 || direction > 1)
      throw new IllegalArgumentException();

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.OUTPUT_POLARITY,
                                                         false,
                                                         0,
                                                         0,
                                                         "ccc",
                                                         (byte) layer,
                                                         (byte) nos,
                                                         (byte) direction);
    sendCommand(functionName, command, false);
  }

  private void setOutputSpeed(String functionName, int layer, int nos, int speed) {
    if (layer < 0 || layer > 3 || nos < 0 || nos > 15)
      throw new IllegalArgumentException();

    speed = roundValue(speed, -100, 100);

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.OUTPUT_SPEED,
                                                         false,
                                                         0,
                                                         0,
                                                         "ccc",
                                                         (byte) layer,
                                                         (byte) nos,
                                                         (byte) speed);
    sendCommand(functionName, command, false);
  }

  private void setOutputPower(String functionName, int layer, int nos, int power) {
    if (layer < 0 || layer > 3 || nos < 0 || nos > 15)
      throw new IllegalArgumentException();

    power = roundValue(power, -100, 100);

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.OUTPUT_POWER,
                                                         false,
                                                         0,
                                                         0,
                                                         "ccc",
                                                         (byte) layer,
                                                         (byte) nos,
                                                         (byte) power);
    sendCommand(functionName, command, false);
  }

  private int getOutputCount(String functionName, int layer, int nos) {
    if (layer < 0 || layer > 3 || nos < 0 || nos > 15)
      throw new IllegalArgumentException();

    // select the port with minimum port number
    nos = ((nos ^ (nos - 1)) + 1 >>> 1);

    int portNumber;
    switch (nos) {
    case 1:
      portNumber = 0;
      break;

    case 2:
      portNumber = 1;
      break;

    case 4:
      portNumber = 2;
      break;

    case 8:
      portNumber = 3;
      break;

    default:
      throw new IllegalArgumentException();
    }

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.OUTPUT_GET_COUNT,
                                                         true,
                                                         4,
                                                         0,
                                                         "ccg",
                                                         (byte) layer,
                                                         (byte) portNumber,
                                                         (byte) 0);
    byte[] reply = sendCommand(functionName, command, true);

    if (reply != null && reply.length == 5 && reply[0] == Ev3Constants.DirectReplyType.DIRECT_REPLY) {
      Object[] values = Ev3BinaryParser.unpack("xi", reply);
      return (Integer) values[0];
    } else {
      return 0;
    }
  }

  private void clearOutputCount(String functionName, int layer, int nos) {
    if (layer < 0 || layer > 3 || nos < 0 || nos > 15)
      throw new IllegalArgumentException();

    byte[] command = Ev3BinaryParser.encodeDirectCommand(Ev3Constants.Opcode.OUTPUT_CLR_COUNT,
                                                         false,
                                                         0,
                                                         0,
                                                         "cc",
                                                         (byte) layer,
                                                         (byte) nos);
    sendCommand(functionName, command, false);
  }

  @Override
  public void beforeDisconnect(BluetoothConnectionBase bluetoothConnection) {
    String functionName = "beforeDisconnect";
    if (stopBeforeDisconnect) {
      try {
        stopOutput(functionName, 0, motorPortBitField, true); // assume layer = 0
      } catch (IllegalArgumentException e) {
        form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_ILLEGAL_ARGUMENT, functionName);
      }
    }
  }
}
