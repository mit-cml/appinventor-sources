// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;

import com.google.appinventor.components.common.ComponentConstants;

import org.junit.Test;

/**
 * Layout tests for {@link AbsoluteArrangement} placed on a Screen between other components.
 */
public class AbsoluteArrangementTest extends RobolectricTestBase {

  private static final int SCREEN_WIDTH = 360;
  private static final int SCREEN_HEIGHT = 800;

  private Label label;
  private AbsoluteArrangement arrangement;
  private Button button;

  private void layOutLabelArrangementButton(int arrangementHeight) {
    label = new Label(getForm());
    label.Text("Text for Label1");
    arrangement = new AbsoluteArrangement(getForm());
    arrangement.Height(arrangementHeight);
    button = new Button(getForm());
    button.Text("Text for Button1");
    runAllEvents();

    ViewGroup screen = (ViewGroup) arrangement.getView().getParent();
    screen.measure(
        View.MeasureSpec.makeMeasureSpec(SCREEN_WIDTH, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(SCREEN_HEIGHT, View.MeasureSpec.EXACTLY));
    screen.layout(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
  }

  /** The y coordinate where the next stacked view starts: the view's bottom plus its margin. */
  private static int nextTop(View view) {
    MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
    return view.getBottom() + params.bottomMargin;
  }

  /** The y coordinate where the view's box starts, including its top margin. */
  private static int boxTop(View view) {
    MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
    return view.getTop() - params.topMargin;
  }

  @Test
  public void testFillParentHeightFillsScreen() {
    layOutLabelArrangementButton(Component.LENGTH_FILL_PARENT);

    View arrangementView = arrangement.getView();
    View buttonView = button.getView();
    // Components stay stacked in order, with no gaps from the leftover space.
    assertEquals(nextTop(label.getView()), boxTop(arrangementView));
    assertEquals(nextTop(arrangementView), boxTop(buttonView));
    // The arrangement takes the leftover height and the button ends at the bottom of the screen.
    assertEquals(SCREEN_HEIGHT, nextTop(buttonView));
    assertTrue(arrangementView.getHeight() > SCREEN_HEIGHT / 2);
  }

  @Test
  public void testAutomaticHeightKeepsPreferredSize() {
    layOutLabelArrangementButton(Component.LENGTH_PREFERRED);

    View arrangementView = arrangement.getView();
    assertEquals(ComponentConstants.EMPTY_HV_ARRANGEMENT_HEIGHT, arrangementView.getHeight());
    assertEquals(nextTop(label.getView()), boxTop(arrangementView));
    assertEquals(nextTop(arrangementView), boxTop(button.getView()));
  }
}
