package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.ComponentContainer;

public class ExternalTextToSpeech extends InternalTextToSpeech {
  public ExternalTextToSpeech(ComponentContainer container, ITextToSpeech.TextToSpeechCallback callback) {
    super(container.$form(), callback);
  }
}
