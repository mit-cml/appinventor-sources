// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0

package com.google.appinventor.components.runtime.util;

import android.content.res.AssetManager;
import android.util.Log;

import com.google.appinventor.components.runtime.Form;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Minimal runtime internationalization helper for App Inventor applications.
 *
 * MVP behavior:
 * - Loads i18n.json from app assets if present.
 * - Resolves strings prefixed with @i18n:.
 * - Falls back safely when file/key/language is missing.
 *
 * This is intentionally runtime-only. Designer key generation, Blockly
 * integration,
 * and build-time strings.xml generation should be added in later phases.
 */
public final class I18nUtil {
    private static final String LOG_TAG = "I18nUtil";
    private static final String I18N_PREFIX = "@i18n:";
    private static final String I18N_FILE = "i18n.json";

    private final Form form;
    private final Map<String, Map<String, String>> translations = new HashMap<String, Map<String, String>>();

    private String defaultLanguage = "en";
    private String currentLanguage;
    private String currentLanguageWithRegion;
    private boolean loaded = false;

    public I18nUtil(Form form) {
        this.form = form;
        Locale locale = Locale.getDefault();
        currentLanguage = normalizeLanguage(locale.getLanguage());

        String country = locale.getCountry();
        if (country != null && country.length() > 0) {
            currentLanguageWithRegion = currentLanguage + "-" + country.toLowerCase(Locale.US);
        } else {
            currentLanguageWithRegion = currentLanguage;
        }
    }

    public void load() {
        if (loaded) {
            return;
        }

        loaded = true;

        try {
            String json = readAsset(I18N_FILE);
            if (json == null || json.trim().length() == 0) {
                return;
            }

            parse(json);
            Log.i(LOG_TAG, "Loaded " + translations.size() + " translation keys from " + I18N_FILE);
        } catch (FileNotFoundException e) {
            // Backward compatibility: most existing apps will not have i18n.json.
            Log.i(LOG_TAG, "No " + I18N_FILE + " found. Internationalization disabled.");
        } catch (IOException e) {
            Log.w(LOG_TAG, "Unable to read " + I18N_FILE + ". Internationalization disabled.", e);
        } catch (JSONException e) {
            Log.w(LOG_TAG, "Invalid " + I18N_FILE + ". Internationalization disabled.", e);
            translations.clear();
        }
    }

    public String resolveText(String text) {
        if (text == null) {
            return "";
        }

        if (!text.startsWith(I18N_PREFIX)) {
            return text;
        }

        String key = text.substring(I18N_PREFIX.length());
        return resolveKey(key, text);
    }

    public String resolveKey(String key, String fallback) {
        if (key == null || key.length() == 0) {
            return fallback == null ? "" : fallback;
        }

        Map<String, String> values = translations.get(key);
        if (values == null) {
            return fallback == null ? key : fallback;
        }

        String value = lookup(values, currentLanguageWithRegion);
        if (value != null) {
            return value;
        }

        value = lookup(values, currentLanguage);
        if (value != null) {
            return value;
        }

        value = lookup(values, defaultLanguage);
        if (value != null) {
            return value;
        }

        value = lookup(values, "en");
        if (value != null) {
            return value;
        }

        return fallback == null ? key : fallback;
    }

    private String lookup(Map<String, String> values, String language) {
        if (language == null) {
            return null;
        }

        String value = values.get(normalizeLanguage(language));
        return value == null || value.length() == 0 ? null : value;
    }

    private void parse(String json) throws JSONException {
        JSONObject root = new JSONObject(json);

        if (root.has("defaultLanguage")) {
            defaultLanguage = normalizeLanguage(root.optString("defaultLanguage", "en"));
        }

        JSONObject translationRoot;

        // Supports MVP format:
        // {
        // "defaultLanguage": "en",
        // "translations": {
        // "screen1_button1_text": { "en": "Submit", "hi": "जमा करें" }
        // }
        // }
        //
        // Also supports flat prototype format:
        // {
        // "screen1_button1_text": { "en": "Submit", "hi": "जमा करें" }
        // }
        if (root.has("translations")) {
            translationRoot = root.getJSONObject("translations");
        } else {
            translationRoot = root;
        }

        Iterator<String> keys = translationRoot.keys();
        while (keys.hasNext()) {
            String key = keys.next();

            if ("defaultLanguage".equals(key) || "schemaVersion".equals(key) || "meta".equals(key)) {
                continue;
            }

            Object maybeValues = translationRoot.opt(key);
            if (!(maybeValues instanceof JSONObject)) {
                continue;
            }

            JSONObject valuesObject = (JSONObject) maybeValues;
            Map<String, String> values = new HashMap<String, String>();

            Iterator<String> languages = valuesObject.keys();
            while (languages.hasNext()) {
                String language = languages.next();
                values.put(normalizeLanguage(language), valuesObject.optString(language, ""));
            }

            translations.put(key, values);
        }
    }

    private String readAsset(String assetName) throws IOException {
        AssetManager assets = form.getAssets();
        InputStream inputStream = null;

        try {
            inputStream = assets.open(assetName);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];
            int count;
            while ((count = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, count);
            }

            return outputStream.toString("UTF-8");
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private String normalizeLanguage(String language) {
        return language == null ? "" : language.replace('_', '-').toLowerCase(Locale.US);
    }
}