// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.udoo.UdooConnectionInterface;
import com.google.appinventor.components.runtime.udoo.UdooConnectedInterface;
import android.util.Log;
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
import com.google.appinventor.components.runtime.udoo.UdooBackgroundEventFirer;
import com.google.appinventor.components.runtime.util.ErrorMessages;

/**
 * A component that interfaces with sensors connected to UDOO boards.
 *
 * @author francesco.monte@gmail.com
 */
@DesignerComponent(version = YaVersion.UDOO_SERVO_COMPONENT_VERSION,
    description = "A component that uses Servo.h library for the Arduino on UDOO boards.",
    category = ComponentCategory.UDOO,
    nonVisible = true,
    iconName = "images/udooGear.png")
@SimpleObject
public class UdooServo extends AndroidNonvisibleComponent
implements UdooConnectedInterface
{
  private UdooConnectionInterface connection = null;
  private final String TAG = "UdooServo";
  private String pin = null;

  public UdooServo(Form form) {
    super(form);
  }
  
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Sets the pin where the servo motor is connected to. ")
  public void PinNumber(String pin) {
    this.pin = pin;
  }
  
  @SimpleFunction
  public void attach()
  {
    if (this.isConnected() && this.pin!=null) {
      getTransport().arduino().servo(pin, "attach");
    }
  }
  
  @SimpleFunction
  public void detach()
  {
    if (this.isConnected() && this.pin!=null) {
      getTransport().arduino().servo(pin, "detach");
    }
  }
  
  @SimpleFunction
  public void write(int degrees)
  {
    if (degrees<0 || degrees>180) {
      form.dispatchErrorOccurredEvent(this, "write", ErrorMessages.ERROR_UDOO_SERVO_WRITE);
      return;
    }
    
    if (this.isConnected() && this.pin!=null) {
      getTransport().arduino().servo(pin, "write", degrees);
    }
  }
  

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_UDOO_ARDUINO_CONNECTION,
    defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void UdooArduino(UdooArduino udooArduino) {
    this.connection = udooArduino.getTransport();
    this.connection.registerComponent(this, form);
  }
  
  @SimpleEvent(description = "Fires when the Arduino is (re)connected.")
  public void Connected()
  {
    UdooBackgroundEventFirer ef = new UdooBackgroundEventFirer();
    ef.setEventName("Connected");
    ef.execute(this);
  }
  
  public synchronized boolean isConnected()
  {
    boolean isc = getTransport().isConnected();
    if (!isc) {
      if (!getTransport().isConnecting()) {
        getTransport().reconnect();
      }
    }
    return isc;
  }
  
  private UdooConnectionInterface getTransport()
  {
    return this.connection;
  }
}
