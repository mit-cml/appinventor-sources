// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import android.util.Log;

import android.view.View;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.ViewUtil;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container for components that arranges them absolutely.
 */

@DesignerComponent(version = YaVersion.ABSOLUTEARRANGEMENT_COMPONENT_VERSION,
    description = "<p>A formatting element in which to place components "
        + "that should be displayed at any coordinates.</p>",
    iconName = "images/absoluteArrangement.png",
    category = ComponentCategory.LAYOUT)
@SimpleObject
public class AbsoluteArrangement extends AndroidViewComponent
    implements Component, ComponentContainer {
  private final Activity context;

  // Layout
  private final RelativeLayout viewLayout;
  private final List<Component> children = new ArrayList<>();
  // Backing for background color
  private int backgroundColor;
  // This is the Drawable corresponding to the Image property.
  // If an Image has never been set or if the most recent Image could not be
  // loaded, this is null.
  private Drawable backgroundImageDrawable;
  // Image path
  private String imagePath = "";

  private static final String LOG_TAG = "AArrangement";

  /**
   * Creates a new AbsoluteArrangement component.
   *
   * @param container container in which components will be placed
   */
  public AbsoluteArrangement(ComponentContainer container) {
    super(container);
    context = container.$context();

    Log.d(LOG_TAG, "Setting up layoutManager");

    viewLayout = new RelativeLayout(context, ComponentConstants.EMPTY_A_ARRANGEMENT_WIDTH,
        ComponentConstants.EMPTY_A_ARRANGEMENT_HEIGHT);

    container.$add(this);
    BackgroundColor(Component.COLOR_DEFAULT);
  }

  // ComponentContainer implementation

  @Override
  public Activity $context() {
    return context;
  }

  @Override
  public Form $form() {
    return container.$form();
  }

  @Override
  public void $add(AndroidViewComponent component) {
    children.add(component);
    viewLayout.add(component);
  }

  @Override
  public void setChildWidth(AndroidViewComponent component, int width) {

    Log.d(LOG_TAG, "setChildWidth: width = " + width + " component = " + component);
    if (width <= LENGTH_PERCENT_TAG) {

      int childWidth = container.$form().Width();

      if ((childWidth > LENGTH_PERCENT_TAG) && (childWidth <= 0)) {
        // FILL_PARENT OR LENGTH_PREFERRED
        width = LENGTH_PREFERRED;
      } else {
        Log.d(LOG_TAG, "%%setChildWidth(): width = " + width + " parent Width = " + childWidth
            + " child = " + component);
        width = childWidth * (-(width - LENGTH_PERCENT_TAG)) / 100;
      }
    }

    component.setLastWidth(width);

    ViewUtil.setChildWidthForRelativeLayout(component.getView(), width);
  }

  @Override
  public void setChildHeight(AndroidViewComponent component, int height) {
    if (height <= LENGTH_PERCENT_TAG) {
      int childHeight = container.$form().Height();

      if ((childHeight > LENGTH_PERCENT_TAG) && (childHeight <= 0)) {
        // FILL_PARENT OR LENGTH_PREFERRED
        height = LENGTH_PREFERRED;
      } else {
        height = childHeight * (-(height - LENGTH_PERCENT_TAG)) / 100;
      }
    }

    component.setLastHeight(height);

    ViewUtil.setChildHeightForRelativeLayout(component.getView(), height);
  }

  @Override
  public void setChildNeedsLayout(AndroidViewComponent component) {
    viewLayout.updateComponentPosition(component);
  }

  @Override
  public List<Component> getChildren() {
    return Collections.unmodifiableList(children);
  }

  // AndroidViewComponent implementation

  @Override
  public View getView() {
    return viewLayout.getLayoutManager();
  }

  /**
   * Returns the component's background color as an alpha-red-green-blue integer.
   *
   * @return background RGB color with alpha
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Returns the component's background color")
  public int BackgroundColor() {
    return backgroundColor;
  }

  /**
   * Specifies the button's background color as an alpha-red-green-blue integer.
   * If the parameter is {@link Component#COLOR_DEFAULT}, the original beveling is
   * restored. If an Image has been set, the color change will not be visible
   * until the Image is removed.
   *
   * @param argb background RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
  @SimpleProperty(description = "Specifies the component's background color. "
      + "The background color will not be visible if an Image is being displayed.")
  public void BackgroundColor(int argb) {
    backgroundColor = argb;
    updateAppearance();
  }

  /**
   * Returns the path of the button's image.
   *
   * @return the path of the button's image
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public String Image() {
    return imagePath;
  }

  /**
   * Specifies the path of the button's image.
   *
   * @internaldoc
   *     See {@link com.google.appinventor.components.runtime.util.MediaUtil#determineMediaSource}
   *     for information about what a path can be.
   *
   * @param path the path of the button's image
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET)
  @SimpleProperty(description = "Specifies the path of the component's image.  "
      + "If there is both an Image and a BackgroundColor, only the Image will be visible.")
  public void Image(String path) {
    // If it's the same as on the prior call and the prior load was successful,
    // do nothing.
    if (path == null) {
      path = "";
    }
    if (path.equals(imagePath) && backgroundImageDrawable != null) {
      return;
    }

    imagePath = path;

    // Clear the prior background image.
    backgroundImageDrawable = null;

    // Load image from file.
    if (imagePath.length() > 0) {
      try {
        backgroundImageDrawable = MediaUtil.getBitmapDrawable(container.$form(), imagePath);
      } catch (IOException ioe) {
        // Fall through with a value of null for backgroundImageDrawable.
      }
    }

    // Update the appearance based on the new value of backgroundImageDrawable.
    updateAppearance();
  }

  // Update appearance based on values of backgroundImageDrawable, backgroundColor
  // and shape.
  // Images take precedence over background colors.
  private void updateAppearance() {
    // If there is no background image,
    // the appearance depends solely on the background color and shape.
    if (backgroundImageDrawable == null) {
      if (backgroundColor == Component.COLOR_DEFAULT) {
        // If there is no background image and color is default,
        // restore original 3D bevel appearance.
        int color = $form().isDarkTheme() ? Color.BLACK : Color.WHITE;
        ViewUtil.setBackgroundDrawable(viewLayout.getLayoutManager(), new ColorDrawable(color));
      } else {
        // Clear the background image.
        ViewUtil.setBackgroundDrawable(viewLayout.getLayoutManager(), null);
        viewLayout.getLayoutManager().setBackgroundColor(backgroundColor);
      }
    } else {
      // If there is a background image
      ViewUtil.setBackgroundImage(viewLayout.getLayoutManager(), backgroundImageDrawable);
    }
  }

}
