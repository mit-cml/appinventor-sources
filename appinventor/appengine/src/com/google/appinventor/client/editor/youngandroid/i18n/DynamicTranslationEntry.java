// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.i18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DynamicTranslationEntry {
  private final String key;
  private final String baseText;
  private final List<String> placeholders;

  public DynamicTranslationEntry(String key, String baseText, List<String> placeholders) {
    this.key = key == null ? "" : key;
    this.baseText = baseText == null ? "" : baseText;

    if (placeholders == null) {
      this.placeholders = new ArrayList<String>();
    } else {
      this.placeholders = new ArrayList<String>(placeholders);
    }
  }

  public String getKey() {
    return key;
  }

  public String getBaseText() {
    return baseText;
  }

  public List<String> getPlaceholders() {
    return Collections.unmodifiableList(placeholders);
  }
}
