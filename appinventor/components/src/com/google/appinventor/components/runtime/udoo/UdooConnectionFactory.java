// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.udoo;

import android.util.Log;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.SdkLevel;
import java.util.HashMap;

/**
 * Factory to create UdooConnectionInterface objects.
 * 
 * @author Francesco Montefoschi francesco.monte@gmail.com
 */
public class UdooConnectionFactory
{
  private static final String TAG = "UdooConnectionFactory";
  private static HashMap<String, UdooTcpRedirector> connections = new HashMap<String, UdooTcpRedirector>();
  private static UdooAdkBroadcastReceiver adkInstance = null;
  
  public static UdooConnectionInterface getConnection(UdooConnectedInterface component, Form form)
  {
    if (component.isLocal()) {
      if (adkInstance == null) {
        if (SdkLevel.getLevel() < SdkLevel.LEVEL_HONEYCOMB) {
          form.dispatchErrorOccurredEvent((Component)component, "getTransport", ErrorMessages.ERROR_UDOO_ADK_UNAVAILABLE);
          return null;
        }

        Log.d(TAG, "Creating local UdooAdkBroadcastReceiver");
        adkInstance = new UdooAdkBroadcastReceiver();
      }
      
      return adkInstance;
      
    } else {
      String key = component.getRemoteAddress() + component.getRemotePort();
      UdooTcpRedirector conn = connections.get(key);
      if (conn == null) {
        Log.d(TAG, "Creating remote UdooTcpRedirector");
        conn = new UdooTcpRedirector(component.getRemoteAddress(), component.getRemotePort(), component.getRemoteSecret());
        connections.put(key, conn);
      } else {
        Log.d(TAG, "Recycling remote UdooTcpRedirector");
      }
      
      return conn;
    }
  }
}
