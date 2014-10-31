// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.properties.json;

import com.google.appinventor.server.properties.json.ServerJsonParser;
import com.google.appinventor.shared.storage.StorageUtil;

import junit.framework.TestCase;

/**
 * Tests JSONUtil.

 * @author lizlooney@google.com (Liz Looney)
 */
public class JSONUtilTest extends TestCase {
  private static final JSONParser JSON_PARSER = new ServerJsonParser();

  public void testStringToJson() throws Exception {
    checkSaveAndLoad("hello world");
  }

  public void testStringToJsonWithI18nCharacters() throws Exception {
    // Pet the Kitty with i18n characters
    checkSaveAndLoad("\u1e56\u00e9\u1e97 \u0167\u1e27\u00eb \u1e32\u01d0\u1e6b\u1e71\u1e99");

    // Here are some reported by customers:
    checkSaveAndLoad("scandinavian letters \u00e6, \u00f8, \u00e5");
    checkSaveAndLoad("Would be nice to have some \u00e4\u00e4\u00e4");
    checkSaveAndLoad("Need \u00c5,\u00c4,\u00d6");
    checkSaveAndLoad("We're l\u00f8ving \u00e5pp-invent\u00f6r but d\u00e6sp\u00e6r\u00e4tely " +
        "need \u00e6\u00f8\u00e5\u00f6\u00e4");
    checkSaveAndLoad("Korean string \ud55c\uad6d\uc5b4");
    checkSaveAndLoad("We need \u00e5, \u00e4 and \u00f6!");
  }

  private void checkSaveAndLoad(String original) throws Exception {
    byte[] bytes = save(original);
    assertEquals(original, load(bytes));
  }

  private static byte[] save(String propertyValue) throws Exception {
    String json = "{\"Text\":" + JSONUtil.toJson(propertyValue) + "}";
    return json.getBytes(StorageUtil.DEFAULT_CHARSET);
  }

  private static String load(byte[] bytes) throws Exception {
    String json = new String(bytes, StorageUtil.DEFAULT_CHARSET);
    JSONValue jsonValue = JSON_PARSER.parse(json);
    assertTrue(jsonValue instanceof JSONObject);
    JSONValue textValue = ((JSONObject) jsonValue).get("Text");
    assertTrue(textValue instanceof JSONString);
    return ((JSONString) textValue).getString();
  }
}
