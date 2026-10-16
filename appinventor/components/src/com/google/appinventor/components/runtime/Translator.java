// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;

import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.translate.TranslatorToken;

import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.Base58Util;
import com.google.appinventor.components.runtime.util.HttpUtil;

import java.io.IOException;

import org.json.JSONException;

/**
 * Use this component to translate words and sentences between
 * different languages. This component needs Internet access, as it
 * will request translations from a server at MIT (which in turn will
 * request translations from a commercial translation service).
 * Specify the source and target language in the form source-target
 * using two letter language codes.  So "en-es" will translate from
 * English to Spanish while "es-ru" will translate from Spanish to
 * Russian. If you leave out the source language, the service will
 * attempt to detect the source language. So providing just "es" will
 * attempt to detect the source language and translate it to Spanish.
 *
 * **Note:** Translation happens asynchronously in the background. When the translation is complete,
 * the {@link #GotTranslation(String, String)} event is triggered.
 */
@DesignerComponent(version = YaVersion.TRANSLATOR_COMPONENT_VERSION,
    description = "Use this component to translate words and sentences between different" +
                  "languages. This component needs Internet access, as it will request" +
                  "translations from a server at MIT (which in turn will request" +
                  "translations from a commercial translation service).  Specify the" +
                  "source and target language in the form source-target using two letter" +
                  "language codes.  So \"en-es\" will translate from English to Spanish" +
                  "while \"es-ru\" will translate from Spanish to Russian. If you leave out" +
                  "the source language, the service will attempt to detect the source" +
                  "language. So providing just \"es\" will attempt to detect the source" +
                  "language and translate it to Spanish.  <i>Note</i>: Translation" +
                  "happens asynchronously in the background. When the translation is" +
                  "complete, the <b>GotTranslation(String, String)</b> event is" +
                    "triggered.",
    category = ComponentCategory.MEDIA,
    nonVisible = true,
    iconName = "images/translator.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "protobuf-java-3.0.0.jar")
@SimpleObject
public class Translator extends AndroidNonvisibleComponent {

  public static final String TRANSLATOR_SERVICE_URL =
    "https://tr.appinventor.mit.edu/tr/v1";

  private final Activity activity;
  private String apiKey;
  private static final String LOG_TAG = "Translator";
  private static final boolean DEBUG = false;

  /**
   * Creates a new component.
   *
   * @param container  container, component will be placed in
   */
  public Translator(ComponentContainer container) {
    super(container.$form());
    activity = container.$context();
  }

  /**
   * By providing a target language to translate to (for instance, 'es' for Spanish, 'en' for
   * English, or 'ru' for Russian), and a word or sentence to translate, this method will request
   * a translation. Once the text is translated by the external
   * service, the event {@link #GotTranslation(String, String)} will be executed.
   *
   *   **Note:** Translator will attempt to detect the source language. You can also specify
   * prepending it to the language translation, e.g., es-ru will specify Spanish to Russian
   * translation.
   */
  @SimpleFunction(description = "By providing a target language to translate to (for instance, " +
      "'es' for Spanish, 'en' for English, or 'ru' for Russian), and a word or sentence to " +
      "translate, this method will request a translation.\n" +
      "Once the text is translated by the external service, the event GotTranslation will be " +
      "executed.\nNote: Translate will attempt to detect the source language. You can " +
      "also specify prepending it to the language translation. I.e., es-ru will specify Spanish " +
      "to Russian translation.")
  public void RequestTranslation(final String languageToTranslateTo,
                                 final String textToTranslate) {

    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        performRequest(languageToTranslateTo, textToTranslate);
      }
      });
  }

  /**
   * This is the actual request to the Translate service. It opens a connection
   * to the translate service and parses the result.
   *
   * @param languageToTranslateTo the language that the text will be translated into.
   * @param textToTranslate the word or sentence to translate.
   * @throws IOException if the connection is not successful.
   * @throws JSONException if the JSON response cannot be parsed.
   */
  private void performRequest(String languageToTranslateTo, String textToTranslate) {
    // languageToTranslateTo is provided either as a two letter code, or two
    // two letter codes separated by a dash. If only one two letter code is
    // provided, it is the target language and we set the source language to auto
    // which tells the service to detect the language
    try {
      String[] languages = languageToTranslateTo.split("-");
      if (languages.length > 2) {
        GotTranslation("400", ""); // Bad Request
        return;
      }
      String srcLanguage = "auto";
      String targetLanguage = languageToTranslateTo;
      if (languages.length == 2) {
        srcLanguage = languages[0];
        targetLanguage = languages[1];
      }

      byte [] decodedToken = Base58Util.decode(apiKey);
      TranslatorToken.token token = TranslatorToken.token.parseFrom(decodedToken);
      TranslatorToken.request request = TranslatorToken.request.newBuilder()
        .setToken(token)
        .setTotranslate(textToTranslate)
        .setSourcelanguage(srcLanguage)
        .setTargetlanguage(targetLanguage).build();

      HttpUtil.post(LOG_TAG, TRANSLATOR_SERVICE_URL, request.toByteArray(), new HttpUtil.HttpCallback() {
        @Override
        public void onResponse(int responseCode, byte[] content, String error) {
          if (error != null) {
            GotTranslation(Integer.toString(responseCode), error);
            return;
          }
          try {
            TranslatorToken.response response = TranslatorToken.response.parseFrom(content);
            String returnText;
            if (responseCode == 200) {
              returnText = response.getTranslated();
              GotTranslation(Integer.toString(responseCode), returnText);
            }
          } catch (Exception e) {
            Log.e(LOG_TAG, "Error parsing response from Translator", e);
            GotTranslation(Integer.toString(responseCode), "Error parsing response from TranslatorBot");
          }
        }
      });
    } catch (Exception e) {
      GotTranslation("400", "Error Fetching Translation");
    }
  }

  /**
   * Event indicating that a request has finished and has returned data (translation).
   *
   * @param responseCode the response code from the server
   * @param translation the response content from the server
   */
  @SimpleEvent(description = "Event triggered when the Translate service returns the " +
      "translated text. This event also provides a response code for error handling. If the " +
      "responseCode is not 200, then something went wrong with the call, and the translation will " +
      "not be available.")
  public void GotTranslation(final String responseCode, final String translation) {
    activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          EventDispatcher.dispatchEvent(Translator.this, "GotTranslation", responseCode, translation);
        }
      });
  }

  /**
   * The API Key to use. MIT App Inventor will automatically fill this
   * value in. You should not need to change it.
   *
   */

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty(description = "The API Key to use. MIT App Inventor will automatically fill this " +
    "value in. You should not need to change it.",
      userVisible = true, category = PropertyCategory.ADVANCED)
  public void ApiKey(String apiKey) {
    this.apiKey = apiKey;
  }
}

