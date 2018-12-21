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
public class SwitchTest extends ToggleTestBase {

  private Switch aSwitch;

  @Before
  public void setUp() {
    super.setUp();
    aSwitch = new Switch(getForm());
    aToggle = aSwitch;
  }

  @Test
  public void testSwitchSharedDefaults() {
    testToggleDefaults();
  }

  @Test
  public void testSwitchDefaults() {
    assertEquals("Invalid default Thumb Color Active", Component.COLOR_WHITE, aSwitch.ThumbColorActive());
    assertEquals("Invalid default Thumb Color Inactive", Component.COLOR_LTGRAY, aSwitch.ThumbColorInactive());
    assertEquals("Invalid default Track Color Active", Component.COLOR_GREEN, aSwitch.TrackColorActive());
    assertEquals("Invalid default Track Color Inactive", Component.COLOR_GRAY, aSwitch.TrackColorInactive());
    aSwitch.On(false);
  }

  @Test
  public void testSwitchColors() {
    aSwitch.ThumbColorActive(Component.COLOR_MAGENTA);
    assertEquals("Invalid Thumb Color Active", Component.COLOR_MAGENTA, aSwitch.ThumbColorActive());

    aSwitch.ThumbColorInactive(Component.COLOR_BLACK);
    assertEquals("Invalid Thumb Color Inactive", Component.COLOR_BLACK, aSwitch.ThumbColorInactive());

    aSwitch.TrackColorActive(Component.COLOR_PINK);
    assertEquals("Invalid Track Color Active", Component.COLOR_PINK, aSwitch.TrackColorActive());

    aSwitch.TrackColorInactive(Component.COLOR_CYAN);
    assertEquals("Invalid Track Color Inactive", Component.COLOR_CYAN, aSwitch.TrackColorInactive());
  }

  @Test
  public void testSwitchOn() {
    aSwitch.On(false);
    assertFalse(aSwitch.On());

    aSwitch.On(true);
    assertTrue(aSwitch.On());
  }

  @Test
  public void testSwitchSharedProperties() {
    testTogglelProperties();
  }

  @Test
  public void testSwitchEnabled() {
    testToggleEnabled();
  }
}
