// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.shadows.org.osmdroid.tileprovider.util;

import org.osmdroid.tileprovider.util.StorageUtils;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.io.File;
import java.io.IOException;

/**
 * Shadow version of the StorageUtils helper class.
 */
@Implements(StorageUtils.class)
public class ShadowStorageUtils {
  @RealObject private StorageUtils realStorageUtils;

  @Implementation
  public static File getStorage() {
    try {
      File temp = File.createTempFile("osmdroid", "tmp");
      if (!temp.delete() || !temp.mkdirs()) {
        throw new IllegalStateException("Unable to create temporary directory.");
      }
      temp.deleteOnExit();
      return temp;
    } catch(IOException e) {
      throw new IllegalStateException("Unable to create temporary directory.");
    }
  }
}
