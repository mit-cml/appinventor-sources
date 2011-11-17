// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.shared.rpc.project.youngandroid;

import com.google.appinventor.shared.rpc.project.FolderNode;

/**
 * YoungAndroid assets folder node in project tree.
 *
 */
public final class YoungAndroidAssetsFolder extends FolderNode {

  // For serialization
  private static final long serialVersionUID = -8483982740480379050L;

  /**
   * Serialization constructor.
   */
  public YoungAndroidAssetsFolder() {
    super(null, null);
  }

  /**
   * Creates a new source folder node.
   */
  public YoungAndroidAssetsFolder(String fileId) {
    super("Assets", fileId);
  }
}