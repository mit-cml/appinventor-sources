// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.util;

import com.google.appinventor.common.youngandroid.YaHttpServerConstants;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Helper methods for resources.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class ResourceUtil {

  // Jar file containing Codeblocks for Young Android
  public static final String CODEBLOCKS_JAR = "BlocksEditor.jar";

  // Main class for Codeblocks
  public static final String CODEBLOCKS_MAIN =
    "com.google.appinventor.blockseditor.youngandroid.YaHttpServerMain";

  // Apk file containing starter app
  private static final String STARTER_APP_APK =
      YaHttpServerConstants.STARTER_PHONEAPP_NAME + ".apk";

  private ResourceUtil() {
  }

  /**
   * Returns a byte array containing the binary content of the signed
   * codeblocks jar.
   */
  public static byte[] downloadSignedCodeblocksJar() throws IOException {
    // The codeblocks jar was signed at build time.
    // We expect the jar file to be in our class directory
    URL url = ResourceUtil.class.getResource(CODEBLOCKS_JAR);
    return Resources.toByteArray(url);
  }

  /**
   * Returns a byte array containing the binary content of the starter app apk.
   */
  public static byte[] downloadStarterAppApk() throws IOException {
    // We expect the start app apk file to be in our class directory
    return Resources.toByteArray(ResourceUtil.class.getResource(STARTER_APP_APK));
  }
}
