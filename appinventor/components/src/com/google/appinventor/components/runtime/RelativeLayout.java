// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;

import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.runtime.util.ViewUtil;
import java.util.LinkedList;
import java.util.List;

/**
 * Relative Layout for placing components at a specified position inside the
 * layout. Closely follows the implementation of LinearLayout with certain
 * changes.
 */
public class RelativeLayout implements Layout {

  private final android.widget.RelativeLayout layoutManager;
  private final Handler handler;
  private final List<AndroidViewComponent> componentsToAdd = new LinkedList<>();

  /**
   * Creates a new relative layout with a preferred empty width/height.
   *
   * @param context              view context
   * @param preferredEmptyWidth  the preferred width of an empty layout
   * @param preferredEmptyHeight the preferred height of an empty layout
   */
  RelativeLayout(Context context, final Integer preferredEmptyWidth,
      final Integer preferredEmptyHeight) {
    if (preferredEmptyWidth == null && preferredEmptyHeight != null
        || preferredEmptyWidth != null && preferredEmptyHeight == null) {
      throw new IllegalArgumentException("RelativeLayout - preferredEmptyWidth and "
          + "preferredEmptyHeight must be either both null or both not null");
    }

    handler = new Handler();

    // Create an Android RelativeLayout, but override onMeasure so that we can use
    // our preferred
    // empty width/height.
    layoutManager = new android.widget.RelativeLayout(context) {
      @Override
      protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // If there was no preferred empty width/height specified (see constructors
        // above), just
        // call super. (This is the case for the Form component.)
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
  }

  /**
   * Returns the width.
   * 
   * @return width
   */
  public int getWidth() {
    return layoutManager.getWidth();
  }

  /**
   * Returns the height.
   * 
   * @return height
   */
  public int getHeight() {
    return layoutManager.getHeight();
  }

  @Override
  public ViewGroup getLayoutManager() {
    return layoutManager;
  }

  @Override
  public void add(AndroidViewComponent component) {
    // We cannot add component to layoutManager just yet because component
    // does not have its own x and y coordinates yet.
    component.getView().setLayoutParams(new LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    addComponentLater(component);
  }

  /**
   * Update the position of the component within the layout.
   *
   * @param component  the component whose left or top coordinate has changed
   */
  public void updateComponentPosition(AndroidViewComponent component) {
    int x = component.Left();
    int y = component.Top();
    if (x == ComponentConstants.DEFAULT_X_Y || y == ComponentConstants.DEFAULT_X_Y) {
      return;  // not yet
    }
    View view = component.getView();
    LayoutParams params = (LayoutParams) view.getLayoutParams();
    params.leftMargin = ViewUtil.calculatePixels(view, x);
    params.topMargin = ViewUtil.calculatePixels(view, y);
    view.requestLayout();
  }

  /**
   * Causes addComponent to be called later.
   *
   * @param component component to be added later
   */
  private void addComponentLater(final AndroidViewComponent component) {
    synchronized (componentsToAdd) {
      if (componentsToAdd.size() == 0) {
        componentsToAdd.add(component);
        handler.post(new Runnable() {
          public void run() {
            synchronized (componentsToAdd) {
              List<AndroidViewComponent> copy = new LinkedList<>(componentsToAdd);
              componentsToAdd.clear();
              for (AndroidViewComponent component : copy) {
                addComponent(component);
              }
            }
          }
        });
      } else {
        componentsToAdd.add(component);
      }
    }
  }

  private void addComponent(final AndroidViewComponent component) {
    int x = component.Left();
    int y = component.Top();
    if (x == ComponentConstants.DEFAULT_X_Y || y == ComponentConstants.DEFAULT_X_Y) {
      addComponentLater(component);
    } else {
      ViewGroup.LayoutParams params = component.getView().getLayoutParams();
      LayoutParams newParams = new LayoutParams(params.width, params.height);
      newParams.topMargin = ViewUtil.calculatePixels(component.getView(), y);
      newParams.leftMargin = ViewUtil.calculatePixels(component.getView(), x);
      layoutManager.addView(component.getView(), newParams);
    }
  }

}
