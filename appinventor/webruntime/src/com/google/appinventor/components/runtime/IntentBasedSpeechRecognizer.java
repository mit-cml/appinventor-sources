package com.google.appinventor.components.runtime;

import android.content.Intent;
import com.google.gwt.core.client.JavaScriptObject;

public class IntentBasedSpeechRecognizer extends SpeechRecognizerController {
  protected final JavaScriptObject recognizer = initSpeechRecognizer();

  public IntentBasedSpeechRecognizer(ComponentContainer container, Intent recognizerIntent) {
  }

  private void onError(int error) {
    if (speechListener != null) {
      speechListener.onError(error);
    }
  }

  private void onPartialResult(String content) {
    if (speechListener != null) {
      speechListener.onPartialResult(content);
    }
  }

  private void onEnd(String content) {
    if (speechListener != null) {
      speechListener.onResult(content);
    }
  }

  @Override
  native void stop() /*-{
    var recognizer = this.@com.google.appinventor.components.runtime.IntentBasedSpeechRecognizer::recognizer;
    if (recognizer) {
      recognizer.stop();
    }
  }-*/;

  @Override
  native void start() /*-{
    var recognizer = this.@com.google.appinventor.components.runtime.IntentBasedSpeechRecognizer::recognizer;
    if (recognizer) {
      recognizer.start();
    }
  }-*/;

  private native JavaScriptObject initSpeechRecognizer() /*-{
    var recognizer;
    if ($wnd.SpeechRecognition) {
      recognizer = new $wnd.SpeechRecognition();
    } else if ($wnd.webkitSpeechRecognition) {
      recognizer = new $wnd.webkitSpeechRecognition();
    } else {
      console.error("Speech recognition not supported in this browser.");
      return null;
    }
    var self = this;
    recognizer.addEventListener('error', function(event) {
      self.@com.google.appinventor.components.runtime.IntentBasedSpeechRecognizer::onError(I)(event.error);
    });
    recognizer.addEventListener('result', function(event) {
      if (event.results && event.results.length > 0) {
        var result = event.results[0];
        if (result.isFinal) {
          self.@com.google.appinventor.components.runtime.IntentBasedSpeechRecognizer::onEnd(Ljava/lang/String;)(result[0].transcript);
        } else {
          self.@com.google.appinventor.components.runtime.IntentBasedSpeechRecognizer::onPartialResult(Ljava/lang/String;)(result[0].transcript);
        }
      }
    });
    return recognizer;
  }-*/;
}
