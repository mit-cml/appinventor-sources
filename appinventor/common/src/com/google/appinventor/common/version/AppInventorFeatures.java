// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
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

  /**
   * If set to true the Companion will display the splash screen (defined in
   * splash.html)
   *
   * @return true to display the splash screen in the Companion
   */
  public static boolean doCompanionSplashScreen() {
    return false;
  }

  /**
   * If set to true, the Blockly XML will be prettified for human readability.
   * If false, the XML is serialized to a more compact form with minimal whitespace.
   */
  public static boolean doPrettifyXml() {
    return false;
  }

  /**
   * If set to true, features marked as part of future App Inventor
   * iterations will be shown.
   */
  public static boolean enableFutureFeatures() {
    return false;
  }

  /**
   * If set to true, redirect http connections to https if running in
   * production (aka, not the Google Dev server).
   */

  public static boolean enableHttpRedirect() {
    return true;
  }

  public static String chatBotHost() {
    return "https://chatbot.appinventor.mit.edu/";
  }

  public static boolean doingSurvey() {
    return false;
  }

  /**
   * If set to true, iOS ad hoc builds will be allowed.
   *
   * @return true to enable iOS ad hoc builds
   */
  public static boolean allowIosBuilds() {
    return false;
  }

  /**
   * If set to true, iOS builds for the App Store will be allowed.
   *
   * @return true to enable iOS app store builds
   */
  public static boolean allowAppStoreBuilds() {
    return false;
  }

  /**
   * Master switch for the AI agent feature. When false, the Form
   * AIAgentMode property is hidden, the chat dialog toolbar entry does
   * not appear, and Blockly's AI assistance integration is disabled.
   */
  public static boolean aiAgentAvailable() {
    return false;
  }

  /**
   * If set to true, AI agent debug logging is captured per request.
   * Dev mode writes to {@code build/logs/aiagent/<conversationId>/<timestamp>.txt};
   * production routes to the {@code aiagent.debug} logger for external
   * ingestion. Nothing goes to the console in either mode.
   */
  public static boolean aiAgentDebugEnabled() {
    return true;
  }

  /**
   * If set to true, ScreenEditor and ProjectEditor AI agent modes are
   * available alongside Advisor. When false, only Advisor is offered and
   * any pre-existing editor-mode setting is coerced to Advisor at read
   * time.
   */
  public static boolean aiAgentEditingModesEnabled() {
    return false;
  }

  /**
   * If set to true, Plan & Execute mode (multi-agent orchestration) is
   * available in Project Editor mode. When false, the orchestration
   * system is hidden everywhere.
   */
  public static boolean aiAgentOrchestrationEnabled() {
    return true;
  }

  /**
   * If set to true, the "Edit & Approve" button is shown on Plan & Execute
   * plan cards. When false, only Approve and Reject are shown.
   */
  public static boolean aiAgentPlanEditEnabled() {
    return false;
  }

  /**
   * If set to true, tutorial content is included in the LLM context when
   * a project has a TutorialURL set.
   */
  public static boolean aiAgentTutorialContextEnabled() {
    return true;
  }

  /**
   * If set to true, Companion runtime state is rendered into the LLM
   * context and the Companion read tools are exposed when the client
   * attaches a snapshot.
   */
  public static boolean aiAgentCompanionContextEnabled() {
    return true;
  }

  /**
   * If set to true, the agent retries with a nudge when the LLM responds
   * with text only (no tool calls) in editing modes. When false,
   * narration-only responses are returned as-is.
   */
  public static boolean aiAgentRetryNarrationEnabled() {
    return false;
  }
}
