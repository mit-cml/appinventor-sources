// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.shadows.org.osmdroid.views;

import org.osmdroid.views.MapView;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowViewGroup;

/**
 * Shadow class for overriding behavior of OpenStreetMaps MapView class.
 */
@Implements(MapView.class)
public class ShadowMapView extends ShadowViewGroup {

  public int invalidateCalls = 0;

  @Implementation
  public void checkZoomButtons() {
    // suppress built-in zoom controls using internal Android IDs.
  }

  @Implementation
  public void invalidate() {
    super.invalidate();
    invalidateCalls++;
  }
}
