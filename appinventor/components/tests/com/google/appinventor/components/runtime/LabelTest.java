// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;


import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for Label component.
 */
public class LabelTest extends RobolectricTestBase {

  private Label aLabel;
  private TextView textView;

  @Before
  public void SetUp() {
    super.setUp();
    aLabel = new Label(getForm());
    textView = (TextView) aLabel.getView();
  }

  @Test
  public void testLabelDefaults() {
    assertEquals("Expected default Label text alignment " + Component.ALIGNMENT_NORMAL, Gravity.LEFT | Gravity.TOP, textView.getGravity());
    assertEquals("Expected default Label background color " + Component.COLOR_NONE, Component.COLOR_NONE, ((ColorDrawable)textView.getBackground()).getColor());
    assertEquals("Expected default Label font typeface " + Component.TYPEFACE_DEFAULT, Component.TYPEFACE_DEFAULT, aLabel.FontTypeface());
    assertEquals(Component.FONT_DEFAULT_SIZE, textView.getTextSize(), 0.0);
    assertEquals("Expected Label text color in light theme", Component.COLOR_BLACK, textView.getTextColors().getDefaultColor());

    // Test Label text color in dark theme
    // Default text color of text in dark theme is Component.COLOR_WHITE
    // Component.COLOR_WHITE = -16777216
    // signed integers are stored as two's complements to their respective positive value.
    getForm().Theme("AppTheme");
    assertEquals("Expected Label text color in dark theme", -16777216, textView.getTextColors().getDefaultColor());
    assertFalse(aLabel.HTMLFormat());
    assertTrue(aLabel.HasMargins());
  }

  @Test
  public void testBackgroundColor() {
    aLabel.BackgroundColor(Component.COLOR_BLUE);
    assertEquals("Invalid Label background color", Component.COLOR_BLUE, ((ColorDrawable)textView.getBackground()).getColor());
  }

  @Test
  public void testFontBold() {
    aLabel.FontBold(true);
    assertEquals("Invalid Label font italic", Typeface.BOLD, textView.getTypeface().getStyle());

    aLabel.FontBold(false);
    assertEquals("Invalid Label font italic", Typeface.NORMAL , textView.getTypeface().getStyle());
  }

  @Test
  public void testFontItalic() {
    aLabel.FontItalic(true);
    assertEquals("Invalid Label font italic", Typeface.ITALIC, textView.getTypeface().getStyle());

    aLabel.FontItalic(false);
    assertEquals("Invalid Label font italic", Typeface.NORMAL , textView.getTypeface().getStyle());
  }

  @Test
  public void testFontSize() {
    aLabel.FontSize(22.02f);
    assertEquals(22.02f, textView.getTextSize(), 0.0f);
  }

  @Test
  public void testFontTypeface() {
    aLabel.FontTypeface(Component.TYPEFACE_MONOSPACE);
    assertEquals("Invalid Label font typeface", Typeface.MONOSPACE, textView.getTypeface());
  }

  @Test
  public void testHTMLFormat() {
    String htmlTest = "<h1>MIT</h1><h2>App Inventor</h2>";
    aLabel.Text(htmlTest);
    aLabel.HTMLFormat(false);
    assertEquals("Invalid Label HTML text", htmlTest, textView.getText());

    aLabel.HTMLFormat(true);
    assertTrue(aLabel.HTMLFormat());
    Html[] textViewSpans = new SpannableString(textView.getText()).getSpans(0, textView.getText().length(), Html.class);
    Html[] defaultSpans = new SpannableString(htmlTest).getSpans(0, textView.getText().length(), Html.class);
    assertArrayEquals("Invalid Label HTML text", defaultSpans, textViewSpans);
  }

  @Test
  public void testHasMargin() {
    aLabel.HasMargins(true);
    assertTrue(aLabel.HasMargins());
    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) textView.getLayoutParams();
    int defaultMargin = Math.round(2 * getForm().deviceDensity());
    int[] defaultMargins = {defaultMargin, defaultMargin, defaultMargin, defaultMargin};
    int[] expectedMargins = {mlp.leftMargin, mlp.topMargin, mlp.rightMargin, mlp.bottomMargin};
    assertArrayEquals("Invalid Label margin", defaultMargins, expectedMargins);

    aLabel.HasMargins(false);
    assertFalse(aLabel.HasMargins());
    defaultMargins = new int[]{0, 0, 0, 0};
    expectedMargins = new int[]{mlp.leftMargin, mlp.topMargin, mlp.rightMargin, mlp.bottomMargin};
    assertArrayEquals("Invalid Label margin", defaultMargins, expectedMargins);
  }

  @Test
  public void testText() {
    aLabel.Text("MIT App Inventor");
    assertEquals("Invalid Label text", "MIT App Inventor", textView.getText());
  }

  @Test
  public void testTextAlignment() {
    aLabel.TextAlignment(Component.ALIGNMENT_CENTER);
    assertEquals("Invalid Label text alignment", Gravity.CENTER_HORIZONTAL | Gravity.TOP, textView.getGravity());

    aLabel.TextAlignment(Component.ALIGNMENT_NORMAL);
    assertEquals("Invalid Label text alignment", Gravity.LEFT | Gravity.TOP, textView.getGravity());
  }

  @Test
  public void testTextColor() {
    aLabel.TextColor(Component.COLOR_BLUE);
    assertEquals("Invalid Label text color", Component.COLOR_BLUE, textView.getTextColors().getDefaultColor());
  }

  @Test
  public void testVisible() {
    aLabel.Visible(true);
    assertEquals("Invalid Label visibility", View.VISIBLE, textView.getVisibility());

    aLabel.Visible(false);
    assertEquals("Invalid Label visibility", View.GONE, textView.getVisibility());
  }

  @Test
  public void testHeightPercent() {
    aLabel.HeightPercent(30);
    assertEquals("Invalid Label height percent", 30.0d, ((double) textView.getHeight()/getForm().Height()) * 100.0d, 0.999d);
  }

  @Test
  public void testWidthPercent() {
    aLabel.WidthPercent(22);
    assertEquals("Invalid Label width percent", 22.0d, ((double) textView.getWidth()/getForm().Width()) * 100.0d, 0.999d);
  }
  
}
