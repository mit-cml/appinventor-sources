// -*- mode: java; c-basic-offset: 2; -*-
package com.google.appinventor.client.editor.youngandroid.i18n;

public final class TranslationEntry {
  private final String key;
  private final String screenName;
  private final String componentName;
  private final String componentType;
  private final String propertyName;
  private final String baseText;

  public TranslationEntry(String key, String screenName, String componentName,
      String componentType, String propertyName, String baseText) {
    this.key = key;
    this.screenName = screenName;
    this.componentName = componentName;
    this.componentType = componentType;
    this.propertyName = propertyName;
    this.baseText = baseText;
  }

  public String getKey() {
    return key;
  }

  public String getScreenName() {
    return screenName;
  }

  public String getComponentName() {
    return componentName;
  }

  public String getComponentType() {
    return componentType;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public String getBaseText() {
    return baseText;
  }
}
