// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
