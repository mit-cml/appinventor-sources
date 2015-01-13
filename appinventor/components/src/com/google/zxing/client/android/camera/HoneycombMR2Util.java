// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package com.google.zxing.client.android.camera;

import android.graphics.Point;
import android.view.Display;

/**
 * Helper methods for calling methods added in Honeycomb (3.2, API level 13)
 *
 *
 */
public class HoneycombMR2Util {

  private HoneycombMR2Util() {
  }

  public static void getSize(Display display, Point pt) {
    display.getSize(pt);
  }

}
