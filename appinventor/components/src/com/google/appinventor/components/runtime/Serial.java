// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2019 MIT, All rights reserved
// Copyright 2017-2019 Kodular, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.Context;
import android.util.Log;

import com.physicaloid.lib.Physicaloid;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import java.io.UnsupportedEncodingException;

@DesignerComponent(version = YaVersion.SERIAL_COMPONENT_VERSION,
    description = "Serial component which can be used to connect to devices like Arduino",
    category = ComponentCategory.CONNECTIVITY,
    nonVisible = true,
    iconName = "images/arduino.png",
    androidMinSdk = 12)

@SimpleObject
@UsesLibraries(libraries = "physicaloid.jar")
public class Serial extends AndroidNonvisibleComponent implements Component {
  private static final String LOG_TAG = "Serial Component";

  private Context context;

  private Physicaloid mPhysicaloid;

  private int baudRate = 9600;
  private int bytes = 256;

  public Serial(ComponentContainer container) {
    super(container.$form());
    context = container.$context();
    Log.d(LOG_TAG, "Created");
  }

  @SimpleFunction(description = "Initializes serial connection.")
  public void InitializeSerial() {
    mPhysicaloid = new Physicaloid(context);
    BaudRate(this.baudRate);
    Log.d(LOG_TAG, "Initialized");
  }

  @SimpleFunction(description = "Opens serial connection. Returns true when opened.")
  public boolean OpenSerial() {
    Log.d(LOG_TAG, "Opening connection");
    if (mPhysicaloid == null) {
      form.dispatchErrorOccurredEvent(Serial.this, "OpenSerial", ErrorMessages.ERROR_SERIAL_NOT_INITIALIZED);
      return false;
    }
    return mPhysicaloid.open();
  }

  @SimpleFunction(description = "Closes serial connection. Returns true when closed.")
  public boolean CloseSerial() {
    Log.d(LOG_TAG, "Closing connection");
    if (mPhysicaloid == null) {
      form.dispatchErrorOccurredEvent(Serial.this, "CloseSerial", ErrorMessages.ERROR_SERIAL_NOT_INITIALIZED);
      return false;
    }
    return mPhysicaloid.close();
  }

  @SimpleFunction(description = "Reads data from serial.")
  public String ReadSerial() {
    String data = "";
    if (mPhysicaloid == null) {
      form.dispatchErrorOccurredEvent(Serial.this, "ReadSerial", ErrorMessages.ERROR_SERIAL_NOT_INITIALIZED);
    } else {
      byte[] buf = new byte[this.bytes];
      if (mPhysicaloid.read(buf) > 0) {
        try {
          data = new String(buf, "UTF-8");
        } catch (UnsupportedEncodingException mEr) {
          Log.e(LOG_TAG, mEr.getMessage());
        }
      }
    }
    return data;
  }

  @SimpleFunction(description = "Writes given data to serial.")
  public void WriteSerial(String data) {
    if (!data.isEmpty() && mPhysicaloid != null) {
      byte[] buf = data.getBytes();
      int result = mPhysicaloid.write(buf);
      if (result == -1)
        form.dispatchErrorOccurredEvent(Serial.this, "WriteSerial", ErrorMessages.ERROR_SERIAL_WRITING);
    } else if (mPhysicaloid == null) {
      form.dispatchErrorOccurredEvent(Serial.this, "WriteSerial", ErrorMessages.ERROR_SERIAL_NOT_INITIALIZED);
    }
  }

  @SimpleFunction(description = "Writes given data to serial, and appends a new line at the end.")
  public void PrintSerial(String data) {
    if (!data.isEmpty())
      WriteSerial(data + "\n");
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns true when the Serial connection is open.")
  public boolean IsOpen() {
    if (mPhysicaloid == null) {
      form.dispatchErrorOccurredEvent(Serial.this, "IsOpen", ErrorMessages.ERROR_SERIAL_NOT_INITIALIZED);
      return false;
    }
    return mPhysicaloid.isOpened();
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns true when the Serial has been initialized.")
  public boolean IsInitialized() {
    return mPhysicaloid != null;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns the current baud rate")
  public int BaudRate() {
    return this.baudRate;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "9600")
  @SimpleProperty
  public void BaudRate(int baudRate) {
    this.baudRate = baudRate;
    Log.d(LOG_TAG, "Baud Rate: " + baudRate);
    if (mPhysicaloid != null)
      mPhysicaloid.setBaudrate(baudRate);
    else
      Log.w(LOG_TAG, "Could not set Serial Baud Rate to " + baudRate + ". Just saved, not applied to serial! Maybe you forgot to initialize it?");
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns the buffer size in bytes")
  public int BufferSize() {
    return this.bytes;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "256")
  @SimpleProperty
  public void BufferSize(int bytes) {
    this.bytes = bytes;
    Log.d(LOG_TAG, "Buffer Size: " + bytes);
  }
}
