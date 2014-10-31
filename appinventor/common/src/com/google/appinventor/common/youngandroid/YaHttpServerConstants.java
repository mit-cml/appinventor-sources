// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.common.youngandroid;

/**
 * Constants related to the Young Android http server.
 *
 * @author sharon@google.com (Sharon Perl)
 */
public class YaHttpServerConstants {

  private YaHttpServerConstants() {
  }

  // URI component used for saving the codeblocks HTTP port
  public static final String CODEBLOCKS_SAVE_PORT = "codeblocks_saveport_";

  // URI component used for downloading the starter apk that goes on the phone
  public static final String STARTER_PHONEAPP_APK = "AppInventorPhoneApp_apk";

  // URI component used for downloading the zipped asset files
  public static final String ASSETS_ZIPFILE = "assets_zipfile";

  // The returned asset zipfile will be "<project name>-assets.zip"
  public static final String ASSET_ZIPFILE_SUFFIX = "-assets.zip";

  // Base name for the YA starter application.
  public static final String STARTER_PHONEAPP_NAME = "AppInventorPhoneApp";

  // Filename used for saving the codeblocks connection info
  public static final String CODEBLOCKS_INFO_FILE_PREFIX = "codeblocks_info_";

  // Constants used in HTTP requests:
  public static final String LOAD_FORM = "loadForm"; //async
  public static final String RELOAD_PROPERTIES = "reloadProperties";  // async
  public static final String SYNC_PROPERTY = "syncProperty";  // async
  public static final String ADD_ASSET = "addAsset";  // async
  public static final String SAVE_CODEBLOCKS_SOURCE = "saveBlk";
  public static final String GENERATE_YAIL = "generateYail";  // async
  public static final String CLEAR_CODEBLOCKS = "clearCodeblocks"; //async
  public static final String IS_PHONE_CONNECTED = "isPhoneConnected";  // async
  public static final String INSTALL_APPLICATION = "installApplication";  // async

  // Constants used in HTTP request parameters:
  // NOTE(lizlooney) - When adding new parameters here, make sure they don't collide with those
  // defined in JsonpConstants.
  public static final String FORM_PROPERTIES_PATH = "fp"; // LOAD_FORM
  public static final String PROJECT_NAME = "n";          // LOAD_FORM
  public static final String ASSET_PATH = "a";            // LOAD_FORM
  public static final String COMPONENT_NAME = "c";        // SYNC_PROPERTY
  public static final String PROPERTY_NAME = "p";         // SYNC_PROPERTY
  public static final String COMPONENT_TYPE = "t";        // SYNC_PROPERTY
  public static final String PROPERTY_VALUE = "v";        // SYNC_PROPERTY
  public static final String APK_FILE_PATH = "afp";       // INSTALL_APPLICATION
  public static final String APP_NAME = "an";             // INSTALL_APPLICATION
  public static final String PACKAGE_NAME = "un";         // INSTALL_APPLICATION
}
