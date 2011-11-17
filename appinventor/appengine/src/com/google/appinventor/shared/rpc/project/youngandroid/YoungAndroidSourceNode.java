// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.appinventor.shared.rpc.project.youngandroid;

import com.google.appinventor.shared.rpc.project.SourceNode;

/**
 * Superclass for all Young Android source file nodes in the project tree.
 *
 */
public abstract class YoungAndroidSourceNode extends SourceNode {

  /**
   * Default constructor (for serialization only).
   */
  YoungAndroidSourceNode() {
  }

  /**
   * Creates a new Young Android source file project node.
   *
   * @param name  file name
   * @param fileId  file id
   */
  public YoungAndroidSourceNode(String name, String fileId) {
    super(name, fileId);
  }
}
