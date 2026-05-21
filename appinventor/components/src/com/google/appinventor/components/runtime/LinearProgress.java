// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2023 Kodular, All rights reserved
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.Context;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.Log;
import android.widget.ProgressBar;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;


@DesignerComponent(
    version = YaVersion.LINEAR_PROGRESS_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "A visible component that indicates the progress of an operation using an animated linear bar.",
    iconName = "images/linearProgress.png"
)
@SimpleObject

public final class LinearProgress extends AndroidViewComponent {
  private static final String LOG_TAG = "LinearProgress";
  private Context context;
  private ProgressBar progressBar;
  private int progressColor = Component.COLOR_BLUE;
  private int indeterminateColor = Component.COLOR_BLUE;

  public LinearProgress(ComponentContainer container) {
    super(container);
    context = container.$context();

    progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);

    container.$add(this);

    Minimum(0);
    Maximum(100);
    ProgressColor(Component.COLOR_BLUE);
    IndeterminateColor(Component.COLOR_BLUE);
    Indeterminate(true);
    Width(LENGTH_FILL_PARENT);
    Log.d(LOG_TAG, "Linear Progress created");
  }

  @Override
  public ProgressBar getView() {
    return progressBar;
  }

  @Override
  public int Height() {
    return getView().getHeight();
  }

  @Override
  public void Height(int height) {
    container.setChildHeight(this, height);
  }

  @Override
  public void HeightPercent(int height) {
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "0")
  @SimpleProperty(description = "Set the lower range of the progress bar to min. "
      + "This function works only for devices with API >= 26",
      category = PropertyCategory.BEHAVIOR)
  public void Minimum(int value) {
    if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      progressBar.setMin(value);
      Log.i(LOG_TAG, "setMin = " + value);
    } else {
      Log.i(LOG_TAG, "setMin of progress bar is not possible. API is " + VERSION.SDK_INT);
    }
  }

  @SimpleProperty
  public int Minimum() {
    return VERSION.SDK_INT >= Build.VERSION_CODES.O ? progressBar.getMin() : 0;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "100")
  @SimpleProperty(description = "Set the upper range of the progress bar max.",
      category = PropertyCategory.BEHAVIOR)
  public void Maximum(int value) {
    progressBar.setMax(value);
    Log.i(LOG_TAG, "setMax = " + value);
  }

  @SimpleProperty
  public int Maximum() {
    return progressBar.getMax();
  }

  @SimpleProperty(description = "Sets the current progress to the specified value. "
      + "Does not do anything if the progress bar is in indeterminate mode.")
  public void Progress(int value) {
    if (VERSION.SDK_INT >= 24) {
      progressBar.setProgress(value, true);
    } else {
      progressBar.setProgress(value);
    }
    ProgressChanged(progressBar.getProgress());
  }

  @SimpleProperty(description = "Get the progress bar's current level of progress.")
  public int Progress() {
    return progressBar.getProgress();
  }

  @SimpleFunction(description = "Increase the progress bar's progress by the specified amount.")
  public void IncrementProgressBy(int value) {
    progressBar.incrementProgressBy(value);
    ProgressChanged(progressBar.getProgress());
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_BLUE)
  @SimpleProperty(description = "Change the progress color of the progress bar.",
      category = PropertyCategory.APPEARANCE)
  public void ProgressColor(int color) {
    this.progressColor = color;
    Drawable drawable = progressBar.getProgressDrawable();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      drawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_IN));
    } else {
      drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
    Log.i(LOG_TAG, "Progress Color = " + color);
  }

  @SimpleProperty
  public int ProgressColor() {
    return this.progressColor;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_BLUE)
  @SimpleProperty(description = "Change the indeterminate color of the progress bar.",
      category = PropertyCategory.APPEARANCE)
  public void IndeterminateColor(int color) {
    this.indeterminateColor = color;
    Drawable drawable = progressBar.getProgressDrawable();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      drawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_IN));
    } else {
      drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
    Log.i(LOG_TAG, "Indeterminate Color = " + color);
  }

  @SimpleProperty
  public int IndeterminateColor() {
    return this.indeterminateColor;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(description = "Change the indeterminate mode for this progress bar. "
      + "In indeterminate mode, the progress is ignored and the "
      + "progress bar shows an infinite animation instead.",
      category = PropertyCategory.BEHAVIOR)
  public void Indeterminate(boolean enabled) {
    progressBar.setIndeterminate(enabled);
    Log.i(LOG_TAG, "Indeterminate is: " + enabled);
  }

  @SimpleProperty(description = "Indicate whether this progress bar is in indeterminate mode.")
  public boolean Indeterminate() {
    return progressBar.isIndeterminate();
  }

  @SimpleEvent(description = "Event that indicates that the progress of the progress bar has "
      + "been changed. Returns the current progress value. "
      + "If \"Indeterminate\" is set to true, then it returns \"0\".")
  public void ProgressChanged(int progress) {
    EventDispatcher.dispatchEvent(this, "ProgressChanged", progress);
  }
}
