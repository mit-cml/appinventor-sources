// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.properties;

import com.google.appinventor.shared.properties.json.JSONUtil;

/**
 * Name/value pair.
 *
 * <p>Note that all property values are kept in string format regardless of
 * their actual type.
 *
 */
public class Property {
  // Name of the property
  private final String name;

  // Default value of the property
  private final String defaultValue;

  // Current value of the property (initially the same as its default value)
  private String value;

  /**
   * Creates a new property.
   *
   * @param name  property's name
   * @param defaultValue  property's default value (will also be its initial
   *                      current value)
   */
  public Property(String name, String defaultValue) {
    this.name = name;
    value = defaultValue;
    this.defaultValue = defaultValue;
  }

  /**
   * Returns the property's name.
   *
   * @return  property's name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the property's current value.
   *
   * @return property's value
   */
  public String getValue() {
    return value;
  }

  /**
   * Changes the property's value.
   *
   * @param value  new property value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Returns the property's default value.
   *
   * @return property's default value
   */
  public final String getDefaultValue() {
    return defaultValue;
  }

  /**
   * Indicates whether a property is persisted to the source file upon saving.
   *
   * @return  {@code true} for persisted properties, {@code false} otherwise
   */
  protected boolean isPersisted() {
    return true;
  }

  protected boolean isYail() {
    return true;
  }

  /**
   * Resets the value the property to its default value.
   */
  public final void resetToDefault() {
    if (!getDefaultValue().equals(getValue())) {
      setValue(getDefaultValue());
    }
  }

  /**
   * Encodes the property using JSON format.
   *
   * @param sb  buffer to add encoded property to
   */
  public final void encode(StringBuilder sb) {
    sb.append('"');
    sb.append(getName());
    sb.append("\":");
    sb.append(JSONUtil.toJson(getValue()));
  }
}
