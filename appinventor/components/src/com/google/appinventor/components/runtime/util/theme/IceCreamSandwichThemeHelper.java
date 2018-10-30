// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util.theme;

import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.Window;
import com.google.appinventor.components.runtime.AppInventorCompatActivity;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.ImageViewUtil;

public class IceCreamSandwichThemeHelper implements ThemeHelper {

  private final AppInventorCompatActivity activity;

  public IceCreamSandwichThemeHelper(AppInventorCompatActivity activity) {
    this.activity = activity;
  }

  @Override
  public void requestActionBar() {
    activity.getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
  }

  @Override
  public boolean setActionBarVisible(boolean visible) {
    ActionBar actionBar = activity.getSupportActionBar();
    if (actionBar == null) {
      if (activity instanceof Form) {
        ((Form) activity).dispatchErrorOccurredEvent((Form) activity, "ActionBar", ErrorMessages.ERROR_ACTIONBAR_NOT_SUPPORTED);
      }
      return false;
    } else if (visible) {
      actionBar.show();
    } else {
      actionBar.hide();
    }
    return true;
  }

  @Override
  public boolean hasActionBar() {
    return activity.getSupportActionBar() != null;
  }

  @Override
  public void setTitle(String title) {
    ActionBar actionBar = activity.getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle(title);
    }
  }

  @Override
  public void setActionBarAnimation(boolean enabled) {
    ActionBar actionBar = activity.getSupportActionBar();
    if (actionBar != null) {
      actionBar.setShowHideAnimationEnabled(enabled);
    }
  }

  @Override
  public void setTitle(String title, boolean black) {
    ActionBar actionBar = activity.getSupportActionBar();
    if (actionBar != null) {
      if (black) {
        actionBar.setTitle(Html.fromHtml("<font color=\"black\">" + title + "</font>"));
        ImageViewUtil.setMenuButtonColor(activity, Color.BLACK);
      } else {
        actionBar.setTitle(title);
        ImageViewUtil.setMenuButtonColor(activity, Color.WHITE);
      }
    }
  }
}
