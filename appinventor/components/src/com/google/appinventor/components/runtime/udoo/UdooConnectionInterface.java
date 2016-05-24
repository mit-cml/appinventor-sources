// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.udoo;

import android.content.ContextWrapper;
import com.google.appinventor.components.runtime.Form;

public interface UdooConnectionInterface
{
  public boolean isConnected();
  public boolean isConnecting();
  public void reconnect();
  public void disconnect();
  
  public UdooArduinoManager arduino();
  public void registerComponent(UdooConnectedInterface component, Form form);
  public void onCreate(ContextWrapper ctx);
  public void onDestroy();
}
