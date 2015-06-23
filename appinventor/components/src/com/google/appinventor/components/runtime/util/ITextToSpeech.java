// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime.util;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

/**
 * This abstracts out what a text to speech implementation needs to have.  In particular we use
 * this to enable having an implementation that uses the internal Android TextToSpeech library for
 * post-Donut devices and uses an externally installed library for the earlier devices.
 *
 * @author markf@google.com (Mark Friedman)
 */
public interface ITextToSpeech {

  /**
   * Callback specifying methods for successful and failed attempts to generate speech.
   */
  interface TextToSpeechCallback {

    /**
     * Callback to be invoked when a message has finished being spoken.
     */
    public void onSuccess();

    /**
     * Callback to be invoked when we have a failure to communicate.
     */
    public void onFailure();
  }

  /**
   * Speaks the given message corresponding to the language and country of the given locale.
   * @param message the message to speak
   * @param loc the locale to use
   */
  public void speak(String message, Locale loc);

  /**
   * This will be called when the Activity is stopped, to give us a chance to cleanup resources,
   * if necessary.
   */
  public void onStop();

  /**
   * This will be called when the Activity is resumed, to give us a chance to re-initialize
   * resources, if necessary.
   */
  public void onResume();


  /**
   * This will be called when the Activity is destroyed, to give us a chance to cleanup resources,
   * if necessary.
   */
  public void onDestroy();

  /**
   * Sets the speech pitch for the TextToSpeech
   * @param pitch 1.0 is the normal pitch, lower values lower the tone of the synthesized voice,
   *              greater values increase it.
   */
  public void setPitch(float pitch);

  /**
   * Sets the speech rate
   * @param speechRate Speech rate. 1.0 is the normal speech rate, lower values slow down the
   *                   speech (0.5 is half the normal speech rate), greater values
   *                   accelerate it (2.0 is twice the normal speech rate).
   */
  public void setSpeechRate(float speechRate);

  public int isLanguageAvailable(Locale loc);

  public boolean isInitialized() ;

}
