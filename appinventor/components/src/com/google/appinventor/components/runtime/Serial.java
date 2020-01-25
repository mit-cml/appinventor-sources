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
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.YaVersion;

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

  public Serial(ComponentContainer container) {
    super(container.$form());
    context = container.$context();
    Log.d(LOG_TAG, "Created");
  }

  @SimpleFunction(description = "Initializes serial connection.")
  public void InitializeSerial() {
    mPhysicaloid = new Physicaloid(context);
    self.BaudRate(self.baudRate);
    Log.d(LOG_TAG, "Initialized");
  }

  @SimpleFunction(description = "Opens serial connection. Returns true when opened.")
  public boolean OpenSerial() {
    Log.d(LOG_TAG, "Opening connection");
    if (mPhysicaloid == null)
      return false;
    return mPhysicaloid.open();
  }

  @SimpleFunction(description = "Closes serial connection. Returns true when closed.")
  public boolean CloseSerial() {
    Log.d(LOG_TAG, "Closing connection");
    if (mPhysicaloid == null)
      return false;
    return mPhysicaloid.close();
  }

  @SimpleFunction(description = "Sets a new baud rate. Default is 9600 bps.")
  public void BaudRate(int baudRate) {
    this.baudRate = baudRate;
    Log.d(LOG_TAG, "Baud Rate: " + baudRate);
    if (mPhysicaloid != null)
      mPhysicaloid.setBaudrate(baudRate);
    else
      Log.w(LOG_TAG, "Could not set Serial Baud Rate to " + baudRate + ". Just saved, not applied to serial! Maybe you forgot to initialize it?");
  }

  @SimpleFunction(description = "Reads data from serial.")
  public void ReadSerial() {
    if (mPhysicaloid == null)
      AfterReadSerial(false, "");
    else {
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
  
      AfterReadSerial(success, data);
    }
  }

  @SimpleFunction(description = "Writes given data to serial.")
  public void WriteSerial(String writeDataSerial) {
    if (!writeDataSerial.isEmpty() && mPhysicaloid != null) {
      byte[] buf = writeDataSerial.getBytes();
      mPhysicaloid.write(buf);
    }
  }

  @SimpleFunction(description = "Writes given data to serial, and appends a new line at the end.")
  public void PrintSerial(String writeDataSerial) {
    if (!writeDataSerial.isEmpty()) {
      WriteSerial(writeDataSerial + "\n");
    }
  }

  @SimpleFunction(description = "Returns true when the Serial connection is open.")
  public boolean IsOpenedSerial() {
    if (mPhysicaloid == null)
      return false;
    return mPhysicaloid.isOpened();
  }

  @SimpleFunction(description = "Returns true when the Serial has been initialized.")
  public boolean IsInitialized() {
    return mPhysicaloid != null;
  }

  @SimpleEvent(description = "Triggered after ReadSerial method.")
  public void AfterReadSerial(boolean success, String data) {
    EventDispatcher.dispatchEvent(this, "AfterRead", success, data);
  }
}
