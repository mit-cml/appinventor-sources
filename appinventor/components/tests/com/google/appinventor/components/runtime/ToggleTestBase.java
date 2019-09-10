// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;


import com.google.appinventor.components.runtime.shadows.org.osmdroid.tileprovider.modules.ShadowMapTileModuleProviderBase;
import com.google.appinventor.components.runtime.shadows.org.osmdroid.views.ShadowMapView;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class ToggleTestBase extends RobolectricTestBase {

  ToggleBase aToggle;

  @Test
  public void testToggleDefaults() {
    assertEquals("Expected default background color NONE",Component.COLOR_NONE, aToggle.BackgroundColor());
    assertEquals("Expected default Typeface " + Component.TYPEFACE_DEFAULT,Component.TYPEFACE_DEFAULT, aToggle.FontTypeface());
    assertTrue(aToggle.Enabled());
    assertEquals(Component.FONT_DEFAULT_SIZE, aToggle.FontSize(), 0.0);
    assertEquals("Expected default Text Color " + Component.COLOR_DEFAULT, Component.COLOR_DEFAULT, aToggle.TextColor());
  }

  public void testTogglelProperties() {
    aToggle.BackgroundColor(Component.COLOR_ORANGE);
    assertEquals("Invalid Switch background color", Component.COLOR_ORANGE, aToggle.BackgroundColor());

    aToggle.FontTypeface(Component.TYPEFACE_MONOSPACE);
    assertEquals("Invalid Font Typeface", Component.TYPEFACE_MONOSPACE, aToggle.FontTypeface());

    aToggle.FontSize(12.0f);
    assertEquals(12.0f, aToggle.FontSize(), 0.0f);

    aToggle.TextColor(Component.COLOR_RED);
    assertEquals("Invalid Text Color", Component.COLOR_RED, aToggle.TextColor());
  }

  public void testToggleEnabled() {
    aToggle.Enabled(false);
    assertFalse(aToggle.Enabled());

    aToggle.Enabled(true);
    assertTrue(aToggle.Enabled());
  }
}
