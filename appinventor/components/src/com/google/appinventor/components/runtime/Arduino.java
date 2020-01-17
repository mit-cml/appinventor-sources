// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2019 MIT, All rights reserved
// Copyright 2017-2019 Kodular, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.Context;
import android.util.Log;

import com.physicaloid.lib.*;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.common.*;

import java.io.UnsupportedEncodingException;

@DesignerComponent(version = YaVersion.ARDUINO_COMPONENT_VERSION,
    description = "Arduino USB Serial Component",
    category = ComponentCategory.CONNECTIVITY,
    nonVisible = true,
    iconName = "images/arduino.png")

@SimpleObject
@UsesLibraries(libraries = "physicaloid.jar")
public class Arduino extends AndroidNonvisibleComponent implements Component {
  private static final String LOG_TAG = "Arduino USB Serial Component";

  private Context context;

  Physicaloid mPhysicaloid;

  private int baudRate = 9600;

  public Arduino(ComponentContainer container) {
    super(container.$form());
    context = container.$context();
    Log.d(LOG_TAG, "Created");
  }

  @SimpleFunction(description = "Initializes Arduino Connection")
  public void InitializeArduino() {
    mPhysicaloid = new Physicaloid(context);
    Log.d(LOG_TAG, "Initialized");
  }

  @SimpleFunction(description = "Opens Arduino Connection")
  public boolean OpenArduino() {
    Log.d(LOG_TAG, "Opening connection");
    return mPhysicaloid.open();
  }

  @SimpleFunction(description = "Closes Arduino Connection")
  public boolean CloseArduino() {
    Log.d(LOG_TAG, "Closing connection");
    return mPhysicaloid.close();
  }

  @SimpleFunction(description = "Default baud rate is 9600 bps")
  public void BaudRate(int baudRate) {
    this.baudRate = baudRate;
    mPhysicaloid.setBaudrate(baudRate);
    Log.d(LOG_TAG, "Baud Rate: " + baudRate);
  }

  @SimpleFunction(description = "Read from Serial")
  public void ReadArduino() {
    byte[] buf = new byte[256];
    boolean success = true;
    String data = "";

    if (mPhysicaloid.read(buf) > 0) {
      try {
        data = new String(buf, "UTF-8");
      } catch (UnsupportedEncodingException mEr) {
        success = false;
        Log.e(LOG_TAG, mEr.getMessage());
      }
    } else {
      success = false;
    }

    AfterReadArduino(success, data);
  }

  @SimpleFunction(description = "Write Data to Serial")
  public void WriteArduino(String writeDataArduino) {
    if (!writeDataArduino.isEmpty()) {
      byte[] buf = writeDataArduino.getBytes();
      mPhysicaloid.write(buf);
    }
  }

  @SimpleFunction(description = "Returns true when the Arduino connection is open")
  public boolean IsOpenedArduino() {
    return mPhysicaloid.isOpened();
  }

  @SimpleEvent(description = "Triggered after Read function")
  public void AfterReadArduino(boolean success, String data) {
    EventDispatcher.dispatchEvent(this, "AfterRead", success, data);
  }
}
