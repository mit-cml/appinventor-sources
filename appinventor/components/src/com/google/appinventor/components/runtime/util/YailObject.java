// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import androidx.annotation.NonNull;

import java.util.Iterator;

/**
 * YailObject is a marker interface for data types defined by YAIL. Currently,
 * that is YailList and YailDictionary. Any new non-primitive data types should
 * also implement YailObject.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public interface YailObject<T> extends Iterable<T> {

  /**
   * Get the object at the given index. The index is 0-based.
   *
   * @param index The index of the object to retrieve.
   * @return An object dependent on the implementation. The returned object
   *     should be coercible to another YAIL type.
   * @throws IndexOutOfBoundsException if the index is less than zero or greater
   *     than or equal to the {@link #size()} of the object.
   */
  Object getObject(int index);

  /**
   * Get the size of the object. The exact semantics depend on the object, but
   * typically this should be the number of elements in the object. For lists,
   * it is the length of the list and for dictionaries it is the size of the
   * dictionary (number of pairs). Ideally this method should be constant time.
   *
   * @return The size of the object.
   */
  int size();

  /**
   * Test whether the object is empty.
   *
   * @return True if the object is empty, otherwise false.
   */
  boolean isEmpty();

  /**
   * Returns an iterator over the elements in the YailObject. For YailList this
   * may be any other YAIL type and for YailDictionary this is always YailList.
   */
  @NonNull
  Iterator<T> iterator();
}
