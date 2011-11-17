// Copyright 2009 Google Inc. All Rights Reserved.

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
