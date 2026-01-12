// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.appinventor.client.explorer.dialogs.ProgressBarDialogBox;

import com.google.appinventor.client.explorer.project.Project;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Show a Progress Bar during connection from the Browser to the Companion.
 * We are designed by be used from a static context. This facilitates calls
 * from both the Java (GWT) side of things and the Javascript (blockly) side
 * of things.
 *
 * Javascript starts the progress bar when we learn the connection information
 * we need from the Rendezvous server. After a connection to the Companion is complete
 * we hand control over to the AssetManager (in GWT land).
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 *
 */


public final class ConnectProgressBar {

  private static ConnectProgressBar INSTANCE = new ConnectProgressBar();
  private ProgressBarDialogBox progressBar;
  private boolean shouldShow = false;

  private ConnectProgressBar() {
    exportMethodsToJavascript();
  }

  public static ConnectProgressBar getInstance() {
    return INSTANCE;
  }

  public static void start() {
    INSTANCE.start1();
  }

  private void start1() {
    Ode ode = Ode.getInstance();
    Project currentProject = ode.getProjectManager().getProject(ode.getCurrentYoungAndroidProjectId());
    if (progressBar == null) {
      progressBar = new ProgressBarDialogBox("ConnectProgressBar", currentProject.getRootNode());
      progressBar.show();
      progressBar.center();
      progressBar.setProgress(0, MESSAGES.startingConnectionDialog());
      progressBar.showDismissButton();
    } else if (!progressBar.isShowing() && progressBar.getProgressBarShow() < 2) {
      progressBar.show();
      progressBar.center();
    }
  }

  public static void setProgress(int progress, String message) {
    INSTANCE.setProgress1(progress, message);
  }

  private void setProgress1(int progress, String message) {
    if (progressBar != null && progressBar.isShowing()) {
      progressBar.setProgress(progress, message);
    }
  }

  public static void hide() {
    INSTANCE.hide1();
  }

  private void hide1() {
    if (progressBar != null && progressBar.isShowing()) {
      progressBar.hide(true);
    }
    progressBar = null;
  }

  public static void tempHide(boolean hide) {
    INSTANCE.tempHide1(hide);
  }

  private void tempHide1(boolean hide) {
    if (progressBar == null) {
      return;                 // Nothing to do
    }
    if (hide) {
      shouldShow = progressBar.isShowing();
      if (shouldShow) {
        progressBar.hide(true);
      }
    } else if (shouldShow) {
      progressBar.show();
      progressBar.center();
    }
  }

  private static native void exportMethodsToJavascript() /*-{
    $wnd.ConnectProgressBar_start =
      $entry(@com.google.appinventor.client.ConnectProgressBar::start());
    $wnd.ConnectProgressBar_setProgress =
      $entry(@com.google.appinventor.client.ConnectProgressBar::setProgress(ILjava/lang/String;));
    $wnd.ConnectProgressBar_hide =
      $entry(@com.google.appinventor.client.ConnectProgressBar::hide());
  }-*/;

}
