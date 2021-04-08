package com.google.appinventor.components.runtime;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for TextBox component.
 */
public class TextBoxTest extends RobolectricTestBase {

  private TextBox aTextBox;

  @Before
  public void SetUp() {
    super.setUp();
    aTextBox = new TextBox(getForm());
  }

  @Test
  public void testTextBoxDefaults() {
    assertEquals("Expected default TextBox text alignment " + Component.ALIGNMENT_NORMAL, Component.ALIGNMENT_NORMAL, aTextBox.TextAlignment());
    assertTrue(aTextBox.Enabled());
    assertEquals("Expected default TextBox font typeface " + Component.TYPEFACE_DEFAULT, Component.TYPEFACE_DEFAULT, aTextBox.FontTypeface());
    assertEquals(Component.FONT_DEFAULT_SIZE, aTextBox.FontSize(), 0.0);
    assertEquals("Expected TextBox text color", Component.COLOR_DEFAULT, aTextBox.TextColor());
    assertFalse(aTextBox.FontBold());
    assertFalse(aTextBox.FontItalic());
    assertFalse(aTextBox.NumbersOnly());
    assertFalse(aTextBox.MultiLine());
    assertFalse(aTextBox.ReadOnly());
    assertEquals("Expected default TextBox text", "", aTextBox.Text());
    assertEquals("Expected default TextBox hint", "", aTextBox.Hint());
  }

  @Test
  public void testBackgroundColor() {
    aTextBox.BackgroundColor(Component.COLOR_BLUE);
    assertEquals("Invalid TextBox background color", Component.COLOR_BLUE, aTextBox.BackgroundColor());
  }

  @Test
  public void testEnabled() {
    aTextBox.Enabled(true);
    assertTrue(aTextBox.Enabled());

    aTextBox.Enabled(false);
    assertFalse(aTextBox.Enabled());
  }

  @Test
  public void testFontBold() {
    aTextBox.FontBold(true);
    assertTrue(aTextBox.FontBold());

    aTextBox.FontBold(false);
    assertFalse(aTextBox.FontBold());
  }

  @Test
  public void testFontItalic() {
    aTextBox.FontItalic(true);
    assertTrue(aTextBox.FontItalic());

    aTextBox.FontItalic(false);
    assertFalse(aTextBox.FontItalic());
  }

  @Test
  public void testFontSize() {
    aTextBox.FontSize(22.02f);
    assertEquals(22.02f, aTextBox.FontSize(), 0.0f);
  }

  @Test
  public void testFontTypeface() {
    aTextBox.FontTypeface(Component.TYPEFACE_MONOSPACE);
    assertEquals("Invalid TextBox font typeface", Component.TYPEFACE_MONOSPACE, aTextBox.FontTypeface());
  }

  @Test
  public void testHint() {
    aTextBox.Hint("MIT App Inventor TextBox Hint");
    assertEquals("Invalid TextBox text", "MIT App Inventor TextBox Hint", aTextBox.Hint());
  }

  @Test
  public void testMultiLine() {
    aTextBox.MultiLine(true);
    assertTrue(aTextBox.MultiLine());

    aTextBox.MultiLine(false);
    assertFalse(aTextBox.MultiLine());
  }

  @Test
  public void testNumbersOnly() {
    aTextBox.NumbersOnly(true);
    assertTrue(aTextBox.NumbersOnly());

    aTextBox.NumbersOnly(false);
    assertFalse(aTextBox.NumbersOnly());
  }

  @Test
  public void testReadOnly() {
    aTextBox.ReadOnly(true);
    assertTrue(aTextBox.ReadOnly());

    aTextBox.ReadOnly(false);
    assertFalse(aTextBox.ReadOnly());
  }

  @Test
  public void testText() {
    aTextBox.Text("MIT App Inventor");
    assertEquals("Invalid TextBox text", "MIT App Inventor", aTextBox.Text());
  }

  @Test
  public void testTextAlignment() {
    aTextBox.TextAlignment(Component.ALIGNMENT_CENTER);
    assertEquals("Invalid TextBox text alignment", Component.ALIGNMENT_CENTER, aTextBox.TextAlignment());
  }

  @Test
  public void testTextColor() {
    aTextBox.TextColor(Component.COLOR_BLUE);
    assertEquals("Invalid TextBox text color", Component.COLOR_BLUE, aTextBox.TextColor());
  }

  @Test
  public void testVisible() {
    aTextBox.Visible(true);
    assertTrue(aTextBox.Visible());

    aTextBox.Visible(false);
    assertFalse(aTextBox.Visible());
  }
}