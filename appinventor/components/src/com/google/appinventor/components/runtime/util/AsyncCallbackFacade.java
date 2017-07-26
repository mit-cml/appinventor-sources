// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

/**
 * AsyncCallbackFacade is an abstract class to provide an {@link #onSuccess(Object)} method to
 * transform the result of an asynchronous operation while passing through any error or failure
 * states to the original callback.
 *
 * @author ewpatton
 *
 * @param <S> Source (original) type expected by the callback
 * @param <T> Target type that the source result will be transformed into
 */
public abstract class AsyncCallbackFacade<S, T> implements AsyncCallbackPair<S> {
  protected final AsyncCallbackPair<T> callback;

  public AsyncCallbackFacade(AsyncCallbackPair<T> target) {
    this.callback = target;
  }

  @Override
  public void onFailure(String message) {
    this.callback.onFailure(message);
  }
}
