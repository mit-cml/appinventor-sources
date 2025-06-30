// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All Rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.appinventor.client.utils.Urls;
import com.google.appinventor.common.version.GitBuildId;

/**
 * Handles reporting of bugs in the ODE client code.
 */
public final class BugReport {

  // Base URL for the bug report form
  private static final String BUG_REPORT_FORM_LINK = "/ode/feedback";

  private BugReport() {
    // Prevent instantiation
  }

  /**
   * Returns a URL for creating a bug report with no additional information.
   *
   * @return Bug report URL
   */
  public static String getBugReportLink() {
    return getBugReportLink("");
  }

  /**
   * Returns a URL for creating a bug report after an internal error,
   * including exception information.
   *
   * @param exception The exception to include in the report
   * @return Bug report URL with exception details
   */
  public static String getBugReportLink(Throwable exception) {
    // Extract meaningful information from the exception
    String exceptionDetails = exception != null ? exception.toString() : "No exception details";
    return getBugReportLink(exceptionDetails);
  }

  /**
   * Constructs the bug report URL, including any additional information provided.
   *
   * @param additionalInformation Extra details to include in the report
   * @return Bug report URL
   */
  private static String getBugReportLink(String additionalInformation) {
    String notes = Urls.escapeQueryParameter("Browser: " + getUserAgent());
    String foundIn = Urls.escapeQueryParameter(GitBuildId.getVersion());
    long projectId = getCurrentProjectId();

    additionalInformation = Urls.escapeQueryParameter(additionalInformation); // Encode additional info
    return BUG_REPORT_FORM_LINK + 
           "?notes=" + notes + 
           "&foundIn=" + foundIn + 
           "&projectId=" + projectId + 
           "&faultData=" + additionalInformation;
  }

  /**
   * Returns the current project ID or -1 if it cannot be determined.
   *
   * @return Project ID
   */
  private static long getCurrentProjectId() {
    try {
      return Ode.getInstance().getCurrentYoungAndroidProjectRootNode().getProjectId();
    } catch (Exception e) {
      // Log error if necessary
      return -1; // Default value if project ID retrieval fails
    }
  }

  /**
   * Returns information regarding the user agent (browser).
   *
   * @return User agent string
   */
  private static native String getUserAgent() /*-{
    return navigator.userAgent;
  }-*/;
}
