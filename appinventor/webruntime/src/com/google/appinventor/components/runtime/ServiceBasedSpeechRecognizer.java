package com.google.appinventor.components.runtime;

import android.content.Intent;

public class ServiceBasedSpeechRecognizer extends IntentBasedSpeechRecognizer {

  public ServiceBasedSpeechRecognizer(ComponentContainer container, Intent recognizerIntent) {
    super(container, recognizerIntent);
    setContinuous(true);
  }

  private native void setContinuous(boolean continuous) /*-{
    var recognizer = this.@com.google.appinventor.components.runtime.IntentBasedSpeechRecognizer::recognizer;
    if (!recognizer) {
      return;
    }
    recognizer.continuous = continuous;
    recognizer.interimResults = continuous;
  }-*/;

}
