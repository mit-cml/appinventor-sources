// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.junit.Assert.assertEquals;

import android.view.View;

import com.google.appinventor.components.runtime.util.ViewUtil;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowMessageQueue;

/**
 * Tests that a child of an {@link AbsoluteArrangement} with Width or Height Fill parent fills
 * the arrangement from its Left or Top to the far edge. The iOS runtime has matching tests in
 * AbsoluteArrangementFillParentTests.swift.
 */
@Config(shadows = {ShadowMessageQueue.class})
public class AbsoluteArrangementFillParentTest extends RobolectricTestBase {
  private static final int FILL = Component.LENGTH_FILL_PARENT;

  private AbsoluteArrangement arrangement;

  @Before
  public void setUp() {
    super.setUp();
    arrangement = new AbsoluteArrangement(getForm());
    arrangement.Width(300);
    arrangement.Height(200);
  }

  /**
   * Creates a button, sets its size and position like the generated app does (properties right
   * after creation, before RelativeLayout's posted add runs), and lays out the arrangement.
   */
  private Button button(int width, int height, int left, int top) {
    Robolectric.getForegroundThreadScheduler().pause();
    Button button = new Button(arrangement);
    button.Width(width);
    button.Height(height);
    button.Left(left);
    button.Top(top);
    Robolectric.getForegroundThreadScheduler().unPause();
    runAllEvents();
    layout();
    return button;
  }

  private void layout() {
    View view = arrangement.getView();
    view.measure(View.MeasureSpec.makeMeasureSpec(px(300), View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(px(200), View.MeasureSpec.EXACTLY));
    view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
  }

  private int px(int dp) {
    return ViewUtil.calculatePixels(arrangement.getView(), dp);
  }

  private void assertFrame(Button button, int left, int top, int right, int bottom) {
    View view = button.getView();
    assertEquals(px(left), view.getLeft());
    assertEquals(px(top), view.getTop());
    assertEquals(px(right), view.getRight());
    assertEquals(px(bottom), view.getBottom());
  }

  @Test
  public void testHeightFillParentFillsFromTop() {
    Button button = button(80, FILL, 10, 100);
    assertFrame(button, 10, 100, 90, 200);
  }

  @Test
  public void testWidthFillParentFillsFromLeft() {
    Button button = button(FILL, 40, 60, 20);
    assertFrame(button, 60, 20, 300, 60);
  }

  @Test
  public void testFillParentAtOriginFillsArrangement() {
    Button button = button(FILL, FILL, 0, 0);
    assertFrame(button, 0, 0, 300, 200);
  }

  @Test
  public void testFillParentSetAfterPosition() {
    Button button = button(80, 40, 60, 100);
    button.Width(FILL);
    button.Height(FILL);
    layout();
    assertFrame(button, 60, 100, 300, 200);
  }

  @Test
  public void testFillParentFollowsMove() {
    Button button = button(FILL, FILL, 60, 100);
    button.Left(100);
    button.Top(50);
    runAllEvents();
    layout();
    assertFrame(button, 100, 50, 300, 200);
  }
}
