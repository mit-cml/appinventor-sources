// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import java.util.Map;

/**
 * Formats i18n templates containing named placeholders such as {name}.
 */
public final class I18nFormatter {
  private I18nFormatter() {
  }

  public static String format(String template, Map<String, String> values) {
    if (template == null || template.length() == 0) {
      return "";
    }
    if (values == null || values.isEmpty()) {
      return template;
    }

    StringBuilder result = new StringBuilder();
    int index = 0;

    while (index < template.length()) {
      int start = template.indexOf('{', index);
      if (start == -1) {
        result.append(template.substring(index));
        break;
      }

      int end = template.indexOf('}', start + 1);
      if (end == -1) {
        result.append(template.substring(index));
        break;
      }

      result.append(template.substring(index, start));

      String name = template.substring(start + 1, end);
      String value = values.get(name);
      if (value == null) {
        result.append(template.substring(start, end + 1));
      } else {
        result.append(value);
      }

      index = end + 1;
    }

    return result.toString();
  }
}
