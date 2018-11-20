// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.common.version;

/**
 * Class used to determine whether a new feature should be visible to the user.
 *
 */
public final class AppInventorFeatures {

  private AppInventorFeatures() {
  }

  public static boolean hasDebuggingView() {
    // Set this to true if you want the Debugging view to show.
    return true;
  }

  public static boolean hasYailGenerationOption() {
    // Set this to true if you want the Package for Phone button to include an option to just
    // generate Yail
    return true;
  }

  public static boolean sendBugReports() {
    // Set this to true if you want to prompt the user to report bugs.
    return true;
  }

  public static boolean allowMultiScreenApplications() {
    // Set this to true if you want users to be able to create more than one screen in a single
    // project.
    return true;
  }

  public static boolean showInternalComponentsCategory() {
    // Set this to true if you want to show the "For internal use only" section of the components
    // palette in the designer.
    return false;
  }

  public static boolean takeScreenShots() {
    // If true, we take a screenshot whenever a user leaves a blocks editor
    // This screenshot is saved with their project but only available for
    // download if you are an administrator
    return false;
  }

  public static boolean trackClientEvents() {
    // Set this to true if you want to track events with Google Analytics.
    return false;
  }

  /**
   * If set to return true we may show a splash screen. Each splash
   * screen has a version stored in the datastore in the SplashData
   * kind. If it doesn't exist, it is initialized to 0. If 0, no
   * splash screen is shown. If it is non-zero it is compared to the
   * users's SplashSettings value. If the users's is less then the
   * current SplashData.version, they are shown the splash screen.
   * Along with the splash screen a "Continue" button is displayed
   * along with a checkbox labeled "Do Not Show Again".  If they check
   * this box then their SplashSettings value is set to the current
   * version and they do not see that splash screen again. We provide
   * a python script for the System Admin to use to set or "bump" the
   * system splash screen version. Presumably you use this when you
   * update the splash screen and want to show the new version to
   * people, including people who had previously checked the "Do Not
   * Show Again" box.
   *
   * Because a system splash version of 0 means "Do not ever show the
   * splash screen" we can leave this feature on. Sites that do not
   * wish to show a splash screen just leave it set to zero.
   *
   * @return true to (maybe) show a splash screen.
   */
  public static boolean showSplashScreen() {
    return true;
  }

  /**
   * If set to return true, a special splash screen offering the person to take
   * a survey is displayed. it is defined in:
   * {@link com.google.appinventor.client.Ode#showSurveySplash}
   * You should alter the wording defined there to be appropriate for your
   * situation. The words there are for MIT.
   *
   * The survey itself is defined in:
   * {@link com.google.appinventor.client.Ode#takeSurvey}
   *
   * Surveys are versioned. Once a person takes a survey they are
   * never shown the survey splash screen again until the value in
   * {@link com.google.appinventor.components.common#YaVersion.SPLASH_SURVEY}
   * is incremented.
   *
   * @return true to display the survey splash screen
   */
  public static boolean showSurveySplashScreen() {
    return false;
  }

  /**
   * If set to true, an account can be in use in only one browser tab.
   * When a new login is detected, all older sessions are no longer
   * permitted to save project files.
   *
   * @return true to limit account use to one session at a time
   */
  public static boolean requireOneLogin() {
    return false;
  }

  public static boolean doCompanionSplashScreen() {
    return false;
  }

}
