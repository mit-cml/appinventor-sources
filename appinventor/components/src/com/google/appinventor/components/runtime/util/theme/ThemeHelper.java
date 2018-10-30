// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util.theme;

public interface ThemeHelper {
  /**
   * Request the ActionBar functionality fot the platform, if available.
   */
  void requestActionBar();

  /**
   * Change the ActionBar visibility
   * @param visible true if the ActionBar should be made visible, otherwise false.
   * @return true if the ActionBar is available. If false is returned, no future requests for the
   * ActionBar will be honored (usually due to being in Classic theme mode).
   */
  boolean setActionBarVisible(boolean visible);

  /**
   * Tests whether the theme supports ActionBar.
   * @return true if ActionBar is supported in the current theme, otherwise false.
   */
  boolean hasActionBar();

  /**
   * Sets the title of the activity in the ActionBar.
   * @param title New title text for the ActionBar
   */
  void setTitle(String title);

  /**
   * Sets whether the ActionBar appear/disappear animation is shown (AppCompat mode only).
   * @param enabled true if the animation should be used when visibility of the ActionBar is toggled, otherwise false.
   */
  void setActionBarAnimation(boolean enabled);

  /**
   * Sets the title of the ActionBar and its color. If {@code black} is true, the helper should attempt to make the
   * text black. Otherwise, the text will be white.
   * @param title the new title for the ActionBar
   * @param black true if black text is desired, false if white text is desired
   */
  void setTitle(String title, boolean black);
}
