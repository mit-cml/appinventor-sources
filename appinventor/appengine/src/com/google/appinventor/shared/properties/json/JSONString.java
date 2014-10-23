// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.properties.json;

/**
 * Representation of a JSON string value.
 *
 */
public interface JSONString extends JSONValue {
  /**
   * Returns the string value.
   *
   * @return  string value
   */
  String getString();
}
