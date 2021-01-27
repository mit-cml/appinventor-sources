// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.buildserver;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * An {@link Executor} used for executing tasks using a thread pool.
 *
 * <p>This ExecutorService allows only a certain number of simultaneous tasks.
 * Additional tasks are rejected, not queued.</p>
 *
 * @author lizlooney@google.com (Liz Looney)
 */
final class NonQueuingExecutor implements Executor {
  // The maximum number of active tasks. O means unlimited.
  private final int maxActiveTasks;

  private final AtomicInteger activeTaskCount = new AtomicInteger(0);
  private final AtomicInteger completedTaskCount = new AtomicInteger(0);

  // Logging support
  private static final Logger LOG = Logger.getLogger(NonQueuingExecutor.class.getName());
  
  // lockExecute is used so that the execute method can be executed only one thread at a time.
  private final Object lockExecute = new Object();

  /**
   * Creates a NonQueuingExecutor.
   *
   * @param maxActiveTasks the maximum number of active tasks
   */
  NonQueuingExecutor(int maxActiveTasks) {
    this.maxActiveTasks = maxActiveTasks;
  }

  @Override
  public void execute(final Runnable runnable) {
    synchronized (lockExecute) {
      // Check whether the executor is below maximum capacity.
      if (maxActiveTasks == 0 || activeTaskCount.get() < maxActiveTasks) {
        // Create a new thread for the task.
        Thread thread = new Thread(new Runnable() {
          @Override
          public void run() {
            runnable.run();
            activeTaskCount.decrementAndGet();
            completedTaskCount.incrementAndGet();
          }
        });
        activeTaskCount.incrementAndGet();
        thread.start();

      } else {
        // If the executor is at maximum capacity, reject the task.
        throw new RejectedExecutionException();
      }
    }
  }

  public int getMaxActiveTasks() {
    return maxActiveTasks;
  }

  public int getActiveTaskCount() {
    return activeTaskCount.get();
  }

  public int getCompletedTaskCount() {
    return completedTaskCount.get();
  }
}
