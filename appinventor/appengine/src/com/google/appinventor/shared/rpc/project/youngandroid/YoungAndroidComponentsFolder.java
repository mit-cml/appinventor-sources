// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project.youngandroid;

import com.google.appinventor.shared.rpc.project.FolderNode;

public class YoungAndroidComponentsFolder extends FolderNode {

  // For serialization
  //private static final long serialVersionUID = L; // TODO assign UID

  /**
   * Serialization constructor.
   */
  public YoungAndroidComponentsFolder() {
    super();
  }

  /**
   * Creates a new source folder node.
   */
  public YoungAndroidComponentsFolder(String fileId) {
    super("Components", fileId);
  }
}
