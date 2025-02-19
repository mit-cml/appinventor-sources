// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.IsColor;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.PaintUtil;
import com.google.appinventor.components.runtime.util.Vector2D;

/**
 * A round 'sprite' that can be placed on a {@link Canvas}, where it can react to touches and drags,
 * interact with other sprites ({@link ImageSprite}s and other `Ball`s) and the edge of the
 * `Canvas`, and move according to its property values.
 *
 * For example, to have a `Ball` move 4 pixels toward the top of a `Canvas` every 500 milliseconds
 * (half second), you would set the {@link #Speed(float)} property to 4 [pixels], the
 * {@link #Interval(int)} property to 500 [milliseconds], the {@link #Heading(double)} property to
 * 90 [degrees], and the {@link #Enabled(boolean)} property to `true`{:.logic.block}. These and its
 * other properties can be changed at any time.
 *
 * The difference between a `Ball` and an `ImageSprite` is that the latter can get its appearance
 * from an image file, while a `Ball`'s appearance can only be changed by varying its
 * {@link #PaintColor(int)} and {@link #Radius(int)} properties.
 */
@DesignerComponent(version = YaVersion.BALL_COMPONENT_VERSION,
    description = "<p>A round 'sprite' that can be placed on a " +
        "<code>Canvas</code>, where it can react to touches and drags, " +
        "interact with other sprites (<code>ImageSprite</code>s and other " +
        "<code>Ball</code>s) and the edge of the Canvas, and move according " +
        "to its property values.</p>" +
        "<p>For example, to have a <code>Ball</code> move 4 pixels toward the " +
        "top of a <code>Canvas</code> every 500 milliseconds (half second), " +
        "you would set the <code>Speed</code> property to 4 [pixels], the " +
        "<code>Interval</code> property to 500 [milliseconds], the " +
        "<code>Heading</code> property to 90 [degrees], and the " +
        "<code>Enabled</code> property to <code>True</code>.</p>" +
        "<p>The difference between a <code>Ball</code> and an <code>ImageSprite</code> is " +
        "that the latter can get its appearance from an image file, while a " +
        "<code>Ball</code>'s appearance can be changed only by varying its " +
        "<code>PaintColor</code> and <code>Radius</code> properties.</p>",
    category = ComponentCategory.ANIMATION,
    iconName = "images/ball.png")
@SimpleObject
public final class Ball extends Sprite {
  private int radius;
  private int paintColor;
  private Paint paint;
  static final int DEFAULT_RADIUS = 5;

  public Ball(ComponentContainer container) {
    super(container);
    paint = new Paint();

    // Set default properties.
    PaintColor(Component.COLOR_BLACK);
    Radius(DEFAULT_RADIUS);
  }

  // Implement or override methods

  @Override
  protected void onDraw(Canvas canvas) {
    if (visible) {
      float correctedXLeft = (float)(xLeft * form.deviceDensity());
      float correctedYTop =  (float)(yTop * form.deviceDensity());
      float correctedRadius = radius * form.deviceDensity();
      canvas.drawCircle(correctedXLeft + correctedRadius, correctedYTop +
          correctedRadius, correctedRadius, paint);
    }
  }

  // Get the vector to the center of the circle
  Vector2D getCenterVector() {
    double xCenter = xLeft + Width() / 2.0;
    double yCenter = yTop + Height() / 2.0;
    return new Vector2D(xCenter, yCenter);
  }

  // The min projection is the projection of the center minus the radius. We consider dot product
  // values as the projection so the radius needs to be multiplied by the axis's magnitude.
  double getMinProjection(Vector2D axis) {
    return Vector2D.dotProduct(getCenterVector(), axis) - Radius() * axis.magnitude();
  }

  // The max projection is the projection of the center plus the radius. We consider dot product
  // values as the projection so the radius needs to be multiplied by the axis's magnitude.
  double getMaxProjection(Vector2D axis) {
    return Vector2D.dotProduct(getCenterVector(), axis) + Radius() * axis.magnitude();
  }

  // The following four methods are required by abstract superclass
  // VisibleComponent.  Because we don't want to expose them to the Simple
  // programmer, we omit the SimpleProperty and DesignerProperty pragmas.
  @Override
  public int Height() {
    return 2 * radius;
  }

  @Override
  public void Height(int height) {
    // ignored
  }

