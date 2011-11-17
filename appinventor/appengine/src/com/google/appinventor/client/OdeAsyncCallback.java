// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.appinventor.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

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
    String errorMessage =
        (failureMessage == null) ? caught.getMessage() : failureMessage;
    ErrorReporter.reportError(errorMessage);
  }
}
