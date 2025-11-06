// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

/**
 * Constants related to Young Android projects and files.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YoungAndroidConstants {
  private YoungAndroidConstants() {}


  /**
   * The filename for a project's keystore, relative to the directory that contains the
   * project.properties file.
   */
  public static final String PROJECT_KEYSTORE_LOCATION = "android.keystore";

  public static final String EXT_COMPS_DIR_NAME = "external_comps";

  public static final String LIBS_DIR_NAME = "libs";
  public static final String ARMEABI_DIR_NAME = "armeabi";
  public static final String ARMEABI_V7A_DIR_NAME = "armeabi-v7a";
  public static final String ARM64_V8A_DIR_NAME = "arm64-v8a";
  public static final String X86_64_DIR_NAME = "x86_64";
}
