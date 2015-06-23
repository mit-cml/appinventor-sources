// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Methods for analyzing the contents of a Young Android Form file.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class FormPropertiesAnalyzer {

  private static final String FORM_PROPERTIES_PREFIX = "#|\n";
  private static final String FORM_PROPERTIES_SUFFIX = "\n|#";
  
  // Logging support
  private static final Logger LOG = Logger.getLogger(FormPropertiesAnalyzer.class.getName());

  private FormPropertiesAnalyzer() {
  }

  /**
   * Parses a complete source file and return the properties as a JSONObject.
   *
   * @param source a complete source file
   * @return the properties as a JSONObject
   */
  public static JSONObject parseSourceFile(String source) {
    // First, locate the beginning of the $JSON section.
    // Older files have a $Properties before the $JSON section and we need to make sure we skip
    // that.
    String jsonSectionPrefix = FORM_PROPERTIES_PREFIX + "$JSON\n";
    int beginningOfJsonSection = source.lastIndexOf(jsonSectionPrefix);
    if (beginningOfJsonSection == -1) {
      throw new IllegalArgumentException(
          "Unable to parse file - cannot locate beginning of $JSON section");
    }
    beginningOfJsonSection += jsonSectionPrefix.length();

    // Then, locate the end of the $JSON section;
    String jsonSectionSuffix = FORM_PROPERTIES_SUFFIX;
    int endOfJsonSection = source.lastIndexOf(jsonSectionSuffix);
    if (endOfJsonSection == -1) {
      throw new IllegalArgumentException(
          "Unable to parse file - cannot locate end of $JSON section");
    }

    String jsonPropertiesString = source.substring(beginningOfJsonSection,
        endOfJsonSection);
    try {
      return new JSONObject(jsonPropertiesString);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse file - invalid $JSON section syntax");
    }
  }

  /**
   * Returns the Set of component types used in the given form file source.
   */
  public static Set<String> getComponentTypesFromFormFile(String source) {
    Set<String> componentTypes = new HashSet<String>();
    JSONObject propertiesObject = parseSourceFile(source);
    try {
      collectComponentTypes(propertiesObject.getJSONObject("Properties"), componentTypes);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse file - invalid $JSON section syntax");
    }
    return componentTypes;
  }

  private static void collectComponentTypes(JSONObject componentProperties,
      Set<String> componentTypes) throws JSONException {
    String componentType = componentProperties.getString("$Type");
    componentTypes.add(componentType);

    // Recursive call to collect nested components.
    if (componentProperties.has("$Components")) {
      JSONArray components = componentProperties.getJSONArray("$Components");
      for (int i = 0; i < components.length(); i++) {
        collectComponentTypes(components.getJSONObject(i), componentTypes);
      }
    }
  }
}
