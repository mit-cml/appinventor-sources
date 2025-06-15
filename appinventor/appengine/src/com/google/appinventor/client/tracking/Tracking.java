// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.tracking;

import com.google.appinventor.common.version.AppInventorFeatures;

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
  public static final String PROJECT_ACTION_RESTORE_PROJECT_YA = PROJECT_ACTION_PREFIX +
      "RestoreProject-YA";
  public static final String PROJECT_ACTION_MOVE_TO_TRASH_PROJECT_YA = PROJECT_ACTION_PREFIX +
      "MoveToTrashProject-YA";
  public static final String PROJECT_ACTION_DELETE_FILE_YA = PROJECT_ACTION_PREFIX +
      "DeleteFile-YA";
  public static final String PROJECT_ACTION_PREVIEW_FILE_YA = PROJECT_ACTION_PREFIX +
      "PreviewFile-YA";
  public static final String PROJECT_ACTION_MOVE_TO_FOLDER_PROJECT_YA = PROJECT_ACTION_PREFIX +
      "MoveToFolderProject-YA";
  public static final String PROJECT_ACTION_BUILD_BARCODE_YA = PROJECT_ACTION_PREFIX +
      "BuildBarcode-YA";
  public static final String PROJECT_ACTION_DOWNLOAD_CACHED_BUILD = PROJECT_ACTION_PREFIX +
      "DownloadCachedBuild-YA";
  public static final String PROJECT_ACTION_BUILD_DOWNLOAD_YA = PROJECT_ACTION_PREFIX +
      "BuildDownload-YA";
  public static final String PROJECT_ACTION_BUILD_YAIL_YA = PROJECT_ACTION_PREFIX +
      "BuildYail-YA";
  public static final String PROJECT_ACTION_DOWNLOAD_PROJECT_SOURCE_YA = PROJECT_ACTION_PREFIX +
      "DownloadProjectSource-YA";
  public static final String PROJECT_ACTION_DOWNLOAD_FILE_YA = PROJECT_ACTION_PREFIX +
      "DownloadFile-YA";
  public static final String PROJECT_ACTION_DOWNLOAD_SELECTED_PROJECTS_SOURCE_YA =
      PROJECT_ACTION_PREFIX + "DownloadSelectedProjectsSource-YA";
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
  public static final String PROJECT_ACTION_CACHE_PROJECT = PROJECT_ACTION_PREFIX + 
      "CacheProject";
  public static final String PROJECT_SUBACTION_BUILD_YA = PROJECT_ACTION_PREFIX +
      "Build-Subcommand-YA";

  public static final String EDITOR_EVENT = "Editor";
  public static final String EDITOR_ACTION_SHOW_DESIGNER = "ShowDesigner";
  public static final String EDITOR_ACTION_SHOW_BLOCKS = "ShowBlocks";

  public static final String CONNECT_EVENT = "Connect";
  public static final String CONNECT_ACTION_WIFI = "Wifi";
  public static final String CONNECT_ACTION_CHROMEBOOK = "Chromebook";
  public static final String CONNECT_ACTION_EMULATOR = "Emulator";
  public static final String CONNECT_ACTION_USB = "Usb";
  public static final String CONNECT_ACTION_RESEND = "Resend";
  public static final String CONNECT_ACTION_RESET = "Reset";
  public static final String CONNECT_ACTION_HARD_RESET = "HardReset";

  public static final String USER_EVENT = "User";
  public static final String USER_ACTION_PREFIX = "User_";
  public static final String USER_ACTION_DOWNLOAD_KEYSTORE = USER_ACTION_PREFIX + "DownloadKeystore";
  public static final String USER_ACTION_UPLOAD_KEYSTORE = USER_ACTION_PREFIX + "UploadKeystore";
  public static final String USER_ACTION_DELETE_KEYSTORE = USER_ACTION_PREFIX + "DeleteKeystore";

  private Tracking() {
  }

  public static void trackEvent(String event) {
    if (trackingAvailable()) {
      gaTrackEvent(event);
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

  private static native void gaTrackEvent(String event) /*-{
    if ($wnd.gtag) {
      $wnd.gtag('event', event);
    }
  }-*/;

  /*
   * Track an event.
   */
  private static native void gaTrackEvent(String event, String action) /*-{
    if ($wnd.gtag) {
      $wnd.gtag('event', event, {'action': action});
    }
  }-*/;

  /*
   * Track an event.
   */
  private static native void gaTrackEvent(String event, String action, String label) /*-{
    if ($wnd.gtag) {
      $wnd.gtag('event', event, {'action': action, 'label': label});
    }
  }-*/;

  /*
   * Track an event.
   */
  private static native void gaTrackEvent(String event, String action, String label, int value) /*-{
    if ($wnd.gtag) {
      $wnd.gtag('event', event, {'action': action, 'label': label, 'value': value});
    }
  }-*/;
}
