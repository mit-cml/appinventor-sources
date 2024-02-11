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
import android.util.Log;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.IsColor;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;


@DesignerComponent(
    version = YaVersion.CIRCULAR_PROGRESS_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "A visible component that indicates the progress of an operation using an animated loop.",
    iconName = "images/circularProgress.png"
)
@SimpleObject

public final class CircularProgress extends AndroidViewComponent {
  private static final String LOG_TAG = "CircularProgress";
  private Context context;
  private ProgressBar progressBar;
  private LayoutParams layoutParams;
  private int indeterminateColor = Component.COLOR_BLUE;

  public CircularProgress(ComponentContainer container) {
    super(container);
    context = container.$context();

    layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyle);
    progressBar.setLayoutParams(layoutParams);

    container.$add(this);

    Color(Component.COLOR_BLUE);
    Log.d(LOG_TAG, "Circular Progress created");
  }

  @Override
  public ProgressBar getView() {
    return progressBar;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_BLUE)
  @SimpleProperty(description = "Change the indeterminate color of the circular progress bar.",
      category = PropertyCategory.APPEARANCE)
  public void Color(int color) {
    this.indeterminateColor = color;
    Drawable drawable = progressBar.getIndeterminateDrawable();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      drawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_IN));
    } else {
      drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
    Log.i(LOG_TAG, "Indeterminate Color = " + color);
  }

  @SimpleProperty
  @IsColor
  public int Color() {
    return this.indeterminateColor;
  }
}
