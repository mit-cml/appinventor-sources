// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime.udoo;

public interface UdooConnectedInterface
{
  public void Connected();

  public boolean isLocal();
  public String getRemoteAddress();
  public String getRemotePort();
  public String getRemoteSecret();
}
