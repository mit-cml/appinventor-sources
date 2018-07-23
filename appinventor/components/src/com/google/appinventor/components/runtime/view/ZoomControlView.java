// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.view;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.google.appinventor.components.runtime.util.ViewUtil;
import org.osmdroid.views.MapView;

public class ZoomControlView extends LinearLayout {

  private final MapView parent;
  private final Button zoomIn;
  private final Button zoomOut;

  private float density;

  public ZoomControlView(MapView parent) {
    super(parent.getContext());

    density = parent.getContext().getResources().getDisplayMetrics().density;

    this.parent = parent;
    this.setOrientation(LinearLayout.VERTICAL);
    zoomIn = new Button(parent.getContext());
    zoomOut = new Button(parent.getContext());
    initButton(zoomIn, "+");
    initButton(zoomOut, "−");
    zoomIn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        ZoomControlView.this.parent.getController().zoomIn();
      }
    });
    zoomOut.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        ZoomControlView.this.parent.getController().zoomOut();
      }
    });

    ViewUtil.setBackgroundDrawable(zoomIn, getZoomInDrawable(density));
    ViewUtil.setBackgroundDrawable(zoomOut, getZoomOutDrawable(density));

    int[][] states = new int[][] {
        new int[] {-android.R.attr.state_enabled },
        new int[] { android.R.attr.state_enabled }
    };
    int[] colors = new int[] {
        Color.LTGRAY,
        Color.BLACK
    };
    zoomIn.setTextColor(new ColorStateList(states, colors));
    zoomOut.setTextColor(new ColorStateList(states, colors));

    addView(zoomIn);
    addView(zoomOut);

    this.setPadding((int)(12 * density), (int)(12 * density), 0, 0);
    updateButtons();
  }

  /**
   * Update the state of the zoom buttons based on the current map tile layer and its zoom level.
   */
  @SuppressWarnings("WeakerAccess")
  public final void updateButtons() {
    zoomIn.setEnabled(parent.canZoomIn());
    zoomOut.setEnabled(parent.canZoomOut());
  }

  private void initButton(Button button, String text) {
    button.setText(text);
    button.setTextSize(22);
    button.setPadding(0, 0, 0, 0);
    button.setWidth((int)(30 * density));
    button.setHeight((int)(30 * density));
    button.setSingleLine();
    button.setGravity(Gravity.CENTER);
  }

  private static Drawable getZoomInDrawable(float density) {
    final int R = (int)(4 * density);
    ShapeDrawable drawable = new ShapeDrawable(new RoundRectShape(new float[] { R, R, R, R, 0, 0, 0, 0 }, null, null));
    drawable.getPaint().setColor(Color.WHITE);
    return drawable;
  }

  private static Drawable getZoomOutDrawable(float density) {
    final int R = (int)(4 * density);
    ShapeDrawable drawable = new ShapeDrawable(new RoundRectShape(new float[] { 0, 0, 0, 0, R, R, R, R }, null, null));
    drawable.getPaint().setColor(Color.WHITE);
    return drawable;
  }
}
