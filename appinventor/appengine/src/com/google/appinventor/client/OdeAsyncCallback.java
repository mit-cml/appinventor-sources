// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client;

import com.google.appinventor.shared.rpc.InvalidSessionException;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.client.output.OdeLog;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;

/**
 * Provides common functionality for asynchronous callbacks from the ODE
 * client.  Specifically, on failure, this does one of two things:
 * <ol>
 * <li> If a failure message was provided during construction,
 *      it is displayed using {@link ErrorReporter#reportError(String)}.
 * <li> Otherwise, if the no-args constructor was used, the message in
 *      the exception passed to {@link #onFailure(Throwable)} is
 *      displayed using {@link ErrorReporter#reportError(String)}.
 * </ol>
 *
 * @param <T> type of object returned by successful RPC call
 */
public abstract class OdeAsyncCallback<T> implements AsyncCallback<T> {

  private String failureMessage;

  /**
   * Constructor for when caller wants subclass to display the message in
   * the {@link Throwable} passed to {@link #onFailure(Throwable)}, rather
   * than a static message provided at construction time
   */
  public OdeAsyncCallback() {
  }

  /**
   * Constructor allowing subclass to specify a message that should be
   * displayed by {@link #onFailure(Throwable)}
   *
   * @param defaultFailureMessage message to display on failure
   */
  public OdeAsyncCallback(String defaultFailureMessage) {
    failureMessage = defaultFailureMessage;
  }

  @Override
  public void onFailure(Throwable caught) {
    if (caught instanceof IncompatibleRemoteServiceException) {
      ErrorReporter.reportError("App Inventor has just been upgraded, you will need to press the reload button in your browser window");
      return;
    }
    if (caught instanceof InvalidSessionException) {
      Ode.getInstance().invalidSessionDialog();
      return;
    }
    if (caught instanceof ChecksumedFileException) {
      Ode.getInstance().corruptionDialog();
      return;
    }
    String errorMessage =
        (failureMessage == null) ? caught.getMessage() : failureMessage;
    ErrorReporter.reportError(errorMessage);
    OdeLog.elog("Got exception: " + caught.getMessage());
    Throwable cause = caught.getCause();
    if (cause != null) {
      OdeLog.elog("Caused by: " + cause.getMessage());
    }
  }
}
