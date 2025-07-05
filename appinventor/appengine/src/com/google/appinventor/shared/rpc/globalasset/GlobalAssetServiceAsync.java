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
    }
