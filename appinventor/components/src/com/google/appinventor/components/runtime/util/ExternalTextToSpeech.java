// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.ActivityResultListener;
import com.google.appinventor.components.runtime.ComponentContainer;

import android.app.Activity;
import android.content.Intent;

import java.util.Locale;

/**
 * Wrapper class for delegating calls to an external text-to-speech library for
 * devices lacking an internal text-to-speech library (pre-1.6).
 *
 * @author markf@google.com (Mark Friedman)
 */

// Note(Hal): I have not looked at making the changes for retries, similar to the ones in
// InternalTextToSpeech.   The use of ExternalTTS is for pre-1.6 systems only, so is obsolete.
// And I don't have an old external TTS library to use in testing, anyway.

public class ExternalTextToSpeech implements ITextToSpeech, ActivityResultListener {

  private static final String TTS_INTENT = "com.google.tts.makeBagel";

  /* Used to identify the call to startActivityForResult. Will be passed back
     into the resultReturned() callback method. */
  private int requestCode;

  private final ComponentContainer container;

  private final TextToSpeechCallback callback;

  public ExternalTextToSpeech(ComponentContainer container,
                              TextToSpeechCallback callback) {
    this.container = container;
    this.callback = callback;
  }

  @Override
  public void speak(String message, Locale loc) {
    Intent intent = new Intent(TTS_INTENT);
    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    intent.putExtra("message", message);
    intent.putExtra("language", loc.getISO3Language());
    intent.putExtra("country", loc.getISO3Country());
    if (requestCode == 0) {
      requestCode = container.$form().registerForActivityResult(this);
    }
    container.$context().startActivityForResult(intent, requestCode);
  }

  @Override
  public void onDestroy() {
    // nothing to do
  }

  @Override
  public void onStop() {
    // nothing to do
  }

  @Override
  public void onResume() {
    // nothing to do
  }

  @Override
    public void setPitch(float pitch) {
        // nothing to do
    }

    @Override
    public void setSpeechRate(float speechRate) {
    // nothing to do
  }

  @Override
  public void resultReturned(int requestCode, int resultCode, Intent data) {
    boolean isSuccess = (requestCode == this.requestCode) && (resultCode == Activity.RESULT_OK);
    if (isSuccess) {
      callback.onSuccess();
    } else {
      callback.onFailure();
    }
  }

}
