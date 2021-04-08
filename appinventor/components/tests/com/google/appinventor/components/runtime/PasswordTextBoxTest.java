package com.google.appinventor.components.runtime;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for PasswordTextBox component.
 */
public class PasswordTextBoxTest extends RobolectricTestBase {

  private PasswordTextBox aPasswordTextBox;

  @Before
  public void SetUp() {
    super.setUp();
    aPasswordTextBox = new PasswordTextBox(getForm());
  }

  @Test
  public void testPasswordTextBoxDefaults() {
    assertEquals("Expected default PasswordTextBox text alignment " + Component.ALIGNMENT_NORMAL, Component.ALIGNMENT_NORMAL, aPasswordTextBox.TextAlignment());
    assertTrue(aPasswordTextBox.Enabled());
    assertEquals("Expected default PasswordTextBox font typeface " + Component.TYPEFACE_DEFAULT, Component.TYPEFACE_DEFAULT, aPasswordTextBox.FontTypeface());
    assertEquals(Component.FONT_DEFAULT_SIZE, aPasswordTextBox.FontSize(), 0.0);
    assertFalse(aPasswordTextBox.FontBold());
    assertFalse(aPasswordTextBox.FontItalic());
    assertEquals("Expected PasswordTextBox text color", Component.COLOR_DEFAULT, aPasswordTextBox.TextColor());
    assertEquals("Expected default PasswordTextBox text", "", aPasswordTextBox.Text());
    assertEquals("Expected default PasswordTextBox hint", "", aPasswordTextBox.Hint());
  }

  @Test
  public void testBackgroundColor() {
    aPasswordTextBox.BackgroundColor(Component.COLOR_BLUE);
    assertEquals("Invalid PasswordTextBox background color", Component.COLOR_BLUE, aPasswordTextBox.BackgroundColor());
  }

  @Test
  public void testEnabled() {
    aPasswordTextBox.Enabled(true);
    assertTrue(aPasswordTextBox.Enabled());

    aPasswordTextBox.Enabled(false);
    assertFalse(aPasswordTextBox.Enabled());
  }

  @Test
  public void testFontBold() {
    aPasswordTextBox.FontBold(true);
    assertTrue(aPasswordTextBox.FontBold());

    aPasswordTextBox.FontBold(false);
    assertFalse(aPasswordTextBox.FontBold());
  }

  @Test
  public void testFontItalic() {
    aPasswordTextBox.FontItalic(true);
    assertTrue(aPasswordTextBox.FontItalic());

    aPasswordTextBox.FontItalic(false);
    assertFalse(aPasswordTextBox.FontItalic());
  }

  @Test
  public void testFontSize() {
    aPasswordTextBox.FontSize(22.02f);
    assertEquals(22.02f, aPasswordTextBox.FontSize(), 0.0f);
  }

  @Test
  public void testFontTypeface() {
    aPasswordTextBox.FontTypeface(Component.TYPEFACE_MONOSPACE);
    assertEquals("Invalid PasswordTextBox font typeface", Component.TYPEFACE_MONOSPACE, aPasswordTextBox.FontTypeface());
  }

  @Test
  public void testHint() {
    aPasswordTextBox.Hint("MIT App Inventor PasswordTextBox Hint");
    assertEquals("Invalid PasswordTextBox text", "MIT App Inventor PasswordTextBox Hint", aPasswordTextBox.Hint());
  }

  @Test
  public void testText() {
    aPasswordTextBox.Text("MIT App Inventor");
    assertEquals("Invalid PasswordTextBox text", "MIT App Inventor", aPasswordTextBox.Text());
  }

  @Test
  public void testTextAlignment() {
    aPasswordTextBox.TextAlignment(Component.ALIGNMENT_CENTER);
    assertEquals("Invalid PasswordTextBox text alignment", Component.ALIGNMENT_CENTER, aPasswordTextBox.TextAlignment());
  }

  @Test
  public void testTextColor() {
    aPasswordTextBox.TextColor(Component.COLOR_BLUE);
    assertEquals("Invalid PasswordTextBox text color", Component.COLOR_BLUE, aPasswordTextBox.TextColor());
  }

  @Test
  public void testVisible() {
    aPasswordTextBox.Visible(true);
    assertTrue(aPasswordTextBox.Visible());

    aPasswordTextBox.Visible(false);
    assertFalse(aPasswordTextBox.Visible());
  }
}