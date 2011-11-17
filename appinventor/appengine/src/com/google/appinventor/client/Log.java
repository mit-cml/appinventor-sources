// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Forwards logging calls to loggers.
 *
 */
// TODO(user): Use deferred binding to make sure that logging messages
// are compiled out.
public class Log {
  private static final List<Logger> loggers = new ArrayList<Logger>();

  private Log() {
  }

  /**
   * Adds a logger to send logging messages to.
   */
  public static void logTo(Logger logger) {
    loggers.add(logger);
  }

  /**
   * Logs an informational message.
   */
  public static void info(String message) {
    for (Logger l : loggers) {
      l.info(message);
    }
  }

  /**
   * Logs a warning message.
   */
  public static void warn(String message) {
    for (Logger l : loggers) {
      l.warn(message);
    }
  }

  /**
   * Logs a debugging message.
   */
  public static void debug(String message) {
    for (Logger l : loggers) {
      l.debug(message);
    }
  }

  /**
   * Logs an error and the stack trace of the associated exception.
   *
   * @param message error message to log
   * @param exception exception to print stack trace for
   */
  public static void error(String message, Throwable exception) {
    for (Logger l : loggers) {
      l.error(message, exception);
    }
  }

  /**
   * Returns the result of the toString method on the object.
   */
  // TODO(user): Replace this with an empty method using deferred binding
  // for production code.
  public static String toLogString(Object o) {
    return o.toString();
  }
}
