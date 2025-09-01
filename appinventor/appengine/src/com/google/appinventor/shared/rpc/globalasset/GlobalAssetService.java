package com.google.appinventor.shared.rpc.globalasset;

import com.google.appinventor.shared.rpc.project.GlobalAsset;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;

@RemoteServiceRelativePath("globalassets")
public interface GlobalAssetService extends RemoteService {
  List<GlobalAsset> getGlobalAssets();
  void deleteGlobalAsset(String fileName) throws RuntimeException;
  void linkGlobalAssetToProject(long projectId, String globalAssetId, long timestamp);
  boolean isGlobalAssetUpdated(String globalAssetId, long currentTimestamp);
  GlobalAsset getGlobalAsset(String fileName);
  void uploadGlobalAsset(String name, String type, byte[] content, List<String> tags, String folder);
  void updateGlobalAsset(String id, String name, List<String> tags, String folder);
  void updateGlobalAssetFolder(String assetId, String folder);
  void importAssetIntoProject(String assetId, String projectId, boolean trackUsage);
  boolean syncGlobalAsset(String assetId, String projectId);
  
  // New efficient relationship-based methods
  void addAssetToProject(String assetFileName, long projectId, boolean trackUsage);
  void removeAssetFromProject(String assetFileName, long projectId);
  List<GlobalAsset> getProjectGlobalAssets(long projectId);
  List<Long> getProjectsUsingAsset(String assetFileName);
  boolean syncProjectGlobalAsset(String assetFileName, long projectId);
  void bulkAddAssetsToProject(List<String> assetFileNames, long projectId, boolean trackUsage);
  
  // Asset conflict detection and impact analysis
  boolean assetExists(String fileName);
  AssetConflictInfo getAssetConflictInfo(String fileName);
}
