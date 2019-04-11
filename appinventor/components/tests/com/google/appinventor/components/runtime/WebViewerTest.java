// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WebViewerTest extends RobolectricTestBase {

  private WebViewer webViewer;

  @Before
  public void setUp() {
    super.setUp();
    webViewer = new WebViewer(getForm());
  }

  /**
   * Tests that setting a WebViewer's WebViewString property does not cause
   */
  @Test
  public void testWebViewStringChange() {
    final String TEST_STRING = "teststring";
    webViewer.WebViewString(TEST_STRING);
    runAllEvents();
    ShadowEventDispatcher.assertEventNotFired(webViewer, "WebViewStringChange");
    assertEquals(TEST_STRING, webViewer.WebViewString());
  }

}
