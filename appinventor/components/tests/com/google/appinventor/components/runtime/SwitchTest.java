// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017-2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;


import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.runtime.shadows.org.osmdroid.tileprovider.modules.ShadowMapTileModuleProviderBase;
import com.google.appinventor.components.runtime.shadows.org.osmdroid.views.ShadowMapView;
import com.google.appinventor.components.runtime.util.YailList;
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
@Config(shadows = {ShadowMapView.class, ShadowMapTileModuleProviderBase.class})

public class SwitchTest extends RobolectricTestBase {

  private Switch aSwitch;

  @Before
  public void setUp() {
    super.setUp();
    aSwitch = new Switch(getForm());
  }

  @Test
  public void testDefaults() {
    assertEquals("Expected default background color NONE",Component.COLOR_NONE, aSwitch.BackgroundColor());
    assertEquals("Expected default Typeface " + Component.TYPEFACE_DEFAULT,Component.TYPEFACE_DEFAULT, aSwitch.FontTypeface());
    assertTrue(aSwitch.Enabled());
    assertEquals("Expected default font size " + Component.FONT_DEFAULT_SIZE, Component.FONT_DEFAULT_SIZE, aSwitch.FontSize());
    assertEquals("Expected default Text Color " + Component.COLOR_DEFAULT, Component.COLOR_DEFAULT, aSwitch.TextColor());
    assertFalse(aSwitch.Checked());

    assertEquals("", Component.COLOR_WHITE, aSwitch.ThumbColorActive());
    assertEquals("", Component.COLOR_LTGRAY, aSwitch.ThumbColorInactive());
    assertEquals("", Component.COLOR_GREEN, aSwitch.TrackColorActive());
    assertEquals("", Component.COLOR_GRAY, aSwitch.TrackColorInactive());
  }

  @Test
  public void testSwitchColors()
  {

  }

}
