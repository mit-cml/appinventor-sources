// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;


import org.junit.Before;
import org.junit.Test;


/**
 * Test class for Button component.
 */
public class ButtonTest extends ButtonTestBase {

  private Button aButton;

  @Before
  public void SetUp() {
    super.setUp();
    aButton = new Button(getForm());
    aButtonBase = aButton;
  }

  @Test
  public void testButtonSharedDefaults() {
    testButtonBaseFontBold();
  }

  @Test
  public void testButtonSharedProperties() {
    testButtonBaseProperties();
  }

  @Test
  public void testButtonEnabled() {
    testButtonBaseEnabled();
  }

  @Test
  public void testButtonVisible() {
    testButtonBaseVisible();
  }

  @Test
  public void testButtonShowFeedback() {
    testButtonBaseShowFeedback();
  }

  @Test
  public void testButtonFontBold() {
    testButtonBaseFontBold();
  }

  @Test
  public void testButtonFontItalic() {
    testButtonBaseFontItalic();
  }
}
