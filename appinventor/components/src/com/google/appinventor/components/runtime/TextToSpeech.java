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
    category = ComponentCategory.MISC,
    nonVisible = true,
    iconName = "images/textToSpeech.png")
@SimpleObject
public class TextToSpeech extends AndroidNonvisibleComponent
    implements Component, OnStopListener, OnResumeListener {

  private static final Map<String, Locale> iso3LanguageToLocaleMap = Maps.newHashMap();
  private static final Map<String, Locale> iso3CountryToLocaleMap = Maps.newHashMap();

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
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
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
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
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
    tts.onStop();
  }

  @Override
  public void onResume() {
    tts.onResume();
  }
}
