package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;

import java.util.ArrayList;

public class IntentBasedSpeechRecognizer extends SpeechRecognizerController
        implements ActivityResultListener{

  private String result;
  private ComponentContainer container;
  private SpeechListener speechListener;
  private Intent recognizerIntent;

  /* Used to identify the call to startActivityForResult. Will be passed back
     into the resultReturned() callback method. */
  private int requestCode;

  public IntentBasedSpeechRecognizer(ComponentContainer container, Intent recognizerIntent) {
    this.container = container;
    this.recognizerIntent = recognizerIntent;
  }

  @Override
  void addListener(SpeechListener speechListener) {
    this.speechListener = speechListener;
  }

  @Override
  void start() {
    if(requestCode == 0) {
      requestCode = container.$form().registerForActivityResult(this);
    }
    container.$context().startActivityForResult(recognizerIntent, requestCode);
  }

  @Override
  void stop() {
    //implementation not available
  }



  @Override
  public void resultReturned(int requestCode, int resultCode, Intent data) {
    if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
      if (data.hasExtra(RecognizerIntent.EXTRA_RESULTS)) {
        ArrayList<String> results = data.getExtras().getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
        result = results.get(0);
        speechListener.onResult(result);
      } else {
        result = "";
      }
    }
  }
}
