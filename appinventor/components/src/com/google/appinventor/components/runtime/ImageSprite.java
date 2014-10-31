// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

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

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import java.io.IOException;

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

  private Matrix mat;

  private Bitmap unrotatedBitmap;
  private Bitmap rotatedBitmap;
  private Bitmap scaledBitmap;

  private BitmapDrawable rotatedDrawable;
  private double cachedRotationHeading;
  private boolean rotationCached;

  /**
   * Constructor for ImageSprite.
   *
   * @param container
   */
  public ImageSprite(ComponentContainer container) {
    super(container);
    form = container.$form();
    mat = new Matrix();
    rotates = true;
    rotationCached = false;
  }

  public void onDraw(android.graphics.Canvas canvas) {
    if (unrotatedBitmap != null && visible) {
      int xinit = (int) Math.round(xLeft);
      int yinit = (int) Math.round(yTop);
      int w = Width();
      int h = Height();
      // If the sprite doesn't rotate,  use the original drawable
      // otherwise use the bitmapDrawable
      if (!rotates) {
        drawable.setBounds(xinit, yinit, xinit + w, yinit + h);
        drawable.draw(canvas);
      } else {
        // compute the new rotated image if the heading has changed
        if (!rotationCached || (cachedRotationHeading != Heading())) {
          // Set up the matrix for the rotation transformation
          // Rotate around the center of the sprite image (w/2, h/2)
          // TODO(halabelson): Add a way for the user to specify the center of rotation.
          mat.setRotate((float) -Heading(), w / 2, h / 2);
          // We must scale the unrotated Bitmap to be the user specified size before
          // rotating.
          if (w != unrotatedBitmap.getWidth() || h != unrotatedBitmap.getHeight()) {
            scaledBitmap = Bitmap.createScaledBitmap(unrotatedBitmap, w, h, true);
          }
          else {
            scaledBitmap = unrotatedBitmap;
          }
          // Next create the rotated bitmap
          // Careful: We use getWidth and getHeight of the unrotated bitmap, rather than the
          // Width and Height of the sprite.  Doing the latter produces an illegal argument
          // exception in creating the bitmap, if the user sets the Width or Height of the
          // sprite to be larger than the image size.
          rotatedBitmap = Bitmap.createBitmap(
              scaledBitmap,
              0, 0,
              scaledBitmap.getWidth(), scaledBitmap.getHeight(),
              mat, true);
          // make a drawable for the rotated image and cache the heading
          rotatedDrawable = new BitmapDrawable(rotatedBitmap);
          cachedRotationHeading = Heading();
        }
        // Position the drawable:
        // We want the center of the image to remain fixed under the rotation.
        // To do this, we have to take account of the fact that, since the original
        // and the rotated bitmaps are rectangular, the offset of the center point from (0,0)
        // in the rotated bitmap will in general be different from the offset
        // in the unrotated bitmap.  Namely, rather than being 1/2 the width and height of the
        // unrotated bitmap, the offset is 1/2 the width and height of the rotated bitmap.
        // So when we display on the canvas, we  need to displace the upper left away
        // from (xinit, yinit) to take account of the difference in the offsets.
        rotatedDrawable.setBounds(
            xinit + w / 2 - rotatedBitmap.getWidth() / 2,
            yinit + h / 2 - rotatedBitmap.getHeight() / 2 ,
            // add in the width and height of the rotated bitmap
            // to get the other right and bottom edges
            xinit + w / 2 + rotatedBitmap.getWidth() / 2,
            yinit + h / 2 + rotatedBitmap.getHeight() / 2);
        rotatedDrawable.draw(canvas);
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
    // NOTE(lizlooney) - drawable can be null!
    if (drawable != null) {
      // we'll need the bitmap for the drawable in order to rotate it
      unrotatedBitmap = drawable.getBitmap();
    } else {
      unrotatedBitmap = null;
    }
    registerChange();
  }

  // The actual width/height of an ImageSprite whose Width/Height property is set to Automatic or
  // Fill Parent will be the width/height of the image.

  @Override
  @SimpleProperty
  public int Height() {
    if (heightHint == LENGTH_PREFERRED || heightHint == LENGTH_FILL_PARENT) {
      // Drawable.getIntrinsicWidth/Height gives weird values, but Bitmap.getWidth/Height works.
      return drawable == null ? 0 : drawable.getBitmap().getHeight();
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
  @SimpleProperty
  public int Width() {
    if (widthHint == LENGTH_PREFERRED || widthHint == LENGTH_FILL_PARENT) {
      // Drawable.getIntrinsicWidth/Height gives weird values, but Bitmap.getWidth/Height works.
      return drawable == null ? 0 : drawable.getBitmap().getWidth();
    }
    return widthHint;
  }

  @Override
  @SimpleProperty
  public void Width(int width) {
    widthHint = width;
    registerChange();
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
