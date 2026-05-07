package com.google.appinventor.components.runtime.util;

/**
 * The {@code Continuation} interface models a continuation that takes 0 or 1 values from a
 * previous code segment.
 *
 * @param <T>
 * @author Evan W. Patton (ewpatton@mit.edu)
 */
public interface Continuation<T> {
  void call(T value);
}
