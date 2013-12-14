// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2013 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectServiceAsync;
import com.google.appinventor.shared.util.Base64Util;
import com.google.appinventor.client.output.OdeLog;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.HashMap;
import java.util.Collection;

/**
 * Manage known assets for a project and arrange to send them to the
 * attached phone as necessary.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */

public final class AssetManager implements ProjectChangeListener {

  private static class AssetInfo { // Describes one asset
    String fileId;
    byte [] fileContent;
    boolean loaded;         // true if already loaded to the repl (phone)
  }

  private HashMap<String, AssetInfo> assets = null;
  private long projectId;
  private Project project;
  private YoungAndroidAssetsFolder assetsFolder;
  private static AssetManager INSTANCE;
  private static boolean DEBUG = false;

  private AssetManager() {
    exportMethodsToJavascript();
  }

  public static AssetManager getInstance() {
    if (INSTANCE == null)
      INSTANCE = new AssetManager();
    return INSTANCE;
  }

  public void loadAssets(long projectId) {

    if (project != null) {    // In case we are changing projects, we toss old project listener
      project.removeProjectChangeListener(this);
    }

    this.projectId = projectId;
    if (DEBUG)
      OdeLog.log("AssetManager: Loading assets for " + projectId);
    if (projectId != 0) {
      project = Ode.getInstance().getProjectManager().getProject(projectId);
      assetsFolder = ((YoungAndroidProjectNode) project.getRootNode()).getAssetsFolder();
      project.addProjectChangeListener(this);
      assets = new HashMap<String,AssetInfo>();
      for (ProjectNode node : assetsFolder.getChildren()) {
        readIn(node);
      }
    } else {
      project = null;
      assetsFolder = null;
      assets = null;
    }
  }

  private void readIn(ProjectNode node) {
    final String fileId = node.getFileId();
    Ode.getInstance().getProjectService().loadraw2(projectId, fileId,
      new AsyncCallback<String>() {
        @Override
          public void onSuccess(String data) {
          AssetInfo assetInfo = new AssetInfo();
          assetInfo.fileId = fileId;
          assetInfo.fileContent = Base64Util.decodeLines(data);
          assetInfo.loaded = false; // Set to true when it is loaded to the repl
          assets.put(fileId, assetInfo);
          if (DEBUG)
            OdeLog.log("Adding asset fileId = " + fileId);
        }

        @Override
          public void onFailure(Throwable ex) {
          OdeLog.elog("Failed to load asset.");
        }
      });
  }

  private void refreshAssets1(String formName) {
    if (DEBUG)
      OdeLog.log("AssetManager: formName = " + formName);
    for (AssetInfo a : assets.values()) {
      if (!a.loaded) {
        boolean didit = doPutAsset(formName, a.fileId, a.fileContent);
        if (didit)
          a.loaded = true;
      }
    }
  }

  public static void refreshAssets(String formName) {
    if (INSTANCE == null)
      return;
    INSTANCE.refreshAssets1(formName);
  }

  public static void reset(String formName) {
    if (INSTANCE == null)
      return;
    INSTANCE.reset1(formName);
  }

  public void reset1(String formName) {
    OdeLog.log("AssetManager: formName = " + formName + " received reset.");
    for (AssetInfo a: assets.values()) {
      a.loaded = false;
    }
  }

  @Override
  public void onProjectLoaded(Project project) {
    if (DEBUG)
      OdeLog.log("AssetManager: got onProjectLoaded for " + project.getProjectId() + ", current project is " + projectId);
    loadAssets(project.getProjectId());
  }

  @Override
  public void onProjectNodeAdded(Project project, ProjectNode node) {
    if (DEBUG)
      OdeLog.log("AssetManager: got projectNodeAdded for node " + node.getFileId()
        + " and project "  + project.getProjectId() + ", current project is " + projectId);
    if (node instanceof YoungAndroidAssetNode) {
      loadAssets(project.getProjectId());
    }
  }

  @Override
  public void onProjectNodeRemoved(Project project, ProjectNode node) {
    if (DEBUG)
      OdeLog.log("AssetManager: got onProjectNodeRemoved for node " + node.getFileId()
        + " and project "  + project.getProjectId() + ", current project is " + projectId);
    if (node instanceof YoungAndroidAssetNode) {
      loadAssets(project.getProjectId());
    }
  }

  private static native void exportMethodsToJavascript() /*-{
    $wnd.AssetManager_refreshAssets =
      $entry(@com.google.appinventor.client.AssetManager::refreshAssets(Ljava/lang/String;));
    $wnd.AssetManager_reset =
      $entry(@com.google.appinventor.client.AssetManager::reset(Ljava/lang/String;));
  }-*/;

  private static native boolean doPutAsset(String formName, String filename, byte[] content) /*-{
    return $wnd.Blocklies[formName].ReplMgr.putAsset(filename, content);
  }-*/;

}
