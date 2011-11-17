// Copyright 2010 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

/**
 * Synchronizes saves to the server.  Tasks can be executed immediately or
 * be waited on.
 *
 * @author kerr@google.com (Debby Wallach)
 */
public class SaverSynchronizer {
  private static final boolean DEBUG = false;

  private ExecutorService executorService = Executors.newSingleThreadExecutor();

  /**
   * Save the code to the server synchronously.  Queues up a runnable
   * representing the save and waits until it is executed.  If the
   * execution fails for any of a variety of reasons, signals
   * SaveException, otherwise returns when the runnable is finished.
   * @param aRunnable the runnable to execute
   */
  public void saveNow(Runnable aRunnable) throws SaveException {
    Future<?> future;
    synchronized(this) {
      try {
        future = executorService.submit(aRunnable);
      } catch (RejectedExecutionException e) {
        throw new SaveException("Problem queueing save on the server: ", e);
      }
    }
    try {
      future.get();
    } catch (CancellationException e) {
      throw new SaveException("Problem queueing save on the server: ", e);
    } catch (ExecutionException e) {
      throw new SaveException("Problem queueing save on the server: ", e);
    } catch (InterruptedException e) {
      throw new SaveException("Problem queueing save on the server: ", e);
    }
  }

  /**
   * Save the code to the server asynchronously.  Queues up a runnable
   * representing the save and returns immediately.  If the queueing
   * process fails, signals SaveException.
   * @param aRunnable the runnable to queue up
   */
  public void saveEventually(Runnable aRunnable) throws SaveException {
    synchronized(this) {
      try {
        executorService.submit(aRunnable);
      } catch (RejectedExecutionException e) {
        throw new SaveException("Problem queueing save on the server: ", e);
      }
    }
  }
}
