package com.google.appinventor.components.runtime;

public interface SpeechListener {
  // For the service approach in which we can report partial results to the blocks
  void onPartialResult(String text);
  // For the final result, used by both approaches
  void onResult(String text);
  // In case we need to report any errors, such as the activity or service dying for an unexpected reason.
  void onError(String message);
}
