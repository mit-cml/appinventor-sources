// Copyright 2011 Google. All Rights Reserved.
package com.google.appinventor.components.runtime.util;

import java.util.Locale;

/**
 * This abstracts out what a text to speech implementation needs to have.  In particular we use
 * this to enable having an implementation that uses the internal Android TextToSpeech library for
 * post-Donut devices and uses an externally installed library for the earlier devices.
 *
 * @author markf@google.com (Mark Friedman)
 */
public interface ITextToSpeech {

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
   * Speak the given message corresponding to the language and country of the given locale
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

}
