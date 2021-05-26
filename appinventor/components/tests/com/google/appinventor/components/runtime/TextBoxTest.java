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
 * Test class for TextBox component.
 */
public class TextBoxTest extends RobolectricTestBase {

  private TextBox aTextBox;
  private EditText editText;

  @Before
  public void SetUp() {
    super.setUp();
    aTextBox = new TextBox(getForm());
    editText = (EditText) aTextBox.getView();
  }
  
  @Test
  public void testTextBoxDefaults() {
    // Test Default TextAlignment Property
    assertEquals("Expected default TextBox TextAlignment", Gravity.LEFT | Gravity.TOP, editText.getGravity());
  
    // Test Default Enabled Property
    assertTrue(editText.isEnabled());
  
    // Test Default FontTypeFace Property
    assertEquals("Expected default TextBox FontTypeface", Typeface.DEFAULT, editText.getTypeface());
  
    // Test Default FontSize Property
    assertEquals(Component.FONT_DEFAULT_SIZE, editText.getTextSize(), 0.0);
  
    // Test Default TextColor Property
    assertEquals("Expected TextBox TextColor", -16777216, editText.getTextColors().getDefaultColor());
    
    // Test Default NumberOnly Property
    assertEquals("Invalid TextBox NumberOnly", InputType.TYPE_CLASS_TEXT, editText.getInputType());
    
    // Test Default MultiLine Property
    assertEquals("Invalid TextBox MultiLine", 1, editText.getLineCount());
    
    // Test Default ReadOnly Property
    assertTrue(editText.isEnabled());
    
    assertEquals("Expected default TextBox Text", "", editText.getText().toString());
    assertEquals("Expected default TextBox Hint", "", editText.getHint().toString());
  }
  
  @Test
  public void testBackgroundColor() {
    aTextBox.BackgroundColor(Component.COLOR_BLUE);
    assertEquals("Invalid TextBox background color", Component.COLOR_BLUE, ((ColorDrawable)editText.getBackground()).getColor());
  }
  
  @Test
  public void testEnabled() {
    aTextBox.Enabled(true);
    assertTrue(editText.isEnabled());
    
    aTextBox.Enabled(false);
    assertFalse(editText.isEnabled());
  }
  
  @Test
  public void testFontBold() {
    aTextBox.FontBold(true);
    assertEquals("Invalid TextBox FontBold", Typeface.BOLD, editText.getTypeface().getStyle());
    
    aTextBox.FontBold(false);
    assertEquals("Invalid TextBox FontBold", Typeface.NORMAL , editText.getTypeface().getStyle());
  }
  
  @Test
  public void testFontItalic() {
    aTextBox.FontItalic(true);
    assertEquals("Invalid TextBox FontItalic", Typeface.ITALIC, editText.getTypeface().getStyle());
    
    aTextBox.FontItalic(false);
    assertEquals("Invalid TextBox FontItalic", Typeface.NORMAL , editText.getTypeface().getStyle());
  }
  
  @Test
  public void testFontSize() {
    aTextBox.FontSize(22.02f);
    assertEquals(22.02f, editText.getTextSize(), 0.0f);
  }
  
  @Test
  public void testFontTypeface() {
    aTextBox.FontTypeface(Component.TYPEFACE_MONOSPACE);
    assertEquals("Invalid TextBox FontTypeface", Typeface.MONOSPACE, editText.getTypeface());
  }
  
  @Test
  public void testText() {
    aTextBox.Text("MIT App Inventor");
    assertEquals("Invalid TextBox Text", "MIT App Inventor", editText.getText().toString());
  }
  
  @Test
  public void testHint() {
    aTextBox.Hint("Testing Hint");
    assertEquals("Invalid TextBox Hint", "Testing Hint", editText.getHint().toString());
  }
  
  @Test
  public void testMultiLine() {
    aTextBox.MultiLine(true);
    aTextBox.Text("MIT App Inventor \nis The Best !!!");
    assertEquals("Invalid TextBox MultiLine", 2, editText.getLineCount());
    
    aTextBox.MultiLine(false);
    aTextBox.Text("MIT App Inventor \nis The Best !!!");
    assertEquals("Invalid TextBox MultiLine", 1, editText.getLineCount());
  }
  
  @Test
  public void testNumbersOnly() {
    aTextBox.NumbersOnly(true);
    assertEquals("Invalid TextBox NumberOnly", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED |InputType.TYPE_NUMBER_FLAG_DECIMAL, editText.getInputType());
    
    aTextBox.NumbersOnly(false);
    assertEquals("Invalid TextBox NumberOnly", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, editText.getInputType());
  }
  
  @Test
  public void testReadOnly() {
    aTextBox.ReadOnly(true);
    assertFalse(editText.isEnabled());
    
    aTextBox.ReadOnly(false);
    assertTrue(editText.isEnabled());
  }
  
  @Test
  public void testTextAlignment() {
    aTextBox.TextAlignment(Component.ALIGNMENT_CENTER);
    assertEquals("Invalid TextBox TextAlignment", Gravity.CENTER_HORIZONTAL | Gravity.TOP, editText.getGravity());
    
    aTextBox.TextAlignment(Component.ALIGNMENT_NORMAL);
    assertEquals("Invalid TextBox TextAlignment", Gravity.LEFT | Gravity.TOP, editText.getGravity());
  }
  
  @Test
  public void testTextColor() {
    aTextBox.TextColor(Component.COLOR_BLUE);
    assertEquals("Invalid TextBox TextColor", Component.COLOR_BLUE, editText.getTextColors().getDefaultColor());
  }
  
  @Test
  public void testVisible() {
    aTextBox.Visible(true);
    assertEquals("Invalid TextBox Visibility", View.VISIBLE, editText.getVisibility());
    
    aTextBox.Visible(false);
    assertEquals("Invalid TextBox Visibility", View.GONE, editText.getVisibility());
  }
  
  @Test
  public void testHeightPercent() {
    aTextBox.HeightPercent(30);
    assertEquals("Invalid TextBox HeightPercent", 30.0d, ((double) editText.getHeight()/getForm().Height()) * 100.0d, 0.999d);
  }
  
  @Test
  public void testWidthPercent() {
    aTextBox.WidthPercent(22);
    assertEquals("Invalid TextBox WidthPercent", 22.0d, ((double) editText.getWidth()/getForm().Width()) * 100.0d, 0.999d);
  }
}
