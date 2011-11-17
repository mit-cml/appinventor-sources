// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.shared.properties.json;

/**
 * Representation of a JSON parser.
 *
 */
public interface JSONParser {
  /**
   * Parses the given JSON encoded string into a JSON data structure.
   *
   * @param properties  JSON encoded string
   * @return JSON data structure or {@code null} if the properties are empty
   */
  JSONValue parse(String properties);
}
