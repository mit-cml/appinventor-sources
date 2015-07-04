// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

/**
 * A modified MediaController that allows adding to a
 * {@link android.view.ViewGroup} instead of the Window that Android adds the
 * MediaController to.
 *
 * This class manages displaying the controller GUI.
 *
 * @author Vance Turnewitsch
 *
 */
public class CustomMediaController extends MediaController implements
    View.OnTouchListener {

  // The view whose touch events are listened to.
  private View mAnchorView;

  /*
   * How long the GUI should be shown when the anchorView passed in
   */
  private int mShowTime = 3000;

  public CustomMediaController(Context context) {
    super(context);
  }

  /**
   * Sets the visibility of the GUI to {@link android.view.View#VISIBLE}
   * and calls {@link android.widget.MediaController#show(int)}.
   * @param timeout
   *          How long the GUI should be shown for.
   */
  @Override
  public void show(int timeout) {
    setVisibility(VISIBLE);
    super.show(timeout);
  }

  /**
   * Sets the visibility of the GUI to {@link android.view.View#VISIBLE}
   * and calls {@link android.widget.MediaController#show()}.
   */
  @Override
  public void show() {
    setVisibility(VISIBLE);
    super.show();
  }

  /**
   * Attempts to remove this CustomMediaController from the Window that Android
   * automatically adds it to and add it to another
   * {@link android.view.ViewGroup}.
   *
   * @param parent
   *          The {@link android.view.ViewGroup} to add the CustomMediaController
   *          to.
   * @param params
   *          The {@link android.view.ViewGroup.LayoutParams} to use when adding
   *          the CustomMediaController to the parent.
   */
  public boolean addTo(ViewGroup parent, ViewGroup.LayoutParams params) {
    Object mParent = getParent();
    if (mParent != null && mParent instanceof ViewGroup) {
      ((ViewGroup) mParent).removeView(this);
      parent.addView(this, params);
      return true;
    } else {
      Log.e("CustomMediaController.addTo",
          "MediaController not available in fullscreen.");
      return false;
    }
  }

  /**
   * Calls {@link android.widget.MediaController#setAnchorView(View)} and sets
   * up a listener that shows a GUI when the anchorView is touched.
   */
  @Override
  public void setAnchorView(View anchorView) {
    mAnchorView = anchorView;
    mAnchorView.setOnTouchListener(this);
    super.setAnchorView(anchorView);
  }

  /**
   * Calls {@link android.widget.MediaController#hide()} and sets the visibility
   * of this object to {@link android.view.View#INVISIBLE}.
   */
  @Override
  public void hide() {
    super.hide();
    setVisibility(INVISIBLE);
  }

  /**
   * Called when the anchorView passed in
   * {@link CustomMediaController#setAnchorView(View)} is touched. Shows the
   * controller GUI.
   */
  @Override
  public boolean onTouch(View v, MotionEvent event) {
    if (v == mAnchorView) {
      show(mShowTime);
    }
    return false;
  }

}
