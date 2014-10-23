// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.properties.json;

/**
 * Representation of a JSON boolean value.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public interface JSONBoolean extends JSONValue {
  /**
   * Returns the boolean value.
   *
   * @return  boolean value
   */
  boolean getBoolean();
}
