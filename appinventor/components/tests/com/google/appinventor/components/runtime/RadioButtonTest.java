// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for RadioButton component.
 *
 * @author thamihardik8@gmail.com (Hardik Thami)
 */
public class RadioButtonTest extends ToggleTestBase {

  private RadioButton aRadiobutton;

  @Before
  public void SetUp() {
    super.setUp();
    aRadiobutton = new RadioButton(getForm());
    aToggle = aRadiobutton;
  }

  @Test
  public void testRadiobuttonSharedDefaults() {
    testToggleDefaults();
    assertFalse(aRadiobutton.Checked());
  }

  @Test
  public void testRadiobuttonSharedProperties() {
    testTogglelProperties();
  }

  @Test
  public void testRadiobuttonEnabled() {
    testToggleEnabled();
  }

  @Test
  public void testRadiobuttonChecked() {
    aRadiobutton.Checked(false);
    assertFalse(aRadiobutton.Checked());

    aRadiobutton.Checked(true);
    assertTrue(aRadiobutton.Checked());
  }

  @Test
  public void testRadiobuttonToggle() {
    aRadiobutton.Checked(false);
    assertFalse(aRadiobutton.Checked());

    aRadiobutton.Toggle();
    assertTrue(aRadiobutton.Checked());

    aRadiobutton.Toggle();
    assertTrue(aRadiobutton.Checked());
  }

}