// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import android.view.View;
import android.widget.RelativeLayout;

import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.runtime.util.ViewUtil;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowMessageQueue;

/**
 * Tests positioning of children in an {@link AbsoluteArrangement}. Negative Left/Top values,
 * including -1, are real positions; only {@link ComponentConstants#DEFAULT_X_Y} means unset.
 * The iOS runtime has matching tests in AbsoluteArrangementTests.swift.
 */
@Config(shadows = {ShadowMessageQueue.class})
public class AbsoluteArrangementTest extends RobolectricTestBase {
  private AbsoluteArrangement arrangement;

  @Before
  public void setUp() {
    super.setUp();
    arrangement = new AbsoluteArrangement(getForm());
    arrangement.Width(300);
    arrangement.Height(200);
  }

  private Button placedButton(int left, int top) {
    // RelativeLayout adds a child from a posted runnable and re-posts while the child's
    // position is unset. Pause the scheduler until Left/Top are set, as the generated app does
    // when it sets designer properties right after creating the component.
    Robolectric.getForegroundThreadScheduler().pause();
    Button button = new Button(arrangement);
    button.Left(left);
    button.Top(top);
    Robolectric.getForegroundThreadScheduler().unPause();
    runAllEvents();
    return button;
  }

  private void assertPosition(Button button, int left, int top) {
    View view = button.getView();
    assertSame(arrangement.getView(), view.getParent());
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
    assertEquals(ViewUtil.calculatePixels(view, left), params.leftMargin);
    assertEquals(ViewUtil.calculatePixels(view, top), params.topMargin);
  }

  @Test
  public void testPositionIsUnsetByDefault() {
    Robolectric.getForegroundThreadScheduler().pause();
    Button button = new Button(arrangement);
    assertEquals(ComponentConstants.DEFAULT_X_Y, button.Left());
    assertEquals(ComponentConstants.DEFAULT_X_Y, button.Top());
  }

  @Test
  public void testPositivePosition() {
    assertPosition(placedButton(20, 30), 20, 30);
  }

  @Test
  public void testNegativeLeftIsHonored() {
    assertPosition(placedButton(-40, 10), -40, 10);
  }

  @Test
  public void testNegativeTopKeepsLeft() {
    assertPosition(placedButton(150, -15), 150, -15);
  }

  @Test
  public void testMinusOneIsARealPosition() {
    assertPosition(placedButton(-1, 120), -1, 120);
  }

  @Test
  public void testMovingToNegativePosition() {
    Button button = placedButton(20, 30);
    button.Left(-25);
    button.Top(-5);
    runAllEvents();
    assertPosition(button, -25, -5);
  }
}
