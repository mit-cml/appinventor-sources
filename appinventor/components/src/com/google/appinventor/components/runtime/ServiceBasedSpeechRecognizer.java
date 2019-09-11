// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.util.ErrorMessages;

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
    if (speech != null) {
      speech.stopListening();
    }
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
    int errorNumber = getErrorMessage(i);
    speechListener.onError(errorNumber);
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

  private int getErrorMessage(int errorCode) {
    int errCode = ErrorMessages.ERROR_DEFAULT;
    switch (errorCode) {
      case SpeechRecognizer.ERROR_AUDIO:
        errCode = ErrorMessages.ERROR_AUDIO;
        break;
      case SpeechRecognizer.ERROR_CLIENT:
        errCode = ErrorMessages.ERROR_CLIENT;
        break;
      case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
        errCode = ErrorMessages.ERROR_INSUFFICIENT_PERMISSIONS;
        break;
      case SpeechRecognizer.ERROR_NETWORK:
        errCode = ErrorMessages.ERROR_NETWORK;
        break;
      case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
        errCode = ErrorMessages.ERROR_NETWORK_TIMEOUT;
        break;
      case SpeechRecognizer.ERROR_NO_MATCH:
        errCode = ErrorMessages.ERROR_NO_MATCH;
        break;
      case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
        errCode = ErrorMessages.ERROR_RECOGNIZER_BUSY;
        break;
      case SpeechRecognizer.ERROR_SERVER:
        errCode = ErrorMessages.ERROR_SERVER;
        break;
      case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
        errCode = ErrorMessages.ERROR_SPEECH_TIMEOUT;
        break;
    }
    return errCode;
  }
}
