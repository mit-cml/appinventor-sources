/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package java.util;

import com.google.appinventor.components.runtime.util.LocaleUtil;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * A very simple emulation of Locale for shared-code patterns like
 * {@code String.toUpperCase(Locale.US)}.
 * <p>
 * Note: Any changes to this class should put into account the assumption that was made in rest of
 * the JRE emulation.
 */
public class Locale {

  private String country;
  private String language;
  private JavaScriptObject nativeLocale;

  public static final Locale ROOT = new Locale() {
    @Override
    public String toString() {
      return "";
    }
  };

  public static final Locale ENGLISH = new Locale() {
    @Override
    public String toString() {
      return "en";
    }
  };

  public static final Locale US = new Locale() {
    @Override
    public String toString() {
      return "en_US";
    }
  };

  /**
   * Returns an instance that represents the browser's default locale (not necessarily the one
   * defined by 'gwt.locale').
   */
  public static native Locale getDefault() /*-{
    var locale = new Intl.Locale(navigator.language || navigator.userLanguage || 'en-US');
    return new @java.util.Locale::make(*)(locale.language, locale.region);
  }-*/;

  public static Locale[] getAvailableLocales() {
    return new Locale[] { ROOT, ENGLISH, US };
  }

  public String getISO3Country() {
    if (country == null) {
      return "";
    }
    // For simplicity, we assume that the country is always in ISO 3166-1 alpha-3 format.
    return country;
  }

  public String getCountry() {
    if (country == null) {
      return "";
    }
    // For simplicity, we assume that the country is always in ISO 3166-1 alpha-2 format.
    return country.toUpperCase(Locale.ENGLISH);
  }

  public String getISO3Language() {
    // For simplicity, we assume that the language is always in ISO 639-2 format.
    return language;
  }

  public String getLanguage() {
    // For simplicity, we assume that the language is always in ISO 639-1 format.
    return language;
  }

  public Locale(String language) {
    this(language, null);
  }

  public Locale(String language, String country) {
    this.language = language;
    this.country = country;
    initNativeLocale();
  }

  private native void initNativeLocale() /*-{
    var language = this.@java.util.Locale::language;
    var country = this.@java.util.Locale::country;
    if (!country) {
      this.@java.util.Locale::nativeLocale = new Intl.Locale(language);
    } else {
      this.@java.util.Locale::nativeLocale = new Intl.Locale(language, { region: country });
    }
  }-*/;

  // Hidden as we don't support manual creation of Locales.
  private Locale() { }

  private static Locale make(String language, String country) {
    if (language == null && country == null) {
      return ROOT;
    }
    if (language == null) {
      language = "";
    }
    if (country == null) {
      country = "";
    }
    return new Locale(language, country);
  }

  public native String toString() /*-{
    return this.@java.util.Locale::nativeLocale.toString();
  }-*/;
}
