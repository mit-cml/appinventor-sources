// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project.youngandroid;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.BLOCKLY_SOURCE_EXTENSION;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.CODEBLOCKS_SOURCE_EXTENSION;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.FORM_PROPERTIES_EXTENSION;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.SRC_FOLDER;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.YAIL_FILE_EXTENSION;

import com.google.common.base.Preconditions;
import com.google.appinventor.shared.rpc.project.SourceNode;
import com.google.appinventor.shared.storage.StorageUtil;

/**
 * Superclass for all Young Android source file nodes in the project tree.
 *
 */
public abstract class YoungAndroidSourceNode extends SourceNode {

  protected static final String SRC_PREFIX = SRC_FOLDER + "/";

  public static final String SCREEN1_FORM_NAME = "Screen1";

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

  /**
   * Returns the qualified name associated with this source node.
   *
   * @return  qualified name
   */
  public String getQualifiedName() {
    return getQualifiedName(getFullName());
  }

  /**
   * Returns the qualified name associated with the given source file id.
   *
   * @param sourceFileId  the file id of a source node
   * @return  qualified name
   */
  public static String getQualifiedName(String sourceFileId) {
    Preconditions.checkArgument(sourceFileId.startsWith(SRC_PREFIX) && (
        sourceFileId.endsWith(FORM_PROPERTIES_EXTENSION) ||
        sourceFileId.endsWith(CODEBLOCKS_SOURCE_EXTENSION) ||
        sourceFileId.endsWith(BLOCKLY_SOURCE_EXTENSION) ||
        sourceFileId.endsWith(YAIL_FILE_EXTENSION)));

    String name = sourceFileId.substring(SRC_PREFIX.length());
    name = StorageUtil.trimOffExtension(name);
    return name.replace('/', '.');
  }

  /**
   * Returns the form name associated with this source node.
   */
  public String getFormName() {
    return getFormName(getFileId());
  }

  /**
   * Returns true if this source node is associated with Screen1.
   */
  public boolean isScreen1() {
    return isScreen1(getFileId());
  }

  /**
   * Returns the form name associated with the given fileId.
   * Note that the extension of the fileId is ignored, so this works for both form (.scm) files and
   * blocks (.blk) files.
   */
  public static String getFormName(String fileId) {
    return StorageUtil.trimOffExtension(StorageUtil.basename(fileId));
  }

  /**
   * Returns true if the given fileId is associated with Screen1, false otherwise.
   * Note that the extension of the fileId is ignored, so this works for both form (.scm) files and
   * blocks (.blk) files.
   */
  public static boolean isScreen1(String fileId) {
    return getFormName(fileId).equals(SCREEN1_FORM_NAME);
  }
}
