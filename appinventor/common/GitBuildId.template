// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appinventor.common.version;

/**
 * Class containing constants for the git version and fingerprint
 * and another one for the Ant Build date
 *
 * We also put the Bugsense API key here so it can be loaded
 * from an argument to the ant build. If it isn't provided, it
 * will be a blank string which will disable Bugsense.
 *
 */
public final class GitBuildId {

  // The following values are set during the ant build.
  public static final String GIT_BUILD_VERSION = "@git.build.version@";
  public static final String GIT_BUILD_FINGERPRINT = "@git.build.fingerprint@";
  public static final String ANT_BUILD_DATE = "@ant.build.date@";
  public static final String ACRA_URI = "@acra.uri@";

  private GitBuildId() {
  }

 public static String getVersion() {
    String version = GIT_BUILD_VERSION;
    // This catches the emptry string or the error returned by git describe
    // in the case where there is no description.  In general
    // the version needs to be a string that can be 
    // embedded into a legal file name.  If it can't, then the
    // blocks editor won't load.
    if ((version == "") || version.contains(" ")) {
      return "none" ;
    } else {
      return version;
    }
  }
  
  public static String getFingerprint() {
    return GIT_BUILD_FINGERPRINT;
  }

  public static String getDate() {
    return ANT_BUILD_DATE;
  }

  public static String getAcraUri() {
    if (ACRA_URI.equals("${acra.uri}"))  // This is the value if no value is provided to ant
      return("");
    return ACRA_URI.trim();
  }

}
