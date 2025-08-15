// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FormPropertiesAnalyzerTest {

  private static final String TEST_DATA =
      "#|\n$JSON\n{\"Properties\":{\"$Type\":\"Form\",\"$Components\":[]}}\n|#\n";

  @Test
  public void testParseSourceFileValid() {
    JSONObject data = FormPropertiesAnalyzer.parseSourceFile(TEST_DATA);
    assertNotNull(data.optJSONObject("Properties"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseSourceFileMissingHeader() {
    FormPropertiesAnalyzer.parseSourceFile("\n|#\n");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseSourceFileMissingFooter() {
    FormPropertiesAnalyzer.parseSourceFile("#|\n$JSON\n");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseSourceFileInvalidJSON() {
    FormPropertiesAnalyzer.parseSourceFile("#|\n$JSON\n{]\n|#\n");
  }

  @Test
  public void testParseSourceFileWindowsLineEndings() {
    JSONObject data = FormPropertiesAnalyzer.parseSourceFile(TEST_DATA.replaceAll("\n", "\r\n"));
    assertNotNull(data.optJSONObject("Properties"));
  }

  @Test
  public void testGetComponentTypesFromFormFile() {
    Set<String> result = FormPropertiesAnalyzer.getComponentTypesFromFormFile(TEST_DATA);
    assertEquals(1, result.size());
    assertTrue(result.contains("Form"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetComponentTypesFromFormFileThrows() {
    FormPropertiesAnalyzer.getComponentTypesFromFormFile("#|\n$JSON\n{}\n|$\n");
  }
}
