// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.settings;

import com.google.appinventor.client.ErrorReporter;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Superclass for collections of settings.
 *
 */
public abstract class CommonSettings {
  // Mapping from category (class) to actual settings singleton instance
  private final Map<String, Settings> settingsMap;

  /**
   * Creates new settings object.
   */
  public CommonSettings() {
    settingsMap = new HashMap<String, Settings>();
  }

  /**
   * Returns an array of the settings for the associated project
   *
   * @return  array of actual settings
   */
  public final Settings[] getSettings() {
    Collection<Settings> values = settingsMap.values();
    return values.toArray(new Settings[values.size()]);
  }

  /**
   * Returns the settings for the given category.
   *
   * @param category settings category
   * @return  settings
   */
  public final Settings getSettings(String category) {
    return settingsMap.get(category);
  }

  /**
   * Creates a new settings instance from a settings category name.
   *
   * @param category  settings category name
   * @param settings  settings associated with the category
   */
  protected void addSettings(String category, Settings settings) {
    settingsMap.put(category, settings);
  }

  /**
   * Decodes the settings from the given string.
   *
   * @param encodedSettings  JSON encoded settings (may be empty)
   */
  protected final void decodeSettings(String encodedSettings) {
    if (!encodedSettings.isEmpty()) {
      JSONObject settingsObject = new ClientJsonParser().parse(encodedSettings).asObject();
      Map<String, JSONValue> properties = settingsObject.getProperties();
      for (String category : properties.keySet()) {
        Settings settings = settingsMap.get(category);
        if (settings == null) {
          OdeLog.wlog("Unknown settings category: " + category);
        } else {
          settings.changeProperties(properties.get(category).asObject());
          settings.updateAfterDecoding();
        }
      }
    } else {
      // New User, no settings, use defaults
      for (String category : settingsMap.keySet()) {
        Settings settings = settingsMap.get(category);
        settings.updateAfterDecoding();
      }
    }
  }

  /**
   * Encodes the associated settings into a JSON encoded string.
   *
   * @return  JSON encoded settings string
   */
  public final String encodeSettings() {
    StringBuilder sb = new StringBuilder();

    sb.append('{');
    String separator = "";
    for (Settings settings : settingsMap.values()) {
      sb.append(separator);
      sb.append(settings.encodeAllAsPairs());
      separator = ",";
    }
    sb.append('}');
    return sb.toString();
  }

  /**
   * Reports an error on loading settings.
   */
  protected static void reportLoadError() {
    ErrorReporter.reportError(MESSAGES.settingsLoadError());
  }

  /**
   * Reports an error on saving settings.
   */
  protected static void reportSaveError() {
    ErrorReporter.reportError(MESSAGES.settingsSaveError());
  }
}
