package com.google.appinventor.components.runtime.util.theme;

public class ClassicThemeHelper implements ThemeHelper {
  public ClassicThemeHelper() {
  }

  @Override
  public void requestActionBar() {
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
  }

  @Override
  public void setActionBarAnimation(boolean enabled) {
  }

  @Override
  public void setTitle(String title, boolean black) {
    setTitle(title);
  }
}
