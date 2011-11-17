// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.properties.json;

import com.google.appinventor.shared.properties.json.JSONParser;
import com.google.appinventor.shared.properties.json.JSONValue;

/**
 * Wrapper for the GWT JSON library JSON parser.
 *
 */
public class ClientJsonParser implements JSONParser {

  @Override
  public JSONValue parse(String source) {
    return source.isEmpty() ? null
        : ClientJsonValue.convert(com.google.gwt.json.client.JSONParser.parse(source));
  }
}
