// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Centralized debug-logging utility for the AI agent pipeline.
 *
 * <p>When {@link AppInventorFeatures#aiAgentDebugEnabled()} is {@code true},
 * debug messages are captured for the current request scope.
 *
 * <h3>Development mode (local dev server)</h3>
 * <p>Messages are buffered per-request and flushed to
 * {@code build/logs/aiagent/<conversationId>/<unixTimestamp>.txt}
 * when {@link #endRequest()} is called. Nothing is written to the console.
 *
 * <h3>Production mode</h3>
 * <p>Messages are logged immediately to the dedicated {@code aiagent.debug}
 * logger (separate from the application's class-level loggers) for external
 * ingestion. Each entry is tagged with the conversation ID.
 *
 * <p>Callers must bracket each request with {@link #beginRequest} /
 * {@link #endRequest} (the latter in a {@code finally} block). The
 * {@link #log} call signature is unchanged for call-site compatibility.
 */
public final class AIDebug {

  /** Dedicated logger for production AI debug output. */
  private static final Logger AI_LOGGER =
      Logger.getLogger("aiagent.debug");

  /** Gson instance for structured logging in production. */
  private static final Gson GSON = new Gson();

  /** Lazily-resolved base directory for dev-mode log files. */
  private static volatile File logBaseDir;

  /** Thread-safe date formatter for per-line timestamps. */
  private static final ThreadLocal<SimpleDateFormat> TIME_FORMAT =
      new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
          return new SimpleDateFormat("HH:mm:ss.SSS");
        }
      };

  private AIDebug() {
  }

  // ---- Per-request state ----

  private static final class RequestLog {
    final String conversationId;
    final long timestamp;
    final List<String> messages;

    RequestLog(String conversationId) {
      this.conversationId = conversationId;
      this.timestamp = System.currentTimeMillis() / 1000;
      this.messages = new ArrayList<String>();
    }
  }

  private static final ThreadLocal<RequestLog> CURRENT_REQUEST =
      new ThreadLocal<RequestLog>();

  // ---- Public API ----

  /**
   * Returns {@code true} when debug logging is enabled.
   */
  public static boolean enabled() {
    try {
      return AppInventorFeatures.aiAgentDebugEnabled();
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Begins a debug logging scope for one user message / request.
   * Must be paired with {@link #endRequest()} in a {@code finally} block.
   *
   * @param conversationId the conversation UUID (used as the log folder
   *                       name in development mode, and as a tag in
   *                       production log entries)
   */
  public static void beginRequest(String conversationId) {
    if (!enabled()) {
      return;
    }
    CURRENT_REQUEST.set(new RequestLog(
        conversationId != null ? conversationId : "unknown"));
  }

  /**
   * Logs a debug message within the current request scope.
   *
   * <p>In development mode the message is buffered and written to a file
   * when {@link #endRequest()} is called. In production it is sent
   * immediately to the dedicated {@code aiagent.debug} logger.
   *
   * <p>The {@code logger} parameter is accepted for call-site compatibility
   * but is not used; all output is routed through the internal mechanism.
   *
   * @param logger ignored (kept for call-site compatibility)
   * @param msg    the message to log
   */
  public static void log(Logger logger, String msg) {
    if (!enabled()) {
      return;
    }
    String timestamp = TIME_FORMAT.get().format(new Date());
    String line = "[" + timestamp + "] " + msg;

    if (isProductionMode()) {
      // Production: emit structured JSON to stdout for Cloud Logging.
      // Fields land in jsonPayload and are filterable in Logs Explorer.
      RequestLog rl = CURRENT_REQUEST.get();
      JsonObject entry = new JsonObject();
      entry.addProperty("severity", "INFO");
      entry.addProperty("message", msg);
      if (rl != null) {
        entry.addProperty("conversationId", rl.conversationId);
        entry.addProperty("messageId", rl.timestamp);
      }
      System.out.println(GSON.toJson(entry));
    } else {
      // Development: buffer for file output on endRequest().
      RequestLog rl = CURRENT_REQUEST.get();
      if (rl != null) {
        rl.messages.add(line);
      }
      // If no request scope is active, silently drop.
    }
  }

  /**
   * Ends the current request scope and flushes buffered messages.
   * In development mode this writes to a log file. Safe to call even
   * if {@link #beginRequest} was never called or debug is disabled.
   */
  public static void endRequest() {
    RequestLog rl = CURRENT_REQUEST.get();
    CURRENT_REQUEST.remove();
    if (rl == null || rl.messages.isEmpty()) {
      return;
    }
    // Only dev mode buffers; production logs immediately in log().
    if (!isProductionMode()) {
      writeToFile(rl);
    }
  }

  // ---- Dev-mode file output ----

  private static void writeToFile(RequestLog rl) {
    try {
      File baseDir = getLogBaseDir();
      File convDir = new File(baseDir, rl.conversationId);
      if (!convDir.exists()) {
        convDir.mkdirs();
      }

      File logFile = new File(convDir, rl.timestamp + ".txt");
      PrintWriter writer = null;
      try {
        writer = new PrintWriter(new OutputStreamWriter(
            new FileOutputStream(logFile, true), "UTF-8"));
        for (String msg : rl.messages) {
          writer.println(msg);
        }
      } finally {
        if (writer != null) {
          writer.close();
        }
      }
    } catch (Exception e) {
      // Last resort: stderr to avoid recursion through the logging system.
      System.err.println("[AIDebug] Failed to write log file: " + e.getMessage());
    }
  }

  // ---- Environment detection ----

  private static boolean isProductionMode() {
    try {
      return SystemProperty.environment.value()
          == SystemProperty.Environment.Value.Production;
    } catch (Exception e) {
      // If App Engine environment is unavailable, assume dev mode.
      return false;
    }
  }

  // ---- Log directory resolution ----

  private static File getLogBaseDir() {
    if (logBaseDir == null) {
      synchronized (AIDebug.class) {
        if (logBaseDir == null) {
          logBaseDir = resolveLogBaseDir();
        }
      }
    }
    return logBaseDir;
  }

  /**
   * Resolves the base directory for log files.
   *
   * <ol>
   *   <li>If {@code ai.agent.log.dir} system property is set, use it.</li>
   *   <li>Otherwise, derive from the class-file location: walk up the
   *       path until a directory named {@code "build"} is found and
   *       return {@code build/logs/aiagent}.</li>
   *   <li>Fallback: {@code appengine/build/logs/aiagent} relative to
   *       the working directory.</li>
   * </ol>
   */
  private static File resolveLogBaseDir() {
    String configured = System.getProperty("ai.agent.log.dir");
    if (configured != null && !configured.isEmpty()) {
      return new File(configured);
    }
    try {
      String path = AIDebug.class.getProtectionDomain()
          .getCodeSource().getLocation().getPath();
      File file = new File(URLDecoder.decode(path, "UTF-8"));
      File dir = file;
      while (dir != null && !"build".equals(dir.getName())) {
        dir = dir.getParentFile();
      }
      if (dir != null) {
        return new File(dir, "logs" + File.separator + "aiagent");
      }
    } catch (Exception e) {
      // ignore, use fallback
    }
    return new File("appengine" + File.separator + "build"
        + File.separator + "logs" + File.separator + "aiagent");
  }
}
