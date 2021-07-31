// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.drawable.Drawable;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup;

import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.HorizontalAlignment;
import com.google.appinventor.components.common.VerticalAlignment;

import com.google.appinventor.components.annotations.Asset;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.PropertyCategory;

import com.google.appinventor.components.runtime.util.AlignmentUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.ViewUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A RadioGroup is used for set of radio buttons. If we check one 
 * radio button that belongs to a radio group, it automatically unchecks any
 * previously checked radio button within the same group.
 *
 * @author thamihardik8@gmail.com (Hardik Thami)
 */

@SimpleObject
public class RadioGroup extends AndroidViewComponent implements Component, ComponentContainer {
  private final Activity context;

  // Layout
  private final int orientation;
  private final android.widget.RadioGroup layoutManager;
  private ViewGroup frameContainer;
  private boolean scrollable = true;
  
  // Backing for background color
  private int backgroundColor;

  // This is the Drawable corresponding to the Image property.
  // If an Image has never been set or if the most recent Image could not be loaded, this is null.
  private Drawable backgroundImageDrawable;

  // Image path
  private String imagePath = "";

  // Alignment
  private AlignmentUtil alignmentSetter = new AlignmentUtil();
  private HorizontalAlignment horizontalAlignment = HorizontalAlignment.Left;
  private VerticalAlignment verticalAlignment = VerticalAlignment.Top;

  // List of component children
  private List<Component> allChildren = new ArrayList<>();

  private Drawable defaultButtonDrawable;

  private final Handler androidUIHandler = new Handler();

  public RadioGroup(ComponentContainer container, int orientation) {
    this(container, orientation, null, null);
  }

  public RadioGroup(ComponentContainer container, int orientation, final Integer preferredEmptyWidth, 
      final Integer preferredEmptyHeight) {
    
    super(container);
    context = container.$context();

    if (preferredEmptyWidth == null && preferredEmptyHeight != null ||
        preferredEmptyWidth != null && preferredEmptyHeight == null) {
      throw new IllegalArgumentException("RadioGroup - preferredEmptyWidth and " +
          "preferredEmptyHeight must be either both null or both not null");
    }

    this.orientation = orientation;

    // Create an Android RadioGroup, but override onMeasure so that we can use our preferred
    // empty width/height.
    layoutManager = new android.widget.RadioGroup(context){
      @Override
      protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // If there was no preferred empty width/height specified (see constructors above) or 
        // If the layout has any children, just call super.
        if (preferredEmptyWidth == null || preferredEmptyHeight == null) {
          super.onMeasure(widthMeasureSpec, heightMeasureSpec);
          return;
        }

        // If the layout has any children, just call super.
        if (getChildCount() != 0) {
          super.onMeasure(widthMeasureSpec, heightMeasureSpec);
          return;
        }

        setMeasuredDimension(getSize(widthMeasureSpec, preferredEmptyWidth),
                             getSize(heightMeasureSpec, preferredEmptyHeight));
      }

      private int getSize(int measureSpec, int preferredSize) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
        // We were told how big to be
          result = specSize;
        } else {
        // Use the preferred size.
          result = preferredSize;
          if (specMode == MeasureSpec.AT_MOST) {
          // Respect AT_MOST value if that was what is called for by measureSpec
            result = Math.min(result, specSize);
          }
        }

