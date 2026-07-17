// -*- mode: java; c-basic-offset: 2; -*-
package com.google.appinventor.client.editor.youngandroid.i18n;

/**
 * Temporary key generator for the first i18n UI slice.
 *
 * The user-facing UI can show screen/component/property names, including Unicode names.
 * Internally, we generate a safe ASCII key so that later Android/iOS resource generation
 * does not depend on user-created names being valid identifiers.
 */
public final class TranslationKeyGenerator {
  private static final String PREFIX = "i18n_";

  private TranslationKeyGenerator() {
  }

  public static String generate(String screenName, String componentName, String propertyName) {
    String source = safe(screenName) + "\u0000"
        + safe(componentName) + "\u0000"
        + safe(propertyName);
    return PREFIX + Integer.toHexString(source.hashCode());
  }

  private static String safe(String value) {
    return value == null ? "" : value;
  }
}
