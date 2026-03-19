// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.
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
 * Test class for Switch component.
 *
 * @author srlane@mit.edu (Susan Rati Lane)
 */
public class CheckboxTest extends ToggleTestBase {

  private CheckBox aCheckbox;

  @Before
  public void SetUp() {
    super.setUp();
    aCheckbox = new CheckBox(getForm());
    aToggle = aCheckbox;
  }

  @Test
  public void testCheckboxSharedDefaults() {
    testToggleDefaults();
    assertFalse(aCheckbox.Checked());
  }

  @Test
  public void testCheckboxSharedProperties() {
    testTogglelProperties();
  }

  @Test
  public void testCheckboxEnabled() {
    testToggleEnabled();
  }

  @Test
  public void testCheckboxChecked() {
    aCheckbox.Checked(false);
    assertFalse(aCheckbox.Checked());

    aCheckbox.Checked(true);
    assertTrue(aCheckbox.Checked());
  }

  @Test
  public void testCheckboxCheckedColorDefault() {
    // Default checked color should be green
    assertEquals(Component.COLOR_GREEN, aCheckbox.CheckedColor());
  }

  @Test
  public void testCheckboxUncheckedColorDefault() {
    // Default unchecked color should be light gray
    assertEquals(Component.COLOR_LTGRAY, aCheckbox.UncheckedColor());
  }

  @Test
  public void testCheckboxCheckedColorCustom() {
    // Test setting custom checked color
    aCheckbox.CheckedColor(Component.COLOR_BLUE);
    assertEquals(Component.COLOR_BLUE, aCheckbox.CheckedColor());
    
    aCheckbox.CheckedColor(Component.COLOR_RED);
    assertEquals(Component.COLOR_RED, aCheckbox.CheckedColor());
  }

  @Test
  public void testCheckboxUncheckedColorCustom() {
    // Test setting custom unchecked color
    aCheckbox.UncheckedColor(Component.COLOR_YELLOW);
    assertEquals(Component.COLOR_YELLOW, aCheckbox.UncheckedColor());
    
    aCheckbox.UncheckedColor(Component.COLOR_CYAN);
    assertEquals(Component.COLOR_CYAN, aCheckbox.UncheckedColor());
  }

  @Test
  public void testCheckboxBothColorsCustom() {
    // Test setting both colors together
    aCheckbox.CheckedColor(Component.COLOR_MAGENTA);
    aCheckbox.UncheckedColor(Component.COLOR_ORANGE);
    
    assertEquals(Component.COLOR_MAGENTA, aCheckbox.CheckedColor());
    assertEquals(Component.COLOR_ORANGE, aCheckbox.UncheckedColor());
  }

}
