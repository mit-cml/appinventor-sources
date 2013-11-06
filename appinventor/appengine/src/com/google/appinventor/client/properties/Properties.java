// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.properties;

import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONValue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Collection of properties.
 *
 */
public class Properties<T extends Property> implements Iterable<T> {

  // Maps property names to properties
  private final Map<String, T> propertiesMap;

  /**
   * Creates a new properties instance.
   */
  public Properties() {
    propertiesMap = new HashMap<String, T>();
  }

  /**
   * Changes the properties from an encoded string of properties.
   *
   * @param propertiesObject  JSON encoded properties
   */
  public void changeProperties(JSONObject propertiesObject) {
    Map<String, JSONValue> properties = propertiesObject.getProperties();
    for (String name : properties.keySet()) {
      changePropertyValue(name, properties.get(name).asString().getString());
    }
  }

  /**
   * Encodes properties whose value is different from their default value or
   * as a JSON string.
   *
   * @return  encoded properties
   */
  public final String encodeAsJsonString() {
    return '{' + encodeAsPairs() + '}';
  }

  /**
   * Encodes all properties as a JSON string.
   *
   * @return  encoded properties
   */
  public final String encodeAllAsJsonString() {
    return '{' + encodeAllAsPairs() + '}';
  }

  /**
   * Encodes properties whose value is different from their default value
   * as JSON pairs.
   *
   * @return  encoded properties
   */
  public String encodeAsPairs() {
    return encode(false);
  }

  /**
   * Encodes all properties as JSON pairs.
   *
   * @return  encoded properties
   */
  public String encodeAllAsPairs() {
    return encode(true);
  }

  /**
   * Encodes properties as JSON pairs.
   *
   * @param all  indicates whether all persistable properties should be encoded
   *             or only those that have value different from their default
   *             value
   * @return  encoded property pairs
   */
  protected String encode(boolean all) {
    StringBuilder sb = new StringBuilder();

    sb.append(getPrefix());

    String separator = "";
    for (Property property : this) {
      // Don't encode non-persistable properties or properties being assigned their default value
      // unless encoding for all properties was explicitly requested
      if (property.isPersisted() &&
          (all || !property.getDefaultValue().equals(property.getValue()))) {
        sb.append(separator);
        separator = ",";
        property.encode(sb);
      }
    }

    sb.append(getSuffix());

    return sb.toString();
  }

  /**
   * Optional prefix to add to encoded properties.
   *
   * @return  prefix
   */
  protected String getPrefix() {
    return "";
  }

  /**
   * Optional suffix to add to encoded properties.
   *
   * @return  suffix
   */
  protected String getSuffix() {
    return "";
  }

  /**
   * Adds a new property to the collection.
   * A property of the same name must not already exist.
   *
   * @param property  property to be added
   * @throws IllegalStateException  if a same-named property already exists
   */
  protected void addProperty(T property) {
    T oldProperty = propertiesMap.put(property.getName(), property);
    if (oldProperty != null) {
      propertiesMap.put(property.getName(), oldProperty);  // restore state
      throw new IllegalStateException("property already exists: " + property.getName());
    }
  }

  /**
   * Deletes all properties.
   */
  public final void deleteAllProperties() {
    propertiesMap.clear();
  }

  /**
   * Changes the value of an existing property.
   *
   * @param name  property name
   * @param value  new property value
   * @return true if the property exists and was changed, false otherwise
   */
  public final boolean changePropertyValue(String name, String value) {
    try {
      getExistingProperty(name).setValue(value);
      return true;
    } catch (IllegalStateException e) {
      OdeLog.wlog(e.toString());
      return false;
    }
  }

  /**
   * Returns the value of an existing property.
   *
   * @param name  property name
   * @return  value of the property
   * @throws IllegalStateException  if no such property exists
   */
  public final String getPropertyValue(String name) {
    return getExistingProperty(name).getValue();
  }

  /**
   * Returns an Iterator over the properties.
   *
   * @return Property iterator
   */
  @Override
  public final Iterator<T> iterator() {
    return propertiesMap.values().iterator();
  }

  /**
   * Returns the property for the given name,
   * or {@code null} if no such property has been defined.
   *
   * @param name  property name
   */
  public final T getProperty(String name) {
    return propertiesMap.get(name);
  }

  /**
   * Returns the existing property for the given name.
   *
   * @throws IllegalStateException  if no such property exists
   */
  public T getExistingProperty(String name) {
    T property = getProperty(name);
    if (property == null) {
      throw new IllegalStateException("no such property: " + name);
    }
    return property;
  }

  /**
   * Resets the values of all properties to their default values.
   */
  public final void resetProperties() {
    for (Property property : this) {
      property.resetToDefault();
    }
  }
}
