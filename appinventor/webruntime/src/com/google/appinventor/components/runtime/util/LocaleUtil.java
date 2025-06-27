package com.google.appinventor.components.runtime.util;

import com.google.gwt.core.client.GWT;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import java.util.Locale;

public class LocaleUtil {
  public interface LocaleResources extends ClientBundle {
    @Source("language-codes.plist")
    TextResource languageCodes();

    @Source("iso3166_1_2_to_iso3166_1_3.plist")
    TextResource countryCodes();
  }

  static final JavaScriptObject iso3Languages;
  static final JavaScriptObject iso3Countries;

  static {
    // Load the resources to ensure they are available.
    LocaleResources resources = GWT.create(LocaleResources.class);
    iso3Languages = plistToMap(resources.languageCodes().getText());
    iso3Countries = plistToMap(resources.countryCodes().getText());
  }

  public static Locale iso3LanguageToLocale(String iso3Language) {
    return lookup(iso3Languages, iso3Language) != null
        ? new Locale(lookup(iso3Languages, iso3Language))
        : Locale.getDefault();
  }

  public static Locale iso3CountryToLocale(String iso3Country) {
    return lookup(iso3Countries, iso3Country) != null
        ? new Locale("", lookup(iso3Countries, iso3Country))
        : Locale.getDefault();
  }

  public static native String iso2CountryToIso3(String iso2Country) /*-{
    // Convert ISO 3166-1 alpha-2 to ISO 3166-1 alpha-3.
    var map = @com.google.appinventor.components.runtime.util.LocaleUtil::iso3Countries;
    return map[iso2Country.toUpperCase()] || iso2Country;
  }-*/;

  public static native String iso3LanguageToIso2(String iso3language) /*-{
    // Convert ISO 639-2 to ISO 639-1.
    var map = @com.google.appinventor.components.runtime.util.LocaleUtil::iso3Languages;
    return map[iso3language.toLowerCase()] || iso3language;
  }-*/;

  private static native String lookup(JavaScriptObject map, String key) /*-{
    return map[key] || null;
  }-*/;

  private static native JavaScriptObject plistToMap(String plist) /*-{
    var parser = new DOMParser();
    var doc = parser.parseFromString(plist, "application/xml");
    var root = doc.getElementsByTagName('dict')[0];
    var map = {};
    for (var i = 0; i < root.children.length; i += 2) {
      map[root.children[i].textContent] = root.children[i + 1].textContent;
    }
    return map;
  }-*/;
}