        return result;
      }
    };

    setBaselineAligned(false);
    setHorizontalGravity(alignmentSetter.getHorizontalAlignment(horizontalAlignment));
    setVerticalGravity(alignmentSetter.getVerticalAlignment(verticalAlignment));

    switch (orientation) {
      case ComponentConstants.LAYOUT_ORIENTATION_VERTICAL:
        layoutManager.setOrientation(android.widget.RadioGroup.VERTICAL);
        frameContainer = new ScrollView(context);
        break;
      
      case ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL:
        layoutManager.setOrientation(android.widget.RadioGroup.HORIZONTAL);
        frameContainer = new HorizontalScrollView(context);
        break;
    }

    frameContainer.setLayoutParams(new ViewGroup.LayoutParams(ComponentConstants.EMPTY_HV_ARRANGEMENT_WIDTH, ComponentConstants.EMPTY_HV_ARRANGEMENT_HEIGHT));
    frameContainer.addView(layoutManager, new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));

    // Save the default values in case the user wants them back later.
    defaultButtonDrawable = getView().getBackground();
    
    container.$add(this);
    BackgroundColor(Component.COLOR_DEFAULT);
  }

  public void setHorizontalGravity(int gravity) {
    layoutManager.setHorizontalGravity(gravity);
  }

  public void setVerticalGravity(int gravity) {
    layoutManager.setVerticalGravity(gravity);
  }

  public void setBaselineAligned(boolean baselineAligned) { 
    layoutManager.setBaselineAligned(baselineAligned); 
  }

  public ComponentContainer container() {
    return this.container;
  }
  
  // ComponentContainer Implementation

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
    layoutManager.addView(component.getView(), new android.widget.RadioGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,  // width
        ViewGroup.LayoutParams.WRAP_CONTENT,  // height
        0f));
    allChildren.add(component);
  }

  @Override
  public List<? extends Component> getChildren() {
    return allChildren;
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
            setChildWidth(component, fWidth, trycount + 1);
          }
        }, 100);                // Try again in 1/10 of a second
    }
    if (width <= LENGTH_PERCENT_TAG) {
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
    return frameContainer;
  }

  /**
   * Returns a number that encodes how contents of the %type% are aligned horizontally.
   * The choices are: 1 = left aligned, 2 = right aligned, 3 = horizontally centered.
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "A number that encodes how contents of the %type% are aligned " +
          " horizontally. The choices are: 1 = left aligned, 2 = right aligned, " +
          " 3 = horizontally centered.  Alignment has no effect if the arrangement's width is " +
          "automatic.")
  public @Options(HorizontalAlignment.class) int AlignHorizontal() {
    return AlignHorizontalAbstract().toUnderlyingValue();
  }

  /**
   * Returns the current horizontal alignment of this arrangement.
   */
  @SuppressWarnings("RegularMethodName")
  public HorizontalAlignment AlignHorizontalAbstract() {
    return horizontalAlignment;
  }

  /**
   * Sets the arrangements horizontal alignment to the given value.
   * @param alignment the alignment to set the arrangement to.
   */
  @SuppressWarnings("RegularMethodName")
  public void AlignHorizontalAbstract(HorizontalAlignment alignment) {
    setHorizontalGravity(alignmentSetter.getHorizontalAlignment(alignment));
    horizontalAlignment = alignment;
  }

  /**
   * A number that encodes how contents of the `%type%` are aligned horizontally. The choices
   * are: `1` = left aligned, `2` = right aligned, `3` = horizontally centered. Alignment has no
   * effect if the `%type%`'s {@link #Width()} is `Automatic`.
   *
   * @param alignment
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_HORIZONTAL_ALIGNMENT,
      defaultValue = ComponentConstants.HORIZONTAL_ALIGNMENT_DEFAULT + "")
  @SimpleProperty
  public void AlignHorizontal(@Options(HorizontalAlignment.class) int alignment) {
    // Make sure alignment is a valid HorizontalAlignment.
    HorizontalAlignment align = HorizontalAlignment.fromUnderlyingValue(alignment);
    if (align == null) {
      container.$form().dispatchErrorOccurredEvent(this, "HorizontalAlignment",
          ErrorMessages.ERROR_BAD_VALUE_FOR_HORIZONTAL_ALIGNMENT, alignment);
      return;
    }
    AlignHorizontalAbstract(align);
  }

  /**
   * Returns a number that encodes how contents of the %type% are aligned vertically.
   * The choices are: 1 = aligned at the top, 2 = vertically centered, 3 = aligned at the bottom.
   * Alignment has no effect if the arrangement's height is automatic.
   */
   @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "A number that encodes how the contents of the %type% are aligned " +
          " vertically. The choices are: 1 = aligned at the top, 2 = vertically centered, " +
          "3 = aligned at the bottom.  Alignment has no effect if the arrangement's height " +
          "is automatic.")
  public @Options(VerticalAlignment.class) int AlignVertical() {
    return AlignVerticalAbstract().toUnderlyingValue();
  }

  /**
   * Returns the current vertical alignment of this arrangement.
   */
  @SuppressWarnings("RegularMethodName")
  public VerticalAlignment AlignVerticalAbstract() {
    return verticalAlignment;
  }

  /**
   * Sets the arrangements vertical alignment to the given value.
   * @param alignment the alignment to set the arrangement to.
   */
  @SuppressWarnings("RegularMethodName")
  public void AlignVerticalAbstract(VerticalAlignment alignment) {
    setVerticalGravity(alignmentSetter.getVerticalAlignment(alignment));
    verticalAlignment = alignment;
  }

  /**
   * A number that encodes how the contents of the `%type%` are aligned vertically. The choices
   * are: `1` = aligned at the top, `2` = vertically centered, `3` = aligned at the bottom.
   * Alignment has no effect if the `%type%`'s {@link #Height()} is `Automatic`.
   *
   * @param alignment
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_VERTICAL_ALIGNMENT,
      defaultValue = ComponentConstants.VERTICAL_ALIGNMENT_DEFAULT + "")
  @SimpleProperty
  public void AlignVertical(@Options(VerticalAlignment.class) int alignment) {
    // Make sure alignment is a valid VerticalAlignment.
    VerticalAlignment align = VerticalAlignment.fromUnderlyingValue(alignment);
    if (align == null) {
      container.$form().dispatchErrorOccurredEvent(this, "VerticalAlignment",
          ErrorMessages.ERROR_BAD_VALUE_FOR_VERTICAL_ALIGNMENT, alignment);
      return;
    }
    AlignVerticalAbstract(align);
  }

  /**
  * Returns the background color of the %type% as an alpha-red-green-blue
  * integer.
  *
  * @return  background RGB color with alpha
  */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
          description = "Returns the background color of the %type%")
  public int BackgroundColor() {
    return backgroundColor;
  }

  /**
  * Specifies the background color of the %type% as an alpha-red-green-blue
  * integer.
  *
  * @param argb background RGB color with alpha
  */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
  @SimpleProperty(description = "Specifies the background color of the %type%. " +
          "The background color will not be visible if an Image is being displayed.")
  public void BackgroundColor(int argb) {
    backgroundColor = argb;
    // getView().setBackgroundColor(argb);
    updateAppearance();
  }

  /**
   * Returns the path of the background image of the `%type%`.
   *
   * @return  the path of the background image
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public String Image() {
    return imagePath;
  }

  /**
   * Specifies the path of the background image of the `%type%`.
   *
   * @internaldoc
   * <p/>See {@link com.google.appinventor.components.runtime.util.MediaUtil#determineMediaSource} for information about what
   * a path can be.
   *
   * @param path the path of the background image
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
  @SimpleProperty(description = "Specifies the path of the background image for the %type%.  " +
          "If there is both an Image and a BackgroundColor, only the Image will be visible.")
  public void Image(@Asset String path) {
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
        ViewUtil.setBackgroundDrawable(layoutManager, defaultButtonDrawable);
      } else {
        // Clear the background image.
        ViewUtil.setBackgroundDrawable(layoutManager, null);
        layoutManager.setBackgroundColor(backgroundColor);
      }
    } else {
      // If there is a background image
      ViewUtil.setBackgroundImage(layoutManager, backgroundImageDrawable);
    }
  }
}