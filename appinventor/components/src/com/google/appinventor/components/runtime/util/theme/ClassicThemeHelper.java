// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util.theme;

public class ClassicThemeHelper implements ThemeHelper {
  @Override
  public void requestActionBar() {
    // ActionBar not supported in Classic theme
  }

  @Override
  public boolean setActionBarVisible(boolean visible) {
    return false;
  }

  @Override
  public boolean hasActionBar() {
    return false;
  }

  @Override
  public void setTitle(String title) {
    // ActionBar not supported in Classic theme
  }

  @Override
  public void setActionBarAnimation(boolean enabled) {
    // ActionBar not supported in Classic theme
  }

  @Override
  public void setTitle(String title, boolean black) {
    // ActionBar not supported in Classic theme
  }
}
