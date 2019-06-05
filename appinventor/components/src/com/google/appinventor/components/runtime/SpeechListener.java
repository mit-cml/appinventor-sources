// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

/**
 * This interface is used to declare functions associated with results and errors of SpeechRecognizer,
 * used in both ServiceBasedSpeechRecognizer and IntentBasedSpeechRecognizer
 */
public interface SpeechListener {

  /**
   * For the service based approach in which we can report partial results to the blocks
   */
  void onPartialResult(String text);

  /**
   * For the final result, used by both approaches
   */
  void onResult(String text);

  /**
   * In case we need to report any errors, such as the activity or service dying for an unexpected reason
   */
  void onError(int errorCode);
}
