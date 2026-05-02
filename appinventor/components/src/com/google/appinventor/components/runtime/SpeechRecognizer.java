// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;


import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.Manifest;

import android.os.Build;
import android.os.Bundle;

import android.speech.RecognizerIntent;

import android.text.TextUtils;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.UsesQueries;

import com.google.appinventor.components.annotations.androidmanifest.ActionElement;
import com.google.appinventor.components.annotations.androidmanifest.IntentFilterElement;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ![SpeechRecognizer icon](images/speechrecognizer.png)
 *
 * Use a `SpeechRecognizer` component to listen to the user speaking and convert the spoken sound
 * into text using the device's speech recognition feature.
 *
 * @internaldoc
 * Component for using the built in VoiceRecognizer to convert speech to text.
 * For more details, please see:
 * http://developer.android.com/reference/android/speech/RecognizerIntent.html
 *
 */
@DesignerComponent(version = YaVersion.SPEECHRECOGNIZER_COMPONENT_VERSION,
    description = "Component for using Voice Recognition to convert from speech to text",
    category = ComponentCategory.MEDIA,
    nonVisible = true,
    iconName = "images/speechRecognizer.png")

@SimpleObject
@UsesPermissions({RECORD_AUDIO, INTERNET})
public class SpeechRecognizer extends AndroidNonvisibleComponent
    implements Component, OnClearListener, SpeechListener {

  private final ComponentContainer container;
  private String result;
  private Intent recognizerIntent;
  private SpeechRecognizerController speechRecognizerController;

  private boolean havePermission = false;
  private boolean useLegacy = true;

  private String language = "";

  private YailList availableLanguages = YailList.makeEmptyList();
  private YailList availableCountries = YailList.makeEmptyList();

  /**
   * Creates a SpeechRecognizer component.
   *
   * @param container container, component will be placed in
   */
  public SpeechRecognizer(ComponentContainer container) {
    super(container.$form());
    container.$form().registerForOnClear(this);
    this.container = container;
    result = "";
    recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
    UseLegacy(useLegacy);
    querySupportedLanguages();
  }

  /**
   * List of the country codes available on this device for use with
   * SpeechRecognizer. The country codes are derived from the region subtags of
   * the supported BCP-47 language tags (for example, US from en-US). An empty
   * list is returned if the device does not support speech recognition or if
   * the list has not yet been populated.
   */
  @SimpleProperty(description = "List of the country codes available on this device "
      + "for use with SpeechRecognizer. The country codes are derived from the "
      + "region subtags of the supported language tags.",
      category = PropertyCategory.BEHAVIOR)
  public YailList AvailableCountries() {
    return availableCountries;
  }

  /**
   * List of the languages available on this device for use with SpeechRecognizer.
   * The languages are provided as
   * [BCP-47](https://en.wikipedia.org/wiki/IETF_language_tag) language tags
   * such as en-US and es-MX. An empty list is returned if the device does not
   * support speech recognition or if the list has not yet been populated.
   */
  @SimpleProperty(description = "List of the languages available on this device "
      + "for use with SpeechRecognizer. The languages are provided as BCP-47 "
      + "language tags such as en-US and es-MX. An empty list is returned if "
      + "the device does not support speech recognition.",
      category = PropertyCategory.BEHAVIOR)
  public YailList AvailableLanguages() {
    return availableLanguages;
  }

  /**
   * Suggests the language to use for recognizing speech. An empty string (the default) will
   * use the system's default language.
   *
   *     Language is specified using a [language tag](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes)
   *     with an optional region suffix, such as en or es-MX. The set of supported languages will
   *     vary by device.
   *
   * @return the target language for recognition
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String Language() {
    return language;
  }

  @SimpleProperty
  public void Language(String language) {
    this.language = language;
    if (TextUtils.isEmpty(language)) {
      recognizerIntent.removeExtra(RecognizerIntent.EXTRA_LANGUAGE);
    } else {
      recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
    }
  }

  /**
   * Returns the last text produced by the recognizer.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public String Result() {
    return result;
  }

  /**
   * Asks the user to speak, and converts the speech to text. Signals the
   * {@link #AfterGettingText(String, boolean)} event when the result is available.
   */
  @SimpleFunction
  public void GetText() {
    // Need to check if we have RECORD_AUDIO permission
    if (!havePermission) {
      final SpeechRecognizer me = this;
      form.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          form.askPermission(RECORD_AUDIO,
              new PermissionResultHandler() {
                @Override
                public void HandlePermissionResponse(String permission, boolean granted) {
                  if (granted) {
                    me.havePermission = true;
                    me.GetText();
                  } else {
                    form.dispatchPermissionDeniedEvent(me, "GetText", RECORD_AUDIO);
                  }
                }
          });
        }
      });
      return;
    }
    BeforeGettingText();
    speechRecognizerController.addListener(this);
    speechRecognizerController.start();
  }

  /**
   * Function used to forcefully stop listening speech in cases where
   * SpeechRecognizer cannot stop automatically.
   * This function works only when the {@link #UseLegacy(boolean)} property is
   * set to `false`{:.logic.block}.
   */
  @SimpleFunction
  public void Stop() {
    if (speechRecognizerController != null) {
      speechRecognizerController.stop();
    }
  }

  /**
   * Simple event to raise when the `SpeechRecognizer` is invoked but before its
   * activity is started.
   */
  @SimpleEvent
  public void BeforeGettingText() {
    EventDispatcher.dispatchEvent(this, "BeforeGettingText");
  }

  /**
   * Simple event to raise after the SpeechRecognizer has recognized speech. If
   * {@link #UseLegacy(boolean)} is `true`{:.logic.block}, then this event will only happen once
   * at the very end of the recognition. If {@link #UseLegacy(boolean)} is `false`{:.logic.block},
   * then this event will run multiple times as the `SpeechRecognizer` incrementally recognizes
   * speech. In this case, `partial` will be `true`{:.logic.block} until the recognized speech
   * has been finalized (e.g., the user has stopped speaking), in which case `partial` will be
   * `false`{:.logic.block}.
   */
  @SimpleEvent
  public void AfterGettingText(String result, boolean partial) {
    EventDispatcher.dispatchEvent(this, "AfterGettingText", result, partial);
  }

  /**
   * Method from SpeechListener interface.
   */
  @Override
  public void onPartialResult(String text) {
    result = text;
    AfterGettingText(result, true);
  }

  /**
   * Method from SpeechListener interface.
   */
  @Override
  public void onResult(String text) {
    result = text;
    AfterGettingText(result, false);
  }

  /**
   * Method from SpeechListener interface.
   */
  @Override
  public void onError(int errorNumber) {
    String functionName = "GetText";
    form.dispatchErrorOccurredEvent(this, functionName, errorNumber);
  }

  @Override
  public void onClear() {
    Stop();
    speechRecognizerController = null;
    recognizerIntent = null;
  }

  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR,
      description = "If true, an app can retain their older behaviour.")
  public boolean UseLegacy() {
    return useLegacy;
  }

  /**
   * If true, a separate dialog is used to recognize speech (the default). If false, speech is
   * recognized in the background and updates are received as it recognizes words.
   * {@link #AfterGettingText(String, boolean)} may get several calls with `partial` set to `true`{:.logic.block}.
   * Once sufficient time has elapsed since the last utterance, or `StopListening` is called,
   * the last string will be returned with `partial` set to `false`{:.logic.block} to indicate that it is the
   * final recognized string and no more data will be provided until recognition is again started. See
   * {@link #AfterGettingText(String, boolean)} for more details on partial speech recognition.
   * @param useLegacy
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(description = "If true, a separate dialog is used to recognize speech "
      + "(the default). If false, speech is recognized in the background and "
      + "partial results are also provided.")
  @UsesQueries(intents = {
      @IntentFilterElement(actionElements = {
          @ActionElement(name = "android.speech.RecognitionService")
      })
  })
  public void UseLegacy(boolean useLegacy) {
    this.useLegacy = useLegacy;
    Stop();
    if (useLegacy || Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
      speechRecognizerController = new IntentBasedSpeechRecognizer(container, recognizerIntent);
    } else {
      speechRecognizerController = new ServiceBasedSpeechRecognizer(container, recognizerIntent);
    }
  }

  /**
   * Queries the device for supported speech recognition languages by sending
   * an ordered broadcast with
   * {@link RecognizerIntent#ACTION_GET_LANGUAGE_DETAILS}. The results are
   * cached in {@link #availableLanguages} and {@link #availableCountries}
   * when the broadcast receiver fires.
   */
  private void querySupportedLanguages() {
    // ACTION_GET_LANGUAGE_DETAILS requires API 11 (Honeycomb).
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
      return;
    }
    Intent detailsIntent =
        new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
    container.$context().sendOrderedBroadcast(detailsIntent, null,
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            Bundle extras = getResultExtras(true);
            if (extras == null) {
              return;
            }
            ArrayList<String> supported = extras.getStringArrayList(
                RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES);
            if (supported == null || supported.isEmpty()) {
              return;
            }
            supported.removeAll(Collections.singleton(null));
            Collections.sort(supported);
            availableLanguages = YailList.makeList(supported);

            Set<String> regions = new HashSet<String>();
            for (String tag : supported) {
              int sep = tag.lastIndexOf('-');
              if (sep < 0) {
                sep = tag.lastIndexOf('_');
              }
              if (sep >= 0 && sep < tag.length() - 1) {
                String region = tag.substring(sep + 1);
                if (region.length() == 2) {  // ISO 3166-1 alpha-2
                  regions.add(region);
                }
              }
            }
            ArrayList<String> regionList =
                new ArrayList<String>(regions);
            Collections.sort(regionList);
            availableCountries = YailList.makeList(regionList);
          }
        }, null, 0, null, null);
  }
}
