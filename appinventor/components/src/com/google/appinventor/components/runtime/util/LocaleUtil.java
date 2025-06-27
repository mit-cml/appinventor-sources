package com.google.appinventor.components.runtime.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

public class LocaleUtil {
  private static final Map<String, Locale> iso3LanguageToLocaleMap = new HashMap<>();
  private static final Map<String, Locale> iso3CountryToLocaleMap = new HashMap<>();

  static {
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

  public static Locale iso3LanguageToLocale(String iso3Language) {
    Locale mappedLocale = iso3LanguageToLocaleMap.get(iso3Language);
    if (mappedLocale == null) {
      // Language codes should be lower case, but maybe the user doesn't know that.
      mappedLocale = iso3LanguageToLocaleMap.get(iso3Language.toLowerCase(Locale.ENGLISH));
    }
    return mappedLocale == null ? Locale.getDefault() : mappedLocale;
  }

  public static Locale iso3CountryToLocale(String iso3Country) {
    Locale mappedLocale = iso3CountryToLocaleMap.get(iso3Country);
    if (mappedLocale == null) {
      // Country codes should be upper case, but maybe the user doesn't know that.
      mappedLocale = iso3CountryToLocaleMap.get(iso3Country.toUpperCase(Locale.ENGLISH));
    }
    return mappedLocale == null ? Locale.getDefault() : mappedLocale;
  }

}
