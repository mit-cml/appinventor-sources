// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.common.version.GitBuildId;
import com.google.appinventor.server.util.BuildData;

import com.google.appengine.api.utils.SystemProperty;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

public final class CrashReport {

  /**
   * Constant for 'information not available'
   */
  public static final String NOT_AVAILABLE = "n/a";

  // Build information
  private static String buildData = BuildData.getTimestampAsString();

  private CrashReport() {
  }

  /**
   * Wraps the given exception into a runtime error and logs an error.
   *
   * @param log  logger for to log error
   * @param req HTTP request (may be {@code null})
   * @param extraInfo  additional information about the context of the crash
   *                   (may be {@code null}), preferably formatted as
   *                   'key1=value1[\n char]key2=value2'
   * @param exception  exception to log
   * @return  runtime exception wrapping the original exception
   */
  public static RuntimeException createAndLogError(Logger log, HttpServletRequest req,
      String extraInfo, Throwable exception) {
    return createAndLogError(log, req, extraInfo, new RuntimeException(exception));
  }

  /**
   * Logs an error.
   *
   * @param log  logger for to log error
   * @param req HTTP request (may be {@code null})
   * @param extraInfo  additional information about the context of the crash
   *                   (may be {@code null}), preferably formatted as
   *                   'key1=value1[\n char]key2=value2'
   * @param exception  exception to log
   * @return  runtime exception wrapping the original exception
   */
  public static RuntimeException createAndLogError(Logger log, HttpServletRequest req,
      String extraInfo, RuntimeException exception) {
    log.log(Level.SEVERE, exception.getMessage() + ": " + extraInfo + "\n" + extraExtraInfo(req),
        exception);
    return exception;
  }

  /**
   * Gets extra system information to add to logs.
   *
   * @param req HTTP request (may be {@code null})
   */
  private static String extraExtraInfo(HttpServletRequest req) {
    StringBuilder s = new StringBuilder();

    // If the app is running on App Engine...
    if (Server.isProductionServer()) {
      // the version ID of the runtime environment
      String version = SystemProperty.version.get();
      s.append("runtime.version").append("=").append(version).append("\n");
      // the application major version number + deploy timestamp
      version = SystemProperty.applicationVersion.get();
      s.append("application.version").append("=").append(version).append("\n");
    }
    s.append("build.version").append("=").append(buildData).append("\n");
    s.append("git.build.version").append("=").append(GitBuildId.getVersion()).append("\n");
    if (req != null) {
      s.append("request.details").append("=").append(req.toString()).append("\n");
    }
    return new String(s);
  }

}
