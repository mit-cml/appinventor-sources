// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.shared.properties.json;

import java.util.List;

/**
 * Representation of a JSON array.
 *
 */
public interface JSONArray extends JSONValue {
  /**
   * Returns a list containing the array elements.
   *
   * @return  array elements
   */
  List<JSONValue> getElements();

  /**
   * Returns the size of the array
   *
   * @return  the size
   */
  int size();

  /**
   * Returns the array element at the given index.
   *
   * @return  an array element
   */
  JSONValue get(int index);
}
