// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.os.Handler;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.AlignmentUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.ViewUtil;

import java.io.IOException;

/**
 * A container for components that arranges them linearly, either
 * horizontally or vertically.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author kkashi01@gmail.com (Hossein Amerkashi) (added Image and BackgroundColors)
 */

@SimpleObject
public class HVArrangement extends AndroidViewComponent implements Component, ComponentContainer {
  private final Activity context;

  // Layout
  private final int orientation;
  private final LinearLayout viewLayout;

  // translates App Inventor alignment codes to Android gravity
  private final AlignmentUtil alignmentSetter;

  // the alignment for this component's LinearLayout
  private int horizontalAlignment;
  private int verticalAlignment;
    // Backing for background color
    private int backgroundColor;
    // This is the Drawable corresponding to the Image property.
    // If an Image has never been set or if the most recent Image could not be loaded, this is null.
    private Drawable backgroundImageDrawable;
    // Image path
    private String imagePath = "";

  private Drawable defaultButtonDrawable;

  private final Handler androidUIHandler = new Handler();

  /**
   * Creates a new HVArrangement component.
   *
   * @param container  container, component will be placed in
   * @param orientation one of
   *     {@link ComponentConstants#LAYOUT_ORIENTATION_HORIZONTAL}.
   *     {@link ComponentConstants#LAYOUT_ORIENTATION_VERTICAL}
  */
  public HVArrangement(ComponentContainer container, int orientation) {
    super(container);
    context = container.$context();

    this.orientation = orientation;
    viewLayout = new LinearLayout(context, orientation,
        ComponentConstants.EMPTY_HV_ARRANGEMENT_WIDTH,
        ComponentConstants.EMPTY_HV_ARRANGEMENT_HEIGHT);
    viewLayout.setBaselineAligned(false);
    alignmentSetter = new AlignmentUtil(viewLayout);

    horizontalAlignment = ComponentConstants.HORIZONTAL_ALIGNMENT_DEFAULT;
    verticalAlignment = ComponentConstants.VERTICAL_ALIGNMENT_DEFAULT;
    alignmentSetter.setHorizontalAlignment(horizontalAlignment);
    alignmentSetter.setVerticalAlignment(verticalAlignment);

      // Save the default values in case the user wants them back later.
      defaultButtonDrawable = getView().getBackground();

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
    viewLayout.add(component);
  }

  @Override
  public void setChildWidth(final AndroidViewComponent component, int width) {
    setChildWidth(component, width, 0);
  }

