// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;


import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for PasswordTextBox component.
 */
public class PasswordTextBoxTest extends RobolectricTestBase {
  
  private PasswordTextBox aPasswordTextBox;
  private EditText editText;
  
  @Before
  public void SetUp() {
    super.setUp();
    aPasswordTextBox = new PasswordTextBox(getForm());
    editText = (EditText) aPasswordTextBox.getView();
  }
  
  @Test
  public void testPasswordTextBoxDefaults() {
    // Test Default TextAlignment Property
    assertEquals("Expected default PasswordTextBox TextAlignment", Gravity.LEFT | Gravity.TOP, editText.getGravity());
    
    // Test Default Enabled Property
    assertTrue(editText.isEnabled());
    
    // Test Default FontTypeFace Property
    assertEquals("Expected default PasswordTextBox FontTypeface", Typeface.DEFAULT, editText.getTypeface());
    
    // Test Default FontSize Property
    assertEquals(Component.FONT_DEFAULT_SIZE, editText.getTextSize(), 0.0);
    
    // Test Default TextColor Property
    assertEquals("Expected PasswordTextBox TextColor", -16777216, editText.getTextColors().getDefaultColor());
    
    assertEquals("Expected default PasswordTextBox Text", "", editText.getText().toString());
    assertEquals("Expected default PasswordTextBox Hint", "", editText.getHint().toString());
  }
  
  @Test
  public void testBackgroundColor() {
    aPasswordTextBox.BackgroundColor(Component.COLOR_BLUE);
    assertEquals("Invalid PasswordTextBox background color", Component.COLOR_BLUE, ((ColorDrawable)editText.getBackground()).getColor());
  }
  
  @Test
  public void testEnabled() {
    aPasswordTextBox.Enabled(true);
    assertTrue(editText.isEnabled());
    
    aPasswordTextBox.Enabled(false);
    assertFalse(editText.isEnabled());
  }
  
  @Test
  public void testFontBold() {
    aPasswordTextBox.FontBold(true);
    assertEquals("Invalid PasswordTextBox FontBold", Typeface.BOLD, editText.getTypeface().getStyle());
    
    aPasswordTextBox.FontBold(false);
    assertEquals("Invalid PasswordTextBox FontBold", Typeface.NORMAL , editText.getTypeface().getStyle());
  }
  
  @Test
  public void testFontItalic() {
    aPasswordTextBox.FontItalic(true);
    assertEquals("Invalid PasswordTextBox FontItalic", Typeface.ITALIC, editText.getTypeface().getStyle());
    
    aPasswordTextBox.FontItalic(false);
    assertEquals("Invalid PasswordTextBox FontItalic", Typeface.NORMAL , editText.getTypeface().getStyle());
  }
  
  @Test
  public void testFontSize() {
    aPasswordTextBox.FontSize(22.02f);
    assertEquals(22.02f, editText.getTextSize(), 0.0f);
  }
  
  @Test
  public void testFontTypeface() {
    aPasswordTextBox.FontTypeface(Component.TYPEFACE_MONOSPACE);
    assertEquals("Invalid PasswordTextBox FontTypeface", Typeface.MONOSPACE, editText.getTypeface());
  }
  
  @Test
  public void testText() {
    aPasswordTextBox.Text("MIT App Inventor");
    assertEquals("Invalid PasswordTextBox Text", "MIT App Inventor", editText.getText().toString());
  }
  
  @Test
  public void testHint() {
    aPasswordTextBox.Hint("Testing Hint");
    assertEquals("Invalid PasswordTextBox Hint", "Testing Hint", editText.getHint().toString());
  }
  
  @Test
  public void testTextAlignment() {
    aPasswordTextBox.TextAlignment(Component.ALIGNMENT_CENTER);
    assertEquals("Invalid PasswordTextBox TextAlignment", Gravity.CENTER_HORIZONTAL | Gravity.TOP, editText.getGravity());
    
    aPasswordTextBox.TextAlignment(Component.ALIGNMENT_NORMAL);
    assertEquals("Invalid PasswordTextBox TextAlignment", Gravity.LEFT | Gravity.TOP, editText.getGravity());
  }
  
  @Test
  public void testPasswordVisible() {
    aPasswordTextBox.PasswordVisible(true);
    aPasswordTextBox.NumbersOnly(true);
    assertEquals("Invalid PasswordTextBox PasswordVisible", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL, editText.getInputType());
  
    aPasswordTextBox.PasswordVisible(true);
    aPasswordTextBox.NumbersOnly(false);
    assertEquals("Invalid PasswordTextBox PasswordVisible", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD, editText.getInputType());
  
    aPasswordTextBox.PasswordVisible(false);
    aPasswordTextBox.NumbersOnly(true);
    assertEquals("Invalid PasswordTextBox PasswordVisible", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD, editText.getInputType());
  
    aPasswordTextBox.PasswordVisible(false);
    aPasswordTextBox.NumbersOnly(false);
    assertEquals("Invalid PasswordTextBox PasswordVisible", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, editText.getInputType());
  }
  
  @Test
  public void testTextColor() {
    aPasswordTextBox.TextColor(Component.COLOR_BLUE);
    assertEquals("Invalid PasswordTextBox TextColor", Component.COLOR_BLUE, editText.getTextColors().getDefaultColor());
  }
  
  @Test
  public void testVisible() {
    aPasswordTextBox.Visible(true);
    assertEquals("Invalid PasswordTextBox Visibility", View.VISIBLE, editText.getVisibility());
    
    aPasswordTextBox.Visible(false);
    assertEquals("Invalid PasswordTextBox Visibility", View.GONE, editText.getVisibility());
  }
  
  @Test
  public void testHeightPercent() {
    aPasswordTextBox.HeightPercent(30);
    assertEquals("Invalid PasswordTextBox HeightPercent", 30.0d, ((double) editText.getHeight()/getForm().Height()) * 100.0d, 0.999d);
  }
  
  @Test
  public void testWidthPercent() {
    aPasswordTextBox.WidthPercent(22);
    assertEquals("Invalid PasswordTextBox WidthPercent", 22.0d, ((double) editText.getWidth()/getForm().Width()) * 100.0d, 0.999d);
  }
}
