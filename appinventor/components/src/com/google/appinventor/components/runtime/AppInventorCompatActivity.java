// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.runtime.util.PaintUtil;

/**
 * AppInventorCompatActivity provides a base implementation of Activity that handles the styling of
 * the application. This allows the user to change the Theme of the application in the REPL (to the
 * best of our ability). Any Activity in App Inventor should now extend this class to get the
 * correct theme.
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class AppInventorCompatActivity extends Activity implements AppCompatCallback {

  public enum Theme {
    PACKAGED,
    CLASSIC,
    DEVICE_DEFAULT,
    BLACK_TITLE_TEXT,
    DARK
  }

  private static final String LOG_TAG = AppInventorCompatActivity.class.getSimpleName();
  static final int DEFAULT_PRIMARY_COLOR = PaintUtil.hexStringToInt(ComponentConstants.DEFAULT_PRIMARY_COLOR);
  private static boolean classicMode;
  private static boolean actionBarEnabled;
  private static Theme currentTheme = Theme.PACKAGED;
  private static int primaryColor;
  private AppCompatDelegate appCompatDelegate;
  android.widget.LinearLayout frameWithTitle;
  TextView titleBar;

  @Override
  public void onCreate(Bundle icicle) {
    if (currentTheme != Theme.PACKAGED) {
      applyTheme();
    }
    Window.Callback classicCallback = getWindow().getCallback();
    appCompatDelegate = AppCompatDelegate.create(this, this);
    if (currentTheme == Theme.CLASSIC) {
      appCompatDelegate = null;
      AppInventorCompatActivity.classicMode = true;
      getWindow().setCallback(classicCallback);
    } else {
      try {
        appCompatDelegate.onCreate(icicle);
      } catch (IllegalStateException e) {
        // Thrown in "Classic" mode
        Log.d(LOG_TAG, "IllegalStateException thrown in onCreate");
        appCompatDelegate = null;
        AppInventorCompatActivity.classicMode = true;
        getWindow().setCallback(classicCallback);
      }
    }

    super.onCreate(icicle);

    frameWithTitle = new android.widget.LinearLayout(this);
    frameWithTitle.setOrientation(android.widget.LinearLayout.VERTICAL);
    titleBar = (TextView) findViewById(android.R.id.title);
    if (shouldCreateTitleBar()) {
      titleBar = new TextView(this);
      titleBar.setBackgroundResource(android.R.drawable.title_bar);
      titleBar.setTextAppearance(this, android.R.style.TextAppearance_WindowTitle);
      titleBar.setGravity(Gravity.CENTER_VERTICAL);
      titleBar.setSingleLine();
      titleBar.setShadowLayer(2, 0, 0, 0xBB000000);
      if (!isClassicMode()) {
        frameWithTitle.addView(titleBar, new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
      }
    }
    setContentView(frameWithTitle);
  }

  @SuppressWarnings("WeakerAccess")
  public final boolean isAppCompatMode() {
    return appCompatDelegate != null;
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    if (appCompatDelegate != null) {
      appCompatDelegate.onPostCreate(savedInstanceState);
    }
  }

  @Override
  protected void onPostResume() {
    super.onPostResume();
    if (appCompatDelegate != null) {
      appCompatDelegate.onPostResume();
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (appCompatDelegate != null) {
      appCompatDelegate.onConfigurationChanged(newConfig);
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (appCompatDelegate != null) {
      appCompatDelegate.onStop();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (appCompatDelegate != null) {
      appCompatDelegate.onDestroy();
    }
  }

  @Override
  protected void onTitleChanged(CharSequence title, int color) {
    super.onTitleChanged(title, color);
    if (appCompatDelegate != null) {
      appCompatDelegate.setTitle(title);
    }
    if (isAppCompatMode() && titleBar != null) {
      titleBar.setText(title);
    }
  }

  @Override
  public void onSupportActionModeStarted(ActionMode actionMode) {
    // Do nothing
  }

  @Override
  public void onSupportActionModeFinished(ActionMode actionMode) {
    // Do nothing
  }

  @Nullable
  @Override
  public ActionMode onWindowStartingSupportActionMode(Callback callback) {
    return null;  // Let's Android decide best action
  }

  @Override
  public void setContentView(View view) {
    // If we are a custom activity, wrap it in frameWithTitle for theme
    if (view != frameWithTitle) {
      frameWithTitle.addView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT));
      view = frameWithTitle;
    }

    // Update the content view based on AppCompat support
    if (appCompatDelegate != null) {
      appCompatDelegate.setContentView(view);
    } else {
      super.setContentView(view);
    }
  }

  @SuppressWarnings("WeakerAccess")  // To maintain visibility compatibility with AppCompatActivity
  public ActionBar getSupportActionBar() {
    Window.Callback classicCallback = getWindow().getCallback();
    try {
      return appCompatDelegate == null ? null : appCompatDelegate.getSupportActionBar();
    } catch (IllegalStateException e) {
      // Thrown in "Classic" mode
      Log.d(LOG_TAG, "IllegalStateException thrown in getSupportActionBar");
      appCompatDelegate = null;
      AppInventorCompatActivity.classicMode = true;
      getWindow().setCallback(classicCallback);
      return null;
    }
  }

  @SuppressWarnings("unused") // Potentially useful for extensions adding custom activities
  protected static boolean isActionBarEnabled() {
    return actionBarEnabled;
  }

  @SuppressWarnings("WeakerAccess")
  protected void setActionBarEnabled(boolean enabled) {
    actionBarEnabled = enabled;
  }

  @SuppressWarnings("unused")  // Potentially useful for extensions adding custom activities
  public static boolean isClassicMode() {
    return classicMode;
  }

  @SuppressWarnings("WeakerAccess")
  protected void setClassicMode(boolean classic) {
    if (isRepl()) {  // Only allow changes in REPL
      classicMode = classic;
    }
  }

  protected static int getPrimaryColor() {
    return primaryColor;
  }

  @SuppressWarnings("WeakerAccess")
  protected void setPrimaryColor(int color) {
    final ActionBar actionBar = getSupportActionBar();
    int newColor = color == Component.COLOR_DEFAULT ? DEFAULT_PRIMARY_COLOR : color;
    if (actionBar != null && newColor != primaryColor) {
      // Only make the change if we have to...
      primaryColor = newColor;
      actionBar.setBackgroundDrawable(new ColorDrawable(color));
    }
  }

  protected boolean isRepl() {
    return false;
  }

  @SuppressWarnings("WeakerAccess") // Potentially useful for extensions adding custom activities
  protected void hideTitleBar() {
    if (titleBar != null) {
      if (titleBar.getParent() != frameWithTitle) {
        ((View)titleBar.getParent()).setVisibility(View.GONE);
      } else {
        titleBar.setVisibility(View.GONE);
      }
    }
  }

  protected void maybeShowTitleBar() {
    Log.d(LOG_TAG, "maybeShowTitleBar");
    if (titleBar != null) {
      titleBar.setVisibility(View.VISIBLE);
      Log.d(LOG_TAG, "titleBar visible");
      if (titleBar.getParent() != null && titleBar.getParent() != frameWithTitle) {
        Log.d(LOG_TAG, "Setting parent visible");
        ((View) titleBar.getParent()).setVisibility(View.VISIBLE);
      }
    }
  }

  @SuppressWarnings("WeakerAccess") // Potentially useful for extensions adding custom activities
  protected void styleTitleBar() {
    ActionBar actionBar = getSupportActionBar();
    Log.d(LOG_TAG, "actionBarEnabled = " + actionBarEnabled);
    Log.d(LOG_TAG, "!classicMode = " + !classicMode);
    Log.d(LOG_TAG, "actionBar = " + actionBar);
    if (actionBar != null) {
      actionBar.setBackgroundDrawable(new ColorDrawable(getPrimaryColor()));
      if (actionBarEnabled) {
        actionBar.show();
        hideTitleBar();
        return;
      } else {
        actionBar.hide();
      }
    }
    maybeShowTitleBar();
  }

  @SuppressWarnings("WeakerAccess")
  protected void setAppInventorTheme(Theme theme) {
    if (!Form.getActiveForm().isRepl()) return;  // Theme changing only allowed in REPL
    if (theme == currentTheme) {
      return;
    }
    currentTheme = theme;
    applyTheme();
  }

  private void applyTheme() {
    Log.d(LOG_TAG, "applyTheme " + currentTheme);
    setClassicMode(false);
    switch (currentTheme) {
      case CLASSIC:
        setClassicMode(true);
        setTheme(android.R.style.Theme);
        break;
      case DEVICE_DEFAULT:
      case BLACK_TITLE_TEXT:
        setTheme(android.R.style.Theme_DeviceDefault_Light_NoActionBar);
        break;
      case DARK:
        setTheme(android.R.style.Theme_DeviceDefault_NoActionBar);
        break;
    }
  }

  private boolean shouldCreateTitleBar() {
    if (isAppCompatMode() && (getSupportActionBar() == null || !isActionBarEnabled())) {
      return true;
    } else if (titleBar == null && isRepl()) {
      return true;
    }
    return false;
  }
}
