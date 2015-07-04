// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project.youngandroid;

import com.google.common.annotations.VisibleForTesting;
import com.google.appinventor.shared.rpc.project.NewProjectParameters;

/**
 * Parameters for creating Young Android projects.
 *
 * @see com.google.appinventor.shared.rpc.project.ProjectService#newProject(String,
 *      String, com.google.appinventor.shared.rpc.project.NewProjectParameters)
 */
public final class NewYoungAndroidProjectParameters implements NewProjectParameters {
  @VisibleForTesting public static final String YOUNG_ANDROID_FORM_NAME = "Screen1";

  // Package name of the main form
  private String packageName;

  // Name of the main form
  private String formName;

  /**
   * Creates new parameters for creating Young Android projects
   *
   * @param packageName the package of the main form
   */
  public NewYoungAndroidProjectParameters(String packageName) {
    this.packageName = packageName;
    formName = YOUNG_ANDROID_FORM_NAME;
  }

  // For serialization only
  @SuppressWarnings("unused")
  private NewYoungAndroidProjectParameters() {
  }

  /**
   * Returns the package name of the main form.
   *
   * @return the package name of the main form
   */
  public String getPackageName() {
    return packageName;
  }

  /**
   * Returns the short name of the main form.
   *
   * @return the short name of the main form
   */
  public String getFormName() {
    return formName;
  }

  /**
   * Returns the fully qualified name of the main form.
   *
   * @return the fully qualified name of the main form.
   */
  public String getQualifiedFormName() {
    return packageName + '.' + formName;
  }

  @Override
  public String toString() {
    return getQualifiedFormName();
  }
}
