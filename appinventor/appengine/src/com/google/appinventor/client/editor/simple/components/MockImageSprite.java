// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLengthPropertyEditor;

/**
 * Mock ImageSprite component.
 *
 */
public final class MockImageSprite extends MockImageBase implements MockSprite {

  /**
   * Component type name.
   */
  public static final String TYPE = "ImageSprite";

  public static final String PROPERTY_NAME_U = "OriginX";
  public static final String PROPERTY_NAME_V = "OriginY";
  public static final String PROPERTY_NAME_MARKORIGIN = "MarkOrigin";

  // The x-y coordinates of the origin of the image sprite
  private int xOrigin;
  private int yOrigin;

  // The unit coordinates of the origin wrt top-left corner
  private double v;
  private double u;

  /**
   * Creates a new MockImageSprite component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockImageSprite(SimpleEditor editor) {
    super(editor, TYPE, images.imageSprite());
  }

  @Override
  protected void addWidthHeightProperties() {
    // Percent based size will scale images strangely, so remove percent based sizes
    addProperty(PROPERTY_NAME_WIDTH, "" + LENGTH_PREFERRED, MESSAGES.widthPropertyCaption(),
        "Appearance", MESSAGES.ImageSprite__WidthPropertyDescriptions(),
        new YoungAndroidLengthPropertyEditor(false));
    addProperty(PROPERTY_NAME_HEIGHT, "" + LENGTH_PREFERRED, MESSAGES.heightPropertyCaption(),
        "Appearance", MESSAGES.ImageSprite__HeightPropertyDescriptions(),
        new YoungAndroidLengthPropertyEditor(false));
  }

  private void setXProperty(String text) {
    try {
      xOrigin = (int) Math.round(Double.parseDouble(text));
    } catch (NumberFormatException e) {
      // Don't change value if unparseable (should not happen).
    }
    refreshCanvas();
  }
  
  private void setYProperty(String text) {
    try {
      yOrigin = (int) Math.round(Double.parseDouble(text));
    } catch (NumberFormatException e) {
      // Don't change value if unparseable (should not happen).
    }
    refreshCanvas();
  }

  private void setZProperty(String text) {
    refreshCanvas();
  }

  private void setUProperty(String text) {
    try {
      u = Double.parseDouble(text);
    } catch (NumberFormatException e) {
      // Don't change value if unparseable (should not happen).
    }
    refreshCanvas();
  }

  private void setVProperty(String text) {
    try {
      v = Double.parseDouble(text);
    } catch (NumberFormatException e) {
      // Don't change value if unparseable (should not happen).
    }
    refreshCanvas();
  }

  private String getUFromOrigin(String text) {
    return text.substring(1, text.indexOf(","));
  }

  private String getVFromOrigin(String text) {
    return text.substring(text.indexOf(",") + 2, text.length() - 1);
  }

  /**
   * Converts a double in the range of [0.0, 1.0] to a string. If the double is an integer value,
   * then the returned value will be specified to two significant figures. Otherwise, the double
   * will be converted to a string using the default conversion.
   *
   * @param d the double to convert
   * @return the string representation of the double
   */
  private static String doubleToString(double d) {
    return d == 0 ? "0.0" : d == 1 ? "1.0" : Double.toString(d);
  }

  private void setOriginProperty(double u, double v) {
    // format as string of type (u, v)
    String s = "(" + doubleToString(u) + ", " + doubleToString(v) + ")";
    changeProperty(PROPERTY_NAME_MARKORIGIN, s);
  }

  private void refreshCanvas() {
    MockCanvas mockCanvas = (MockCanvas) getContainer();
    // mockCanvas will be null for the MockImageSprite on the palette
    if (mockCanvas != null) {
      mockCanvas.reorderComponents(this);
    }
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
    if (propertyName.equals(PROPERTY_NAME_Z)) {
      setZProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_X)) {
      setXProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_Y)) {
      setYProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_U)) {
      setUProperty(newValue);
      setOriginProperty(u, v);
    } else if (propertyName.equals(PROPERTY_NAME_V)) {
      setVProperty(newValue);
      setOriginProperty(u, v);
    } else if (propertyName.equals(PROPERTY_NAME_MARKORIGIN)) {
      if (hasProperty(PROPERTY_NAME_U)) {  // Not true when project initializing
        changeProperty(PROPERTY_NAME_U, getUFromOrigin(newValue));
      }
      if (hasProperty(PROPERTY_NAME_V)) {  // Not true when project initializing
        changeProperty(PROPERTY_NAME_V, getVFromOrigin(newValue));
      }
    }
  }

  @Override
  public int getLeftX() {
    return xOrigin - getXOffset();
  }

  @Override
  public int getTopY() {
    return yOrigin - getYOffset();
  }

  @Override
  public int getXOffset() {
    int width = Integer.parseInt(getPropertyValue(PROPERTY_NAME_WIDTH));
    // if set to automatic or fill parent get the width from the image resource
    if (width < 0) {
      width = getPreferredWidth();
    }
    return (int) Math.round(width * u);
  }

  @Override
  public int getYOffset() {
    int height = Integer.parseInt(getPropertyValue(PROPERTY_NAME_HEIGHT));
    // if set to automatic or fill parent get the height from the image resource
    if (height < 0) {
      height = getPreferredHeight();
    }
    return (int) Math.round(height * v);
  }
}
