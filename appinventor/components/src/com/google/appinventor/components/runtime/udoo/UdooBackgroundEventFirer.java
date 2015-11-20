// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.udoo;

import android.os.AsyncTask;
import android.util.Log;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.EventDispatcher;

public class UdooBackgroundEventFirer extends AsyncTask<Component, Void, Void>
{
  private final String TAG = "UdooBackgroundEventFirer";
  
  int pinNumber;
  
  public UdooBackgroundEventFirer setPinNumber(int pin) {
    pinNumber = pin;
    return this;
  }
  
  String eventName;
  
  public UdooBackgroundEventFirer setEventName(String event) {
    eventName = event;
    return this;
  }
  
  @Override
  protected Void doInBackground(Component... component) {
    Log.d(TAG, "Firing event " + eventName);
    
    if (eventName.equals("InterruptFired")) {
      EventDispatcher.dispatchEvent(component[0], eventName, pinNumber);
    } else {
      EventDispatcher.dispatchEvent(component[0], eventName);
    }
    
    return null;
  }
}