  @Override
  public void HeightPercent(int pCent) {
    // ignored
  }

  @Override
  public int Width() {
    return 2 * radius;
  }

  @Override
  public void Width(int width) {
    // ignored
  }

  @Override
  public void WidthPercent(int pCent) {
    // ignored
  }

  @Override
  public boolean containsPoint(double qx, double qy) {
    double xCenter = xLeft + Width() / 2.0;
    double yCenter = yTop + Height() / 2.0;
    return ((qx - xCenter) * (qx - xCenter) + (qy - yCenter) * (qy - yCenter))
        <= radius * radius;
  }


  // Additional properties

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = "5")
  @SimpleProperty(description = "The distance from the edge of the Ball to its center.",
      category = PropertyCategory.APPEARANCE)
  public void Radius(int radius) {
    this.radius = radius;
    xLeft = xOriginToLeft(xOrigin);
    yTop = yOriginToTop(yOrigin);
    registerChange();
  }

  /**
   * The distance from the center of the `Ball` to its edge.
   */
  @SimpleProperty
  public int Radius() {
    return radius;
  }

  /**
   * The color of the `Ball`.
   *
   * @return  paint RGB color with alpha
   */
  @SimpleProperty(
      description = "The color of the Ball.",
      category = PropertyCategory.APPEARANCE)
  @IsColor
  public int PaintColor() {
    return paintColor;
  }

  /**
   * PaintColor property setter method.
   *
   * @suppressdoc
   * @param argb  paint RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void PaintColor(int argb) {
    paintColor = argb;
    if (argb != Component.COLOR_DEFAULT) {
      PaintUtil.changePaint(paint, argb);
    } else {
      // The default paint color is black.
      PaintUtil.changePaint(paint, Component.COLOR_BLACK);
    }
    registerChange();
  }

  // We need to override methods defined in the superclass to generate appropriate documentation.

  /**
   * Whether the x- and y-coordinates should represent the center of the `Ball`
   * (`true`{:.logic.block}) or its left and top edges (`false`{:.logic.block}).
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = DEFAULT_ORIGIN_AT_CENTER ? "True" : "False")
  @SimpleProperty(userVisible = false,
      description = "Whether the x- and y-coordinates should represent the center of the Ball " +
          "(true) or its left and top edges (false).",
      category = PropertyCategory.BEHAVIOR)
  public void OriginAtCenter(boolean b) {
    super.OriginAtCenter(b);
  }

  /**
   * The horizontal coordinate of the `Ball`, increasing as the `Ball` moves right. If the property
   * {@link #OriginAtCenter(boolean)} is true, the coordinate is for the center of the `Ball`;
   * otherwise, it is for the leftmost point of the `Ball`.
   */
  @SimpleProperty(
      description = "The horizontal coordinate of the Ball, increasing as the Ball moves right. " +
          "If the property OriginAtCenter is true, the coordinate is for the center of the Ball; " +
          "otherwise, it is for the leftmost point of the Ball.")
  @Override
  public double X() {
    return super.X();
  }

  /**
   * The vertical coordinate of the `Ball`, increasing as the `Ball` moves down. If the property
   * {@link #OriginAtCenter(boolean)} is true, the coordinate is for the center of the `Ball`
   * otherwise, it is for the uppermost point of the `Ball`.
   */
  @SimpleProperty(
      description = "The vertical coordinate of the Ball, increasing as the Ball moves " +
          "down. If the property OriginAtCenter is true, the coordinate is for the center of the Ball; " +
          "otherwise, it is for the uppermost point of the Ball.")
  @Override
  public double Y() {
    return super.Y();
  }

  /**
   * Sets the `x` and `y` coordinates of the `Ball`. If {@link #OriginAtCenter(boolean)} is true,
   * the center of the `Ball` will be placed here. Otherwise, the top left edge of the `Ball` will
   * be placed at the specified coordinates.
   * @param x the x-coordinate
   * @param y the y-coordinate
   */
  @SimpleFunction(
      description = "Sets the x and y coordinates of the Ball. If CenterAtOrigin is " +
          "true, the center of the Ball will be placed here. Otherwise, the top left edge of the Ball " +
          "will be placed at the specified coordinates.")
  @Override
  public void MoveTo(double x, double y) {
    super.MoveTo(x, y);
  }
}
