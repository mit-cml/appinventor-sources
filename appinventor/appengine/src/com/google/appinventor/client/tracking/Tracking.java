// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.tracking;

import com.google.appinventor.client.Ode;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * Methods for tracking/logging with Google Analytics.
 *
 */
public class Tracking {

  /**
   * Pre-defined events and actions.
   */
  public static final String PROJECT_EVENT = "Project";
  public static final String PROJECT_ACTION_PREFIX = "Project_";
  public static final String PROJECT_ACTION_NEW_YA = PROJECT_ACTION_PREFIX +
      "New-YA";
  public static final String PROJECT_ACTION_DELETE_PROJECT_YA = PROJECT_ACTION_PREFIX +
      "DeleteProject-YA";
  public static final String PROJECT_ACTION_DELETE_FILE_YA = PROJECT_ACTION_PREFIX +
      "DeleteFile-YA";
  public static final String PROJECT_ACTION_BUILD_BARCODE_YA = PROJECT_ACTION_PREFIX +
      "BuildBarcode-YA";
  public static final String PROJECT_ACTION_BUILD_DOWNLOAD_YA = PROJECT_ACTION_PREFIX +
      "BuildDownload-YA";
  public static final String PROJECT_ACTION_BUILD_YAIL_YA = PROJECT_ACTION_PREFIX +
      "BuildYail-YA";
  public static final String PROJECT_ACTION_DOWNLOAD_PROJECT_SOURCE_YA = PROJECT_ACTION_PREFIX +
      "DownloadProjectSource-YA";
  public static final String PROJECT_ACTION_DOWNLOAD_FILE_YA = PROJECT_ACTION_PREFIX +
      "DownloadFile-YA";
  public static final String PROJECT_ACTION_DOWNLOAD_ALL_PROJECTS_SOURCE_YA =
      PROJECT_ACTION_PREFIX + "DownloadAllProjectsSource-YA";
  public static final String PROJECT_ACTION_SAVE_YA = PROJECT_ACTION_PREFIX +
      "Save-YA";
  public static final String PROJECT_ACTION_SAVE_AS_YA = PROJECT_ACTION_PREFIX +
      "SaveAs-YA";
  public static final String PROJECT_ACTION_CHECKPOINT_YA = PROJECT_ACTION_PREFIX +
      "Checkpoint-YA";
  public static final String PROJECT_ACTION_ADDFORM_YA = PROJECT_ACTION_PREFIX +
      "AddForm-YA";
  public static final String PROJECT_ACTION_REMOVEFORM_YA = PROJECT_ACTION_PREFIX +
      "RemoveForm-YA";
  public static final String PROJECT_SUBACTION_BUILD_YA = PROJECT_ACTION_PREFIX +
      "Build-Subcommand-YA";

  public static final String USER_EVENT = "User";
  public static final String USER_ACTION_PREFIX = "User_";
  public static final String USER_ACTION_DOWNLOAD_KEYSTORE = USER_ACTION_PREFIX + "DownloadKeystore";
  public static final String USER_ACTION_UPLOAD_KEYSTORE = USER_ACTION_PREFIX + "UploadKeystore";
  public static final String USER_ACTION_DELETE_KEYSTORE = USER_ACTION_PREFIX + "DeleteKeystore";

  // Google Analytics account
  private static final String GA_ACCOUNT = "UA-5856106-2";

  // Google Analytics tracker object
  @SuppressWarnings("unused")
  private static JavaScriptObject gaTracker;
  static {
    if (trackingAvailable()) {
      gaInit(GA_ACCOUNT);
    }
  }

  private Tracking() {
  }

  /**
   * Track a page view.
   */
  public static void trackPageview() {
    if (trackingAvailable()) {
      gaTrackPageview();
    }
  }

  /**
   * Track an event.
   *
   * @param event  event category
   * @param action  event action
   */
  public static void trackEvent(String event, String action) {
    if (trackingAvailable()) {
      gaTrackEvent(event, action);
      // Should we continue to log every "event" as a page view (which is how
      // Herbert set it up) in addition to logging it as an actual event?
      gaTrackEventAsPage("/event/" + event + '/' + action);
    }
  }

  /**
   * Track an event.
   *
   * @param event  event category
   * @param action  event action
   * @param label  event label
   */
  public static void trackEvent(String event, String action, String label) {
    if (trackingAvailable()) {
      gaTrackEvent(event, action, label);
      // Should we continue to log every "event" as a page view (which is how
      // Herbert set it up) in addition to logging it as an actual event?
      gaTrackEventAsPage("/event/" + event + '/' + action);
    }
  }

  /**
   * Track an event.
   *
   * @param event  event category
   * @param action  event action
   * @param label  event label
   * @param value  event value (latency)
   */
  public static void trackEvent(String event, String action, String label, int value) {
    if (trackingAvailable()) {
      gaTrackEvent(event, action, label, value);
    }
  }

  /*
   * Indicates whether to track or not.
   */
  private static boolean trackingAvailable() {
    return AppInventorFeatures.trackClientEvents();
  }

  /*
   * Initializes the tracker object.
   */
  private static native void gaInit(String key) /*-{
    var _gat = $wnd["Ode.Tracking"];
    if (_gat != null) {
      var tracker = _gat._getTracker(key);
      if (tracker != null) {
        tracker._initData();
        @com.google.appinventor.client.tracking.Tracking::gaTracker = tracker;
      }
    }
  }-*/;

  /*
   * Tracks a page view.
   */
  private static native void gaTrackPageview() /*-{
    var tracker = @com.google.appinventor.client.tracking.Tracking::gaTracker;
    if (tracker != null) {
      tracker._trackPageview();
    }
  }-*/;

  /*
   * Track an event as a pageview.  For backwards compatibility purposes only.
   */
  private static native void gaTrackEventAsPage(String event) /*-{
    var tracker = @com.google.appinventor.client.tracking.Tracking::gaTracker;
    if (tracker != null) {
      tracker._trackPageview(event);
    }
  }-*/;

  /*
   * Track an event.
   */
  private static native void gaTrackEvent(String event, String action) /*-{
    var tracker = @com.google.appinventor.client.tracking.Tracking::gaTracker;
    if (tracker != null) {
      tracker._trackEvent(event, action);
    }
  }-*/;

  /*
   * Track an event.
   */
  private static native void gaTrackEvent(String event, String action, String label) /*-{
    var tracker = @com.google.appinventor.client.tracking.Tracking::gaTracker;
    if (tracker != null) {
      tracker._trackEvent(event, action, label);
    }
  }-*/;

  /*
   * Track an event.
   */
  private static native void gaTrackEvent(String event, String action, String label, int value) /*-{
    var tracker = @com.google.appinventor.client.tracking.Tracking::gaTracker;
    if (tracker != null) {
      tracker._trackEvent(event, action, label, value);
    }
  }-*/;
}
