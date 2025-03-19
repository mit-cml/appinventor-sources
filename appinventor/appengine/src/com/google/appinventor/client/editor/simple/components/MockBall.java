// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.widgetideas.graphics.client.Color;
import com.google.gwt.widgetideas.graphics.client.GWTCanvas;

/**
 * Mock Ball component.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class MockBall extends MockVisibleComponent implements MockSprite {

  /**
   * Component type name.
   */
  public static final String TYPE = "Ball";

  private static final String PROPERTY_NAME_RADIUS = "Radius";
  private static final String PROPERTY_NAME_PAINTCOLOR = "PaintColor";
  private static final String PROPERTY_NAME_ORIGIN_AT_CENTER = "OriginAtCenter";

  private static final int DEFAULT_RADIUS = 5;

  // Widget for showing the mock ball
  private final SimplePanel ballWidget;
  private final GWTCanvas canvas;

  private int radius = DEFAULT_RADIUS;
  private int diameter = 2 * radius;
  private Color color = Color.BLACK;
  private boolean originAtCenter = false;

  int x;
  int y;

  /**
   * Creates a new MockBall component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockBall(SimpleEditor editor) {
    super(editor, TYPE, images.ball());

    // Initialize mock ball UI
    ballWidget = new SimplePanel();
    ballWidget.setStylePrimaryName("ode-SimpleMockComponent");

    // Create an appropriately sized ball
    canvas = new GWTCanvas(diameter, diameter);
    canvas.setPixelSize(diameter, diameter);
    canvas.setBackgroundColor(GWTCanvas.TRANSPARENT);
    fillCircle();
    ballWidget.setWidget(canvas);

    initComponent(ballWidget);
  }

  // Drawing

  private void fillCircle() {
    canvas.clear();
    canvas.setFillStyle(color);
    canvas.beginPath();
    canvas.arc(radius, radius, radius, 0, Math.PI * 2, true);
    canvas.fill();
  }

  // Handling property changes

  private void setRadiusProperty(String text) {
    try {
      radius = Integer.parseInt(text);
      diameter = 2 * radius;
      canvas.setCoordSize(diameter, diameter);
      canvas.setPixelSize(diameter, diameter);
      fillCircle();
      if (originAtCenter) {
        // Force the position of the ballWidget to be adjusted relative
        // to the parent canvas.
        refreshCanvas();
      }
    } catch (NumberFormatException e) {
      // Ignore this. If we throw an exception here, the project is unrecoverable.
    }
  }

  private void setColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFF000000";  // black
    }
    color = MockComponentsUtil.getColor(text);
    fillCircle();
  }

  private void refreshCanvas() {
    MockCanvas mockCanvas = (MockCanvas) getContainer();
    // mockCanvas will be null for the MockBall on the palette
    if (mockCanvas != null) {
      mockCanvas.reorderComponents(this); //refreshForm();
    }
  }

  private void setXProperty(String text) {
    try {
      x = (int) Math.round(Double.parseDouble(text));
    } catch (NumberFormatException e) {
      // Don't change value if unparseable (should not happen).
    }
    refreshCanvas();
  }
  
  private void setYProperty(String text) {
    try {
      y = (int) Math.round(Double.parseDouble(text));
    } catch (NumberFormatException e) {
      // Don't change value if unparseable (should not happen).
    }
    refreshCanvas();
  }

  private void setZProperty(String text) {
    MockCanvas mockCanvas = (MockCanvas) getContainer();
    // mockCanvas will be null for the MockBall on the palette
    if (mockCanvas != null) {
      mockCanvas.reorderComponents(this);
    }
  }

  private void setOriginAtCenterProperty(String text) {
    originAtCenter = Boolean.valueOf(text);
    refreshCanvas();
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_WIDTH) ||
        propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      return false;
    }
    return super.isPropertyVisible(propertyName);
  }

  @Override
  public int getPreferredWidth() {
    // The superclass uses getOffsetWidth, which won't work for us.
    return diameter;
  }

  @Override
  public int getPreferredHeight() {
    // The superclass uses getOffsetHeight, which won't work for us.
    return diameter;
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
    if (propertyName.equals(PROPERTY_NAME_RADIUS)) {
      setRadiusProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_PAINTCOLOR)) {
      setColorProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_Z)) {
      setZProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_X)) {
      setXProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_Y)) {
      setYProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_ORIGIN_AT_CENTER)) {
      setOriginAtCenterProperty(newValue);
    }
  }

  @Override
  public int getLeftX() {
    return x - getXOffset();
  }

  @Override
  public int getTopY() {
    return y - getYOffset();
  }

  @Override
  public int getXOffset() {
    return originAtCenter ? radius : 0;
  }

  @Override
  public int getYOffset() {
    return originAtCenter ? radius : 0;
  }
}
