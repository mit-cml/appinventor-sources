// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client;

import com.google.appinventor.client.utils.Urls;
import com.google.appinventor.common.version.GitBuildId;

/**
 * Handles reporting of bugs in the ODE client code.
 *
 */
public final class BugReport {

  /**
   * Returns an URL for creating a bug report (with some information already
   * filled in).
   */

  // This URL goes to the dogfooding bug report form.
  // TODO(halabelson): Decide what to do when we release.
  // TODO(user): Decide what to do.
  private static final String BUG_REPORT_FORM_LINK =
      "http://code.google.com/p/app-inventor-for-android/wiki/ReportingBugs";


  public static String getBugReportLink() {
    return getBugReportLink("");
  }

  /**
   * Returns an URL for creating a bug report after an internal error (with
   * some information already filled in).
   */
  public static String getBugReportLink(Throwable exception) {
    // TODO(lizlooney) - We should grep through our client code for "throw" and make all of our
    // exception messages contain an identifiable and searchable name. For example, we could name
    // our exceptions like Tropical Storms. (We could even upgrade them to Hurricanes if they happen
    // too often!)
    // TODO(lizlooney) - We need to use different code to get the stack trace so that it properly
    // handles the GWT UmbrellaException, which has multiple causes. Also, since our production
    // code is obfuscated, the stack trace may be much less important than the exception messages.
    return getBugReportLink(exception.toString());
  }

  /**
   * Returns an URL for creating a bug report.
   */
  private static String getBugReportLink(String additionalInformation) {
    // TODO(user): additionalInformation, notes and foundIn all appear to be ignored.
    String notes = Urls.escapeQueryParameter("Browser: " + getUserAgent() + "\n\n" +
        "Please attach a screenshot to help us fix the problem!\n\n" +
    "Steps to reproduce:\n");
    String foundIn = Urls.escapeQueryParameter(GitBuildId.getVersion());
    return BUG_REPORT_FORM_LINK;

  }
  

  /**
   * Returns information regarding the user agent (aka browser).
   */
  private static native String getUserAgent() /*-{
    return navigator.userAgent;
  }-*/;
}
