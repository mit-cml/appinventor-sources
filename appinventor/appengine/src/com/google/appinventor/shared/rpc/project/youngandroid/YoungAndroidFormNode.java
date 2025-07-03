// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project.youngandroid;

import com.google.common.base.Preconditions;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.youngandroid.YoungAndroidSourceAnalyzer;


/**
 * Young Android form source file node in the project tree.
 *
 */
public final class YoungAndroidFormNode extends YoungAndroidSourceNode {

  // For serialization
  private static final long serialVersionUID = -933267987704020542L;

  /**
   * Default constructor (for serialization only).
   */
  public YoungAndroidFormNode() {
  }

  /**
   * Creates a new Young Android form source file project node.
   *
   * @param fileId  file id
   */
  public YoungAndroidFormNode(String fileId) {
    super(StorageUtil.basename(fileId), fileId);
  }

  public static String getFormFileId(String qualifiedName) {
    return SRC_PREFIX + qualifiedName.replace('.', '/')
        + YoungAndroidSourceAnalyzer.FORM_PROPERTIES_EXTENSION;
  }
}
