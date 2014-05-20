// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.collect.Maps;
import com.google.appinventor.components.runtime.util.ExternalTextToSpeech;
import com.google.appinventor.components.runtime.util.ITextToSpeech;
import com.google.appinventor.components.runtime.util.InternalTextToSpeech;
import com.google.appinventor.components.runtime.util.SdkLevel;

import android.media.AudioManager;
import android.util.Log;

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

/**
 * Component for converting text to speech using either the built-in TTS library or the
 * TextToSpeech Extended library (which must be pre-installed for Android versions earlier
 * than Donut).
 *
 * @author markf@google.com (Mark Friedman)
 */
@DesignerComponent(version = YaVersion.TEXTTOSPEECH_COMPONENT_VERSION,
    description = "Component for using TextToSpeech to speak a message",
    category = ComponentCategory.MEDIA,
    nonVisible = true,
    iconName = "images/textToSpeech.png")
@SimpleObject
public class TextToSpeech extends AndroidNonvisibleComponent
    implements Component, OnStopListener, OnResumeListener, OnDestroyListener {

  private static final Map<String, Locale> iso3LanguageToLocaleMap = Maps.newHashMap();
  private static final Map<String, Locale> iso3CountryToLocaleMap = Maps.newHashMap();
  private float pitch = 1.0f;
  private float speechRate = 1.0f;
  private static final String LOG_TAG = "TextToSpeech";

  static {
    initLocaleMaps();
  }

  private boolean result;
  private String language;
  private String country;

  private final ITextToSpeech tts;
  private String iso2Language;
  private String iso2Country;

  private static void initLocaleMaps() {
    Locale[] locales = Locale.getAvailableLocales();
    for (Locale locale : locales) {
      try {
        String iso3Country = locale.getISO3Country();
        if (iso3Country.length() > 0) {
          iso3CountryToLocaleMap.put(iso3Country, locale);
        }
      } catch (MissingResourceException e) {
        // ignore;
      }
      try {
        String iso3Language = locale.getISO3Language();
        if (iso3Language.length() > 0) {
          iso3LanguageToLocaleMap.put(iso3Language, locale);
        }
      } catch (MissingResourceException e) {
        // ignore;
      }
    }
  }

  /**
   * Creates a TextToSpeech component.
   *
   * @param container container, component will be placed in
   */
  public TextToSpeech(ComponentContainer container) {
    super(container.$form());
    result = false;
    Language("");
    Country("");
    /* Determine which TTS library to use */
    boolean useExternalLibrary = SdkLevel.getLevel() < SdkLevel.LEVEL_DONUT;
    Log.v(LOG_TAG, "Using " + (useExternalLibrary ? "external" : "internal") + " TTS library.");
    ITextToSpeech.TextToSpeechCallback callback = new ITextToSpeech.TextToSpeechCallback() {
      @Override
      public void onSuccess() {
        result = true;
        AfterSpeaking(true);
      }

      @Override
      public void onFailure() {
        result = false;
        AfterSpeaking(false);
      }
    };
    tts = useExternalLibrary ? new ExternalTextToSpeech(container, callback)
                             : new InternalTextToSpeech(container.$context(), callback);
    // Set up listeners
    form.registerForOnStop(this);
    form.registerForOnResume(this);
    form.registerForOnDestroy(this);

    // Make volume buttons control media, not ringer.
    form.setVolumeControlStream(AudioManager.STREAM_MUSIC);
  }

  /**
   * Result property getter method.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public boolean Result() {
    return result;
  }

  /**
   * Sets the language for this TextToSpeech component.
   *
   * @param language is the ISO2 (i.e. 2 letter) or ISO3 (i.e. 3 letter) language code to set this
   * TextToSpeech component to.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Sets the language for TextToSpeech")
  public void Language(String language) {
    Locale locale;
    switch (language.length()) {
      case 3:
        locale = iso3LanguageToLocale(language);
        this.language = locale.getISO3Language();
        break;
      case 2:
        locale = new Locale(language);
        this.language = locale.getLanguage();
        break;
      default:
        locale = Locale.getDefault();
        this.language = locale.getLanguage();
        break;
    }
    iso2Language = locale.getLanguage();
  }

  private static Locale iso3LanguageToLocale(String iso3Language) {
    Locale mappedLocale = iso3LanguageToLocaleMap.get(iso3Language);
    if (mappedLocale == null) {
      // Language codes should be lower case, but maybe the user doesn't know that.
      mappedLocale = iso3LanguageToLocaleMap.get(iso3Language.toLowerCase(Locale.ENGLISH));
    }
    return mappedLocale == null ? Locale.getDefault() : mappedLocale;
  }

  /**
     * Sets the speech pitch for the TextToSpeech. 1.0 is the normal pitch, lower values lower the tone of
     * the synthesized voice, greater values increase it.
     *
     * @param pitch a pitch level between 0 and 2
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "1.0")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Sets the Pitch for tts. The values should " +
            "be between 0 and 2 where lower values lower the tone of synthesized voice and greater values " +
            "increases it")
    public void Pitch(float pitch) {
        if (pitch < 0 || pitch > 2) {
            Log.i(LOG_TAG, "Pitch value should be between 0 and 2, but user specified: " + pitch);
            return;
        }

        this.pitch = pitch;

        /* Lowest pitch value should be > 0. If 0, we just set to .1f
         * Rather than having user specify .1, we just check and if 0, we set to .1
         */
        tts.setPitch(pitch==0?.1f:pitch);
    }

    /**
     * Reports the current value of speech pitch
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns current value of Pitch")
    public float Pitch() {
        return this.pitch;
    }

    /**
     * Sets the speech rate
     *
     * @param speechRate Speech rate 1.0 is the normal speech rate, lower values slow down the
     *                   speech (0.5 is half the normal speech rate), greater values
     *                   accelerate it (2.0 is twice the normal speech rate).
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "1.0")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Sets the SpeechRate for tts. " +
            "The values should be between 0 and 2 where lower values slow down the pitch and greater values " +
            "accelerate it")
    public void SpeechRate(float speechRate) {
        if (speechRate < 0 || speechRate > 2) {
            Log.i(LOG_TAG, "speechRate value should be between 0 and 2, but user specified: " + speechRate);
            return;
        }

        this.speechRate = speechRate;

        /* Lowest value should be > 0. If 0, we just set to .1f
         * Rather than having user specify .1, we just check and if 0, we set to .1
         */
        tts.setSpeechRate(speechRate == 0 ? .1f : speechRate);
    }

    /**
     * Reports the current value of speechRate
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Returns current value of SpeechRate")
    public float SpeechRate() {
        return this.speechRate;
  }

  /**
   * Gets the language for this TextToSpeech component.  This will be either an ISO2 (i.e. 2 letter)
   * or ISO3 (i.e. 3 letter) code depending on which kind of code the property was set with.
   *
   * @return the language code for this TextToSpeech component.
   */
  @SimpleProperty
  public String Language() {
    return language;
  }

  /**
   * Sets the country for this TextToSpeech component.
   *
   * @param country is the ISO2 (i.e. 2 letter) or ISO3 (i.e. 3 letter) country code to set this
   * TextToSpeech component to.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void Country(String country) {
    Locale locale;
    switch (country.length()) {
      case 3:
        locale = iso3CountryToLocale(country);
        this.country = locale.getISO3Country();
        break;
      case 2:
        locale = new Locale(country);
        this.country = locale.getCountry();
        break;
      default:
        locale = Locale.getDefault();
        this.country = locale.getCountry();
        break;
    }
    iso2Country = locale.getCountry();
  }

  private static Locale iso3CountryToLocale(String iso3Country) {
    Locale mappedLocale = iso3CountryToLocaleMap.get(iso3Country);
    if (mappedLocale == null) {
      // Country codes should be upper case, but maybe the user doesn't know that.
      mappedLocale = iso3CountryToLocaleMap.get(iso3Country.toUpperCase(Locale.ENGLISH));
    }
    return mappedLocale == null ? Locale.getDefault() : mappedLocale;
  }

  /**
   * Gets the country for this TextToSpeech component.  This will be either an ISO2 (i.e. 2 letter)
   * or ISO3 (i.e. 3 letter) code depending on which kind of code the property was set with.
   *
   * @return country code for this TextToSpeech component.
   */
  @SimpleProperty
  public String Country() {
    return country;
  }

  /**
   * Speaks the given message.
   */
  @SimpleFunction
  public void Speak(final String message) {
    BeforeSpeaking();
    final Locale loc = new Locale(iso2Language, iso2Country);
    tts.speak(message, loc);
  }


  /**
   * Event to raise when Speak is invoked, before the message is spoken.
   */
  @SimpleEvent
  public void BeforeSpeaking() {
    EventDispatcher.dispatchEvent(this, "BeforeSpeaking");
  }


  /**
   * Event to raise after the message is spoken.
   *
   * @param result whether the message was successfully spoken
   */
  @SimpleEvent
  public void AfterSpeaking(boolean result) {
    EventDispatcher.dispatchEvent(this, "AfterSpeaking", result);
  }

  @Override
  public void onStop() {
    // tts.onStop in fact does nothing, but we'll keep this onStop here for flexibility
    tts.onStop();
  }

  @Override
  public void onResume() {
    tts.onResume();
  }

  @Override
  public void onDestroy() {
    tts.onDestroy();
  }

}
