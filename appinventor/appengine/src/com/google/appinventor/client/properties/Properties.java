// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.properties;

import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONValue;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Collection of properties.
 *
 */
public class Properties<T extends Property> implements Iterable<T> {

  // We define our own special comparator here. It turns out that
  // properties are displayed in the properties panel based on the order
  // they are pulled out of the map. When we used a hashmap, the height
  // and width properties appeared together (lucky I guess). When
  // we replaced it with a TreeMap, properties now appear in the panel
  // in sort order (based on their English names). This separated
  // the heigth and width proprites. This comparator arranges for
  // "Width" to be sorted immediately after "Height." So properties
  // are now displayed in sort order, with this notable exception, the
  // width property now appears immediately after the Height property
  // which is what people have grown used to (and seems correct).
  private class Comparer implements Comparator<String> {
    public int compare(String a, String b) {
      if (a.equals("Width"))
        a = "Heightz";
      if (b.equals("Width"))
        b = "Heightz";
      return a.compareTo(b);
    }
  }

  // Maps property names to properties
  private final Map<String, T> propertiesMap;

  /**
   * Creates a new properties instance.
   */
  public Properties() {
    // Note: We used to use a HashMap here. However when we iterate over its
    //       contents we get the keys in an arbitrary order based on the hash
    //       implementation. We have had at least one case where an update of
    //       the GWT library changed the order which triggered unanticipated
    //       bugs. By using a TreeMap we will get the keys in a consistent order
    propertiesMap = new TreeMap<String, T>(new Comparer());
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
    return '{' + encodeAsPairs(false) + '}';
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
  public String encodeAsPairs(boolean forYail) {
    return encode(forYail, false);
  }

  /**
   * Encodes all properties as JSON pairs.
   *
   * @return  encoded properties
   */
  public String encodeAllAsPairs() {
    return encode(true, true);
  }

  /**
   * Encodes properties as JSON pairs.
   *
   * @param all  indicates whether all persistable properties should be encoded
   *             or only those that have value different from their default
   *             value
   * @return  encoded property pairs
   */
  protected String encode(boolean forYail, boolean all) {
    StringBuilder sb = new StringBuilder();

    sb.append(getPrefix());

    String separator = "";
    for (Property property : this) {
      // Don't encode non-persistable properties or properties being assigned their default value
      // unless encoding for all properties was explicitly requested
      if ((property.isPersisted() || (property.isYail() && forYail)) &&
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
