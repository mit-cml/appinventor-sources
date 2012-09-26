// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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