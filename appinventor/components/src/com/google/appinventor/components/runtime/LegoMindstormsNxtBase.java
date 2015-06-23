// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A base class for components that can control a LEGO MINDSTORMS NXT robot.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
@SimpleObject
public class LegoMindstormsNxtBase extends AndroidNonvisibleComponent
    implements BluetoothConnectionListener, Component, Deleteable {
  private static final int TOY_ROBOT = 0x0804; // from android.bluetooth.BluetoothClass.Device.

  private static final Map<Integer, String> ERROR_MESSAGES;
  static {
    ERROR_MESSAGES = new HashMap<Integer, String>();
    ERROR_MESSAGES.put(0x20, "Pending communication transaction in progress");
    ERROR_MESSAGES.put(0x40, "Specified mailbox queue is empty");
    ERROR_MESSAGES.put(0x81, "No more handles");
    ERROR_MESSAGES.put(0x82, "No space");
    ERROR_MESSAGES.put(0x83, "No more files");
    ERROR_MESSAGES.put(0x84, "End of file expected");
    ERROR_MESSAGES.put(0x85, "End of file");
    ERROR_MESSAGES.put(0x86, "Not a linear file");
    ERROR_MESSAGES.put(0x87, "File not found");
    ERROR_MESSAGES.put(0x88, "Handle already closed");
    ERROR_MESSAGES.put(0x89, "No linear space");
    ERROR_MESSAGES.put(0x8A, "Undefined error");
    ERROR_MESSAGES.put(0x8B, "File is busy");
    ERROR_MESSAGES.put(0x8C, "No write buffers");
    ERROR_MESSAGES.put(0x8D, "Append not possible");
    ERROR_MESSAGES.put(0x8E, "File is full");
    ERROR_MESSAGES.put(0x8F, "File exists");
    ERROR_MESSAGES.put(0x90, "Module not found");
    ERROR_MESSAGES.put(0x91, "Out of boundary");
    ERROR_MESSAGES.put(0x92, "Illegal file name");
    ERROR_MESSAGES.put(0x93, "Illegal handle");
    ERROR_MESSAGES.put(0xBD, "Request failed (i.e. specified file not found)");
    ERROR_MESSAGES.put(0xBE, "Unknown command opcode");
    ERROR_MESSAGES.put(0xBF, "Insane packet");
    ERROR_MESSAGES.put(0xC0, "Data contains out-of-range values");
    ERROR_MESSAGES.put(0xDD, "Communication bus error");
    ERROR_MESSAGES.put(0xDE, "No free memory in communication buffer");
    ERROR_MESSAGES.put(0xDF, "Specified channel/connection is not valid");
    ERROR_MESSAGES.put(0xE0, "Specified channel/connection not configured or busy");
    ERROR_MESSAGES.put(0xEC, "No active program");
    ERROR_MESSAGES.put(0xED, "Illegal size specified");
    ERROR_MESSAGES.put(0xEE, "Illegal mailbox queue ID specified");
    ERROR_MESSAGES.put(0xEF, "Attempted to access invalid field of a structure");
    ERROR_MESSAGES.put(0xF0, "Bad input or output specified");
    ERROR_MESSAGES.put(0xFB, "Insufficient memory available");
    ERROR_MESSAGES.put(0xFF, "Bad arguments");
  }

  protected final String logTag;

  // TODO(lizlooney) - allow communication via USB if possible.
  protected BluetoothClient bluetooth;


  /**
   * Creates a new LegoMindstormsNxtBase.
   */
  protected LegoMindstormsNxtBase(ComponentContainer container, String logTag) {
    super(container.$form());
    this.logTag = logTag;
  }

  /**
   * This constructor is for testing purposes only.
   */
  protected LegoMindstormsNxtBase() {
    super(null);
    logTag = null;
  }

  /**
   * Default Initialize
   */
  public final void Initialize() {
  }

  /**
   * Returns the BluetoothClient component that should be used for communication.
   */
  @SimpleProperty(
      description = "The BluetoothClient component that should be used for communication.",
      category = PropertyCategory.BEHAVIOR, userVisible = false)
  public BluetoothClient BluetoothClient() {
    return bluetooth;
  }

  /**
   * Specifies the BluetoothClient component that should be used for communication.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BLUETOOTHCLIENT,
      defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void BluetoothClient(BluetoothClient bluetoothClient) {
    if (bluetooth != null) {
      bluetooth.removeBluetoothConnectionListener(this);
      bluetooth.detachComponent(this);
      bluetooth = null;
    }

    if (bluetoothClient != null) {
      bluetooth = bluetoothClient;
      bluetooth.attachComponent(this, Collections.singleton(TOY_ROBOT));
      bluetooth.addBluetoothConnectionListener(this);
      if (bluetooth.IsConnected()) {
        // We missed the real afterConnect event.
        afterConnect(bluetooth);
      }
    }
  }

  protected final void setOutputState(String functionName, int port, int power, int mode,
      int regulationMode, int turnRatio, int runState, long tachoLimit) {
    power = sanitizePower(power);
    byte[] command = new byte[12];
    command[0] = (byte) 0x80;  // Direct command telegram, no response
    command[1] = (byte) 0x04;  // SETOUTPUTSTATE command
    copyUBYTEValueToBytes(port, command, 2);
    copySBYTEValueToBytes(power, command, 3);
    copyUBYTEValueToBytes(mode, command, 4);
    copyUBYTEValueToBytes(regulationMode, command, 5);
    copySBYTEValueToBytes(turnRatio, command, 6);
    copyUBYTEValueToBytes(runState, command, 7);
    // NOTE(lizlooney) - the LEGO MINDSTORMS NXT Direct Commands documentation (AKA Appendix 2)
    // says to use bytes 8-12 for the ULONG tacho limit. That's 5 bytes!
    // I've tested sending a 5th byte and it is ignored. Paul Gyugyi confirmed that the code for
    // the NXT firmware only uses 4 bytes. I'm pretty sure the documentation was supposed to say
    // bytes 8-11.
    copyULONGValueToBytes(tachoLimit, command, 8);
    sendCommand(functionName, command);
  }

  protected final void setInputMode(String functionName, int port, int sensorType, int sensorMode) {
    byte[] command = new byte[5];
    command[0] = (byte) 0x80;  // Direct command telegram, no response
    command[1] = (byte) 0x05;  // SETINPUTMODE command
    copyUBYTEValueToBytes(port, command, 2);
    copyUBYTEValueToBytes(sensorType, command, 3);
    copyUBYTEValueToBytes(sensorMode, command, 4);
    sendCommand(functionName, command);
  }

  protected final byte[] getInputValues(String functionName, int port) {
    byte[] command = new byte[3];
    command[0] = (byte) 0x00;  // Direct command telegram, response required
    command[1] = (byte) 0x07;  // GETINPUTVALUES command
    copyUBYTEValueToBytes(port, command, 2);
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    if (evaluateStatus(functionName, returnPackage, command[1])) {
      if (returnPackage.length == 16) {
        return returnPackage;
      } else {
        Log.w(logTag, functionName + ": unexpected return package length " +
            returnPackage.length + " (expected 16)");
      }
    }
    return null;
  }

  protected final void resetInputScaledValue(String functionName, int port) {
    byte[] command = new byte[3];
    command[0] = (byte) 0x80;  // Direct command telegram, no response
    command[1] = (byte) 0x08;  // RESETINPUTSCALEDVALUE command
    copyUBYTEValueToBytes(port, command, 2);
    sendCommand(functionName, command);
  }

  protected final int lsGetStatus(String functionName, int port) {
    byte[] command = new byte[3];
    command[0] = (byte) 0x00;  // Direct command telegram, response required
    command[1] = (byte) 0x0E;  // LSGETSTATUS command
    copyUBYTEValueToBytes(port, command, 2);
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    if (evaluateStatus(functionName, returnPackage, command[1])) {
      if (returnPackage.length == 4) {
        return getUBYTEValueFromBytes(returnPackage, 3);
      } else {
        Log.w(logTag, functionName + ": unexpected return package length " +
            returnPackage.length + " (expected 4)");
      }
    }
    return 0;
  }

  protected final void lsWrite(String functionName, int port, byte[] data, int rxDataLength) {
    if (data.length > 16) {
      throw new IllegalArgumentException("length must be <= 16");
    }
    byte[] command = new byte[5 + data.length];
    command[0] = (byte) 0x00;  // Direct command telegram, response required
    command[1] = (byte) 0x0F;  // LSWRITE command
    copyUBYTEValueToBytes(port, command, 2);
    copyUBYTEValueToBytes(data.length, command, 3);
    copyUBYTEValueToBytes(rxDataLength, command, 4);
    System.arraycopy(data, 0, command, 5, data.length);
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    evaluateStatus(functionName, returnPackage, command[1]);
  }

  protected final byte[] lsRead(String functionName, int port) {
    byte[] command = new byte[3];
    command[0] = (byte) 0x00;  // Direct command telegram, response required
    command[1] = (byte) 0x10;  // LSREAD command
    copyUBYTEValueToBytes(port, command, 2);
    byte[] returnPackage = sendCommandAndReceiveReturnPackage(functionName, command);
    if (evaluateStatus(functionName, returnPackage, command[1])) {
      if (returnPackage.length == 20) {
        return returnPackage;
      } else {
        Log.w(logTag, functionName + ": unexpected return package length " +
            returnPackage.length + " (expected 20)");
      }
    }
    return null;
  }


  /*
   * Checks whether the bluetooth property has been set or whether this
   * component is connected to a robot and, if necessary, dispatches the
   * appropriate error.
   *
   * Returns true if everything is ok, false if there was an error.
   */
  protected final boolean checkBluetooth(String functionName) {
    if (bluetooth == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_BLUETOOTH_NOT_SET);
      return false;
    }
    if (!bluetooth.IsConnected()) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_NXT_NOT_CONNECTED_TO_ROBOT);
      return false;
    }
    return true;
  }

  protected final byte[] sendCommandAndReceiveReturnPackage(String functionName, byte[] command) {
    sendCommand(functionName, command);
    return receiveReturnPackage(functionName);
  }

  protected final void sendCommand(String functionName, byte[] command) {
    byte[] header = new byte[2];
    copyUWORDValueToBytes(command.length, header, 0);
    bluetooth.write(functionName, header);
    bluetooth.write(functionName, command);
  }

  private byte[] receiveReturnPackage(String functionName) {
    byte[] header = bluetooth.read(functionName, 2);
    if (header.length == 2) {
      int length = getUWORDValueFromBytes(header, 0);
      byte[] returnPackage = bluetooth.read(functionName, length);
      if (returnPackage.length >= 3) {
        return returnPackage;
      }
    }

    form.dispatchErrorOccurredEvent(this, functionName,
        ErrorMessages.ERROR_NXT_INVALID_RETURN_PACKAGE);
    return new byte[0];
  }

  protected final boolean evaluateStatus(String functionName, byte[] returnPackage, byte command) {
    int status = getStatus(functionName, returnPackage, command);
    if (status == 0) {
      return true;
    } else {
      handleError(functionName, status);
      return false;
    }
  }

  protected final int getStatus(String functionName, byte[] returnPackage, byte command) {
    if (returnPackage.length >= 3) {
      if (returnPackage[0] != (byte) 0x02) {
        Log.w(logTag, functionName + ": unexpected return package byte 0: 0x" +
            Integer.toHexString(returnPackage[0] & 0xFF) + " (expected 0x02)");
      }
      if (returnPackage[1] != command) {
        Log.w(logTag, functionName + ": unexpected return package byte 1: 0x" +
            Integer.toHexString(returnPackage[1] & 0xFF) + " (expected 0x" +
            Integer.toHexString(command & 0xFF) + ")");
      }
      return getUBYTEValueFromBytes(returnPackage, 2);
    } else {
      Log.w(logTag, functionName + ": unexpected return package length " +
          returnPackage.length + " (expected >= 3)");
    }
    return -1;
  }

  private void handleError(String functionName, int status) {
    if (status < 0) {
      // Real status bytes received from the NXT are unsigned.
      // -1 is returned from getStatus when the returnPackage is not even big enough to contain a
      // status byte. In that case, we've already called form.dispatchErrorOccurredEvent from
      // receiveReturnPackage.
    } else {
      String errorMessage = ERROR_MESSAGES.get(status);
      if (errorMessage != null) {
        form.dispatchErrorOccurredEvent(this, functionName,
            ErrorMessages.ERROR_NXT_ERROR_CODE_RECEIVED, errorMessage);
      } else {
        form.dispatchErrorOccurredEvent(this, functionName,
            ErrorMessages.ERROR_NXT_ERROR_CODE_RECEIVED,
            "Error code 0x" + Integer.toHexString(status & 0xFF));
      }
    }
  }

  protected final void copyBooleanValueToBytes(boolean value, byte[] bytes, int offset) {
    bytes[offset] = value ? (byte) 1 : (byte) 0;
  }

  protected final void copySBYTEValueToBytes(int value, byte[] bytes, int offset) {
    bytes[offset] = (byte) value;
  }

  protected final void copyUBYTEValueToBytes(int value, byte[] bytes, int offset) {
    bytes[offset] = (byte) value;
  }

  protected final void copySWORDValueToBytes(int value, byte[] bytes, int offset) {
    bytes[offset] = (byte) (value & 0xff);
    value = value >> 8;
    bytes[offset + 1] = (byte) (value & 0xff);
  }

  protected final void copyUWORDValueToBytes(int value, byte[] bytes, int offset) {
    bytes[offset] = (byte) (value & 0xff);
    value = value >> 8;
    bytes[offset + 1] = (byte) (value & 0xff);
  }

  protected final void copySLONGValueToBytes(int value, byte[] bytes, int offset) {
    bytes[offset] = (byte) (value & 0xff);
    value = value >> 8;
    bytes[offset + 1] = (byte) (value & 0xff);
    value = value >> 8;
    bytes[offset + 2] = (byte) (value & 0xff);
    value = value >> 8;
    bytes[offset + 3] = (byte) (value & 0xff);
  }

  protected final void copyULONGValueToBytes(long value, byte[] bytes, int offset) {
    bytes[offset] = (byte) (value & 0xff);
    value = value >> 8;
    bytes[offset + 1] = (byte) (value & 0xff);
    value = value >> 8;
    bytes[offset + 2] = (byte) (value & 0xff);
    value = value >> 8;
    bytes[offset + 3] = (byte) (value & 0xff);
  }

  protected final void copyStringValueToBytes(String value, byte[] bytes, int offset,
      int maxCount) {
    if (value.length() > maxCount) {
      value = value.substring(0, maxCount);
    }
    byte[] valueBytes;
    try {
      valueBytes = value.getBytes("ISO-8859-1");
    } catch (UnsupportedEncodingException e) {
      Log.w(logTag, "UnsupportedEncodingException: " + e.getMessage());
      valueBytes = value.getBytes();
    }
    int lengthToCopy = Math.min(maxCount, valueBytes.length);
    System.arraycopy(valueBytes, 0, bytes, offset, lengthToCopy);
  }

  protected final boolean getBooleanValueFromBytes(byte[] bytes, int offset) {
    return bytes[offset] != 0;
  }

  protected final int getSBYTEValueFromBytes(byte[] bytes, int offset) {
    return bytes[offset];
  }

  protected final int getUBYTEValueFromBytes(byte[] bytes, int offset) {
    return bytes[offset] & 0xFF;
  }

  protected final int getSWORDValueFromBytes(byte[] bytes, int offset) {
    return (bytes[offset] & 0xFF) |
           (bytes[offset + 1] << 8);
  }

  protected final int getUWORDValueFromBytes(byte[] bytes, int offset) {
    return (bytes[offset] & 0xFF) |
           ((bytes[offset + 1] & 0xFF) << 8);
  }

  protected final int getSLONGValueFromBytes(byte[] bytes, int offset) {
    return (bytes[offset] & 0xFF) |
           ((bytes[offset + 1] & 0xFF) << 8) |
           ((bytes[offset + 2] & 0xFF) << 16) |
           (bytes[offset + 3] << 24);
  }

  protected final long getULONGValueFromBytes(byte[] bytes, int offset) {
    return (bytes[offset] & 0xFFL) |
           ((bytes[offset + 1] & 0xFFL) << 8) |
           ((bytes[offset + 2] & 0xFFL) << 16) |
           ((bytes[offset + 3] & 0xFFL) << 24);
  }

  protected final String getStringValueFromBytes(byte[] bytes, int offset) {
    // Determine length by looking for the null termination byte.
    int length = 0;
    for (int i = offset; i < bytes.length; i++) {
      if (bytes[i] == 0) {
        length = i - offset;
        break;
      }
    }
    return getStringValueFromBytes(bytes, offset, length);
  }

  protected final String getStringValueFromBytes(byte[] bytes, int offset, int count) {
    try {
      return new String(bytes, offset, count, "ISO-8859-1");
    } catch (UnsupportedEncodingException e) {
      Log.w(logTag, "UnsupportedEncodingException: " + e.getMessage());
      return new String(bytes, offset, count);
    }
  }

  protected final int convertMotorPortLetterToNumber(String motorPortLetter) {
    if (motorPortLetter.length() == 1) {
      return convertMotorPortLetterToNumber(motorPortLetter.charAt(0));
    }
    throw new IllegalArgumentException("Illegal motor port letter " + motorPortLetter);
  }

  protected final int convertMotorPortLetterToNumber(char motorPortLetter) {
    if (motorPortLetter == 'A' || motorPortLetter == 'a') {
      return 0;
    } else if (motorPortLetter == 'B' || motorPortLetter == 'b') {
      return 1;
    } else if (motorPortLetter == 'C' || motorPortLetter == 'c') {
      return 2;
    }
    throw new IllegalArgumentException("Illegal motor port letter " + motorPortLetter);
  }

  protected final int convertSensorPortLetterToNumber(String sensorPortLetter) {
    if (sensorPortLetter.length() == 1) {
      return convertSensorPortLetterToNumber(sensorPortLetter.charAt(0));
    }
    throw new IllegalArgumentException("Illegal sensor port letter " + sensorPortLetter);
  }

  protected final int convertSensorPortLetterToNumber(char sensorPortLetter) {
    if (sensorPortLetter == '1') {
      return 0;
    } else if (sensorPortLetter == '2') {
      return 1;
    } else if (sensorPortLetter == '3') {
      return 2;
    } else if (sensorPortLetter == '4') {
      return 3;
    }
    throw new IllegalArgumentException("Illegal sensor port letter " + sensorPortLetter);
  }

  protected final int sanitizePower(int power) {
    if (power < -100) {
      Log.w(logTag, "power " + power + " is invalid, using -100.");
      power = -100;
    }
    if (power > 100) {
      Log.w(logTag, "power " + power + " is invalid, using 100.");
      power = 100;
    }
    return power;
  }

  protected final int sanitizeTurnRatio(int turnRatio) {
    if (turnRatio < -100) {
      Log.w(logTag, "turnRatio " + turnRatio + " is invalid, using -100.");
      turnRatio = -100;
    }
    if (turnRatio > 100) {
      Log.w(logTag, "turnRatio " + turnRatio + " is invalid, using 100.");
      turnRatio = 100;
    }
    return turnRatio;
  }

  // BluetoothConnectionListener implementation

  @Override
  public void afterConnect(BluetoothConnectionBase bluetoothConnection) {
    // Subclasses may wish to do something.
  }

  @Override
  public void beforeDisconnect(BluetoothConnectionBase bluetoothConnection) {
    // Subclasses may wish to do something.
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    if (bluetooth != null) {
      bluetooth.removeBluetoothConnectionListener(this);
      bluetooth.detachComponent(this);
      bluetooth = null;
    }
  }
}
