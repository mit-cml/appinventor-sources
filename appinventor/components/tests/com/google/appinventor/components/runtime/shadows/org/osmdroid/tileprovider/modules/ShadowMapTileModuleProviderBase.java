// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.shadows.org.osmdroid.tileprovider.modules;

import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(MapTileModuleProviderBase.class)
public class ShadowMapTileModuleProviderBase {
  @Implementation
  public void loadMapTileAsync(final MapTileRequestState pState) {
    // do nothing
  }
}
