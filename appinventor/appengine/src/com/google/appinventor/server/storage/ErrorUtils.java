package com.google.appinventor.server.storage;

import com.google.appinventor.server.CrashReport;

public final class ErrorUtils {
  private ErrorUtils() {
  }

  public static String collectUserErrorInfo(final String userId) {
    return collectUserErrorInfo(userId, CrashReport.NOT_AVAILABLE);
  }

  public static String collectUserErrorInfo(final String userId, String fileName) {
    return "user=" + userId + ", file=" + fileName;
  }

  public static String collectProjectErrorInfo(final String userId, final long projectId, final String fileName) {
    return "user=" + userId + ", project=" + projectId + ", file=" + fileName;
  }

  public static String collectUserProjectErrorInfo(final String userId, final long projectId) {
    return "user=" + userId + ", project=" + projectId;
  }
}
