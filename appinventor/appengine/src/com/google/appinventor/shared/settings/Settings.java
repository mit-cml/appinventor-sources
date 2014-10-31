// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.settings;

import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONParser;
import com.google.appinventor.shared.properties.json.JSONValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Read-only settings.
 *
 */
public final class Settings {

  // Mapping of settings category name to settings (which map a settings name to a settings value)
  private final Map<String, Map<String, String>> settings;

  /**
   * Creates new read-only settings initialized with encoded settings.
   *
   * @param parser  JSON parser to be used for decoding
   * @param encodedSettings  JSON encoded settings
   */
  public Settings(JSONParser parser, String encodedSettings) {
    settings = new HashMap<String, Map<String,String>>();

    parseCategory(parser.parse(encodedSettings).asObject());
  }

  /**
   * Parses the categories of the JSON encoded settings.
   */
  private void parseCategory(JSONObject categories) {
    for (Entry<String, JSONValue> category : categories.getProperties().entrySet()) {
      parseSettings(category.getKey(), category.getValue().asObject());
    }
  }

  /**
   * Parses the actual settings of the JSON encoded settings.
   */
  private void parseSettings(String categoryName, JSONObject category) {
    Map<String, String> categorySettings = new HashMap<String, String>();
    settings.put(categoryName, categorySettings);

    for (Entry<String, JSONValue> categorySetting : category.getProperties().entrySet()) {
      categorySettings.put(categorySetting.getKey(),
          categorySetting.getValue().asString().getString());
    }
  }

  /**
   * Returns the settings value for a given settings category and settings
   * name.
   *
   * @param category  settings category
   * @param name  settings name
   * @return  settings value or null if either {@code category} or
   *          {@code name} are not found
   * @throws IllegalArgumentException  if either {@code category} or
   *                                   {@code name} are null
   */
  public String getSetting(String category, String name) {
    if (category == null ) {
      throw new IllegalArgumentException("Null settings category " + category);
    }
    if (name == null ) {
      throw new IllegalArgumentException("Null settings name " + name);
    }
    Map<String, String> categorySettings = settings.get(category);
    if (categorySettings != null) {
      return categorySettings.get(name);
    }

    return null;
  }
}
