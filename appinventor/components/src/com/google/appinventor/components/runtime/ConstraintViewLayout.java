// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.runtime.util.ViewUtil;

/**
 * Layout for placing components at absolute (x, y) positions, backed by a ConstraintLayout.
 * Used by Form when ScreenLayout is set to "Absolute".
 *
 * <p>Views are added synchronously with ConstraintLayout.LayoutParams (leftToLeft / topToTop
 * constrained to parent) so that components like Label which read view.getLayoutParams()
 * immediately after container.$add() receive proper MarginLayoutParams. Subsequent position
 * updates always create fresh LayoutParams (rather than mutating existing ones) and remove+re-add
 * the view, because ConstraintLayout.resolveLayoutDirection() clears leftToLeft/topToTop to
 * UNSET during the first layout pass; mutating the cleared params loses the anchor constraints.
 */
public class ConstraintViewLayout implements Layout {

  private final ConstraintLayout layoutManager;

  ConstraintViewLayout(Context context) {
    layoutManager = new ConstraintLayout(context);
  }

  @Override
  public ViewGroup getLayoutManager() {
    return layoutManager;
  }

  @Override
  public void add(AndroidViewComponent component) {
    placeComponent(component);
  }

  /**
   * Updates the position of a component based on its current Left() and Top() values.
   * Called by Form.setChildNeedsLayout() when Left or Top changes.
   *
   * <p>Always creates fresh ConstraintLayout.LayoutParams rather than mutating the existing
   * object. ConstraintLayout.resolveLayoutDirection() clears leftToLeft/topToTop to UNSET
   * during the first layout pass (transforming them into start-relative internal fields).
   * If the existing params were reused after that pass, the anchor constraints would be lost
   * and the component would fall back to its default (0,0) position. Fresh params guarantee
   * the correct leftToLeft/topToTop anchors on every position update.
   */
  public void updateComponentPosition(AndroidViewComponent component) {
    View view = component.getView();
    if (view.getParent() != layoutManager) {
      return;  // not yet in this layout; placeComponent will set the correct position on addView
    }
    int x = component.Left();
    int y = component.Top();
    if (x == ComponentConstants.DEFAULT_X_Y) x = 0;
    if (y == ComponentConstants.DEFAULT_X_Y) y = 0;

    int index = layoutManager.indexOfChild(view);
    // Preserve current width/height (may have been set by setChildWidth/HeightForConstraintLayout).
    ViewGroup.LayoutParams old = view.getLayoutParams();
    // Create fresh params — never reuse the existing object. ConstraintLayout's
    // resolveLayoutDirection() clears leftToLeft/topToTop to UNSET during the first layout
    // pass; reusing the mutated params would lose the parent anchor constraints on reload.
    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(old.width, old.height);
    params.leftToLeft = ConstraintSet.PARENT_ID;
    params.topToTop = ConstraintSet.PARENT_ID;
    params.leftMargin = ViewUtil.calculatePixels(view, x);
    params.topMargin = ViewUtil.calculatePixels(view, y);
    layoutManager.removeViewAt(index);
    layoutManager.addView(view, index, params);
  }

  /**
   * Adds a component to this layout, positioning it at its current Left/Top (or 0 if unset).
   * Used when migrating components from another layout at runtime.
   */
  public void migrateComponent(AndroidViewComponent component) {
    placeComponent(component);
  }

  private void placeComponent(AndroidViewComponent component) {
    View view = component.getView();
    if (view.getId() == View.NO_ID) {
      view.setId(View.generateViewId());
    }

    int x = component.Left();
    int y = component.Top();
    if (x == ComponentConstants.DEFAULT_X_Y) x = 0;
    if (y == ComponentConstants.DEFAULT_X_Y) y = 0;

    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.leftToLeft = ConstraintSet.PARENT_ID;
    params.topToTop = ConstraintSet.PARENT_ID;
    params.leftMargin = ViewUtil.calculatePixels(view, x);
    params.topMargin = ViewUtil.calculatePixels(view, y);
    layoutManager.addView(view, params);
  }
}
