// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2015 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.google.appinventor.components.runtime.util.HoneycombUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;

/**
 * This is a FrameLayout that displays all content by a scaled amount.
 * Paddding is adjusted such that scale * (size-padding) = size.
 * Code adjusted from Google example code found at:
 * http://developer.android.com/reference/android/view/ViewGroup.html
 */
public class ScaledFrameLayout extends ViewGroup {

  private static final int MATRIX_SAVE_FLAG = 0x01;

  /** The amount of space used by children in the left gutter. */
  private int mLeftWidth;

  /** The amount of space used by children in the right gutter. */
  private int mRightWidth;

  /** These are used for computing child frames based on their gravity. */
  private final Rect mTmpContainerRect = new Rect();
  private final Rect mTmpChildRect = new Rect();

  private float mScale = 1.0f;

  public ScaledFrameLayout(Context context) {
    super(context);
  }

  public ScaledFrameLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ScaledFrameLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setClipChildren(false);
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    canvas.save();
    canvas.scale(mScale, mScale);
    super.dispatchDraw(canvas);
    canvas.restore();
  }

  @Override
  public ViewParent invalidateChildInParent(final int[] location,
                                            final Rect dirty) {
    // This function is overridden so that children properly invalidate
    // when scaling is in place.
    final int[] scaledLocation = { (int) (location[0] * mScale),
                                   (int) (location[1] * mScale) };

    final Rect scaledDirty = new Rect((int) (dirty.left * mScale),
                                      (int) (dirty.top * mScale), (int) (dirty.right * mScale),
                                      (int) (dirty.bottom * mScale));

    this.invalidate(scaledDirty);

    return super.invalidateChildInParent(scaledLocation, scaledDirty);
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    // Modify the touch events so that children receive scaled touch events
    ev.setLocation(ev.getX() * (1f / mScale), ev.getY() * (1f / mScale));
    super.dispatchTouchEvent(ev);
    return true;
  }

  public void setScale(float scale) {
    mScale = scale;
    updatePadding(getWidth(), getHeight());
  }

  private void updatePadding(int width, int height) {
    // To maintain constant size, the padding is adjusted such that
    // scale * (size - padding) = size
    int paddingRight = (int)((width * (mScale - 1f)) / mScale);
    int paddingBottom = (int)((height * (mScale - 1f)) / mScale);
    setPadding(0, 0, paddingRight, paddingBottom);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    updatePadding(w,h);
  }

  /**
   * Override because viewgroup does not scroll
   */
  @Override
  public boolean shouldDelayChildPressedState() {
    return false;
  }

  /**
   * Ask all children to measure themselves and compute the measurement of
   * this layout based on the children.
   */
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int count = getChildCount();

    mLeftWidth = 0;
    mRightWidth = 0;

    int maxHeight = 0;
    int maxWidth = 0;
    int childState = 0;

    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() != GONE) {
        measureChild(child, widthMeasureSpec, heightMeasureSpec);

        mLeftWidth += Math.max(maxWidth, child.getMeasuredWidth());

        maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
        if (SdkLevel.getLevel() >= SdkLevel.LEVEL_HONEYCOMB) {
          childState = HoneycombUtil.combineMeasuredStates(this, childState,
            HoneycombUtil.getMeasuredState(child));
        }
      }
    }

    maxWidth += mLeftWidth + mRightWidth;

    maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
    maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_HONEYCOMB) {
      setMeasuredDimension(
        HoneycombUtil.resolveSizeAndState(this, maxWidth, widthMeasureSpec, childState),
        HoneycombUtil.resolveSizeAndState(this, maxHeight, heightMeasureSpec,
          childState << HoneycombUtil.VIEWGROUP_MEASURED_HEIGHT_STATE_SHIFT));
    } else {
      setMeasuredDimension(
        resolveSize(maxWidth, widthMeasureSpec),
        resolveSize(maxHeight, heightMeasureSpec));
    }
  }

  /**
   * Position all children within this layout.
   */
  @Override
  protected void onLayout(boolean changed, int left, int top, int right,
                          int bottom) {
    final int count = getChildCount();

    int leftPos = getPaddingLeft();

    final int parentTop = getPaddingTop();
    final int parentBottom = bottom - top - getPaddingBottom();

    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() != GONE) {
        final int width = child.getMeasuredWidth();
        final int height = child.getMeasuredHeight();

        mTmpContainerRect.left = leftPos;
        mTmpContainerRect.right = leftPos;
        leftPos = mTmpContainerRect.right;
        mTmpContainerRect.top = parentTop;
        mTmpContainerRect.bottom = parentBottom;

        Gravity.apply(Gravity.TOP | Gravity.LEFT, width, height,
                      mTmpContainerRect, mTmpChildRect);

        child.layout(mTmpChildRect.left, mTmpChildRect.top,
                     mTmpChildRect.right, mTmpChildRect.bottom);
      }
    }
  }
}
