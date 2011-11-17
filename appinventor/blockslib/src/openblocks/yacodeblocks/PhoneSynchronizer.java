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
import java.util.concurrent.TimeUnit;

/**
 * Synchronizes communication with the phone.  Tasks can be executed
 * immediately or be waited on.  The entire queue can also be cancelled at
 * any point.
 *
 * @author kerr@google.com (Debby Wallach)
 */
public class PhoneSynchronizer {
  private static final boolean DEBUG = true;

  /* This class uses the executorService to order tasks being sent to the
   * phone.  Because we restart a new executorService every time the set of
   * outstanding tasks gets cancelled, we have to synchronize around
   * executorService to make sure that tasks get submitted to the right
   * instantiation of it.
   */
  private ExecutorService executorService = Executors.newSingleThreadExecutor();

  /**
   * Communicate with the phone synchronously.  Queues up a callable
   * representing the communication and waits until it is executed.  If the
   * execution fails for any of a variety of reasons, signals
   * PhoneCommunicationException, otherwise returns the result of the
   * callable.
   * @param aCallable the callable to execute
   * @return the result of executing aCallable
   */
  public <T> T sendNow(Callable<? extends T> aCallable) throws PhoneCommunicationException {
    Future<? extends T> future;
    synchronized(this) {
      try {
        future = executorService.submit(aCallable);
      } catch (RejectedExecutionException e) {
        throw new PhoneCommunicationException("Problem sending to phone: " + e.getMessage());
      }
    }
    try {
      return future.get();
    } catch (CancellationException e) {
      throw new PhoneCommunicationException("Problem sending to phone: " + e.getMessage());
    } catch (ExecutionException e) {
      throw new PhoneCommunicationException("Problem sending to phone: " + e.getMessage());
    } catch (InterruptedException e) {
      throw new PhoneCommunicationException("Problem sending to phone: " + e.getMessage());
    }
  }

  /**
   * Communicate with the phone synchronously.  Queues up a runnable
   * representing the communication and waits until it is executed.  If the
   * execution fails for any of a variety of reasons, signals
   * PhoneCommunicationException, otherwise returns when the runnable is
   * finished.
   * @param aRunnable the runnable to execute
   */
  public void sendNow(Runnable aRunnable) throws PhoneCommunicationException {
    Future<?> future;
    synchronized(this) {
      try {
        future = executorService.submit(aRunnable);
      } catch (RejectedExecutionException e) {
        throw new PhoneCommunicationException("Problem sending to phone: " + e.getMessage());
      }
    }
    try {
      future.get();
    } catch (CancellationException e) {
      throw new PhoneCommunicationException("Problem sending to phone: " + e.getMessage());
    } catch (ExecutionException e) {
      throw new PhoneCommunicationException("Problem sending to phone: " + e.getMessage());
    } catch (InterruptedException e) {
      throw new PhoneCommunicationException("Problem sending to phone: " + e.getMessage());
    }
  }

  /**
   * Communicate with the phone asynchronously.  Queues up a runnable
   * representing the communication and returns immediately.  If the
   * queueing process fails, signals PhoneCommunicationException.
   * @param aRunnable the runnable to queue up
   */
  public void sendEventually(Runnable aRunnable) throws PhoneCommunicationException {
    synchronized(this) {
      try {
        executorService.submit(aRunnable);
      } catch (RejectedExecutionException e) {
        throw new PhoneCommunicationException("Problem sending to phone: " + e.getMessage());
      }
    }
  }

  /**
   * Makes a best-effort attempt to cancel the current task and all
   * outstanding ones.  If we can't cancel any of the tasks, we don't
   * cancel them.
   */
  public void cancelAllAndWait() throws PhoneCommunicationException {
    synchronized(this) {
      if (! executorService.isShutdown()) {
        try {
          executorService.shutdownNow();
        } catch (SecurityException e) {
          // do nothing; it's only best effort.
          if (DEBUG) {
            System.out.println("Unexpected security exception while cancelling tasks.  Ignoring.");
          }
        }
      }
      try {
        if (DEBUG) {
          System.out.println("Waiting for termination after a cancellation...");
        }
        boolean cleanTermination = executorService.awaitTermination(15, TimeUnit.SECONDS);
        if (! cleanTermination) {
          throw new PhoneCommunicationException("Not all outstanding tasks cancelled (timeout)");
        }
      } catch (InterruptedException e) {
        throw new PhoneCommunicationException("Not all outstanding tasks cancelled: " +
            e.getMessage());
      } finally {
        if (DEBUG) {
          System.out.println("Starting a new executor service after a cancellation.");
        }
        executorService = Executors.newSingleThreadExecutor();
      }
    }
  }

}
