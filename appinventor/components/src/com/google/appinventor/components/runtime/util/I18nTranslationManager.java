// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.util.Log;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Iterator;
import java.util.Locale;
import java.util.WeakHashMap;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Loads and applies App Inventor i18n translation data bundled with compiled apps.
 */
public final class I18nTranslationManager {
  private static final String LOG_TAG = "I18nTranslationManager";
  private static final String TRANSLATIONS_ASSET = "i18n/translations.json";
  private static final Map<Form, JSONObject> TRANSLATIONS_BY_FORM =
    new WeakHashMap<Form, JSONObject>();

  private I18nTranslationManager() {
  }

  public static void load(Form form) {
    InputStream inputStream = null;
    try {
      inputStream = form.openAsset(TRANSLATIONS_ASSET);
      String json = readFully(inputStream);
      JSONObject root = new JSONObject(json);
      TRANSLATIONS_BY_FORM.put(form, root);

      String language = selectLanguage(root);
      JSONObject entries = root.optJSONObject("entries");
      int appliedCount = applyTranslations(form, entries, language);

      int entryCount = entries == null ? 0 : entries.length();
      Log.d(LOG_TAG, "Loaded i18n translations for locale "
          + getDeviceLanguageCode() + " using language " + language
          + " with " + entryCount + " entries and " + appliedCount + " applied values.");
    } catch (IOException e) {
      Log.d(LOG_TAG, "No i18n translations asset found.");
    } catch (JSONException e) {
      Log.w(LOG_TAG, "Invalid i18n translations JSON.", e);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          Log.w(LOG_TAG, "Unable to close i18n translations asset.", e);
        }
      }
    }
  }

  public static String lookupDynamic(Form form, String key, Map<String, String> values) {
    if (form == null || key == null || key.length() == 0) {
      return "";
    }

    JSONObject root = TRANSLATIONS_BY_FORM.get(form);
    if (root == null) {
      return "";
    }

    String language = selectLanguage(root);
    JSONObject entries = root.optJSONObject("entries");
    if (entries == null) {
      return "";
    }

    JSONObject entry = entries.optJSONObject(key);
    if (entry == null) {
      return "";
    }

    JSONObject translations = entry.optJSONObject("translations");
    String template = null;

    if (translations != null) {
      template = translations.optString(language, "");
      if (template.length() == 0) {
        template = translations.optString(getBaseLanguageCode(), "");
      }
    }

    if (template == null || template.length() == 0) {
      template = entry.optString("baseText", "");
    }

    if (template.length() == 0) {
      JSONObject source = entry.optJSONObject("source");
      if (source != null) {
        template = source.optString("baseText", "");
      }
    }

    return I18nFormatter.format(template, values);
  }

  static void putTranslationsForTesting(Form form, JSONObject root) {
    TRANSLATIONS_BY_FORM.put(form, root);
  }

  private static int applyTranslations(Form form, JSONObject entries, String language)
      throws JSONException {
    if (entries == null || language == null || language.length() == 0) {
      return 0;
    }

    int appliedCount = 0;
    Iterator<String> keys = entries.keys();

    while (keys.hasNext()) {
      String key = keys.next();
      JSONObject entry = entries.optJSONObject(key);
      if (entry == null) {
        continue;
      }

      JSONObject source = entry.optJSONObject("source");
      JSONObject translations = entry.optJSONObject("translations");
      if (source == null || translations == null) {
        continue;
      }

      String screenName = source.optString("screen", "");
      if (screenName.length() > 0 && !screenName.equals(form.getFormName())) {
        continue;
      }

      String componentName = source.optString("component", "");
      String propertyName = source.optString("property", "");
      String translatedValue = translations.optString(language, "");

      if (translatedValue.length() == 0) {
        continue;
      }

      Component component = form.lookupComponent(componentName);
      if (component == null) {
        Log.d(LOG_TAG, "No component found for i18n entry " + key
            + " component " + componentName);
        continue;
      }

      if (applyStringProperty(component, propertyName, translatedValue)) {
        appliedCount++;
      }
    }

    return appliedCount;
  }

  private static boolean applyStringProperty(Component component, String propertyName,
      String translatedValue) {
    if (propertyName == null || propertyName.length() == 0) {
      return false;
    }

    try {
      Method setter = component.getClass().getMethod(propertyName, String.class);
      setter.invoke(component, translatedValue);
      return true;
    } catch (Exception e) {
      Log.d(LOG_TAG, "Unable to apply translated property " + propertyName
          + " on " + component.getClass().getName(), e);
      return false;
    }
  }

  private static String selectLanguage(JSONObject root) {
    String deviceLanguage = getDeviceLanguageCode();
    String baseLanguage = getBaseLanguageCode();

    JSONObject entries = root.optJSONObject("entries");
    if (entries == null) {
      return deviceLanguage;
    }

    Iterator<String> keys = entries.keys();
    while (keys.hasNext()) {
      JSONObject entry = entries.optJSONObject(keys.next());
      if (entry == null) {
        continue;
      }

      JSONObject translations = entry.optJSONObject("translations");
      if (translations == null) {
        continue;
      }

      if (translations.has(deviceLanguage)) {
        return deviceLanguage;
      }

      if (translations.has(baseLanguage)) {
        return baseLanguage;
      }
    }

    return deviceLanguage;
  }

  private static String getDeviceLanguageCode() {
    Locale locale = Locale.getDefault();
    String language = locale.getLanguage();
    String country = locale.getCountry();

    if (country == null || country.length() == 0) {
      return language;
    }

    return language + "-" + country;
  }

  private static String getBaseLanguageCode() {
    Locale locale = Locale.getDefault();
    return locale.getLanguage();
  }

  private static String readFully(InputStream inputStream) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];

    int count;
    while ((count = inputStream.read(buffer)) != -1) {
      outputStream.write(buffer, 0, count);
    }

    return outputStream.toString("UTF-8");
  }
}