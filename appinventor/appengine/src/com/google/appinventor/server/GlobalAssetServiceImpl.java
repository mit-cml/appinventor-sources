// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.storage.StoredData;
import com.google.appinventor.shared.rpc.globalasset.GlobalAssetService;
import com.google.appinventor.shared.rpc.project.GlobalAsset;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.appinventor.server.util.JsonpUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The servlet for global asset management.
 *
 */
@RemoteServiceRelativePath("globalassets")
public class GlobalAssetServiceImpl extends OdeRemoteServiceServlet implements GlobalAssetService {

  private static final Logger LOG = Logger.getLogger(GlobalAssetServiceImpl.class.getName());

  private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();

  @Override
  public List<GlobalAsset> getGlobalAssets() {
    String userId = userInfoProvider.getUserId();
    List<StoredData.GlobalAssetData> storedAssets = storageIo.getGlobalAssets(userId);
    List<GlobalAsset> globalAssets = new ArrayList<GlobalAsset>();
    for (StoredData.GlobalAssetData storedAsset : storedAssets) {
      globalAssets.add(new GlobalAsset(userId, storedAsset.fileName, storedAsset.folder, storedAsset.timestamp));
    }
    return globalAssets;
  }

  @Override
  public void deleteGlobalAsset(String fileName) {
    String userId = userInfoProvider.getUserId();
    storageIo.deleteGlobalAsset(userId, fileName);
  }

  @Override
  public void linkGlobalAssetToProject(long projectId, String globalAssetId, long timestamp) {
    String userId = userInfoProvider.getUserId();
    StoredData.GlobalAssetData globalAssetData = storageIo.getGlobalAssetByFileName(userId, globalAssetId);
    if (globalAssetData != null) {
      // Construct the file name for the project, e.g., "assets/_global_/folder/asset.png"
      String projectFileName = "assets/_global_/";
      if (globalAssetData.folder != null && !globalAssetData.folder.isEmpty()) {
        projectFileName += globalAssetData.folder + "/";
      }
      projectFileName += globalAssetData.fileName;

      try {
        // Add the global asset as a source file to the project
        storageIo.addSourceFilesToProject(userId, projectId, true, projectFileName);
        // Upload the content of the global asset to the project file
        storageIo.uploadRawFileForce(projectId, projectFileName, userId, globalAssetData.content);
        LOG.info("Successfully linked global asset " + globalAssetId + " to project " + projectId);
      } catch (Exception e) {
        LOG.severe("Error linking global asset " + globalAssetId + " to project " + projectId + ": " + e.getMessage());
        throw new RuntimeException("Failed to link global asset: " + e.getMessage());
      }
    } else {
      LOG.warning("Attempted to link non-existent global asset: " + globalAssetId + " for user " + userId);
      throw new RuntimeException("Global asset not found: " + globalAssetId);
    }
  }

  @Override
  public boolean isGlobalAssetUpdated(String globalAssetId, long currentTimestamp) {
    String userId = userInfoProvider.getUserId();
    StoredData.GlobalAssetData globalAssetData = storageIo.getGlobalAssetByFileName(userId, globalAssetId);
    if (globalAssetData != null) {
      return globalAssetData.timestamp > currentTimestamp;
    }
    // If the asset is not found, consider it not updated (or handle as an error based on UX needs)
    return false;
  }

  @Override
  public GlobalAsset getGlobalAsset(String fileName) {
    String userId = userInfoProvider.getUserId();
    StoredData.GlobalAssetData storedAsset = storageIo.getGlobalAssetByFileName(userId, fileName);
    if (storedAsset != null) {
      return new GlobalAsset(userId, storedAsset.fileName, storedAsset.folder, storedAsset.timestamp);
    }
    return null;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, java.io.IOException {
    // This is needed for JSONP
    String userId = userInfoProvider.getUserId();
    String op = req.getParameter("op");
    if (op != null && op.equals("getGlobalAssets")) {
      List<GlobalAsset> globalAssets = getGlobalAssets();
      JsonpUtil.writeJsonResponse(resp, globalAssets);
    } else if (req.getPathInfo() != null && req.getPathInfo().startsWith("/globalasset/")) {
      // Handle global asset download
      String fileName = req.getPathInfo().substring("/globalasset/".length());
      StoredData.GlobalAssetData storedAsset = storageIo.getGlobalAssetByFileName(userId, fileName);
      if (storedAsset != null) {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + storedAsset.fileName + "\"");
        resp.setContentType(storedAsset.mimeType != null ? storedAsset.mimeType : "application/octet-stream");
        resp.getOutputStream().write(storedAsset.content);
      } else {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.getWriter().write("Global asset not found: " + fileName);
      }
    } else {
      super.doGet(req, resp);
    }
  }
}