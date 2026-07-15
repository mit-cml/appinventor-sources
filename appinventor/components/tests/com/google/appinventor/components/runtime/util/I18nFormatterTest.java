// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class I18nFormatterTest {
  @Test
  public void replacesNamedPlaceholders() {
    Map<String, String> values = new HashMap<String, String>();
    values.put("name", "Akash");
    values.put("count", "5");

    assertEquals("Hello Akash, you have 5 messages.",
        I18nFormatter.format("Hello {name}, you have {count} messages.", values));
  }

  @Test
  public void allowsPlaceholderReorderingAcrossLanguages() {
    Map<String, String> values = new HashMap<String, String>();
    values.put("name", "Akash");
    values.put("count", "5");

    assertEquals("Akash, आपके पास 5 संदेश हैं।",
        I18nFormatter.format("{name}, आपके पास {count} संदेश हैं।", values));
  }

  @Test
  public void keepsMissingPlaceholdersVisible() {
    Map<String, String> values = new HashMap<String, String>();
    values.put("name", "Akash");

    assertEquals("Hello Akash, you have {count} messages.",
        I18nFormatter.format("Hello {name}, you have {count} messages.", values));
  }

  @Test
  public void leavesMalformedTemplateUnchangedFromErrorPoint() {
    Map<String, String> values = new HashMap<String, String>();
    values.put("name", "Akash");

    assertEquals("Hello {name",
        I18nFormatter.format("Hello {name", values));
  }

  @Test
  public void returnsTemplateWhenValuesAreEmpty() {
    assertEquals("Hello {name}", I18nFormatter.format("Hello {name}", null));
  }
}
