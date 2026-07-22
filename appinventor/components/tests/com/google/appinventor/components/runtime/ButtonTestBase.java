// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;


import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class ButtonTestBase extends RobolectricTestBase {

  ButtonBase aButtonBase;

  @Test
  public void testButtonBaseDefaults() {
    assertEquals("Expected default background color NONE" + Component.COLOR_DEFAULT, Component.COLOR_DEFAULT, aButtonBase.BackgroundColor());
    assertEquals("Expected default Typeface " + Component.TYPEFACE_DEFAULT, Component.TYPEFACE_DEFAULT, aButtonBase.FontTypeface());
    assertTrue(aButtonBase.Enabled());
    assertEquals(Component.FONT_DEFAULT_SIZE, aButtonBase.FontSize(), 0.0);
    assertEquals("Expected default Text Color " + Component.COLOR_DEFAULT, Component.COLOR_DEFAULT, aButtonBase.TextColor());
  }

  public void testButtonBaseProperties() {
    aButtonBase.BackgroundColor(Component.COLOR_BLUE);
    assertEquals("Invalid Switch background color", Component.COLOR_BLUE, aButtonBase.BackgroundColor());

    aButtonBase.FontSize(22.02f);
    assertEquals(22.02f, aButtonBase.FontSize(), 0.0f);

    aButtonBase.FontTypeface(Component.TYPEFACE_SERIF);
    assertEquals("Invalid Font Typeface", Component.TYPEFACE_SERIF, aButtonBase.FontTypeface());

    aButtonBase.Shape(Component.BUTTON_SHAPE_RECT);
    assertEquals("Invalid Shape", Component.BUTTON_SHAPE_RECT, aButtonBase.Shape());

    aButtonBase.ShowFeedback(true);
    assertTrue(aButtonBase.ShowFeedback());

    aButtonBase.TextAlignment(Component.ALIGNMENT_CENTER);
    assertEquals("Invalid Text Alignment", Component.ALIGNMENT_CENTER, aButtonBase.TextAlignment());

    aButtonBase.TextColor(Component.COLOR_RED);
    assertEquals("Invalid Text Color", Component.COLOR_RED, aButtonBase.TextColor());
  }

  public void testButtonBaseEnabled() {
    aButtonBase.Enabled(false);
    assertFalse(aButtonBase.Enabled());

    aButtonBase.Enabled(true);
    assertTrue(aButtonBase.Enabled());
  }

  public void testButtonBaseVisible() {
    aButtonBase.Visible(false);
    assertFalse(aButtonBase.Visible());

    aButtonBase.Visible(true);
    assertTrue(aButtonBase.Visible());
  }

  public void testButtonBaseShowFeedback() {
    aButtonBase.ShowFeedback(true);
    assertTrue(aButtonBase.ShowFeedback());

    aButtonBase.ShowFeedback(false);
    assertFalse(aButtonBase.ShowFeedback());
  }

  public void testButtonBaseFontBold() {
    aButtonBase.FontBold(true);
    assertTrue(aButtonBase.FontBold());

    aButtonBase.FontBold(false);
    assertFalse(aButtonBase.FontBold());
  }

  public void testButtonBaseFontItalic() {
    aButtonBase.FontItalic(true);
    assertTrue(aButtonBase.FontItalic());

    aButtonBase.FontItalic(false);
    assertFalse(aButtonBase.FontItalic());
  }

}
