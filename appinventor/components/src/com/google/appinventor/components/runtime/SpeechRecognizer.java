// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

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

import android.content.Intent;
import android.Manifest;
import android.os.Build;
import android.speech.RecognizerIntent;

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
@UsesPermissions(permissionNames = "android.permission.RECORD_AUDIO," +
        "android.permission.INTERNET")
public class SpeechRecognizer extends AndroidNonvisibleComponent
    implements Component, OnClearListener, SpeechListener {

  private final ComponentContainer container;
  private String result;
  private Intent recognizerIntent;
  private SpeechRecognizerController speechRecognizerController;

  private boolean havePermission = false;
  private boolean useLegacy = true;

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
          form.askPermission(Manifest.permission.RECORD_AUDIO,
              new PermissionResultHandler() {
                @Override
                public void HandlePermissionResponse(String permission, boolean granted) {
                  if (granted) {
                    me.havePermission = true;
                    me.GetText();
                  } else {
                    form.dispatchPermissionDeniedEvent(me, "GetText", Manifest.permission.RECORD_AUDIO);
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
   * recognized in the background and partial results are also provided. See
   * {@link #AfterGettingText(String, boolean)} for more details on partial speech recognition.
   * @param useLegacy
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(description = "If true, a separate dialog is used to recognize speech "
      + "(the default). If false, speech is recognized in the background and "
      + "partial results are also provided.")
  public void UseLegacy(boolean useLegacy) {
    this.useLegacy = useLegacy;
    Stop();
    if (useLegacy == true || Build.VERSION.SDK_INT<8) {
      speechRecognizerController = new IntentBasedSpeechRecognizer(container, recognizerIntent);
    } else {
      speechRecognizerController = new ServiceBasedSpeechRecognizer(container, recognizerIntent);
    }
  }
}
