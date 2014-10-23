// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
