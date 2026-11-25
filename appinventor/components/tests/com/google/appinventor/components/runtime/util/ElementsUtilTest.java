// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

/**
 * Tests for {@link ElementsUtil}.
 */
public class ElementsUtilTest {
  private static final YailList ITEMS =
      YailList.makeList(Arrays.asList("Apple", "Banana", "Cherry"));

  @Test
  public void testSetSelectionFromIndexInRange() {
    assertEquals("Apple", ElementsUtil.setSelectionFromIndex(1, ITEMS));
    assertEquals("Cherry", ElementsUtil.setSelectionFromIndex(3, ITEMS));
  }

  @Test
  public void testSetSelectionFromIndexOutOfRange() {
    assertEquals("", ElementsUtil.setSelectionFromIndex(0, ITEMS));
    assertEquals("", ElementsUtil.setSelectionFromIndex(4, ITEMS));
  }

  @Test
  public void testSetSelectionFromIndexNegative() {
    assertEquals("", ElementsUtil.setSelectionFromIndex(-1, ITEMS));
    assertEquals("", ElementsUtil.setSelectionFromIndex(-5, ITEMS));
  }

  @Test
  public void testSelectionIndexNegative() {
    assertEquals(0, ElementsUtil.selectionIndex(-1, ITEMS));
  }
}
