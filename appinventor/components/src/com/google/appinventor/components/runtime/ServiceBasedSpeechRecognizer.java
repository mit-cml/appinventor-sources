// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

public class ServiceBasedSpeechRecognizer extends SpeechRecognizerController
    implements RecognitionListener {

  private SpeechRecognizer speech = null;
  private Intent recognizerIntent;
  private ComponentContainer container;
  private String result;

  public ServiceBasedSpeechRecognizer(ComponentContainer container, Intent recognizerIntent) {
    this.container = container;
    this.recognizerIntent = recognizerIntent;
  }

  @Override
  public void start() {
    speech = SpeechRecognizer.createSpeechRecognizer(container.$context());
    speech.setRecognitionListener(this);
    speech.startListening(recognizerIntent);
  }

  @Override
  public void stop() {
    speech.stopListening();
  }

  @Override
  public void onReadyForSpeech(Bundle bundle) {

  }

  @Override
  public void onBeginningOfSpeech() {

  }

  @Override
  public void onRmsChanged(float v) {

  }

  @Override
  public void onBufferReceived(byte[] bytes) {

  }

  @Override
  public void onEndOfSpeech() {

  }

  @Override
  public void onError(int i) {
    result = getErrorMessage(i);
    speechListener.onError(result);
  }

  @Override
  public void onResults(Bundle bundle) {
    if (bundle.isEmpty()) {
      result = "";
    } else {
      ArrayList<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
      result = results.get(0);
    }
    speechListener.onResult(result);
  }

  @Override
  public void onPartialResults(Bundle bundle) {
    if (bundle.isEmpty()) {
      result = "";
    } else {
      ArrayList<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
      result = results.get(0);
    }
    speechListener.onPartialResult(result);
  }

  @Override
  public void onEvent(int i, Bundle bundle) {

  }

  private String getErrorMessage(int errorCode) {
    String message;
    switch (errorCode) {
      case SpeechRecognizer.ERROR_AUDIO:
        message = "Audio Recording Error";
        break;
      case SpeechRecognizer.ERROR_CLIENT:
        message = "Client Side Error";
        break;
      case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
        message = "Insufficient Permissions";
        break;
      case SpeechRecognizer.ERROR_NETWORK:
        message = "Network Error";
        break;
      case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
        message = "Network Timeout";
        break;
      case SpeechRecognizer.ERROR_NO_MATCH:
        message = "No Match";
        break;
      case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
        message = "RecognitionService Busy";
        break;
      case SpeechRecognizer.ERROR_SERVER:
        message = "Error From Server";
        break;
      case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
        message = "No Speech Input";
        break;
      default:
        message = "Please Try Again !";
        break;
    }
    return message;
  }
}
