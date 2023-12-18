// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import java.io.IOException;

/**
 * Thrown by StorageIo modules when an extension
 * is detected in a project.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */
public final class GalleryExtensionException  extends IOException {
  /**
   * Creates a GalleryExtentionException
   *
   */
  public GalleryExtensionException() {
    super();
  }
}