  public void setChildWidth(final AndroidViewComponent component, int width, final int trycount) {
    int cWidth = container.$form().Width();
    if (cWidth == 0 && trycount < 2) {     // We're not really ready yet...
      final int fWidth = width;            // but give up after two tries...
      androidUIHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            System.err.println("(HVArrangement)Width not stable yet... trying again");
            setChildWidth(component, fWidth, trycount + 1);
          }
        }, 100);                // Try again in 1/10 of a second
    }
    if (width <= LENGTH_PERCENT_TAG) {
      System.err.println("HVArrangement.setChildWidth(): width = " + width + " parent Width = " + cWidth + " child = " + component);
      width = cWidth * (- (width - LENGTH_PERCENT_TAG)) / 100;
    }

    component.setLastWidth(width);

    if (orientation == ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL) {
      ViewUtil.setChildWidthForHorizontalLayout(component.getView(), width);
    } else {
      ViewUtil.setChildWidthForVerticalLayout(component.getView(), width);
    }
  }

  @Override
  public void setChildHeight(final AndroidViewComponent component, int height) {
    int cHeight = container.$form().Height();
    if (cHeight == 0) {         // Not ready yet...
      final int fHeight = height;
      androidUIHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            System.err.println("(HVArrangement)Height not stable yet... trying again");
            setChildHeight(component, fHeight);
          }
        }, 100);                // Try again in 1/10 of a second
    }
    if (height <= LENGTH_PERCENT_TAG) {
      height = cHeight * (- (height - LENGTH_PERCENT_TAG)) / 100;
    }

    component.setLastHeight(height);

    if (orientation == ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL) {
      ViewUtil.setChildHeightForHorizontalLayout(component.getView(), height);
    } else {
      ViewUtil.setChildHeightForVerticalLayout(component.getView(), height);
    }
  }

  // AndroidViewComponent implementation

  @Override
  public View getView() {
    return viewLayout.getLayoutManager();
  }

 // These property definitions are duplicated in Form.java

  // The numeric encodings are defined Component Constants

  /**
   * Returns a number that encodes how contents of the arrangement are aligned horizontally.
   * The choices are: 1 = left aligned, 2 = horizontally centered, 3 = right aligned
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "A number that encodes how contents of the arrangement are aligned " +
          " horizontally. The choices are: 1 = left aligned, 2 = horizontally centered, " +
          " 3 = right aligned.  Alignment has no effect if the arrangement's width is " +
          "automatic.")
  public int AlignHorizontal() {
    return horizontalAlignment;
  }

  /**
   * Sets the horizontal alignment for contents of the arrangement
   *
   * @param alignment
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_HORIZONTAL_ALIGNMENT,
      defaultValue = ComponentConstants.HORIZONTAL_ALIGNMENT_DEFAULT + "")
  @SimpleProperty
  public void AlignHorizontal(int alignment) {
    try {
      // notice that the throw will prevent the alignment from being changed
      // if the argument is illegal
      alignmentSetter.setHorizontalAlignment(alignment);
      horizontalAlignment = alignment;
    } catch (IllegalArgumentException e) {
      container.$form().dispatchErrorOccurredEvent(this, "HorizontalAlignment",
          ErrorMessages.ERROR_BAD_VALUE_FOR_HORIZONTAL_ALIGNMENT, alignment);
    }
  }

  /**
   * Returns a number that encodes how contents of the arrangement are aligned vertically.
   * The choices are: 1 = top, 2 = vertically centered, 3 = aligned at the bottom.
   * Alignment has no effect if the arrangement's height is automatic.
   */
   @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "A number that encodes how the contents of the arrangement are aligned " +
          " vertically. The choices are: 1 = aligned at the top, 2 = vertically centered, " +
          "3 = aligned at the bottom.  Alignment has no effect if the arrangement's height " +
          "is automatic.")
  public int AlignVertical() {
    return verticalAlignment;
  }

  /**
   * Sets the vertical alignment for contents of the arrangement
   *
   * @param alignment
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_VERTICAL_ALIGNMENT,
      defaultValue = ComponentConstants.VERTICAL_ALIGNMENT_DEFAULT + "")
  @SimpleProperty
  public void AlignVertical(int alignment) {
    try {
      // notice that the throw will prevent the alignment from being changed
      // if the argument is illegal
      alignmentSetter.setVerticalAlignment(alignment);
      verticalAlignment = alignment;
    } catch (IllegalArgumentException e) {
      container.$form().dispatchErrorOccurredEvent(this, "VerticalAlignment",
          ErrorMessages.ERROR_BAD_VALUE_FOR_VERTICAL_ALIGNMENT, alignment);
    }
  }

    /**
     * Returns the component's background color as an alpha-red-green-blue
     * integer.
     *
     * @return  background RGB color with alpha
     */
    @SimpleProperty(category = PropertyCategory.APPEARANCE,
            description = "Returns the component's background color")
    public int BackgroundColor() {
        return backgroundColor;
    }

    /**
     * Specifies the button's background color as an alpha-red-green-blue
     * integer.  If the parameter is {@link Component#COLOR_DEFAULT}, the
     * original beveling is restored.  If an Image has been set, the color
     * change will not be visible until the Image is removed.
     *
     * @param argb background RGB color with alpha
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
            defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
    @SimpleProperty(description = "Specifies the component's background color. " +
            "The background color will not be visible if an Image is being displayed.")
    public void BackgroundColor(int argb) {
        backgroundColor = argb;
//        getView().setBackgroundColor(argb);
        updateAppearance();

    }

    /**
     * Returns the path of the button's image.
     *
     * @return  the path of the button's image
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE)
    public String Image() {
        return imagePath;
    }

    /**
     * Specifies the path of the button's image.
     *
     * <p/>See {@link com.google.appinventor.components.runtime.util.MediaUtil#determineMediaSource} for information about what
     * a path can be.
     *
     * @param path  the path of the button's image
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
    @SimpleProperty(description = "Specifies the path of the component's image.  " +
            "If there is both an Image and a BackgroundColor, only the Image will be visible.")
    public void Image(String path) {
        // If it's the same as on the prior call and the prior load was successful,
        // do nothing.
        if (path.equals(imagePath) && backgroundImageDrawable != null) {
            return;
        }

        imagePath = (path == null) ? "" : path;

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

    // Update appearance based on values of backgroundImageDrawable, backgroundColor and shape.
    // Images take precedence over background colors.
    private void updateAppearance() {
        // If there is no background image,
        // the appearance depends solely on the background color and shape.
        if (backgroundImageDrawable == null) {
                if (backgroundColor == Component.COLOR_DEFAULT) {
                    // If there is no background image and color is default,
                    // restore original 3D bevel appearance.
                    ViewUtil.setBackgroundDrawable(getView(), defaultButtonDrawable);
                } else {
                    // Clear the background image.
                    ViewUtil.setBackgroundDrawable(getView(), null);
                    getView().setBackgroundColor(backgroundColor);
                }
        } else {
            // If there is a background image
            ViewUtil.setBackgroundImage(getView(), backgroundImageDrawable);
        }
    }
}
