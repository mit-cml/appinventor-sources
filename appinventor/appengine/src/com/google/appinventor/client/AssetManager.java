// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.appinventor.client.explorer.dialogs.ProgressBarDialogBox;
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

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.google.appinventor.client.Ode.MESSAGES;

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
  private ProgressBarDialogBox progress = null;
  private List<String> extensions = new ArrayList<>();
  private int retryCount = 0;
  private volatile int assetTransferProgress = 0;

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
      extensions.clear();
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
            if (node.getName().equals("classes.jar")) {
              extensions.add(node.getFileId().split("/")[2]);
            }
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
        int depth = StringUtils.countMatches(fileId, "/");
        if (depth == 3) {

          // Filter : For classes.jar File
          if (name.equals("classes.jar")) {
            allow = true;

          }
        } else if (depth > 3) {
          String[] parts = fileId.split("/");
          if (ASSETS_FOLDER.equals(parts[3])) {
            return true;
          }
        }
      }
    }
    return allow | allowAll;
  }

  private void readIn(final AssetInfo assetInfo) {
    Ode.getInstance().getProjectService().loadraw2(projectId, assetInfo.fileId,
      new AsyncCallback<String>() {
        @Override
          public void onSuccess(String data) {
            assetTransferProgress++;
            assetInfo.fileContent = Base64Util.decodeLines(data);
            assetInfo.loaded = false; // Set to true when it is loaded to the repl
            assetInfo.transferred = false; // Set to true when file is received on phone
            refreshAssets1();
          }
        @Override
          public void onFailure(Throwable ex) {
          if (retryCount > 0) {
            retryCount--;
            readIn(assetInfo);
          } else {
            OdeLog.elog("Failed to load asset.");
          }
        }
      });
  }

  private void refreshAssets1(JavaScriptObject callback) {
    assetsTransferredCallback = callback;
    assetTransferProgress = 0;
    refreshAssets1();
  }

  private void refreshAssets1() {
    for (AssetInfo a : assets.values()) {
      if (!a.loaded) {
        if (progress == null) {
          progress = new ProgressBarDialogBox("AssetManager", project.getRootNode());
          progress.setProgress(0, MESSAGES.startingAssetTransfer());
          progress.showDismissButton();
        } else if (!progress.isShowing() && progress.getProgressBarShow() < 2) {
          progress.show();
          progress.center();
        }
        if (a.fileContent == null && !useWebRTC()) { // Need to fetch it from the server
          retryCount = 3;
          if (progress != null) {
            progress.setProgress(100 * assetTransferProgress / (2 * assets.size()),
                MESSAGES.loadingAsset(a.fileId));
          }
          readIn(a);          // Read it in asynchronously
          break;                     // we'll resume when we have it
        } else {
          if (progress != null) {
            progress.setProgress(100 * assetTransferProgress / (2 * assets.size()),
                MESSAGES.sendingAssetToCompanion(a.fileId));
          }
          boolean didit = doPutAsset(Long.toString(projectId), a.fileId, a.fileContent);
          if (didit) {
            assetTransferProgress++;
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

  public static void refreshAssets(JavaScriptObject callback) {
    if (INSTANCE == null)
      return;
    INSTANCE.refreshAssets1(callback);
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
    // Dismiss the progress bar if showing
    if (progress != null && progress.isShowing()) {
      progress.hide(true);
    }
    progress = null;
    // If we get here, then all assets have been transferred to the device
    // so we fire the assetsTransferredCallback
    doCallBack(assetsTransferredCallback);
    // Dismiss the progress bar if showing
    if (progress != null && progress.isShowing()) {
      progress.hide(true);
    }
    progress = null;
    return  true;
  }

  public static JsArrayString getExtensionsToLoad() {
    JsArrayString result = JsArrayString.createArray().cast();
    if (INSTANCE != null) {
      for (String s : INSTANCE.extensions) {
        result.push(s);
      }
    }
    return result;
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
      $entry(@com.google.appinventor.client.AssetManager::refreshAssets(Lcom/google/gwt/core/client/JavaScriptObject;));
    $wnd.AssetManager_reset =
      $entry(@com.google.appinventor.client.AssetManager::reset(Ljava/lang/String;));
    $wnd.AssetManager_markAssetTransferred =
      $entry(@com.google.appinventor.client.AssetManager::markAssetTransferred(Ljava/lang/String;));
    $wnd.AssetManager_getExtensions =
      $entry(@com.google.appinventor.client.AssetManager::getExtensionsToLoad());
  }-*/;

  private static native boolean doPutAsset(String projectId, String filename, byte[] content) /*-{
    return Blockly.ReplMgr.putAsset(projectId, filename, content, function() { window.parent.AssetManager_markAssetTransferred(filename) });
  }-*/;

  private static native void doCallBack(JavaScriptObject callback) /*-{
    if (typeof callback === 'function') callback.call(null);
  }-*/;

  private static native boolean useWebRTC() /*-{
    return top.usewebrtc;
  }-*/;

}
