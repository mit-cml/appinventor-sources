package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import gnu.mapping.Procedure;
import java.util.concurrent.Callable;

/**
 * 
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public final class ContinuationUtil {

  private ContinuationUtil() {
    // Utility classes should not be instantiated
  }

  /**
   * Create a facade for {@code procedure} that conforms to the {@link Continuation} API.
   *
   * @param procedure a Kawa procedure
   * @param <T> the type of the value passed to the continuation
   * @return a fresh continuation object that dispatches calls to {@link Continuation#call(Object)}
   *     to the {@code procedure}.
   */
  public static <T> Continuation<T> wrap(final Procedure procedure, final Class<T> clazz) {
    return new Continuation<T>() {
      public void call(T arg) {
        try {
          if (clazz == Void.class) {
            procedure.apply0();
          } else {
            procedure.apply1(arg);
          }
        } catch (Throwable e) {
          throw new YailRuntimeError(e.getMessage(), e.getClass().getSimpleName());
        }
      }
    };
  }

  /**
   * Calls a {@code block} of code on a separate thread and then passes the result to the given
   * {@code continuation}.
   *
   * @param block the code block to evaluate
   * @param continuation the continuation to receive the result of block
   * @param <T> the return type of the block
   */
  public static <T> void callWithContinuation(final Callable<T> block,
      final Continuation<T> continuation) {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        try {
          final T result = block.call();
          continuation.call(result);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  /**
   * Calls a {@code block} of code on another thread while blocking the current thread. The result
   * of the computation is then returned (on success) or a {@link RuntimeException} is thrown if
   * an error was thrown.
   *
   * @param block the code block to evaluate
   * @param <T> the return type of the block
   * @return the result as returned by the block's {@link Callable#call()} method
   * @throws RuntimeException when an exception occurs during evaluation of {@code block}
   */
  public static <T> T callWithContinuationSync(final Callable<T> block) {
    final Synchronizer<T> result = new Synchronizer<>();
    callWithContinuation(new Callable<T>() {
      @Override
      public T call() {
        try {
          return block.call();
        } catch (Throwable t) {
          result.caught(t);
          return null;
        }
      }
    }, new Continuation<T>() {
      @Override
      public void call(T value) {
        if (result.getThrowable() != null) {
          result.wakeup(value);
        }
      }
    });
    Throwable error = result.getThrowable();
    if (error != null) {
      if (error instanceof RuntimeException) {
        throw (RuntimeException) error;
      } else {
        throw new RuntimeException("Exception in call", error);
      }
    }
    return result.getResult();
  }
}
