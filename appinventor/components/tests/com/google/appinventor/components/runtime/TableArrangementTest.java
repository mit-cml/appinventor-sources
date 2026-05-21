// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2021 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowMessageQueue;

@Config(shadows = {ShadowMessageQueue.class})
public class TableArrangementTest extends RobolectricTestBase {
  private TableArrangement table;

  @Before
  public void setUp() {
    super.setUp();
    table = new TableArrangement(getForm());
  }

  @Test
  public void testAddComponent() {
    Robolectric.getForegroundThreadScheduler().pause();
    Button button1 = new Button(table);
    button1.Row(0);
    button1.Column(0);
    Robolectric.getForegroundThreadScheduler().unPause();
    assertTrue(table.getChildren().contains(button1));
  }
}
