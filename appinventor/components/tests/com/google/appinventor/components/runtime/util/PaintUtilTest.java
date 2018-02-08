// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class PaintUtilTest {

  @Test
  public void testLargeAlphaChannelStringToInt() {
    int argb = PaintUtil.hexStringToInt("&HFFFFFFFF");
    assertEquals(-1, argb);
  }

  @Test
  public void testSmallestNegativeStringToInt() {
    int argb = PaintUtil.hexStringToInt("&H80000000");
    assertEquals(Integer.MIN_VALUE, argb);
  }

  @Test
  public void testSmallAlphaChannelStringToInt() {
    int argb = PaintUtil.hexStringToInt("#x00FFFFFF");
    assertEquals(0xFFFFFF, argb);
  }

}
