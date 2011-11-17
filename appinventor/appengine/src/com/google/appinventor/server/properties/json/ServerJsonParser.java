// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.server.properties.json;

import com.google.appinventor.shared.properties.json.JSONParser;
import com.google.appinventor.shared.properties.json.JSONValue;

import org.json.JSONException;

/**
 * Wrapper for the json.org JSON library's JSON parser.
 *
 */
public class ServerJsonParser implements JSONParser {

  @Override
  public JSONValue parse(String source) {
    if (source.isEmpty()) {
      return null;
    }
    try {
      switch (source.charAt(0)) {
        default:
          throw new IllegalArgumentException();

        case '{':
          return new ServerJsonObject(new org.json.JSONObject(source));

        case '[':
          return new ServerJsonArray(new org.json.JSONArray(source));
      }
    } catch (JSONException e) {
      // Should never happen
      throw new AssertionError(e);
    }
  }
}
