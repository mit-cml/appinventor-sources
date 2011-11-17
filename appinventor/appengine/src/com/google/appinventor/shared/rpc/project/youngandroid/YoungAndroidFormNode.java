// Copyright 2007 Google Inc. All Rights Reserved.

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

  private static final String SRC_PREFIX = YoungAndroidSourceAnalyzer.SRC_FOLDER + "/";

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

  /**
   * Returns the qualified name of the form associated with this form node.
   *
   * @return  qualified name
   */
  public String getQualifiedName() {
    return getQualifiedName(getFullName());
  }

  /**
   * Returns the qualified name of the form associated with the given form
   * file id.
   *
   * @param formFileId  the file id of a form
   * @return  qualified name
   */
  public static String getQualifiedName(String formFileId) {
    Preconditions.checkArgument(formFileId.startsWith(SRC_PREFIX) &&
        formFileId.endsWith(YoungAndroidSourceAnalyzer.FORM_PROPERTIES_EXTENSION));

    return formFileId.substring(SRC_PREFIX.length(),
        formFileId.length() - YoungAndroidSourceAnalyzer.FORM_PROPERTIES_EXTENSION.length())
        .replace('/', '.');
  }

  public static String getFormFileId(String qualifiedFormName) {
    return SRC_PREFIX + qualifiedFormName.replace('.', '/')
        + YoungAndroidSourceAnalyzer.FORM_PROPERTIES_EXTENSION;
  }
}
