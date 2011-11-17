// Copyright 2011 Google Inc. All Rights Reserved.
package com.google.appinventor.components.runtime.util;

import android.app.Activity;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;

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

  private final Activity activity;
  private final TextToSpeechCallback callback;
  private TextToSpeech tts;
  private volatile boolean isTtsInitialized;

  private int nextUtteranceId = 1;

  public InternalTextToSpeech(Activity activity, TextToSpeechCallback callback) {
    this.activity = activity;
    this.callback = callback;
    initializeTts();
  }

  private void initializeTts() {
    if (tts == null) {
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
    if (isTtsInitialized) {
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
        callback.onFailure();
      }
    } else {
      callback.onFailure();
    }
  }

  @Override
  public void onStop() {
    if (tts != null) {
      tts.shutdown();
      isTtsInitialized = false;
      tts = null;
    }
  }

  @Override
  public void onResume() {
    initializeTts();
  }
}
