// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentConstants;

/**
 * Interface for Simple components.
 *
 */
@SimpleObject
public interface Component {
  /**
   * Returns the dispatch delegate that is responsible for dispatching events
   * for this component.
   */
  public HandlesEventDispatching getDispatchDelegate();

  /*
   * Components asset directory.
   */
  public static final String ASSET_DIRECTORY = "component";

  /*
   * Text alignment constants.
   */
  static final int ALIGNMENT_NORMAL = 0;
  static final int ALIGNMENT_CENTER = 1;
  static final int ALIGNMENT_OPPOSITE = 2;

  /*
   * Accelerometer sensitivity.
   */
  static final int ACCELEROMETER_SENSITIVITY_WEAK = 1;
  static final int ACCELEROMETER_SENSITIVITY_MODERATE = 2;
  static final int ACCELEROMETER_SENSITIVITY_STRONG = 3;

  /*
   * Button Styles.
   */
  static final int BUTTON_SHAPE_DEFAULT = 0;
  static final int BUTTON_SHAPE_ROUNDED = 1;
  static final int BUTTON_SHAPE_RECT = 2;
  static final int BUTTON_SHAPE_OVAL = 3;

  /*
   * Color constants.
   */
  static final int COLOR_NONE = 0x00FFFFFF;
  static final int COLOR_BLACK = 0xFF000000;
  static final int COLOR_BLUE = 0xFF0000FF;
  static final int COLOR_CYAN = 0xFF00FFFF;
  static final int COLOR_DKGRAY = 0xFF444444;
  static final int COLOR_GRAY = 0xFF888888;
  static final int COLOR_GREEN = 0xFF00FF00;
  static final int COLOR_LTGRAY = 0xFFCCCCCC;
  static final int COLOR_MAGENTA = 0xFFFF00FF;
  static final int COLOR_ORANGE = 0xFFFFC800;
  static final int COLOR_PINK = 0xFFFFAFAF;
  static final int COLOR_RED = 0xFFFF0000;
  static final int COLOR_WHITE = 0xFFFFFFFF;
  static final int COLOR_YELLOW = 0xFFFFFF00;
  static final int COLOR_DEFAULT = 0x00000000;

  static final String DEFAULT_VALUE_COLOR_NONE = "&H00FFFFFF";
  static final String DEFAULT_VALUE_COLOR_BLACK = "&HFF000000";
  static final String DEFAULT_VALUE_COLOR_BLUE = "&HFF0000FF";
  static final String DEFAULT_VALUE_COLOR_CYAN = "&HFF00FFFF";
  static final String DEFAULT_VALUE_COLOR_DKGRAY = "&HFF444444";
  static final String DEFAULT_VALUE_COLOR_GRAY = "&HFF888888";
  static final String DEFAULT_VALUE_COLOR_GREEN = "&HFF00FF00";
  static final String DEFAULT_VALUE_COLOR_LTGRAY = "&HFFCCCCCC";
  static final String DEFAULT_VALUE_COLOR_MAGENTA = "&HFFFF00FF";
  static final String DEFAULT_VALUE_COLOR_ORANGE = "&HFFFFC800";
  static final String DEFAULT_VALUE_COLOR_PINK = "&HFFFFAFAF";
  static final String DEFAULT_VALUE_COLOR_RED = "&HFFFF0000";
  static final String DEFAULT_VALUE_COLOR_WHITE = "&HFFFFFFFF";
  static final String DEFAULT_VALUE_COLOR_YELLOW = "&HFFFFFF00";
  static final String DEFAULT_VALUE_COLOR_DEFAULT = "&H00000000";

  /*
   * Font constants.
   */
  static final float FONT_DEFAULT_SIZE = 14;

  /*
   * Layout constants.
   */
  static final int LAYOUT_ORIENTATION_HORIZONTAL = ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL;
  static final int LAYOUT_ORIENTATION_VERTICAL = ComponentConstants.LAYOUT_ORIENTATION_VERTICAL;

  /*
   * Typeface constants.
   */
  static final int TYPEFACE_DEFAULT = 0;
  static final int TYPEFACE_SANSSERIF = 1;
  static final int TYPEFACE_SERIF = 2;
  static final int TYPEFACE_MONOSPACE = 3;

  /*
   * Length constants (for width and height).
   */

  // Note: the values below are duplicated in MockVisibleComponent.java
  // If you change them here, change them there!

  static final int LENGTH_PREFERRED = -1;
  static final int LENGTH_FILL_PARENT = -2;
  static final int LENGTH_UNKNOWN = -3;
  // If the length is <= -1000 then add 1000 and change the sign to
  // get the length is percent of Screen1
  static final int LENGTH_PERCENT_TAG = -1000;

  /*
   * Length constants for toast.
   */
  static final int TOAST_LENGTH_SHORT = 0;
  static final int TOAST_LENGTH_LONG = 1;

  /*
   * Screen direction constants.
   * Observe that opposite directions have the same magnitude but opposite signs.
   */
  static final int DIRECTION_NORTH = 1;
  static final int DIRECTION_NORTHEAST = 2;
  static final int DIRECTION_EAST = 3;
  static final int DIRECTION_SOUTHEAST = 4;
  static final int DIRECTION_SOUTH = -1;
  static final int DIRECTION_SOUTHWEST = -2;
  static final int DIRECTION_WEST = -3;
  static final int DIRECTION_NORTHWEST = -4;
  // Special values
  static final int DIRECTION_NONE = 0;
  static final int DIRECTION_MIN = -4;
  static final int DIRECTION_MAX = 4;

  //Slider defaults for setting MinimumValue and MaximumValue
  public static float SLIDER_MIN_VALUE = 10;
  public static float SLIDER_MAX_VALUE = 50;
  public static float SLIDER_THUMB_VALUE = (SLIDER_MIN_VALUE + SLIDER_MAX_VALUE) / 2.0f;

  static final String DEFAULT_VALUE_TEXT_TO_SPEECH_COUNTRY = "";
  static final String DEFAULT_VALUE_TEXT_TO_SPEECH_LANGUAGE = "";

}
