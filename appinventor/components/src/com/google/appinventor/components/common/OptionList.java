// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

/**
 * A marker interface for defining option list helper blocks via the Java scripting language.
 * This can be expanded later if we want to get more information out of option list definitions.
 */
public interface OptionList<T> {
  /**
   * Returns the underlying value of the option.
   */
  T toUnderlyingValue();
}
