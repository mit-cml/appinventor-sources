// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;

import java.util.ArrayList;

/**
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
public class SpeechRecognizer extends AndroidNonvisibleComponent
    implements Component, ActivityResultListener {

  private final ComponentContainer container;
  private String result;

  /* Used to identify the call to startActivityForResult. Will be passed back
     into the resultReturned() callback method. */
  private int requestCode;

  /**
   * Creates a SpeechRecognizer component.
   *
   * @param container container, component will be placed in
   */
  public SpeechRecognizer(ComponentContainer container) {
    super(container.$form());
    this.container = container;
    result = "";
  }

  /**
   * Result property getter method.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public String Result() {
    return result;
  }

  /**
   * Solicits speech input from the user.  After the speech is converted to
   * text, the AfterGettingText event will be raised.
   */
  @SimpleFunction
  public void GetText() {
    BeforeGettingText();
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    if (requestCode == 0) {
      requestCode = form.registerForActivityResult(this);
    }
    container.$context().startActivityForResult(intent, requestCode);
  }

  @Override
  public void resultReturned(int requestCode, int resultCode, Intent data) {
    if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
      if (data.hasExtra(RecognizerIntent.EXTRA_RESULTS)) {
        ArrayList<String> results;
        results = data.getExtras().getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
        result = results.get(0);
      } else {
        result = "";
      }
      AfterGettingText(result);
    }
  }

  /**
   * Simple event to raise when VoiceReco is invoked but before the VoiceReco
   * activity is started.
   */
  @SimpleEvent
  public void BeforeGettingText() {
    EventDispatcher.dispatchEvent(this, "BeforeGettingText");
  }

  /**
   * Simple event to raise after the VoiceReco activity has returned
   */
  @SimpleEvent
  public void AfterGettingText(String result) {
    EventDispatcher.dispatchEvent(this, "AfterGettingText", result);
  }

}
