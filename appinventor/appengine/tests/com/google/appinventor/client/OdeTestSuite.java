// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.appinventor.client.settings.GeneralSettingsTestCase;
import com.google.gwt.junit.tools.GWTTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

public class OdeTestSuite extends GWTTestSuite {
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for App Inventor");
    suite.addTestSuite(OdeTestCase.class);
    suite.addTestSuite(GeneralSettingsTestCase.class);
    return suite;
  }
}