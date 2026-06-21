// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.util.Log;
import com.google.appinventor.components.runtime.Form;
import com.google.common.annotations.VisibleForTesting;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Loads and applies App Inventor i18n translation data bundled with compiled apps.
 */
public final class I18nTranslationManager {
  private static final String LOG_TAG = "I18nTranslationManager";
  private static final String TRANSLATIONS_ASSET = "i18n/translations.json";

  private JSONObject translationsRoot;
  private String previewLanguageOverride = "";

  public void load(TranslationProvider provider) {
    if (provider == null) {
      return;
    }

    InputStream inputStream = null;
    try {
      inputStream = provider.openAsset(TRANSLATIONS_ASSET);
      String json = readFully(inputStream);
      translationsRoot = new JSONObject(json);

      String language = selectLanguage();
      JSONObject entries = translationsRoot.optJSONObject("entries");
      int appliedCount = applyTranslations(provider, entries, language);

      int entryCount = entries == null ? 0 : entries.length();
      Log.d(LOG_TAG, "Loaded i18n translations for locale "
          + getDeviceLanguageCode() + " using language " + language
          + " with " + entryCount + " entries and " + appliedCount + " applied values.");
    } catch (IOException e) {
      Log.d(LOG_TAG, "No i18n translations asset found.");
    } catch (JSONException e) {
      Log.w(LOG_TAG, "Invalid i18n translations JSON.", e);
    } finally {
      IOUtils.closeQuietly(LOG_TAG, inputStream);
    }
  }

  public void loadFromJson(String json, TranslationProvider provider) {
    if (json == null || json.trim().length() == 0) {
      clear();
      return;
    }

    try {
      translationsRoot = new JSONObject(json);
      applyLoadedTranslations(provider);
    } catch (JSONException e) {
      Log.w(LOG_TAG, "Invalid i18n translations JSON.", e);
    }
  }

  public void applyLoadedTranslations(TranslationProvider provider) {
    if (provider == null || translationsRoot == null) {
      return;
    }

    try {
      String language = selectLanguage();
      JSONObject entries = translationsRoot.optJSONObject("entries");
      int appliedCount = applyTranslations(provider, entries, language);

      int entryCount = entries == null ? 0 : entries.length();
      Log.d(LOG_TAG, "Applied loaded i18n translations using language " + language
          + " with " + entryCount + " entries and " + appliedCount + " applied values.");
    } catch (JSONException e) {
      Log.w(LOG_TAG, "Unable to apply loaded i18n translations.", e);
    }
  }

  public String lookupDynamic(String key, Map<String, String> values) {
    if (key == null || key.length() == 0 || translationsRoot == null) {
      return "";
    }

    String language = selectLanguage();
    JSONObject entries = translationsRoot.optJSONObject("entries");
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

  public void setPreviewLanguageOverride(String language) {
    previewLanguageOverride = language == null ? "" : language.trim();
  }

  public void clearPreviewLanguageOverride() {
    previewLanguageOverride = "";
  }

  public void clear() {
    translationsRoot = null;
    previewLanguageOverride = "";
  }

  @VisibleForTesting
  void putTranslationsForTesting(JSONObject root) {
    translationsRoot = root;
  }

  public static void setPreviewLanguageForCompanion(
      String language, String translationsJson) {
    Form form = Form.getActiveForm();

    if (form == null) {
      return;
    }

    I18nTranslationManager manager =
        form.getI18nTranslationManager();

    manager.setPreviewLanguageOverride(language);

    if (translationsJson != null
        && translationsJson.trim().length() > 0) {
      manager.loadFromJson(translationsJson, form);
    } else {
      manager.applyLoadedTranslations(form);
    }
  }

  private int applyTranslations(TranslationProvider provider, JSONObject entries,
      String language) throws JSONException {
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
      if (screenName.length() > 0 && !screenName.equals(provider.getFormName())) {
        continue;
      }

      String componentName = source.optString("component", "");
      String propertyName = source.optString("property", "");
      String translatedValue = translations.optString(language, "");

      if (translatedValue.length() == 0) {
        continue;
      }

      Object component = provider.lookupComponent(componentName);
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

  private boolean applyStringProperty(Object component, String propertyName,
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

  private String selectLanguage() {
    if (previewLanguageOverride.length() > 0) {
      return previewLanguageOverride;
    }

    String deviceLanguage = getDeviceLanguageCode();
    String baseLanguage = getBaseLanguageCode();

    if (translationsRoot == null) {
      return deviceLanguage;
    }

    JSONObject entries = translationsRoot.optJSONObject("entries");
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