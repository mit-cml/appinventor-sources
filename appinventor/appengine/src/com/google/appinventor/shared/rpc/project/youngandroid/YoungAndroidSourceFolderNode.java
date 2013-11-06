// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.rpc.project.youngandroid;

import com.google.appinventor.shared.rpc.project.SourceFolderNode;

/**
 * Young Android source folder node in project tree.
 *
 */
public final class YoungAndroidSourceFolderNode extends SourceFolderNode {

  // For serialization
  private static final long serialVersionUID = -1036164488317207599L;

  /**
   * Serialization constructor.
   */
  public YoungAndroidSourceFolderNode() {
    super(null, null);
  }

  /**
   * Creates a new source folder node.
   */
  public YoungAndroidSourceFolderNode(String fileId) {
    super("Sources", fileId);
  }
}