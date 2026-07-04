// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;

/**
 * Submits the current work to the LMS that launched App Inventor over LTI. Posts
 * to the same origin /lti/submit endpoint, which records the submission with the
 * LMS through LTI Assignment and Grade Services using the launch context. A
 * fixed status message is shown, an info banner while submitting and on success
 * and an error banner on failure, and the server response text is never
 * rendered. No separate LMS login is needed, because the LTI launch already
 * established the session.
 */
public class SubmitToLmsAction implements Command {

  // Guards against a double click sending two submissions.
  private static boolean submitting = false;

  @Override
  public void execute() {
    if (submitting) {
      return;
    }
    submitting = true;
    ErrorReporter.reportInfo(MESSAGES.submittingToLms());
    RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, "/lti/submit");
    // A custom header the server requires. A cross site page cannot set it, so
    // only a submission started from within App Inventor is accepted.
    builder.setHeader("X-AppInventor-LTI", "1");
    builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    try {
      builder.sendRequest("projectId=" + projectId, new RequestCallback() {
        @Override
        public void onResponseReceived(Request request, Response response) {
          submitting = false;
          if (response.getStatusCode() == Response.SC_OK) {
            ErrorReporter.reportInfo(MESSAGES.submitToLmsSuccess());
          } else {
            ErrorReporter.reportError(MESSAGES.submitToLmsFailed());
          }
        }

        @Override
        public void onError(Request request, Throwable exception) {
          submitting = false;
          ErrorReporter.reportError(MESSAGES.submitToLmsFailed());
        }
      });
    } catch (RequestException e) {
      submitting = false;
      ErrorReporter.reportError(MESSAGES.submitToLmsFailed());
    }
  }
}
