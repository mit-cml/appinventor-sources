// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;

import android.text.TextUtils;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Use this component to translate words and sentences between different languages. This component
 * needs Internet access, as it will request translations to the Yandex.Translate service.
 * Specify the source and target language in the form source-target using two letter language codes.
 * So "en-es" will translate from English to Spanish while "es-ru" will translate from Spanish to
 * Russian. If you leave out the source language, the service will attempt to detect the source
 * language. So providing just "es" will attempt to detect the source language and translate it
 * to Spanish.
 *
 * This component is powered by the Yandex translation service. See
 * http://api.yandex.com/translate/ for more information, including the list of available languages
 * and the meanings of the language codes and status codes.
 *
 * **Note:** Translation happens asynchronously in the background. When the translation is complete,
 * the {@link #GotTranslation(String, String)} event is triggered.
 */
@DesignerComponent(version = YaVersion.YANDEX_COMPONENT_VERSION,
    description = "Use this component to translate words and sentences between different " +
        "languages. This component needs Internet access, as it will request " +
        "translations to the Yandex.Translate service. Specify the source and " +
        "target language in the form source-target using two letter language " +
        "codes. So\"en-es\" will translate from English to Spanish while " +
        "\"es-ru\" will translate from Spanish to Russian. If you leave " +
        "out the source language, the service will attempt to detect the source " +
        "language. So providing just \"es\" will attempt to detect the source " +
        "language and translate it to Spanish.<p /> This component is powered by the " +
        "Yandex translation service.  See http://api.yandex.com/translate/ " +
        "for more information, including the list of available languages and the " +
        "meanings of the language codes and status codes. " +
        "<p />Note: Translation happens asynchronously in the background. When the " +
        "translation is complete, the \"GotTranslation\" event is triggered.",
    category = ComponentCategory.INTERNAL,
    nonVisible = true,
    iconName = "images/yandex.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@SimpleObject
public final class YandexTranslate extends AndroidNonvisibleComponent {


  /**
   * Creates a new component.
   *
   * @param container  container, component will be placed in
   */
  public YandexTranslate(ComponentContainer container) {
    super(container.$form());
  }

  /**
   * By providing a target language to translate to (for instance, 'es' for Spanish, 'en' for
   * English, or 'ru' for Russian), and a word or sentence to translate, this method will request
   * a translation to the Yandex.Translate service. Once the text is translated by the external
   * service, the event {@link #GotTranslation(String, String)} will be executed.
   *
   *   **Note:** Yandex.Translate will attempt to detect the source language. You can also specify
   * prepending it to the language translation, e.g., es-ru will specify Spanish to Russian
   * translation.
   */
  @SimpleFunction(description = "By providing a target language to translate to (for instance, " +
      "'es' for Spanish, 'en' for English, or 'ru' for Russian), and a word or sentence to " +
      "translate, this method will request a translation to the Yandex.Translate service.\n" +
      "Once the text is translated by the external service, the event GotTranslation will be " +
      "executed.\nNote: Yandex.Translate will attempt to detect the source language. You can " +
      "also specify prepending it to the language translation. I.e., es-ru will specify Spanish " +
      "to Russian translation.")
  public void RequestTranslation(final String languageToTranslateTo,
                                 final String textToTranslate) {

  }

  /**
   * Event indicating that a request has finished and has returned data (translation).
   *
   * @param responseCode the response code from the server
   * @param translation the response content from the server
   */
  @SimpleEvent(description = "Event triggered when the Yandex.Translate service returns the " +
      "translated text. This event also provides a response code for error handling. If the " +
      "responseCode is not 200, then something went wrong with the call, and the translation will " +
      "not be available.")
  public void GotTranslation(String responseCode, String translation) {
  }

  /**
   * The Yandex API Key to use. If set to DEFAULT the platform default key (if any)
   * will be used. Otherwise should be set to a valid API key which can be obtained
   * from https://tech.yandex.com/translate/. If the platform doesn't have a default
   * key and one isn't provided here, an error will be raised.
   */

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "DEFAULT")
  @SimpleProperty(description = "Set the API Key to use with Yandex. " +
      "You do not need to set this if you are using the MIT system because " +
      "MIT has its own key builtin. If set, the key provided here will be " +
      "used instead",
      category = PropertyCategory.BEHAVIOR)
  public void ApiKey(String apiKey) {
  }

}
