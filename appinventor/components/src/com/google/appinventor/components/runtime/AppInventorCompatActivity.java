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
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.theme.ClassicThemeHelper;
import com.google.appinventor.components.runtime.util.theme.HoneycombThemeHelper;
import com.google.appinventor.components.runtime.util.theme.IceCreamSandwichThemeHelper;
import com.google.appinventor.components.runtime.util.theme.ThemeHelper;

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
  private static boolean classicMode = false;
  private static boolean actionBarEnabled;
  private static Theme currentTheme = Theme.PACKAGED;
  private static int primaryColor;
  private AppCompatDelegate appCompatDelegate;
  android.widget.LinearLayout frameWithTitle;
  TextView titleBar;
  private static boolean didSetClassicModeFromYail = false;
  @SuppressWarnings("WeakerAccess")  // Potentially useful to extensions with custom activities
  protected ThemeHelper themeHelper;

  @Override
  public void onCreate(Bundle icicle) {
    classicMode = classicMode || SdkLevel.getLevel() < SdkLevel.LEVEL_HONEYCOMB;
    if (classicMode) {
      themeHelper = new ClassicThemeHelper();
    } else if (SdkLevel.getLevel() < SdkLevel.LEVEL_ICE_CREAM_SANDWICH) {
      // On Honeycomb, so requesting the ActionBar
      themeHelper = new HoneycombThemeHelper(this);
      themeHelper.requestActionBar();
      actionBarEnabled = true;
    } else {
      // AppCompat libraries require minSdk 14 (Ice Cream Sandwich). Therefore, we only need to
      // run this code if we are on Ice Cream Sandwich or higher and in a newer (i.e., non-Classic)
      // theme.
      themeHelper = new IceCreamSandwichThemeHelper(this);
      if (currentTheme != Theme.PACKAGED) {
        applyTheme();
      }
      appCompatDelegate = AppCompatDelegate.create(this, this);
      appCompatDelegate.onCreate(icicle);
    }

    super.onCreate(icicle);

    frameWithTitle = new android.widget.LinearLayout(this);
    frameWithTitle.setOrientation(android.widget.LinearLayout.VERTICAL);
    setContentView(frameWithTitle);  // Due to a bug in Honeycomb 3.0 and 3.1, a content view must
                                     // exist before attempting to check the ActionBar status,
                                     // which is done indirectly via shouldCreateTitleBar()
    actionBarEnabled = themeHelper.hasActionBar();
    titleBar = (TextView) findViewById(android.R.id.title);
    if (shouldCreateTitleBar()) {
      titleBar = new TextView(this);
      titleBar.setBackgroundResource(android.R.drawable.title_bar);
      titleBar.setTextAppearance(this, android.R.style.TextAppearance_WindowTitle);
      titleBar.setGravity(Gravity.CENTER_VERTICAL);
      titleBar.setSingleLine();
      titleBar.setShadowLayer(2, 0, 0, 0xBB000000);
      if (!isClassicMode() || SdkLevel.getLevel() < SdkLevel.LEVEL_HONEYCOMB) {
        // Since AppCompat requires SDK 14 or higher, all apps prior to that will receive the
        // "classic" theme. However, if the app has a different theme then we will end up without
        // a title bar even though isClassicMode() will return true. Here we add the title bar
        // if we are the REPL and not in classic mode (so we can simulate Classic title bar) or
        // on an older device regardless of theme.
        frameWithTitle.addView(titleBar, new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
      }
    } else {
      Log.d(LOG_TAG, "Already have a title bar (classic mode): " + titleBar);
    }
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
    } else if (titleBar != null) {
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
      appCompatDelegate = null;
      AppInventorCompatActivity.classicMode = true;
      getWindow().setCallback(classicCallback);
      return null;
    }
  }

  public static boolean isEmulator() {
    return android.os.Build.PRODUCT.contains("google_sdk") ||  // Old emulator build (2.x)
        android.os.Build.PRODUCT.equals("sdk") ||              // Honeycomb image (for testing)
        android.os.Build.PRODUCT.contains("sdk_gphone");       // New emulator build (3.x)
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
    if (isRepl() && SdkLevel.getLevel() >= SdkLevel.LEVEL_HONEYCOMB) {  // Only allow changes in REPL when running on a supported SDK level
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
        if (titleBar.getParent() != null) {
          ((View)titleBar.getParent()).setVisibility(View.GONE);
        }
      } else {
        titleBar.setVisibility(View.GONE);
      }
    }
  }

  protected void maybeShowTitleBar() {
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
    if (isAppCompatMode() && (!themeHelper.hasActionBar() || !isActionBarEnabled())) {
      return true;
    } else if (titleBar == null && (isRepl() || classicMode)) {
      return true;
    }
    return false;
  }

  @SuppressWarnings("unused")  // Called from YAIL
  public static void setClassicModeFromYail(boolean newClassicMode) {
    if (!didSetClassicModeFromYail) {  // Check so we only do this once (from Screen1)
      Log.d(LOG_TAG, "Setting classic mode from YAIL: " + newClassicMode);
      classicMode = newClassicMode;
      didSetClassicModeFromYail = true;
    }
  }
}
