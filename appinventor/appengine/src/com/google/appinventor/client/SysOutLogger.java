// Copyright 2009 Google Inc. All Rights Reserved.

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
