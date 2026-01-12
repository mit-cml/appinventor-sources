// -*- mode: java; c-basic-offset: 2; -*-
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
import com.google.appinventor.components.runtime.util.Ev3BinaryParser;
import java.util.Collections;

/**
 * The base class for EV3 components.
 *
 * @author jerry73204@gmail.com (jerry73204)
 * @author spaded06543@gmail.com (Alvin Chang)
 */
@SimpleObject
public class LegoMindstormsEv3Base extends AndroidNonvisibleComponent
  implements BluetoothConnectionListener, Component, Deleteable {

  private static final int TOY_ROBOT = 0x0804;
  protected int commandCount;
  protected final String logTag;
  protected BluetoothClient bluetooth;

  protected LegoMindstormsEv3Base(ComponentContainer container, String logTag) {
    super(container.$form());
    this.logTag = logTag;
  }

  protected LegoMindstormsEv3Base() {
    super(null);
    logTag = null;
  }

  @SimpleProperty(description = "The BluetoothClient component that should be used for communication.",
                  category = PropertyCategory.BEHAVIOR)
  public BluetoothClient BluetoothClient() {
    return bluetooth;
  }

  /**
   * Specifies the BluetoothClient component that should be used for communication.
   * **Must be set in the Designer**.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BLUETOOTHCLIENT,
                    defaultValue = "")
  @SimpleProperty
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

  protected final boolean isBluetoothConnected(String functionName) {
    if (bluetooth == null) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_BLUETOOTH_NOT_SET);
      return false;
    }

    if (!bluetooth.IsConnected()) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_NOT_CONNECTED_TO_ROBOT);
      return false;
    }

    return true;
  }

  protected final byte[] sendCommand(String functionName, byte[] command, boolean doReceiveReply) {
    // check connecttivity
    if (!isBluetoothConnected(functionName))
      return null;

    // prepend header and send payload
    byte[] header = Ev3BinaryParser.pack("hh", (short) (command.length + 2), (short) commandCount);
    commandCount++;

    bluetooth.write(functionName, header);
    bluetooth.write(functionName, command);

    // receive reply if required
    if (doReceiveReply) {
      header = bluetooth.read(functionName, 4);

      if (header.length == 4) {
        Object[] decodedHeader = Ev3BinaryParser.unpack("hh", header);
        int replySize = (int) ((Short) decodedHeader[0]) - 2;
        int replyCount = (int) ((Short) decodedHeader[1]);
        byte[] reply = bluetooth.read(functionName, replySize);

        if (reply.length == replySize)
          return reply;
        else
        {
          form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_INVALID_REPLY);
          return null;
        }
      }

      // handle errors
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_EV3_INVALID_REPLY);
      return null;
    } else {
      return null;
    }
  }

  protected final int sensorPortLetterToPortNumber(String letter) {
    if (letter.length() != 1)
      throw new IllegalArgumentException("String \"" + letter + "\" is not a valid sensor port letter");

    int portNumber = letter.charAt(0) - '1';

    if (portNumber < 0 || portNumber > 3)
      throw new IllegalArgumentException("String \"" + letter + "\" is not a valid sensor port letter");

    return portNumber;
  }

  protected final String portNumberToSensorPortLetter(int portNumber) {
    if (portNumber < 0 || portNumber > 3)
      throw new IllegalArgumentException(portNumber + " is not a valid port number");

    return "" + ('1' + portNumber);
  }

  protected final int motorPortLettersToBitField(String letters) {
    if (letters.length() > 4)
      throw new IllegalArgumentException("Malformed motor port letters \"" + letters + "\"");

    int portABit = 0;
    int portBBit = 0;
    int portCBit = 0;
    int portDBit = 0;

    for (int i = 0; i < letters.length(); i++)
    {
      switch (letters.charAt(i))
      {
      case 'A':
        if (portABit != 0)
          throw new IllegalArgumentException("Malformed motor port letters \"" + letters + "\"");
        portABit = 1;
        break;

      case 'B':
        if (portBBit != 0)
          throw new IllegalArgumentException("Malformed motor port letters \"" + letters + "\"");
        portBBit = 2;
        break;

      case 'C':
        if (portCBit != 0)
          throw new IllegalArgumentException("Malformed motor port letters \"" + letters + "\"");
        portCBit = 4;
        break;

      case 'D':
        if (portDBit != 0)
          throw new IllegalArgumentException("Malformed motor port letters \"" + letters + "\"");
        portDBit = 8;
        break;

      default:
        throw new IllegalArgumentException("Malformed motor port letters \"" + letters + "\"");
      }
    }

    return portABit | portBBit | portCBit | portDBit;
  }

  protected final String bitFieldToMotorPortLetters(int bitField) {
    if (bitField < 0 || bitField > 15)
      throw new IllegalArgumentException("Invalid bit field number " + bitField);

    String portLetters = "";

    if ((bitField & 1) != 0)
      portLetters += "A";

    if ((bitField & 2) != 0)
      portLetters += "B";

    if ((bitField & 4) != 0)
      portLetters += "C";

    if ((bitField & 8) != 0)
      portLetters += "D";

    return portLetters;
  }

  @Override
  public void afterConnect(BluetoothConnectionBase bluetoothConnection) {
    // Subclasses may wish to do something.
  }

  @Override
  public void beforeDisconnect(BluetoothConnectionBase bluetoothConnection) {
    // Subclasses may wish to do something.
  }

  // interface Deleteable implementation
  @Override
  public void onDelete() {
    if (bluetooth != null) {
      bluetooth.removeBluetoothConnectionListener(this);
      bluetooth.detachComponent(this);
      bluetooth = null;
    }
  }
}
