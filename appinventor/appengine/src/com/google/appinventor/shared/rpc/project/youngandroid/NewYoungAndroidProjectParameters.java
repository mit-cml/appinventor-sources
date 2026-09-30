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

  private String theme;

  private String toolkit;

  // Custom Android package name for the app, or empty to derive it from packageName
  private String customPackageName;

  /**
   * Creates new parameters for creating Young Android projects
   *
   * @param packageName the package of the main form
   */

  public NewYoungAndroidProjectParameters(String packageName) {
    this.packageName = packageName;
    formName = YOUNG_ANDROID_FORM_NAME;
  }

  public NewYoungAndroidProjectParameters(String packageName, String theme) {
    this.packageName = packageName;
    formName = YOUNG_ANDROID_FORM_NAME;
    this.theme = theme;
  }

  public NewYoungAndroidProjectParameters(String packageName, String theme, String toolkit) {
    this.packageName = packageName;
    formName = YOUNG_ANDROID_FORM_NAME;
    this.theme = theme;
    this.toolkit = toolkit;
  }

  public NewYoungAndroidProjectParameters(String packageName, String theme, String toolkit,
      String customPackageName) {
    this(packageName, theme, toolkit);
    this.customPackageName = customPackageName;
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

  public String getThemeName() {
    if (theme == null) {
      return "Classic";
    }
    return theme;
  }

  public String getBlocksToolkit() {
    if (toolkit == null) {
      return "";
    }
    return toolkit;
  }

  /**
   * Returns the Android package name chosen for the app, or an empty string if the package name
   * should be derived from the package of the main form.
   *
   * @return the custom Android package name, or an empty string
   */
  public String getCustomPackageName() {
    if (customPackageName == null) {
      return "";
    }
    return customPackageName;
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
