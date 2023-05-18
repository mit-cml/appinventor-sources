// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.NxtMailbox;
import com.google.appinventor.components.common.NxtMotorMode;
import com.google.appinventor.components.common.NxtMotorPort;
import com.google.appinventor.components.common.NxtRegulationMode;
import com.google.appinventor.components.common.NxtRunState;
import com.google.appinventor.components.common.NxtSensorMode;
import com.google.appinventor.components.common.NxtSensorPort;
import com.google.appinventor.components.common.NxtSensorType;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.YailList;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

/**
 * ![NXT component icon](images/legoMindstormsNxt.png)
 *
 * A component that provides a low-level interface to a LEGO MINDSTORMS NXT
 * robot, with functions to send NXT Direct Commands.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@DesignerComponent(version = YaVersion.NXT_DIRECT_COMMANDS_COMPONENT_VERSION,
    description = "A component that provides a low-level interface to a LEGO MINDSTORMS NXT " +
    "robot, with functions to send NXT Direct Commands.",
    category = ComponentCategory.LEGOMINDSTORMS,
    nonVisible = true,
    iconName = "images/legoMindstormsNxt.png")
@SimpleObject
public class NxtDirectCommands extends LegoMindstormsNxtBase {

  /**
   * Creates a new NxtDirectCommands component.
   */
  public NxtDirectCommands(ComponentContainer container) {
    super(container, "NxtDirectCommands");
  }

  // TODO(user, lizlooney) - Add a property for a "helper program", like MotorControl21.rxe. If
  // set, then the Connect method would automatically take care of checking for, downloading (if
  // necessary) and starting the helper program. This would minimize the programming blocks for a
  // classroom project.

  @SimpleFunction(description = "Start execution of a previously downloaded program on " +
      "the robot.")
  public void StartProgram(String programName) {
    String functionName = "StartProgram";
    if (!checkBluetooth(functionName)) {
      return;
    }
    if (programName.length() == 0) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_PROGRAM_NAME);
      return;
    }
    if (programName.indexOf(".") == -1) {
      programName += ".rxe";
    }

    byte[] command = new byte[22];
    command[0] = (byte) 0x80;  // Direct command telegram, no response
    command[1] = (byte) 0x00;  // STARTPROGRAM command
    copyStringValueToBytes(programName, command, 2, 19);
    sendCommand(functionName, command);
  }

  @SimpleFunction(description = "Stop execution of the currently running program on " +
      "the robot.")
  public void StopProgram() {
    String functionName = "StopProgram";
    if (!checkBluetooth(functionName)) {
      return;
    }

    byte[] command = new byte[2];
    command[0] = (byte) 0x80;  // Direct command telegram, no response
    command[1] = (byte) 0x01;  // STOPPROGRAM command
    sendCommand(functionName, command);
  }

  @SimpleFunction(description = "Play a sound file on the robot.")
  public void PlaySoundFile(String fileName) {
    String functionName = "PlaySoundFile";
    if (!checkBluetooth(functionName)) {
      return;
    }
    if (fileName.length() == 0) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_FILE_NAME);
      return;
    }
    if (fileName.indexOf(".") == -1) {
      fileName += ".rso";
    }

    byte[] command = new byte[23];
    command[0] = (byte) 0x80;  // Direct command telegram, no response
    command[1] = (byte) 0x02;  // PLAYSOUNDFILE command
    copyBooleanValueToBytes(false, command, 2);  // play file once only
    copyStringValueToBytes(fileName, command, 3, 19);
    sendCommand(functionName, command);
  }

  @SimpleFunction(description = "Make the robot play a tone.")
  public void PlayTone(int frequencyHz, int durationMs) {
    String functionName = "PlayTone";
    if (!checkBluetooth(functionName)) {
      return;
    }

    if (frequencyHz < 200) {
      Log.w(logTag, "frequencyHz " + frequencyHz + " is invalid, using 200.");
      frequencyHz = 200;
    }
    if (frequencyHz > 14000) {
      Log.w(logTag, "frequencyHz " + frequencyHz + " is invalid, using 14000.");
      frequencyHz = 14000;
    }
    byte[] command = new byte[6];
    command[0] = (byte) 0x80;  // Direct command telegram, no response
    command[1] = (byte) 0x03;  // PLAYTONE command
    copyUWORDValueToBytes(frequencyHz, command, 2);  // 2-3: frequency, Hz (UWORD)
    copyUWORDValueToBytes(durationMs, command, 4);   // 4-5: Duration, ms (UWORD)
    sendCommand(functionName, command);
  }

  @SimpleFunction(description = "Sets the output state of a motor on the robot.")
  public void SetOutputState(
      @Options(NxtMotorPort.class) String motorPortLetter,
      int power,
      @Options(NxtMotorMode.class) int mode,
      @Options(NxtRegulationMode.class) int regulationMode,
      int turnRatio,
      @Options(NxtRunState.class) int runState,
      long tachoLimit) {
    String functionName = "SetOutputState";
    if (!checkBluetooth(functionName)) {
      return;
    }

    // Make sure motorPortLetter is a valid NxtMotorPort.
    NxtMotorPort port = NxtMotorPort.fromUnderlyingValue(motorPortLetter);
    if (port == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_MOTOR_PORT, motorPortLetter);
      return;
    }

    // Make sure mode is a valid NxtMotorMode.
    NxtMotorMode motorMode = NxtMotorMode.fromUnderlyingValue(mode);
    if (motorMode == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_MOTOR_MODE, mode);
      return;
    }

    // Make sure regulationMode is a valid NxtRegulationMode.
    NxtRegulationMode regMode = NxtRegulationMode.fromUnderlyingValue(regulationMode);
    if (regMode == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_REGULATION_MODE, regMode);
      return;
    }

    // Make sure runState is a valid NxtRunState;
    NxtRunState state = NxtRunState.fromUnderlyingValue(runState);

    setOutputState(
        functionName,
        port,
        power,
        motorMode,
        regMode,
        turnRatio,
        state,
        tachoLimit);
  }

  @SimpleFunction(description = "Configure an input sensor on the robot.")
  public void SetInputMode(
      @Options(NxtSensorPort.class) String sensorPortLetter,
      @Options(NxtSensorType.class) int sensorType,
      @Options(NxtSensorMode.class) int sensorMode) {
    String functionName = "SetInputMode";
    if (!checkBluetooth(functionName)) {
      return;
    }

    // Make sure sensorPortLetter is a valid NxtSensorPort.
    NxtSensorPort port = NxtSensorPort.fromUnderlyingValue(sensorPortLetter);
    if (port == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_SENSOR_PORT, sensorPortLetter);
      return;
    }

    // Make sure sensorType is a valid NxtSensorType.
    NxtSensorType type = NxtSensorType.fromUnderlyingValue(sensorType);
    if (type == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_SENSOR_TYPE, type);
      return;
    }

    // Make sure sensorMode is a valid NxtSensorMode.
    NxtSensorMode mode = NxtSensorMode.fromUnderlyingValue(sensorMode);
    if (mode == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_SENSOR_MODE, mode);
      return;
    }

    setInputMode(functionName, port, type, mode);
  }

  @SimpleFunction(description = "Reads the output state of a motor on the robot.")
  public List<Number> GetOutputState(@Options(NxtMotorPort.class) String motorPortLetter) {
    String functionName = "GetOutputState";
    if (!checkBluetooth(functionName)) {
      return new ArrayList<Number>();
    }

    // Make sure motorPortLetter is a valid NxtMotorPort.
    NxtMotorPort port = NxtMotorPort.fromUnderlyingValue(motorPortLetter);
    if (port == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_MOTOR_PORT, motorPortLetter);
      return new ArrayList<Number>();
    }

    byte[] returnPackage = getOutputState(functionName, port);
    if (returnPackage != null) {
      List<Number> outputState = new ArrayList<Number>();
      outputState.add(getSBYTEValueFromBytes(returnPackage, 4));   // Power (SBYTE -100 to 100)
      outputState.add(getUBYTEValueFromBytes(returnPackage, 5));   // Mode (UBYTE)
      outputState.add(getUBYTEValueFromBytes(returnPackage, 6));   // Regulation mode (UBYTE)
      outputState.add(getSBYTEValueFromBytes(returnPackage, 7));   // TurnRatio (SBYTE -100 to 100)
      outputState.add(getUBYTEValueFromBytes(returnPackage, 8));   // RunState (UBYTE)
      outputState.add(getULONGValueFromBytes(returnPackage, 9));   // TachoLimit (ULONG)
      outputState.add(getSLONGValueFromBytes(returnPackage, 13));  // TachoCount (SLONG)
      outputState.add(getSLONGValueFromBytes(returnPackage, 17));  // BlockTachoCount (SLONG)
      outputState.add(getSLONGValueFromBytes(returnPackage, 21));  // RotationCount (SLONG)
      return outputState;
    }

    // invalid response
    return new ArrayList<Number>();
  }

  private byte[] getOutputState(String functionName, NxtMotorPort port) {
    byte[] command = new byte[3];
    command[0] = (byte) 0x00;  // Direct command telegram, response required
    command[1] = (byte) 0x06;  // GETOUTPUTSTATE command
    copyUBYTEValueToBytes(port.toInt(), command, 2);
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    if (evaluateStatus(functionName, returnPackage, command[1])) {
      if (returnPackage.length == 25) {
        return returnPackage;
      } else {
        Log.w(logTag, functionName + ": unexpected return package length " +
            returnPackage.length + " (expected 25)");
      }
    }
    return null;
  }

  @SimpleFunction(description = "Reads the values of an input sensor on the robot. " +
      "Assumes sensor type has been configured via SetInputMode.")
  public List<Object> GetInputValues(@Options(NxtSensorPort.class) String sensorPortLetter) {
    String functionName = "GetInputValues";
    if (!checkBluetooth(functionName)) {
      return new ArrayList<Object>();
    }

    // Make sure sensorPortLeter is a valid NxtSensorPort.
    NxtSensorPort port = NxtSensorPort.fromUnderlyingValue(sensorPortLetter);
    if (port == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_SENSOR_PORT, sensorPortLetter);
      return new ArrayList<Object>();
    }

    byte[] returnPackage = getInputValues(functionName, port);
    if (returnPackage != null) {
      List<Object> inputValues = new ArrayList<Object>();
      inputValues.add(getBooleanValueFromBytes(returnPackage, 4));  // Valid
      inputValues.add(getBooleanValueFromBytes(returnPackage, 5));  // Calibrated
      inputValues.add(getUBYTEValueFromBytes(returnPackage, 6));    // Sensor type
      inputValues.add(getUBYTEValueFromBytes(returnPackage, 7));    // Sensor mode
      inputValues.add(getUWORDValueFromBytes(returnPackage, 8));    // Raw A/D value (UWORD)
      inputValues.add(getUWORDValueFromBytes(returnPackage, 10));   // Normalized A/D value (UWORD)
      inputValues.add(getSWORDValueFromBytes(returnPackage, 12));   // Scaled value (SWORD)
      inputValues.add(getSWORDValueFromBytes(returnPackage, 14));   // Calibrated value (SWORD)
      return inputValues;
    }

    // invalid response
    return new ArrayList<Object>();
  }

  @SimpleFunction(description = "Reset the scaled value of an input sensor on the robot.")
  public void ResetInputScaledValue(@Options(NxtSensorPort.class) String sensorPortLetter) {
    String functionName = "ResetInputScaledValue";
    if (!checkBluetooth(functionName)) {
      return;
    }

    // Make sure sensorPortLetter is a valid NxtSensorPort.
    NxtSensorPort port = NxtSensorPort.fromUnderlyingValue(sensorPortLetter);
    if (port == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_SENSOR_PORT, sensorPortLetter);
      return;
    }

    resetInputScaledValue(functionName, port);
    byte[] command = new byte[3];
    command[0] = (byte) 0x80;  // Direct command telegram, no response
    command[1] = (byte) 0x08;  // RESETINPUTSCALEDVALUE command
    copyUBYTEValueToBytes(port.toInt(), command, 2);
    sendCommand(functionName, command);
  }

  @SimpleFunction(description = "Reset motor position.")
  public void ResetMotorPosition(
      @Options(NxtMotorPort.class) String motorPortLetter,
      boolean relative) {
    String functionName = "ResetMotorPosition";
    if (!checkBluetooth(functionName)) {
      return;
    }

    // Make sure motorPortLetter is a valid NxtMotorPort.
    NxtMotorPort port = NxtMotorPort.fromUnderlyingValue(motorPortLetter);
    if (port == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_MOTOR_PORT, motorPortLetter);
      return;
    }

    byte[] command = new byte[4];
    command[0] = (byte) 0x80;  // Direct command telegram, no response
    command[1] = (byte) 0x0A;  // RESETMOTORPOSITION command
    copyUBYTEValueToBytes(port.toInt(), command, 2);
    copyBooleanValueToBytes(relative, command, 3);
    sendCommand(functionName, command);
  }

  @SimpleFunction(description = "Get the battery level for the robot. " +
      "Returns the voltage in millivolts.")
  public int GetBatteryLevel() {
    String functionName = "GetBatteryLevel";
    if (!checkBluetooth(functionName)) {
      return 0;
    }

    byte[] command = new byte[2];
    command[0] = (byte) 0x00;  // Direct command telegram, response required
    command[1] = (byte) 0x0B;  // GETBATTERYLEVEL command
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    if (evaluateStatus(functionName, returnPackage, command[1])) {
      if (returnPackage.length == 5) {
        return getUWORDValueFromBytes(returnPackage, 3);
      } else {
        Log.w(logTag, "GetBatteryLevel: unexpected return package length " +
            returnPackage.length + " (expected 5)");
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Stop sound playback.")
  public void StopSoundPlayback() {
    String functionName = "StopSoundPlayback";
    if (!checkBluetooth(functionName)) {
      return;
    }

    byte[] command = new byte[2];
    command[0] = (byte) 0x80;  // Direct command telegram, no response
    command[1] = (byte) 0x0C;  // STOPSOUNDPLAYBACK command
    sendCommand(functionName, command);
  }

  @SimpleFunction(description = "Keep Alive. " +
      "Returns the current sleep time limit in milliseconds.")
  public long KeepAlive() {
    String functionName = "KeepAlive";
    if (!checkBluetooth(functionName)) {
      return 0;
    }

    byte[] command = new byte[2];
    command[0] = (byte) 0x00;  // Direct command telegram, response required
    command[1] = (byte) 0x0D;  // KEEPALIVE command
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    if (evaluateStatus(functionName, returnPackage, command[1])) {
      if (returnPackage.length == 7) {
        return getULONGValueFromBytes(returnPackage, 3);
      } else {
        Log.w(logTag, "KeepAlive: unexpected return package length " +
            returnPackage.length + " (expected 7)");
      }
    }
    return 0;
  }

  @SimpleFunction(description = "Returns the count of available bytes to read.")
  public int LsGetStatus(@Options(NxtSensorPort.class) String sensorPortLetter) {
    String functionName = "LsGetStatus";
    if (!checkBluetooth(functionName)) {
      return 0;
    }

    // Make sure sensorPortLetter is a valid NxtSensorPort.
    NxtSensorPort port = NxtSensorPort.fromUnderlyingValue(sensorPortLetter);
    if (port == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_SENSOR_PORT, sensorPortLetter);
      return 0;
    }

    return lsGetStatus(functionName, port);
  }

  @SimpleFunction(description = "Writes low speed data to an input sensor on the robot. " +
      "Assumes sensor type has been configured via SetInputMode.")
  public void LsWrite(
      @Options(NxtSensorPort.class) String sensorPortLetter,
      YailList list,
      int rxDataLength) {
    String functionName = "LsWrite";
    if (!checkBluetooth(functionName)) {
      return;
    }

    // Make sure sensorPortLetter is a valid NxtSensorPort.
    NxtSensorPort port = NxtSensorPort.fromUnderlyingValue(sensorPortLetter);
    if (port == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_SENSOR_PORT, sensorPortLetter);
      return;
    }

    if (list.size() > 16) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_DATA_TOO_LARGE);
      return;
    }

    Object[] array = list.toArray();
    byte[] bytes = new byte[array.length];
    for (int i = 0; i < array.length; i++) {
      // We use Object.toString here because the element might be a String or it might be some
      // numeric class.
      Object element = array[i];
      String s = element.toString();
      int n;
      try {
        n = Integer.decode(s);
      } catch (NumberFormatException e) {
        form.dispatchErrorOccurredEvent(this, functionName,
            ErrorMessages.ERROR_NXT_COULD_NOT_DECODE_ELEMENT, i + 1);
        return;
      }
      bytes[i] = (byte) (n & 0xFF);
      n = n >> 8;
      if (n != 0 && n != -1) {
        form.dispatchErrorOccurredEvent(this, functionName,
            ErrorMessages.ERROR_NXT_COULD_NOT_FIT_ELEMENT_IN_BYTE, i + 1);
        return;
      }
    }
    lsWrite(functionName, port, bytes, rxDataLength);
  }


  @SimpleFunction(description = "Reads unsigned low speed data from an input sensor on the " +
      "robot. Assumes sensor type has been configured via SetInputMode.")
  public List<Integer> LsRead(@Options(NxtSensorPort.class) String sensorPortLetter) {
    String functionName = "LsRead";
    if (!checkBluetooth(functionName)) {
      return new ArrayList<Integer>();
    }

    // Make sure sensorPortLetter is a valid NxtSensorPort.
    NxtSensorPort port = NxtSensorPort.fromUnderlyingValue(sensorPortLetter);
    if (port == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_SENSOR_PORT, sensorPortLetter);
      return new ArrayList<Integer>();
    }

    byte[] returnPackage = lsRead(functionName, port);
    if (returnPackage != null) {
      List<Integer> list = new ArrayList<Integer>();
      int count = getUBYTEValueFromBytes(returnPackage, 3);
      for (int i = 0; i < count; i++) {
        int n = returnPackage[4 + i] & 0xFF; // unsigned
        list.add(n);
      }
      return list;
    }

    // invalid response
    return new ArrayList<Integer>();
  }

  @SimpleFunction(description = "Get the name of currently running program on " +
      "the robot.")
  public String GetCurrentProgramName() {
    String functionName = "GetCurrentProgramName";
    if (!checkBluetooth(functionName)) {
      return "";
    }

    byte[] command = new byte[2];
    command[0] = (byte) 0x00;  // Direct command telegram, response required
    command[1] = (byte) 0x11;  // GETCURRRENTPROGRAMNAME command
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    int status = getStatus(functionName, returnPackage, command[1]);
    if (status == 0) {
      // Success
      return getStringValueFromBytes(returnPackage, 3);
    }
    if (status == 0xEC) {
      // No active program. We don't treat this as an error.
      return "";
    }
    // Some other error code.
    evaluateStatus(functionName, returnPackage, command[1]);
    return "";
  }

  @SimpleFunction(description = "Read a message from a mailbox (1-10) on the robot.")
  public String MessageRead(@Options(NxtMailbox.class) int mailbox) {
    String functionName = "MessageRead";
    // Make sure mailbox is a valid NxtMailbox.
    NxtMailbox box = NxtMailbox.fromUnderlyingValue(mailbox);
    if (box == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_MAILBOX, mailbox);
      return "";
    }
    return MessageReadAbstract(box);
  }

  /**
   * Read a message from the given {@link NxtMailbox}.
   *
   * @param mailbox the Nxt inbox identifier
   * @return the message as a string
   */
  @SuppressWarnings("RegularMethodName")
  public String MessageReadAbstract(NxtMailbox mailbox) {
    final String functionName = "MessageRead";
    final int intMailbox = mailbox.toInt();

    if (!checkBluetooth(functionName)) {
      return "";
    }

    byte[] command = new byte[5];
    command[0] = (byte) 0x00;  // Direct command telegram, response required
    command[1] = (byte) 0x13;  // MESSAGEREAD command
    copyUBYTEValueToBytes(0, command, 2);  // no remote mailbox
    copyUBYTEValueToBytes(intMailbox, command, 3);
    copyBooleanValueToBytes(true, command, 4);  // remove message from mailbox
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    if (evaluateStatus(functionName, returnPackage, command[1])) {
      if (returnPackage.length == 64) {
        int mailboxEcho = getUBYTEValueFromBytes(returnPackage, 3);
        if (mailboxEcho != intMailbox) {
          Log.w(logTag, "MessageRead: unexpected return mailbox: Box"
              + mailboxEcho + " (expected " + intMailbox + ")");
        }
        int messageLength = getUBYTEValueFromBytes(returnPackage, 4) - 1;
        return getStringValueFromBytes(returnPackage, 5, messageLength);
      } else {
        Log.w(logTag, "MessageRead: unexpected return package length "
            + returnPackage.length + " (expected 64)");
      }
    }
    return "";
  }

  /**
   * Write a message to a mailbox (1-10) on the robot.
   *
   * @param mailbox the mailbox number
   * @param message the text of the message
   */
  @SimpleFunction(description = "Write a message to a mailbox (1-10) on the robot.")
  public void MessageWrite(@Options(NxtMailbox.class) int mailbox, String message) {
    String functionName = "MessageWrite";
    // Make sure mailbox is a valid NxtMailbox.
    NxtMailbox box = NxtMailbox.fromUnderlyingValue(mailbox);
    if (box == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_MAILBOX, mailbox);
      return;
    }
    MessageWriteAbstract(box, message);
  }

  /**
   * Write a message to the given {@link NxtMailbox}.
   *
   * @param mailbox the target Nxt mailbox identifier
   * @param message the text message for the Nxt
   */
  @SuppressWarnings("RegularMethodName")
  public void MessageWriteAbstract(NxtMailbox mailbox, String message) {
    final String functionName = "MessageWrite";

    if (!checkBluetooth(functionName)) {
      return;
    }
    int messageLength = message.length();
    if (messageLength > 58) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_MESSAGE_TOO_LONG);
      return;
    }

    byte[] command = new byte[4 + messageLength + 1];
    command[0] = (byte) 0x80;  // Direct command telegram, no response
    command[1] = (byte) 0x09;  // MESSAGEWRITE command
    copyUBYTEValueToBytes(mailbox.toInt(), command, 2);
    // message length includes null termination byte
    copyUBYTEValueToBytes(messageLength + 1, command, 3);
    copyStringValueToBytes(message, command, 4, messageLength);
    // The command array is already filled with zeros. No need to actually set the last byte to 0.
    sendCommand(functionName, command);
  }

  /**
   * Download a file to the robot.
   *
   * <p/>See {@link MediaUtil#determineMediaSource} for information about what
   * a path can be.
   *
   * @param source the path of the file to download
   * @param destination the name of the file on the robot
   */
  @SimpleFunction(description = "Download a file to the robot.")
  public void DownloadFile(String source, String destination) {
    String functionName = "DownloadFile";
    if (!checkBluetooth(functionName)) {
      return;
    }
    if (source.length() == 0) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_SOURCE_ARGUMENT);
      return;
    }
    if (destination.length() == 0) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_DESTINATION_ARGUMENT);
      return;
    }

    try {
      File tempFile = MediaUtil.copyMediaToTempFile(form, source);
      try {
        InputStream in = new BufferedInputStream(FileUtil.openFile(form, tempFile), 1024);
        try {
          long fileSize = tempFile.length();
          Integer handle = (destination.endsWith(".rxe") || destination.endsWith(".ric"))
              ? openWriteLinear(functionName, destination, fileSize)
              : openWrite(functionName, destination, fileSize);
          if (handle == null) {
            return;
          }
          try {
            // Send data to NXT 32 bytes at a time.
            byte[] buffer = new byte[32];
            long sentLength = 0;
            while (sentLength < fileSize) {
              int chunkLength = (int) Math.min(32, fileSize - sentLength);
              in.read(buffer, 0, chunkLength);
              int writtenLength = writeChunk(functionName, handle, buffer, chunkLength);
              sentLength += writtenLength;
            }
          } finally {
            closeHandle(functionName, handle);
          }
        } finally {
          in.close();
        }
      } finally {
        tempFile.delete();
      }
    } catch (IOException e) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_UNABLE_TO_DOWNLOAD_FILE, e.getMessage());
      return;
    }
  }

  private Integer openWrite(String functionName, String fileName, long fileSize) {
    byte[] command = new byte[26];
    command[0] = (byte) 0x01;  // System command telegram, response required
    command[1] = (byte) 0x81;  // OPEN WRITE command
    copyStringValueToBytes(fileName, command, 2, 19);
    copyULONGValueToBytes(fileSize, command, 22);
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    if (evaluateStatus(functionName, returnPackage, command[1])) {
      if (returnPackage.length == 4) {
        return getUBYTEValueFromBytes(returnPackage, 3);
      } else {
        Log.w(logTag, functionName + ": unexpected return package length " +
            returnPackage.length + " (expected 4)");
      }
    }
    return null;
  }

  private int writeChunk(String functionName, int handle, byte[] buffer, int length)
      throws IOException {
    if (length > 32) {
      throw new IllegalArgumentException("length must be <= 32");
    }

    byte[] command = new byte[3 + length];
    command[0] = (byte) 0x01;  // System command telegram, response required
    command[1] = (byte) 0x83;  // WRITE command
    copyUBYTEValueToBytes(handle, command, 2);
    System.arraycopy(buffer, 0, command, 3, length);
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    if (evaluateStatus(functionName, returnPackage, command[1])) {
      if (returnPackage.length == 6) {
        int writtenLength = getUWORDValueFromBytes(returnPackage, 4);
        if (writtenLength != length) {
          Log.e(logTag, functionName + ": only " + writtenLength + " bytes were written " +
              "(expected " + length + ")");
          throw new IOException("Unable to write file on robot");
        }
        return writtenLength;
      } else {
        Log.w(logTag, functionName + ": unexpected return package length " +
            returnPackage.length + " (expected 6)");
      }
    }
    return 0;
  }

  private void closeHandle(String functionName, int handle) {
    byte[] command = new byte[3];
    command[0] = (byte) 0x01;  // System command telegram, response required
    command[1] = (byte) 0x84;  // CLOSE command
    copyUBYTEValueToBytes(handle, command, 2);
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    evaluateStatus(functionName, returnPackage, command[1]);
  }

  @SimpleFunction(description = "Delete a file on the robot.")
  public void DeleteFile(String fileName) {
    String functionName = "DeleteFile";
    if (!checkBluetooth(functionName)) {
      return;
    }
    if (fileName.length() == 0) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_INVALID_FILE_NAME);
      return;
    }

    byte[] command = new byte[22];
    command[0] = (byte) 0x01;  // System command telegram, response required
    command[1] = (byte) 0x85;  // DELETE command
    copyStringValueToBytes(fileName, command, 2, 19);
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    evaluateStatus(functionName, returnPackage, command[1]);
  }

  @SimpleFunction(description = "Returns a list containing the names of matching files found on " +
      "the robot.")
  public List<String> ListFiles(String wildcard) {
    String functionName = "ListFiles";
    if (!checkBluetooth(functionName)) {
      return new ArrayList<String>();
    }

    List<String> fileNames = new ArrayList<String>();

    if (wildcard.length() == 0) {
      wildcard = "*.*";
    }

    byte[] command = new byte[22];
    command[0] = (byte) 0x01;  // System command telegram, response required
    command[1] = (byte) 0x86;  // FIND FIRST command
    copyStringValueToBytes(wildcard, command, 2, 19);
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    int status = getStatus(functionName, returnPackage, command[1]);
    while (status == 0) {
      int handle = getUBYTEValueFromBytes(returnPackage, 3);
      String fileName = getStringValueFromBytes(returnPackage, 4);
      fileNames.add(fileName);
      command = new byte[3];
      command[0] = (byte) 0x01;  // System command telegram, response required
      command[1] = (byte) 0x87;  // FIND NEXT command
      copyUBYTEValueToBytes(handle, command, 2);
      returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
      status = getStatus(functionName, returnPackage, command[1]);
    }
    return fileNames;
  }

  @SimpleFunction(description = "Get the firmware and protocol version numbers for the robot as" +
      " a list where the first element is the firmware version number and the second element is" +
      " the protocol version number.")
  public List<String> GetFirmwareVersion() {
    String functionName = "GetFirmwareVersion";
    if (!checkBluetooth(functionName)) {
      return new ArrayList<String>();
    }

    byte[] command = new byte[2];
    command[0] = (byte) 0x01;  // System command telegram, response required
    command[1] = (byte) 0x88;  // GET FIRMWARE VERSION command
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    if (evaluateStatus(functionName, returnPackage, command[1])) {
      List<String> versions = new ArrayList<String>();
      versions.add(returnPackage[6] + "." + returnPackage[5]); // firmware
      versions.add(returnPackage[4] + "." + returnPackage[3]); // protocol
      return versions;
    }
    return new ArrayList<String>();
  }

  private Integer openWriteLinear(String functionName, String fileName, long fileSize) {
    byte[] command = new byte[26];
    command[0] = (byte) 0x01;  // System command telegram, response required
    command[1] = (byte) 0x89;  // OPEN WRITE LINEAR command
    copyStringValueToBytes(fileName, command, 2, 19);
    copyULONGValueToBytes(fileSize, command, 22);
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    if (evaluateStatus(functionName, returnPackage, command[1])) {
      if (returnPackage.length == 4) {
        return getUBYTEValueFromBytes(returnPackage, 3);
      } else {
        Log.w(logTag, functionName + ": unexpected return package length " +
            returnPackage.length + " (expected 4)");
      }
    }
    return null;
  }

  // SetBrickName will change the name of the NXT.
  // The new name will appear on the NXT LCD immediately,
  // but the AddressesAndNames property does not update until
  // the app is restarted.
  @SimpleFunction(description = "Set the brick name of the robot.")
  public void SetBrickName(String name) {
    String functionName = "SetBrickName";
    if (!checkBluetooth(functionName)) {
      return;
    }

    byte[] command = new byte[18];
    command[0] = (byte) 0x01;  // System command telegram, response required
    command[1] = (byte) 0x98;  // SET BRICK NAME command
    copyStringValueToBytes(name, command, 2, 15);
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    evaluateStatus(functionName, returnPackage, command[1]);
  }

  @SimpleFunction(description = "Get the brick name of the robot.")
  public String GetBrickName() {
    String functionName = "GetBrickName";
    if (!checkBluetooth(functionName)) {
      return "";
    }

    byte[] command = new byte[2];
    command[0] = (byte) 0x01;  // System command telegram, response required
    command[1] = (byte) 0x9B;  // GET DEVICE INFO command
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    if (evaluateStatus(functionName, returnPackage, command[1])) {
      return getStringValueFromBytes(returnPackage, 3);
    }
    return "";
  }
}
