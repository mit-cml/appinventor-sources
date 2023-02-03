// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2016-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

/**
 * This class is used to synchronize calls between the UI thread and an asynchronous operation,
 * and await the result (blocking the UI Thread :-().
 */
public class Synchronizer<T> {
  private volatile boolean finished = false;
  private T result;
  private String errorMessage;
  private Throwable error;

  /**
   * Wait for a result to be reported to the Synchronizer.
   */
  public synchronized void waitfor() {
    while (!finished) {
      try {
        wait();
      } catch (InterruptedException e) {
        // Will attempt to try again...
      }
    }
  }

  /**
   * Wake the thread(s) waiting for a result from the Synchronizer.
   *
   * @param result the result of the computation
   */
  public synchronized void wakeup(T result) {
    finished = true;
    this.result = result;
    notifyAll();
  }

  /**
   * Report an error to any thread(s) waiting on the Synchronizer.
   *
   * @param error an error message to report to the original thread
   */
  public synchronized void error(String error) {
    finished = true;
    this.errorMessage = error;
    notifyAll();
  }

  /**
   * Report a Throwable (e.g., an exception) to any thread(s) waiting on the Synchronizer.
   *
   * @param error a throwable caught during processing
   */
  public synchronized void caught(Throwable error) {
    finished = true;
    this.error = error;
    notifyAll();
  }

  public T getResult() {
    return result;
  }

  public String getError() {
    return errorMessage;
  }

  public Throwable getThrowable() {
    return error;
  }

  @Override
  public String toString() {
    return "Synchronizer(" + result + ", " + error + ", " + errorMessage + ")";
  }
}
