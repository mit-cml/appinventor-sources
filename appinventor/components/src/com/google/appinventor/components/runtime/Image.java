// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.Manifest;
import com.google.appinventor.components.annotations.Asset;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.errors.IllegalArgumentError;
import com.google.appinventor.components.runtime.util.AnimationUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.HoneycombUtil;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.TiramisuUtil;
import com.google.appinventor.components.runtime.util.ViewUtil;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import java.io.IOException;

/**
 * Component for displaying images and basic animations.
 *
 * The picture to display, and other aspects of the Image's appearance, can be specified in the
 * Designer or in the Blocks Editor.
 */
@DesignerComponent(version = YaVersion.IMAGE_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "Component for displaying images.  The picture to display, " +
    "and other aspects of the Image's appearance, can be specified in the " +
    "Designer or in the Blocks Editor.",
    iconName = "images/image.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET," +
    "android.permission.READ_EXTERNAL_STORAGE")
public final class Image extends AndroidViewComponent {

  private final ImageView view;

  private String picturePath = "";  // Picture property

  private double rotationAngle = 0.0;

  private int scalingMode = Component.SCALING_SCALE_PROPORTIONALLY;
  
  private boolean clickable = false;

  /**
   * Creates a new Image component.
   *
   * @param container  container, component will be placed in
   */
  public Image(ComponentContainer container) {
    super(container);

    view = new ImageView(container.$context()) {
      @Override
      public boolean verifyDrawable(Drawable dr) {
        super.verifyDrawable(dr);
        // TODO(user): multi-image animation
        return true;
      }
    };
    view.setFocusable(true);

    // Adds the component to its designated container
    container.$add(this);
  }

  @Override
  public View getView() {
    return view;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "A written description of what the image looks like.",
      category = PropertyCategory.APPEARANCE)
  public void AlternateText(String description){
    view.setContentDescription(description);
  }

  @SimpleEvent(description = "An event that occurs when an image is clicked.")
  public void Click() {
    EventDispatcher.dispatchEvent(this, "Click");
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
    defaultValue = "False")
  @SimpleProperty(description = "Specifies whether the image should be clickable or not.")
  public void Clickable(boolean clickable) {
    this.clickable = clickable;
    view.setClickable(this.clickable);
    if (this.clickable) {
      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Click();
        }
      });
    } else {
      view.setOnClickListener(null);
    }
  }

  @SimpleProperty(description = "Specifies whether the image should be clickable or not.", category = PropertyCategory.APPEARANCE)
  public boolean Clickable() {
    return this.clickable;
  }

  /**
   * Returns the path of the image's picture.
   *
   * @return  the path of the image's picture
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public String Picture() {
    return picturePath;
  }

  /**
   * Specifies the path of the `Image`'s `Picture`.
   *
   * @internaldoc
   * <p/>See {@link MediaUtil#determineMediaSource} for information about what
   * a path can be.
   *
   * @param path  the path of the image's picture
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  @SimpleProperty
  public void Picture(@Asset String path) {
    final String tempPath = path == null ? "" : path;
    if (TiramisuUtil.requestImagePermissions(container.$form(), path,
        new PermissionResultHandler() {
            @Override
            public void HandlePermissionResponse(String permission, boolean granted) {
              if (granted) {
                Picture(tempPath);
              } else {
                container.$form().dispatchPermissionDeniedEvent(Image.this, "Picture", permission);
              }
            }
          })) {
      return;
    }
    picturePath = tempPath;

    Drawable drawable;
    try {
      drawable = MediaUtil.getBitmapDrawable(container.$form(), picturePath);
    } catch (IOException ioe) {
      Log.e("Image", "Unable to load " + picturePath);
      drawable = null;
    }

    ViewUtil.setImage(view, drawable);
  }

  /**
   * Specifies the angle, in degrees, at which the image picture appears rotated.
   *
   * @param rotated  the rotation angle
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = "0.0")
  @SimpleProperty
  public void RotationAngle(double rotationAngle) {
    if (this.rotationAngle == rotationAngle) {
      return;                   // Nothing to do...
                                // This also means that you can always set the
                                // the angle to 0.0 even on older Android devices
    }
    if (SdkLevel.getLevel() < SdkLevel.LEVEL_HONEYCOMB) {
      container.$form().dispatchErrorOccurredEvent(this, "RotationAngle",
        ErrorMessages.ERROR_IMAGE_CANNOT_ROTATE);
      return;
    }
    HoneycombUtil.viewSetRotate(view, rotationAngle);
    this.rotationAngle = rotationAngle;
  }

  @SimpleProperty(description = "The angle at which the image picture appears rotated. " +
      "This rotation does not appear on the designer screen, only on the device.",
      category = PropertyCategory.APPEARANCE)
  public double RotationAngle() {
    return rotationAngle;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
    defaultValue = "False")
  // @Deprecated -- We will deprecate this in a future release (jis: 2/12/2016)
  @SimpleProperty(description = "Specifies whether the image should be resized to match the size of the ImageView.",
      category = PropertyCategory.APPEARANCE)
  public void ScalePictureToFit(boolean scale) {
    if (scale)
      view.setScaleType(ImageView.ScaleType.FIT_XY);
    else
      view.setScaleType(ImageView.ScaleType.FIT_CENTER);
  }

  /**
   * This is a limited form of animation that can attach a small number of motion types to images.
   * The allowable motions are `ScrollRightSlow`, `ScrollRight`, `ScrollRightFast`,
   * `ScrollLeftSlow`, `ScrollLeft`, `ScrollLeftFast`, and `Stop`.
   *
   * @see AnimationUtil
   *
   * @param animation  animation kind
   */
  @SimpleProperty(description = "This is a limited form of animation that can attach " +
      "a small number of motion types to images.  The allowable motions are " +
      "ScrollRightSlow, ScrollRight, ScrollRightFast, ScrollLeftSlow, ScrollLeft, " +
      "ScrollLeftFast, and Stop",
      category = PropertyCategory.APPEARANCE)
  // TODO(user): This should be changed from a property to an "animate" method, and have the choices
  // placed in a dropdown.  Aternatively the whole thing should be removed and we should do
  // something that is more consistent with sprites.
  public void Animation(String animation) {
    AnimationUtil.ApplyAnimation(view, animation);
  }

  @Deprecated
//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_SCALING,
//      defaultValue = Component.SCALING_SCALE_PROPORTIONALLY + "")
  @SimpleProperty(description = "This property determines how the picture " +
      "scales according to the Height or Width of the Image. Scale " +
      "proportionally (0) preserves the picture aspect ratio. Scale to fit " +
      "(1) matches the Image area, even if the aspect ratio changes.")
  public void Scaling(int mode) {
    switch (mode) {
      case 0:
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        break;
      case 1:
        view.setScaleType(ImageView.ScaleType.FIT_XY);
        break;
      default:
        throw new IllegalArgumentError("Illegal scaling mode: " + mode);
    }
    scalingMode = mode;
  }

  /**
   * @suppressdoc
   */
  @SimpleProperty
  public int Scaling() {
    return scalingMode;
  }
}
