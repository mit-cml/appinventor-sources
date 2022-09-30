// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.view.Gravity;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.HorizontalAlignment;
import com.google.appinventor.components.common.VerticalAlignment;
import com.google.appinventor.components.runtime.LinearLayout;

/**
 * Utilities for centering contents of arrangements and screen
 *
 * @author hal@mit.edu (Hal Abelson)
 */
public class AlignmentUtil {
  LinearLayout viewLayout;

  public AlignmentUtil() {
  }

  public AlignmentUtil(LinearLayout viewLayout) {
    this.viewLayout = viewLayout;
  }

  /**
   * Sets the horizontal alignment (gravity) of the alignment
   * @param alignment
   */
  public void setHorizontalAlignment(int alignment){
    viewLayout.setHorizontalGravity(getHorizontalAlignment(alignment));
  }

  public void setHorizontalAlignment(HorizontalAlignment alignment){
    viewLayout.setHorizontalGravity(getHorizontalAlignment(alignment));
  }

  /**
   * Sets the vertical alignment of the view layout.
   * @param alignment
   */
  public void setVerticalAlignment(int alignment){
    viewLayout.setVerticalGravity(getVerticalAlignment(alignment));
  }

  public void setVerticalAlignment(VerticalAlignment alignment) {
    viewLayout.setVerticalGravity(getVerticalAlignment(alignment));
  }

  /**
   * These getters are used in case someone's
   * viewlayout object is not of LinearLayout type. Used in RadioGroup component.
   */
  
  /**
   * Gets the horizontal alignment (gravity) of the alignment
   * Throws an IllegalArgumentException if alignment has illegal value.
   * @param alignment
   */
  public int getHorizontalAlignment(int alignment) throws IllegalArgumentException {
    switch (alignment) {
      case ComponentConstants.GRAVITY_LEFT:
        return Gravity.LEFT;
      case ComponentConstants.GRAVITY_RIGHT:
        return Gravity.RIGHT;
      case ComponentConstants.GRAVITY_CENTER_HORIZONTAL:
        return Gravity.CENTER_HORIZONTAL;
      default:
        throw new IllegalArgumentException("Bad value to setHorizontalAlignment: " + alignment);
    }
  }

  public int getHorizontalAlignment(HorizontalAlignment alignment) throws IllegalArgumentException {
    switch (alignment) {
      case Left:
        return Gravity.LEFT;
      case Right:
        return Gravity.RIGHT;
      case Center:
        return Gravity.CENTER_HORIZONTAL;
      default:
        throw new IllegalArgumentException("Bad value to setHorizontalAlignment: " + alignment);
    }
  }

  /**
   * Gets the vertical alignment (gravity) of the alignment
   * Throws an IllegalArgumentException if alignment has illegal value.
   * @param alignment
   */
  public int getVerticalAlignment(int alignment) throws IllegalArgumentException {
    switch (alignment) {
      case ComponentConstants.GRAVITY_TOP:
        return Gravity.TOP;
      case ComponentConstants.GRAVITY_CENTER_VERTICAL:
        return Gravity.CENTER_VERTICAL;
      case ComponentConstants.GRAVITY_BOTTOM:
        return Gravity.BOTTOM;
      default:
        throw new IllegalArgumentException("Bad value to setHorizontalAlignment: " + alignment);
    }
  }

  public int getVerticalAlignment(VerticalAlignment alignment) throws IllegalArgumentException {
    switch (alignment) {
      case Top:
        return Gravity.TOP;
      case Center:
        return Gravity.CENTER_VERTICAL;
      case Bottom:
        return Gravity.BOTTOM;
      default:
        throw new IllegalArgumentException("Bad value to setHorizontalAlignment: " + alignment);
    }
  }
  
}






