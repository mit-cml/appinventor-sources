// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;
/**
 * This class is used to implement start and stop functionality of SpeechRecognizer,
 * and registering SpeechListener.
 */
public abstract class SpeechRecognizerController {

  SpeechListener speechListener;

  /**
   * Registering Listener to handle SpeechRecognition events
   */
  void addListener(SpeechListener speechListener){
    this.speechListener = speechListener;
  }

  /**
   * Starting SpeechRecognition
   */
  void start(){}

  /**
   * Stopping SpeechRecognition
   */
  void stop(){}
}
