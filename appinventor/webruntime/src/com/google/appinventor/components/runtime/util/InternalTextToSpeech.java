package com.google.appinventor.components.runtime.util;

import android.app.Activity;
import java.util.Locale;

public class InternalTextToSpeech implements ITextToSpeech {
  private final ITextToSpeech.TextToSpeechCallback callback;
  private float pitch = 1.0f;
  private float rate = 1.0f;

  public InternalTextToSpeech(Activity form, ITextToSpeech.TextToSpeechCallback callback) {
    this.callback = callback;
  }

  @Override
  public native void speak(String message, Locale loc) /*-{
    if ($wnd.speechSynthesis) {
      var utterance = new $wnd.SpeechSynthesisUtterance(message);
      utterance.lang = loc.toString();
      utterance.pitch = this.@com.google.appinventor.components.runtime.util.InternalTextToSpeech::pitch;
      utterance.rate = this.@com.google.appinventor.components.runtime.util.InternalTextToSpeech::rate;
      utterance.onend = function(event) {
        if (this.@com.google.appinventor.components.runtime.util.InternalTextToSpeech::callback != null) {
          this.@com.google.appinventor.components.runtime.util.InternalTextToSpeech::callback.
              @com.google.appinventor.components.runtime.util.ITextToSpeech.TextToSpeechCallback::onSuccess()();
        }
      }.bind(this);
      utterance.onerror = function(event) {
        if (this.@com.google.appinventor.components.runtime.util.InternalTextToSpeech::callback != null) {
          this.@com.google.appinventor.components.runtime.util.InternalTextToSpeech::callback.
              @com.google.appinventor.components.runtime.util.ITextToSpeech.TextToSpeechCallback::onFailure()();
        }
      }.bind(this);
      $wnd.speechSynthesis.speak(utterance);
    }
  }-*/;

  @Override
  public void onStop() {
    stop();
  }

  @Override
  public void onResume() {

  }

  @Override
  public void onDestroy() {
    stop();
  }

  @Override
  public void setPitch(float pitch) {
    this.pitch = pitch;
  }

  @Override
  public void setSpeechRate(float speechRate) {
    this.rate = speechRate;
  }

  @Override
  public native void stop() /*-{
    if ($wnd.speechSynthesis && $wnd.speechSynthesis.speaking) {
      $wnd.speechSynthesis.cancel();
    }
  }-*/;

  @Override
  public int isLanguageAvailable(Locale loc) {
    return 0;
  }

  @Override
  public boolean isInitialized() {
    return false;
  }
}
