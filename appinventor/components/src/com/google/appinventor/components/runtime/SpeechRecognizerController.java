package com.google.appinventor.components.runtime;

abstract class SpeechRecognizerController {
  abstract void start();
  abstract void stop();
  abstract void addListener(SpeechListener speechListener);
}