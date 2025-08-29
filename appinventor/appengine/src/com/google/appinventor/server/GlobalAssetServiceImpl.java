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
import com.google.appinventor.shared.rpc.globalasset.AssetConflictInfo;
import com.google.appinventor.shared.rpc.project.GlobalAsset;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.json.JSONObject;

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
      globalAssets.add(new GlobalAsset(userId, storedAsset.fileName, storedAsset.folder, storedAsset.timestamp, new ArrayList<String>()));
    }
    return globalAssets;
  }

  @Override
  public void deleteGlobalAsset(String fileName) {
    String userId = userInfoProvider.getUserId();
    StoredData.GlobalAssetData globalAssetData = storageIo.getGlobalAssetByFileName(userId, fileName);

    if (globalAssetData == null) {
      LOG.warning("Attempted to delete non-existent global asset: " + fileName + " for user " + userId);
      throw new RuntimeException("Global asset not found: " + fileName);
    }

    if (globalAssetData.referencedBy != null && !globalAssetData.referencedBy.isEmpty()) {
      throw new RuntimeException("Cannot delete asset: " + fileName + " is still used by " + globalAssetData.referencedBy.size() + " projects.");
    }

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
        byte[] globalAssetContent = storageIo.downloadRawGlobalAsset(userId, globalAssetId);
        storageIo.uploadRawFileForce(projectId, projectFileName, userId, globalAssetContent);
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
      return new GlobalAsset(userId, storedAsset.fileName, storedAsset.folder, storedAsset.timestamp, new ArrayList<String>());
    }
    return null;
  }

  @Override
  public void uploadGlobalAsset(String name, String type, byte[] content, List<String> tags, String folder) {
    String userId = userInfoProvider.getUserId();
    storageIo.uploadGlobalAsset(userId, folder, name, content);
    // TODO(user): Add tags to the asset.
  }

  @Override
  public void updateGlobalAsset(String id, String name, List<String> tags, String folder) {
    String userId = userInfoProvider.getUserId();
    LOG.info("Updating global asset with ID: " + id + " for user: " + userId);
    LOG.info("New name: " + name);
    LOG.info("New tags: " + tags);
    LOG.info("New folder: " + folder);

    StoredData.GlobalAssetData globalAssetData = storageIo.getGlobalAsset(userId, id);
    if (globalAssetData == null) {
      LOG.warning("Attempted to update non-existent global asset: " + id + " for user: " + userId);
      throw new RuntimeException("Global asset not found: " + id);
    }

    // Update metadata fields
    globalAssetData.fileName = name; // Assuming 'name' is the new fileName
    globalAssetData.folder = folder;
    // Tags are not directly stored in GlobalAssetData, but can be added if needed.
    // globalAssetData.tags = tags; // Uncomment if tags are added to StoredData.GlobalAssetData

    // Save updated entity
    byte[] globalAssetContent = storageIo.downloadRawGlobalAsset(userId, id);
    storageIo.uploadGlobalAsset(userId, folder, name, globalAssetContent); // Re-upload with new metadata
  }

  @Override
  public void importAssetIntoProject(String assetId, String projectIdStr, boolean trackUsage) {
    String userId = userInfoProvider.getUserId();
    long projectId = Long.parseLong(projectIdStr);
    StoredData.GlobalAssetData globalAssetData = storageIo.getGlobalAssetByFileName(userId, assetId);

    if (globalAssetData == null) {
      LOG.warning("Attempted to import non-existent global asset: " + assetId + " for user " + userId);
      throw new RuntimeException("Global asset not found: " + assetId);
    }

    // If tracking, add project ID to referencedBy list
    if (trackUsage) {
      List<Long> referencedBy = globalAssetData.referencedBy;
      if (referencedBy == null) {
        referencedBy = new ArrayList<>();
      }
      if (!referencedBy.contains(projectId)) {
        referencedBy.add(projectId);
        storageIo.updateGlobalAssetReferencedBy(userId, assetId, referencedBy);
      }
    }

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
      byte[] globalAssetContent = storageIo.downloadRawGlobalAsset(userId, assetId);
      if (globalAssetContent == null || globalAssetContent.length == 0) {
        LOG.severe("Failed to download global asset content for: " + assetId + " user: " + userId);
        throw new RuntimeException("Failed to download global asset content: " + assetId);
      }
      
      LOG.info("Importing global asset " + assetId + " to project " + projectId + " as " + projectFileName + 
               " (size: " + globalAssetContent.length + " bytes)");
      storageIo.uploadRawFileForce(projectId, projectFileName, userId, globalAssetContent);

      // Update project metadata
      String projectSettings = storageIo.loadProjectSettings(userId, projectId);
      JSONObject projectSettingsJson;
      if (projectSettings != null && !projectSettings.isEmpty()) {
        projectSettingsJson = new JSONObject(projectSettings);
      } else {
        projectSettingsJson = new JSONObject();
      }

      JSONObject globalAssetMetadata = new JSONObject();
      globalAssetMetadata.put("globalAssetKey", assetId);
      globalAssetMetadata.put("syncedAtTimestamp", globalAssetData.timestamp);
      // Assuming localCopyBlobKey is not directly exposed or needed for now, 
      // but can be added if the storageIo provides it.

      projectSettingsJson.put("globalAsset_" + assetId.replace('.', '_'), globalAssetMetadata);
      storageIo.storeProjectSettings(userInfoProvider.getSessionId(), projectId, projectSettingsJson.toString());

      LOG.info("Successfully imported global asset " + assetId + " into project " + projectId + ", tracked: " + trackUsage);
    } catch (Exception e) {
      LOG.severe("Error importing global asset " + assetId + " to project " + projectId + ": " + e.getMessage());
      throw new RuntimeException("Failed to import global asset: " + e.getMessage());
    }
  }

  @Override
  public void updateGlobalAssetFolder(String assetId, String folder) {
    String userId = userInfoProvider.getUserId();
    storageIo.updateGlobalAssetFolder(userId, assetId, folder);
  }

  @Override
  public boolean syncGlobalAsset(String assetId, String projectIdStr) {
    String userId = userInfoProvider.getUserId();
    long projectId = Long.parseLong(projectIdStr);

    StoredData.GlobalAssetData globalAssetData = storageIo.getGlobalAssetByFileName(userId, assetId);
    if (globalAssetData == null) {
      LOG.warning("Attempted to sync non-existent global asset: " + assetId + " for user " + userId);
      return false;
    }

    String projectSettings = storageIo.loadProjectSettings(userId, projectId);
    JSONObject projectSettingsJson;
    long syncedAtTimestamp = 0;
    String localCopyBlobKey = null; 

    try {
      if (projectSettings != null && !projectSettings.isEmpty()) {
        projectSettingsJson = new JSONObject(projectSettings);
        String assetMetadataKey = "globalAsset_" + assetId.replace('.', '_');
        if (projectSettingsJson.has(assetMetadataKey)) {
          JSONObject globalAssetMetadata = projectSettingsJson.getJSONObject(assetMetadataKey);
          syncedAtTimestamp = globalAssetMetadata.optLong("syncedAtTimestamp", 0);
          localCopyBlobKey = globalAssetMetadata.optString("localCopyBlobKey", null);
        }
      } else {
        projectSettingsJson = new JSONObject();
      }

      if (globalAssetData.timestamp > syncedAtTimestamp) {
        // Global asset is newer, replace local copy
        String projectFileName = "assets/_global_/";
        if (globalAssetData.folder != null && !globalAssetData.folder.isEmpty()) {
          projectFileName += globalAssetData.folder + "/";
        }
        projectFileName += globalAssetData.fileName;

        byte[] globalAssetContent = storageIo.downloadRawGlobalAsset(userId, assetId);
        storageIo.uploadRawFileForce(projectId, projectFileName, userId, globalAssetContent);

        // Update syncedAtTimestamp in project metadata
        JSONObject globalAssetMetadata = new JSONObject();
        globalAssetMetadata.put("globalAssetKey", assetId);
        globalAssetMetadata.put("syncedAtTimestamp", globalAssetData.timestamp);
        if (localCopyBlobKey != null) {
          globalAssetMetadata.put("localCopyBlobKey", localCopyBlobKey);
        }

        projectSettingsJson.put("globalAsset_" + assetId.replace('.', '_'), globalAssetMetadata);
        storageIo.storeProjectSettings(userInfoProvider.getSessionId(), projectId, projectSettingsJson.toString());

        LOG.info("Successfully synced global asset " + assetId + " in project " + projectId);
        return true;
      } else {
        LOG.info("Global asset " + assetId + " in project " + projectId + " is already up-to-date.");
        return false;
      }
    } catch (Exception e) {
      LOG.severe("Error syncing global asset " + assetId + " in project " + projectId + ": " + e.getMessage());
      throw new RuntimeException("Failed to sync global asset: " + e.getMessage());
    }
  }

  // New efficient relationship-based methods
  @Override
  public void addAssetToProject(String assetFileName, long projectId, boolean trackUsage) {
    String userId = userInfoProvider.getUserId();
    
    // Get the global asset
    StoredData.GlobalAssetData globalAssetData = storageIo.getGlobalAssetByFileName(userId, assetFileName);
    if (globalAssetData == null) {
      LOG.warning("Attempted to add non-existent global asset: " + assetFileName + " for user " + userId);
      throw new RuntimeException("Global asset not found: " + assetFileName);
    }

    // Check if relationship already exists
    if (storageIo.getProjectGlobalAssetRelation(projectId, assetFileName, userId) != null) {
      LOG.info("Global asset " + assetFileName + " already exists in project " + projectId);
      return;
    }

    // Construct the project file path
    String projectFileName = "assets/_global_/";
    if (globalAssetData.folder != null && !globalAssetData.folder.isEmpty()) {
      projectFileName += globalAssetData.folder + "/";
    }
    projectFileName += globalAssetData.fileName;

    try {
      // Add the source file to the project
      storageIo.addSourceFilesToProject(userId, projectId, true, projectFileName);
      
      // Upload the content to the project
      byte[] globalAssetContent = storageIo.downloadRawGlobalAsset(userId, assetFileName);
      storageIo.uploadRawFileForce(projectId, projectFileName, userId, globalAssetContent);

      // Create the relationship record
      storageIo.addProjectGlobalAssetRelation(projectId, assetFileName, userId, trackUsage, projectFileName);

      LOG.info("Successfully added global asset " + assetFileName + " to project " + projectId + ", tracked: " + trackUsage);
    } catch (Exception e) {
      LOG.severe("Error adding global asset " + assetFileName + " to project " + projectId + ": " + e.getMessage());
      throw new RuntimeException("Failed to add global asset: " + e.getMessage());
    }
  }

  @Override
  public void removeAssetFromProject(String assetFileName, long projectId) {
    String userId = userInfoProvider.getUserId();
    
    try {
      // Get the relationship to find the local path
      StoredData.ProjectGlobalAssetData relation = storageIo.getProjectGlobalAssetRelation(projectId, assetFileName, userId);
      if (relation == null) {
        LOG.warning("Attempted to remove non-existent asset relationship: " + assetFileName + " from project " + projectId);
        return;
      }

      // Remove the file from the project
      storageIo.deleteFile(userId, projectId, relation.localAssetPath);
      
      // Remove the relationship record
      storageIo.removeProjectGlobalAssetRelation(projectId, assetFileName, userId);

      LOG.info("Successfully removed global asset " + assetFileName + " from project " + projectId);
    } catch (Exception e) {
      LOG.severe("Error removing global asset " + assetFileName + " from project " + projectId + ": " + e.getMessage());
      throw new RuntimeException("Failed to remove global asset: " + e.getMessage());
    }
  }

  @Override
  public List<GlobalAsset> getProjectGlobalAssets(long projectId) {
    String userId = userInfoProvider.getUserId();
    List<StoredData.ProjectGlobalAssetData> relations = storageIo.getProjectGlobalAssetRelations(projectId);
    List<GlobalAsset> globalAssets = new ArrayList<>();
    
    for (StoredData.ProjectGlobalAssetData relation : relations) {
      if (relation.globalAssetUserId.equals(userId)) {
        StoredData.GlobalAssetData globalAssetData = storageIo.getGlobalAssetByFileName(userId, relation.globalAssetFileName);
        if (globalAssetData != null) {
          globalAssets.add(new GlobalAsset(userId, globalAssetData.fileName, globalAssetData.folder, 
                                          globalAssetData.timestamp, new ArrayList<String>()));
        }
      }
    }
    
    return globalAssets;
  }

  @Override
  public List<Long> getProjectsUsingAsset(String assetFileName) {
    String userId = userInfoProvider.getUserId();
    return storageIo.getProjectsUsingGlobalAsset(assetFileName, userId);
  }

  @Override
  public boolean syncProjectGlobalAsset(String assetFileName, long projectId) {
    String userId = userInfoProvider.getUserId();
    
    // Get the relationship
    StoredData.ProjectGlobalAssetData relation = storageIo.getProjectGlobalAssetRelation(projectId, assetFileName, userId);
    if (relation == null || !relation.trackUsage) {
      LOG.info("Asset " + assetFileName + " in project " + projectId + " is not tracked for syncing");
      return false;
    }

    // Get the current global asset
    StoredData.GlobalAssetData globalAssetData = storageIo.getGlobalAssetByFileName(userId, assetFileName);
    if (globalAssetData == null) {
      LOG.warning("Global asset " + assetFileName + " no longer exists");
      return false;
    }

    // Check if sync is needed
    if (globalAssetData.timestamp <= relation.syncedTimestamp) {
      LOG.info("Global asset " + assetFileName + " in project " + projectId + " is already up-to-date");
      return false;
    }

    try {
      // Update the project file with new content
      byte[] globalAssetContent = storageIo.downloadRawGlobalAsset(userId, assetFileName);
      storageIo.uploadRawFileForce(projectId, relation.localAssetPath, userId, globalAssetContent);
      
      // Update the sync timestamp
      storageIo.updateProjectGlobalAssetSyncTimestamp(projectId, assetFileName, userId, globalAssetData.timestamp);

      LOG.info("Successfully synced global asset " + assetFileName + " in project " + projectId);
      return true;
    } catch (Exception e) {
      LOG.severe("Error syncing global asset " + assetFileName + " in project " + projectId + ": " + e.getMessage());
      throw new RuntimeException("Failed to sync global asset: " + e.getMessage());
    }
  }

  @Override
  public void bulkAddAssetsToProject(List<String> assetFileNames, long projectId, boolean trackUsage) {
    String userId = userInfoProvider.getUserId();
    int successCount = 0;
    int errorCount = 0;
    
    for (String assetFileName : assetFileNames) {
      try {
        addAssetToProject(assetFileName, projectId, trackUsage);
        successCount++;
      } catch (Exception e) {
        errorCount++;
        LOG.warning("Failed to add asset " + assetFileName + " to project " + projectId + ": " + e.getMessage());
      }
    }
    
    LOG.info("Bulk add completed for project " + projectId + ": " + successCount + " successful, " + errorCount + " errors");
    
    if (errorCount > 0) {
      throw new RuntimeException("Bulk add completed with " + errorCount + " errors. " + successCount + " assets were added successfully.");
    }
  }

  @Override
  public boolean assetExists(String fileName) {
    String userId = userInfoProvider.getUserId();
    StoredData.GlobalAssetData globalAssetData = storageIo.getGlobalAssetByFileName(userId, fileName);
    return globalAssetData != null;
  }

  @Override
  public AssetConflictInfo getAssetConflictInfo(String fileName) {
    String userId = userInfoProvider.getUserId();
    
    // Get the existing asset
    StoredData.GlobalAssetData globalAssetData = storageIo.getGlobalAssetByFileName(userId, fileName);
    if (globalAssetData == null) {
      return null; // No conflict if asset doesn't exist
    }
    
    // Convert to GlobalAsset DTO
    GlobalAsset existingAsset = new GlobalAsset(userId, globalAssetData.fileName, 
                                                globalAssetData.folder, globalAssetData.timestamp, 
                                                new ArrayList<String>());
    
    // Get affected projects
    List<AssetConflictInfo.ProjectInfo> affectedProjects = new ArrayList<>();
    List<Long> projectIds = getProjectsUsingAsset(fileName);
    
    for (Long projectId : projectIds) {
      try {
        // Get project name using the available method
        String projectName = storageIo.getProjectName(userId, projectId);
        if (projectName != null) {
          // Get the relationship info to check if it's tracked
          StoredData.ProjectGlobalAssetData relationData = 
              storageIo.getProjectGlobalAssetRelation(projectId, fileName, userId);
          
          boolean isTracked = (relationData != null && relationData.trackUsage);
          long lastSyncTime = (relationData != null) ? relationData.syncedTimestamp : 0;
          
          AssetConflictInfo.ProjectInfo projectInfo = new AssetConflictInfo.ProjectInfo(
              projectId, projectName, isTracked, lastSyncTime);
          
          affectedProjects.add(projectInfo);
        }
      } catch (Exception e) {
        LOG.warning("Error getting project info for project " + projectId + ": " + e.getMessage());
      }
    }
    
    return new AssetConflictInfo(existingAsset, affectedProjects, 
                                affectedProjects.size(), globalAssetData.timestamp);
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
        // Download the actual content (handles both datastore and GCS storage)
        byte[] content = storageIo.downloadRawGlobalAsset(userId, fileName);
        if (content != null) {
          resp.setStatus(HttpServletResponse.SC_OK);
          resp.setHeader("Content-Disposition", "attachment; filename=\"" + storedAsset.fileName + "\"");
          resp.setContentType(storedAsset.mimeType != null ? storedAsset.mimeType : "application/octet-stream");
          resp.getOutputStream().write(content);
        } else {
          resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          resp.getWriter().write("Error downloading global asset content: " + fileName);
        }
      } else {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.getWriter().write("Global asset not found: " + fileName);
      }
    } else {
      super.doGet(req, resp);
    }
  }
}