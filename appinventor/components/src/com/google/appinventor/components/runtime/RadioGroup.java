// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

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

import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.PropertyCategory;

import com.google.appinventor.components.runtime.util.ViewUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SimpleObject
public class RadioGroup extends AndroidViewComponent implements Component, ComponentContainer {
  private final Activity context;

  // Layout
  private final int orientation;
  private final android.widget.RadioGroup layoutManager;
  private ViewGroup frameContainer;
  private boolean scrollable = true;

  private int backgroundColor;

  // List of component children
  private List<Component> allChildren = new ArrayList<>();

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
        if ((preferredEmptyWidth == null || preferredEmptyHeight == null) || (getChildCount() != 0)) {
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
    setHorizontalGravity(Gravity.LEFT);
    setVerticalGravity(Gravity.TOP);

    switch (orientation) {
      case ComponentConstants.LAYOUT_ORIENTATION_VERTICAL:
        setOrientation(android.widget.RadioGroup.VERTICAL);
        frameContainer = new ScrollView(context);
        break;
      
      case ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL:
        setOrientation(android.widget.RadioGroup.HORIZONTAL);
        frameContainer = new HorizontalScrollView(context);
        break;
    }

    frameContainer.setLayoutParams(new ViewGroup.LayoutParams(ComponentConstants.EMPTY_HV_ARRANGEMENT_WIDTH, ComponentConstants.EMPTY_HV_ARRANGEMENT_HEIGHT));
    frameContainer.addView(layoutManager, new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));

    container.$add(this);
    BackgroundColor(Component.COLOR_DEFAULT);
  }

  public void setOrientation(int orientation) {
    layoutManager.setOrientation(orientation);
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

  @SimpleProperty(category = PropertyCategory.APPEARANCE,
          description = "Returns the background color of the %type%")
  public int BackgroundColor() {
    return backgroundColor;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
  @SimpleProperty(description = "Specifies the background color of the %type%. ")
  public void BackgroundColor(int argb) {
    backgroundColor = argb;
    getView().setBackgroundColor(argb);
  }

}