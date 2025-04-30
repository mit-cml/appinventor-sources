// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.InvalidSessionException;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;

import com.google.gwt.http.client.Response;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.StatusCodeException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

  private static final Logger LOG = Logger.getLogger(OdeAsyncCallback.class.getName());

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
    if (caught instanceof BlocksTruncatedException) {
      LOG.info("Caught BlocksTruncatedException");
      ErrorReporter.reportError("Caught BlocksTruncatedException");
      return;
    }
    // SC_PRECONDITION_FAILED if our session has expired or login cookie
    // has become invalid
    if ((caught instanceof StatusCodeException) &&
      ((StatusCodeException)caught).getStatusCode() == Response.SC_PRECONDITION_FAILED) {
      Ode.getInstance().sessionDead();
      return;
    }
    String errorMessage =
        (failureMessage == null) ? caught.getMessage() : failureMessage;
    ErrorReporter.reportError(errorMessage);
    LOG.log(Level.SEVERE, "Got exception", caught);
  }
}
