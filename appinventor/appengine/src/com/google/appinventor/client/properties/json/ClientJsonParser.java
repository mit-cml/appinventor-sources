// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
