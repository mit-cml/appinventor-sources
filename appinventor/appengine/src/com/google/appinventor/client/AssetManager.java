// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2013 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidComponentNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidComponentsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.util.Base64Util;
import com.google.appinventor.client.output.OdeLog;

import com.google.gwt.core.client.JavaScriptObject;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.HashMap;

/**
 * Manage known assets and components for a project and arrange to send them to the
 * attached phone as necessary.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */

public final class AssetManager implements ProjectChangeListener {

  private static class AssetInfo { // Describes one asset
    String fileId;
    byte [] fileContent;
    boolean loaded;         // true if already loaded to the repl (phone)
    boolean transferred;           // true if asset received on phone
  }

  private HashMap<String, AssetInfo> assets = null;
  private long projectId;
  private Project project;
  private YoungAndroidAssetsFolder assetsFolder;
  private YoungAndroidComponentsFolder componentsFolder;
  private JavaScriptObject assetsTransferredCallback;

  private static AssetManager INSTANCE;
  private static boolean DEBUG = false;
  private static final String ASSETS_FOLDER = "assets";
  private static final String EXTERNAL_COMPS_FOLDER = "external_comps";


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
      componentsFolder = ((YoungAndroidProjectNode) project.getRootNode()).getComponentsFolder();
      project.addProjectChangeListener(this);
      assets = new HashMap<String,AssetInfo>();
      // Add Asset Files
      for (ProjectNode node : assetsFolder.getChildren()) {
        if (nodeFilter(node)) {
          if (node.getChildren().iterator().hasNext()) {
            loadAssets(node);
            continue;
          }
          else {
            assetSetup(node);
          }
        }
      }
      // Add Component Files
      for (ProjectNode node : componentsFolder.getChildren()) {
        if (nodeFilter(node)) {
          if (node.getChildren().iterator().hasNext()) {
            loadAssets(node);
            continue;
          }
          else {
            assetSetup(node);
          }
        }
      }
    } else {
      project = null;
      assetsFolder = null;
      assetsTransferredCallback = null;
      assets = null;
    }
  }

  /**
   * Recursively add
   * @param nodeFolder
   */
  public void loadAssets(ProjectNode nodeFolder) {
    long targetProjectId = nodeFolder.getProjectId();
    if (this.projectId != targetProjectId) { // We are on a different project so stop and change project
      loadAssets(targetProjectId); // redo
      return;
    }
    for (ProjectNode node : nodeFolder.getChildren()) {
      if (node.getChildren().iterator().hasNext()) {  // is a directory
        loadAssets(node); // setup files inside it
        continue;
      }
      assetSetup(node); // is a file
    }
  }

  private void assetSetup(ProjectNode node) {
    String fileId = node.getFileId();
    AssetInfo assetInfo = new AssetInfo();
    assetInfo.fileId = fileId;
    assetInfo.fileContent = null;
    assetInfo.loaded = false; // Set to true when it is loaded to the repl
    assetInfo.transferred = false; // Set to true when asset is received on phone
    assets.put(fileId, assetInfo);
  }

  /**
   * Filter that allows only specific nodes to be sent to AssetManager for Transfer
   * @param node
   * @return true to allow transfer
   */
  private boolean nodeFilter(ProjectNode node) {
    boolean allowAll = false; // Set to true to allow all Asset and Component Files!
    boolean allow = false;
    String name = node.getName();
    String fileId = node.getFileId();
    // Filter : For files in ASSETS_FOLDER
    if (fileId.startsWith(ASSETS_FOLDER)) {
      allow = true;

      // Filter : For files in EXTERNAL_COMPS_FOLDER
      if (fileId.startsWith(ASSETS_FOLDER + '/' + EXTERNAL_COMPS_FOLDER + '/')) {
        allow = false;

        // Filter : For files in directly in EXTERNAL_COMPS_FOLDER/COMP_FOLDER
        if (StringUtils.countMatches(fileId, "/") == 3) {

          // Filter : For classes.jar File
          if (name.equals("classes.jar")) {
            allow = true;

          }
        }
      }
    }
    return allow | allowAll;
  }

  private void readIn(final AssetInfo assetInfo, final String formName) {
    Ode.getInstance().getProjectService().loadraw2(projectId, assetInfo.fileId,
      new AsyncCallback<String>() {
        @Override
          public void onSuccess(String data) {
            assetInfo.fileContent = Base64Util.decodeLines(data);
            assetInfo.loaded = false; // Set to true when it is loaded to the repl
            assetInfo.transferred = false; // Set to true when file is received on phone
            refreshAssets1(formName);
          }
        @Override
          public void onFailure(Throwable ex) {
          OdeLog.elog("Failed to load asset.");
        }
      });
  }

  private void refreshAssets1(String formName, JavaScriptObject callback) {
    assetsTransferredCallback = callback;
    refreshAssets1(formName);
  }

  private void refreshAssets1(String formName) {
    if (DEBUG)
      OdeLog.log("AssetManager: formName = " + formName);
    for (AssetInfo a : assets.values()) {
      if (!a.loaded) {
        if (a.fileContent == null) { // Need to fetch it from the server
          readIn(a, formName);       // Read it in asynchronously
          break;                     // we'll resume when we have it
        } else {
          boolean didit = doPutAsset(formName, a.fileId, a.fileContent);
          if (didit) {
            a.loaded = true;
            a.fileContent = null; // Save memory
          }
        }
      }
    }
    // If no assets are in the project, perform the callback immediately.
    if (assets.values().size() == 0 && assetsTransferredCallback != null) {
      doCallBack(assetsTransferredCallback);
    }
  }

  public static void refreshAssets(String formName, JavaScriptObject callback) {
    if (INSTANCE == null)
      return;
    INSTANCE.refreshAssets1(formName, callback);
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
      a.transferred = false;
    }
  }

  public static boolean markAssetTransferred(String transferredAsset) {
    if (INSTANCE == null)
      return false;
    INSTANCE.markAssetTransferred1(transferredAsset);
    return true;
  }

  public boolean markAssetTransferred1(String transferredAsset) {
    if (transferredAsset == null)
      return false;
    AssetInfo assetInfo = INSTANCE.assets.get(transferredAsset);
    assetInfo.transferred = true;
    // Let's see if all assets are transferred. If so, fire the
    // assetsTransferredCallback
    for (AssetInfo a : assets.values()) {
      if (!a.transferred) {     // Something didn't get transferred
        return true;
      }
    }
    // If we get here, then all assets have been transferred to the device
    // so we fire the assetsTransferredCallback
    doCallBack(assetsTransferredCallback);
    return  true;
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
    if (node instanceof YoungAndroidAssetNode || node instanceof YoungAndroidComponentNode) {
      loadAssets(project.getProjectId());
    }
  }

  @Override
  public void onProjectNodeRemoved(Project project, ProjectNode node) {
    if (DEBUG)
      OdeLog.log("AssetManager: got onProjectNodeRemoved for node " + node.getFileId()
        + " and project "  + project.getProjectId() + ", current project is " + projectId);
    if (node instanceof YoungAndroidAssetNode || node instanceof YoungAndroidComponentNode) {
      loadAssets(project.getProjectId());
    }
  }

  private static native void exportMethodsToJavascript() /*-{
    $wnd.AssetManager_refreshAssets =
      $entry(@com.google.appinventor.client.AssetManager::refreshAssets(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;));
    $wnd.AssetManager_reset =
      $entry(@com.google.appinventor.client.AssetManager::reset(Ljava/lang/String;));
    $wnd.AssetManager_markAssetTransferred =
      $entry(@com.google.appinventor.client.AssetManager::markAssetTransferred(Ljava/lang/String;));
  }-*/;

  private static native boolean doPutAsset(String formName, String filename, byte[] content) /*-{
    return $wnd.Blocklies[formName].ReplMgr.putAsset(filename, content, function() { window.parent.AssetManager_markAssetTransferred(filename) });
  }-*/;

  private static native void doCallBack(JavaScriptObject callback) /*-{
    callback.call(null);
  }-*/;

}
