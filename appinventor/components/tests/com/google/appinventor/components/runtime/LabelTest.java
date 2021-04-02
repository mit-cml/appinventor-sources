package com.google.appinventor.components.runtime;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for Label component.
 */
public class LabelTest extends RobolectricTestBase {

  private Label aLabel;

  @Before
  public void SetUp() {
    super.setUp();
    aLabel = new Label(getForm());
  }

  @Test
  public void testLabelDefaults() {
    assertEquals("Expected default Label text alignment " + Component.ALIGNMENT_NORMAL, Component.ALIGNMENT_NORMAL, aLabel.TextAlignment());
    assertEquals("Expected default Label background color " + Component.COLOR_NONE, Component.COLOR_NONE, aLabel.BackgroundColor());
    assertEquals("Expected default Label font typeface " + Component.TYPEFACE_DEFAULT, Component.TYPEFACE_DEFAULT, aLabel.FontTypeface());
    assertEquals(Component.FONT_DEFAULT_SIZE, aLabel.FontSize(), 0.0);
    assertEquals("Expected Label text color", Component.COLOR_DEFAULT, aLabel.TextColor());
    assertFalse(aLabel.HTMLFormat());
    assertTrue(aLabel.HasMargins());
  }

  @Test
  public void testBackgroundColor() {
    aLabel.BackgroundColor(Component.COLOR_BLUE);
    assertEquals("Invalid Label background color", Component.COLOR_BLUE, aLabel.BackgroundColor());
  }

  @Test
  public void testFontBold() {
    aLabel.FontBold(true);
    assertTrue(aLabel.FontBold());

    aLabel.FontBold(false);
    assertFalse(aLabel.FontBold());
  }

  @Test
  public void testFontItalic() {
    aLabel.FontItalic(true);
    assertTrue(aLabel.FontItalic());

    aLabel.FontItalic(false);
    assertFalse(aLabel.FontItalic());
  }

  @Test
  public void testFontSize() {
    aLabel.FontSize(22.02f);
    assertEquals(22.02f, aLabel.FontSize(), 0.0f);
  }

  @Test
  public void testFontTypeface() {
    aLabel.FontTypeface(Component.TYPEFACE_MONOSPACE);
    assertEquals("Invalid Label font typeface", Component.TYPEFACE_MONOSPACE, aLabel.FontTypeface());
  }

  @Test
  public void testHTMLFormat() {
    aLabel.HTMLFormat(true);
    assertTrue(aLabel.HTMLFormat());

    aLabel.HTMLFormat(false);
    assertFalse(aLabel.HTMLFormat());
  }

  @Test
  public void testHasMargin() {
    aLabel.HasMargins(true);
    assertTrue(aLabel.HasMargins());

    aLabel.HasMargins(false);
    assertFalse(aLabel.HasMargins());
  }

  @Test
  public void testText() {
    aLabel.Text("MIT App Inventor");
    assertEquals("Invalid Label text", "MIT App Inventor", aLabel.Text());
  }

  @Test
  public void testTextAlignment() {
    aLabel.TextAlignment(Component.ALIGNMENT_CENTER);
    assertEquals("Invalid Label text alignment", Component.ALIGNMENT_CENTER, aLabel.TextAlignment());
  }

  @Test
  public void testTextColor() {
    aLabel.TextColor(Component.COLOR_BLUE);
    assertEquals("Invalid Label text color", Component.COLOR_BLUE, aLabel.TextColor());
  }

  @Test
  public void testVisible() {
    aLabel.Visible(true);
    assertTrue(aLabel.Visible());

    aLabel.Visible(false);
    assertFalse(aLabel.Visible());
  }
}
