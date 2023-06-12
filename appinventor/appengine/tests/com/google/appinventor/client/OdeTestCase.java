// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client;

import com.google.gwt.junit.client.GWTTestCase;

public class OdeTestCase extends GWTTestCase {
  public void testCompareLocales() {
    assertTrue("Handles default case one is null",
        Ode.compareLocales(null, "en", "en"));
    assertTrue("Handles both cases being null",
        Ode.compareLocales(null, null, "en"));
    assertTrue("Handles when both cases are the same",
        Ode.compareLocales("en", "en", "en"));
    assertFalse("Handles when the cases are different",
        Ode.compareLocales("en", "fr_FR", "en"));
    assertFalse("Handles when the default is different",
        Ode.compareLocales(null, "fr_FR", "en"));
  }

  public void testhandleUserLocale() {
    boolean value = Ode.compareLocales(null, "en", "en");
    assertEquals(value, Ode.handleUserLocale());
  }

  @Override
  public String getModuleName() {
    return "com.google.appinventor.YaClient";
  }
}
