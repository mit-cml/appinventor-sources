package com.google.appinventor.shared.rpc.globalasset;

import com.google.appinventor.shared.rpc.project.GlobalAsset;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;

public interface GlobalAssetServiceAsync {
  void getGlobalAssets(AsyncCallback<List<GlobalAsset>> callback);
  void deleteGlobalAsset(String fileName, AsyncCallback<Void> callback);
  void linkGlobalAssetToProject(long projectId, String globalAssetId, long timestamp, AsyncCallback<Void> callback);
  void isGlobalAssetUpdated(String globalAssetId, long currentTimestamp, AsyncCallback<Boolean> callback);
  void getGlobalAsset(String fileName, AsyncCallback<GlobalAsset> callback);
  void uploadGlobalAsset(String name, String type, byte[] content, List<String> tags, String folder, AsyncCallback<Void> callback);
  void updateGlobalAsset(String id, String name, List<String> tags, String folder, AsyncCallback<Void> callback);
  void updateGlobalAssetFolder(String assetId, String folder, AsyncCallback<Void> callback);
  void importAssetIntoProject(String assetId, String projectId, boolean trackUsage, AsyncCallback<Void> callback);
  void syncGlobalAsset(String assetId, String projectId, AsyncCallback<Boolean> callback);
  
  // New efficient relationship-based methods
  void addAssetToProject(String assetFileName, long projectId, boolean trackUsage, AsyncCallback<Void> callback);
  void removeAssetFromProject(String assetFileName, long projectId, AsyncCallback<Void> callback);
  void getProjectGlobalAssets(long projectId, AsyncCallback<List<GlobalAsset>> callback);
  void getProjectsUsingAsset(String assetFileName, AsyncCallback<List<Long>> callback);
  void syncProjectGlobalAsset(String assetFileName, long projectId, AsyncCallback<Boolean> callback);
  void bulkAddAssetsToProject(List<String> assetFileNames, long projectId, boolean trackUsage, AsyncCallback<Void> callback);
  
  // Asset conflict detection and impact analysis
  void assetExists(String fileName, AsyncCallback<Boolean> callback);
  void getAssetConflictInfo(String fileName, AsyncCallback<AssetConflictInfo> callback);
}
