// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.google.appinventor.components.annotations.Asset;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.Vector2D;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A 'sprite' that can be placed on a {@link Canvas}, where it can react to touches and drags,
 * interact with other sprites ({@link Ball}s and other `ImageSprite`s) and the edge of the
 * `Canvas`, and move according to its property values. Its appearance is that of the image
 * specified in its {@link #Picture()} property (unless its {@link #Visible()} property is
 * `false`{:.logic.block}.
 *
 * To have an `ImageSprite` move 10 pixels to the left every 1000 milliseconds (one second), for
 * example, you would set the {@link #Speed()} property to 10 [pixels], the {@link #Interval()}
 * property to 1000 [milliseconds], the {@link #Heading()} property to 180 [degrees], and the
 * {@link #Enabled()} property to `true`{:.logic.block}. A sprite whose {@link #Rotates()}
 * property is `true`{:.logic.block} will rotate its image as the sprite's heading changes.
 * *Checking for collisions with a rotated sprite currently checks the sprite's unrotated position
 * so that collision checking will be inaccurate for tall narrow or short wide sprites that are
 * rotated.* Any of the sprite properties can be changed at any time under program control.
 */
@DesignerComponent(version = YaVersion.IMAGESPRITE_COMPONENT_VERSION,
    description = "<p>A 'sprite' that can be placed on a " +
        "<code>Canvas</code>, where it can react to touches and drags, " +
        "interact with other sprites (<code>Ball</code>s and other " +
        "<code>ImageSprite</code>s) and the edge of the Canvas, and move " +
        "according to its property values.  Its appearance is that of the " +
        "image specified in its <code>Picture</code> property (unless its " +
        "<code>Visible</code> property is <code>False</code>).</p> " +
        "<p>To have an <code>ImageSprite</code> move 10 pixels to the left " +
        "every 1000 milliseconds (one second), for example, " +
        "you would set the <code>Speed</code> property to 10 [pixels], the " +
        "<code>Interval</code> property to 1000 [milliseconds], the " +
        "<code>Heading</code> property to 180 [degrees], and the " +
        "<code>Enabled</code> property to <code>True</code>.  A sprite whose " +
        "<code>Rotates</code> property is <code>True</code> will rotate its " +
        "image as the sprite's <code>Heading</code> changes.  Checking for collisions " +
        "with a rotated sprite currently checks the sprite's unrotated position " +
        "so that collision checking will be inaccurate for tall narrow or short " +
        "wide sprites that are rotated.  Any of the sprite properties " +
        "can be changed at any time under program control.</p> ",
    category = ComponentCategory.ANIMATION,
    iconName = "images/imageSprite.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public class ImageSprite extends Sprite {
  private final Form form;
  private BitmapDrawable drawable;
  private int widthHint = LENGTH_PREFERRED;
  private int heightHint = LENGTH_PREFERRED;
  private String picturePath = "";  // Picture property
  private boolean rotates;

  /**
   * Constructor for ImageSprite.
   *
   * @param container
   */
  public ImageSprite(ComponentContainer container) {
    super(container);
    form = container.$form();
    rotates = true;
  }

  /**
   * This method uses getWidth and getHeight directly from the bitmap,
   * so we apply corrections for density for coordinates and size.
   * @param canvas the canvas on which to draw
   */
  public void onDraw(android.graphics.Canvas canvas) {
    if (drawable != null && visible) {
      int xinit = (int) (Math.round(xLeft) * form.deviceDensity());
      int yinit = (int) (Math.round(yTop) * form.deviceDensity());
      int w = (int)(Width() * form.deviceDensity());
      int h = (int)(Height() * form.deviceDensity());
      drawable.setBounds(xinit, yinit, xinit + w, yinit + h);
      // If the sprite doesn't rotate, just draw the drawable
      // within the bounds of the sprite rectangle
      if (!rotates) {
        drawable.draw(canvas);
      } else {
        // if the sprite does rotate, draw the sprite on the canvas
        // that has been rotated in the opposite direction
        // Still within those same image bounds.
        canvas.save();
        // rotate the canvas for drawing.  This pivot point of the
        // rotation will be the origin of the sprite
        canvas.rotate((float) (- Heading()), xinit + w * (float) u, yinit + h * (float) v);
        drawable.draw(canvas);
        canvas.restore();
      }
    }
  }

  // get the vector to the center of the image sprite
  protected Vector2D getCenterVector() {
    double xCenter = xLeft + Width() / 2.0;
    double yCenter = yTop + Height() / 2.0;

    Vector2D center = new Vector2D(xCenter, yCenter);

    return getVectorRotated(center);
  }

  // Get all non - parallel axes normal to the edges of the sprite. We need to consider only two
  // axes as the other two would be parallel to these.
  protected java.util.List<Vector2D> getNormalAxes() {
    java.util.List<Vector2D> corners = getExtremityVectors();

    java.util.List<Vector2D> normalAxes = new ArrayList<>();

    Vector2D leftRightEdge = Vector2D.difference(corners.get(0), corners.get(1));
    Vector2D topDownEdge = Vector2D.difference(corners.get(1), corners.get(2));

    Vector2D leftRightNormal = leftRightEdge.getNormalVector();
    Vector2D topDownNormal = topDownEdge.getNormalVector();

    normalAxes.add(leftRightNormal);
    normalAxes.add(topDownNormal);

    return normalAxes;
  }

  // The minimum projection will be that of one of the corners. Note this is the minimum dot product
  // i.e. the axis is not guaranteed to be a unit vector. As we only do comparisons on these values,
  // calculating the unit vector is a waster of time.
  protected double getMinProjection(Vector2D axis) {
    java.util.List<Vector2D> corners = getExtremityVectors();
    double minimum = Vector2D.dotProduct(axis, corners.get(0));

    for (Vector2D point : corners) {
      double projectionMagnitude = Vector2D.dotProduct(axis, point);
      if (projectionMagnitude < minimum) {
        minimum = projectionMagnitude;
      }
    }

    return minimum;
  }

  // The maximum projection will be that of one of the corners. Note this is the maximum dot product
  // i.e. the axis is not guaranteed to be a unit vector. As we only do comparisons on these values,
  // calculating the unit vector is a waster of time.
  protected double getMaxProjection(Vector2D axis) {
    java.util.List<Vector2D> corners = getExtremityVectors();
    double maximum = Vector2D.dotProduct(axis, corners.get(0));

    for (Vector2D point : corners) {
      double projectionMagnitude = Vector2D.dotProduct(axis, point);
      if (projectionMagnitude > maximum) {
        maximum = projectionMagnitude;
      }
    }

    return maximum;
  }

  // To get the extremity vectors for the rotated image sprite, first calculate the the extremity
  // vectors for the un-rotated image sprite and rotate them about the origin.
  // Let v be the vector fot the origin. This does not change on rotation.
  // Let u be the vector for some corner. After rotation u changes as follows:
  // v - u is the vector from origin to the corner. Rotate v - u by Heading() degrees and say it
  // becomes w. Then the vector for the corner after rotation will be v + w.
  protected java.util.List<Vector2D> getExtremityVectors() {
    java.util.List<Vector2D> corners = new ArrayList<>();

    // [u, v] values of the four corners, taken in clockwise direction starting from top - left
    final int[][] delta = new int[][] {{0, 0}, {1, 0}, {1, 1}, {0, 1}};

    // add all corners to corners
    for (int[] d : delta) {
      double dx = d[0] * Width();
      double dy = d[1] * Height();
      Vector2D corner = new Vector2D(xLeft + dx, yTop + dy);
      corners.add(getVectorRotated(corner));
    }

    return corners;
  }

  // If the image is rotated return the vector toRotate after rotation
  private Vector2D getVectorRotated(Vector2D toRotate) {
    if (rotates) {
      Vector2D origin = new Vector2D(xOrigin, yOrigin);
      Vector2D originToPoint = Vector2D.difference(toRotate, origin);
      originToPoint.rotate(headingRadians);
      return Vector2D.addition(origin, originToPoint);
    } else {
      return toRotate;
    }
  }

  /**
   * Returns the path of the sprite's picture
   *
   * @return  the path of the sprite's picture
   */
  @SimpleProperty(
      description = "The picture that determines the ImageSprite's appearance.",
      category = PropertyCategory.APPEARANCE)
  public String Picture() {
    return picturePath;
  }

  /**
   * Specifies the path of the sprite's picture.
   *
   * @internaldoc
   * <p/>See {@link MediaUtil#determineMediaSource} for information about what
   * a path can be.
   *
   * @param path  the path of the sprite's picture
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  @SimpleProperty
  public void Picture(@Asset String path) {
    picturePath = (path == null) ? "" : path;
    try {
      drawable = MediaUtil.getBitmapDrawable(form, picturePath);
    } catch (IOException ioe) {
      Log.e("ImageSprite", "Unable to load " + picturePath);
      drawable = null;
    }
    // note: drawable can be null!
    registerChange();
  }

  // The actual width/height of an ImageSprite whose Width/Height property is set to Automatic or
  // Fill Parent will be the width/height of the image.

  @Override
  @SimpleProperty(description = "The height of the ImageSprite in pixels.")
  public int Height() {
    if (heightHint == LENGTH_PREFERRED || heightHint == LENGTH_FILL_PARENT || heightHint <= LENGTH_PERCENT_TAG) {
      // Drawable.getIntrinsicWidth/Height gives weird values, but Bitmap.getWidth/Height works.
      return drawable == null ? 0 : (int)(drawable.getBitmap().getHeight() / form.deviceDensity());
    }
    return heightHint;
  }

  /**
   * @suppressdoc
   * @param height  height property used by the layout
   */
  @Override
  @SimpleProperty
  public void Height(int height) {
    heightHint = height;
    yTop = yOriginToTop(yOrigin);
    registerChange();
  }

  @Override
  public void HeightPercent(int pCent) {
    // Ignore
  }

  @Override
  @SimpleProperty(description = "The width of the ImageSprite in pixels.")
  public int Width() {
    if (widthHint == LENGTH_PREFERRED || widthHint == LENGTH_FILL_PARENT || widthHint <= LENGTH_PERCENT_TAG) {
      // Drawable.getIntrinsicWidth/Height gives weird values, but Bitmap.getWidth/Height works.
      return drawable == null ? 0 : (int)(drawable.getBitmap().getWidth() / form.deviceDensity());
    }
    return widthHint;
  }

  /**
   * @suppressdoc
   * @param width  width property used by the layout
   */
  @Override
  @SimpleProperty
  public void Width(int width) {
    widthHint = width;
    xLeft = xOriginToLeft(xOrigin);
    registerChange();
  }

  @Override
  public void WidthPercent(int pCent) {
    // Ignore
  }

  /**
   * Rotates property getter method.
   *
   * @return  {@code true} indicates that the image rotates to match the sprite's heading
   * {@code false} indicates that the sprite image doesn't rotate.
   */
  @SimpleProperty(
      description = "Whether the image should rotate to match the ImageSprite's heading. " +
          "The sprite rotates around its origin.",
      category = PropertyCategory.BEHAVIOR)
  public boolean Rotates() {
    return rotates;
  }

  /**
   * If true, the sprite image rotates to match the sprite's heading. If false, the sprite image
   * does not rotate when the sprite changes heading. The sprite rotates around its origin.
   *
   * @param rotates  {@code true} indicates that the image rotates to match the sprite's heading
   * {@code false} indicates that the sprite image doesn't rotate.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty
    public void Rotates(boolean rotates) {
    this.rotates = rotates;
    registerChange();
  }

  // We need to override methods defined in the superclass to generate appropriate documentation.

  @SimpleProperty(
      description = "The horizontal coordinate of the origin of the ImageSprite, " +
          "increasing as the ImageSprite moves right.")
  @Override
  public double X() {
    return super.X();
  }

  @SimpleProperty(
      description = "The vertical coordinate of the origin of the ImageSprite, " +
          "increasing as the ImageSprite moves down.")
  @Override
  public double Y() {
    return super.Y();
  }

  /**
   * The horizontal unit coordinate of the origin with respect to the left edge. Value between
   * 0.0 and 1.0. For example, a value of 0.0 mean the origin is on the left edge, 0.5 means the
   * origin is in the middle and 1.0 means the origin lies on the right edge.
   *
   * @return  Horizontal unit coordinate of origin with respect to left edge
   */
  @SimpleProperty
  public double OriginX() {
    return super.U();
  }

  /**
   * Horizontal unit coordinate of the origin with respect to left edge. Permitted values in [0, 1].
   * A value of 0.0 means the origin lies on the left edge, 0.5 means the origin lies in the middle
   * and 1.0 means the origin is on the right edge.
   * @param u Horizontal unit coordinate of origin with respect to left edge
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_UNIT_COORDINATE,
          defaultValue = "0.0")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void OriginX(double u) {
    super.U(u);
  }

  /**
   * The vertical unit coordinate of the origin with respect to the top edge. Value between 0.0
   * and 1.0. For example, a value of 0.0 means the origin is on the top edge, 0.5 means the origin
   * is in the middle and 1.0 means the origin lies on the bottom edge.
   *
   * @return Vertical unit coordinate of the origin with respect to top edge
   */
  @SimpleProperty
  public double OriginY() {
    return super.V();
  }

  /**
   * Vertical unit coordinate of the origin with respect to top edge. Permitted values in [0, 1].
   * A value of 0.0 means the origin lies on the top edge, 0.5 means the origin lies in the middle
   * and 1.0 means the origin is on the bottom edge.
   * @param v Vertical unit coordinate of the origin with respect to top edge
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_UNIT_COORDINATE,
          defaultValue = "0.0")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void OriginY(double v) {
    super.V(v);
  }

  /**
   * Mark the origin of %type% using a draggable marker.
   * @param originCoordinates The unit coordinates of the origin
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ORIGIN,
          defaultValue = DEFAULT_ORIGIN)
  @SimpleProperty(description = "Mark the origin of the image sprite using a draggable marker",
      category = PropertyCategory.BEHAVIOR)
  public void MarkOrigin(String originCoordinates) {
    // parse u and v with originCoordinates interpreted in "(u, v)" format
    double u = Double.parseDouble(originCoordinates.substring(1, originCoordinates.indexOf(",")));
    double v = Double.parseDouble(originCoordinates.substring(
            originCoordinates.indexOf(",") + 2, originCoordinates.length() - 1));
    super.U(u);
    super.V(v);
  }

  /**
   * Moves the %type% so that its origin is at the specified `x` and `y` coordinates.
   * @param x the x-coordinate
   * @param y the y-coordinate
   */
  @SimpleFunction(
      description = "Moves the ImageSprite so that its origin is at " +
          "the specified x and y coordinates.")
  @Override
  public void MoveTo(double x, double y) {
    super.MoveTo(x, y);
  }
}
