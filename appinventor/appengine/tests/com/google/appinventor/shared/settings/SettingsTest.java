// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.settings;

import com.google.appinventor.server.properties.json.ServerJsonParser;

import junit.framework.TestCase;

/**
 * Checks functionality of the read-only settings.
 *
 * @see Settings
 *
 */
public class SettingsTest extends TestCase {

  private Settings settings;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    settings = new Settings(new ServerJsonParser(), "{\"category\":{\"setting\":\"value\"}}");
  }

  /**
   * Tests reading the settings.
   *
   * @see Settings#getSetting(String, String)
   */
  public void testGetSettings() {
    assertEquals("value", settings.getSetting("category", "setting"));

    assertNull(settings.getSetting("unknown_category", "setting"));

    assertNull(settings.getSetting("category", "unknown_setting"));

    try {
      settings.getSetting(null, "setting");
      fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      settings.getSetting("category", null);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }
}
