// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.flags.Flag;

import java.util.logging.Logger;

/**
 * Centralized debug-logging utility for the AI agent pipeline.
 *
 * <p>When the {@code ai.agent.debug} system property is {@code true},
 * all {@link #log} calls emit {@code [AI-DEBUG]} prefixed messages at
 * {@code INFO} level so they appear in Cloud Logging. When the flag is
 * {@code false} (the default) no output is produced.
 */
public final class AIDebug {

  private static final Flag<Boolean> FLAG =
      Flag.createFlag("ai.agent.debug", false);

  private AIDebug() {
  }

  /**
   * Returns {@code true} when debug logging is enabled.
   */
  public static boolean enabled() {
    try {
      return FLAG.get();
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Logs a debug message if the debug flag is enabled.
   *
   * @param logger the logger to use
   * @param msg    the message to log (will be prefixed with {@code [AI-DEBUG]})
   */
  public static void log(Logger logger, String msg) {
    if (enabled()) {
      logger.info("[AI-DEBUG] " + msg);
    }
  }
}
