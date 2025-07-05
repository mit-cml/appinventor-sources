package com.google.appinventor.shared.rpc.globalasset;

import com.google.appinventor.shared.rpc.project.GlobalAsset;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;

@RemoteServiceRelativePath("globalassets")
public interface GlobalAssetService extends RemoteService {
  List<GlobalAsset> getGlobalAssets();
  void deleteGlobalAsset(String fileName);
  void linkGlobalAssetToProject(long projectId, String globalAssetId, long timestamp);
  boolean isGlobalAssetUpdated(String globalAssetId, long currentTimestamp);
  GlobalAsset getGlobalAsset(String fileName);
}
