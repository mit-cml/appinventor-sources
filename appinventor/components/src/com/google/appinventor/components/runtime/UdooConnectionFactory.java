// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.SdkLevel;

/**
 * @author francesco.monte@gmail.com
 */
class UdooConnectionFactory
{
  static UdooConnectionInterface getConnection(UdooConnectedInterface component, Form form)
  {
    if (component.isLocal()) {
      if (SdkLevel.getLevel() < SdkLevel.LEVEL_HONEYCOMB) {
        form.dispatchErrorOccurredEvent((Component)component, "getTransport", ErrorMessages.ERROR_UDOO_ADK_UNAVAILABLE);
      }
      return UdooAdkBroadcastReceiver.getInstance();
    } else {
      return new UdooTcpRedirector(component.getRemoteAddress(), component.getRemotePort(), component.getRemoteSecret());
    }
  }
}
