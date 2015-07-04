// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project.youngandroid;

import com.google.appinventor.shared.rpc.project.FileNode;

/**
 * Young Android asset node in project tree.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YoungAndroidAssetNode extends FileNode {

  // For serialization
  private static final long serialVersionUID = 8824815241429504801L;

  /**
   * Default constructor (for serialization only).
   */
  public YoungAndroidAssetNode() {
  }

  /**
   * Creates a new asset file project node.
   *
   * @param name  asset file name
   * @param fileId  file ID
   */
  public YoungAndroidAssetNode(String name, String fileId) {
    super(name, fileId);
  }
}
