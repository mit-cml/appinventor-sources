// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.ErrorReporter;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;

/**
 * Submits the current work to the LMS that launched App Inventor over LTI. Calls
 * the same-origin /lti/submit endpoint, which records the submission with the LMS
 * through LTI Assignment and Grade Services using the launch context. The result
 * is shown in an info banner. No separate LMS login is needed, because the LTI
 * launch already established the session.
 */
public class SubmitToLmsAction implements Command {

  // Guards against a double click sending two submissions.
  private static volatile boolean submitting = false;

  @Override
  public void execute() {
    if (submitting) {
      return;
    }
    submitting = true;
    ErrorReporter.reportInfo("Submitting to your LMS...");
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/lti/submit");
    try {
      builder.sendRequest(null, new RequestCallback() {
        @Override
        public void onResponseReceived(Request request, Response response) {
          submitting = false;
          if (response.getStatusCode() == Response.SC_OK) {
            ErrorReporter.reportInfo(response.getText());
          } else {
            ErrorReporter.reportError("Submit to LMS failed: " + response.getText());
          }
        }

        @Override
        public void onError(Request request, Throwable exception) {
          submitting = false;
          ErrorReporter.reportError("Submit to LMS failed: " + exception.getMessage());
        }
      });
    } catch (RequestException e) {
      submitting = false;
      ErrorReporter.reportError("Submit to LMS failed: " + e.getMessage());
    }
  }
}
