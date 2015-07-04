// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

/**
 * Provides a common interface to presenting log messages.
 *
 */
public interface Logger {
  /**
   * Prints a log message.
   *
   * @param message message to print
   */
  public void info(String message);

  /**
   * Prints a log warning message.
   *
   * @param message message to print
   */
  public void warn(String message);

  /**
   * Prints a log error message and stacktrace (if provided).
   *
   * @param message message to print
   * @param exception exception stacktrace to print
   */
  public void error(String message, Throwable exception);

  /**
   * Prints a log debug message.
   *
   * @param message message to print
   */
  public void debug(String message);
}
