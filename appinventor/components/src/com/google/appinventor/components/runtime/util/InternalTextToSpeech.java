// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime.util;

import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.util.Log;

import android.os.Handler;

/**
 * Wrapper class for Android's {@link android.speech.tts.TextToSpeech} class, which doesn't exist on
 * pre-1.6 devices.
 *
 * We need to wrap this because the Dalvik class loader will fail to verify a class that contains
 * references to non-existence classes and therefore we arrange for the failure to occur in this
 * class, rather than the component class which references this.  The component class can figure
 * out whether it wants to use this wrapper class or not depending on the SDK level.
 *
 * See http://android-developers.blogspot.com/2009/04/backward-compatibility-for-android.html for
 * some more about this.
 *
 * @author markf@google.com (Mark Friedman)
 */
public class InternalTextToSpeech implements ITextToSpeech {

  private static final String LOG_TAG = "InternalTTS";

  private final Activity activity;
  private final TextToSpeechCallback callback;
  private TextToSpeech tts;
  private volatile boolean isTtsInitialized;

  private Handler mHandler = new Handler();

  private int nextUtteranceId = 1;

  // time (ms) to delay before retrying speak when tts not yet initialized
  private int ttsRetryDelay = 500;

  // max number of retries waiting for initialization before speak fails
  // This is very long, but it's better to get a long delay than simply have
  // no speech in the case of initialization slowness
  private int ttsMaxRetries = 20;

  public InternalTextToSpeech(Activity activity, TextToSpeechCallback callback) {
    this.activity = activity;
    this.callback = callback;
    initializeTts();
  }

  private void initializeTts() {
    if (tts == null) {
      Log.d(LOG_TAG, "INTERNAL TTS is reinitializing");
      tts = new TextToSpeech(activity, new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
          if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true;
          }
        }
      });
    }
  }

  @Override
  public void speak(final String message, final Locale loc) {
    Log.d(LOG_TAG, "Internal TTS got speak");
    speak(message, loc, 0);
  }

  public boolean isInitialized() {
    return isTtsInitialized;
  }


  private void speak(final String message, final Locale loc, final int retries) {
    Log.d(LOG_TAG, "InternalTTS speak called, message = " + message);
    if (retries > ttsMaxRetries) {
      Log.d(LOG_TAG, "max number of speak retries exceeded: speak will fail");
      callback.onFailure();
    }
    // If speak was called before initialization was complete, we retry after a delay.
    // Keep track of the number of retries and fail if there are too many.
    if (isTtsInitialized) {
      Log.d(LOG_TAG, "TTS initialized after " + retries + " retries.");
      tts.setLanguage(loc);
      tts.setOnUtteranceCompletedListener(
          new TextToSpeech.OnUtteranceCompletedListener() {
            @Override
            public void onUtteranceCompleted(String utteranceId) {
              // onUtteranceCompleted is not called on the UI thread, so we use
              // Activity.runOnUiThread() to call callback.onSuccess().
              activity.runOnUiThread(new Runnable() {
                public void run() {
                  callback.onSuccess();
                }
              });
            }
          });
      // We need to provide an utterance id. Otherwise onUtteranceCompleted won't be called.
      HashMap<String, String> params = new HashMap<String, String>();
      params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Integer.toString(nextUtteranceId++));
      int result = tts.speak(message, tts.QUEUE_FLUSH, params);
      if (result == TextToSpeech.ERROR) {
        Log.d(LOG_TAG, "speak called and tts.speak result was an error");
        callback.onFailure();
      }
    } else {
      Log.d(LOG_TAG, "speak called when TTS not initialized");
      mHandler.postDelayed(new Runnable() {
        public void run() {
          Log.d(LOG_TAG,
              "delaying call to speak.  Retries is: " + retries + " Message is: " + message);
          speak(message, loc, retries + 1);
        }
      }, ttsRetryDelay);
    }
  }

  @Override
  public void onStop() {
    Log.d(LOG_TAG, "Internal TTS got onStop");
    // do nothing.  Resources will be cleaned up in onDestroy
  }

  @Override
  public void onDestroy() {
    Log.d(LOG_TAG, "Internal TTS got onDestroy");
    if (tts != null) {
      tts.shutdown();
      isTtsInitialized = false;
      tts = null;
    }
  }

  @Override
  public void onResume() {
    Log.d(LOG_TAG, "Internal TTS got onResume");
    initializeTts();
  }

  @Override
  public void setPitch(float pitch) {
    tts.setPitch(pitch);
  }

  @Override
  public void setSpeechRate(float speechRate) {
    tts.setSpeechRate(speechRate);
  }

  // This is for use by the higher level TextToSpeech component
  public int isLanguageAvailable(Locale loc) {
    return tts.isLanguageAvailable(loc);
  }

}

