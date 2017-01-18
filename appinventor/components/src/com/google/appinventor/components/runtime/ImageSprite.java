// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.MediaUtil;

/**
 * Simple image-based Sprite.
 *
 */
@DesignerComponent(version = YaVersion.IMAGESPRITE_COMPONENT_VERSION,
    description = "<p>A 'sprite' that can be placed on a " +
    "<code>Canvas</code>, where it can react to touches and drags, " +
    "interact with other sprites (<code>Ball</code>s and other " +
    "<code>ImageSprite</code>s) and the edge of the Canvas, and move " +
    "according to its property values.  Its appearance is that of the " +
    "image specified in its <code>Picture</code> property (unless its " +
    "<code>Visible</code> property is <code>False</code>.</p> " +
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
    category = ComponentCategory.ANIMATION)
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
        // rotation will be the center of the sprite
        canvas.rotate((float) (- Heading()), xinit + w/2, yinit + h/2);
        drawable.draw(canvas);
        canvas.restore();
      }
    }
  }
 
  /**
   * Returns the path of the sprite's picture
   *
   * @return  the path of the sprite's picture
   */
  @SimpleProperty(
      description = "The picture that determines the sprite's appearence",
      category = PropertyCategory.APPEARANCE)
  public String Picture() {
    return picturePath;
  }

  /**
   * Specifies the path of the sprite's picture
   *
   * <p/>See {@link MediaUtil#determineMediaSource} for information about what
   * a path can be.
   *
   * @param path  the path of the sprite's picture
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  @SimpleProperty
  public void Picture(String path) {
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
  @SimpleProperty
  public int Height() {
    if (heightHint == LENGTH_PREFERRED || heightHint == LENGTH_FILL_PARENT || heightHint <= LENGTH_PERCENT_TAG) {
      // Drawable.getIntrinsicWidth/Height gives weird values, but Bitmap.getWidth/Height works.
      return drawable == null ? 0 : (int)(drawable.getBitmap().getHeight() / form.deviceDensity());
    }
    return heightHint;
  }

  @Override
  @SimpleProperty
  public void Height(int height) {
    heightHint = height;
    registerChange();
  }

  @Override
  public void HeightPercent(int pCent) {
    // Ignore
  }

  @Override
  @SimpleProperty
  public int Width() {
    if (widthHint == LENGTH_PREFERRED || widthHint == LENGTH_FILL_PARENT || widthHint <= LENGTH_PERCENT_TAG) {
      // Drawable.getIntrinsicWidth/Height gives weird values, but Bitmap.getWidth/Height works.
      return drawable == null ? 0 : (int)(drawable.getBitmap().getWidth() / form.deviceDensity());
    }
    return widthHint;
  }

  @Override
  @SimpleProperty
  public void Width(int width) {
    widthHint = width;
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
      description = "If true, the sprite image rotates to match the sprite's heading. " +
      "If false, the sprite image does not rotate when the sprite changes heading. " +
      "The sprite rotates around its centerpoint.",
      category = PropertyCategory.BEHAVIOR)
  public boolean Rotates() {
    return rotates;
  }

  /**
   * Rotates property setter method
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
}
