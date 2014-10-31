// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.buildserver;

import java.util.concurrent.RejectedExecutionException;

import junit.framework.TestCase;

/**
 * Tests NonQueuingExecutor class.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class NonQueuingExecutorTest extends TestCase {
  public void testAdditionalTaskIsRejected() throws Exception {
    // Create the NonQueuingExecutor with capacity 10.
    int maxCapacity = 10;
    NonQueuingExecutor executor = new NonQueuingExecutor(maxCapacity);

    // Execute the maximum number of tasks, which will all wait until I notify them via the signal.
    final Object signal = new Object();
    for (int i = 0; i < maxCapacity; i++) {
      executor.execute(new TaskThatWaitsForSignal(signal));
    }

    // Now the executor should be at maximum capacity.
    assertEquals(maxCapacity, executor.getActiveTaskCount());

    // Try to execute another task. We expect it to be rejected.
    try {
      executor.execute(new TaskThatDoesNothing());
      fail();
    } catch (RejectedExecutionException e) {
      // expected
    }

    // Notify the signal so the active tasks can complete.
    synchronized (signal) {
      signal.notifyAll();
    }
  }

  private static class TaskThatWaitsForSignal implements Runnable {
    private final Object signal;
    private TaskThatWaitsForSignal(Object signal) {
      this.signal = signal;
    }

    @Override
    public void run() {
      synchronized (signal) {
        try {
          // Wait for the signal.
          signal.wait();
        } catch (InterruptedException e) {
          // ignored
        }
      }
    }
  }

  private static class TaskThatDoesNothing implements Runnable {
    @Override
    public void run() {
    }
  }
}
