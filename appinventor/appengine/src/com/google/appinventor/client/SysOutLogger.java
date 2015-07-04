// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

/**
 * Provides logging to {@link System#out}.
 *
 */
public class SysOutLogger implements Logger {
  @Override
  public void info(String message) {
    System.out.println("I " + message);
  }

  @Override
  public void warn(String message) {
    System.out.println("W " + message);
  }

  @Override
  public void debug(String message) {
    System.out.println("D " + message);
  }

  @Override
  public void error(String message, Throwable exception) {
    System.out.println("E " + message);
    exception.printStackTrace(System.out);
  }
}
