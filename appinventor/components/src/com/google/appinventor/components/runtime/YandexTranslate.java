// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import android.app.Activity;
import com.google.appinventor.components.annotations.UsesPermissions;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
    category = ComponentCategory.MEDIA,
    nonVisible = true,
    iconName = "images/yandex.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@SimpleObject
public final class YandexTranslate extends AndroidNonvisibleComponent {

  public static final String YANDEX_TRANSLATE_SERVICE_URL =
      "https://translate.yandex.net/api/v1.5/tr.json/translate?key=";
  private final String yandexKey;
  private final Activity activity;

  /**
   * Creates a new component.
   *
   * @param container  container, component will be placed in
   */
  public YandexTranslate(ComponentContainer container) {
    super(container.$form());

    // Set up the Yandex.Translate Tagline in the 'About' screen
    form.setYandexTranslateTagline();

    // TODO (user) To provide users with this component you will need to obtain a key with the
    // Yandex.Translate service at http://api.yandex.com/translate/
    yandexKey = "";
    activity = container.$context();
  }

  /**
   * Performs an HTTP GET request to the Yandex.Translate service
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

    if (yandexKey.equals("")){
      form.dispatchErrorOccurredEvent(YandexTranslate.this, "RequestTranslation",
          ErrorMessages.ERROR_TRANSLATE_NO_KEY_FOUND);
      return;
    }

    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        try {
          performRequest(languageToTranslateTo, textToTranslate);
        } catch (IOException e) {
          form.dispatchErrorOccurredEvent(YandexTranslate.this, "RequestTranslation",
              ErrorMessages.ERROR_TRANSLATE_SERVICE_NOT_AVAILABLE);
        } catch (JSONException je) {
          form.dispatchErrorOccurredEvent(YandexTranslate.this, "RequestTranslation",
              ErrorMessages.ERROR_TRANSLATE_JSON_RESPONSE);
        }
      }
    });
  }

  /**
   * This is the actual request to the Yandex.Translate service. It opens a https connection to
   * their web based API and parses the JSON response.
   * @param languageToTranslateTo the language that the text will be translated into.
   * @param textToTranslate the word or sentence to translate.
   * @throws IOException if the connection is not successful.
   * @throws JSONException if the JSON response cannot be parsed.
   */
  private void performRequest(String languageToTranslateTo, String textToTranslate)
      throws IOException, JSONException {

    final String finalURL = YANDEX_TRANSLATE_SERVICE_URL +
        this.yandexKey +
        "&lang=" + languageToTranslateTo +
        "&text=" + textToTranslate;

    URL url = new URL(finalURL);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    if (connection != null) {
      try {
        final String responseContent = getResponseContent(connection);

        JSONObject jsonResponse = new JSONObject(responseContent);

        final String responseCode = jsonResponse.getString("code");

        // The translation will be in position Zero of the array returned with key 'text'
        org.json.JSONArray response = jsonResponse.getJSONArray("text");
        final String translation = (String)response.get(0);

        // Dispatch the event.
        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            GotTranslation(responseCode, translation);
          }
        });

      } finally {
        connection.disconnect();
      }
    }
  }

  /**
   * This method reads from a stream based on the passed connection
   * @param connection the connection to read from
   * @return the contents of the stream
   * @throws IOException if it cannot read from the http connection
   */
  private static String getResponseContent(HttpURLConnection connection) throws IOException {
    // Use the content encoding to convert bytes to characters.
    String encoding = connection.getContentEncoding();
    if (encoding == null) {
      encoding = "UTF-8";
    }
    InputStreamReader reader = new InputStreamReader(connection.getInputStream(), encoding);
    try {
      int contentLength = connection.getContentLength();
      StringBuilder sb = (contentLength != -1)
          ? new StringBuilder(contentLength)
          : new StringBuilder();
      char[] buf = new char[1024];
      int read;
      while ((read = reader.read(buf)) != -1) {
        sb.append(buf, 0, read);
      }
      return sb.toString();
    } finally {
      reader.close();
    }
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
    EventDispatcher.dispatchEvent(this, "GotTranslation", responseCode, translation);
  }

}
