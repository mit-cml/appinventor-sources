// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

/**
 * @author francesco.monte@gmail.com
 */
class UdooConnectionFactory
{
  static UdooConnectionInterface getConnection(String transport, String remoteAddress, String remotePort, String remoteSecret)
  {
    if (transport.equals("local")) {
      return UdooAdkBroadcastReceiver.getInstance();
    } else {
      return new UdooTcpRedirector(remoteAddress, remotePort, remoteSecret);
    }
  }
}
