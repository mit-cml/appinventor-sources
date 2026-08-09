// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.EditorManager;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;

/**
 * Submits the current project to the LMS that launched App Inventor over LTI, by
 * posting to the same origin /lti/submit endpoint. No separate LMS login is
 * needed, because the LTI launch already established the session.
 *
 * <p>Work in an open editor reaches the server on a timer rather than on every
 * keystroke, so the editors are saved first and the submission is only sent once
 * that save has finished. Otherwise a learner who edits and submits straight away
 * would hand in whatever the last automatic save happened to catch.
 *
 * <p>Every attempt carries a number, and a callback that arrives after its own
 * attempt has ended is ignored. A late answer from an attempt that already timed
 * out therefore cannot report over a newer attempt or release its guard.
 */
public class SubmitToLmsAction implements Command {

  /**
   * How long to wait for the submit request. The server freezes a copy of the
   * project and then reaches the platform over the network, so this is generous.
   * It is here so a request that never answers cannot leave the menu item unusable
   * until the page is reloaded, rather than as a limit on a healthy submission.
   */
  private static final int REQUEST_TIMEOUT_MILLIS = 60000;

  /**
   * A backstop for the save that runs before the request. The request carries its
   * own timeout, so this only matters if saving never finishes.
   */
  private static final int OVERALL_TIMEOUT_MILLIS = 90000;

  /** Guards against a double click sending two submissions. */
  private static boolean submitting = false;

  /** Identifies the attempt in flight, so a late callback knows it is stale. */
  private static int attempt = 0;

  /** Releases the guard if neither the save nor the request ever comes back. */
  private static Timer deadline;

  @Override
  public void execute() {
    if (submitting) {
      return;
    }
    submitting = true;
    final int thisAttempt = ++attempt;
    deadline = new Timer() {
      @Override
      public void run() {
        finish(thisAttempt, false);
      }
    };
    deadline.schedule(OVERALL_TIMEOUT_MILLIS);
    ErrorReporter.reportInfo(MESSAGES.submittingToLms());

    final EditorManager editorManager = Ode.getInstance().getEditorManager();
    editorManager.saveDirtyEditors(new Command() {
      @Override
      public void execute() {
        if (!isCurrent(thisAttempt)) {
          return;
        }
        if (editorManager.hasUnsavedChanges()) {
          // The save reported back, but something is still dirty, which is how a
          // failed save shows up. Submitting now would hand in older content than
          // the learner is looking at.
          finish(thisAttempt, false);
          return;
        }
        send(thisAttempt);
      }
    });
  }

  /** Posts the saved project, once every editor is known to be on the server. */
  private static void send(final int thisAttempt) {
    if (!isCurrent(thisAttempt)) {
      return;
    }
    RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, "/lti/submit");
    builder.setTimeoutMillis(REQUEST_TIMEOUT_MILLIS);
    // A custom header the server requires. A cross site page cannot set it, so
    // only a submission started from within App Inventor is accepted.
    builder.setHeader("X-AppInventor-LTI", "1");
    builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    try {
      builder.sendRequest("projectId=" + projectId, new RequestCallback() {
        @Override
        public void onResponseReceived(Request request, Response response) {
          finish(thisAttempt, response.getStatusCode() / 100 == 2);
        }

        @Override
        public void onError(Request request, Throwable exception) {
          finish(thisAttempt, false);
        }
      });
    } catch (RequestException e) {
      finish(thisAttempt, false);
    }
  }

  /** Whether the given attempt is still the one in flight. */
  private static boolean isCurrent(int thisAttempt) {
    return submitting && thisAttempt == attempt;
  }

  /**
   * Ends the submission, releases the guard, and reports once. Every path that can
   * stop the submission comes through here, and a call from an attempt that has
   * already ended does nothing, so the deadline and a late answer cannot both
   * report to the learner.
   */
  private static void finish(int thisAttempt, boolean succeeded) {
    if (!isCurrent(thisAttempt)) {
      return;
    }
    submitting = false;
    if (deadline != null) {
      deadline.cancel();
      deadline = null;
    }
    if (succeeded) {
      ErrorReporter.reportInfo(MESSAGES.submitToLmsSuccess());
    } else {
      ErrorReporter.reportError(MESSAGES.submitToLmsFailed());
    }
  }
}
